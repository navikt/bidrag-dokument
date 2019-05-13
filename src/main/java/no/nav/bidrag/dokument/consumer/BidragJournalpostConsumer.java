package no.nav.bidrag.dokument.consumer;

import java.util.Collections;
import java.util.List;
import no.nav.bidrag.commons.web.HttpStatusResponse;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommandDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class BidragJournalpostConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(BidragJournalpostConsumer.class);
  private static final String PATH_JOURNALPOST = "/journalpost";
  private static final String PATH_SAK = "/sakjournal/";
  private static final String PARAM_FAGOMRADE = "fagomrade";

  private final RestTemplate restTemplate;

  public BidragJournalpostConsumer(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public HttpStatusResponse<List<JournalpostDto>> finnJournalposter(String saksnummer, String fagomrade) {
    String uri = UriComponentsBuilder.fromPath(PATH_SAK + saksnummer)
        .queryParam(PARAM_FAGOMRADE, fagomrade)
        .toUriString();

    var journalposterForBidragRequest = restTemplate.exchange(uri, HttpMethod.GET, null, typereferansenErListeMedJournalposter());

    HttpStatus httpStatus = journalposterForBidragRequest.getStatusCode();
    LOGGER.info("Fikk http status {} fra journalposter i bidragssak med saksnummer {} på fagområde {}", httpStatus, saksnummer, fagomrade);
    List<JournalpostDto> journalposter = journalposterForBidragRequest.getBody();

    return new HttpStatusResponse<>(journalposterForBidragRequest.getStatusCode(), journalposter != null ? journalposter : Collections.emptyList());
  }

  private static ParameterizedTypeReference<List<JournalpostDto>> typereferansenErListeMedJournalposter() {
    return new ParameterizedTypeReference<>() {
    };
  }

  public HttpStatusResponse<JournalpostDto> hentJournalpost(Integer id) {
    String path = PATH_JOURNALPOST + '/' + id;

    var exchange = restTemplate.exchange(path, HttpMethod.GET, null, JournalpostDto.class);

    LOGGER.info("Hent journalpost fikk http status {} fra bidrag-dokument-journalpost", exchange.getStatusCode());
    return new HttpStatusResponse<>(exchange.getStatusCode(), exchange.getBody());
  }

  public HttpStatusResponse<JournalpostDto> endre(EndreJournalpostCommandDto endreJournalpostCommandDto) {
    LOGGER.info("Endre journalpost BidragDokument: " + endreJournalpostCommandDto);

    var endretJournalpostResponse = restTemplate.exchange(
        PATH_JOURNALPOST + '/' + endreJournalpostCommandDto.getJournalpostId(),
        HttpMethod.PUT,
        new HttpEntity<>(endreJournalpostCommandDto),
        JournalpostDto.class
    );

    LOGGER.info("Endre journalpost fikk http status {}, body: {}", endretJournalpostResponse.getStatusCode(), endretJournalpostResponse.getBody());
    return new HttpStatusResponse<>(endretJournalpostResponse.getStatusCode(), endretJournalpostResponse.getBody());
  }
}
