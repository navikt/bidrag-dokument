package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.dokument.domain.dto.DtoManager;
import no.nav.bidrag.dokument.domain.dto.JournalforingDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;

public class JournalforingConsumer {

    private final UriTemplateHandler uriTemplateHandler;
    private final String endpoint;

    public JournalforingConsumer(UriTemplateHandler uriTemplateHandler, String endpoint) {
        this.uriTemplateHandler = uriTemplateHandler;
        this.endpoint = endpoint;
    }

    public DtoManager<JournalforingDto> hentJournalforing(Object id) {
        RestTemplate restTemplate = RestTemplateFactory.create(uriTemplateHandler);
        ResponseEntity<JournalforingDto> journalforingDtoResponseEntity = restTemplate.getForEntity(endpoint, JournalforingDto.class);

        return new DtoManager<>(journalforingDtoResponseEntity.getBody(), journalforingDtoResponseEntity.getStatusCode());
    }
}
