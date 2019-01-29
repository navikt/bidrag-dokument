package no.nav.bidrag.dokument;

import static no.nav.bidrag.dokument.BidragDokumentConfig.DELIMTER;

public class PrefixUtil {

    private static final String NON_DIGITS = "\\D+";

    public static Integer tryExtraction(String streng) {
        return Integer.valueOf(streng.replaceAll(NON_DIGITS, ""));
    }

    public static boolean startsWith(String prefix, String string) {
        return string != null && string.trim().toUpperCase().startsWith(prefix + DELIMTER);
    }
}
