package no.nav.bidrag.dokument.consumer;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.web.client.RestTemplate;

public final class RestTemplateFactory {

    private static RestTemplateFactory instance = new RestTemplateFactory();

    private final Map<String, RestTemplate> restTemplatesPerBaseUri = new HashMap<>();
    private final InitRestTemplate initRestTemplate;

    private RestTemplateFactory() {
        initRestTemplate = RestTemplate::new;
    }

    private RestTemplateFactory(InitRestTemplate initRestTemplate) {
        this.initRestTemplate = initRestTemplate;
    }

    static RestTemplate create(String baseUrl, String bearerToken) {
        return instance.createTemplate(baseUrl, bearerToken);
    }

    private RestTemplate createTemplate(String baseUrl, String bearerToken) {
        RestTemplate restTemplate;

        if (restTemplatesPerBaseUri.containsKey(baseUrl)) {
            restTemplate = restTemplatesPerBaseUri.get(baseUrl);
        } else {
            restTemplate = initRestTemplate.init();
            restTemplate.setUriTemplateHandler(new RootUriTemplateHandler(baseUrl));
            restTemplatesPerBaseUri.put(baseUrl, restTemplate);
        }

        return restTemplate;
    }

    public static void use(InitRestTemplate initRestTemplate) {
        instance = new RestTemplateFactory(initRestTemplate);
    }

    public static void reset() {
        instance = new RestTemplateFactory();
    }

    @FunctionalInterface
    public interface InitRestTemplate {
        RestTemplate init();
    }
}
