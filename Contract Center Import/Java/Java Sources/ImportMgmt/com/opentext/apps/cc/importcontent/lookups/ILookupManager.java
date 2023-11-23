package com.opentext.apps.cc.importcontent.lookups;

import java.util.HashMap;
import java.util.Map;

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

public interface ILookupManager {

	//Return value could be id or itemId of the entity, agreed by implementation class and caller
	public String lookupEntity(Map<String, String> rowData, String... data);
	public static final Map<String, ILookupManager> handlerMap = new HashMap<String, ILookupManager>();
	static final CordysLogger logger = CordysLogger.getCordysLogger(ILookupManager.class);
	
	public static ILookupManager getInstance(String property, String handler) {
		ILookupManager lookupManager = null;
		if((lookupManager = handlerMap.get(handler)) == null ) {
			try {
				Object object = Class.forName(handler).newInstance();
				if(object instanceof ILookupManager) {
					lookupManager = (ILookupManager) object;
					handlerMap.put(handler, lookupManager);
				}
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {			
				logger._log("com.opentext.apps.cc.custom.ILookupManager", Severity.ERROR, e, "Unable to initialize the handler \""+handler+"\" for the property: "+property+".");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.UNABLE_TO_INITIALIZE_HANDLER,handler,property,e.getMessage());
			}
		}
		return lookupManager;
	}
	
	public default String findEntityData(final String nameSpace, final String serviceName, final String findBy, final String lookupFor){
		String value = null;
		Document document = NOMDocumentPool.getInstance().lendDocument();
		int nameElement = 0;
		int response = 0;
		try {
			nameElement = document.createElementNS("name", null, null, "http://schemas.cordys.com/default", 0);
			document.createText(findBy, nameElement);
			SOAPRequestObject importRequest = new SOAPRequestObject(nameSpace, serviceName, null, null);
			importRequest.addParameterAsXml(nameElement);
			response = importRequest.sendAndWait();
	    	int itemNode = XPath.getFirstMatch(lookupFor, null, response);
	    	value = Node.getData(itemNode);
	    	if(Utilities.isStringEmpty(value)){//value is empty means no entity present with the given input, hence you need to throw error
	    		logger._log("com.opentext.apps.cc.custom.ILookupManager", Severity.ERROR, null,"Entity instance not found with given data. No "+lookupFor+" found from "+serviceName+" with given input "+findBy);
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ENTITY_INSTANCE_NOT_FOUND,lookupFor,serviceName,findBy);
	    	}
		}
		finally {
			Utilities.cleanAll(nameElement, response);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
		return value;
	}
}
