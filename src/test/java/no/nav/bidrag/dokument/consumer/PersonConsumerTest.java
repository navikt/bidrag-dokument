package no.nav.bidrag.dokument.consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.Optional;
import no.nav.bidrag.dokument.dto.PersonDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PersonConsumer")
class PersonConsumerTest {

  private PersonConsumer personConsumer = new PersonConsumer();

  @Test
  @DisplayName("hent person")
  void skalHentePerson() {
    Optional<PersonDto> muligPersonDto = personConsumer.hentPersonInfo("11111898765");

    assertAll(
        () -> assertThat(muligPersonDto).hasValueSatisfying(personDto -> assertAll(
            () -> assertThat(personDto.getDiskresjonskode()).isEqualTo("VABO"),
            () -> assertThat(personDto.getDoedsdato()).isEqualTo(LocalDate.now()),
            () -> assertThat(personDto.getNavn()).isEqualTo("Bjarne Bidrag")
        ))
    );
  }
}
