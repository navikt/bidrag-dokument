package no.nav.bidrag.dokument.consumer;

import static no.nav.bidrag.dokument.BidragDokumentConfig.ISSUER;
import static no.nav.bidrag.dokument.consumer.ConsumerUtil.addSecurityHeader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.nimbusds.jwt.SignedJWT;
import java.util.Optional;
import no.nav.bidrag.dokument.dto.JournalpostDto;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@DisplayName("BidragArkivConsumer")
@TestInstance(Lifecycle.PER_CLASS)
class BidragArkivConsumerTest {

  private String idToken;
  private OIDCValidationContext oidcValidationContext;
  private BidragArkivConsumer bidragArkivConsumer;

  @Mock
  private OIDCRequestContextHolder securityContextHolder;
  @Mock
  private Appender appenderMock;
  @Mock
  private RestTemplate restTemplateMock;

  @BeforeAll
  void prepareOidcValidationContext() {
    var testTokenGeneratorResource = new TestTokenGeneratorResource();
    idToken = testTokenGeneratorResource.issueToken("localhost-idtoken");

    oidcValidationContext = new OIDCValidationContext();
    TokenContext tokenContext = new TokenContext(ISSUER, idToken);
    SignedJWT signedJWT = testTokenGeneratorResource.jwtClaims(ISSUER);
    OIDCClaims oidcClaims = new OIDCClaims(signedJWT);

    oidcValidationContext.addValidatedToken(ISSUER, tokenContext, oidcClaims);
  }

  @BeforeEach
  void setUp() {
    initMocks();
    initTestClass();
    mockLogAppender();
    mockOIDCValidationContext();
  }

  private void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  private void initTestClass() {
    bidragArkivConsumer = new BidragArkivConsumer(securityContextHolder, restTemplateMock);
  }

  @SuppressWarnings("unchecked")
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
  @DisplayName("skal hente en journalpost med spring sin RestTemplate")
  void skalHenteJournalpostMedRestTemplate() {

    when(restTemplateMock.exchange(
        anyString(),
        any(HttpMethod.class),
        any(HttpEntity.class),
        ArgumentMatchers.<Class<JournalpostDto>>any())).thenReturn(new ResponseEntity<>(enJournalpostMedJournaltilstand("ENDELIG"), HttpStatus.OK));

    Optional<JournalpostDto> journalpostOptional = bidragArkivConsumer.hentJournalpost(101);
    JournalpostDto journalpostDto = journalpostOptional.orElseThrow(() -> new AssertionError("Ingen Dto fra manager!"));

    assertThat(journalpostDto.getInnhold()).isEqualTo("ENDELIG");

    verify(restTemplateMock).exchange(
        "/journalpost/101",
        HttpMethod.GET,
        addSecurityHeader(null, getBearerToken()),
        JournalpostDto.class);
  }

  private JournalpostDto enJournalpostMedJournaltilstand(@SuppressWarnings("SameParameterValue") String innhold) {
    JournalpostDto journalpostDto = new JournalpostDto();
    journalpostDto.setInnhold(innhold);

    return journalpostDto;
  }

  @Test
  @SuppressWarnings("unchecked")
  @DisplayName("skalLoggeHentJournalpost")
  @Disabled("feiler???")
  void skalLoggeHentJournalpost() {

    when(restTemplateMock.exchange(
        anyString(),
        any(HttpMethod.class),
        any(HttpEntity.class),
        ArgumentMatchers.<Class<JournalpostDto>>any())).thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

    bidragArkivConsumer.hentJournalpost(123);

    verify(appenderMock, times(1)).doAppend(
        argThat((ArgumentMatcher) argument -> {
          assertThat(((ILoggingEvent) argument).getFormattedMessage())
              .contains("Journalpost med id=123 har http status 500 INTERNAL_SERVER_ERROR");

          return true;
        }));
  }

  String getBearerToken() {
    return "Bearer " + idToken;
  }
}
