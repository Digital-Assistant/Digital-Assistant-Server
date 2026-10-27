package com.nistapp.uda.index.config;

import io.quarkus.vertx.http.runtime.filters.Filters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * There is no default realm: every {@code /api/} request must name its realm in the
 * {@code UDAN-Realm} header (see {@link CustomTenantResolver}). A request without it, or with an
 * empty one, is answered with 400 before authentication runs.
 * <p>
 * Runs after the CORS handler (priority 300) so the 400 carries CORS headers, and before the
 * authentication handler (priority 200). Preflight ({@code OPTIONS}) requests carry no custom
 * header and are never rejected; paths outside {@code /api/} (health, metrics) are untouched.
 */
@ApplicationScoped
public class RequireRealmFilter {

    static final String REALM_HEADER = "UDAN-Realm";
    static final int PRIORITY = 250;

    @ConfigProperty(name = "udan.realm.required", defaultValue = "true")
    boolean required;

    void register(@Observes Filters filters) {
        if (!required) {
            return;
        }
        filters.register(rc -> {
            if (isRejected(rc.request().method().name(), rc.request().path(),
                    rc.request().getHeader(REALM_HEADER))) {
                rc.response().setStatusCode(400)
                        .putHeader("Content-Type", "application/json")
                        .end("{\"error\":\"" + REALM_HEADER + " header is required\"}");
                return;
            }
            rc.next();
        }, PRIORITY);
    }

    static boolean isRejected(String method, String path, String realm) {
        return path != null && path.startsWith("/api/")
                && !"OPTIONS".equals(method)
                && (realm == null || realm.isBlank());
    }
}
