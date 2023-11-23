package com.opentext.apps.cc.importhandler.contract.relateddepartment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importhandler.contractbillerperiod.ImportConstants;

public class RelatedDepartmentMetadataInitializer {

	private WeakHashMap<String, String> contractNumberMap = new WeakHashMap<>();

	public String getContractIdByContractNumber(String contractNumber) {
		String contractId = this.contractNumberMap.get(contractNumber);
		if (null == contractId) {
			int response = 0, cNumberNode = 0;
			int nodes[]=null;
			try {
				SOAPRequestObject request = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetContractDetailsByCNumber",null,null);
				String cNumber = "<contractNumber>" + contractNumber + "</contractNumber>";
				cNumberNode = NomUtil.parseXML(cNumber);
				request.addParameterAsXml(cNumberNode);
				response = request.sendAndWait();
				nodes = NomUtil.getNodeList(".//Contract", response);
				for (int i : nodes) 
				{
					contractId = Node.getDataWithDefault(NomUtil.getNode(".//Contract-id/ItemId", i), null);
					if(!Utilities.isStringEmpty(contractNumberMap.get(contractNumber))) {
						contractNumberMap.put(contractNumber,"false");
						contractId="false";
						break;
					}
					else {
						contractNumberMap.put(contractNumber,contractId);
					}
			}
			} finally {
				Utilities.cleanAll(response, cNumberNode);
			}
		}
		return contractId;
	}
}
