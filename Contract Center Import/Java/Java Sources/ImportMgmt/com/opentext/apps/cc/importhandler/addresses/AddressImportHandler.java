package com.opentext.apps.cc.importhandler.addresses;

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

public class AddressImportHandler extends AbstractImportHandler {

	private MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(AddressImportHandler.class);

	public AddressImportHandler() {
		validator = new ImportValidator();
	}

	@Override
	protected String getSheetName() {
		return ImportConstants.SHEET_NAME;
	}

	@Override
	protected ImportListener processRow(ImportConfiguration configuration, Map<String, String> row) {
		metadata = new MetadataInitializer(row.get(ImportConstants.EMAIL), row.get(ImportConstants.STATE_NAME), row.get(ImportConstants.COUNTRY_NAME));
		AddressRecord addressRecord= null;
		if(row != null){
			ReportItem reportItem = validator.validate(row, metadata,importConfig.getJobId());
			if (reportItem.getErrors().isEmpty()) 
			{
				addressRecord = new AddressRecord(metadata,reportItem);
				addressRecord.doWork(new ImportEvent(addressRecord,row));
			}
			else
			{
				updateImportLogs(row.get(ImportConstants.LEGACY_ID),importConfig.getJobId(), reportItem.getErrors().toString());
			}
		}
		return addressRecord;
	}

	@Override
	protected ReportListener createReportListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void commit(Collection<ImportListener> records) {
		int addressRecordNode=0,jobIdNode=0,addressImportResponse=0;
		if(records.size()>0){
			addressRecordNode = NomUtil.parseXML("<Addresses></Addresses>");


			for(ImportListener record:records){
				Node.appendToChildren(record.getnode(), addressRecordNode);

			}
			jobIdNode = NomUtil.parseXML("<JobID>"+super.importConfig.getJobId()+"</JobID>");
			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3","AddressImport", null, null);
			importRequest.addParameterAsXml(addressRecordNode);
			importRequest.addParameterAsXml(jobIdNode);
			try {
				addressImportResponse=importRequest.execute();
			}catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.addresses.AddressImportHandler", Severity.ERROR, e,"Error while executing AddressImport.");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,"AddressImport");
			}
			finally {
				Utilities.cleanAll(addressRecordNode,addressImportResponse,jobIdNode);
			}
		}
	}

	private void updateImportLogs(String legacyID,String jobID, String errors) {
		int addressRegIDNode=0,statusItemIdResponse=0,updateImportStatusNode=0,updateDataStatusNode=0,updateImportStatusResponse=0,jobIDNode=0;
		try
		{
			if(!Utilities.isStringEmpty(legacyID))
			{
				SOAPRequestObject statusItemIdRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/AddressImportStatus/operations", "AddressImportStatusLegacyIDFilter", null, null);
				addressRegIDNode = NomUtil.parseXML("<LegacyID>"+legacyID+"</LegacyID>");
				jobIDNode = NomUtil.parseXML("<JobID>"+jobID+"</JobID>");
				statusItemIdRequest.addParameterAsXml(addressRegIDNode);
				statusItemIdRequest.addParameterAsXml(jobIDNode);
				statusItemIdResponse = statusItemIdRequest.sendAndWait();
				String itemId=Node.getDataWithDefault(NomUtil.getNode(".//AddressImportStatus-id/ItemId", statusItemIdResponse),null);

				SOAPRequestObject updateImportJobRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/AddressImportStatus/operations", "UpdateAddressImportStatus", null, null);
				updateImportStatusNode = NomUtil.parseXML("<AddressImportStatus-id></AddressImportStatus-id>");
				Node.setDataElement(updateImportStatusNode, "ItemId", itemId);
				updateDataStatusNode = NomUtil.parseXML("<AddressImportStatus-update></AddressImportStatus-update>");
				Node.setDataElement(updateDataStatusNode, "ImportStatus", "InvalidData");
				Node.setDataElement(updateDataStatusNode, "LogDetails", errors);
				//Node.setDataElement(updateDataStatusNode, "ExternalContactEmail", ImportConstants.EMAIL );
				updateImportJobRequest.addParameterAsXml(updateImportStatusNode);
				updateImportJobRequest.addParameterAsXml(updateDataStatusNode);
				updateImportStatusResponse = updateImportJobRequest.sendAndWait();
			}
		}
		finally
		{
			Utilities.cleanAll(addressRegIDNode,updateImportStatusNode,updateDataStatusNode,statusItemIdResponse,updateImportStatusResponse,jobIDNode);
		}
	}

	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData, String jobId) {

		boolean status = true; 
		String jobStatus = "Reading ZIPs completed";
		int count=0,createNode=0,importNode=0,child=0,importStatusNode=0,AddressRegistrationNode=0,externalContactEmailNode=0,createImportStatusResponse=0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);//entity web service to read job details and update count value
		try
		{
			for(;count<sheetData.size();count++)
			{
				row=sheetData.get(count);
				if(row.get(getKey(ImportConstants.LEGACY_ID)) != null){
					SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/AddressImportStatus/operations", "CreateAddressImportStatus", null, null);
					createNode = NomUtil.parseXML("<AddressImportStatus-create xmlns=\"http://schemas/OpenTextContractCenterImport/AddressImportStatus\"></AddressImportStatus-create>");
					importNode = NomUtil.parseXML("<RelatedJobId></RelatedJobId>");
					child = Node.createElementNS("DataImport-id","","", "http://schemas/OpenTextContractCenterImport/DataImport", importNode);
					Node.appendToChildren(NomUtil.parseXML("<Id>"+jobId+"</Id>"),child);
					Node.appendToChildren(importNode,createNode);
					AddressRegistrationNode = NomUtil.parseXML("<LegacyID>"+row.get(getKey(ImportConstants.LEGACY_ID))+"</LegacyID>");
					if(row.get(getKey(ImportConstants.EMAIL)) != null)
						externalContactEmailNode = NomUtil.parseXML("<ExternalContactEmail>"+row.get(getKey(ImportConstants.EMAIL))+"</ExternalContactEmail>");
					else
						externalContactEmailNode = NomUtil.parseXML("<ExternalContactEmail>"+""+"</ExternalContactEmail>");
					Node.appendToChildren(AddressRegistrationNode,createNode);	
					Node.appendToChildren(externalContactEmailNode,createNode);
					importStatusNode = NomUtil.parseXML("<ImportStatus>NotStarted</ImportStatus>");
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
			logger._log("com.opentext.apps.cc.importhandler.addresses.AddressImportHandler", Severity.ERROR, e, "Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		}
		finally 
		{
			Utilities.cleanAll(createNode,importNode,child,importStatusNode,AddressRegistrationNode,createImportStatusResponse);
		}
		updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump success"
		return status;
	}

	protected void generateErrorReport(String jobId, Path path,ImportConfiguration importparm) {
		int response = 0, nodes[] = null;
		int createNode=0;
		try {
			Map<String, Map<String, String>> rowData = super.getSheetData(path, this.getSheetName(), ImportConstants.LEGACY_ID);
			if(null == rowData || rowData.size() == 0) return;
			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/AddressImportStatus/operations", "GetFailedAddressImportItemsByJobID", null, null);
			createNode = NomUtil.parseXML("<JobID>"+jobId+"</JobID>");
			importRequest.addParameterAsXml(createNode);
			response = importRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//AddressImportStatus", response);
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
			super.createErrorFile(errorRecords,FileUtil.getDownloadReadPath()+jobId+File.separatorChar+ImportHandler.Address.name()+"_Error_Report.xlsx", this.getSheetName(),importparm);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ContentManagementRuntimeException(e);
		} finally { 
			if(nodes != null) Utilities.cleanAll(nodes);
			Utilities.cleanAll(response);
		}
	}

}