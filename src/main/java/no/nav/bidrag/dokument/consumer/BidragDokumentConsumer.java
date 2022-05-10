package no.nav.bidrag.dokument.consumer;

import static no.nav.bidrag.commons.web.EnhetFilter.X_ENHET_HEADER;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import no.nav.bidrag.commons.web.HttpResponse;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.BehandleAvvikshendelseResponse;
import no.nav.bidrag.dokument.dto.DistribuerJournalpostRequest;
import no.nav.bidrag.dokument.dto.DistribuerJournalpostResponse;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.JournalpostResponse;
import org.apache.logging.log4j.util.Strings;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class BidragDokumentConsumer {

  private static final String PATH_JOURNAL = "/sak/%s/journal";
  public static final String PATH_JOURNALPOST_UTEN_SAK = "/journal/%s";
  public static final String PATH_SAK_JOURNAL = "/sak/%s/journal";
  private static final String PATH_JOURNALPOST = "/journal/%s";
  private static final String PATH_DISTRIBUER = "/journal/distribuer/%s";
  private static final String PATH_DISTRIBUER_ENABLED = "/journal/distribuer/%s/enabled";
  private static final String PATH_JOURNALPOST_MED_SAKPARAM = "/journal/%s?saksnummer=%s";
  private static final String PARAM_FAGOMRADE = "fagomrade";
  private static final String PARAM_BATCHID = "batchId";
  private static final String PARAM_SAKSNUMMER = "saksnummer";
  public static final String PATH_AVVIK_PA_JOURNALPOST_MED_SAK_PARAM = "/journal/%s/avvik?" + PARAM_SAKSNUMMER + "=%s";
  public static final String PATH_AVVIK_PA_JOURNALPOST = "/journal/%s/avvik";
  public static final String PATH_HENT_DOKUMENT = "/dokument/%s";

  private final RestTemplate restTemplate;

  public BidragDokumentConsumer(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
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

    var avviksResponse = restTemplate.exchange(path, HttpMethod.GET, null, typereferansenErListeMedAvvikstyper());
    return new HttpResponse<>(avviksResponse);
  }


  public HttpResponse<BehandleAvvikshendelseResponse> behandleAvvik(String enhetsnummer, String journalpostId, Avvikshendelse avvikshendelse) {
    var path = String.format(PATH_JOURNALPOST_UTEN_SAK + "/avvik", journalpostId);

    var avviksResponse = restTemplate
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

    var journalpostExchange = restTemplate.exchange(url, HttpMethod.GET, null, JournalpostResponse.class);

    return new HttpResponse<>(journalpostExchange);
  }

  public List<JournalpostDto> finnJournalposter(String saksnummer, String fagomrade) {
    var uri = UriComponentsBuilder.fromPath(String.format(PATH_JOURNAL, saksnummer)).queryParam(PARAM_FAGOMRADE, fagomrade).toUriString();

    var journalposterFraArkiv = restTemplate
        .exchange(uri, HttpMethod.GET, null, typereferansenErListeMedJournalposter());

    return Optional.ofNullable(journalposterFraArkiv.getBody()).orElse(Collections.emptyList());
  }

  public HttpResponse<Void> endre(String enhet, EndreJournalpostCommand endreJournalpostCommand) {
    var path = String.format(PATH_JOURNALPOST_UTEN_SAK, endreJournalpostCommand.getJournalpostId());

    var endretJournalpostResponse = restTemplate
        .exchange(path, HttpMethod.PATCH, new HttpEntity<>(endreJournalpostCommand, createEnhetHeader(enhet)), Void.class);


    return new HttpResponse<>(endretJournalpostResponse);
  }

  public HttpResponse<DistribuerJournalpostResponse> distribuerJournalpost(String journalpostId, String batchId, DistribuerJournalpostRequest distribuerJournalpostRequest) {
    var uriBuilder = UriComponentsBuilder.fromPath(String.format(PATH_DISTRIBUER, journalpostId));
    if (Strings.isNotEmpty(batchId)){
      uriBuilder = uriBuilder.queryParam(PARAM_BATCHID, batchId);
    }

    var uri = uriBuilder.toUriString();

    var distribuerJournalpostResponse = restTemplate
        .exchange(uri, HttpMethod.POST, new HttpEntity<>(distribuerJournalpostRequest), DistribuerJournalpostResponse.class);

    return new HttpResponse<>(distribuerJournalpostResponse);
  }

  public HttpResponse<Void> kanDistribuereJournalpost(String journalpostId) {
    var path = String.format(PATH_DISTRIBUER_ENABLED, journalpostId);

    var distribuerJournalpostResponse = restTemplate.exchange(path, HttpMethod.GET, null, Void.class);

    return new HttpResponse<>(distribuerJournalpostResponse);
  }

  public ResponseEntity<byte[]> hentDokument(String journalpostId, String dokumentreferanse) {
    var dokumentReferanseUrl = Strings.isNotEmpty(dokumentreferanse) ? "/" +dokumentreferanse : "";
    var dokumentUrl = String.format(PATH_HENT_DOKUMENT, journalpostId) + dokumentReferanseUrl;

    return restTemplate.exchange(dokumentUrl, HttpMethod.GET, HttpEntity.EMPTY, byte[].class);
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
