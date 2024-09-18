package no.nav.bidrag.dokument.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import no.nav.bidrag.commons.util.KildesystemIdenfikator
import no.nav.bidrag.commons.web.HttpResponse
import no.nav.bidrag.dokument.BidragDokumentConfig
import no.nav.bidrag.dokument.consumer.BidragDokumentConsumer
import no.nav.bidrag.transport.dokument.JournalpostDto
import no.nav.bidrag.transport.dokument.JournalpostResponse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles

@DisplayName("JournalpostService")
@ExtendWith(MockKExtension::class)
@ActiveProfiles("test")
internal class JournalpostServiceTest {
    @MockK(name = BidragDokumentConfig.ARKIV_QUALIFIER)
    lateinit var bidragArkivConsumer: BidragDokumentConsumer

    @MockK(name = BidragDokumentConfig.MIDL_BREVLAGER_QUALIFIER)
    lateinit var bidragJournalpostConsumer: BidragDokumentConsumer

    @MockK(name = BidragDokumentConfig.FORSENDELSE_QUALIFIER)
    lateinit var bidragForsendelseConsumer: BidragDokumentConsumer

    lateinit var journalpostService: JournalpostService

    @BeforeEach
    fun init() {
        journalpostService = JournalpostService(
            bidragForsendelseConsumer,
            bidragArkivConsumer,
            bidragJournalpostConsumer,
        )
    }

    @Test
    @DisplayName("skal ikke hente journalpost")
    fun skalIkkeHenteJournalpostGittId() {
        every { bidragArkivConsumer.hentJournalpost(any(), any()) } returns HttpResponse.from(
            HttpStatus.NO_CONTENT,
        )
        val httpStatusResponse =
            journalpostService.hentJournalpost("69", KildesystemIdenfikator("joark-2"))
        Assertions.assertThat(httpStatusResponse.body).isNull()
    }

    @Test
    @DisplayName("skal hente journalpost gitt id")
    fun skalHenteJournalpostGittId() {
        every { bidragArkivConsumer.hentJournalpost(any(), any()) } returns HttpResponse.from(
            HttpStatus.OK,
            JournalpostResponse(),
        )
        val httpStatusResponse =
            journalpostService.hentJournalpost("69", KildesystemIdenfikator("joark-3"))
        Assertions.assertThat(httpStatusResponse.body).isNotNull()
    }

    @Test
    @DisplayName("skal kombinere resultat fra BidragDokumentJournalpostConsumer samt BidragDokumentArkivConsumer")
    suspend fun skalKombinereResultaterFraJournalpostOgArkiv() {
        every { bidragArkivConsumer.finnJournalposter("1", listOf("FAG")) } returns listOf(
            JournalpostDto(),
        )
        every { bidragForsendelseConsumer.finnJournalposter("1", listOf("FAG")) } returns listOf(
            JournalpostDto(),
        )
        every { bidragJournalpostConsumer.finnJournalposter("1", listOf("FAG")) } returns listOf(
            JournalpostDto(),
        )

        val journalposter = journalpostService.finnJournalposter("1", listOf("FAG"))
        org.junit.jupiter.api.Assertions.assertAll(
            Executable { Assertions.assertThat(journalposter).hasSize(3) },
            Executable {
                verify {
                    bidragJournalpostConsumer.finnJournalposter(
                        "1",
                        listOf("FAG"),
                    )
                }
            },
        )
    }
}
