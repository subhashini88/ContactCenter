package com.opentext.apps.cc.importhandler.manager;

import java.util.Map;

import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.manager.ImportConstants;

public class ImportValidator {

	public ReportItem validate(Map<String, String> rowData, MetadataInitializer metadata) {
		
		ReportItem report = new ReportItem();
		
		if (rowData == null) return report;
		
		if(Utilities.isStringEmpty(rowData.get(ImportConstants.CM))){
			report.error(ImportConstants.CM, "Coustomer manager is empty");
		}
		if(Utilities.isStringEmpty(rowData.get(ImportConstants.CM_REPLACEMENT))){
			report.error(ImportConstants.CM_REPLACEMENT, "Replacement Manager is empty");
		}
		return report;
	}

}
