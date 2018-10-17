package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.dokument.domain.bisys.BidragJournalpostDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class BidragJournalpostConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BidragJournalpostConsumer.class);

    private final String restServiceUrl;

    public BidragJournalpostConsumer(String restServiceUrl) {
        this.restServiceUrl = restServiceUrl;
    }

    public List<BidragJournalpostDto> finnJournalposter(String saksnummer) {
        RestTemplate restTemplate = RestTemplateFactory.create();
        ResponseEntity<List> journalposterForBidragRequest = restTemplate.getForEntity(restServiceUrl, List.class);

        if (LOGGER.isDebugEnabled()) {
            HttpStatus httpStatus = journalposterForBidragRequest.getStatusCode();

            if (httpStatus.is2xxSuccessful()) {
                LOGGER.trace(String.format("Journalposter for bidrag for saksnummer %s har http status %s - %s", saksnummer, httpStatus, httpStatus.getReasonPhrase()));
            } else {
                LOGGER.debug(String.format("Journalposter for bidrag for saksnummer %s har http status %s - %s", saksnummer, httpStatus, httpStatus.getReasonPhrase()));
            }
        }

        //noinspection unchecked
        return (List<BidragJournalpostDto>) journalposterForBidragRequest.getBody();
    }
}
