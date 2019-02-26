package no.nav.bidrag.dokument.aop;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import no.nav.bidrag.dokument.dto.MedDtoId;
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
    var possibleDtoId = Arrays.stream(joinPoint.getArgs())
        .filter(o -> o instanceof MedDtoId)
        .map(o -> (MedDtoId) o)
        .findFirst();

    var dtoId = possibleDtoId.map(MedDtoId::getBeskrevetDtoId).orElseGet(() -> LocalDateTime.now().toString());
    var exceptionSource = within(joinPoint.getSourceLocation());

    LOGGER.error("{} - Exception caught in BidragDokument within {}", dtoId, exceptionSource);
    LOGGER.error("{} - Failed by {}", dtoId, exception.toString());
    possibleDtoId.ifPresent(medDtoId -> LOGGER.error("{} - Body {}", medDtoId.getBeskrevetDtoId(), medDtoId));

    logCause(dtoId, exception.getCause());
  }

  private String within(SourceLocation sourceLocation) {
    return String.valueOf(sourceLocation.getWithinType());
  }

  private void logCause(String beskrevetDtoId, Throwable cause) {
    Optional<Throwable> possibleCause = Optional.ofNullable(cause);

    while (possibleCause.isPresent()) {
      LOGGER.error("{} - Cause: {}", beskrevetDtoId, possibleCause.get());
      possibleCause = Optional.ofNullable(possibleCause.get().getCause());
    }
  }
}
