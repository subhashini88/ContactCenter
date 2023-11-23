package com.opentext.apps.cc.importhandler.organizations;

import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;

public class MetadataInitializer {
	public Map<String, String> orgMap = new HashMap<String, String>();
	public Map<String, String> orgIdMap = new HashMap<String,String>();
	public List<String> duplicateorgMap = new ArrayList<String>();
	
	public MetadataInitializer() {
		getAllOrganizations();
	}
	
	private void getAllOrganizations() 
	{
		int response = 0;
		int nodes[] = null;
		try 
		{
			SOAPRequestObject configuratorsRequest = new SOAPRequestObject("http://schemas/OpenTextBasicComponents/GCOrganization/operations", "GetAllOrgs", null, null);
			response = configuratorsRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//GCOrganization", response);
			String orgParentItemId,OrganizationCode,OrganizationName;
			for (int i : nodes) 
			{
				OrganizationName = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				OrganizationCode = Node.getDataWithDefault(NomUtil.getNode(".//OrgCode", i), null);
				orgParentItemId = Node.getDataWithDefault(NomUtil.getNode(".//ParentOrganization/GCOrganization-id/ItemId", i), null);
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//GCOrganization-id/ItemId", i), null);
				if(orgMap.containsKey(OrganizationCode)) {
					duplicateorgMap.add(OrganizationCode);
				}else {
				if(!Utilities.isStringEmpty(OrganizationCode)) {
				orgMap.put(OrganizationCode,OrganizationName);
				orgIdMap.put(OrganizationCode, itemId);
					}
				}
//				if(Objects.nonNull(orgIdMap.get(OrganizationName))) {
//					orgIdMap.get(OrganizationName).add(orgParentItemId);
//				
//				}else {
//					orgIdMap.put(OrganizationName, new ArrayList<String>()).add(orgParentItemId);
//				}
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
	public String getOrgCode(String code) {
		if(duplicateorgMap.contains(code)) {
			return "duplicate";
		}
		else if(orgIdMap.containsKey(code)) {
			return orgIdMap.get(code);
		}
		else return "false";
	}
}
