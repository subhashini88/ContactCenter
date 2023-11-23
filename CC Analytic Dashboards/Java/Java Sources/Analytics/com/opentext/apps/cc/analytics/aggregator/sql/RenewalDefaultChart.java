package com.opentext.apps.cc.analytics.aggregator.sql;

import com.opentext.apps.cc.analytics.aggregator.sql.builder.DBType;
import com.opentext.apps.cc.process.ReportDashboardConfig;

public class RenewalDefaultChart extends AbstractDefaultReport {

	public RenewalDefaultChart(DBType dbType, ReportDashboardConfig configuration) {
		super(dbType, "NextExpirationDate", configuration);
	}

	@Override
	public String getSql() {
		String sql = "select NextExpirationDate xcol," + super.addYCol() + " count(1) agg from " + "( " + super.getSql()
				+ ") tab " + groupByCol();
		return sql;
	}

	public String prepareWhereClasue() {
		return " and cc.s_lbb_current_state='Active' and cc.perpetual='false' and cc.NextExpirationDate is not null "
				+ "and cc.NextExpirationDate >= " + DbUtil.getPresentDateStr("yyyy-MM-dd")
				+ "and cc.NextExpirationDate <= " + super.getFinalDate() + "and cc.s_item_status=1 ";
	}

	public String groupByCol() {
		return " group by  NextExpirationDate " + super.addGroupByCol();
	}

}
