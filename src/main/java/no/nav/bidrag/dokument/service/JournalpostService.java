package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.PrefixUtil;
import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.consumer.BidragSakConsumer;
import no.nav.bidrag.dokument.dto.BidragSakDto;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommandDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.NyJournalpostCommandDto;
import no.nav.bidrag.dokument.exception.KildesystemException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static no.nav.bidrag.dokument.BidragDokumentConfig.PREFIX_BIDRAG;
import static no.nav.bidrag.dokument.BidragDokumentConfig.PREFIX_JOARK;

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

    public Optional<JournalpostDto> hentJournalpost(String journalpostId, String bearerToken) throws KildesystemException {
        try {
            if (PrefixUtil.startsWith(PREFIX_BIDRAG, journalpostId)) {
                Optional<JournalpostDto> muligJournalpostDto = bidragJournalpostConsumer.hentJournalpost(PrefixUtil.tryExtraction(journalpostId), bearerToken);
                muligJournalpostDto.ifPresent(
                        journalpostDto -> journalpostDto.setBidragssaker(finnBidragssaker(journalpostDto.getGjelderBrukerId(), bearerToken))
                );

                return muligJournalpostDto;
            } else if (PrefixUtil.startsWith(PREFIX_JOARK, journalpostId)) {
                return bidragArkivConsumer.hentJournalpost(PrefixUtil.tryExtraction(journalpostId), bearerToken);
            }
        } catch (NumberFormatException nfe) {
            throw new KildesystemException("Kan ikke prosesseres som et tall: " + journalpostId);
        }

        throw new KildesystemException("Kunne ikke identifisere kildesystem for id: " + journalpostId);
    }

    private List<BidragSakDto> finnBidragssaker(List<String> gjelderBrukerId, String bearerToken) {
        List<BidragSakDto> bidragssaker = new ArrayList<>();
        gjelderBrukerId.forEach(fnr -> bidragssaker.addAll(bidragSakConsumer.finnInnvolverteSaker(fnr, bearerToken)));

        return bidragssaker;
    }

    public List<JournalpostDto> finnJournalposter(String saksnummer, String fagomrade, String bearerToken) {
        return bidragJournalpostConsumer.finnJournalposter(saksnummer, fagomrade, bearerToken);
    }

    public Optional<JournalpostDto> registrer(NyJournalpostCommandDto nyJournalpostCommandDto, String bearerToken) {
        return bidragJournalpostConsumer.registrer(nyJournalpostCommandDto, bearerToken);
    }

    public Optional<JournalpostDto> endre(EndreJournalpostCommandDto endreJournalpostCommandDto, String bearerToken) {
        return bidragJournalpostConsumer.endre(endreJournalpostCommandDto, bearerToken);
    }
}
