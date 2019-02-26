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

  @AfterThrowing(pointcut = "within (no.nav.bidrag.dokument.controller..*)", throwing = "exception")
  public void logException(JoinPoint joinPoint, Exception exception) {
    var dtoArguments = Arrays.stream(joinPoint.getArgs())
        .filter(o -> o instanceof MedDtoId)
        .map(o -> (MedDtoId) o)
        .collect(toList());

    for (MedDtoId medDtoId : dtoArguments) {
      var beskrevetDtoId = medDtoId.getBeskrevetDtoId();
      LOGGER.error("{} - Exception caught in BidragDokument {}: {}", beskrevetDtoId, joinPoint.getSignature(), joinPoint.getSourceLocation());
      LOGGER.error("{} - Failed by {}", beskrevetDtoId, exception.toString());
      LOGGER.error("{} - Body {}", beskrevetDtoId, medDtoId);

      logCause(beskrevetDtoId, exception.getCause());
    }

    if (dtoArguments.isEmpty()) {
      LOGGER.error("Exception caught in BidragDokument {}: {}", joinPoint.getSignature(), joinPoint.getSourceLocation());
      LOGGER.error("Failed by {}", exception.toString());

      logCause(String.valueOf(LocalDateTime.now()), exception.getCause());
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
