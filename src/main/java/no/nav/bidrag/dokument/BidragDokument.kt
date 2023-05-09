package no.nav.bidrag.dokument

import mu.KotlinLogging
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.SpringApplication
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration

val sikkerLogg = KotlinLogging.logger("secureLogger")

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class, ManagementWebSecurityAutoConfiguration::class])
@EnableJwtTokenValidation(ignore = ["org.springdoc"])
class BidragDokument

fun main(args: Array<String>) {
    val profile = if (args.isEmpty()) BidragDokumentConfig.NAIS_PROFILE else args[0]
    val app = SpringApplication(BidragDokument::class.java)
    app.setAdditionalProfiles(profile)
    app.run(*args)
}
