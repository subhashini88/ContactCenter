package com.opentext.apps.cc.importhandler.externalcontacts;

import java.util.Map;
import java.util.TreeMap;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;

public class MetadataInitializer {
	public Map<String, String> genderMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	
	public MetadataInitializer(){
		this.getGender();
	}
	
	private void getGender() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject genderRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetGenderFromIdentity", null, null);
			response = genderRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Genders", response);
			for (int i : nodes) {
				String name=Node.getDataWithDefault(NomUtil.getNode(".//Gender", i),null);
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//Genders-id/ItemId", i),null);
				if(null != name){
					genderMap.put(name,itemId);
				}
			}		
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}
	}
}
