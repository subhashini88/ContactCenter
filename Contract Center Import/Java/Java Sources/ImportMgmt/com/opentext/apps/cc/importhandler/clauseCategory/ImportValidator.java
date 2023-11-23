package com.opentext.apps.cc.importhandler.clauseCategory;

import java.util.Map;

import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;
import com.opentext.apps.cc.importhandler.clauseCategory.ImportConstants;
import com.opentext.apps.cc.importhandler.clauseCategory.MetadataInitializer;

public class ImportValidator {
	public ReportItem validate(Map<String, String> rowData, MetadataInitializer metadata, String jobId) {
		ReportItem report = new ReportItem();

		if (rowData == null) {
			return report;
		}
		if (Utilities.isStringEmpty(rowData.get(ImportConstants.LEGACY_ID))) {
			report.error(ImportConstants.LEGACY_ID, "Cannot be empty");
		}
		if (Utilities.isStringEmpty(rowData.get(ImportConstants.CLAUSE_CAT_NAME))) {
			report.error(ImportConstants.CLAUSE_CAT_NAME, "Cannot be empty");
		}
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.STATUS))) {
			String statusValue = rowData.get(ImportConstants.STATUS);
			if (!Status.contains(statusValue)) {
				report.error(ImportConstants.STATUS,rowData.get(ImportConstants.STATUS) + "' is not valid");
			}
		}
		return report;
	}
}
