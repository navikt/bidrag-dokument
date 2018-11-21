package no.nav.bidrag.dokument.controller;

import no.nav.bidrag.dokument.JournalpostDtoBygger;
import no.nav.bidrag.dokument.consumer.RestTemplateFactory;
import no.nav.bidrag.dokument.dto.BrevlagerJournalpostDto;
import no.nav.bidrag.dokument.dto.DokumentDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
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
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class) @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("JournalpostController") class JournalpostControllerTest {

    private static final String ENDPOINT_JOURNALPOST = "/journalpost";
    private static final String ENDPOINT_SAKJOURNAL = "/sakjournal";

    @LocalServerPort private int port;
    @Mock private RestTemplate restTemplateMock;
    @Value("${server.servlet.context-path}") private String contextPath;
    @Autowired private TestRestTemplate testRestTemplate;

    @BeforeEach void mockRestTemplateFactory() {
        MockitoAnnotations.initMocks(this);
        RestTemplateFactory.use(() -> restTemplateMock);
    }

    @DisplayName("endpoint: " + ENDPOINT_JOURNALPOST)
    @Nested class EndpointHentJournalpost {

        private String url = initEndpointUrl(ENDPOINT_JOURNALPOST);

        @DisplayName("skal mangle body når journalpost ikke finnes")
        @Test void skalMangleBodyNarJournalpostIkkeFinnes() {
            when(restTemplateMock.getForEntity(eq("/journalpost/1"), eq(JournalpostDto.class))).thenReturn(new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT));

            ResponseEntity<JournalpostDto> journalpostResponseEntity = testRestTemplate.getForEntity(url + "/joark-1", JournalpostDto.class);

            verify(restTemplateMock).getForEntity(eq("/journalpost/1"), eq(JournalpostDto.class));

            assertThat(Optional.of(journalpostResponseEntity)).hasValueSatisfying(response -> assertAll(
                    () -> assertThat(response.getBody()).isNull(),
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT)
            ));
        }

        @DisplayName("skal hente Journalpost når den eksisterer")
        @Test void skalHenteJournalpostNarDenEksisterer() {
            when(restTemplateMock.getForEntity(eq("/journalpost/1"), eq(JournalpostDto.class))).thenReturn(new ResponseEntity<>(
                    enJournalpostMedInnhold("MIDLERTIDIG"), HttpStatus.I_AM_A_TEAPOT
            ));

            ResponseEntity<JournalpostDto> responseEntity = testRestTemplate.getForEntity(url + "/joark-1", JournalpostDto.class);

            verify(restTemplateMock).getForEntity(eq("/journalpost/1"), eq(JournalpostDto.class));

            assertThat(Optional.of(responseEntity)).hasValueSatisfying(response -> assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).extracting(JournalpostDto::getHello).isEqualTo("hello from bidrag-dokument"),
                    () -> assertThat(response.getBody()).extracting(JournalpostDto::getInnhold).isEqualTo("MIDLERTIDIG")
            ));
        }

        private JournalpostDto enJournalpostMedInnhold(@SuppressWarnings("SameParameterValue") String innhold) {
            JournalpostDto journalpostDto = new JournalpostDto();
            journalpostDto.setInnhold(innhold);

            return journalpostDto;
        }

        @DisplayName("skal hente journalpost fra midlertidig brevlager")
        @Test void skalHenteJournalpostFraMidlertidigBrevlager() {
            when(restTemplateMock.getForEntity(eq("/journalpost/1"), eq(JournalpostDto.class))).thenReturn(new ResponseEntity<>(
                    enJournalpostFra("Grev Still E. Ben"), HttpStatus.I_AM_A_TEAPOT
            ));

            ResponseEntity<JournalpostDto> responseEntity = testRestTemplate.getForEntity(url + "/bid-1", JournalpostDto.class);

            verify(restTemplateMock).getForEntity(eq("/journalpost/1"), eq(JournalpostDto.class));

            assertThat(Optional.of(responseEntity)).hasValueSatisfying(response -> assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).extracting(JournalpostDto::getHello).isEqualTo("hello from bidrag-dokument"),
                    () -> assertThat(response.getBody()).extracting(JournalpostDto::getAvsenderNavn).isEqualTo("Grev Still E. Ben")
            ));
        }

        private JournalpostDto enJournalpostFra(@SuppressWarnings("SameParameterValue") String setAvsenderNavn) {
            JournalpostDto jp = new JournalpostDto();
            jp.setAvsenderNavn(setAvsenderNavn);

            return jp;
        }

        @DisplayName("skal gi http status 400 når post gjøres uten dokument")
        @Test void skalGiHttpStatus400GrunnetDokument() {
            JournalpostDto lagreJournalpostDto = new JournalpostDtoBygger()
                    .medGjelderBrukerId("06127412345")
                    .utenDokument()
                    .build();

            ResponseEntity<JournalpostDto> responseEntity = testRestTemplate.postForEntity(url, lagreJournalpostDto, JournalpostDto.class);

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @DisplayName("skal gi http status 400 når post gjøres med flere dokument")
        @Test void skalGiHttpStatus400GrunnetFlereDokument() {
            JournalpostDto lagreJournalpostDto = new JournalpostDtoBygger()
                    .medDokumenter(asList(new DokumentDto(), new DokumentDto()))
                    .medGjelderBrukerId("06127412345")
                    .build();

            ResponseEntity<JournalpostDto> responseEntity = testRestTemplate.postForEntity(url, lagreJournalpostDto, JournalpostDto.class);

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @DisplayName("skal gi http status 400 når post gjøres uten gjelder bruker id")
        @Test void skalGiHttpStatus400GrunnetGjelderBrukerId() {
            JournalpostDto lagreJournalpostDto = new JournalpostDtoBygger()
                    .medDokumenter(singletonList(new DokumentDto()))
                    .utenBrukerId()
                    .build();

            ResponseEntity<JournalpostDto> responseEntity = testRestTemplate.postForEntity(url, lagreJournalpostDto, JournalpostDto.class);

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @DisplayName("skal gi http status 400 når post gjøres med flere gjelder bruker id")
        @Test void skalGiHttpStatus400GrunnetFlereGjelderBrukerId() {
            JournalpostDto lagreJournalpostDto = new JournalpostDtoBygger()
                    .medDokumenter(singletonList(new DokumentDto()))
                    .medGjelderBrukerId("06127412345", "01117712345")
                    .build();

            ResponseEntity<JournalpostDto> responseEntity = testRestTemplate.postForEntity(url, lagreJournalpostDto, JournalpostDto.class);

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @DisplayName("skal registrere ny journalpost")
        @Test void skalRegistrereNyJournalpost() {
            JournalpostDto lagreJournalpostDto = new JournalpostDtoBygger()
                    .medDokumenter(singletonList(new DokumentDto()))
                    .medGjelderBrukerId("06127412345")
                    .build();

            when(restTemplateMock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(BrevlagerJournalpostDto.class)))
                    .thenReturn(new ResponseEntity<>(enBrevlagerJournalpostDtoMedId(101), HttpStatus.CREATED));

            ResponseEntity<JournalpostDto> responseEntity = testRestTemplate.postForEntity(url, lagreJournalpostDto, JournalpostDto.class);

            assertThat(optional(responseEntity)).hasValueSatisfying(response -> assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED),
                    () -> assertThat(response.getBody()).extracting(JournalpostDto::getJournalpostId).isEqualTo("BID-101")
            ));
        }

        private BrevlagerJournalpostDto enBrevlagerJournalpostDtoMedId(@SuppressWarnings("SameParameterValue") int id) {
            BrevlagerJournalpostDto brevlagerJournalpostDto = new BrevlagerJournalpostDto();
            brevlagerJournalpostDto.setJournalpostId(id);

            return brevlagerJournalpostDto;
        }
    }

    @DisplayName("endpoint: " + ENDPOINT_SAKJOURNAL)
    @Nested class EndpointJournalpost {

        private String url = initEndpointUrl(ENDPOINT_SAKJOURNAL);

        @DisplayName("skal finne Journalposter for en bidragssak") @SuppressWarnings("unchecked")
        @Test void skalFinneJournalposterForEnBidragssak() {
            when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any()))
                    .thenReturn(new ResponseEntity<>(asList(new JournalpostDto(), new JournalpostDto()), HttpStatus.I_AM_A_TEAPOT));

            ResponseEntity<List<JournalpostDto>> responseEntity = testRestTemplate.exchange(url + "/bid-1001", HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<JournalpostDto>>() {
                    }
            );

            assertThat(optional(responseEntity)).hasValueSatisfying(response -> assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).hasSize(2)
            ));

            verify(restTemplateMock).exchange(eq("/sak/1001"), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any());
        }

        @DisplayName("skal finne Journalposter for en gsak") @SuppressWarnings("unchecked")
        @Test void skalFinneJournalposterForEnGsak() {
            when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any()))
                    .thenReturn(new ResponseEntity<>(asList(new JournalpostDto(), new JournalpostDto()), HttpStatus.I_AM_A_TEAPOT));

            ResponseEntity<List<JournalpostDto>> responseEntity = testRestTemplate.exchange(url + "/gsak-1001", HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<JournalpostDto>>() {
                    }
            );

            assertThat(optional(responseEntity)).hasValueSatisfying(response -> assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).hasSize(2)
            ));

            verify(restTemplateMock).exchange(eq("/journalpost/gsak/1001"), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any());
        }

        @DisplayName("skal ikke få internal server error (HttpStatus 500) når ukjent bidragssaksnummerstreng brukes") @SuppressWarnings("unchecked")
        @Test void skalIkkeFremprovosereHttpStatus500MedUkjentSaksnummerStreng() {
            when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any()))
                    .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

            ResponseEntity<List<JournalpostDto>> responseEntity = testRestTemplate.exchange(url + "/bid-svada", HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<JournalpostDto>>() {
                    }
            );

            assertThat(optional(responseEntity)).hasValueSatisfying(response -> assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT),
                    () -> assertThat(response.getBody()).isNull()
            ));
        }

        @DisplayName("skal få bad request (HttpStatus 400) når ukjent saksnummerstreng brukes") @SuppressWarnings("unchecked")
        @Test void skalFremprovosereHttpStatus500MedUkjentSaksnummerStreng() {
            ResponseEntity<List<JournalpostDto>> responseEntity = testRestTemplate.exchange(url + "/svada", HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<JournalpostDto>>() {
                    }
            );

            assertThat(optional(responseEntity)).hasValueSatisfying(response -> assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                    () -> assertThat(response.getBody()).isNull()
            ));

            verifyZeroInteractions(restTemplateMock);
        }
    }

    private <T> Optional<ResponseEntity<T>> optional(ResponseEntity<T> responseEntity) {
        return Optional.ofNullable(responseEntity);
    }

    private String initEndpointUrl(@SuppressWarnings("SameParameterValue") String endpoint) {
        return "http://localhost:" + port + contextPath + endpoint;
    }

    @AfterEach void resetFactory() {
        RestTemplateFactory.reset();
    }
}
