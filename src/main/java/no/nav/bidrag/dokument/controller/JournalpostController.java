package no.nav.bidrag.dokument.controller;

import static no.nav.bidrag.commons.web.EnhetFilter.X_ENHET_HEADER;
import static no.nav.bidrag.commons.web.WebUtil.initHttpHeadersWith;
import static no.nav.bidrag.dokument.BidragDokumentConfig.DELIMTER;
import static no.nav.bidrag.dokument.BidragDokumentConfig.ISSUER;
import static no.nav.bidrag.dokument.BidragDokumentConfig.PREFIX_BIDRAG;
import static no.nav.bidrag.dokument.BidragDokumentConfig.PREFIX_JOARK;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import no.nav.bidrag.commons.KildesystemIdenfikator;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.JournalpostResponse;
import no.nav.bidrag.dokument.dto.OpprettAvvikshendelseResponse;
import no.nav.bidrag.dokument.service.JournalpostService;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ProtectedWithClaims(issuer = ISSUER)
public class JournalpostController {

  private static final Logger LOGGER = LoggerFactory.getLogger(JournalpostController.class);
  private static final String NON_DIGITS = "\\D+";

  private final JournalpostService journalpostService;

  public JournalpostController(JournalpostService journalpostService) {
    this.journalpostService = journalpostService;
  }

  @GetMapping("/sak/{saksnummer}/journal")
  @Operation(summary = "Finn saksjournal for et saksnummer, samt parameter 'fagomrade' (FAR - farskapsjournal) og (BID - bidragsjournal)")
//  @ApiResponses(value = {
//      @ApiResponse(code = 200, message = "Fant journalposter for saksnummer"),
//      @ApiResponse(code = 204, message = "Ingen journalposter for saksnummer"),
//      @ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
//      @ApiResponse(code = 403, message = "Saksbehandler har ikke tilgang til aktuell journalpost"),
//  })
  public ResponseEntity<List<JournalpostDto>> hentJournal(
      @PathVariable String saksnummer,
      @RequestParam String fagomrade
  ) {

    LOGGER.info("request: bidrag-dokument/sak/{}?fagomrade={}", saksnummer, fagomrade);

    if (saksnummer.matches(NON_DIGITS)) {
      LOGGER.warn("Ugyldig saksnummer: {}", saksnummer);
      return new ResponseEntity<>(initHttpHeadersWith(HttpHeaders.WARNING, "Ugyldig saksnummer"), HttpStatus.BAD_REQUEST);
    }

    var journalposter = journalpostService.finnJournalposter(saksnummer, fagomrade);

    return new ResponseEntity<>(journalposter, journalposter.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK);
  }

  @GetMapping("/journal/{journalpostIdForKildesystem}")
  @Operation(summary = "Hent en journalpost for en id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER + "<journalpostId>")
//  @ApiResponses(value = {
//      @ApiResponse(code = 200, message = "Journalpost er hentet"),
//      @ApiResponse(code = 400, message = "Ukjent/ugyldig journalpostId som har/mangler prefix"),
//      @ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
//      @ApiResponse(code = 403, message = "Saksbehandler har ikke tilgang til aktuell journalpost"),
//      @ApiResponse(code = 404, message = "Journalposten som skal hentes eksisterer ikke eller det er feil prefix/id på journalposten")
//  })
  public ResponseEntity<JournalpostResponse> hentJournalpost(
      @PathVariable String journalpostIdForKildesystem,
      @Parameter(name = "saksnummer", description = "journalposten tilhører sak") @RequestParam(required = false) String saksnummer
  ) {
    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostIdForKildesystem);
    if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
      return new ResponseEntity<>(initHttpHeadersWith(HttpHeaders.WARNING, "Ugyldig prefix på journalpostId"), HttpStatus.BAD_REQUEST);
    }

    LOGGER.info("request: bidrag-dokument/journal/{}?saksnummer={}", journalpostIdForKildesystem, saksnummer);

    return journalpostService.hentJournalpost(saksnummer, kildesystemIdenfikator).getResponseEntity();
  }

  @GetMapping("/journal/{journalpostIdForKildesystem}/avvik")
  @Operation(summary = "Henter mulige avvik for en journalpost, id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER
      + "<journalpostId>")
//  @ApiResponses(value = {
//      @ApiResponse(code = 200, message = "Tilgjengelig avvik for journalpost er hentet"),
//      @ApiResponse(code = 204, message = "Ingen tilgjengelige avvik for journalpost eller journalposten er for annen sak"),
//      @ApiResponse(code = 401, message = "Du mangler sikkerhetstoken"),
//      @ApiResponse(code = 403, message = "Sikkerhetstoken er ikke gyldig"),
//      @ApiResponse(code = 404, message = "Fant ikke journalpost som det skal hentes avvik på")
//  })
  public ResponseEntity<List<AvvikType>> hentAvvik(
      @PathVariable String journalpostIdForKildesystem,
      @Parameter(name = "saksnummer", description = "journalposten tilhører sak") @RequestParam(required = false) String saksnummer
  ) {
    LOGGER.info("request: bidrag-dokument/journal/{}/avvik", journalpostIdForKildesystem);

    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostIdForKildesystem);
    if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
      return new ResponseEntity<>(initHttpHeadersWith(HttpHeaders.WARNING, "Ugyldig prefix på journalpostId"), HttpStatus.BAD_REQUEST);
    }

    return journalpostService.finnAvvik(saksnummer, kildesystemIdenfikator).getResponseEntity();
  }

  @PostMapping(value = "/journal/{journalpostIdForKildesystem}/avvik", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Lagrer et avvik for en journalpost, id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER
      + "<journalpostId>")
//  @ApiResponses(value = {
//      @ApiResponse(code = 200, message = "Avvik på journalpost er behandlet"),
//      @ApiResponse(code = 201, message = "Opprettet oppgave for behandlet avvik"),
//      @ApiResponse(code = 400, message = "Ugyldig id, avvikstype mangler i avvikshendelsen, enhetsnummer ugyldig (i header) eller behandling er ugyldig"),
//      @ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
//      @ApiResponse(code = 404, message = "Fant ikke journalpost som det skal lages avvik på eller feil prefix/id på journalposten"),
//      @ApiResponse(code = 503, message = "Oppretting av oppgave for avviket feilet")
//  })
  public ResponseEntity<OpprettAvvikshendelseResponse> opprettAvvik(
      @RequestHeader(X_ENHET_HEADER) String enhet,
      @PathVariable String journalpostIdForKildesystem,
      @RequestBody Avvikshendelse avvikshendelse
  ) {
    LOGGER.info("opprett: bidrag-dokument/journal/{}/avvik - {}", journalpostIdForKildesystem, avvikshendelse);

    try {
      AvvikType.valueOf(avvikshendelse.getAvvikType());
    } catch (Exception e) {
      var message = String.format(
          "Ukjent avvikstype: %s, exception: %s: %s", avvikshendelse.getAvvikType(), e.getClass().getSimpleName(), e.getMessage()
      );

      LOGGER.warn(message);
      return new ResponseEntity<>(initHttpHeadersWith(HttpHeaders.WARNING, message), HttpStatus.BAD_REQUEST);
    }

    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostIdForKildesystem);
    if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
      return new ResponseEntity<>(initHttpHeadersWith(HttpHeaders.WARNING, "Ugyldig prefix på journalpostId"), HttpStatus.BAD_REQUEST);
    }

    return journalpostService.opprettAvvik(enhet, kildesystemIdenfikator, avvikshendelse).getResponseEntity();
  }

  @PutMapping("/journal/{journalpostIdForKildesystem}")
  @Operation(summary = "Endre eksisterende journalpost, id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER + "<journalpostId>")
//  @ApiResponses(value = {
//      @ApiResponse(code = 202, message = "Journalpost er endret (eller registrert/journalført når payload inkluderer \"skalJournalfores\":\"true\")"),
//      @ApiResponse(code = 400, message = "En av følgende: "
//          + "(1) prefiks på journalpostId er ugyldig "
//          + "(2) EndreJournalpostCommandDto.gjelder er ikke satt "
//          + "(3) eksister ikke journalpost for gitt id "
//          + "(4) enhet mangler/ugyldig (fra header)"
//          + "(5) journalpost skal journalføres, men har ikke sakstilknytninger"),
//      @ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
//      @ApiResponse(code = 403, message = "Saksbehandler har ikke tilgang til aktuell journalpost/sak"),
//      @ApiResponse(code = 404, message = "Fant ikke journalpost som skal endres, ingen 'payload' eller feil prefix/id på journalposten")
//  })
  public ResponseEntity<Void> endreJournalpost(
      @RequestHeader(X_ENHET_HEADER) String enhet,
      @RequestBody EndreJournalpostCommand endreJournalpostCommand,
      @PathVariable String journalpostIdForKildesystem
  ) {

    LOGGER.info("put endret: bidrag-dokument/journal/{}\n \\-> {}", journalpostIdForKildesystem, endreJournalpostCommand);

    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostIdForKildesystem);
    if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
      return new ResponseEntity<>(initHttpHeadersWith(HttpHeaders.WARNING, "Ugyldig prefix på journalpostId"), HttpStatus.BAD_REQUEST);
    }

    endreJournalpostCommand.setJournalpostId(journalpostIdForKildesystem);

    return journalpostService.endre(enhet, endreJournalpostCommand).getResponseEntity();
  }
}
