package com.nistapp.uda.index.config;

import io.quarkus.oidc.TenantResolver;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Context;
import java.net.http.HttpHeaders;

@ApplicationScoped
public class CustomTenantResolver implements TenantResolver {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CustomTenantResolver.class);

    @Context
    HttpHeaders httpHeaders;
    @Override
    public String resolve(RoutingContext context) {
        String path = context.request().getHeader("UDAN-Realm");

        if(path == null) {
            return "UDAN";
        } else {
            return path.toString();
        }
    }
}
