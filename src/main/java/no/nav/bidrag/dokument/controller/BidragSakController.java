package no.nav.bidrag.dokument.controller;


import static no.nav.bidrag.dokument.BidragDokumentConfig.ISSUER;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.service.JournalpostService;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(BidragSakController.SAKJOURNAL)
@ProtectedWithClaims(issuer = ISSUER)
public class BidragSakController {

  static final String SAKJOURNAL = "/sakjournal";

  private static final Logger LOGGER = LoggerFactory.getLogger(BidragSakController.class);
  private static final String NON_DIGITS = "\\D+";

  private final JournalpostService journalpostService;

  public BidragSakController(JournalpostService journalpostService) {
    this.journalpostService = journalpostService;
  }

  @GetMapping("/{saksnummer}")
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

    LOGGER.info("request: bidrag-dokument/{}/{}?fagomrade={}", SAKJOURNAL, saksnummer, fagomrade);

    if (saksnummer.matches(NON_DIGITS)) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    var journalposter = journalpostService.finnJournalposter(saksnummer, fagomrade);

    return new ResponseEntity<>(journalposter, journalposter.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK);
  }
}