package no.nav.bidrag.dokument;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration;
import no.nav.security.spring.oidc.api.EnableOIDCTokenValidation;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BidragDokumentLocal.class)
@ActiveProfiles("dev")
@DisplayName("BidragDokument")
public class BidragDokumentTest {
    
    private final static String idToken = "eyJraWQiOiJsb2NhbGhvc3Qtc2lnbmVyIiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiIxMjM0NTY3ODkxMCIsImF1ZCI6ImF1ZC1sb2NhbGhvc3QiLCJhY3IiOiJMZXZlbDQiLCJ2ZXIiOiIxLjAiLCJuYmYiOjE1NDY0MTk5MjcsImF1dGhfdGltZSI6MTU0NjQxOTkyNywiaXNzIjoiaXNzLWxvY2FsaG9zdCIsImV4cCI6MjMyNDAxOTkyNywibm9uY2UiOiJteU5vbmNlIiwiaWF0IjoxNTQ2NDE5OTI3LCJqdGkiOiIwNTM4MDhhNS01MDg1LTQ1NzQtOGE0Ni1jZDEwMTExZTE5YjcifQ";
    private final static String bearerToken = "Bearer " + idToken;

    @DisplayName("skal laste spring-context")
    @Test void shouldLoadContext() {
    }
    
    public static String bearer() {
        return bearerToken;
    }
}
