package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.dokument.dto.JournalpostDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

public class BidragArkivConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BidragArkivConsumer.class);

    private final String bidragArkivBaseUrl;

    public BidragArkivConsumer(String bidragArkivBaseUrl) {
        this.bidragArkivBaseUrl = bidragArkivBaseUrl;
    }

    public Optional<JournalpostDto> hentJournalpost(Integer id) {
        RestTemplate restTemplate = RestTemplateFactory.create(bidragArkivBaseUrl);
        ResponseEntity<JournalpostDto> journalforingDtoResponseEntity = restTemplate.getForEntity("/journalpost/" + id, JournalpostDto.class);
        HttpStatus httpStatus = journalforingDtoResponseEntity.getStatusCode();

        LOGGER.info("Journalpost med id={} har http status {}", id, httpStatus);

        return Optional.ofNullable(journalforingDtoResponseEntity.getBody());
    }

    public List<JournalpostDto> finnJournalposter(String saksnummer) {
        RestTemplate restTemplate = RestTemplateFactory.create(bidragArkivBaseUrl);
        ResponseEntity<List<JournalpostDto>> responseEntity = restTemplate.exchange("/journalpost/gsak/" + saksnummer, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<JournalpostDto>>() {
                }
        );

        HttpStatus httpStatus = responseEntity.getStatusCode();
        LOGGER.info("Journalposter knyttet til gsak={} har http status {}", saksnummer, httpStatus);

        return responseEntity.getBody();
    }
}
