package com.opentext.apps.cc.importhandler.datamanager.landingPages;

import java.util.Map;
import java.util.Objects;

import com.opentext.apps.cc.importcontent.ReportItem;

public class ImportValidator {
	public ImportValidator() {

	}

	public ReportItem validateLists(Map<String, String> rowData, MetadataInitializer metadata) {
		
		ReportItem report = new ReportItem();
		String inputValue;
		
		if (Objects.nonNull(rowData) && Objects.nonNull(metadata)) {
			
			inputValue = rowData.get(ImportConstants.CODE);
			if ((Objects.isNull(inputValue) || inputValue.isEmpty())) {
				report.error(ImportConstants.CODE, "Code cannot be empty.");
			}
			inputValue = rowData.get(ImportConstants.LISTNAME);
			if ((Objects.isNull(inputValue) || inputValue.isEmpty())) {
				report.error(ImportConstants.LISTNAME, "List name cannot be empty.");
			}
			inputValue = rowData.get(ImportConstants.LISTDISPLAYNAME);
			if ((Objects.isNull(inputValue) || inputValue.isEmpty())) {
				report.error(ImportConstants.LISTDISPLAYNAME, "List display name cannot be empty.");
			}
			inputValue = rowData.get(ImportConstants.TYPE);
			if ((Objects.isNull(inputValue) || inputValue.isEmpty())) {
				report.error(ImportConstants.TYPE, "Type cannot be empty.");
			}
			inputValue = rowData.get(ImportConstants.LISTDETAILSJSON);
			if ((Objects.isNull(inputValue) || inputValue.isEmpty())) {
				report.error(ImportConstants.LISTDETAILSJSON, "List Json details cannot be empty.");
			}
			inputValue = rowData.get(ImportConstants.ROLEVIEWJSON);
			if ((Objects.isNull(inputValue) || inputValue.isEmpty())) {
				report.error(ImportConstants.ROLEVIEWJSON, "Role view JSON cannot be empty.");
			}
		}
		else {
			report.error(ImportConstants.LISTNAME, "Row is null.!");
			return report;
		}
		return report;
	}

}
