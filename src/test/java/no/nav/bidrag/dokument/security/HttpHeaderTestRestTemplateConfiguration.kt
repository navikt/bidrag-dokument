package no.nav.bidrag.dokument.security

import com.nimbusds.jose.JOSEObjectType
import no.nav.bidrag.dokument.BidragDokumentTest
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders

@Configuration
@Profile(BidragDokumentTest.TEST_PROFILE)
class HttpHeaderTestRestTemplateConfiguration {

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @Bean
    fun securedTestRestTemplate(testRestTemplate: TestRestTemplate): HttpHeaderTestRestTemplate {
        val httpHeaderTestRestTemplate = HttpHeaderTestRestTemplate(testRestTemplate)
        httpHeaderTestRestTemplate.add(HttpHeaders.AUTHORIZATION) {
            generateTestToken()
        }
        return httpHeaderTestRestTemplate
    }

    private fun generateTestToken(): String {
        val iss = mockOAuth2Server.issuerUrl("aad")
        val newIssuer = iss.newBuilder().host("localhost").build()

        val token = mockOAuth2Server.issueToken(
            "aad",
            "aud-localhost",
            DefaultOAuth2TokenCallback(
                "aad",
                "aud-localhost",
                JOSEObjectType.JWT.type,
                listOf("aud-localhost"),
                mapOf(Pair("iss", newIssuer.toString())),
                3600,
            ),
        )
        return "Bearer " + token.serialize()
    }
}
