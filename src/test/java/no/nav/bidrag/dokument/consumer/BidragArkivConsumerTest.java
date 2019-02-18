package no.nav.bidrag.dokument.consumer;

import static no.nav.bidrag.dokument.BidragDokumentConfig.ISSUER;
import static no.nav.bidrag.dokument.consumer.ConsumerUtil.addSecurityHeader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
    mockOIDCValidationContext();
  }

  private void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  private void initTestClass() {
    bidragArkivConsumer = new BidragArkivConsumer(securityContextHolder, restTemplateMock);
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

  String getBearerToken() {
    return "Bearer " + idToken;
  }
}
