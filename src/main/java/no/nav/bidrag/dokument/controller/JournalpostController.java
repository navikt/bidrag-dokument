package no.nav.bidrag.dokument.controller;

import io.swagger.annotations.ApiOperation;
import no.nav.bidrag.dokument.BidragDokument;
import no.nav.bidrag.dokument.dto.DokumentDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
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
    @ApiOperation("Finn journalpost for en id på formatet <" + BidragDokument.JOURNALPOST_ID_BIDRAG_REQUEST + "|" + BidragDokument.JOURNALPOST_ID_JOARK_REQUEST + "journalpostId>")
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

    @GetMapping(ENDPOINT_SAKJOURNAL + "/{saksnummer}")
    @ApiOperation("Finn journalposter for et saksnummer på en bidragssak")
    public ResponseEntity<List<JournalpostDto>> get(@PathVariable String saksnummer) {
        LOGGER.debug("request: bidrag-dokument" + ENDPOINT_JOURNALPOST + "/" + saksnummer);
        List<JournalpostDto> journalposter = journalpostService.finnJournalposter(saksnummer);

        if (journalposter.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(journalposter, HttpStatus.OK);
    }

    @PostMapping(ENDPOINT_JOURNALPOST)
    @ApiOperation("Registrer ny journalpost")
    public ResponseEntity<JournalpostDto> post(@RequestBody JournalpostDto journalpostDto) {
        List<DokumentDto> dokumenter = journalpostDto.getDokumenter();
        List<String> gjelderBrukerId = journalpostDto.getGjelderBrukerId();

        if (harIngenEllerFlere(dokumenter) || harIngenEllerFlere(gjelderBrukerId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return journalpostService.save(journalpostDto)
                .map(journalpost -> new ResponseEntity<>(journalpost, HttpStatus.CREATED))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.OK));
    }

    private <T> boolean harIngenEllerFlere(List<T> items) {
        return items == null || items.isEmpty() || items.size() > 1;
    }
}
