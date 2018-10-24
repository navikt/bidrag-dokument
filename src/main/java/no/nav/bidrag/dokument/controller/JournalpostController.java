package no.nav.bidrag.dokument.controller;

import io.swagger.annotations.ApiOperation;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.service.JournalpostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class JournalpostController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JournalpostController.class);

    private final JournalpostService journalpostService;

    @Autowired
    public JournalpostController(JournalpostService journalpostService) {
        this.journalpostService = journalpostService;
    }

    @GetMapping("/status")
    public @ResponseBody
    String get() {
        return "OK";
    }

    @GetMapping("/journalforing/{jid}")
    @ApiOperation("Finn journalpost for en journalført id")
    public ResponseEntity<JournalpostDto> get(@PathVariable Integer jid) {
        return journalpostService.hentJournalpost(jid)
                .map(journalpostDto -> new ResponseEntity<>(journalpostDto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @GetMapping("/journalpost/{saksnummer}")
    @ApiOperation("Finn journalposter for et saksnummer på en bidragssak")
    public ResponseEntity<List<JournalpostDto>> get(@PathVariable String saksnummer) {
        LOGGER.debug("request: bidrag-dokument/journalpost/" + saksnummer);
        List<JournalpostDto> journalposter = journalpostService.finnJournalposter(saksnummer);

        if (journalposter.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(journalposter, HttpStatus.OK);
    }
}
