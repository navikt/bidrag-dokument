package no.nav.bidrag.dokument.consumer;

import static no.nav.bidrag.dokument.BidragDokumentConfig.ISSUER;
import static no.nav.bidrag.dokument.consumer.ConsumerUtil.addSecurityHeader;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import no.nav.bidrag.dokument.dto.BidragSakDto;
import no.nav.security.oidc.context.OIDCRequestContextHolder;

public class BidragSakConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BidragSakConsumer.class);

    private final String baseUrlBidragSak;
    private final OIDCRequestContextHolder securityContextHolder;

    public BidragSakConsumer(
            String baseUrlBidragSak,
            OIDCRequestContextHolder securityContextHolder) {

        this.baseUrlBidragSak = baseUrlBidragSak;
        this.securityContextHolder = securityContextHolder;
    }

    public List<BidragSakDto> finnInnvolverteSaker(String foedselsnummer) {
        RestTemplate restTemplate = RestTemplateFactory.create(baseUrlBidragSak);
        String uri = UriComponentsBuilder.fromPath("/person/sak/" + foedselsnummer)
                .toUriString();

        var sakerForPersonResponse = restTemplate.exchange(
                uri, HttpMethod.GET, addSecurityHeader(null, getBearerToken()), listeMedBidragSakDtoType());

        HttpStatus httpStatus = sakerForPersonResponse.getStatusCode();
        LOGGER.info("Fikk http status {} fra bidrag-sak/{}", httpStatus, uri);
        var sakerForPerson = sakerForPersonResponse.getBody();

        return sakerForPerson != null ? sakerForPerson : Collections.emptyList();
    }

    private static ParameterizedTypeReference<List<BidragSakDto>> listeMedBidragSakDtoType() {
        return new ParameterizedTypeReference<>() {
        };
    }

    private String getBearerToken() {
        return "Bearer " + securityContextHolder.getOIDCValidationContext().getToken(ISSUER).getIdToken();
    }
}
