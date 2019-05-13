package no.nav.bidrag.dokument.service;

import static no.nav.bidrag.dokument.KildesystemIdenfikator.Kildesystem.BIDRAG;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import no.nav.bidrag.dokument.KildesystemIdenfikator;
import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.consumer.BidragSakConsumer;
import no.nav.bidrag.dokument.consumer.SecurityTokenConsumer;
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
  private final SecurityTokenConsumer securityTokenConsumer;

  public JournalpostService(
      BidragJournalpostConsumer bidragJournalpostConsumer, BidragSakConsumer bidragSakConsumer, BidragArkivConsumer bidragArkivConsumer,
      SecurityTokenConsumer securityTokenConsumer) {
    this.bidragJournalpostConsumer = bidragJournalpostConsumer;
    this.bidragSakConsumer = bidragSakConsumer;
    this.bidragArkivConsumer = bidragArkivConsumer;
    this.securityTokenConsumer = securityTokenConsumer;
  }

  public Optional<JournalpostDto> hentJournalpost(KildesystemIdenfikator kildesystemIdenfikator) {
    if (BIDRAG.er(kildesystemIdenfikator.hentKildesystem())) {
      return hentJournalpostFraMidlertidigBrevlager(kildesystemIdenfikator);
    }

    return hentJournalpostFraJoark(kildesystemIdenfikator);
  }

  private Optional<JournalpostDto> hentJournalpostFraMidlertidigBrevlager(KildesystemIdenfikator kildesystemIdenfikator) {
    var muligJournalpostDto = bidragJournalpostConsumer.hentJournalpost(kildesystemIdenfikator.hentJournalpostId());

    var muligSamlToken = securityTokenConsumer.konverterOidcTokenTilSamlToken();

    muligJournalpostDto.ifPresent(
        journalpostDto -> {
          journalpostDto.setBidragssaker(finnBidragssaker(Objects.requireNonNull(journalpostDto.getGjelderAktor())));
          hentPersonInformasjon(journalpostDto);
        }
    );

    return muligJournalpostDto;
  }

  private Optional<JournalpostDto> hentJournalpostFraJoark(KildesystemIdenfikator kildesystemIdenfikator) {
    return bidragArkivConsumer.hentJournalpost(kildesystemIdenfikator.hentJournalpostId());
  }

  private void hentPersonInformasjon(JournalpostDto journalpostDto) {

//TODO implement
//
//    var gjelderAktor = journalpostDto.getGjelderAktor();
//
//    if (gjelderAktor != null && gjelderAktor.erPerson()) {
//      var muligPerson = personConsumer.hentPersonInfo(gjelderAktor.getIdent());
//
//      muligPerson.ifPresent(person -> berikPerson(new PersonDto(gjelderAktor.getIdent()), person));
//    }
  }

  private List<BidragSakDto> finnBidragssaker(AktorDto aktorDto) {
    return new ArrayList<>(bidragSakConsumer.finnInnvolverteSaker(aktorDto.getIdent()));
  }

  public List<JournalpostDto> finnJournalposter(String saksnummer, String fagomrade) {
    return bidragJournalpostConsumer.finnJournalposter(saksnummer, fagomrade);
  }

  public Optional<JournalpostDto> endre(EndreJournalpostCommandDto endreJournalpostCommandDto) {
    return bidragJournalpostConsumer.endre(endreJournalpostCommandDto);
  }
}
