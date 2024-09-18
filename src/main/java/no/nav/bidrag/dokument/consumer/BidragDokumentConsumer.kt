package no.nav.bidrag.dokument.consumer

import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import no.nav.bidrag.commons.web.EnhetFilter
import no.nav.bidrag.commons.web.HttpResponse
import no.nav.bidrag.transport.dokument.AvvikType
import no.nav.bidrag.transport.dokument.Avvikshendelse
import no.nav.bidrag.transport.dokument.BehandleAvvikshendelseResponse
import no.nav.bidrag.transport.dokument.DistribuerJournalpostRequest
import no.nav.bidrag.transport.dokument.DistribuerJournalpostResponse
import no.nav.bidrag.transport.dokument.DistribusjonInfoDto
import no.nav.bidrag.transport.dokument.DokumentMetadata
import no.nav.bidrag.transport.dokument.EndreJournalpostCommand
import no.nav.bidrag.transport.dokument.JournalpostDto
import no.nav.bidrag.transport.dokument.JournalpostResponse
import no.nav.bidrag.transport.dokument.OpprettJournalpostRequest
import no.nav.bidrag.transport.dokument.OpprettJournalpostResponse
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

private val log = KotlinLogging.logger {}

class BidragDokumentConsumer(
    private val name: String,
    private val restTemplate: RestTemplate,
    private val rootUri: String,
    private val metricsRegistry: MeterRegistry,
) {
    fun finnAvvik(
        saksnummer: String?,
        journalpostId: String?,
    ): HttpResponse<List<AvvikType>> {
        val path: String =
            if (saksnummer != null) {
                String.format(PATH_AVVIK_PA_JOURNALPOST_MED_SAK_PARAM, journalpostId, saksnummer)
            } else {
                String.format(PATH_AVVIK_PA_JOURNALPOST, journalpostId)
            }
        val avviksResponse =
            restTemplate.exchange(path, HttpMethod.GET, null, typereferansenErListeMedAvvikstyper())
        return HttpResponse(avviksResponse)
    }

    fun behandleAvvik(
        enhetsnummer: String?,
        journalpostId: String?,
        avvikshendelse: Avvikshendelse?,
    ): HttpResponse<BehandleAvvikshendelseResponse> {
        val path = String.format("$PATH_JOURNALPOST_UTEN_SAK/avvik", journalpostId)
        val avviksResponse =
            restTemplate
                .exchange(
                    path,
                    HttpMethod.POST,
                    HttpEntity(avvikshendelse, createEnhetHeader(enhetsnummer)),
                    BehandleAvvikshendelseResponse::class.java,
                )
        return HttpResponse(avviksResponse)
    }

    fun hentJournalpost(
        saksnummer: String?,
        id: String?,
    ): HttpResponse<JournalpostResponse> {
        val url: String =
            if (saksnummer == null) {
                String.format(PATH_JOURNALPOST, id)
            } else {
                String.format(PATH_JOURNALPOST_MED_SAKPARAM, id, saksnummer)
            }
        val journalpostExchange =
            restTemplate.exchange(url, HttpMethod.GET, null, JournalpostResponse::class.java)
        return HttpResponse(journalpostExchange)
    }

    fun finnJournalposter(
        saksnummer: String?,
        fagomrade: List<String> = emptyList(),
    ): List<JournalpostDto> {
        val uriBuilder = UriComponentsBuilder.fromPath(String.format(PATH_JOURNAL, saksnummer))
        fagomrade.forEach { uriBuilder.queryParam(PARAM_FAGOMRADE, it) }
        val uri = uriBuilder.toUriString()
        log.info { "Henter journalposter for sak $saksnummer" }
        val timer = metricsRegistry.timer("finnJournalposter", "service", name)
        return try {
            val journalposterFraArkiv =
                timer.recordCallable {
                    restTemplate
                        .exchange(uri, HttpMethod.GET, null, typereferansenErListeMedJournalposter())
                }!!
            journalposterFraArkiv.body ?: emptyList()
        } catch (e: HttpStatusCodeException) {
            log.error(e) {
                "Det skjedde en feil ved henting av journal for sak $saksnummer og fagområder ${
                    fagomrade.joinToString(
                        ",",
                    )
                } fra url $rootUri/$uri"
            }
            if (e.statusCode == HttpStatus.NOT_FOUND) {
                emptyList()
            } else {
                throw e
            }
        }
    }

    fun endre(
        enhet: String?,
        endreJournalpostCommand: EndreJournalpostCommand,
    ): HttpResponse<Void> {
        val path = String.format(PATH_JOURNALPOST_UTEN_SAK, endreJournalpostCommand.journalpostId)
        val endretJournalpostResponse =
            restTemplate
                .exchange(
                    path,
                    HttpMethod.PATCH,
                    HttpEntity(endreJournalpostCommand, createEnhetHeader(enhet)),
                    Void::class.java,
                )
        return HttpResponse(endretJournalpostResponse)
    }

    fun opprett(opprettJournalpostRequest: OpprettJournalpostRequest): HttpResponse<OpprettJournalpostResponse> {
        val endretJournalpostResponse =
            restTemplate
                .exchange(
                    PATH_OPPRETT_JOURNALPOST,
                    HttpMethod.POST,
                    HttpEntity(opprettJournalpostRequest),
                    OpprettJournalpostResponse::class.java,
                )
        return HttpResponse(endretJournalpostResponse)
    }

    fun distribuerJournalpost(
        journalpostId: String?,
        batchId: String?,
        distribuerJournalpostRequest: DistribuerJournalpostRequest,
    ): HttpResponse<DistribuerJournalpostResponse> {
        var uriBuilder =
            UriComponentsBuilder.fromPath(String.format(PATH_DISTRIBUER, journalpostId))
        if (!batchId.isNullOrEmpty()) {
            uriBuilder = uriBuilder.queryParam(PARAM_BATCHID, batchId)
        }
        val uri = uriBuilder.toUriString()
        val distribuerJournalpostResponse =
            restTemplate
                .exchange(
                    uri,
                    HttpMethod.POST,
                    HttpEntity(distribuerJournalpostRequest),
                    DistribuerJournalpostResponse::class.java,
                )
        return HttpResponse(distribuerJournalpostResponse)
    }

    fun kanDistribuereJournalpost(journalpostId: String?): HttpResponse<Void> {
        val path = String.format(PATH_DISTRIBUER_ENABLED, journalpostId)
        val distribuerJournalpostResponse =
            restTemplate.exchange(path, HttpMethod.GET, null, Void::class.java)
        return HttpResponse(distribuerJournalpostResponse)
    }

    fun hentDistribusjonsInfo(journalpostId: String): DistribusjonInfoDto? {
        val path = String.format(PATH_HENT_DIST_INFO, journalpostId)
        return restTemplate.exchange(
            path,
            HttpMethod.GET,
            null,
            DistribusjonInfoDto::class.java,
        ).body
    }

    fun hentDokument(
        journalpostId: String?,
        dokumentreferanse: String?,
    ): ResponseEntity<ByteArray> {
        if (journalpostId.isNullOrEmpty()) return hentDokument(dokumentreferanse)
        val dokumentReferanseUrl =
            if (!dokumentreferanse.isNullOrEmpty()) "/$dokumentreferanse" else ""
        val dokumentUrl = String.format(PATH_HENT_DOKUMENT, journalpostId) + dokumentReferanseUrl
        return restTemplate.exchange(
            dokumentUrl,
            HttpMethod.GET,
            HttpEntity.EMPTY,
            ByteArray::class.java,
        )
    }

    fun hentDokumentMetadata(
        journalpostId: String?,
        dokumentreferanse: String?,
    ): List<DokumentMetadata> {
        if (journalpostId.isNullOrEmpty()) {
            return hentDokumentMetadata(dokumentreferanse)
                ?: emptyList()
        }
        val dokumentReferanseUrl =
            if (!dokumentreferanse.isNullOrEmpty()) "/$dokumentreferanse" else ""
        val dokumentUrl = String.format(PATH_HENT_DOKUMENT, journalpostId) + dokumentReferanseUrl
        return restTemplate.exchange(
            dokumentUrl,
            HttpMethod.OPTIONS,
            HttpEntity.EMPTY,
            object : ParameterizedTypeReference<List<DokumentMetadata>>() {},
        ).body
            ?: emptyList()
    }

    fun hentDokumentMetadata(dokumentreferanse: String?): List<DokumentMetadata>? {
        val dokumentUrl = String.format(PATH_HENT_DOKUMENT_REFERANSE, dokumentreferanse)
        return restTemplate.exchange(
            dokumentUrl,
            HttpMethod.OPTIONS,
            HttpEntity.EMPTY,
            object : ParameterizedTypeReference<List<DokumentMetadata>>() {},
        ).body
    }

    fun hentDokument(dokumentreferanse: String?): ResponseEntity<ByteArray> {
        val dokumentUrl = String.format(PATH_HENT_DOKUMENT_REFERANSE, dokumentreferanse)
        return restTemplate.exchange(
            dokumentUrl,
            HttpMethod.GET,
            HttpEntity.EMPTY,
            ByteArray::class.java,
        )
    }

    fun erFerdigstilt(dokumentreferanse: String): ResponseEntity<Boolean> {
        val dokumentUrl = String.format(PATH_HENT_DOKUMENT_ER_FERDIGSTILT, dokumentreferanse)
        return restTemplate.exchange(
            dokumentUrl,
            HttpMethod.GET,
            HttpEntity.EMPTY,
            Boolean::class.java,
        )
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
        private const val PATH_HENT_DIST_INFO = "/journal/distribuer/info/%s"
        private const val PATH_JOURNALPOST_MED_SAKPARAM = "/journal/%s?saksnummer=%s"
        private const val PARAM_FAGOMRADE = "fagomrade"
        private const val PARAM_BATCHID = "batchId"
        private const val PARAM_SAKSNUMMER = "saksnummer"
        const val PATH_AVVIK_PA_JOURNALPOST_MED_SAK_PARAM =
            "/journal/%s/avvik?" + PARAM_SAKSNUMMER + "=%s"
        const val PATH_AVVIK_PA_JOURNALPOST = "/journal/%s/avvik"
        const val PATH_HENT_DOKUMENT = "/dokument/%s"
        const val PATH_HENT_DOKUMENT_REFERANSE = "/dokumentreferanse/%s"
        const val PATH_HENT_DOKUMENT_ER_FERDIGSTILT = "/dokumentreferanse/%s/erFerdigstilt"

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
