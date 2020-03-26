package no.nav.bidrag.dokument.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("BidragJournalpostConsumer")
@SuppressWarnings("unchecked")
class BidragJournalpostConsumerTest {

  @InjectMocks
  private BidragJournalpostConsumer bidragJournalpostConsumer;

  @Mock
  private RestTemplate restTemplateMock;

  @Test
  @DisplayName("skal hente journalen til en sak")
  void skalHenteSakJournal() {
    when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

    bidragJournalpostConsumer.finnJournalposter("101", "BID");
    verify(restTemplateMock)
        .exchange(
            eq("/sak/101/journal?fagomrade=BID"),
            eq(HttpMethod.GET),
            any(),
            (ParameterizedTypeReference<List<JournalpostDto>>) any()
        );
  }

  @Test
  @DisplayName("skal endre journalpost")
  void skalEndreJournalpost() {
    when(restTemplateMock.exchange(anyString(), any(), any(), (Class<Object>) any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));

    bidragJournalpostConsumer.endre("007", "4802", endreJournalpostCommandMedId101());

    verify(restTemplateMock)
        .exchange(
            eq("/sak/007/journal/BID-101"),
            eq(HttpMethod.PUT),
            any(),
            eq(Void.class)
        );
  }

  private EndreJournalpostCommand endreJournalpostCommandMedId101() {
    EndreJournalpostCommand endreJournalpostCommand = new EndreJournalpostCommand();
    endreJournalpostCommand.setJournalpostId("BID-101");

    return endreJournalpostCommand;
  }
}
