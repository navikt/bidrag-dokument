package no.nav.bidrag.dokument.domain.bisys;

import java.time.LocalDate;

@SuppressWarnings("unused") /* properties brukes av json-mapping */
public class BidragJournalpostDto {

    private String journalTilstand;
    private LocalDate journalfortDato;

    // gettere og settere

    public String getJournalTilstand() {
        return journalTilstand;
    }

    public void setJournalTilstand(String journalTilstand) {
        this.journalTilstand = journalTilstand;
    }

    public LocalDate getJournalfortDato() {
        return journalfortDato;
    }

    public void setJournalfortDato(LocalDate journalfortDato) {
        this.journalfortDato = journalfortDato;
    }
}
