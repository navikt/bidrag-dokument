package no.nav.bidrag.dokument.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import no.nav.bidrag.commons.web.HttpStatusResponse;
import no.nav.bidrag.dokument.KildesystemIdenfikator;
import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

@DisplayName("JournalpostService")
class JournalpostServiceTest {

  @Mock
  private BidragArkivConsumer bidragArkivConsumerMock;
  @Mock
  private BidragJournalpostConsumer bidragJournalpostConsumerMock;
  @InjectMocks
  private JournalpostService journalpostService;

  @BeforeEach
  void initMocksAndService() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  @DisplayName("skal ikke hente journalpost")
  void skalIkkeHenteJournalpostGittId() {
    when(bidragArkivConsumerMock.hentJournalpost(anyInt())).thenReturn(new HttpStatusResponse<>(HttpStatus.NO_CONTENT));
    KildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix("joark-2");
    assertThat(journalpostService.hentJournalpost(KildesystemIdenfikator.hent()).fetchOptionalResult()).isNotPresent();
  }

  @Test
  @DisplayName("skal hente journalpost gitt id")
  void skalHenteJournalpostGittId() {
    when(bidragArkivConsumerMock.hentJournalpost(2)).thenReturn(new HttpStatusResponse<>(HttpStatus.OK, new JournalpostDto()));
    KildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix("joark-2");
    assertThat(journalpostService.hentJournalpost(KildesystemIdenfikator.hent()).fetchOptionalResult()).isPresent();
  }

  @Test
  @DisplayName("skal kombinere resultat fra BidragDokumentJournalpostConsumer samt BidragDokumentArkivConsumer")
  void skalKobinereResultaterFraJournalpostOgArkiv() {
    when(bidragJournalpostConsumerMock.finnJournalposter("1", "FAG"))
        .thenReturn(Collections.singletonList(new JournalpostDto()));
    when(bidragArkivConsumerMock.finnJournalposter("1", "FAG"))
        .thenReturn(Collections.singletonList(new JournalpostDto()));

    var journalposter = journalpostService.finnJournalposter("1", "FAG");

    assertAll(
        () -> assertThat(journalposter).hasSize(2),
        () -> verify(bidragArkivConsumerMock).finnJournalposter("1", "FAG"),
        () -> verify(bidragJournalpostConsumerMock).finnJournalposter("1", "FAG")
    );
  }
}
