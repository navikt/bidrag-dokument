package no.nav.bidrag.dokument;

import java.util.Optional;
import no.nav.bidrag.commons.ExceptionLogger;
import no.nav.bidrag.commons.web.CorrelationIdFilter;
import no.nav.bidrag.commons.web.EnhetFilter;
import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.consumer.DokumentConsumer;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BidragDokumentConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(BidragDokumentConfig.class);

  public static final String DELIMTER = "-";
  public static final String PREFIX_BIDRAG = "BID";
  public static final String PREFIX_JOARK = "JOARK";
  public static final String ISSUER = "isso";

  static final String LIVE_PROFILE = "live";

  @Bean
  public BidragJournalpostConsumer bidragJournalpostConsumer(
      @Value("${JOURNALPOST_URL}") String journalpostBaseUrl, RestTemplate restTemplate
  ) {
    restTemplate.setUriTemplateHandler(new RootUriTemplateHandler(journalpostBaseUrl));
    LOGGER.info("BidragJournalpostConsumer med base url: " + journalpostBaseUrl);

    return new BidragJournalpostConsumer(restTemplate);
  }

  @Bean
  public BidragArkivConsumer journalforingConsumer(
      @Value("${BIDRAG_ARKIV_URL}") String bidragArkivBaseUrl, RestTemplate restTemplate
  ) {
    restTemplate.setUriTemplateHandler(new RootUriTemplateHandler(bidragArkivBaseUrl));
    LOGGER.info("BidragArkivConsumer med base url: " + bidragArkivBaseUrl);

    return new BidragArkivConsumer(restTemplate);
  }

  @Bean
  public DokumentConsumer dokumentConsumer(
      @Value("${JOURNALPOST_URL}") String journalpostBaseUrl, RestTemplate restTemplate
  ) {
    restTemplate.setUriTemplateHandler(new RootUriTemplateHandler(journalpostBaseUrl));
    LOGGER.info("DokumentConsumer med base url: " + journalpostBaseUrl);

    return new DokumentConsumer(restTemplate);
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
        .map(tokenValidationContext -> tokenValidationContext.getJwtTokenAsOptional(ISSUER))
        .map(Optional::get)
        .map(JwtToken::getTokenAsString)
        .orElseThrow(() -> new IllegalStateException("Kunne ikke videresende Bearer token"));
  }

  @FunctionalInterface
  public interface OidcTokenManager {

    String fetchToken();
  }
}
