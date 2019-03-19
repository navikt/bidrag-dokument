package no.nav.bidrag.dokument.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Optional;
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

  @DisplayName("skal ikke hente journalpost")
  @Test
  void skalIkkeHenteJournalpostGittId() {
    when(bidragArkivConsumerMock.hentJournalpost(anyInt())).thenReturn(Optional.empty());
    assertThat(journalpostService.hentJournalpost(new KildesystemIdenfikator("joark-2"))).isNotPresent();
  }

  @DisplayName("skal hente journalpost gitt id")
  @Test
  void skalHenteJournalpostGittId() {
    when(bidragArkivConsumerMock.hentJournalpost(2)).thenReturn(Optional.of(new JournalpostDto()));
    assertThat(journalpostService.hentJournalpost(new KildesystemIdenfikator("joark-2"))).isPresent();
  }
}
