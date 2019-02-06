package no.nav.bidrag.dokument;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BidragDokumentLocal.class)
@ActiveProfiles("dev")
@DisplayName("BidragDokument")
public class BidragDokumentTest {

    @DisplayName("skal laste spring-context")
    @Test
    void shouldLoadContext() {
    }

}
