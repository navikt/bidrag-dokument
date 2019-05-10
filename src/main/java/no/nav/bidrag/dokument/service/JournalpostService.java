package no.nav.bidrag.dokument.service;

import static no.nav.bidrag.dokument.KildesystemIdenfikator.Kildesystem.BIDRAG;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import no.nav.bidrag.commons.web.HttpStatusResponse;
import no.nav.bidrag.dokument.KildesystemIdenfikator;
import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.consumer.BidragSakConsumer;
import no.nav.bidrag.dokument.dto.AktorDto;
import no.nav.bidrag.dokument.dto.BidragSakDto;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommandDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import org.springframework.stereotype.Component;

@Component
public class JournalpostService {

  private final BidragJournalpostConsumer bidragJournalpostConsumer;
  private final BidragSakConsumer bidragSakConsumer;
  private final BidragArkivConsumer bidragArkivConsumer;

  public JournalpostService(
      BidragJournalpostConsumer bidragJournalpostConsumer, BidragSakConsumer bidragSakConsumer, BidragArkivConsumer bidragArkivConsumer
  ) {
    this.bidragJournalpostConsumer = bidragJournalpostConsumer;
    this.bidragSakConsumer = bidragSakConsumer;
    this.bidragArkivConsumer = bidragArkivConsumer;
  }

  public Optional<JournalpostDto> hentJournalpost(KildesystemIdenfikator kildesystemIdenfikator) {
    if (BIDRAG.er(kildesystemIdenfikator.hentKildesystem())) {
      Optional<JournalpostDto> muligJournalpostDto = bidragJournalpostConsumer.hentJournalpost(kildesystemIdenfikator.hentJournalpostId());

      muligJournalpostDto.ifPresent(
          journalpostDto -> journalpostDto.setBidragssaker(finnBidragssaker(Objects.requireNonNull(journalpostDto.getGjelderAktor())))
      );

      return muligJournalpostDto;
    }

    return bidragArkivConsumer.hentJournalpost(kildesystemIdenfikator.hentJournalpostId());
  }

  private List<BidragSakDto> finnBidragssaker(AktorDto aktorDto) {
    return new ArrayList<>(bidragSakConsumer.finnInnvolverteSaker(aktorDto.getIdent()));
  }

  public HttpStatusResponse<List<JournalpostDto>> finnJournalposter(String saksnummer, String fagomrade) {
    return bidragJournalpostConsumer.finnJournalposter(saksnummer, fagomrade);
  }

  public HttpStatusResponse<JournalpostDto> endre(EndreJournalpostCommandDto endreJournalpostCommandDto) {
    return bidragJournalpostConsumer.endre(endreJournalpostCommandDto);
  }
}
