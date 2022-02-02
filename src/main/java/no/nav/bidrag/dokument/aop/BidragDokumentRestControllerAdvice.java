package no.nav.bidrag.dokument.aop;

import static no.nav.bidrag.commons.web.WebUtil.initHttpHeadersWith;

import no.nav.bidrag.commons.ExceptionLogger;
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class BidragDokumentRestControllerAdvice extends ResponseEntityExceptionHandler {

  private final ExceptionLogger exceptionLogger;

  public BidragDokumentRestControllerAdvice(ExceptionLogger exceptionLogger) {
    this.exceptionLogger = exceptionLogger;
  }

  @ResponseBody
  @ExceptionHandler
  public ResponseEntity<?> handleHttClientErrorException(HttpClientErrorException httpClientErrorException) {
    return ResponseEntity.status(httpClientErrorException.getStatusCode())
        .header(HttpHeaders.WARNING, getWarningHeader(httpClientErrorException))
        .build();
  }

  @ExceptionHandler(value = JwtTokenUnauthorizedException.class)
  protected ResponseEntity<Object> handeUnauthorized(
      final JwtTokenUnauthorizedException ex, final WebRequest request) {
    var message = "Ugyldig sikkerhetstoken";

    exceptionLogger.logException(ex, "RestResponseEntityExceptionHandler");

    return handleExceptionInternal(
        ex,
        message,
        initHttpHeadersWith(HttpHeaders.WARNING, message),
        HttpStatus.UNAUTHORIZED,
        request);
  }

  private String getWarningHeader(HttpClientErrorException httpClientErrorException){
    var message = httpClientErrorException.getMessage();
    if (httpClientErrorException.getResponseHeaders() == null) {
      return message;
    }
    var warningHeaders = httpClientErrorException.getResponseHeaders().get(HttpHeaders.WARNING);
    if (warningHeaders == null || warningHeaders.isEmpty()){
      return message;
    }
    return warningHeaders.get(0);
  }
}
