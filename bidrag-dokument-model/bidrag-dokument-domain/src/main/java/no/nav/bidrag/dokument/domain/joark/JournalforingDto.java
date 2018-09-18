package no.nav.bidrag.dokument.domain.joark;

import no.nav.bidrag.dokument.domain.JournalTilstand;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class JournalforingDto {

    private JournalTilstand journalTilstand;
    private AvsenderDto avsender;

    //  NB: Etter journalføring er det saksparten på arkivsaken som er å anse som den formelle brukeren på journalposten
    private List<BrukerDto> brukere = Collections.emptyList();  // i utgangspunkt er det bare en bruker per journalpost.

    private ArkivSakDto arkivSak;
    private String fagomrade; // tema
    private String tittel;
    private String kanalReferanseId;
    private LocalDate forsendelseMottatt;
    private String mottakskanal;
    private String journalforendeEnhet;
    private List<DokumentDto> dokumenter = Collections.emptyList();

    public JournalTilstand getJournalTilstand() {
        return journalTilstand;
    }

    public void setJournalTilstand(JournalTilstand journalTilstand) {
        this.journalTilstand = journalTilstand;
    }

    public AvsenderDto getAvsender() {
        return avsender;
    }

    public void setAvsender(AvsenderDto avsender) {
        this.avsender = avsender;
    }

    public List<BrukerDto> getBrukere() {
        return brukere;
    }

    public void setBrukere(List<BrukerDto> brukere) {
        this.brukere = brukere;
    }

    public ArkivSakDto getArkivSak() {
        return arkivSak;
    }

    public void setArkivSak(ArkivSakDto arkivSak) {
        this.arkivSak = arkivSak;
    }

    public String getFagomrade() {
        return fagomrade;
    }

    public void setFagomrade(String fagomrade) {
        this.fagomrade = fagomrade;
    }

    public String getTittel() {
        return tittel;
    }

    public void setTittel(String tittel) {
        this.tittel = tittel;
    }

    public String getKanalReferanseId() {
        return kanalReferanseId;
    }

    public void setKanalReferanseId(String kanalReferanseId) {
        this.kanalReferanseId = kanalReferanseId;
    }

    public LocalDate getForsendelseMottatt() {
        return forsendelseMottatt;
    }

    public void setForsendelseMottatt(LocalDate forsendelseMottatt) {
        this.forsendelseMottatt = forsendelseMottatt;
    }

    public String getMottakskanal() {
        return mottakskanal;
    }

    public void setMottakskanal(String mottakskanal) {
        this.mottakskanal = mottakskanal;
    }

    public String getJournalforendeEnhet() {
        return journalforendeEnhet;
    }

    public void setJournalforendeEnhet(String journalforendeEnhet) {
        this.journalforendeEnhet = journalforendeEnhet;
    }

    public List<DokumentDto> getDokumenter() {
        return dokumenter;
    }

    public void setDokumenter(List<DokumentDto> dokumenter) {
        this.dokumenter = dokumenter;
    }

    public static JournalforingDtoBygger build() {
        return new JournalforingDtoBygger(new JournalforingDto());
    }

    public static class JournalforingDtoBygger {
        private JournalforingDto journalforingDto;

        private JournalforingDtoBygger(JournalforingDto journalforingDto) {
            this.journalforingDto = journalforingDto;
        }

        public JournalforingDtoBygger with(JournalTilstand journalTilstand) {
            journalforingDto.setJournalTilstand(journalTilstand);
            return this;
        }

        public JournalforingDto get() {
            return journalforingDto;
        }
    }
}
