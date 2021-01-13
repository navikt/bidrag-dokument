package no.nav.bidrag.dokument.consumer;

import static no.nav.bidrag.commons.web.EnhetFilter.X_ENHET_HEADER;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import no.nav.bidrag.commons.web.HttpResponse;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.JournalpostResponse;
import no.nav.bidrag.dokument.dto.OpprettAvvikshendelseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class BidragJournalpostConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(BidragJournalpostConsumer.class);
  private static final String PARAM_FAGOMRADE = "fagomrade";
  private static final String PARAM_SAKSNUMMER = "saksnummer";
  private static final String PATH_AVVIK_PA_JOURNALPOST = "/journal/%s/avvik";
  private static final String PATH_AVVIK_PA_JOURNALPOST_MED_SAK_PARAM =
      "/journal/%s/avvik?" + PARAM_SAKSNUMMER + "=%s";
  private static final String PATH_JOURNALPOST_MED_SAKPARAM =
      "/journal/%s?" + PARAM_SAKSNUMMER + "=%s";
  private static final String PATH_JOURNALPOST_UTEN_SAK = "/journal/%s";
  private static final String PATH_SAK_JOURNAL = "/sak/%s/journal";

  private final RestTemplate restTemplate;

  public BidragJournalpostConsumer(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private static ParameterizedTypeReference<List<JournalpostDto>>
      typereferansenErListeMedJournalposter() {
    return new ParameterizedTypeReference<>() {};
  }

  public static HttpHeaders createEnhetHeader(String enhet) {
    var header = new HttpHeaders();
    header.add(X_ENHET_HEADER, enhet);
    return header;
  }

  public List<JournalpostDto> finnJournalposter(String saksnummer, String fagomrade) {
    var uri =
        UriComponentsBuilder.fromPath(String.format(PATH_SAK_JOURNAL, saksnummer))
            .queryParam(PARAM_FAGOMRADE, fagomrade)
            .toUriString();

    var journalposterFraBrevlagerRequest =
        restTemplate.exchange(uri, HttpMethod.GET, null, typereferansenErListeMedJournalposter());
    var httpStatus = journalposterFraBrevlagerRequest.getStatusCode();

    LOGGER.info(
        "Fikk http status {} fra journalposter i bidragssak med saksnummer {} på fagområde {}",
        httpStatus,
        saksnummer,
        fagomrade);

    return Optional.ofNullable(journalposterFraBrevlagerRequest.getBody())
        .orElse(Collections.emptyList());
  }

  public HttpResponse<JournalpostResponse> hentJournalpostResponse(String saksnummer, String id) {
    String path;

    if (saksnummer != null) {
      path = String.format(PATH_JOURNALPOST_MED_SAKPARAM, id, saksnummer);
    } else {
      path = String.format(PATH_JOURNALPOST_UTEN_SAK, id);
    }

    LOGGER.info("Hent journalpost fra bidrag-dokument-journalpost{}", path);
    var exchange = restTemplate.exchange(path, HttpMethod.GET, null, JournalpostResponse.class);

    LOGGER.info(
        "Hent journalpost fikk http status {} fra bidrag-dokument-journalpost",
        exchange.getStatusCode());
    return new HttpResponse<>(exchange);
  }

  public HttpResponse<Void> endre(String enhet, EndreJournalpostCommand endreJournalpostCommand) {
    var path = String.format(PATH_JOURNALPOST_UTEN_SAK, endreJournalpostCommand.getJournalpostId());
    LOGGER.info("Endre journalpost BidragDokument: {}, path {}", endreJournalpostCommand, path);

    var endretJournalpostResponse =
        restTemplate.exchange(
            path,
            HttpMethod.PUT,
            new HttpEntity<>(endreJournalpostCommand, createEnhetHeader(enhet)),
            Void.class);

    LOGGER.info("Endre journalpost fikk http status {}", endretJournalpostResponse.getStatusCode());
    return new HttpResponse<>(endretJournalpostResponse);
  }

  public HttpResponse<List<AvvikType>> finnAvvik(String saksnummer, String journalpostId) {
    String path;

    if (saksnummer != null) {
      path = String.format(PATH_AVVIK_PA_JOURNALPOST_MED_SAK_PARAM, journalpostId, saksnummer);
    } else {
      path = String.format(PATH_AVVIK_PA_JOURNALPOST, journalpostId);
    }

    LOGGER.info("Finner avvik på journalpost fra bidrag-dokument-journalpost{}", path);

    var avviksResponse =
        restTemplate.exchange(path, HttpMethod.GET, null, typereferansenErListeMedAvvikstyper());
    return new HttpResponse<>(avviksResponse);
  }

  private ParameterizedTypeReference<List<AvvikType>> typereferansenErListeMedAvvikstyper() {
    return new ParameterizedTypeReference<>() {};
  }

  public HttpResponse<OpprettAvvikshendelseResponse> opprettAvvik(
      String enhetsnummer, String journalpostId, Avvikshendelse avvikshendelse) {
    var path = String.format(PATH_JOURNALPOST_UTEN_SAK + "/avvik", journalpostId);
    LOGGER.info("bidrag-dokument-journalpost{}: {}", path, avvikshendelse);

    var avviksResponse =
        restTemplate.exchange(
            path,
            HttpMethod.POST,
            new HttpEntity<>(avvikshendelse, createEnhetHeader(enhetsnummer)),
            OpprettAvvikshendelseResponse.class);

    return new HttpResponse<>(avviksResponse);
  }
}
