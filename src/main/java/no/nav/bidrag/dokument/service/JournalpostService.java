package no.nav.bidrag.dokument.service;

import no.nav.bidrag.dokument.BidragDokument;
import no.nav.bidrag.dokument.DigitUtil;
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
            if (startsWith(BidragDokument.JOURNALPOST_ID_BIDRAG_REQUEST, journalpostId)) {
                return bidragJournalpostConsumer.hentJournalpost(DigitUtil.tryExtraction(journalpostId));
            } else if (startsWith(BidragDokument.JOURNALPOST_ID_JOARK_REQUEST, journalpostId)) {
                return bidragArkivConsumer.hentJournalpost(DigitUtil.tryExtraction(journalpostId));
            }
        } catch (NumberFormatException nfe) {
            throw new KildesystemException("Kan ikke prosesseres som et tall: " + journalpostId);
        }

        throw new KildesystemException("Kunne ikke identifisere kildesystem for id: " + journalpostId);
    }

    private boolean startsWith(String prefix, String string) {
        return string != null && string.trim().toUpperCase().startsWith(prefix);
    }

    public List<JournalpostDto> finnJournalposter(String saksnummer) {
        return bidragJournalpostConsumer.finnJournalposter(saksnummer);
    }

    public Optional<JournalpostDto> registrer(NyJournalpostCommandDto nyJournalpostCommandDto) {
        return bidragJournalpostConsumer.registrer(nyJournalpostCommandDto);
    }

    public Optional<JournalpostDto> endre(EndreJournalpostCommandDto endreJournalpostCommandDto) {
        return bidragJournalpostConsumer.endre(endreJournalpostCommandDto);
    }
}
