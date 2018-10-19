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

    private final String baseUrlBidragJournalpost;
    private final MyLogger myLogger;

    public BidragJournalpostConsumer(String baseUrlBidragJournalpost) {
        this(baseUrlBidragJournalpost, () -> LOGGER);
    }

    BidragJournalpostConsumer(String baseUrlBidragJournalpost, MyLogger myLogger) {
        this.baseUrlBidragJournalpost = baseUrlBidragJournalpost;
        this.myLogger = myLogger;
    }

    public List<BidragJournalpostDto> finnJournalposter(String saksnummer) {
        RestTemplate restTemplate = RestTemplateFactory.create();
        ResponseEntity<List<BidragJournalpostDto>> journalposterForBidragRequest = restTemplate.exchange(
                baseUrlBidragJournalpost + "/sak/" + saksnummer, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<BidragJournalpostDto>>() {
                }
        );

        HttpStatus httpStatus = journalposterForBidragRequest.getStatusCode();

        if (LOGGER.isDebugEnabled()) {
            if (httpStatus.is2xxSuccessful()) {
                LOGGER.trace(String.format("Fikk http status %s fra journalposter i bidragssak med saksnummer %s - %s", httpStatus, saksnummer, httpStatus.getReasonPhrase()));
            } else {
                LOGGER.debug(String.format("Fikk http status %s fra journalposter i bidragssak med saksnummer %s - %s", httpStatus, saksnummer, httpStatus.getReasonPhrase()));
            }
        }

        if (httpStatus.is4xxClientError() || httpStatus.is5xxServerError()) {
            myLogger.getLogger().warn(String.format("Fikk http status %s fra journalposter i bidragssak med saksnummer %s - %s", httpStatus, saksnummer, httpStatus.getReasonPhrase()));
        }

        return journalposterForBidragRequest.getBody();
    }

    @FunctionalInterface
    interface MyLogger {
        Logger getLogger();
    }
}
