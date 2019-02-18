package no.nav.bidrag.dokument.consumer;

import static no.nav.bidrag.dokument.BidragDokumentConfig.ISSUER;
import static no.nav.bidrag.dokument.consumer.ConsumerUtil.addSecurityHeader;

import java.util.Optional;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.oidc.context.TokenContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class BidragArkivConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(BidragArkivConsumer.class);
  private static final String PATH_JOURNALPOST = "/journalpost/";

  private final OIDCRequestContextHolder securityContextHolder;
  private final RestTemplate restTemplate;

  public BidragArkivConsumer(OIDCRequestContextHolder securityContextHolder, RestTemplate restTemplate) {
    this.securityContextHolder = securityContextHolder;
    this.restTemplate = restTemplate;
  }

  public Optional<JournalpostDto> hentJournalpost(Integer id) {
    ResponseEntity<JournalpostDto> journalforingDtoResponseEntity = restTemplate.exchange(
        PATH_JOURNALPOST + id, HttpMethod.GET, addSecurityHeader(null, getBearerToken()), JournalpostDto.class);

    HttpStatus httpStatus = journalforingDtoResponseEntity.getStatusCode();

    LOGGER.info("Journalpost med id={} har http status {}", id, httpStatus);

    return Optional.ofNullable(journalforingDtoResponseEntity.getBody());
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
