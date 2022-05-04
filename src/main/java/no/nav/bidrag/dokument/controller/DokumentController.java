package no.nav.bidrag.dokument.controller;

import no.nav.bidrag.commons.KildesystemIdenfikator;
import no.nav.bidrag.dokument.dto.DokumentTilgangResponse;
import no.nav.bidrag.dokument.service.DokumentService;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Protected
public class DokumentController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DokumentController.class);

  private final DokumentService dokumentService;

  public DokumentController(DokumentService dokumentService) {
    this.dokumentService = dokumentService;
  }

  @GetMapping("/tilgang/{journalpostId}/{dokumentreferanse}")
  public ResponseEntity<DokumentTilgangResponse> giTilgangTilDokument(@PathVariable String journalpostId, @PathVariable String dokumentreferanse) {
    LOGGER.info("Spør om tilgang til dokument: " + dokumentreferanse);

    var dokumentUrlResponse = dokumentService.hentTilgangUrl(journalpostId, dokumentreferanse);

    LOGGER.info(String
        .format("tilgang til dokument: %s, status: %s", dokumentUrlResponse.fetchBody(), dokumentUrlResponse.getResponseEntity().getStatusCode()));

    return dokumentUrlResponse.getResponseEntity();
  }

  @GetMapping({"/dokument/{journalpostId}/{dokumentreferanse}", "/dokument/{journalpostId}"})
  public ResponseEntity<byte[]> hentDokument(@PathVariable String journalpostId, @PathVariable(required = false) String dokumentreferanse) {
    LOGGER.info("Henter dokument med journalpostId={} og dokumentreferanse={} ", journalpostId, dokumentreferanse);
    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostId);

    return dokumentService.hentDokument(kildesystemIdenfikator, dokumentreferanse);
  }
}
