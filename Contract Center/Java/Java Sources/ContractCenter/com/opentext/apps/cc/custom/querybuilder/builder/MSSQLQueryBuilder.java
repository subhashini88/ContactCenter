package com.opentext.apps.cc.custom.querybuilder.builder;

import com.opentext.apps.cc.custom.querybuilder.builder.data.ConnectorNode;
import com.opentext.apps.cc.custom.querybuilder.builder.data.ContainerNode;
import com.opentext.apps.cc.custom.querybuilder.builder.data.ExpressionNode;
import com.opentext.apps.cc.custom.querybuilder.builder.data.ExpressionOperandDataType;
import com.opentext.apps.cc.custom.querybuilder.builder.data.ExpressionOperator;
import com.opentext.apps.cc.custom.querybuilder.builder.data.IDataNode;
import com.opentext.apps.cc.custom.querybuilder.builder.data.SearchQueryData;

public class MSSQLQueryBuilder extends AbstractQueryBuilder {

	@Override
	public IQueryBuilder addOperand(String operandName) {
		if (COMPLIANCE_STATUS_LABEL.equalsIgnoreCase(operandName)) {
			buffer.append(operandName);
		} else
			buffer.append("JSON_VALUE(" + this.jsonColumnName + "_cjson ,'$.\"" + operandName + "\"') ");
		return this;
	}

	public void addOperand(String operandName, StringBuffer buffer) {
		if (COMPLIANCE_STATUS_LABEL.equalsIgnoreCase(operandName)) {
			buffer.append(operandName);
		} else
			buffer.append("JSON_VALUE(" + this.jsonColumnName + "_cjson ,'$.\"" + operandName + "\"') ");
	}

	public void addOperand(String operandName, StringBuffer buffer, String attrType) {
		if (COMPLIANCE_STATUS_LABEL.equalsIgnoreCase(operandName)) {
			buffer.append(operandName);
		} else
			buffer.append(
					"JSON_VALUE(" + getColumnName(attrType, operandName) + "_cjson ,'$.\"" + operandName + "\"') ");
	}

	public void addOperand(String operandName, StringBuffer buffer, String attrType,
			ExpressionOperandDataType operandDataType) {
		if (COMPLIANCE_STATUS_LABEL.equalsIgnoreCase(operandName)) {
			buffer.append(operandName);
		} else
			buffer.append(resolveColumnCasting(
					"JSON_VALUE(" + getColumnName(attrType, operandName) + "_cjson ,'$.\"" + operandName + "\"') ",
					operandDataType));
	}

	@Override
	public IQueryBuilder buildQuery(SearchQueryData queryData) {
		buffer.append("Select  qTable.id, '\"'+"
				+ addCtrComplainceOnCondition(queryData, nvlColumn("ccobl.compliance_status", "'None'"))
				+ "+'\"' as compliance_status, " + this.jsonColumnName + "_cjson ," + this.jsonFixedColumnName
				+ "_cjson ," + this.jsonCustomColumnName + "_cjson from ( select  id," + "( " + this.jsonColumnName
				+ ") as " + this.jsonColumnName + "_cjson ," + "( " + this.jsonFixedColumnName + ") as "
				+ this.jsonFixedColumnName + "_cjson ," + "( " + this.jsonCustomColumnName + ") as "
				+ this.jsonCustomColumnName + "_cjson " + ", s_lastmodifieddate as jsonLastModified from "
				+ this.tableName + " WITH (NOLOCK) where " + this.jsonColumnName + " is not null " + buildWorkList()
				+ " ) as qTable " + addCtrOblTableOnCondition(queryData) + " where  ");
		queryData.containers.forEach(data -> {
			resolveExpression(data, buffer);
		});
		buffer.append("  ").append(prepareLimitAndOffset());
		return this;
	}

	
	protected String addStrConcat() {
		return "+";
	}

	protected StringBuffer resolveExpression(IDataNode data, StringBuffer parentBuilder) {
		StringBuffer res = new StringBuffer();
		if (data instanceof ContainerNode) {
			res.append("( ");
			((ContainerNode) data).childNodes.forEach(node -> {
				resolveExpression(node, res);
			});
			res.append(" )");
		} else if (data instanceof ConnectorNode) {
			String connec = ((ConnectorNode) data).connector.toString();
			res.append(" " + connec + " ");
		} else if (data instanceof ExpressionNode) {
			ExpressionNode expressionNode = ((ExpressionNode) data);
			res.append(" ");
			addOperand(resolveOperanName(expressionNode.operandName), res, expressionNode.attrType,
					expressionNode.operandDataType);
			res.append(" ");
			res.append(resolveOperator((expressionNode.operator)));
			res.append(" ");
			if (!(expressionNode.operator.equals(ExpressionOperator.EMPTY)
					|| expressionNode.operator.equals(ExpressionOperator.NOTEMPTY))) {
				String val = resolveOperatorValue(expressionNode, expressionNode.operandDataType);
				res.append(val);
			}
			res.append(" ");
		}
		parentBuilder.append(res.toString());
		return parentBuilder;
	}

	private String resolveColumnCasting(String columnName, ExpressionOperandDataType operandDataType) {

		if (operandDataType.equals(ExpressionOperandDataType.DATE)) {
			return "CAST(" + columnName + " as DATE)";
		} else if (operandDataType.equals(ExpressionOperandDataType.DECIMAL)) {
			return "CAST(" + columnName + " AS FLOAT)";
		} else if (operandDataType.equals(ExpressionOperandDataType.INTEGER)) {
			return "CAST(" + columnName + " AS INTEGER)";
		} else if (operandDataType.equals(ExpressionOperandDataType.DURATION)) {
			// deprecated cc23.3
			// return "CAST( SUBSTRING("+columnName+", CHARINDEX('P', "+columnName+") + 1,
			// CHARINDEX('M', "+columnName+") - CHARINDEX('P', "+columnName+") - 1) AS
			// INTEGER)";
			return "CAST(" + columnName + " AS INTEGER)";
		}

		return columnName;
	}

	@Override
	protected String resolveOperatorValue(ExpressionNode expressionNode, ExpressionOperandDataType operandDataType) {
		if (expressionNode.operator.equals(ExpressionOperator.CONTAINS)) {
			return "'%" + expressionNode.operandValue + "%'";
		}
		if (operandDataType.equals(ExpressionOperandDataType.DATE)) {
			return "CAST( '" + expressionNode.operandValue + "' AS DATE)";
		} else if (operandDataType.equals(ExpressionOperandDataType.DECIMAL)) {
			return "CAST( '" + expressionNode.operandValue + "' AS FLOAT)";
		} else if (operandDataType.equals(ExpressionOperandDataType.INTEGER)) {
			return "CAST( '" + expressionNode.operandValue + "' AS INTEGER)";
		} else if (operandDataType.equals(ExpressionOperandDataType.DURATION)) {
			return "CAST( '" + expressionNode.operandValue + "' AS INTEGER)";
		}
		return "'" + expressionNode.operandValue + "'";
	}

	@Override
	public String prepareLimitAndOffset() {
		if (this.limit > 0) {
			return "order by jsonLastModified DESC OFFSET " + this.offset + " ROWS FETCH NEXT " + this.limit
					+ " ROWS ONLY";
		}
		return "";
	}

	protected String addIndexOfFunc(String colVal, String colName) {
		return " CHARINDEX(" + colVal + "," + colName + ")";
	}

	@Override
	protected String addStringAggrgator(String colValExpr) {
		return "STRING_AGG( " + colValExpr + ",'')";
	}

	@Override
	protected String nvlColumn(String colName, String nullDefaultVal) {
		return "COALESCE(" + colName + "," + nullDefaultVal + ")";
	}

}
