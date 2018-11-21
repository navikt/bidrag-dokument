package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.PrefixUtil;
import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommandDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.NyJournalpostCommandDto;
import no.nav.bidrag.dokument.exception.KildesystemException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.DoubleStream;

import static no.nav.bidrag.dokument.BidragDokument.DELIMTER;
import static no.nav.bidrag.dokument.BidragDokument.PREFIX_BIDRAG;
import static no.nav.bidrag.dokument.BidragDokument.PREFIX_GSAK;
import static no.nav.bidrag.dokument.BidragDokument.PREFIX_JOARK;

@Component
public class JournalpostService {

    private final BidragJournalpostConsumer bidragJournalpostConsumer;
    private final BidragArkivConsumer bidragArkivConsumer;

    public JournalpostService(BidragJournalpostConsumer bidragJournalpostConsumer, BidragArkivConsumer bidragArkivConsumer) {
        this.bidragJournalpostConsumer = bidragJournalpostConsumer;
        this.bidragArkivConsumer = bidragArkivConsumer;
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

    public Optional<JournalpostDto> registrer(NyJournalpostCommandDto nyJournalpostCommandDto) {
        return bidragJournalpostConsumer.registrer(nyJournalpostCommandDto);
    }

    public Optional<JournalpostDto> endre(EndreJournalpostCommandDto endreJournalpostCommandDto) {
        return bidragJournalpostConsumer.endre(endreJournalpostCommandDto);
    }
}
