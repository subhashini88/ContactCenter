package com.opentext.apps.cc.importcontent.lookups;

import java.util.Map;
import java.util.WeakHashMap;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.internal.NOMDocumentPool;
import com.eibus.xml.xpath.XPath;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;

public class PartyLookupManager implements ILookupManager{
	
	 Map<String, String> partyMapByRegistrationID = new WeakHashMap<String, String>();
	 Map<String, String> partyMap = new WeakHashMap<String, String>();
	 private static final CordysLogger logger = CordysLogger.getCordysLogger(PartyLookupManager.class);

	@Override
	public String lookupEntity(Map<String, String> rowData, String... data) {
		String itemId = null;
		if(Utilities.isArrayEmpty(data) || Utilities.isStringEmpty(data[0])) {
			logger._log("com.opentext.apps.cc.custom.PartyLookupManager", Severity.INFO, null, "Input shouldn't be null/empty");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.INPUT_SHOULD_NOT_BE_NULL);
		} 
		if( (itemId = partyMap.get(data[0])) == null ) {
			itemId = findEntityData("http://schemas/OpenTextPartyManagement/Party/operations", "GetContractingEntityByRegName", data[0], ".//ItemId");
	    	partyMap.put(data[0], itemId);
		}
		return itemId;
	}
	
	public String lookupEntityByRegistrationID(Map<String, String> rowData, String... data) {
		String itemId = null;
		try {
			if(Utilities.isArrayEmpty(data) || Utilities.isStringEmpty(data[0])) {
				logger._log("com.opentext.apps.cc.custom.PartyLookupManager", Severity.INFO, null, "Input shouldn't be null/empty");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.INPUT_SHOULD_NOT_BE_NULL);
			}
			if( (itemId = partyMapByRegistrationID.get(data[0])) == null ) {
				itemId = findEntityData("http://schemas.cordys.com/default", "GetPartyByRegistrationID", data[0], ".//ItemId");
				partyMapByRegistrationID.put(data[0], itemId);
			}
		} catch (Exception e) {			
			logger._log("com.opentext.apps.cc.custom.PartyLookupManager", Severity.ERROR, e, "");
		}
		return itemId;
	}
	
	public String findEntityDatawithOneFilter(final String nameSpace, final String serviceName, final String filterElement, final String filter, final String lookupFor){
		String value = null;
		Document document = NOMDocumentPool.getInstance().lendDocument();
		int nameElement = 0;
		int response = 0;
		try {
			nameElement = document.createElement(filterElement);
			document.createText(filter, nameElement);
			SOAPRequestObject importRequest = new SOAPRequestObject(nameSpace, serviceName, null, null);
			importRequest.addParameterAsXml(nameElement);
			response = importRequest.sendAndWait();
			int itemNode = XPath.getFirstMatch(lookupFor, null, response);
			value = Node.getData(itemNode);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			Utilities.cleanAll(nameElement, response);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
		return value;
	}
}
