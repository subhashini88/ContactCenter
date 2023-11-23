package com.opentext.apps.cc.importcontent;

import java.util.Map;

public class ImportEvent {
	private final Map<String, String> row;
	private final ImportListener record;

	public ImportEvent(ImportListener record, Map<String, String> row) {//,BusObjectManager busObjectManager, Properties importProperties, ImportConfiguration importConfig) {
		this.record = record;
		this.row = row;
	}

	public ImportListener getSource() {
		return record;
	}

	public Object getSourceId() {
		if (getSource() != null) {		
			return this.getSource().getSourceId();
		}
		return null;
	}

	public Map<String, String> getRow() {
		return row;
	}
}
