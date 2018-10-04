package no.nav.bidrag.dokument.domain

import java.time.LocalDate

data class JournalpostDto(
        var avsenderId: String? = null,
        var avsenderNavn: String? = null,
        var dokumentreferanse: String? = null,
        var dokumentDato: LocalDate? = null,
        var dokumentType: String? = null,
        var fagomrade: String? = null,
        var gjelderBrukerId: String? = null,
        var innhold: String? = null,
        var journalpostId: Int? = null,
        var journaltilstand: String? = null,
        var journalforendeEnhet: String? = null,
        var journalfortAv: String? = null,
        var journalfortDato: LocalDate? = null,
        var jounalpostIdBisys: Int? = null,
        var mottattDato: LocalDate? = null,
        var saksnummerBidrag: String? = null,
        val hello: String = "hello from bidrag-dokument"
)
