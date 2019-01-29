package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.exception.KildesystemException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static  no.nav.bidrag.dokument.BidragDokumentTest.bearer;

@DisplayName("JournalpostService")
class JournalpostServiceTest {

    private @Mock BidragArkivConsumer bidragArkivConsumerMock;
    private @Mock BidragJournalpostConsumer bidragJournalpostConsumerMock;
    private @InjectMocks JournalpostService journalpostService;

    @BeforeEach void initMocksAndService() {
        MockitoAnnotations.initMocks(this);
    }

    @DisplayName("skal ikke hente journalpost")
    @Test void skalIkkeHenteJournalpostGittId() throws KildesystemException {
        when(bidragArkivConsumerMock.hentJournalpost(anyInt(), anyString())).thenReturn(Optional.empty());
        assertThat(journalpostService.hentJournalpost("joark-2", bearer())).isNotPresent();
    }

    @DisplayName("skal hente journalpost gitt id")
    @Test void skalHenteJournalpostGittId() throws KildesystemException {
        when(bidragArkivConsumerMock.hentJournalpost(2, bearer())).thenReturn(Optional.of(new JournalpostDto()));
        assertThat(journalpostService.hentJournalpost("joark-2", bearer())).isPresent();
    }

    @DisplayName("skal feile med KildesystemException når kilsdesystem ikke er identifisert av id")
    @Test void skalFeileNarIdIkkeIdentifisererKildesystem() {
        // given
        String journalpostIdUtenKildesystem = "2";

        // when
        Throwable thrown = catchThrowable(() -> journalpostService.hentJournalpost(journalpostIdUtenKildesystem, bearer()));

        // then
        assertThat(thrown)
                .isInstanceOf(KildesystemException.class)
                .hasMessage("Kunne ikke identifisere kildesystem for id: 2");
    }

    @DisplayName("skal feile med KildesystemException når journalpost id har riktig prefix, men iden ikke er et tall...")
    @Test void skalFeileNarIdIkkeErEtTall() {
        // given
        String journalpostIdMedUbrukeligId = "bid-jalla";

        // when
        Throwable thrown = catchThrowable(() -> journalpostService.hentJournalpost(journalpostIdMedUbrukeligId, bearer()));

        // then
        assertThat(thrown)
                .isInstanceOf(KildesystemException.class)
                .hasMessage("Kan ikke prosesseres som et tall: bid-jalla");
    }
}
