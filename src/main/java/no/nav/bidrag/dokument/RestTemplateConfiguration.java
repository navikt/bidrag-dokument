package no.nav.bidrag.dokument;

import static no.nav.bidrag.commons.web.EnhetFilter.X_ENHET_HEADER;

import no.nav.bidrag.commons.web.CorrelationIdFilter;
import no.nav.bidrag.commons.web.EnhetFilter;
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate;
import no.nav.bidrag.dokument.BidragDokumentConfig.OidcTokenManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

  @Bean
  @Scope("prototype")
  public RestTemplate restTemplate(OidcTokenManager oidcTokenManager) {
    HttpHeaderRestTemplate httpHeaderRestTemplate = new HttpHeaderRestTemplate();

    httpHeaderRestTemplate.addHeaderGenerator(HttpHeaders.AUTHORIZATION, () -> "Bearer " + oidcTokenManager.fetchToken());
    httpHeaderRestTemplate.addHeaderGenerator(CorrelationIdFilter.CORRELATION_ID_HEADER, CorrelationIdFilter::fetchCorrelationIdForThread);

    return httpHeaderRestTemplate;
  }
}
