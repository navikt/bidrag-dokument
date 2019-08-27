package no.nav.bidrag.dokument;

import java.util.Optional;
import no.nav.bidrag.commons.ExceptionLogger;
import no.nav.bidrag.commons.web.CorrelationIdFilter;
import no.nav.bidrag.commons.web.EnhetFilter;
import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.consumer.DokumentConsumer;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.oidc.context.TokenContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
  public CorrelationIdFilter correlationIdFilter() {
    return new CorrelationIdFilter();
  }

  @Bean
  public ExceptionLogger exceptionLogger() {
    return new ExceptionLogger(BidragDokument.class.getSimpleName());
  }

  @Bean
  public OidcTokenManager oidcTokenManager(OIDCRequestContextHolder oidcRequestContextHolder) {
    return () -> Optional.ofNullable(oidcRequestContextHolder)
        .map(OIDCRequestContextHolder::getOIDCValidationContext)
        .map(oidcValidationContext -> oidcValidationContext.getToken(ISSUER))
        .map(TokenContext::getIdToken)
        .orElseThrow(() -> new IllegalStateException("Kunne ikke videresende Bearer token"));
  }

  @Bean
  public EnhetFilter enhetFilter() {
    return new EnhetFilter();
  }

  @Bean
  public FilterRegistrationBean<EnhetFilter> filterRegistrationBean(EnhetFilter enhetFilter) {
    FilterRegistrationBean <EnhetFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(enhetFilter);
    registrationBean.setOrder(2); //set precedence

    return registrationBean;
  }

  @FunctionalInterface
  public interface OidcTokenManager {

    String fetchToken();
  }
}
