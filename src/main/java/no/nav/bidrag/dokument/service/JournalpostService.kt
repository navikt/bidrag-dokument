package no.nav.bidrag.dokument.service

import no.nav.bidrag.commons.KildesystemIdenfikator
import no.nav.bidrag.commons.KildesystemIdenfikator.Kildesystem
import no.nav.bidrag.commons.web.HttpResponse
import no.nav.bidrag.dokument.BidragDokumentConfig
import no.nav.bidrag.dokument.consumer.BidragDokumentConsumer
import no.nav.bidrag.dokument.dto.ArkivSystem
import no.nav.bidrag.dokument.dto.AvvikType
import no.nav.bidrag.dokument.dto.Avvikshendelse
import no.nav.bidrag.dokument.dto.BehandleAvvikshendelseResponse
import no.nav.bidrag.dokument.dto.DistribuerJournalpostRequest
import no.nav.bidrag.dokument.dto.DistribuerJournalpostResponse
import no.nav.bidrag.dokument.dto.DokumentRef
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand
import no.nav.bidrag.dokument.dto.JournalpostDto
import no.nav.bidrag.dokument.dto.JournalpostResponse
import no.nav.bidrag.dokument.dto.Kilde
import no.nav.bidrag.dokument.dto.OpprettJournalpostRequest
import no.nav.bidrag.dokument.dto.OpprettJournalpostResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class JournalpostService(
    @Qualifier(BidragDokumentConfig.FORSENDELSE_QUALIFIER) private val bidragForsendelseConsumer: BidragDokumentConsumer,
    @Qualifier(BidragDokumentConfig.ARKIV_QUALIFIER) private val bidragArkivConsumer: BidragDokumentConsumer,
    @Qualifier(BidragDokumentConfig.MIDL_BREVLAGER_QUALIFIER) private val bidragJournalpostConsumer: BidragDokumentConsumer
) {
    fun hentJournalpost(dokumentRef: DokumentRef, saksnummer: String?): HttpResponse<JournalpostResponse> {
        return if (dokumentRef.erForKilde(Kilde.BIDRAG)) bidragJournalpostConsumer.hentJournalpost(saksnummer, dokumentRef.journalpostId)
            else if (dokumentRef.erForKilde(Kilde.JOARK)) bidragArkivConsumer.hentJournalpost(saksnummer, dokumentRef.journalpostId)
            else bidragForsendelseConsumer.hentJournalpost(saksnummer, dokumentRef.journalpostId)
    }

    fun hentJournalpost(saksnummer: String?, kildesystemIdenfikator: KildesystemIdenfikator): HttpResponse<JournalpostResponse> {
        return if (kildesystemIdenfikator.erFor(Kildesystem.BIDRAG)) bidragJournalpostConsumer.hentJournalpost(saksnummer, kildesystemIdenfikator.prefiksetJournalpostId)
        else if (kildesystemIdenfikator.erFor(Kildesystem.JOARK)) bidragArkivConsumer.hentJournalpost(saksnummer, kildesystemIdenfikator.prefiksetJournalpostId)
        else bidragForsendelseConsumer.hentJournalpost(saksnummer, kildesystemIdenfikator.prefiksetJournalpostId)
    }

    fun finnAvvik(saksnummer: String?, kildesystemIdenfikator: KildesystemIdenfikator): HttpResponse<List<AvvikType>> {
        return if (kildesystemIdenfikator.erFor(Kildesystem.BIDRAG)) bidragJournalpostConsumer.finnAvvik(saksnummer, kildesystemIdenfikator.prefiksetJournalpostId)
        else if (kildesystemIdenfikator.erFor(Kildesystem.JOARK)) bidragArkivConsumer.finnAvvik(saksnummer, kildesystemIdenfikator.prefiksetJournalpostId)
        else bidragForsendelseConsumer.finnAvvik(saksnummer, kildesystemIdenfikator.prefiksetJournalpostId)
    }

    fun behandleAvvik(
        enhet: String?, kildesystemIdenfikator: KildesystemIdenfikator, avvikshendelse: Avvikshendelse?
    ): HttpResponse<BehandleAvvikshendelseResponse> {
        return if (kildesystemIdenfikator.erFor(Kildesystem.BIDRAG)) bidragJournalpostConsumer.behandleAvvik(enhet, kildesystemIdenfikator.prefiksetJournalpostId, avvikshendelse)
        else if (kildesystemIdenfikator.erFor(Kildesystem.JOARK)) bidragArkivConsumer.behandleAvvik(enhet, kildesystemIdenfikator.prefiksetJournalpostId, avvikshendelse)
        else bidragForsendelseConsumer.behandleAvvik(enhet, kildesystemIdenfikator.prefiksetJournalpostId, avvikshendelse)
    }

    fun finnJournalposter(saksnummer: String?, fagomrade: String?): List<JournalpostDto> {
        val sakjournal = ArrayList(bidragJournalpostConsumer.finnJournalposter(saksnummer, fagomrade))
        sakjournal.addAll(bidragArkivConsumer.finnJournalposter(saksnummer, fagomrade))
        sakjournal.addAll(bidragForsendelseConsumer.finnJournalposter(saksnummer, fagomrade))
        return sakjournal
    }

    fun endre(enhet: String?, kildesystemIdenfikator: KildesystemIdenfikator, endreJournalpostCommand: EndreJournalpostCommand): HttpResponse<Void> {
        return if (kildesystemIdenfikator.erFor(Kildesystem.BIDRAG)) bidragJournalpostConsumer.endre(enhet, endreJournalpostCommand)
        else if (kildesystemIdenfikator.erFor(Kildesystem.JOARK)) bidragArkivConsumer.endre(enhet, endreJournalpostCommand)
        else bidragForsendelseConsumer.endre(enhet, endreJournalpostCommand)
    }

    fun opprett(opprettJournalpostRequest: OpprettJournalpostRequest, arkivSystem: ArkivSystem): HttpResponse<OpprettJournalpostResponse> {
        return if (ArkivSystem.BIDRAG == arkivSystem) {
            bidragJournalpostConsumer.opprett(opprettJournalpostRequest)
        } else bidragArkivConsumer.opprett(opprettJournalpostRequest)
    }

    fun distribuerJournalpost(
        batchId: String?,
        kildesystemIdenfikator: KildesystemIdenfikator,
        distribuerJournalpostRequest: DistribuerJournalpostRequest
    ): HttpResponse<DistribuerJournalpostResponse> {
        return if (kildesystemIdenfikator.erFor(Kildesystem.BIDRAG)) bidragJournalpostConsumer.distribuerJournalpost(kildesystemIdenfikator.prefiksetJournalpostId, batchId, distribuerJournalpostRequest)
        else if (kildesystemIdenfikator.erFor(Kildesystem.JOARK)) bidragArkivConsumer.distribuerJournalpost(kildesystemIdenfikator.prefiksetJournalpostId, batchId, distribuerJournalpostRequest)
        else bidragForsendelseConsumer.distribuerJournalpost(kildesystemIdenfikator.prefiksetJournalpostId, batchId, distribuerJournalpostRequest)
    }

    fun kanDistribuereJournalpost(kildesystemIdenfikator: KildesystemIdenfikator): HttpResponse<Void> {
        return if (kildesystemIdenfikator.erFor(Kildesystem.BIDRAG)) bidragJournalpostConsumer.kanDistribuereJournalpost(kildesystemIdenfikator.prefiksetJournalpostId)
        else if (kildesystemIdenfikator.erFor(Kildesystem.BIDRAG)) bidragArkivConsumer.kanDistribuereJournalpost(kildesystemIdenfikator.prefiksetJournalpostId)
        else bidragForsendelseConsumer.kanDistribuereJournalpost(kildesystemIdenfikator.prefiksetJournalpostId)
    }
}