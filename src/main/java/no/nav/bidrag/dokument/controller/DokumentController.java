package no.nav.bidrag.dokument.controller;

import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import no.nav.bidrag.dokument.dto.DokumentRef;
import no.nav.bidrag.dokument.dto.DokumentTilgangResponse;
import no.nav.bidrag.dokument.service.DokumentService;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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
  public ResponseEntity<byte[]> hentDokument(@PathVariable String journalpostId, @PathVariable(required = false) String dokumentreferanse, @RequestParam(required = false) boolean resizeToA4) {
    LOGGER.info("Henter dokument med journalpostId={} og dokumentreferanse={}, resizeToA4={}", journalpostId, dokumentreferanse, resizeToA4);
    var dokument = new DokumentRef(journalpostId, dokumentreferanse);
    return dokumentService.hentDokument(dokument, resizeToA4);
  }

  @GetMapping({ "/dokument"})
  public ResponseEntity<byte[]> hentDokumenter(
      @RequestParam(required = false) boolean resizeToA4,
      @Parameter(name = "dokument", description = "Liste med dokumenter formatert <Kilde>-<journalpostId>:<dokumentReferanse>") @RequestParam(required = false, name = "dokument") List<String> dokumentreferanseList) {
    LOGGER.info("Henter dokumenter {} med resizeToA4={} ", dokumentreferanseList, resizeToA4);
    return dokumentService.hentDokumenter(dokumentreferanseList, resizeToA4);
  }
}
