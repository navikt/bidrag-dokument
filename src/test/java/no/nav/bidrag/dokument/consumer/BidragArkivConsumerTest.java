package no.nav.bidrag.dokument.consumer;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static no.nav.bidrag.dokument.BidragDokumentConfig.ARKIV_QUALIFIER;
import static no.nav.bidrag.dokument.BidragDokumentLocal.TEST_PROFILE;
import static no.nav.bidrag.dokument.consumer.BidragDokumentConsumer.PATH_JOURNALPOST_UTEN_SAK;
import static no.nav.bidrag.dokument.consumer.stub.RestConsumerStub.generereJournalpostrespons;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import java.util.HashMap;
import java.util.Map;
import no.nav.bidrag.dokument.BidragDokumentConfig.OidcTokenManager;
import no.nav.bidrag.dokument.BidragDokumentLocal;
import no.nav.bidrag.dokument.consumer.stub.RestConsumerStub;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.security.token.support.test.jersey.TestTokenGeneratorResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@DisplayName("BidragArkivConsumer")
@SpringBootTest(classes = {BidragDokumentLocal.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(TEST_PROFILE)
@AutoConfigureWireMock(port = 0)
class BidragArkivConsumerTest {

  @Autowired
  @Qualifier(ARKIV_QUALIFIER)
  private BidragDokumentConsumer bidragArkivConsumer;

  @Autowired
  private RestConsumerStub restConsumerStub;

  @MockBean
  private OidcTokenManager oidcTokenManager;

  private static String generateTestToken() {
    TestTokenGeneratorResource testTokenGeneratorResource = new TestTokenGeneratorResource();
    return testTokenGeneratorResource.issueToken("localhost-idtoken");
  }

  @Test
  @DisplayName("skal hente en journalpost med spring sin RestTemplate")
  void skalHenteJournalpostMedRestTemplate() {

    var jpId = "BID-101";
    var saksnr = "69";
    var path = String.format(PATH_JOURNALPOST_UTEN_SAK, jpId);
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("saksnummer", equalTo(saksnr));

    Map<String, String> journalpostelementer = new HashMap<>();
    journalpostelementer.put("innhold", "ENDELIG");
    var idToken = generateTestToken();

    when(oidcTokenManager.fetchToken()).thenReturn(idToken);
    restConsumerStub.runGetArkiv(path, queryParams, HttpStatus.OK, generereJournalpostrespons(journalpostelementer));

    var httpResponse = bidragArkivConsumer.hentJournalpost(saksnr, jpId);
    var journalpostResponse = httpResponse.fetchBody().orElseThrow(() -> new AssertionError("BidragArkivConsumer kunne ikke finne journalpost!"));

    assertThat(journalpostResponse.getJournalpost()).extracting(JournalpostDto::getInnhold).isEqualTo("ENDELIG");

  }
}
