package com.nistapp.uda.index.config;

import io.smallrye.config.PropertiesConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LocalPropertiesConfigSourceProvider implements ConfigSourceProvider {

    private static final Logger logger = LoggerFactory.getLogger(LocalPropertiesConfigSourceProvider.class);

    private final List<ConfigSource> configSources = new ArrayList<>();


    public LocalPropertiesConfigSourceProvider() throws IOException {
        URL additionalProperties = Thread.currentThread().getContextClassLoader().getResource("additional.properties");
        if (additionalProperties != null) {
            ConfigSource additional = new PropertiesConfigSource(additionalProperties);
            logger.info("Adding properties file " + additionalProperties + " with ordinal " + additional.getOrdinal());
            configSources.add(additional);
        }
        URL localProperties = Thread.currentThread().getContextClassLoader().getResource("local.properties");
        if (localProperties != null) {
            ConfigSource local = new PropertiesConfigSource(localProperties);
            logger.info("Adding properties file " + localProperties + " with ordinal " + local.getOrdinal());
            configSources.add(local);
        }
        // Untracked local credentials written by sync-credentials.sh; absent in CI and in images.
        URL localSecrets = Thread.currentThread().getContextClassLoader().getResource("local-secrets.properties");
        if (localSecrets != null) {
            ConfigSource secrets = new PropertiesConfigSource(localSecrets);
            logger.info("Adding properties file " + localSecrets + " with ordinal " + secrets.getOrdinal());
            configSources.add(secrets);
        }
    }

    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
        return configSources;
    }
}
