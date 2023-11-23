package com.opentext.apps.cc.importhandler.datamanager.landingPages;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.internal.NOMDocumentPool;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;

public class MetadataInitializer {

	public MetadataInitializer() {
		lists = new TreeMap<String,String>();
		charts = new TreeMap<String,String>();
		roles = new TreeMap<String,String>();
		roleViews = new TreeMap<String,String>();
		roleNames = new TreeMap<String,String>();
		loadAllLists();
		loadAllRoles();
		addAllRoleNames();
	}
	
	private Map<String, String> lists;
	private Map<String, String> charts;
	private Map<String, String> roles;
	private Map<String, String> roleViews;
	private Map<String, String> roleNames;
	
	private String ALL_ROLES = "Contract Author,Primary Contract Approver,Contract Manager,Contract Administrator,Executive,Contract Requester,Contract Request Approver,Legal Analyst,Legal Approver,Publisher,Account Manager,External Negotiator,Guest,Contract Approver,Obligation Escalation Performer,Obligation Performer,Obligation Creator,Obligation Manager";

	public Map<String, String> getLists() {
		if (Objects.isNull(lists)) {
			lists = new HashMap<>();
		}
		return lists;
	}

	public Map<String, String> getCharts() {
		if (Objects.isNull(charts)) {
			charts = new HashMap<>();
		}
		return charts;
	}
	
	public Map<String, String> getRoles() {
		if (Objects.isNull(roles)) {
			roles = new HashMap<>();
		}
		return roles;
	}
	
	public Map<String, String> getRoleViews() {
		if (Objects.isNull(roleViews)) {
			roleViews = new HashMap<>();
		}
		return roleViews;
	}
	
	public Map<String, String> getRoleNames() {
		if (Objects.isNull(roleNames)) {
			roleNames = new HashMap<>();
		}
		return roleNames;
	}
	
	public void addListToMap(String listCode,String itemId)
	{
		if(null != listCode && null != itemId)
			lists.put(listCode, itemId);
	}
	
	public void addRoleViewToMap(String role,String itemId)
	{
		if(null != role && null != itemId)
			roleViews.put(role, itemId);
	}
	
	public void clearRoleViewMap()
	{
			roleViews.clear();
	}


	private void loadAllLists() {
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject listsRequest = new SOAPRequestObject(
					"http://schemas/OpenTextBasicComponents/GCList/operations", "GetAllGCLists", null, null);
			response = listsRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//GCList", response);
			for (int i : nodes) {
				String name = Node.getDataWithDefault(NomUtil.getNode(".//Code", i), null);
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//GCList-id/ItemId", i), null);
				if (null != name) {
					getLists().put(name, itemId);
				}
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}

	private void loadAllRoles() {
		int response = 0;
		int nodes[] = null;
//		int packageNameNode = 0;
//		packageNameNode = NOMDocumentPool.getInstance().createElement("PackageName");
//		Node.setDataElement(packageNameNode, "", String.valueOf(ImportConstants.PACKAGENAME));
		try {
			SOAPRequestObject rolesRequest = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/cc/basiccomponents/20.2",
					"GetCCRolesInList", null, null);
			int roleNode = NOMDocumentPool.getInstance().createElement("Role");
			NomUtil.setData(roleNode, ALL_ROLES);
			response = rolesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Roles/FindAllRolesInternalResponse/Role", response);
			for (int i : nodes) {
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//Identity-id/ItemId", i), null);
				String name = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				if (null != name) {
					getRoles().put(name, itemId);
				}
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}
	
	private void addAllRoleNames() {
		getRoleNames().put("ContractAuthor", "Contract Author");
		getRoleNames().put("PrimaryContractApprover", "Primary Contract Approver");
		getRoleNames().put("ContractManager", "Contract Manager");
		getRoleNames().put("ContractAdministrator", "Contract Administrator");
		getRoleNames().put("Executive", "Executive");
		getRoleNames().put("ContractRequester", "Contract Requester");
		getRoleNames().put("ContractRequestApprover", "Contract Request Approver");
		getRoleNames().put("LegalAnalyst", "Legal Analyst");
		getRoleNames().put("LegalApprover", "Legal Approver");
		getRoleNames().put("Publisher", "Publisher");
		getRoleNames().put("AccountManager", "Account Manager");
		getRoleNames().put("ExternalNegotiator", "External Negotiator");
		getRoleNames().put("Guest", "Guest");
		getRoleNames().put("ContractApprover", "Contract Approver");
		getRoleNames().put("ObligationManager", "Obligation Manager");
		getRoleNames().put("ObligationCreator", "Obligation Creator");
		getRoleNames().put("ObligationPerformer", "Obligation Performer");
		getRoleNames().put("ObligationEscalationPerformer", "Obligation Escalation Performer");
	}
	
	protected void loadViewRoleMappings(String ItemID, String GCType) {
		int idNode =0;
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject viewRoleRequest = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/cc/basiccomponents/20.2",
					"GetViewRoleMappingsWithFilters", null, null);
			if(GCType.equals(ImportConstants.GCLIST_ENTITY_NAME))
				idNode = NOMDocumentPool.getInstance().createElement("listID");
			else if(GCType.equals(ImportConstants.GCCHART_ENTITY_NAME))
				idNode = NOMDocumentPool.getInstance().createElement("chartID");
			NomUtil.setData(idNode, ItemID.substring(ItemID.lastIndexOf(".") + 1));
			viewRoleRequest.addParameterAsXml(idNode);
			response = viewRoleRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//viewRoleMappings/FindZ_INT_ViewRoleMappingsImportResponse/GCViewRoleMapping", response);
			for (int i : nodes) {
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//GCViewRoleMapping-id/ItemId", i), null);
				String name = Node.getDataWithDefault(NomUtil.getNode(".//RelatedRole/Name", i), null);
				if (null != name) {
					getRoleViews().put(name, itemId);
				}
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}
	
}
