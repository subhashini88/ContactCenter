package com.opentext.apps.cc.importhandler.relatedCostCenter;

import java.util.Map;

import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;
import com.opentext.apps.cc.importhandler.relatedCostCenter.ImportConstants;

public class ImportValidator {
	public ReportItem validate(Map<String, String> rowData, MetadataInitializer metadata, String jobId) {
		ReportItem report = new ReportItem();
		if (rowData == null) {
			return report;
		}
		if (Utilities.isStringEmpty(rowData.get(ImportConstants.CONTRACTNUMBER))) {
			report.error(ImportConstants.CONTRACTNUMBER, " cannot be empty");
		}
		else {
			metadata.getContractIdByContractNumber(rowData.get(ImportConstants.CONTRACTNUMBER));
			String contractId=metadata.contractNumberMap.get(rowData.get(ImportConstants.CONTRACTNUMBER));
			if (contractId==null) {
				report.error(ImportConstants.CONTRACTNUMBER, rowData.get(ImportConstants.CONTRACTNUMBER)+" is not in use");
			}
			else if(contractId=="false") {
				report.error(ImportConstants.CONTRACTNUMBER, " must not contain duplicate values");
			}
		}
		
		if (Utilities.isStringEmpty(rowData.get(ImportConstants.RELATEDCOSTCENTER_ID))) {
			report.error(ImportConstants.RELATEDCOSTCENTER_ID, " cannot be empty");
		}else {
			if(!metadata.costcenterIdMap.contains(rowData.get(ImportConstants.RELATEDCOSTCENTER_ID))) {
				report.error(ImportConstants.RELATEDCOSTCENTER_ID, rowData.get(ImportConstants.RELATEDCOSTCENTER_ID)+" is not in use");
			}
		}
		
		if (Utilities.isStringEmpty(rowData.get(ImportConstants.LEGACY_ID))) {
			report.error(ImportConstants.LEGACY_ID, " cannot be empty");
		}	

		// Enumeration validations
		
		return report;
	}
}