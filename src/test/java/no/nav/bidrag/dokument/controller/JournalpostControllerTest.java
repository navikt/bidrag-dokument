package no.nav.bidrag.dokument.controller;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.bidrag.commons.web.EnhetFilter.X_ENHET_HEADER;
import static no.nav.bidrag.dokument.BidragDokumentLocal.SECURE_TEST_PROFILE;
import static no.nav.bidrag.dokument.BidragDokumentLocal.TEST_PROFILE;
import static no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer.createEnhetHeader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate;
import no.nav.bidrag.dokument.BidragDokumentConfig.RestTemplateProvider;
import no.nav.bidrag.dokument.BidragDokumentLocal;
import no.nav.bidrag.dokument.dto.AktorDto;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.JournalpostResponse;
import no.nav.bidrag.dokument.dto.OpprettAvvikshendelseResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest(classes = BidragDokumentLocal.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({TEST_PROFILE, SECURE_TEST_PROFILE})
@DisplayName("JournalpostController")
class JournalpostControllerTest {

  @LocalServerPort
  private int port;
  @MockBean
  private RestTemplateProvider restTemplateProviderMock;
  @MockBean
  private RestTemplate restTemplateMock;

  @Value("${server.servlet.context-path}")
  private String contextPath;

  @Autowired
  private HttpHeaderTestRestTemplate httpHeaderTestRestTemplate;

  @AfterEach
  void resetMocking() {
    Mockito.reset(restTemplateMock);
  }

  private <T> HttpEntity<T> initHttpEntity(T body, CustomHeader... customHeaders) {
    var httpHeaders = new HttpHeaders();

    if (customHeaders != null) {
      for (CustomHeader customHeader : customHeaders) {
        httpHeaders.add(customHeader.name, customHeader.value);
      }
    }

    return new HttpEntity<>(body, httpHeaders);
  }

  private <T> Optional<ResponseEntity<T>> optional(ResponseEntity<T> responseEntity) {
    return Optional.ofNullable(responseEntity);
  }

  private String initEndpointUrl(String endpoint) {
    return "http://localhost:" + port + contextPath + endpoint;
  }

  private static class CustomHeader {

    final String name;
    final String value;

    private CustomHeader(String name, String value) {
      this.name = name;
      this.value = value;
    }
  }

  @Nested
  @DisplayName("hent journalpost")
  class Hent {

    @Test
    @DisplayName("skal mangle body når journalpost ikke finnes")
    void skalMangleBodyNarJournalpostIkkeFinnes() {
      when(restTemplateProviderMock.provideRestTemplate(anyString(), anyString())).thenReturn(restTemplateMock);
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JournalpostResponse.class)))
          .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

      var url = initEndpointUrl("/journal/joark-1?saksnummer=007");
      var journalpostResponseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.GET, null, JournalpostResponse.class);

      assertThat(Optional.of(journalpostResponseEntity)).hasValueSatisfying(response -> assertAll(() -> assertThat(response.getBody()).isNull(),
          () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT),
          () -> verify(restTemplateMock).exchange(eq("/journal/JOARK-1?saksnummer=007"), eq(HttpMethod.GET), any(), eq(JournalpostResponse.class))));
    }

    @Test
    @DisplayName("skal hente Journalpost når den eksisterer")
    void skalHenteJournalpostNarDenEksisterer() {
      when(restTemplateProviderMock.provideRestTemplate(anyString(), anyString())).thenReturn(restTemplateMock);
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JournalpostResponse.class)))
          .thenReturn(new ResponseEntity<>(enJournalpostMedInnhold("MIDLERTIDIG"), HttpStatus.I_AM_A_TEAPOT));

      var url = initEndpointUrl("/journal/joark-2?saksnummer=007");
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.GET, null, JournalpostResponse.class);

      assertThat(Optional.of(responseEntity)).hasValueSatisfying(
          response -> assertAll(() -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.I_AM_A_TEAPOT),
              () -> assertThat(response.getBody()).extracting(JournalpostResponse::getJournalpost).extracting(JournalpostDto::getInnhold)
                  .isEqualTo("MIDLERTIDIG"), () -> verify(restTemplateMock)
                  .exchange(eq("/journal/JOARK-2?saksnummer=007"), eq(HttpMethod.GET), any(), eq(JournalpostResponse.class))));
    }

    @Test
    @DisplayName("Skal gi status 401 dersom token mangler")
    void skalGiStatus401DersomTokenMangler() {

      var testRestTemplate = new TestRestTemplate();

      var url = initEndpointUrl("/journal/joark-2?saksnummer=007");
      var responseEntity = testRestTemplate.exchange(url, HttpMethod.GET, null, String.class);

      assertEquals(responseEntity.getStatusCode(), HttpStatus.UNAUTHORIZED);
    }

    private JournalpostResponse enJournalpostMedInnhold(@SuppressWarnings("SameParameterValue") String innhold) {
      JournalpostDto journalpostDto = new JournalpostDto();
      journalpostDto.setInnhold(innhold);

      return new JournalpostResponse(journalpostDto, emptyList());
    }

    @Test
    @DisplayName("skal hente journalpost fra midlertidig brevlager")
    void skalHenteJournalpostFraMidlertidigBrevlager() {
      when(restTemplateProviderMock.provideRestTemplate(anyString(), anyString())).thenReturn(restTemplateMock);
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JournalpostResponse.class)))
          .thenReturn(new ResponseEntity<>(enJournalpostFra("Grev Still E. Ben"), HttpStatus.I_AM_A_TEAPOT));

      var url = initEndpointUrl("/journal/bid-1?saksnummer=007");
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.GET, null, JournalpostResponse.class);

      assertThat(Optional.of(responseEntity)).hasValueSatisfying(
          response -> assertAll(() -> assertThat(response.getStatusCode()).as("status code").isEqualTo(HttpStatus.I_AM_A_TEAPOT),
              () -> assertThat(response.getBody()).as("JournalpostResponse").extracting(JournalpostResponse::getJournalpost).as("journalpost")
                  .extracting(JournalpostDto::getAvsenderNavn).isEqualTo("Grev Still E. Ben"),
              () -> verify(restTemplateMock).exchange("/journal/BID-1?saksnummer=007", HttpMethod.GET, null, JournalpostResponse.class)));
    }

    private JournalpostResponse enJournalpostFra(@SuppressWarnings("SameParameterValue") String setAvsenderNavn) {
      JournalpostDto jp = new JournalpostDto();
      jp.setAvsenderNavn(setAvsenderNavn);
      jp.setGjelderAktor(new AktorDto("06127412345"));

      return new JournalpostResponse(jp, emptyList());
    }
  }

  @Nested
  @DisplayName("lagre journalpost")
  class Lagre {

    @Test
    @DisplayName("skal få BAD_REQUEST når prefix er ukjent")
    void skalFaBadRequestMedUkjentPrefix() {
      var lagreJournalpostUrl = initEndpointUrl("/journal/svada-1");
      var entityMedEnhetsheader = new HttpEntity<>(new EndreJournalpostCommand(), createEnhetHeader("4802"));
      var badRequestResponse = httpHeaderTestRestTemplate.exchange(lagreJournalpostUrl, HttpMethod.PUT, entityMedEnhetsheader, Void.class);

      assertThat(badRequestResponse).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal få BAD_REQUEST når journalpostId ikke er et tall")
    void skalFaBadRequestMedJournalpostIdSomIkkeErEtTall() {
      var lagreJournalpostUrl = initEndpointUrl("/journal/bid-en?saksnummer=69");
      var badRequestResponse = httpHeaderTestRestTemplate.exchange(lagreJournalpostUrl, HttpMethod.GET, null, JournalpostResponse.class);

      assertThat(badRequestResponse).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal få BAD_REQUEST når prefix er ukjent ved endring av journalpost")
    void skalFaBadRequestMedUkjentPrefixVedEndringAvJournalpost() {
      var lagreJournalpostUrl = initEndpointUrl("/journal/svada-1");

      var badRequestResponse = httpHeaderTestRestTemplate
          .exchange(lagreJournalpostUrl, HttpMethod.PUT, new HttpEntity<>(new EndreJournalpostCommand(), createEnhetHeader("4802")),
              JournalpostResponse.class);

      assertThat(badRequestResponse).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal endre journalpost")
    void skalEndreJournalpost() {
      when(restTemplateProviderMock.provideRestTemplate(anyString(), anyString())).thenReturn(restTemplateMock);
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(Void.class))).thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));

      var lagreJournalpostUrl = initEndpointUrl("/journal/bid-1");
      var endretJournalpostResponse = httpHeaderTestRestTemplate
          .exchange(lagreJournalpostUrl, HttpMethod.PUT, new HttpEntity<>(new EndreJournalpostCommand(), createEnhetHeader("4802")), Void.class);

      assertThat(optional(endretJournalpostResponse)).hasValueSatisfying(
          response -> assertAll(() -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED),
              () -> verify(restTemplateMock).exchange(eq("/journal/bid-1"), eq(HttpMethod.PUT), any(), eq(Void.class))));
    }

    @Test
    @DisplayName("skal videresende headere når journalpost endres")
    void skalVideresendeHeadereVedEndreJournalpost() {
      var advarselHeader = new HttpHeaders();
      advarselHeader.add(HttpHeaders.WARNING, "rofl");
      when(restTemplateProviderMock.provideRestTemplate(anyString(), anyString())).thenReturn(restTemplateMock);
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(Void.class)))
          .thenReturn(new ResponseEntity<>(advarselHeader, HttpStatus.BAD_REQUEST));

      var lagreJournalpostUrl = initEndpointUrl("/journal/bid-1");
      var endretJournalpostResponse = httpHeaderTestRestTemplate
          .exchange(lagreJournalpostUrl, HttpMethod.PUT, new HttpEntity<>(new EndreJournalpostCommand(), createEnhetHeader("4802")), Void.class);

      assertThat(optional(endretJournalpostResponse))
          .hasValueSatisfying(response -> assertAll(() -> assertThat(response.getStatusCode()).as("status").isEqualTo(HttpStatus.BAD_REQUEST), () -> {
            var headers = response.getHeaders();
            assertThat(headers.getFirst(HttpHeaders.WARNING)).as("warning header").isEqualTo("rofl");
          }));
    }
  }

  @Nested
  @DisplayName("avvik på journalpost")
  class Avvik {

    @Test
    @DisplayName("skal feile når man henter avvikshendelser uten å prefikse journalpostId med kildesystem")
    void skalFeileVedHentingAvAvvikshendelserForJournalpostNarJournalpostIdIkkeErPrefiksetMedKildesystem() {
      var url = initEndpointUrl("/journal/1/avvik");
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.GET, null, responseTypeErListeMedAvvikType());

      assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal finne avvikshendelser på en journalpost")
    void skalFinneAvvikshendelserForJournalpost() {
      when(restTemplateProviderMock.provideRestTemplate(anyString(), anyString())).thenReturn(restTemplateMock);
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(responseTypeErListeMedAvvikType())))
          .thenReturn(new ResponseEntity<>(List.of(AvvikType.BESTILL_ORIGINAL), HttpStatus.OK));

      var url = initEndpointUrl("/journal/BID-1/avvik?saksnummer=1001");
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.GET, null, responseTypeErListeMedAvvikType());

      assertAll(() -> assertThat(responseEntity.getStatusCode()).as("status").isEqualTo(HttpStatus.OK),
          () -> assertThat(responseEntity.getBody()).as("avvik").hasSize(1),
          () -> assertThat(responseEntity.getBody()).as("avvik").contains(AvvikType.BESTILL_ORIGINAL), () -> verify(restTemplateMock)
              .exchange(eq("/journal/BID-1/avvik?saksnummer=1001"), eq(HttpMethod.GET), any(), eq(responseTypeErListeMedAvvikType())));
    }

    @Test
    @DisplayName("skal opprette et avvik på en journalpost")
    void skalOppretteAvvikPaJournalpost() {
      when(restTemplateProviderMock.provideRestTemplate(anyString(), anyString())).thenReturn(restTemplateMock);
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(OpprettAvvikshendelseResponse.class)))
          .thenReturn(new ResponseEntity<>(new OpprettAvvikshendelseResponse(AvvikType.BESTILL_ORIGINAL), HttpStatus.CREATED));

      final var enhetsnummer = "4806";
      final var avvikshendelse = new Avvikshendelse(AvvikType.BESTILL_ORIGINAL.name(), enhetsnummer);

      var bestillOriginalEntity = initHttpEntity(avvikshendelse, new CustomHeader(X_ENHET_HEADER, "1234"));
      var url = initEndpointUrl("/journal/BID-4/avvik");
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.POST, bestillOriginalEntity, OpprettAvvikshendelseResponse.class);

      assertAll(() -> assertThat(responseEntity.getStatusCode()).as("status").isEqualTo(HttpStatus.CREATED),
          () -> assertThat(responseEntity.getBody()).as("body").isEqualTo(new OpprettAvvikshendelseResponse(AvvikType.BESTILL_ORIGINAL)),
          () -> verify(restTemplateMock).exchange(eq("/journal/BID-4/avvik"), eq(HttpMethod.POST), any(), eq(OpprettAvvikshendelseResponse.class)));
    }

    @Test
    @DisplayName("skal få bad request uten header value")
    void skalFaBadRequestUtenHeaderValue() {
      final var avvikshendelse = new Avvikshendelse("BESTILL_ORIGINAL", "4806", "1001");
      var ukjentAvvikEntity = initHttpEntity(avvikshendelse);
      var url = initEndpointUrl("/journal/BID-1/avvik");
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.POST, ukjentAvvikEntity, OpprettAvvikshendelseResponse.class);

      assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal få bad request når avvikstype ikke kan parses")
    void skalFaBadRequest() {
      final var avvikshendelse = new Avvikshendelse("AVVIK_IKKE_BLANT_KJENTE_AVVIKSTYPER", "4806", "1001");
      var ukjentAvvikEntity = initHttpEntity(avvikshendelse, new CustomHeader(X_ENHET_HEADER, "1234"));
      var url = initEndpointUrl("/journal/BID-1/avvik");
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.POST, ukjentAvvikEntity, OpprettAvvikshendelseResponse.class);

      assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private ParameterizedTypeReference<List<AvvikType>> responseTypeErListeMedAvvikType() {
      return new ParameterizedTypeReference<>() {
      };
    }
  }

  @Nested
  @DisplayName("Journalstatus er mottaksregistrert")
  class MottaksregistrertJournalstatus {

    private static final String PATH_JOURNALPOST_UTEN_SAK = "/journal/";

    @Test
    @DisplayName("skal få httpstatus 400 (BAD_REQUEST) når man henter journalpost uten gyldig prefix på journalpost id")
    void skalFaBadRequestVedFeilPrefixPaId() {
      var journalpostResponseEntity = httpHeaderTestRestTemplate
          .exchange(PATH_JOURNALPOST_UTEN_SAK + "ugyldig-id", HttpMethod.GET, null, JournalpostResponse.class);

      assertThat(journalpostResponseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal hente journalpost uten sakstilknytning")
    void skalHenteJournalpostUtenSakstilknytning() {
      when(restTemplateProviderMock.provideRestTemplate(anyString(), anyString())).thenReturn(restTemplateMock);
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JournalpostResponse.class)))
          .thenReturn(new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT));

      httpHeaderTestRestTemplate.exchange(PATH_JOURNALPOST_UTEN_SAK + "BID-1", HttpMethod.GET, null, JournalpostResponse.class);

      verify(restTemplateMock).exchange(PATH_JOURNALPOST_UTEN_SAK + "BID-1", HttpMethod.GET, null, JournalpostResponse.class);
    }

    @Test
    @DisplayName("skal få httpstatus 400 (BAD_REQUST) når man skal finne avvik på journalpost uten gyldig prefix på id")
    void skalFaBadRequestVedFinnAvvikForJournalpostMedUgyldigPrefixPaId() {
      var journalpostResponseEntity = httpHeaderTestRestTemplate
          .exchange(PATH_JOURNALPOST_UTEN_SAK + "ugyldig-id/avvik", HttpMethod.GET, null, responseTypeErListeMedAvvikType());

      assertThat(journalpostResponseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal finne avvik på journalpost uten sakstilknytning")
    void skalFinneAvvikPaJournalpostUtenSakstilknytning() {
      when(restTemplateProviderMock.provideRestTemplate(anyString(), anyString())).thenReturn(restTemplateMock);
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JournalpostDto.class)))
          .thenReturn(new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT));

      httpHeaderTestRestTemplate.exchange(PATH_JOURNALPOST_UTEN_SAK + "BID-1/avvik", HttpMethod.GET, null, responseTypeErListeMedAvvikType());

      verify(restTemplateMock).exchange(PATH_JOURNALPOST_UTEN_SAK + "BID-1/avvik", HttpMethod.GET, null, responseTypeErListeMedAvvikType());
    }

    @Test
    @DisplayName("skal opprette avvik")
    void skalOppretteAvvik() {
      when(restTemplateProviderMock.provideRestTemplate(anyString(), anyString())).thenReturn(restTemplateMock);
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(OpprettAvvikshendelseResponse.class)))
          .thenReturn(new ResponseEntity<>(new OpprettAvvikshendelseResponse(AvvikType.ENDRE_FAGOMRADE), HttpStatus.CREATED));

      final var enhetsnummer = "4806";
      final var avvikshendelse = new Avvikshendelse(AvvikType.ENDRE_FAGOMRADE.name(), enhetsnummer);

      var bestillOriginalEntity = initHttpEntity(avvikshendelse, new CustomHeader(X_ENHET_HEADER, "1234"));
      var url = initEndpointUrl("/journal/BID-666/avvik");
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.POST, bestillOriginalEntity, OpprettAvvikshendelseResponse.class);

      assertAll(() -> assertThat(responseEntity.getStatusCode()).as("status").isEqualTo(HttpStatus.CREATED),
          () -> assertThat(responseEntity.getBody()).as("body").isEqualTo(new OpprettAvvikshendelseResponse(AvvikType.ENDRE_FAGOMRADE)),
          () -> verify(restTemplateMock).exchange(eq("/journal/BID-666/avvik"), eq(HttpMethod.POST), any(), eq(OpprettAvvikshendelseResponse.class)));
    }

    private ParameterizedTypeReference<List<AvvikType>> responseTypeErListeMedAvvikType() {
      return new ParameterizedTypeReference<>() {
      };
    }
  }

  @Nested
  @DisplayName("sak journal")
  class SakJournal {

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("skal finne Journalposter for en bidragssak")
    void skalFinneJournalposterForEnBidragssak() {
      when(restTemplateProviderMock.provideRestTemplate(anyString(), anyString())).thenReturn(restTemplateMock);
      when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any()))
          .thenReturn(new ResponseEntity<>(singletonList(new JournalpostDto()), HttpStatus.OK)); // blir kalla en gang i arkiv og en gang i brevlager

      var listeMedJournalposterResponse = httpHeaderTestRestTemplate
          .exchange(lagUrlForFagomradeBid("/sak/1001/journal"), HttpMethod.GET, null, responseTypeErListeMedJournalposter());

      assertThat(optional(listeMedJournalposterResponse)).hasValueSatisfying(
          response -> assertAll(() -> assertThat(response.getStatusCode()).as("status").isEqualTo(HttpStatus.OK),
              () -> assertThat(response.getBody()).as("body").hasSize(2), // skal kalle også kalle bidrag-dokument-arkiv
              () -> verify(restTemplateMock, times(2)) // skal kalle også kalle bidrag-dokument-arkiv
                  .exchange(eq("/sak/1001/journal?fagomrade=BID"), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any())));
    }

    @Test
    @DisplayName("skal få BAD_REQUEST(400) som statuskode når saksnummer ikke er et heltall")
    void skalFaBadRequestNarSaksnummerIkkeErHeltall() {
      var journalposterResponse = httpHeaderTestRestTemplate
          .exchange(lagUrlForFagomradeBid("/sak/xyz/journal"), HttpMethod.GET, null, responseTypeErListeMedJournalposter());

      assertThat(optional(journalposterResponse)).hasValueSatisfying(
          response -> assertAll(() -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
              () -> assertThat(response.getBody()).isNull()));
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("skal hente sakjournal fra bidrag-dokument-arkiv såfremt bidrag-dokument-journalpost")
    void skalHenteSakJournalFraBidragDokumentArkiv() {

      when(restTemplateProviderMock.provideRestTemplate(anyString(), anyString())).thenReturn(restTemplateMock);
      when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any()))
          .thenReturn(new ResponseEntity<>(singletonList(new JournalpostDto()), HttpStatus.OK));

      var listeMedJournalposterResponse = httpHeaderTestRestTemplate
          .exchange(lagUrlForFagomradeBid("/sak/2020001/journal"), HttpMethod.GET, null, responseTypeErListeMedJournalposter());

      assertThat(listeMedJournalposterResponse.getBody()).hasSize(2); //  skal kalle også kalle bidrag-dokument-arkiv
    }

    private String lagUrlForFagomradeBid(String path) {
      return UriComponentsBuilder.fromHttpUrl("http://localhost:" + port + "/bidrag-dokument" + path).queryParam("fagomrade", "BID").toUriString();
    }

    private ParameterizedTypeReference<List<JournalpostDto>> responseTypeErListeMedJournalposter() {
      return new ParameterizedTypeReference<>() {
      };
    }
  }
}
