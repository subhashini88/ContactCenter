package com.opentext.apps.cc.importhandler.contract;

import com.opentext.apps.cc.importcontent.AbstractReportListener;
import com.opentext.apps.cc.importcontent.ReportEvent;


public class CSVReportListenerForContracts extends AbstractReportListener {
	
	@Override
	public void prepareReport(ReportEvent event) {
		super.prepareReport(event);
	}
	
	@Override
	protected String getImportTypeColumnName() {
		return "CONTRACT NAME";
	}

	@Override
	protected String getStatusColumnName() {
		return "STATUS";
	}

	@Override
	protected String getReportColumnName() {
		return "REPORT";
	}

	@Override
	protected String getImportEntryName() {
		return "CONTRACT_NAME";
	}
	
	@Override
	protected String getImportedRecordIdColumnName() {
		return "CONTRACT_ID";
	}
}
