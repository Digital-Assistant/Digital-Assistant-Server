package com.nistapp.uda.index.config;

import io.quarkus.arc.profile.IfBuildProfile;
import com.nistapp.uda.index.utils.InputSanitizer;
import io.quarkus.runtime.configuration.MemorySize;
import io.vertx.core.http.HttpServerRequest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.ext.Provider;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

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

	private static final int READ_CHUNK_SIZE = 8192;

	/** Filter limit for JSON bodies of POST, PUT and PATCH. */
	@ConfigProperty(name = "udan.sanitizer.max-body-size", defaultValue = "5M")
	MemorySize maxBodySize;

	@Context
	HttpServerRequest httpRequest;

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

		long limit = maxBodySize.asLongValue();

		// Check the declared size before any read
		long declaredLength = parseContentLength(requestContext.getHeaderString("Content-Length"));
		if (declaredLength > limit) {
			rejectTooLarge(requestContext, String.valueOf(declaredLength));
			return;
		}

		// Read the body, at most limit + 1 bytes
		byte[] bodyBytes = readBounded(requestContext.getEntityStream(), limit);
		if (bodyBytes == null) {
			rejectTooLarge(requestContext, "over limit");
			return;
		}
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

	/**
	 * Parses a Content-Length value. Returns -1 when the value is missing, not a
	 * number or negative (size not known).
	 */
	private static long parseContentLength(String value) {
		if (value == null) {
			return -1;
		}
		try {
			long length = Long.parseLong(value.trim());
			return length < 0 ? -1 : length;
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	/**
	 * Reads the stream until its end, but never more than limit + 1 bytes.
	 *
	 * @return the bytes, or null if the stream has more than limit bytes
	 */
	private static byte[] readBounded(InputStream inputStream, long limit) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[READ_CHUNK_SIZE];
		long total = 0;
		while (total <= limit) {
			long remaining = limit + 1 - total;
			int n = inputStream.read(buffer, 0, (int) Math.min(buffer.length, remaining));
			if (n < 0) {
				return out.toByteArray();
			}
			out.write(buffer, 0, n);
			total += n;
		}
		return null;
	}

	/**
	 * Aborts the request with 413 (RFC 7807 body) and logs one WARN line. The log
	 * and the response never contain body text.
	 */
	private void rejectTooLarge(ContainerRequestContext requestContext, String size) {
		warnTooLarge(clientIp(), requestContext.getMethod(), requestContext.getUriInfo().getPath(), size);

		Map<String, Object> problem = new LinkedHashMap<>();
		problem.put("type", "https://tools.ietf.org/html/rfc7807");
		problem.put("title", "Request Entity Too Large");
		problem.put("status", Response.Status.REQUEST_ENTITY_TOO_LARGE.getStatusCode());
		problem.put("detail", "Request body exceeds the maximum allowed size");
		problem.put("timestamp", Instant.now().toString());

		requestContext.abortWith(Response.status(Response.Status.REQUEST_ENTITY_TOO_LARGE)
				.type(MediaType.APPLICATION_JSON)
				.entity(problem)
				.build());
	}

	private void warnTooLarge(String ip, String method, String path, String size) {
		logger.warn("Request body too large: ip={}, method={}, path={}, size={}", ip, method, path, size);
	}

	private String clientIp() {
		try {
			if (httpRequest != null && httpRequest.remoteAddress() != null) {
				String host = httpRequest.remoteAddress().host();
				if (host != null) {
					return host;
				}
			}
		} catch (RuntimeException e) {
			// fall through
		}
		return "unknown";
	}
}
