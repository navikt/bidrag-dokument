package no.nav.bidrag.dokument.consumer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import no.nav.bidrag.commons.web.HttpStatusResponse;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.OpprettAvvikshendelseResponse;
import no.nav.bidrag.dokument.dto.RegistrereJournalpostCommand;
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
  private static final String PATH_JOURNALPOST_UTEN_SAKSTILGANG = "/journal/%s";
  private static final String PATH_SAK_JOURNAL = "/sak/%s/journal";
  private static final String PARAM_FAGOMRADE = "fagomrade";

  private final RestTemplate restTemplate;

  public BidragJournalpostConsumer(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public List<JournalpostDto> finnJournalposter(String saksnummer, String fagomrade) {
    var uri = UriComponentsBuilder.fromPath(String.format(PATH_SAK_JOURNAL, saksnummer))
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
    String path;

    if (saksnummer != null) {
      path = String.format(PATH_JOURNALPOST, saksnummer, id);
    } else {
      path = String.format(PATH_JOURNALPOST_UTEN_SAKSTILGANG, id);
    }

    LOGGER.info("Hent journalpost fra bidrag-dokument-journalpost{}", path);

    var exchange = restTemplate.exchange(path, HttpMethod.GET, null, JournalpostDto.class);

    LOGGER.info("Hent journalpost fikk http status {} fra bidrag-dokument-journalpost", exchange.getStatusCode());
    return new HttpStatusResponse<>(exchange.getStatusCode(), exchange.getBody());
  }

  public HttpStatusResponse<Void> endre(String saksnummer, EndreJournalpostCommand endreJournalpostCommand) {
    var path = String.format(PATH_JOURNALPOST, saksnummer, endreJournalpostCommand.getJournalpostId());
    LOGGER.info("Endre journalpost BidragDokument: {}, path {}", endreJournalpostCommand, path);

    var endretJournalpostResponse = restTemplate.exchange(path, HttpMethod.PUT, new HttpEntity<>(endreJournalpostCommand), Void.class);

    LOGGER.info("Endre journalpost fikk http status {}", endretJournalpostResponse.getStatusCode());
    return new HttpStatusResponse<>(endretJournalpostResponse.getStatusCode());
  }

  public HttpStatusResponse<Void> registrer(RegistrereJournalpostCommand registrereJournalpostCommand) {
    var path = String.format(PATH_JOURNALPOST_UTEN_SAKSTILGANG, registrereJournalpostCommand.getJournalpostId());
    LOGGER.info("Registrer journalpost: {}, path {}", registrereJournalpostCommand, path);

    var registrereJournalpostResponse = restTemplate.exchange(path, HttpMethod.PUT, new HttpEntity<>(registrereJournalpostCommand), Void.class);
    return new HttpStatusResponse<>(registrereJournalpostResponse.getStatusCode());
  }

  public HttpStatusResponse<List<AvvikType>> finnAvvik(String saksnummer, String journalpostId) {
    String path;

    if (saksnummer != null) {
      path = String.format(PATH_JOURNALPOST, saksnummer, journalpostId) + "/avvik";
    } else {
      path = String.format(PATH_JOURNALPOST_UTEN_SAKSTILGANG, journalpostId) + "/avvik";
    }

    LOGGER.info("Finner avvik på journalpost fra bidrag-dokument-journalpost{}", path);

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
