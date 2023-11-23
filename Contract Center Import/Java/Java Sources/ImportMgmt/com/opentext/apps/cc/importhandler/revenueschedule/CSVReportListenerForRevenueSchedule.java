package com.opentext.apps.cc.importhandler.revenueschedule;

import com.opentext.apps.cc.importcontent.AbstractReportListener;

public class CSVReportListenerForRevenueSchedule extends AbstractReportListener{

	@Override
	protected String getImportedRecordIdColumnName() {
		return "Revenue Schedule";
	}

	@Override
	protected String getImportTypeColumnName() {
		// TODO Auto-generated method stub
		return "ID1";
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
		return "Revenue Schedule";
	}

}
