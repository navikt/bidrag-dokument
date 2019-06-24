package no.nav.bidrag.dokument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KildesystemIdenfikatorTest {

  @Test
  @DisplayName("skal validere prefix for tråd")
  void skalValiderePrefix() {
    assertAll(
        () -> assertThat(KildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix(null)).as("null").isTrue(),
        () -> assertThat(KildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix("123")).as("ingen prefix").isTrue(),
        () -> assertThat(KildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix("NA-123")).as("ukjent prefix").isTrue(),
        () -> assertThat(KildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix("BID-xyz")).as("ikke tall").isTrue(),
        () -> assertThat(KildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix("BID-123")).as("bidrag prefix").isFalse(),
        () -> assertThat(KildesystemIdenfikator.erUkjentPrefixEllerHarIkkeTallEtterPrefix("JOARK-123")).as("joark prefix").isFalse()
    );
  }
}