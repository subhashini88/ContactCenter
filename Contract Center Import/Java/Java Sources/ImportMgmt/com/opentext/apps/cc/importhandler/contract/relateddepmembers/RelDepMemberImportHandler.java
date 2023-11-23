package com.opentext.apps.cc.importhandler.contract.relateddepmembers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.opentext.apps.cc.importcontent.ImportConfiguration;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.internal.NOMDocumentPool;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.AbstractImportHandler;
import com.opentext.apps.cc.importcontent.FileUtil;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportListener;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;
import com.opentext.apps.cc.importhandler.contract.relateddepmembers.ImportConstants;
import com.opentext.apps.cc.importcontent.ContentManagementRuntimeException;

public class RelDepMemberImportHandler extends AbstractImportHandler {

	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(RelDepMemberImportHandler.class);

	public RelDepMemberImportHandler() {

		metadata = new MetadataInitializer();
		validator = new ImportValidator();
	}

	@Override
	protected String getSheetName() {
		return ImportConstants.SHEET_NAME;
	}

	@Override
	protected ImportListener processRow(ImportConfiguration configuration, Map<String, String> row) {
		RelDepMemberRecord relDepMemberRecord = null;
		if (row != null && !Utilities.isStringEmpty(row.get(ImportConstants.LEGACY_ID))) {
			
			ReportItem reportItem = validator.validate(row, metadata, importConfig.getJobId());
			if (reportItem.getErrors().isEmpty()) {
				
				relDepMemberRecord = new RelDepMemberRecord(metadata, reportItem);
				relDepMemberRecord.doWork(new ImportEvent(relDepMemberRecord, row));
				
			} else {
				
				updateImportLogs(row.get(ImportConstants.LEGACY_ID), importConfig.getJobId(),reportItem.getErrors().toString());
			}
		}
		return relDepMemberRecord;
	}

	@Override
	protected ReportListener createReportListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void commit(Collection<ImportListener> records) {
		
		int relDepMemberRecordNode=0,jobIdNode=0,relDepMemberImportResponse=0;
		if(records.size()>0){
			relDepMemberRecordNode = NomUtil.parseXML("<RelatedDepartmentMembers></RelatedDepartmentMembers>");
			for(ImportListener record:records){
				Node.appendToChildren(record.getnode(), relDepMemberRecordNode);
			}
			jobIdNode = NomUtil.parseXML("<JobID>"+super.importConfig.getJobId()+"</JobID>");
			SOAPRequestObject relDepMembersImportRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3","RelatedDepartmentMembersImport", null, null);
			relDepMembersImportRequest.addParameterAsXml(jobIdNode);
			relDepMembersImportRequest.addParameterAsXml(relDepMemberRecordNode);
			try {
				relDepMemberImportResponse=relDepMembersImportRequest.execute();
			}catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.contract.relateddepmembers.RelDepMemberImportHandler", Severity.ERROR, e,"Error while executing RelatedDepartmentMembersImports");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,"RelatedDepartmentMembersImports");
			}
			finally {
				Utilities.cleanAll(relDepMemberRecordNode,relDepMemberImportResponse,jobIdNode);
			}
		}
	}

	
	private void updateImportLogs(String legacyID, String jobID, String errors) {
		Document document = NOMDocumentPool.getInstance().lendDocument();
		int legacyIDNode = 0, jobIDNode = 0, getRelDepMemResponse = 0,
				relDepStatusNode = 0, relDepStatusUpdateNode = 0,
				updateResponse = 0;
		try {
			if (!Utilities.isStringEmpty(legacyID)) {
				SOAPRequestObject getRelDepMemRequest = new SOAPRequestObject(
						"http://schemas/OpenTextContractCenterImport/RelDepMembersImportStatus/operations",
						"GetRelDepMemberStatusByLegacyID", null, null);
				legacyIDNode = NomUtil.parseXML("<LegacyID>" + legacyID + "</LegacyID>");
				jobIDNode = NomUtil.parseXML("<JobID>" + jobID + "</JobID>");
				getRelDepMemRequest.addParameterAsXml(legacyIDNode);
				getRelDepMemRequest.addParameterAsXml(jobIDNode);
				getRelDepMemResponse = getRelDepMemRequest
						.sendAndWait();
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//RelDepMembersImportStatus-id/ItemId",
						getRelDepMemResponse), null);

				SOAPRequestObject updateRequest = new SOAPRequestObject(
						"http://schemas/OpenTextContractCenterImport/RelDepMembersImportStatus/operations",
						"UpdateRelDepMembersImportStatus", null, null);
				relDepStatusNode = document.createElement("RelDepMembersImportStatus-id");
				Node.createTextElement("ItemId", itemId, relDepStatusNode);
				relDepStatusUpdateNode = document
						.createElement("RelDepMembersImportStatus-update");
				Node.createTextElement("ImportStatus", "InvalidData", relDepStatusUpdateNode);
				Node.createTextElement("LogDetails", errors, relDepStatusUpdateNode);
				updateRequest.addParameterAsXml(relDepStatusNode);
				updateRequest.addParameterAsXml(relDepStatusUpdateNode);
				updateResponse = updateRequest.sendAndWait();
			}
		} finally {
			Utilities.cleanAll(legacyIDNode, jobIDNode, getRelDepMemResponse,
					relDepStatusNode, relDepStatusUpdateNode,
					updateResponse);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
	}
	
	
	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData, String jobId)
	{
       
		boolean status = true;
		String jobStatus = "Reading ZIPs completed";
		int count = 0, createNode = 0, importNode = 0, importStatusNode = 0, child=0, legacyIDNode = 0, contractIDNode=0,
				createImportStatusResponse = 0, userIDNode=0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);// entity web service to read job details and update count value
		try {
			for (; count < sheetData.size(); count++) {
				row = sheetData.get(count);
				if (row.get(getKey(ImportConstants.LEGACY_ID)) != null) {
					SOAPRequestObject createRequest = new SOAPRequestObject(
							"http://schemas/OpenTextContractCenterImport/RelDepMembersImportStatus/operations",
							"CreateRelDepMembersImportStatus", null, null);					
					createNode = NomUtil.parseXML(
							"<RelDepMembersImportStatus-create xmlns=\"http://schemas/OpenTextContractCenterImport/RelDepMembersImportStatus\"></RelDepMembersImportStatus-create>");
					importNode = NomUtil.parseXML("<RelatedJobID></RelatedJobID>");
					child = Node.createElementNS("DataImport-id", "", "",
							"http://schemas/OpenTextContractCenterImport/DataImport", importNode);
					Node.appendToChildren(NomUtil.parseXML("<Id>" + jobId + "</Id>"), child);
					Node.appendToChildren(importNode, createNode);					
					legacyIDNode = NomUtil.parseXML("<LegacyID>"+ row.get(getKey(ImportConstants.LEGACY_ID)) +"</LegacyID>");
					Node.appendToChildren(legacyIDNode, createNode);
					importStatusNode = NomUtil.parseXML("<ImportStatus>NotStarted</ImportStatus>");
					Node.appendToChildren(importStatusNode, createNode);
					if(row.get(getKey(ImportConstants.CONTRACT_NUMBER)) != null) {
					contractIDNode = NomUtil.parseXML("<ContractID>"+ row.get(getKey(ImportConstants.CONTRACT_NUMBER)) +"</ContractID>");
					Node.appendToChildren(contractIDNode, createNode);
					}
					if(row.get(getKey(ImportConstants.USER_ID)) != null) {
					userIDNode = NomUtil.parseXML("<UserID>"+ row.get(getKey(ImportConstants.USER_ID)) +"</UserID>");
					Node.appendToChildren(userIDNode, createNode);
					}
					createRequest.addParameterAsXml(createNode);
					createImportStatusResponse = createRequest.sendAndWait();
				}
			}
		} catch (Exception e) {
			status = false;
			jobStatus = "Reading ZIPs failed";
			updateImportJob(jobId, jobStatus, count);// update job with count and status "Dump failed"
			logger._log("com.opentext.apps.cc.importhandler.contract.relateddepmembers.RelDepMemberImportHandler", Severity.ERROR, e,
					"Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		} finally {
			Utilities.cleanAll(createNode, importNode,child, importStatusNode, legacyIDNode, contractIDNode, userIDNode, 
					createImportStatusResponse);
		}
		updateImportJob(jobId, jobStatus, count);// update job with count and status "Dump success"
		return status;
	}
	
	
	protected void generateErrorReport(String jobId, Path path,ImportConfiguration importparm)
	{
		int failedResponse=0, jobIDNode=0, nodes[] = null;
		Document document = NOMDocumentPool.getInstance().lendDocument();
		try {
			Map<String, Map<String, String>> rowData = super.getSheetData(path, this.getSheetName(), ImportConstants.LEGACY_ID);
			if(null == rowData || rowData.size() == 0) return;
			SOAPRequestObject request = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/RelDepMembersImportStatus/operations", "GetFailedRelDepMemberStatusByJobID", null, null);
			jobIDNode = NomUtil.parseXML("<JobID>"+jobId+"</JobID>");
			request.addParameterAsXml(jobIDNode);
			failedResponse = request.sendAndWait();
			nodes = NomUtil.getNodeList(".//RelDepMembersImportStatus", failedResponse);
			List<Map<String, String>> errorRecords = new LinkedList<Map<String, String>>();
			for (int i : nodes) {
				String legacyID=null, importStatus = null, logDetails=null;
				legacyID = Node.getDataWithDefault(NomUtil.getNode(".//LegacyID", i),null);
				importStatus = Node.getDataWithDefault(NomUtil.getNode(".//ImportStatus", i),null);
				logDetails =Node.getDataWithDefault(NomUtil.getNode(".//LogDetails", i),null);
				if(rowData.get(legacyID) != null) {
					Map<String, String> row = rowData.get(legacyID);
					row.put("ImportStatus", importStatus);
					row.put("Error", logDetails);
					errorRecords.add(row);
				}
			}
			super.createErrorFile(errorRecords,FileUtil.getDownloadReadPath()+jobId+File.separatorChar+this.getSheetName()+"_Error_Report.xlsx", this.getSheetName(),importparm);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ContentManagementRuntimeException(e);
		} finally { 
			if(nodes != null) Utilities.cleanAll(nodes);
			Utilities.cleanAll(failedResponse, jobIDNode);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
	}
}
