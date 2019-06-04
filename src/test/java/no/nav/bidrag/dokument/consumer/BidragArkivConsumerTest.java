package no.nav.bidrag.dokument.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@DisplayName("BidragArkivConsumer")
@TestInstance(Lifecycle.PER_CLASS)
class BidragArkivConsumerTest {

  private BidragArkivConsumer bidragArkivConsumer;

  @Mock
  private RestTemplate restTemplateMock;

  @BeforeEach
  void setUp() {
    initMocks();
    initTestClass();
  }

  private void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  private void initTestClass() {
    bidragArkivConsumer = new BidragArkivConsumer(restTemplateMock);
  }

  @Test
  @DisplayName("skal hente en journalpost med spring sin RestTemplate")
  void skalHenteJournalpostMedRestTemplate() {

    when(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), eq(null), eq(JournalpostDto.class)))
        .thenReturn(new ResponseEntity<>(enJournalpostMedJournaltilstand("ENDELIG"), HttpStatus.OK));

    var journalpostResponse = bidragArkivConsumer.hentJournalpost(101);
    JournalpostDto journalpostDto = journalpostResponse.fetchOptionalResult()
        .orElseThrow(() -> new AssertionError("BidragArkivConsumer kunne ikke finne journalpost!"));

    assertThat(journalpostDto.getInnhold()).isEqualTo("ENDELIG");

    verify(restTemplateMock).exchange("/journalpost/101", HttpMethod.GET, null, JournalpostDto.class);
  }

  private JournalpostDto enJournalpostMedJournaltilstand(@SuppressWarnings("SameParameterValue") String innhold) {
    JournalpostDto journalpostDto = new JournalpostDto();
    journalpostDto.setInnhold(innhold);

    return journalpostDto;
  }
}
