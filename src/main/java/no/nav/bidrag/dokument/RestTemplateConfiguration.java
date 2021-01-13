package no.nav.bidrag.dokument;

import java.util.Optional;
import no.nav.bidrag.commons.web.CorrelationIdFilter;
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate;
import no.nav.bidrag.dokument.BidragDokumentConfig.OidcTokenManager;
import no.nav.security.token.support.client.core.ClientProperties;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

  @Bean("aad-bdj")
  @Scope("prototype")
  public RestTemplate restTemplateAzureBidragDokumentJournalpost(
      ClientConfigurationProperties clientConfigurationProperties,
      OAuth2AccessTokenService oAuth2AccessTokenService,
      RestTemplateBuilder restTemplateBuilder) {
    return azureRestTemplate(
        "bidrag-dokument-journalpost",
        clientConfigurationProperties,
        oAuth2AccessTokenService,
        restTemplateBuilder);
  }

  @Bean("aad-bda")
  @Scope("prototype")
  public RestTemplate restTemplateAzureBidragDokumentArkiv(
      ClientConfigurationProperties clientConfigurationProperties,
      OAuth2AccessTokenService oAuth2AccessTokenService,
      RestTemplateBuilder restTemplateBuilder) {
    return azureRestTemplate(
        "bidrag-dokument-arkiv",
        clientConfigurationProperties,
        oAuth2AccessTokenService,
        restTemplateBuilder);
  }

  @Bean("isso")
  @Scope("prototype")
  public RestTemplate restTemplateIsso(OidcTokenManager oidcTokenManager) {
    HttpHeaderRestTemplate httpHeaderRestTemplate = new HttpHeaderRestTemplate();

    httpHeaderRestTemplate.addHeaderGenerator(
        HttpHeaders.AUTHORIZATION, () -> "Bearer " + oidcTokenManager.fetchToken());
    httpHeaderRestTemplate.addHeaderGenerator(
        CorrelationIdFilter.CORRELATION_ID_HEADER,
        CorrelationIdFilter::fetchCorrelationIdForThread);

    return httpHeaderRestTemplate;
  }

  private ClientHttpRequestInterceptor bearerTokenInterceptor(
      ClientProperties clientProperties, OAuth2AccessTokenService oAuth2AccessTokenService) {
    return (request, body, execution) -> {
      OAuth2AccessTokenResponse response =
          oAuth2AccessTokenService.getAccessToken(clientProperties);
      request.getHeaders().setBearerAuth(response.getAccessToken());
      return execution.execute(request, body);
    };
  }

  private RestTemplate azureRestTemplate(
      String clientName,
      ClientConfigurationProperties clientConfigurationProperties,
      OAuth2AccessTokenService oAuth2AccessTokenService,
      RestTemplateBuilder restTemplateBuilder) {
    ClientProperties clientProperties =
        Optional.ofNullable(clientConfigurationProperties.getRegistration().get(clientName))
            .orElseThrow(
                () ->
                    new RuntimeException("could not find oauth2 client config for " + clientName));
    return restTemplateBuilder
        .additionalInterceptors(bearerTokenInterceptor(clientProperties, oAuth2AccessTokenService))
        .build();
  }
}
