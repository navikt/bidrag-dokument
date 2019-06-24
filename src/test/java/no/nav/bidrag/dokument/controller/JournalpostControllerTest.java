package no.nav.bidrag.dokument.controller;

import static java.util.Collections.singletonList;
import static no.nav.bidrag.dokument.BidragDokumentLocal.SECURE_TEST_PROFILE;
import static no.nav.bidrag.dokument.BidragDokumentLocal.TEST_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import no.nav.bidrag.commons.web.test.SecuredTestRestTemplate;
import no.nav.bidrag.dokument.BidragDokumentLocal;
import no.nav.bidrag.dokument.JournalpostDtoBygger;
import no.nav.bidrag.dokument.dto.AktorDto;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.DokumentDto;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommandDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BidragDokumentLocal.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({TEST_PROFILE, SECURE_TEST_PROFILE})
@DisplayName("JournalpostController")
class JournalpostControllerTest {

  private static final String ENDPOINT_JOURNALPOST = "/journalpost";
  private static final String ENDPOINT_SAKJOURNAL = "/sakjournal";

  @LocalServerPort
  private int port;
  @MockBean
  private RestTemplate restTemplateMock;
  @Value("${server.servlet.context-path}")
  private String contextPath;
  @Autowired
  private SecuredTestRestTemplate securedTestRestTemplate;

  @Nested
  @DisplayName("endpoint - hent: " + ENDPOINT_JOURNALPOST)
  class EndpointHentJournalpost {

    private String url = initEndpointUrl(ENDPOINT_JOURNALPOST);

    @Test
    @DisplayName("skal mangle body når journalpost ikke finnes")
    void skalMangleBodyNarJournalpostIkkeFinnes() {
      when(restTemplateMock.exchange("/journalpost/1", HttpMethod.GET, null, JournalpostDto.class))
          .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

      var journalpostResponseEntity = securedTestRestTemplate.exchange(url + "/joark-1", HttpMethod.GET, null, JournalpostDto.class);

      verify(restTemplateMock, atLeastOnce()).exchange("/journalpost/1", HttpMethod.GET, null, JournalpostDto.class);

      assertThat(Optional.of(journalpostResponseEntity)).hasValueSatisfying(response -> assertAll(
          () -> assertThat(response.getBody()).isNull(),
          () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT)));
    }

    @Test
    @DisplayName("skal hente Journalpost når den eksisterer")
    void skalHenteJournalpostNarDenEksisterer() {
      when(restTemplateMock.exchange("/journalpost/1", HttpMethod.GET, null, JournalpostDto.class))
          .thenReturn(new ResponseEntity<>(enJournalpostMedInnhold("MIDLERTIDIG"), HttpStatus.I_AM_A_TEAPOT));

      var responseEntity = securedTestRestTemplate.exchange(url + "/joark-1", HttpMethod.GET, null, JournalpostDto.class);

      verify(restTemplateMock, atLeastOnce()).exchange("/journalpost/1", HttpMethod.GET, null, JournalpostDto.class);

      assertThat(Optional.of(responseEntity)).hasValueSatisfying(response -> assertAll(
          () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.I_AM_A_TEAPOT),
          () -> assertThat(response.getBody()).extracting(JournalpostDto::getInnhold).isEqualTo("MIDLERTIDIG")));
    }

    private JournalpostDto enJournalpostMedInnhold(@SuppressWarnings("SameParameterValue") String innhold) {
      JournalpostDto journalpostDto = new JournalpostDto();
      journalpostDto.setInnhold(innhold);

      return journalpostDto;
    }

    @Test
    @DisplayName("skal hente journalpost fra midlertidig brevlager")
    void skalHenteJournalpostFraMidlertidigBrevlager() {
      when(restTemplateMock.exchange("/journalpost/1", HttpMethod.GET, null, JournalpostDto.class))
          .thenReturn(new ResponseEntity<>(enJournalpostFra("Grev Still E. Ben"), HttpStatus.I_AM_A_TEAPOT));

      var responseEntity = securedTestRestTemplate.exchange(url + "/bid-1", HttpMethod.GET, null, JournalpostDto.class);

      verify(restTemplateMock).exchange("/journalpost/1", HttpMethod.GET, null, JournalpostDto.class);

      assertThat(Optional.of(responseEntity)).hasValueSatisfying(response -> assertAll(
          () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.I_AM_A_TEAPOT),
          () -> assertThat(response.getBody()).extracting(JournalpostDto::getAvsenderNavn).isEqualTo("Grev Still E. Ben")));
    }

    private JournalpostDto enJournalpostFra(@SuppressWarnings("SameParameterValue") String setAvsenderNavn) {
      JournalpostDto jp = new JournalpostDto();
      jp.setAvsenderNavn(setAvsenderNavn);
      jp.setGjelderAktor(new AktorDto("06127412345"));

      return jp;
    }
  }

  @Nested
  @DisplayName("endpoint - lagre: " + ENDPOINT_JOURNALPOST)
  class EndpointLagreJournalpost {

    private String lagreJournalpostUrl = initEndpointUrl(ENDPOINT_JOURNALPOST);

    @Test
    @DisplayName("skal få BAD_REQUEST når prefix er ukjent")
    void skalFaBadRequestMedUkjentPrefix() {
      var badRequestResponse = securedTestRestTemplate.exchange(lagreJournalpostUrl + "/svada-1", HttpMethod.GET, null, JournalpostDto.class);

      assertThat(badRequestResponse).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal få BAD_REQUEST når journalpostId ikke er et tall")
    void skalFaBadRequestMedJournalpostIdSomIkkeErEtTall() {
      var badRequestResponse = securedTestRestTemplate.exchange(lagreJournalpostUrl + "/bid-en", HttpMethod.GET, null, JournalpostDto.class);

      assertThat(badRequestResponse).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal få BAD_REQUEST når prefix er ukjent ved endring av journalpost")
    void skalFaBadRequestMedUkjentPrefixVedEndringAvJournalpost() {
      var badRequestResponse = securedTestRestTemplate.exchange(
          lagreJournalpostUrl + "/svada-1", HttpMethod.PUT, new HttpEntity<>(new EndreJournalpostCommandDto()), JournalpostDto.class
      );

      assertThat(badRequestResponse).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal endre journalpost")
    void skalEndreJournalpost() {
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(JournalpostDto.class)))
          .thenReturn(new ResponseEntity<>(new JournalpostDtoBygger()
              .medDokumenter(singletonList(new DokumentDto()))
              .medGjelderAktor("06127412345")
              .medJournalpostId("BID-101")
              .build(), HttpStatus.ACCEPTED)
          );

      var endretJournalpostResponse = securedTestRestTemplate.exchange(
          lagreJournalpostUrl + "/bid-1", HttpMethod.PUT, new HttpEntity<>(new EndreJournalpostCommandDto()), JournalpostDto.class
      );

      assertThat(optional(endretJournalpostResponse)).hasValueSatisfying(response -> assertAll(
          () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED),
          () -> assertThat(response.getBody()).extracting(JournalpostDto::getJournalpostId).isEqualTo("BID-101"))
      );
    }
  }

  @Nested
  @DisplayName("endpoint - hent: " + ENDPOINT_SAKJOURNAL)
  class EndpointJournalpost {

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("skal finne Journalposter for en bidragssak")
    void skalFinneJournalposterForEnBidragssak() {
      when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any()))
          .thenReturn(new ResponseEntity<>(singletonList(new JournalpostDto()), HttpStatus.OK)); // blir kalla en gang i arkiv og en gang i brevlager

      var listeMedJournalposterResponse = securedTestRestTemplate.exchange(
          lagSakjournalUrlForFagomradeBid("/1001"), HttpMethod.GET, null, responseTypeErListeMedJournalposter()
      );

      assertThat(optional(listeMedJournalposterResponse)).hasValueSatisfying(response -> assertAll(
          () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
          () -> assertThat(response.getBody()).hasSize(2),
          () -> verify(restTemplateMock, times(2))
              .exchange(eq("/sakjournal/1001?fagomrade=BID"), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any())
      ));
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

      assertThat(listeMedJournalposterResponse.getBody()).hasSize(2);
    }
  }

  @Nested
  @DisplayName("endpoint - avvik: " + ENDPOINT_JOURNALPOST)
  class Avvik {

    @Test
    @DisplayName("skal feile når man henter avvikshendelser uten å prefikse journalpostId med kildesystem")
    void skalFeileVedHentingAvAvvikshendelserForJournalpostNarJournalpostIdIkkeErPrefiksetMedKildesystem() {
      var responseEntity = securedTestRestTemplate.exchange(initUrl("/avvik/1"), HttpMethod.GET, null, responseTypeErListeMedAvvikType());

      assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal hente avvikshendelser for en journalpost")
    void skalHenteAvvikshendelserForJournalpost() {
      var responseEntity = securedTestRestTemplate.exchange(initUrl("/avvik/BID-1"), HttpMethod.GET, null, responseTypeErListeMedAvvikType());

      assertAll(
          () -> assertThat(responseEntity.getStatusCode()).as("status").isEqualTo(HttpStatus.I_AM_A_TEAPOT),
          () -> assertThat(responseEntity.getBody()).as("avvik").hasSize(1),
          () -> {
            assertThat(responseEntity.getBody()).as("avvik").isNotNull();

            AvvikType avvikType = responseEntity.getBody().get(0);

            assertThat(avvikType).as("bestill orginal").isEqualTo(AvvikType.BESTILL_ORGINAL);
          }
      );
    }

    @Test
    @DisplayName("skal opprette et avvik på en journalpost")
    void skalOppretteAvvikPaJournalpost() {
      var bestillOrginalEntity = new HttpEntity<>(new Avvikshendelse(AvvikType.BESTILL_ORGINAL));
      var responseEntity = securedTestRestTemplate.exchange(initUrl("/avvik/BID-1"), HttpMethod.POST, bestillOrginalEntity, Void.class);

      assertAll(
          () -> assertThat(responseEntity.getStatusCode()).as("status").isEqualTo(HttpStatus.I_AM_A_TEAPOT),
          () -> assertThat(responseEntity.getBody()).as("body").isNull()
      );
    }

    private String initUrl(String relative) {
      return initEndpointUrl(ENDPOINT_JOURNALPOST + relative);
    }

    private ParameterizedTypeReference<List<AvvikType>> responseTypeErListeMedAvvikType() {
      return new ParameterizedTypeReference<>() {
      };
    }
  }

  @Nested
  @DisplayName("all endpoints")
  class AllEndpoints {

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("skal returnere tom respons med advarsel og status kode fra andre rest tjenestester")
    void skalReturnereTomResponsMedAdvarselSomHeader() {
      when(restTemplateMock.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
          .thenThrow(new HttpClientErrorException(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, "holy crap!"));

      var errorResponse = securedTestRestTemplate.exchange(
          lagSakjournalUrlForFagomradeBid("/1001"), HttpMethod.GET, null, responseTypeErListeMedJournalposter()
      );

      assertThat(Optional.of(errorResponse)).hasValueSatisfying(responseEntity -> assertAll(
          () -> assertThat(responseEntity.getStatusCode()).as("status").isEqualTo(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED),
          () -> assertThat(responseEntity.getBody()).as("body").isNull(),
          () -> {
            HttpHeaders httpHeaders = errorResponse.getHeaders();

            assertAll(
                () -> assertThat(httpHeaders.get(HttpHeaders.WARNING)).as("warning header").isNotNull(),
                () -> assertThat(httpHeaders.get(HttpHeaders.WARNING)).as("header value").isEqualTo(List.of("Http client says: 509 holy crap!"))
            );
          }
      ));
    }
  }

  @AfterEach
  void resetRestTemplateMock() {
    Mockito.reset(restTemplateMock);
  }

  private String lagSakjournalUrlForFagomradeBid(String path) {
    return UriComponentsBuilder
        .fromHttpUrl(initEndpointUrl(ENDPOINT_SAKJOURNAL) + path)
        .queryParam("fagomrade", "BID")
        .toUriString();
  }

  private ParameterizedTypeReference<List<JournalpostDto>> responseTypeErListeMedJournalposter() {
    return new ParameterizedTypeReference<>() {
    };
  }

  private <T> Optional<ResponseEntity<T>> optional(ResponseEntity<T> responseEntity) {
    return Optional.ofNullable(responseEntity);
  }

  private String initEndpointUrl(String endpoint) {
    return "http://localhost:" + port + contextPath + endpoint;
  }
}
