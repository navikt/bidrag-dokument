package no.nav.bidrag.dokument.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import no.nav.bidrag.commons.util.KildesystemIdenfikator
import no.nav.bidrag.commons.util.KildesystemIdenfikator.Kildesystem
import no.nav.bidrag.commons.web.HttpResponse
import no.nav.bidrag.dokument.BidragDokumentConfig
import no.nav.bidrag.dokument.consumer.BidragDokumentConsumer
import no.nav.bidrag.transport.dokument.ArkivSystem
import no.nav.bidrag.transport.dokument.AvvikType
import no.nav.bidrag.transport.dokument.Avvikshendelse
import no.nav.bidrag.transport.dokument.BehandleAvvikshendelseResponse
import no.nav.bidrag.transport.dokument.DistribuerJournalpostRequest
import no.nav.bidrag.transport.dokument.DistribuerJournalpostResponse
import no.nav.bidrag.transport.dokument.DistribusjonInfoDto
import no.nav.bidrag.transport.dokument.EndreJournalpostCommand
import no.nav.bidrag.transport.dokument.JournalpostDto
import no.nav.bidrag.transport.dokument.JournalpostId
import no.nav.bidrag.transport.dokument.JournalpostResponse
import no.nav.bidrag.transport.dokument.OpprettJournalpostRequest
import no.nav.bidrag.transport.dokument.OpprettJournalpostResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class JournalpostService(
    @Qualifier(BidragDokumentConfig.FORSENDELSE_QUALIFIER) private val bidragForsendelseConsumer: BidragDokumentConsumer,
    @Qualifier(BidragDokumentConfig.ARKIV_QUALIFIER) private val bidragArkivConsumer: BidragDokumentConsumer,
    @Qualifier(BidragDokumentConfig.MIDL_BREVLAGER_QUALIFIER) private val bidragJournalpostConsumer: BidragDokumentConsumer,
) {
    fun hentJournalpost(
        saksnummer: String?,
        kildesystemIdenfikator: KildesystemIdenfikator,
    ): HttpResponse<JournalpostResponse> {
        return if (kildesystemIdenfikator.erFor(Kildesystem.BIDRAG)) {
            bidragJournalpostConsumer.hentJournalpost(
                saksnummer,
                kildesystemIdenfikator.prefiksetJournalpostId,
            )
        } else if (kildesystemIdenfikator.erFor(Kildesystem.JOARK)) {
            bidragArkivConsumer.hentJournalpost(
                saksnummer,
                kildesystemIdenfikator.prefiksetJournalpostId,
            )
        } else {
            bidragForsendelseConsumer.hentJournalpost(
                saksnummer,
                kildesystemIdenfikator.prefiksetJournalpostId,
            )
        }
    }

    fun finnAvvik(
        saksnummer: String?,
        kildesystemIdenfikator: KildesystemIdenfikator,
    ): HttpResponse<List<AvvikType>> {
        return if (kildesystemIdenfikator.erFor(Kildesystem.BIDRAG)) {
            bidragJournalpostConsumer.finnAvvik(
                saksnummer,
                kildesystemIdenfikator.prefiksetJournalpostId,
            )
        } else if (kildesystemIdenfikator.erFor(Kildesystem.JOARK)) {
            bidragArkivConsumer.finnAvvik(
                saksnummer,
                kildesystemIdenfikator.prefiksetJournalpostId,
            )
        } else {
            bidragForsendelseConsumer.finnAvvik(
                saksnummer,
                kildesystemIdenfikator.prefiksetJournalpostId,
            )
        }
    }

    fun behandleAvvik(
        enhet: String?,
        kildesystemIdenfikator: KildesystemIdenfikator,
        avvikshendelse: Avvikshendelse?,
    ): HttpResponse<BehandleAvvikshendelseResponse> {
        return if (kildesystemIdenfikator.erFor(Kildesystem.BIDRAG)) {
            bidragJournalpostConsumer.behandleAvvik(
                enhet,
                kildesystemIdenfikator.prefiksetJournalpostId,
                avvikshendelse,
            )
        } else if (kildesystemIdenfikator.erFor(Kildesystem.JOARK)) {
            bidragArkivConsumer.behandleAvvik(
                enhet,
                kildesystemIdenfikator.prefiksetJournalpostId,
                avvikshendelse,
            )
        } else {
            bidragForsendelseConsumer.behandleAvvik(
                enhet,
                kildesystemIdenfikator.prefiksetJournalpostId,
                avvikshendelse,
            )
        }
    }

    suspend fun finnJournalposter(
        saksnummer: String,
        fagomrade: List<String> = emptyList(),
    ): List<JournalpostDto> {
        val scope =
            CoroutineScope(Dispatchers.IO + SecurityCoroutineContext() + RequestContextAsyncContext())
        return runBlocking {
            awaitAll(
                scope.async {
                    bidragJournalpostConsumer.finnJournalposter(
                        saksnummer,
                        fagomrade,
                    )
                },
                scope.async {
                    bidragArkivConsumer.finnJournalposter(saksnummer, fagomrade)
                },
                scope.async {
                    bidragForsendelseConsumer.finnJournalposter(
                        saksnummer,
                        fagomrade,
                    )
                },
            ).flatten()
        }
    }

    fun endre(
        enhet: String?,
        kildesystemIdenfikator: KildesystemIdenfikator,
        endreJournalpostCommand: EndreJournalpostCommand,
    ): HttpResponse<Void> {
        return if (kildesystemIdenfikator.erFor(Kildesystem.BIDRAG)) {
            bidragJournalpostConsumer.endre(enhet, endreJournalpostCommand)
        } else if (kildesystemIdenfikator.erFor(Kildesystem.JOARK)) {
            bidragArkivConsumer.endre(enhet, endreJournalpostCommand)
        } else {
            bidragForsendelseConsumer.endre(enhet, endreJournalpostCommand)
        }
    }

    fun opprett(
        opprettJournalpostRequest: OpprettJournalpostRequest,
        arkivSystem: ArkivSystem,
    ): HttpResponse<OpprettJournalpostResponse> {
        return if (ArkivSystem.BIDRAG == arkivSystem) {
            bidragJournalpostConsumer.opprett(opprettJournalpostRequest)
        } else {
            bidragArkivConsumer.opprett(opprettJournalpostRequest)
        }
    }

    fun distribuerJournalpost(
        batchId: String?,
        kildesystemIdenfikator: KildesystemIdenfikator,
        distribuerJournalpostRequest: DistribuerJournalpostRequest,
    ): HttpResponse<DistribuerJournalpostResponse> {
        return if (kildesystemIdenfikator.erFor(Kildesystem.BIDRAG)) {
            bidragJournalpostConsumer.distribuerJournalpost(
                kildesystemIdenfikator.prefiksetJournalpostId,
                batchId,
                distribuerJournalpostRequest,
            )
        } else if (kildesystemIdenfikator.erFor(Kildesystem.JOARK)) {
            bidragArkivConsumer.distribuerJournalpost(
                kildesystemIdenfikator.prefiksetJournalpostId,
                batchId,
                distribuerJournalpostRequest,
            )
        } else {
            bidragForsendelseConsumer.distribuerJournalpost(
                kildesystemIdenfikator.prefiksetJournalpostId,
                batchId,
                distribuerJournalpostRequest,
            )
        }
    }

    fun kanDistribuereJournalpost(kildesystemIdenfikator: KildesystemIdenfikator): HttpResponse<Void> {
        return if (kildesystemIdenfikator.erFor(Kildesystem.BIDRAG)) {
            bidragJournalpostConsumer.kanDistribuereJournalpost(kildesystemIdenfikator.prefiksetJournalpostId)
        } else if (kildesystemIdenfikator.erFor(Kildesystem.JOARK)) {
            bidragArkivConsumer.kanDistribuereJournalpost(kildesystemIdenfikator.prefiksetJournalpostId)
        } else {
            bidragForsendelseConsumer.kanDistribuereJournalpost(kildesystemIdenfikator.prefiksetJournalpostId)
        }
    }

    fun hentDistribusjonsInfo(journalpostId: JournalpostId): DistribusjonInfoDto? {
        return if (journalpostId.erSystemBidrag) {
            bidragJournalpostConsumer.hentDistribusjonsInfo(journalpostId.medSystemPrefiks!!)
        } else if (journalpostId.erSystemJoark) {
            bidragArkivConsumer.hentDistribusjonsInfo(journalpostId.medSystemPrefiks!!)
        } else {
            bidragForsendelseConsumer.hentDistribusjonsInfo(journalpostId.medSystemPrefiks!!)
        }
    }
}
