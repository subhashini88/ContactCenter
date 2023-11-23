package com.opentext.apps.cc.importhandler.contractlines;

import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.eibus.xml.nom.internal.NOMDocumentPool;
import com.eibus.xml.nom.Node;

public class MetadataInitializer {
	public Map<String, String> productGroupsandServicesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> unitOfMeasurementsMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> poNumbersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public WeakHashMap<String, String> contractNumberMap = new WeakHashMap<>();

	public MetadataInitializer() {
		// Create a BPM to Get all the Volume Units and generate Web service
		this.getAllProductGroupsandServices();
		this.getAllUnitOfMeasurements();
		
	}
	private void getAllProductGroupsandServices(){
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "getProductGroupsandServices", null,null);
			response = createRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Service", response);
			for (int i : nodes) {
				String service = null, productgroup = null, itemId1 = null;
				service = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				itemId1 = Node.getDataWithDefault(NomUtil.getNode(".//Service-id/ItemId1", i), null);
				productgroup = Node.getDataWithDefault(NomUtil.getNode(".//Owner/Name", i), null);
				if (null != itemId1) {
					productGroupsandServicesMap.put(productgroup + ";" + service, itemId1);
				}
			}
		} finally {
			if(nodes != null) Utilities.cleanAll(nodes);
			Utilities.cleanAll(response);
		}

		
	}
	
	private void getAllUnitOfMeasurements() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetAllUOMs", null, null);
			response = createRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//UnitOfMeasurement", response);
			for (int i : nodes) {
				String unit=null,itemId=null;
				unit= Node.getDataWithDefault(NomUtil.getNode(".//Unit", i),null);
				itemId=Node.getDataWithDefault(NomUtil.getNode(".//UnitOfMeasurement-id/ItemId", i),null);
				if(null != itemId){
					unitOfMeasurementsMap.put(unit, itemId);
				}
			}
		}finally {
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}
	}
	
	public void getAllContractPONumbers(String contractID) {
		int response = 0;
		int nodes[] = null;
		int contractIDNode = 0;
		try
		{
			SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenter/Contract/operations", "GetRelatedPONumbers", null, null);
            contractIDNode = NOMDocumentPool.getInstance().createElement("Contract-id");
			Node.createTextElement("Id", contractID, contractIDNode);
			createRequest.addParameterAsXml(contractIDNode);
			response = createRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RelatedPOs", response);
			for (int i : nodes) {
				String poNumber=null,itemId=null;
				poNumber= Node.getDataWithDefault(NomUtil.getNode(".//PONumber", i),null);
				itemId=Node.getDataWithDefault(NomUtil.getNode(".//RelatedPOs-id/ItemId", i),null);
				if(null != itemId){
					poNumbersMap.put(poNumber, itemId);
				}
			}
		}finally {
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}
	}
}