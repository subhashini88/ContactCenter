package com.opentext.apps.cc.importhandler.contractType;

import java.util.Map;
import java.util.Objects;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.CommonEnums.CreationType;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;
import com.opentext.apps.cc.importhandler.clause.ClauseRecord;
import com.opentext.apps.cc.importhandler.contractType.ImportConstants;
import com.opentext.apps.cc.importhandler.contractType.MetadataInitializer;

public class ContractTypeRecord implements ImportListener{
	
	
	int contractTypeRecord;
	MetadataInitializer metadata;
	
	public ContractTypeRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
	}
	
	@Override
	public void doWork(ImportEvent event) {
		// TODO Auto-generated method stub
		createRequest(event.getRow());
	}
	
	private void createRequest(Map<String, String> row) {
		contractTypeRecord = NomUtil.parseXML("<ContractType></ContractType>");
		
		Node.setDataElement(contractTypeRecord, ImportConstants.LEGACY_ID, row.get(ImportConstants.LEGACY_ID));
		Node.setDataElement(contractTypeRecord, ImportConstants.CONTRACT_TYPE_NAME, row.get(ImportConstants.CONTRACT_TYPE_NAME));
		Node.setDataElement(contractTypeRecord, ImportConstants.INTENT_TYPE, row.get(ImportConstants.INTENT_TYPE));
		
		if(!Utilities.isStringEmpty(row.get(ImportConstants.CONTRACT_TYPE_DESCRIPTION))) {
			Node.setDataElement(contractTypeRecord, ImportConstants.CONTRACT_TYPE_DESCRIPTION, row.get(ImportConstants.CONTRACT_TYPE_DESCRIPTION));
			}
		
		if (!Utilities.isStringEmpty(row.get(ImportConstants.STATUS))) {
			Status status = Status.getEnumObject(row.get(ImportConstants.STATUS));
			if (Objects.nonNull(status)) {
				Node.setDataElement(contractTypeRecord, ImportConstants.STATUS, status.getValue());
			}
		}
		CreationType creationType = CreationType.getEnumObject("IMPORTED");
		Node.setDataElement(contractTypeRecord, ImportConstants.CREATION_TYPE, creationType.getValue());	
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
		return contractTypeRecord;
	}
	
	public void doWork(ContractTypeRecord ctrRecords, Map<String, String> row) 
	{
		// TODO Auto-generated method stub

	}

}
