package com.opentext.apps.cc.analytics.aggregator.sql;

import com.opentext.apps.cc.analytics.aggregator.sql.builder.DBType;
import com.opentext.apps.cc.process.ReportDashboardConfig;

public class TerminationDefaultChart extends AbstractDefaultReport {

	public TerminationDefaultChart(DBType dbType, ReportDashboardConfig configuration) {
		super(dbType, "cancellationdate", configuration);
	}

	@Override
	public String getSql() {
		String sql = " select cancellationdate xcol," + super.addYCol() + " count(1) agg  from " + "( " + super.getSql()
				+ ") tab " + groupByCol();
		return sql;
	}

	public String prepareWhereClasue() {
		return " and cc.perpetual='false' and  ( z_int_status='TERMINATION_INPROGRESS' or z_int_status='TerminationCancelReview' ) and  cancellationdate is not null and cancellationdate >= "
				+ DbUtil.getPresentDateStr("yyyy-MM-dd") + "and cancellationdate <= " + super.getFinalDate()
				+ "and cc.s_item_status=1 ";
	}

	public String groupByCol() {
		return " group by  cancellationdate " + super.addGroupByCol();
	}

}
