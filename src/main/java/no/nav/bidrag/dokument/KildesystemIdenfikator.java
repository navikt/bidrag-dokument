package no.nav.bidrag.dokument;

import static no.nav.bidrag.dokument.BidragDokumentConfig.DELIMTER;

public class KildesystemIdenfikator {

  private static final String NON_DIGITS = "\\D+";

  private final String prefiksetJournalpostId;

  private Kildesystem kildesystem;
  private Integer journalpostId;

  public KildesystemIdenfikator(String prefiksetJournalpostId) {
    this.prefiksetJournalpostId = prefiksetJournalpostId;
  }

  public boolean harIkkeJournalpostIdSomTall() {
    try {
      journalpostId = Integer.valueOf(prefiksetJournalpostId.replaceAll(NON_DIGITS, ""));
    } catch (NumberFormatException | NullPointerException e) {
      return true;
    }

    return false;
  }

  public Kildesystem hentKildesystem() {
    if (kildesystem == null && prefiksetJournalpostId != null) {
      if (prefiksetJournalpostId.trim().toUpperCase().startsWith(BidragDokumentConfig.PREFIX_BIDRAG + DELIMTER)) {
        kildesystem = Kildesystem.BIDRAG;
      } else if (prefiksetJournalpostId.trim().toUpperCase().startsWith(BidragDokumentConfig.PREFIX_JOARK + DELIMTER)) {
        kildesystem = Kildesystem.JOARK;
      } else {
        kildesystem = Kildesystem.UKJENT;
      }
    }

    return kildesystem;
  }

  public Integer hentJournalpostId() {
    if (journalpostId == null) {
      journalpostId = Integer.valueOf(prefiksetJournalpostId.replaceAll(NON_DIGITS, ""));
    }

    return journalpostId;
  }

  public enum Kildesystem {
    BIDRAG, JOARK, UKJENT;

    public boolean er(Kildesystem kildesystem) {
      return kildesystem == this;
    }
  }
}
