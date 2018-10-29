package no.nav.bidrag.dokument.consumer;

import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public final class RestTemplateFactory {
    private static final Map<String, RestTemplate> restTemplatesPerBaseUri = new HashMap<>();

    private static InitRestTemplate initRestTemplate = defaultInitRestTemplate();

    private RestTemplateFactory() {
        // final factory
    }

    static RestTemplate create(String baseUrl) {
        restTemplatesPerBaseUri.putIfAbsent(baseUrl, initRestTemplate.init());
        return restTemplatesPerBaseUri.get(baseUrl);
    }

    private static InitRestTemplate defaultInitRestTemplate() {
        return RestTemplate::new;
    }

    public static void use(InitRestTemplate initRestTemplate) {
        restTemplatesPerBaseUri.clear();
        RestTemplateFactory.initRestTemplate = initRestTemplate;
    }

    public static void reset() {
        use(defaultInitRestTemplate());
    }

    @FunctionalInterface public interface InitRestTemplate {
        RestTemplate init();
    }
}
