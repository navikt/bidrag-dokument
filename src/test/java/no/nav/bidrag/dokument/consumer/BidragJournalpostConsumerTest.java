package no.nav.bidrag.dokument.consumer;

import static no.nav.bidrag.dokument.BidragDokumentConfig.MIDL_BREVLAGER_QUALIFIER;
import static no.nav.bidrag.dokument.BidragDokumentLocal.TEST_PROFILE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import no.nav.bidrag.commons.security.service.OidcTokenManager;
import no.nav.bidrag.dokument.BidragDokumentLocal;
import no.nav.bidrag.dokument.consumer.stub.RestConsumerStub;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {BidragDokumentLocal.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(TEST_PROFILE)
@DisplayName("BidragJournalpostConsumer")
@AutoConfigureWireMock(port = 0)
@EnableMockOAuth2Server
class BidragJournalpostConsumerTest {

  @Autowired
  @Qualifier(MIDL_BREVLAGER_QUALIFIER)
  private BidragDokumentConsumer bidragJournalpostConsumer;

  @Autowired
  private RestConsumerStub restConsumerStub;

  @MockBean
  private OidcTokenManager oidcTokenManager;

  private static String generateTestToken() {
    return "Token";
  }

  @Test
  @DisplayName("skal hente journalpost til en sak")
  void skalHenteJournalpostTilSak() throws IOException {

    // given
    var saksnr = "1900000";

    restConsumerStub.runHenteJournalpostForSak(saksnr);

    var idToken = generateTestToken();

    when(oidcTokenManager.isValidTokenIssuedByAzure()).thenReturn(false);
    when(oidcTokenManager.isValidTokenIssuedBySTS()).thenReturn(false);
    when(oidcTokenManager.fetchTokenAsString()).thenReturn("");
    when(oidcTokenManager.getIssuer()).thenReturn("");
    // when
    var respons = bidragJournalpostConsumer.finnJournalposter(saksnr, "BID");

    // then
    assertEquals(2, respons.size());
  }

  @Test
  @DisplayName("skal endre journalpost")
  void skalEndreJournalpost() throws IOException {
    var idToken = generateTestToken();
    var request = endreJournalpostCommandMedId101();

    when(oidcTokenManager.isValidTokenIssuedByAzure()).thenReturn(false);
    when(oidcTokenManager.isValidTokenIssuedBySTS()).thenReturn(false);
    when(oidcTokenManager.fetchTokenAsString()).thenReturn("");
    when(oidcTokenManager.getIssuer()).thenReturn("");

    restConsumerStub.runEndreJournalpost(request.getJournalpostId(), HttpStatus.OK);

    var respons = bidragJournalpostConsumer.endre("4802", endreJournalpostCommandMedId101());
    Assertions.assertTrue(respons.is2xxSuccessful());
  }

  private EndreJournalpostCommand endreJournalpostCommandMedId101() {
    EndreJournalpostCommand endreJournalpostCommand = new EndreJournalpostCommand();
    endreJournalpostCommand.setJournalpostId("BID-101");

    return endreJournalpostCommand;
  }
}
