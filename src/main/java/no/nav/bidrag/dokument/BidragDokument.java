package no.nav.bidrag.dokument;

import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@SpringBootApplication
@PropertySource("classpath:url.properties")
public class BidragDokument extends WebMvcConfigurationSupport {

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

    public static void main(String[] args) {
        SpringApplication.run(BidragDokument.class, args);
    }
}
