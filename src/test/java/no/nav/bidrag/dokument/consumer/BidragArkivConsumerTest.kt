package no.nav.bidrag.dokument.consumer

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import io.kotest.matchers.shouldBe
import no.nav.bidrag.dokument.BidragDokumentConfig
import no.nav.bidrag.dokument.BidragDokumentTest
import no.nav.bidrag.dokument.consumer.stub.RestConsumerStub
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles

@DisplayName("BidragArkivConsumer")
@SpringBootTest(classes = [BidragDokumentTest::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = [BidragDokumentTest.TEST_PROFILE, "mock-security"])
@EnableMockOAuth2Server
@AutoConfigureWireMock(port = 0)
internal class BidragArkivConsumerTest {
    @Autowired
    @Qualifier(BidragDokumentConfig.ARKIV_QUALIFIER)
    private val bidragArkivConsumer: BidragDokumentConsumer? = null

    @Autowired
    private val restConsumerStub: RestConsumerStub? = null

    @Test
    @DisplayName("skal hente en journalpost med spring sin RestTemplate")
    fun skalHenteJournalpostMedRestTemplate() {
        val jpId = "BID-101"
        val saksnr = "69"
        val path = String.format(BidragDokumentConsumer.PATH_JOURNALPOST_UTEN_SAK, jpId)
        val queryParams: MutableMap<String, StringValuePattern> = HashMap()
        queryParams["saksnummer"] = WireMock.equalTo(saksnr)
        val journalpostelementer: MutableMap<String, String> = HashMap()
        journalpostelementer["innhold"] = "ENDELIG"
        restConsumerStub!!.runGetArkiv(path, queryParams, HttpStatus.OK, RestConsumerStub.generereJournalpostrespons(journalpostelementer))
        val httpResponse = bidragArkivConsumer!!.hentJournalpost(saksnr, jpId)
        val (journalpost) = httpResponse.fetchBody().orElseThrow { AssertionError("BidragArkivConsumer kunne ikke finne journalpost!") }
        journalpost?.innhold shouldBe "ENDELIG"
    }
}
