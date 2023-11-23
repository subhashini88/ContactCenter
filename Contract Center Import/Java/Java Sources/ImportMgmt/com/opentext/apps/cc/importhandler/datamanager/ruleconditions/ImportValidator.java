package com.opentext.apps.cc.importhandler.datamanager.ruleconditions;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import com.opentext.apps.cc.importcontent.ReportItem;


public class ImportValidator {

	public ReportItem validateRuleConditions(Map<String, String> rowData, MetadataInitializer metadata) {
		ReportItem report = new ReportItem();
		if (Objects.nonNull(rowData) && Objects.nonNull(metadata)) {
			String ruleName = rowData.get(ImportConstants.RULENAME);
			if ((Objects.isNull(ruleName) || ruleName.isEmpty())) {
				report.error(ImportConstants.RULENAME, rowData.get(ImportConstants.RULENAME) + " is invalid data ");
			}
			String dataype = rowData.get(ImportConstants.DATATYPE);
			if ((Objects.isNull(dataype) || dataype.isEmpty()) || !Arrays.asList(ImportConstants.DATA_TYPES).contains(dataype)) {
				report.error(ImportConstants.DATATYPE,
						rowData.get(ImportConstants.DATATYPE) + " is invalid data ");
			}
			String purpose = rowData.get(ImportConstants.PURPOSE);
			if ((Objects.isNull(purpose) || purpose.isEmpty())) {
				report.error(ImportConstants.PURPOSE,
						rowData.get(ImportConstants.PURPOSE) + " is invalid data ");
			}

		}
		return report;
	}
}
