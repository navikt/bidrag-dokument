package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.consumer.JournalforingConsumer;
import no.nav.bidrag.dokument.domain.bisys.JournalpostDto;
import no.nav.bidrag.dokument.domain.joark.DtoManager;
import no.nav.bidrag.dokument.domain.joark.JournalforingDto;
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

    public Optional<JournalpostDto> hentJournalpost(Object id) {
        DtoManager<JournalforingDto> journalforingDtoManager = journalforingConsumer.hentJournalforing(id);

        if (LOGGER.isDebugEnabled()) {
            if (journalforingDtoManager.harStatus(HttpStatus.class)) {
                loggHttpStatus(id, journalforingDtoManager.hentStatus(HttpStatus.class));
            } else if (journalforingDtoManager.harStatus()) {
                loggGenerellStatus(id, journalforingDtoManager.getStatus());
            }
        }

        return journalforingDtoManager.hent()
                .map(JournalpostDto::populate);
    }

    private void loggHttpStatus(Object id, HttpStatus httpStatus) {
        LOGGER.debug(String.format("JournalpostDto med id=%s har http status %s - %s", id, httpStatus, httpStatus.getReasonPhrase()));
    }

    private void loggGenerellStatus(Object id, Enum<?> status) {
        LOGGER.debug(String.format("JournalpostDto med id=%s har generell status %s", id, status));
    }
}
