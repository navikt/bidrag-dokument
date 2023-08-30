package no.nav.bidrag.dokument

import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import mu.KotlinLogging
import no.nav.bidrag.commons.security.api.EnableSecurityConfiguration
import no.nav.bidrag.commons.security.service.SecurityTokenService
import no.nav.bidrag.commons.web.CorrelationIdFilter
import no.nav.bidrag.commons.web.DefaultCorsFilter
import no.nav.bidrag.commons.web.EnhetFilter
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.commons.web.UserMdcFilter
import no.nav.bidrag.dokument.consumer.BidragDokumentConsumer
import no.nav.bidrag.dokument.consumer.DokumentTilgangConsumer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RootUriTemplateHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Import
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

private val log = KotlinLogging.logger {}

@Configuration
@OpenAPIDefinition(
    info = Info(title = "bidrag-dokument", version = "v1"),
    security = [SecurityRequirement(name = "bearer-key")]
)
@SecurityScheme(
    bearerFormat = "JWT",
    name = "bearer-key",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP
)
@EnableAspectJAutoProxy
@EnableSecurityConfiguration
@Import(
    EnhetFilter::class,
    DefaultCorsFilter::class,
    UserMdcFilter::class,
    CorrelationIdFilter::class
)
class BidragDokumentConfig {
    @Bean
    @Qualifier(MIDL_BREVLAGER_QUALIFIER)
    fun bidragJournalpostConsumer(
        @Value("\${JOURNALPOST_URL}") journalpostBaseUrl: String,
        securityTokenService: SecurityTokenService
    ): BidragDokumentConsumer {
        val restTemplate = createRestTemplate(
            journalpostBaseUrl,
            securityTokenService,
            KLIENTNAVN_BIDRAG_DOKUMENT_JOURNALPOST
        )
        return BidragDokumentConsumer(restTemplate, journalpostBaseUrl)
    }

    @Bean
    @Qualifier(ARKIV_QUALIFIER)
    fun bidragArkivConsumer(
        @Value("\${BIDRAG_ARKIV_URL}") bidragArkivBaseUrl: String,
        securityTokenService: SecurityTokenService
    ): BidragDokumentConsumer {
        val restTemplate = createRestTemplate(
            bidragArkivBaseUrl,
            securityTokenService,
            KLIENTNAVN_BIDRAG_DOKUMENT_ARKIV
        )
        return BidragDokumentConsumer(restTemplate, bidragArkivBaseUrl)
    }

    @Bean
    @Qualifier(FORSENDELSE_QUALIFIER)
    fun bidragForsendelseConsumer(
        @Value("\${BIDRAG_FORSENDELSE_URL}") bidragForsendelseUrl: String,
        securityTokenService: SecurityTokenService
    ): BidragDokumentConsumer {
        val restTemplate = createRestTemplate(
            bidragForsendelseUrl,
            securityTokenService,
            KLIENTNAVN_BIDRAG_DOKUMENT_FORSENDELSE
        )
        return BidragDokumentConsumer(restTemplate, bidragForsendelseUrl)
    }

    @Bean
    fun dokumentConsumer(
        @Value("\${JOURNALPOST_URL}") journalpostBaseUrl: String,
        securityTokenService: SecurityTokenService
    ): DokumentTilgangConsumer {
        val restTemplate = createRestTemplate(
            journalpostBaseUrl,
            securityTokenService,
            KLIENTNAVN_BIDRAG_DOKUMENT_JOURNALPOST
        )
        return DokumentTilgangConsumer(restTemplate)
    }

    private fun createRestTemplate(
        baseUrl: String,
        securityTokenService: SecurityTokenService,
        clientId: String
    ): RestTemplate {
        val requestFactory = HttpComponentsClientHttpRequestFactory()
        requestFactory.setBufferRequestBody(false)
        val httpHeaderRestTemplate = HttpHeaderRestTemplate()
        httpHeaderRestTemplate.interceptors.add(securityTokenService.authTokenInterceptor(clientId))
        httpHeaderRestTemplate.withDefaultHeaders()
        httpHeaderRestTemplate.requestFactory = requestFactory
        httpHeaderRestTemplate.uriTemplateHandler = RootUriTemplateHandler(baseUrl)
        return httpHeaderRestTemplate
    }

    @Bean
    fun timedAspect(registry: MeterRegistry): TimedAspect {
        return TimedAspect(registry)
    }

    companion object {
        const val DELIMTER = "-"
        const val PREFIX_BIDRAG = "BID"
        const val PREFIX_JOARK = "JOARK"
        const val MIDL_BREVLAGER_QUALIFIER = "midlertidigBrevlager"
        const val ARKIV_QUALIFIER = "arkiv"
        const val FORSENDELSE_QUALIFIER = "forsendelse"
        const val KLIENTNAVN_BIDRAG_DOKUMENT_FORSENDELSE = "bidrag-dokument-forsendelse"
        const val KLIENTNAVN_BIDRAG_DOKUMENT_ARKIV = "bidrag-dokument-arkiv"
        const val KLIENTNAVN_BIDRAG_DOKUMENT_JOURNALPOST = "bidrag-dokument-journalpost"
        const val NAIS_PROFILE = "nais"
    }
}
