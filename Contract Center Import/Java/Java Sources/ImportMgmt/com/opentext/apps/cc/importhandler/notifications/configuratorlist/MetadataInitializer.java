package com.opentext.apps.cc.importhandler.notifications.configuratorlist;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importhandler.notifications.ImportConstants;
import com.opentext.apps.cc.importhandler.notifications.process.Process;

public class MetadataInitializer {

	private Map<String, Process> allProcessMap;
	private Map<String, String> allProcessIds;
	private Map<String, String> allRolesMap;
	private Map<String, String> allConfiguratorsMap;
	private Map<String, String> allConfiguratorsRolesMap;
	private Map<String, String> allConfiguratorsCCListMap;

	public MetadataInitializer() {
		allProcessMap = new HashMap<>();
		allProcessIds = new HashMap<>();
		allRolesMap = new HashMap<>();
		allConfiguratorsMap = new HashMap<>();
		allConfiguratorsRolesMap = new HashMap<>();
		allConfiguratorsCCListMap = new HashMap<>();
		loadAllProcess();
		loadNotificationProcessAllStates();
		loadNotificationProcessAllActions();
		loadNotificationProcessAllEmailTemplates();
		loadAllRoles();
		getAllConfiguratorsbyConcatString();
	}

	private void getAllConfiguratorsbyConcatString() 
	{
		int response = 0;
		int nodes[] = null;
		String registeredProcess,registeredAction,registeredTemplate,isStateTransition,registeredState,registeredFromState,registeredToState,configuratorItemID,rolesString = null, CCList = null, configuratorConcatString;
		try 
		{
			SOAPRequestObject configuratorsRequest = new SOAPRequestObject("http://schemas/OpenTextNotifications/Configurator/operations", "GetAllConfigurators", null, null);
			response = configuratorsRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Configurator", response);
			for (int i : nodes) 
			{
				registeredProcess = Node.getDataWithDefault(NomUtil.getNode(".//RegisteredProcess/Process-id/ItemId", i), null);
				registeredAction = Node.getDataWithDefault(NomUtil.getNode(".//RegisteredAction/RelatedActions-id/ItemId1", i), null);
				registeredTemplate = Node.getDataWithDefault(NomUtil.getNode(".//RegisteredTemplate/RelatedTemplates-id/ItemId1", i), null);
				isStateTransition = Node.getDataWithDefault(NomUtil.getNode(".//IsStateTransition", i), null);
				registeredState = Node.getDataWithDefault(NomUtil.getNode(".//RegisteredState/RelatedProcessState-id/ItemId1", i), null);
				registeredFromState = Node.getDataWithDefault(NomUtil.getNode(".//RegisteredFromState/RelatedProcessState-id/ItemId1", i), null);
				registeredToState = Node.getDataWithDefault(NomUtil.getNode(".//RegisteredToState/RelatedProcessState-id/ItemId1", i), null);
				configuratorItemID = Node.getDataWithDefault(NomUtil.getNode(".//Configurator-id/ItemId", i), null);
				CCList = Node.getDataWithDefault(NomUtil.getNode(".//CCList", i), null);
				if (null != configuratorItemID) {
					rolesString = getrolesConcatStringbyCongiguratorID(configuratorItemID);
				}
				configuratorConcatString = registeredProcess+ImportConstants.CONCAT_DELIMITER+
						registeredAction+ImportConstants.CONCAT_DELIMITER+
						registeredTemplate+ImportConstants.CONCAT_DELIMITER+
						isStateTransition+ImportConstants.CONCAT_DELIMITER+
						registeredState+ImportConstants.CONCAT_DELIMITER+
						registeredFromState+ImportConstants.CONCAT_DELIMITER+
						registeredToState;
				if(Objects.nonNull(rolesString) && !rolesString.isBlank()) {
					rolesString=getSortedRoles(rolesString);
				}
				if (null != configuratorConcatString && null != configuratorItemID)
				{
					allConfiguratorsMap.put(configuratorItemID, configuratorConcatString);
					allConfiguratorsRolesMap.put(configuratorItemID, rolesString);
					allConfiguratorsCCListMap.put(configuratorItemID, CCList);
				}
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


	private String getrolesConcatStringbyCongiguratorID(String configuratorItemID) 
	{
		int response = 0, itemIDNode=0;
		int nodes[] = null;
		String rolesString = "";
		Boolean firstRoleFlag = true;
		try 
		{
			SOAPRequestObject getRolesRequest = new SOAPRequestObject("http://schemas/OpenTextNotifications/Configurator/operations", "GetRegisteredRole", null, null);
			itemIDNode = NomUtil.parseXML("<Configurator-id><ItemId>"+configuratorItemID+"</ItemId></Configurator-id>");
			getRolesRequest.addParameterAsXml(itemIDNode);
			response = getRolesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Role", response);
			for (int i : nodes) 
			{
				String name = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				if (null != name)
				{
					if(firstRoleFlag)
					{
						rolesString = rolesString+name;
						firstRoleFlag = false;
					}
					else {
						rolesString = rolesString+","+name;	
					}
				}
			}
		} 
		finally
		{
			if (null != nodes) 
			{
				Utilities.cleanAll(response);
			}
		}
		return rolesString;
	}
	public String getSortedRoles(String rolestring)
	{
		StringBuilder returnString = new StringBuilder();
		if(Objects.nonNull(rolestring) && !rolestring.isBlank()) {
			Pattern PATTERN = Pattern.compile(",");
			String[] rolesArray = PATTERN.split(rolestring);
			Arrays.sort(rolesArray);
			for (String str : rolesArray)
			{
				returnString.append(str+',');
			}
		}
		return returnString.toString();
		
	}
	public Map<String, Process> getAllProcessMap()
	{
		return allProcessMap;
	}

	public Map<String, String> getAllRolesMap()
	{
		return allRolesMap;
	}
	public Map<String, String> getAllConfiguratorsMap() 
	{
		return allConfiguratorsMap;
	}
	
	public Map<String, String> getAllConfiguratorsRolesMap() 
	{
		return allConfiguratorsRolesMap;
	}
	
	public Map<String, String> getAllConfiguratorsCCListMap() 
	{
		return allConfiguratorsCCListMap;
	}
	
	private void loadAllProcess()
	{
		int response = 0;
		int nodes[] = null;
		try 
		{
			SOAPRequestObject typesRequest = new SOAPRequestObject("http://schemas/OpenTextNotifications/Process/operations", "findAllProcess", null, null);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Process", response);
			for (int i : nodes) 
			{
				String name = Node.getDataWithDefault(NomUtil.getNode(".//Title/Value", i), null);
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//Process-id/ItemId", i), null);
				Process process = new Process(itemId, name);
				if (null != name)
				{
					allProcessMap.put(name, process);
					allProcessIds.put(itemId, name);
				}
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

	private void loadNotificationProcessAllActions()
	{
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject typesRequest = new SOAPRequestObject("http://schemas/OpenTextNotifications/Process.RelatedActions/operations", "findAllRelatedActions",null, null);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RelatedActions", response);
			for (int i : nodes) 
			{
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedActions-id/ItemId", i), null);
				String actionItemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedActions-id/ItemId1", i), null);
				String actionName = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				String processName = allProcessIds.get(itemId);
				if (Objects.nonNull(processName) && processName.length() > 0)
				{
					Process process = allProcessMap.get(processName);
					if (Objects.nonNull(process) && null != actionName) 
					{
						process.getActions().put(actionName, actionItemId);
					}
				}
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

	private void loadNotificationProcessAllStates() 
	{
		int response = 0;
		int nodes[] = null;
		try 
		{
			SOAPRequestObject typesRequest = new SOAPRequestObject("http://schemas/OpenTextNotifications/Process.RelatedProcessState/operations","findAllRelatedProcessStates", null, null);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RelatedProcessState", response);
			for (int i : nodes)
			{
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedProcessState-id/ItemId", i), null);
				String stateItemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedProcessState-id/ItemId1", i),null);
				String stateName = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				String processName = allProcessIds.get(itemId);
				if (Objects.nonNull(processName) && processName.length() > 0)
				{
					Process process = allProcessMap.get(processName);
					if (Objects.nonNull(process) && null != stateName)
					{
						process.getStates().put(stateName, stateItemId);
					}
				}
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

	private void loadNotificationProcessAllEmailTemplates() 
	{
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject typesRequest = new SOAPRequestObject("http://schemas/OpenTextNotifications/Process.RelatedTemplates/operations","findAllRelatedTemplates", null, null);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RelatedTemplates", response);
			for (int i : nodes) 
			{
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedTemplates-id/ItemId", i), null);
				String templateId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedTemplates-id/ItemId1", i), null);
				String templateName = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				String processName = allProcessIds.get(itemId);
				if (Objects.nonNull(processName) && processName.length() > 0) 
				{
					Process process = allProcessMap.get(processName);
					if (Objects.nonNull(process))
					{
						process.getEmailTemplates().put(templateName, templateId);
					}
				}
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

	private void loadAllRoles() 
	{
		int response = 0, rolesNode = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject typesRequest = new SOAPRequestObject("http://schemas/OpenTextEntityIdentityComponents/Role/operations", "GetAllRoles", null, null);
			rolesNode = NomUtil.parseXML("<contains></contains>");
			typesRequest.addParameterAsXml(rolesNode);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Role", response);
			for (int i : nodes)
			{
				String roleName = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				String roleId = Node.getDataWithDefault(NomUtil.getNode("./Identity-id/ItemId", i), null);
				if (Objects.nonNull(roleName) && !roleName.isEmpty() && Objects.nonNull(roleId))
				{
					allRolesMap.put(roleName, roleId);
				}
			}
		} 
		finally
		{
			if (null != nodes)
			{
				Utilities.cleanAll(rolesNode, response);
			}
		}

	}


}