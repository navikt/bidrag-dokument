package no.nav.bidrag.dokument.consumer;

import static no.nav.bidrag.dokument.BidragDokumentConfig.KLIENTNAVN_BIDRAG_DOKUMENT_JOURNALPOST;

import no.nav.bidrag.commons.web.HttpResponse;
import no.nav.bidrag.dokument.dto.DokumentTilgangResponse;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class DokumentConsumer {

  private final ConsumerTarget consumerTarget;

  public DokumentConsumer(ConsumerTarget consumerTarget) {
    this.consumerTarget = consumerTarget;
  }

  private RestTemplate henteRestTemplateForToken() {
    return consumerTarget
        .getRestTemplateProvider()
        .provideRestTemplate(KLIENTNAVN_BIDRAG_DOKUMENT_JOURNALPOST, consumerTarget.getBaseUrl());
  }

  public HttpResponse<DokumentTilgangResponse> hentTilgangUrl(
      String journalpostId, String dokumentreferanse) {
    var response =
        henteRestTemplateForToken()
            .exchange(
                String.format("/tilgang/%s/%s", journalpostId, dokumentreferanse),
                HttpMethod.GET,
                null,
                DokumentTilgangResponse.class);

    return new HttpResponse<>(response);
  }
}
