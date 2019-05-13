package no.nav.bidrag.dokument.consumer;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import no.nav.bidrag.commons.web.HttpStatusResponse;
import no.nav.bidrag.dokument.BidragDokumentConfig.OidcTokenManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SecurityTokenConsumer {

  private static final String ACCESS_TOKEN_URI = "access_token";
  private static final String BASIC_AUTH_SEPARATOR = ":";
  private static final String GRANT_TYPE_PARAM = "urn:ietf:params:oauth:grant-type:token-exchange";
  private static final String REQUESTED_TOKEN_TYPE_PARAM = "urn:ietf:params:oauth:token-type:saml2";
  private static final String SUBJECT_TOKEN_TYPE_PARAM = "urn:ietf:params:oauth:token-type:access_token";
  static final String PATH_SECURITY_TOKEN = "/rest/v1/sts/token/exchange";

  private final RestTemplate restTemplate;
  private final String systemUser;
  private final String systemPassword;
  private final OidcTokenManager oidcTokenManager;

  public SecurityTokenConsumer(RestTemplate restTemplate, String systemUser, String systemPassword, OidcTokenManager oidcTokenManager) {
    this.restTemplate = restTemplate;
    this.systemUser = systemUser;
    this.systemPassword = systemPassword;
    this.oidcTokenManager = oidcTokenManager;
  }

  public HttpStatusResponse<String> konverterOidcTokenTilSamlToken() {
    var requestEntity = new HttpEntity<>(byggJsonRequest(), byggAuthHeader());

    return Optional.ofNullable(restTemplate.postForEntity(PATH_SECURITY_TOKEN, requestEntity, Map.class))
        .map(this::konverterMottattToken)
        .orElseGet(() -> new HttpStatusResponse<>(HttpStatus.I_AM_A_TEAPOT, null));
  }

  private HttpStatusResponse<String> konverterMottattToken(ResponseEntity<Map> tokenResponseEntity) {
    @SuppressWarnings("unchecked") Map<String, String> body = tokenResponseEntity.getBody();
    return new HttpStatusResponse<>(tokenResponseEntity.getStatusCode(), hentUtSamlToken(body));
  }

  private String hentUtSamlToken(Map<String, String> map) {
    return map != null ? map.get(ACCESS_TOKEN_URI) : "ingen response body tilgjengelig";
  }

  private Map<String, String> byggJsonRequest() {
    Map<String, String> requestBodyMap = new HashMap<>();
    requestBodyMap.put("grant_type", GRANT_TYPE_PARAM);
    requestBodyMap.put("requested_token_type", REQUESTED_TOKEN_TYPE_PARAM);
    requestBodyMap.put("subject_token_type", SUBJECT_TOKEN_TYPE_PARAM);
    requestBodyMap.put("subject_token", oidcTokenManager.fetchToken());

    return requestBodyMap;
  }

  private HttpHeaders byggAuthHeader() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    String auth = systemUser + BASIC_AUTH_SEPARATOR + systemPassword;

    String encodedAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    headers.set("Authorization", encodedAuth);

    return headers;
  }
}
