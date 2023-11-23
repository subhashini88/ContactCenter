package com.opentext.apps.cc.importhandler.externalcontacts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.AbstractImportHandler;
import com.opentext.apps.cc.importcontent.ContentManagementRuntimeException;
import com.opentext.apps.cc.importcontent.FileUtil;
import com.opentext.apps.cc.importcontent.ImportConfiguration;
import com.opentext.apps.cc.importcontent.ImportConstants.ImportHandler;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.ReportListener;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;

public class ExternalContactImportHandler extends AbstractImportHandler{

	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(ExternalContactImportHandler.class);

	public ExternalContactImportHandler() {
		metadata = new MetadataInitializer();
		validator = new ImportValidator();
	}

	@Override
	protected String getSheetName() {
		return ImportConstants.SHEET_NAME;
	}

	@Override
	protected ImportListener processRow(ImportConfiguration configuration, Map<String, String> row) {
		ExternalContactRecord externalContactRecord= null;

		if(row != null){
			ReportItem reportItem = validator.validate(row, metadata,importConfig.getJobId());
			if (reportItem.getErrors().isEmpty()) 
			{
				externalContactRecord = new ExternalContactRecord(metadata,reportItem);
				externalContactRecord.doWork(new ImportEvent(externalContactRecord,row));
			}
			else 
			{
				updateImportLogs(row.get(ImportConstants.LEGACY_ID),importConfig.getJobId(), reportItem.getErrors().toString());
			}
		}
		return externalContactRecord;
	}

	@Override
	protected ReportListener createReportListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void commit(Collection<ImportListener> records) {
		int externalContactRecordNode=0,jobIdNode=0,externalContactImportResponse=0;
		if(records.size()>0){
			externalContactRecordNode = NomUtil.parseXML("<ExternalContacts></ExternalContacts>");
			for(ImportListener record:records){
				Node.appendToChildren(record.getnode(), externalContactRecordNode);
				/*jobIdNode = NomUtil.parseXML("<JobID>"+super.importConfig.getJobId()+"</JobID>");
					Node.append(jobIdNode, record.getnode());*/
			}
			jobIdNode = NomUtil.parseXML("<JobID>"+super.importConfig.getJobId()+"</JobID>");
			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3","ExternalContactImport", null, null);
			importRequest.addParameterAsXml(externalContactRecordNode);
			importRequest.addParameterAsXml(jobIdNode);
			try {
				externalContactImportResponse=importRequest.execute();
			}catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.externalcontacts.ExternalContactImportHandler", Severity.ERROR, e, "Error while executing ExternalContactsImport.");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,"ExternalContactsImport");
			}
			finally {
				Utilities.cleanAll(externalContactRecordNode,externalContactImportResponse,jobIdNode);
			}
		}
	}

	private void updateImportLogs(String legacyID,String jobID, String errors) {
		int externalContactRegIDNode=0,statusExternalContactIdResponse=0,updateImportStatusNode=0,updateDataStatusNode=0,updateImportStatusResponse=0,jobIDNode=0;
		try
		{
			if(!Utilities.isStringEmpty(legacyID))
			{
				SOAPRequestObject statusExternalContactIdRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ExternalContactImportStatus/operations", "ExternalContactImportStatusLegacyIDFilter", null, null);
				externalContactRegIDNode = NomUtil.parseXML("<LegacyID>"+legacyID+"</LegacyID>");
				jobIDNode = NomUtil.parseXML("<JobID>"+jobID+"</JobID>");
				statusExternalContactIdRequest.addParameterAsXml(externalContactRegIDNode);
				statusExternalContactIdRequest.addParameterAsXml(jobIDNode);
				statusExternalContactIdResponse = statusExternalContactIdRequest.sendAndWait();
				String itemId=Node.getDataWithDefault(NomUtil.getNode(".//ExternalContactImportStatus-id/ItemId", statusExternalContactIdResponse),null);

				SOAPRequestObject updateImportJobRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ExternalContactImportStatus/operations", "UpdateExternalContactImportStatus", null, null);
				updateImportStatusNode = NomUtil.parseXML("<ExternalContactImportStatus-id></ExternalContactImportStatus-id>");
				Node.setDataElement(updateImportStatusNode, "ItemId", itemId);
				updateDataStatusNode = NomUtil.parseXML("<ExternalContactImportStatus-update></ExternalContactImportStatus-update>");
				Node.setDataElement(updateDataStatusNode, "ImportStatus", "InvalidData");
				Node.setDataElement(updateDataStatusNode, "LogDetails", errors);
				//Node.setDataElement(updateDataStatusNode, "Email", ImportConstants.EMAIL);
				updateImportJobRequest.addParameterAsXml(updateImportStatusNode);
				updateImportJobRequest.addParameterAsXml(updateDataStatusNode);
				updateImportStatusResponse = updateImportJobRequest.sendAndWait();
			}
		}
		finally
		{
			Utilities.cleanAll(externalContactRegIDNode,updateImportStatusNode,updateDataStatusNode,statusExternalContactIdResponse,updateImportStatusResponse,jobIDNode);
		}
	}

	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData, String jobId) {

		boolean status = true; 
		String jobStatus = "Reading ZIPs completed";
		int count=0,createNode=0,importNode=0,child=0,importStatusNode=0,externalContactRegistrationNode=0,externalContactEmailNode=0,createImportStatusResponse=0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);//entity web service to read job details and update count value
		try
		{
			for(;count<sheetData.size();count++)
			{
				row=sheetData.get(count);
				if(row.get(getKey(ImportConstants.LEGACY_ID)) != null){
					SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ExternalContactImportStatus/operations", "CreateExternalContactImportStatus", null, null);
					createNode = NomUtil.parseXML("<ExternalContactImportStatus-create xmlns=\"http://schemas/OpenTextContractCenterImport/ExternalContactImportStatus\"></ExternalContactImportStatus-create>");
					importNode = NomUtil.parseXML("<RelatedJobId></RelatedJobId>");
					child = Node.createElementNS("DataImport-id","","", "http://schemas/OpenTextContractCenterImport/DataImport", importNode);
					Node.appendToChildren(NomUtil.parseXML("<Id>"+jobId+"</Id>"),child);
					Node.appendToChildren(importNode,createNode);
					externalContactRegistrationNode = NomUtil.parseXML("<LegacyID>"+row.get(getKey(ImportConstants.LEGACY_ID))+"</LegacyID>");
					if(row.get(getKey(ImportConstants.EMAIL)) != null)
						externalContactEmailNode = NomUtil.parseXML("<Email>"+row.get(getKey(ImportConstants.EMAIL))+"</Email>");
					else
						externalContactEmailNode = NomUtil.parseXML("<Email>"+""+"</Email>");
					Node.appendToChildren(externalContactRegistrationNode,createNode);				
					importStatusNode = NomUtil.parseXML("<ImportStatus>NotStarted</ImportStatus>");
					Node.appendToChildren(externalContactEmailNode,createNode);
					Node.appendToChildren(importStatusNode,createNode);
					createRequest.addParameterAsXml(createNode);
					createImportStatusResponse=createRequest.sendAndWait();
				}
			}
		}catch (Exception e) 
		{
			status = false;
			jobStatus = "Reading ZIPs failed";
			updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump failed"
			logger._log("com.opentext.apps.cc.importhandler.externalcontacts.ExternalContactImportHandler", Severity.ERROR, e, "Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		}
		finally 
		{
			Utilities.cleanAll(createNode,importNode,child,importStatusNode,externalContactRegistrationNode,createImportStatusResponse);
		}
		updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump success"
		return status;
	}

	@SuppressWarnings("deprecation")
	protected void generateErrorReport(String jobId, Path path,ImportConfiguration importparm) {
		int response = 0, nodes[] = null;
		int createNode=0;
		try {
			Map<String, Map<String, String>> rowData = super.getSheetData(path, this.getSheetName(), ImportConstants.LEGACY_ID);
			if(null == rowData || rowData.size() == 0) return;
			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ExternalContactImportStatus/operations", "GetFailedExternalContactImportItemsByJobID", null, null);
			createNode = NomUtil.parseXML("<JobID>"+jobId+"</JobID>");
			importRequest.addParameterAsXml(createNode);
			response = importRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//ExternalContactImportStatus", response);
			List<Map<String, String>> errorRecords = new LinkedList<Map<String, String>>();
			for (int i : nodes) {
				String LegacyID=null, importStatus = null, logDetails=null;
				LegacyID = Node.getDataWithDefault(NomUtil.getNode(".//LegacyID", i),null);
				importStatus = Node.getDataWithDefault(NomUtil.getNode(".//ImportStatus", i),null);
				logDetails =Node.getDataWithDefault(NomUtil.getNode(".//LogDetails", i),null);
				if(rowData.get(LegacyID) != null) { 
					Map<String, String> row = rowData.get(LegacyID);
					row.put("ImportStatus", importStatus);
					row.put("Error", logDetails);
					errorRecords.add(row);
				}
			}
			super.createErrorFile(errorRecords,FileUtil.getDownloadReadPath()+jobId+File.separatorChar+ImportHandler.ExternalContact.name()+"_Error_Report.xlsx", this.getSheetName(),importparm);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ContentManagementRuntimeException(e);
		} finally { 
			if(nodes != null) Utilities.cleanAll(nodes);
			Utilities.cleanAll(response);
		}
	}
}
