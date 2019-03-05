package no.nav.bidrag.dokument.aop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import java.util.HashSet;
import java.util.Optional;
import no.nav.bidrag.dokument.BidragDokumentLocal;
import no.nav.bidrag.dokument.controller.JournalpostController;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommandDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.oidc.context.OIDCValidationContext;
import no.nav.security.oidc.context.TokenContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles("dev")
@DisplayName("ExceptionLogger")
@SpringBootTest(classes = BidragDokumentLocal.class, webEnvironment = WebEnvironment.DEFINED_PORT)
class ExceptionLoggerTest {

  @Autowired
  private JournalpostController journalpostController;
  @MockBean
  private OIDCRequestContextHolder oidcRequestContextHolderMock;
  @MockBean
  private Appender appenderMock;
  @MockBean
  private RestTemplate restTemplateMock;

  @BeforeEach
  void mockBearerToken() {
    OIDCValidationContext oidcValidationContextMock = mock(OIDCValidationContext.class);
    TokenContext tokenContextMock = mock(TokenContext.class);

    when(oidcRequestContextHolderMock.getOIDCValidationContext()).thenReturn(oidcValidationContextMock);
    when(oidcValidationContextMock.getToken(anyString())).thenReturn(tokenContextMock);
    when(tokenContextMock.getIdToken()).thenReturn("a token");
  }

  @BeforeEach
  @SuppressWarnings("unchecked")
  void mockLogAppender() {
    ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    when(appenderMock.getName()).thenReturn("MOCK");
    when(appenderMock.isStarted()).thenReturn(true);
    logger.addAppender(appenderMock);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("skal logge exception som oppstår")
  void skalLoggeExceptionSomOppstar() {
    var logMeldinger = new HashSet<String>();
    when(restTemplateMock.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(JournalpostDto.class)))
        .thenThrow(new IllegalStateException("something fishy happened"));

    assertThatIllegalStateException().isThrownBy(() ->
        journalpostController.put(endreJournalpostCommandMedId101(), "BID-101")
    ).withMessage("something fishy happened");

    verify(appenderMock, atLeastOnce()).doAppend(
        argThat((ArgumentMatcher) argument -> {
          logMeldinger.add(((ILoggingEvent) argument).getFormattedMessage());

          return true;
        }));

    assertThat(Optional.of(String.join("", logMeldinger))).hasValueSatisfying(logs -> assertAll(
        () -> assertThat(logs).contains("journalpostId=101"),
        () -> assertThat(logs).contains("java.lang.IllegalStateException: something fishy happened")
    ));
  }

  private EndreJournalpostCommandDto endreJournalpostCommandMedId101() {
    return new EndreJournalpostCommandDto(
        "101", null, null, null, null, null, null
    );
  }
}