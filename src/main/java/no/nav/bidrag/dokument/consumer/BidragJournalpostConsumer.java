package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.dokument.dto.BrevlagerJournalpostDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BidragJournalpostConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BidragJournalpostConsumer.class);

    private final String baseUrlBidragJournalpost;

    public BidragJournalpostConsumer(String baseUrlBidragJournalpost) {
        this.baseUrlBidragJournalpost = baseUrlBidragJournalpost;
    }

    public List<JournalpostDto> finnJournalposter(String saksnummer) {
        RestTemplate restTemplate = RestTemplateFactory.create(baseUrlBidragJournalpost);
        ResponseEntity<List<JournalpostDto>> journalposterForBidragRequest = restTemplate.exchange(
                "/sak/" + saksnummer, HttpMethod.GET, null, typereferansenErListeMedJournalposter()
        );

        HttpStatus httpStatus = journalposterForBidragRequest.getStatusCode();
        LOGGER.info("Fikk http status {} fra journalposter i bidragssak med saksnummer {}", httpStatus, saksnummer);
        List<JournalpostDto> journalposter = journalposterForBidragRequest.getBody();

        return journalposter != null ? journalposter : Collections.emptyList();
    }

    private static ParameterizedTypeReference<List<JournalpostDto>> typereferansenErListeMedJournalposter() {
        return new ParameterizedTypeReference<List<JournalpostDto>>() {
        };
    }

    public Optional<BrevlagerJournalpostDto> save(BrevlagerJournalpostDto brevlagerJournalpostDto) {
        RestTemplate restTemplate = RestTemplateFactory.create(baseUrlBidragJournalpost);
        ResponseEntity<BrevlagerJournalpostDto> registrertJournalpost = restTemplate.exchange(
                "/registrer", HttpMethod.POST, new HttpEntity<>(brevlagerJournalpostDto), BrevlagerJournalpostDto.class
        );

        HttpStatus httpStatus = registrertJournalpost.getStatusCode();
        LOGGER.info("Fikk http status {} fra registrer ny journalpost: {}", httpStatus, brevlagerJournalpostDto);

        return Optional.ofNullable(registrertJournalpost.getBody());
    }

    public Optional<JournalpostDto> hentJournalpost(Integer id) {
        RestTemplate restTemplate = RestTemplateFactory.create(baseUrlBidragJournalpost);
        ResponseEntity<JournalpostDto> journalpostResponseEntity = restTemplate.getForEntity("/journalpost/" + id, JournalpostDto.class);
        HttpStatus httpStatus = journalpostResponseEntity.getStatusCode();

        LOGGER.info("BidragJournalpostDto med id={} har http status {}", id, httpStatus);

        return Optional.ofNullable(journalpostResponseEntity.getBody());
    }
}
