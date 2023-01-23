package no.nav.bidrag.dokument;

import static no.nav.bidrag.dokument.BidragDokumentConfig.NAIS_PROFILE;

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class })
@EnableJwtTokenValidation(ignore = {"org.springdoc"})
public class BidragDokument {
  public static final Logger SECURE_LOGGER = LoggerFactory.getLogger("secureLogger");

  public static void main(String[] args) {

    String profile = args.length < 1 ? NAIS_PROFILE : args[0];

    SpringApplication app = new SpringApplication(BidragDokument.class);
    app.setAdditionalProfiles(profile);
    app.run(args);
  }
}
