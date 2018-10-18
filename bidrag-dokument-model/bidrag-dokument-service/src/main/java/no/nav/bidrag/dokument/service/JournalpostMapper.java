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

    JournalpostDto fraBidragJournalpost(BidragJournalpostDto bidragJournalpostDto) {
        JournalpostDto journalpostDto = new JournalpostDto();
        journalpostDto.setAvsenderNavn(bidragJournalpostDto.getAvsender());
        journalpostDto.setDokumentDato(bidragJournalpostDto.getDokumentdato());
        journalpostDto.setDokumentreferanse(Collections.singletonList(bidragJournalpostDto.getDokumentreferanse()));
        journalpostDto.setDokumentType(bidragJournalpostDto.getDokumentType());
        journalpostDto.setFagomrade(bidragJournalpostDto.getFagomrade());
        journalpostDto.setGjelderBrukerId(Collections.singletonList(bidragJournalpostDto.getGjelder()));
        journalpostDto.setInnhold(bidragJournalpostDto.getBeskrivelse());
        journalpostDto.setJournalforendeEnhet(bidragJournalpostDto.getJournalforendeEnhet());
        journalpostDto.setJournalfortAv(bidragJournalpostDto.getJournalfortAv());
        journalpostDto.setJournalfortDato(bidragJournalpostDto.getJournaldato());
        journalpostDto.setJournalpostIdBisys(bidragJournalpostDto.getJournalpostId());
        journalpostDto.setMottattDato(bidragJournalpostDto.getMottattDato());
        journalpostDto.setSaksnummerBidrag(bidragJournalpostDto.getSaksnummer());

        return journalpostDto;
    }
}
