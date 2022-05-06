package no.nav.bidrag.dokument.dto

data class DokumentRef(
    val journalpostId: String,
    val dokumentId: String?
){
    fun erForKilde(kilde: Kilde): Boolean{
        if (kilde == Kilde.BIDRAG){
            return journalpostId.startsWith(Kilde.BIDRAG.prefix)
        }
        return journalpostId.startsWith(Kilde.JOARK.prefix)
    }

    fun hasDokumentId(): Boolean = dokumentId?.isNotEmpty() == true
    companion object {
        fun parseFromString(str: String): DokumentRef {
            val segments = str.split(":")
            return DokumentRef(segments[0], if (segments.size>1) segments[1] else null)
        }
    }


}

enum class Kilde(var prefix: String) {
    JOARK("JOARK"),
    BIDRAG("BID")
}