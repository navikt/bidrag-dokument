package no.nav.bidrag.dokument.service;

import no.nav.bidrag.commons.web.HttpResponse;
import no.nav.bidrag.dokument.consumer.DokumentConsumer;
import no.nav.bidrag.dokument.dto.DokumentTilgangResponse;
import org.springframework.stereotype.Service;

@Service
public class DokumentService {

  private final DokumentConsumer dokumentConsumer;

  public DokumentService(DokumentConsumer dokumentConsumer) {
    this.dokumentConsumer = dokumentConsumer;
  }

  public HttpResponse<DokumentTilgangResponse> hentTilgangUrl(String journalpostId, String dokumentreferanse) {
    return dokumentConsumer.hentTilgangUrl(journalpostId, dokumentreferanse);
  }
}
