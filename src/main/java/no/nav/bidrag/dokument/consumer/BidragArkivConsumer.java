package no.nav.bidrag.dokument.consumer;

import static no.nav.bidrag.dokument.BidragDokumentConfig.ISSUER;
import static no.nav.bidrag.dokument.consumer.ConsumerUtil.addSecurityHeader;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.security.oidc.context.OIDCRequestContextHolder;

public class BidragArkivConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BidragArkivConsumer.class);

    private final String bidragArkivBaseUrl;

    private final OIDCRequestContextHolder securityContextHolder;

    public BidragArkivConsumer(
            String bidragArkivBaseUrl,
            OIDCRequestContextHolder securityContextHolder) {

        this.bidragArkivBaseUrl = bidragArkivBaseUrl;
        this.securityContextHolder = securityContextHolder;
    }

    public Optional<JournalpostDto> hentJournalpost(Integer id) {
        RestTemplate restTemplate = RestTemplateFactory.create(bidragArkivBaseUrl);

        ResponseEntity<JournalpostDto> journalforingDtoResponseEntity = restTemplate.exchange(
                "/journalpost/" + id, HttpMethod.GET, addSecurityHeader(null, getBearerToken()), JournalpostDto.class);

        HttpStatus httpStatus = journalforingDtoResponseEntity.getStatusCode();

        LOGGER.info("Journalpost med id={} har http status {}", id, httpStatus);

        return Optional.ofNullable(journalforingDtoResponseEntity.getBody());
    }

    private String getBearerToken() {
        return "Bearer " + securityContextHolder.getOIDCValidationContext().getToken(ISSUER).getIdToken();
    }
}
