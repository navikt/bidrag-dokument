package no.nav.bidrag.dokument;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;

@Configuration
public class BidragDokumentConfig {
    
    public static final String DELIMTER = "-";
    public static final String PREFIX_BIDRAG = "BID";
    public static final String PREFIX_GSAK = "GSAK";
    public static final String PREFIX_JOARK = "JOARK";

    @Bean public BidragJournalpostConsumer bidragJournalpostConsumer(
            @Value("${JOURNALPOST_URL}") String bidragDokumentBaseUrl
    ) {
        return new BidragJournalpostConsumer(bidragDokumentBaseUrl);
    }

    @Bean public BidragArkivConsumer journalforingConsumer(
            @Value("${BIDRAG_ARKIV_URL}") String bidragArkivBaseUrl
    ) {
        return new BidragArkivConsumer(bidragArkivBaseUrl);
    }


}
