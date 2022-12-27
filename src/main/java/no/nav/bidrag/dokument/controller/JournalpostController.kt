package no.nav.bidrag.dokument.controller;

import static no.nav.bidrag.commons.web.EnhetFilter.X_ENHET_HEADER;
import static no.nav.bidrag.commons.web.WebUtil.initHttpHeadersWith;
import static no.nav.bidrag.dokument.BidragDokument.SECURE_LOGGER;
import static no.nav.bidrag.dokument.BidragDokumentConfig.DELIMTER;
import static no.nav.bidrag.dokument.BidragDokumentConfig.PREFIX_BIDRAG;
import static no.nav.bidrag.dokument.BidragDokumentConfig.PREFIX_JOARK;

import com.google.common.base.Strings;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import no.nav.bidrag.commons.KildesystemIdenfikator;
import no.nav.bidrag.commons.web.EnhetFilter;
import no.nav.bidrag.dokument.dto.ArkivSystem;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.BehandleAvvikshendelseResponse;
import no.nav.bidrag.dokument.dto.DistribuerJournalpostRequest;
import no.nav.bidrag.dokument.dto.DistribuerJournalpostResponse;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.JournalpostResponse;
import no.nav.bidrag.dokument.dto.OpprettJournalpostRequest;
import no.nav.bidrag.dokument.dto.OpprettJournalpostResponse;
import no.nav.bidrag.dokument.service.JournalpostService;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Protected
@Timed
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
      @ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig", content = @Content(schema = @Schema(hidden = true))),
      @ApiResponse(responseCode = "403", description = "Saksbehandler har ikke tilgang til aktuell journalpost", content = @Content(schema = @Schema(hidden = true)))
  })
  public ResponseEntity<List<JournalpostDto>> hentJournal(@PathVariable String saksnummer, @RequestParam String fagomrade) {

    LOGGER.info("Henter journal for sak {} og fagomrade {}", saksnummer, fagomrade);

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
      @ApiResponse(responseCode = "400", description = "Ukjent/ugyldig journalpostId som har/mangler prefix", content = @Content(schema = @Schema(hidden = true))),
      @ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig", content = @Content(schema = @Schema(hidden = true))),
      @ApiResponse(responseCode = "403", description = "Saksbehandler har ikke tilgang til aktuell journalpost", content = @Content(schema = @Schema(hidden = true))),
      @ApiResponse(responseCode = "404", description = "Journalposten som skal hentes eksisterer ikke eller det er feil prefix/id på journalposten", content = @Content(schema = @Schema(hidden = true)))
  })
  public ResponseEntity<JournalpostResponse> hentJournalpost(@PathVariable String journalpostIdForKildesystem,
      @Parameter(name = "saksnummer", description = "journalposten tilhører sak") @RequestParam(required = false) String saksnummer) {
    var journalpostId = journalpostIdForKildesystem;
    if (!Strings.isNullOrEmpty(journalpostIdForKildesystem) && journalpostIdForKildesystem.contains(":")){
      journalpostId = journalpostIdForKildesystem.split(":")[0];
      LOGGER.warn("Mottok ugyldig journalpostId {}, forsøket korreksjon til journalpostId {}", journalpostIdForKildesystem, journalpostId);
    }
    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostId);
    if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
      return new ResponseEntity<>(initHttpHeadersWith(HttpHeaders.WARNING, "Ugyldig prefix på journalpostId"), HttpStatus.BAD_REQUEST);
    }

    LOGGER.info("Henter journalpost {} for saksnummer {}", journalpostId, saksnummer);

    var response =  journalpostService.hentJournalpost(saksnummer, kildesystemIdenfikator);
    return response.clearContentHeaders().getResponseEntity();
  }

  @GetMapping("/journal/{journalpostIdForKildesystem}/avvik")
  @Operation(
      security = {@SecurityRequirement(name = "bearer-key")},
      description = "Henter mulige avvik for en journalpost, id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER
          + "<journalpostId>"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tilgjengelig avvik for journalpost er hentet"),
      @ApiResponse(responseCode = "401", description = "Du mangler sikkerhetstoken", content = @Content(schema = @Schema(hidden = true))),
      @ApiResponse(responseCode = "403", description = "Sikkerhetstoken er ikke gyldig", content = @Content(schema = @Schema(hidden = true))),
      @ApiResponse(responseCode = "404", description = "Fant ikke journalpost som det skal hentes avvik på", content = @Content(schema = @Schema(hidden = true)))
  })
  public ResponseEntity<List<AvvikType>> hentAvvik(@PathVariable String journalpostIdForKildesystem,
      @Parameter(name = "saksnummer", description = "journalposten tilhører sak") @RequestParam(required = false) String saksnummer) {
    LOGGER.info("Henter avvik for journalpost {}", journalpostIdForKildesystem);

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
    LOGGER.info("Behandler avvik for journalpost {}", journalpostIdForKildesystem);
    SECURE_LOGGER.info("Behandler avvik for journalpost {} med avvikshendelse {}", journalpostIdForKildesystem, avvikshendelse);

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

  @PatchMapping("/journal/{journalpostIdForKildesystem}")
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
  public ResponseEntity<Void> patchJournalpost(
      @RequestBody EndreJournalpostCommand endreJournalpostCommand,
      @PathVariable String journalpostIdForKildesystem,
      @RequestHeader(EnhetFilter.X_ENHET_HEADER) String enhet
  ) {
    LOGGER.info("Endrer journalpost {}", journalpostIdForKildesystem);
    SECURE_LOGGER.info("Endrer journalpost {} med endringer {}", journalpostIdForKildesystem, endreJournalpostCommand);

    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostIdForKildesystem);
    if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
      return new ResponseEntity<>(initHttpHeadersWith(HttpHeaders.WARNING, "Ugyldig prefix på journalpostId"), HttpStatus.BAD_REQUEST);
    }

    endreJournalpostCommand.setJournalpostId(journalpostIdForKildesystem);

    return journalpostService.endre(enhet, kildesystemIdenfikator, endreJournalpostCommand).getResponseEntity();
  }

  @PostMapping("/journalpost/{arkivSystem}")
  @Operation(
      security = {@SecurityRequirement(name = "bearer-key")},
      description = """
          Opprett notat eller utgående journalpost i midlertidlig brevlager.
          Opprett inngående, notat eller utgående journalpost i Joark
          """
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Journalpost er opprettet"),
      @ApiResponse(responseCode = "400", description = "Input inneholder ugyldig data"),
      @ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig")
  })
  public ResponseEntity<OpprettJournalpostResponse> opprettJournalpost(
      @RequestBody OpprettJournalpostRequest opprettJournalpostRequest,
      @PathVariable ArkivSystem arkivSystem
  ) {
    SECURE_LOGGER.info("Oppretter journalpost {} for arkivsystem {}", opprettJournalpostRequest, arkivSystem);

    return journalpostService.opprett(opprettJournalpostRequest, arkivSystem).getResponseEntity();
  }


  @PostMapping("/journal/distribuer/{joarkJournalpostId}")
  @Operation(description = "Bestill distribusjon av journalpost")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Distribusjon av journalpost er bestilt"),
      @ApiResponse(responseCode = "400", description = "Journalpost mangler mottakerid eller adresse er ikke oppgitt i kallet"),
      @ApiResponse(responseCode = "401", description = "Sikkerhetstoken er ikke gyldig"),
      @ApiResponse(responseCode = "403", description = "Sikkerhetstoke1n er ikke gyldig, eller det er ikke gitt adgang til kode 6 og 7 (nav-ansatt)"),
      @ApiResponse(responseCode = "404", description = "Fant ikke journalpost som skal distribueres")
  })
  @ResponseBody
  public ResponseEntity<DistribuerJournalpostResponse> distribuerJournalpost(
      @RequestBody(required = false) DistribuerJournalpostRequest distribuerJournalpostRequest,
      @PathVariable String joarkJournalpostId,
      @RequestParam(required = false) String batchId
  ) {
    LOGGER.info("Distribuerer journalpost {}", joarkJournalpostId);
    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(joarkJournalpostId);

    if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
      var msgBadRequest = String.format("Id har ikke riktig prefix: %s", joarkJournalpostId);

      LOGGER.warn(msgBadRequest);

      return ResponseEntity
          .badRequest()
          .header(HttpHeaders.WARNING, msgBadRequest)
          .build();
    }

    return journalpostService.distribuerJournalpost(batchId, kildesystemIdenfikator, distribuerJournalpostRequest).getResponseEntity();
  }

  @GetMapping("/journal/distribuer/{journalpostId}/enabled")
  @Operation(description = "Sjekk om distribusjon av journalpost kan bestilles")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Distribusjon av journalpost kan bestilles"),
      @ApiResponse(responseCode = "406", description = "Distribusjon av journalpost kan ikke bestilles"),
      @ApiResponse(responseCode = "401", description = "Sikkerhetstoken er ikke gyldig"),
      @ApiResponse(responseCode = "403", description = "Sikkerhetstoke1n er ikke gyldig, eller det er ikke gitt adgang til kode 6 og 7 (nav-ansatt)"),
      @ApiResponse(responseCode = "404", description = "Fant ikke journalpost som skal distribueres")
  })
  @ResponseBody
  public ResponseEntity<Void> kanDistribuerJournalpost(@PathVariable String journalpostId) {
    LOGGER.info("Sjekker om journalpost {} kan distribueres", journalpostId);
    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostId);

    if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
      var msgBadRequest = String.format("Id har ikke riktig prefix: %s", journalpostId);

      LOGGER.warn(msgBadRequest);

      return ResponseEntity
          .badRequest()
          .header(HttpHeaders.WARNING, msgBadRequest)
          .build();
    }

    return journalpostService.kanDistribuereJournalpost(kildesystemIdenfikator).getResponseEntity();
  }
}
