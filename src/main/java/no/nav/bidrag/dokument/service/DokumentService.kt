package no.nav.bidrag.dokument.service

import com.google.common.io.ByteSource
import io.micrometer.core.annotation.Timed
import no.nav.bidrag.commons.web.HttpResponse
import no.nav.bidrag.dokument.BidragDokumentConfig
import no.nav.bidrag.dokument.consumer.BidragDokumentConsumer
import no.nav.bidrag.dokument.consumer.DokumentTilgangConsumer
import no.nav.bidrag.dokument.dto.DocumentProperties
import no.nav.bidrag.dokument.dto.DokumentArkivSystemDto
import no.nav.bidrag.dokument.dto.DokumentDto
import no.nav.bidrag.dokument.dto.DokumentFormatDto
import no.nav.bidrag.dokument.dto.DokumentRef
import no.nav.bidrag.dokument.dto.DokumentStatusDto
import no.nav.bidrag.dokument.dto.DokumentTilgangResponse
import no.nav.bidrag.dokument.dto.Kilde
import no.nav.bidrag.dokument.dto.fantIkkeDokument
import no.nav.bidrag.dokument.dto.ÅpneDokumentMetadata
import org.apache.pdfbox.io.MemoryUsageSetting
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
import java.util.UUID
import java.util.stream.Collectors

@Service
class DokumentService(
    private val journalpostService: JournalpostService,
    private val dokumentTilgangConsumer: DokumentTilgangConsumer,
    @Qualifier(BidragDokumentConfig.ARKIV_QUALIFIER) private val bidragArkivConsumer: BidragDokumentConsumer,
    @Qualifier(BidragDokumentConfig.MIDL_BREVLAGER_QUALIFIER) private val bidragJournalpostConsumer: BidragDokumentConsumer,
    @Qualifier(BidragDokumentConfig.FORSENDELSE_QUALIFIER) private val bidragForsendelseConsumer: BidragDokumentConsumer
) {
    fun hentTilgangUrl(journalpostId: String?, dokumentreferanse: String?): HttpResponse<DokumentTilgangResponse> {
        return dokumentTilgangConsumer.hentTilgangUrl(journalpostId, dokumentreferanse)
    }

    @Timed("hentDokumentMetadata")
    fun hentDokumentMetadata(dokumentRef: DokumentRef): ÅpneDokumentMetadata {
       return hentDokumentMetaData(dokumentRef) ?: fantIkkeDokument("Fant ikke dokumentmetadata for $dokumentRef")
    }

    @Timed("hentDokument")
    fun hentDokument(dokumentRef: DokumentRef, documentProperties: DocumentProperties): ResponseEntity<ByteArray> {
        if (!dokumentRef.hasDokumentId() && !dokumentRef.erForKilde(Kilde.BIDRAG)) {
            val dokumentReferanser = hentAlleJournalpostDokumentReferanser(dokumentRef)
            return hentDokumenterData(dokumentReferanser, documentProperties)
        }
        val response = hentDokumentData(dokumentRef)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, response.headers.contentDisposition.toString())
            .body(PDFDokumentProcessor().process(response.body, documentProperties))
    }

    @Timed("hentDokumenter")
    fun hentDokumenter(dokumenterString: List<String>, documentProperties: DocumentProperties): ResponseEntity<ByteArray> {
        val dokumenter = parseDokumentString(dokumenterString)
        return hentDokumenterData(dokumenter, documentProperties)
    }

    private fun hentDokumentData(dokumentRef: DokumentRef): ResponseEntity<ByteArray> {
        val dokument = hentDokumentRefMedRiktigKilde(dokumentRef)
        return if (dokument.erForKilde(Kilde.BIDRAG)) bidragJournalpostConsumer.hentDokument(dokument.journalpostId, dokument.dokumentId)
        else if (dokument.erForKilde(Kilde.FORSENDELSE)) bidragForsendelseConsumer.hentDokument(dokument.journalpostId, dokument.dokumentId)
        else bidragArkivConsumer.hentDokument(dokument.journalpostId, dokument.dokumentId)
    }

    private fun hentDokumentRefMedRiktigKilde(dokumentRef: DokumentRef): DokumentRef {
       return if (dokumentRef.erForKilde(Kilde.FORSENDELSE)){
            val (journalpost) = journalpostService.hentJournalpost(dokumentRef, null).fetchBody().get()
            journalpost?.dokumenter?.find { jpDok -> jpDok.dokumentreferanse == dokumentRef.dokumentId }
                ?.let { mapDokumentTilDokumentRef(it, null) } ?: dokumentRef
        } else dokumentRef
    }

    private fun hentDokumentMetaData(dokumentRef: DokumentRef): ÅpneDokumentMetadata? {
        val dokument = hentDokumentRefMedRiktigKilde(dokumentRef)
        return if (dokument.erForKilde(Kilde.BIDRAG)) bidragJournalpostConsumer.hentDokumentMetadata(dokument.journalpostId, dokument.dokumentId)
        else if (dokument.erForKilde(Kilde.FORSENDELSE)) bidragForsendelseConsumer.hentDokumentMetadata(dokument.journalpostId, dokument.dokumentId)
        else ÅpneDokumentMetadata(format = DokumentFormatDto.PDF, status = DokumentStatusDto.FERDIGSTILT)
    }

    private fun hentDokumenterData(dokumentRefList: List<DokumentRef>, documentProperties: DocumentProperties): ResponseEntity<ByteArray> {
        return try {
            val dokumentByte = hentOgMergeAlleDokumenter(dokumentRefList, documentProperties)
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=dokumenter_sammenslatt.pdf")
                .body(dokumentByte)
        } catch (e: Exception) {
            LOGGER.error("Det skjedde en feil ved henting av dokumenter {}", dokumentRefList, e)
            if (e is HttpStatusCodeException) ResponseEntity.status(e.statusCode).build()
            else ResponseEntity.internalServerError().build()
        }
    }

    @Throws(IOException::class)
    private fun hentOgMergeAlleDokumenter(dokumentList: List<DokumentRef>, documentProperties: DocumentProperties): ByteArray? {
        documentProperties.numberOfDocuments = dokumentList.size
        if (dokumentList.size == 1) {
            return hentDokument(dokumentList[0], documentProperties).body
        }
        val mergedFileName = "/tmp/" + UUID.randomUUID()
        val mergedDocument = PDFMergerUtility()
        mergedDocument.destinationFileName = mergedFileName
        for (dokument in dokumentList) {
            val dokumentResponse = hentDokument(dokument, DocumentProperties(documentProperties, dokumentList.indexOf(dokument)))
            val dokumentInputStream = ByteSource.wrap(dokumentResponse.body!!).openStream()
            mergedDocument.addSource(dokumentInputStream)
            dokumentInputStream.close()
        }
        mergedDocument.mergeDocuments(MemoryUsageSetting.setupTempFileOnly())
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
        val (journalpost) = journalpostService.hentJournalpost(dokumentRef, null).fetchBody().get()
        return journalpost!!.dokumenter
            .stream()
            .map { dokument: DokumentDto -> mapDokumentTilDokumentRef(dokument, dokumentRef.journalpostId) }
            .collect(Collectors.toList())
    }

    val String?.erForsendelseId get() = this?.startsWith(Kilde.FORSENDELSE.prefix) ?: false
    private fun mapDokumentTilDokumentRef(dokument: DokumentDto, journalpostId: String?): DokumentRef {
        val dokumentJournalpostId = if (dokument.arkivSystem != DokumentArkivSystemDto.BIDRAG && journalpostId.erForsendelseId) dokument.journalpostId else dokument.journalpostId ?: journalpostId
        return DokumentRef(dokumentJournalpostId, dokumentId = dokument.dokumentreferanse, kilde = when(dokument.arkivSystem){
            DokumentArkivSystemDto.MIDLERTIDLIG_BREVLAGER -> Kilde.BIDRAG
            DokumentArkivSystemDto.JOARK -> Kilde.JOARK
            DokumentArkivSystemDto.BIDRAG -> Kilde.FORSENDELSE
            else -> null
        })
    }

    private fun parseDokumentString(dokumenterString: List<String>): List<DokumentRef> {
        return dokumenterString.stream().map { str: String? -> DokumentRef.parseFromString(str!!)}.collect(Collectors.toList())
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DokumentService::class.java)
    }
}