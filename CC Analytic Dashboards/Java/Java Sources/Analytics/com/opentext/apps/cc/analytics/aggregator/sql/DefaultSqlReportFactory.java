package com.opentext.apps.cc.analytics.aggregator.sql;

import com.opentext.apps.cc.analytics.aggregator.sql.builder.DBType;
import com.opentext.apps.cc.process.ReportDashboardConfig;

public class DefaultSqlReportFactory {

	public static enum DefaultReports {
		Renewal, Termination
	}

	public static ISqlReport getDefaultReport(DefaultReports reportType, DBType dbType, ReportDashboardConfig configuration) {
		if (reportType.equals(DefaultReports.Renewal)) {
			return new RenewalDefaultChart(dbType, configuration);
		} else if (reportType.equals(DefaultReports.Termination)) {
			return new TerminationDefaultChart(dbType, configuration);
		}
		return null;
	}

}