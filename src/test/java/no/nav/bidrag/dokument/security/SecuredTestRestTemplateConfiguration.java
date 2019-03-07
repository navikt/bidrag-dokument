package no.nav.bidrag.dokument.security;

import no.nav.bidrag.commons.web.test.SecuredTestRestTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("secure-test")
public class SecuredTestRestTemplateConfiguration {

  @Bean
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public SecuredTestRestTemplate securedTestRestTemplate(TestRestTemplate testRestTemplate) {
    return new SecuredTestRestTemplate(testRestTemplate);
  }
}
