package no.nav.bidrag.dokument.aop

import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

private val log = KotlinLogging.logger {}

@RestControllerAdvice
class BidragDokumentRestControllerAdvice : ResponseEntityExceptionHandler() {
    @ResponseBody
    @ExceptionHandler
    fun handleOtherExceptions(exception: Exception): ResponseEntity<*> {
        log.error(exception) { "Det skjedde en ukjent feil: " + exception.message }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(HttpHeaders.WARNING, "Det skjedde en ukjent feil: " + exception.message)
            .build<Any>()
    }

    @ResponseBody
    @ExceptionHandler
    fun handleOtherErrors(error: Error): ResponseEntity<*> {
        log.error(error) { "Det skjedde en ukjent feil: " + error.message }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(
                HttpHeaders.WARNING,
                "Det skjedde en ukjent feil: " + error.javaClass.simpleName,
            )
            .build<Any>()
    }

    @ResponseBody
    @ExceptionHandler
    fun handleHttClientErrorException(httpClientErrorException: HttpStatusCodeException): ResponseEntity<*> {
        log.warn(httpClientErrorException) { getWarningHeader(httpClientErrorException) }
        return ResponseEntity.status(httpClientErrorException.statusCode)
            .header(HttpHeaders.WARNING, getWarningHeader(httpClientErrorException))
            .build<Any>()
    }

    @ExceptionHandler(value = [JwtTokenUnauthorizedException::class])
    protected fun handeUnauthorized(
        ex: JwtTokenUnauthorizedException?,
        req: HttpServletRequest?,
    ): ResponseEntity<*> {
        log.warn(ex) { "Ugyldig sikkerhetstoken for url=${req?.requestURL}" }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .header(HttpHeaders.WARNING, "Ugyldig sikkerhetstoken")
            .build<Any>()
    }

    private fun getWarningHeader(httpClientErrorException: HttpStatusCodeException): String? {
        val message = httpClientErrorException.message
        if (httpClientErrorException.responseHeaders == null) {
            return message
        }
        val warningHeaders = httpClientErrorException.responseHeaders!![HttpHeaders.WARNING]
        return if (warningHeaders.isNullOrEmpty()) message else warningHeaders[0]
    }
}
