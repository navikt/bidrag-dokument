package no.nav.bidrag.dokument.aop;

import java.util.Optional;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.SourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExceptionLogger {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionLogger.class);

  @AfterThrowing(pointcut = "within (no.nav.bidrag.dokument.controller..*)", throwing = "exception")
  public void logException(JoinPoint joinPoint, Exception exception) {
    var exceptionSource = within(joinPoint.getSourceLocation());

    LOGGER.error("Exception caught in BidragDokument within {}", exceptionSource);
    LOGGER.error("Failed by {}", exception.toString());
    LOGGER.error("Arguments to controller: {}", joinPoint.getArgs());

    logCause(exception.getCause());
  }

  private String within(SourceLocation sourceLocation) {
    return String.valueOf(sourceLocation.getWithinType());
  }

  private void logCause(Throwable cause) {
    Optional<Throwable> possibleCause = Optional.ofNullable(cause);

    while (possibleCause.isPresent()) {
      LOGGER.error("Cause: {}", possibleCause.get());
      possibleCause = Optional.ofNullable(possibleCause.get().getCause());
    }
  }
}
