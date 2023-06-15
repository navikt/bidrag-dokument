package no.nav.bidrag.dokument.security;

import static no.nav.bidrag.dokument.BidragDokumentTest.TEST_PROFILE;

import com.nimbusds.jose.JOSEObjectType;
import java.util.List;
import java.util.Map;
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
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
    var iss = mockOAuth2Server.issuerUrl("aad");
    var newIssuer = iss.newBuilder().host("localhost").build();
//    var token = mockOAuth2Server.issueToken("aad", "aud-localhost", "aud-localhost");

    var token = mockOAuth2Server.issueToken("aad", "aud-localhost", new DefaultOAuth2TokenCallback("aad", "aud-localhost", JOSEObjectType.JWT.getType(), List.of("aud-localhost"), Map.of("iss", newIssuer.toString()), 3600));
    return "Bearer " + token.serialize();
  }
}
