package no.nav.bidrag.dokument.service;

import static no.nav.bidrag.commons.KildesystemIdenfikator.Kildesystem.BIDRAG;
import static no.nav.bidrag.dokument.BidragDokumentConfig.ARKIV_QUALIFIER;
import static no.nav.bidrag.dokument.BidragDokumentConfig.MIDL_BREVLAGER_QUALIFIER;

import java.util.ArrayList;
import java.util.List;
import no.nav.bidrag.commons.KildesystemIdenfikator;
import no.nav.bidrag.commons.web.HttpResponse;
import no.nav.bidrag.dokument.consumer.BidragDokumentConsumer;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.BehandleAvvikshendelseResponse;
import no.nav.bidrag.dokument.dto.DistribuerJournalpostRequest;
import no.nav.bidrag.dokument.dto.DistribuerJournalpostResponse;
import no.nav.bidrag.dokument.dto.DokumentRef;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.JournalpostResponse;
import no.nav.bidrag.dokument.dto.Kilde;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class JournalpostService {

  private final BidragDokumentConsumer bidragJournalpostConsumer;
  private final BidragDokumentConsumer bidragArkivConsumer;

  public JournalpostService(
      @Qualifier(ARKIV_QUALIFIER) BidragDokumentConsumer bidragArkivConsumer,
      @Qualifier(MIDL_BREVLAGER_QUALIFIER) BidragDokumentConsumer bidragJournalpostConsumer
  ) {
    this.bidragArkivConsumer = bidragArkivConsumer;
    this.bidragJournalpostConsumer = bidragJournalpostConsumer;
  }

  public HttpResponse<JournalpostResponse> hentJournalpost(DokumentRef dokumentRef, String saksnummer) {
    if (dokumentRef.erForKilde(Kilde.BIDRAG)) {
      return bidragJournalpostConsumer.hentJournalpost(saksnummer, dokumentRef.getJournalpostId());
    }
    return bidragArkivConsumer.hentJournalpost(saksnummer, dokumentRef.getJournalpostId());
  }

  public HttpResponse<JournalpostResponse> hentJournalpost(String saksnummer, KildesystemIdenfikator kildesystemIdenfikator) {
    if (kildesystemIdenfikator.erFor(BIDRAG)) {
      return bidragJournalpostConsumer.hentJournalpost(saksnummer, kildesystemIdenfikator.getPrefiksetJournalpostId());
    }
    return bidragArkivConsumer.hentJournalpost(saksnummer, kildesystemIdenfikator.getPrefiksetJournalpostId());
  }

  public HttpResponse<List<AvvikType>> finnAvvik(String saksnummer, KildesystemIdenfikator kildesystemIdenfikator) {
    if (kildesystemIdenfikator.erFor(BIDRAG)) {
      return bidragJournalpostConsumer.finnAvvik(saksnummer, kildesystemIdenfikator.getPrefiksetJournalpostId());
    }

    return bidragArkivConsumer.finnAvvik(saksnummer, kildesystemIdenfikator.getPrefiksetJournalpostId());
  }

  public HttpResponse<BehandleAvvikshendelseResponse> behandleAvvik(
      String enhet, KildesystemIdenfikator kildesystemIdenfikator, Avvikshendelse avvikshendelse
  ) {
    if (kildesystemIdenfikator.erFor(BIDRAG)) {
      return bidragJournalpostConsumer.behandleAvvik(enhet, kildesystemIdenfikator.getPrefiksetJournalpostId(), avvikshendelse);
    }

    return bidragArkivConsumer.behandleAvvik(enhet, kildesystemIdenfikator.getPrefiksetJournalpostId(), avvikshendelse);
  }

  public List<JournalpostDto> finnJournalposter(String saksnummer, String fagomrade) {
    var sakjournal = new ArrayList<>(bidragJournalpostConsumer.finnJournalposter(saksnummer, fagomrade));
    sakjournal.addAll(bidragArkivConsumer.finnJournalposter(saksnummer, fagomrade));
    return sakjournal;
  }

  public HttpResponse<Void> endre(String enhet, KildesystemIdenfikator kildesystemIdenfikator, EndreJournalpostCommand endreJournalpostCommand) {
    if (kildesystemIdenfikator.erFor(BIDRAG)) {
      return bidragJournalpostConsumer.endre(enhet, endreJournalpostCommand);
    }
    return bidragArkivConsumer.endre(enhet, endreJournalpostCommand);
  }

  public HttpResponse<DistribuerJournalpostResponse> distribuerJournalpost(String batchId, KildesystemIdenfikator kildesystemIdenfikator, DistribuerJournalpostRequest distribuerJournalpostRequest) {
    if (kildesystemIdenfikator.erFor(BIDRAG)) {
      return bidragJournalpostConsumer.distribuerJournalpost(kildesystemIdenfikator.getPrefiksetJournalpostId(), batchId, distribuerJournalpostRequest);
    }
    return bidragArkivConsumer.distribuerJournalpost(kildesystemIdenfikator.getPrefiksetJournalpostId(), batchId, distribuerJournalpostRequest);
  }

  public HttpResponse<Void> kanDistribuereJournalpost(KildesystemIdenfikator kildesystemIdenfikator) {
    if (kildesystemIdenfikator.erFor(BIDRAG)) {
      return bidragJournalpostConsumer.kanDistribuereJournalpost(kildesystemIdenfikator.getPrefiksetJournalpostId());
    }
    return bidragArkivConsumer.kanDistribuereJournalpost(kildesystemIdenfikator.getPrefiksetJournalpostId());
  }
}
