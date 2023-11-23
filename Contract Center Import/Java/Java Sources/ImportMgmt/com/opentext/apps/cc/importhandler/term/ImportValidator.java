package com.opentext.apps.cc.importhandler.term;

import java.util.Map;

import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;
import com.opentext.apps.cc.importhandler.term.ImportConstants;
import com.opentext.apps.cc.importhandler.term.MetadataInitializer;
import java.util.regex.*;

public class ImportValidator {
	public ReportItem validate(Map<String, String> rowData, MetadataInitializer metadata, String jobId) {
		ReportItem report = new ReportItem();
		if (rowData == null) {
			return report;
		}
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.TERM_NAME))) {
			String term_name = rowData.get(ImportConstants.TERM_NAME);
			Pattern pattern = Pattern.compile(ImportConstants.VALIDATION_PATTERN);
			Matcher matcher = pattern.matcher(term_name);
			if (matcher.find()) {
				report.error(ImportConstants.TERM_NAME, "Term name has invalid characters");
			}
		} else {
			report.error(ImportConstants.TERM_NAME, "Please give term name");
		}
		if (Utilities.isStringEmpty(rowData.get(ImportConstants.LEGACY_ID))) {
			report.error(ImportConstants.LEGACY_ID, "Please give LEGACYID");
		}

		// Enumeration validations

		// Status.
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.STATUS))) {
			String statusValue = rowData.get(ImportConstants.STATUS);
			if (!Status.contains(statusValue)) {
				report.error(ImportConstants.STATUS,rowData.get(ImportConstants.STATUS) + "' is not valid");
			}
		}

		return report;
	}
}