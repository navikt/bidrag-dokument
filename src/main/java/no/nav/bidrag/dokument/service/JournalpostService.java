package no.nav.bidrag.dokument.service;

import static no.nav.bidrag.dokument.BidragDokumentConfig.PREFIX_BIDRAG;
import static no.nav.bidrag.dokument.BidragDokumentConfig.PREFIX_JOARK;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import no.nav.bidrag.dokument.PrefixUtil;
import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.consumer.BidragSakConsumer;
import no.nav.bidrag.dokument.dto.BidragSakDto;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommandDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.NyJournalpostCommandDto;
import no.nav.bidrag.dokument.exception.KildesystemException;

@Component
public class JournalpostService {

    private final BidragJournalpostConsumer bidragJournalpostConsumer;
    private final BidragSakConsumer bidragSakConsumer;
    private final BidragArkivConsumer bidragArkivConsumer;

    public JournalpostService(BidragJournalpostConsumer bidragJournalpostConsumer, BidragSakConsumer bidragSakConsumer, BidragArkivConsumer bidragArkivConsumer) {
        this.bidragJournalpostConsumer = bidragJournalpostConsumer;
        this.bidragSakConsumer = bidragSakConsumer;
        this.bidragArkivConsumer = bidragArkivConsumer;
    }

    public Optional<JournalpostDto> hentJournalpost(String journalpostId) throws KildesystemException {
        try {
            if (PrefixUtil.startsWith(PREFIX_BIDRAG, journalpostId)) {
                Optional<JournalpostDto> muligJournalpostDto = bidragJournalpostConsumer.hentJournalpost(PrefixUtil.tryExtraction(journalpostId));
                muligJournalpostDto.ifPresent(
                        journalpostDto -> journalpostDto.setBidragssaker(finnBidragssaker(journalpostDto.getGjelderBrukerId())));

                return muligJournalpostDto;
            } else if (PrefixUtil.startsWith(PREFIX_JOARK, journalpostId)) {
                return bidragArkivConsumer.hentJournalpost(PrefixUtil.tryExtraction(journalpostId));
            }
        } catch (NumberFormatException nfe) {
            throw new KildesystemException("Kan ikke prosesseres som et tall: " + journalpostId);
        }

        throw new KildesystemException("Kunne ikke identifisere kildesystem for id: " + journalpostId);
    }

    private List<BidragSakDto> finnBidragssaker(List<String> gjelderBrukerId) {
        List<BidragSakDto> bidragssaker = new ArrayList<>();
        gjelderBrukerId.forEach(fnr -> bidragssaker.addAll(bidragSakConsumer.finnInnvolverteSaker(fnr)));

        return bidragssaker;
    }

    public List<JournalpostDto> finnJournalposter(String saksnummer, String fagomrade) {
        return bidragJournalpostConsumer.finnJournalposter(saksnummer, fagomrade);
    }

    public Optional<JournalpostDto> registrer(NyJournalpostCommandDto nyJournalpostCommandDto) {
        return bidragJournalpostConsumer.registrer(nyJournalpostCommandDto);
    }

    public Optional<JournalpostDto> endre(EndreJournalpostCommandDto endreJournalpostCommandDto) {
        return bidragJournalpostConsumer.endre(endreJournalpostCommandDto);
    }
}
