package com.opentext.apps.cc.importhandler.accountmanager;

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

public class AccountManagerImportHandler extends AbstractImportHandler{

	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(AccountManagerImportHandler.class);

	public AccountManagerImportHandler() {
		metadata = new MetadataInitializer();
		validator = new ImportValidator();
	}

	@Override
	protected String getSheetName() {
		return ImportConstants.SHEET_NAME;
	}

	@Override
	protected ImportListener processRow(ImportConfiguration configuration, Map<String, String> row) {
		AccountManagerRecord accountmanagerRecord= null;
		if(row != null){
			ReportItem reportItem = validator.validate(row, metadata,importConfig.getJobId());
			if (reportItem.getErrors().isEmpty()) 
			{
				accountmanagerRecord = new AccountManagerRecord(metadata,reportItem);
				accountmanagerRecord.doWork(new ImportEvent(accountmanagerRecord,row));
			}
			else 
			{
				updateImportLogs(row.get(ImportConstants.MANAGER_REG_ID),importConfig.getJobId(), reportItem.getErrors().toString());
			}
		}
		return accountmanagerRecord;
	}

	@Override
	protected ReportListener createReportListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void commit(Collection<ImportListener> records) {
		int contactRecordNode=0,jobIdNode=0,contactImportResponse=0;
		if(records.size()>0){
			contactRecordNode = NomUtil.parseXML("<AccountManagers></AccountManagers>");
			for(ImportListener record:records){
				Node.appendToChildren(record.getnode(), contactRecordNode);
			}
			jobIdNode = NomUtil.parseXML("<JobID>"+super.importConfig.getJobId()+"</JobID>");
			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3","AccountManagersImport", null, null);
			importRequest.addParameterAsXml(jobIdNode);
			importRequest.addParameterAsXml(contactRecordNode);
			try {
				contactImportResponse=importRequest.execute();
			}catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.accountmanager.AccountManagerImportHandler", Severity.ERROR, e, "Error while executing AccountManagerImport");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,"AccountManagerImport");
			}
			finally {
				Utilities.cleanAll(contactRecordNode,contactImportResponse,jobIdNode);
			}
		}
	}
	private void updateImportLogs(String legacyID,String jobID, String errors) {
		int managerIDNode=0,statusItemIdResponse=0,updateImportStatusNode=0,updateDataStatusNode=0,updateImportStatusResponse=0,jobIDNode=0;
		try
		{
			if(!Utilities.isStringEmpty(legacyID))
			{
				SOAPRequestObject statusItemIdRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/AccountManagerImportStatus/operations", "AccountManagerImportStatusManagerIDFilter", null, null);
				managerIDNode = NomUtil.parseXML("<ManagerID>"+legacyID+"</ManagerID>");
				jobIDNode = NomUtil.parseXML("<JobID>"+jobID+"</JobID>");
				statusItemIdRequest.addParameterAsXml(managerIDNode);
				statusItemIdRequest.addParameterAsXml(jobIDNode);
				statusItemIdResponse = statusItemIdRequest.sendAndWait();
				String itemId=Node.getDataWithDefault(NomUtil.getNode(".//AccountManagerImportStatus-id/ItemId", statusItemIdResponse),null);

				SOAPRequestObject updateImportJobRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/AccountManagerImportStatus/operations", "UpdateAccountManagerImportStatus", null, null);
				updateImportStatusNode = NomUtil.parseXML("<AccountManagerImportStatus-id></AccountManagerImportStatus-id>");
				Node.setDataElement(updateImportStatusNode, "ItemId", itemId);
				updateDataStatusNode = NomUtil.parseXML("<AccountManagerImportStatus-update></AccountManagerImportStatus-update>");
				Node.setDataElement(updateDataStatusNode, "LogDetails", errors);
				Node.setDataElement(updateDataStatusNode, "ImportStatus", "InvalidData");
				updateImportJobRequest.addParameterAsXml(updateDataStatusNode);
				updateImportJobRequest.addParameterAsXml(updateImportStatusNode);
				updateImportStatusResponse = updateImportJobRequest.sendAndWait();
			}
		}
		finally
		{
			Utilities.cleanAll(managerIDNode,updateImportStatusNode,updateDataStatusNode,statusItemIdResponse,updateImportStatusResponse,jobIDNode);
		}
	}
	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData, String jobId) {
		boolean status = true; 
		String jobStatus = "Reading ZIPs completed";
		int count=0,createNode=0,importNode=0,child=0,importStatusNode=0,PartyRegistrationNode=0,createImportStatusResponse=0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);//entity web service to read job details and update count value
		try
		{
			for(;count<sheetData.size();count++)
			{
				row=sheetData.get(count);
				if(row.get(getKey(ImportConstants.MANAGER_REG_ID)) != null){
					SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/AccountManagerImportStatus/operations", "CreateAccountManagerImportStatus", null, null);
					createNode = NomUtil.parseXML("<AccountManagerImportStatus-create xmlns=\"http://schemas/OpenTextContractCenterImport/AccountManagerImportStatus\"></AccountManagerImportStatus-create>");
					importNode = NomUtil.parseXML("<RelatedJobID></RelatedJobID>");
					child = Node.createElementNS("DataImport-id","","", "http://schemas/OpenTextContractCenterImport/DataImport", importNode);
					Node.appendToChildren(NomUtil.parseXML("<Id>"+jobId+"</Id>"),child);
					Node.appendToChildren(importNode,createNode);
					PartyRegistrationNode = NomUtil.parseXML("<ManagerID>"+row.get(getKey(ImportConstants.MANAGER_REG_ID))+"</ManagerID>");
					Node.appendToChildren(PartyRegistrationNode,createNode);				
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
			logger._log("com.opentext.apps.cc.importhandler.accountmanager.AccountManagerImportHandler", Severity.ERROR, e, "Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		}
		finally 
		{
			Utilities.cleanAll(createNode,importNode,child,importStatusNode,PartyRegistrationNode,createImportStatusResponse);
		}
		updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump success"
		return status;
	}
	@SuppressWarnings("deprecation")
	protected void generateErrorReport(String jodId, Path path,ImportConfiguration importparm) {
		int response = 0, nodes[] = null;

		try {
			Map<String, Map<String, String>> rowData = super.getSheetData(path, this.getSheetName(), ImportConstants.MANAGER_REG_ID);
			if(null == rowData || rowData.size() == 0) return;
			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "getFailedAccountManagerImportItemsByJobID", null, null);
			int input = NomUtil.parseXML("<Input></Input>");
			//String input = "<Input></Input>";
			//String jobId = "<JobID>"+jodId+"</JobID>";
			Node.appendToChildren(NomUtil.parseXML("<JobID>"+jodId+"</JobID>"),input);
			importRequest.addParameterAsXml(input);
			response = importRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//AccountManagerImportStatus", response);
			List<Map<String, String>> errorRecords = new LinkedList<Map<String, String>>();
			for (int i : nodes) {
				String managerID=null, importStatus = null, logDetails=null;
				managerID = Node.getDataWithDefault(NomUtil.getNode(".//ManagerID", i),null);
				importStatus = Node.getDataWithDefault(NomUtil.getNode(".//ImportStatus", i),null);
				logDetails =Node.getDataWithDefault(NomUtil.getNode(".//LogDetails", i),null);
				if(rowData.get(managerID) != null) {
					Map<String, String> row = rowData.get(managerID);
					row.put("ImportStatus", importStatus);
					row.put("Error", logDetails);
					errorRecords.add(row);
				}
			}
			super.createErrorFile(errorRecords,FileUtil.getDownloadReadPath()+jodId+File.separatorChar+ImportHandler.AccountManager.name()+"_Error_Report.xlsx", this.getSheetName(),importparm);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ContentManagementRuntimeException(e);
		} finally { 
			if(nodes != null) Utilities.cleanAll(nodes);
			Utilities.cleanAll(response);
		}
	}
}
