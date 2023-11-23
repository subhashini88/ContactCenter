package com.opentext.apps.cc.analytics.aggregator.sql.builder;

import java.util.HashSet;
import java.util.Set;

import com.opentext.apps.cc.analytics.aggregator.sql.builder.data.SearchQueryData;
import com.opentext.apps.cc.process.ReportDashboardConfig;

public class ContractMSSQLQueryBuilder extends AbstractQueryBuilder {

	public ContractMSSQLQueryBuilder() {
		super(DBType.MSSQL);
	}

	@Override
	public IQueryBuilder buildQuery(SearchQueryData queryData) {
		if (null == queryData) {
			buffer.append("Select xcol," + addYColumn() + " agg from (  "
					+ aggregatorStrategy.buildQuery(this, buildWorkList()) + " ) as qTable  ");
			buffer.append(prepareLimitAndOffset());
		}
		return this;
	}

	private String addYColumn() {
		return isYColumnSelected() ? "ycol, " : "";
	}

	@Override
	public String prepareLimitAndOffset() {
		if (this.limit > 0) {
			return "order by " + this.xColumnName + "_cjson " + " OFFSET " + this.offset + " ROWS FETCH NEXT "
					+ this.limit + " ROWS ONLY";
		}
		return "";
	}

	@Override
	public String buildCustomAttrTable(String orgId) {
		if (isXCustCol() || isYCustCol() || isAggCustCol()) {

			return " left join " + "(" + "Select id  " + addCustColSelect() + "  from  " + orgId
					+ "opentextcontractcenterctraddlprops" + "   where  custpropjson is not null" + " )  "
					+ " cccustattr on cc.id=cccustattr.id";
		}
		return "";
	}

//	public String addCustColSelect() {
//		Set<String> aggCols = new HashSet<>();
//		aggCols.add(addXCol());
//		aggCols.add(addYCol());
//		aggCols.add(addAggCol());
//		return String.join("", aggCols);
//	}

	protected String addXCol() {
		return (isXCustCol()
				? ", JSON_VALUE(custpropjson, '$.\"" + getxColumnName().split(ReportDashboardConfig.CUST_KEY)[1]
						+ "\"') as " + getxColumnName()
				: "");
	}

	protected String addYCol() {
		return (isYCustCol()
				? (", JSON_VALUE(custpropjson, '$.\"" + getyColumnName().split(ReportDashboardConfig.CUST_KEY)[1]
						+ "\"') as " + getyColumnName())
				: "");
	}

	protected String addAggCol() {
		return (isAggCustCol()
				? (", JSON_VALUE(custpropjson, '$.\"" + getAggColumnName().split(ReportDashboardConfig.CUST_KEY)[1]
						+ "\"') as " + getAggColumnName())
				: "");
	}

}
