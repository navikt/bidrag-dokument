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
            @Value("${JOURNALPOST_URL}") String bidragBaseUrl
    ) {
        return new BidragJournalpostConsumer(bidragBaseUrl);
    }

    @Bean public BidragArkivConsumer journalforingConsumer(
            @Value("${BIDRAG_ARKIV_URL}") String joarkRestServiceUrl
    ) {
        return new BidragArkivConsumer(joarkRestServiceUrl + "/rest/journalfoerinngaaende/v1/journalposter/");
    }

    public static void main(String[] args) {
        SpringApplication.run(BidragDokument.class, args);
    }
}
