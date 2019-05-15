package no.nav.bidrag.dokument;

import static no.nav.bidrag.dokument.BidragDokumentLocal.TEST_PROFILE;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class})
@SpringBootTest(classes = BidragDokumentLocal.class)
@ActiveProfiles(TEST_PROFILE)
@DisplayName("BidragDokument")
class BidragDokumentTest {

  @Test
  @DisplayName("skal laste spring-context")
  void shouldLoadContext() {
  }
}
