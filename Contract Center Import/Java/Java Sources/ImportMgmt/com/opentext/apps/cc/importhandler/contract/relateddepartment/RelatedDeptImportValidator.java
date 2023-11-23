package com.opentext.apps.cc.importhandler.contract.relateddepartment;

import java.util.Map;

import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.organizations.MetadataInitializer;
import com.opentext.apps.cc.importhandler.relatedCostCenter.ImportConstants;

public class RelatedDeptImportValidator {
	public ReportItem validate(Map<String, String> rowData, MetadataInitializer orgMemMetadata, RelatedDepartmentMetadataInitializer departmentMetadataInitializer, String jobId) {
		ReportItem report = new ReportItem();
		if (rowData == null) {
			return report;
		}
		if (Utilities.isStringEmpty(rowData.get(RelatedDeptImportConstants.FIELD_CONTRACT_NUMBER))) {
			report.error(RelatedDeptImportConstants.FIELD_CONTRACT_NUMBER, " cannot be empty");
		}
		if (Utilities.isStringEmpty(rowData.get(RelatedDeptImportConstants.FIELD_ORG_CODE))) {
			report.error(RelatedDeptImportConstants.FIELD_ORG_CODE, " cannot be empty");
		}
		if (Utilities.isStringEmpty(rowData.get(RelatedDeptImportConstants.LEGACY_ID))) {
			report.error(RelatedDeptImportConstants.LEGACY_ID, " cannot be empty");
		}

		String organizationItemId = orgMemMetadata.getOrgCode(rowData.get(RelatedDeptImportConstants.FIELD_ORG_CODE));
		if (organizationItemId==null || Utilities.isStringEmpty(organizationItemId) || organizationItemId.equals("false") ) {
			report.error(RelatedDeptImportConstants.FIELD_ORG_CODE, rowData.get(RelatedDeptImportConstants.FIELD_ORG_CODE)+" is not in use");
		}
		else if(organizationItemId.equals("duplicate")) {
			report.error(RelatedDeptImportConstants.FIELD_ORG_CODE, rowData.get(RelatedDeptImportConstants.FIELD_ORG_CODE)+" must not contain duplicate values");
		}
		
		String contractId = departmentMetadataInitializer.getContractIdByContractNumber(rowData.get(RelatedDeptImportConstants.FIELD_CONTRACT_NUMBER));
		if (contractId==null) {
			report.error(RelatedDeptImportConstants.FIELD_CONTRACT_NUMBER, rowData.get(RelatedDeptImportConstants.FIELD_CONTRACT_NUMBER)+" is not in use");
		}
		else if(contractId=="false") {
			report.error(ImportConstants.CONTRACTNUMBER, " must not contain duplicate values");
		}
		
		return report;
	}
}
