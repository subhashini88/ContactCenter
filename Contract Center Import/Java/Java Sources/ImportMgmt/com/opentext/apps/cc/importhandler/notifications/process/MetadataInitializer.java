package com.opentext.apps.cc.importhandler.notifications.process;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;

public class MetadataInitializer {

	public MetadataInitializer() {
		allProcessMap = new HashMap<>();
		allProcessIds = new HashMap<>();
		allRolesMap = new HashMap<>();
		loadAllProcess();
		loadNotificationProcessAllStates();
		loadNotificationProcessAllActions();
		loadNotificationProcessAllEmailTemplates();
		loadNotificationProcessAllRoles();
		loadAllRoles();
	}

	private Map<String, Process> allProcessMap;
	private Map<String, String> allProcessIds;
	private Map<String, String> allRolesMap;

	public Map<String, Process> getAllProcessMap() {
		return allProcessMap;
	}

	public Map<String, String> getAllRolesMap() {
		return allRolesMap;
	}

	private void loadAllProcess() {
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject typesRequest = new SOAPRequestObject(
					"http://schemas/OpenTextNotifications/Process/operations", "findAllProcess", null, null);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Process", response);
			for (int i : nodes) {
				String name = Node.getDataWithDefault(NomUtil.getNode(".//Title/Value", i), null);
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//Process-id/ItemId", i), null);
				Process process = new Process(itemId, name);
				if (null != name) {
					allProcessMap.put(name, process);
					allProcessIds.put(itemId, name);
				}
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}

	private void loadNotificationProcessAllActions() {
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject typesRequest = new SOAPRequestObject(
					"http://schemas/OpenTextNotifications/Process.RelatedActions/operations", "findAllRelatedActions",
					null, null);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RelatedActions", response);
			for (int i : nodes) {
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedActions-id/ItemId", i), null);
				String actionItemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedActions-id/ItemId1", i), null);
				String actionName = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				String processName = allProcessIds.get(itemId);
				if (Objects.nonNull(processName) && processName.length() > 0) {
					Process process = allProcessMap.get(processName);
					if (Objects.nonNull(process)) {
						process.getActions().put(actionName, actionItemId);
					}
				}
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}

	private void loadNotificationProcessAllStates() {
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject typesRequest = new SOAPRequestObject(
					"http://schemas/OpenTextNotifications/Process.RelatedProcessState/operations",
					"findAllRelatedProcessStates", null, null);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RelatedProcessState", response);
			for (int i : nodes) {
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedProcessState-id/ItemId", i), null);
				String stateItemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedProcessState-id/ItemId1", i),
						null);
				String stateName = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				String processName = allProcessIds.get(itemId);
				if (Objects.nonNull(processName) && processName.length() > 0) {
					Process process = allProcessMap.get(processName);
					if (Objects.nonNull(process)) {
						process.getStates().put(stateName, stateItemId);
					}
				}
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}

	private void loadNotificationProcessAllEmailTemplates() {
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject typesRequest = new SOAPRequestObject(
					"http://schemas/OpenTextNotifications/Process.RelatedTemplates/operations",
					"findAllRelatedTemplates", null, null);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RelatedTemplates", response);
			for (int i : nodes) {
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedTemplates-id/ItemId", i), null);
				String templateId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedTemplates-id/ItemId1", i), null);
				String templateName = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				String processName = allProcessIds.get(itemId);
				if (Objects.nonNull(processName) && processName.length() > 0) {
					Process process = allProcessMap.get(processName);
					if (Objects.nonNull(process)) {
						process.getEmailTemplates().put(templateName, templateId);
					}
				}
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}
	private void loadNotificationProcessAllRoles() {
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject typesRequest = new SOAPRequestObject(
					"http://schemas.cordys.com/apps/notifications/18.4",
					"getAllRelatedProcessRolesList", null, null);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Response/FindZ_INT_ProcessRelatedRolesResponse/RelatedProcessRoles", response);
			for (int i : nodes) {
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedProcessRoles-id/ItemId", i), null);
				String roleId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedProcessRoles-id/ItemId1", i), null);
				String roleName = Node.getDataWithDefault(NomUtil.getNode(".//RelatedRole/Name", i), null);
				String processName = allProcessIds.get(itemId);
				if (Objects.nonNull(processName) && processName.length() > 0) {
					Process process = allProcessMap.get(processName);
					if (Objects.nonNull(process)) {
						process.getRoles().put(roleName, roleId);
					}
				}
			}
		}finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}

	private void loadAllRoles() {
		int response = 0, rolesNode = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject typesRequest = new SOAPRequestObject(
					"http://schemas/OpenTextEntityIdentityComponents/Role/operations", "GetAllRoles", null, null);
			rolesNode = NomUtil.parseXML("<contains></contains>");
			typesRequest.addParameterAsXml(rolesNode);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Role", response);
			for (int i : nodes) {
				String roleName = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				String roleId = Node.getDataWithDefault(NomUtil.getNode("./Identity-id/ItemId", i), null);
				if (Objects.nonNull(roleName) && !roleName.isEmpty() && Objects.nonNull(roleId)) {
					allRolesMap.put(roleName, roleId);
				}
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(rolesNode, response);
			}
		}

	}

}
