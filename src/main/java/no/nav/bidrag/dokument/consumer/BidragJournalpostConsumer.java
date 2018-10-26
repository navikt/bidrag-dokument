package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.dokument.dto.bisys.BidragJournalpostDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

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
        RestTemplate restTemplate = RestTemplateFactory.create(baseUrlBidragJournalpost);
        ResponseEntity<List<BidragJournalpostDto>> journalposterForBidragRequest = restTemplate.exchange(
                "/sak/" + saksnummer, HttpMethod.GET, null, typereferansenErListeMedJournalposter()
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

    public Optional<BidragJournalpostDto> save(BidragJournalpostDto bidragJournalpostDto) {
        RestTemplate restTemplate = RestTemplateFactory.create(baseUrlBidragJournalpost);
        ResponseEntity<BidragJournalpostDto> registrertJournalpost = restTemplate.exchange(
                "/registrer", HttpMethod.POST, new HttpEntity<>(bidragJournalpostDto), BidragJournalpostDto.class
        );

        HttpStatus httpStatus = registrertJournalpost.getStatusCode();
        String reasonPhrase = httpStatus != null ? httpStatus.getReasonPhrase() : null;

        myLogger.getLogger().info("Fikk http status {}({}) fra registrer ny journalpost {}", httpStatus, reasonPhrase, bidragJournalpostDto);

        return Optional.ofNullable(registrertJournalpost.getBody());
    }

    @FunctionalInterface
    interface MyLogger {
        Logger getLogger();
    }
}
