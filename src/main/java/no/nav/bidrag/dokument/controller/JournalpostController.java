package no.nav.bidrag.dokument.controller;

import static no.nav.bidrag.dokument.BidragDokumentConfig.DELIMTER;
import static no.nav.bidrag.dokument.BidragDokumentConfig.ISSUER;
import static no.nav.bidrag.dokument.BidragDokumentConfig.PREFIX_BIDRAG;
import static no.nav.bidrag.dokument.BidragDokumentConfig.PREFIX_JOARK;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import no.nav.bidrag.commons.web.HttpStatusResponse;
import no.nav.bidrag.dokument.KildesystemIdenfikator;
import no.nav.bidrag.dokument.KildesystemIdenfikator.Kildesystem;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommandDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.service.JournalpostService;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ProtectedWithClaims(issuer = ISSUER)
public class JournalpostController {

  private static final Logger LOGGER = LoggerFactory.getLogger(JournalpostController.class);
  private static final String ENDPOINT_JOURNALPOST = "/journalpost";
  private static final String ENDPOINT_SAKJOURNAL = "/sakjournal";
  private static final String NON_DIGITS = "\\D+";

  private final JournalpostService journalpostService;

  public JournalpostController(JournalpostService journalpostService) {
    this.journalpostService = journalpostService;
  }

  @GetMapping("/status")
  @ResponseBody
  public String get() {
    return "OK";
  }

  @GetMapping(ENDPOINT_JOURNALPOST + "/{journalpostIdForKildesystem}")
  @ApiOperation("Hent en journalpost for en id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER + "<journalpostId>")
  public ResponseEntity<JournalpostDto> hent(@PathVariable String journalpostIdForKildesystem) {

    LOGGER.info("request: bidrag-dokument{}/{}", ENDPOINT_JOURNALPOST, journalpostIdForKildesystem);

    KildesystemIdenfikator kildesystemIdentifikator = new KildesystemIdenfikator(journalpostIdForKildesystem);

    if (kildesystemIdentifikator.erUkjent() || kildesystemIdentifikator.harIkkeJournalpostIdSomTall()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    var journalpostDtoResponse = journalpostService.hentJournalpost(kildesystemIdentifikator);
    return new ResponseEntity<>(journalpostDtoResponse.getBody(), journalpostDtoResponse.getHttpStatus());
  }

  @GetMapping(ENDPOINT_SAKJOURNAL + "/{saksnummer}")
  @ApiOperation("Finn saksjournal for et saksnummer, samt parameter 'fagomrade' (FAR - farskapsjournal) og (BID - bidragsjournal)")
  public ResponseEntity<List<JournalpostDto>> get(
      @PathVariable String saksnummer,
      @RequestParam String fagomrade
  ) {

    LOGGER.info("request: bidrag-dokument{}/{}?fagomrade={}", ENDPOINT_JOURNALPOST, saksnummer, fagomrade);

    if (saksnummer.matches(NON_DIGITS)) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    var journalposter = journalpostService.finnJournalposter(saksnummer, fagomrade);

    return new ResponseEntity<>(journalposter, journalposter.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK);
  }

  @PutMapping(ENDPOINT_JOURNALPOST + "/{journalpostIdForKildesystem}")
  @ApiOperation("Endre eksisterende journalpost")
  public ResponseEntity<JournalpostDto> put(
      @RequestBody EndreJournalpostCommandDto endreJournalpostCommandDto,
      @PathVariable String journalpostIdForKildesystem
  ) {

    LOGGER.info("put endret: bidrag-dokument{}/{}\n \\-> {}", ENDPOINT_JOURNALPOST, journalpostIdForKildesystem, endreJournalpostCommandDto);

    var kildesystemIdentifikator = new KildesystemIdenfikator(journalpostIdForKildesystem);

    if (Kildesystem.UKJENT == kildesystemIdentifikator.hentKildesystem()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    var endreResponse = journalpostService.endre(endreJournalpostCommandDto);
    return new ResponseEntity<>(endreResponse.getBody(), endreResponse.getHttpStatus());
  }
}
