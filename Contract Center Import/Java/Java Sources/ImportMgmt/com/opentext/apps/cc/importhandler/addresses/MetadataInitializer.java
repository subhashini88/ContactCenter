package com.opentext.apps.cc.importhandler.addresses;

import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;

public class MetadataInitializer {
	public Map<String, String> countryMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> emailMap = new WeakHashMap<String, String>();
	public Map<String, String> stateMap = new WeakHashMap<String, String>();

	public MetadataInitializer(){
		this.getALLCountries();
	}

	public MetadataInitializer(String email, String state, String country) {
		this.getALLCountries();
		this.getPersonId(email);
		this.getStateId(state, country);
	}

	private void getALLCountries() {
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

	private void getPersonId(String emailId) 
	{
		int response = 0;
		try
		{
			SOAPRequestObject request = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetPersonDetailsbyEmailId", null, null);
			request.addParameterAsXml(NomUtil.parseXML("<EmailId>"+emailId+"</EmailId>"));
			request.addParameterAsXml(NomUtil.parseXML("<Is_Internal>true</Is_Internal>"));
			response = request.sendAndWait();
			emailMap.put(emailId, Node.getDataWithDefault(NomUtil.getNode(".//Person-id/ItemId", response),null));	
		}
		finally
		{
			Utilities.cleanAll(response);	
		}
	}

	private void getStateId(String state, String country) {
		int response = 0;
		try{
			SOAPRequestObject request = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetStateByNameAndCountry", null, null);
			request.addParameterAsXml(NomUtil.parseXML("<statename>"+state+"</statename>"));
			request.addParameterAsXml(NomUtil.parseXML("<countryname>"+country+"</countryname>"));
			response = request.sendAndWait();
			stateMap.put(state+country, Node.getDataWithDefault(NomUtil.getNode(".//State-id/ItemId1", response),null));	
		}finally{
			Utilities.cleanAll(response);	

		}
	}

}
