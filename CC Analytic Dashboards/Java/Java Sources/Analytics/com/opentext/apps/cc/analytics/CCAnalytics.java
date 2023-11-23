package com.opentext.apps.cc.analytics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import com.cordys.cpc.bsf.busobject.BusObjectConfig;
import com.cordys.cpc.bsf.busobject.BusObjectIterator;
import com.opentext.apps.cc.process.ReportProcessor;

public class CCAnalytics extends CCAnalyticsBase {

	public CCAnalytics() {
		this((BusObjectConfig) null);
	}

	public CCAnalytics(BusObjectConfig config) {
		super(config);
	}

	public static BusObjectIterator<com.opentext.apps.cc.analytics.CCAnalytics> getCCAnalyticsObjects(
			com.cordys.cpc.bsf.query.Cursor cursor) {
		// TODO implement body
		return null;
	}

	public static String getReportData() {
		return "SUCCESS";
	}

	public static String addFrequencyToDateTime(String dateTime, int frequency, String freq_Type) {

		final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String dateAfter = null;
		try {
			if (freq_Type.equals("DAYS")) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(inputFormat.parse(dateTime));
				cal.add(Calendar.DAY_OF_MONTH, frequency);
				dateAfter = inputFormat.format(cal.getTime());
			} else if (freq_Type.equals("HOURS")) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(inputFormat.parse(dateTime));
				cal.add(Calendar.HOUR_OF_DAY, frequency);
				dateAfter = inputFormat.format(cal.getTime());
			} else if (freq_Type.equals("MONTHS")) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(inputFormat.parse(dateTime));
				cal.add(Calendar.MONTH, frequency);
				dateAfter = inputFormat.format(cal.getTime());
			}
		} catch (ParseException e) {

		}
		return dateAfter;
	}

	public static String processInAppReports() {
		ReportProcessor reportProcessor = new ReportProcessor();
		reportProcessor.process();
		return "SUCCESS";
	}

	public static String processUserReport(String itemId1) {
		if (Objects.nonNull(itemId1)) {
			ReportProcessor reportProcessor = new ReportProcessor();
			String json = reportProcessor.processSavedReport(itemId1);
			return json;
		} else {
			throw new RuntimeException("Empty report id");
		}
	}

	public static String processReportIndividual(String itemId1) {
		if (Objects.nonNull(itemId1)) {
			ReportProcessor reportProcessor = new ReportProcessor();
			String json = reportProcessor.processSavedReportIndividual(itemId1);
			return json;
		} else {
			throw new RuntimeException("Empty report id");
		}
	}

	public static String processReportInd(String itemId1) {
		if (Objects.nonNull(itemId1)) {
			ReportProcessor reportProcessor = new ReportProcessor();
			String json = reportProcessor.processSavedReportIndividual(itemId1);
			return json;
		} else {
			throw new RuntimeException("Empty report id");
		}
	}

	public static String processMyDashboardReportInd(String chartItemId) {
		if (Objects.nonNull(chartItemId)) {
			ReportProcessor reportProcessor = new ReportProcessor();
			String json = reportProcessor.processSavedReportMyDashboard(chartItemId);
			return json;
		} else {
			throw new RuntimeException("Empty report id");
		}
	}

	public void onInsert() {
	}

	public void onUpdate() {
	}

	public void onDelete() {
	}
}
