package no.nav.bidrag.dokument;

import static no.nav.bidrag.dokument.BidragDokumentConfig.LIVE_PROFILE;

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableJwtTokenValidation(ignore = {"springfox.documentation.swagger.web.ApiResourceController"})
public class BidragDokument {

  public static void main(String[] args) {

    String profile = args.length < 1 ? LIVE_PROFILE : args[0];

    SpringApplication app = new SpringApplication(BidragDokument.class);
    app.setAdditionalProfiles(profile);
    app.run(args);
  }
}
