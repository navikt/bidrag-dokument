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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import no.nav.bidrag.commons.web.EnhetFilter;
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate;
import no.nav.bidrag.dokument.BidragDokumentLocal;
import no.nav.bidrag.dokument.JournalpostDtoBygger;
import no.nav.bidrag.dokument.dto.AktorDto;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.DokumentDto;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand;
import no.nav.bidrag.dokument.dto.JournalpostDto;
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
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

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
  private HttpHeaderTestRestTemplate securedTestRestTemplate;

  @Nested
  @DisplayName("hent journalpost")
  class Hent {

    @Test
    @DisplayName("skal mangle body når journalpost ikke finnes")
    void skalMangleBodyNarJournalpostIkkeFinnes() {
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JournalpostDto.class)))
          .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

      var url = initEndpointUrl("/sak/007/journal/joark-1");
      var journalpostResponseEntity = securedTestRestTemplate.exchange(url, HttpMethod.GET, null, JournalpostDto.class);

      assertThat(Optional.of(journalpostResponseEntity)).hasValueSatisfying(response -> assertAll(
          () -> assertThat(response.getBody()).isNull(),
          () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT),
          () -> verify(restTemplateMock).exchange(eq("/sak/007/journal/joark-1"), eq(HttpMethod.GET), any(), eq(JournalpostDto.class))
      ));
    }

    @Test
    @DisplayName("skal hente Journalpost når den eksisterer")
    void skalHenteJournalpostNarDenEksisterer() {
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JournalpostDto.class)))
          .thenReturn(new ResponseEntity<>(enJournalpostMedInnhold("MIDLERTIDIG"), HttpStatus.I_AM_A_TEAPOT));

      var url = initEndpointUrl("/sak/007/journal/joark-2");
      var responseEntity = securedTestRestTemplate.exchange(url, HttpMethod.GET, null, JournalpostDto.class);

      verify(restTemplateMock, atLeastOnce()).exchange("/sak/007/journal/joark-2", HttpMethod.GET, null, JournalpostDto.class);

      assertThat(Optional.of(responseEntity)).hasValueSatisfying(
          response -> assertAll(
              () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.I_AM_A_TEAPOT),
              () -> assertThat(response.getBody()).extracting(JournalpostDto::getInnhold).isEqualTo("MIDLERTIDIG"),
              () -> verify(restTemplateMock).exchange(eq("/sak/007/journal/joark-2"), eq(HttpMethod.GET), any(), eq(JournalpostDto.class))
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
      var responseEntity = securedTestRestTemplate.exchange(url, HttpMethod.GET, null, JournalpostDto.class);

      assertThat(Optional.of(responseEntity)).hasValueSatisfying(response -> assertAll(
          () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.I_AM_A_TEAPOT),
          () -> assertThat(response.getBody()).extracting(JournalpostDto::getAvsenderNavn).isEqualTo("Grev Still E. Ben"),
          () -> verify(restTemplateMock).exchange("/sak/007/journal/bid-1", HttpMethod.GET, null, JournalpostDto.class)
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
      var badRequestResponse = securedTestRestTemplate.exchange(lagreJournalpostUrl, HttpMethod.GET, null, JournalpostDto.class);

      assertThat(badRequestResponse).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal få BAD_REQUEST når journalpostId ikke er et tall")
    void skalFaBadRequestMedJournalpostIdSomIkkeErEtTall() {
      var lagreJournalpostUrl = initEndpointUrl("/sak/69/journal/bid-en");
      var badRequestResponse = securedTestRestTemplate.exchange(lagreJournalpostUrl, HttpMethod.GET, null, JournalpostDto.class);

      assertThat(badRequestResponse).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal få BAD_REQUEST når prefix er ukjent ved endring av journalpost")
    void skalFaBadRequestMedUkjentPrefixVedEndringAvJournalpost() {
      var lagreJournalpostUrl = initEndpointUrl("/sak/69/journal/svada-en");
      var badRequestResponse = securedTestRestTemplate.exchange(
          lagreJournalpostUrl, HttpMethod.PUT, new HttpEntity<>(new EndreJournalpostCommand()), JournalpostDto.class
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

      var lagreJournalpostUrl = initEndpointUrl("/sak/69/journal/bid-1");
      var endretJournalpostResponse = securedTestRestTemplate.exchange(
          lagreJournalpostUrl, HttpMethod.PUT, new HttpEntity<>(new EndreJournalpostCommand()), JournalpostDto.class
      );

      assertThat(optional(endretJournalpostResponse)).hasValueSatisfying(response -> assertAll(
          () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED),
          () -> assertThat(response.getBody()).extracting(JournalpostDto::getJournalpostId).isEqualTo("BID-101"),
          () -> verify(restTemplateMock).exchange(eq("/sak/69/journal/bid-1"), eq(HttpMethod.PUT), any(), eq(JournalpostDto.class))
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
      var responseEntity = securedTestRestTemplate.exchange(url, HttpMethod.GET, null, responseTypeErListeMedAvvikType());

      assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal finne avvikshendelser på en journalpost")
    void skalFinneAvvikshendelserForJournalpost() {
      when(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(responseTypeErListeMedAvvikType())))
          .thenReturn(new ResponseEntity<>(List.of(AvvikType.BESTILL_ORIGINAL), HttpStatus.OK));

      var url = initEndpointUrl("/sak/1001/journal/BID-1/avvik");
      var responseEntity = securedTestRestTemplate.exchange(url, HttpMethod.GET, null, responseTypeErListeMedAvvikType());

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

      var bestillOriginalEntity = initHttpEntity(avvikshendelse, new CustomHeader(EnhetFilter.X_ENHETSNR_HEADER, "1234"));
      var url = initEndpointUrl("/sak/1001/journal/BID-4/avvik");
      var responseEntity = securedTestRestTemplate.exchange(url, HttpMethod.POST, bestillOriginalEntity, OpprettAvvikshendelseResponse.class);

      assertAll(
          () -> assertThat(responseEntity.getStatusCode()).as("status").isEqualTo(HttpStatus.CREATED),
          () -> assertThat(responseEntity.getBody()).as("body").isEqualTo(new OpprettAvvikshendelseResponse(AvvikType.BESTILL_ORIGINAL)),
          () -> verify(restTemplateMock).exchange(
              eq("/sak/1001/journal/BID-4/avvik"), eq(HttpMethod.POST), any(), eq(OpprettAvvikshendelseResponse.class)
          )
      );
    }

    @Test
    @DisplayName("skal få bad request uten header value")
    void skalFaBadRequestUtenHeaderValue() {
      final var avvikshendelse = new Avvikshendelse("BESTILL_ORIGINAL", "4806");
      var ukjentAvvikEntity = initHttpEntity(avvikshendelse);
      var url = initEndpointUrl("/sak/1001/journal/BID-1/avvik");
      var responseEntity = securedTestRestTemplate.exchange(url, HttpMethod.POST, ukjentAvvikEntity, OpprettAvvikshendelseResponse.class);

      assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal få bad request når avvikstype ikke kan parses")
    void skalFaBadRequest() {
      final var avvikshendelse = new Avvikshendelse("AVVIK_IKKE_BLANT_KJENTE_AVVIKSTYPER", "4806");
      var ukjentAvvikEntity = initHttpEntity(avvikshendelse, new CustomHeader(EnhetFilter.X_ENHETSNR_HEADER, "1234"));
      var url = initEndpointUrl("/sak/1001/journal/BID-1/avvik");
      var responseEntity = securedTestRestTemplate.exchange(url, HttpMethod.POST, ukjentAvvikEntity, OpprettAvvikshendelseResponse.class);

      assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @AfterEach
    void resetMocking() {
      Mockito.reset(restTemplateMock);
    }

    private ParameterizedTypeReference<List<AvvikType>> responseTypeErListeMedAvvikType() {
      return new ParameterizedTypeReference<>() {
      };
    }

    private <T> HttpEntity<T> initHttpEntity(T body,CustomHeader ... customHeaders) {
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

  private <T> Optional<ResponseEntity<T>> optional(ResponseEntity<T> responseEntity) {
    return Optional.ofNullable(responseEntity);
  }

  private String initEndpointUrl(String endpoint) {
    return "http://localhost:" + port + contextPath + endpoint;
  }
}
