package no.nav.bidrag.dokument;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import no.nav.bidrag.commons.ExceptionLogger;
import no.nav.bidrag.commons.security.api.EnableSecurityConfiguration;
import no.nav.bidrag.commons.security.service.OidcTokenManager;
import no.nav.bidrag.commons.security.service.SecurityTokenService;
import no.nav.bidrag.commons.web.CorrelationIdFilter;
import no.nav.bidrag.commons.web.DefaultCorsFilter;
import no.nav.bidrag.commons.web.EnhetFilter;
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate;
import no.nav.bidrag.commons.web.UserMdcFilter;
import no.nav.bidrag.dokument.consumer.BidragDokumentConsumer;
import no.nav.bidrag.dokument.consumer.DokumentTilgangConsumer;
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableOAuth2Client(cacheEnabled = true)
@OpenAPIDefinition(
    info = @Info(title = "bidrag-dokument", version = "v1"),
    security = @SecurityRequirement(name = "bearer-key")
)
@SecurityScheme(
    bearerFormat = "JWT",
    name = "bearer-key",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP
)
@EnableAspectJAutoProxy
@EnableSecurityConfiguration
public class BidragDokumentConfig {

  public static final String DELIMTER = "-";
  public static final String PREFIX_BIDRAG = "BID";
  public static final String PREFIX_JOARK = "JOARK";
  public static final String MIDL_BREVLAGER_QUALIFIER = "midlertidigBrevlager";
  public static final String ARKIV_QUALIFIER = "arkiv";
  public static final String KLIENTNAVN_BIDRAG_DOKUMENT_ARKIV = "bidrag-dokument-arkiv";
  public static final String KLIENTNAVN_BIDRAG_DOKUMENT_JOURNALPOST = "bidrag-dokument-journalpost";
  static final String LIVE_PROFILE = "live";
  private static final Logger LOGGER = LoggerFactory.getLogger(BidragDokumentConfig.class);

  @Bean
  @Qualifier(MIDL_BREVLAGER_QUALIFIER)
  public BidragDokumentConsumer bidragJournalpostConsumer(
      @Value("${JOURNALPOST_URL}") String journalpostBaseUrl,
      SecurityTokenService securityTokenService
  ) {
    LOGGER.info("BidragJournalpostConsumer med base url: " + journalpostBaseUrl);
    var restTemplate = createRestTemplate(journalpostBaseUrl, securityTokenService, KLIENTNAVN_BIDRAG_DOKUMENT_JOURNALPOST);

    return new BidragDokumentConsumer(restTemplate);
  }

  @Bean
  @Qualifier(ARKIV_QUALIFIER)
  public BidragDokumentConsumer bidragArkivConsumer(
      @Value("${BIDRAG_ARKIV_URL}") String bidragArkivBaseUrl,
      SecurityTokenService securityTokenService
  ) {
    LOGGER.info("BidragArkivConsumer med base url: " + bidragArkivBaseUrl);
    var restTemplate = createRestTemplate(bidragArkivBaseUrl, securityTokenService, KLIENTNAVN_BIDRAG_DOKUMENT_ARKIV);
    return new BidragDokumentConsumer(restTemplate);
  }

  @Bean
  public DokumentTilgangConsumer dokumentConsumer(
      @Value("${JOURNALPOST_URL}") String journalpostBaseUrl,
      SecurityTokenService securityTokenService
  ) {
    LOGGER.info("DokumentConsumer med base url: " + journalpostBaseUrl);
    var restTemplate = createRestTemplate(journalpostBaseUrl, securityTokenService, KLIENTNAVN_BIDRAG_DOKUMENT_JOURNALPOST);
    return new DokumentTilgangConsumer(restTemplate);
  }

  @Bean
  @Order(1)
  public CorrelationIdFilter correlationIdFilter() {
    return new CorrelationIdFilter();
  }

  @Bean
  @Order(2)
  public EnhetFilter enhetFilter() {
    return new EnhetFilter();
  }

  @Bean
  public UserMdcFilter userMdcFilter(OidcTokenManager oidcTokenManager) {
    return new UserMdcFilter(oidcTokenManager);
  }
  @Bean
  public DefaultCorsFilter corsFilter() {
    return new DefaultCorsFilter();
  }

  @Bean
  public ExceptionLogger exceptionLogger() {
    return new ExceptionLogger(BidragDokument.class.getSimpleName());
  }

  private RestTemplate createRestTemplate(String baseUrl, SecurityTokenService securityTokenService, String clientId) {
    var requestFactory = new HttpComponentsClientHttpRequestFactory();

    var httpHeaderRestTemplate = new HttpHeaderRestTemplate();

    httpHeaderRestTemplate.getInterceptors().add(securityTokenService.authTokenInterceptor(clientId, true));
    httpHeaderRestTemplate.withDefaultHeaders();
    httpHeaderRestTemplate.setRequestFactory(requestFactory);
    httpHeaderRestTemplate.setUriTemplateHandler(new RootUriTemplateHandler(baseUrl));

    return httpHeaderRestTemplate;
  }

  @Bean
  public TimedAspect timedAspect(MeterRegistry registry) {
    return new TimedAspect(registry);
  }

}
