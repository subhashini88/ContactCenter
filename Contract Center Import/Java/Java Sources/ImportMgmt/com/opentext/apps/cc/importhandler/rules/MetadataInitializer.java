package com.opentext.apps.cc.importhandler.rules;

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
	public Map<String, String> gcprocessMap = new HashMap<String, String>();
	public Map<String, String> gcprocessActionsMap = new HashMap<String, String>();
	public Map<String, String> gcStateMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> orgMap = new HashMap<String, String>();
	public List<String> duplicateOrgs = new ArrayList<String>();
	public Map<String, String> tasklistMap = new HashMap<String, String>();
	public Map<String, String> tasklistIdMap = new HashMap<String, String>();
	public List<String> duplicateTasklists = new ArrayList<String>();
	public Map<String, String> contracttypeMap = new HashMap<String, String>();
	public Map<String, String> contractpropertyMap = new HashMap<String, String>();
	public Map<String, String> contractpropertyIdMap = new HashMap<String, String>();
	public static HashMap<String,List<String>> propertyOperators=new HashMap<String,List<String>>();
	

	public MetadataInitializer() {
		getAllGCProcesses();
		getAllGCProcessActions();
		getAllGCStates();
		getAllOrganizations();
		getAllTasklists();
		getAllContracttypes();
		getAllContractProperties();
		AddValues();
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
					"http://schemas/OpenTextBasicComponents/GCProcess.RelatedGCActions/operations", "GetAllGCProcessActions", null, null);
			response = configuratorsRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RelatedGCActions", response);
			String gcActionId, gcActionName;
			for (int i : nodes) {
				gcActionId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedGCActions-id/Id1", i), null);
				gcActionName = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				gcprocessActionsMap.put(gcActionName, gcActionId);
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
					"http://schemas/OpenTextBasicComponents/GCProcess.RelatedGCState/operations", "GetGCStatesByProcessIDAndPurpose", null, null);
			int purpose = NomUtil.parseXML("<purpose>" + "CTRRULE;"  + "</purpose>");
			int processId = NomUtil.parseXML("<processID>" + gcprocessMap.get("Contract") + "</processID>");
			configuratorsRequest.addParameterAsXml(purpose);
			configuratorsRequest.addParameterAsXml(processId);
			response = configuratorsRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RelatedGCState", response);
			String gcActionId, gcActionName;
			for (int i : nodes) {
				gcActionId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedGCState-id/Id1", i), null);
				gcActionName = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				gcStateMap.put(gcActionName, gcActionId);
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}

	private void getAllOrganizations() {
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject configuratorsRequest = new SOAPRequestObject(
					"http://schemas/OpenTextBasicComponents/GCOrganization/operations", "GetAllOrgs", null, null);
			response = configuratorsRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//GCOrganization", response);
			String OrganizationItemId, OrganizationCode;
			for (int i : nodes) {
				OrganizationItemId = Node.getDataWithDefault(NomUtil.getNode(".//GCOrganization-id/ItemId", i), null);
				OrganizationCode = Node.getDataWithDefault(NomUtil.getNode(".//OrgCode", i), null);
				if(orgMap.containsKey(OrganizationCode)) {
					duplicateOrgs.add(OrganizationCode);
				}else {
				orgMap.put(OrganizationCode, OrganizationItemId);
				}
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}

	private void getAllTasklists() {
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject configuratorsRequest = new SOAPRequestObject(
					"http://schemas/OpenTextCCConfigurableWorkflow/ActivityList/operations", "GetAllActivityLists", null,
					null);
			response = configuratorsRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//ActivityList", response);
			String ActivityListItemId, ActivityListId, ActivityListName;
			for (int i : nodes) {
				ActivityListItemId = Node.getDataWithDefault(NomUtil.getNode(".//ActivityList-id/ItemId", i), null);
				ActivityListName = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				ActivityListId = Node.getDataWithDefault(NomUtil.getNode(".//ActivityList-id/Id", i), null);
				if(tasklistMap.containsKey(ActivityListName)) {
					duplicateTasklists.add(ActivityListName);
					tasklistIdMap.put(ActivityListId,ActivityListItemId);
				}else {
				tasklistMap.put(ActivityListName, ActivityListItemId);
				tasklistIdMap.put(ActivityListId,ActivityListItemId);
				}
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}

	private void getAllContracttypes() {
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject configuratorsRequest = new SOAPRequestObject(
					"http://schemas/OpenTextBasicComponents/GCType/operations", "GetAllTypes", null, null);
			response = configuratorsRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//GCType", response);
			String GCTypeItemId, GCTypeName;
			for (int i : nodes) {
				GCTypeItemId = Node.getDataWithDefault(NomUtil.getNode(".//GCType-id/Id", i), null);
				GCTypeName = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				contracttypeMap.put(GCTypeName, GCTypeItemId);
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}

	private void getAllContractProperties() {
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject configuratorsRequest = new SOAPRequestObject(
					"http://schemas/OpenTextCCConfigurableWorkflow/RuleConditionOperand/operations",
					"GetAllRuleConditionOperands", null, null);
			response = configuratorsRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RuleConditionOperand", response);
			String displayName, dataType,operandId;
			for (int i : nodes) {
				displayName = Node.getDataWithDefault(NomUtil.getNode(".//DisplayName", i), null);
				dataType = Node.getDataWithDefault(NomUtil.getNode(".//DataType", i), null);
				operandId = Node.getDataWithDefault(NomUtil.getNode(".//RuleConditionOperand-id/ItemId", i), null);
				displayName=displayName.replace(" "," ").trim();
				contractpropertyMap.put(displayName, dataType);
				contractpropertyIdMap.put(displayName,operandId);
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}
	
	public static void AddValues()
	{
		
		List<String> booleanlist=new ArrayList<String>();
		booleanlist.add("EQUALTO");
		booleanlist.add("NOTEQUALTO");
		List<String> Datelist=new ArrayList<String>();
		Datelist.add("EQUALTO");Datelist.add("NOTEQUALTO");Datelist.add("GREATERTHAN");Datelist.add("GREATERTHANOREQUALTO");
		Datelist.add("LESSTHAN");Datelist.add("LESSTHANOREQUALTO");Datelist.add("EMPTY");Datelist.add("NOTEMPTY");
		List<String> Numberlist=new ArrayList<String>();
		Numberlist.add("EQUALTO");Numberlist.add("NOTEQUALTO");Numberlist.add("GREATERTHAN");Numberlist.add("GREATERTHANOREQUALTO");
		Numberlist.add("LESSTHAN");Numberlist.add("LESSTHANOREQUALTO");Numberlist.add("EMPTY");Numberlist.add("NOTEMPTY");
		List<String> Decimallist=new ArrayList<String>();
		Decimallist.add("EQUALTO");Decimallist.add("NOTEQUALTO");Decimallist.add("GREATERTHAN");Decimallist.add("GREATERTHANOREQUALTO");
		Decimallist.add("LESSTHAN");Decimallist.add("LESSTHANOREQUALTO");Decimallist.add("EMPTY");Decimallist.add("NOTEMPTY");
		List<String> EnumeratedTextlist=new ArrayList<String>();
		EnumeratedTextlist.add("EQUALTO");EnumeratedTextlist.add("NOTEQUALTO");EnumeratedTextlist.add("EMPTY");EnumeratedTextlist.add("NOTEMPTY");
		List<String> Textlist=new ArrayList<String>();
		Textlist.add("EQUALTO");Textlist.add("NOTEQUALTO");Textlist.add("CONTAINS");Textlist.add("EMPTY");Textlist.add("NOTEMPTY");
		
		propertyOperators.put("BOOLEAN",booleanlist);
		propertyOperators.put("DATE",Datelist);
		propertyOperators.put("NUMBER",Numberlist);
		propertyOperators.put("DECIMAL",Decimallist);
		propertyOperators.put("DURATION",Decimallist);
		propertyOperators.put("LONGTEXT",Textlist);
		propertyOperators.put("TEXT",Textlist);
		propertyOperators.put("ENUMERATEDTEXT",EnumeratedTextlist);
		
	}
}
