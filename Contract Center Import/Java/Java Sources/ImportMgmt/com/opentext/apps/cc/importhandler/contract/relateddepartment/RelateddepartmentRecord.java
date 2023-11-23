package com.opentext.apps.cc.importhandler.contract.relateddepartment;

import java.util.Map;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.importcontent.CommonEnums.CreationType;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.contractType.ImportConstants;
import com.opentext.apps.cc.importhandler.organizations.MetadataInitializer;

public class RelateddepartmentRecord implements ImportListener {

	int relatedDepartmentRecord;
	MetadataInitializer metadata;
	RelatedDepartmentMetadataInitializer departmentMetadataInitializer;

	public RelateddepartmentRecord(MetadataInitializer orgMemMetadata, RelatedDepartmentMetadataInitializer departmentMetadataInitializer,ReportItem reportItem) {
		this.metadata = orgMemMetadata;
		this.departmentMetadataInitializer = departmentMetadataInitializer;
	}

	@Override
	public void doWork(ImportEvent event) {
		createRequest(event.getRow());
	}

	private void createRequest(Map<String, String> row) {
		relatedDepartmentRecord = NomUtil.parseXML("<RelatedDepartment></RelatedDepartment>");
		Node.setDataElement(relatedDepartmentRecord, ImportConstants.LEGACY_ID, row.get(ImportConstants.LEGACY_ID));
		Node.setDataElement(relatedDepartmentRecord, RelatedDeptImportConstants.FIELD_CONTRACT_ID, departmentMetadataInitializer.getContractIdByContractNumber(row.get(RelatedDeptImportConstants.FIELD_CONTRACT_NUMBER)));
		String organizationItemId = metadata.getOrgCode(row.get(RelatedDeptImportConstants.FIELD_ORG_CODE));
		Node.setDataElement(relatedDepartmentRecord, RelatedDeptImportConstants.FIELD_ORG_ID, organizationItemId);
		CreationType creationType = CreationType.getEnumObject("IMPORTED");
		Node.setDataElement(relatedDepartmentRecord, RelatedDeptImportConstants.CREATION_TYPE, creationType.getValue());	
	}

	@Override
	public void commit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCommit() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getSourceId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getnode() {
		// TODO Auto-generated method stub
		return relatedDepartmentRecord;
	}

	public void doWork(RelateddepartmentRecord ctrRecords, Map<String, String> row) {
		// TODO Auto-generated method stub

	}

}
