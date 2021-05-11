package no.nav.bidrag.dokument.service;

import static no.nav.bidrag.commons.KildesystemIdenfikator.Kildesystem.BIDRAG;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import no.nav.bidrag.commons.KildesystemIdenfikator;
import no.nav.bidrag.commons.web.HttpResponse;
import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.BehandleAvvikshendelseResponse;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.JournalpostResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class JournalpostService {

  private final BidragJournalpostConsumer bidragJournalpostConsumer;
  private final BidragArkivConsumer bidragArkivConsumer;

  public JournalpostService(
      BidragArkivConsumer bidragArkivConsumer,
      BidragJournalpostConsumer bidragJournalpostConsumer
  ) {
    this.bidragArkivConsumer = bidragArkivConsumer;
    this.bidragJournalpostConsumer = bidragJournalpostConsumer;
  }

  public HttpResponse<JournalpostResponse> hentJournalpost(String saksnummer, KildesystemIdenfikator kildesystemIdenfikator) {
    if (kildesystemIdenfikator.erFor(BIDRAG)) {
      return bidragJournalpostConsumer.hentJournalpostResponse(saksnummer, kildesystemIdenfikator.getPrefiksetJournalpostId());
    }

    return bidragArkivConsumer.hentJournalpost(saksnummer, kildesystemIdenfikator.getPrefiksetJournalpostId());
  }

  public HttpResponse<List<AvvikType>> finnAvvik(String saksnummer, KildesystemIdenfikator kildesystemIdenfikator) {
    if (kildesystemIdenfikator.erFor(BIDRAG)) {
      return bidragJournalpostConsumer.finnAvvik(saksnummer, kildesystemIdenfikator.getPrefiksetJournalpostId());
    }

    return HttpResponse.from(HttpStatus.BAD_REQUEST, Collections.emptyList());
  }

  public HttpResponse<BehandleAvvikshendelseResponse> behandleAvvik(
      String enhet, KildesystemIdenfikator kildesystemIdenfikator, Avvikshendelse avvikshendelse
  ) {
    if (kildesystemIdenfikator.erFor(BIDRAG)) {
      return bidragJournalpostConsumer.behandleAvvik(enhet, kildesystemIdenfikator.getPrefiksetJournalpostId(), avvikshendelse);
    }

    return HttpResponse.from(HttpStatus.BAD_REQUEST);
  }

  public List<JournalpostDto> finnJournalposter(String saksnummer, String fagomrade) {
    var sakjournal = new ArrayList<>(bidragJournalpostConsumer.finnJournalposter(saksnummer, fagomrade));
    sakjournal.addAll(bidragArkivConsumer.finnJournalposter(saksnummer, fagomrade));

    return sakjournal;
  }

  public HttpResponse<Void> endre(String enhet, EndreJournalpostCommand endreJournalpostCommand) {
    return bidragJournalpostConsumer.endre(enhet, endreJournalpostCommand);
  }
}
