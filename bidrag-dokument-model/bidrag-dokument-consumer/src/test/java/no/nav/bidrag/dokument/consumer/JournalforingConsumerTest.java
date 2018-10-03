package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.dokument.domain.JournalTilstand;
import no.nav.bidrag.dokument.domain.joark.JournalforingDto;
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
import static org.mockito.Mockito.when;

@DisplayName("JournalforingConsumer")
class JournalforingConsumerTest {

    private JournalforingConsumer journalforingConsumer;
    @Mock private RestTemplate restTemplateMock;

    @BeforeEach void initMocksAndTestClass() {
        MockitoAnnotations.initMocks(this);
        RestTemplateFactory.use(() -> restTemplateMock);
        journalforingConsumer = new JournalforingConsumer("/journalforing");
    }

    @DisplayName("skal hente en journalforing med spring sin RestTemplate")
    @Test void skalHenteJournalforingMedRestTemplate() {
        when(restTemplateMock.getForEntity("/journalforing", JournalforingDto.class))
                .thenReturn(new ResponseEntity<>(JournalforingDto.build().with(JournalTilstand.ENDELIG).get(), HttpStatus.OK));

        Optional<JournalforingDto> journalpostOptional = journalforingConsumer.hentJournalforing(null);
        JournalforingDto journalforingDto = journalpostOptional.orElseThrow(() -> new AssertionError("Ingen Dto fra manager!"));

        assertThat(journalforingDto.getJournalTilstand()).isEqualTo(JournalTilstand.ENDELIG);
    }

    @AfterEach void resetFactory() {
        RestTemplateFactory.reset();
    }
}
