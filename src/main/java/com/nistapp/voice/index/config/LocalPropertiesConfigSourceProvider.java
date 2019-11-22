package com.nistapp.voice.index.config;

import io.smallrye.config.PropertiesConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LocalPropertiesConfigSourceProvider implements ConfigSourceProvider {

    private List<ConfigSource> configSources = new ArrayList<>();

    public LocalPropertiesConfigSourceProvider() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("local.properties");
        if (url != null) {
            configSources.add(new PropertiesConfigSource(url));
        }
    }

    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
        return configSources;
    }
}
