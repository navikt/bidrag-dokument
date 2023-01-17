package no.nav.bidrag.dokument.controller

import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Parameter
import no.nav.bidrag.dokument.dto.DocumentProperties
import no.nav.bidrag.dokument.dto.DokumentMetadata
import no.nav.bidrag.dokument.dto.DokumentRef
import no.nav.bidrag.dokument.dto.DokumentTilgangResponse
import no.nav.bidrag.dokument.service.DokumentService
import no.nav.bidrag.dokument.service.PDFDokumentProcessor
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
@Timed
class DokumentController(private val dokumentService: DokumentService) {
    @GetMapping("/tilgang/{journalpostId}/{dokumentreferanse}")
    fun giTilgangTilDokument(
        @PathVariable journalpostId: String?,
        @PathVariable dokumentreferanse: String?
    ): DokumentTilgangResponse? {
        val dokumentUrlResponse = dokumentService.hentTilgangUrl(journalpostId, dokumentreferanse)
        LOGGER.info("Gitt tilgang til dokument $dokumentreferanse")
        return dokumentUrlResponse
    }

    @RequestMapping(*["/dokument/{journalpostId}/{dokumentreferanse}", "/dokument/{journalpostId}"], method = [RequestMethod.OPTIONS])
    fun hentDokumentMetadata(
        @PathVariable journalpostId: String,
        @PathVariable(required = false) dokumentreferanse: String?,
    ): List<DokumentMetadata> {
        LOGGER.info("Henter dokument metadata med journalpostId=$journalpostId og dokumentreferanse=$dokumentreferanse")
        val dokument = DokumentRef(journalpostId, dokumentreferanse, null)
        return dokumentService.hentDokumentMetadata(dokument)
    }

    @GetMapping(*["/dokument/{journalpostId}/{dokumentreferanse}", "/dokument/{journalpostId}"])
    fun hentDokument(
        @PathVariable journalpostId: String,
        @PathVariable(required = false) dokumentreferanse: String?,
        @RequestParam(required = false) resizeToA4: Boolean,
        @RequestParam(required = false, defaultValue = "true") optimizeForPrint: Boolean
    ): ResponseEntity<ByteArray> {
        LOGGER.info("Henter dokument med journalpostId=$journalpostId og dokumentreferanse=$dokumentreferanse, resizeToA4=$resizeToA4")
        val dokument = DokumentRef(journalpostId, dokumentreferanse, null)
        val response = dokumentService.hentDokument(dokument, DocumentProperties(resizeToA4, optimizeForPrint))

        response.body
            ?.also {
                LOGGER.info("Hentet dokument med journalpostId=$journalpostId og dokumentreferanse=$dokumentreferanse med total størrelse ${PDFDokumentProcessor.bytesIntoHumanReadable(it.size.toLong())}")
            }

        return response
    }

    @GetMapping(*["/dokument"])
    fun hentDokumenter(
        @Parameter(name = "dokument", description = "Liste med dokumenter formatert <Kilde>-<journalpostId>:<dokumentReferanse>")
        @RequestParam(name = "dokument") dokumentreferanseList: List<String>,
        @RequestParam(required = false, defaultValue = "true") optimizeForPrint: Boolean,
        @RequestParam(required = false) resizeToA4: Boolean
    ): ResponseEntity<ByteArray> {
        LOGGER.info("Henter dokumenter $dokumentreferanseList med resizeToA4=$resizeToA4, optimizeForPrint=$optimizeForPrint")
        val response = dokumentService.hentDokumenter(dokumentreferanseList, DocumentProperties(resizeToA4, optimizeForPrint))

        response.body?.also { LOGGER.info("Hentet dokumenter $dokumentreferanseList med total størrelse ${PDFDokumentProcessor.bytesIntoHumanReadable(it.size.toLong())}")}
        return response
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DokumentController::class.java)
    }
}