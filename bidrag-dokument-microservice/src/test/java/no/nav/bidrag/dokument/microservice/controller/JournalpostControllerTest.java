package no.nav.bidrag.dokument.microservice.controller;

import no.nav.bidrag.dokument.consumer.RestTemplateFactory;
import no.nav.bidrag.dokument.domain.JournalTilstand;
import no.nav.bidrag.dokument.domain.Journalpost;
import no.nav.bidrag.dokument.domain.dto.JournalforingDto;
import org.assertj.core.api.Condition;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("JournalpostController")
class JournalpostControllerTest {

    @LocalServerPort private int port;
    @Mock private RestTemplate joarkRestTemplateMock;
    @Value("${bidrag.dokument.url.journalpost}") private String journalpostEndpoint;
    @Value("${bidrag.joark.url.journalforing}") private String journalforingEndpoint;
    @Value("${server.servlet.context-path}") private String contextPath;
    @Autowired private TestRestTemplate testRestTemplate;

    @BeforeEach void mockRestTemplateFactory() {
        MockitoAnnotations.initMocks(this);
        RestTemplateFactory.use(uriTemplateHandler -> joarkRestTemplateMock);
    }

    @DisplayName("skal ha body som null når journalforing ikke finnes")
    @Test void skalGiBodySomNullNarJournalforingIkkeFinnes() {
        when(joarkRestTemplateMock.getForEntity(eq(journalforingEndpoint), eq(JournalforingDto.class))).thenReturn(new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT));

        String url = String.format("http://localhost:%d%s/%s/hent/1", port, contextPath, journalpostEndpoint);
        ResponseEntity<Journalpost> journalpostResponseEntity = testRestTemplate.getForEntity(url, Journalpost.class);

        assertThat(Optional.of(journalpostResponseEntity)).hasValueSatisfying(response -> assertAll(
                () -> assertThat(response.getBody()).isNull(),
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT)
        ));
    }

    @DisplayName("skal finne Journalpost når journalforing finnes")
    @Test void skalGiJournalpostNarJournalforingFinnes() {
        when(joarkRestTemplateMock.getForEntity(eq(journalforingEndpoint), eq(JournalforingDto.class))).thenReturn(new ResponseEntity<>(
                JournalforingDto.build().with(JournalTilstand.MIDLERTIDIG).get(), HttpStatus.I_AM_A_TEAPOT
        ));

        String url = String.format("http://localhost:%d%s/%s/hent/1", port, contextPath, journalpostEndpoint);
        ResponseEntity<Journalpost> responseEntity = testRestTemplate.getForEntity(url, Journalpost.class);

        Condition<Journalpost> equalToJournalforing = new Condition<>(
                j -> j != null && JournalTilstand.MIDLERTIDIG.equals(j.getJournalTilstand()), "Journalpost med midlertidig journalforing"
        );

        assertThat(Optional.of(responseEntity)).hasValueSatisfying(response -> assertAll(
                () -> assertThat(response.getBody()).is(equalToJournalforing),
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK)
        ));
    }

    @AfterEach void resetFactory() {
        RestTemplateFactory.reset();
    }
}
