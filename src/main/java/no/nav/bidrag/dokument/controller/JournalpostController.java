package no.nav.bidrag.dokument.controller;

import static no.nav.bidrag.commons.web.EnhetFilter.X_ENHET_HEADER;
import static no.nav.bidrag.commons.web.WebUtil.initHttpHeadersWith;
import static no.nav.bidrag.dokument.BidragDokumentConfig.DELIMTER;
import static no.nav.bidrag.dokument.BidragDokumentConfig.PREFIX_BIDRAG;
import static no.nav.bidrag.dokument.BidragDokumentConfig.PREFIX_JOARK;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import no.nav.bidrag.commons.KildesystemIdenfikator;
import no.nav.bidrag.commons.web.EnhetFilter;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.BehandleAvvikshendelseResponse;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.JournalpostResponse;
import no.nav.bidrag.dokument.service.JournalpostService;
import no.nav.security.token.support.core.api.Protected;
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
@Protected
public class JournalpostController {

  private static final Logger LOGGER = LoggerFactory.getLogger(JournalpostController.class);
  private static final String NON_DIGITS = "\\D+";

  private final JournalpostService journalpostService;

  public JournalpostController(JournalpostService journalpostService) {
    this.journalpostService = journalpostService;
  }

  @GetMapping("/sak/{saksnummer}/journal")
  @Operation(
      security = {@SecurityRequirement(name = "bearer-key")},
      description = "Finn saksjournal for et saksnummer, samt parameter 'fagomrade' (FAR - farskapsjournal) og (BID - bidragsjournal)"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Fant journalposter for saksnummer"),
      @ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      @ApiResponse(responseCode = "403", description = "Saksbehandler har ikke tilgang til aktuell journalpost"),})
  public ResponseEntity<List<JournalpostDto>> hentJournal(@PathVariable String saksnummer, @RequestParam String fagomrade) {

    LOGGER.info("request: bidrag-dokument/sak/{}?fagomrade={}", saksnummer, fagomrade);

    if (saksnummer.matches(NON_DIGITS)) {
      LOGGER.warn("Ugyldig saksnummer: {}", saksnummer);
      return new ResponseEntity<>(initHttpHeadersWith(HttpHeaders.WARNING, "Ugyldig saksnummer"), HttpStatus.BAD_REQUEST);
    }

    var journalposter = journalpostService.finnJournalposter(saksnummer, fagomrade);

    return new ResponseEntity<>(journalposter, HttpStatus.OK);
  }

  @GetMapping("/journal/{journalpostIdForKildesystem}")
  @Operation(
      security = {@SecurityRequirement(name = "bearer-key")},
      description = "Hent en journalpost for en id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER + "<journalpostId>"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Journalpost er hentet"),
      @ApiResponse(responseCode = "400", description = "Ukjent/ugyldig journalpostId som har/mangler prefix"),
      @ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      @ApiResponse(responseCode = "403", description = "Saksbehandler har ikke tilgang til aktuell journalpost"),
      @ApiResponse(responseCode = "404", description = "Journalposten som skal hentes eksisterer ikke eller det er feil prefix/id på journalposten")
  })
  public ResponseEntity<JournalpostResponse> hentJournalpost(@PathVariable String journalpostIdForKildesystem,
      @Parameter(name = "saksnummer", description = "journalposten tilhører sak") @RequestParam(required = false) String saksnummer) {
    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostIdForKildesystem);
    if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
      return new ResponseEntity<>(initHttpHeadersWith(HttpHeaders.WARNING, "Ugyldig prefix på journalpostId"), HttpStatus.BAD_REQUEST);
    }

    LOGGER.info("request: bidrag-dokument/journal/{}?saksnummer={}", journalpostIdForKildesystem, saksnummer);

    return journalpostService.hentJournalpost(saksnummer, kildesystemIdenfikator).getResponseEntity();
  }

  @GetMapping("/journal/{journalpostIdForKildesystem}/avvik")
  @Operation(
      security = {@SecurityRequirement(name = "bearer-key")},
      description = "Henter mulige avvik for en journalpost, id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER
          + "<journalpostId>"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tilgjengelig avvik for journalpost er hentet"),
      @ApiResponse(responseCode = "401", description = "Du mangler sikkerhetstoken"),
      @ApiResponse(responseCode = "403", description = "Sikkerhetstoken er ikke gyldig"),
      @ApiResponse(responseCode = "404", description = "Fant ikke journalpost som det skal hentes avvik på")
  })
  public ResponseEntity<List<AvvikType>> hentAvvik(@PathVariable String journalpostIdForKildesystem,
      @Parameter(name = "saksnummer", description = "journalposten tilhører sak") @RequestParam(required = false) String saksnummer) {
    LOGGER.info("request: bidrag-dokument/journal/{}/avvik", journalpostIdForKildesystem);

    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostIdForKildesystem);
    if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
      return new ResponseEntity<>(initHttpHeadersWith(HttpHeaders.WARNING, "Ugyldig prefix på journalpostId"), HttpStatus.BAD_REQUEST);
    }

    return journalpostService.finnAvvik(saksnummer, kildesystemIdenfikator).getResponseEntity();
  }

  @PostMapping(value = "/journal/{journalpostIdForKildesystem}/avvik", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      security = {@SecurityRequirement(name = "bearer-key")},
      description = "Lagrer et avvik for en journalpost, id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER + "<journalpostId>"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Avvik på journalpost er behandlet"),
      @ApiResponse(responseCode = "400", description = """
          En av følgende:
          - prefiks på journalpostId er ugyldig
          - avvikstypen mangler i avvikshendelsen
          - enhetsnummer i header (X_ENHET) mangler
          - ugyldig behandling av avvikshendelse (som bla. inkluderer):
            - oppretting av oppgave feiler
            - BESTILL_SPLITTING: beskrivelse må være i avvikshendelsen
            - OVERFOR_TIL_ANNEN_ENHET: nyttEnhetsnummer og gammeltEnhetsnummer må være i detaljer map
          """),
      @ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      @ApiResponse(responseCode = "404", description = "Fant ikke journalpost som det skal lages avvik på eller feil prefix/id på journalposten"),
      @ApiResponse(responseCode = "503", description = "Oppretting av oppgave for avviket feilet")
  })
  public ResponseEntity<BehandleAvvikshendelseResponse> behandleAvvik(
      @RequestHeader(X_ENHET_HEADER) String enhet,
      @PathVariable String journalpostIdForKildesystem,
      @RequestBody Avvikshendelse avvikshendelse
  ) {
    LOGGER.info("opprett: bidrag-dokument/journal/{}/avvik - {}", journalpostIdForKildesystem, avvikshendelse);

    try {
      AvvikType.valueOf(avvikshendelse.getAvvikType());
    } catch (Exception e) {
      var message = String
          .format("Ukjent avvikstype: %s, exception: %s: %s", avvikshendelse.getAvvikType(), e.getClass().getSimpleName(), e.getMessage());

      LOGGER.warn(message);
      return new ResponseEntity<>(initHttpHeadersWith(HttpHeaders.WARNING, message), HttpStatus.BAD_REQUEST);
    }

    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostIdForKildesystem);
    if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
      return new ResponseEntity<>(initHttpHeadersWith(HttpHeaders.WARNING, "Ugyldig prefix på journalpostId"), HttpStatus.BAD_REQUEST);
    }

    return journalpostService.behandleAvvik(enhet, kildesystemIdenfikator, avvikshendelse).getResponseEntity();
  }

  @PutMapping("/journal/{journalpostIdForKildesystem}")
  @Operation(
      security = {@SecurityRequirement(name = "bearer-key")},
      description = "Endre eksisterende journalpost, id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER + "<journalpostId>"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Journalpost er endret (eller registrert/journalført når payload inkluderer \"skalJournalfores\":\"true\")"),
      @ApiResponse(responseCode = "400", description = """
          En av følgende:
          - prefiks på journalpostId er ugyldig
          - EndreJournalpostCommandDto.gjelder er ikke satt og det finnes dokumenter tilknyttet journalpost
          - enhet mangler/ugyldig (fra header)
          - journalpost skal journalføres, men har ikke sakstilknytninger
          """),
      @ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      @ApiResponse(responseCode = "403", description = "Saksbehandler har ikke tilgang til aktuell journalpost/sak"),
      @ApiResponse(responseCode = "404", description = "Fant ikke journalpost som skal endres")
  })
  public ResponseEntity<Void> endreJournalpost(
      @RequestBody EndreJournalpostCommand endreJournalpostCommand,
      @PathVariable String journalpostIdForKildesystem,
      @RequestHeader(EnhetFilter.X_ENHET_HEADER) String enhet
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
