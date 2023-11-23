package com.opentext.apps.cc.importhandler.relatedCostCenter;

import java.util.*;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importhandler.contractbillerperiod.ImportConstants;

public class MetadataInitializer {
	public List<String> costcenterIdMap = new ArrayList<String>();
	public HashMap<String, String> contractNumberMap = new HashMap<>();
	
	
	public MetadataInitializer() {
		getAllRelatedCostCenters();
	}
	
	private void getAllRelatedCostCenters() 
	{
		int response = 0;
		int nodes[] = null;
		try 
		{
			SOAPRequestObject configuratorsRequest = new SOAPRequestObject("http://schemas/OpenTextBasicComponents/GCOrganization/operations", "GetAllOrgs", null, null);
			response = configuratorsRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//GCOrganization", response);
			String CostCenterID;
			for (int i : nodes) 
			{
				CostCenterID = Node.getDataWithDefault(NomUtil.getNode(".//CostCenterId", i), null);
				if(!Utilities.isStringEmpty(CostCenterID)) {
					costcenterIdMap.add(CostCenterID);
				}
		}
	}
		finally
		{
			if (null != nodes) 
			{
				Utilities.cleanAll(response);
			}
		}
}
	
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
