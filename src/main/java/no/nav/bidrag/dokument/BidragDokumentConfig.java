package no.nav.bidrag.dokument;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.consumer.BidragSakConsumer;
import no.nav.security.oidc.context.OIDCRequestContextHolder;

@Configuration
public class BidragDokumentConfig {

    public static final String DELIMTER = "-";
    public static final String PREFIX_BIDRAG = "BID";
    public static final String PREFIX_JOARK = "JOARK";
    public static final String ISSUER = "isso";

    @Bean
    public BidragJournalpostConsumer bidragJournalpostConsumer(
            @Value("${JOURNALPOST_URL}") String journalpostBaseUrl,
            OIDCRequestContextHolder securityContextHolder) {

        return new BidragJournalpostConsumer(
                journalpostBaseUrl,
                securityContextHolder);
    }

    @Bean
    public BidragSakConsumer bidragSakConsumer(
            @Value("${BIDRAG_SAK_URL}") String sakBaseUrl,
            OIDCRequestContextHolder securityContextHolder) {

        return new BidragSakConsumer(
                sakBaseUrl,
                securityContextHolder);
    }

    @Bean
    public BidragArkivConsumer journalforingConsumer(
            @Value("${BIDRAG_ARKIV_URL}") String bidragArkivBaseUrl,
            OIDCRequestContextHolder securityContextHolder) {

        return new BidragArkivConsumer(
                bidragArkivBaseUrl,
                securityContextHolder);
    }
}
