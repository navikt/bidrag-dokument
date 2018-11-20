package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.JournalpostDtoBygger;
import no.nav.bidrag.dokument.dto.BrevlagerJournalpostDto;
import no.nav.bidrag.dokument.dto.DokumentDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("JournalpostMapperTest")
class JournalpostMapperTest {

    private JournalpostMapper journalpostMapper = new JournalpostMapper();

    @DisplayName("skal mappe fra BrevlagerJournalpostDto")
    @Test void skalMappeFraBrevlagerJournalpostDto() {
        BrevlagerJournalpostDto brevlagerJournalpostDto = new BrevlagerJournalpostDto();
        brevlagerJournalpostDto.setAvsender("Bi Drag");
        brevlagerJournalpostDto.setBeskrivelse("...and know, something completely different...");
        brevlagerJournalpostDto.setDokumentdato(LocalDate.now().minusDays(3));
        brevlagerJournalpostDto.setDokumentreferanse("101010101");
        brevlagerJournalpostDto.setDokumentType("N");
        brevlagerJournalpostDto.setGjelder("06127412345");
        brevlagerJournalpostDto.setJournalforendeEnhet("JUnit");
        brevlagerJournalpostDto.setJournaldato(LocalDate.now().minusDays(1));
        brevlagerJournalpostDto.setJournalfortAv("Dr. A. Cula");
        brevlagerJournalpostDto.setJournalpostId(101);
        brevlagerJournalpostDto.setMottattDato(LocalDate.now().minusDays(2));
        brevlagerJournalpostDto.setSaksnummer("10101");

        JournalpostDto journalpostDto = journalpostMapper.fraBrevlagerJournalpostDto(brevlagerJournalpostDto);
        assertThat(journalpostDto).isNotNull();

        assertAll(
                () -> assertThat(journalpostDto.getAvsenderNavn()).as("avsender -> avsenderNavn").isEqualTo("Bi Drag"),
                () -> assertThat(journalpostDto.getInnhold()).as("beskrivelse -> innhold").isEqualTo("...and know, something completely different..."),
                () -> assertThat(journalpostDto.getFagomrade()).as("fagomrade").isEqualTo("BID"),
                () -> assertThat(journalpostDto.getDokumentDato()).as("dokumentdato -> dokumentDato").isEqualTo(LocalDate.now().minusDays(3)),
                () -> assertThat(journalpostDto.getDokumenter()).extracting(DokumentDto::getDokumentreferanse).as("dokumentreferanse").contains("101010101"),
                () -> assertThat(journalpostDto.getDokumenter()).extracting(DokumentDto::getDokumentType).as("dokumentType").isEqualTo(singletonList("N")),
                () -> assertThat(journalpostDto.getGjelderBrukerId()).as("gjelder -> gjelderBrukerId").contains("06127412345"),
                () -> assertThat(journalpostDto.getJournalforendeEnhet()).as("journalforendeEnhet").isEqualTo("JUnit"),
                () -> assertThat(journalpostDto.getJournalfortAv()).as("journalfortAv").isEqualTo("Dr. A. Cula"),
                () -> assertThat(journalpostDto.getJournalfortDato()).as("journaldato -> journalfortDato").isEqualTo(LocalDate.now().minusDays(1)),
                () -> assertThat(journalpostDto.getJournalpostId()).as("journalpostId").isEqualTo("BID-101"),
                () -> assertThat(journalpostDto.getMottattDato()).as("mottattDato").isEqualTo(LocalDate.now().minusDays(2)),
                () -> assertThat(journalpostDto.getSaksnummer()).as("saksnummer").isEqualTo("BID-10101")
        );
    }

    @DisplayName("skal mappe til JournalpostDto")
    @Test void skalMappeTilJournalpostDto() {
        JournalpostDto journalpostDto = new JournalpostDtoBygger()
                .medAvsenderNavn("Kom Post")
                .medDokumentdato(LocalDate.now())
                .medDokumentreferanse("10101")
                .medDokumentType("Røyksignal")
                .medFagomrade("BID")
                .medGjelderBrukerId("06127412345")
                .medInnhold("from russia with love")
                .medJournalforendeEnhet("trygdekontoret")
                .medJournalfortAv("tobias")
                .medJournalfortDato(LocalDate.now().plusDays(1))
                .medJournalpostId("BID-123")
                .medMottattDato(LocalDate.now().minusDays(1))
                .medSaksnummer("123456789")
                .build();

        BrevlagerJournalpostDto bidragJournalpostDto = journalpostMapper.tilBidragJournalpost(journalpostDto);
        assertThat(bidragJournalpostDto).isNotNull();

        assertAll(
                () -> assertThat(bidragJournalpostDto.getAvsender()).as("avsender").isEqualTo(journalpostDto.getAvsenderNavn()),
                () -> assertThat(bidragJournalpostDto.getBeskrivelse()).as("beskrivelse").isEqualTo(journalpostDto.getInnhold()),
                () -> assertThat(bidragJournalpostDto.getDokumentdato()).as("dokumentdato").isEqualTo(journalpostDto.getDokumentDato()),
                () -> assertThat(bidragJournalpostDto.getDokumentreferanse()).as("dokumentreferanse").isEqualTo(journalpostDto.getDokumenter().get(0).getDokumentreferanse()),
                () -> assertThat(bidragJournalpostDto.getDokumentType()).as("dokumenttype").isEqualTo(journalpostDto.getDokumenter().get(0).getDokumentType()),
                () -> assertThat(bidragJournalpostDto.getFagomrade()).as("fagomrade").isEqualTo(journalpostDto.getFagomrade()),
                () -> assertThat(bidragJournalpostDto.getGjelder()).as("gjelder").isEqualTo(journalpostDto.getGjelderBrukerId().get(0)),
                () -> assertThat(bidragJournalpostDto.getJournalfortAv()).as("journalfortAv").isEqualTo(journalpostDto.getJournalfortAv()),
                () -> assertThat(bidragJournalpostDto.getJournalforendeEnhet()).as("journalforendeEnhet").isEqualTo("trygdekontoret"),
                () -> assertThat(bidragJournalpostDto.getJournaldato()).as("journaldato").isEqualTo(LocalDate.now().plusDays(1)),
                () -> assertThat(bidragJournalpostDto.getJournalpostId()).as("journalpostId").isEqualTo(123),
                () -> assertThat(bidragJournalpostDto.getMottattDato()).as("mottattDato").isEqualTo(LocalDate.now().minusDays(1)),
                () -> assertThat(bidragJournalpostDto.getSaksnummer()).as("saksnummer").isEqualTo("123456789")
        );
    }
}
