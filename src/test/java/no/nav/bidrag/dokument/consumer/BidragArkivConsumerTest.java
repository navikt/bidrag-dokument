package no.nav.bidrag.dokument.consumer;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static no.nav.bidrag.dokument.BidragDokumentTest.bearer;
import static no.nav.bidrag.dokument.consumer.ConsumerUtil.addSecurityHeader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("BidragArkivConsumer")
@SuppressWarnings("unchecked")
class BidragArkivConsumerTest {

    private BidragArkivConsumer bidragArkivConsumer;

    @Mock
    private Appender appenderMock;
    @Mock
    private RestTemplate restTemplateMock;

    @BeforeEach
    void setUp() {
        initMocks();
        initTestClass();
        mockRestTemplateFactory();
        mockLogAppender();
    }

    private void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    private void initTestClass() {
        bidragArkivConsumer = new BidragArkivConsumer("baseUrl");
    }

    private void mockRestTemplateFactory() {
        RestTemplateFactory.use(() -> restTemplateMock);
    }

    private void mockLogAppender() {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        when(appenderMock.getName()).thenReturn("MOCK");
        when(appenderMock.isStarted()).thenReturn(true);
        logger.addAppender(appenderMock);
    }

    @DisplayName("skal hente en journalpost med spring sin RestTemplate")
    @Test
    void skalHenteJournalpostMedRestTemplate() {

        when(restTemplateMock.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                ArgumentMatchers.<Class<JournalpostDto>>any())
        ).thenReturn(new ResponseEntity<>(enJournalpostMedJournaltilstand("ENDELIG"), HttpStatus.OK));

        Optional<JournalpostDto> journalpostOptional = bidragArkivConsumer.hentJournalpost(101, bearer());
        JournalpostDto journalpostDto = journalpostOptional.orElseThrow(() -> new AssertionError("Ingen Dto fra manager!"));

        assertThat(journalpostDto.getInnhold()).isEqualTo("ENDELIG");

        verify(restTemplateMock).exchange(
                "/journalpost/101",
                HttpMethod.GET,
                addSecurityHeader(null, bearer()),
                JournalpostDto.class);
    }

    private JournalpostDto enJournalpostMedJournaltilstand(@SuppressWarnings("SameParameterValue") String innhold) {
        JournalpostDto journalpostDto = new JournalpostDto();
        journalpostDto.setInnhold(innhold);

        return journalpostDto;
    }

    @DisplayName("skalLoggeHentJournalpost")
    @Test
    void skalLoggeHentJournalpost() {

        when(restTemplateMock.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                ArgumentMatchers.<Class<JournalpostDto>>any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        bidragArkivConsumer.hentJournalpost(123, bearer());

        verify(appenderMock).doAppend(
                argThat((ArgumentMatcher) argument -> {
                    assertThat(((ILoggingEvent) argument).getFormattedMessage())
                            .contains("Journalpost med id=123 har http status 500 INTERNAL_SERVER_ERROR");

                    return true;
                }));
    }

    @AfterEach
    void resetFactory() {
        RestTemplateFactory.reset();
    }
}
