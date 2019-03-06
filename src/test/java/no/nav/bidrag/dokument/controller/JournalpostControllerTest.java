package no.nav.bidrag.dokument.controller;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import no.nav.bidrag.dokument.BidragDokumentLocal;
import no.nav.bidrag.dokument.JournalpostDtoBygger;
import no.nav.bidrag.dokument.security.SecuredTestRestTemplateConfiguration.SecuredTestRestTemplate;
import no.nav.bidrag.dokument.dto.BidragSakDto;
import no.nav.bidrag.dokument.dto.DokumentDto;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommandDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.NyJournalpostCommandDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BidragDokumentLocal.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev", "secure-test"})
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

  @BeforeEach
  void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  @DisplayName("endpoint - hent: " + ENDPOINT_JOURNALPOST)
  class EndpointHentJournalpost {

    private String url = initEndpointUrl(ENDPOINT_JOURNALPOST);

    @Test
    @DisplayName("skal mangle body når journalpost ikke finnes")
    void skalMangleBodyNarJournalpostIkkeFinnes() {
      when(restTemplateMock.exchange("/journalpost/1", HttpMethod.GET, null, JournalpostDto.class))
          .thenReturn(new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT));

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
          () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
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
          () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
          () -> assertThat(response.getBody()).extracting(JournalpostDto::getAvsenderNavn).isEqualTo("Grev Still E. Ben")));
    }

    private JournalpostDto enJournalpostFra(@SuppressWarnings("SameParameterValue") String setAvsenderNavn) {
      JournalpostDto jp = new JournalpostDto();
      jp.setAvsenderNavn(setAvsenderNavn);

      return jp;
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("skal hente BidragSakDto for journalpostens gjelderBrukerId")
    void skalHenteBidragSakDtoForJournalpostensGjelderBrukerId() {
      when(restTemplateMock.exchange("/journalpost/1", HttpMethod.GET, null, JournalpostDto.class))
          .thenReturn(new ResponseEntity<>(enJournalpostFraBrukerId("06127412345"), HttpStatus.I_AM_A_TEAPOT));

      when(restTemplateMock.exchange("/person/sak/06127412345", HttpMethod.GET, null, listAvBidragssakerType()))
          .thenReturn(new ResponseEntity<>(List.of(new BidragSakDto()), HttpStatus.I_AM_A_TEAPOT));

      var journalpostDtoResponseEntity = securedTestRestTemplate.exchange(url + "/bid-1", HttpMethod.GET, null, JournalpostDto.class);

      assertAll(
          () -> assertThat(journalpostDtoResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK),
          () -> assertThat(journalpostDtoResponseEntity.getBody()).isNotNull(),
          () -> assertThat(journalpostDtoResponseEntity.getBody().getBidragssaker()).isNotEmpty()
      );

      verify(restTemplateMock).exchange(eq("/person/sak/06127412345"), any(), any(), (ParameterizedTypeReference<List<BidragSakDto>>) any());
    }

    private JournalpostDto enJournalpostFraBrukerId(@SuppressWarnings("SameParameterValue") String brukerId) {
      JournalpostDto journalpostDto = new JournalpostDto();
      journalpostDto.setGjelderBrukerId(List.of(brukerId));

      return journalpostDto;
    }

    private ParameterizedTypeReference<List<BidragSakDto>> listAvBidragssakerType() {
      return new ParameterizedTypeReference<>() {
      };
    }
  }

  @Nested
  @DisplayName("endpoint - lagre: " + ENDPOINT_JOURNALPOST)
  class EndpointLagreJournalpost {

    private String lagreJournalpostUrl = initEndpointUrl(ENDPOINT_JOURNALPOST);

    @Test
    @DisplayName("skal registrere ny journalpost")
    void skalRegistrereNyJournalpost() {
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(JournalpostDto.class)))
          .thenReturn(new ResponseEntity<>(new JournalpostDtoBygger()
              .medDokumenter(singletonList(new DokumentDto()))
              .medGjelderBrukerId("06127412345")
              .medJournalpostId("BID-101")
              .build(), HttpStatus.CREATED)
          );

      var responseEntity = securedTestRestTemplate.exchange(
          lagreJournalpostUrl, HttpMethod.POST, new HttpEntity<>(new NyJournalpostCommandDto()), JournalpostDto.class
      );

      assertThat(optional(responseEntity)).hasValueSatisfying(response -> assertAll(
          () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED),
          () -> assertThat(response.getBody()).extracting(JournalpostDto::getJournalpostId).isEqualTo("BID-101"))
      );
    }

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
              .medGjelderBrukerId("06127412345")
              .medJournalpostId("BID-101")
              .build(), HttpStatus.I_AM_A_TEAPOT)
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

    private String urlForFagomradeBid(@SuppressWarnings("SameParameterValue") String path) {
      return UriComponentsBuilder
          .fromHttpUrl(initEndpointUrl(ENDPOINT_SAKJOURNAL) + path)
          .queryParam("fagomrade", "BID")
          .toUriString();
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("skal finne Journalposter for en bidragssak")
    void skalFinneJournalposterForEnBidragssak() {
      when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any()))
          .thenReturn(new ResponseEntity<>(asList(new JournalpostDto(), new JournalpostDto()), HttpStatus.I_AM_A_TEAPOT));

      var listeMedJournalposterResponse = securedTestRestTemplate.exchange(
          urlForFagomradeBid("/1001"), HttpMethod.GET, null, responseTypeErListeMedJournalposter()
      );

      assertThat(optional(listeMedJournalposterResponse)).hasValueSatisfying(response -> assertAll(
          () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
          () -> assertThat(response.getBody()).hasSize(2))
      );

      verify(restTemplateMock)
          .exchange(eq("/sakjournal/1001?fagomrade=BID"), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any());
    }
  }

  private ParameterizedTypeReference<List<JournalpostDto>> responseTypeErListeMedJournalposter() {
    return new ParameterizedTypeReference<>() {
    };
  }

  private <T> Optional<ResponseEntity<T>> optional(ResponseEntity<T> responseEntity) {
    return Optional.ofNullable(responseEntity);
  }

  private String initEndpointUrl(@SuppressWarnings("SameParameterValue") String endpoint) {
    return "http://localhost:" + port + contextPath + endpoint;
  }
}
