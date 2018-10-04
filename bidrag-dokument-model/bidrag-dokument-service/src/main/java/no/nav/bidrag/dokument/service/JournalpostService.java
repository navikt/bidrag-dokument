package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.consumer.JournalforingConsumer;
import no.nav.bidrag.dokument.domain.bisys.BidragJournalpostDto;
import no.nav.bidrag.dokument.domain.joark.JournalforingDto;
import no.nav.bidrag.dokument.domain.JournalpostDto;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class JournalpostService {

    private final BidragJournalpostConsumer bidragJournalpostConsumer;
    private final JournalforingConsumer journalforingConsumer;
    private final JournalpostMapper journalpostMapper;

    public JournalpostService(BidragJournalpostConsumer bidragJournalpostConsumer, JournalforingConsumer journalforingConsumer, JournalpostMapper journalpostMapper) {
        this.bidragJournalpostConsumer = bidragJournalpostConsumer;
        this.journalforingConsumer = journalforingConsumer;
        this.journalpostMapper = journalpostMapper;
    }

    public Optional<JournalpostDto> hentJournalpost(Integer id) {
        Optional<JournalforingDto> journalforingDtoRequest = journalforingConsumer.hentJournalforing(id);

        return journalforingDtoRequest.map(journalpostMapper::fraJournalforing);
    }

    public List<JournalpostDto> finnJournalposter(String bidragssaksnummer) {
        List<BidragJournalpostDto> bidragJournalpostDtoRequest = bidragJournalpostConsumer.finnJournalposter(bidragssaksnummer);

        return bidragJournalpostDtoRequest.stream()
                .map(journalpostMapper::fraBidragJournalpost)
                .collect(toList());
    }
}
