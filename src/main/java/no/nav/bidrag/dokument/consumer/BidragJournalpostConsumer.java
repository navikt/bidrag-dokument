package no.nav.bidrag.dokument.consumer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import no.nav.bidrag.commons.web.HttpStatusResponse;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommandDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.OpprettAvvikshendelseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class BidragJournalpostConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(BidragJournalpostConsumer.class);
  private static final String PATH_JOURNALPOST = "/sak/%s/journal/%s";
  private static final String PATH_SAK = "/sakjournal/";
  private static final String PARAM_FAGOMRADE = "fagomrade";

  private final RestTemplate restTemplate;

  public BidragJournalpostConsumer(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public List<JournalpostDto> finnJournalposter(String saksnummer, String fagomrade) {
    var uri = UriComponentsBuilder.fromPath(PATH_SAK + saksnummer)
        .queryParam(PARAM_FAGOMRADE, fagomrade)
        .toUriString();

    var journalposterFraBrevlagerRequest = restTemplate.exchange(uri, HttpMethod.GET, null, typereferansenErListeMedJournalposter());
    var httpStatus = journalposterFraBrevlagerRequest.getStatusCode();

    LOGGER.info("Fikk http status {} fra journalposter i bidragssak med saksnummer {} på fagområde {}", httpStatus, saksnummer, fagomrade);

    return Optional.ofNullable(journalposterFraBrevlagerRequest.getBody()).orElse(Collections.emptyList());
  }

  private static ParameterizedTypeReference<List<JournalpostDto>> typereferansenErListeMedJournalposter() {
    return new ParameterizedTypeReference<>() {
    };
  }

  public HttpStatusResponse<JournalpostDto> hentJournalpost(String saksnummer, String id) {
    var path = String.format(PATH_JOURNALPOST, saksnummer, id);
    LOGGER.info("Hent journalpost med id {} fra bidrag-dokument-journalpost", id);

    var exchange = restTemplate.exchange(path, HttpMethod.GET, null, JournalpostDto.class);

    LOGGER.info("Hent journalpost fikk http status {} fra bidrag-dokument-journalpost", exchange.getStatusCode());
    return new HttpStatusResponse<>(exchange.getStatusCode(), exchange.getBody());
  }

  public HttpStatusResponse<JournalpostDto> endre(String saksnummer, EndreJournalpostCommandDto endreJournalpostCommandDto) {
    var path = String.format(PATH_JOURNALPOST, saksnummer, endreJournalpostCommandDto.getJournalpostId());
    LOGGER.info("Endre journalpost BidragDokument: {}, path {}", endreJournalpostCommandDto, path);

    var endretJournalpostResponse = restTemplate.exchange(path, HttpMethod.PUT, new HttpEntity<>(endreJournalpostCommandDto), JournalpostDto.class);

    LOGGER.info("Endre journalpost fikk http status {}, body: {}", endretJournalpostResponse.getStatusCode(), endretJournalpostResponse.getBody());
    return new HttpStatusResponse<>(endretJournalpostResponse.getStatusCode(), endretJournalpostResponse.getBody());
  }

  public HttpStatusResponse<List<AvvikType>> finnAvvik(String saksnummer, String journalpostId) {
    var path = String.format(PATH_JOURNALPOST, saksnummer, journalpostId) + "/avvik";
    LOGGER.info("Finner avvik på journalpost med id {} fra bidrag-dokument-journalpost", journalpostId);

    var avviksResponse = restTemplate.exchange(path, HttpMethod.GET, null, typereferansenErListeMedAvvikstyper());
    return new HttpStatusResponse<>(avviksResponse.getStatusCode(), avviksResponse.getBody());
  }

  private ParameterizedTypeReference<List<AvvikType>> typereferansenErListeMedAvvikstyper() {
    return new ParameterizedTypeReference<>() {
    };
  }

  public HttpStatusResponse<OpprettAvvikshendelseResponse> opprettAvvik(String saksnummer, String journalpostId, Avvikshendelse avvikshendelse) {
    var path = String.format(PATH_JOURNALPOST, saksnummer, journalpostId) + "/avvik";
    LOGGER.info("Oppretter {} på journalpost med id {} fra bidrag-dokument-journalpost", avvikshendelse, journalpostId);

    var avviksResponse = restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(avvikshendelse), OpprettAvvikshendelseResponse.class);

    return new HttpStatusResponse<>(avviksResponse.getStatusCode(), avviksResponse.getBody());
  }
}
