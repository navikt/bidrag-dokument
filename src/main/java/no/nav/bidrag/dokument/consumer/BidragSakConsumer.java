package no.nav.bidrag.dokument.consumer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import no.nav.bidrag.dokument.dto.BidragSakDto;
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

  private final RestTemplate restTemplate;

  public BidragSakConsumer(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public List<BidragSakDto> finnInnvolverteSaker(String foedselsnummer) {
    String uri = UriComponentsBuilder.fromPath(PATH_PERSON_SAK + foedselsnummer)
        .toUriString();

    var sakerForPersonResponse = restTemplate.exchange(uri, HttpMethod.GET, null, listeMedBidragSakDtoType());

    HttpStatus httpStatus = sakerForPersonResponse.getStatusCode();
    LOGGER.info("Fikk http status {} fra bidrag-sak/{}", httpStatus, uri);
    var sakerForPerson = Optional.ofNullable(sakerForPersonResponse.getBody());

    return sakerForPerson.orElse(Collections.emptyList());
  }

  private static ParameterizedTypeReference<List<BidragSakDto>> listeMedBidragSakDtoType() {
    return new ParameterizedTypeReference<>() {
    };
  }
}
