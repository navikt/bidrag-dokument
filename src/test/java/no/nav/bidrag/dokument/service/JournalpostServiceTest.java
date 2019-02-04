package no.nav.bidrag.dokument.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.exception.KildesystemException;

@DisplayName("JournalpostService")
class JournalpostServiceTest {

    private @Mock BidragArkivConsumer bidragArkivConsumerMock;
    private @Mock BidragJournalpostConsumer bidragJournalpostConsumerMock;
    private @InjectMocks JournalpostService journalpostService;

    @BeforeEach
    void initMocksAndService() {
        MockitoAnnotations.initMocks(this);
    }

    @DisplayName("skal ikke hente journalpost")
    @Test
    void skalIkkeHenteJournalpostGittId() throws KildesystemException {
        when(bidragArkivConsumerMock.hentJournalpost(anyInt())).thenReturn(Optional.empty());
        assertThat(journalpostService.hentJournalpost("joark-2")).isNotPresent();
    }

    @DisplayName("skal hente journalpost gitt id")
    @Test
    void skalHenteJournalpostGittId() throws KildesystemException {
        when(bidragArkivConsumerMock.hentJournalpost(2)).thenReturn(Optional.of(new JournalpostDto()));
        assertThat(journalpostService.hentJournalpost("joark-2")).isPresent();
    }

    @DisplayName("skal feile med KildesystemException når kilsdesystem ikke er identifisert av id")
    @Test
    void skalFeileNarIdIkkeIdentifisererKildesystem() {
        // given
        String journalpostIdUtenKildesystem = "2";

        // when
        Throwable thrown = catchThrowable(() -> journalpostService.hentJournalpost(journalpostIdUtenKildesystem));

        // then
        assertThat(thrown)
                .isInstanceOf(KildesystemException.class)
                .hasMessage("Kunne ikke identifisere kildesystem for id: 2");
    }

    @DisplayName("skal feile med KildesystemException når journalpost id har riktig prefix, men iden ikke er et tall...")
    @Test
    void skalFeileNarIdIkkeErEtTall() {
        // given
        String journalpostIdMedUbrukeligId = "bid-jalla";

        // when
        Throwable thrown = catchThrowable(() -> journalpostService.hentJournalpost(journalpostIdMedUbrukeligId));

        // then
        assertThat(thrown)
                .isInstanceOf(KildesystemException.class)
                .hasMessage("Kan ikke prosesseres som et tall: bid-jalla");
    }
}
