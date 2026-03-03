package com.nistapp.uda.index.config;

import io.quarkus.arc.profile.IfBuildProfile;
import com.nistapp.uda.index.utils.InputSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.ext.Provider;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * JAX-RS request filter that sanitizes all incoming query parameters and
 * JSON request bodies to prevent XSS and script injection attacks.
 * <p>
 * This filter runs before request matching ({@link PreMatching}) so it
 * processes ALL incoming requests. It:
 * <ul>
 * <li>Sanitizes all query parameter values</li>
 * <li>Sanitizes JSON request body content (for POST/PUT/PATCH)</li>
 * <li>Logs warnings when malicious content is detected and stripped</li>
 * </ul>
 *
 * @see InputSanitizer
 */
@Provider
@PreMatching
@IfBuildProfile("!dev")
public class XssSanitizationFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(XssSanitizationFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // 1. Sanitize query parameters
        sanitizeQueryParams(requestContext);

        // 2. Sanitize request body (for POST/PUT/PATCH with JSON content)
        sanitizeRequestBody(requestContext);
    }

    /**
     * Sanitizes all query parameter values in the incoming request.
     * Rebuilds the request URI with sanitized parameter values.
     */
    private void sanitizeQueryParams(ContainerRequestContext requestContext) {
        MultivaluedMap<String, String> queryParams = requestContext.getUriInfo().getQueryParameters();
        if (queryParams.isEmpty()) {
            return;
        }

        boolean modified = false;
        UriBuilder uriBuilder = requestContext.getUriInfo().getRequestUriBuilder();

        // Clear existing query params and re-add sanitized versions
        uriBuilder.replaceQuery("");

        for (var entry : queryParams.entrySet()) {
            String paramName = entry.getKey();
            for (String value : entry.getValue()) {
                String sanitized = InputSanitizer.sanitize(value);
                if (!value.equals(sanitized)) {
                    logger.warn(
                            "XSS content detected and sanitized in query param '{}': original length={}, sanitized length={}",
                            paramName, value.length(), sanitized.length());
                    modified = true;
                }
                uriBuilder.queryParam(paramName, sanitized);
            }
        }

        if (modified) {
            requestContext.setRequestUri(uriBuilder.build());
        }
    }

    /**
     * Sanitizes the JSON request body for POST, PUT, and PATCH requests.
     * Reads the body as a string, applies sanitization, and replaces the
     * input stream with the sanitized content.
     */
    private void sanitizeRequestBody(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        if (!"POST".equals(method) && !"PUT".equals(method) && !"PATCH".equals(method)) {
            return;
        }

        // Only process if there's a content type and it's JSON
        if (requestContext.getMediaType() == null) {
            return;
        }
        String contentType = requestContext.getMediaType().toString();
        if (!contentType.contains("json")) {
            return;
        }

        // Read the body
        InputStream inputStream = requestContext.getEntityStream();
        byte[] bodyBytes = inputStream.readAllBytes();
        String body = new String(bodyBytes, StandardCharsets.UTF_8);

        if (body.isEmpty()) {
            // Restore stream for downstream handlers
            requestContext.setEntityStream(new ByteArrayInputStream(bodyBytes));
            return;
        }

        // Sanitize the body
        String sanitizedBody = InputSanitizer.sanitize(body);

        if (!body.equals(sanitizedBody)) {
            logger.warn(
                    "XSS content detected and sanitized in request body: method={}, path={}, original length={}, sanitized length={}",
                    method,
                    requestContext.getUriInfo().getPath(),
                    body.length(),
                    sanitizedBody.length());
            // Replace the entity stream with sanitized content
            requestContext.setEntityStream(new ByteArrayInputStream(sanitizedBody.getBytes(StandardCharsets.UTF_8)));
        } else {
            // Restore original stream if no sanitization needed
            requestContext.setEntityStream(new ByteArrayInputStream(bodyBytes));
        }
    }
}
