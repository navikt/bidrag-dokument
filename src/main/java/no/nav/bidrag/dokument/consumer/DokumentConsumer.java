package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.commons.web.HttpStatusResponse;
import no.nav.bidrag.dokument.dto.DokumentUrlDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class DokumentConsumer {

  private final RestTemplate restTemplate;

  public DokumentConsumer(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public HttpStatusResponse<DokumentUrlDto> hentTilgangUrl(DokumentUrlDto dokumentUrlDto) {
    var response = restTemplate.exchange("/tilgang/url", HttpMethod.POST, new HttpEntity<>(dokumentUrlDto), DokumentUrlDto.class);
    return new HttpStatusResponse<>(response.getStatusCode(), response.getBody());
  }
}
