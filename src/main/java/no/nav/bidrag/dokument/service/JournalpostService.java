package no.nav.bidrag.dokument.service;

import static no.nav.bidrag.dokument.KildesystemIdenfikator.Kildesystem.BIDRAG;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import no.nav.bidrag.commons.ExceptionLogger;
import no.nav.bidrag.commons.web.HttpStatusResponse;
import no.nav.bidrag.dokument.KildesystemIdenfikator;
import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommandDto;
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

  public HttpStatusResponse<JournalpostDto> hentJournalpost(KildesystemIdenfikator kildesystemIdenfikator) {
    if (BIDRAG.er(kildesystemIdenfikator.hentKildesystem())) {
      return bidragJournalpostConsumer.hentJournalpost(kildesystemIdenfikator.hentJournalpostId());
    }

    return bidragArkivConsumer.hentJournalpost(kildesystemIdenfikator.hentJournalpostId());
  }

  public HttpStatusResponse<List<AvvikType>> finnAvvik(KildesystemIdenfikator kildesystemIdenfikator) {
    if (BIDRAG.er(kildesystemIdenfikator.hentKildesystem())) {
      return bidragJournalpostConsumer.finnAvvik(kildesystemIdenfikator.hentJournalpostId());
    }

    return new HttpStatusResponse<>(HttpStatus.BAD_REQUEST, Collections.emptyList());
  }

  public HttpStatusResponse<OpprettAvvikshendelseResponse> opprettAvvik(
      KildesystemIdenfikator kildesystemIdenfikator,
      Avvikshendelse avvikshendelse
  ) {
    if (BIDRAG.er(kildesystemIdenfikator.hentKildesystem())) {
      return bidragJournalpostConsumer.opprettAvvik(kildesystemIdenfikator.hentJournalpostId(), avvikshendelse);
    }

    return new HttpStatusResponse<>(HttpStatus.BAD_REQUEST, null);
  }

  public List<JournalpostDto> finnJournalposter(String saksnummer, String fagomrade) {
    List<JournalpostDto> sakjournal = new ArrayList<>(bidragJournalpostConsumer.finnJournalposter(saksnummer, fagomrade));
    sakjournal.addAll(bidragArkivConsumer.finnJournalposter(saksnummer, fagomrade));

    return sakjournal;

//    CompletableFuture<List<JournalpostDto>> sakjournal = CompletableFuture.completedFuture((List<JournalpostDto>) new ArrayList<JournalpostDto>())
//        .thenApplyAsync(journalposter -> journalposterFraArkiv(journalposter, saksnummer, fagomrade)).exceptionally(this::handle)
//        .thenApplyAsync(journalposter -> journalposterFraBrevlager(journalposter, saksnummer, fagomrade)).exceptionally(this::handle);
//
//    try {
//      return sakjournal.get();
//    } catch (InterruptedException | ExecutionException e) {
//      throw new IllegalStateException("Kunne ikke hente sakjournal fra arkiv og brevlager", e);
//    }
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

  public HttpStatusResponse<JournalpostDto> endre(EndreJournalpostCommandDto endreJournalpostCommandDto) {
    return bidragJournalpostConsumer.endre(endreJournalpostCommandDto);
  }
}
