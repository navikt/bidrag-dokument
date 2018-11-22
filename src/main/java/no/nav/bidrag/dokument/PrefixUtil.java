package no.nav.bidrag.dokument;

import static no.nav.bidrag.dokument.BidragDokument.DELIMTER;

public class PrefixUtil {

    private static final String HAVE_DIGITS = ".*\\d+.*";
    private static final String NON_DIGITS = "\\D+";

    public static Integer extract(String streng) {
        if (streng != null && streng.matches(HAVE_DIGITS)) {
            return Integer.valueOf(streng.replaceAll(NON_DIGITS, ""));
        }

        return null;
    }

    public static Integer tryExtraction(String streng) {
        return Integer.valueOf(streng.replaceAll(NON_DIGITS, ""));
    }

    public static String remove(String prefix, String streng) {
        return streng.substring(prefix.length() + DELIMTER.length());
    }

    public static boolean startsWith(String prefix, String string) {
        return string != null && string.trim().toUpperCase().startsWith(prefix + DELIMTER);
    }
}
