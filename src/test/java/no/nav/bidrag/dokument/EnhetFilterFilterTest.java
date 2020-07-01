package no.nav.bidrag.dokument;

import static no.nav.bidrag.dokument.BidragDokumentLocal.SECURE_TEST_PROFILE;
import static no.nav.bidrag.dokument.BidragDokumentLocal.TEST_PROFILE;
import static no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer.createEnhetHeader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import java.util.stream.Collectors;
import no.nav.bidrag.commons.KildesystemIdenfikator;
import no.nav.bidrag.commons.web.HttpStatusResponse;
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate;
import no.nav.bidrag.dokument.service.JournalpostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = BidragDokumentLocal.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({TEST_PROFILE, SECURE_TEST_PROFILE})
@DisplayName("EnhetFilter")
class EnhetFilterFilterTest {

  @Autowired
  private HttpHeaderTestRestTemplate securedTestRestTemplate;

  @SuppressWarnings("rawtypes")
  @MockBean
  private Appender appenderMock;
  @MockBean
  private JournalpostService journalpostServiceMock;

  @LocalServerPort
  private int port;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void mockLogAppender() {
    ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    when(appenderMock.getName()).thenReturn("MOCK");
    when(appenderMock.isStarted()).thenReturn(true);
    logger.addAppender(appenderMock);
  }

  @Test
  @SuppressWarnings("unchecked")
  @DisplayName("skal logge requests mot applikasjonen som ikke inneholder enhetsinformasjon i header")
  void skalLoggeRequestsMotApplikasjonenUtenHeaderInformasjon() {
    when(journalpostServiceMock.hentJournalpost(anyString(), any(KildesystemIdenfikator.class)))
        .thenReturn(new HttpStatusResponse<>(HttpStatus.I_AM_A_TEAPOT));

    var response = securedTestRestTemplate.exchange(
        "http://localhost:" + port + "/bidrag-dokument/journal/BID-123?saksnummer=777",
        HttpMethod.GET,
        null,
        String.class
    );

    assertAll(
        () -> assertThat(response).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.I_AM_A_TEAPOT),
        () -> {
          var loggingEventCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
          verify(appenderMock, atLeastOnce()).doAppend(loggingEventCaptor.capture());

          var loggingEvents = loggingEventCaptor.getAllValues();
          var allMsgs = loggingEvents.stream().map(ILoggingEvent::getFormattedMessage).collect(Collectors.joining("\n"));

          assertThat(allMsgs).contains("Behandler request '/bidrag-dokument/journal/BID-123' uten informasjon om enhetsnummer.");
        }
    );
  }

  @Test
  @SuppressWarnings("unchecked")
  @DisplayName("skal logge requests mot applikasjonen som ikke inneholder enhetsinformasjon i header")
  void skalLoggeRequestsMotApplikasjonenMedHeaderInformasjon() {
    when(journalpostServiceMock.hentJournalpost(anyString(), any(KildesystemIdenfikator.class)))
        .thenReturn(new HttpStatusResponse<>(HttpStatus.I_AM_A_TEAPOT));
    var enhet = "4802";
    var htpEntity = new HttpEntity<Void>(null, createEnhetHeader(enhet));
    var response = securedTestRestTemplate.exchange(
        "http://localhost:" + port + "/bidrag-dokument/journal/BID-123?saksnummer=777",
        HttpMethod.GET,
        htpEntity,
        String.class
    );

    assertAll(
        () -> assertThat(response).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.I_AM_A_TEAPOT),
        () -> {
          var loggingEventCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
          verify(appenderMock, atLeastOnce()).doAppend(loggingEventCaptor.capture());

          var loggingEvents = loggingEventCaptor.getAllValues();
          var allMsgs = loggingEvents.stream().map(ILoggingEvent::getFormattedMessage).collect(Collectors.joining("\n"));

          assertThat(allMsgs).containsIgnoringCase("Behandler request '/bidrag-dokument/journal/BID-123' for enhet med enhetsnummer " + enhet);
        }
    );
  }
}
