package com.opentext.apps.cc.analytics.aggregator.sql;

import java.util.Calendar;

import com.opentext.apps.cc.analytics.aggregator.AbstractProcessor;
import com.opentext.apps.cc.analytics.aggregator.sql.dao.CCCustomJsonDao;
import com.opentext.apps.cc.process.ReportDashboardConfig;

public class ProcessReportSql extends AbstractProcessor {

	@Override
	public String processReport(ReportDashboardConfig configuration) {
		QueryBuilder builder = new QueryBuilder(configuration);
		String sql = builder.buildSql();
		CCCustomJsonDao ccCustomJsonDao = new CCCustomJsonDao();
		String res = ccCustomJsonDao.getDbJsonObjects(sql);
		String jsonData = null;
		if (res.toString().isEmpty()) {
			jsonData = "{\"data\":[]}";
		} else {
			jsonData = "{\"time\":\"" + Calendar.getInstance().getTimeInMillis() + "\" , \"data\":" + res + "}";
		}
		return jsonData;
	}

}
