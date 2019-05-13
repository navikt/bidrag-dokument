package no.nav.bidrag.dokument.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import no.nav.bidrag.dokument.BidragDokumentConfig.OidcTokenManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@DisplayName("SecurityTokenConsumer")
@SuppressWarnings("unchecked")
class SecurityTokenConsumerTest {

  private SecurityTokenConsumer securityTokenConsumer;

  @Mock
  private RestTemplate restTemplateMock;

  @Mock
  private OidcTokenManager oidcTokenManagerMock;

  private static final String SYSTEM_USER = "username";
  private static final String SYSTEM_PASSWORD = "password";
  private static final String SAML_TOKEN = "saml token";
  private static final String OIDC_TOKEN = "oidc token";

  @BeforeEach
  void setup() {
    initMocks();
    initTestClass();
  }

  private void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  private void initTestClass() {
    securityTokenConsumer = new SecurityTokenConsumer(restTemplateMock, SYSTEM_USER, SYSTEM_PASSWORD, oidcTokenManagerMock);
  }

  @Test
  @DisplayName("skal kalle tjenesten med riktig path og type")
  void skalKalleTjenestenMedRiktigPathOgType() {
    securityTokenConsumer.konverterOidcTokenTilSamlToken();
    verify(restTemplateMock).postForEntity(eq(SecurityTokenConsumer.PATH_SECURITY_TOKEN), any(), eq(Map.class));
  }

  @Test
  @DisplayName("skal bygge opp HttpEntity som en map med oidc token")
  void skalByggeOppHttpEntitySomEnMapMedOidcToken() {

    when(oidcTokenManagerMock.fetchToken()).thenReturn(OIDC_TOKEN);

    securityTokenConsumer.konverterOidcTokenTilSamlToken();

    ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
    verify(restTemplateMock).postForEntity(eq(SecurityTokenConsumer.PATH_SECURITY_TOKEN), captor.capture(), eq(Map.class));

    var muligBodySomJsonMap = Optional.ofNullable(captor.getValue()).map(httpEntity -> (Map<String, String>) httpEntity.getBody());
    assertThat(muligBodySomJsonMap).hasValueSatisfying(jsonMap -> assertThat(jsonMap.get("subject_token")).isEqualTo(OIDC_TOKEN));
  }

  @Test
  @DisplayName("skal sende med authentication header som inneholder basic authentication")
  void skalSendeMedAuthHeaderSomInneholderBasicAuth() {

    securityTokenConsumer.konverterOidcTokenTilSamlToken();

    ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
    verify(restTemplateMock).postForEntity(eq(SecurityTokenConsumer.PATH_SECURITY_TOKEN), captor.capture(), eq(Map.class));

    var headers = Optional.ofNullable(captor.getValue()).map(HttpEntity::getHeaders);

    assertThat(headers).hasValueSatisfying(httpHeaders -> assertThat(httpHeaders.get(HttpHeaders.AUTHORIZATION)).
        contains("Basic " + Base64.getEncoder().encodeToString((SYSTEM_USER + ":" + SYSTEM_PASSWORD).getBytes())));
  }

  @Test
  @DisplayName("skal hente ut saml token fra response")
  void skalHenteUtSamlTokenFraResponse() {

    Map<String, String> jsonAsMap = new HashMap<>();
    jsonAsMap.put("access_token", SAML_TOKEN);
    when(restTemplateMock.postForEntity(anyString(), any(), eq(Map.class))).thenReturn(new ResponseEntity<>(jsonAsMap, HttpStatus.OK));

    var samlTokenResponse = securityTokenConsumer.konverterOidcTokenTilSamlToken();

    assertThat(samlTokenResponse.fetchOptionalResult()).hasValueSatisfying(token -> assertThat(token).isEqualTo(SAML_TOKEN));
  }
}
