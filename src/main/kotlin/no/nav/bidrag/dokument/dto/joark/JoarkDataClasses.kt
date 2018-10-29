package no.nav.bidrag.dokument.dto.joark

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDate

@ApiModel(value = "Metadata om en sak fra joark")
data class ArkivSakDto(
        var id: String? = null,
        var system: String? = null
)

@ApiModel(value = "Metadata for journalføringens avsender")
data class AvsenderDto(
        var avsenderType: String? = null,
        var avsenderId: String? = null,
        var avsender: String? = null
)

data class BrukerDto(
        var brukerType: String? = null,
        var brukerId: String? = null
)

@ApiModel(value = "Metadata for en journalføring i joark")
data class JournalforingDto(
        var arkivSak: ArkivSakDto? = null,
        var avsenderDto: AvsenderDto? = null,
        var brukere: List<BrukerDto> = emptyList(),
        var datoDokument: LocalDate? = null,
        var datoJournal: LocalDate? = null,
        var datoMottatt: LocalDate? = null,
        var dokumenter: List<JoarkDokumentDto> = emptyList(),
        var fagomrade: String? = null,
        var forsendelseMottatt: LocalDate? = null,
        var innhold: String? = null,
        var journalforendeEnhet: String? = null,
        var journalfortAvNavn: String? = null,
        var journalpostId: Int? = null,
        var journalTilstand: String? = null,
        var kanalReferanseId: String? = null,
        var mottakskanal: String? = null
)

@ApiModel(value = "Metadata om et dokument hentet fra joark")
data class JoarkDokumentDto(
        @ApiModelProperty(value = "Identifiserer et dokument") var dokumentId: String? = null,
        var dokumentkategori: String? = null,
        var dokumentTypeId: String? = null,
        var navSkjemaId: String? = null,
        var tittel: String? = null
)
