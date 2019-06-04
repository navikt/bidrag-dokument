package no.nav.bidrag.dokument.service;

import no.nav.bidrag.commons.web.HttpStatusResponse;
import no.nav.bidrag.dokument.consumer.DokumentConsumer;
import no.nav.bidrag.dokument.dto.DokumentTilgangRequest;
import no.nav.bidrag.dokument.dto.DokumentUrlDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class DokumentService {

  private final DokumentConsumer dokumentConsumer;

  public DokumentService(DokumentConsumer dokumentConsumer) {
    this.dokumentConsumer = dokumentConsumer;
  }

  public HttpStatusResponse<DokumentUrlDto> hentTilgangUrl(DokumentTilgangRequest dokumentTilgangRequest) {
    return dokumentConsumer.hentTilgangUrl(dokumentTilgangRequest);
  }
}
