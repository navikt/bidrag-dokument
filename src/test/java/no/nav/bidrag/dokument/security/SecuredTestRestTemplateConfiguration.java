package no.nav.bidrag.dokument.security;

import static no.nav.bidrag.dokument.BidragDokumentConfig.SECURE_TEST_PROFILE;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import no.nav.bidrag.commons.web.test.SecuredTestRestTemplate;

@Configuration
@Profile(SECURE_TEST_PROFILE)
public class SecuredTestRestTemplateConfiguration {

  @Bean
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public SecuredTestRestTemplate securedTestRestTemplate(TestRestTemplate testRestTemplate) {
    return new SecuredTestRestTemplate(testRestTemplate);
  }
}
