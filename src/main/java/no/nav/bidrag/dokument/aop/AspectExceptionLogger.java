package no.nav.bidrag.dokument.aop;

import no.nav.bidrag.commons.ExceptionLogger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AspectExceptionLogger {

  private final ExceptionLogger exceptionLogger;

  public AspectExceptionLogger(ExceptionLogger exceptionLogger) {
    this.exceptionLogger = exceptionLogger;
  }

  @AfterThrowing(pointcut = "within (no.nav.bidrag.dokument.controller..*)", throwing = "exception")
  public void logException(JoinPoint joinPoint, Exception exception) {
    exceptionLogger.logException(exception, String.valueOf(joinPoint.getSourceLocation().getWithinType()));
  }
}
