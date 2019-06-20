package no.nav.bidrag.dokument.consumer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import no.nav.bidrag.commons.web.HttpStatusResponse;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class BidragArkivConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(BidragArkivConsumer.class);
  private static final String PATH_JOURNALPOST = "/journalpost/";
  private static final String PATH_SAKJOURNAL = "/sakjournal/";
  private static final String PARAM_FAGOMRADE = "fagomrade";

  private final RestTemplate restTemplate;

  public BidragArkivConsumer(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public HttpStatusResponse<JournalpostDto> hentJournalpost(Integer id) {
    var journalpostExchange = restTemplate.exchange(PATH_JOURNALPOST + id, HttpMethod.GET, null, JournalpostDto.class);

    LOGGER.info("Hent journalpost fikk http status {} fra bidrag-dokument-arkiv", journalpostExchange.getStatusCode());

    return new HttpStatusResponse<>(journalpostExchange.getStatusCode(), journalpostExchange.getBody());
  }

  public List<JournalpostDto> finnJournalposter(String saksnummer, String fagomrade) {
    var uri = UriComponentsBuilder.fromPath(PATH_SAKJOURNAL + saksnummer)
        .queryParam(PARAM_FAGOMRADE, fagomrade)
        .toUriString();

    var journalposterForBidragRequest = restTemplate.exchange(uri, HttpMethod.GET, null, typereferansenErListeMedJournalposter());
    var httpStatus = journalposterForBidragRequest.getStatusCode();

    LOGGER.info(
        "Fikk http status {} fra journalposter i bidragssak med saksnummer {} på fagområde {} fra bidrag-dokuemnt-arkiv",
        httpStatus, saksnummer, fagomrade
    );

    var journalposter = journalposterForBidragRequest.getBody();

    return Optional.ofNullable(journalposter).orElse(Collections.emptyList());
  }

  private static ParameterizedTypeReference<List<JournalpostDto>> typereferansenErListeMedJournalposter() {
    return new ParameterizedTypeReference<>() {
    };
  }
}
