package no.nav.bidrag.dokument;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@EnableWebMvc
public class SwaggerConfig implements WebMvcConfigurer {

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.OAS_30)
        .apiInfo(
            new ApiInfoBuilder()
                .title("Journalpost API")
                .description("Vise, registrere og avvik på journalpost")
                .license("MIT")
                .licenseUrl("https://opensource.org/licenses/MIT")
                .build()
        ).tags(
            new Tag("Journalpost", "Operasjoner på journalpost"),
            new Tag("Dokument", "Tilgang på dokumenter til journalpost")
        ).select()
        .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
        .build()
        .securitySchemes(List.of(apiKey()))
        .securityContexts(List.of(securityContext()));
  }

  private ApiKey apiKey() {
    return new ApiKey("mykey", "Authorization", "header");
  }

  private SecurityContext securityContext() {
    return SecurityContext.builder()
        .securityReferences(defaultAuth())
        .build();
  }

  private List<SecurityReference> defaultAuth() {
    return List.of(new SecurityReference(
        "mykey",
        new AuthorizationScope[]{new AuthorizationScope("global", "accessEverything")}
    ));
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addRedirectViewController("/api/v2/api-docs", "/v2/api-docs");
    registry.addRedirectViewController("/api/swagger-resources/configuration/ui", "/swagger-resources/configuration/ui");
    registry.addRedirectViewController("/api/swagger-resources/configuration/security", "/swagger-resources/configuration/security");
    registry.addRedirectViewController("/api/swagger-resources", "/swagger-resources");
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/api/swagger-ui.html**").addResourceLocations("classpath:/META-INF/resources/swagger-ui.html");
    registry.addResourceHandler("/api/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
  }
}
