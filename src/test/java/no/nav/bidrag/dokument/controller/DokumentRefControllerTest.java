package no.nav.bidrag.dokument.controller;

import static no.nav.bidrag.dokument.BidragDokumentTest.TEST_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import no.nav.bidrag.commons.security.service.OidcTokenManager;
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate;
import no.nav.bidrag.dokument.BidragDokumentTest;
import no.nav.bidrag.dokument.consumer.stub.RestConsumerStub;
import no.nav.bidrag.dokument.dto.DokumentTilgangResponse;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(TEST_PROFILE)
@DisplayName("DokumentController")
@AutoConfigureWireMock(port = 0)
@SpringBootTest(classes = BidragDokumentTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
class DokumentRefControllerTest {

  @Autowired
  private HttpHeaderTestRestTemplate securedTestRestTemplate;

  @LocalServerPort
  private int port;

  @Autowired
  private RestConsumerStub restConsumerStub;

  @Test
  @DisplayName("skal spørre brevserver om tilgang til dokument")
  void skalVideresendeRequestOmTilgangTilDokument() throws IOException {

    var journalpostId = "BID-12312312";
    var dokumentReferanse = "1234";
    var dokumentUrl = "https://dokument-url.no/";
    var type = "BREVLAGER";

    restConsumerStub
        .runGiTilgangTilDokument(journalpostId, dokumentReferanse, dokumentUrl, type, HttpStatus.OK.value());

    var dokumentUrlResponse = Optional.of(securedTestRestTemplate
        .exchange(localhostUrl("/bidrag-dokument/tilgang/" + journalpostId + "/" + dokumentReferanse), HttpMethod.GET, null, DokumentTilgangResponse.class));

    assertThat(dokumentUrlResponse).hasValueSatisfying(
        response -> assertAll(() -> assertThat(response.getStatusCode()).as("status").isEqualTo(HttpStatus.OK),
            () -> assertThat(response).extracting(ResponseEntity::getBody).as("url")
                .isEqualTo(new DokumentTilgangResponse(dokumentUrl, type))));
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
