package no.nav.bidrag.dokument.consumer;

import java.util.Optional;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class BidragArkivConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(BidragArkivConsumer.class);
  private static final String PATH_JOURNALPOST = "/journalpost/";

  private final RestTemplate restTemplate;

  public BidragArkivConsumer(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public Optional<JournalpostDto> hentJournalpost(Integer id) {
    Optional<ResponseEntity<JournalpostDto>> possibleExchange = Optional.ofNullable(
        restTemplate.exchange(PATH_JOURNALPOST + id, HttpMethod.GET, null, JournalpostDto.class)
    );

    possibleExchange.ifPresent(
        (responseEntity) -> LOGGER.info("Hent journalpost fikk http status {} fra joark", responseEntity.getStatusCode())
    );

    return possibleExchange.map(ResponseEntity::getBody);
  }
}
