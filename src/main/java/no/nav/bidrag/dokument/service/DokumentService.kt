package no.nav.bidrag.dokument.service

import com.google.common.io.ByteSource
import io.micrometer.core.annotation.Timed
import no.nav.bidrag.dokument.BidragDokumentConfig
import no.nav.bidrag.dokument.consumer.BidragDokumentConsumer
import no.nav.bidrag.dokument.consumer.DokumentTilgangConsumer
import no.nav.bidrag.dokument.dto.DocumentProperties
import no.nav.bidrag.dokument.dto.DokumentArkivSystemDto
import no.nav.bidrag.dokument.dto.DokumentMetadata
import no.nav.bidrag.dokument.dto.DokumentRef
import no.nav.bidrag.dokument.dto.DokumentTilgangResponse
import no.nav.bidrag.dokument.dto.Kilde
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.io.RandomAccessReadBuffer
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import java.io.File
import java.io.IOException
import java.util.*
import java.util.stream.Collectors

@Service
class DokumentService(
    private val dokumentTilgangConsumer: DokumentTilgangConsumer,
    @Qualifier(BidragDokumentConfig.ARKIV_QUALIFIER) private val bidragArkivConsumer: BidragDokumentConsumer,
    @Qualifier(BidragDokumentConfig.MIDL_BREVLAGER_QUALIFIER) private val bidragJournalpostConsumer: BidragDokumentConsumer,
    @Qualifier(BidragDokumentConfig.FORSENDELSE_QUALIFIER) private val bidragForsendelseConsumer: BidragDokumentConsumer,
) {
    fun hentTilgangUrl(
        journalpostId: String?,
        dokumentreferanse: String?,
    ): DokumentTilgangResponse? {
        return dokumentTilgangConsumer.hentTilgangUrl(journalpostId, dokumentreferanse)
    }

    @Timed("hentDokumentMetadata")
    fun hentDokumentMetadata(dokumentRef: DokumentRef): List<DokumentMetadata> {
        return if (dokumentRef.erForKilde(Kilde.MIDLERTIDLIG_BREVLAGER)) {
            bidragJournalpostConsumer.hentDokumentMetadata(
                dokumentRef.journalpostId,
                dokumentRef.dokumentId,
            )
        } else if (dokumentRef.erForKilde(Kilde.FORSENDELSE)) {
            bidragForsendelseConsumer.hentDokumentMetadata(
                dokumentRef.journalpostId,
                dokumentRef.dokumentId,
            )
        } else {
            bidragArkivConsumer.hentDokumentMetadata(
                dokumentRef.journalpostId,
                dokumentRef.dokumentId,
            )
        }
    }

    @Timed("hentDokument")
    fun hentDokument(
        _dokumentRef: DokumentRef,
        documentProperties: DocumentProperties,
    ): ResponseEntity<ByteArray> {
        val dokumentRef = hentDokumentRefMedRiktigKilde(_dokumentRef)
        if (!dokumentRef.hasDokumentId() && !dokumentRef.erForKilde(Kilde.MIDLERTIDLIG_BREVLAGER)) {
            val dokumentReferanser = hentAlleJournalpostDokumentReferanser(dokumentRef)
            return hentDokumenterData(dokumentReferanser, documentProperties)
        }
        val response = hentDokumentData(dokumentRef)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, response.headers.contentDisposition.toString())
            .body(response.body?.let { PDFDokumentProcessor().process(it, documentProperties) })
    }

    @Timed("hentDokumenter")
    fun hentDokumenter(
        dokumenterString: List<String>,
        documentProperties: DocumentProperties,
    ): ResponseEntity<ByteArray> {
        val dokumenter = parseDokumentString(dokumenterString)
        return hentDokumenterData(dokumenter, documentProperties)
    }

    private fun hentDokumentData(dokument: DokumentRef): ResponseEntity<ByteArray> {
        return if (dokument.erForKilde(Kilde.MIDLERTIDLIG_BREVLAGER)) {
            bidragJournalpostConsumer.hentDokument(dokument.journalpostId, dokument.dokumentId)
        } else if (dokument.erForKilde(Kilde.FORSENDELSE)) {
            bidragForsendelseConsumer.hentDokument(dokument.journalpostId, dokument.dokumentId)
        } else {
            bidragArkivConsumer.hentDokument(dokument.journalpostId, dokument.dokumentId)
        }
    }

    private fun hentDokumentRefMedRiktigKilde(dokumentRef: DokumentRef): DokumentRef {
        return if (dokumentRef.erForKilde(Kilde.FORSENDELSE) && dokumentRef.hasDokumentId()) {
            hentDokumentMetadata(dokumentRef)
                .firstOrNull()
                ?.let { mapDokumentTilDokumentRef(it) } ?: dokumentRef
        } else {
            dokumentRef
        }
    }

    private fun hentDokumenterData(
        dokumentRefList: List<DokumentRef>,
        documentProperties: DocumentProperties,
    ): ResponseEntity<ByteArray> {
        return try {
            val dokumentByte = hentOgMergeAlleDokumenter(dokumentRefList, documentProperties)
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=dokumenter_sammenslatt.pdf",
                )
                .body(dokumentByte)
        } catch (e: Exception) {
            LOGGER.error("Det skjedde en feil ved henting av dokumenter {}", dokumentRefList, e)
            if (e is HttpStatusCodeException) {
                ResponseEntity.status(e.statusCode).build()
            } else {
                ResponseEntity.internalServerError().build()
            }
        }
    }

    @Throws(IOException::class)
    private fun hentOgMergeAlleDokumenter(
        dokumentList: List<DokumentRef>,
        documentProperties: DocumentProperties,
    ): ByteArray? {
        documentProperties.numberOfDocuments = dokumentList.size
        if (dokumentList.size == 1) {
            return hentDokument(dokumentList[0], documentProperties).body
        }
        val mergedFileName = "/tmp/" + UUID.randomUUID()
        val mergedDocument = PDFMergerUtility()
        mergedDocument.destinationFileName = mergedFileName
        for (dokument in dokumentList) {
            val dokumentResponse = hentDokument(
                dokument,
                DocumentProperties(documentProperties, dokumentList.indexOf(dokument)),
            )
            val dokumentInputStream = ByteSource.wrap(dokumentResponse.body!!).openStream()
            mergedDocument.addSource(RandomAccessReadBuffer(dokumentInputStream))
            dokumentInputStream.close()
        }
        mergedDocument.mergeDocuments(MemoryUsageSetting.setupTempFileOnly().streamCache)
        return getByteDataAndDeleteFile(mergedFileName)
    }

    @Throws(IOException::class)
    private fun getByteDataAndDeleteFile(filename: String): ByteArray {
        val file = File(filename)
        return try {
            PDFDokumentProcessor.fileToByte(File(filename))
        } finally {
            file.delete()
        }
    }

    private fun hentAlleJournalpostDokumentReferanser(dokumentRef: DokumentRef): List<DokumentRef> {
        val metadataList = hentDokumentMetadata(dokumentRef)
        return metadataList.map { dokumentMetadata -> mapDokumentTilDokumentRef(dokumentMetadata) }
    }

    private fun mapDokumentTilDokumentRef(dokument: DokumentMetadata): DokumentRef {
        return DokumentRef(
            journalpostId = dokument.journalpostId,
            dokumentId = dokument.dokumentreferanse,
            kilde = when (dokument.arkivsystem) {
                DokumentArkivSystemDto.MIDLERTIDLIG_BREVLAGER -> Kilde.MIDLERTIDLIG_BREVLAGER
                DokumentArkivSystemDto.JOARK -> Kilde.JOARK
                DokumentArkivSystemDto.BIDRAG -> Kilde.FORSENDELSE
                else -> null
            },
        )
    }

    private fun parseDokumentString(dokumenterString: List<String>): List<DokumentRef> {
        return dokumenterString.stream().map { str: String? -> DokumentRef.parseFromString(str!!) }
            .collect(Collectors.toList())
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DokumentService::class.java)
    }
}
