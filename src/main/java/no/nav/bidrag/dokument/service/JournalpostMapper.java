package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.DigitUtil;
import no.nav.bidrag.dokument.dto.DokumentDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.bisys.BidragJournalpostDto;
import no.nav.bidrag.dokument.dto.joark.BrukerDto;
import no.nav.bidrag.dokument.dto.joark.JoarkDokumentDto;
import no.nav.bidrag.dokument.dto.joark.JournalforingDto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
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
        journalpostDto.setJournalpostId(withPrefix("JOARK", journalforingDto.getJournalpostId()));
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
        journalpostDto.setJournalpostId(withPrefix("BID", bidragJournalpostDto.getJournalpostId()));
        journalpostDto.setMottattDato(bidragJournalpostDto.getMottattDato());
        journalpostDto.setSaksnummerBidrag(bidragJournalpostDto.getSaksnummer());

        return journalpostDto;
    }

    private String withPrefix(String prefix, Integer journalpostId) {
        return prefix + '-' + journalpostId;
    }

    private DokumentDto tilDokumentDto(BidragJournalpostDto bidragJournalpostDto) {
        DokumentDto dokumentDto = new DokumentDto();
        dokumentDto.setDokumentreferanse(bidragJournalpostDto.getDokumentreferanse());
        dokumentDto.setTittel(bidragJournalpostDto.getBeskrivelse());
        dokumentDto.setDokumentType(bidragJournalpostDto.getDokumentType());

        return dokumentDto;
    }

    BidragJournalpostDto tilBidragJournalpost(JournalpostDto journalpostDto) {

        BidragJournalpostDto bidragJournalpostDto = new BidragJournalpostDto();
        bidragJournalpostDto.setAvsender(journalpostDto.getAvsenderNavn());
        bidragJournalpostDto.setBeskrivelse(journalpostDto.getInnhold());
        bidragJournalpostDto.setDokumentdato(journalpostDto.getDokumentDato());
        bidragJournalpostDto.setGjelder(fetchFirstOrFail(journalpostDto.getGjelderBrukerId()));
        bidragJournalpostDto.setJournalforendeEnhet(journalpostDto.getJournalforendeEnhet());
        bidragJournalpostDto.setJournalfortAv(journalpostDto.getJournalfortAv());
        bidragJournalpostDto.setJournaldato(journalpostDto.getJournalfortDato());
        bidragJournalpostDto.setJournalpostId(DigitUtil.extract(journalpostDto.getJournalpostId()));
        bidragJournalpostDto.setMottattDato(journalpostDto.getMottattDato());
        bidragJournalpostDto.setSaksnummer(journalpostDto.getSaksnummerBidrag());

        DokumentDto dokumentDto = fetchFirstOrFail(journalpostDto.getDokumenter());
        bidragJournalpostDto.setDokumentreferanse(dokumentDto.getDokumentreferanse());
        bidragJournalpostDto.setDokumentType(dokumentDto.getDokumentType());
        bidragJournalpostDto.setFagomrade(journalpostDto.getFagomrade());

        return bidragJournalpostDto;
    }

    private <T> T fetchFirstOrFail(List<T> list) {
        if (list.size() == 1) {
            return list.get(0);
        }

        throw new IllegalArgumentException("Unable to fetch item from " + list);
    }
}
