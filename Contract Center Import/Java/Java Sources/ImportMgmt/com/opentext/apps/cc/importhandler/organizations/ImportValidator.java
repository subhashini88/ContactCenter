package com.opentext.apps.cc.importhandler.organizations;

import java.util.Map;

import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;
import com.opentext.apps.cc.importhandler.organizations.ImportConstants;
import com.opentext.apps.cc.importhandler.organizations.MetadataInitializer;
import java.util.regex.*;

public class ImportValidator {
	public ReportItem validate(Map<String, String> rowData, MetadataInitializer metadata, String jobId) {
		ReportItem report = new ReportItem();
		if (rowData == null) {
			return report;
		}
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.ORGANIZATION_NAME))) {
			String organization_name = rowData.get(ImportConstants.ORGANIZATION_NAME);
		} else {
			report.error(ImportConstants.ORGANIZATION_NAME, "Organization name cannot be empty");
		}
		if(!Utilities.isStringEmpty(rowData.get(ImportConstants.PARENT_ORGANIZATION_CODE))) {
			String organization_name = rowData.get(ImportConstants.PARENT_ORGANIZATION_CODE);
			if(metadata.duplicateorgMap.contains(organization_name)) {
				report.error(ImportConstants.PARENT_ORGANIZATION_CODE, "The parent organization code '"+rowData.get(ImportConstants.PARENT_ORGANIZATION_CODE)+"' refers to more than one organization. Ensure that only one reference is made for a given organization code");
			}
		}
//		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.ORGANIZATION_CODE))) {
//			String organization_code = rowData.get(ImportConstants.ORGANIZATION_CODE);
//		} else {
//			report.error(ImportConstants.ORGANIZATION_CODE, "Organization code cannot be empty.");
//		}
		
		if (Utilities.isStringEmpty(rowData.get(ImportConstants.LEGACY_ID))) {
			report.error(ImportConstants.LEGACY_ID, "Legacy ID cannot be empty");
		}	

		// Enumeration validations
		

		return report;
	}
}