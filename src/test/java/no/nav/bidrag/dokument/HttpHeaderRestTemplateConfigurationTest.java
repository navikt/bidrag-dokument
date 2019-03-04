package no.nav.bidrag.dokument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import no.nav.bidrag.dokument.HttpHeaderRestTemplateConfiguration.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

@DisplayName("HttpHeaderRestTemplateConfiguration")
class HttpHeaderRestTemplateConfigurationTest {

  private HttpHeaderRestTemplateConfiguration httpHeaderRestTemplateConfiguration = new HttpHeaderRestTemplateConfiguration();
  private Set<String> logMeldinger = new HashSet<>();

  @Mock
  private Appender appenderMock;
  @Mock
  private Header headerMock;
  @Mock
  private Type typeMock;

  @BeforeEach
  void initMocks() {
    MockitoAnnotations.initMocks(this);
    mockLogAppender();
  }

  @SuppressWarnings("unchecked")
  private void mockLogAppender() {
    ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    when(appenderMock.getName()).thenReturn("MOCK");
    when(appenderMock.isStarted()).thenReturn(true);
    logger.addAppender(appenderMock);
  }

  @Test
  @SuppressWarnings("unchecked")
  @DisplayName("skal logge hvilke http headers den bruker")
  void skalLoggeBrukAvHttpHeader() {
    when(headerMock.name()).thenReturn("JUNIT_HEADER");
    when(headerMock.value()).thenReturn("header value");
    httpHeaderRestTemplateConfiguration.addHeaderGenerator(() -> headerMock);

    httpHeaderRestTemplateConfiguration.httpEntityCallback(null, typeMock);

    assertAll(
        () -> verify(appenderMock).doAppend(
            argThat((ArgumentMatcher) argument -> {
              logMeldinger.add(((ILoggingEvent) argument).getFormattedMessage());

              return true;
            })),
        () -> assertThat(String.join("\n", logMeldinger)).contains("Using JUNIT_HEADER: header value")
    );
  }

  @Test
  @SuppressWarnings("unchecked")
  @DisplayName("skal logge eksisterende headers fra gitt request object")
  void skalLoggeBrukAvEksisterendeHttpHeader() {
    HttpHeaders existingHttpHeaders = new HttpHeaders();
    existingHttpHeaders.add("EXISTING_HEADER", "existing value");

    when(headerMock.name()).thenReturn("ADDITIONAL_HEADER");
    when(headerMock.value()).thenReturn("additional value");

    httpHeaderRestTemplateConfiguration.addHeaderGenerator(() -> headerMock);

    httpHeaderRestTemplateConfiguration.httpEntityCallback(new HttpEntity<>(null, existingHttpHeaders), typeMock);

    assertAll(
        () -> verify(appenderMock, atLeastOnce()).doAppend(
            argThat((ArgumentMatcher) argument -> {
              logMeldinger.add(((ILoggingEvent) argument).getFormattedMessage());

              return true;
            })),
        () -> {
          String alleMeldinger = String.join("\n", logMeldinger);

          assertAll(
              () -> assertThat(alleMeldinger).contains("Using EXISTING_HEADER: existing value"),
              () -> assertThat(alleMeldinger).contains("Using ADDITIONAL_HEADER: additional value")
          );
        }
    );
  }

  @Test
  @DisplayName("skal feile når httpEntityCallback brukes med request body som ikke er en HttpEntity")
  void skalFeileNaarHttpEntityCallbackBrukesMedTypeSomIkkeErAvHttpEntity() {
    httpHeaderRestTemplateConfiguration.addHeaderGenerator(() -> headerMock);

    assertThatIllegalStateException()
        .isThrownBy(() -> httpHeaderRestTemplateConfiguration.httpEntityCallback("a request body", typeMock))
        .withMessage("String cannot be used as a request body for a HttpEntityCallback");

    assertThatIllegalStateException()
        .isThrownBy(() -> httpHeaderRestTemplateConfiguration.httpEntityCallback(new Object(), typeMock))
        .withMessage("Object cannot be used as a request body for a HttpEntityCallback");
  }
}
