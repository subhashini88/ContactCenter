package com.opentext.apps.cc.importhandler.datamanager.rules;

import java.util.Map;
import java.util.Objects;

import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ReportItem;

public class ImportValidator {

	public ReportItem validate(Map<String, String> rowData, MetadataInitializer metadata) {
		ReportItem report = new ReportItem();
		if (Objects.nonNull(rowData) && Objects.nonNull(metadata)) {

			String ruleName = rowData.get(ImportConstants.RULE_NAME);
			if ((Objects.isNull(ruleName) || ruleName.isEmpty())) {
				report.error(ImportConstants.RULE_NAME, rowData.get(ImportConstants.RULE_NAME) + " is invalid data ");
			}

			if (rowData.get(ImportConstants.TYPE).equalsIgnoreCase("Rule")) {
				report = new ReportItem();
				if (Utilities.isStringEmpty(rowData.get(ImportConstants.RULE_NAME))) {
					report.error(ImportConstants.RULE_NAME, "Rule name cannot be empty");
				}

				if (Utilities.isStringEmpty(rowData.get(ImportConstants.TASK_LISTCODE))) {
					report.error(ImportConstants.TASK_LISTCODE, "Task list cannot be empty");
				}

			}

		}

		return report;
	}

}