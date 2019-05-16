package no.nav.bidrag.dokument.controller;

import static no.nav.bidrag.dokument.BidragDokumentConfig.ISSUER;

import no.nav.bidrag.commons.web.HttpStatusResponse;
import no.nav.bidrag.dokument.dto.DokumentUrlDto;
import no.nav.bidrag.dokument.service.DokumentService;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ProtectedWithClaims(issuer = ISSUER)
public class DokumentController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DokumentController.class);

  private final DokumentService dokumentService;

  public DokumentController(DokumentService dokumentService) {
    this.dokumentService = dokumentService;
  }

  @PostMapping("/tilgang/url")
  public ResponseEntity<DokumentUrlDto> giTilgangTilDokument(DokumentUrlDto dokumentUrlDto) {
    LOGGER.info("Spør om tilgang til dokument: " + dokumentUrlDto);

    HttpStatusResponse<DokumentUrlDto> response = dokumentService.hentTilgangUrl(dokumentUrlDto);

    LOGGER.info(String.format(
        "tilgang til dokument: %s, status: %s", response.getBody(), response.getHttpStatus()
    ));

    return new ResponseEntity<>(response.getBody(), response.getHttpStatus());
  }

}
