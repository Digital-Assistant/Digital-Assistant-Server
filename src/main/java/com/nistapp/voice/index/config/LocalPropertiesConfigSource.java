package com.nistapp.voice.index.config;

import io.smallrye.config.PropertiesConfigSource;

import java.io.IOException;
import java.net.URL;

public class LocalPropertiesConfigSource extends PropertiesConfigSource {

    public LocalPropertiesConfigSource(URL url) throws IOException {
        super(url);
    }
}
