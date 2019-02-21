package no.nav.bidrag.dokument;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestTemplateConfiguration.class);

  @Bean
  @Scope("prototype")
  public RestTemplate restTemplate() {
    return new RestTemplateLogger();
  }

  private static class RestTemplateLogger extends RestTemplate {

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables)
        throws RestClientException {

      return logRestApi(url, method, () -> super.exchange(url, method, requestEntity, responseType, uriVariables));
    }

    @SuppressWarnings("unchecked")
    private <T> ResponseEntity<T> logRestApi(String url, HttpMethod method, RestCaller restCaller) {
      try {
        return (ResponseEntity<T>) restCaller.doRestApi();
      } catch (RuntimeException e) {
        var baseUrl = Optional.ofNullable(getUriTemplateHandler())
            .filter(handler -> handler instanceof RootUriTemplateHandler)
            .map(handler -> (RootUriTemplateHandler) handler)
            .map(RootUriTemplateHandler::getRootUri)
            .orElse("RestTemplate not configured correctly");

        LOGGER.error("Failed to execute rest api, {}, '{}{}': {}", method, baseUrl, url, e.getMessage());

        Throwable cause = e.getCause();
        logCause(cause);

        throw e;
      }
    }

    private void logCause(Throwable cause) {
      Optional<Throwable> possibleCause = Optional.ofNullable(cause);

      while (possibleCause.isPresent()) {
        LOGGER.error("Cause: " + possibleCause.get());
        possibleCause = Optional.ofNullable(possibleCause.get().getCause());
      }
    }

    @FunctionalInterface
    private interface RestCaller {

      Object doRestApi();
    }
  }
}
