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

    private final String joarkRestServiceUrl;

    public JournalforingConsumer(String joarkRestServiceUrl) {
        this.joarkRestServiceUrl = joarkRestServiceUrl;
    }

    public Optional<JournalforingDto> hentJournalforing(Integer id) {
        RestTemplate restTemplate = RestTemplateFactory.create();
        ResponseEntity<JournalforingDto> journalforingDtoResponseEntity = restTemplate.getForEntity(joarkRestServiceUrl, JournalforingDto.class);

        if (LOGGER.isDebugEnabled()) {
            HttpStatus httpStatus = journalforingDtoResponseEntity.getStatusCode();

            if (httpStatus.is2xxSuccessful()) {
                LOGGER.trace(String.format("JournalforingDto med id=%s har http status %s - %s", id, httpStatus, httpStatus.getReasonPhrase()));
            } else {
                LOGGER.debug(String.format("JournalforingDto med id=%s har http status %s - %s", id, httpStatus, httpStatus.getReasonPhrase()));
            }
        }

        return Optional.ofNullable(journalforingDtoResponseEntity.getBody());
    }
}
