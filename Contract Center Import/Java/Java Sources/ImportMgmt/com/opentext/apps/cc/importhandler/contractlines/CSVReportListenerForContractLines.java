package com.opentext.apps.cc.importhandler.contractlines;

import com.opentext.apps.cc.importcontent.AbstractReportListener;

public class CSVReportListenerForContractLines extends AbstractReportListener{

	@Override
	protected String getImportedRecordIdColumnName() {
		return "Contract line";
	}

	@Override
	protected String getImportTypeColumnName() {
		// TODO Auto-generated method stub
		return "LINE_ID";
	}

	@Override
	protected String getStatusColumnName() {
		// TODO Auto-generated method stub
		return "STATUS";
	}

	@Override
	protected String getReportColumnName() {
		// TODO Auto-generated method stub
		return "REPORT";
	}

	@Override
	protected String getImportEntryName() {
		// TODO Auto-generated method stub
		return "Contract line";
	}

}
