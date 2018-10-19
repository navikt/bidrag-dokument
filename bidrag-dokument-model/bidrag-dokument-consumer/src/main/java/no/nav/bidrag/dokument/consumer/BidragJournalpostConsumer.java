package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.dokument.domain.bisys.BidragJournalpostDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class BidragJournalpostConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BidragJournalpostConsumer.class);

    private final String restServiceUrlBidragJournalpost;
    private final MyLogger myLogger;

    public BidragJournalpostConsumer(String restServiceUrlBidragJournalpost) {
        this(restServiceUrlBidragJournalpost, () -> LOGGER);
    }

    BidragJournalpostConsumer(String restServiceUrlBidragJournalpost, MyLogger myLogger) {
        this.restServiceUrlBidragJournalpost = restServiceUrlBidragJournalpost;
        this.myLogger = myLogger;
    }

    public List<BidragJournalpostDto> finnJournalposter(String saksnummer) {
        RestTemplate restTemplate = RestTemplateFactory.create();
        ResponseEntity<List<BidragJournalpostDto>> journalposterForBidragRequest = restTemplate.exchange(
                restServiceUrlBidragJournalpost + saksnummer, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<BidragJournalpostDto>>() {
                }
        );

        HttpStatus httpStatus = journalposterForBidragRequest.getStatusCode();
        myLogger.getLogger().info("Fikk http status {} fra journalposter i bidragssak med saksnummer {} - {}", httpStatus, saksnummer, httpStatus.getReasonPhrase());

        return journalposterForBidragRequest.getBody();
    }

    @FunctionalInterface
    interface MyLogger {
        Logger getLogger();
    }
}
