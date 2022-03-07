package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.dokument.BidragDokumentConfig.RestTemplateProvider;
import org.springframework.web.client.RestTemplate;

public class ConsumerTarget {

  private RestTemplate issoRestTemplate;
  private RestTemplateProvider restTemplateProvider;
  private String targetApp;

  public ConsumerTarget() {
  }

  public ConsumerTarget(RestTemplate issoRestTemplate, RestTemplateProvider restTemplateProvider, String targetApp) {
    this.issoRestTemplate = issoRestTemplate;
    this.restTemplateProvider = restTemplateProvider;
    this.targetApp = targetApp;
  }

  public RestTemplate getIssoRestTemplate() {
    return issoRestTemplate;
  }

  public RestTemplateProvider getRestTemplateProvider() {
    return restTemplateProvider;
  }

  public String getTargetApp() {
    return targetApp;
  }

  public RestTemplate henteRestTemplateForIssuer() {
    return restTemplateProvider.provideRestTemplate(this);
  }
}
