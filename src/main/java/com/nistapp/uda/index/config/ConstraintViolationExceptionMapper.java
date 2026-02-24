package com.nistapp.uda.index.config;

import jakarta.annotation.Priority;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Specific mapper for ConstraintViolationException to override the default
 * Quarkus mapper
 * and provide RFC 7807 compliant responses.
 */
@Provider
@Priority(Priorities.USER)
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Map<String, String> violations = exception.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> extractFieldName(v),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing + "; " + replacement));

        Map<String, Object> problem = new LinkedHashMap<>();
        problem.put("type", "https://tools.ietf.org/html/rfc7807");
        problem.put("title", "Constraint Violation");
        problem.put("status", Response.Status.BAD_REQUEST.getStatusCode());
        problem.put("detail", "Input validation failed");
        problem.put("timestamp", Instant.now().toString());
        problem.put("violations", violations);

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(problem)
                .build();
    }

    private String extractFieldName(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int lastDot = path.lastIndexOf('.');
        return lastDot > 0 ? path.substring(lastDot + 1) : path;
    }
}
