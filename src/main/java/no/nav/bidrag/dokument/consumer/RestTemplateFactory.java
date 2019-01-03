package no.nav.bidrag.dokument.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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
        CloseableHttpClient httpClient;
        
        Header authorization = new BasicHeader(HttpHeaders.AUTHORIZATION, bearerToken);
        
        List<Header> headers = new ArrayList<>();
        headers.add(authorization);
        
        httpClient = HttpClients.custom().setDefaultHeaders(headers).build();

        if (restTemplatesPerBaseUri.containsKey(baseUrl)) {
            restTemplate = restTemplatesPerBaseUri.get(baseUrl);
        } else {
            restTemplate = initRestTemplate.init();
            restTemplate.setUriTemplateHandler(new RootUriTemplateHandler(baseUrl));
            restTemplatesPerBaseUri.put(baseUrl, restTemplate);
        }
        
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));

        return restTemplate;
    }

    public static void use(InitRestTemplate initRestTemplate) {
        instance = new RestTemplateFactory(initRestTemplate);
    }

    public static void reset() {
        instance = new RestTemplateFactory();
    }

    @FunctionalInterface public interface InitRestTemplate {
        RestTemplate init();
    }
}
