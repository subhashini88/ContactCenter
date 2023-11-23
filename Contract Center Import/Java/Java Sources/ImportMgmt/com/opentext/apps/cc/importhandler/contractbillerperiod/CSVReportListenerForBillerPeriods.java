package com.opentext.apps.cc.importhandler.contractbillerperiod;

import com.opentext.apps.cc.importcontent.AbstractReportListener;


public class CSVReportListenerForBillerPeriods extends AbstractReportListener{

	@Override
	protected String getImportedRecordIdColumnName() {
		return "Biller Period";
	}

	@Override
	protected String getImportTypeColumnName() {
		// TODO Auto-generated method stub
		return "BILLER_ID";
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
		return "Biller Period";
	}

}
