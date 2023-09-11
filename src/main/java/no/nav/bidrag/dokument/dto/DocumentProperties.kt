package no.nav.bidrag.dokument.dto

data class DocumentProperties(
    val resizeToA4: Boolean? = false,
    val optimizeForPrint: Boolean? = false,
    var numberOfDocuments: Int? = 1,
    var currentDocumentIndex: Int? = 0,
) {
    constructor(resizeToA4: Boolean) : this(resizeToA4, false)
    constructor(resizeToA4: Boolean, printable: Boolean) : this(resizeToA4, printable, 1)
    constructor(documentProperties: DocumentProperties, currentDocumentIndex: Int) : this(documentProperties.resizeToA4, documentProperties.optimizeForPrint, documentProperties.numberOfDocuments, currentDocumentIndex)
    fun hasMoreThanOneDocument(): Boolean = numberOfDocuments != null && numberOfDocuments!! > 1
    fun isLastDocument(): Boolean = numberOfDocuments != null && currentDocumentIndex != null && numberOfDocuments!! > 1 && numberOfDocuments == (currentDocumentIndex!! + 1)

    fun resizeToA4(): Boolean = resizeToA4 == true
    fun optimizeForPrint(): Boolean = optimizeForPrint == true

    fun shouldProcess(): Boolean = resizeToA4() || optimizeForPrint()
}
