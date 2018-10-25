package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.dokument.dto.bisys.BidragJournalpostDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("BidragJournalpostConsumer")
@SuppressWarnings("unchecked") class BidragJournalpostConsumerTest {

    private BidragJournalpostConsumer bidragJournalpostConsumer;

    private @Mock Logger loggerMock;
    private @Mock RestTemplate restTemplateMock;

    @BeforeEach void setup() {
        initMocks();
        initTestClass();
        mockRestTemplateFactory();
    }

    private void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    private void initTestClass() {
        bidragJournalpostConsumer = new BidragJournalpostConsumer("journalpost/sak/", () -> loggerMock);
    }

    private void mockRestTemplateFactory() {
        RestTemplateFactory.use(() -> restTemplateMock);
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
        verify(restTemplateMock).exchange(eq("101"), eq(HttpMethod.GET), any(), (ParameterizedTypeReference<List<BidragJournalpostDto>>) any());
    }

    @DisplayName("should log not invocations")
    @Test void shouldLogInvocations() {
        when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<BidragJournalpostDto>>) any())).thenReturn(
                new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
        );

        bidragJournalpostConsumer.finnJournalposter("101");

        verify(loggerMock).info(eq("Fikk http status {} fra journalposter i bidragssak med saksnummer {} - {}"), (Object[]) any());
    }
}
