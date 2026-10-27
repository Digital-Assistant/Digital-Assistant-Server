package com.nistapp.uda.index.config;

import io.quarkus.oidc.TenantResolver;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Context;
import java.net.http.HttpHeaders;

@ApplicationScoped
public class CustomTenantResolver implements TenantResolver {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CustomTenantResolver.class);

    @Context
    HttpHeaders httpHeaders;

    /**
     * The tenant is the value of the {@code UDAN-Realm} header. There is no default realm:
     * {@link RequireRealmFilter} rejects {@code /api/} requests without it, so a missing header
     * only reaches here on paths that do not authenticate (health, metrics) and gets no tenant.
     */
    @Override
    public String resolve(RoutingContext context) {
        String realm = context.request().getHeader("UDAN-Realm");
        if (realm == null || realm.isBlank()) {
            return null;
        }
        return realm.trim();
    }
}
