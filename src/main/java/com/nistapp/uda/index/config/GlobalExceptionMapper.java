package com.nistapp.uda.index.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception mapper that converts exceptions to RFC 7807 Problem Details
 * JSON responses.
 * <p>
 * This mapper handles:
 * <ul>
 * <li>{@link ConstraintViolationException} — Bean Validation failures
 * (400)</li>
 * <li>{@link WebApplicationException} — JAX-RS exceptions (preserves original
 * status)</li>
 * <li>{@link Exception} — All other uncaught exceptions (500)</li>
 * </ul>
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7807">RFC 7807 - Problem
 *      Details for HTTP APIs</a>
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof WebApplicationException wae) {
            return handleWebApplicationException(wae);
        }
        return handleGenericException(exception);
    }

    /**
     * Handles JAX-RS WebApplicationExceptions, preserving the original HTTP status.
     *
     * @param wae the web application exception
     * @return a response with the original status code
     */
    private Response handleWebApplicationException(WebApplicationException wae) {
        int status = wae.getResponse().getStatus();
        Map<String, Object> problem = buildProblemDetails(
                "https://tools.ietf.org/html/rfc7807",
                Response.Status.fromStatusCode(status).getReasonPhrase(),
                status,
                wae.getMessage(),
                null);

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(problem)
                .build();
    }

    /**
     * Handles all uncaught exceptions as Internal Server Errors.
     *
     * @param exception the uncaught exception
     * @return a 500 Internal Server Error response
     */
    private Response handleGenericException(Exception exception) {
        Map<String, Object> problem = buildProblemDetails(
                "https://tools.ietf.org/html/rfc7807",
                "Internal Server Error",
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "An unexpected error occurred",
                null);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(problem)
                .build();
    }

    /**
     * Builds a Problem Details JSON object per RFC 7807.
     *
     * @param type   a URI reference that identifies the problem type
     * @param title  a short, human-readable summary
     * @param status the HTTP status code
     * @param detail a human-readable explanation specific to this occurrence
     * @param extra  optional additional properties (e.g., validation violations)
     * @return a Map representing the Problem Details JSON
     */
    private Map<String, Object> buildProblemDetails(String type, String title, int status, String detail,
            Map<String, String> extra) {
        Map<String, Object> problem = new LinkedHashMap<>();
        problem.put("type", type);
        problem.put("title", title);
        problem.put("status", status);
        problem.put("detail", detail);
        problem.put("timestamp", Instant.now().toString());
        if (extra != null && !extra.isEmpty()) {
            problem.put("violations", extra);
        }
        return problem;
    }

    /**
     * Extracts the field name from a constraint violation's property path.
     *
     * @param violation the constraint violation
     * @return the field name that caused the violation
     */
    private String extractFieldName(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int lastDot = path.lastIndexOf('.');
        return lastDot > 0 ? path.substring(lastDot + 1) : path;
    }
}
