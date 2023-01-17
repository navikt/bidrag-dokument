package no.nav.bidrag.dokument.consumer

import no.nav.bidrag.dokument.dto.DokumentTilgangResponse
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate

class DokumentTilgangConsumer(private val restTemplate: RestTemplate) {
    fun hentTilgangUrl(journalpostId: String?, dokumentreferanse: String?): DokumentTilgangResponse? {
        if (journalpostId.isNullOrEmpty()){
            return restTemplate
                .exchange(
                    String.format(PATH_DOKUMENT_TILGANG_DOKREF, dokumentreferanse),
                    HttpMethod.GET,
                    null,
                    DokumentTilgangResponse::class.java
                ).body
        }
        return restTemplate
            .exchange(
                String.format(PATH_DOKUMENT_TILGANG, journalpostId, dokumentreferanse),
                HttpMethod.GET,
                null,
                DokumentTilgangResponse::class.java
            ).body
    }

    companion object {
        const val PATH_DOKUMENT_TILGANG = "/tilgang/%s/%s"
        const val PATH_DOKUMENT_TILGANG_DOKREF = "/tilgang/dokumentreferanse/%s"
        const val PATH_HENT_DOKUMENT = "/dokument/%s"
    }
}