package no.nav.bidrag.dokument.consumer;

import static no.nav.bidrag.commons.web.EnhetFilter.X_ENHET_HEADER;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import no.nav.bidrag.commons.web.HttpResponse;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.BehandleAvvikshendelseResponse;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.JournalpostResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

public class BidragDokumentConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(BidragDokumentConsumer.class);
  private static final String PATH_JOURNAL = "/sak/%s/journal";
  public static final String PATH_JOURNALPOST_UTEN_SAK = "/journal/%s";
  public static final String PATH_SAK_JOURNAL = "/sak/%s/journal";
  private static final String PATH_JOURNALPOST = "/journal/%s";
  private static final String PATH_JOURNALPOST_MED_SAKPARAM = "/journal/%s?saksnummer=%s";
  private static final String PARAM_FAGOMRADE = "fagomrade";
  private static final String PARAM_SAKSNUMMER = "saksnummer";
  public static final String PATH_AVVIK_PA_JOURNALPOST_MED_SAK_PARAM = "/journal/%s/avvik?" + PARAM_SAKSNUMMER + "=%s";
  public static final String PATH_AVVIK_PA_JOURNALPOST = "/journal/%s/avvik";

  private final ConsumerTarget consumerTarget;

  public BidragDokumentConsumer(ConsumerTarget consumerTarget) {
    this.consumerTarget = consumerTarget;
  }

  private static ParameterizedTypeReference<List<JournalpostDto>> typereferansenErListeMedJournalposter() {
    return new ParameterizedTypeReference<>() {
    };
  }

  public HttpResponse<List<AvvikType>> finnAvvik(String saksnummer, String journalpostId) {
    String path;

    if (saksnummer != null) {
      path = String.format(PATH_AVVIK_PA_JOURNALPOST_MED_SAK_PARAM, journalpostId, saksnummer);
    } else {
      path = String.format(PATH_AVVIK_PA_JOURNALPOST, journalpostId);
    }

    LOGGER.info("Finner avvik på journalpost fra {}{}", consumerTarget.getTargetApp(), path);

    var avviksResponse = consumerTarget.henteRestTemplateForIssuer().exchange(path, HttpMethod.GET, null, typereferansenErListeMedAvvikstyper());
    return new HttpResponse<>(avviksResponse);
  }


  public HttpResponse<BehandleAvvikshendelseResponse> behandleAvvik(String enhetsnummer, String journalpostId, Avvikshendelse avvikshendelse) {
    var path = String.format(PATH_JOURNALPOST_UTEN_SAK + "/avvik", journalpostId);
    LOGGER.info("{}{}: {}", consumerTarget.getTargetApp(), path, avvikshendelse);

    var avviksResponse = consumerTarget.henteRestTemplateForIssuer()
        .exchange(path, HttpMethod.POST, new HttpEntity<>(avvikshendelse, createEnhetHeader(enhetsnummer)), BehandleAvvikshendelseResponse.class);

    return new HttpResponse<>(avviksResponse);
  }

  public HttpResponse<JournalpostResponse> hentJournalpost(String saksnummer, String id) {
    String url;

    if (saksnummer == null) {
      url = String.format(PATH_JOURNALPOST, id);
    } else {
      url = String.format(PATH_JOURNALPOST_MED_SAKPARAM, id, saksnummer);
    }

    var journalpostExchange = consumerTarget.henteRestTemplateForIssuer().exchange(url, HttpMethod.GET, null, JournalpostResponse.class);

    LOGGER.info("Hent journalpost fikk http status {} fra {}", journalpostExchange.getStatusCode(), consumerTarget.getTargetApp());

    return new HttpResponse<>(journalpostExchange);
  }

  public List<JournalpostDto> finnJournalposter(String saksnummer, String fagomrade) {
    var uri = UriComponentsBuilder.fromPath(String.format(PATH_JOURNAL, saksnummer)).queryParam(PARAM_FAGOMRADE, fagomrade).toUriString();

    var journalposterFraArkiv = consumerTarget.henteRestTemplateForIssuer()
        .exchange(uri, HttpMethod.GET, null, typereferansenErListeMedJournalposter());
    var httpStatus = journalposterFraArkiv.getStatusCode();

    var responseBody = journalposterFraArkiv.getBody();
    var antallSaker = responseBody == null ? 0 : responseBody.size();
    LOGGER.info("Fikk http status {} fra journalposter i bidragssak med saksnummer {} på fagområde {} og med {} saker i respons fra {}", httpStatus,
        saksnummer, fagomrade, antallSaker, consumerTarget.getTargetApp());

    return Optional.ofNullable(responseBody).orElse(Collections.emptyList());
  }

  public HttpResponse<Void> endre(String enhet, EndreJournalpostCommand endreJournalpostCommand) {
    var path = String.format(PATH_JOURNALPOST_UTEN_SAK, endreJournalpostCommand.getJournalpostId());
    LOGGER.info("Endre journalpost BidragDokument: {}, path {}", endreJournalpostCommand, path);

    var endretJournalpostResponse = consumerTarget.henteRestTemplateForIssuer()
        .exchange(path, HttpMethod.PATCH, new HttpEntity<>(endreJournalpostCommand, createEnhetHeader(enhet)), Void.class);

    LOGGER.info("Endre journalpost fikk http status {}", endretJournalpostResponse.getStatusCode());

    return new HttpResponse<>(endretJournalpostResponse);
  }

  public static HttpHeaders createEnhetHeader(String enhet) {
    var header = new HttpHeaders();
    header.add(X_ENHET_HEADER, enhet);
    return header;
  }

  private ParameterizedTypeReference<List<AvvikType>> typereferansenErListeMedAvvikstyper() {
    return new ParameterizedTypeReference<>() {
    };
  }
}
