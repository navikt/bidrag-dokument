package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.commons.web.HttpResponse;
import no.nav.bidrag.dokument.dto.DokumentTilgangResponse;
import org.springframework.http.HttpMethod;

public class DokumentConsumer {

  public static final String PATH_DOKUMENT_TILGANG = "/tilgang/%s/%s";

  private final ConsumerTarget consumerTarget;

  public DokumentConsumer(ConsumerTarget consumerTarget) {
    this.consumerTarget = consumerTarget;
  }
  public HttpResponse<DokumentTilgangResponse> hentTilgangUrl(String journalpostId, String dokumentreferanse) {

    var response = consumerTarget.henteRestTemplateForIssuer()
        .exchange(String.format(PATH_DOKUMENT_TILGANG, journalpostId, dokumentreferanse), HttpMethod.GET, null, DokumentTilgangResponse.class);

    return new HttpResponse<>(response);
  }
}
