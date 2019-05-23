package no.nav.bidrag.dokument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import no.nav.bidrag.dokument.dto.AktorDto;
import no.nav.bidrag.dokument.dto.PersonDto;
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
  @DisplayName("skal mappe aktør med personinfo til json og tilbake")
  void skalMappeAktorMedPersoninfoTilJson() throws IOException {
    String json = objectMapper.writeValueAsString(new AktorDto("06127412345", new PersonDto(
        "Dr. A. Cula", null, "SVADA"
    )));

    assertAll(
        () -> assertThat(json).as("ident").contains("\"ident\":\"06127412345\""),
        () -> assertThat(json).as("navn").contains("\"navn\":\"Dr. A. Cula\""),
        () -> assertThat(json).as("doedsdato").contains("\"doedsdato\":null"),
        () -> assertThat(json).as("diskresjonskode").contains("\"diskresjonskode\":\"SVADA\"")
    );

    AktorDto deserialisert = objectMapper.readValue(json, AktorDto.class);

    assertThat(deserialisert).isEqualTo(new AktorDto("06127412345", new PersonDto(
        "Dr. A. Cula", null, "SVADA"
    )));
  }
}
