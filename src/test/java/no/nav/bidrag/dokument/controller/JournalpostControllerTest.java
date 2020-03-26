package no.nav.bidrag.dokument.controller;

import static java.util.Collections.singletonList;
import static no.nav.bidrag.commons.web.EnhetFilter.X_ENHET_HEADER;
import static no.nav.bidrag.dokument.BidragDokumentLocal.SECURE_TEST_PROFILE;
import static no.nav.bidrag.dokument.BidragDokumentLocal.TEST_PROFILE;
import static no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer.createEnhetHeader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import no.nav.bidrag.commons.web.EnhetFilter;
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate;
import no.nav.bidrag.dokument.BidragDokumentLocal;
import no.nav.bidrag.dokument.dto.AktorDto;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.OpprettAvvikshendelseResponse;
import no.nav.bidrag.dokument.dto.RegistrereJournalpostCommand;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest(classes = BidragDokumentLocal.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({TEST_PROFILE, SECURE_TEST_PROFILE})
@DisplayName("JournalpostController")
class JournalpostControllerTest {

  @LocalServerPort
  private int port;
  @MockBean
  private RestTemplate restTemplateMock;
  @Value("${server.servlet.context-path}")
  private String contextPath;
  @Autowired
  private HttpHeaderTestRestTemplate httpHeaderTestRestTemplate;

  @Nested
  @DisplayName("hent journalpost")
  class Hent {

    @Test
    @DisplayName("skal mangle body når journalpost ikke finnes")
    void skalMangleBodyNarJournalpostIkkeFinnes() {
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JournalpostDto.class)))
          .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

      var url = initEndpointUrl("/sak/007/journal/joark-1");
      var journalpostResponseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.GET, null, JournalpostDto.class);

      assertThat(Optional.of(journalpostResponseEntity)).hasValueSatisfying(response -> assertAll(
          () -> assertThat(response.getBody()).isNull(),
          () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT),
          () -> verify(restTemplateMock).exchange(eq("/sak/007/journal/JOARK-1"), eq(HttpMethod.GET), any(), eq(JournalpostDto.class))
      ));
    }

    @Test
    @DisplayName("skal hente Journalpost når den eksisterer")
    void skalHenteJournalpostNarDenEksisterer() {
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JournalpostDto.class)))
          .thenReturn(new ResponseEntity<>(enJournalpostMedInnhold("MIDLERTIDIG"), HttpStatus.I_AM_A_TEAPOT));

      var url = initEndpointUrl("/sak/007/journal/joark-2");
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.GET, null, JournalpostDto.class);

      assertThat(Optional.of(responseEntity)).hasValueSatisfying(
          response -> assertAll(
              () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.I_AM_A_TEAPOT),
              () -> assertThat(response.getBody()).extracting(JournalpostDto::getInnhold).isEqualTo("MIDLERTIDIG"),
              () -> verify(restTemplateMock).exchange(eq("/sak/007/journal/JOARK-2"), eq(HttpMethod.GET), any(), eq(JournalpostDto.class))
          )
      );
    }

    private JournalpostDto enJournalpostMedInnhold(@SuppressWarnings("SameParameterValue") String innhold) {
      JournalpostDto journalpostDto = new JournalpostDto();
      journalpostDto.setInnhold(innhold);

      return journalpostDto;
    }

    @Test
    @DisplayName("skal hente journalpost fra midlertidig brevlager")
    void skalHenteJournalpostFraMidlertidigBrevlager() {
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JournalpostDto.class)))
          .thenReturn(new ResponseEntity<>(enJournalpostFra("Grev Still E. Ben"), HttpStatus.I_AM_A_TEAPOT));

      var url = initEndpointUrl("/sak/007/journal/bid-1");
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.GET, null, JournalpostDto.class);

      assertThat(Optional.of(responseEntity)).hasValueSatisfying(response -> assertAll(
          () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.I_AM_A_TEAPOT),
          () -> assertThat(response.getBody()).extracting(JournalpostDto::getAvsenderNavn).isEqualTo("Grev Still E. Ben"),
          () -> verify(restTemplateMock).exchange("/sak/007/journal/BID-1", HttpMethod.GET, null, JournalpostDto.class)
      ));
    }

    private JournalpostDto enJournalpostFra(@SuppressWarnings("SameParameterValue") String setAvsenderNavn) {
      JournalpostDto jp = new JournalpostDto();
      jp.setAvsenderNavn(setAvsenderNavn);
      jp.setGjelderAktor(new AktorDto("06127412345"));

      return jp;
    }
  }

  @Nested
  @DisplayName("lagre journalpost")
  class Lagre {

    @Test
    @DisplayName("skal få BAD_REQUEST når prefix er ukjent")
    void skalFaBadRequestMedUkjentPrefix() {
      var lagreJournalpostUrl = initEndpointUrl("/sak/69/journal/svada-1");
      var badRequestResponse = httpHeaderTestRestTemplate.exchange(lagreJournalpostUrl, HttpMethod.GET, null, JournalpostDto.class);

      assertThat(badRequestResponse).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal få BAD_REQUEST når journalpostId ikke er et tall")
    void skalFaBadRequestMedJournalpostIdSomIkkeErEtTall() {
      var lagreJournalpostUrl = initEndpointUrl("/sak/69/journal/bid-en");
      var badRequestResponse = httpHeaderTestRestTemplate.exchange(lagreJournalpostUrl, HttpMethod.GET, null, JournalpostDto.class);

      assertThat(badRequestResponse).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal få BAD_REQUEST når prefix er ukjent ved endring av journalpost")
    void skalFaBadRequestMedUkjentPrefixVedEndringAvJournalpost() {
      var lagreJournalpostUrl = initEndpointUrl("/sak/69/journal/svada-en");

      var badRequestResponse = httpHeaderTestRestTemplate.exchange(
          lagreJournalpostUrl, HttpMethod.PUT, new HttpEntity<>(new EndreJournalpostCommand(), createEnhetHeader("4802")), JournalpostDto.class
      );

      assertThat(badRequestResponse).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal endre journalpost")
    void skalEndreJournalpost() {
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(Void.class)))
          .thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));

      var lagreJournalpostUrl = initEndpointUrl("/sak/69/journal/bid-1");
      var endretJournalpostResponse = httpHeaderTestRestTemplate.exchange(
          lagreJournalpostUrl, HttpMethod.PUT, new HttpEntity<>(new EndreJournalpostCommand(), createEnhetHeader("4802")), Void.class
      );

      assertThat(optional(endretJournalpostResponse)).hasValueSatisfying(response -> assertAll(
          () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED),
          () -> verify(restTemplateMock).exchange(eq("/sak/69/journal/bid-1"), eq(HttpMethod.PUT), any(), eq(Void.class))
      ));
    }
  }

  @Nested
  @DisplayName("avvik på journalpost")
  class Avvik {

    @Test
    @DisplayName("skal feile når man henter avvikshendelser uten å prefikse journalpostId med kildesystem")
    void skalFeileVedHentingAvAvvikshendelserForJournalpostNarJournalpostIdIkkeErPrefiksetMedKildesystem() {
      var url = initEndpointUrl("/sak/1001/journal/1/avvik");
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.GET, null, responseTypeErListeMedAvvikType());

      assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal finne avvikshendelser på en journalpost")
    void skalFinneAvvikshendelserForJournalpost() {
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(responseTypeErListeMedAvvikType())))
          .thenReturn(new ResponseEntity<>(List.of(AvvikType.BESTILL_ORIGINAL), HttpStatus.OK));

      var url = initEndpointUrl("/sak/1001/journal/BID-1/avvik");
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.GET, null, responseTypeErListeMedAvvikType());

      assertAll(
          () -> assertThat(responseEntity.getStatusCode()).as("status").isEqualTo(HttpStatus.OK),
          () -> assertThat(responseEntity.getBody()).as("avvik").hasSize(1),
          () -> assertThat(responseEntity.getBody()).as("avvik").contains(AvvikType.BESTILL_ORIGINAL),
          () -> verify(restTemplateMock).exchange(
              eq("/sak/1001/journal/BID-1/avvik"), eq(HttpMethod.GET), any(), eq(responseTypeErListeMedAvvikType())
          )
      );
    }

    @Test
    @DisplayName("skal opprette et avvik på en journalpost")
    void skalOppretteAvvikPaJournalpost() {
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(OpprettAvvikshendelseResponse.class)))
          .thenReturn(new ResponseEntity<>(new OpprettAvvikshendelseResponse(AvvikType.BESTILL_ORIGINAL), HttpStatus.CREATED));

      final var enhetsnummer = "4806";
      final var avvikshendelse = new Avvikshendelse(AvvikType.BESTILL_ORIGINAL.name(), enhetsnummer);

      var bestillOriginalEntity = initHttpEntity(avvikshendelse, new CustomHeader(X_ENHET_HEADER, "1234"));
      var url = initEndpointUrl("/sak/1001/journal/BID-4/avvik");
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.POST, bestillOriginalEntity, OpprettAvvikshendelseResponse.class);

      assertAll(
          () -> assertThat(responseEntity.getStatusCode()).as("status").isEqualTo(HttpStatus.CREATED),
          () -> assertThat(responseEntity.getBody()).as("body").isEqualTo(new OpprettAvvikshendelseResponse(AvvikType.BESTILL_ORIGINAL)),
          () -> verify(restTemplateMock).exchange(
              eq("/sak/1001/journal/BID-4/avvik"), eq(HttpMethod.POST), any(), eq(OpprettAvvikshendelseResponse.class)
          )
      );
    }

    @Ignore
    @Test
    @DisplayName("skal få not implemented uten header value")
    void skalFaBadRequestUtenHeaderValue() {
      final var avvikshendelse = new Avvikshendelse("BESTILL_ORIGINAL", "4806");
      var ukjentAvvikEntity = initHttpEntity(avvikshendelse);
      var url = initEndpointUrl("/sak/1001/journal/BID-1/avvik");
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.POST, ukjentAvvikEntity, OpprettAvvikshendelseResponse.class);

      assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
    }

    @Test
    @DisplayName("skal få bad request når avvikstype ikke kan parses")
    void skalFaBadRequest() {
      final var avvikshendelse = new Avvikshendelse("AVVIK_IKKE_BLANT_KJENTE_AVVIKSTYPER", "4806");
      var ukjentAvvikEntity = initHttpEntity(avvikshendelse, new CustomHeader(EnhetFilter.X_ENHET_HEADER, "1234"));
      var url = initEndpointUrl("/sak/1001/journal/BID-1/avvik");
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.POST, ukjentAvvikEntity, OpprettAvvikshendelseResponse.class);

      assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
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

    private class CustomHeader {

      final String name;
      final String value;

      private CustomHeader(String name, String value) {
        this.name = name;
        this.value = value;
      }
    }
  }

  @DisplayName("Journalstatus er mottaksregistrert")
  @Nested
  class MottaksregistrertJournalstatus {

    private static final String PATH_JOURNALPOST_UTEN_SAK = "/journal/";

    @Test
    @DisplayName("skal få httpstatus 400 (BAD_REQUEST) når man henter journalpost uten gyldig prefix på journalpost id")
    void skalFaBadRequestVedFeilPrefixPaId() {
      var journalpostResponseEntity = httpHeaderTestRestTemplate.exchange(
          PATH_JOURNALPOST_UTEN_SAK + "ugyldig-id", HttpMethod.GET, null, JournalpostDto.class
      );

      assertThat(journalpostResponseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal hente journalpost uten sakstilknytning")
    void skalHenteJournalpostUtenSakstilknytning() {
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JournalpostDto.class)))
          .thenReturn(new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT));

      httpHeaderTestRestTemplate.exchange(PATH_JOURNALPOST_UTEN_SAK + "BID-1", HttpMethod.GET, null, JournalpostDto.class);

      verify(restTemplateMock).exchange(
          PATH_JOURNALPOST_UTEN_SAK + "BID-1", HttpMethod.GET, null, JournalpostDto.class
      );
    }

    @Test
    @DisplayName("skal få httpstatus 400 (BAD_REQUST) når man skal finne avvik på journalpost uten gyldig prefix på id")
    void skalFaBadRequestVedFinnAvvikForJournalpostMedUgyldigPrefixPaId() {
      var journalpostResponseEntity = httpHeaderTestRestTemplate.exchange(
          PATH_JOURNALPOST_UTEN_SAK + "ugyldig-id/avvik", HttpMethod.GET, null, responseTypeErListeMedAvvikType()
      );

      assertThat(journalpostResponseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal finne avvik på journalpost uten sakstilknytning")
    void skalFinneAvvikPaJournalpostUtenSakstilknytning() {
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JournalpostDto.class)))
          .thenReturn(new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT));

      httpHeaderTestRestTemplate.exchange(PATH_JOURNALPOST_UTEN_SAK + "BID-1/avvik", HttpMethod.GET, null, responseTypeErListeMedAvvikType());

      verify(restTemplateMock).exchange(
          PATH_JOURNALPOST_UTEN_SAK + "BID-1/avvik", HttpMethod.GET, null, responseTypeErListeMedAvvikType()
      );
    }

    @Test
    @DisplayName("skal registrere journalpost")
    void skalRegistrereJournalpost() {
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(Void.class)))
          .thenReturn(new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT));

      HttpEntity<RegistrereJournalpostCommand> registrerEntity = new HttpEntity<>(new RegistrereJournalpostCommand(), createEnhetHeader("4802"));
      httpHeaderTestRestTemplate.exchange(PATH_JOURNALPOST_UTEN_SAK + "BID-1", HttpMethod.PUT, registrerEntity, Void.class);

      verify(restTemplateMock).exchange(eq(PATH_JOURNALPOST_UTEN_SAK + "BID-1"), eq(HttpMethod.PUT), any(), eq(Void.class));
    }
  }

  @Nested
  @DisplayName("sak journal")
  class SakJournal {

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("skal finne Journalposter for en bidragssak")
    void skalFinneJournalposterForEnBidragssak() {
      when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any()))
          .thenReturn(new ResponseEntity<>(singletonList(new JournalpostDto()), HttpStatus.OK)); // blir kalla en gang i arkiv og en gang i brevlager

      var listeMedJournalposterResponse = httpHeaderTestRestTemplate.exchange(
          lagSakjournalUrlForFagomradeBid("1001"), HttpMethod.GET, null, responseTypeErListeMedJournalposter()
      );

      assertThat(optional(listeMedJournalposterResponse)).hasValueSatisfying(
          response -> assertAll(
              () -> assertThat(response.getStatusCode()).as("status").isEqualTo(HttpStatus.OK),
              // () -> assertThat(response.getBody()).as("body").hasSize(2), skal kalle også kalle bidrag-dokument-arkiv
              () -> assertThat(response.getBody()).as("body").hasSize(1),
              // () -> verify(restTemplateMock, times(2)) skal kalle også kalle bidrag-dokument-arkiv
              () -> verify(restTemplateMock)
                  .exchange(eq("/sak/1001/journal?fagomrade=BID"), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any())
          )
      );
    }

    @Test
    @DisplayName("skal få BAD_REQUEST(400) som statuskode når saksnummer ikke er et heltall")
    void skalFaBadRequestNarSaksnummerIkkeErHeltall() {
      var journalposterResponse = httpHeaderTestRestTemplate.exchange(
          lagSakjournalUrlForFagomradeBid("xyz"), HttpMethod.GET, null, responseTypeErListeMedJournalposter()
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

      var listeMedJournalposterResponse = httpHeaderTestRestTemplate.exchange(
          lagSakjournalUrlForFagomradeBid("2020001"), HttpMethod.GET, null, responseTypeErListeMedJournalposter()
      );

//    assertThat(listeMedJournalposterResponse.getBody()).hasSize(2); skal kalle også kalle bidrag-dokument-arkiv
      assertThat(listeMedJournalposterResponse.getBody()).hasSize(1);
    }

    private String lagSakjournalUrlForFagomradeBid(String saksnummer) {
      return UriComponentsBuilder
          .fromHttpUrl("http://localhost:" + port + "/bidrag-dokument/sak/" + saksnummer + "/journal")
          .queryParam("fagomrade", "BID")
          .toUriString();
    }

    private ParameterizedTypeReference<List<JournalpostDto>> responseTypeErListeMedJournalposter() {
      return new ParameterizedTypeReference<>() {
      };
    }
  }

  @AfterEach
  void resetMocking() {
    Mockito.reset(restTemplateMock);
  }

  private ParameterizedTypeReference<List<AvvikType>> responseTypeErListeMedAvvikType() {
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
