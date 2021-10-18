package no.nav.bidrag.dokument.consumer;

import lombok.Builder;
import lombok.Getter;
import no.nav.bidrag.dokument.BidragDokumentConfig.RestTemplateProvider;
import org.springframework.web.client.RestTemplate;

@Getter
@Builder
public class ConsumerTarget {

  private RestTemplate azureRestTemplate;
  private RestTemplate issoRestTemplate;
  private RestTemplateProvider restTemplateProvider;
  private String targetApp;

  public RestTemplate henteRestTemplateForIssuer() {
    return restTemplateProvider.provideRestTemplate(this);
  }
}
