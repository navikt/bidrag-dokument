package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.consumer.JournalforingConsumer;
import no.nav.bidrag.dokument.dto.joark.JournalforingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@DisplayName("JournalpostService")
class JournalpostServiceTest {

    private @Mock JournalforingConsumer journalforingConsumerMock;
    private JournalpostService journalpostService;

    @BeforeEach void initMocksAndService() {
        MockitoAnnotations.initMocks(this);
        journalpostService = new JournalpostService(null, journalforingConsumerMock, new JournalpostMapper());
    }

    @DisplayName("skal ikke hente journalforing")
    @Test void skalIkkeHenteJournalforingGittId() {
        when(journalforingConsumerMock.hentJournalforing(anyInt())).thenReturn(Optional.empty());
        assertThat(journalpostService.hentJournalpost(2)).isNotPresent();
    }

    @DisplayName("skal hente journalforing gitt id")
    @Test void skalHenteJournalforingGittId() {
        when(journalforingConsumerMock.hentJournalforing(2)).thenReturn(Optional.of(new JournalforingDto()));
        assertThat(journalpostService.hentJournalpost(2)).isPresent();
    }
}
