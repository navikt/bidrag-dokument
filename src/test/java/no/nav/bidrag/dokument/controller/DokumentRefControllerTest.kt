package no.nav.bidrag.dokument.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.dokument.BidragDokumentTest
import no.nav.bidrag.dokument.consumer.stub.RestConsumerStub
import no.nav.bidrag.transport.dokument.DokumentTilgangResponse
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import java.io.IOException
import java.util.function.Consumer

@ActiveProfiles(BidragDokumentTest.TEST_PROFILE)
@DisplayName("DokumentController")
@AutoConfigureWireMock(port = 0)
@SpringBootTest(
    classes = [BidragDokumentTest::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EnableMockOAuth2Server
internal class DokumentRefControllerTest {
    @Autowired
    private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

    @LocalServerPort
    private var port = 0

    @Autowired
    private lateinit var restConsumerStub: RestConsumerStub

    @Test
    @DisplayName("skal spørre brevserver om tilgang til dokument")
    @Throws(IOException::class)
    fun skalVideresendeRequestOmTilgangTilDokument() {
        val journalpostId = "BID-12312312"
        val dokumentReferanse = "1234"
        val dokumentUrl = "https://dokument-url.no/"
        val type = "BREVLAGER"
        restConsumerStub
            .runGiTilgangTilDokument(
                journalpostId,
                dokumentReferanse,
                dokumentUrl,
                type,
                HttpStatus.OK.value()
            )
        val dokumentUrlResponse =
            securedTestRestTemplate
                .getForEntity<DokumentTilgangResponse>(localhostUrl("/bidrag-dokument/tilgang/$journalpostId/$dokumentReferanse"))

        Assertions.assertThat(dokumentUrlResponse).satisfies(
            Consumer { response: ResponseEntity<DokumentTilgangResponse> ->
                org.junit.jupiter.api.Assertions.assertAll(
                    {
                        Assertions.assertThat(response.statusCode).`as`("status")
                            .isEqualTo(HttpStatus.OK)
                    },
                    {
                        Assertions.assertThat(response).extracting { it.body }
                            .`as`("url")
                            .isEqualTo(DokumentTilgangResponse(dokumentUrl, type))
                    },
                )
            },
        )
    }

    @Test
    @DisplayName("Skal git status 401 dersom token mangler")
    fun skalGiStatus401DersomTokenMangler() {
        val testRestTemplate = TestRestTemplate()
        val responseEntity =
            testRestTemplate.exchange(
                localhostUrl("/bidrag-dokument/tilgang/BID-123/dokref"),
                HttpMethod.GET,
                null,
                String::class.java
            )
        org.junit.jupiter.api.Assertions.assertEquals(
            responseEntity.statusCode,
            HttpStatus.UNAUTHORIZED
        )
    }

    private fun localhostUrl(url: String): String {
        return "http://localhost:$port$url"
    }
}
