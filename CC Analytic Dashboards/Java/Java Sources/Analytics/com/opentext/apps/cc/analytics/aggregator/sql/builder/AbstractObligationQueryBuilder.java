package com.opentext.apps.cc.analytics.aggregator.sql.builder;

public abstract class AbstractObligationQueryBuilder extends AbstractQueryBuilder {

	public AbstractObligationQueryBuilder(DBType dbType) {
		super(dbType);
	}

	@Override
	protected String prepareDataSetJoinQuery(String tableName) {
		String orgId = tableName.split("opentextcontractcenter")[0];
		return " select " + addCustCol() + "  ctype.name ContractType, " + " cc.contractvalueusd contractvalueusd,"
				+ "cc.contractvalue contractvalue," + "cregion.name  Region,"
				+ "cccountry.country_name Country , ccurrency.name Currency, "
				+ " cc.s_lbb_current_state  CurrentState , cc.InitialContractTenure InitialContractTenure, ccamend.name AmendType ,"
				+ " cctermin.reason TerminationReason,ccorg.name RelatedOrganization, crenewalflag.name RenewalFlagStatus, cc.Priority Priority"
				+ ",cc.IntentType IntentType, cc.AutoRenewDuration AutoRenewDuration, cc.RenewalDiscount RenewalDiscount,"
				+ " cc.AutoRenew AutoRenew, cc.IsExecuted IsExecuted, cc.Perpetual Perpetual, "
				+ " cc.StartDate StartDate, cc.MinStartdate MinStartdate, cc.CurrentStartDate CurrentStartDate, cc.CurrentEndDate CurrentEndDate ,'\"'"
				+ addStrConcat() + nvlColumn("ccoblst.compliance_status", "'None'") + addStrConcat()
				+ "'\"' as compliance_status" + " from " + tableName + " as cc " + " left join " + orgId
				+ "opentextbasiccomponentsgctype " + "as ctype on cc.contracttype_id=ctype.id left join  " + orgId
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
				+ buildObligationTable(orgId) + buildCustomAttrTable(orgId) + addWhereClause();
	}

}
