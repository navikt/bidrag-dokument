package no.nav.bidrag.dokument.domain;

import no.nav.bidrag.dokument.domain.dto.JournalforingDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Journalpost {

    private JournalTilstand journalTilstand;

    @SuppressWarnings("unused") public Journalpost() { // brukes av jackson
    }

    public Journalpost(JournalforingDto journalforingDto) {
        journalTilstand = journalforingDto.getJournalTilstand();
    }

    @Override public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("journalTilstand", journalTilstand)
                .toString();
    }

    public JournalTilstand getJournalTilstand() {
        return journalTilstand;
    }

    public void setJournalTilstand(JournalTilstand journalTilstand) {
        this.journalTilstand = journalTilstand;
    }

    public static JournalpostBygger create() {
        return new JournalpostBygger();
    }

    public static class JournalpostBygger {
        private JournalforingDto journalforingDto = new JournalforingDto();

        private JournalpostBygger() {
        }

        public JournalpostBygger with(JournalTilstand journalTilstand) {
            journalforingDto.setJournalTilstand(journalTilstand);
            return this;
        }

        public Journalpost build() {
            return new Journalpost(journalforingDto);
        }
    }
}
