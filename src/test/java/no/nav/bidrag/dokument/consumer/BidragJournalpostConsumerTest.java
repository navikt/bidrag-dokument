package no.nav.bidrag.dokument.consumer;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import no.nav.bidrag.dokument.dto.bisys.BidragJournalpostDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("BidragJournalpostConsumer")
@SuppressWarnings("unchecked") class BidragJournalpostConsumerTest {

    private BidragJournalpostConsumer bidragJournalpostConsumer;

    private @Mock Appender appenderMock;
    private @Mock RestTemplate restTemplateMock;

    @BeforeEach void setup() {
        initMocks();
        initTestClass();
        mockRestTemplateFactory();
        mockLogAppender();
    }

    private void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    private void initTestClass() {
        bidragJournalpostConsumer = new BidragJournalpostConsumer("journalpost");
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

    @AfterEach void shouldResetRestTemplateFactory() {
        RestTemplateFactory.reset();
    }

    @DisplayName("skal bruke bidragssakens saksnummer i sti til tjeneste")
    @Test void shouldUseValueFromPath() {
        when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<BidragJournalpostDto>>) any())).thenReturn(
                new ResponseEntity<>(HttpStatus.NO_CONTENT)
        );

        bidragJournalpostConsumer.finnJournalposter("101");
        verify(restTemplateMock).exchange(eq("/sak/101"), eq(HttpMethod.GET), any(), (ParameterizedTypeReference<List<BidragJournalpostDto>>) any());
    }

    @DisplayName("should log get invocations")
    @Test void shouldLogGetInvocations() {
        when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<BidragJournalpostDto>>) any())).thenReturn(
                new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
        );

        bidragJournalpostConsumer.finnJournalposter("101");

        verify(appenderMock).doAppend(
                argThat((ArgumentMatcher) argument -> {
                    assertThat(((ILoggingEvent) argument).getFormattedMessage())
                            .contains("Fikk http status 500 INTERNAL_SERVER_ERROR fra journalposter i bidragssak med saksnummer 101");

                    return true;
                })
        );
    }

    @DisplayName("should log post invocations")
    @Test void shouldLogPostInvocations() {
        when(restTemplateMock.exchange(anyString(), any(), any(), eq(BidragJournalpostDto.class))).thenReturn(
                new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
        );

        bidragJournalpostConsumer.save(new BidragJournalpostDto());

        verify(appenderMock).doAppend(
                argThat((ArgumentMatcher) argument -> {
                    assertThat(((ILoggingEvent) argument).getFormattedMessage())
                            .contains("Fikk http status 500 INTERNAL_SERVER_ERROR fra registrer ny journalpost: BidragJournalpostDto");

                    return true;
                })
        );
    }
}
