package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.consumer.JournalforingConsumer;
import no.nav.bidrag.dokument.domain.joark.DtoManager;
import no.nav.bidrag.dokument.domain.joark.JournalforingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("JournalpostService")
class JournalpostDtoServiceTest {

    private @Mock JournalforingConsumer journalforingConsumerMock;
    private @InjectMocks JournalpostService journalpostService;

    @BeforeEach void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @DisplayName("skal hente journalforing gitt id")
    @Test void skalHenteJournalforingGittId() {
        when(journalforingConsumerMock.hentJournalforing("id")).thenReturn(new DtoManager<>(JournalforingDto.build().get(), null));
        assertThat(journalpostService.hentJournalpost("id")).isNotNull();
    }
}
