package no.nav.bidrag.dokument.controller;

import no.nav.bidrag.dokument.JournalpostDtoBygger;
import no.nav.bidrag.dokument.consumer.RestTemplateFactory;
import no.nav.bidrag.dokument.dto.DokumentDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.bisys.BidragJournalpostDto;
import no.nav.bidrag.dokument.dto.joark.JournalforingDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("JournalpostController")
class JournalpostControllerTest {

    private static final String ENDPOINT_JOURNALPOST = "/journalpost";
    private static final String ENDPOINT_JOURNALPOST_HENT = "/journalpost/hent";

    @LocalServerPort private int port;
    @Mock private RestTemplate restTemplateMock;
    @Value("${server.servlet.context-path}") private String contextPath;
    @Autowired private TestRestTemplate testRestTemplate;

    @BeforeEach void mockRestTemplateFactory() {
        MockitoAnnotations.initMocks(this);
        RestTemplateFactory.use(() -> restTemplateMock);
    }

    @DisplayName("endpoint: " + ENDPOINT_JOURNALPOST_HENT)
    @Nested class EndpointHentJournalpost {

        private String url = initEndpointUrl(ENDPOINT_JOURNALPOST_HENT);

        @DisplayName("skal ha body som null når enkel journalpost ikke finnes")
        @Test void skalGiBodySomNullNarJournalpostIkkeFinnes() {
            when(restTemplateMock.getForEntity(eq("1"), eq(JournalforingDto.class))).thenReturn(new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT));

            ResponseEntity<JournalpostDto> journalpostResponseEntity = testRestTemplate.getForEntity(url + "/joark-1", JournalpostDto.class);

            verify(restTemplateMock).getForEntity(eq("1"), eq(JournalforingDto.class));

            assertThat(Optional.of(journalpostResponseEntity)).hasValueSatisfying(response -> assertAll(
                    () -> assertThat(response.getBody()).isNull(),
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT)
            ));
        }

        @DisplayName("skal hente Journalpost når den eksisterer")
        @Test void skalHenteJournalpostNarDenEksisterer() {
            when(restTemplateMock.getForEntity(eq("1"), eq(JournalforingDto.class))).thenReturn(new ResponseEntity<>(
                    enJournalforingMedTilstand("MIDLERTIDIG"), HttpStatus.I_AM_A_TEAPOT
            ));

            ResponseEntity<JournalpostDto> responseEntity = testRestTemplate.getForEntity(url + "/joark-1", JournalpostDto.class);

            verify(restTemplateMock).getForEntity(eq("1"), eq(JournalforingDto.class));

            assertThat(Optional.of(responseEntity)).hasValueSatisfying(response -> assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).extracting(JournalpostDto::getHello).isEqualTo("hello from bidrag-dokument"),
                    () -> assertThat(response.getBody()).extracting(JournalpostDto::getJournaltilstand).isEqualTo("MIDLERTIDIG")
            ));
        }

        private JournalforingDto enJournalforingMedTilstand(@SuppressWarnings("SameParameterValue") String journaltilstand) {
            JournalforingDto journalforingDto = new JournalforingDto();
            journalforingDto.setJournalTilstand(journaltilstand);

            return journalforingDto;
        }

        @DisplayName("skal hente journalpost fra midlertidig brevlager")
        @Test void skalHenteJournalpostFraMidlertidigBrevlager() {
            when(restTemplateMock.getForEntity(eq("/journalpost/1"), eq(BidragJournalpostDto.class))).thenReturn(new ResponseEntity<>(
                    enJournalpostFra("Grev Still E. Ben"), HttpStatus.I_AM_A_TEAPOT
            ));

            ResponseEntity<JournalpostDto> responseEntity = testRestTemplate.getForEntity(url + "/bid-1", JournalpostDto.class);

            verify(restTemplateMock).getForEntity(eq("/journalpost/1"), eq(BidragJournalpostDto.class));

            assertThat(Optional.of(responseEntity)).hasValueSatisfying(response -> assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).extracting(JournalpostDto::getHello).isEqualTo("hello from bidrag-dokument"),
                    () -> assertThat(response.getBody()).extracting(JournalpostDto::getAvsenderNavn).isEqualTo("Grev Still E. Ben")
            ));
        }

        private BidragJournalpostDto enJournalpostFra(@SuppressWarnings("SameParameterValue") String avsender) {
            BidragJournalpostDto jp = new BidragJournalpostDto();
            jp.setAvsender(avsender);

            return jp;
        }
    }

    @DisplayName("endpoint: " + ENDPOINT_JOURNALPOST)
    @Nested class EndpointJournalpost {

        private String url = initEndpointUrl(ENDPOINT_JOURNALPOST);

        @DisplayName("skal finne Journalposter for en bidragssak") @SuppressWarnings("unchecked")
        @Test void skalFinneJournalposterForEnBidragssak() {
            when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<BidragJournalpostDto>>) any()))
                    .thenReturn(new ResponseEntity<>(asList(new BidragJournalpostDto(), new BidragJournalpostDto()), HttpStatus.I_AM_A_TEAPOT));

            ResponseEntity<List<JournalpostDto>> responseEntity = testRestTemplate.exchange(url + "/1001", HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<JournalpostDto>>() {
                    }
            );

            assertThat(optional(responseEntity)).hasValueSatisfying(response -> assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).hasSize(2)
            ));
        }

        @DisplayName("skal gi http status 400 når post gjøres uten dokument")
        @Test void skalGiHttpStatus400GrunnetDokument() {
            JournalpostDto lagreJournalpostDto = new JournalpostDtoBygger()
                    .medGjelderBrukerId("06127412345")
                    .get();

            ResponseEntity<JournalpostDto> responseEntity = testRestTemplate.postForEntity(url, lagreJournalpostDto, JournalpostDto.class);

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @DisplayName("skal gi http status 400 når post gjøres med flere dokument")
        @Test void skalGiHttpStatus400GrunnetFlereDokument() {
            JournalpostDto lagreJournalpostDto = new JournalpostDtoBygger()
                    .medDokumenter(asList(new DokumentDto(), new DokumentDto()))
                    .medGjelderBrukerId("06127412345")
                    .get();

            ResponseEntity<JournalpostDto> responseEntity = testRestTemplate.postForEntity(url, lagreJournalpostDto, JournalpostDto.class);

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @DisplayName("skal gi http status 400 når post gjøres uten gjelder bruker id")
        @Test void skalGiHttpStatus400GrunnetGjelderBrukerId() {
            JournalpostDto lagreJournalpostDto = new JournalpostDtoBygger()
                    .medDokumenter(singletonList(new DokumentDto()))
                    .get();

            ResponseEntity<JournalpostDto> responseEntity = testRestTemplate.postForEntity(url, lagreJournalpostDto, JournalpostDto.class);

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @DisplayName("skal gi http status 400 når post gjøres med flere gjelder bruker id")
        @Test void skalGiHttpStatus400GrunnetFlereGjelderBrukerId() {
            JournalpostDto lagreJournalpostDto = new JournalpostDtoBygger()
                    .medDokumenter(singletonList(new DokumentDto()))
                    .medGjelderBrukerId("06127412345", "01117712345")
                    .get();

            ResponseEntity<JournalpostDto> responseEntity = testRestTemplate.postForEntity(url, lagreJournalpostDto, JournalpostDto.class);

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @DisplayName("skal registrere ny journalpost")
        @Test void skalRegistrereNyJournalpost() {
            JournalpostDto lagreJournalpostDto = new JournalpostDtoBygger()
                    .medDokumenter(singletonList(new DokumentDto()))
                    .medGjelderBrukerId("06127412345")
                    .get();

            when(restTemplateMock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(BidragJournalpostDto.class)))
                    .thenReturn(new ResponseEntity<>(enBidragJournalpostMedId(101), HttpStatus.CREATED));

            ResponseEntity<JournalpostDto> responseEntity = testRestTemplate.postForEntity(url, lagreJournalpostDto, JournalpostDto.class);

            assertThat(optional(responseEntity)).hasValueSatisfying(response -> assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED),
                    () -> assertThat(response.getBody()).extracting(JournalpostDto::getJournalpostIdBisys).isEqualTo(101)
            ));
        }

        private BidragJournalpostDto enBidragJournalpostMedId(@SuppressWarnings("SameParameterValue") int id) {
            BidragJournalpostDto bidragJournalpostDto = new BidragJournalpostDto();
            bidragJournalpostDto.setJournalpostId(id);

            return bidragJournalpostDto;
        }

        private <T> Optional<ResponseEntity<T>> optional(ResponseEntity<T> responseEntity) {
            return Optional.ofNullable(responseEntity);
        }
    }

    private String initEndpointUrl(@SuppressWarnings("SameParameterValue") String endpoint) {
        return "http://localhost:" + port + contextPath + endpoint;
    }

    @AfterEach void resetFactory() {
        RestTemplateFactory.reset();
    }
}
