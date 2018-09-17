package no.nav.bidrag.dokument.consumer;

import no.nav.bidrag.dokument.service.JournalpostService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriTemplateHandler;

@SpringBootApplication
public class Bidragsdokumenter extends WebMvcConfigurationSupport {

    @Bean public JournalpostService journalpostService(JournalforingConsumer journalpostConsumer) {
        return new JournalpostService(journalpostConsumer);
    }

    @Bean public JournalforingConsumer journalforingConsumer(
            UriTemplateHandler joarkUriTemplateHandler, @Value("${bidrag.joark.url.journalforing}") String journalforingEndpoint
    ) {
        return new JournalforingConsumer(joarkUriTemplateHandler, journalforingEndpoint);
    }

    @Bean public UriTemplateHandler joarkUriTemplateHandler(@Value("${bidrag.joark.url.base}") String base) {
        return new DefaultUriBuilderFactory(base);
    }

    public static void main(String[] args) {
        SpringApplication.run(Bidragsdokumenter.class, args);
    }
}
