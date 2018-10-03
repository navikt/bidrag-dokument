package no.nav.bidrag.dokument.domain;

import no.nav.bidrag.dokument.domain.bisys.BidragJournalpostDto;
import no.nav.bidrag.dokument.domain.joark.JournalforingDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDate;

@SuppressWarnings("unused") /* properties brukes av json-mapping */
public class JournalpostDto {
    private JournalTilstand journalTilstand;
    private LocalDate dokumentDato;
    private LocalDate journalfortDato;

    public JournalpostDto fraJournalforing(JournalforingDto journalforingDto) {
        journalTilstand = journalforingDto.getJournalTilstand();

        return this;
    }

    public JournalpostDto fraBidragJournalpost(BidragJournalpostDto bidragJournalpostDtoDto) {
        return this;
    }

    @Override public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("journalTilstand", journalTilstand)
                .toString();
    }

    // getters & setters

    public String getHello() {
        return "hello from bidrag-dokument";
    }

    public LocalDate getDokumentDato() {
        return dokumentDato;
    }

    public void setDokumentDato(LocalDate dokumentDato) {
        this.dokumentDato = dokumentDato;
    }

    public LocalDate getJournalfortDato() {
        return journalfortDato;
    }

    public void setJournalfortDato(LocalDate journalfortDato) {
        this.journalfortDato = journalfortDato;
    }

    public JournalTilstand getJournalTilstand() {
        return journalTilstand;
    }

    public void setJournalTilstand(JournalTilstand journalTilstand) {
        this.journalTilstand = journalTilstand;
    }
}
