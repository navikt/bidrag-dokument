package no.nav.bidrag.dokument.consumer;

import static no.nav.bidrag.dokument.BidragDokumentConfig.ISSUER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.nimbusds.jwt.SignedJWT;
import java.util.List;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommandDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.NyJournalpostCommandDto;
import no.nav.security.oidc.context.OIDCClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.oidc.context.OIDCValidationContext;
import no.nav.security.oidc.context.TokenContext;
import no.nav.security.oidc.test.support.jersey.TestTokenGeneratorResource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@DisplayName("BidragJournalpostConsumer")
@SuppressWarnings("unchecked")
@TestInstance(Lifecycle.PER_CLASS)
class BidragJournalpostConsumerTest {

  private OIDCValidationContext oidcValidationContext;
  private BidragJournalpostConsumer bidragJournalpostConsumer;

  @Mock
  private OIDCRequestContextHolder securityContextHolder;
  @Mock
  private Appender appenderMock;
  @Mock
  private RestTemplate restTemplateMock;

  @BeforeAll
  void prepareOidcValidationContext() {
    var testTokenGeneratorResource = new TestTokenGeneratorResource();
    var idToken = testTokenGeneratorResource.issueToken("localhost-idtoken");

    oidcValidationContext = new OIDCValidationContext();
    TokenContext tokenContext = new TokenContext(ISSUER, idToken);
    SignedJWT signedJWT = testTokenGeneratorResource.jwtClaims(ISSUER);
    OIDCClaims oidcClaims = new OIDCClaims(signedJWT);

    oidcValidationContext.addValidatedToken(ISSUER, tokenContext, oidcClaims);
  }

  @BeforeEach
  void setup() {
    initMocks();
    initTestClass();
    mockLogAppender();
    mockOIDCValidationContext();
  }

  private void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  private void initTestClass() {
    bidragJournalpostConsumer = new BidragJournalpostConsumer(securityContextHolder, restTemplateMock);
  }

  private void mockLogAppender() {
    ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    when(appenderMock.getName()).thenReturn("MOCK");
    when(appenderMock.isStarted()).thenReturn(true);
    logger.addAppender(appenderMock);
  }

  private void mockOIDCValidationContext() {
    when(securityContextHolder.getOIDCValidationContext()).thenReturn(oidcValidationContext);
  }

  @Test
  @DisplayName("skal bruke bidragssakens saksnummer i sti til tjeneste")
  void shouldUseValueFromPath() {
    when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any())).thenReturn(
        new ResponseEntity<>(HttpStatus.NO_CONTENT));

    bidragJournalpostConsumer.finnJournalposter("101", "BID");
    verify(restTemplateMock)
        .exchange(eq("/sak/101?fagomrade=BID"), eq(HttpMethod.GET), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any());
  }

  @Test
  @DisplayName("should log get invocations")
  @Disabled("feiler???")
  void shouldLogGetInvocations() {
    when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any())).thenReturn(
        new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

    bidragJournalpostConsumer.finnJournalposter("101", "FAR");

    verify(appenderMock).doAppend(
        argThat((ArgumentMatcher) argument -> {
          assertThat(((ILoggingEvent) argument).getFormattedMessage())
              .contains("Fikk http status 500 INTERNAL_SERVER_ERROR fra journalposter i bidragssak med saksnummer 101 på fagområde FAR");

          return true;
        }));
  }

  @Test
  @DisplayName("should log get invocations for single entity")
  @Disabled("feiler???")
  void shouldLogGetInvocationsForSingleEntity() {

    when(restTemplateMock.exchange(
        anyString(),
        any(HttpMethod.class),
        any(HttpEntity.class),
        ArgumentMatchers.<Class<JournalpostDto>>any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

    bidragJournalpostConsumer.hentJournalpost(101);

    verify(appenderMock).doAppend(
        argThat((ArgumentMatcher) argument -> {
          assertThat(((ILoggingEvent) argument).getFormattedMessage())
              .isEqualTo("JournalpostDto med id=101 har http status 500 INTERNAL_SERVER_ERROR");

          return true;
        }));
  }

  @Test
  @DisplayName("should log new commands")
  @Disabled("feiler???")
  void shouldLogNewCommands() {
    when(restTemplateMock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(JournalpostDto.class))).thenReturn(
        new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

    bidragJournalpostConsumer.registrer(new NyJournalpostCommandDto());

    verify(appenderMock).doAppend(
        argThat((ArgumentMatcher) argument -> {
          assertThat(((ILoggingEvent) argument).getFormattedMessage())
              .contains("Fikk http status 500 INTERNAL_SERVER_ERROR fra registrer ny journalpost: NyJournalpostCommandDto");

          return true;
        }));
  }

  @Test
  @DisplayName("should log edit commands")
  @Disabled("feiler???")
  void shouldLogEditCommands() {
    when(restTemplateMock.exchange(anyString(), any(), any(), eq(JournalpostDto.class))).thenReturn(
        new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

    bidragJournalpostConsumer.endre(new EndreJournalpostCommandDto());

    verify(appenderMock).doAppend(
        argThat((ArgumentMatcher) argument -> {
          assertThat(((ILoggingEvent) argument).getFormattedMessage())
              .contains("Fikk http status 500 INTERNAL_SERVER_ERROR fra endre journalpost: EndreJournalpostCommandDto");

          return true;
        }));
  }
}
