package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.commons.web.HttpStatusResponse;
import no.nav.bidrag.dokument.dto.DokumentTilgangResponse;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class DokumentConsumer {

  private final RestTemplate restTemplate;

  public DokumentConsumer(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public HttpStatusResponse<DokumentTilgangResponse> hentTilgangUrl(String dokumentreferanse) {
    var response = restTemplate.exchange("/tilgang/" + dokumentreferanse, HttpMethod.GET, null, DokumentTilgangResponse.class);
    return new HttpStatusResponse<>(response.getStatusCode(), response.getBody());
  }
}
