package com.nistapp.voice.index.models;

import org.hibernate.search.engine.backend.document.DocumentElement;
import org.hibernate.search.engine.backend.document.IndexObjectFieldReference;
import org.hibernate.search.mapper.pojo.bridge.PropertyBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.PropertyBridgeWriteContext;

import java.util.Map;

public class AdditionalParamsBridge implements PropertyBridge<Map> {

    private final IndexObjectFieldReference reference;

    public AdditionalParamsBridge(IndexObjectFieldReference reference) {
        this.reference = reference;
    }

    @Override
    public void write(DocumentElement target, Map bridgedElement, PropertyBridgeWriteContext context) {
        Map<String, Object> additionalParams = (Map<String, Object>) bridgedElement;
        if(additionalParams != null) {
            DocumentElement additionalParamsData = target.addObject(reference);
            additionalParams.forEach((field, value) -> additionalParamsData.addValue(field, value.toString()));
        }
    }
}
