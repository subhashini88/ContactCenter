package com.opentext.apps.cc.importhandler.notifications.process;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.notifications.ImportConstants;

public class ProcessRecord implements ImportListener {

	public int processNode;
	private final MetadataInitializer metadata;
	private final ReportItem reportItem;
	private ProcessItemType processItemType;

	public ProcessRecord(MetadataInitializer metadata, ReportItem reportItem, ProcessItemType processItemType) {
		this.metadata = metadata;
		this.reportItem = reportItem;
		this.processItemType = processItemType;
	}

	@Override
	public void doWork(ImportEvent event) {
		switch (processItemType) {
		case STATE:
			createRelatedStateIfnotExists(event);
			break;
		case ACTION:
			createRelatedActionIfnotExists(event);
			break;
		case EMAIL_TEMPLATE:
			createRelatedTemplateIfnotExists(event);
			break;
		case ROLE:
			createRelatedRolesIfnotExists(event);
			break;
		}
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
		return this.processNode;
	}

	public ReportItem getReportItem() {
		return this.reportItem;
	}

	public int getProcessNode() {
		return processNode;
	}

	private void createRelatedActionIfnotExists(ImportEvent event) {
		int processIdNode = 0, processItemIdResponse = 0, relatedActionNode = 0;
		Map<String, String> row = event.getRow();
		Process process = metadata.getAllProcessMap().get(row.get(ImportConstants.PROCESS_NAME));
		try {
			if (!process.getActions().containsKey(row.get(ImportConstants.ACTION))) {
				processIdNode = NomUtil.parseXML("<Process-id></Process-id>");
				Node.setDataElement(processIdNode, "ItemId", process.getItemId());
				relatedActionNode = NomUtil.parseXML("<RelatedActions-create></RelatedActions-create>");
				Node.setDataElement(relatedActionNode, "Name", row.get(ImportConstants.ACTION));
				if(!Objects.isNull(row.get(ImportConstants.MESSAGE_FORMAT)))
				Node.setDataElement(relatedActionNode, "MessageFormat", row.get(ImportConstants.MESSAGE_FORMAT));
				SOAPRequestObject createProcessRequest = new SOAPRequestObject(
						"http://schemas/OpenTextNotifications/Process/operations", "CreateRelatedActions", null, null);
				createProcessRequest.addParameterAsXml(processIdNode);
				createProcessRequest.addParameterAsXml(relatedActionNode);
				processItemIdResponse = createProcessRequest.sendAndWait();
				String itemId = Node.getDataWithDefault(
						NomUtil.getNode(".//RelatedActions/RelatedActions-id/ItemId", processItemIdResponse), null);
				if (Objects.isNull(itemId)) {
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
					row.put(ImportConstants.STATUS_LOG, Node
							.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", processItemIdResponse), null));
				} else {
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
					row.put(ImportConstants.STATUS_LOG, "Action mapped to Process successfully.");
				}
			} else {
				
				updateRelatedActionIfExists(process, row);
				
			}
		} catch (Exception e) {
			row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
			row.put(ImportConstants.STATUS_LOG, e.getMessage());
		} finally {
			Utilities.cleanAll(processIdNode, processItemIdResponse);
		}
	}
	
	private void updateRelatedActionIfExists(Process process, Map<String, String> row ) {
		
		int actionIdNode=0, relatedActionNode=0, processItemIdResponse=0;
		try {
		actionIdNode = NomUtil.parseXML("<RelatedActions-id></RelatedActions-id>");
		Node.setDataElement(actionIdNode, "ItemId1", process.getActions().get(row.get(ImportConstants.ACTION)));
		relatedActionNode = NomUtil.parseXML("<RelatedActions-update></RelatedActions-update>");
		Node.setDataElement(relatedActionNode, "Name", row.get(ImportConstants.ACTION));
		if(!Objects.isNull(row.get(ImportConstants.MESSAGE_FORMAT)))
		Node.setDataElement(relatedActionNode, "MessageFormat", row.get(ImportConstants.MESSAGE_FORMAT));
		SOAPRequestObject updateProcessRequest = new SOAPRequestObject(
				"http://schemas/OpenTextNotifications/Process.RelatedActions/operations", "UpdateRelatedActions", null, null);
		updateProcessRequest.addParameterAsXml(actionIdNode);
		updateProcessRequest.addParameterAsXml(relatedActionNode);
		processItemIdResponse = updateProcessRequest.sendAndWait();
		String itemId = Node.getDataWithDefault(
				NomUtil.getNode(".//RelatedActions/RelatedActions-id/ItemId", processItemIdResponse), null);
		if (Objects.isNull(itemId)) {
			row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
			row.put(ImportConstants.STATUS_LOG, Node
					.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", processItemIdResponse), null));
		} else {
			row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
			row.put(ImportConstants.STATUS_LOG, "Action Updated to Process successfully.");
		}
		} finally {
			Utilities.cleanAll(actionIdNode, relatedActionNode, processItemIdResponse);
		}
	}

	private void createRelatedStateIfnotExists(ImportEvent event) {
		int processIdNode = 0, processItemIdResponse = 0, relatedActionNode = 0;
		Map<String, String> row = event.getRow();
		Process process = metadata.getAllProcessMap().get(row.get(ImportConstants.PROCESS_NAME));
		try {
			if (!process.getStates().containsKey(row.get(ImportConstants.STATE))) {
				processIdNode = NomUtil.parseXML("<Process-id></Process-id>");
				Node.setDataElement(processIdNode, "ItemId", process.getItemId());
				relatedActionNode = NomUtil.parseXML("<RelatedProcessState-create></RelatedProcessState-create>");
				Node.setDataElement(relatedActionNode, "Name", row.get(ImportConstants.STATE));
				if(!Objects.isNull(row.get(ImportConstants.MESSAGE_FORMAT)))
					Node.setDataElement(relatedActionNode, "MessageFormat", row.get(ImportConstants.MESSAGE_FORMAT));
				SOAPRequestObject createProcessRequest = new SOAPRequestObject(
						"http://schemas/OpenTextNotifications/Process/operations", "CreateRelatedProcessState", null,
						null);
				createProcessRequest.addParameterAsXml(processIdNode);
				createProcessRequest.addParameterAsXml(relatedActionNode);
				processItemIdResponse = createProcessRequest.sendAndWait();
				String itemId = Node.getDataWithDefault(
						NomUtil.getNode(".//RelatedProcessState/RelatedProcessState-id/ItemId", processItemIdResponse),
						null);
				if (Objects.isNull(itemId)) {
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
					row.put(ImportConstants.STATUS_LOG, Node
							.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", processItemIdResponse), null));
				} else {
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
					row.put(ImportConstants.STATUS_LOG, "State mapped to Process successfully.");
				}
			} else {
				
				updateRelatedStateIfExists(process, row);
			}
		} catch (Exception e) {
			row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
			row.put(ImportConstants.STATUS_LOG, e.getMessage());
		} finally {
			Utilities.cleanAll(processIdNode, processItemIdResponse);
		}
	}
	
	private void updateRelatedStateIfExists(Process process, Map<String, String> row) {
		
		int stateIdNode=0, relatedStateNode=0, processItemIdResponse=0;
		try {
			stateIdNode = NomUtil.parseXML("<RelatedProcessState-id></RelatedProcessState-id>");
			Node.setDataElement(stateIdNode, "ItemId1", process.getStates().get(row.get(ImportConstants.STATE)));
			relatedStateNode = NomUtil.parseXML("<RelatedProcessState-update></RelatedProcessState-update>");
			Node.setDataElement(relatedStateNode, "Name", row.get(ImportConstants.STATE));
			if(!Objects.isNull(row.get(ImportConstants.MESSAGE_FORMAT)))
				Node.setDataElement(relatedStateNode, "MessageFormat", row.get(ImportConstants.MESSAGE_FORMAT));
			SOAPRequestObject updateProcessRequest = new SOAPRequestObject(
					"http://schemas/OpenTextNotifications/Process.RelatedProcessState/operations", "UpdateRelatedProcessState", null,
					null);
			updateProcessRequest.addParameterAsXml(stateIdNode);
			updateProcessRequest.addParameterAsXml(relatedStateNode);
			processItemIdResponse = updateProcessRequest.sendAndWait();
			String itemId = Node.getDataWithDefault(
					NomUtil.getNode(".//RelatedProcessState/RelatedProcessState-id/ItemId", processItemIdResponse),
					null);
			if (Objects.isNull(itemId)) {
				row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
				row.put(ImportConstants.STATUS_LOG, Node
						.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", processItemIdResponse), null));
			} else {
				row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
				row.put(ImportConstants.STATUS_LOG, "State updated to Process successfully.");
			}
		} finally {
			Utilities.cleanAll(stateIdNode, relatedStateNode, processItemIdResponse);
		}
	}

	private void createRelatedTemplateIfnotExists(ImportEvent event) {
		int processIdNode = 0, processItemIdResponse = 0, relatedActionNode = 0;
		Map<String, String> row = event.getRow();
		Process process = metadata.getAllProcessMap().get(row.get(ImportConstants.PROCESS_NAME));
		try {
			if (!process.getEmailTemplates().containsKey(row.get(ImportConstants.EMAIL_TEMPLATE))) {
				processIdNode = NomUtil.parseXML("<Process-id></Process-id>");
				Node.setDataElement(processIdNode, "ItemId", process.getItemId());
				relatedActionNode = NomUtil.parseXML("<RelatedTemplates-create></RelatedTemplates-create>");
				Node.setDataElement(relatedActionNode, "Name", row.get(ImportConstants.EMAIL_TEMPLATE));
				if (Objects.nonNull(row.get(ImportConstants.EMAIL_TEMPLATE_DISPLAYNAME)) && !row.get(ImportConstants.EMAIL_TEMPLATE_DISPLAYNAME).isEmpty()) {
				Node.setDataElement(relatedActionNode, "TemplateDisplayName", row.get(ImportConstants.EMAIL_TEMPLATE_DISPLAYNAME));
				}
				else {
					Node.setDataElement(relatedActionNode, "TemplateDisplayName", row.get(ImportConstants.EMAIL_TEMPLATE));
				}
				SOAPRequestObject createProcessRequest = new SOAPRequestObject(
						"http://schemas/OpenTextNotifications/Process/operations", "CreateRelatedTemplates", null,
						null);
				createProcessRequest.addParameterAsXml(processIdNode);
				createProcessRequest.addParameterAsXml(relatedActionNode);
				processItemIdResponse = createProcessRequest.sendAndWait();
				String itemId = Node.getDataWithDefault(
						NomUtil.getNode(".//RelatedTemplates/RelatedTemplates-id/ItemId", processItemIdResponse), null);
				if (Objects.isNull(itemId)) {
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
					row.put(ImportConstants.STATUS_LOG, Node
							.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", processItemIdResponse), null));
				} else {
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
					row.put(ImportConstants.STATUS_LOG, "Email Template mapped to Process successfully.");
				}
			} else {
				
				updateRelatedTemplateIfExists(process, row);
			}
		} catch (Exception e) {
			row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
			row.put(ImportConstants.STATUS_LOG, e.getMessage());
		} finally {
			Utilities.cleanAll(processIdNode, processItemIdResponse);
		}
	}
	
	private void updateRelatedTemplateIfExists(Process process, Map<String, String> row) {
		
		int templateIdNode=0, relatedTemplateNode=0, processItemIdResponse=0;
		try {
			templateIdNode = NomUtil.parseXML("<RelatedTemplates-id></RelatedTemplates-id>");
			Node.setDataElement(templateIdNode, "ItemId1", process.getEmailTemplates().get(row.get(ImportConstants.EMAIL_TEMPLATE)));
			relatedTemplateNode = NomUtil.parseXML("<RelatedTemplates-update></RelatedTemplates-update>");
			Node.setDataElement(relatedTemplateNode, "Name", row.get(ImportConstants.EMAIL_TEMPLATE));
			if (Objects.nonNull(row.get(ImportConstants.EMAIL_TEMPLATE_DISPLAYNAME)) && !row.get(ImportConstants.EMAIL_TEMPLATE_DISPLAYNAME).isEmpty()) {
			Node.setDataElement(relatedTemplateNode, "TemplateDisplayName", row.get(ImportConstants.EMAIL_TEMPLATE_DISPLAYNAME));
			}
			else {
				Node.setDataElement(relatedTemplateNode, "TemplateDisplayName", row.get(ImportConstants.EMAIL_TEMPLATE));
			}
			SOAPRequestObject updateProcessRequest = new SOAPRequestObject(
					"http://schemas/OpenTextNotifications/Process.RelatedTemplates/operations", "UpdateRelatedTemplates", null,
					null);
			updateProcessRequest.addParameterAsXml(templateIdNode);
			updateProcessRequest.addParameterAsXml(relatedTemplateNode);
			processItemIdResponse = updateProcessRequest.sendAndWait();
			String itemId = Node.getDataWithDefault(
					NomUtil.getNode(".//RelatedTemplates/RelatedTemplates-id/ItemId", processItemIdResponse), null);
			if (Objects.isNull(itemId)) {
				row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
				row.put(ImportConstants.STATUS_LOG, Node
						.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", processItemIdResponse), null));
			} else {
				row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
				row.put(ImportConstants.STATUS_LOG, "Email Template updated to Process successfully.");
			}
		}finally {
			Utilities.cleanAll(templateIdNode, relatedTemplateNode, processItemIdResponse);
		}
	}
	
	private void createRelatedRolesIfnotExists(ImportEvent event) {
	int processIdNode = 0, processItemIdResponse = 0,relatedRoleCreateNode=0;
	Map<String, String> row = event.getRow();
	Process process = metadata.getAllProcessMap().get(row.get(ImportConstants.PROCESS_NAME));
	try {
		if (!process.getRoles().containsKey(row.get(ImportConstants.PROCESS_ROLE))) {
			processIdNode = NomUtil.parseXML("<Process-id><ItemId>"+process.getItemId()+"</ItemId></Process-id>");
			relatedRoleCreateNode = NomUtil.parseXML("<RelatedProcessRoles-create><RelatedRole><Identity-id><ItemId>"+metadata.getAllRolesMap().get(row.get(ImportConstants.PROCESS_ROLE))+"</ItemId></Identity-id></RelatedRole></RelatedProcessRoles-create>");
			SOAPRequestObject createProcessRequest = new SOAPRequestObject(
					"http://schemas/OpenTextNotifications/Process/operations", "CreateRelatedProcessRoles", null,
					null);
			createProcessRequest.addParameterAsXml(processIdNode);
			createProcessRequest.addParameterAsXml(relatedRoleCreateNode);
			processItemIdResponse = createProcessRequest.sendAndWait();
			String itemId = Node.getDataWithDefault(
					NomUtil.getNode(".//RelatedProcessRoles/RelatedProcessRoles-id/ItemId1", processItemIdResponse), null);
			if (Objects.isNull(itemId)) {
				row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
				row.put(ImportConstants.STATUS_LOG, Node
						.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", processItemIdResponse), null));
			} else {
				row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
				row.put(ImportConstants.STATUS_LOG, "Role mapped to Process successfully.");
			}
		} else {
			row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
			row.put(ImportConstants.STATUS_LOG, "Role updated to Process successfully.");
		}
	} catch (Exception e) {
		row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
		row.put(ImportConstants.STATUS_LOG, e.getMessage());
	} finally {
		Utilities.cleanAll(processIdNode, processItemIdResponse);
	}
	}
	
	public ProcessItemType getProcessItemType() {
		return processItemType;
	}

}
