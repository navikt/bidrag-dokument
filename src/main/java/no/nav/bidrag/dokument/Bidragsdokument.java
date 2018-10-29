package no.nav.bidrag.dokument;

import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.consumer.JournalforingConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@SpringBootApplication
@PropertySource("classpath:url.properties")
public class Bidragsdokument extends WebMvcConfigurationSupport {

    @Bean public BidragJournalpostConsumer bidragJournalpostConsumer(
            @Value("${JOURNALPOST_URL}") String bidragBaseUrl
    ) {
        return new BidragJournalpostConsumer(bidragBaseUrl);
    }

    @Bean public JournalforingConsumer journalforingConsumer(
            @Value("${JOARK_URL}") String joarkRestServiceUrl
    ) {
        return new JournalforingConsumer(joarkRestServiceUrl + "/rest/journalfoerinngaaende/v1/journalposter/" );
    }

    public static void main(String[] args) {
        SpringApplication.run(Bidragsdokument.class, args);
    }
}
