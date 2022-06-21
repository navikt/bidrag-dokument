package no.nav.bidrag.dokument.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import no.nav.bidrag.dokument.dto.DocumentProperties;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PDFDokumentProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(PDFDokumentProcessor.class);

  private PDDocument document;
  private PDFRenderer pdfRenderer;

  private byte[] originalDocument;

  private DocumentProperties documentProperties;

  public byte[] process(byte[] dokumentFil, DocumentProperties documentProperties){
    this.originalDocument = dokumentFil;
    this.documentProperties = documentProperties;
    ByteArrayOutputStream documentByteStream = new ByteArrayOutputStream();
    try (PDDocument document = PDDocument.load(dokumentFil)) {
      this.document = document;
      this.pdfRenderer = new PDFRenderer(document);

      if (documentProperties.resizeToA4()){
        konverterAlleSiderTilA4();
      }

      if (documentProperties.optimizeForPrint()){
        optimaliserForDobbelsidigPrinting();
      }

      this.document.save(documentByteStream);
      this.document.close();
      return documentByteStream.toByteArray();
    } catch (Exception e) {
      LOGGER.error("Det skjedde en feil ved prossesering av PDF dokument", e);
      return dokumentFil;
    } finally {
      IOUtils.closeQuietly(documentByteStream);
    }
  }

  public void optimaliserForDobbelsidigPrinting(){
    if (documentHasOddNumberOfPages() && documentProperties.hasMoreThanOneDocument() && !documentProperties.isLastDocument()){
      LOGGER.debug("Dokumentet har oddetall antall sider. Legger til en blank side på slutten av dokumentet.");
      document.addPage(new PDPage(PDRectangle.A4));
    }
  }

  private boolean documentHasOddNumberOfPages(){
    return document.getNumberOfPages() % 2 != 0;
  }

  private boolean isPageSizeA4(PDPage pdPage){
    var a4PageMediaBox = PDRectangle.A4;
    var pageMediaBox = pdPage.getMediaBox();
    return isSameWithMargin(pageMediaBox.getHeight(), a4PageMediaBox.getHeight(), 1F) && isSameWithMargin(pageMediaBox.getWidth(), a4PageMediaBox.getWidth(), 1F);
  }

  private void konverterAlleSiderTilA4() throws IOException {
    LOGGER.debug("Konverterer {} sider til A4 størrelse. Filstørrelse {}", document.getNumberOfPages(), bytesIntoHumanReadable(this.originalDocument.length));
    for (int pageNumber = 0; pageNumber < document.getNumberOfPages(); pageNumber++) {
      var page = document.getPage(pageNumber);
      page.setRotation(0);
      if (!isPageSizeA4(page)) {
        convertPageToA4(page);
      }
    }
  }

  private void convertPageToA4Slow(PDPage newPage, int pageNumber) throws IOException {
    Double renderScale = 4D;
    Double pageScale = 1 / renderScale;
    BufferedImage bufferedImage = pdfRenderer.renderImage(pageNumber, renderScale.floatValue());
    PDImageXObject pdImage = LosslessFactory.createFromImage(document, bufferedImage);
    Double heightScaled = pdImage.getHeight() * pageScale;
    Double widthScaled = pdImage.getWidth() * pageScale;

    try (PDPageContentStream contentStream = new PDPageContentStream(document, newPage, AppendMode.OVERWRITE, false, true)) {
      contentStream.drawImage(pdImage, newPage.getMediaBox().getLowerLeftX(), newPage.getMediaBox().getLowerLeftY(), PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight());
    }
  }

  private void convertPageToA4(PDPage page) throws IOException {

    Matrix matrix = new Matrix();
    float xScale = PDRectangle.A4.getWidth() / page.getMediaBox().getWidth();
    float yScale = PDRectangle.A4.getHeight() / page.getMediaBox().getHeight();
    matrix.scale(xScale, yScale);

    try (PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.PREPEND, false)) {
      contentStream.transform(matrix);
    }

    page.setMediaBox(PDRectangle.A4);
    page.setCropBox(PDRectangle.A4);
  }

  private boolean isSameWithMargin(Float val1, Float val2, Float margin){
    return Math.abs(val1-val2)<margin;
  }

  public static byte[] fileToByte(File file) throws IOException {
    FileInputStream inputStream = new FileInputStream(file);
    byte[] byteArray = new byte[(int)file.length()];
    inputStream.read(byteArray);
    inputStream.close();
    return byteArray;
  }

  public static String bytesIntoHumanReadable(long bytes) {
    long kilobyte = 1024;
    long megabyte = kilobyte * 1024;
    long gigabyte = megabyte * 1024;
    long terabyte = gigabyte * 1024;

    if ((bytes >= 0) && (bytes < kilobyte)) {
      return bytes + " B";

    } else if ((bytes >= kilobyte) && (bytes < megabyte)) {
      return (bytes / kilobyte) + " KB";

    } else if ((bytes >= megabyte) && (bytes < gigabyte)) {
      return (bytes / megabyte) + " MB";

    } else if ((bytes >= gigabyte) && (bytes < terabyte)) {
      return (bytes / gigabyte) + " GB";

    } else if (bytes >= terabyte) {
      return (bytes / terabyte) + " TB";

    } else {
      return bytes + " Bytes";
    }
  }
}
