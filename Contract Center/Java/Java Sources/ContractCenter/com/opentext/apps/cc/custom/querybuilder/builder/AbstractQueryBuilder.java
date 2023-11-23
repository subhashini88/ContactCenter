package com.opentext.apps.cc.custom.querybuilder.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.ContractConstants;
import com.opentext.apps.cc.custom.NomUtil;
import com.opentext.apps.cc.custom.querybuilder.builder.data.ConnectorNode;
import com.opentext.apps.cc.custom.querybuilder.builder.data.ContainerNode;
import com.opentext.apps.cc.custom.querybuilder.builder.data.ExpressionNode;
import com.opentext.apps.cc.custom.querybuilder.builder.data.ExpressionOperandDataType;
import com.opentext.apps.cc.custom.querybuilder.builder.data.ExpressionOperator;
import com.opentext.apps.cc.custom.querybuilder.builder.data.IDataNode;
import com.opentext.apps.cc.custom.querybuilder.builder.data.SearchQueryData;

public abstract class AbstractQueryBuilder implements IQueryBuilder {

	protected StringBuffer buffer = new StringBuffer();
	protected String jsonColumnName = "";
	protected String jsonCustomColumnName = "";
	protected String jsonFixedColumnName = "";
	protected String tableName = "";

	protected final String COMPLIANCE_STATUS_LABEL = "compliance_status";

	private boolean started = false;
	private boolean completed = false;
	protected boolean isUserAdmin = false;

	protected List<String> targetWorkList = new ArrayList<>();
	protected int offset;
	protected int limit;
	protected String orgId;

	public AbstractQueryBuilder() {
		getUserTargetWorkList();
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset;

	}

	@Override
	public void setLimit(int limit) {
		this.limit = limit;
	}

	public void setJsonColumnName(String columnName) {
		this.jsonColumnName = columnName;
	}

	public void setJsonFixedColumnName(String columnName) {
		this.jsonFixedColumnName = columnName;
	}

	public String getJsonfixedColumnName() {
		return this.jsonFixedColumnName;
	}

	public void setJsonCustomColumnName(String columnName) {
		this.jsonCustomColumnName = columnName;
	}

	public String getJsonCustomColumnName() {
		return jsonCustomColumnName;
	}

	@Override
	public IQueryBuilder addDBType(String dbType) {

		throw new NotImplementedException("Db type not implemented");
	}

	@Override
	public IQueryBuilder addTableName(String tableName) {
		this.tableName = tableName;
		return this;
	}

	@Override
	public IQueryBuilder start() {
		started = true;
		return this;
	}

	@Override
	public IQueryBuilder and() {
		if (!started) {
			// Throw exception
		}
		return this;
	}

	@Override
	public IQueryBuilder or() {
		buffer.append("OR");
		return this;
	}

	@Override
	public IQueryBuilder contains() {
		buffer.append("contains");
		return this;
	}

	@Override
	public IQueryBuilder addOperand(String operandName) {
		buffer.append(operandName);
		return this;
	}

	public void addOperand(String operandName, StringBuffer buffer) {
		buffer.append(operandName);
	}

	@Override
	public void addOperand(String operandName, StringBuffer buffer, String attrType) {
		buffer.append(operandName);
	}

	@Override
	public IQueryBuilder addOperator(String operator) {
		buffer.append(operator);
		return this;
	}

	public void addOperator(String operandName, StringBuffer buffer) {
		buffer.append(operandName);
	}

	@Override
	public IQueryBuilder addOperatorValue(String value) {
		buffer.append(value);
		return this;
	}

	public void addOperatorValue(String operandName, StringBuffer buffer) {
		buffer.append(operandName);
	}

	@Override
	public IQueryBuilder end() {
		completed = true;
		return this;
	}

	@Override
	public IQueryBuilder clear() {
		buffer = new StringBuffer();
		return this;
	}

	@Override
	public String getSqlQuery() {
		if (!completed) {
		}
		return buffer.toString();
	}

	@Override
	public IQueryBuilder buildQuery(SearchQueryData queryData) {
		buffer.append("select * from (Select  from " + this.tableName + " where  " + this.jsonColumnName
				+ " is not null " + buildWorkList() + ") as tab where ");
		queryData.containers.forEach(data -> {
			resolveExpression(data, buffer);
		});
		return this;
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
			addOperand(expressionNode.operandName, res, expressionNode.attrType);
			res.append(" ");
			res.append(resolveOperator((expressionNode.operator)));
			res.append(" ");
			if (!(expressionNode.operator.equals(ExpressionOperator.EMPTY)
					|| expressionNode.operator.equals(ExpressionOperator.NOTEMPTY))) {
				String val = resolveOperatorValue(expressionNode);
				res.append(val);
			}
			res.append(" ");
		}
		parentBuilder.append(res.toString());
		return parentBuilder;
	}

	protected String resolveOperatorValue(ExpressionNode expressionNode) {
		if (expressionNode.operator.equals(ExpressionOperator.CONTAINS)) {
			return "'%" + expressionNode.operandValue + "%'";
		}
		return "'" + expressionNode.operandValue + "'";
	}

	protected String resolveOperatorValue(ExpressionNode expressionNode, ExpressionOperandDataType operandDataType) {
		if (expressionNode.operator.equals(ExpressionOperator.CONTAINS)) {
			return "'%" + expressionNode.operandValue + "%'";
		}
		return "'" + expressionNode.operandValue + "'";
	}

	protected String resolveOperator(ExpressionOperator expressionOperator) {
		if (expressionOperator.equals(ExpressionOperator.EQUALS)) {
			return "=";
		} else if (expressionOperator.equals(ExpressionOperator.NOTEQUALS)) {
			return "!=";
		} else if (expressionOperator.equals(ExpressionOperator.NOTEMPTY)) {
			return "is not null";
		} else if (expressionOperator.equals(ExpressionOperator.EMPTY)) {
			return "is null";
		} else if (expressionOperator.equals(ExpressionOperator.GREATERTHANEQUALTO)) {
			return ">=";
		} else if (expressionOperator.equals(ExpressionOperator.LESSTHANEQUALTO)) {
			return "<=";
		}
		return "like";
	}

	protected List<String> getUserTargetWorkList() {
		SOAPRequestObject getAllTargetsRequest = new SOAPRequestObject(
				"http://schemas.cordys.com/notification/workflow/1.0", "GetAllTargets", null, null);
		int taskCountRequired = NomUtil.parseXML("<TaskCountRequired>" + "false" + "</TaskCountRequired>");
		int returnParticipantDetails = NomUtil
				.parseXML("<ReturnParticipantDetails>" + "true" + "</ReturnParticipantDetails>");
		getAllTargetsRequest.addParameterAsXml(returnParticipantDetails);
		getAllTargetsRequest.addParameterAsXml(taskCountRequired);
		int response = getAllTargetsRequest.sendAndWait();
		int[] nodes = NomUtil.getNodeList(".//Target", response);
		isUserAdmin = false;
		for (int i = 0; i < nodes.length; i++) {
			String type = Node.getDataWithDefault(NomUtil.getNode(".//Type", nodes[i]), null);
			String id = Node.getDataWithDefault(NomUtil.getNode(".//Id", nodes[i]), null);
			if (type.equalsIgnoreCase("worklist")) {
				targetWorkList.add(id);
			} else if (type.equalsIgnoreCase("role") && id.contains("Contract Administrator")) {
				isUserAdmin = true;
			}
		}

		return targetWorkList;
	}

	protected String buildWorkList() {
		StringBuffer buffer = new StringBuffer();
		if (!isUserAdmin) {
			buffer.append(" and z_int_worklistid in (");
			if (this.targetWorkList.size() > 0) {
				for (int i = 0; i < this.targetWorkList.size(); i++) {
					buffer.append("'" + this.targetWorkList.get(i) + "' ");
					if (i < (this.targetWorkList.size() - 1)) {
						buffer.append(" , ");
					}
				}
			} else {
				buffer.append("''");
			}
			buffer.append(" )");
		}
		return buffer.toString();
	}

	protected String getColumnName(String attrType, String operandName) {
		if (Objects.isNull(attrType) || !attrType.equalsIgnoreCase("CUSTOM")) {
			if (ArrayUtils.contains(ContractConstants.FIXEDCONTRACTPROPS, operandName)) {
				return this.jsonFixedColumnName;
			}
			return this.jsonColumnName;
		}

		return this.jsonCustomColumnName;
	}

	protected String resolveOperanName(String operandName) {
		if (operandName.equals("AutoRenewDuration")) {
			operandName = "AutoRenewDurationCal";
		} else if (operandName.equals("InitialContractTenure")) {
			operandName = "InitialContractTenureCal";
		}

		return operandName;
	}

//	protected String buildCtrObligationStateTable() {
//		return "left join (select   ccobl_int1.id,  ccobl_int1.compliance_status as compliance_status from   ( select  id, "
//				+ "case when " + addIndexOfFunc("'###Notmet'", "compliancestatus_agg") + " > 0 then 'Not met' "
//				+ "when " + addIndexOfFunc("'###Met'", "compliancestatus_agg") + " > 0 and ( "
//				+ addIndexOfFunc("'###InProgress'", "compliancestatus_agg") + " > 0  or "
//				+ addIndexOfFunc("'###Open'", "compliancestatus_agg") + " > 0 or  "
//				+ addIndexOfFunc("'###InProgress'", "compliancestatus_agg") + " > 0 then 'Open - In Progress' "
//				+ "else 'Met' end as compliance_status     " + "from ( select id,"
//				+ addStringAggrgator("'###' " + addStrConcat() + " compliancelevel " + addStrConcat() + " '###'")
//				+ " compliancestatus_agg " + " from  " + this.orgId
//				+ "opentextcontractcenterobligations  group by  id) ccobl_int   ) ccobl_int1 ) ccobl on qTable.id=ccobl.id";
//	}

	protected String buildCtrObligationStateTable() {
		return "left join (select   ccobl_int1.id,  ccobl_int1.compliance_status as compliance_status from   ( select  id, "
				+ "case when " + addIndexOfFunc("'###NotMet'", "compliancestatus_agg") + " > 0 or "
				+ addIndexOfFunc("'###Notmet'", "compliancestatus_agg") + " > 0 then 'Not met' " + "when "
				+ addIndexOfFunc("'###Met'", "compliancestatus_agg") + " > 0 and ( "
				+ addIndexOfFunc("'###InProgress'", "compliancestatus_agg") + " > 0  or "
				+ addIndexOfFunc("'###Open'", "compliancestatus_agg") + " > 0  ) then 'Partially Met' " + "when "
				+ addIndexOfFunc("'###Open'", "compliancestatus_agg") + " > 0 or  "
				+ addIndexOfFunc("'###InProgress'", "compliancestatus_agg") + " > 0 then 'Open - In Progress' "
				+ "else 'Met' end as compliance_status     " + "from ( select id,"
				+ addStringAggrgator("'###' " + addStrConcat() + " compliancelevel " + addStrConcat() + " '###'")
				+ " compliancestatus_agg " + " from  " + orgId
				+ "opentextcontractcenterobligations  group by  id) ccobl_int   ) ccobl_int1 ) ccobl on qTable.id=ccobl.id";
	}

	protected abstract String addStrConcat();

	protected abstract String addStringAggrgator(String colValExpr);

	protected abstract String addIndexOfFunc(String colVal, String colName);

	@Override
	public IQueryBuilder addOrgId(String orgId) {
		this.orgId = orgId;
		return this;
	}

	protected abstract String nvlColumn(String colName, String nullDefaultVal);

	public abstract String prepareLimitAndOffset();

	protected String addCtrOblTableOnCondition(SearchQueryData queryData) {
		return queryData.isHasComplainceStatus() ? buildCtrObligationStateTable() : "";
	}

	protected String addCtrComplainceOnCondition(SearchQueryData queryData, String complainceName) {
		return queryData.isHasComplainceStatus() ? complainceName : "''";
	}

}
