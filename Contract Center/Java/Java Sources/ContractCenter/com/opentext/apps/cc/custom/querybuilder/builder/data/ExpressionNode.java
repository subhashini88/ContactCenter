package com.opentext.apps.cc.custom.querybuilder.builder.data;

import java.util.Objects;

public final class ExpressionNode implements IDataNode {
	public final ExpressionOperator operator;
	public final String operandName;
	public final String operandValue;
	public final String attrType;
	public final ExpressionOperandDataType operandDataType;
	public final String expOperator;
	public final String expDataType;

	public ExpressionNode(ExpressionOperator operator, String operandName, String operandValue) {
		super();
		this.operator = operator;
		this.operandName = operandName;
		this.operandValue = escapeInjections(operandValue);
		this.attrType = "";
		this.operandDataType = null;
		this.expOperator = null;
		this.expDataType = null;
	}

	public ExpressionNode(ExpressionOperator operator, String operandName, String operandValue, String attrType) {
		super();
		this.operator = operator;
		this.operandName = operandName;
		this.operandValue = escapeInjections(operandValue);
		this.attrType = attrType;
		this.operandDataType = null;
		this.expOperator = null;
		this.expDataType = null;
	}
	
	public ExpressionNode(ExpressionOperator operator, String operandName, String operandValue, String attrType, ExpressionOperandDataType operandDataType) {
		super();
		this.operator = operator;
		this.operandName = operandName;
		this.operandValue = escapeInjections(operandValue);
		this.attrType = attrType;
		this.operandDataType = operandDataType;
		this.expOperator = null;
		this.expDataType = null;
	}
	
	public ExpressionNode(String operator, String operandName, String operandValue, String attrType, String operandDataType) {
		super();
		this.operator = null;
		this.operandName = operandName;
		this.operandValue = escapeInjections(operandValue);
		this.attrType = attrType;
		this.operandDataType = null;
		this.expOperator = operator;
		this.expDataType = operandDataType;
	}
	
	

	private String escapeInjections(String operandValue) {
		return Objects.isNull(operandValue) ? "" : operandValue.replaceAll("[^A-Za-z0-9_.@$&#\\s-]", "");
	}
}