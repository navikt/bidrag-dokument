package no.nav.bidrag.dokument.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import no.nav.bidrag.commons.web.HttpStatusResponse;
import no.nav.bidrag.dokument.KildesystemIdenfikator;
import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.consumer.BidragSakConsumer;
import no.nav.bidrag.dokument.consumer.PersonConsumer;
import no.nav.bidrag.dokument.consumer.SecurityTokenConsumer;
import no.nav.bidrag.dokument.dto.AktorDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

@DisplayName("JournalpostService")
class JournalpostServiceTest {

  @Mock
  private BidragArkivConsumer bidragArkivConsumerMock;
  @Mock
  private BidragSakConsumer bidragSakConsumerMock;
  @Mock
  private BidragJournalpostConsumer bidragJournalpostConsumerMock;
  @Mock
  private PersonConsumer personConsumerMock;
  @Mock
  private SecurityTokenConsumer securityTokenConsumerMock;
  @InjectMocks
  private JournalpostService journalpostService;

  @BeforeEach
  void initMocksAndService() {
    MockitoAnnotations.initMocks(this);
  }

  @DisplayName("skal ikke hente journalpost")
  @Test
  void skalIkkeHenteJournalpostGittId() {
    when(bidragArkivConsumerMock.hentJournalpost(anyInt())).thenReturn(new HttpStatusResponse<>(HttpStatus.NO_CONTENT, null));
    assertThat(journalpostService.hentJournalpost(new KildesystemIdenfikator("joark-2")).fetchOptionalResult()).isNotPresent();
  }

  @DisplayName("skal hente journalpost gitt id")
  @Test
  void skalHenteJournalpostGittId() {
    when(bidragArkivConsumerMock.hentJournalpost(2)).thenReturn(new HttpStatusResponse<>(HttpStatus.OK, new JournalpostDto()));
    assertThat(journalpostService.hentJournalpost(new KildesystemIdenfikator("joark-2")).fetchOptionalResult()).isPresent();
  }

  @DisplayName("skal hente journalpost fra midlertidig brevlager og berike person")
  @Test
  void skalHenteJournalpostOgBerikePerson() {
    JournalpostDto journalpost = new JournalpostDto();
    journalpost.setGjelderAktor(new AktorDto("06127412345", "NorskIdent", "person"));
    when(bidragJournalpostConsumerMock.hentJournalpost(69)).thenReturn(new HttpStatusResponse<>(HttpStatus.OK, journalpost));
    when(bidragSakConsumerMock.finnInnvolverteSaker("06127412345")).thenReturn(Collections.emptyList());

    var journalpostBeriket = journalpostService.hentJournalpost(new KildesystemIdenfikator("bid-69"));

    assertThat(journalpostBeriket.fetchOptionalResult()).hasValueSatisfying(journalpostDto -> assertAll(
        () -> assertThat(journalpostDto).extracting(JournalpostDto::getGjelderAktor).isNotNull(),
        () -> assertThat(journalpostDto.getGjelderAktor()).extracting(AktorDto::erPerson).isEqualTo(true),
        () -> verify(personConsumerMock).hentPersonInfo("06127412345")
    ));
  }

  @Disabled
  @Test
  @DisplayName("skal hente ut OIDC token som skal konverteres til SAML token")
  void skalHenteOidcTokenSomSkalKonverteresTilSamlToken() {
    when(bidragJournalpostConsumerMock.hentJournalpost(any())).thenReturn(new HttpStatusResponse<>(HttpStatus.OK, new JournalpostDto()));
    journalpostService.hentJournalpost(new KildesystemIdenfikator("bid-69"));
    verify(securityTokenConsumerMock, times(1)).konverterOidcTokenTilSamlToken();
  }
}
