package no.nav.bidrag.dokument.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
  public byte[] konverterAlleSiderTilA4(byte[] dokumentFil) {
    ByteArrayOutputStream documentByteStream = new ByteArrayOutputStream();
    try (PDDocument document = PDDocument.load(dokumentFil);) {
      this.document = document;
      this.pdfRenderer = new PDFRenderer(document);
      processPages();

      this.document.setAllSecurityToBeRemoved(true);
      this.document.save(documentByteStream);
      this.document.close();
      return documentByteStream.toByteArray();
    } catch (Exception e) {
      LOGGER.error("Det skjedde en feil ved konverting av PDF dokument til A4 størrelse", e);
      return dokumentFil;
    } finally {
      IOUtils.closeQuietly(documentByteStream);
    }
  }

  private boolean isPageSizeA4(PDPage pdPage){
    var a4PageMediaBox = PDRectangle.A4;
    var pageMediaBox = pdPage.getMediaBox();
    return isSameWithMargin(pageMediaBox.getHeight(), a4PageMediaBox.getHeight(), 1F) && isSameWithMargin(pageMediaBox.getWidth(), a4PageMediaBox.getWidth(), 1F);
  }

  private void processPages() throws IOException {
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
    float minValue = Math.min(xScale, yScale);
    matrix.scale(minValue, minValue);

    try (PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.PREPEND, false)) {
      contentStream.transform(matrix);
    }

    page.setMediaBox(PDRectangle.A4);
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
}
