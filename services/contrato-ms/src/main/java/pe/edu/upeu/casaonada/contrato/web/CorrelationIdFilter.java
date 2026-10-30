package pe.edu.upeu.casaonada.contrato.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
  public static final String HEADER = "X-Trace-ID";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String traceId = request.getHeader(HEADER);
    if (traceId == null || !traceId.matches("[A-Za-z0-9_-]{1,80}"))
      traceId = UUID.randomUUID().toString();
    MDC.put("traceId", traceId);
    response.setHeader(HEADER, traceId);
    try {
      long started = System.nanoTime();
      try { chain.doFilter(request, response); } finally {
        org.slf4j.LoggerFactory.getLogger(getClass()).info("HTTP method={} path={} status={} durationMs={}", request.getMethod(), request.getRequestURI(), response.getStatus(), (System.nanoTime()-started)/1_000_000);
      }
    } finally {
      MDC.remove("traceId");
    }
  }
}
