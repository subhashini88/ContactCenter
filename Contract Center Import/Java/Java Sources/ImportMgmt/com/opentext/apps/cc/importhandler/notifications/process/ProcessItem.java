package com.opentext.apps.cc.importhandler.notifications.process;

public class ProcessItem {
	private String id;
	private String value;
	private ProcessItemType type;

	public String getValue() {
		return value;
	}

	public ProcessItemType getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	public ProcessItem(String id, String value, ProcessItemType type) {
		super();
		this.id = id;
		this.value = value;
		this.type = type;
	}

}
