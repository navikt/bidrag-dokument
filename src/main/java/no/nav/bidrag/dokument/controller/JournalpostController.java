package no.nav.bidrag.dokument.controller;

import io.swagger.annotations.ApiOperation;
import no.nav.bidrag.dokument.BidragDokument;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommandDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.NyJournalpostCommandDto;
import no.nav.bidrag.dokument.exception.KildesystemException;
import no.nav.bidrag.dokument.service.JournalpostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static no.nav.bidrag.dokument.BidragDokument.DELIMTER;
import static no.nav.bidrag.dokument.BidragDokument.PREFIX_BIDRAG;
import static no.nav.bidrag.dokument.BidragDokument.PREFIX_GSAK;
import static no.nav.bidrag.dokument.BidragDokument.PREFIX_JOARK;

@RestController
public class JournalpostController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JournalpostController.class);
    private static final String ENDPOINT_JOURNALPOST = "/journalpost";
    private static final String ENDPOINT_SAKJOURNAL = "/sakjournal";

    private final JournalpostService journalpostService;

    public JournalpostController(JournalpostService journalpostService) {
        this.journalpostService = journalpostService;
    }

    @GetMapping("/status")
    @ResponseBody public String get() {
        return "OK";
    }

    @GetMapping(ENDPOINT_JOURNALPOST + "/{journalpostIdForKildesystem}")
    @ApiOperation("Finn journalpost for en id på formatet [" + PREFIX_BIDRAG + '|' + PREFIX_JOARK + ']' + DELIMTER + "<journalpostId>")
    public ResponseEntity<JournalpostDto> hent(@PathVariable String journalpostIdForKildesystem) {
        LOGGER.debug("request: bidrag-dokument" + ENDPOINT_JOURNALPOST + '/' + journalpostIdForKildesystem);

        try {
            return journalpostService.hentJournalpost(journalpostIdForKildesystem)
                    .map(journalpostDto -> new ResponseEntity<>(journalpostDto, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        } catch (KildesystemException e) {
            LOGGER.warn("Ukjent kildesystem: " + e.getMessage());

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(ENDPOINT_SAKJOURNAL + "/{saksnummerForKildesystem}")
    @ApiOperation("Finn journalposter for et saksnummer i en sak på formatet [" + PREFIX_BIDRAG + "|" + PREFIX_GSAK + "]" + DELIMTER + "<saksnummer>")
    public ResponseEntity<List<JournalpostDto>> get(@PathVariable String saksnummerForKildesystem) {
        LOGGER.debug("request: bidrag-dokument" + ENDPOINT_JOURNALPOST + "/" + saksnummerForKildesystem);

        try {
            List<JournalpostDto> journalposter = journalpostService.finnJournalposter(saksnummerForKildesystem);

            if (journalposter.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(journalposter, HttpStatus.OK);
        } catch (KildesystemException e) {
            LOGGER.warn("Ukjent kildesystem: " + e.getMessage());

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(ENDPOINT_JOURNALPOST + "/ny")
    @ApiOperation("Registrer ny journalpost")
    public ResponseEntity<JournalpostDto> post(@RequestBody NyJournalpostCommandDto nyJournalpostCommandDto) {
        LOGGER.debug("post ny: {}\n \\-> bidrag-dokument/{}/ny", nyJournalpostCommandDto, ENDPOINT_JOURNALPOST);

        return journalpostService.registrer(nyJournalpostCommandDto)
                .map(journalpost -> new ResponseEntity<>(journalpost, HttpStatus.CREATED))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.OK));
    }

    @PostMapping(ENDPOINT_JOURNALPOST)
    @ApiOperation("Endre eksisterende journalpost")
    public ResponseEntity<JournalpostDto> post(@RequestBody EndreJournalpostCommandDto endreJournalpostCommandDto) {
        LOGGER.debug("post endret: {}\n \\-> bidrag-dokument/{}/endre", endreJournalpostCommandDto, ENDPOINT_JOURNALPOST);

        return journalpostService.endre(endreJournalpostCommandDto)
                .map(journalpost -> new ResponseEntity<>(journalpost, HttpStatus.CREATED))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.OK));
    }
}
