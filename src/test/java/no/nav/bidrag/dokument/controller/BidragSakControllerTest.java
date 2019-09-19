package no.nav.bidrag.dokument.controller;

import static java.util.Collections.singletonList;
import static no.nav.bidrag.dokument.BidragDokumentLocal.SECURE_TEST_PROFILE;
import static no.nav.bidrag.dokument.BidragDokumentLocal.TEST_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate;
import no.nav.bidrag.dokument.BidragDokumentLocal;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest(classes = BidragDokumentLocal.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({TEST_PROFILE, SECURE_TEST_PROFILE})
@DisplayName("BidragSakController")
class BidragSakControllerTest {

  @LocalServerPort
  private int port;
  @MockBean
  private RestTemplate restTemplateMock;
  @Autowired
  private HttpHeaderTestRestTemplate securedTestRestTemplate;

  @Test
  @SuppressWarnings("unchecked")
  @DisplayName("skal finne Journalposter for en bidragssak")
  void skalFinneJournalposterForEnBidragssak() {
    when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any()))
        .thenReturn(new ResponseEntity<>(singletonList(new JournalpostDto()), HttpStatus.OK)); // blir kalla en gang i arkiv og en gang i brevlager

    var listeMedJournalposterResponse = securedTestRestTemplate.exchange(
        lagSakjournalUrlForFagomradeBid("/1001"), HttpMethod.GET, null, responseTypeErListeMedJournalposter()
    );

    assertThat(optional(listeMedJournalposterResponse)).hasValueSatisfying(
        response -> assertAll(
            () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
            // () -> assertThat(response.getBody()).hasSize(2), skal kalle også kalle bidrag-dokument-arkiv
            () -> assertThat(response.getBody()).hasSize(1),
            // () -> verify(restTemplateMock, times(2)) skal kalle også kalle bidrag-dokument-arkiv
            () -> verify(restTemplateMock)
                .exchange(eq("/sakjournal/1001?fagomrade=BID"), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any())
        )
    );
  }

  @Test
  @DisplayName("skal få BAD_REQUEST(400) som statuskode når saksnummer ikke er et heltall")
  void skalFaBadRequestNarSaksnummerIkkeErHeltall() {
    var journalposterResponse = securedTestRestTemplate.exchange(
        lagSakjournalUrlForFagomradeBid("/xyz"), HttpMethod.GET, null, responseTypeErListeMedJournalposter()
    );

    assertThat(optional(journalposterResponse)).hasValueSatisfying(response -> assertAll(
        () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
        () -> assertThat(response.getBody()).isNull())
    );
  }

  @Test
  @SuppressWarnings("unchecked")
  @DisplayName("skal hente sakjournal fra bidrag-dokument-arkiv såfremt bidrag-dokument-journalpost")
  void skalHenteSakJournalFraBidragDokumentArkiv() {
    when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any()))
        .thenReturn(new ResponseEntity<>(singletonList(new JournalpostDto()), HttpStatus.OK));

    var listeMedJournalposterResponse = securedTestRestTemplate.exchange(
        lagSakjournalUrlForFagomradeBid("/2020001"), HttpMethod.GET, null, responseTypeErListeMedJournalposter()
    );

//    assertThat(listeMedJournalposterResponse.getBody()).hasSize(2); skal kalle også kalle bidrag-dokument-arkiv
    assertThat(listeMedJournalposterResponse.getBody()).hasSize(1);
  }

  private String lagSakjournalUrlForFagomradeBid(String saksnummer) {
    return UriComponentsBuilder
        .fromHttpUrl("http://localhost:" + port + "/bidrag-dokument/sakjournal/" + saksnummer)
        .queryParam("fagomrade", "BID")
        .toUriString();
  }

  private <T> Optional<T> optional(T type) {
    return Optional.ofNullable(type);
  }

  private ParameterizedTypeReference<List<JournalpostDto>> responseTypeErListeMedJournalposter() {
    return new ParameterizedTypeReference<>() {
    };
  }
}