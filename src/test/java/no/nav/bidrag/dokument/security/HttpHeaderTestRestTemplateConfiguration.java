package no.nav.bidrag.dokument.security;

import static no.nav.bidrag.dokument.BidragDokumentTest.TEST_PROFILE;

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;

@Configuration
@Profile(TEST_PROFILE)
public class HttpHeaderTestRestTemplateConfiguration {
  @Autowired
  private MockOAuth2Server mockOAuth2Server;
  @Bean
  HttpHeaderTestRestTemplate securedTestRestTemplate(TestRestTemplate testRestTemplate) {
    HttpHeaderTestRestTemplate httpHeaderTestRestTemplate = new HttpHeaderTestRestTemplate(testRestTemplate);
    httpHeaderTestRestTemplate.add(HttpHeaders.AUTHORIZATION, ()->generateTestToken());

    return httpHeaderTestRestTemplate;
  }

  private String generateTestToken() {
    var token = mockOAuth2Server.issueToken("aad", "aud-localhost", "aud-localhost");
    return "Bearer " + token.serialize();
  }
}
