package com.opentext.apps.cc.importhandler.contract.relateddepmembers;

import java.util.Map;
import java.util.Objects;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;

public class RelDepMemberRecord  implements ImportListener{
	
	int relDepMemberNode;
	MetadataInitializer metadata;
	private String legacyId = null;

	public RelDepMemberRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
	}

	@Override
	public void doWork(ImportEvent event) {
		createRequest(event.getRow());
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
		return relDepMemberNode;
	}
	
	private void createRequest(Map<String, String> row) {
		relDepMemberNode = NomUtil.parseXML("<RelatedDepartmentMember></RelatedDepartmentMember>");
		Node.setDataElement(relDepMemberNode, ImportConstants.LEGACY_ID, row.get(ImportConstants.LEGACY_ID));
		Node.setDataElement(relDepMemberNode, ImportConstants.CONTRACT_ID, metadata.getContractIdByContractNumber(row.get(ImportConstants.CONTRACT_NUMBER)));
		Node.setDataElement(relDepMemberNode, "PersonID", metadata.getOtdsPerson(row.get(ImportConstants.USER_ID)));
	}
}
