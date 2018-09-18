package no.nav.bidrag.dokument.microservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableSet;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private static final Contact CONTACT = new Contact("Team Bidrag", "www.nav.no", "nav.ikt.prosjekt.og.forvaltning.bidrag@nav.no");

    private static final ApiInfo API_INFO = new ApiInfo(
            "JournalpostDto BISYS", "Journalforing for BISYS", "1.0", null, CONTACT, null, null
    );

    private static final Set<String> PRODUCES_AND_CONSUMES = unmodifiableSet(Stream.of("application/json", "application/xml").collect(Collectors.toSet()));

    @Bean public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(API_INFO)
                .produces(PRODUCES_AND_CONSUMES)
                .consumes(PRODUCES_AND_CONSUMES);
    }
}
