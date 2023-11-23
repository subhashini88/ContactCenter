package com.opentext.apps.cc.upgradeutils.model;

public final class FieldModel {
	private String fieldName;
	private String fieldDisplayName;
	private String type;
	private String operator;
	private String value;

	public FieldModel(String fieldName, String fieldDisplayName, String type, String operator, String value) {
		super();
		this.fieldName = fieldName;
		this.fieldDisplayName = fieldDisplayName;
		this.type = type;
		this.operator = operator;
		this.value = value;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getFieldDisplayName() {
		return fieldDisplayName;
	}

	public String getType() {
		return type;
	}

	public String getOperator() {
		return operator;
	}

	public String getValue() {
		return value;
	}

}
