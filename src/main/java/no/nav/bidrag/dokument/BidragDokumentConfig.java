package no.nav.bidrag.dokument;

import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import java.text.ParseException;
import java.util.Optional;
import no.nav.bidrag.commons.ExceptionLogger;
import no.nav.bidrag.commons.web.CorrelationIdFilter;
import no.nav.bidrag.commons.web.EnhetFilter;
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate;
import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.consumer.ConsumerTarget;
import no.nav.bidrag.dokument.consumer.DokumentConsumer;
import no.nav.security.token.support.client.core.ClientProperties;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableOAuth2Client(cacheEnabled = true)
@EnableJwtTokenValidation
@OpenAPIDefinition(
    info = @Info(title = "bidrag-dokument", version = "v1"),
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    bearerFormat = "JWT",
    name = "bearerAuth",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP
)
public class BidragDokumentConfig {

  public static final String DELIMTER = "-";
  public static final String PREFIX_BIDRAG = "BID";
  public static final String PREFIX_JOARK = "JOARK";
  public static final String KLIENTNAVN_BIDRAG_DOKUMENT_ARKIV = "bidrag-dokument-arkiv";
  public static final String KLIENTNAVN_BIDRAG_DOKUMENT_JOURNALPOST = "bidrag-dokument-journalpost";
  static final String LIVE_PROFILE = "live";
  private static final Logger LOGGER = LoggerFactory.getLogger(BidragDokumentConfig.class);
  private static final String ISSUER_AZURE_AD_IDENTIFIER = "login.microsoftonline.com";

  private final ClientConfigurationProperties clientConfigurationProperties;
  private final OAuth2AccessTokenService oAuth2AccessTokenService;
  private final RestTemplateBuilder restTemplateBuilder;

  public BidragDokumentConfig(
      ClientConfigurationProperties clientConfigurationProperties,
      OAuth2AccessTokenService oAuth2AccessTokenService,
      RestTemplateBuilder restTemplateBuilder
  ) {
    this.clientConfigurationProperties = clientConfigurationProperties;
    this.oAuth2AccessTokenService = oAuth2AccessTokenService;
    this.restTemplateBuilder = restTemplateBuilder;
  }

  private static String henteIssuer(String idToken) {
    try {
      return parseIdToken(idToken).getJWTClaimsSet().getIssuer();
    } catch (ParseException e) {
      throw new IllegalStateException("Kunne ikke hente informasjon om tokenets issuer", e);
    }
  }

  private static SignedJWT parseIdToken(String idToken) throws ParseException {
    return (SignedJWT) JWTParser.parse(idToken);
  }

  @Bean
  public BidragJournalpostConsumer bidragJournalpostConsumer(
      @Value("${JOURNALPOST_URL}") String journalpostBaseUrl,
      OidcTokenManager oidcTokenManager,
      RestTemplateProvider restTemplateProvider
  ) {
    LOGGER.info("BidragJournalpostConsumer med base url: " + journalpostBaseUrl);
    var consumerTarget = ConsumerTarget.builder().azureRestTemplate(azureRestTemplate(KLIENTNAVN_BIDRAG_DOKUMENT_JOURNALPOST, journalpostBaseUrl))
        .issoRestTemplate(issoRestTemplate(journalpostBaseUrl, oidcTokenManager)).restTemplateProvider(restTemplateProvider).build();

    return new BidragJournalpostConsumer(consumerTarget);
  }

  @Bean
  public BidragArkivConsumer journalforingConsumer(
      @Value("${BIDRAG_ARKIV_URL}") String bidragArkivBaseUrl,
      OidcTokenManager oidcTokenManager,
      RestTemplateProvider restTemplateProvider
  ) {
    LOGGER.info("BidragArkivConsumer med base url: " + bidragArkivBaseUrl);
    var consumerTarget = ConsumerTarget.builder().azureRestTemplate(azureRestTemplate(KLIENTNAVN_BIDRAG_DOKUMENT_ARKIV, bidragArkivBaseUrl))
        .issoRestTemplate(issoRestTemplate(bidragArkivBaseUrl, oidcTokenManager)).restTemplateProvider(restTemplateProvider).build();
    return new BidragArkivConsumer(consumerTarget);
  }

  @Bean
  public DokumentConsumer dokumentConsumer(
      @Value("${JOURNALPOST_URL}") String journalpostBaseUrl,
      OidcTokenManager oidcTokenManager,
      RestTemplateProvider restTemplateProvider
  ) {
    LOGGER.info("DokumentConsumer med base url: " + journalpostBaseUrl);
    var consumerTarget = ConsumerTarget.builder().azureRestTemplate(azureRestTemplate(KLIENTNAVN_BIDRAG_DOKUMENT_JOURNALPOST, journalpostBaseUrl))
        .issoRestTemplate(issoRestTemplate(journalpostBaseUrl, oidcTokenManager)).restTemplateProvider(restTemplateProvider).build();
    return new DokumentConsumer(consumerTarget);
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
  public ExceptionLogger exceptionLogger() {
    return new ExceptionLogger(BidragDokument.class.getSimpleName());
  }

  @Bean
  public OidcTokenManager oidcTokenManager(TokenValidationContextHolder tokenValidationContextHolder) {
    return () -> Optional.ofNullable(tokenValidationContextHolder)
        .map(TokenValidationContextHolder::getTokenValidationContext)
        .map(TokenValidationContext::getFirstValidToken)
        .map(Optional::get)
        .map(JwtToken::getTokenAsString)
        .orElseThrow(() -> new IllegalStateException("Kunne ikke hente Bearer token"));
  }

  @Bean
  public RestTemplateProvider restTemplateProvider(OidcTokenManager oidcTokenManager) {
    return (consumerTarget) -> selector(oidcTokenManager.fetchToken(), consumerTarget);
  }

  private RestTemplate selector(String idToken, ConsumerTarget consumerTarget) {
    var issuer = henteIssuer(idToken);
    if (issuer.contains(ISSUER_AZURE_AD_IDENTIFIER)) {
      return consumerTarget.getAzureRestTemplate();
    } else {
      return consumerTarget.getIssoRestTemplate();
    }
  }

  private RestTemplate azureRestTemplate(String clientName, String baseUrl) {
    ClientProperties clientProperties = Optional.ofNullable(clientConfigurationProperties.getRegistration().get(clientName))
        .orElseThrow(() -> new IllegalStateException("could not find oauth2 client config for " + clientName));
    return restTemplateBuilder.rootUri(baseUrl).additionalInterceptors(bearerTokenInterceptor(clientProperties, oAuth2AccessTokenService)).build();
  }

  private ClientHttpRequestInterceptor bearerTokenInterceptor(ClientProperties clientProperties, OAuth2AccessTokenService oAuth2AccessTokenService) {
    return (request, body, execution) -> {
      OAuth2AccessTokenResponse response = oAuth2AccessTokenService.getAccessToken(clientProperties);
      request.getHeaders().setBearerAuth(response.getAccessToken());
      return execution.execute(request, body);
    };
  }

  private RestTemplate issoRestTemplate(String baseUrl, OidcTokenManager oidcTokenManager) {
    HttpHeaderRestTemplate httpHeaderRestTemplate = new HttpHeaderRestTemplate();

    httpHeaderRestTemplate.addHeaderGenerator(HttpHeaders.AUTHORIZATION, () -> "Bearer " + oidcTokenManager.fetchToken());
    httpHeaderRestTemplate.addHeaderGenerator(CorrelationIdFilter.CORRELATION_ID_HEADER, CorrelationIdFilter::fetchCorrelationIdForThread);

    httpHeaderRestTemplate.setUriTemplateHandler(new RootUriTemplateHandler(baseUrl));

    return httpHeaderRestTemplate;
  }

  @FunctionalInterface
  public interface RestTemplateProvider {

    RestTemplate provideRestTemplate(ConsumerTarget consumerTarget);
  }

  @FunctionalInterface
  public interface OidcTokenManager {

    String fetchToken();
  }
}
