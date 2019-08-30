package no.nav.bidrag.dokument.security;

import static no.nav.bidrag.dokument.BidragDokumentLocal.SECURE_TEST_PROFILE;

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate;
import no.nav.security.oidc.test.support.jersey.TestTokenGeneratorResource;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;

@Configuration
@Profile(SECURE_TEST_PROFILE)
public class HttpHeaderTestRestTemplateConfiguration {

  @Bean
  public HttpHeaderTestRestTemplate securedTestRestTemplate(TestRestTemplate testRestTemplate) {
    var httpHeaderTestRestTemplate = new HttpHeaderTestRestTemplate(testRestTemplate);
    httpHeaderTestRestTemplate.add(HttpHeaders.AUTHORIZATION, this::generateTestToken);

    return httpHeaderTestRestTemplate;
  }

  private String generateTestToken() {
    TestTokenGeneratorResource testTokenGeneratorResource = new TestTokenGeneratorResource();
    return "Bearer " + testTokenGeneratorResource.issueToken("localhost-idtoken");
  }
}
