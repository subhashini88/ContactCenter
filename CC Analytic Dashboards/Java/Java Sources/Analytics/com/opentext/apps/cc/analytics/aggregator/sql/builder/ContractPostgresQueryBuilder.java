package com.opentext.apps.cc.analytics.aggregator.sql.builder;

import java.util.HashSet;
import java.util.Set;

import com.opentext.apps.cc.analytics.aggregator.sql.builder.data.SearchQueryData;
import com.opentext.apps.cc.process.ReportDashboardConfig;

public class ContractPostgresQueryBuilder extends AbstractQueryBuilder {

	public ContractPostgresQueryBuilder() {
		super(DBType.POSTGRES);
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
			return "LIMIT " + this.limit + " OFFSET " + this.offset;
		}
		return "";
	}

	@Override
	public String buildCustomAttrTable(String orgId) {
		if (isXCustCol() || isYCustCol() || isAggCustCol()) {
			return " left join " + "(" + "Select id  " + addCustColSelect() + "  from  (  select id,"
					+ "   to_jsonb(custpropjson :: jsonb) as custpropjson_cjson,"
					+ " s_lastmodifieddate as jsonLastModified   from   " + orgId + "opentextcontractcenterctraddlprops"
					+ "   where  ctrpropjson is not null" + "  ) as qTable )  " + " cccustattr on cc.id=cccustattr.id";

		}
		return "";
	}

	protected String addXCol() {
		return (isXCustCol()
				? ", custpropjson_cjson ->> '" + getxColumnName().split(ReportDashboardConfig.CUST_KEY)[1] + "' as "
						+ getxColumnName()
				: "");
	}

	protected String addYCol() {
		return (isYCustCol()
				? (", custpropjson_cjson ->> '" + getyColumnName().split(ReportDashboardConfig.CUST_KEY)[1] + "' as "
						+ getyColumnName())
				: "");
	}

	protected String addAggCol() {
		return (isAggCustCol()
				? (", custpropjson_cjson ->> '" + getAggColumnName().split(ReportDashboardConfig.CUST_KEY)[1] + "' as "
						+ getAggColumnName())
				: "");
	}

}
