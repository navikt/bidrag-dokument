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
        var journalTilstand: String? = null,
        var avsenderDto: AvsenderDto? = null,
        var brukere: List<BrukerDto> = emptyList(),
        var arkivSak: ArkivSakDto? = null,
        var fagomrade: String? = null,
        var tittel: String? = null,
        var kanalReferanseId: String? = null,
        var forsendelseMottatt: LocalDate? = null,
        var mottakskanal: String? = null,
        var journalforendeEnhet: String? = null,
        var dokumenter: List<DokumentDto> = emptyList()
)

data class DokumentDto(
        var dokumentId: String? = null,
        var dokumentTypeId: String? = null,
        var navSkjemaId: String? = null,
        var tittel: String? = null,
        var dokumentkategori: String? = null,
        var varianter: List<VariantDto> = emptyList(),
        var vedlegg: List<VedleggDto> = emptyList()
)

data class VariantDto(
        var filtype: String? = null,
        var format: String? = null
)

data class VedleggDto(
        var filtype: String? = null,
        var format: String? = null
)
