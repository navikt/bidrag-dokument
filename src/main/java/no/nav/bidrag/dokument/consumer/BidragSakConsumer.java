package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.dokument.dto.BidragSakDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

import static no.nav.bidrag.dokument.consumer.ConsumerUtil.addSecurityHeader;

public class BidragSakConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BidragSakConsumer.class);

    private final String baseUrlBidragSak;

    public BidragSakConsumer(String baseUrlBidragSak) {
        this.baseUrlBidragSak = baseUrlBidragSak;
    }

    public List<BidragSakDto> finnInnvolverteSaker(String foedselsnummer, String bearerToken) {
        RestTemplate restTemplate = RestTemplateFactory.create(baseUrlBidragSak, bearerToken);
        String uri = UriComponentsBuilder.fromPath("/person/sak/" + foedselsnummer)
                .toUriString();

        var sakerForPersonResponse = restTemplate.exchange(
                uri, HttpMethod.GET, addSecurityHeader(null, bearerToken), listeMedBidragSakDtoType()
        );

        HttpStatus httpStatus = sakerForPersonResponse.getStatusCode();
        LOGGER.info("Fikk http status {} fra bidrag-sak/{}", httpStatus, uri);
        var sakerForPerson = sakerForPersonResponse.getBody();

        return sakerForPerson != null ? sakerForPerson : Collections.emptyList();
    }

    private static ParameterizedTypeReference<List<BidragSakDto>> listeMedBidragSakDtoType() {
        return new ParameterizedTypeReference<>() {
        };
    }
}
