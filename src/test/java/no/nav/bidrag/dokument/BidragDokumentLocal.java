package no.nav.bidrag.dokument;

import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration;
import no.nav.security.spring.oidc.api.EnableOIDCTokenValidation;

@SpringBootApplication
@PropertySource("classpath:url.properties")
@EnableOIDCTokenValidation(ignore = {"springfox.documentation.swagger.web.ApiResourceController", "org.springframework"})
@Import(TokenGeneratorConfiguration.class)
@ComponentScan(excludeFilters = { @Filter(type = ASSIGNABLE_TYPE, value = BidragDokument.class) })
public class BidragDokumentLocal {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BidragDokumentLocal.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }
}
