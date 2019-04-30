package no.nav.bidrag.dokument;

import static no.nav.bidrag.dokument.BidragDokumentConfig.SECURE_TEST_PROFILE;
import static no.nav.bidrag.dokument.BidragDokumentConfig.TEST_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import java.util.HashSet;
import java.util.Set;
import no.nav.bidrag.commons.web.test.SecuredTestRestTemplate;
import no.nav.bidrag.dokument.service.JournalpostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = BidragDokumentLocal.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({TEST_PROFILE, SECURE_TEST_PROFILE})
@DisplayName("CorrelationIdFilter")
class CorrelationIdFilterTest {

  @Autowired
  private SecuredTestRestTemplate securedTestRestTemplate;
  @MockBean
  private Appender appenderMock;
  @MockBean
  private JournalpostService journalpostServiceMock;
  @LocalServerPort
  private int port;

  private Set<String> logMeldinger = new HashSet<>();

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
  @DisplayName("skal logge requests mot applikasjonen")
  void skalLoggeRequestsMotApplikasjonen() {
    var response = securedTestRestTemplate.exchange(
        "http://localhost:" + port + "/bidrag-dokument/journalpost/BID-123",
        HttpMethod.GET,
        null,
        String.class
    );

    assertAll(
        () -> assertThat(response).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.NO_CONTENT),
        () -> verify(appenderMock, atLeastOnce()).doAppend(
            argThat((ArgumentMatcher) argument -> {
              logMeldinger.add(((ILoggingEvent) argument).getFormattedMessage());

              return true;
            })),
        () -> assertThat(String.join("\n", logMeldinger)).contains("Prosessing GET /bidrag-dokument/journalpost/BID-123")
    );
  }

  @Test
  @SuppressWarnings("unchecked")
  @DisplayName("skal ikke logge requests mot actuator endpoints")
  void skalIkkeLoggeRequestsMotActuatorEndpoints() {
    var response = securedTestRestTemplate.exchange(
        "http://localhost:" + port + "/bidrag-dokument/actuator/health",
        HttpMethod.GET,
        null,
        String.class
    );

    assertAll(
        () -> assertThat(response).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.OK),
        () -> verify(appenderMock, atLeastOnce()).doAppend(
            argThat((ArgumentMatcher) argument -> {
              logMeldinger.add(((ILoggingEvent) argument).getFormattedMessage());

              return true;
            })),
        () -> assertThat(String.join("\n", logMeldinger)).doesNotContain("Processing").doesNotContain("/actuator/")
    );
  }
}