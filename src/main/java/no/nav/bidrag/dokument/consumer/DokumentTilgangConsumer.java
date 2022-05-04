package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.commons.web.HttpResponse;
import no.nav.bidrag.dokument.dto.DokumentTilgangResponse;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class DokumentTilgangConsumer {

  public static final String PATH_DOKUMENT_TILGANG = "/tilgang/%s/%s";
  public static final String PATH_HENT_DOKUMENT = "/dokument/%s";

  private final RestTemplate restTemplate;

  public DokumentTilgangConsumer(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }
  public HttpResponse<DokumentTilgangResponse> hentTilgangUrl(String journalpostId, String dokumentreferanse) {

    var response = restTemplate
        .exchange(String.format(PATH_DOKUMENT_TILGANG, journalpostId, dokumentreferanse), HttpMethod.GET, null, DokumentTilgangResponse.class);

    return new HttpResponse<>(response);
  }
}
