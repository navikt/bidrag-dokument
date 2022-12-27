package no.nav.bidrag.dokument;

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.test.context.ActiveProfiles;

import static no.nav.bidrag.dokument.BidragDokumentTest.TEST_PROFILE;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class })
@EnableJwtTokenValidation(ignore = {"org.springdoc"})
@ActiveProfiles("local")
@ComponentScan(excludeFilters = {@Filter(type = ASSIGNABLE_TYPE, value = BidragDokument.class)})
public class BidragDokumentLocal {
  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(BidragDokumentLocal.class);
    app.setAdditionalProfiles("nais", "local", "lokal-nais-secrets");
    app.run(args);
  }


}