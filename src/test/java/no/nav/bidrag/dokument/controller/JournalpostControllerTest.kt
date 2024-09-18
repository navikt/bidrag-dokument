package no.nav.bidrag.dokument.controller

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import no.nav.bidrag.commons.web.EnhetFilter
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.dokument.BidragDokumentTest
import no.nav.bidrag.dokument.consumer.BidragDokumentConsumer
import no.nav.bidrag.dokument.consumer.BidragDokumentConsumer.Companion.createEnhetHeader
import no.nav.bidrag.dokument.consumer.stub.RestConsumerStub
import no.nav.bidrag.transport.dokument.AvvikType
import no.nav.bidrag.transport.dokument.Avvikshendelse
import no.nav.bidrag.transport.dokument.BehandleAvvikshendelseResponse
import no.nav.bidrag.transport.dokument.EndreJournalpostCommand
import no.nav.bidrag.transport.dokument.JournalpostDto
import no.nav.bidrag.transport.dokument.JournalpostResponse
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException
import java.util.*

@SpringBootTest(
    classes = [BidragDokumentTest::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles(BidragDokumentTest.TEST_PROFILE)
@DisplayName("JournalpostController")
@EnableMockOAuth2Server
@Disabled("")
internal class JournalpostControllerTest {

    @LocalServerPort
    private var localServerPort = 0

    @Value("\${server.servlet.context-path}")
    private lateinit var contextPath: String

    @Autowired
    private lateinit var httpHeaderTestRestTemplate: HttpHeaderTestRestTemplate

    @Autowired
    private lateinit var restConsumerStub: RestConsumerStub
    private fun <T> initHttpEntity(body: T, vararg customHeaders: CustomHeader): HttpEntity<T> {
        val httpHeaders = HttpHeaders().apply {
            for ((name, value) in customHeaders) {
                add(name, value)
            }
        }
        return HttpEntity(body, httpHeaders)
    }

    private fun initEndpointUrl(endpoint: String): String {
        return "http://localhost:$localServerPort$contextPath$endpoint"
    }

    @JvmRecord
    private data class CustomHeader(val name: String, val value: String)

    @BeforeEach
    fun cleanup() {
        WireMock.resetToDefault()
        WireMock.resetAllScenarios()
        WireMock.reset()
    }

    @Nested
    @DisplayName("hent journalpost")
    internal inner class Hent {
        @Test
        @DisplayName("skal mangle body når journalpost ikke finnes")
        fun skalMangleBodyNarJournalpostIkkeFinnes() {
            val jpId = "JOARK-1"
            val saksnr = "007"
            val queryParams = HashMap<String, StringValuePattern>()
            queryParams["saksnummer"] = WireMock.equalTo(saksnr)
            restConsumerStub.runHenteJournalpostArkiv(jpId, queryParams, HttpStatus.NO_CONTENT, "")
            val journalpostResponseEntity = httpHeaderTestRestTemplate
                .getForEntity<JournalpostResponse>(
                    initEndpointUrl(
                        String.format(
                            BidragDokumentConsumer.PATH_JOURNALPOST_UTEN_SAK,
                            jpId,
                        ) + "?saksnummer=" + saksnr,
                    ),
                )
            Assertions.assertThat(Optional.of(journalpostResponseEntity)).hasValueSatisfying {
                assertAll(
                    { Assertions.assertThat(it.body).isNull() },
                    { Assertions.assertThat(it.statusCode).isEqualTo(HttpStatus.NO_CONTENT) },
                )
            }
        }

        @Test
        @DisplayName("skal hente Journalpost når den eksisterer")
        @Throws(IOException::class)
        fun skalHenteJournalpostNarDenEksisterer() {
            val jpId = "JOARK-2"
            val saksnr = "007"
            val queryParams = HashMap<String, StringValuePattern>()
            queryParams["saksnummer"] = WireMock.equalTo(saksnr)
            val responsfilnavn = "journalpostInnholdMidlertidig.json"
            restConsumerStub.runHenteJournalpostArkiv(
                jpId,
                queryParams,
                HttpStatus.OK,
                RestConsumerStub.lesResponsfilSomStreng(responsfilnavn),
            )
            val url = initEndpointUrl("/journal/joark-2?saksnummer=007")
            val responseEntity =
                httpHeaderTestRestTemplate.getForEntity<JournalpostResponse>(url)
            Assertions.assertThat(responseEntity)
                .satisfies({
                    assertAll(
                        { Assertions.assertThat(it.statusCode).isEqualTo(HttpStatus.OK) },
                        {
                            Assertions.assertThat<JournalpostResponse?>(it.body)
                                .extracting<JournalpostDto> { it.journalpost }
                                .extracting<String> { it.innhold }
                                .isEqualTo("MIDLERTIDIG")
                        },
                    )
                })
        }

        @Test
        @DisplayName("Skal gi status 401 dersom token mangler")
        fun skalGiStatus401DersomTokenMangler() {
            val testRestTemplate = TestRestTemplate()
            val url = initEndpointUrl("/journal/joark-2?saksnummer=007")
            val responseEntity = testRestTemplate.getForEntity(url, String::class.java)
            org.junit.jupiter.api.Assertions.assertEquals(
                HttpStatus.valueOf(responseEntity.statusCode.value()),
                HttpStatus.UNAUTHORIZED,
            )
        }

        @Test
        @DisplayName("skal hente journalpost fra midlertidig brevlager")
        fun skalHenteJournalpostFraMidlertidigBrevlager() {
            val jpId = "BID-1"
            val saksnr = "007"
            val queryParams = HashMap<String, StringValuePattern>()
            queryParams["saksnummer"] = WireMock.equalTo(saksnr)
            val journalpostelementer: MutableMap<String, String> = HashMap()
            journalpostelementer["avsenderNavn"] = "Grev Still E. Ben"
            restConsumerStub.runHenteJournalpost(
                jpId,
                queryParams,
                HttpStatus.OK,
                RestConsumerStub.generereJournalpostrespons(journalpostelementer),
            )
            val url = initEndpointUrl("/journal/$jpId?saksnummer=$saksnr")
            val responseEntity =
                httpHeaderTestRestTemplate.getForEntity<JournalpostResponse>(url)
            Assertions.assertThat(responseEntity)
                .satisfies(
                    {
                        assertAll(
                            {
                                Assertions.assertThat(it.statusCode).`as`("status code")
                                    .isEqualTo(HttpStatus.OK)
                            },
                            {
                                Assertions.assertThat<JournalpostResponse?>(it.body)
                                    .`as`("JournalpostResponse")
                                    .extracting<JournalpostDto> { it.journalpost }
                                    .extracting<String> { it.avsenderNavn }
                                    .isEqualTo("Grev Still E. Ben")
                            },
                        )
                    },
                )
        }
    }

    @Nested
    @DisplayName("lagre journalpost")
    internal inner class Lagre {
        @Test
        @DisplayName("skal få BAD_REQUEST når prefix er ukjent")
        fun skalFaBadRequestMedUkjentPrefix() {
            val lagreJournalpostUrl = initEndpointUrl("/journal/svada-1")
            val entityMedEnhetsheader =
                HttpEntity(EndreJournalpostCommand(), createEnhetHeader("4802"))
            val badRequestResponse =
                httpHeaderTestRestTemplate.patchForEntity<Unit>(
                    lagreJournalpostUrl,
                    entityMedEnhetsheader,
                )
            Assertions.assertThat(badRequestResponse).extracting { it.statusCode }.isEqualTo(
                HttpStatus.BAD_REQUEST,
            )
        }

        @Test
        @DisplayName("skal få BAD_REQUEST når journalpostId ikke er et tall")
        fun skalFaBadRequestMedJournalpostIdSomIkkeErEtTall() {
            val lagreJournalpostUrl = initEndpointUrl("/journal/bid-en?saksnummer=69")
            val badRequestResponse =
                httpHeaderTestRestTemplate.getForEntity<JournalpostResponse>(lagreJournalpostUrl)
            Assertions.assertThat(badRequestResponse)
                .extracting { it.statusCode }.isEqualTo(
                    HttpStatus.BAD_REQUEST,
                )
        }

        @Test
        @DisplayName("skal få BAD_REQUEST når prefix er ukjent ved endring av journalpost")
        fun skalFaBadRequestMedUkjentPrefixVedEndringAvJournalpost() {
            val lagreJournalpostUrl = initEndpointUrl("/journal/svada-1")
            val badRequestResponse = httpHeaderTestRestTemplate
                .patchForEntity<JournalpostResponse>(
                    lagreJournalpostUrl,
                    HttpEntity(EndreJournalpostCommand(), createEnhetHeader("4802")),
                )
            Assertions.assertThat(badRequestResponse)
                .extracting { it.statusCode }.isEqualTo(HttpStatus.BAD_REQUEST)
        }

        @Disabled("Henger i ett minutt før fullførelse")
        @Test
        @DisplayName("skal endre journalpost")
        @Throws(
            IOException::class,
        )
        fun skalEndreJournalpost() {
            val jpId = "BID-1"
            restConsumerStub.runEndreJournalpost(jpId, HttpStatus.ACCEPTED)
            val lagreJournalpostUrl = initEndpointUrl("/journal/BID-1")
            val endretJournalpostResponse = httpHeaderTestRestTemplate
                .patchForEntity<Unit>(
                    lagreJournalpostUrl,
                    HttpEntity(EndreJournalpostCommand(), createEnhetHeader("4802")),
                )
            Assertions.assertThat(endretJournalpostResponse)
                .satisfies({ Assertions.assertThat(it.statusCode).isEqualTo(HttpStatus.ACCEPTED) })
        }

        @Disabled("Henger i ett minutt før fullførelse")
        @Test
        @DisplayName("skal videresende headere når journalpost endres")
        fun skalVideresendeHeadereVedEndreJournalpost() {
            val jpId = "BID-1"
            restConsumerStub.runEndreJournalpostMedHeader(
                jpId,
                HttpHeader.httpHeader(HttpHeaders.WARNING, "rofl"),
                HttpStatus.OK,
                "",
            )
            val lagreJournalpostUrl = initEndpointUrl("/journal/BID-1")
            val endretJournalpostResponse = httpHeaderTestRestTemplate
                .patchForEntity<Unit>(
                    lagreJournalpostUrl,
                    HttpEntity(EndreJournalpostCommand(), createEnhetHeader("4802")),
                )
            Assertions.assertThat(endretJournalpostResponse)
                .satisfies({
                    assertAll(
                        {
                            Assertions.assertThat(it.statusCode).`as`("status")
                                .isEqualTo(HttpStatus.OK)
                        },
                        {
                            val headers = it.headers
                            Assertions.assertThat(headers.getFirst(HttpHeaders.WARNING))
                                .`as`("warning header").isEqualTo("rofl")
                        },
                    )
                })
        }
    }

    @Nested
    @DisplayName("avvik på journalpost")
    internal inner class Avvik {
        @Test
        @DisplayName("skal feile når man henter avvikshendelser uten å prefikse journalpostId med kildesystem")
        fun skalFeileVedHentingAvAvvikshendelserForJournalpostNarJournalpostIdIkkeErPrefiksetMedKildesystem() {
            val url = initEndpointUrl("/journal/1/avvik")
            val responseEntity =
                httpHeaderTestRestTemplate.getForEntity<List<AvvikType>>(url)
            Assertions.assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        }

        @Test
        @DisplayName("skal finne avvikshendelser på en journalpost")
        fun skalFinneAvvikshendelserForJournalpost() {
            val jpId = "BID-1"
            val saksnr = "1001"
            val path = String.format(BidragDokumentConsumer.PATH_AVVIK_PA_JOURNALPOST, jpId)
            val queryParams = HashMap<String, StringValuePattern>()
            queryParams["saksnummer"] = WireMock.equalTo(saksnr)
            val respons = java.lang.String.join("\n", " [", "\"BESTILL_ORIGINAL\"", "]")
            restConsumerStub.runGet(path, queryParams, HttpStatus.OK, respons)
            val url = initEndpointUrl("$path?saksnummer=1001")

            // when
            val responseEntity =
                httpHeaderTestRestTemplate.getForEntity<List<AvvikType>>(url)
            assertAll(
                {
                    Assertions.assertThat(responseEntity.statusCode).`as`("status")
                        .isEqualTo(HttpStatus.OK)
                },
                { Assertions.assertThat(responseEntity.body).`as`("avvik").hasSize(1) },
                {
                    Assertions.assertThat(responseEntity.body).`as`("avvik")
                        .contains(AvvikType.BESTILL_ORIGINAL)
                },
            )
        }

        @Test
        @DisplayName("skal finne avvikshendelser på en joark journalpost")
        fun skalFinneAvvikshendelserForJoarkJournalpost() {
            val jpId = "JOARK-1"
            val saksnr = "1001"
            val path = String.format(BidragDokumentConsumer.PATH_AVVIK_PA_JOURNALPOST, jpId)
            val queryParams = HashMap<String, StringValuePattern>()
            queryParams["saksnummer"] = WireMock.equalTo(saksnr)
            val respons = java.lang.String.join("\n", " [", "\"BESTILL_ORIGINAL\"", "]")
            restConsumerStub.runGetArkiv(path, queryParams, HttpStatus.OK, respons)
            val url = initEndpointUrl("$path?saksnummer=1001")

            // when
            val responseEntity =
                httpHeaderTestRestTemplate.getForEntity<List<AvvikType>>(url)
            assertAll(
                {
                    Assertions.assertThat(responseEntity.statusCode).`as`("status")
                        .isEqualTo(HttpStatus.OK)
                },
                { Assertions.assertThat(responseEntity.body).`as`("avvik").hasSize(1) },
                {
                    Assertions.assertThat(responseEntity.body).`as`("avvik")
                        .contains(AvvikType.BESTILL_ORIGINAL)
                },
            )
        }

        @Test
        @DisplayName("skal opprette et avvik på en journalpost")
        @Throws(InterruptedException::class)
        fun skalOppretteAvvikPaJournalpost() {
            // given
            val jpId = "BID-4"
            val enhetsnummer = "4806"
            val avvikshendelse = Avvikshendelse(AvvikType.BESTILL_ORIGINAL.name, enhetsnummer)
            val path = String.format(BidragDokumentConsumer.PATH_AVVIK_PA_JOURNALPOST, jpId)
            val respons = java.lang.String.join(
                "\n",
                " {",
                "\"avvikType\":",
                "\"" + avvikshendelse.avvikType + "\"",
                "}",
            )
            restConsumerStub.runPost(path, HttpStatus.CREATED, respons)
            val bestillOriginalEntity =
                initHttpEntity(avvikshendelse, CustomHeader(EnhetFilter.X_ENHET_HEADER, "1234"))
            val url = initEndpointUrl(path)

            // when
            val responseEntity =
                httpHeaderTestRestTemplate.postForEntity<BehandleAvvikshendelseResponse>(
                    url,
                    bestillOriginalEntity,
                )

            // then
            assertAll(
                {
                    Assertions.assertThat(responseEntity.statusCode).`as`("status")
                        .isEqualTo(HttpStatus.CREATED)
                },
                {
                    Assertions.assertThat(responseEntity.body).`as`("body")
                        .isEqualTo(BehandleAvvikshendelseResponse(AvvikType.BESTILL_ORIGINAL))
                },
            )
        }

        @Test
        @DisplayName("skal opprette et avvik på en Joark journalpost")
        fun skalOppretteAvvikPaJoarkJournalpost() {
            // given
            val jpId = "JOARK-4"
            val enhetsnummer = "4806"
            val avvikshendelse = Avvikshendelse(AvvikType.BESTILL_ORIGINAL.name, enhetsnummer)
            val path = String.format(BidragDokumentConsumer.PATH_AVVIK_PA_JOURNALPOST, jpId)
            val respons = java.lang.String.join(
                "\n",
                " {",
                "\"avvikType\":",
                "\"" + avvikshendelse.avvikType + "\"",
                "}",
            )
            restConsumerStub.runPostArkiv(path, HttpStatus.CREATED, respons)
            val bestillOriginalEntity =
                initHttpEntity(avvikshendelse, CustomHeader(EnhetFilter.X_ENHET_HEADER, "1234"))
            val url = initEndpointUrl(path)

            // when
            val responseEntity =
                httpHeaderTestRestTemplate.postForEntity<BehandleAvvikshendelseResponse>(
                    url,
                    bestillOriginalEntity,
                )

            // then
            assertAll(
                {
                    Assertions.assertThat(responseEntity.statusCode).`as`("status")
                        .isEqualTo(HttpStatus.CREATED)
                },
                {
                    Assertions.assertThat(responseEntity.body).`as`("body")
                        .isEqualTo(BehandleAvvikshendelseResponse(AvvikType.BESTILL_ORIGINAL))
                },
            )
        }

        @Test
        @DisplayName("skal få bad request uten header value")
        fun skalFaBadRequestUtenHeaderValue() {
            val avvikshendelse = Avvikshendelse("BESTILL_ORIGINAL", "4806", "1001")
            val ukjentAvvikEntity = initHttpEntity(avvikshendelse)
            val url = initEndpointUrl("/journal/BID-1/avvik")
            val responseEntity =
                httpHeaderTestRestTemplate.postForEntity<String>(url, ukjentAvvikEntity)
            Assertions.assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        }

        @Test
        @DisplayName("skal få bad request når avvikstype ikke kan parses")
        fun skalFaBadRequest() {
            val avvikshendelse =
                Avvikshendelse("AVVIK_IKKE_BLANT_KJENTE_AVVIKSTYPER", "4806", "1001")
            val ukjentAvvikEntity =
                initHttpEntity(avvikshendelse, CustomHeader(EnhetFilter.X_ENHET_HEADER, "1234"))
            val url = initEndpointUrl("/journal/BID-1/avvik")
            val responseEntity =
                httpHeaderTestRestTemplate.postForEntity<BehandleAvvikshendelseResponse>(
                    url,
                    ukjentAvvikEntity,
                )
            Assertions.assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        }
    }

    @Nested
    @DisplayName("Journalstatus er mottaksregistrert")
    internal inner class MottaksregistrertJournalstatus {
        @Test
        @DisplayName("skal få httpstatus 400 (BAD_REQUEST) når man henter journalpost uten gyldig prefix på journalpost id")
        fun skalFaBadRequestVedFeilPrefixPaId() {
            val journalpostResponseEntity = httpHeaderTestRestTemplate
                .getForEntity<JournalpostResponse>(PATH_JOURNALPOST_UTEN_SAK + "ugyldig-id")
            Assertions.assertThat(journalpostResponseEntity.statusCode)
                .isEqualTo(HttpStatus.BAD_REQUEST)
        }

        @Test
        @DisplayName("skal hente journalpost uten sakstilknytning")
        fun skalHenteJournalpostUtenSakstilknytning() {
            // given
            val jpId = "BID-1"
            val journalpostelementer: MutableMap<String, String> = HashMap()
            journalpostelementer["avsenderNavn"] = "Grev Still E. Ben"
            restConsumerStub
                .runGet(
                    String.format(Companion.PATH_JOURNALPOST_UTEN_SAK + "%s", jpId),
                    HttpStatus.OK,
                    RestConsumerStub.generereJournalpostrespons(journalpostelementer),
                )

            // when
            val respons = httpHeaderTestRestTemplate.getForEntity<JournalpostResponse>(
                PATH_JOURNALPOST_UTEN_SAK + jpId,
            )

            // then
            org.junit.jupiter.api.Assertions.assertTrue(respons.statusCode.is2xxSuccessful)
        }

        @Test
        @DisplayName("skal få httpstatus 400 (BAD_REQUST) når man skal finne avvik på journalpost uten gyldig prefix på id")
        fun skalFaBadRequestVedFinnAvvikForJournalpostMedUgyldigPrefixPaId() {
            // given, when
            val journalpostResponseEntity =
                httpHeaderTestRestTemplate.getForEntity<List<AvvikType>>(PATH_JOURNALPOST_UTEN_SAK + "ugyldig-id/avvik")

            // then
            Assertions.assertThat(journalpostResponseEntity.statusCode)
                .isEqualTo(HttpStatus.BAD_REQUEST)
        }

        @Test
        @DisplayName("skal finne avvik på journalpost uten sakstilknytning")
        fun skalFinneAvvikPaJournalpostUtenSakstilknytning() {
            // given
            val jpId = "BID-1"
            restConsumerStub
                .runGet(
                    String.format(BidragDokumentConsumer.PATH_AVVIK_PA_JOURNALPOST, jpId),
                    HttpStatus.OK,
                    java.lang.String.join("\n", " [", " \"ARKIVERE_JOURNALPOST\"]"),
                )

            // when
            val respons = httpHeaderTestRestTemplate
                .getForEntity<List<AvvikType>>(Companion.PATH_JOURNALPOST_UTEN_SAK + "BID-1/avvik")

            // then
            org.junit.jupiter.api.Assertions.assertTrue(respons.statusCode.is2xxSuccessful)
        }

        @Test
        @DisplayName("skal opprette avvik")
        @Disabled
        fun // feilet når RequestFactory fra apache ble lagt til i RestTemplate grunnet endring av PUT -> PATCH... ???
        skalOppretteAvvik() {
            // given
            val jpId = "BID-666"
            val enhetsnummer = "4806"
            val avvikshendelse = Avvikshendelse(AvvikType.ENDRE_FAGOMRADE.name, enhetsnummer)
            val path = String.format(BidragDokumentConsumer.PATH_AVVIK_PA_JOURNALPOST, jpId)
            val respons = "na"
            restConsumerStub.runPost(path, HttpStatus.CREATED, respons)
            val bestillOriginalEntity =
                initHttpEntity(avvikshendelse, CustomHeader(EnhetFilter.X_ENHET_HEADER, "1234"))
            val url = initEndpointUrl(path)

            // when
            val responseEntity =
                httpHeaderTestRestTemplate.postForEntity<BehandleAvvikshendelseResponse>(
                    url,
                    bestillOriginalEntity,
                )

            // then
            assertAll(
                {
                    Assertions.assertThat(responseEntity.statusCode).`as`("status")
                        .isEqualTo(HttpStatus.CREATED)
                },
                {
                    Assertions.assertThat(responseEntity.body).`as`("body")
                        .isEqualTo(BehandleAvvikshendelseResponse(AvvikType.ENDRE_FAGOMRADE))
                },
            )
        }
    }

    @Nested
    @DisplayName("sak journal")
    internal inner class SakJournal {
        @Test
        @DisplayName("skal finne Journalposter for en bidragssak")
        @Throws(IOException::class)
        fun skalFinneJournalposterForEnBidragssak() {
            // given
            val saksnr = "1001"
            val path = String.format(BidragDokumentConsumer.PATH_SAK_JOURNAL, saksnr)
            val navnResponsfil = "bdj-respons.json"

            // Bruker en fellestub for journalpost og arkiv pga identisk path. Kan evnt sette opp egne
            // WireMock-instanser for hver app, men det krever mer arbeid.
            restConsumerStub.runGetArkiv(
                path,
                HttpStatus.OK,
                RestConsumerStub.lesResponsfilSomStreng(navnResponsfil),
            )
            restConsumerStub.runGet(
                path,
                HttpStatus.OK,
                RestConsumerStub.lesResponsfilSomStreng(navnResponsfil),
            )
            restConsumerStub.runGetForsendelse(
                path,
                HttpStatus.OK,
                RestConsumerStub.lesResponsfilSomStreng(navnResponsfil),
            )

            // when
            val listeMedJournalposterResponse = httpHeaderTestRestTemplate
                .getForEntity<List<JournalpostDto>>(lagUrlForFagomradeBid(path))

            // then
            Assertions.assertThat(listeMedJournalposterResponse)
                .satisfies(
                    {
                        assertAll(
                            {
                                Assertions.assertThat(it.statusCode).`as`("status")
                                    .isEqualTo(HttpStatus.OK)
                            },
                            // henter to journalposter fra journalpost og to fra arkiv (samme respons)
                            { Assertions.assertThat(it.body).`as`("body").hasSize(6) },
                        )
                    },
                )
        }

        @Test
        @Throws(IOException::class)
        fun skalBareHenteJournalposterMedFarskapUtelukket() {
            // given
            val saksnr = "1001"
            val path = String.format(BidragDokumentConsumer.PATH_SAK_JOURNAL, saksnr)
            val navnResponsfil = "bdj-respons.json"
            val navnResponsfilFarskapUtelukket = "respons_farskap_utelukket.json"

            // Bruker en fellestub for journalpost og arkiv pga identisk path. Kan evnt sette opp egne
            // WireMock-instanser for hver app, men det krever mer arbeid.
            restConsumerStub.runGetArkiv(
                path,
                HttpStatus.OK,
                RestConsumerStub.lesResponsfilSomStreng(navnResponsfil),
            )
            restConsumerStub.runGet(
                path,
                HttpStatus.OK,
                RestConsumerStub.lesResponsfilSomStreng(navnResponsfilFarskapUtelukket),
            )
            restConsumerStub.runGetForsendelse(
                path,
                HttpStatus.OK,
                RestConsumerStub.lesResponsfilSomStreng(navnResponsfil),
            )

            // when
            val listeMedJournalposterResponse = httpHeaderTestRestTemplate
                .getForEntity<List<JournalpostDto>>(lagUrlForFagomradeBid(path, true))

            // then
            Assertions.assertThat(listeMedJournalposterResponse)
                .satisfies(
                    {
                        assertAll(
                            {
                                Assertions.assertThat(it.statusCode).`as`("status")
                                    .isEqualTo(HttpStatus.OK)
                            },
                            // henter to journalposter fra journalpost og to fra arkiv (samme respons)
                            { Assertions.assertThat(it.body).`as`("body").hasSize(2) },
                        )
                    },
                )
        }

        @Test
        @Throws(IOException::class)
        fun skalIkkeHenteJournalposterMedFarskapUtelukket() {
            // given
            val saksnr = "1001"
            val path = String.format(BidragDokumentConsumer.PATH_SAK_JOURNAL, saksnr)
            val navnResponsfil = "bdj-respons.json"
            val navnResponsfilFarskapUtelukket = "respons_farskap_utelukket.json"

            // Bruker en fellestub for journalpost og arkiv pga identisk path. Kan evnt sette opp egne
            // WireMock-instanser for hver app, men det krever mer arbeid.
            restConsumerStub.runGetArkiv(
                path,
                HttpStatus.OK,
                RestConsumerStub.lesResponsfilSomStreng(navnResponsfil),
            )
            restConsumerStub.runGet(
                path,
                HttpStatus.OK,
                RestConsumerStub.lesResponsfilSomStreng(navnResponsfilFarskapUtelukket),
            )
            restConsumerStub.runGetForsendelse(
                path,
                HttpStatus.OK,
                RestConsumerStub.lesResponsfilSomStreng(navnResponsfil),
            )

            // when
            val listeMedJournalposterResponse = httpHeaderTestRestTemplate
                .getForEntity<List<JournalpostDto>>(lagUrlForFagomradeBid(path, false))

            // then
            Assertions.assertThat(listeMedJournalposterResponse)
                .satisfies(
                    {
                        assertAll(
                            {
                                Assertions.assertThat(it.statusCode).`as`("status")
                                    .isEqualTo(HttpStatus.OK)
                            },
                            // henter to journalposter fra journalpost og to fra arkiv (samme respons)
                            { Assertions.assertThat(it.body).`as`("body").hasSize(7) },
                        )
                    },
                )
        }

        @Test
        @DisplayName("skal få BAD_REQUEST(400) som statuskode når saksnummer ikke er et heltall")
        fun skalFaBadRequestNarSaksnummerIkkeErHeltall() {
            val journalposterResponse = httpHeaderTestRestTemplate
                .getForEntity<List<JournalpostDto>>(lagUrlForFagomradeBid("/sak/xyz/journal"))
            Assertions.assertThat(journalposterResponse).satisfies(
                {
                    assertAll(
                        { Assertions.assertThat(it.statusCode).isEqualTo(HttpStatus.BAD_REQUEST) },
                        { Assertions.assertThat(it.body).isNull() },
                    )
                },
            )
        }

        @Test
        @DisplayName("skal hente sakjournal fra bidrag-dokument-arkiv såfremt bidrag-dokument-journalpost")
        @Throws(
            IOException::class,
        )
        fun skalHenteSakJournalFraBidragDokumentArkiv() {
            // given
            val saksnr = "2020001"
            val path = String.format(BidragDokumentConsumer.PATH_SAK_JOURNAL, saksnr)
            val navnResponsfil = "bdj-respons.json"

            // Bruker en fellestub for journalpost og arkiv pga identisk path. Kan evnt sette opp egne
            // WireMock-instanser for hver app, men det krever mer arbeid.
            restConsumerStub.runGetArkiv(
                path,
                HttpStatus.OK,
                RestConsumerStub.lesResponsfilSomStreng(navnResponsfil),
            )
            restConsumerStub.runGet(
                path,
                HttpStatus.OK,
                RestConsumerStub.lesResponsfilSomStreng(navnResponsfil),
            )
            restConsumerStub.runGetForsendelse(
                path,
                HttpStatus.OK,
                RestConsumerStub.lesResponsfilSomStreng(navnResponsfil),
            )
            val listeMedJournalposterResponse =
                httpHeaderTestRestTemplate.getForEntity<List<JournalpostDto>>(
                    lagUrlForFagomradeBid(path),
                )
            Assertions.assertThat(listeMedJournalposterResponse.body)
                .hasSize(6) //  skal kalle også kalle bidrag-dokument-arkiv
        }

        private fun lagUrlForFagomradeBid(path: String, farskapUtelukket: Boolean = false): String {
            return UriComponentsBuilder.fromHttpUrl("http://localhost:$localServerPort/bidrag-dokument$path")
                .queryParam("fagomrade", "BID")
                .queryParam("bareFarskapUtelukket", if (farskapUtelukket) "true" else "false")
                .toUriString()
        }
    }

    companion object {
        private const val PATH_JOURNALPOST_UTEN_SAK = "/journal/"
    }
}
