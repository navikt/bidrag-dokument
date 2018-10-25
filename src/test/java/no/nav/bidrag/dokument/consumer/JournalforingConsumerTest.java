package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.dokument.dto.joark.JournalforingDto;
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

@DisplayName("JournalforingConsumer")
class JournalforingConsumerTest {

    private JournalforingConsumer journalforingConsumer;
    @Mock private RestTemplate restTemplateMock;

    @BeforeEach void initMocksAndTestClass() {
        MockitoAnnotations.initMocks(this);
        RestTemplateFactory.use(() -> restTemplateMock);
        journalforingConsumer = new JournalforingConsumer("consumer");
    }

    @DisplayName("skal hente en journalforing med spring sin RestTemplate")
    @Test void skalHenteJournalforingMedRestTemplate() {
        when(restTemplateMock.getForEntity(anyString(), any()))
                .thenReturn(new ResponseEntity<>(journalforingMedTilstand("ENDELIG"), HttpStatus.OK));

        Optional<JournalforingDto> journalpostOptional = journalforingConsumer.hentJournalforing(101);
        JournalforingDto journalforingDto = journalpostOptional.orElseThrow(() -> new AssertionError("Ingen Dto fra manager!"));

        assertThat(journalforingDto.getJournalTilstand()).isEqualTo("ENDELIG");
        verify(restTemplateMock).getForEntity("101", JournalforingDto.class);
    }

    private JournalforingDto journalforingMedTilstand(@SuppressWarnings("SameParameterValue") String journaltilstand) {
        JournalforingDto journalforingDto = new JournalforingDto();
        journalforingDto.setJournalTilstand(journaltilstand);

        return journalforingDto;
    }

    @AfterEach void resetFactory() {
        RestTemplateFactory.reset();
    }
}
