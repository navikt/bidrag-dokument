package no.nav.bidrag.dokument.controller;

import static no.nav.bidrag.dokument.BidragDokumentLocal.SECURE_TEST_PROFILE;
import static no.nav.bidrag.dokument.BidragDokumentLocal.TEST_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate;
import no.nav.bidrag.dokument.BidragDokumentLocal;
import no.nav.bidrag.dokument.dto.DokumentTilgangResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles({TEST_PROFILE, SECURE_TEST_PROFILE})
@DisplayName("DokumentController")
@SpringBootTest(classes = BidragDokumentLocal.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DokumentControllerTest {

  @Autowired
  private HttpHeaderTestRestTemplate securedTestRestTemplate;
  @LocalServerPort
  private int port;
  @MockBean
  private RestTemplate restTemplateMock;

  @Test
  @DisplayName("skal spørre brevserver om tilgang til dokument")
  void skalVideresendeRequestOmTilgangTilDokument() {
    when(restTemplateMock.exchange(eq("/tilgang/BID-123/dokref"), eq(HttpMethod.GET), any(), eq(DokumentTilgangResponse.class)))
        .thenReturn(new ResponseEntity<>(new DokumentTilgangResponse("urlWithToken", "BREVLAGER"), HttpStatus.I_AM_A_TEAPOT));

    var dokumentUrlResponse = Optional.of(securedTestRestTemplate.exchange(
        localhostUrl("/bidrag-dokument/tilgang/BID-123/dokref"),
        HttpMethod.GET,
        null,
        DokumentTilgangResponse.class
    ));

    assertThat(dokumentUrlResponse).hasValueSatisfying(response -> assertAll(
        () -> assertThat(response.getStatusCode()).as("status").isEqualTo(HttpStatus.I_AM_A_TEAPOT),
        () -> assertThat(response).extracting(ResponseEntity::getBody).as("url")
            .isEqualTo(new DokumentTilgangResponse("urlWithToken", "BREVLAGER")))
    );
  }

  @Test
  @DisplayName("Skal git status 401 dersom token mangler")
  void skalGiStatus401DersomTokenMangler() {

    var testRestTemplate = new TestRestTemplate();

    var responseEntity = testRestTemplate.exchange(localhostUrl("/bidrag-dokument/tilgang/BID-123/dokref"), HttpMethod.GET, null, String.class);

    assertEquals(responseEntity.getStatusCode(), HttpStatus.UNAUTHORIZED);
  }

  private String localhostUrl(@SuppressWarnings("SameParameterValue") String url) {
    return "http://localhost:" + port + url;
  }
}
