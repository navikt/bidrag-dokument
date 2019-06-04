package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.commons.web.HttpStatusResponse;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class BidragArkivConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(BidragArkivConsumer.class);
  private static final String PATH_JOURNALPOST = "/journalpost/";

  private final RestTemplate restTemplate;

  public BidragArkivConsumer(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public HttpStatusResponse<JournalpostDto> hentJournalpost(Integer id) {
    var journalpostExchange = restTemplate.exchange(PATH_JOURNALPOST + id, HttpMethod.GET, null, JournalpostDto.class);

    LOGGER.info("Hent journalpost fikk http status {} fra joark", journalpostExchange.getStatusCode());

    return new HttpStatusResponse<>(journalpostExchange.getStatusCode(), journalpostExchange.getBody());
  }
}
