package no.nav.bidrag.dokument.aop;

import java.util.Arrays;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AspectExceptionLogger {
  private static final Logger LOGGER = LoggerFactory.getLogger(AspectExceptionLogger.class);

  @AfterThrowing(pointcut = "within (no.nav.bidrag.dokument.controller..*)", throwing = "exception")
  public void logException(JoinPoint joinPoint, Exception exception) {
    LOGGER.warn("Det skjedde en feil i controller metoden {}", joinPoint.getSignature().toShortString() + "| Args => " + Arrays.asList(joinPoint.getArgs()), exception);
  }
}
