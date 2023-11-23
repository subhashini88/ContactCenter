package com.opentext.apps.cc.importcontent;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.opentext.apps.cc.custom.Utilities;

public class BatchIterator implements Iterator<List<Map<String, String>>>
{	
	private final List<Map<String, String>> sheetData;
	private final int batchSize;
	private int currentIndex = 0;
	private final Properties mappingProperties;
	
	public BatchIterator(List<Map<String, String>> sheetData, Properties mappingProperties, int batchSize) {
		this.sheetData = sheetData;
		this.batchSize = batchSize;
		this.mappingProperties = mappingProperties; 
	}
	
	@Override
	public boolean hasNext() {
		return (currentIndex < sheetData.size());
	}

	@Override
	public List<Map<String, String>> next() {
		List<Map<String, String>> batchData = new LinkedList<>();
		for (int size = Math.min(currentIndex + batchSize, sheetData.size()); currentIndex < size; currentIndex++) {
			Map<String, String> row = sheetData.get(currentIndex);
			trimSpaces(row);
			CustomMap customMap = new CustomMap(row);
			batchData.add(customMap);
		}
		return batchData;
	}
	
	private void trimSpaces(Map<String, String> row) {
		for (Map.Entry<String, String> entry : row.entrySet()) {
			
			// if value is empty or null, just use the same value
			// otherwise trim the value for removing the prefix & suffix spaces
			String value = Utilities.isStringEmpty(entry.getValue()) 
							? entry.getValue() : entry.getValue().trim();
							
			entry.setValue(value);
		}
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException("This method is not supported");
	}
	
	private class CustomMap extends AbstractMap<String, String> {
		private Map<String, String> userMap;
		
		public CustomMap(Map<String, String> map) {
			this.userMap = map;
		}
		@Override
		public Set<java.util.Map.Entry<String, String>> entrySet() {
			return userMap.entrySet();
		}
		
		@Override
		public String get(Object key){
			String userAttr = (String)mappingProperties.get(key);
			String sheetHeader = (String) ((null!=userAttr) ? userAttr:key);
			return userMap.get(sheetHeader.trim());
		}
		
		@Override
		public String put(String key, String value) {
			return userMap.put(key, value);
		}
	}
}