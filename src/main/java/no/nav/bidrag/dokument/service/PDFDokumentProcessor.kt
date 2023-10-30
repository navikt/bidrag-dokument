package no.nav.bidrag.dokument.service

import no.nav.bidrag.transport.dokument.DocumentProperties
import org.apache.pdfbox.Loader
import org.apache.pdfbox.io.IOUtils
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.io.RandomAccessReadBuffer
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.util.Matrix
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import kotlin.math.abs

class PDFDokumentProcessor {
    private var document: PDDocument? = null
    private lateinit var originalDocument: ByteArray
    private var documentProperties: DocumentProperties? = null
    fun process(dokumentFil: ByteArray, documentProperties: DocumentProperties): ByteArray {
        if (!documentProperties.shouldProcess()) {
            return dokumentFil
        }
        originalDocument = dokumentFil
        this.documentProperties = documentProperties
        val documentByteStream = ByteArrayOutputStream()
        try {
            Loader.loadPDF(
                RandomAccessReadBuffer(dokumentFil),
                MemoryUsageSetting.setupTempFileOnly().streamCache,
            ).use { document ->
                this.document = document
                if (documentProperties.resizeToA4()) {
                    konverterAlleSiderTilA4()
                }
                if (documentProperties.optimizeForPrint()) {
                    optimaliserForDobbelsidigPrinting()
                }
                this.document?.save(documentByteStream)
                this.document?.close()
                return documentByteStream.toByteArray()
            }
        } catch (e: Exception) {
            LOGGER.error("Det skjedde en feil ved prossesering av PDF dokument", e)
            return dokumentFil
        } finally {
            IOUtils.closeQuietly(documentByteStream)
        }
    }

    fun optimaliserForDobbelsidigPrinting() {
        if (documentHasOddNumberOfPages() && documentProperties!!.hasMoreThanOneDocument() && !documentProperties!!.isLastDocument()) {
            LOGGER.debug("Dokumentet har oddetall antall sider. Legger til en blank side på slutten av dokumentet.")
            document?.addPage(PDPage(PDRectangle.A4))
        }
    }

    private fun documentHasOddNumberOfPages(): Boolean {
        return document != null && document!!.numberOfPages % 2 != 0
    }

    private fun isPageSizeA4(pdPage: PDPage): Boolean {
        val a4PageMediaBox = PDRectangle.A4
        val pageMediaBox = pdPage.mediaBox
        val hasSameHeightAndWidth =
            isSameWithMargin(pageMediaBox.height, a4PageMediaBox.height, 1f) && isSameWithMargin(
                pageMediaBox.width,
                a4PageMediaBox.width,
                1f,
            )
        val hasSameHeightAndWidthRotated =
            isSameWithMargin(pageMediaBox.width, a4PageMediaBox.height, 1f) && isSameWithMargin(
                pageMediaBox.height,
                a4PageMediaBox.width,
                1f,
            )
        return hasSameHeightAndWidth || hasSameHeightAndWidthRotated
    }

    @Throws(IOException::class)
    private fun konverterAlleSiderTilA4() {
        LOGGER.debug(
            "Konverterer {} sider til A4 størrelse. Filstørrelse {}",
            document!!.numberOfPages,
            bytesIntoHumanReadable(
                originalDocument.size.toLong(),
            ),
        )
        for (pageNumber in 0 until document!!.numberOfPages) {
            val page = document!!.getPage(pageNumber)
            updatePageRotationToVertical(page)
            if (!isPageSizeA4(page)) {
                convertPageToA4(page)
            }
        }
    }

    private fun updatePageRotationToVertical(page: PDPage) {
        if (isVertical(page) && page.rotation != 0) {
            page.rotation = 0
        } else if (isHorizontal(page) && page.rotation == 0 && !isPageSizeA4(page)) {
            page.rotation = 90
        }
    }

    private fun isHorizontal(page: PDPage): Boolean {
        return !isVertical(page)
    }

    /*
     En side skal roteres til å være vertikalt kun hvis siden er dimensjonert slik at høyden > bredden. Ellers skal det ignoreres
     */
    private fun isVertical(page: PDPage): Boolean {
        return Optional.ofNullable(page.mediaBox)
            .map { mediaBox: PDRectangle -> mediaBox.height > mediaBox.width }
            .orElse(false)
    }

    @Throws(IOException::class)
    private fun convertPageToA4(page: PDPage) {
        val matrix = Matrix()
        val xScale = PDRectangle.A4.width / page.mediaBox.width
        val yScale = PDRectangle.A4.height / page.mediaBox.height
        matrix.scale(xScale, yScale)
        PDPageContentStream(
            document,
            page,
            AppendMode.PREPEND,
            false,
        ).use { contentStream -> contentStream.transform(matrix) }
        page.mediaBox = PDRectangle.A4
        page.cropBox = PDRectangle.A4
    }

    private fun isSameWithMargin(val1: Float, val2: Float, margin: Float): Boolean {
        return abs(val1 - val2) < margin
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(
            PDFDokumentProcessor::class.java,
        )

        @Throws(IOException::class)
        fun fileToByte(file: File): ByteArray {
            val inputStream = FileInputStream(file)
            val byteArray = ByteArray(file.length().toInt())
            inputStream.read(byteArray)
            inputStream.close()
            return byteArray
        }

        fun bytesIntoHumanReadable(bytes: Long): String {
            val kilobyte: Long = 1024
            val megabyte = kilobyte * 1024
            val gigabyte = megabyte * 1024
            val terabyte = gigabyte * 1024
            return if (bytes in 0..<kilobyte) {
                "$bytes B"
            } else if (bytes in kilobyte..<megabyte) {
                (bytes / kilobyte).toString() + " KB"
            } else if (bytes in megabyte..<gigabyte) {
                (bytes / megabyte).toString() + " MB"
            } else if (bytes in gigabyte..<terabyte) {
                (bytes / gigabyte).toString() + " GB"
            } else if (bytes >= terabyte) {
                (bytes / terabyte).toString() + " TB"
            } else {
                "$bytes Bytes"
            }
        }
    }
}
