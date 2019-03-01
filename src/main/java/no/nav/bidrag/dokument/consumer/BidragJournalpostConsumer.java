package no.nav.bidrag.dokument.consumer;

import static no.nav.bidrag.dokument.BidragDokumentConfig.ISSUER;
import static no.nav.bidrag.dokument.consumer.ConsumerUtil.initHttpEntityWithSecurityHeader;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommandDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.NyJournalpostCommandDto;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.oidc.context.TokenContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class BidragJournalpostConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(BidragJournalpostConsumer.class);
  private static final String PATH_JOURNALPOST = "/journalpost";
  private static final String PATH_SAK = "/sakjournal/";
  private static final String PARAM_FAGOMRADE = "fagomrade";

  private final OIDCRequestContextHolder securityContextHolder;
  private final RestTemplate restTemplate;

  public BidragJournalpostConsumer(OIDCRequestContextHolder securityContextHolder, RestTemplate restTemplate) {
    this.securityContextHolder = securityContextHolder;
    this.restTemplate = restTemplate;
  }

  public List<JournalpostDto> finnJournalposter(String saksnummer, String fagomrade) {
    String path = PATH_SAK + saksnummer;

    String uri = UriComponentsBuilder.fromPath(path)
        .queryParam(PARAM_FAGOMRADE, fagomrade)
        .toUriString();

    ResponseEntity<List<JournalpostDto>> journalposterForBidragRequest = restTemplate.exchange(
        uri, HttpMethod.GET, initHttpEntityWithSecurityHeader(null, getBearerToken()), typereferansenErListeMedJournalposter()
    );

    HttpStatus httpStatus = journalposterForBidragRequest.getStatusCode();
    LOGGER.info("Fikk http status {} fra journalposter i bidragssak med saksnummer {} på fagområde {}", httpStatus, saksnummer, fagomrade);
    List<JournalpostDto> journalposter = journalposterForBidragRequest.getBody();

    return journalposter != null ? journalposter : Collections.emptyList();
  }

  private static ParameterizedTypeReference<List<JournalpostDto>> typereferansenErListeMedJournalposter() {
    return new ParameterizedTypeReference<>() {
    };
  }

  public Optional<JournalpostDto> registrer(NyJournalpostCommandDto nyJournalpostCommandDto) {
    String path = PATH_JOURNALPOST + "/ny";

    ResponseEntity<JournalpostDto> registrertJournalpost = restTemplate.exchange(
        path, HttpMethod.POST, initHttpEntityWithSecurityHeader(nyJournalpostCommandDto, getBearerToken()), JournalpostDto.class
    );

    HttpStatus httpStatus = registrertJournalpost.getStatusCode();
    LOGGER.info("Fikk http status {} fra registrer ny journalpost: {}", httpStatus, nyJournalpostCommandDto);

    return Optional.ofNullable(registrertJournalpost.getBody());
  }

  public Optional<JournalpostDto> hentJournalpost(Integer id) {
    String path = PATH_JOURNALPOST + '/' + id;

    Optional<ResponseEntity<JournalpostDto>> possibleExchange = Optional.ofNullable(
        restTemplate.exchange(path, HttpMethod.GET, initHttpEntityWithSecurityHeader(null, getBearerToken()), JournalpostDto.class
        )
    );

    possibleExchange.ifPresent(
        (response) -> LOGGER.info("Hent journalpost fikk http status {} fra bidrag-dokument-journalpost", response.getStatusCode())
    );

    return possibleExchange.map(ResponseEntity::getBody);
  }

  public Optional<JournalpostDto> endre(EndreJournalpostCommandDto endreJournalpostCommandDto) {
    LOGGER.info("Endre journalpost BidragDokument: " + endreJournalpostCommandDto);

    Optional<ResponseEntity<JournalpostDto>> possibleExchange = Optional.ofNullable(
        restTemplate.exchange(
            PATH_JOURNALPOST + '/' + endreJournalpostCommandDto.getJournalpostId(),
            HttpMethod.PUT,
            initHttpEntityWithSecurityHeader(endreJournalpostCommandDto, getBearerToken()),
            JournalpostDto.class
        )
    );

    possibleExchange.ifPresent(
        (responseEntity) -> LOGGER.info("Endre journalpost fikk http status {}, body: ", responseEntity.getStatusCode(), endreJournalpostCommandDto)
    );

    return possibleExchange.map(ResponseEntity::getBody);
  }

  private String getBearerToken() {
    String token = "Bearer " + featchBearerToken();
    LOGGER.info("Using token: " + token);

    return token;
  }

  private String featchBearerToken() {
    return Optional.ofNullable(securityContextHolder)
        .map(OIDCRequestContextHolder::getOIDCValidationContext)
        .map(oidcValidationContext -> oidcValidationContext.getToken(ISSUER))
        .map(TokenContext::getIdToken)
        .orElseThrow(() -> new IllegalStateException("Kunne ikke videresende Bearer token"));
  }
}
