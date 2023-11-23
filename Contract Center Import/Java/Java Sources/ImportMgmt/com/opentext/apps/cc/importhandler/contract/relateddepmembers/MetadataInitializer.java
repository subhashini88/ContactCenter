package com.opentext.apps.cc.importhandler.contract.relateddepmembers;

import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importhandler.contractbillerperiod.ImportConstants;

public class MetadataInitializer {

	private Map<String, String> otdsPersonsMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	private WeakHashMap<String, String> contractNumberMap = new WeakHashMap<>();

	public String getContractIdByContractNumber(String contractNumber) {
		String contractId = this.contractNumberMap.get(contractNumber);
		int nodes[] = null;
		if (null == contractId) {
			int response = 0, cNumberNode = 0;
			try {
				SOAPRequestObject request = new SOAPRequestObject(ImportConstants.CONTRACTS_IMPORT_NAMESPACE,
						"GetContractDetailsByCNumber", null, null);
				String cNumber = "<contractNumber>" + contractNumber + "</contractNumber>";
				cNumberNode = NomUtil.parseXML(cNumber);
				request.addParameterAsXml(cNumberNode);
				response = request.sendAndWait();
				nodes = NomUtil.getNodeList(".//FindZ_INT_ContractNumberListResponse/Contract", response);
				if (nodes.length == 1) {
					contractId = Node.getDataWithDefault(NomUtil.getNode(".//Contract-id/ItemId", nodes[0]), null);
					if (null != contractId) {
						this.contractNumberMap.put(contractNumber, contractId);
					} else {
						contractId = null;
					}
				} else if (nodes.length == 0) {
					contractId = null;
				} else {
					return "MULTIPLE_CONTRACTS";
				}
			} finally {
				Utilities.cleanAll(response, cNumberNode);
			}
		}

		return contractId;
	}

	public String getOtdsPerson(String userId) {
		int response = 0;
		int nodes[] = null;
		String itemId = this.otdsPersonsMap.get(userId);
		String response_user = null;
		if (null == itemId) {
			try {
				String[] paramNames = { "userID", "isInternal" };
				Object[] paramValues = { userId, "false" };
				SOAPRequestObject personRequest = new SOAPRequestObject(
						"http://schemas.opentext.com/apps/cc/basiccomponents/20.2", "GetIdentityPersoswithFilters",
						paramNames, paramValues);
				response = personRequest.sendAndWait();
				nodes = NomUtil.getNodeList(".//Persons/FindPersonListInternalResponse/Person", response);
				for (int i : nodes) {
					response_user = Node.getDataWithDefault(NomUtil.getNode(".//User_ID", i), null);
					if (response_user != null && userId.equals(response_user))
						itemId = Node.getDataWithDefault(NomUtil.getNode(".//Person-id/ItemId", i), null);
				}
				if (itemId != null)
					this.otdsPersonsMap.put(userId, itemId);
			} finally {
				if (null != nodes) {
					Utilities.cleanAll(response);
				}
			}
		}
		return itemId;
	}

}
