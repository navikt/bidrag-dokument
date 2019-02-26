package no.nav.bidrag.dokument.consumer;

import static no.nav.bidrag.dokument.BidragDokumentConfig.ISSUER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nimbusds.jwt.SignedJWT;
import java.util.List;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommandDto;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@DisplayName("BidragJournalpostConsumer")
@SuppressWarnings("unchecked")
class BidragJournalpostConsumerTest {

  private static OIDCValidationContext oidcValidationContext;
  private BidragJournalpostConsumer bidragJournalpostConsumer;

  @Mock
  private OIDCRequestContextHolder securityContextHolder;
  @Mock
  private RestTemplate restTemplateMock;

  @BeforeAll
  static void prepareOidcValidationContext() {
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
    mockOIDCValidationContext();
  }

  private void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  private void initTestClass() {
    bidragJournalpostConsumer = new BidragJournalpostConsumer(securityContextHolder, restTemplateMock);
  }

  private void mockOIDCValidationContext() {
    when(securityContextHolder.getOIDCValidationContext()).thenReturn(oidcValidationContext);
  }

  @Test
  @DisplayName("skal bruke bidragssakens saksnummer i sti til tjeneste")
  void shouldUseValueFromPath() {
    when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

    bidragJournalpostConsumer.finnJournalposter("101", "BID");
    verify(restTemplateMock)
        .exchange(
            eq("/sakjournal/101?fagomrade=BID"),
            eq(HttpMethod.GET),
            any(),
            (ParameterizedTypeReference<List<JournalpostDto>>) any()
        );
  }

  @Test
  @DisplayName("skal endre journalpost")
  void skalEndreJournalpost() {
    when(restTemplateMock.exchange(anyString(), any(), any(), (Class<Object>) any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));

    bidragJournalpostConsumer.endre(endreJournalpostCommandMedId101());
    verify(restTemplateMock)
        .exchange(
            eq("/journalpost/101"),
            eq(HttpMethod.PUT),
            any(),
            eq(JournalpostDto.class)
        );
  }

  private EndreJournalpostCommandDto endreJournalpostCommandMedId101() {
    return new EndreJournalpostCommandDto(
        "101", null, null, null, null, null, null, "id"
    );
  }
}
