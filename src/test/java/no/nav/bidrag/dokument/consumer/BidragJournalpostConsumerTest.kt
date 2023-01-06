package no.nav.bidrag.dokument.consumer

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import no.nav.bidrag.commons.security.service.SecurityTokenService
import no.nav.bidrag.dokument.BidragDokumentConfig
import no.nav.bidrag.dokument.BidragDokumentConfig.Companion.KLIENTNAVN_BIDRAG_DOKUMENT_ARKIV
import no.nav.bidrag.dokument.BidragDokumentConfig.Companion.KLIENTNAVN_BIDRAG_DOKUMENT_FORSENDELSE
import no.nav.bidrag.dokument.BidragDokumentConfig.Companion.KLIENTNAVN_BIDRAG_DOKUMENT_JOURNALPOST
import no.nav.bidrag.dokument.BidragDokumentTest
import no.nav.bidrag.dokument.consumer.stub.RestConsumerStub
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.devtools.remote.client.HttpHeaderInterceptor
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import java.io.IOException

@SpringBootTest(classes = [BidragDokumentTest::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = [BidragDokumentTest.TEST_PROFILE, "mock-security"])
@DisplayName("BidragJournalpostConsumer")
@AutoConfigureWireMock(port = 0)
@EnableMockOAuth2Server
@ExtendWith(MockKExtension::class)
internal class BidragJournalpostConsumerTest {
    @Autowired
    @Qualifier(BidragDokumentConfig.MIDL_BREVLAGER_QUALIFIER)
    lateinit var bidragJournalpostConsumer: BidragDokumentConsumer

    @Autowired
    lateinit var restConsumerStub: RestConsumerStub

    @Test
    @DisplayName("skal hente journalpost til en sak")
    @Throws(IOException::class)
    fun skalHenteJournalpostTilSak() {

        // given
        val saksnr = "1900000"
        restConsumerStub.runHenteJournalpostForSak(saksnr)
        // when
        val respons = bidragJournalpostConsumer.finnJournalposter(saksnr, "BID")

        // then
        Assertions.assertEquals(2, respons.size)
    }

    @Test
    @DisplayName("skal endre journalpost")
    @Throws(IOException::class)
    fun skalEndreJournalpost() {
        val (journalpostId) = endreJournalpostCommandMedId101()
        restConsumerStub.runEndreJournalpost(journalpostId, HttpStatus.OK)
        val respons = bidragJournalpostConsumer.endre("4802", endreJournalpostCommandMedId101())
        Assertions.assertTrue(respons.is2xxSuccessful)
    }

    private fun endreJournalpostCommandMedId101(): EndreJournalpostCommand {
        val endreJournalpostCommand = EndreJournalpostCommand()
        endreJournalpostCommand.journalpostId = "BID-101"
        return endreJournalpostCommand
    }
}