package no.nav.bidrag.dokument.domain

import java.time.LocalDate

data class JournalpostDto(
        var journaltilstand: String? = null,
        var journalfortDto: LocalDate? = null,
        var dokumentDato: LocalDate? = null,
        val hello: String = "hello from bidrag-dokument"
)