package com.opentext.apps.cc.importhandler.contractType;

import java.util.Map;

import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;
import com.opentext.apps.cc.importcontent.CommonEnums.IntentType;
import com.opentext.apps.cc.importhandler.contractType.ImportConstants;
import com.opentext.apps.cc.importhandler.contractType.MetadataInitializer;

public class ImportValidator {
	public ReportItem validate(Map<String, String> rowData, MetadataInitializer metadata, String jobId) {
		ReportItem report = new ReportItem();
		String intentType = ImportConstants.INTENT_TYPE;

		if (rowData == null) {
			return report;
		}
		if (Utilities.isStringEmpty(rowData.get(ImportConstants.LEGACY_ID))) {
			report.error(ImportConstants.LEGACY_ID, "Cannot be empty");
		}
		if (Utilities.isStringEmpty(rowData.get(ImportConstants.CONTRACT_TYPE_NAME))) {
			report.error(ImportConstants.CONTRACT_TYPE_NAME, "Cannot be empty");
		}
		if (!Utilities.isStringEmpty(rowData.get(intentType))) {
			String statusValue = rowData.get(intentType);
			if (!IntentType.contains(statusValue)) {
				report.error(intentType,rowData.get(intentType) + "' is not valid");
			}
		}
		else{
			report.error(intentType, "Cannot be empty");
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
