package no.nav.bidrag.dokument.consumer;

import org.springframework.web.client.RestTemplate;

public final class RestTemplateFactory {

    private static InitRestTemplate initRestTemplate = defaultInitRestTemplate();

    private RestTemplateFactory() {
        // final factory
    }

    static RestTemplate create() {
        return initRestTemplate.init();
    }

    private static InitRestTemplate defaultInitRestTemplate() {
        return RestTemplate::new;
    }

    public static void use(InitRestTemplate initRestTemplate) {
        RestTemplateFactory.initRestTemplate = initRestTemplate;
    }

    public static void reset() {
        initRestTemplate = defaultInitRestTemplate();
    }

    @FunctionalInterface public interface InitRestTemplate {
        RestTemplate init();
    }
}
