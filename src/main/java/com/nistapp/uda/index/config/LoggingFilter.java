package com.nistapp.uda.index.config;

import org.jboss.logging.MDC;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.UUID;

/**
 * JAX-RS filter to inject a correlation ID into the logging MDC (Mapped
 * Diagnostic Context).
 * It extracts the X-Correlation-ID header if present, otherwise generates a new
 * UUID.
 * The correlation ID is added to the MDC which makes it available for logging.
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_CORRELATION_ID_KEY = "correlationId";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String correlationId = requestContext.getHeaderString(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        // Put correlation ID into MDC so that the logging framework can access it
        MDC.put(MDC_CORRELATION_ID_KEY, correlationId);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        // Always clean up the MDC after the request is processed to avoid thread-local
        // leakages
        MDC.remove(MDC_CORRELATION_ID_KEY);
    }
}
