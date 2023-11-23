package com.opentext.apps.cc.custom.model;

public class GCSectionContainerModel extends AbstractGCContainerModel {

	private int sectionId;
	private String name;
	private String itemID;

	public int getSectionId() {
		return sectionId;
	}

	public void setSectionId(int sectionId) {
		this.sectionId = sectionId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}
}
