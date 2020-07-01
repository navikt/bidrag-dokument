package no.nav.bidrag.dokument.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import no.nav.bidrag.commons.KildesystemIdenfikator;
import no.nav.bidrag.commons.web.HttpResponse;
import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.JournalpostResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@DisplayName("JournalpostService")
@ExtendWith(MockitoExtension.class)
class JournalpostServiceTest {

  @Mock
  private BidragArkivConsumer bidragArkivConsumerMock;
  @Mock
  private BidragJournalpostConsumer bidragJournalpostConsumerMock;
  @InjectMocks
  private JournalpostService journalpostService;

  @Test
  @DisplayName("skal ikke hente journalpost")
  void skalIkkeHenteJournalpostGittId() {
    when(bidragArkivConsumerMock.hentJournalpost(anyString(), anyString())).thenReturn(HttpResponse.from(HttpStatus.NO_CONTENT));

    var httpStatusResponse = journalpostService.hentJournalpost("69", new KildesystemIdenfikator("joark-2"));
    assertThat(httpStatusResponse.fetchBody()).isNotPresent();
  }

  @Test
  @DisplayName("skal hente journalpost gitt id")
  void skalHenteJournalpostGittId() {
    when(bidragArkivConsumerMock.hentJournalpost(anyString(), anyString())).thenReturn(HttpResponse.from(HttpStatus.OK, new JournalpostResponse()));

    var httpStatusResponse = journalpostService.hentJournalpost("69", new KildesystemIdenfikator("joark-3"));
    assertThat(httpStatusResponse.fetchBody()).isPresent();
  }

  @Test
  @DisplayName("skal kombinere resultat fra BidragDokumentJournalpostConsumer samt BidragDokumentArkivConsumer")
  void skalKombinereResultaterFraJournalpostOgArkiv() {
    when(bidragJournalpostConsumerMock.finnJournalposter("1", "FAG"))
        .thenReturn(Collections.singletonList(new JournalpostDto()));

    var journalposter = journalpostService.finnJournalposter("1", "FAG");

    assertAll(
        () -> assertThat(journalposter).hasSize(1),
        () -> verify(bidragJournalpostConsumerMock).finnJournalposter("1", "FAG")
    );
  }
}
