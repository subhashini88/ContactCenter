package com.opentext.apps.cc.importhandler.accountmanager;

import java.util.Map;
import java.util.Objects;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.CommonEnums.Location;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;

public class AccountManagerRecord implements ImportListener{
	int accountManagerNode;
	MetadataInitializer metadata;
	public AccountManagerRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
	}
	@Override
	public void doWork(ImportEvent event) {
		createRequest(event.getRow());
	}
	private void createRequest(Map<String, String> row) {
		accountManagerNode = NomUtil.parseXML("<AccountManager></AccountManager>");
		Node.setDataElement(accountManagerNode, ImportConstants.MANAGER_REG_ID, row.get(ImportConstants.MANAGER_REG_ID));
		Node.setDataElement(accountManagerNode, ImportConstants.MANAGER_EMAIL, row.get(ImportConstants.MANAGER_EMAIL));
		Node.setDataElement(accountManagerNode, ImportConstants.MANAGER_MANAGER_NAME, row.get(ImportConstants.MANAGER_MANAGER_NAME));
		Node.setDataElement(accountManagerNode, ImportConstants.REGION, row.get(ImportConstants.REGION));
		Node.setDataElement(accountManagerNode, ImportConstants.LINE_MANAGER, row.get(ImportConstants.LINE_MANAGER));
		if(!Utilities.isStringEmpty(row.get(ImportConstants.LOCATION))) {
			Location location =  Location.getEnumObject(row.get(ImportConstants.LOCATION));
			if(Objects.nonNull(location)) {
				Node.setDataElement(accountManagerNode, ImportConstants.LOCATION, location.getValue());
			}
		}
		if(!Utilities.isStringEmpty(row.get(ImportConstants.MANAGER_NAME))) {
			String contactId =  metadata.accountManagerMap.get(row.get(ImportConstants.MANAGER_NAME));
			Node.setDataElement(accountManagerNode, ImportConstants.MANAGER_NAME, contactId);
		}
		Node.setDataElement(accountManagerNode, ImportConstants.CODE, row.get(ImportConstants.CODE));
		Node.setDataElement(accountManagerNode, ImportConstants.CREATION_TYPE, row.get(ImportConstants.CREATION_TYPE));
		if(!Utilities.isStringEmpty(row.get(ImportConstants.STATUS))) {
			Status status =  Status.getEnumObject(row.get(ImportConstants.STATUS));
			if(Objects.nonNull(status)) {
				Node.setDataElement(accountManagerNode, ImportConstants.STATUS, status.getValue());
			}
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
		return accountManagerNode;
	}
	public void doWork(AccountManagerRecord accountmanagerRecord, Map<String, String> row) {
		// TODO Auto-generated method stub

	}

}

