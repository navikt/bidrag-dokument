package no.nav.bidrag.dokument.domain.dto;

import java.util.Collections;
import java.util.List;

public class DokumentDto {
    private String dokumentId;
    private String dokumentTypeId;
    private String navSkjemaId;
    private String tittel;
    private String dokumentkategori;
    private List<VariantDto> varianter = Collections.emptyList();
    private List<VedleggDto> vedlegg = Collections.emptyList(); // NB: Kun for skannede forsendendelser
}
