package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.dto.DokumentDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.bisys.BidragJournalpostDto;
import no.nav.bidrag.dokument.dto.joark.ArkivSakDto;
import no.nav.bidrag.dokument.dto.joark.AvsenderDto;
import no.nav.bidrag.dokument.dto.joark.BrukerDto;
import no.nav.bidrag.dokument.dto.joark.JoarkDokumentDto;
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
                () -> assertThat(journalpostDto.getJournalpostIdBisys()).as("journalpostId -> journalpostIdBisys").isEqualTo(101),
                () -> assertThat(journalpostDto.getMottattDato()).as("mottattDato").isEqualTo(LocalDate.now().minusDays(2)),
                () -> assertThat(journalpostDto.getSaksnummerBidrag()).as("saksnummer -> saksnummerBidrag").isEqualTo("10101")
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
                () -> assertThat(journalpostDto.getJournalpostIdJoark()).as("journalpostIdBisys").isEqualTo(journalforingDto.getJournalpostId()),
                () -> assertThat(journalpostDto.getMottattDato()).as("mottattDato").isEqualTo(journalforingDto.getDatoMottatt()),
                () -> assertThat(journalpostDto.getSaksnummerGsak()).as("saksnummerGsak").isEqualTo(journalforingDto.getArkivSak().getId())
        );
    }

    @SuppressWarnings("SameParameterValue") private class JournalforingBuilder {
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
}