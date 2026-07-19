package com.nistapp.uda.index.config;

import io.smallrye.config.PropertiesConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LocalPropertiesConfigSourceProvider implements ConfigSourceProvider {

    private static final Logger logger = LoggerFactory.getLogger(LocalPropertiesConfigSourceProvider.class);

    private final List<ConfigSource> configSources = new ArrayList<>();


    public LocalPropertiesConfigSourceProvider() throws IOException {
        File extAdditional = new File("config/additional.properties");
        if (extAdditional.exists()) {
            ConfigSource additional = new PropertiesConfigSource(extAdditional.toURI().toURL());
            ConfigSource overridden = new OrdinalOverrideConfigSource(additional, 270);
            logger.info("Adding external properties file " + extAdditional.getAbsolutePath() + " with ordinal " + overridden.getOrdinal());
            configSources.add(overridden);
        } else {
            URL additionalProperties = Thread.currentThread().getContextClassLoader().getResource("additional.properties");
            if (additionalProperties != null) {
                ConfigSource additional = new PropertiesConfigSource(additionalProperties);
                ConfigSource overridden = new OrdinalOverrideConfigSource(additional, 270);
                logger.info("Adding classpath properties file " + additionalProperties + " with ordinal " + overridden.getOrdinal());
                configSources.add(overridden);
            }
        }

        File extLocal = new File("config/local.properties");
        if (extLocal.exists()) {
            ConfigSource local = new PropertiesConfigSource(extLocal.toURI().toURL());
            ConfigSource overridden = new OrdinalOverrideConfigSource(local, 270);
            logger.info("Adding external properties file " + extLocal.getAbsolutePath() + " with ordinal " + overridden.getOrdinal());
            configSources.add(overridden);
        } else {
            URL localProperties = Thread.currentThread().getContextClassLoader().getResource("local.properties");
            if (localProperties != null) {
                ConfigSource local = new PropertiesConfigSource(localProperties);
                ConfigSource overridden = new OrdinalOverrideConfigSource(local, 270);
                logger.info("Adding classpath properties file " + localProperties + " with ordinal " + overridden.getOrdinal());
                configSources.add(overridden);
            }
        }
    }

    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
        return configSources;
    }

    private static class OrdinalOverrideConfigSource implements ConfigSource {
        private final ConfigSource delegate;
        private final int ordinal;

        public OrdinalOverrideConfigSource(ConfigSource delegate, int ordinal) {
            this.delegate = delegate;
            this.ordinal = ordinal;
        }

        @Override
        public java.util.Map<String, String> getProperties() {
            return delegate.getProperties();
        }

        @Override
        public java.util.Set<String> getPropertyNames() {
            return delegate.getPropertyNames();
        }

        @Override
        public String getValue(String propertyName) {
            return delegate.getValue(propertyName);
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public int getOrdinal() {
            return ordinal;
        }
    }
}
