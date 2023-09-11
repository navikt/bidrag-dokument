package no.nav.bidrag.dokument

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nav.bidrag.commons.web.HttpResponse.Companion.from
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.dokument.consumer.BidragDokumentConsumer.Companion.createEnhetHeader
import no.nav.bidrag.dokument.service.JournalpostService
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [BidragDokumentTest::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(BidragDokumentTest.TEST_PROFILE)
@DisplayName("EnhetFilter")
@EnableMockOAuth2Server
internal class EnhetFilterFilterTest {

    @Autowired
    private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

    @MockkBean(relaxed = true)
    private lateinit var appenderMock: Appender<ILoggingEvent>

    @MockkBean
    private lateinit var journalpostServiceMock: JournalpostService

    @LocalServerPort
    private val port = 0

    @BeforeEach
    fun mockLogAppender() {
        val logger = LoggerFactory.getLogger(
            org.slf4j.Logger.ROOT_LOGGER_NAME,
        ) as Logger
        every { appenderMock.name } returns "MOCK"
        every { appenderMock.isStarted } returns true
        logger.addAppender(appenderMock)
    }

    @Test
    @DisplayName("skal logge requests mot applikasjonen som ikke inneholder enhetsinformasjon i header")
    fun skalLoggeRequestsMotApplikasjonenUtenHeaderInformasjon() {
        every {
            journalpostServiceMock.hentJournalpost(
                any(),
                any(),
            )
        } returns from(HttpStatus.I_AM_A_TEAPOT)
        val response =
            securedTestRestTemplate.getForEntity<String>("http://localhost:$port/bidrag-dokument/journal/BID-123?saksnummer=777")
        assertAll(
            { Assertions.assertThat(response).extracting { it.statusCode }.isEqualTo(HttpStatus.I_AM_A_TEAPOT) },
            {
                val loggingEventCaptor = mutableListOf<ILoggingEvent>()
                verify { appenderMock.doAppend(capture(loggingEventCaptor)) }
                val allMsgs = loggingEventCaptor.joinToString("\n") { it.formattedMessage }
                Assertions.assertThat(allMsgs).contains(
                    "Behandler request '/bidrag-dokument/journal/BID-123' uten informasjon om enhetsnummer.",
                )
            },
        )
    }

    @Test
    @DisplayName("skal logge requests mot applikasjonen som ikke inneholder enhetsinformasjon i header")
    fun skalLoggeRequestsMotApplikasjonenMedHeaderInformasjon() {
        every { journalpostServiceMock.hentJournalpost(any(), any()) } returns from(HttpStatus.I_AM_A_TEAPOT)
        val enhet = "4802"
        val htpEntity = HttpEntity<Void>(null, createEnhetHeader(enhet))
        val response =
            securedTestRestTemplate.getForEntity<String>("http://localhost:$port/bidrag-dokument/journal/BID-123?saksnummer=777", htpEntity)
        assertAll(
            {
                Assertions.assertThat(response).extracting { it.statusCode }.isEqualTo(HttpStatus.I_AM_A_TEAPOT)
            },
            {
                val loggingEventCaptor = mutableListOf<ILoggingEvent>()
                verify { appenderMock.doAppend(capture(loggingEventCaptor)) }
                val allMsgs = loggingEventCaptor.joinToString("\n") { it.formattedMessage }
                Assertions.assertThat(allMsgs).containsIgnoringCase(
                    "Behandler request '/bidrag-dokument/journal/BID-123' for enhet med enhetsnummer " +
                        enhet,
                )
            },
        )
    }
}
