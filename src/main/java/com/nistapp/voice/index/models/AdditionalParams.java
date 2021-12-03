package com.nistapp.voice.index.models;

import java.util.Map;

public class AdditionalParams implements FieldBridge {
	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {

		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>)value;
		for (String key : map.keySet()) {
			String val = map.get(key).toString();
			if (val != null) {
				luceneOptions.addFieldToDocument(key, val, document);
			}
		}
	}
}
