package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.dokument.domain.JournalTilstand;
import no.nav.bidrag.dokument.domain.joark.DtoManager;
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
import org.springframework.web.util.UriTemplateHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("JournalforingConsumer")
class JournalforingConsumerTest {

    private JournalforingConsumer journalforingConsumer;
    @Mock private RestTemplate restTemplateMock;
    @Mock private UriTemplateHandler uriTemplateHandlerMock;

    @BeforeEach void initMocksAndFactoryThenInjectTestClass() {
        MockitoAnnotations.initMocks(this);
        RestTemplateFactory.use((handler) -> restTemplateMock);
        journalforingConsumer = new JournalforingConsumer(uriTemplateHandlerMock, "/journalforing");
    }

    @DisplayName("skal hente en journalforing med spring sin RestTemplate")
    @Test void skalHenteJournalforingMedRestTemplate() {
        when(restTemplateMock.getForEntity("/journalforing", JournalforingDto.class))
                .thenReturn(new ResponseEntity<>(JournalforingDto.build().with(JournalTilstand.ENDELIG).get(), HttpStatus.OK));

        DtoManager<JournalforingDto> journalpostDtoManager = journalforingConsumer.hentJournalforing(null);
        JournalforingDto journalforingDto = journalpostDtoManager.hent().orElseThrow(() -> new AssertionError("Ingen Dto fra manager!"));

        assertThat(journalforingDto.getJournalTilstand()).isEqualTo(JournalTilstand.ENDELIG);
    }

    @DisplayName("skal hente status")
    @Test void skalHenteStatus() {
        when(restTemplateMock.getForEntity("/journalforing", JournalforingDto.class))
                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        DtoManager<JournalforingDto> journalpostDtoManager = journalforingConsumer.hentJournalforing(null);

        assertThat((Enum<?>) journalpostDtoManager.hentStatus(HttpStatus.class)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @AfterEach void resetFactory() {
        RestTemplateFactory.reset();
    }
}
