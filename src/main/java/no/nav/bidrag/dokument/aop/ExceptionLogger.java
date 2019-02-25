package no.nav.bidrag.dokument.aop;

import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import no.nav.bidrag.dokument.dto.MedDtoId;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExceptionLogger {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionLogger.class);

  @AfterThrowing(pointcut = "execution (* no.nav.bidrag.dokument.controller..*Controller(..))", throwing = "exception")
  public void logException(JoinPoint joinPoint, Exception exception) {
    var dtoArguments = Arrays.stream(joinPoint.getArgs())
        .filter(o -> o instanceof MedDtoId)
        .map(o -> (MedDtoId) o)
        .collect(toList());

    for (MedDtoId medDtoId : dtoArguments) {
      LOGGER.error("{} - Error in {}", medDtoId.getBeskrevetDtoId(), joinPoint.toShortString());
      LOGGER.error("{} - Failed by {}", medDtoId.getBeskrevetDtoId(), exception);
      LOGGER.error("{} - Body {}", medDtoId.getBeskrevetDtoId(), medDtoId);

      logCause(medDtoId.getBeskrevetDtoId(), exception.getCause());
    }

    if (dtoArguments.isEmpty()) {
      String timestamp = String.valueOf(LocalDateTime.now());

      LOGGER.error("{} - Error in {}", timestamp, joinPoint.toShortString());
      LOGGER.error("{} - Failed by {}", timestamp, exception);

      logCause(timestamp, exception.getCause());
    }
  }

  private void logCause(String beskrevetDtoId, Throwable cause) {
    Optional<Throwable> possibleCause = Optional.ofNullable(cause);

    while (possibleCause.isPresent()) {
      LOGGER.error("{} - Cause: {}", beskrevetDtoId, possibleCause.get());
      possibleCause = Optional.ofNullable(possibleCause.get().getCause());
    }
  }
}
