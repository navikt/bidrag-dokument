package no.nav.bidrag.dokument.service;

import static no.nav.bidrag.commons.KildesystemIdenfikator.Kildesystem.BIDRAG;
import static no.nav.bidrag.dokument.BidragDokumentConfig.ARKIV_QUALIFIER;
import static no.nav.bidrag.dokument.BidragDokumentConfig.MIDL_BREVLAGER_QUALIFIER;

import no.nav.bidrag.commons.KildesystemIdenfikator;
import no.nav.bidrag.commons.web.HttpResponse;
import no.nav.bidrag.dokument.consumer.BidragDokumentConsumer;
import no.nav.bidrag.dokument.consumer.DokumentTilgangConsumer;
import no.nav.bidrag.dokument.dto.DokumentTilgangResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DokumentService {

  private final BidragDokumentConsumer bidragJournalpostConsumer;
  private final BidragDokumentConsumer bidragArkivConsumer;
  private final DokumentTilgangConsumer dokumentTilgangConsumer;

  public DokumentService(
      DokumentTilgangConsumer dokumentTilgangConsumer,
      @Qualifier(ARKIV_QUALIFIER) BidragDokumentConsumer bidragArkivConsumer,
      @Qualifier(MIDL_BREVLAGER_QUALIFIER) BidragDokumentConsumer bidragJournalpostConsumer
  ) {
    this.dokumentTilgangConsumer = dokumentTilgangConsumer;
    this.bidragArkivConsumer = bidragArkivConsumer;
    this.bidragJournalpostConsumer = bidragJournalpostConsumer;
  }
  public HttpResponse<DokumentTilgangResponse> hentTilgangUrl(String journalpostId, String dokumentreferanse) {
    return dokumentTilgangConsumer.hentTilgangUrl(journalpostId, dokumentreferanse);
  }

  public ResponseEntity<byte[]> hentDokument(KildesystemIdenfikator kildesystemIdenfikator, String dokumentreferanse) {
    if (kildesystemIdenfikator.erFor(BIDRAG)) {
      return bidragJournalpostConsumer.hentDokument(kildesystemIdenfikator.hentJournalpostIdLong().toString(), dokumentreferanse);
    }
    return bidragArkivConsumer.hentDokument(kildesystemIdenfikator.hentJournalpostIdLong().toString(), dokumentreferanse);
  }
}
