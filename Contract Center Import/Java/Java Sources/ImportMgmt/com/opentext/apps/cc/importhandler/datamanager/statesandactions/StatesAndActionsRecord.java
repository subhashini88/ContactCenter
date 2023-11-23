package com.opentext.apps.cc.importhandler.datamanager.statesandactions;

import java.util.Map;
import java.util.Objects;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;

public class StatesAndActionsRecord implements ImportListener {

	public int processNode;
	private final MetadataInitializer metadata;
	private final ReportItem reportItem;
	private ProcessItemType processItemType;

	public StatesAndActionsRecord(MetadataInitializer metadata, ReportItem reportItem,
			ProcessItemType processItemType) {
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
		case PROCESS:
			createProcessIfnotExists(event);
			break;
		case PROPERTIES:
			createRelatedPropertiesIfnotExists(event);
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

	private void createProcessIfnotExists(ImportEvent event) {
		int createNode = 0, createProcessResponse = 0, updateIDNode = 0, updateDataNode = 0, updateProcessResponse = 0;
		Map<String, String> row = event.getRow();
		try {
			if ((Objects.isNull(metadata) || Objects.isNull(metadata.getAllProcessMap())
					|| Objects.isNull(metadata.getAllProcessMap().get(row.get(ImportConstants.PROCESSNAME)))
					|| Objects.isNull(
							metadata.getAllProcessMap().get(row.get(ImportConstants.PROCESSNAME)).getItemId()))) {
				SOAPRequestObject createProcess = new SOAPRequestObject(
						"http://schemas/OpenTextBasicComponents/GCProcess/operations", "CreateGCProcess", null, null);
				createNode = NomUtil.parseXML("<GCProcess-create></GCProcess-create>");
				Node.setDataElement(createNode, "Name", row.get(ImportConstants.PROCESSNAME));
				Node.setDataElement(createNode, "CreationType", row.get(ImportConstants.PROCESSNAME));
				Node.setDataElement(createNode, "Purpose", row.get(ImportConstants.PURPOSE));
				createProcess.addParameterAsXml(createNode);
				createProcessResponse = createProcess.sendAndWait();
				String itemId = Node.getDataWithDefault(
						NomUtil.getNode(".//GCProcess/GCProcess-id/ItemId", createProcessResponse), null);
				if (Objects.isNull(itemId)) {
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
					row.put(ImportConstants.STATUS_LOG, Node
							.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", createProcessResponse), null));
				} else {
					Process process = new Process(itemId, row.get(ImportConstants.PROCESSNAME));
					metadata.getAllProcessMap().put(row.get(ImportConstants.PROCESSNAME), process);
					metadata.getAllProcessIdsMap().put(itemId, row.get(ImportConstants.PROCESSNAME));
					metadata.getAllProcessMap().get(row.get(ImportConstants.PROCESSNAME));
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
					row.put(ImportConstants.STATUS_LOG, "Process creation is success.");
				}
			} else if (!Objects.isNull(metadata.getAllProcessMap().get(row.get(ImportConstants.PROCESSNAME))) & !Objects
					.isNull(metadata.getAllProcessMap().get(row.get(ImportConstants.PROCESSNAME)).getItemId())) {
				SOAPRequestObject updateProcess = new SOAPRequestObject(
						"http://schemas/OpenTextBasicComponents/GCProcess/operations", "UpdateGCProcess", null, null);
				updateIDNode = NomUtil.parseXML("<GCProcess-id></GCProcess-id>");
				Node.setDataElement(updateIDNode, "ItemId",
						metadata.getAllProcessMap().get(row.get(ImportConstants.PROCESSNAME)).getItemId());
				updateProcess.addParameterAsXml(updateIDNode);
				updateDataNode = NomUtil.parseXML("<GCProcess-update></GCProcess-update>");
				Node.setDataElement(updateDataNode, "Purpose", row.get(ImportConstants.PURPOSE));
				updateProcess.addParameterAsXml(updateDataNode);
				updateProcessResponse = updateProcess.sendAndWait();
				String itemId = Node.getDataWithDefault(
						NomUtil.getNode(".//GCProcess/GCProcess-id/ItemId", updateProcessResponse), null);
				if (!Objects.isNull(itemId)) {
					row.put(ImportConstants.STATUS, ImportConstants.RECORD_EXISTS);
					row.put(ImportConstants.STATUS_LOG, "Process updation is success.");
				}
			}
		} catch (Exception e) {
			row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
			row.put(ImportConstants.STATUS_LOG, e.getMessage());
		} finally {
			Utilities.cleanAll(createNode, createProcessResponse, updateIDNode, updateDataNode, updateProcessResponse);
		}
	}

	private void createRelatedActionIfnotExists(ImportEvent event) {
		int processIdNode = 0, processItemIdResponse = 0, relatedActionNode = 0, relatedGCActionsIDNode = 0,
				updateRelatedGCActionsNode = 0, updateRelatedGCActionsRequestResponse = 0;
		Map<String, String> row = event.getRow();
		Process process = metadata.getAllProcessMap().get(row.get(ImportConstants.PROCESSNAME));
		try {
			if (!process.getActions().containsKey(row.get(ImportConstants.ACTION))) {
				processIdNode = NomUtil.parseXML("<GCProcess-id></GCProcess-id>");
				Node.setDataElement(processIdNode, "ItemId", process.getItemId());
				relatedActionNode = NomUtil.parseXML("<RelatedGCActions-create></RelatedGCActions-create>");
				Node.setDataElement(relatedActionNode, "Name", row.get(ImportConstants.ACTION));
				Node.setDataElement(relatedActionNode, "Purpose", row.get(ImportConstants.PURPOSE));
				SOAPRequestObject createProcessRequest = new SOAPRequestObject(
						"http://schemas/OpenTextBasicComponents/GCProcess/operations", "CreateRelatedGCActions", null,
						null);
				createProcessRequest.addParameterAsXml(processIdNode);
				createProcessRequest.addParameterAsXml(relatedActionNode);
				processItemIdResponse = createProcessRequest.sendAndWait();
				String itemId = Node.getDataWithDefault(
						NomUtil.getNode(".//RelatedGCActions/RelatedGCActions-id/ItemId", processItemIdResponse), null);
				if (Objects.isNull(itemId)) {
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
					row.put(ImportConstants.STATUS_LOG, Node
							.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", processItemIdResponse), null));
				} else {
					process.getActions().put(row.get(ImportConstants.ACTION), itemId);
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
					row.put(ImportConstants.STATUS_LOG, "Action mapped to Process successfully.");
				}
			} else if (process.getActions().containsKey(row.get(ImportConstants.ACTION))) {
				relatedGCActionsIDNode = NomUtil.parseXML("<RelatedGCActions-id></RelatedGCActions-id>");
				Node.setDataElement(relatedGCActionsIDNode, "ItemId",
						process.getActions().get(row.get(ImportConstants.ACTION)));
				updateRelatedGCActionsNode = NomUtil.parseXML("<RelatedGCActions-update></RelatedGCActions-update>");
				Node.setDataElement(updateRelatedGCActionsNode, "Purpose", row.get(ImportConstants.PURPOSE));
				SOAPRequestObject updateRelatedGCActionsRequest = new SOAPRequestObject(
						"http://schemas/OpenTextBasicComponents/GCProcess.RelatedGCActions/operations",
						"UpdateRelatedGCActions", null, null);
				updateRelatedGCActionsRequest.addParameterAsXml(relatedGCActionsIDNode);
				updateRelatedGCActionsRequest.addParameterAsXml(updateRelatedGCActionsNode);
				updateRelatedGCActionsRequestResponse = updateRelatedGCActionsRequest.sendAndWait();
				String itemId = Node.getDataWithDefault(NomUtil.getNode(
						".//RelatedGCActions/RelatedGCActions-id/ItemId", updateRelatedGCActionsRequestResponse), null);
				if (!Objects.isNull(itemId)) {
					row.put(ImportConstants.STATUS, ImportConstants.RECORD_EXISTS);
					row.put(ImportConstants.STATUS_LOG, "Action mapped to Process is updated successfully");
				}
			}
		} catch (Exception e) {
			row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
			row.put(ImportConstants.STATUS_LOG, e.getMessage());
		} finally {
			Utilities.cleanAll(processIdNode, processItemIdResponse, relatedActionNode, relatedGCActionsIDNode,
					updateRelatedGCActionsNode, updateRelatedGCActionsRequestResponse);
		}
	}

	private void createRelatedStateIfnotExists(ImportEvent event) {
		int processIdNode = 0, processItemIdResponse = 0, relatedStateNode = 0, relatedGCStateIDNode = 0,
				updateRelatedGCStateNode = 0, updateRelatedGCStateRequestResponse = 0;
		Map<String, String> row = event.getRow();
		Process process = metadata.getAllProcessMap().get(row.get(ImportConstants.PROCESSNAME));
		try {
			if (!process.getStates().containsKey(row.get(ImportConstants.STATE))) {
				processIdNode = NomUtil.parseXML("<GCProcess-id></GCProcess-id>");
				Node.setDataElement(processIdNode, "ItemId", process.getItemId());
				relatedStateNode = NomUtil.parseXML("<RelatedGCState-create></RelatedGCState-create>");
				Node.setDataElement(relatedStateNode, "Name", row.get(ImportConstants.STATE));
				Node.setDataElement(relatedStateNode, "Purpose", row.get(ImportConstants.PURPOSE));
				SOAPRequestObject createProcessRequest = new SOAPRequestObject(
						"http://schemas/OpenTextBasicComponents/GCProcess/operations", "CreateRelatedGCState", null,
						null);
				createProcessRequest.addParameterAsXml(processIdNode);
				createProcessRequest.addParameterAsXml(relatedStateNode);
				processItemIdResponse = createProcessRequest.sendAndWait();
				String itemId = Node.getDataWithDefault(
						NomUtil.getNode(".//RelatedGCState/RelatedGCState-id/ItemId", processItemIdResponse), null);
				if (Objects.isNull(itemId)) {
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
					row.put(ImportConstants.STATUS_LOG, Node
							.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", processItemIdResponse), null));
				} else {
					process.getStates().put(row.get(ImportConstants.STATE), itemId);
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
					row.put(ImportConstants.STATUS_LOG, "State mapped to Process successfully.");
				}
			} else if (process.getStates().containsKey(row.get(ImportConstants.STATE))) {
				relatedGCStateIDNode = NomUtil.parseXML("<RelatedGCState-id></RelatedGCState-id>");
				Node.setDataElement(relatedGCStateIDNode, "ItemId",
						process.getStates().get(row.get(ImportConstants.STATE)));
				updateRelatedGCStateNode = NomUtil.parseXML("<RelatedGCState-update></RelatedGCState-update>");
				Node.setDataElement(updateRelatedGCStateNode, "Purpose", row.get(ImportConstants.PURPOSE));
				SOAPRequestObject updateRelatedGCStateRequest = new SOAPRequestObject(
						"http://schemas/OpenTextBasicComponents/GCProcess.RelatedGCState/operations",
						"UpdateRelatedGCState", null, null);
				updateRelatedGCStateRequest.addParameterAsXml(relatedGCStateIDNode);
				updateRelatedGCStateRequest.addParameterAsXml(updateRelatedGCStateNode);
				updateRelatedGCStateRequestResponse = updateRelatedGCStateRequest.sendAndWait();
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedGCState/RelatedGCState-id/ItemId",
						updateRelatedGCStateRequestResponse), null);
				if (!Objects.isNull(itemId)) {
					row.put(ImportConstants.STATUS, ImportConstants.RECORD_EXISTS);
					row.put(ImportConstants.STATUS_LOG, "State mapped to Process is updated successfully");
				}
			}
		} catch (Exception e) {
			row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
			row.put(ImportConstants.STATUS_LOG, e.getMessage());
		} finally {
			Utilities.cleanAll(processIdNode, processItemIdResponse, relatedStateNode, relatedGCStateIDNode,
					updateRelatedGCStateNode, updateRelatedGCStateRequestResponse);
		}
	}

	private void createRelatedPropertiesIfnotExists(ImportEvent event) {
		int processIdNode = 0, processItemIdResponse = 0, relatedPropNode = 0, relatedGCStateIDNode = 0,
				updateRelatedGCStateNode = 0, updateRelatedGCStateRequestResponse = 0;
		Map<String, String> row = event.getRow();
		Process process = metadata.getAllProcessMap().get(row.get(ImportConstants.PROCESSNAME));
		try {
			if (!process.getProperties().containsKey(row.get(ImportConstants.DISPLAYNAME))) {
				processIdNode = NomUtil.parseXML("<GCProcess-id></GCProcess-id>");
				Node.setDataElement(processIdNode, "ItemId", process.getItemId());
				relatedPropNode = NomUtil.parseXML("<RelatedGCProps-create></RelatedGCProps-create>");
				Node.setDataElement(relatedPropNode, "DataType", row.get(ImportConstants.DATATYPE));
				Node.setDataElement(relatedPropNode, "DisplayName", row.get(ImportConstants.DISPLAYNAME));
				Node.setDataElement(relatedPropNode, "IsMultiField", row.get(ImportConstants.ISMULTIVAL).toUpperCase());
				Node.setDataElement(relatedPropNode, "Name", row.get(ImportConstants.PROPNAME));
				Node.setDataElement(relatedPropNode, "Path", row.get(ImportConstants.PATH));
				Node.setDataElement(relatedPropNode, "Purpose", row.get(ImportConstants.PURPOSE));
				Node.setDataElement(relatedPropNode, "Type", row.get(ImportConstants.TYPE));
				Node.setDataElement(relatedPropNode, "Xpath", row.get(ImportConstants.Xpath));
				Node.setDataElement(relatedPropNode, "IsMandatory", row.get(ImportConstants.IsMandatory));
				Node.setDataElement(relatedPropNode, "GroupID", row.get(ImportConstants.GroupID));
				Node.setDataElement(relatedPropNode, "IsReadOnly", row.get(ImportConstants.IsReadOnly));
				SOAPRequestObject createProcessRequest = new SOAPRequestObject(
						"http://schemas/OpenTextBasicComponents/GCProcess/operations", "CreateRelatedGCProps", null,
						null);
				createProcessRequest.addParameterAsXml(processIdNode);
				createProcessRequest.addParameterAsXml(relatedPropNode);
				processItemIdResponse = createProcessRequest.sendAndWait();
				String itemId = Node.getDataWithDefault(
						NomUtil.getNode(".//RelatedGCProps/RelatedGCProps-id/ItemId1", processItemIdResponse), null);
				if (Objects.isNull(itemId)) {
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
					row.put(ImportConstants.STATUS_LOG, Node
							.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", processItemIdResponse), null));
				} else {
					process.getProperties().put(row.get(ImportConstants.DISPLAYNAME), itemId);
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
					row.put(ImportConstants.STATUS_LOG, "Property mapped to Process successfully.");
				}
			} else if (process.getProperties().containsKey(row.get(ImportConstants.DISPLAYNAME))) {
				relatedGCStateIDNode = NomUtil.parseXML("<RelatedGCProps-id></RelatedGCProps-id>");
				Node.setDataElement(relatedGCStateIDNode, "ItemId",
						process.getProperties().get(row.get(ImportConstants.DISPLAYNAME)));
				updateRelatedGCStateNode = NomUtil.parseXML("<RelatedGCProps-update></RelatedGCProps-update>");
				Node.setDataElement(updateRelatedGCStateNode, "Purpose", row.get(ImportConstants.PURPOSE));
				Node.setDataElement(updateRelatedGCStateNode, "DataType", row.get(ImportConstants.DATATYPE));
				Node.setDataElement(updateRelatedGCStateNode, "IsMandatory", row.get(ImportConstants.IsMandatory));
				Node.setDataElement(updateRelatedGCStateNode, "GroupID", row.get(ImportConstants.GroupID));
				Node.setDataElement(updateRelatedGCStateNode, "Name", row.get(ImportConstants.PROPNAME));
				Node.setDataElement(updateRelatedGCStateNode, "IsReadOnly", row.get(ImportConstants.IsReadOnly));
				Node.setDataElement(updateRelatedGCStateNode, "Xpath", row.get(ImportConstants.Xpath));
				SOAPRequestObject updateRelatedGCStateRequest = new SOAPRequestObject(
						"http://schemas/OpenTextBasicComponents/GCProcess.RelatedGCProps/operations",
						"UpdateRelatedGCProps", null, null);
				updateRelatedGCStateRequest.addParameterAsXml(relatedGCStateIDNode);
				updateRelatedGCStateRequest.addParameterAsXml(updateRelatedGCStateNode);
				updateRelatedGCStateRequestResponse = updateRelatedGCStateRequest.sendAndWait();
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedGCProps/RelatedGCProps-id/ItemId1",
						updateRelatedGCStateRequestResponse), null);
				if (!Objects.isNull(itemId)) {
					row.put(ImportConstants.STATUS, ImportConstants.RECORD_EXISTS);
					row.put(ImportConstants.STATUS_LOG, "Property mapped to Process is updated successfully");
				}
			}
		} catch (Exception e) {
			row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
			row.put(ImportConstants.STATUS_LOG, e.getMessage());
		} finally {
			Utilities.cleanAll(processIdNode, processItemIdResponse, relatedPropNode, relatedGCStateIDNode,
					updateRelatedGCStateNode, updateRelatedGCStateRequestResponse);
		}
	}

}
