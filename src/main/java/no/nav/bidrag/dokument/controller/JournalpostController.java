package no.nav.bidrag.dokument.controller;

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
import no.nav.bidrag.dokument.dto.OpprettAvvikshendelseResponse;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@ProtectedWithClaims(issuer = ISSUER)
public class JournalpostController {

  private static final Logger LOGGER = LoggerFactory.getLogger(JournalpostController.class);

  private final JournalpostService journalpostService;

  public JournalpostController(JournalpostService journalpostService) {
    this.journalpostService = journalpostService;
  }

  @GetMapping("/sak/{saksnummer}/journal/{journalpostIdForKildesystem}")
  @ApiOperation("Hent en journalpost for en id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER + "<journalpostId>")
  public ResponseEntity<JournalpostDto> hent(@PathVariable String saksnummer, @PathVariable String journalpostIdForKildesystem) {

    LOGGER.info("request: bidrag-dokument/sak/{}/journal/{}", saksnummer, journalpostIdForKildesystem);

    if (KildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix(journalpostIdForKildesystem)) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    var journalpostDtoResponse = journalpostService.hentJournalpost(saksnummer, KildesystemIdenfikator.hent());
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

    if (KildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix(journalpostIdForKildesystem)) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    var avvikslisteRespnse = journalpostService.finnAvvik(saksnummer, KildesystemIdenfikator.hent());
    return new ResponseEntity<>(avvikslisteRespnse.getBody(), avvikslisteRespnse.getHttpStatus());
  }

  @PostMapping(value = "/sak/{saksnummer}/journal/{journalpostIdForKildesystem}/avvik", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Lagrer et avvik for en journalpost, id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER + "<journalpostId>")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Avvik på journalpost er opprettet"),
      @ApiResponse(code = 400, message = "Avvikstype mangler/ugyldig i avvikshendelsen eller enhetsnummer mangler/ugyldig (fra header)"),
      @ApiResponse(code = 401, message = "Du mangler sikkerhetstoken"),
      @ApiResponse(code = 403, message = "Sikkerhetstoken er ikke gyldig"),
      @ApiResponse(code = 404, message = "Fant ikke journalpost som det skal lages avvik på")
  })
  public ResponseEntity<OpprettAvvikshendelseResponse> opprettAvvik(
      @SuppressWarnings("unused") @RequestHeader(EnhetFilter.X_ENHETSNR_HEADER) String enhetsnummer, // ikke brukt direkte, kun for validering
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

    if (KildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix(journalpostIdForKildesystem)) {
      LOGGER.warn("Ugyldig prefiks på journalpostId: " + journalpostIdForKildesystem);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    var opprettAvvikResponse = journalpostService.opprettAvvik(saksnummer, KildesystemIdenfikator.hent(), avvikshendelse);
    return new ResponseEntity<>(opprettAvvikResponse.getBody(), opprettAvvikResponse.getHttpStatus());
  }

  @PutMapping("/sak/{saksnummer}/journal/{journalpostIdForKildesystem}")
  @ApiOperation("Endre eksisterende journalpost")
  @ApiResponses(value = {
      @ApiResponse(code = 203, message = "Journalpost er endret"),
      @ApiResponse(code = 400, message = "EndreJournalpostCommand.gjelder er ikke satt eller det finnes ikke en journalpost på gitt id"),
      @ApiResponse(code = 401, message = "Du mangler sikkerhetstoken"),
      @ApiResponse(code = 403, message = "Sikkerhetstoken er ikke gyldig, eller du har ikke adgang til kode 6 og 7 (nav-ansatt)")
  })
  public ResponseEntity<Void> put(
      @PathVariable String saksnummer,
      @RequestBody EndreJournalpostCommand endreJournalpostCommand,
      @PathVariable String journalpostIdForKildesystem
  ) {

    LOGGER.info("put endret: bidrag-dokument/sak/{}/journal/{}\n \\-> {}", saksnummer, journalpostIdForKildesystem, endreJournalpostCommand);

    if (KildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix(journalpostIdForKildesystem)) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    endreJournalpostCommand.setJournalpostId(journalpostIdForKildesystem);

    var endreResponse = journalpostService.endre(saksnummer, endreJournalpostCommand);
    return new ResponseEntity<>(endreResponse.getHttpStatus());
  }

  @PutMapping("/journal/{journalpostIdForKildesystem}")
  @ApiOperation("Registrere journalpost")
  @ApiResponses(value = {
      @ApiResponse(code = 203, message = "Journalpost er registrert"),
      @ApiResponse(code = 400, message = "Det finnes ikke en journalpost på gitt id"),
      @ApiResponse(code = 401, message = "Du mangler sikkerhetstoken"),
      @ApiResponse(code = 403, message = "Sikkerhetstoken er ikke gyldig, eller du har ikke adgang til kode 6 og 7 (nav-ansatt)")
  })
  public ResponseEntity<Void> registrerOpprettetJournalpost(
      @PathVariable String journalpostIdForKildesystem
  ) {
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PutMapping("/journal/{journalpostIdForKildesystem}/avvik")
  @ApiOperation("Registrere avvik på journalpost uten sakstilknytning")
  @ApiResponses(value = {
      @ApiResponse(code = 203, message = "Avviket på journalposten er registrert"),
      @ApiResponse(code = 400, message = "Det finnes ikke en journalpost på gitt id"),
      @ApiResponse(code = 401, message = "Du mangler sikkerhetstoken"),
      @ApiResponse(code = 403, message = "Sikkerhetstoken er ikke gyldig, eller du har ikke adgang til kode 6 og 7 (nav-ansatt)")
  })
  public ResponseEntity<Void> registrerAvvikPaJournalpostUtenSak(
      @PathVariable String journalpostIdForKildesystem
  ) {
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
