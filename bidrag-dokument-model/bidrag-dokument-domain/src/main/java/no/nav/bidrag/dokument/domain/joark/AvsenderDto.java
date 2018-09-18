package no.nav.bidrag.dokument.domain.joark;

import no.nav.bidrag.dokument.domain.Part;

public class AvsenderDto {
    private Part avsenderType;
    private String avsenderId;
    private String avsender;

    public Part getAvsenderType() {
        return avsenderType;
    }

    public void setAvsenderType(Part avsenderType) {
        this.avsenderType = avsenderType;
    }

    public String getAvsenderId() {
        return avsenderId;
    }

    public void setAvsenderId(String avsenderId) {
        this.avsenderId = avsenderId;
    }

    public String getAvsender() {
        return avsender;
    }

    public void setAvsender(String avsender) {
        this.avsender = avsender;
    }
}
