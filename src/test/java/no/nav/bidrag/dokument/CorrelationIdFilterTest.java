package no.nav.bidrag.dokument;

import static no.nav.bidrag.dokument.BidragDokumentLocal.TEST_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import no.nav.bidrag.commons.KildesystemIdenfikator;
import no.nav.bidrag.commons.security.service.OidcTokenManager;
import no.nav.bidrag.commons.web.HttpResponse;
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate;
import no.nav.bidrag.dokument.dto.JournalpostResponse;
import no.nav.bidrag.dokument.service.JournalpostService;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = BidragDokumentLocal.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(TEST_PROFILE)
@DisplayName("CorrelationIdFilter")
@EnableMockOAuth2Server
class CorrelationIdFilterTest {

  @Autowired
  private HttpHeaderTestRestTemplate securedTestRestTemplate;

  @SuppressWarnings("rawtypes")
  @MockBean
  private Appender appenderMock;
  @MockBean
  private JournalpostService journalpostServiceMock;

  @LocalServerPort
  private int port;

  @MockBean
  OidcTokenManager oidcTokenManager;

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
    when(oidcTokenManager.isValidTokenIssuedByAzure()).thenReturn(false);
    when(oidcTokenManager.fetchTokenAsString()).thenReturn("");
    when(journalpostServiceMock.hentJournalpost(anyString(), any(KildesystemIdenfikator.class)))
        .thenReturn(HttpResponse.from(HttpStatus.I_AM_A_TEAPOT));

    var response = securedTestRestTemplate.exchange(
        "http://localhost:" + port + "/bidrag-dokument/journal/BID-123?saksnummer=777",
        HttpMethod.GET,
        null,
        JournalpostResponse.class
    );

    assertAll(
        () -> assertThat(response).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.I_AM_A_TEAPOT),
        () -> {
          var loggingEventCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
          verify(appenderMock, atLeastOnce()).doAppend(loggingEventCaptor.capture());
        }
    );
  }
}
