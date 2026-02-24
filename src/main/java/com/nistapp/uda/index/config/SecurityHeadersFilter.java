package com.nistapp.uda.index.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

/**
 * JAX-RS response filter that adds security headers to all HTTP responses.
 * <p>
 * Headers added:
 * <ul>
 * <li><strong>X-Content-Type-Options</strong>: Prevents MIME type sniffing</li>
 * <li><strong>X-Frame-Options</strong>: Prevents clickjacking by disabling
 * iframe embedding</li>
 * <li><strong>X-XSS-Protection</strong>: Enables browser-level XSS
 * filtering</li>
 * <li><strong>Strict-Transport-Security</strong>: Enforces HTTPS for 1
 * year</li>
 * <li><strong>Content-Security-Policy</strong>: Restricts resource origins to
 * self</li>
 * <li><strong>Referrer-Policy</strong>: Limits referrer information
 * leakage</li>
 * <li><strong>Permissions-Policy</strong>: Disables camera, microphone, and
 * geolocation access</li>
 * </ul>
 *
 * @see <a href="https://owasp.org/www-project-secure-headers/">OWASP Secure
 *      Headers Project</a>
 */
@Provider
public class SecurityHeadersFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        responseContext.getHeaders().add("X-Content-Type-Options", "nosniff");
        responseContext.getHeaders().add("X-Frame-Options", "DENY");
        responseContext.getHeaders().add("X-XSS-Protection", "1; mode=block");
        responseContext.getHeaders().add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        responseContext.getHeaders().add("Content-Security-Policy", "default-src 'self'");
        responseContext.getHeaders().add("Referrer-Policy", "strict-origin-when-cross-origin");
        responseContext.getHeaders().add("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
    }
}
