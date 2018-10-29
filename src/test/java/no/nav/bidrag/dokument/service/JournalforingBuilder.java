package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.dto.joark.ArkivSakDto;
import no.nav.bidrag.dokument.dto.joark.AvsenderDto;
import no.nav.bidrag.dokument.dto.joark.BrukerDto;
import no.nav.bidrag.dokument.dto.joark.JoarkDokumentDto;
import no.nav.bidrag.dokument.dto.joark.JournalforingDto;

import java.time.LocalDate;

import static java.util.Collections.singletonList;

@SuppressWarnings("SameParameterValue") class JournalforingBuilder {
    private JournalforingDto journalforingDto = new JournalforingDto();

    JournalforingBuilder withArkivSakId(String arkivSakId) {
        journalforingDto.setArkivSak(new ArkivSakDto(arkivSakId, null));
        return this;
    }

    JournalforingBuilder withAvsender(String avsender) {
        AvsenderDto avsenderDto = new AvsenderDto();
        avsenderDto.setAvsender(avsender);
        journalforingDto.setAvsenderDto(avsenderDto);

        return this;
    }

    JournalforingBuilder withBruker(String brukerIdent) {
        journalforingDto.setBrukere(singletonList(initBrukerDto(brukerIdent)));
        return this;
    }

    private BrukerDto initBrukerDto(String brukerIdent) {
        return new BrukerDto(null, brukerIdent);
    }

    JournalforingBuilder withFagomrade(String fagomrade) {
        journalforingDto.setFagomrade(fagomrade);
        return this;
    }

    JournalforingBuilder withDatoDokument(LocalDate datoDokument) {
        journalforingDto.setDatoDokument(datoDokument);
        return this;
    }

    JournalforingBuilder withDatoJournal(LocalDate datoJournal) {
        journalforingDto.setDatoJournal(datoJournal);
        return this;
    }

    JournalforingBuilder withDatoMottatt(LocalDate datoMottatt) {
        journalforingDto.setDatoMottatt(datoMottatt);
        return this;
    }

    JournalforingBuilder withDokumentId(String dokumentId) {
        initJoarkDokumentDto().setDokumentId(dokumentId);
        return this;
    }

    private JoarkDokumentDto initJoarkDokumentDto() {
        if (journalforingDto.getDokumenter().isEmpty()) {
            journalforingDto.setDokumenter(singletonList(new JoarkDokumentDto()));
        }

        return journalforingDto.getDokumenter().get(0);
    }

    JournalforingBuilder withInnhold(String innhold) {
        journalforingDto.setInnhold(innhold);
        return this;
    }

    JournalforingBuilder withJournalforendeEnhet(String journalforendeEnhet) {
        journalforingDto.setJournalforendeEnhet(journalforendeEnhet);
        return this;
    }

    JournalforingBuilder withJournalfortAvNavn(String journalfortAvNavn) {
        journalforingDto.setJournalfortAvNavn(journalfortAvNavn);
        return this;
    }

    JournalforingBuilder withJournalpostId(int journalpostId) {
        journalforingDto.setJournalpostId(journalpostId);
        return this;
    }

    JournalforingBuilder withDokumentTypeId(String dokumentTypeId) {
        initJoarkDokumentDto().setDokumentTypeId(dokumentTypeId);
        return this;
    }

    JournalforingDto get() {
        return journalforingDto;
    }
}
