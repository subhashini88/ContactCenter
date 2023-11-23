package com.opentext.apps.cc.importhandler.notifications.configuratorlist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;
import com.opentext.apps.cc.importhandler.notifications.ImportConstants;
import com.opentext.apps.cc.importhandler.notifications.process.Process;

public class ConfiguratorRecord implements ImportListener {

	public int notificationNode;
	private final MetadataInitializer metadata;
	private final ReportItem reportItem;

	private static final CordysLogger logger = CordysLogger.getCordysLogger(ConfiguratorRecord.class);
	
	public ConfiguratorRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
		this.reportItem = reportItem;
	}

	@Override
	public void doWork(ImportEvent event) {
		this.createRequest(event);
	}

	@Override
	public void commit() {

	}

	@Override
	public void postCommit() {
	}

	@Override
	public Object getSourceId() {
		return null;
	}

	@Override
	public int getnode() {
		return this.notificationNode;
	}

	public ReportItem getReportItem() {
		return this.reportItem;
	}

	private void createRequest(ImportEvent event) {
		int configurationNode = 0, registeredProcess = 0, registeredAction = 0, registeredState = 0,
				registeredTemplate = 0, registeredToState = 0, registeredFromState = 0, configuratorItemIdResponse = 0,
				processId = 0, actionId = 0, templateId = 0, stateId = 0, fromStateId = 0, toStateId = 0;
		Map<String, String> row = event.getRow();
		try {
			if (Objects.nonNull(row)) {
				Process process = metadata.getAllProcessMap().get(row.get(ImportConstants.PROCESS_NAME));
				String createConcatString = process.getItemId() + ImportConstants.CONCAT_DELIMITER
						+ process.getActions().get(row.get(ImportConstants.ACTION)) + ImportConstants.CONCAT_DELIMITER
						+ process.getEmailTemplates().get(row.get(ImportConstants.EMAIL_TEMPLATE))
						+ ImportConstants.CONCAT_DELIMITER
						+ Boolean.parseBoolean(row.get(ImportConstants.IS_STATE_TRANSITION))
						+ ImportConstants.CONCAT_DELIMITER + process.getStates().get(row.get(ImportConstants.STATE))
						+ ImportConstants.CONCAT_DELIMITER
						+ process.getStates().get(row.get(ImportConstants.FROM_STATE))
						+ ImportConstants.CONCAT_DELIMITER + process.getStates().get(row.get(ImportConstants.TO_STATE));
				String rowRolesString = row.get(ImportConstants.ROLE);
				if(Objects.nonNull(rowRolesString) && !rowRolesString.isBlank()) {
					rowRolesString=metadata.getSortedRoles(rowRolesString);
				}
				if (!metadata.getAllConfiguratorsMap().containsValue(createConcatString)) {
					if (Objects.nonNull(process)) {
						SOAPRequestObject createConfiguratorRequest = new SOAPRequestObject(
								"http://schemas/OpenTextNotifications/Configurator/operations", "CreateConfigurator",
								null, null);
						configurationNode = NomUtil.parseXML("<Configurator-create></Configurator-create>");
						if (Objects.nonNull(process.getItemId())) {
							registeredProcess = Node.createElement("RegisteredProcess", configurationNode);
							processId = Node.createElement("Process-id", registeredProcess);
							Node.setDataElement(processId, "ItemId", process.getItemId());
						}
						if (Objects.nonNull(process.getActions().get(row.get(ImportConstants.ACTION)))) {
							registeredAction = Node.createElement("RegisteredAction", configurationNode);
							actionId = Node.createElement("RelatedActions-id", registeredAction);
							Node.setDataElement(actionId, "ItemId1",
									process.getActions().get(row.get(ImportConstants.ACTION)));
						}
						if (Objects.nonNull(process.getEmailTemplates().get(row.get(ImportConstants.EMAIL_TEMPLATE)))) {
							registeredTemplate = Node.createElement("RegisteredTemplate", configurationNode);
							templateId = Node.createElement("RelatedTemplates-id", registeredTemplate);
							Node.setDataElement(templateId, "ItemId1",
									process.getEmailTemplates().get(row.get(ImportConstants.EMAIL_TEMPLATE)));
						}
						boolean isStateTransition = Boolean.parseBoolean(row.get(ImportConstants.IS_STATE_TRANSITION));
						Node.setDataElement(configurationNode, ImportConstants.IS_STATE_TRANSITION,
								isStateTransition ? "true" : "false");
						if (isStateTransition) {
							if (Objects.nonNull(process.getStates().get(row.get(ImportConstants.FROM_STATE)))) {
								registeredFromState = Node.createElement("RegisteredFromState", configurationNode);
								fromStateId = Node.createElement("RelatedProcessState-id", registeredFromState);
								Node.setDataElement(fromStateId, "ItemId1",
										process.getStates().get(row.get(ImportConstants.FROM_STATE)));
							}
							if (Objects.nonNull(process.getStates().get(row.get(ImportConstants.TO_STATE)))) {
								registeredToState = Node.createElement("RegisteredToState", configurationNode);
								toStateId = Node.createElement("RelatedProcessState-id", registeredToState);
								Node.setDataElement(toStateId, "ItemId1",
										process.getStates().get(row.get(ImportConstants.TO_STATE)));
							}
						} else {
							if (Objects.nonNull(process.getStates().get(row.get(ImportConstants.STATE)))) {
								registeredState = Node.createElement("RegisteredState", configurationNode);
								stateId = Node.createElement("RelatedProcessState-id", registeredState);
								Node.setDataElement(stateId, "ItemId1",
										process.getStates().get(row.get(ImportConstants.STATE)));
							}
						}
						Node.setDataElement(configurationNode, ImportConstants.CC_LIST, row.get(ImportConstants.CC_LIST));
						//Node.setDataElement(configurationNode, ImportConstants.TO_LIST, row.get(ImportConstants.TO_LIST));
						Node.setDataElement(configurationNode, "CreationType", "IMPORT");
						createConfiguratorRequest.addParameterAsXml(configurationNode);
						configuratorItemIdResponse = createConfiguratorRequest.sendAndWait();
						String itemId = Node.getDataWithDefault(
								NomUtil.getNode(".//Configurator-id/ItemId", configuratorItemIdResponse), null);
						if (Objects.isNull(itemId)) {
							row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
							row.put(ImportConstants.STATUS_LOG, Node.getDataWithDefault(
									NomUtil.getNode(".//Fault/faultstring", configuratorItemIdResponse), null));
						} else {
							updateRoles(itemId, row.get(ImportConstants.ROLE));
							row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
							row.put(ImportConstants.STATUS_LOG, "Imported successfully.");
						}
					}
				} else {
					String itemId = "";
					StringBuilder rolesToAdd = new StringBuilder();
					for (Map.Entry<String, String> entry : metadata.getAllConfiguratorsMap().entrySet()) {
					    if (entry.getValue().equals(createConcatString)) {
					    	itemId = entry.getKey();
					        break;
					    }
					}
					
					//update CC list
					Set<String> rowCCListSet = getRolesSet(row.get(ImportConstants.CC_LIST), ";");
					Set<String> ConfigurationCCListSet = getRolesSet(metadata.getAllConfiguratorsCCListMap().get(itemId), ";");
					rowCCListSet.addAll(ConfigurationCCListSet);
					for (String ele : rowCCListSet) {
				    	rolesToAdd.append(ele+';');
				    }
					updateCCList(itemId, rolesToAdd.toString());
					
					//Update receiver roles
					String configurationRoles = metadata.getAllConfiguratorsRolesMap().get(itemId);
					rolesToAdd = new StringBuilder();
					Set<String> ConfugurationRolesSet = getRolesSet(configurationRoles, ",");
				    Set<String> rowRolesSet = getRolesSet(rowRolesString, ",");

				    rowRolesSet.removeAll(ConfugurationRolesSet);
				        
				    for (String ele : rowRolesSet) {
				    	rolesToAdd.append(ele+',');
				    }
					updateRoles(itemId, rolesToAdd.toString());
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
					row.put(ImportConstants.STATUS_LOG, "Imported successfully.");
				}
			}
		} catch (Exception e) {
			row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
			row.put(ImportConstants.STATUS_LOG, e.getMessage());

		} finally {
			Utilities.cleanAll(configurationNode, registeredProcess, registeredAction, registeredState,
					registeredTemplate, registeredToState, registeredFromState, configuratorItemIdResponse, processId,
					actionId, templateId, stateId, fromStateId, toStateId);
		}
	}

	private void updateRoles(String configuratorId, String roles) {
		if (Objects.nonNull(configuratorId) && !configuratorId.isEmpty() && Objects.nonNull(metadata)
				&& Objects.nonNull(roles) && !roles.isEmpty()) {
			int configurator = 0, addRoleResponse = 0;
			List<Integer> roleIds = new ArrayList<>();
			try {
				SOAPRequestObject createProcessRequest = new SOAPRequestObject(
						"http://schemas/OpenTextNotifications/Configurator/operations", "AddToRegisteredRole", null,
						null);
				configurator = NomUtil.parseXML("<Configurator-id></Configurator-id>");
				Node.setDataElement(configurator, "ItemId", configuratorId);
				createProcessRequest.addParameterAsXml(configurator);
				for (String role : roles.split(",")) {
					role = role.trim();
					if (metadata.getAllRolesMap().containsKey(role)) {
						int roleId = NomUtil.parseXML("<RegisteredRole></RegisteredRole>");
						int identyId = Node.createElement("Identity-id", roleId);
						Node.setDataElement(identyId, "ItemId", metadata.getAllRolesMap().get(role));
						createProcessRequest.addParameterAsXml(roleId);
						roleIds.add(roleId);
						roleIds.add(identyId);
					}
				}
				addRoleResponse = createProcessRequest.sendAndWait();
			} catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.notifications.configuratorlist.ConfiguratorRecord", Severity.ERROR, e,"Error while mapping the user roles with notification configuration.");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_MAPPING_ROLES);
			} finally {
				Utilities.cleanAll(configurator, addRoleResponse);
				for (int i : roleIds) {
					Utilities.cleanAll(i);
				}
			}
		}
	}
	
	private void updateCCList(String configuratorId, String CCList) {
		if (Objects.nonNull(configuratorId) && !configuratorId.isEmpty() && Objects.nonNull(metadata)
				&& Objects.nonNull(CCList) && !CCList.isEmpty()) {
			int configuratorIdNode = 0, configuratorItemIdNode = 0, configurationNode=0, configuratorItemIdResponse=0;
			try {
				SOAPRequestObject createConfiguratorRequest = new SOAPRequestObject(
						"http://schemas/OpenTextNotifications/Configurator/operations", "UpdateConfigurator",
						null, null);
				configuratorIdNode = NomUtil.parseXML("<Configurator-id></Configurator-id>");
				configuratorItemIdNode = NomUtil.parseXML("<ItemId></ItemId>");
				NomUtil.setData(configuratorItemIdNode, configuratorId);
				NomUtil.appendChild(configuratorItemIdNode,configuratorIdNode);
				configurationNode = NomUtil.parseXML("<Configurator-update></Configurator-update>");
				Node.setDataElement(configurationNode, ImportConstants.CC_LIST, CCList);
				createConfiguratorRequest.addParameterAsXml(configuratorIdNode);
				createConfiguratorRequest.addParameterAsXml(configurationNode);
				configuratorItemIdResponse = createConfiguratorRequest.sendAndWait();
			} catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.notifications.configuratorlist.ConfiguratorRecord", Severity.ERROR, e,"Error while updating the configurator with CCLIST");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_TRIGGERING_WEBSERVICE);
			} finally {
				Utilities.cleanAll(configuratorIdNode,configuratorItemIdNode,configurationNode,configuratorItemIdResponse);
			}
		}
	}
	
	private HashSet<String> getRolesSet(String roles, String delimeter) {
		HashSet<String> rolesSet= new HashSet<String>();
		
		if (Objects.nonNull(roles) && !roles.isEmpty()) {
			 String[] arrayOfRoles = roles.split(delimeter);
		        for (String str : arrayOfRoles) {
		            rolesSet.add(str.trim());
		        }
		}
		
		return rolesSet;
	}
}
