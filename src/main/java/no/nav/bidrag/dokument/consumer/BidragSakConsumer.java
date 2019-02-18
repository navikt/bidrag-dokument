package no.nav.bidrag.dokument.consumer;

import static no.nav.bidrag.dokument.BidragDokumentConfig.ISSUER;
import static no.nav.bidrag.dokument.consumer.ConsumerUtil.addSecurityHeader;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import no.nav.bidrag.dokument.dto.BidragSakDto;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class BidragSakConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(BidragSakConsumer.class);
  private static final String PATH_PERSON_SAK = "/person/sak/";

  private final OIDCRequestContextHolder securityContextHolder;
  private final RestTemplate restTemplate;

  public BidragSakConsumer(OIDCRequestContextHolder securityContextHolder, RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
    this.securityContextHolder = securityContextHolder;
  }

  public List<BidragSakDto> finnInnvolverteSaker(String foedselsnummer) {
    String uri = UriComponentsBuilder.fromPath(PATH_PERSON_SAK + foedselsnummer)
        .toUriString();

    var sakerForPersonResponse = restTemplate.exchange(
        uri, HttpMethod.GET, addSecurityHeader(null, getBearerToken()), listeMedBidragSakDtoType()
    );

    HttpStatus httpStatus = sakerForPersonResponse.getStatusCode();
    LOGGER.info("Fikk http status {} fra bidrag-sak/{}", httpStatus, uri);
    var sakerForPerson = Optional.ofNullable(sakerForPersonResponse.getBody());

    return sakerForPerson.orElse(Collections.emptyList());
  }

  private static ParameterizedTypeReference<List<BidragSakDto>> listeMedBidragSakDtoType() {
    return new ParameterizedTypeReference<>() {
    };
  }

  private String getBearerToken() {
    return "Bearer " + securityContextHolder.getOIDCValidationContext().getToken(ISSUER).getIdToken();
  }
}
