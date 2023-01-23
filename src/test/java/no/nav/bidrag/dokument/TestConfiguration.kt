package no.nav.bidrag.dokument

import io.mockk.every
import io.mockk.mockkClass
import no.nav.bidrag.commons.security.service.SecurityTokenService
import org.springframework.boot.devtools.remote.client.HttpHeaderInterceptor
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("mock-security")
@TestConfiguration
class TestConfig {
    @Bean
    @Primary
    fun securityTokenService2(): SecurityTokenService {
        val securityTokenService =  mockkClass(SecurityTokenService::class)
        every { securityTokenService.authTokenInterceptor(any()) } returns HttpHeaderInterceptor("test", "test")
        return securityTokenService
    }
}