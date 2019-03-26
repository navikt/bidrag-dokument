package no.nav.bidrag.dokument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import no.nav.bidrag.commons.ExceptionLogger;
import no.nav.bidrag.commons.web.CorrelationIdFilter;
import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.consumer.BidragSakConsumer;

@Configuration
public class BidragDokumentConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(BidragDokumentConfig.class);

  public static final String DELIMTER = "-";
  public static final String PREFIX_BIDRAG = "BID";
  public static final String PREFIX_JOARK = "JOARK";
  public static final String ISSUER = "isso";
  public static final String LIVE_PROFILE = "live";
  public static final String TEST_PROFILE = "test";
  public static final String SECURE_TEST_PROFILE = "secure-test";

  @Bean
  public BidragJournalpostConsumer bidragJournalpostConsumer(
      @Value("${JOURNALPOST_URL}") String journalpostBaseUrl, RestTemplate restTemplate) {
    restTemplate.setUriTemplateHandler(new RootUriTemplateHandler(journalpostBaseUrl));
    LOGGER.info("BidragJournalpostConsumer med base url: " + journalpostBaseUrl);

    return new BidragJournalpostConsumer(restTemplate);
  }

  @Bean
  public BidragSakConsumer bidragSakConsumer(
      @Value("${BIDRAG_SAK_URL}") String sakBaseUrl, RestTemplate restTemplate) {
    restTemplate.setUriTemplateHandler(new RootUriTemplateHandler(sakBaseUrl));
    LOGGER.info("BidragSakConsumer med base url: " + sakBaseUrl);

    return new BidragSakConsumer(restTemplate);
  }

  @Bean
  public BidragArkivConsumer journalforingConsumer(
      @Value("${BIDRAG_ARKIV_URL}") String bidragArkivBaseUrl, RestTemplate restTemplate) {
    restTemplate.setUriTemplateHandler(new RootUriTemplateHandler(bidragArkivBaseUrl));
    LOGGER.info("BidragArkivConsumer med base url: " + bidragArkivBaseUrl);

    return new BidragArkivConsumer(restTemplate);
  }

  @Bean
  public CorrelationIdFilter correlationIdFilter() {
    return new CorrelationIdFilter();
  }

  @Bean
  public ExceptionLogger exceptionLogger() {
    return new ExceptionLogger(BidragDokument.class.getSimpleName());
  }
}
