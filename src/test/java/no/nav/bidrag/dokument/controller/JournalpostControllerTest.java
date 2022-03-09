package no.nav.bidrag.dokument.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.resetAllScenarios;
import static com.github.tomakehurst.wiremock.client.WireMock.resetToDefault;
import static no.nav.bidrag.commons.web.EnhetFilter.X_ENHET_HEADER;
import static no.nav.bidrag.dokument.BidragDokumentLocal.TEST_PROFILE;
import static no.nav.bidrag.dokument.consumer.BidragDokumentConsumer.PATH_AVVIK_PA_JOURNALPOST;
import static no.nav.bidrag.dokument.consumer.BidragDokumentConsumer.PATH_JOURNALPOST_UTEN_SAK;
import static no.nav.bidrag.dokument.consumer.BidragDokumentConsumer.PATH_SAK_JOURNAL;
import static no.nav.bidrag.dokument.consumer.BidragDokumentConsumer.createEnhetHeader;
import static no.nav.bidrag.dokument.consumer.stub.RestConsumerStub.generereJournalpostrespons;
import static no.nav.bidrag.dokument.consumer.stub.RestConsumerStub.lesResponsfilSomStreng;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate;
import no.nav.bidrag.dokument.BidragDokumentLocal;
import no.nav.bidrag.dokument.consumer.stub.RestConsumerStub;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.BehandleAvvikshendelseResponse;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.JournalpostResponse;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest(classes = BidragDokumentLocal.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles(TEST_PROFILE)
@DisplayName("JournalpostController")
@EnableMockOAuth2Server
class JournalpostControllerTest {

  @LocalServerPort
  private int localServerPort;

  @Value("${server.servlet.context-path}")
  private String contextPath;

  @Autowired
  private HttpHeaderTestRestTemplate httpHeaderTestRestTemplate;

  @Autowired
  private RestConsumerStub restConsumerStub;

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
    return "http://localhost:" + localServerPort + contextPath + endpoint;
  }

  private record CustomHeader(String name, String value) {

  }
  @BeforeEach
  void cleanup(){
    resetToDefault();
    resetAllScenarios();
    reset();
  }

  @Nested
  @DisplayName("hent journalpost")
  class Hent {

    @Test
    @DisplayName("skal mangle body når journalpost ikke finnes")
    void skalMangleBodyNarJournalpostIkkeFinnes() {

      var jpId = "JOARK-1";
      var saksnr = "007";
      var queryParams = new HashMap<String, StringValuePattern>();
      queryParams.put("saksnummer", equalTo(saksnr));

      restConsumerStub.runHenteJournalpostArkiv(jpId, queryParams, HttpStatus.NO_CONTENT, "");

      var journalpostResponseEntity = httpHeaderTestRestTemplate
          .exchange(initEndpointUrl(String.format(PATH_JOURNALPOST_UTEN_SAK, jpId) + "?saksnummer=" + saksnr), HttpMethod.GET, null,
              JournalpostResponse.class);

      assertThat(Optional.of(journalpostResponseEntity)).hasValueSatisfying(response -> assertAll(() -> assertThat(response.getBody()).isNull(),
          () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT)));
    }

    @Test
    @DisplayName("skal hente Journalpost når den eksisterer")
    void skalHenteJournalpostNarDenEksisterer() throws IOException {

      var jpId = "JOARK-2";
      var saksnr = "007";
      var queryParams = new HashMap<String, StringValuePattern>();
      queryParams.put("saksnummer", equalTo(saksnr));
      var responsfilnavn = "journalpostInnholdMidlertidig.json";

      restConsumerStub.runHenteJournalpostArkiv(jpId, queryParams, HttpStatus.OK, lesResponsfilSomStreng(responsfilnavn));

      var url = initEndpointUrl("/journal/joark-2?saksnummer=007");
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.GET, null, JournalpostResponse.class);

      assertThat(Optional.of(responseEntity)).hasValueSatisfying(
          response -> assertAll(() -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
              () -> assertThat(response.getBody()).extracting(JournalpostResponse::getJournalpost).extracting(JournalpostDto::getInnhold)
                  .isEqualTo("MIDLERTIDIG")));
    }

    @Test
    @DisplayName("Skal gi status 401 dersom token mangler")
    void skalGiStatus401DersomTokenMangler() {

      var testRestTemplate = new TestRestTemplate();

      var url = initEndpointUrl("/journal/joark-2?saksnummer=007");
      var responseEntity = testRestTemplate.exchange(url, HttpMethod.GET, null, String.class);

      assertEquals(responseEntity.getStatusCode(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("skal hente journalpost fra midlertidig brevlager")
    void skalHenteJournalpostFraMidlertidigBrevlager() {

      var jpId = "BID-1";
      var saksnr = "007";
      var queryParams = new HashMap<String, StringValuePattern>();
      queryParams.put("saksnummer", equalTo(saksnr));
      Map<String, String> journalpostelementer = new HashMap<>();
      journalpostelementer.put("avsenderNavn", "Grev Still E. Ben");

      restConsumerStub.runHenteJournalpost(jpId, queryParams, HttpStatus.OK, generereJournalpostrespons(journalpostelementer));

      var url = initEndpointUrl("/journal/" + jpId + "?saksnummer=" + saksnr);
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.GET, null, JournalpostResponse.class);

      assertThat(Optional.of(responseEntity)).hasValueSatisfying(
          response -> assertAll(() -> assertThat(response.getStatusCode()).as("status code").isEqualTo(HttpStatus.OK),
              () -> assertThat(response.getBody()).as("JournalpostResponse").extracting(JournalpostResponse::getJournalpost).as("journalpost")
                  .extracting(JournalpostDto::getAvsenderNavn).isEqualTo("Grev Still E. Ben")));
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
      var badRequestResponse = httpHeaderTestRestTemplate.exchange(lagreJournalpostUrl, HttpMethod.PATCH, entityMedEnhetsheader, Void.class);

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
          .exchange(lagreJournalpostUrl, HttpMethod.PATCH, new HttpEntity<>(new EndreJournalpostCommand(), createEnhetHeader("4802")),
              JournalpostResponse.class);

      assertThat(badRequestResponse).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Disabled("Henger i ett minutt før fullførelse")
    @Test
    @DisplayName("skal endre journalpost")
    void skalEndreJournalpost() throws IOException {
      var jpId = "BID-1";
      restConsumerStub.runEndreJournalpost(jpId, HttpStatus.ACCEPTED);

      var lagreJournalpostUrl = initEndpointUrl("/journal/BID-1");
      var endretJournalpostResponse = httpHeaderTestRestTemplate
          .exchange(lagreJournalpostUrl, HttpMethod.PATCH, new HttpEntity<>(new EndreJournalpostCommand(), createEnhetHeader("4802")), Void.class);

      assertThat(optional(endretJournalpostResponse))
          .hasValueSatisfying(response -> assertAll(() -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED)));
    }

    @Disabled("Henger i ett minutt før fullførelse")
    @Test
    @DisplayName("skal videresende headere når journalpost endres")
    void skalVideresendeHeadereVedEndreJournalpost() {
      var jpId = "BID-1";
      restConsumerStub.runEndreJournalpostMedHeader(jpId, HttpHeader.httpHeader(HttpHeaders.WARNING, "rofl"), HttpStatus.OK, "");

      var lagreJournalpostUrl = initEndpointUrl("/journal/BID-1");
      var endretJournalpostResponse = httpHeaderTestRestTemplate
          .exchange(lagreJournalpostUrl, HttpMethod.PATCH, new HttpEntity<>(new EndreJournalpostCommand(), createEnhetHeader("4802")), Void.class);

      assertThat(optional(endretJournalpostResponse))
          .hasValueSatisfying(response -> assertAll(() -> assertThat(response.getStatusCode()).as("status").isEqualTo(HttpStatus.OK), () -> {
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
      final var jpId = "BID-1";
      final var saksnr = "1001";
      final var path = String.format(PATH_AVVIK_PA_JOURNALPOST, jpId);
      var queryParams = new HashMap<String, StringValuePattern>();
      queryParams.put("saksnummer", equalTo(saksnr));
      final var respons = String.join("\n", " [", "\"BESTILL_ORIGINAL\"", "]");

      restConsumerStub.runGet(path, queryParams, HttpStatus.OK, respons);

      var url = initEndpointUrl(path + "?saksnummer=1001");

      // when
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.GET, null, responseTypeErListeMedAvvikType());

      assertAll(() -> assertThat(responseEntity.getStatusCode()).as("status").isEqualTo(HttpStatus.OK),
          () -> assertThat(responseEntity.getBody()).as("avvik").hasSize(1),
          () -> assertThat(responseEntity.getBody()).as("avvik").contains(AvvikType.BESTILL_ORIGINAL));
    }

    @Test
    @DisplayName("skal finne avvikshendelser på en joark journalpost")
    void skalFinneAvvikshendelserForJoarkJournalpost() {
      final var jpId = "JOARK-1";
      final var saksnr = "1001";
      final var path = String.format(PATH_AVVIK_PA_JOURNALPOST, jpId);
      var queryParams = new HashMap<String, StringValuePattern>();
      queryParams.put("saksnummer", equalTo(saksnr));
      final var respons = String.join("\n", " [", "\"BESTILL_ORIGINAL\"", "]");

      restConsumerStub.runGetArkiv(path, queryParams, HttpStatus.OK, respons);

      var url = initEndpointUrl(path + "?saksnummer=1001");

      // when
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.GET, null, responseTypeErListeMedAvvikType());

      assertAll(() -> assertThat(responseEntity.getStatusCode()).as("status").isEqualTo(HttpStatus.OK),
          () -> assertThat(responseEntity.getBody()).as("avvik").hasSize(1),
          () -> assertThat(responseEntity.getBody()).as("avvik").contains(AvvikType.BESTILL_ORIGINAL));
    }

    @Test
    @DisplayName("skal opprette et avvik på en journalpost")
    void skalOppretteAvvikPaJournalpost() throws InterruptedException {

      // given
      final var jpId = "BID-4";
      final var enhetsnummer = "4806";
      final var avvikshendelse = new Avvikshendelse(AvvikType.BESTILL_ORIGINAL.name(), enhetsnummer);
      final var path = String.format(PATH_AVVIK_PA_JOURNALPOST, jpId);
      final var respons = String.join("\n", " {", "\"avvikType\":", "\"" + avvikshendelse.getAvvikType() + "\"", "}");

      restConsumerStub.runPost(path, HttpStatus.CREATED, respons);

      var bestillOriginalEntity = initHttpEntity(avvikshendelse, new CustomHeader(X_ENHET_HEADER, "1234"));
      var url = initEndpointUrl(path);

      // when
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.POST, bestillOriginalEntity, BehandleAvvikshendelseResponse.class);

      // then
      assertAll(
          () -> assertThat(responseEntity.getStatusCode()).as("status").isEqualTo(HttpStatus.CREATED),
          () -> assertThat(responseEntity.getBody()).as("body").isEqualTo(new BehandleAvvikshendelseResponse(AvvikType.BESTILL_ORIGINAL)));
    }

    @Test
    @DisplayName("skal opprette et avvik på en Joark journalpost")
    void skalOppretteAvvikPaJoarkJournalpost() {

      // given
      final var jpId = "JOARK-4";
      final var enhetsnummer = "4806";
      final var avvikshendelse = new Avvikshendelse(AvvikType.BESTILL_ORIGINAL.name(), enhetsnummer);
      final var path = String.format(PATH_AVVIK_PA_JOURNALPOST, jpId);
      final var respons = String.join("\n", " {", "\"avvikType\":", "\"" + avvikshendelse.getAvvikType() + "\"", "}");

      restConsumerStub.runPostArkiv(path, HttpStatus.CREATED, respons);

      var bestillOriginalEntity = initHttpEntity(avvikshendelse, new CustomHeader(X_ENHET_HEADER, "1234"));
      var url = initEndpointUrl(path);

      // when
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.POST, bestillOriginalEntity, BehandleAvvikshendelseResponse.class);

      // then
      assertAll(
          () -> assertThat(responseEntity.getStatusCode()).as("status").isEqualTo(HttpStatus.CREATED),
          () -> assertThat(responseEntity.getBody()).as("body").isEqualTo(new BehandleAvvikshendelseResponse(AvvikType.BESTILL_ORIGINAL)));
    }

    @Test
    @DisplayName("skal få bad request uten header value")
    void skalFaBadRequestUtenHeaderValue() {
      final var avvikshendelse = new Avvikshendelse("BESTILL_ORIGINAL", "4806", "1001");
      var ukjentAvvikEntity = initHttpEntity(avvikshendelse);
      var url = initEndpointUrl("/journal/BID-1/avvik");
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.POST, ukjentAvvikEntity, BehandleAvvikshendelseResponse.class);

      assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal få bad request når avvikstype ikke kan parses")
    void skalFaBadRequest() {
      final var avvikshendelse = new Avvikshendelse("AVVIK_IKKE_BLANT_KJENTE_AVVIKSTYPER", "4806", "1001");
      var ukjentAvvikEntity = initHttpEntity(avvikshendelse, new CustomHeader(X_ENHET_HEADER, "1234"));
      var url = initEndpointUrl("/journal/BID-1/avvik");
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.POST, ukjentAvvikEntity, BehandleAvvikshendelseResponse.class);

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

      // given
      var jpId = "BID-1";
      Map<String, String> journalpostelementer = new HashMap<>();
      journalpostelementer.put("avsenderNavn", "Grev Still E. Ben");
      restConsumerStub
          .runGet(String.format(PATH_JOURNALPOST_UTEN_SAK + "%s", jpId), HttpStatus.OK, generereJournalpostrespons(journalpostelementer));

      // when
      var respons = httpHeaderTestRestTemplate.exchange(PATH_JOURNALPOST_UTEN_SAK + jpId, HttpMethod.GET, null, JournalpostResponse.class);

      // then
      assertTrue(respons.getStatusCode().is2xxSuccessful());
    }

    @Test
    @DisplayName("skal få httpstatus 400 (BAD_REQUST) når man skal finne avvik på journalpost uten gyldig prefix på id")
    void skalFaBadRequestVedFinnAvvikForJournalpostMedUgyldigPrefixPaId() {

      // given, when
      var journalpostResponseEntity = httpHeaderTestRestTemplate
          .exchange(PATH_JOURNALPOST_UTEN_SAK + "ugyldig-id/avvik", HttpMethod.GET, null, responseTypeErListeMedAvvikType());

      // then
      assertThat(journalpostResponseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("skal finne avvik på journalpost uten sakstilknytning")
    void skalFinneAvvikPaJournalpostUtenSakstilknytning() {

      // given
      var jpId = "BID-1";
      restConsumerStub
          .runGet(String.format(PATH_AVVIK_PA_JOURNALPOST, jpId), HttpStatus.OK, String.join("\n", " [", " \"ARKIVERE_JOURNALPOST\"]"));

      // when
      var respons = httpHeaderTestRestTemplate
          .exchange(PATH_JOURNALPOST_UTEN_SAK + "BID-1/avvik", HttpMethod.GET, null, responseTypeErListeMedAvvikType());

      // then
      assertTrue(respons.getStatusCode().is2xxSuccessful());
    }

    @Test
    @DisplayName("skal opprette avvik")
    @Disabled // feilet når RequestFactory fra apache ble lagt til i RestTemplate grunnet endring av PUT -> PATCH... ???
    void skalOppretteAvvik() {
      // given
      final var jpId = "BID-666";
      final var enhetsnummer = "4806";
      final var avvikshendelse = new Avvikshendelse(AvvikType.ENDRE_FAGOMRADE.name(), enhetsnummer);
      final var path = String.format(PATH_AVVIK_PA_JOURNALPOST, jpId);
      final var respons ="na";

      restConsumerStub.runPost(path, HttpStatus.CREATED, respons);

      var bestillOriginalEntity = initHttpEntity(avvikshendelse, new CustomHeader(X_ENHET_HEADER, "1234"));
      var url = initEndpointUrl(path);

      // when
      var responseEntity = httpHeaderTestRestTemplate.exchange(url, HttpMethod.POST, bestillOriginalEntity, BehandleAvvikshendelseResponse.class);

      // then
      assertAll(() -> assertThat(responseEntity.getStatusCode()).as("status").isEqualTo(HttpStatus.CREATED),
          () -> assertThat(responseEntity.getBody()).as("body").isEqualTo(new BehandleAvvikshendelseResponse(AvvikType.ENDRE_FAGOMRADE)));
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
    @DisplayName("skal finne Journalposter for en bidragssak")
    void skalFinneJournalposterForEnBidragssak() throws IOException {

      // given
      final var saksnr = "1001";
      final var path = String.format(PATH_SAK_JOURNAL, saksnr);
      final var navnResponsfil = "bdj-respons.json";

      // Bruker en fellestub for journalpost og arkiv pga identisk path. Kan evnt sette opp egne
      // WireMock-instanser for hver app, men det krever mer arbeid.
      restConsumerStub.runGetArkiv(path, HttpStatus.OK, lesResponsfilSomStreng(navnResponsfil));
      restConsumerStub.runGet(path, HttpStatus.OK, lesResponsfilSomStreng(navnResponsfil));

      // when
      var listeMedJournalposterResponse = httpHeaderTestRestTemplate
          .exchange(lagUrlForFagomradeBid(path), HttpMethod.GET, null, responseTypeErListeMedJournalposter());

      // then
      assertThat(optional(listeMedJournalposterResponse))
          .hasValueSatisfying(response -> assertAll(() -> assertThat(response.getStatusCode()).as("status").isEqualTo(HttpStatus.OK),
              // henter to journalposter fra journalpost og to fra arkiv (samme respons)
              () -> assertThat(response.getBody()).as("body").hasSize(4)));
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
    @DisplayName("skal hente sakjournal fra bidrag-dokument-arkiv såfremt bidrag-dokument-journalpost")
    void skalHenteSakJournalFraBidragDokumentArkiv() throws IOException {

      // given
      final var saksnr = "2020001";
      final var path = String.format(PATH_SAK_JOURNAL, saksnr);
      final var navnResponsfil = "bdj-respons.json";

      // Bruker en fellestub for journalpost og arkiv pga identisk path. Kan evnt sette opp egne
      // WireMock-instanser for hver app, men det krever mer arbeid.
      restConsumerStub.runGetArkiv(path, HttpStatus.OK, lesResponsfilSomStreng(navnResponsfil));
      restConsumerStub.runGet(path, HttpStatus.OK, lesResponsfilSomStreng(navnResponsfil));

      var listeMedJournalposterResponse = httpHeaderTestRestTemplate
          .exchange(lagUrlForFagomradeBid(path), HttpMethod.GET, null, responseTypeErListeMedJournalposter());

      assertThat(listeMedJournalposterResponse.getBody()).hasSize(4); //  skal kalle også kalle bidrag-dokument-arkiv
    }

    private String lagUrlForFagomradeBid(String path) {
      return UriComponentsBuilder.fromHttpUrl("http://localhost:" + localServerPort + "/bidrag-dokument" + path).queryParam("fagomrade", "BID")
          .toUriString();
    }

    private ParameterizedTypeReference<List<JournalpostDto>> responseTypeErListeMedJournalposter() {
      return new ParameterizedTypeReference<>() {
      };
    }
  }
}
