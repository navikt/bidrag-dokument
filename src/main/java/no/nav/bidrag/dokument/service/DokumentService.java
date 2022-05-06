package no.nav.bidrag.dokument.service;

import static no.nav.bidrag.dokument.BidragDokumentConfig.ARKIV_QUALIFIER;
import static no.nav.bidrag.dokument.BidragDokumentConfig.MIDL_BREVLAGER_QUALIFIER;
import static no.nav.bidrag.dokument.service.PDFDokumentProcessor.fileToByte;

import com.google.common.io.ByteSource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import no.nav.bidrag.commons.web.HttpResponse;
import no.nav.bidrag.dokument.consumer.BidragDokumentConsumer;
import no.nav.bidrag.dokument.consumer.DokumentTilgangConsumer;
import no.nav.bidrag.dokument.dto.DokumentDto;
import no.nav.bidrag.dokument.dto.DokumentRef;
import no.nav.bidrag.dokument.dto.DokumentTilgangResponse;
import no.nav.bidrag.dokument.dto.JournalpostResponse;
import no.nav.bidrag.dokument.dto.Kilde;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DokumentService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DokumentService.class);

  private final JournalpostService journalpostService;
  private final BidragDokumentConsumer bidragJournalpostConsumer;
  private final BidragDokumentConsumer bidragArkivConsumer;
  private final DokumentTilgangConsumer dokumentTilgangConsumer;

  public DokumentService(
      JournalpostService journalpostService, DokumentTilgangConsumer dokumentTilgangConsumer,
      @Qualifier(ARKIV_QUALIFIER) BidragDokumentConsumer bidragArkivConsumer,
      @Qualifier(MIDL_BREVLAGER_QUALIFIER) BidragDokumentConsumer bidragJournalpostConsumer
  ) {
    this.journalpostService = journalpostService;
    this.dokumentTilgangConsumer = dokumentTilgangConsumer;
    this.bidragArkivConsumer = bidragArkivConsumer;
    this.bidragJournalpostConsumer = bidragJournalpostConsumer;
  }
  public HttpResponse<DokumentTilgangResponse> hentTilgangUrl(String journalpostId, String dokumentreferanse) {
    return dokumentTilgangConsumer.hentTilgangUrl(journalpostId, dokumentreferanse);
  }

  public ResponseEntity<byte[]> hentDokument(DokumentRef dokumentRef, boolean resizeToA4) {
    if (!dokumentRef.hasDokumentId() && dokumentRef.erForKilde(Kilde.JOARK)){
      var dokumentReferanser = hentAlleJournalpostDokumentReferanser(dokumentRef);
      return hentDokumenterData(dokumentReferanser, resizeToA4);
    }

    ResponseEntity<byte[]> response = hentDokumentData(dokumentRef);

    if (resizeToA4){
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_PDF)
          .header(HttpHeaders.CONTENT_DISPOSITION, response.getHeaders().getContentDisposition().toString())
          .body(new PDFDokumentProcessor().konverterAlleSiderTilA4(response.getBody()));
    }

    return response;
  }

  public ResponseEntity<byte[]> hentDokumenter(List<String> dokumenterString, boolean resizeToA4){
      var dokumenter = parseDokumentString(dokumenterString);
      return hentDokumenterData(dokumenter, resizeToA4);
  }

  private ResponseEntity<byte[]> hentDokumentData(DokumentRef dokument) {
    if (dokument.erForKilde(Kilde.BIDRAG)) {
      return bidragJournalpostConsumer.hentDokument(dokument.getJournalpostId(), dokument.getDokumentId());
    }
    return bidragArkivConsumer.hentDokument(dokument.getJournalpostId(), dokument.getDokumentId());
  }

  private ResponseEntity<byte[]> hentDokumenterData(List<DokumentRef> dokumentRefList, boolean resizeToA4){
    var mergedFileName = "alle_dokumenter.pdf";
    var mergedFileNameTmp = "/tmp/"+mergedFileName+"_"+ UUID.randomUUID();
    try {
      hentOgMergeAlleDokumenter(dokumentRefList, mergedFileNameTmp, resizeToA4);
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_PDF)
          .header(HttpHeaders.CONTENT_DISPOSITION, String.format("inline; filename=%s", mergedFileName))
          .body(getByteDataAndDeleteFile(mergedFileNameTmp));
    } catch (Exception e){
      LOGGER.error("Det skjedde en feil ved henting av dokumenter {}", dokumentRefList, e);
      return ResponseEntity.internalServerError().build();
    }
  }

  private void hentOgMergeAlleDokumenter(List<DokumentRef> dokumentList, String mergedFileName, boolean resizeToA4) throws IOException {
    PDFMergerUtility mergedDocument = new PDFMergerUtility();
    mergedDocument.setDestinationFileName(mergedFileName);
    for (var dokument: dokumentList){
      var dokumentResponse = hentDokument(dokument, resizeToA4);
      var dokumentInputStream = ByteSource.wrap(dokumentResponse.getBody()).openStream();
      mergedDocument.addSource(dokumentInputStream);
      dokumentInputStream.close();
    }
    mergedDocument.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
  }

  private byte[] getByteDataAndDeleteFile(String filename) throws IOException {
    var file = new File(filename);
    try {
      return fileToByte(new File(filename));
    } finally {
      file.delete();
    }
  }

  private List<DokumentRef> hentAlleJournalpostDokumentReferanser(DokumentRef dokumentRef){
    JournalpostResponse journalpost = journalpostService.hentJournalpost(dokumentRef, null).fetchBody().get();
    return journalpost.getJournalpost().getDokumenter()
        .stream()
        .map((DokumentDto::getDokumentreferanse))
        .map((dokumentId)->new DokumentRef(dokumentRef.getJournalpostId(), dokumentId))
        .collect(Collectors.toList());
  }

  private List<DokumentRef> parseDokumentString(List<String> dokumenterString){
    return dokumenterString.stream().map(DokumentRef.Companion::parseFromString).collect(Collectors.toList());
  }
}
