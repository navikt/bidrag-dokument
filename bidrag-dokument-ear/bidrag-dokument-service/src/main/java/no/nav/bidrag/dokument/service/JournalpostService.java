package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.consumer.JournalforingConsumer;
import no.nav.bidrag.dokument.domain.Journalpost;
import no.nav.bidrag.dokument.domain.dto.DtoManager;
import no.nav.bidrag.dokument.domain.dto.JournalforingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.Optional;

public class JournalpostService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JournalpostService.class);

    private final JournalforingConsumer journalforingConsumer;

    public JournalpostService(JournalforingConsumer journalforingConsumer) {
        this.journalforingConsumer = journalforingConsumer;
    }

    public Optional<Journalpost> hentJournalpost(Object id) {
        DtoManager<JournalforingDto> journalforingDtoManager = journalforingConsumer.hentJournalforing(id);

        if (LOGGER.isDebugEnabled() && journalforingDtoManager.harStatus(HttpStatus.class)) {
            HttpStatus httpStatus = journalforingDtoManager.hentStatus(null);
            LOGGER.debug(String.format("Journalpost med id=%s har status %s - %s", id, httpStatus, httpStatus.getReasonPhrase()));
        }

        return journalforingDtoManager.hent()
                .map(Journalpost::new);
    }
}
