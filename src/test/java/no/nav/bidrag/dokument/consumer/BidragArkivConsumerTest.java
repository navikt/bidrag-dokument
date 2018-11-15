package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.dokument.dto.JournalpostDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("BidragArkivConsumer")
class BidragArkivConsumerTest {

    private BidragArkivConsumer bidragArkivConsumer;
    @Mock private RestTemplate restTemplateMock;

    @BeforeEach void initMocksAndTestClass() {
        MockitoAnnotations.initMocks(this);
        RestTemplateFactory.use(() -> restTemplateMock);
        bidragArkivConsumer = new BidragArkivConsumer("consumer");
    }

    @DisplayName("skal hente en journalpost med spring sin RestTemplate")
    @Test void skalHenteJournalpostMedRestTemplate() {
        when(restTemplateMock.getForEntity(anyString(), any()))
                .thenReturn(new ResponseEntity<>(enJournalpostMedJournaltilstand("ENDELIG"), HttpStatus.OK));

        Optional<JournalpostDto> journalpostOptional = bidragArkivConsumer.hentJournalpost(101);
        JournalpostDto journalpostDto = journalpostOptional.orElseThrow(() -> new AssertionError("Ingen Dto fra manager!"));

        assertThat(journalpostDto.getJournaltilstand()).isEqualTo("ENDELIG");
        verify(restTemplateMock).getForEntity("101", JournalpostDto.class);
    }

    private JournalpostDto enJournalpostMedJournaltilstand(@SuppressWarnings("SameParameterValue") String journaltilstand) {
        JournalpostDto journalpostDto = new JournalpostDto();
        journalpostDto.setJournaltilstand(journaltilstand);

        return journalpostDto;
    }

    @AfterEach void resetFactory() {
        RestTemplateFactory.reset();
    }
}
