package no.nav.bidrag.dokument.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import no.nav.bidrag.dokument.BidragDokumentConfig.RestTemplateProvider;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.JournalpostResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@DisplayName("BidragArkivConsumer")
@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
class BidragArkivConsumerTest {

  @Mock
  ConsumerTarget consumerTarget;
  @Mock
  RestTemplateProvider restTemplateProviderMock;
  @InjectMocks
  private BidragArkivConsumer bidragArkivConsumer;
  @Mock
  private RestTemplate restTemplateMock;

  @Test
  @DisplayName("skal hente en journalpost med spring sin RestTemplate")
  void skalHenteJournalpostMedRestTemplate() {
    when(consumerTarget.getBaseUrl()).thenReturn("bidrag-dokument-arkiv-url");
    when(consumerTarget.getRestTemplateProvider()).thenReturn(restTemplateProviderMock);
    when(restTemplateProviderMock.provideRestTemplate(anyString(), anyString())).thenReturn(restTemplateMock);
    when(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JournalpostResponse.class)))
        .thenReturn(new ResponseEntity<>(enJournalpostMedJournaltilstand("ENDELIG"), HttpStatus.OK));

    var httpResponse = bidragArkivConsumer.hentJournalpost("69", "BID-101");
    var journalpostResponse = httpResponse.fetchBody()s.orElseThrow(() -> new AssertionError("BidragArkivConsumer kunne ikke finne journalpost!"));

    assertThat(journalpostResponse.getJournalpost()).extracting(JournalpostDto::getInnhold).isEqualTo("ENDELIG");

    verify(restTemplateMock).exchange("/journal/BID-101?saksnummer=69", HttpMethod.GET, null, JournalpostResponse.class);
  }

  private JournalpostResponse enJournalpostMedJournaltilstand(@SuppressWarnings("SameParameterValue") String innhold) {
    JournalpostDto journalpostDto = new JournalpostDto();
    journalpostDto.setInnhold(innhold);

    return new JournalpostResponse(journalpostDto, Collections.emptyList());
  }
}
