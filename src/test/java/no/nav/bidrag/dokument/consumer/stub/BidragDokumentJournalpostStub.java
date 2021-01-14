package no.nav.bidrag.dokument.consumer.stub;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer.PATH_JOURNALPOST_UTEN_SAK;
import static no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer.PATH_SAK_JOURNAL;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BidragDokumentJournalpostStub {

  public void runHenteJournalpostForSak(String saksnr) throws IOException {

    stubFor(get(urlPathMatching(String.format(PATH_SAK_JOURNAL, saksnr))).withQueryParam("fagomrade", equalTo("BID")).willReturn(
        aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withStatus(201)
            .withBody(Files.readString(Path.of("src/test/resources/respons.json"), StandardCharsets.UTF_8))));
  }

  public void runHenteEndreJournalpost(String journalpostId) throws IOException {

    stubFor(put(urlPathMatching(String.format(PATH_JOURNALPOST_UTEN_SAK, journalpostId))).willReturn(
        aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withStatus(201)
            .withBody(Files.readString(Path.of("src/test/resources/respons.json"), StandardCharsets.UTF_8))));
  }


}
