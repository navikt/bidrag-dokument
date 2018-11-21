package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.PrefixUtil;
import no.nav.bidrag.dokument.dto.BrevlagerJournalpostDto;
import no.nav.bidrag.dokument.dto.DokumentDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class JournalpostMapper {

    JournalpostDto fraBrevlagerJournalpostDto(BrevlagerJournalpostDto brevlagerJournalpostDto) {
        JournalpostDto journalpostDto = new JournalpostDto();
        journalpostDto.setAvsenderNavn(brevlagerJournalpostDto.getAvsender());
        journalpostDto.setDokumentDato(brevlagerJournalpostDto.getDokumentdato());
        journalpostDto.setDokumenter(Collections.singletonList(tilDokumentDto(brevlagerJournalpostDto)));
        journalpostDto.setFagomrade(brevlagerJournalpostDto.getFagomrade());
        journalpostDto.setGjelderBrukerId(Collections.singletonList(brevlagerJournalpostDto.getGjelder()));
        journalpostDto.setInnhold(brevlagerJournalpostDto.getBeskrivelse());
        journalpostDto.setJournalforendeEnhet(brevlagerJournalpostDto.getJournalforendeEnhet());
        journalpostDto.setJournalfortAv(brevlagerJournalpostDto.getJournalfortAv());
        journalpostDto.setJournalfortDato(brevlagerJournalpostDto.getJournaldato());
        journalpostDto.setJournalpostId("BID-" + brevlagerJournalpostDto.getJournalpostId());
        journalpostDto.setMottattDato(brevlagerJournalpostDto.getMottattDato());
        journalpostDto.setSaksnummer("BID-" + brevlagerJournalpostDto.getSaksnummer());

        return journalpostDto;
    }

    private DokumentDto tilDokumentDto(BrevlagerJournalpostDto brevlagerJournalpostDto) {
        DokumentDto dokumentDto = new DokumentDto();
        dokumentDto.setDokumentreferanse(brevlagerJournalpostDto.getDokumentreferanse());
        dokumentDto.setTittel(brevlagerJournalpostDto.getBeskrivelse());
        dokumentDto.setDokumentType(brevlagerJournalpostDto.getDokumentType());

        return dokumentDto;
    }

    BrevlagerJournalpostDto tilBidragJournalpost(JournalpostDto journalpostDto) {

        BrevlagerJournalpostDto brevlagerJournalpostDto = new BrevlagerJournalpostDto();
        brevlagerJournalpostDto.setAvsender(journalpostDto.getAvsenderNavn());
        brevlagerJournalpostDto.setBeskrivelse(journalpostDto.getInnhold());
        brevlagerJournalpostDto.setDokumentdato(journalpostDto.getDokumentDato());
        brevlagerJournalpostDto.setGjelder(fetchFirstOrFail(journalpostDto.getGjelderBrukerId()));
        brevlagerJournalpostDto.setJournalforendeEnhet(journalpostDto.getJournalforendeEnhet());
        brevlagerJournalpostDto.setJournalfortAv(journalpostDto.getJournalfortAv());
        brevlagerJournalpostDto.setJournaldato(journalpostDto.getJournalfortDato());
        brevlagerJournalpostDto.setJournalpostId(PrefixUtil.extract(journalpostDto.getJournalpostId()));
        brevlagerJournalpostDto.setMottattDato(journalpostDto.getMottattDato());
        brevlagerJournalpostDto.setSaksnummer(journalpostDto.getSaksnummer());

        DokumentDto dokumentDto = fetchFirstOrFail(journalpostDto.getDokumenter());
        brevlagerJournalpostDto.setDokumentreferanse(dokumentDto.getDokumentreferanse());
        brevlagerJournalpostDto.setDokumentType(dokumentDto.getDokumentType());

        return brevlagerJournalpostDto;
    }

    private <T> T fetchFirstOrFail(List<T> list) {
        if (list.size() == 1) {
            return list.get(0);
        }

        throw new IllegalArgumentException("Unable to fetch item from " + list);
    }
}
