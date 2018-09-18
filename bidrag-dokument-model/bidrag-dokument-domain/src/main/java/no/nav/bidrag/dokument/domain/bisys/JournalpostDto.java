package no.nav.bidrag.dokument.domain.bisys;

import no.nav.bidrag.dokument.domain.JournalTilstand;
import no.nav.bidrag.dokument.domain.joark.JournalforingDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDate;

public class JournalpostDto {

    private JournalTilstand journalTilstand;
    private LocalDate journalfortDato;

    @SuppressWarnings("WeakerAccess") /* brukes også av jackson */ public JournalpostDto() {
    }

    private JournalpostDto with(JournalforingDto journalforingDto) {
        journalTilstand = journalforingDto.getJournalTilstand();

        return this;
    }

    @Override public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("journalTilstand", journalTilstand)
                .toString();
    }

    // gettere og settere

    public String getHello() {
        return "hello from bidrag-dokument";
    }

    @SuppressWarnings("unused") /* brukes av json */ public LocalDate getJournalfortDato() {
        return journalfortDato;
    }

    @SuppressWarnings("unused") /* brukes av json */ public void setJournalfortDato(LocalDate journalfortDato) {
        this.journalfortDato = journalfortDato;
    }

    public JournalTilstand getJournalTilstand() {
        return journalTilstand;
    }

    @SuppressWarnings("unused")/* brukes av json */ public void setJournalTilstand(JournalTilstand journalTilstand) {
        this.journalTilstand = journalTilstand;
    }

    // static methods

    public static JournalpostDto populate(JournalforingDto journalforingDto) {
        return new JournalpostDto().with(journalforingDto);
    }
}
