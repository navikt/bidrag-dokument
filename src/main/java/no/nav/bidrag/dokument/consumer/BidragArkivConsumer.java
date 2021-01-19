package no.nav.bidrag.dokument.consumer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import no.nav.bidrag.commons.web.HttpResponse;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.JournalpostResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

public class BidragArkivConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(BidragArkivConsumer.class);
  private static final String PATH_JOURNAL = "/sak/%s/journal";
  private static final String PATH_JOURNALPOST = "/journal/%s";
  private static final String PATH_JOURNALPOST_MED_SAKPARAM = "/journal/%s?saksnummer=%s";
  private static final String PARAM_FAGOMRADE = "fagomrade";

  private final ConsumerTarget consumerTarget;

  public BidragArkivConsumer(ConsumerTarget consumerTarget) {
    this.consumerTarget = consumerTarget;
  }

  private static ParameterizedTypeReference<List<JournalpostDto>> typereferansenErListeMedJournalposter() {
    return new ParameterizedTypeReference<>() {
    };
  }

  public HttpResponse<JournalpostResponse> hentJournalpost(String saksnummer, String id) {
    String url;

    if (saksnummer == null) {
      url = String.format(PATH_JOURNALPOST, id);
    } else {
      url = String.format(PATH_JOURNALPOST_MED_SAKPARAM, id, saksnummer);
    }

    var journalpostExchange = consumerTarget.henteRestTemplateForIssuer().exchange(url, HttpMethod.GET, null, JournalpostResponse.class);

    LOGGER.info("Hent journalpost fikk http status {} fra bidrag-dokument-arkiv", journalpostExchange.getStatusCode());

    return new HttpResponse<>(journalpostExchange);
  }

  public List<JournalpostDto> finnJournalposter(String saksnummer, String fagomrade) {
    var uri = UriComponentsBuilder.fromPath(String.format(PATH_JOURNAL, saksnummer)).queryParam(PARAM_FAGOMRADE, fagomrade).toUriString();

    var journalposterFraArkiv = consumerTarget.henteRestTemplateForIssuer()
        .exchange(uri, HttpMethod.GET, null, typereferansenErListeMedJournalposter());
    var httpStatus = journalposterFraArkiv.getStatusCode();

    LOGGER.info("Fikk http status {} fra journalposter i bidragssak med saksnummer {} på fagområde {} fra bidrag-dokuemnt-arkiv", httpStatus,
        saksnummer, fagomrade);

    return Optional.ofNullable(journalposterFraArkiv.getBody()).orElse(Collections.emptyList());
  }
}
