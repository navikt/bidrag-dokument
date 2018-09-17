package no.nav.bidrag.dokument.domain.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("DtoManagerTest")
class DtoManagerTest {

    @DisplayName("skal si om har status")
    @Test void skalSiOmHarStatus() {
        DtoManager<Dto> dtoDtoManagerUtenStatus = new DtoManager<>(null, null);
        DtoManager<Dto> dtoDtoManagerMedStatus = new DtoManager<>(null, StatusEn.SVADA);

        assertAll(
                () -> assertThat(dtoDtoManagerUtenStatus.harStatus(StatusEn.class)).isFalse(),
                () -> assertThat(dtoDtoManagerMedStatus.harStatus(StatusEn.class)).isTrue()
        );
    }

    @DisplayName("skal ikke ha status når status er annen klasse")
    @Test void skalIkkeHaStatusNarStatusErAnnenKlasse() {
        DtoManager<Dto> dtoDtoManagerMedStatus = new DtoManager<>(null, StatusTo.LADA);

        assertThat(dtoDtoManagerMedStatus.harStatus(StatusEn.class)).isFalse();
    }

    private class Dto {

    }

    private enum StatusEn {
        SVADA
    }

    private enum StatusTo {
        LADA
    }
}