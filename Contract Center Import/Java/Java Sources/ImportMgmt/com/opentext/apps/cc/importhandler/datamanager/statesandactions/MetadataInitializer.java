package com.opentext.apps.cc.importhandler.datamanager.statesandactions;

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
		loadAllProcess();
		loadAllProcessStates();
		loadAllProcessActions();
		loadAllProcessProperties();
	}

	private Map<String, Process> allProcessMap;
	private Map<String, String> allProcessIds;

	public Map<String, Process> getAllProcessMap() {
		return allProcessMap;
	}

	public Map<String, String> getAllProcessIdsMap() {
		return allProcessIds;
	}

	private void loadAllProcess() {
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject typesRequest = new SOAPRequestObject(
					"http://schemas/OpenTextBasicComponents/GCProcess/operations", "GetAllGCProcess", null, null);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//GCProcess", response);
			for (int i : nodes) {
				String name = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//GCProcess-id/ItemId", i), null);
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

	private void loadAllProcessActions() {
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject typesRequest = new SOAPRequestObject(
					"http://schemas/OpenTextBasicComponents/GCProcess.RelatedGCActions/operations",
					"GetAllGCProcessActions", null, null);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RelatedGCActions", response);
			for (int i : nodes) {
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedGCActions-id/ItemId", i), null);
				String actionItemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedGCActions-id/ItemId1", i),
						null);
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

	private void loadAllProcessStates() {
		int response = 0;
		int nodes[] = null;
		try {

			SOAPRequestObject typesRequest = new SOAPRequestObject(
					"http://schemas/OpenTextBasicComponents/GCProcess.RelatedGCState/operations",
					"GetAllGCProcessStates", null, null);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RelatedGCState", response);
			for (int i : nodes) {
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedGCState-id/ItemId", i), null);
				String stateItemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedGCState-id/ItemId1", i), null);
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

	private void loadAllProcessProperties() {
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject typesRequest = new SOAPRequestObject(
					"http://schemas/OpenTextBasicComponents/GCProcess.RelatedGCProps/operations",
					"GetAllGCProcessProps", null, null);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RelatedGCProps", response);
			for (int i : nodes) {
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedGCProps-id/ItemId", i), null);
				String propItemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedGCProps-id/ItemId1", i), null);
				String displayName = Node.getDataWithDefault(NomUtil.getNode(".//DisplayName", i), null);
				String processName = allProcessIds.get(itemId);
				if (Objects.nonNull(processName) && processName.length() > 0) {
					Process process = allProcessMap.get(processName);
					if (Objects.nonNull(process)) {
						process.getProperties().put(displayName, propItemId);
					}
				}
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}
}
