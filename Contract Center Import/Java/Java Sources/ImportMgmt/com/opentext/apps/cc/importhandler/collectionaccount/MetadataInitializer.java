package com.opentext.apps.cc.importhandler.collectionaccount;

import java.util.Map;
import java.util.TreeMap;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;

public class MetadataInitializer {
	public Map<String, String> managersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> partiesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private static final CordysLogger logger = CordysLogger.getCordysLogger(MetadataInitializer.class);
	MetadataInitializer()
	{
		String managerID = null, managerItemID = null;
		int response=0,nodes[] = null;
		try {
			SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas.cordys.com/default","GetAllManagers", null, null);
			response = createRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Manager", response);
			for (int i : nodes) {
				managerID = Node.getDataWithDefault(NomUtil.getNode(".//ManagerId", i), null);
				managerItemID = Node.getDataWithDefault(NomUtil.getNode(".//Manager-id/ItemId", i), null);
				if (null != managerID && null != managerItemID) {
					managersMap.put(managerID, managerItemID);
				}
			}
		} catch (Exception e) {
			logger._log("com.opentext.apps.cc.importhandler.collectionaccount.MetadataInitializer", Severity.ERROR, e, "Error while executing GetAllManagers");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,"GetAllManagers");
		}
		finally {
			if(nodes != null) Utilities.cleanAll(nodes);
			Utilities.cleanAll(response);
		}
	}
}
