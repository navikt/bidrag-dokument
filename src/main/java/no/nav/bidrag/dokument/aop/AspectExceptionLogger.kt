package no.nav.bidrag.dokument.aop

import mu.two.KotlinLogging
import no.nav.bidrag.dokument.sikkerLogg
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Aspect
@Component
class AspectExceptionLogger {
    @AfterThrowing(pointcut = "within (no.nav.bidrag.dokument.controller..*)", throwing = "exception")
    fun logException(joinPoint: JoinPoint, exception: Exception?) {
        log.warn(exception) { "Det skjedde en feil i controller metoden ${joinPoint.signature.toShortString()}. Se sikkerlogg for detaljer" }
        sikkerLogg.warn(exception) { "Det skjedde en feil i controller metoden ${joinPoint.signature.toShortString()} | Argumenter => ${joinPoint.args.joinToString(", ")}" }
    }
}
