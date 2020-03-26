package no.nav.bidrag.dokument.service;

import static no.nav.bidrag.commons.KildesystemIdenfikator.Kildesystem.BIDRAG;

import java.util.Collections;
import java.util.List;
import no.nav.bidrag.commons.KildesystemIdenfikator;
import no.nav.bidrag.commons.web.HttpStatusResponse;
import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.OpprettAvvikshendelseResponse;
import no.nav.bidrag.dokument.dto.RegistrereJournalpostCommand;
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

  public HttpStatusResponse<JournalpostDto> hentJournalpost(KildesystemIdenfikator kildesystemIdenfikator) {
    return hentJournalpost(null, kildesystemIdenfikator);
  }

  public HttpStatusResponse<JournalpostDto> hentJournalpost(String saksnummer, KildesystemIdenfikator kildesystemIdenfikator) {
    if (kildesystemIdenfikator.erFor(BIDRAG)) {
      return bidragJournalpostConsumer.hentJournalpost(saksnummer, kildesystemIdenfikator.getPrefiksetJournalpostId());
    }

    return bidragArkivConsumer.hentJournalpost(saksnummer, kildesystemIdenfikator.getPrefiksetJournalpostId());
  }

  public HttpStatusResponse<List<AvvikType>> finnAvvik(KildesystemIdenfikator kildesystemIdenfikator) {
    return finnAvvik(null, kildesystemIdenfikator);
  }

  public HttpStatusResponse<List<AvvikType>> finnAvvik(String saksnummer, KildesystemIdenfikator kildesystemIdenfikator) {
    if (kildesystemIdenfikator.erFor(BIDRAG)) {
      return bidragJournalpostConsumer.finnAvvik(saksnummer, kildesystemIdenfikator.getPrefiksetJournalpostId());
    }

    return new HttpStatusResponse<>(HttpStatus.BAD_REQUEST, Collections.emptyList());
  }

  public HttpStatusResponse<OpprettAvvikshendelseResponse> opprettAvvik(
      String saksnummer, String enhet, KildesystemIdenfikator kildesystemIdenfikator,
      Avvikshendelse avvikshendelse
  ) {
    if (kildesystemIdenfikator.erFor(BIDRAG)) {
      return bidragJournalpostConsumer.opprettAvvik(saksnummer, enhet, kildesystemIdenfikator.getPrefiksetJournalpostId(), avvikshendelse);
    }

    return new HttpStatusResponse<>(HttpStatus.BAD_REQUEST);
  }

  public List<JournalpostDto> finnJournalposter(String saksnummer, String fagomrade) {
    return bidragJournalpostConsumer.finnJournalposter(saksnummer, fagomrade);
//    List<JournalpostDto> sakjournal = new ArrayList<>(bidragJournalpostConsumer.finnJournalposter(saksnummer, fagomrade));
//    sakjournal.addAll(bidragArkivConsumer.finnJournalposter(saksnummer, fagomrade)); ingen kall mot bidrag-dokument-arkiv gjøres per dato

//    return sakjournal;
  }

  private List<JournalpostDto> journalposterFraArkiv(List<JournalpostDto> journalposter, String saksnummer, String fagomrade) {
    journalposter.addAll(bidragArkivConsumer.finnJournalposter(saksnummer, fagomrade));
    return journalposter;
  }

  private List<JournalpostDto> journalposterFraBrevlager(List<JournalpostDto> journalposter, String saksnummer, String fagomrade) {
    journalposter.addAll(bidragJournalpostConsumer.finnJournalposter(saksnummer, fagomrade));
    return journalposter;
  }

  public HttpStatusResponse<Void> endre(String saksnummer, String enhet, EndreJournalpostCommand endreJournalpostCommand) {
    return bidragJournalpostConsumer.endre(saksnummer, enhet,  endreJournalpostCommand);
  }

  public HttpStatusResponse<Void> registrer(String enhet, RegistrereJournalpostCommand registrereJournalpostCommand) {
    return bidragJournalpostConsumer.registrer(enhet, registrereJournalpostCommand);
  }
}
