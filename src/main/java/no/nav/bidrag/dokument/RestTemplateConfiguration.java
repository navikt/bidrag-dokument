package no.nav.bidrag.dokument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestTemplateConfiguration.class);

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplateLogger();
  }

  private static class RestTemplateLogger extends RestTemplate {

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables)
        throws RestClientException {
      return logExchange(url, () -> super.exchange(url, method, requestEntity, responseType, uriVariables));
    }

    @SuppressWarnings("unchecked")
    private <T> ResponseEntity<T> logExchange(String url, RestCaller restCaller) {
      try {
        return (ResponseEntity<T>) restCaller.doRestApi();
      } catch (RuntimeException e) {
        LOGGER.error("Failed to execute rest api - {}, {}: {}", this.getUriTemplateHandler(),  url, e);

        throw e;
      }
    }

    @FunctionalInterface
    private interface RestCaller {

      Object doRestApi();
    }
  }
}
