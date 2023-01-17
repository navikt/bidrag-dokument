package no.nav.bidrag.dokument.aop

import no.nav.bidrag.commons.web.WebUtil
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class BidragDokumentRestControllerAdvice : ResponseEntityExceptionHandler() {
    @ResponseBody
    @ExceptionHandler
    fun handleOtherExceptions(exception: Exception): ResponseEntity<*> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(HttpHeaders.WARNING, "Det skjedde en ukjent feil: " + exception.message)
            .build<Any>()
    }

    @ResponseBody
    @ExceptionHandler
    fun handleHttClientErrorException(httpClientErrorException: HttpStatusCodeException): ResponseEntity<*> {
        return ResponseEntity.status(httpClientErrorException.statusCode)
            .header(HttpHeaders.WARNING, getWarningHeader(httpClientErrorException))
            .build<Any>()
    }

    @ExceptionHandler(value = [JwtTokenUnauthorizedException::class])
    protected fun handeUnauthorized(
        ex: JwtTokenUnauthorizedException?, request: WebRequest?
    ): ResponseEntity<Any> {
        val message = "Ugyldig sikkerhetstoken"
        return handleExceptionInternal(
            ex!!,
            message,
            WebUtil.initHttpHeadersWith(HttpHeaders.WARNING, message),
            HttpStatus.UNAUTHORIZED,
            request!!
        )
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