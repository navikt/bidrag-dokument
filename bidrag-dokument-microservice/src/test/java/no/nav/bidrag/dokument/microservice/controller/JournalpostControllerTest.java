package no.nav.bidrag.dokument.microservice.controller;

import no.nav.bidrag.dokument.consumer.RestTemplateFactory;
import no.nav.bidrag.dokument.domain.JournalTilstand;
import no.nav.bidrag.dokument.domain.JournalpostDto;
import no.nav.bidrag.dokument.domain.bisys.BidragJournalpostDto;
import no.nav.bidrag.dokument.domain.joark.JournalforingDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("JournalpostController")
class JournalpostControllerTest {

    // todo: how to read services and inject from fasit...

    @LocalServerPort private int port;
    @Mock private RestTemplate joarkRestTemplateMock;
    @Value("${journalpost.restservice}") private String journalpostRestservice;
    @Value("${joark.restservice}") private String journalforingRestservice;
    @Value("${server.servlet.context-path}") private String contextPath;
    @Autowired private TestRestTemplate testRestTemplate;

    @BeforeEach void mockRestTemplateFactory() {
        MockitoAnnotations.initMocks(this);
        RestTemplateFactory.use(() -> joarkRestTemplateMock);
    }

    @DisplayName("skal ha body som null når journalforing ikke finnes")
    @Test void skalGiBodySomNullNarJournalforingIkkeFinnes() {
        when(joarkRestTemplateMock.getForEntity(eq(journalforingRestservice), eq(JournalforingDto.class))).thenReturn(new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT));

        String url = initBaseUrl() + "/journalpost/hent/1";
        ResponseEntity<JournalpostDto> journalpostResponseEntity = testRestTemplate.getForEntity(url, JournalpostDto.class);

        assertThat(Optional.of(journalpostResponseEntity)).hasValueSatisfying(response -> assertAll(
                () -> assertThat(response.getBody()).isNull(),
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT)
        ));
    }

    @DisplayName("skal finne Journalpost når journalforing finnes")
    @Test void skalGiJournalpostNarJournalforingFinnes() {
        when(joarkRestTemplateMock.getForEntity(eq(journalforingRestservice), eq(JournalforingDto.class))).thenReturn(new ResponseEntity<>(
                JournalforingDto.build().with(JournalTilstand.MIDLERTIDIG).get(), HttpStatus.I_AM_A_TEAPOT
        ));

        String url = initBaseUrl() + "/journalpost/hent/1";
        ResponseEntity<JournalpostDto> responseEntity = testRestTemplate.getForEntity(url, JournalpostDto.class);

        assertThat(Optional.of(responseEntity)).hasValueSatisfying(response -> assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).extracting(JournalpostDto::getHello).contains("hello from bidrag-dokument"),
                () -> assertThat(response.getBody()).extracting(JournalpostDto::getJournalTilstand).contains(JournalTilstand.MIDLERTIDIG)
        ));
    }

    @DisplayName("skal finne Journalposter for en bidragssak")
    @Test void skalFinneJournalposterForEnBidragssak() {
        when(joarkRestTemplateMock.getForEntity(eq(journalpostRestservice), eq(List.class))).thenReturn(new ResponseEntity<>(
                asList(new BidragJournalpostDto(), new BidragJournalpostDto()), HttpStatus.I_AM_A_TEAPOT
        ));

        String url = initBaseUrl() + "/journalpost/finn/for/1001";
        ResponseEntity<List> responseEntity = testRestTemplate.getForEntity(url, List.class);

        assertThat(Optional.of(responseEntity)).hasValueSatisfying(response -> assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).hasSize(2)
        ));
    }

    private String initBaseUrl() {
        return "http://localhost:" + port + contextPath;
    }

    @AfterEach void resetFactory() {
        RestTemplateFactory.reset();
    }
}
