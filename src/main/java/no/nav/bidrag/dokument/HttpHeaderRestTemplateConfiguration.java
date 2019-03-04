package no.nav.bidrag.dokument;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

public class HttpHeaderRestTemplateConfiguration extends RestTemplate {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpHeaderRestTemplateConfiguration.class);

  private Set<HeaderGenerator> headerGenerators = new HashSet<>();

  @Override
  @SuppressWarnings("unchecked")
  public <T> RequestCallback httpEntityCallback(Object requestBody, Type responseType) {
    if (headerGenerators.isEmpty()) {
      return super.httpEntityCallback(requestBody, responseType);
    }

    HttpEntity<T> httpEntity = Optional.ofNullable(requestBody)
        .map(this::newEntityWithAdditionalHttpHeaders)
        .orElseGet(() -> new HttpEntity<>(null, generateHttpHeaders()));

    httpEntity.getHeaders().forEach((name, values) -> LOGGER.info("Using {}: {}", name, values.get(0)));

    return super.httpEntityCallback(httpEntity, responseType);
  }

  private HttpHeaders generateHttpHeaders() {
    HttpHeaders httpHeaders = new HttpHeaders();

    headerGenerators.stream()
        .map(HeaderGenerator::generate)
        .forEach(header -> httpHeaders.add(header.name(), header.value()));

    return httpHeaders;
  }

  @SuppressWarnings("unchecked")
  private HttpEntity newEntityWithAdditionalHttpHeaders(Object o) {
    HttpEntity httpEntity = Stream.of(o)
        .filter(obj -> obj instanceof HttpEntity)
        .map(obj -> (HttpEntity) obj)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            String.format("%s cannot be used as a request body for a HttpEntityCallback", o.getClass().getSimpleName())
        ));

    MultiValueMap<String, String> allHeaders = combineHeaders(httpEntity.getHeaders());

    return new HttpEntity(httpEntity.getBody(), allHeaders);
  }

  private MultiValueMap<String, String> combineHeaders(HttpHeaders existingHeaders) {
    HttpHeaders allHeaders = new HttpHeaders();
    existingHeaders.forEach((name, listValue) -> listValue.forEach(value -> allHeaders.add(name, value)));

    headerGenerators.stream()
        .map(HeaderGenerator::generate)
        .forEach(header -> allHeaders.add(header.name(), header.value()));

    return allHeaders;
  }

  public void addHeaderGenerator(HeaderGenerator headerGenerator) {
    headerGenerators.add(headerGenerator);
  }

  @FunctionalInterface
  public interface HeaderGenerator {

    Header generate();
  }

  public interface Header {

    String name();

    String value();
  }
}
