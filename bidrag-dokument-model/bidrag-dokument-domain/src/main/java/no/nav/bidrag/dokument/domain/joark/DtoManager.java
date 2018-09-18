package no.nav.bidrag.dokument.domain.joark;

import java.util.Optional;

public class DtoManager<T> {
    private final T dto;
    private final Enum<?> status;

    public DtoManager(T dto, Enum<?> status) {
        this.dto = dto;
        this.status = status;
    }

    public Optional<T> hent() {
        return Optional.ofNullable(dto);
    }

    @SuppressWarnings("unchecked") public <E extends Enum<?>> E hentStatus(@SuppressWarnings("unused") Class<?> statusClass) { // parameter brukt for generic informasjon compile time
        return (E) status;
    }

    public boolean harStatus() {
        return status != null;
    }

    public <E extends Enum<?>> boolean harStatus(Class<E> clazz) {
        return harStatus() && status.getDeclaringClass().equals(clazz);
    }

    public Enum<?> getStatus() {
        return status;
    }
}
