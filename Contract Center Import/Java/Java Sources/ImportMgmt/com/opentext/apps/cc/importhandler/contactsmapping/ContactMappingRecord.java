package com.opentext.apps.cc.importhandler.contactsmapping;

import java.util.Map;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.contactsmapping.ImportConstants;

public class ContactMappingRecord implements ImportListener{
	int contactNode;
	MetadataInitializer metadata;
	public ContactMappingRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
	}
	@Override
	public void doWork(ImportEvent event) {
		createRequest(event.getRow());
	}
	private void createRequest(Map<String, String> row) {
		contactNode = NomUtil.parseXML("<Contact></Contact>");
		Node.setDataElement(contactNode, ImportConstants.LEGACY_ID, row.get(ImportConstants.LEGACY_ID));
		if(!Utilities.isStringEmpty(row.get(ImportConstants.PARTY_REGISTRATION_ID))) {
			String partyId =  metadata.partyMap.get(row.get(ImportConstants.PARTY_REGISTRATION_ID));
			Node.setDataElement(contactNode, ImportConstants.PARTY_ID, partyId);
		}
		if(!Utilities.isStringEmpty(row.get(ImportConstants.CONTACT_EMAIL))) {
			String contactId =  metadata.contactMap.get(row.get(ImportConstants.CONTACT_EMAIL));
			Node.setDataElement(contactNode, ImportConstants.CONTACT_ID, contactId);
		}

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
		return contactNode;
	}
	public void doWork(ContactMappingRecord contactRecord, Map<String, String> row) {
		// TODO Auto-generated method stub

	}

}

