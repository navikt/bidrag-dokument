package no.nav.bidrag.dokument.consumer;

import static no.nav.bidrag.dokument.consumer.ConsumerUtil.addSecurityHeader;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import no.nav.bidrag.dokument.dto.JournalpostDto;

public class BidragArkivConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BidragArkivConsumer.class);

    private final String bidragArkivBaseUrl;

    public BidragArkivConsumer(String bidragArkivBaseUrl) {
        this.bidragArkivBaseUrl = bidragArkivBaseUrl;
    }

    public Optional<JournalpostDto> hentJournalpost(Integer id, String bearerToken) {
        RestTemplate restTemplate = RestTemplateFactory.create(bidragArkivBaseUrl, bearerToken);
        ResponseEntity<JournalpostDto> journalforingDtoResponseEntity = restTemplate.exchange(
                "/journalpost/" + id, HttpMethod.GET, addSecurityHeader(null, bearerToken), JournalpostDto.class);

        HttpStatus httpStatus = journalforingDtoResponseEntity.getStatusCode();

        LOGGER.info("Journalpost med id={} har http status {}", id, httpStatus);

        return Optional.ofNullable(journalforingDtoResponseEntity.getBody());
    }

    public List<JournalpostDto> finnJournalposter(String saksnummer, String bearerToken) {
        RestTemplate restTemplate = RestTemplateFactory.create(bidragArkivBaseUrl, bearerToken);
        ResponseEntity<List<JournalpostDto>> responseEntity = restTemplate.exchange(
                "/journalpost/gsak/" + saksnummer, HttpMethod.GET, addSecurityHeader(null, bearerToken),
                new ParameterizedTypeReference<List<JournalpostDto>>() {
                });

        HttpStatus httpStatus = responseEntity.getStatusCode();
        LOGGER.info("Journalposter knyttet til gsak={} har http status {}", saksnummer, httpStatus);

        return responseEntity.getBody();
    }
}
