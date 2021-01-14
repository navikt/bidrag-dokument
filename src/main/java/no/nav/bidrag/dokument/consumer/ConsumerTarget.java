package no.nav.bidrag.dokument.consumer;

import lombok.Builder;
import lombok.Getter;
import no.nav.bidrag.dokument.BidragDokumentConfig.RestTemplateProvider;

@Getter
@Builder
public class ConsumerTarget {

  private RestTemplateProvider restTemplateProvider;
  private String baseUrl;
}
