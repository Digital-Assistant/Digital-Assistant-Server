package com.nistapp.voice.index.models;


import org.hibernate.search.engine.backend.document.model.dsl.IndexSchemaElement;
import org.hibernate.search.engine.backend.document.model.dsl.IndexSchemaObjectField;
import org.hibernate.search.mapper.pojo.bridge.binding.PropertyBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.PropertyBinder;

import java.util.Map;

public class AdditionalParamsBinder implements PropertyBinder {

    @Override
    public void bind(PropertyBindingContext context) {
        context.dependencies().useRootOnly();
        IndexSchemaElement schemaElement = context.indexSchemaElement();
        IndexSchemaObjectField objectField = schemaElement.objectField("additionalParams");
        final AdditionalParamsBridge bridge = new AdditionalParamsBridge(objectField.toReference());
        context.bridge(Map.class,  bridge);
    }
}
