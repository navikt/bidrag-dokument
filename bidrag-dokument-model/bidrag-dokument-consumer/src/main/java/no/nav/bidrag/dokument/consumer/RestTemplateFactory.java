package no.nav.bidrag.dokument.consumer;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;

public final class RestTemplateFactory {

    private static RestTemplateFactory restTemplateFactory;

    private final InitRestTemplate initRestTemplate;

    private RestTemplateFactory(InitRestTemplate initRestTemplate) {
        this.initRestTemplate = initRestTemplate;
    }

    public static RestTemplate create(UriTemplateHandler uriTemplateHandler) {
        return restTemplateFactory.initRestTemplate.init(uriTemplateHandler);
    }

    private static InitRestTemplate defaultInitRestTemplate() {
        return uriTemplateHandler -> {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setUriTemplateHandler(uriTemplateHandler);

            return restTemplate;
        };
    }

    public static void use(InitRestTemplate initRestTemplate) {
        restTemplateFactory = new RestTemplateFactory(initRestTemplate);
    }

    private static void defaultRestTemplateFactory() {
        restTemplateFactory = new RestTemplateFactory(defaultInitRestTemplate());
    }

    public static void reset() {
        restTemplateFactory = new RestTemplateFactory(defaultInitRestTemplate());
    }

    static {
        defaultRestTemplateFactory();
    }

    @FunctionalInterface public interface InitRestTemplate {
        RestTemplate init(UriTemplateHandler uriTemplateHandler);
    }
}
