package no.nav.bidrag.dokument.consumer.stub;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer.PATH_JOURNALPOST_UTEN_SAK;
import static no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer.PATH_SAK_JOURNAL;
import static no.nav.bidrag.dokument.consumer.DokumentConsumer.PATH_DOKUMENT_TILGANG;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RestConsumerStub {

  public static String lesResponsfilSomStreng(String filnavn) throws IOException {
    return Files.readString(Path.of("src/test/resources/stubrespons/" + filnavn), StandardCharsets.UTF_8);
  }

  public static String generereJournalpostrespons(Map<String, String> elementer) {

    var startingElements = String.join("\n", " {", " \"journalpost\": {");

    var closingElements = String.join("\n", "}", "}");

    var respons = new StringBuilder();
    respons.append(startingElements);

    int i = 0;
    for (Map.Entry<String, String> element : elementer.entrySet()) {
      if (i > elementer.size() - 1) {
        respons.append(String.join("\n", " \"" + element.getKey() + "\": \"" + element.getValue() + "\","));
      } else {
        respons.append(String.join("\n", " \"" + element.getKey() + "\": \"" + element.getValue() + "\""));
      }
      i++;
    }
    respons.append(closingElements);
    return respons.toString();
  }

  public void runHenteJournalpostForSak(String saksnr) throws IOException {
    stubFor(get(urlPathMatching(String.format(PATH_SAK_JOURNAL, saksnr))).withQueryParam("fagomrade", equalTo("BID")).willReturn(
        aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withStatus(HttpStatus.OK.value())
            .withBody(Files.readString(Path.of("src/test/resources/stubrespons/bdj-respons.json"), StandardCharsets.UTF_8))));
  }

  public void runHenteJournalpost(String jpId, Map<String, StringValuePattern> queryParams, HttpStatus status, String respons) {
    stubFor(get(urlPathMatching(String.format(PATH_JOURNALPOST_UTEN_SAK, jpId))).withQueryParams(queryParams)
        .willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withStatus(status.value()).withBody(respons)));
  }

  public void runEndreJournalpost(String journalpostId, HttpStatus status) throws IOException {
    stubFor(put(urlPathMatching(String.format(PATH_JOURNALPOST_UTEN_SAK, journalpostId))).willReturn(
        aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withStatus(status.value())
            .withBody(Files.readString(Path.of("src/test/resources/stubrespons/bdj-respons.json"), StandardCharsets.UTF_8))));
  }

  public void runEndreJournalpostMedHeader(String journalpostId, HttpHeader headerinput, HttpStatus status, String respons) {
    HttpHeaders headers = new HttpHeaders(headerinput, HttpHeader.httpHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE));
    stubFor(put(urlPathMatching(String.format(PATH_JOURNALPOST_UTEN_SAK, journalpostId)))
        .willReturn(aResponse().withHeaders(headers).withStatus(status.value()).withBody(respons)));
  }

  public void runGiTilgangTilDokument(String jpid, String dokref, String dokurl, String type, int status) {
    stubFor(get(urlPathMatching(String.format(PATH_DOKUMENT_TILGANG, jpid, dokref))).willReturn(
        aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withStatus(status)
            .withBody(String.join("\n", "{", "\"dokumentUrl\": \"" + dokurl + "\",", "\"type\": \"" + type + "\"", "}"))));
  }

  public void runGet(String path, Map<String, StringValuePattern> queryParams, HttpStatus status, String respons) {

    stubFor(get(urlPathMatching(path)).withQueryParams(queryParams)
        .willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withStatus(status.value()).withBody(respons)));
  }

  public void runGet(String path, HttpStatus status, String respons) {
    stubFor(get(urlPathMatching(path))
        .willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withStatus(status.value()).withBody(respons)));
  }

  public void runPost(String path, HttpStatus status, String respons) {
    stubFor(post(urlPathMatching(path))
        .willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withStatus(status.value()).withBody(respons)));
  }
}
