package no.nav.bidrag.dokument.domain.bisys

import java.time.LocalDate

data class BidragJournalpostDto(
        var avsender: String? = null,
        var beskrivelse: String? = null,
        var dokumentdato: LocalDate? = null,
        var dokumentType: String? = null,
        var gjelder: String? = null,
        var journaldato: LocalDate? = null,
        var journalforendeEnhet: String? = null,
        var journalfortAv: String? = null,
        var journalpostId: Int? = null,
        var mottattDato: LocalDate? = null,
        var saksnummer: String? = null,
        val fagomrade: String = "BID"
)
