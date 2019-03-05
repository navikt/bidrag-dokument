package no.nav.bidrag.dokument.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponseWrapper;
import no.nav.bidrag.dokument.BidragDokumentLocal;
import no.nav.bidrag.dokument.SecuredTestRestTemplateConfiguration.SecuredTestRestTemplate;
import no.nav.bidrag.dokument.service.JournalpostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
@ActiveProfiles("dev")
@DisplayName("CorrelationIdFilter")
class CorrelationIdFilterTest {

  private static final String CORRELATION_ID = "X_CORRELATION_ID";

  @Autowired
  private SecuredTestRestTemplate securedTestRestTemplate;
  @MockBean
  private JournalpostService journalpostServiceMock;
  @LocalServerPort
  private int port;

  private Set<String> logMeldinger = new HashSet<>();

  @MockBean
  private Appender appenderMock;

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

  @Nested
  @DisplayName("funksjonstest")
  class Functional {

    private CorrelationIdFilter correlationIdFilter = new CorrelationIdFilter();

    @Mock
    private FilterChain filterChainMock;

    @Mock
    private HttpServletRequest httpServletRequestMock;

    @Mock
    private HttpServletResponseWrapper httpServletResponseMock;

    @BeforeEach
    void initMocksAndWrapper() {
      MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("skal legge HttpHeader.CORRELATION_ID på response")
    void skalLeggeHttpHeaderCorrelationIdPaaResponse() throws IOException, ServletException {
      when(httpServletRequestMock.getRequestURI()).thenReturn("somewhere");

      correlationIdFilter.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);

      verify(httpServletResponseMock).addHeader(eq(CORRELATION_ID), anyString());
    }

    @Test
    @DisplayName("skal ikke legge HttpHeader.CORRELATION_ID på response når den allerede eksisterer")
    void skalIkkeLeggeHttpHeaderCorrelationIdPaaResponseNaarDenAlleredeEksisterer() throws IOException, ServletException {
      when(httpServletRequestMock.getRequestURI()).thenReturn("somewhere");
      when(httpServletRequestMock.getHeader(CORRELATION_ID)).thenReturn("svada");
      when(httpServletResponseMock.containsHeader(CORRELATION_ID)).thenReturn(true);

      correlationIdFilter.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);

      verify(httpServletResponseMock, never()).addHeader(anyString(), anyString());
    }

    @Test
    @DisplayName("skal bruke request uri som correlation id")
    void skalBrukeRequestUriSomCorrelationId() throws IOException, ServletException {
      when(httpServletRequestMock.getRequestURI()).thenReturn("somewhere");
      correlationIdFilter.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);

      ArgumentCaptor<String> correlationCaptor = ArgumentCaptor.forClass(String.class);
      verify(httpServletResponseMock).addHeader(eq(CORRELATION_ID), correlationCaptor.capture());

      assertThat(correlationCaptor.getValue()).contains("(somewhere)");
    }

    @Test
    @DisplayName("skal bruke siste del av request uri som correlation id")
    void skalBrukeSisteDelAvRequestUriSomCorrelationId() throws IOException, ServletException {
      when(httpServletRequestMock.getRequestURI()).thenReturn("/en/forbanna/journalpost");
      correlationIdFilter.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);

      ArgumentCaptor<String> correlationCaptor = ArgumentCaptor.forClass(String.class);
      verify(httpServletResponseMock).addHeader(eq(CORRELATION_ID), correlationCaptor.capture());

      assertThat(correlationCaptor.getValue()).contains("(journalpost)");
    }
  }
}
