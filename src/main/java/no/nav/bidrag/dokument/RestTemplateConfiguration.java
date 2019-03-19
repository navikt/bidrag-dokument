package no.nav.bidrag.dokument;

import static no.nav.bidrag.dokument.BidragDokumentConfig.ISSUER;

import java.util.Optional;
import no.nav.bidrag.commons.web.CorrelationIdFilter;
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.oidc.context.TokenContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

  @Bean
  @Scope("prototype")
  public RestTemplate restTemplate(OIDCRequestContextHolder oidcRequestContextHolder) {
    HttpHeaderRestTemplate httpHeaderRestTemplate = new HttpHeaderRestTemplate();

    httpHeaderRestTemplate.addHeaderGenerator(
        () -> new HttpHeaderRestTemplate.Header(HttpHeaders.AUTHORIZATION, () -> "Bearer " + fetchBearerToken(oidcRequestContextHolder))
    );

    httpHeaderRestTemplate.addHeaderGenerator(
        () -> new HttpHeaderRestTemplate.Header(CorrelationIdFilter.CORRELATION_ID_HEADER, CorrelationIdFilter::fetchCorrelationIdForThread)
    );

    return httpHeaderRestTemplate;
  }

  private String fetchBearerToken(OIDCRequestContextHolder oidcRequestContextHolder) {
    return Optional.ofNullable(oidcRequestContextHolder)
        .map(OIDCRequestContextHolder::getOIDCValidationContext)
        .map(oidcValidationContext -> oidcValidationContext.getToken(ISSUER))
        .map(TokenContext::getIdToken)
        .orElseThrow(() -> new IllegalStateException("Kunne ikke videresende Bearer token"));
  }
}
