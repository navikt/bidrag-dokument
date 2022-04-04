package no.nav.bidrag.dokument;

import static no.nav.bidrag.dokument.BidragDokumentLocal.TEST_PROFILE;

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = BidragDokumentLocal.class)
@ActiveProfiles(TEST_PROFILE)
@DisplayName("BidragDokument")
@EnableMockOAuth2Server
class BidragDokumentTest {

  @Test
  @DisplayName("skal laste spring-context")
  void shouldLoadContext() {
  }
}
