package no.nav.bidrag.dokument.dto

data class DokumentRef(
    val journalpostId: String?,
    val dokumentId: String?,
    val kilde: Kilde? = null
){
    fun erForKilde(kilde: Kilde): Boolean {
        return if (this.kilde != null) this.kilde == kilde
        else when(kilde){
            Kilde.MIDLERTIDLIG_BREVLAGER -> journalpostId?.startsWith(Kilde.MIDLERTIDLIG_BREVLAGER.prefix) == true
            Kilde.JOARK -> journalpostId?.startsWith(Kilde.JOARK.prefix) == true
            Kilde.FORSENDELSE -> journalpostId.isNullOrEmpty() || journalpostId.startsWith(Kilde.FORSENDELSE.prefix)
        }
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
    MIDLERTIDLIG_BREVLAGER("BID"),
    FORSENDELSE("BIF")
}