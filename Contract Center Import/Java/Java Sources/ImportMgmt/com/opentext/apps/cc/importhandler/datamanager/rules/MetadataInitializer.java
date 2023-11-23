package com.opentext.apps.cc.importhandler.datamanager.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;

public class MetadataInitializer {

	public Map<String, String> gcprocessMap = new HashMap<String, String>();
	private Map<String, String> purposeMap = new HashMap<String, String>();
	protected HashMap<String, String> duplicateRules = new HashMap<String, String>();
	private Map<String, HashMap<String, String>> allprocessstates = new HashMap<String, HashMap<String, String>>();
	private Map<String, HashMap<String, String>> allprocessactions = new HashMap<String, HashMap<String, String>>();
	private HashMap<String, String> allRulesList = new HashMap<String, String>();

	public MetadataInitializer() {
		getAllGCProcesses();
		getAllGCProcessActions();
		getAllGCStates();
		getRulesWithFilters();
		purposeMap.put("Contract", "CTRRULE;");
		purposeMap.put("Clause", "CLARULE;");
		purposeMap.put("Template", "TEMRULE;");

	}

	public String getRelatedStateItemId1(String process, String stateName) {
		if (Objects.nonNull(gcprocessMap.get(process)) && allprocessstates.containsKey(gcprocessMap.get(process))) {
			return allprocessstates.get(gcprocessMap.get(process)).get(stateName);
		}
		return null;
	}

	public String getRelatedActionItemId1(String process, String actionName) {
		if (Objects.nonNull(gcprocessMap.get(process)) && allprocessactions.containsKey(gcprocessMap.get(process))) {
			return allprocessactions.get(gcprocessMap.get(process)).get(actionName);
		}
		return null;
	}

	public String getRuleByCodeName(String code) {
		if (allRulesList.containsKey(code)) {
			return allRulesList.get(code);
		}
		return null;
	}

	private void getAllGCProcesses() {
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject configuratorsRequest = new SOAPRequestObject(
					"http://schemas/OpenTextBasicComponents/GCProcess/operations", "GetAllGCProcess", null, null);
			response = configuratorsRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//GCProcess", response);
			String gcprocessId, gcprocessName;
			for (int i : nodes) {
				gcprocessId = Node.getDataWithDefault(NomUtil.getNode(".//GCProcess-id/Id", i), null);
				gcprocessName = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				gcprocessMap.put(gcprocessName, gcprocessId);
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}

	private void getAllGCProcessActions() {
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject configuratorsRequest = new SOAPRequestObject(
					"http://schemas/OpenTextBasicComponents/GCProcess.RelatedGCActions/operations",
					"GetAllGCProcessActions", null, null);
			response = configuratorsRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RelatedGCActions", response);
			String gcActionItemId1, gcActionName, processId;
			for (int i : nodes) {
				gcActionItemId1 = Node.getDataWithDefault(NomUtil.getNode(".//RelatedGCActions-id/ItemId1", i), null);
				gcActionName = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				processId = Node.getDataWithDefault(NomUtil.getNode(".//OwnerGCProcess/GCProcess-id/Id", i), null);
				if (Objects.nonNull(allprocessactions.get(processId))) {
					allprocessactions.get(processId).put(gcActionName, gcActionItemId1);
				} else {
					HashMap<String, String> temp = new HashMap<String, String>();
					temp.put(gcActionName, gcActionItemId1);
					allprocessactions.put(processId, temp);
				}
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}

	private void getAllGCStates() {
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject configuratorsRequest = new SOAPRequestObject(
					"http://schemas/OpenTextBasicComponents/GCProcess.RelatedGCState/operations",
					"GetAllGCProcessStates", null, null);
			response = configuratorsRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RelatedGCState", response);
			String gcStateItemId1, gcStateName, processId;
			for (int i : nodes) {
				gcStateItemId1 = Node.getDataWithDefault(NomUtil.getNode(".//RelatedGCState-id/ItemId1", i), null);
				gcStateName = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				processId = Node.getDataWithDefault(NomUtil.getNode(".//OwnerGCProcess/GCProcess-id/Id", i), null);
				if (Objects.nonNull(allprocessstates.get(processId))) {
					allprocessstates.get(processId).put(gcStateName, gcStateItemId1);
				} else {
					HashMap<String, String> temp = new HashMap<String, String>();
					temp.put(gcStateName, gcStateItemId1);
					allprocessstates.put(processId, temp);
				}
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}

	private void getRulesWithFilters() {
		int creationTypeNode = 0;
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject getDefaultRulesRequest = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/cc/configworkflow/20.2", "GetRulesWithFilters ", null, null);
			creationTypeNode = NomUtil.parseXML("<creationTypeFilter>DEFAULT,DEFAULT-IMPORTED</creationTypeFilter>");
			getDefaultRulesRequest.addParameterAsXml(creationTypeNode);
			response = getDefaultRulesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//FindZ_INT_RulesListResponse/Rule", response);
			String ruleItemId, ruleCode;
			for (int i : nodes) {
				// ruleName = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				ruleItemId = Node.getDataWithDefault(NomUtil.getNode(".//Rule-id/ItemId", i), null);
				ruleCode = Node.getDataWithDefault(NomUtil.getNode(".//Code", i), null);
				if (null != ruleCode) {
					allRulesList.put(ruleCode, ruleItemId);
				}
			}
			if (Objects.nonNull(allRulesList)) {
				duplicateRules = (HashMap<String, String>) allRulesList.clone();
			}

		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}

}
