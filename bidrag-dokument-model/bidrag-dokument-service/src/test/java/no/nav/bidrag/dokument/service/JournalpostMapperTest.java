package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.domain.JournalpostDto;
import no.nav.bidrag.dokument.domain.bisys.BidragJournalpostDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("JournalpostMapperTest")
class JournalpostMapperTest {

    @DisplayName("skal mappe fra BidragJournalpostDto")
    @Test void skalMappeFraBidragJournalpost() {
        BidragJournalpostDto bidragJournalpostDto = new BidragJournalpostDto();
        bidragJournalpostDto.setAvsender("Bi Drag");
        bidragJournalpostDto.setBeskrivelse("...and know, something completely different...");
        bidragJournalpostDto.setFagomrade("BID");
        bidragJournalpostDto.setDokumentdato(LocalDate.now().minusDays(3));
        bidragJournalpostDto.setDokumentreferanse("101010101");
        bidragJournalpostDto.setDokumentType("N");
        bidragJournalpostDto.setGjelder("06127412345");
        bidragJournalpostDto.setJournalforendeEnhet("JUnit");
        bidragJournalpostDto.setJournaldato(LocalDate.now().minusDays(1));
        bidragJournalpostDto.setJournalfortAv("Dr. A. Cula");
        bidragJournalpostDto.setJournalpostId(101);
        bidragJournalpostDto.setMottattDato(LocalDate.now().minusDays(2));
        bidragJournalpostDto.setSaksnummer("10101");

        JournalpostDto journalpostDto = new JournalpostMapper().fraBidragJournalpost(bidragJournalpostDto);
        assertThat(journalpostDto).isNotNull();

        assertAll(
                () -> assertThat(journalpostDto.getAvsenderNavn()).as("avsender -> avsenderNavn").isEqualTo("Bi Drag"),
                () -> assertThat(journalpostDto.getInnhold()).as(" -> innhold/beskrivelse").isEqualTo("...and know, something completely different..."),
                () -> assertThat(journalpostDto.getFagomrade()).as("fagomrade").isEqualTo("BID"),
                () -> assertThat(journalpostDto.getDokumentDato()).as(" -> dokumentDato/dokumentdato").isEqualTo(LocalDate.now().minusDays(3)),
                () -> assertThat(journalpostDto.getDokumentreferanse()).as("dokumentreferanse").isEqualTo("101010101"),
                () -> assertThat(journalpostDto.getDokumentType()).as("dokumentType").isEqualTo("N"),
                () -> assertThat(journalpostDto.getGjelderBrukerId()).as(" -> gjelderBrukerId/gjelder").isEqualTo("06127412345"),
                () -> assertThat(journalpostDto.getJournalforendeEnhet()).as(" -> journalforendeEnhet").isEqualTo("JUnit"),
                () -> assertThat(journalpostDto.getJournalfortDato()).as(" -> journaldato -> journalfortDato").isEqualTo(LocalDate.now().minusDays(1)),
                () -> assertThat(journalpostDto.getJournalfortAv()).as("journalfortAv").isEqualTo("Dr. A. Cula"),
                () -> assertThat(journalpostDto.getJounalpostIdBisys()).as("journalpostId -> journalpostIdBisys").isEqualTo(101),
                () -> assertThat(journalpostDto.getMottattDato()).as("mottattDato").isEqualTo(LocalDate.now().minusDays(2)),
                () -> assertThat(journalpostDto.getSaksnummerBidrag()).as("saksnummer -> saksnummerBidrag").isEqualTo("10101")
        );
    }
}