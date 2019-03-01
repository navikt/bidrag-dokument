package no.nav.bidrag.dokument.filter;

import java.io.IOException;
import java.util.stream.Stream;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class CorrelationIdFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorrelationIdFilter.class);
  private static final String CORRELATION_ID = "X_CORRELATION_ID";

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    var httpServletRequest = (HttpServletRequest) servletRequest;
    var httpServletResponse = (HttpServletResponse) servletResponse;
    var method = httpServletRequest.getMethod();
    var requestURI = httpServletRequest.getRequestURI();

    String correlationId;

    if (httpServletResponse.containsHeader(CORRELATION_ID)) {
      correlationId = httpServletResponse.getHeader(CORRELATION_ID);
    } else {
      correlationId = addCorreleationIdToHttpHeader(httpServletResponse, fetchedTimestamped(requestURI));
    }

    MDC.put(CORRELATION_ID, correlationId);

    LOGGER.info("Prosessing {} {}", method, requestURI);

    filterChain.doFilter(servletRequest, servletResponse);
    MDC.clear();
  }

  private String addCorreleationIdToHttpHeader(HttpServletResponse httpServletResponse, String correlationId) {
    httpServletResponse.addHeader(CORRELATION_ID, correlationId);

    return correlationId;
  }

  private String fetchedTimestamped(String requestUri) {
    String currentTimeAsString = Long.toHexString(System.currentTimeMillis());
    return currentTimeAsString + '(' + fetchLastPartOfRequestUri(requestUri) + ')';
  }

  private String fetchLastPartOfRequestUri(String requestUri) {
    return requestUri != null ? Stream.of(requestUri)
        .filter(uri -> uri.contains("/"))
        .map(uri -> uri.substring(uri.lastIndexOf('/') + 1))
        .findFirst()
        .orElse(requestUri) : "wtf???";
  }
}
