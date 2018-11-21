package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.dokument.dto.JournalpostDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

public class BidragArkivConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BidragArkivConsumer.class);

    private final String joarkHentJournalforingerRestUrl;

    public BidragArkivConsumer(String joarkHentJournalforingerRestUrl) {
        this.joarkHentJournalforingerRestUrl = joarkHentJournalforingerRestUrl;
    }

    public Optional<JournalpostDto> hentJournalpost(Integer id) {
        RestTemplate restTemplate = RestTemplateFactory.create(joarkHentJournalforingerRestUrl);
        ResponseEntity<JournalpostDto> journalforingDtoResponseEntity = restTemplate.getForEntity(String.valueOf(id), JournalpostDto.class);
        HttpStatus httpStatus = journalforingDtoResponseEntity.getStatusCode();

        LOGGER.info("Journalpost med id={} har http status {} - {}", id, httpStatus, httpStatus.getReasonPhrase());

        return Optional.ofNullable(journalforingDtoResponseEntity.getBody());
    }
}
