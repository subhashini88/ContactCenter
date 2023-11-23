package com.opentext.apps.cc.importhandler.addresses;

import java.util.Map;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.addresses.ImportConstants;
import com.opentext.apps.cc.importhandler.addresses.MetadataInitializer;

public class AddressRecord implements ImportListener{

	int addressNode;
	MetadataInitializer metadata;
	public AddressRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
	}

	@Override
	public void doWork(ImportEvent event) {
		createRequest(event.getRow());	
	}

	private void createRequest(Map<String, String> row) {
		addressNode = NomUtil.parseXML("<Address></Address>");
		Node.setDataElement(addressNode, ImportConstants.LEGACY_ID, row.get(ImportConstants.LEGACY_ID));
		//Node.setDataElement(addressNode, ImportConstants.EMAIL, row.get(ImportConstants.EMAIL));
		Node.setDataElement(addressNode, ImportConstants.EMAIL, metadata.emailMap.get(row.get(ImportConstants.EMAIL)));

		Node.setDataElement(addressNode, ImportConstants.ADDRESS_LINE_1, row.get(ImportConstants.ADDRESS_LINE_1));
		Node.setDataElement(addressNode, ImportConstants.CITY, row.get(ImportConstants.CITY));
		Node.setDataElement(addressNode, ImportConstants.POSTAL_CODE, row.get(ImportConstants.POSTAL_CODE));
		//Node.setDataElement(addressNode, ImportConstants.COUNTRY_NAME, metadata.countryMap.get(row.get(ImportConstants.COUNTRY_NAME)));
		if(!Utilities.isStringEmpty(row.get(ImportConstants.COUNTRY_NAME))) {
			String countryID = metadata.countryMap.get(row.get(ImportConstants.COUNTRY_NAME));
			Node.setDataElement(addressNode, ImportConstants.COUNTRY_NAME, countryID);
		}
		if(!Utilities.isStringEmpty(row.get(ImportConstants.STATE_NAME))) {
			Node.setDataElement(addressNode, ImportConstants.STATE_NAME, metadata.stateMap.get(row.get(ImportConstants.STATE_NAME)+row.get(ImportConstants.COUNTRY_NAME)));
		}
		Node.setDataElement(addressNode, ImportConstants.DEFAULT, row.get(ImportConstants.DEFAULT));	
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
		return addressNode;
	}

}