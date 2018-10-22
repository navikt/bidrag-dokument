package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.consumer.dto.DokumentDto;
import no.nav.bidrag.dokument.consumer.dto.JournalpostDto;
import no.nav.bidrag.dokument.consumer.dto.bisys.BidragJournalpostDto;
import no.nav.bidrag.dokument.consumer.dto.joark.BrukerDto;
import no.nav.bidrag.dokument.consumer.dto.joark.JoarkDokumentDto;
import no.nav.bidrag.dokument.consumer.dto.joark.JournalforingDto;

import java.util.Collections;
import java.util.stream.Collectors;

public class JournalpostMapper {

    JournalpostDto fraJournalforing(JournalforingDto journalforingDto) {
        JournalpostDto journalpostDto = new JournalpostDto();
        journalpostDto.setAvsenderNavn(journalforingDto.getAvsenderDto() != null ? journalforingDto.getAvsenderDto().getAvsender() : null);
        journalpostDto.setFagomrade(journalforingDto.getFagomrade());
        journalpostDto.setDokumentDato(journalforingDto.getDatoDokument());
        journalpostDto.setJournaltilstand(journalforingDto.getJournalTilstand());
        journalpostDto.setDokumenter(journalforingDto.getDokumenter().stream().map(this::tilDokumentDto).collect(Collectors.toList()));
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

    private DokumentDto tilDokumentDto(JoarkDokumentDto joarkDokumentDto) {
        DokumentDto dokumentDto = new DokumentDto();
        dokumentDto.setDokumentreferanse(joarkDokumentDto.getDokumentId());
        dokumentDto.setTittel(joarkDokumentDto.getTittel());
        dokumentDto.setDokumentType(joarkDokumentDto.getDokumentTypeId());

        return dokumentDto;
    }

    JournalpostDto fraBidragJournalpost(BidragJournalpostDto bidragJournalpostDto) {
        JournalpostDto journalpostDto = new JournalpostDto();
        journalpostDto.setAvsenderNavn(bidragJournalpostDto.getAvsender());
        journalpostDto.setDokumentDato(bidragJournalpostDto.getDokumentdato());
        journalpostDto.setDokumenter(Collections.singletonList(tilDokumentDto(bidragJournalpostDto)));
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

    private DokumentDto tilDokumentDto(BidragJournalpostDto bidragJournalpostDto) {
        DokumentDto dokumentDto = new DokumentDto();
        dokumentDto.setDokumentreferanse(bidragJournalpostDto.getDokumentreferanse());
        dokumentDto.setTittel(bidragJournalpostDto.getBeskrivelse());
        dokumentDto.setDokumentType(bidragJournalpostDto.getDokumentType());

        return dokumentDto;
    }
}
