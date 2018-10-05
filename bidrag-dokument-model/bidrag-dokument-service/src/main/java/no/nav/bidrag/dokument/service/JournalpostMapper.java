package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.domain.JournalpostDto;
import no.nav.bidrag.dokument.domain.bisys.BidragJournalpostDto;
import no.nav.bidrag.dokument.domain.joark.BrukerDto;
import no.nav.bidrag.dokument.domain.joark.DokumentDto;
import no.nav.bidrag.dokument.domain.joark.JournalforingDto;

import java.util.Collections;
import java.util.stream.Collectors;

public class JournalpostMapper {

    JournalpostDto fraJournalforing(JournalforingDto journalforingDto) {
        JournalpostDto journalpostDto = new JournalpostDto();
        journalpostDto.setAvsenderNavn(journalforingDto.getAvsenderDto() != null ? journalforingDto.getAvsenderDto().getAvsender() : null);
        journalpostDto.setFagomrade(journalforingDto.getFagomrade());
        journalpostDto.setDokumentDato(journalforingDto.getDatoDokument());
        journalpostDto.setJournaltilstand(journalforingDto.getJournalTilstand());
        journalpostDto.setDokumentreferanse(journalforingDto.getDokumenter().stream().map(DokumentDto::getDokumentId).collect(Collectors.toList()));
        journalpostDto.setDokumentType(journalforingDto.getJournalpostType());
        journalpostDto.setGjelderBrukerId(journalforingDto.getBrukere().stream().map(BrukerDto::getBrukerId).collect(Collectors.toList()));
        journalpostDto.setInnhold(journalforingDto.getInnhold());
        journalpostDto.setJournalforendeEnhet(journalforingDto.getJournalforendeEnhet());
        journalpostDto.setJournalfortAv(journalforingDto.getJournalfortAvNavn());
        journalpostDto.setJournalfortDato(journalforingDto.getDatoJournal());
        journalpostDto.setJournalpostIdJoark(journalforingDto.getJournalpostId());
        journalpostDto.setMottattDato(journalforingDto.getDatoMottatt());
        journalpostDto.setSaksnummerGsak(journalforingDto.getArkivSak() != null ? journalforingDto.getArkivSak().getId() : null);

        return journalpostDto;
    }

    JournalpostDto fraBidragJournalpost(BidragJournalpostDto bidragJournalpostDtoDto) {
        JournalpostDto journalpostDto = new JournalpostDto();
        journalpostDto.setAvsenderNavn(bidragJournalpostDtoDto.getAvsender());
        journalpostDto.setDokumentDato(bidragJournalpostDtoDto.getDokumentdato());
        journalpostDto.setDokumentreferanse(Collections.singletonList(bidragJournalpostDtoDto.getDokumentreferanse()));
        journalpostDto.setDokumentType(bidragJournalpostDtoDto.getDokumentType());
        journalpostDto.setFagomrade(bidragJournalpostDtoDto.getFagomrade());
        journalpostDto.setGjelderBrukerId(Collections.singletonList(bidragJournalpostDtoDto.getGjelder()));
        journalpostDto.setInnhold(bidragJournalpostDtoDto.getBeskrivelse());
        journalpostDto.setJournalforendeEnhet(bidragJournalpostDtoDto.getJournalforendeEnhet());
        journalpostDto.setJournalfortAv(bidragJournalpostDtoDto.getJournalfortAv());
        journalpostDto.setJournalfortDato(bidragJournalpostDtoDto.getJournaldato());
        journalpostDto.setJournalpostIdBisys(bidragJournalpostDtoDto.getJournalpostId());
        journalpostDto.setMottattDato(bidragJournalpostDtoDto.getMottattDato());
        journalpostDto.setSaksnummerBidrag(bidragJournalpostDtoDto.getSaksnummer());

        return journalpostDto;
    }
}
