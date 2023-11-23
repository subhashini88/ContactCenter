package com.opentext.apps.cc.importhandler.externalcontacts;

import java.util.Map;
import java.util.Objects;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.CommonEnums.PersonPrefix;

public class ExternalContactRecord implements ImportListener{

	int externalContactNode;
	MetadataInitializer metadata;
	public ExternalContactRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
	}
	
	@Override
	public void doWork(ImportEvent event) {
		createRequest(event.getRow());
	}
	
	private void createRequest(Map<String, String> row) {
		externalContactNode = NomUtil.parseXML("<ExternalContact></ExternalContact>");
		Node.setDataElement(externalContactNode, ImportConstants.LEGACY_ID, row.get(ImportConstants.LEGACY_ID));		
		if(!Utilities.isStringEmpty(row.get(ImportConstants.TITLE))) {
			PersonPrefix title =  PersonPrefix.getEnumObject(row.get(ImportConstants.TITLE));
			if(Objects.nonNull(title)) {
				Node.setDataElement(externalContactNode, ImportConstants.TITLE, title.getValue());
			}
		}
		Node.setDataElement(externalContactNode, ImportConstants.FIRST_NAME, row.get(ImportConstants.FIRST_NAME));
		Node.setDataElement(externalContactNode, ImportConstants.LAST_NAME, row.get(ImportConstants.LAST_NAME));
		Node.setDataElement(externalContactNode, ImportConstants.DISPLAY_NAME, row.get(ImportConstants.DISPLAY_NAME));		
//		if(!Utilities.isStringEmpty(row.get(ImportConstants.GENDER))) {
//			String genderID =  metadata.genderMap.get(row.get(ImportConstants.GENDER));
//			Node.setDataElement(externalContactNode, ImportConstants.GENDER, genderID);
//		}		
		Node.setDataElement(externalContactNode, ImportConstants.BIRTH_DATE, row.get(ImportConstants.BIRTH_DATE));
		Node.setDataElement(externalContactNode, ImportConstants.EMAIL, row.get(ImportConstants.EMAIL));
		Node.setDataElement(externalContactNode, ImportConstants.PHONE, row.get(ImportConstants.PHONE));
		Node.setDataElement(externalContactNode, ImportConstants.MOBILE, row.get(ImportConstants.MOBILE));
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
		return externalContactNode;
	}
	public void doWork(ExternalContactRecord externalContactRecord, Map<String, String> row) {
		// TODO Auto-generated method stub

	}
	
	
}