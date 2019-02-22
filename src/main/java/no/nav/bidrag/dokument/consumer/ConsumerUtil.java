package no.nav.bidrag.dokument.consumer;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public class ConsumerUtil {

    public static <T> HttpEntity<T> initHttpEntityWithSecurityHeader(T body, String bearerToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, bearerToken);

        return new HttpEntity<>(body, headers);
    }

}
