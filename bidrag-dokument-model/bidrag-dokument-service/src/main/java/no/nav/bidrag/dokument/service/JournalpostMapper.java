package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.domain.bisys.BidragJournalpostDto;
import no.nav.bidrag.dokument.domain.joark.JournalforingDto;
import no.nav.bidrag.dokument.domain.JournalpostDto;

final class JournalpostMapper {
    private JournalpostMapper() {
        // utility class
    }

    static JournalpostDto fraJournalforing(JournalforingDto journalforingDto) {
        JournalpostDto journalpostDto = new JournalpostDto();
        journalpostDto.setJournaltilstand(journalforingDto.getJournalTilstand());

        return journalpostDto;
    }

    static JournalpostDto fraBidragJournalpost(BidragJournalpostDto bidragJournalpostDtoDto) {
        return new JournalpostDto();
    }
}
