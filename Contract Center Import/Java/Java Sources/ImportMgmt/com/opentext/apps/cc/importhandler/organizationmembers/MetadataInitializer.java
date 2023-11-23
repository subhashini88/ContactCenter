package com.opentext.apps.cc.importhandler.organizationmembers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;

public class MetadataInitializer {

	private Map<String, String> allOrganizationsIdMap;
	private Map<String, OrganizationRecord> allOrganizationsRecordMap;
	private Map<String, String> allContactTypesIdMap;
	public Map<String, String> otdsPersonsMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	List<String> duplicateOrg = new ArrayList<>();

	public MetadataInitializer() {
		allOrganizationsIdMap = new HashMap<>();
		allContactTypesIdMap = new HashMap<>();
		allOrganizationsRecordMap = new HashMap<>();
		loadAllOrganizationsId();
		loadAllContactTypesId();
	}

	public Map<String, String> getAllOrganizationsIdMap() {
		return allOrganizationsIdMap;
	}

	public Map<String, String> getAllContactTypesIdMap() {
		return allContactTypesIdMap;
	}

	private void loadAllOrganizationsId() {

		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject typesRequest = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/cc/basiccomponents/20.2", "GetOrgsWithFilters", null, null);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//FindZ_INT_OrgListResponse/GCOrganization", response);
			for (int i : nodes) {
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//GCOrganization-id/ItemId", i), null);
				String orgCode = Node.getDataWithDefault(NomUtil.getNode(".//OrgCode", i), null);

				if (null != orgCode && null != itemId) {
					if (allOrganizationsIdMap.containsKey(orgCode)) {
						duplicateOrg.add(orgCode);
					} else {
						allOrganizationsIdMap.put(orgCode, itemId);
					}
				}
				populateAllOrganizationsRecordMap(i);
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}

	private void populateAllOrganizationsRecordMap(int nodeId) {
		String itemId = Node.getDataWithDefault(NomUtil.getNode(".//GCOrganization-id/ItemId", nodeId), null);
		String orgName = Node.getDataWithDefault(NomUtil.getNode(".//Name", nodeId), null);
		String costCenterId = Node.getDataWithDefault(NomUtil.getNode(".//CostCenterId", nodeId), null);
		String parentOrgItemId = Node
				.getDataWithDefault(NomUtil.getNode(".//ParentOrganization/GCOrganization-id/ItemId", nodeId), null);
		String parentorgName = Node.getDataWithDefault(NomUtil.getNode(".//ParentOrganization/Name", nodeId), null);
		OrganizationRecord organizationRecord = new OrganizationRecord();
		organizationRecord.setOrgId(itemId);
		organizationRecord.setOrgName(orgName);
		organizationRecord.setCostCenter(costCenterId);
		organizationRecord.setParentOrgId(parentOrgItemId);
		organizationRecord.setParentOrgName(parentorgName);
		if (null != orgName) {
			String key = orgName + "#" + (parentorgName != null ? parentorgName : "");
			if (!allOrganizationsRecordMap.containsKey(key)) {
				allOrganizationsRecordMap.put(key, organizationRecord);
			}
		}
	}

	public OrganizationRecord findOrganizationWithOrgName(String orgName, String parentOrgName) {
		return allOrganizationsRecordMap.get(orgName + "#" + (parentOrgName != null ? parentOrgName : ""));
	}
	
	public String getOrganizationWithOrgCode(String orgCode) {
		return getAllOrganizationsIdMap().get(orgCode);
	}

	private void loadAllContactTypesId() {

		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject typesRequest = new SOAPRequestObject(
					"http://schemas/OpenTextBasicComponents/GCContactType/operations", "GetAllContactTypes", null,
					null);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//GCContactType", response);
			for (int i : nodes) {
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//GCContactType-id/ItemId", i), null);
				String name = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);

				if (null != name && null != itemId) {
					allContactTypesIdMap.put(name, itemId);
				}
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}
}
