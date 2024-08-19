package no.nav.bidrag.dokument

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import com.ninjasquad.springmockk.MockkBean
import io.kotest.assertions.assertSoftly
import io.mockk.every
import no.nav.bidrag.commons.util.KildesystemIdenfikator
import no.nav.bidrag.commons.web.HttpResponse
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.dokument.service.JournalpostService
import no.nav.bidrag.transport.dokument.JournalpostResponse
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    classes = [BidragDokumentTest::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ActiveProfiles(BidragDokumentTest.TEST_PROFILE, "mock-security")
@DisplayName("CorrelationIdFilter")
@EnableMockOAuth2Server
@Disabled("")
internal class CorrelationIdFilterTest {
    @Autowired
    private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

    @MockkBean(relaxed = true)
    lateinit var appenderMock: Appender<ILoggingEvent>

    @MockkBean
    lateinit var journalpostServiceMock: JournalpostService

    @LocalServerPort
    private val port = 0

    @BeforeEach
    fun mockLogAppender() {
        val logger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger
        every { appenderMock.name } returns "MOCK"
        every { appenderMock.isStarted } returns true
        logger.addAppender(appenderMock)
    }

    @Test
    @DisplayName("skal logge requests mot applikasjonen")
    fun skalLoggeRequestsMotApplikasjonen() {
        every {
            journalpostServiceMock.hentJournalpost(
                any(),
                any<KildesystemIdenfikator>(),
            )
        } returns HttpResponse.from(HttpStatus.I_AM_A_TEAPOT)
        val response =
            securedTestRestTemplate.getForEntity<JournalpostResponse>("http://localhost:$port/bidrag-dokument/journal/BID-123?saksnummer=777")
        assertSoftly {
            Assertions.assertThat(response).extracting { it.statusCode }
                .isEqualTo(HttpStatus.I_AM_A_TEAPOT);
            {
                val loggingEventCaptor = ArgumentCaptor.forClass(ILoggingEvent::class.java)
                Mockito.verify(appenderMock, Mockito.atLeastOnce())
                    .doAppend(loggingEventCaptor.capture())
            }
        }
    }
}
