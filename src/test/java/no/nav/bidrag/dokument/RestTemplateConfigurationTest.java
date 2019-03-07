package no.nav.bidrag.dokument;

import static org.assertj.core.api.Assertions.assertThat;
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
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.oidc.context.OIDCValidationContext;
import no.nav.security.oidc.context.TokenContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

@DisplayName("RestTemplateConfiguration")
class RestTemplateConfigurationTest {

  private RestTemplateConfiguration restTemplateConfiguration = new RestTemplateConfiguration();
  private Set<String> logMeldinger = new HashSet<>();

  @Mock
  private Appender appenderMock;
  @Mock
  private OIDCValidationContext oidcValidationContextMock;
  @Mock
  private OIDCRequestContextHolder oidcRequestContextHolderMock;
  @Mock
  private TokenContext tokenContextMock;
  @Mock
  private Type typeMock;

  @BeforeEach
  void initMocks() {
    MockitoAnnotations.initMocks(this);
    mockLogAppender();
    mockOidcTokenContext();
  }

  @SuppressWarnings("unchecked")
  private void mockLogAppender() {
    ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    when(appenderMock.getName()).thenReturn("MOCK");
    when(appenderMock.isStarted()).thenReturn(true);
    logger.addAppender(appenderMock);
  }

  private void mockOidcTokenContext() {
    when(oidcRequestContextHolderMock.getOIDCValidationContext()).thenReturn(oidcValidationContextMock);
    when(oidcValidationContextMock.getToken("isso")).thenReturn(tokenContextMock);
  }

  @Test
  @DisplayName("skal gi RestTemplate som setter AUTHORIZATION dynamisk")
  void skalSetteAuthorizationHeaderValueDynamisk() {
    when(tokenContextMock.getIdToken())
        .thenReturn("one")
        .thenReturn("two")
        .thenReturn("three");

    RestTemplate restTemplate = restTemplateConfiguration.restTemplate(oidcRequestContextHolderMock);

    assertAll(
        () -> {
          restTemplate.httpEntityCallback(null, typeMock);
          verifyLogging();
          assertThat(logMeldinger.toString()).contains("Bearer one").doesNotContain("two", "three");
        }, () -> {
          restTemplate.httpEntityCallback(null, typeMock);
          verifyLogging();
          assertThat(logMeldinger.toString()).contains("Bearer one", "Bearer two").doesNotContain("three");
        }, () -> {
          restTemplate.httpEntityCallback(null, typeMock);
          verifyLogging();
          assertThat(logMeldinger.toString()).contains("Bearer one", "Bearer two", "Bearer three");
        }
    );
  }

  @SuppressWarnings("unchecked")
  private void verifyLogging() {
    verify(appenderMock, atLeastOnce()).doAppend(
        argThat((ArgumentMatcher) argument -> {
          logMeldinger.add(((ILoggingEvent) argument).getFormattedMessage());

          return true;
        }));
  }
}
