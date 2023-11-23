package com.opentext.apps.cc.importhandler.revenueschedule;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.eibus.xml.nom.Node;

public class MetadataInitializer {
	public WeakHashMap<String,String> contractNumberMap = new WeakHashMap<>();
	public Map<String, String> uomMap = new HashMap<>();

	public MetadataInitializer(){
		//this.initializeVolumeUnits();
		this.initializeUOMs();
	}
	
	private void initializeUOMs() {
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
					uomMap.put(unit, itemId);
				}
			}
		}finally{
			if(nodes != null) Utilities.cleanAll(nodes);
			Utilities.cleanAll(response);
		}
	}

}
