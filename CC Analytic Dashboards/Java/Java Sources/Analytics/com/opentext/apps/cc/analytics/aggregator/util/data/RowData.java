package com.opentext.apps.cc.analytics.aggregator.util.data;

import java.util.HashMap;
import java.util.Map;

public class RowData {

	private final Map<String, Object> row = new HashMap<>();

	public void addTuple(String key, Object val) {
		row.put(key, val);
	}

	public Map<String, Object> getRowData() {
		return row;
	}

	public Object getValue(String key) {
		return row.get(key);
	}
}
