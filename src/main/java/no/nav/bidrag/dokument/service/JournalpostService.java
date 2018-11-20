package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.PrefixUtil;
import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.dto.BrevlagerJournalpostDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.exception.KildesystemException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static no.nav.bidrag.dokument.BidragDokument.DELIMTER;
import static no.nav.bidrag.dokument.BidragDokument.PREFIX_BIDRAG;
import static no.nav.bidrag.dokument.BidragDokument.PREFIX_GSAK;
import static no.nav.bidrag.dokument.BidragDokument.PREFIX_JOARK;

@Component
public class JournalpostService {

    private final BidragJournalpostConsumer bidragJournalpostConsumer;
    private final BidragArkivConsumer bidragArkivConsumer;
    private final JournalpostMapper journalpostMapper;

    public JournalpostService(BidragJournalpostConsumer bidragJournalpostConsumer, BidragArkivConsumer bidragArkivConsumer, JournalpostMapper journalpostMapper) {
        this.bidragJournalpostConsumer = bidragJournalpostConsumer;
        this.bidragArkivConsumer = bidragArkivConsumer;
        this.journalpostMapper = journalpostMapper;
    }

    public Optional<JournalpostDto> hentJournalpost(String journalpostId) throws KildesystemException {
        try {
            if (startsWith(PREFIX_BIDRAG, journalpostId)) {
                return bidragJournalpostConsumer.hentJournalpost(PrefixUtil.tryExtraction(journalpostId));
            } else if (startsWith(PREFIX_JOARK, journalpostId)) {
                return bidragArkivConsumer.hentJournalpost(PrefixUtil.tryExtraction(journalpostId));
            }
        } catch (NumberFormatException nfe) {
            throw new KildesystemException("Kan ikke prosesseres som et tall: " + journalpostId);
        }

        throw new KildesystemException("Kunne ikke identifisere kildesystem for id: " + journalpostId);
    }

    public List<JournalpostDto> finnJournalposter(String saksnummer) throws KildesystemException {
        if (startsWith(PREFIX_BIDRAG, saksnummer)) {
            return bidragJournalpostConsumer.finnJournalposter(PrefixUtil.replace(PREFIX_BIDRAG + DELIMTER, saksnummer.toUpperCase()));
        } else if (startsWith(PREFIX_GSAK, saksnummer)) {
            throw new UnsupportedOperationException("not implemented");
        }

        throw new KildesystemException("Kunne ikke identifisere kildesystem for saksnummer: " + saksnummer);
    }

    private boolean startsWith(String prefix, String string) {
        return string != null && string.trim().toUpperCase().startsWith(prefix + DELIMTER);
    }

    public Optional<JournalpostDto> save(JournalpostDto journalpostDto) {
        BrevlagerJournalpostDto brevlagerJournalpostDto = journalpostMapper.tilBidragJournalpost(journalpostDto);
        return bidragJournalpostConsumer.save(brevlagerJournalpostDto)
                .map(journalpostMapper::fraBrevlagerJournalpostDto);
    }
}
