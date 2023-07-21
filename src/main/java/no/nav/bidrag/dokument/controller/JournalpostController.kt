package no.nav.bidrag.dokument.controller

import com.google.common.base.Strings
import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mu.KotlinLogging
import no.nav.bidrag.commons.util.KildesystemIdenfikator
import no.nav.bidrag.commons.web.EnhetFilter
import no.nav.bidrag.commons.web.WebUtil
import no.nav.bidrag.dokument.BidragDokumentConfig
import no.nav.bidrag.dokument.consumer.GlobalApiReponses
import no.nav.bidrag.dokument.service.JournalpostService
import no.nav.bidrag.dokument.sikkerLogg
import no.nav.bidrag.transport.dokument.ArkivSystem
import no.nav.bidrag.transport.dokument.AvvikType
import no.nav.bidrag.transport.dokument.Avvikshendelse
import no.nav.bidrag.transport.dokument.BehandleAvvikshendelseResponse
import no.nav.bidrag.transport.dokument.DistribuerJournalpostRequest
import no.nav.bidrag.transport.dokument.DistribuerJournalpostResponse
import no.nav.bidrag.transport.dokument.DistribusjonInfoDto
import no.nav.bidrag.transport.dokument.EndreJournalpostCommand
import no.nav.bidrag.transport.dokument.JournalpostDto
import no.nav.bidrag.transport.dokument.JournalpostId
import no.nav.bidrag.transport.dokument.JournalpostResponse
import no.nav.bidrag.transport.dokument.OpprettJournalpostRequest
import no.nav.bidrag.transport.dokument.OpprettJournalpostResponse
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

fun JournalpostDto.erFarskapUtelukketEllerBidragJournalpostMedTemaFar() =
    this.erFarskapUtelukket() // || JournalpostId(this.journalpostId).erSystemBidrag && fagomrade == "FAR"

@RestController
@Protected
@Timed
class JournalpostController(private val journalpostService: JournalpostService) {
    @GetMapping("/sak/{saksnummer}/journal")
    @Operation(
        security = [SecurityRequirement(name = "bearer-key")],
        summary = "Finn saksjournal for et saksnummer, samt parameter 'fagomrade' (FAR - farskapsjournal) og (BID - bidragsjournal)"
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "Fant journalposter for saksnummer"
        )]
    )
    @GlobalApiReponses
    fun hentJournal(
        @PathVariable saksnummer: String,
        @RequestParam fagomrade: List<String> = emptyList(),
        @RequestParam(required = false, defaultValue = "false") bareFarskapUtelukket: Boolean
    ): ResponseEntity<List<JournalpostDto>> {
        log.info("Henter journal for sak $saksnummer og fagomrader ${fagomrade.joinToString(",")} bareFarskapUtelukket = $bareFarskapUtelukket")
        if (saksnummer.matches(NON_DIGITS.toRegex())) {
            log.warn("Ugyldig saksnummer: $saksnummer")
            return ResponseEntity(
                WebUtil.initHttpHeadersWith(
                    HttpHeaders.WARNING,
                    "Ugyldig saksnummer"
                ), HttpStatus.BAD_REQUEST
            )
        }
        val journalposter = journalpostService.finnJournalposter(saksnummer, fagomrade)
            .filter { if (bareFarskapUtelukket) it.erFarskapUtelukketEllerBidragJournalpostMedTemaFar() else !it.erFarskapUtelukketEllerBidragJournalpostMedTemaFar() }

        return ResponseEntity(journalposter, HttpStatus.OK)
    }

    @GetMapping("/journal/{journalpostIdForKildesystem}")
    @Operation(
        security = [SecurityRequirement(name = "bearer-key")],
        description = "Hent en journalpost for en id på formatet [" + BidragDokumentConfig.PREFIX_BIDRAG + '|' + BidragDokumentConfig.PREFIX_JOARK + ']' + BidragDokumentConfig.DELIMTER + "<journalpostId>"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Journalpost er hentet"),
            ApiResponse(
                responseCode = "400",
                description = "Ukjent/ugyldig journalpostId som har/mangler prefix"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Journalposten som skal hentes eksisterer ikke eller det er feil prefix/id på journalposten"
            )
        ]
    )
    @GlobalApiReponses
    fun hentJournalpost(
        @PathVariable journalpostIdForKildesystem: String,
        @Parameter(name = "saksnummer", description = "journalposten tilhører sak")
        @RequestParam(required = false)
        saksnummer: String?
    ): ResponseEntity<JournalpostResponse?> {
        var journalpostId = journalpostIdForKildesystem
        if (!Strings.isNullOrEmpty(journalpostIdForKildesystem) && journalpostIdForKildesystem.contains(
                ":"
            )
        ) {
            journalpostId =
                journalpostIdForKildesystem.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0]
            log.warn("Mottok ugyldig journalpostId $journalpostIdForKildesystem, forsøket korreksjon til journalpostId $journalpostId")
        }
        val kildesystemIdenfikator = KildesystemIdenfikator(journalpostId)
        if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
            return ResponseEntity(
                WebUtil.initHttpHeadersWith(
                    HttpHeaders.WARNING,
                    "Ugyldig prefix på journalpostId"
                ), HttpStatus.BAD_REQUEST
            )
        }
        log.info("Henter journalpost $journalpostId for saksnummer $saksnummer")
        val response = journalpostService.hentJournalpost(saksnummer, kildesystemIdenfikator)
        return response.clearContentHeaders().responseEntity
    }

    @GetMapping("/journal/{journalpostIdForKildesystem}/avvik")
    @Operation(
        security = [SecurityRequirement(name = "bearer-key")],
        description = "Henter mulige avvik for en journalpost, id på formatet [" + BidragDokumentConfig.PREFIX_BIDRAG + '|' + BidragDokumentConfig.PREFIX_JOARK + ']' + BidragDokumentConfig.DELIMTER +
                "<journalpostId>"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Tilgjengelig avvik for journalpost er hentet"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Fant ikke journalpost som det skal hentes avvik på"
            )
        ]
    )
    @GlobalApiReponses
    fun hentAvvik(
        @PathVariable journalpostIdForKildesystem: String,
        @Parameter(name = "saksnummer", description = "journalposten tilhører sak")
        @RequestParam(required = false)
        saksnummer: String?
    ): ResponseEntity<List<AvvikType>> {
        log.info("Henter avvik for journalpost $journalpostIdForKildesystem")
        val kildesystemIdenfikator = KildesystemIdenfikator(journalpostIdForKildesystem)
        return if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
            ResponseEntity(
                WebUtil.initHttpHeadersWith(
                    HttpHeaders.WARNING,
                    "Ugyldig prefix på journalpostId"
                ),
                HttpStatus.BAD_REQUEST
            )
        } else {
            journalpostService.finnAvvik(saksnummer, kildesystemIdenfikator).responseEntity
        }
    }

    @PostMapping(
        value = ["/journal/{journalpostIdForKildesystem}/avvik"],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        security = [SecurityRequirement(name = "bearer-key")],
        description = "Lagrer et avvik for en journalpost, id på formatet [" + BidragDokumentConfig.PREFIX_BIDRAG + '|' + BidragDokumentConfig.PREFIX_JOARK + ']' + BidragDokumentConfig.DELIMTER + "<journalpostId>"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Avvik på journalpost er behandlet"),
            ApiResponse(
                responseCode = "400",
                description = """
          En av følgende:
          - prefiks på journalpostId er ugyldig
          - avvikstypen mangler i avvikshendelsen
          - enhetsnummer i header (X_ENHET) mangler
          - ugyldig behandling av avvikshendelse (som bla. inkluderer):
            - oppretting av oppgave feiler
            - BESTILL_SPLITTING: beskrivelse må være i avvikshendelsen
            - OVERFOR_TIL_ANNEN_ENHET: nyttEnhetsnummer og gammeltEnhetsnummer må være i detaljer map
          
          """
            ),
            ApiResponse(
                responseCode = "503",
                description = "Oppretting av oppgave for avviket feilet"
            )
        ]
    )
    @GlobalApiReponses
    fun behandleAvvik(
        @RequestHeader(EnhetFilter.X_ENHET_HEADER) enhet: String,
        @PathVariable journalpostIdForKildesystem: String,
        @RequestBody avvikshendelse: Avvikshendelse
    ): ResponseEntity<BehandleAvvikshendelseResponse> {
        log.info(
            "Behandler avvik for journalpost $journalpostIdForKildesystem",
            journalpostIdForKildesystem
        )
        sikkerLogg.info("Behandler avvik for journalpost $journalpostIdForKildesystem med avvikshendelse $avvikshendelse")
        try {
            AvvikType.valueOf(avvikshendelse.avvikType)
        } catch (e: Exception) {
            val message =
                "Ukjent avvikstype: ${avvikshendelse.avvikType}, exception: ${e.javaClass.simpleName}: ${e.message}"
            log.warn(message)
            return ResponseEntity(
                WebUtil.initHttpHeadersWith(HttpHeaders.WARNING, message),
                HttpStatus.BAD_REQUEST
            )
        }
        val kildesystemIdenfikator = KildesystemIdenfikator(journalpostIdForKildesystem)
        return if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
            ResponseEntity(
                WebUtil.initHttpHeadersWith(
                    HttpHeaders.WARNING,
                    "Ugyldig prefix på journalpostId"
                ), HttpStatus.BAD_REQUEST
            )
        } else {
            journalpostService.behandleAvvik(
                enhet,
                kildesystemIdenfikator,
                avvikshendelse
            ).responseEntity
        }
    }

    @PatchMapping("/journal/{journalpostIdForKildesystem}")
    @Operation(
        security = [SecurityRequirement(name = "bearer-key")],
        summary = "Endre eksisterende journalpost, id på formatet [" + BidragDokumentConfig.PREFIX_BIDRAG + '|' + BidragDokumentConfig.PREFIX_JOARK + ']' + BidragDokumentConfig.DELIMTER + "<journalpostId>"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Journalpost er endret (eller registrert/journalført når payload inkluderer \"skalJournalfores\":\"true\")"
            ),
            ApiResponse(
                responseCode = "400",
                description = """
          En av følgende:
          - prefiks på journalpostId er ugyldig
          - EndreJournalpostCommandDto.gjelder er ikke satt og det finnes dokumenter tilknyttet journalpost
          - enhet mangler/ugyldig (fra header)
          - journalpost skal journalføres, men har ikke sakstilknytninger
          """
            ),
            ApiResponse(responseCode = "404", description = "Fant ikke journalpost som skal endres")
        ]
    )
    @GlobalApiReponses
    fun patchJournalpost(
        @RequestBody endreJournalpostCommand: EndreJournalpostCommand,
        @PathVariable journalpostIdForKildesystem: String,
        @RequestHeader(EnhetFilter.X_ENHET_HEADER) enhet: String
    ): ResponseEntity<Void> {
        log.info("Endrer journalpost $journalpostIdForKildesystem")
        sikkerLogg.info("Endrer journalpost $journalpostIdForKildesystem med endringer $endreJournalpostCommand")
        val kildesystemIdenfikator = KildesystemIdenfikator(journalpostIdForKildesystem)
        if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
            return ResponseEntity(
                WebUtil.initHttpHeadersWith(
                    HttpHeaders.WARNING,
                    "Ugyldig prefix på journalpostId"
                ), HttpStatus.BAD_REQUEST
            )
        }
        val journalpostCommandMedKildesystem = endreJournalpostCommand.copy(journalpostId = journalpostIdForKildesystem)
        return journalpostService.endre(
            enhet,
            kildesystemIdenfikator,
            journalpostCommandMedKildesystem
        ).responseEntity
    }

    @PostMapping("/journalpost/{arkivSystem}")
    @Operation(
        security = [SecurityRequirement(name = "bearer-key")],
        description = """
          Opprett notat eller utgående journalpost i midlertidlig brevlager.
          Opprett inngående, notat eller utgående journalpost i Joark
          """
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Journalpost er opprettet"),
            ApiResponse(responseCode = "400", description = "Input inneholder ugyldig data")
        ]
    )
    @GlobalApiReponses
    fun opprettJournalpost(
        @RequestBody opprettJournalpostRequest: OpprettJournalpostRequest,
        @PathVariable arkivSystem: ArkivSystem
    ): ResponseEntity<OpprettJournalpostResponse> {
        sikkerLogg.info("Oppretter journalpost $opprettJournalpostRequest for arkivsystem $arkivSystem")
        return journalpostService.opprett(opprettJournalpostRequest, arkivSystem).responseEntity
    }

    @PostMapping("/journal/distribuer/{joarkJournalpostId}")
    @Operation(description = "Bestill distribusjon av journalpost")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Distribusjon av journalpost er bestilt"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Journalpost mangler mottakerid eller adresse er ikke oppgitt i kallet"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Fant ikke journalpost som skal distribueres"
            )
        ]
    )
    @GlobalApiReponses
    @ResponseBody
    fun distribuerJournalpost(
        @RequestBody(required = false) distribuerJournalpostRequest: DistribuerJournalpostRequest?,
        @PathVariable joarkJournalpostId: String,
        @RequestParam(required = false) batchId: String?
    ): ResponseEntity<DistribuerJournalpostResponse> {
        log.info("Distribuerer journalpost $joarkJournalpostId")
        val kildesystemIdenfikator = KildesystemIdenfikator(joarkJournalpostId)
        if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
            val msgBadRequest = "Id har ikke riktig prefix: $joarkJournalpostId"
            log.warn(msgBadRequest)
            return ResponseEntity
                .badRequest()
                .header(HttpHeaders.WARNING, msgBadRequest)
                .build()
        }
        return journalpostService.distribuerJournalpost(
            batchId,
            kildesystemIdenfikator,
            distribuerJournalpostRequest ?: DistribuerJournalpostRequest()
        ).responseEntity
    }

    @GetMapping("/journal/distribuer/{journalpostId}/enabled")
    @Operation(description = "Sjekk om distribusjon av journalpost kan bestilles")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Distribusjon av journalpost kan bestilles"
            ),
            ApiResponse(
                responseCode = "406",
                description = "Distribusjon av journalpost kan ikke bestilles"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Fant ikke journalpost som skal distribueres"
            )
        ]
    )
    @GlobalApiReponses
    @ResponseBody
    fun kanDistribuerJournalpost(@PathVariable journalpostId: String): ResponseEntity<Void> {
        log.info("Sjekker om journalpost $journalpostId kan distribueres")
        val kildesystemIdenfikator = KildesystemIdenfikator(journalpostId)
        if (kildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix()) {
            val msgBadRequest = "Id har ikke riktig prefix: $journalpostId"
            log.warn(msgBadRequest)
            return ResponseEntity
                .badRequest()
                .header(HttpHeaders.WARNING, msgBadRequest)
                .build()
        }
        return journalpostService.kanDistribuereJournalpost(kildesystemIdenfikator).responseEntity
    }

    @GetMapping("/journal/distribuer/info/{journalpostId}")
    @Operation(description = "Hent informasjon om distribusjon av journalpost")
    @ResponseBody
    fun hentDistribusjonsInfo(@PathVariable journalpostId: String): ResponseEntity<DistribusjonInfoDto> {
        log.info("Henter distribusjonsinfo for journalpost {}", journalpostId)
        val kildesystemIdenfikator = JournalpostId(journalpostId)
        if (!kildesystemIdenfikator.erSystemJoark) {
            val msgBadRequest = String.format("Id har ikke riktig prefix: %s", journalpostId)
            log.warn(msgBadRequest)
            return ResponseEntity
                .badRequest()
                .header(HttpHeaders.WARNING, msgBadRequest)
                .build()
        }

        val distinfo = journalpostService.hentDistribusjonsInfo(kildesystemIdenfikator)
        return ResponseEntity.ok(distinfo)
    }

    companion object {
        private const val NON_DIGITS = "\\D+"
    }
}
