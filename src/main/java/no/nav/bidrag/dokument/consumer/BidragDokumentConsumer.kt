package no.nav.bidrag.dokument.consumer

import no.nav.bidrag.commons.web.EnhetFilter
import no.nav.bidrag.commons.web.HttpResponse
import no.nav.bidrag.dokument.dto.AvvikType
import no.nav.bidrag.dokument.dto.Avvikshendelse
import no.nav.bidrag.dokument.dto.BehandleAvvikshendelseResponse
import no.nav.bidrag.dokument.dto.DistribuerJournalpostRequest
import no.nav.bidrag.dokument.dto.DistribuerJournalpostResponse
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand
import no.nav.bidrag.dokument.dto.JournalpostDto
import no.nav.bidrag.dokument.dto.JournalpostResponse
import no.nav.bidrag.dokument.dto.OpprettJournalpostRequest
import no.nav.bidrag.dokument.dto.OpprettJournalpostResponse
import org.apache.logging.log4j.util.Strings
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.Optional

class BidragDokumentConsumer(private val restTemplate: RestTemplate) {
    fun finnAvvik(saksnummer: String?, journalpostId: String?): HttpResponse<List<AvvikType>> {
        val path: String = if (saksnummer != null) String.format(PATH_AVVIK_PA_JOURNALPOST_MED_SAK_PARAM, journalpostId, saksnummer)
        else String.format(PATH_AVVIK_PA_JOURNALPOST, journalpostId)
        val avviksResponse = restTemplate.exchange(path, HttpMethod.GET, null, typereferansenErListeMedAvvikstyper())
        return HttpResponse(avviksResponse)
    }

    fun behandleAvvik(enhetsnummer: String?, journalpostId: String?, avvikshendelse: Avvikshendelse?): HttpResponse<BehandleAvvikshendelseResponse> {
        val path = String.format("$PATH_JOURNALPOST_UTEN_SAK/avvik", journalpostId)
        val avviksResponse = restTemplate
            .exchange(path, HttpMethod.POST, HttpEntity(avvikshendelse, createEnhetHeader(enhetsnummer)), BehandleAvvikshendelseResponse::class.java)
        return HttpResponse(avviksResponse)
    }

    fun hentJournalpost(saksnummer: String?, id: String?): HttpResponse<JournalpostResponse> {
        val url: String = if (saksnummer == null)String.format(PATH_JOURNALPOST, id)
        else String.format(PATH_JOURNALPOST_MED_SAKPARAM, id, saksnummer)
        val journalpostExchange = restTemplate.exchange(url, HttpMethod.GET, null, JournalpostResponse::class.java)
        return HttpResponse(journalpostExchange)
    }

    fun finnJournalposter(saksnummer: String?, fagomrade: String?): List<JournalpostDto> {
        val uri = UriComponentsBuilder.fromPath(String.format(PATH_JOURNAL, saksnummer)).queryParam(PARAM_FAGOMRADE, fagomrade).toUriString()
        val journalposterFraArkiv = restTemplate
            .exchange(uri, HttpMethod.GET, null, typereferansenErListeMedJournalposter())
        return Optional.ofNullable(journalposterFraArkiv.body).orElse(emptyList())
    }

    fun endre(enhet: String?, endreJournalpostCommand: EndreJournalpostCommand): HttpResponse<Void> {
        val path = String.format(PATH_JOURNALPOST_UTEN_SAK, endreJournalpostCommand.journalpostId)
        val endretJournalpostResponse = restTemplate
            .exchange(path, HttpMethod.PATCH, HttpEntity(endreJournalpostCommand, createEnhetHeader(enhet)), Void::class.java)
        return HttpResponse(endretJournalpostResponse)
    }

    fun opprett(opprettJournalpostRequest: OpprettJournalpostRequest): HttpResponse<OpprettJournalpostResponse> {
        val endretJournalpostResponse = restTemplate
            .exchange(PATH_OPPRETT_JOURNALPOST, HttpMethod.POST, HttpEntity(opprettJournalpostRequest), OpprettJournalpostResponse::class.java)
        return HttpResponse(endretJournalpostResponse)
    }

    fun distribuerJournalpost(
        journalpostId: String?,
        batchId: String?,
        distribuerJournalpostRequest: DistribuerJournalpostRequest
    ): HttpResponse<DistribuerJournalpostResponse> {
        var uriBuilder = UriComponentsBuilder.fromPath(String.format(PATH_DISTRIBUER, journalpostId))
        if (!batchId.isNullOrEmpty()) {
            uriBuilder = uriBuilder.queryParam(PARAM_BATCHID, batchId)
        }
        val uri = uriBuilder.toUriString()
        val distribuerJournalpostResponse = restTemplate
            .exchange(uri, HttpMethod.POST, HttpEntity(distribuerJournalpostRequest), DistribuerJournalpostResponse::class.java)
        return HttpResponse(distribuerJournalpostResponse)
    }

    fun kanDistribuereJournalpost(journalpostId: String?): HttpResponse<Void> {
        val path = String.format(PATH_DISTRIBUER_ENABLED, journalpostId)
        val distribuerJournalpostResponse = restTemplate.exchange(path, HttpMethod.GET, null, Void::class.java)
        return HttpResponse(distribuerJournalpostResponse)
    }

    fun hentDokument(journalpostId: String?, dokumentreferanse: String?): ResponseEntity<ByteArray> {
        val dokumentReferanseUrl = if (!dokumentreferanse.isNullOrEmpty()) "/$dokumentreferanse" else ""
        val dokumentUrl = String.format(PATH_HENT_DOKUMENT, journalpostId) + dokumentReferanseUrl
        return restTemplate.exchange(dokumentUrl, HttpMethod.GET, HttpEntity.EMPTY, ByteArray::class.java)
    }

    private fun typereferansenErListeMedAvvikstyper(): ParameterizedTypeReference<List<AvvikType>> {
        return object : ParameterizedTypeReference<List<AvvikType>>() {}
    }

    companion object {
        private const val PATH_JOURNAL = "/sak/%s/journal"
        private const val PATH_OPPRETT_JOURNALPOST = "/journalpost"
        const val PATH_JOURNALPOST_UTEN_SAK = "/journal/%s"
        const val PATH_SAK_JOURNAL = "/sak/%s/journal"
        private const val PATH_JOURNALPOST = "/journal/%s"
        private const val PATH_DISTRIBUER = "/journal/distribuer/%s"
        private const val PATH_DISTRIBUER_ENABLED = "/journal/distribuer/%s/enabled"
        private const val PATH_JOURNALPOST_MED_SAKPARAM = "/journal/%s?saksnummer=%s"
        private const val PARAM_FAGOMRADE = "fagomrade"
        private const val PARAM_BATCHID = "batchId"
        private const val PARAM_SAKSNUMMER = "saksnummer"
        const val PATH_AVVIK_PA_JOURNALPOST_MED_SAK_PARAM = "/journal/%s/avvik?" + PARAM_SAKSNUMMER + "=%s"
        const val PATH_AVVIK_PA_JOURNALPOST = "/journal/%s/avvik"
        const val PATH_HENT_DOKUMENT = "/dokument/%s"
        private fun typereferansenErListeMedJournalposter(): ParameterizedTypeReference<List<JournalpostDto>> {
            return object : ParameterizedTypeReference<List<JournalpostDto>>() {}
        }

        @JvmStatic
        fun createEnhetHeader(enhet: String?): HttpHeaders {
            val header = HttpHeaders()
            header.add(EnhetFilter.X_ENHET_HEADER, enhet)
            return header
        }
    }
}