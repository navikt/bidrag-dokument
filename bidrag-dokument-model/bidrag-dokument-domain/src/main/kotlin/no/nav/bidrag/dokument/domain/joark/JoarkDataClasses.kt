package no.nav.bidrag.dokument.domain.joark

import java.time.LocalDate

data class ArkivSakDto(
        var id: String? = null,
        var system: String? = null
)

data class AvsenderDto(
        var avsenderType: String? = null,
        var avsenderId: String? = null,
        var avsender: String? = null
)

data class BrukerDto(
        var brukerType: String? = null,
        var brukerId: String? = null
)

data class JournalforingDto(
        var arkivSak: ArkivSakDto? = null,
        var avsenderDto: AvsenderDto? = null,
        var brukere: List<BrukerDto> = emptyList(),
        var datoDokument: LocalDate? = null,
        var datoJournal: LocalDate? = null,
        var datoMottatt: LocalDate? = null,
        var dokumenter: List<DokumentDto> = emptyList(),
        var fagomrade: String? = null,
        var forsendelseMottatt: LocalDate? = null,
        var innhold: String? = null,
        var journalforendeEnhet: String? = null,
        var journalfortAvNavn: String? = null,
        var journalpostId: Int? = null,
        var journalpostType: String? = null,
        var journalTilstand: String? = null,
        var kanalReferanseId: String? = null,
        var mottakskanal: String? = null,
        var tittel: String? = null
)

data class DokumentDto(
        var dokumentId: String? = null,
        var dokumentkategori: String? = null,
        var dokumentTypeId: String? = null,
        var navSkjemaId: String? = null,
        var tittel: String? = null
)
