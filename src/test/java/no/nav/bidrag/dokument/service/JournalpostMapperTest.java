package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.JournalpostDtoBygger;
import no.nav.bidrag.dokument.dto.DokumentDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.bisys.BidragJournalpostDto;
import no.nav.bidrag.dokument.dto.joark.BrukerDto;
import no.nav.bidrag.dokument.dto.joark.JournalforingDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("JournalpostMapperTest")
class JournalpostMapperTest {

    private JournalpostMapper journalpostMapper = new JournalpostMapper();

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

        JournalpostDto journalpostDto = journalpostMapper.fraBidragJournalpost(bidragJournalpostDto);
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
                () -> assertThat(journalpostDto.getSaksnummerBidrag()).as("saksnummer -> saksnummerBidrag").isEqualTo("10101")
        );
    }

    @DisplayName("skal mappe fra bidrag-dokument journalpost til en journalpost i bidrag-dokument-journalpost")
    @Test void skalMappeFraJournalpostTilBidragJournalpost() {
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
                .medSaksnummerBidrag("123456789")
                .build();

        BidragJournalpostDto bidragJournalpostDto = journalpostMapper.tilBidragJournalpost(journalpostDto);
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

    @DisplayName("skal mappe fra JournalforingDto") @SuppressWarnings("ConstantConditions")
    @Test void skalMappeFraJournalforingDto() {
        JournalforingDto journalforingDto = new JournalforingBuilder()
                .withArkivSakId("10101")
                .withAvsender("U. A. N. Svarlig")
                .withBruker("06127412345")
                .withFagomrade("BID")
                .withDatoDokument(LocalDate.now().minusDays(3))
                .withDatoJournal(LocalDate.now().minusDays(1))
                .withDatoMottatt(LocalDate.now().minusDays(2))
                .withDokumentId("101")
                .withInnhold("...and know, something completely different...")
                .withJournalforendeEnhet("JUnit")
                .withJournalfortAvNavn("Dr. A. Cula")
                .withJournalpostId(101)
                .withDokumentTypeId("N")
                .get();

        JournalpostDto journalpostDto = journalpostMapper.fraJournalforing(journalforingDto);
        assertThat(journalpostDto).isNotNull();

        assertAll(
                () -> assertThat(journalpostDto.getAvsenderNavn()).as("avsenderNavn").isEqualTo(journalforingDto.getAvsenderDto().getAvsender()),
                () -> assertThat(journalpostDto.getFagomrade()).as("fagomrade").isEqualTo(journalforingDto.getFagomrade()),
                () -> assertThat(journalpostDto.getDokumentDato()).as("dokumentdato").isEqualTo(journalforingDto.getDatoDokument()),
                () -> assertThat(journalpostDto.getDokumenter()).extracting(DokumentDto::getDokumentreferanse).as("dokumentreferanse")
                        .isEqualTo(singletonList(journalforingDto.getDokumenter().get(0).getDokumentId())),
                () -> assertThat(journalpostDto.getDokumenter()).extracting(DokumentDto::getDokumentType).as("dokumentType")
                        .isEqualTo(singletonList(journalforingDto.getDokumenter().get(0).getDokumentTypeId())),
                () -> assertThat(journalpostDto.getGjelderBrukerId()).as("gjelderBrukerId")
                        .isEqualTo(journalforingDto.getBrukere().stream().map(BrukerDto::getBrukerId).collect(toList())),
                () -> assertThat(journalpostDto.getInnhold()).as("innhold").isEqualTo(journalforingDto.getInnhold()),
                () -> assertThat(journalpostDto.getJournalforendeEnhet()).as("journalforendeEnhet").isEqualTo(journalforingDto.getJournalforendeEnhet()),
                () -> assertThat(journalpostDto.getJournalfortAv()).as("journalfortAv").isEqualTo(journalforingDto.getJournalfortAvNavn()),
                () -> assertThat(journalpostDto.getJournalfortDato()).as("journalfortDato").isEqualTo(journalforingDto.getDatoJournal()),
                () -> assertThat(journalpostDto.getJournalpostId()).as("journalpostId").isEqualTo("JOARK-" + journalforingDto.getJournalpostId()),
                () -> assertThat(journalpostDto.getMottattDato()).as("mottattDato").isEqualTo(journalforingDto.getDatoMottatt()),
                () -> assertThat(journalpostDto.getSaksnummerGsak()).as("saksnummerGsak").isEqualTo(journalforingDto.getArkivSak().getId())
        );
    }

    @DisplayName("skal ignorere eventuell prefix når journalpostId mappes")
    @Test void skalIgnorePrefixPaJournalpostId() {
        JournalpostDto journalpostDto = new JournalpostDtoBygger()
                .medJournalpostId("svada123")
                .build();

        BidragJournalpostDto bidragJournalpostDto = journalpostMapper.tilBidragJournalpost(journalpostDto);

        assertThat(bidragJournalpostDto).isNotNull();
        assertThat(bidragJournalpostDto.getJournalpostId()).isEqualTo(123);
    }
}
