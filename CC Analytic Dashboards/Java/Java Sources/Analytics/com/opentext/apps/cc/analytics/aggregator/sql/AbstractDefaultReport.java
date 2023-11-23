package com.opentext.apps.cc.analytics.aggregator.sql;

import java.util.Calendar;
import java.util.Objects;

import com.opentext.apps.cc.analytics.aggregator.sql.builder.DBType;
import com.opentext.apps.cc.process.ReportDashboardConfig;

public abstract class AbstractDefaultReport implements ISqlReport {

	private final DBType dbType;

	private final String colName;
	private final ReportDashboardConfig configuration;

	protected static int month = Calendar.DAY_OF_MONTH;
	protected static int day = Calendar.MONTH;

	public AbstractDefaultReport(DBType dbType, String colName, ReportDashboardConfig configuration) {
		super();
		this.dbType = dbType;
		this.colName = colName;
		this.configuration = configuration;
	}

	public String getSql() {
		return prepareContractJoinQuery(getTableName()) + prepareWhereClasue();

	};

	private String prepareContractJoinQuery(String tableName) {
		String orgId = tableName.split("opentextcontractcenter")[0];
		return " select   ctype.name ContractType,cc.NextExpirationDate NextExpirationDate,"
				+ "cc.z_int_status z_int_status,cc.cancellationdate cancellationdate,"
				+ " cc.contractvalueusd contractvalueusd," + "cc.contractvalue contractvalue," + "cregion.name  Region,"
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
				+ addWhereClause();
	}

	private String addWhereClause() {
		return " where 1=1 " + addTargetWorklistClasue();
	}

	private String addTargetWorklistClasue() {
		String targetWorkListStr = this.configuration.getUser().getTargetWorklistStr();
		if (!this.configuration.getUser().getIsAdmin() && !Objects.isNull(targetWorkListStr)) {
			return "and ccorg.z_int_worklistid in (" + targetWorkListStr + ")";
		} else if (!this.configuration.getUser().getIsAdmin()
				&& (Objects.isNull(targetWorkListStr) || "".equalsIgnoreCase(targetWorkListStr))) {
			return "and ccorg.z_int_worklistid in ('')";
		}
		return "";
	}

	private String getTableName() {
		return Utilities.getTableName("OpenTextContractCenter", "Contract");
	}

	public abstract String prepareWhereClasue();

	public abstract String groupByCol();

	public String getFinalDate() {
		if ("ONEMONTH".equalsIgnoreCase(this.configuration.getDefaultChartDuration())) {
			return DbUtil.getFutureDateStr("yyyy-MM-dd", Calendar.MONTH, 1);
		} else if ("THREEMONTH".equalsIgnoreCase(this.configuration.getDefaultChartDuration())) {
			return DbUtil.getFutureDateStr("yyyy-MM-dd", Calendar.MONTH, 3);
		}
		return DbUtil.getFutureDateStr("yyyy-MM-dd", Calendar.DAY_OF_MONTH, 7);
	}

	public String addYCol() {
		String col = getGroupByCol();
		return "".equals(col) ? "" : col + " as ycol,";
	}

	public String addGroupByCol() {
		String col = getGroupByCol();
		return "".equals(col) ? "" : "," + col;
	}

	public String getGroupByCol() {
		return (null != this.configuration.getDefaultGroupBy()
				&& !this.colName.equalsIgnoreCase(this.configuration.getDefaultGroupBy()))
						? this.configuration.getDefaultGroupBy()
						: "";
	}
}