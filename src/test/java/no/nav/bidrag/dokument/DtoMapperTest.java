package no.nav.bidrag.dokument;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import no.nav.bidrag.dokument.dto.AktorDto;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = BidragDokumentLocal.class)
class DtoMapperTest {

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("skal mappe aktør til json og tilbake")
  void skalMappeAktorTilJson() throws IOException {
    String json = objectMapper.writeValueAsString(new AktorDto("06127412345"));

    assertThat(json).contains("\"ident\":\"06127412345\"");

    System.out.println(json);
    AktorDto deserialisert = objectMapper.readValue(json, AktorDto.class);

    assertThat(deserialisert).isEqualTo(new AktorDto("06127412345"));
  }

  @Test
  @DisplayName("skal mappe BestillOriginal til json og tilbake")
  void skalMappeBestillOriginalTilJson() throws IOException {
    String json = objectMapper.writeValueAsString(new Avvikshendelse(AvvikType.BESTILL_ORIGINAL));

    assertThat(json).contains("\"avvikType\":\"BESTILL_ORIGINAL\"");

    System.out.println(json);
    Avvikshendelse deserialisert = objectMapper.readValue(json, Avvikshendelse.class);

    assertThat(deserialisert).isEqualTo(new Avvikshendelse(AvvikType.BESTILL_ORIGINAL));
  }
}
