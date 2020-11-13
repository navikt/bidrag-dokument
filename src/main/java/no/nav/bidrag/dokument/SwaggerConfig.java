package no.nav.bidrag.dokument;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public GroupedOpenApi Api() {
    return GroupedOpenApi.builder()
        .group("bidrag-dokument")
        .packagesToScan("no.nav.bidrag.dokument.controller")
        .pathsToMatch("/tilgang/**", "/sak/**", "/journal/**")
        .build();
  }

  @Bean
  public OpenAPI api() {
    return new OpenAPI()
        .info(
            new Info().title("bidrag-dokument")
                .description("Integrasjon mellom BISYS, midlertidig brevlager og JOARK (Brev og Arkiv)")
                .version("v0.0.1")
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT"))
        ).externalDocs(
            new ExternalDocumentation()
                .description("bidrag-dokument, github site")
                .url("https://github.com/navikt/bidrag-dokument")
        );
  }
}
