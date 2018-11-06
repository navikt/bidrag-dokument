package no.nav.bidrag.dokument;

public class DigitUtil {

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
}
