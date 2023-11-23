package com.opentext.apps.cc.analytics.aggregator.sql.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.analytics.aggregator.sql.agg.AggregatorStrategy;
import com.opentext.apps.cc.analytics.aggregator.sql.builder.data.ExpressionNode;
import com.opentext.apps.cc.analytics.aggregator.sql.builder.data.ExpressionOperator;
import com.opentext.apps.cc.analytics.aggregator.sql.builder.data.SearchQueryData;
import com.opentext.apps.cc.analytics.nom.NomUtil;

public abstract class AbstractQueryBuilder implements IQueryBuilder {

	private static final Map<String, String> dataSetMap = new HashMap<>();
	static {
		populateDataSetMap();
	}

	protected final DBType dbType;
	protected StringBuffer buffer = new StringBuffer();
	protected boolean isXCustCol = false;
	protected boolean isYCustCol = false;
	protected boolean isAggCustCol = false;
	protected String yColumnName = "";
	protected String dataSet = "";
	protected String xColumnName = "";
	protected String aggColumnName = "";
	protected String aggregator = "";
	protected String tableName = "";
	protected String joinquery = null;

	private boolean started = false;
	private boolean completed = false;
	protected boolean isUserAdmin = false;

	protected List<String> targetWorkList = new ArrayList<>();
	protected int offset;
	protected int limit;
	private String targetWorkListStr;
	private boolean isAdmin;

	public boolean isAggCustCol() {
		return isAggCustCol;
	}

	private static void populateDataSetMap() {
		dataSetMap.put("ActiveContract", "Active");
		dataSetMap.put("DraftContract", "Draft");
		dataSetMap.put("TerminatedContract", "Terminated");
		dataSetMap.put("ExecutedContract", "Execution");
		dataSetMap.put("ExpiredContract", "Expired");
	}

//	public AbstractQueryBuilder() {
//		
//	}

//	public AbstractQueryBuilder(AggregatorStrategy strategy) {
//		this();
//		this.aggregatorStrategy = strategy;
//	}

	public AbstractQueryBuilder(DBType dbType) {
		super();
		this.dbType = dbType;
		getUserTargetWorkList();
	}

	@Override
	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	@Override
	public void setTargetWorkListStr(String targetWorkList) {
		this.targetWorkListStr = targetWorkList;
	}

	public void setAggCustCol(boolean isAggCustCol) {
		this.isAggCustCol = isAggCustCol;
	}

	public boolean isXCustCol() {
		return isXCustCol;
	}

	public void setXCustCol(boolean isXCustCol) {
		this.isXCustCol = isXCustCol;
	}

	public void setDataSet(String dataSet) {
		this.dataSet = dataSet;
	}

	public String getDataSet() {
		return this.dataSet;
	}

	public boolean isYCustCol() {
		return isYCustCol;
	}

	public void setYCustCol(boolean isYCustCol) {
		this.isYCustCol = isYCustCol;
	}

	public boolean isYColumnSelected() {
		return !(Objects.isNull(yColumnName) || "--select--".equalsIgnoreCase(yColumnName));
	}

	protected AggregatorStrategy aggregatorStrategy = null;

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
	public IQueryBuilder addJoinQuery() {
		this.joinquery = prepareDataSetJoinQuery(this.tableName);
		return this;
	}

	@Override
	public IQueryBuilder addJoinQuery(String joinQuery) {
		this.joinquery = joinQuery;
		return this;
	}

	public String getJoinQuery() {
		return this.joinquery;
	}

	@Override
	public IQueryBuilder start() {
		started = true;
		return this;
	}

	public void addOperator(String operandName, StringBuffer buffer) {
		buffer.append(operandName);
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
		buffer.append("select * from (Select  from " + this.tableName + " where  " + this.xColumnName + " is not null "
				+ buildWorkList() + ") as tab where ");
		queryData.containers.forEach(data -> {
		});
		return this;
	}

	protected String resolveOperatorValue(ExpressionNode expressionNode) {
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
		/*
		 * StringBuffer buffer = new StringBuffer(); if (!isUserAdmin) {
		 * buffer.append(" and  z_int_worklistid in ("); if (this.targetWorkList.size()
		 * > 0) { for (int i = 0; i < this.targetWorkList.size(); i++) {
		 * buffer.append("'" + this.targetWorkList.get(i) + "' "); if (i <
		 * (this.targetWorkList.size() - 1)) { buffer.append(" , "); } } } else {
		 * buffer.append("''"); } buffer.append(" )"); } return buffer.toString();
		 */
		return "";
	}

	protected String resolveOperanName(String operandName) {
		if (operandName.equals("AutoRenewDuration")) {
			operandName = "AutoRenewDurationCal";
		} else if (operandName.equals("InitialContractTenure")) {
			operandName = "InitialContractTenureCal";
		}

		return operandName;
	}

	@Override
	public void setAggStratergy(AggregatorStrategy aggregatorStrategy) {
		this.aggregatorStrategy = aggregatorStrategy;
	}

	public String getxColumnName() {
		return xColumnName;
	}

	public String getyColumnName() {
		return yColumnName;
	}

	public String getAggColumnName() {
		return aggColumnName;
	}

	public String getAggregator() {
		return aggregator;
	}

	public String getTableName() {
		return tableName;
	}

	public AggregatorStrategy getAggregatorStrategy() {
		return aggregatorStrategy;
	}

	public void setAggregatorStrategy(AggregatorStrategy aggregatorStrategy) {
		this.aggregatorStrategy = aggregatorStrategy;
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public void setLimit(int limit) {
		this.limit = limit;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public boolean isStarted() {
		return started;
	}

	public boolean isUserAdmin() {
		return isUserAdmin;
	}

	public List<String> getTargetWorkList() {
		return targetWorkList;
	}

	public int getOffset() {
		return offset;
	}

	public int getLimit() {
		return limit;
	}

	public void setxColumnName(String xColumnName) {
		this.xColumnName = xColumnName;
	}

	public void setyColumnName(String yColumnName) {
		this.yColumnName = yColumnName;
	}

	public void setAggColumnName(String aggColumnName) {
		this.aggColumnName = aggColumnName;
	}

	public void setAggregator(String aggregator) {
		this.aggregator = aggregator;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public abstract String prepareLimitAndOffset();

	protected String prepareDataSetJoinQuery(String tableName) {
		String orgId = tableName.split("opentextcontractcenter")[0];
		return " select " + addCustCol() + "  ctype.name ContractType, " + " cc.contractvalueusd contractvalueusd,"
				+ "cc.contractvalue contractvalue," + "cregion.name  Region,"
				+ "cccountry.country_name Country , ccurrency.name Currency, "
				+ " cc.s_lbb_current_state  CurrentState , cc.InitialContractTenure InitialContractTenure, ccamend.name AmendType ,"
				+ " cctermin.reason TerminationReason,ccorg.name RelatedOrganization, crenewalflag.name RenewalFlagStatus, cc.Priority Priority"
				+ ",cc.IntentType IntentType, cc.AutoRenewDuration AutoRenewDuration, cc.RenewalDiscount RenewalDiscount,"
				+ " cc.AutoRenew AutoRenew, cc.IsExecuted IsExecuted, cc.Perpetual Perpetual,"
				+ " cc.StartDate StartDate, cc.MinStartdate MinStartdate, cc.CurrentStartDate CurrentStartDate, cc.CurrentEndDate CurrentEndDate "
				+ " from " + tableName + " as cc " + " left join " + orgId + "opentextbasiccomponentsgctype "
				+ "as ctype on cc.contracttype_id=ctype.id left join  " + orgId
				+ "opentextcontractcenterregion region on cc.region_id=region.id  left join " + orgId
				+ "opentextcontractcenterregion cregion on  cc.country_id =cregion.id left join " + orgId
				+ "opentextcontractcenterrelatedcountries ccrtdcountries "
				+ "on cc.country_id =ccrtdcountries.id and cc.country_id1 =ccrtdcountries.id1 left join " + orgId
				+ "opentextentityidentitycomponentscountry cccountry on ccrtdcountries.linkedcountry_id=cccountry.id "
				+ " left join " + orgId + "opentextcontractcentercurrency ccurrency on cc.currency_id =ccurrency.id "
				+ " left join " + orgId + "opentextcontractcenteramendmenttype ccamend on cc.amendtype_id=ccamend.id "
				+ "left join " + orgId
				+ "opentextcontractcenterterminationreason cctermin on cc.terminationreason_id=cctermin.id "
				+ "left join " + orgId
				+ "opentextbasiccomponentsgcorganization ccorg on cc.relatedorganization_id=ccorg.id " + "left join "
				+ orgId
				+ "opentextcontractcenterrenewalflagstatus crenewalflag on  cc.renewalflagstatus_id =crenewalflag.id "
				+ buildCustomAttrTable(orgId) + addWhereClause();
	}

	protected String addWhereClause() {
		return " where 1=1 " + addTargetWorklistClasue() + addDataSet();
	}

	private String addTargetWorklistClasue() {
		if (!isAdmin && !Objects.isNull(this.targetWorkListStr)) {
			return "and ccorg.z_int_worklistid in (" + this.targetWorkListStr + ")";
		}
		return "";
	}

	protected String addDataSet() {
		if (null != this.getDataSet() && isSubSetOfContract(this.getDataSet())) {
			return " and  cc.s_lbb_current_state='" + dataSetMap.get(this.getDataSet()) + "'";
		}
		return "";
	}

	private boolean isSubSetOfContract(String dataSet) {
		boolean found = false;
		for (String dataSetKey : dataSetMap.keySet()) {
			if (dataSetKey.equalsIgnoreCase(dataSet)) {
				found = true;
				break;
			}
		}
		return found;
	}

	public String addCustCol() {
		Set<String> aggCols = new HashSet<>();
		aggCols.add(addCustXCol());
		aggCols.add(addCustYCol());
		aggCols.add(addCustAggCol());
		return String.join("", aggCols);
	}

	private String addCustYCol() {
		return isYCustCol() ? ("cccustattr." + getyColumnName() + ",") : "";
	}

	private String addCustXCol() {
		return isXCustCol() ? ("cccustattr." + getxColumnName() + ",") : "";
	}

	private String addCustAggCol() {
		return isAggCustCol() ? ("cccustattr." + getAggColumnName() + ",") : "";
	}

	public abstract String buildCustomAttrTable(String orgId);

	protected String buildObligationTable(String orgId) {
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
				+ "opentextcontractcenterobligations  group by  id) ccobl_int   ) ccobl_int1 ) ccoblst on cc.id=ccoblst.id";
	}

	protected String addStringAggrgator(String colValExpr) {
		return "STRING_AGG( " + colValExpr + ",'')";
	}

	private String addIndexOfFunc(String colVal, String colName) {
		if (dbType.equals(DBType.POSTGRES))
			return "position( " + colVal + " in " + colName + ")";
		else
			return " CHARINDEX(" + colVal + "," + colName + ")";
	}

	protected String nvlColumn(String colName, String nullDefaultVal) {
		return "COALESCE(" + colName + "," + nullDefaultVal + ")";
	}

	public String addCustColSelect() {
		Set<String> aggCols = new HashSet<>();
		aggCols.add(addXCol());
		aggCols.add(addYCol());
		aggCols.add(addAggCol());
		return String.join("", aggCols);
	}

	protected String addStrConcat() {
		return this.dbType.equals(DBType.MSSQL) ? "+" : "||";
	}

	protected abstract String addAggCol();

	protected abstract String addXCol();

	protected abstract String addYCol();

}
