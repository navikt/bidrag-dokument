package no.nav.bidrag.dokument;

import static no.nav.bidrag.dokument.BidragDokumentConfig.ISSUER;

import java.util.Optional;
import no.nav.bidrag.dokument.HttpHeaderRestTemplate.Header;
import no.nav.bidrag.dokument.HttpHeaderRestTemplate.HeaderGenerator;
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
    httpHeaderRestTemplate.addHeaderGenerator(createBearerTokenGenerator(oidcRequestContextHolder));

    return httpHeaderRestTemplate;
  }

  private HeaderGenerator createBearerTokenGenerator(OIDCRequestContextHolder oidcRequestContextHolder) {
    return () -> new Header() {

      @Override
      public String name() {
        return HttpHeaders.AUTHORIZATION;
      }

      @Override
      public String value() {
        return "Bearer " + fetchBearerToken();
      }

      private String fetchBearerToken() {
        return Optional.ofNullable(oidcRequestContextHolder)
            .map(OIDCRequestContextHolder::getOIDCValidationContext)
            .map(oidcValidationContext -> oidcValidationContext.getToken(ISSUER))
            .map(TokenContext::getIdToken)
            .orElseThrow(() -> new IllegalStateException("Kunne ikke videresende Bearer token"));
      }
    };
  }
}
