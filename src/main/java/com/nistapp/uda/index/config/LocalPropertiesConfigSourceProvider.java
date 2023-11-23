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
        URL url = Thread.currentThread().getContextClassLoader().getResource("local.properties");
        if (url != null) {
            ConfigSource local = new PropertiesConfigSource(url);
            logger.info("Adding properties file " + url + " with ordinal " + local.getOrdinal());
            configSources.add(local);

        }
    }

    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
        return configSources;
    }
}
