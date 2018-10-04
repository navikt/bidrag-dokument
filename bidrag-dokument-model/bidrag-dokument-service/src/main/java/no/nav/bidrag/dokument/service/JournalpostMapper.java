package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.domain.JournalpostDto;
import no.nav.bidrag.dokument.domain.bisys.BidragJournalpostDto;
import no.nav.bidrag.dokument.domain.joark.JournalforingDto;

public class JournalpostMapper {

    JournalpostDto fraJournalforing(JournalforingDto journalforingDto) {
        JournalpostDto journalpostDto = new JournalpostDto();
        journalpostDto.setJournaltilstand(journalforingDto.getJournalTilstand());

        return journalpostDto;
    }

    JournalpostDto fraBidragJournalpost(BidragJournalpostDto bidragJournalpostDtoDto) {
        JournalpostDto journalpostDto = new JournalpostDto();
        journalpostDto.setAvsenderNavn(bidragJournalpostDtoDto.getAvsender());
        journalpostDto.setDokumentDato(bidragJournalpostDtoDto.getDokumentdato());
        journalpostDto.setDokumentreferanse(bidragJournalpostDtoDto.getDokumentreferanse());
        journalpostDto.setDokumentType(bidragJournalpostDtoDto.getDokumentType());
        journalpostDto.setFagomrade(bidragJournalpostDtoDto.getFagomrade());
        journalpostDto.setGjelderBrukerId(bidragJournalpostDtoDto.getGjelder());
        journalpostDto.setInnhold(bidragJournalpostDtoDto.getBeskrivelse());
        journalpostDto.setJournalforendeEnhet(bidragJournalpostDtoDto.getJournalforendeEnhet());
        journalpostDto.setJournalfortAv(bidragJournalpostDtoDto.getJournalfortAv());
        journalpostDto.setJournalfortDato(bidragJournalpostDtoDto.getJournaldato());
        journalpostDto.setJounalpostIdBisys(bidragJournalpostDtoDto.getJournalpostId());
        journalpostDto.setMottattDato(bidragJournalpostDtoDto.getMottattDato());
        journalpostDto.setSaksnummerBidrag(bidragJournalpostDtoDto.getSaksnummer());

        return journalpostDto;
    }
}
