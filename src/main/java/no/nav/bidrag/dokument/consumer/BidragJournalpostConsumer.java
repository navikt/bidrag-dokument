package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.dokument.dto.bisys.BidragJournalpostDto;
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
        RestTemplate restTemplate = RestTemplateFactory.create(restServiceUrlBidragJournalpost);
        ResponseEntity<List<BidragJournalpostDto>> journalposterForBidragRequest = restTemplate.exchange(
                saksnummer, HttpMethod.GET, null, typereferansenErListeMedJournalposter()
        );

        HttpStatus httpStatus = journalposterForBidragRequest.getStatusCode();
        String reasonPhrase = httpStatus != null ? httpStatus.getReasonPhrase() : null;

        myLogger.getLogger().info("Fikk http status {} fra journalposter i bidragssak med saksnummer {} - {}", httpStatus, saksnummer, reasonPhrase);

        return journalposterForBidragRequest.getBody();
    }

    private static ParameterizedTypeReference<List<BidragJournalpostDto>> typereferansenErListeMedJournalposter() {
        return new ParameterizedTypeReference<List<BidragJournalpostDto>>() {
        };
    }

    @FunctionalInterface
    interface MyLogger {
        Logger getLogger();
    }
}
