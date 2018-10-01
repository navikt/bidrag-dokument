package no.nav.bidrag.dokument.domain.bisys

import java.time.LocalDate

data class BidragJournalpostDto(
        var journalTilstand: String? = null,
        var journalfortDato: LocalDate? = null
)