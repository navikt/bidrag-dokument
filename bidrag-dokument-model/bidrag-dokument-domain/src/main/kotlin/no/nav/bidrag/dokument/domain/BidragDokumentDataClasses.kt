package no.nav.bidrag.dokument.domain

import java.time.LocalDate

data class JournalpostDto(
        var journalpostId: Int? = null,
        var journaltilstand: String? = null,
        var journalfortDato: LocalDate? = null,
        var dokumentDato: LocalDate? = null,
        var jounalpostIdBisys: Int? = null,
        var dokumentType: String? = null,
        var fagomrade: String? = null,
        var innhold: String? = null,
        var avsenderId: String? = null,
        var avsenderNavn: String? = null,
        var journalfortAv: String? = null,
        var mottattDato: LocalDate? = null,
        var journalforendeEnhet: String? = null,
        var bestilltOriginal: Boolean? = false,
        var antallRetur: Short? = null,
        var saksnummerArkiv: String? = null,
        var gjelder: String? = null,
        var dokumentId: String? = null,
        val hello: String = "hello from bidrag-dokument"
)
