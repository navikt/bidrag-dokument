package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.dokument.dto.joark.JournalforingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

public class JournalforingConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JournalforingConsumer.class);

    private final String joarkHentJournalforingerRestUrl;

    public JournalforingConsumer(String joarkHentJournalforingerRestUrl) {
        this.joarkHentJournalforingerRestUrl = joarkHentJournalforingerRestUrl;
    }

    public Optional<JournalforingDto> hentJournalforing(Integer id) {
        RestTemplate restTemplate = RestTemplateFactory.create(joarkHentJournalforingerRestUrl);
        ResponseEntity<JournalforingDto> journalforingDtoResponseEntity = restTemplate.getForEntity(String.valueOf(id), JournalforingDto.class);
        HttpStatus httpStatus = journalforingDtoResponseEntity.getStatusCode();

        LOGGER.info("JournalforingDto med id={} har http status {} - {}", id, httpStatus, httpStatus.getReasonPhrase());

        return Optional.ofNullable(journalforingDtoResponseEntity.getBody());
    }
}
