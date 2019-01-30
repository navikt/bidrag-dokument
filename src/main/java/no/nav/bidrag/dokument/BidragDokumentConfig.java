package no.nav.bidrag.dokument;

import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.consumer.BidragSakConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BidragDokumentConfig {
    
    public static final String DELIMTER = "-";
    public static final String PREFIX_BIDRAG = "BID";
    public static final String PREFIX_JOARK = "JOARK";

    @Bean public BidragJournalpostConsumer bidragJournalpostConsumer(
            @Value("${JOURNALPOST_URL}") String journalpostBaseUrl
    ) {
        return new BidragJournalpostConsumer(journalpostBaseUrl);
    }

    @Bean public BidragSakConsumer bidragSakConsumer(
            @Value("${BIDRAG_SAK_URL}") String sakBaseUrl
    ) {
        return new BidragSakConsumer(sakBaseUrl);
    }

    @Bean public BidragArkivConsumer journalforingConsumer(
            @Value("${BIDRAG_ARKIV_URL}") String bidragArkivBaseUrl
    ) {
        return new BidragArkivConsumer(bidragArkivBaseUrl);
    }
}
