package no.nav.bidrag.dokument;

import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication()
@PropertySource({"classpath:url.properties", "classpath:secret.properties"})
@EnableJwtTokenValidation(ignore = {"springfox.documentation.swagger.web.ApiResourceController"})
@Import(TokenGeneratorConfiguration.class)
@ComponentScan(excludeFilters = {@Filter(type = ASSIGNABLE_TYPE, value = BidragDokument.class)})
public class BidragDokumentLocal {

  public static final String SECURE_TEST_PROFILE = "secure-test";
  public static final String TEST_PROFILE = "test";

  public static void main(String[] args) {
    String profile = args.length < 1 ? TEST_PROFILE : args[0];

    SpringApplication app = new SpringApplication(BidragDokumentLocal.class);
    app.setAdditionalProfiles(profile);
    app.run(args);
  }
}