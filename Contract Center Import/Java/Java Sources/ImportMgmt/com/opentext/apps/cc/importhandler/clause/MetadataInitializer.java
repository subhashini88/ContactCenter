package com.opentext.apps.cc.importhandler.clause;

import java.util.Map;
import java.util.TreeMap;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;

public class MetadataInitializer {
	public Map<String, String> typeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> clauseCategoryMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	
	MetadataInitializer(){
		this.getAllTypes();
		this.getAllClauseCategories();
	}
	
	private void getAllTypes() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject typesRequest = new SOAPRequestObject("http://schemas/OpenTextBasicComponents/GCType/operations", "GetAllTypes", null, null);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//GCType", response);
			for (int i : nodes) {
				String name=Node.getDataWithDefault(NomUtil.getNode(".//Name", i),null);
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//GCType-id/ItemId", i),null);
				if(null != name){
					typeMap.put(name,itemId);
				}
			}
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}		
		
	}
	
	private void getAllClauseCategories() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject typesRequest = new SOAPRequestObject("http://schemas/OpenTextContentLibrary/GCClauseCategory/operations", "Import_GetAllClauseCategories", null, null);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//GCClauseCategory", response);
			for (int i : nodes) {
				String name=Node.getDataWithDefault(NomUtil.getNode(".//Name", i),null);
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//GCClauseCategory-id/ItemId", i),null);
				if(null != name){
					clauseCategoryMap.put(name,itemId);
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
