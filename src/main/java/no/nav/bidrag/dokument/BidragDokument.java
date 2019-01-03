package no.nav.bidrag.dokument;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import no.nav.security.spring.oidc.api.EnableOIDCTokenValidation;

@SpringBootApplication
@PropertySource("classpath:url.properties")
@EnableOIDCTokenValidation(ignore = {"springfox.documentation.swagger.web.ApiResourceController", "org.springframework"})
public class BidragDokument extends WebMvcConfigurationSupport {

    public static void main(String[] args) {
        SpringApplication.run(BidragDokument.class, args);
    }
}