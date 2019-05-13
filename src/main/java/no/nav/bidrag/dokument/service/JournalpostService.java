package no.nav.bidrag.dokument.service;

import static no.nav.bidrag.dokument.KildesystemIdenfikator.Kildesystem.BIDRAG;

import java.util.ArrayList;
import java.util.List;
import no.nav.bidrag.commons.web.HttpStatusResponse;
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
      BidragArkivConsumer bidragArkivConsumer,
      BidragJournalpostConsumer bidragJournalpostConsumer,
      BidragSakConsumer bidragSakConsumer,
      SecurityTokenConsumer securityTokenConsumer
  ) {
    this.bidragArkivConsumer = bidragArkivConsumer;
    this.bidragJournalpostConsumer = bidragJournalpostConsumer;
    this.bidragSakConsumer = bidragSakConsumer;
    this.securityTokenConsumer = securityTokenConsumer;
  }

  public HttpStatusResponse<JournalpostDto> hentJournalpost(KildesystemIdenfikator kildesystemIdenfikator) {
    if (BIDRAG.er(kildesystemIdenfikator.hentKildesystem())) {
      return hentJournalpostFraMidlertidigBrevlager(kildesystemIdenfikator);
    }

    return hentJournalpostFraJoark(kildesystemIdenfikator);
  }

  private HttpStatusResponse<JournalpostDto> hentJournalpostFraMidlertidigBrevlager(KildesystemIdenfikator kildesystemIdenfikator) {
    var journalpostResponse = bidragJournalpostConsumer.hentJournalpost(kildesystemIdenfikator.hentJournalpostId());
    var muligJournalpost = journalpostResponse.fetchOptionalResult();
    var muligSamlToken = securityTokenConsumer.konverterOidcTokenTilSamlToken();

    muligJournalpost.ifPresent(
        journalpostDto -> {
          if (journalpostDto.getGjelderAktor() != null) {
            journalpostDto.setBidragssaker(finnBidragssaker(journalpostDto.getGjelderAktor()));
            journalpostDto.setGjelderAktor(hentPersonInformasjon(journalpostDto.getGjelderAktor()));
          }
        }
    );

    return journalpostResponse;
  }

  private HttpStatusResponse<JournalpostDto> hentJournalpostFraJoark(KildesystemIdenfikator kildesystemIdenfikator) {
    return bidragArkivConsumer.hentJournalpost(kildesystemIdenfikator.hentJournalpostId());
  }

  private AktorDto hentPersonInformasjon(AktorDto gjelderAktor) {

//TODO implement
//    if (gjelderAktor != null && gjelderAktor.erPerson()) {
//      var muligPerson = personConsumer.hentPersonInfo(gjelderAktor.getIdent());
//
//      muligPerson.ifPresent(person -> berikPerson(new PersonDto(gjelderAktor.getIdent()), person));
//
//      return muligPerson.get();
//    }

    return gjelderAktor;
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
