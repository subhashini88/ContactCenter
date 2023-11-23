package com.opentext.apps.cc.importhandler.party;

import java.util.Map;
import java.util.TreeMap;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;

public class MetadataInitializer {
	public Map<String, String> countryMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	MetadataInitializer(){
		this.getALLCountries();
	}
	private void getALLCountries() {
/*		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject docTypesRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetAllCountries", null, null);
			response = docTypesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RelatedCountries", response);
			for (int i : nodes) {
				String name=Node.getDataWithDefault(NomUtil.getNode(".//Country_Name", i),null);
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedCountries-id/ItemId1", i),null);
				if(null != name){
					countryMap.put(name,itemId);
				}
			}		
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}*/
		
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject docTypesRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetCountryFromIdentity", null, null);
			response = docTypesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Country", response);
			for (int i : nodes) {
				String name=Node.getDataWithDefault(NomUtil.getNode(".//Country_Name", i),null);
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//Country-id/ItemId", i),null);
				if(null != name){
					countryMap.put(name,itemId);
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
