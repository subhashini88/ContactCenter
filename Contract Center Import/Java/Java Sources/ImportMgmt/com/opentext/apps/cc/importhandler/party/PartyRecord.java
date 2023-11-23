package com.opentext.apps.cc.importhandler.party;
import java.util.Map;
import java.util.Objects;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.party.ImportConstants.AccountType;

public class PartyRecord implements ImportListener{

	int partyNode;
	MetadataInitializer metadata;
	public PartyRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
	}

	@Override
	public void doWork(ImportEvent event) {
		createRequest(event.getRow());
	}

	private void createRequest(Map<String, String> row) {
		partyNode = NomUtil.parseXML("<Party></Party>");
		
		Node.setDataElement(partyNode, ImportConstants.PARTY_REGISTRATION_ID, row.get(ImportConstants.PARTY_REGISTRATION_ID));
		Node.setDataElement(partyNode, ImportConstants.PARTY_REGISTERED_NAME, row.get(ImportConstants.PARTY_REGISTERED_NAME));
		Node.setDataElement(partyNode, ImportConstants.PARTY_NAME, row.get(ImportConstants.PARTY_REGISTERED_NAME));
		Node.setDataElement(partyNode, ImportConstants.PARTY_DESCRIPTION, row.get(ImportConstants.PARTY_DESCRIPTION));
		if(!Utilities.isStringEmpty(row.get(ImportConstants.PARTY_COUNTRY))) {
			String countryId =  metadata.countryMap.get(row.get(ImportConstants.PARTY_COUNTRY));
			Node.setDataElement(partyNode, ImportConstants.PARTY_COUNTRY, countryId);
		}
		
		if(!Utilities.isStringEmpty(row.get(ImportConstants.PARTY_ACCOUNT_TYPE))) {
			AccountType accountType =  AccountType.getEnumObject(row.get(ImportConstants.PARTY_ACCOUNT_TYPE));
			if(Objects.nonNull(accountType)) {
				Node.setDataElement(partyNode, ImportConstants.PARTY_ACCOUNT_TYPE, accountType.getValue());
			}
		}
		Node.setDataElement(partyNode, ImportConstants.CODE, row.get(ImportConstants.CODE));
		Node.setDataElement(partyNode, ImportConstants.EMAIL, row.get(ImportConstants.EMAIL));
		Node.setDataElement(partyNode, ImportConstants.IDENTIFICATION_NUMBER, row.get(ImportConstants.IDENTIFICATION_NUMBER));
		Node.setDataElement(partyNode, ImportConstants.INCEPTION_DATE, row.get(ImportConstants.INCEPTION_DATE));
		Node.setDataElement(partyNode, ImportConstants.MASTER_ID, row.get(ImportConstants.MASTER_ID));
		if(!Utilities.isStringEmpty(row.get(ImportConstants.STATUS))) {
			Status status =  Status.getEnumObject(row.get(ImportConstants.STATUS));
			if(Objects.nonNull(status)) {
				Node.setDataElement(partyNode, ImportConstants.STATUS, status.getValue());
			}
		}
		Node.setDataElement(partyNode, ImportConstants.WEBSITE, row.get(ImportConstants.WEBSITE));
		
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
		return partyNode;
	}

	public void doWork(PartyRecord collectionAccount, Map<String, String> row) {
		// TODO Auto-generated method stub
		
	}

}

