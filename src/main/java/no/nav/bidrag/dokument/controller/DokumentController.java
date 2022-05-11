package no.nav.bidrag.dokument.controller;

import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import no.nav.bidrag.dokument.dto.DokumentRef;
import no.nav.bidrag.dokument.dto.DokumentTilgangResponse;
import no.nav.bidrag.dokument.service.DokumentService;
import no.nav.bidrag.dokument.service.PDFDokumentProcessor;
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

    var dokumentUrlResponse = dokumentService.hentTilgangUrl(journalpostId, dokumentreferanse);

    LOGGER.info("Gitt tilgang til dokument {}" + dokumentreferanse);

    return dokumentUrlResponse.getResponseEntity();
  }

  @GetMapping({"/dokument/{journalpostId}/{dokumentreferanse}", "/dokument/{journalpostId}"})
  public ResponseEntity<byte[]> hentDokument(@PathVariable String journalpostId, @PathVariable(required = false) String dokumentreferanse, @RequestParam(required = false) boolean resizeToA4) {
    LOGGER.info("Henter dokument med journalpostId={} og dokumentreferanse={}, resizeToA4={}", journalpostId, dokumentreferanse, resizeToA4);
    var dokument = new DokumentRef(journalpostId, dokumentreferanse);
    var response = dokumentService.hentDokument(dokument, resizeToA4);
    LOGGER.info("Hentet dokument med journalpostId={} og dokumentreferanse={} med total størrelse {}", journalpostId, dokumentreferanse, PDFDokumentProcessor.bytesIntoHumanReadable(response.getBody().length));
    return response;
  }

  @GetMapping({ "/dokument"})
  public ResponseEntity<byte[]> hentDokumenter(
      @Parameter(name = "dokument", description = "Liste med dokumenter formatert <Kilde>-<journalpostId>:<dokumentReferanse>") @RequestParam(name = "dokument") List<String> dokumentreferanseList,
      @RequestParam(required = false) boolean resizeToA4) {
    LOGGER.info("Henter dokumenter {} med resizeToA4={} ", dokumentreferanseList, resizeToA4);
    var response =  dokumentService.hentDokumenter(dokumentreferanseList, resizeToA4);
    LOGGER.info("Hentet dokumenter {} med total størrelse {}", dokumentreferanseList, PDFDokumentProcessor.bytesIntoHumanReadable(response.getBody().length));
    return response;
  }
}
