package no.nav.bidrag.dokument.controller;

import static no.nav.bidrag.commons.web.EnhetFilter.X_ENHET_HEADER;
import static no.nav.bidrag.dokument.BidragDokumentConfig.DELIMTER;
import static no.nav.bidrag.dokument.BidragDokumentConfig.ISSUER;
import static no.nav.bidrag.dokument.BidragDokumentConfig.PREFIX_BIDRAG;
import static no.nav.bidrag.dokument.BidragDokumentConfig.PREFIX_JOARK;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import no.nav.bidrag.commons.KildesystemIdenfikator;
import no.nav.bidrag.commons.web.EnhetFilter;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.JournalpostResponse;
import no.nav.bidrag.dokument.dto.OpprettAvvikshendelseResponse;
import no.nav.bidrag.dokument.dto.RegistrereJournalpostCommand;
import no.nav.bidrag.dokument.service.JournalpostService;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  @ApiOperation("Finn saksjournal for et saksnummer, samt parameter 'fagomrade' (FAR - farskapsjournal) og (BID - bidragsjournal)")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Fant journalposter for saksnummer"),
      @ApiResponse(code = 204, message = "Ingen journalposter for saksnummer"),
      @ApiResponse(code = 401, message = "Du mangler sikkerhetstoken"),
      @ApiResponse(code = 403, message = "Sikkerhetstoken er ikke gyldig")
  })
  public ResponseEntity<List<JournalpostDto>> get(
      @PathVariable String saksnummer,
      @RequestParam String fagomrade
  ) {

    LOGGER.info("request: bidrag-dokument/sak/{}?fagomrade={}", saksnummer, fagomrade);

    if (saksnummer.matches(NON_DIGITS)) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    var journalposter = journalpostService.finnJournalposter(saksnummer, fagomrade);

    return new ResponseEntity<>(journalposter, journalposter.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK);
  }

  @GetMapping("/sak/{saksnummer}/journal/{journalpostIdForKildesystem}")
  @ApiOperation("Hent en journalpost for en id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER + "<journalpostId>")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Journalpost er hentet"),
      @ApiResponse(code = 204, message = "Journalpost funnet, men mangler relatert sak. Returnerer ikke data."),
      @ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      @ApiResponse(code = 403, message = "Saksbehandler har ikke tilgang til aktuell journalpost"),
      @ApiResponse(code = 404, message = "Journalposten som skal hentes eksisterer ikke eller er koblet mot annet saksnummer. Eller det er feil prefix/id på journalposten")
  })
  public ResponseEntity<JournalpostDto> hent(@PathVariable String saksnummer, @PathVariable String journalpostIdForKildesystem) {

    LOGGER.info("request: bidrag-dokument/sak/{}/journal/{}", saksnummer, journalpostIdForKildesystem);

    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostIdForKildesystem);
    if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    var journalpostDtoResponse = journalpostService.hentJournalpost(saksnummer, kildesystemIdenfikator);
    return new ResponseEntity<>(journalpostDtoResponse.getBody(), journalpostDtoResponse.getHttpStatus());
  }

  @GetMapping("/sak/{saksnummer}/journal/{journalpostIdForKildesystem}/avvik")
  @ApiOperation("Henter mulige avvik for en journalpost, id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER + "<journalpostId>")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Tilgjengelig avvik for journalpost er hentet"),
      @ApiResponse(code = 204, message = "Ingen tilgjengelige avvik for journalpost"),
      @ApiResponse(code = 401, message = "Du mangler sikkerhetstoken"),
      @ApiResponse(code = 403, message = "Sikkerhetstoken er ikke gyldig"),
      @ApiResponse(code = 404, message = "Fant ikke journalpost som det skal hentes avvik på")
  })
  public ResponseEntity<List<AvvikType>> finnAvvik(@PathVariable String saksnummer, @PathVariable String journalpostIdForKildesystem) {
    LOGGER.info("request: bidrag-dokument/sak/{}/journal/{}/avvik", saksnummer, journalpostIdForKildesystem);

    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostIdForKildesystem);
    if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    var avvikslisteRespnse = journalpostService.finnAvvik(saksnummer, kildesystemIdenfikator);
    return new ResponseEntity<>(avvikslisteRespnse.getBody(), avvikslisteRespnse.getHttpStatus());
  }

  @PostMapping(value = "/sak/{saksnummer}/journal/{journalpostIdForKildesystem}/avvik", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Lagrer et avvik for en journalpost, id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER + "<journalpostId>")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Avvik på journalpost er behandlet"),
      @ApiResponse(code = 201, message = "Opprettet oppgave for behandlet avvik"),
      @ApiResponse(code = 400, message = "Avvikstype mangler i avvikshendelsen, enhet mangler/ugyldig (fra header) eller behandling av avviket er ugyldig"),
      @ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      @ApiResponse(code = 404, message = "Fant ikke journalpost som det skal lages avvik på eller feil prefix/id på journalposten"),
      @ApiResponse(code = 503, message = "Oppretting av oppgave for avviket feilet")})
  public ResponseEntity<OpprettAvvikshendelseResponse> opprettAvvik(
      @RequestHeader(X_ENHET_HEADER) String enhet,
      @PathVariable String saksnummer,
      @PathVariable String journalpostIdForKildesystem,
      @RequestBody Avvikshendelse avvikshendelse
  ) {
    LOGGER.info("create: bidrag-dokument/sak/{}/journal/{}/avvik - {}", saksnummer, journalpostIdForKildesystem, avvikshendelse);

    try {
      AvvikType.valueOf(avvikshendelse.getAvvikType());
    } catch (Exception e) {
      LOGGER.warn("Ukjent avvikstype: {}, exception: {}: {}", avvikshendelse.getAvvikType(), e.getClass().getSimpleName(), e.getMessage());
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostIdForKildesystem);
    if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    var opprettAvvikResponse = journalpostService.opprettAvvik(saksnummer, enhet, kildesystemIdenfikator, avvikshendelse);
    return new ResponseEntity<>(opprettAvvikResponse.getBody(), opprettAvvikResponse.getHttpStatus());
  }

  @PutMapping("/sak/{saksnummer}/journal/{journalpostIdForKildesystem}")
  @ApiOperation("Endre eksisterende journalpost, id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER + "<journalpostId>")
  @ApiResponses(value = {
      @ApiResponse(code = 202, message = "Journalpost er endret"),
      @ApiResponse(code = 204, message = "Journalpost funnet, men mangler relatert sak. Returnerer ikke data."),
      @ApiResponse(code = 400, message = "Prefiks på journalpostId er ugyldig, enhet mangler/ugyldig (fra header), JournalpostEndreJournalpostCommandDto.gjelder er ikke satt, eller det ikke finnes en journalpost på gitt id"),
      @ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      @ApiResponse(code = 403, message = "Saksbehandler har ikke tilgang til aktuell journalpost"),
      @ApiResponse(code = 404, message = "Fant ikke journalpost som skal endres, ingen 'payload' eller feil prefix/id på journalposten")
  })
  public ResponseEntity<Void> put(
      @RequestHeader(X_ENHET_HEADER) String enhet,
      @PathVariable String saksnummer,
      @RequestBody EndreJournalpostCommand endreJournalpostCommand,
      @PathVariable String journalpostIdForKildesystem
  ) {

    LOGGER.info("put endret: bidrag-dokument/sak/{}/journal/{}\n \\-> {}", saksnummer, journalpostIdForKildesystem, endreJournalpostCommand);

    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostIdForKildesystem);
    if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    endreJournalpostCommand.setJournalpostId(journalpostIdForKildesystem);

    var endreResponse = journalpostService.endre(saksnummer, enhet, endreJournalpostCommand);
    return new ResponseEntity<>(endreResponse.getHttpStatus());
  }

  @GetMapping("/journal/{journalpostIdForKildesystem}")
  @ApiOperation(
      "Hent en journalpost for en id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER + "<journalpostId>"
  )
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Journalpost er hentet"),
      @ApiResponse(code = 400, message = "Ukjent/ugyldig journalpostId som har/mangler prefix"),
      @ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      @ApiResponse(code = 403, message = "Saksbehandler har ikke tilgang til aktuell journalpost"),
      @ApiResponse(code = 404, message = "Journalposten som skal hentes eksisterer ikke")
  })
  public ResponseEntity<JournalpostResponse> hentJournalpost(@PathVariable String journalpostIdForKildesystem) {
    LOGGER.info("GET: /journal/{}", journalpostIdForKildesystem);

    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostIdForKildesystem);
    if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    var journalpostDtoResponse = journalpostService.hentJournalpostResponse(kildesystemIdenfikator);
    return new ResponseEntity<>(journalpostDtoResponse.getBody(), journalpostDtoResponse.getHttpStatus());
  }

  @GetMapping("/journal/{journalpostIdForKildesystem}/avvik")
  @ApiOperation(
      "Finner mulige avvik for en journalpost med status mottaksregistrert, id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER
          + "<journalpostId>")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Tilgjengelig avvik for journalpost er hentet"),
      @ApiResponse(code = 204, message = "Ingen tilgjengelige avvik for journalpost eller journalposten har ikke status mottaksregistrert"),
      @ApiResponse(code = 400, message = "Ukjent prefix på journalpost id"),
      @ApiResponse(code = 401, message = "Du mangler sikkerhetstoken"),
      @ApiResponse(code = 403, message = "Sikkerhetstoken er ikke gyldig"),
      @ApiResponse(code = 404, message = "Fant ikke journalpost som det skal hentes avvik på")
  })
  public ResponseEntity<List<AvvikType>> finnAvvikPaJournalpostMedStatusMottaksregistrert(@PathVariable String journalpostIdForKildesystem) {
    LOGGER.info("GET: /journal/{}/avvik", journalpostIdForKildesystem);

    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostIdForKildesystem);
    if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    var avvikslisteResponse = journalpostService.finnAvvik(kildesystemIdenfikator);
    return new ResponseEntity<>(avvikslisteResponse.getBody(), avvikslisteResponse.getHttpStatus());
  }

  @PutMapping("/journal/{journalpostIdForKildesystem}")
  @ApiOperation(
      "Registrere en mottaksregistrert journalpost med id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER + "<journalpostId>"
  )
  @ApiResponses(value = {
      @ApiResponse(code = 202, message = "Journalpost er registrert"),
      @ApiResponse(code = 400, message = "Det er ukjent prefix for journalpostId, det finnes ikke en journalpost på gitt id, enhet mangler/ugyldig (fra header), eller journalposten ikke har status mottaksregistrert"),
      @ApiResponse(code = 401, message = "Du mangler sikkerhetstoken"),
      @ApiResponse(code = 403, message = "Sikkerhetstoken er ikke gyldig, eller du har ikke adgang til kode 6 og 7 (nav-ansatt)")
  })
  public ResponseEntity<Void> registrerMottaksregistrertJournalpost(
      @RequestHeader(X_ENHET_HEADER) String enhet,
      @PathVariable String journalpostIdForKildesystem,
      @RequestBody RegistrereJournalpostCommand registrereJournalpostCommand
  ) {

    LOGGER.info("put: bidrag-dokument/journal/{} - {}", journalpostIdForKildesystem, registrereJournalpostCommand);

    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostIdForKildesystem);
    if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    registrereJournalpostCommand.setJournalpostId(journalpostIdForKildesystem);
    journalpostService.registrer(enhet, registrereJournalpostCommand);

    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  @PostMapping(value = "/journal/{journalpostIdForKildesystem}/avvik", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(
      "Registrere avvik på mottaksregistrert journalpost, id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER + "<journalpostId>"
  )
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Avvik på journalpost er behandlet"),
      @ApiResponse(code = 201, message = "Opprettet oppgave for behandlet avvik"),
      @ApiResponse(code = 400, message = "Ugyldig id, avvikstype mangler i avvikshendelsen, enhetsnummer ugyldig (i header) eller behandling er ugyldig"),
      @ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      @ApiResponse(code = 404, message = "Fant ikke journalpost som det skal lages avvik på eller feil prefix/id på journalposten"),
      @ApiResponse(code = 503, message = "Oppretting av oppgave for avviket feilet")
  })
  public ResponseEntity<OpprettAvvikshendelseResponse> opprettAvvikPaMottaksregistrertJournalpost(
      @PathVariable String journalpostIdForKildesystem,
      @RequestBody Avvikshendelse avvikshendelse,
      @RequestHeader(EnhetFilter.X_ENHET_HEADER) String enhetsnummer
  ) {

    LOGGER.info("post: bidrag-dokument/journal/{}/avvik - {}", journalpostIdForKildesystem, avvikshendelse);

    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostIdForKildesystem);
    if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    var response = journalpostService.opprettAvvikPaMottaksregistrertJournalpost(avvikshendelse, kildesystemIdenfikator, enhetsnummer);

    return new ResponseEntity<>(response.getBody(), response.getHttpStatus());
  }
}
