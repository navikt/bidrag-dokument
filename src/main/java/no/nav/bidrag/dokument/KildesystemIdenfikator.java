package no.nav.bidrag.dokument;

import static no.nav.bidrag.dokument.BidragDokumentConfig.DELIMTER;

public class KildesystemIdenfikator {

  private static final String NON_DIGITS = "\\D+";
  private static final ThreadLocal<KildesystemIdenfikator> KILDESYSTEM_IDENFIKATOR_THREAD_LOCAL = ThreadLocal.withInitial(() -> null);

  private final String prefiksetJournalpostId;

  private Kildesystem kildesystem;
  private Integer journalpostId;

  private KildesystemIdenfikator(String prefiksetJournalpostId) {
    this.prefiksetJournalpostId = prefiksetJournalpostId;
  }

  private boolean harIkkeJournalpostIdSomTall() {
    try {
      journalpostId = Integer.valueOf(prefiksetJournalpostId.replaceAll(NON_DIGITS, ""));
    } catch (NumberFormatException | NullPointerException e) {
      return true;
    }

    return false;
  }

  private boolean erUkjent() {
    Kildesystem kildesystem = hentKildesystem();
    return kildesystem == null || kildesystem.er(Kildesystem.UKJENT);
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

  public static boolean erUkjentPrefixEllerHarIkkeTallEtterPrefix(String journalpostIdMedPrefix) {
    KildesystemIdenfikator kildesystemIdenfikator = new KildesystemIdenfikator(journalpostIdMedPrefix);
    KILDESYSTEM_IDENFIKATOR_THREAD_LOCAL.set(kildesystemIdenfikator);

    return kildesystemIdenfikator.erUkjent() || kildesystemIdenfikator.harIkkeJournalpostIdSomTall();
  }

  public static KildesystemIdenfikator hent() {
    KildesystemIdenfikator kildesystemIdenfikator = KILDESYSTEM_IDENFIKATOR_THREAD_LOCAL.get();

    if (kildesystemIdenfikator == null) {
      throw new IllegalStateException("Prefix på journalpostId er ikke validert");
    }

    return kildesystemIdenfikator;
  }

  public enum Kildesystem {
    BIDRAG, JOARK, UKJENT;

    public boolean er(Kildesystem kildesystem) {
      return kildesystem == this;
    }
  }
}
