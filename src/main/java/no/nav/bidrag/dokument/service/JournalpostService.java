package no.nav.bidrag.dokument.service;

import static no.nav.bidrag.commons.KildesystemIdenfikator.Kildesystem.BIDRAG;

import java.util.Collections;
import java.util.List;
import no.nav.bidrag.commons.ExceptionLogger;
import no.nav.bidrag.commons.KildesystemIdenfikator;
import no.nav.bidrag.commons.web.HttpStatusResponse;
import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.OpprettAvvikshendelseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class JournalpostService {

  private final BidragJournalpostConsumer bidragJournalpostConsumer;
  private final BidragArkivConsumer bidragArkivConsumer;
  private final ExceptionLogger exceptionLogger;

  public JournalpostService(
      BidragArkivConsumer bidragArkivConsumer,
      BidragJournalpostConsumer bidragJournalpostConsumer,
      ExceptionLogger exceptionLogger
  ) {
    this.bidragArkivConsumer = bidragArkivConsumer;
    this.bidragJournalpostConsumer = bidragJournalpostConsumer;
    this.exceptionLogger = exceptionLogger;
  }

  public HttpStatusResponse<JournalpostDto> hentJournalpost(String saksnummer, KildesystemIdenfikator kildesystemIdenfikator) {
    if (BIDRAG.er(kildesystemIdenfikator.hentKildesystem())) {
      return bidragJournalpostConsumer.hentJournalpost(saksnummer, kildesystemIdenfikator.getPrefiksetJournalpostId());
    }

    return bidragArkivConsumer.hentJournalpost(saksnummer, kildesystemIdenfikator.getPrefiksetJournalpostId());
  }

  public HttpStatusResponse<List<AvvikType>> finnAvvik(String saksnummer, KildesystemIdenfikator kildesystemIdenfikator) {
    if (BIDRAG.er(kildesystemIdenfikator.hentKildesystem())) {
      return bidragJournalpostConsumer.finnAvvik(saksnummer, kildesystemIdenfikator.getPrefiksetJournalpostId());
    }

    return new HttpStatusResponse<>(HttpStatus.BAD_REQUEST, Collections.emptyList());
  }

  public HttpStatusResponse<OpprettAvvikshendelseResponse> opprettAvvik(
      String saksnummer, KildesystemIdenfikator kildesystemIdenfikator,
      Avvikshendelse avvikshendelse
  ) {
    if (BIDRAG.er(kildesystemIdenfikator.hentKildesystem())) {
      return bidragJournalpostConsumer.opprettAvvik(saksnummer, kildesystemIdenfikator.getPrefiksetJournalpostId(), avvikshendelse);
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

  private List<JournalpostDto> handle(Throwable throwable) {
    if (throwable instanceof RuntimeException) {
      throw (RuntimeException) throwable;
    }

    exceptionLogger.logException(throwable, "JournalpostService");

    return Collections.emptyList();
  }

  public HttpStatusResponse<JournalpostDto> endre(String saksnummer, EndreJournalpostCommand endreJournalpostCommand) {
    return bidragJournalpostConsumer.endre(saksnummer, endreJournalpostCommand);
  }
}
