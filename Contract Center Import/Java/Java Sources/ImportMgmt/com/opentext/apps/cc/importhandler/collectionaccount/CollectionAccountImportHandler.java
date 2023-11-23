package com.opentext.apps.cc.importhandler.collectionaccount;

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
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.ImportProperties;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.ReportListener;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;

public class CollectionAccountImportHandler extends AbstractImportHandler
{
	MetadataInitializer metadata;
	private final ImportValidator validator;
	ImportProperties importconfigproperties;
	ReportItem reportItem;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(CollectionAccountImportHandler.class);

	public CollectionAccountImportHandler() 
	{
		metadata = new MetadataInitializer();
		validator = new ImportValidator();
	}

	@Override
	protected String getSheetName()
	{
		return ImportConstants.SHEET_NAME;
	}

	@Override
	protected ImportListener processRow(ImportConfiguration configuration, Map<String, String> row) 
	{
		CollectionAccountRecord collectionAccount= null;
		if(row != null)
		{
			ReportItem reportItem = validator.validate(row, metadata,importConfig.getJobId());
			if (reportItem.getErrors().isEmpty()) 
			{
				collectionAccount = new CollectionAccountRecord(metadata,reportItem);
				collectionAccount.doWork(new ImportEvent(collectionAccount,row));
			}
			else 
			{
				updateImportLogs(row.get(ImportConstants.LEGACY_ID),importConfig.getJobId(), reportItem.getErrors().toString());
			}
		}
		return collectionAccount;
	}

	@Override
	protected ReportListener createReportListener()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void commit(Collection<ImportListener> records)
	{
		int collectionAccountsNode=0,jobIdNode=0,collectionAccountImportResponse=0;
		if(records.size()>0)
		{
			collectionAccountsNode = NomUtil.parseXML("<CollectionAccounts></CollectionAccounts>");
			for(ImportListener record:records)
			{
				Node.appendToChildren(record.getnode(), collectionAccountsNode);
			}
			jobIdNode = NomUtil.parseXML("<JobID>"+super.importConfig.getJobId()+"</JobID>");
			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3","CollectionAccountImport", null, null);
			importRequest.addParameterAsXml(jobIdNode);
			importRequest.addParameterAsXml(collectionAccountsNode);
			try 
			{
				collectionAccountImportResponse=importRequest.execute();
			}catch (Exception e) 
			{
				logger._log("com.opentext.apps.cc.importhandler.collectionaccount.CollectionAccountImportHandler", Severity.ERROR, e, "Error while executing CollectionAccountImport");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,"CollectionAccountImport");
			}
			finally 
			{
				Utilities.cleanAll(collectionAccountsNode,collectionAccountImportResponse,jobIdNode);
			}
		}
	}

	private void updateImportLogs(String legacyID,String jobID, String errors) 
	{
		int legacyIDNode=0,statusItemIdResponse=0,updateImportStatusNode=0,updateDataStatusNode=0,updateImportStatusResponse=0,jobIDNode=0;
		try
		{
			if(!Utilities.isStringEmpty(legacyID))
			{
				SOAPRequestObject statusItemIdRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/CollectionAccountImportStatus/operations", "GetCAStatusbyLegacyIDandJobID", null, null);
				legacyIDNode = NomUtil.parseXML("<LegacyID>"+legacyID+"</LegacyID>");
				jobIDNode = NomUtil.parseXML("<JobID>"+jobID+"</JobID>");
				statusItemIdRequest.addParameterAsXml(legacyIDNode);
				statusItemIdRequest.addParameterAsXml(jobIDNode);
				statusItemIdResponse = statusItemIdRequest.sendAndWait();
				String itemId=Node.getDataWithDefault(NomUtil.getNode(".//CollectionAccountImportStatus-id/ItemId", statusItemIdResponse),null);

				SOAPRequestObject updateImportJobRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/CollectionAccountImportStatus/operations", "UpdateCollectionAccountImportStatus", null, null);
				updateImportStatusNode = NomUtil.parseXML("<CollectionAccountImportStatus-id></CollectionAccountImportStatus-id>");
				Node.setDataElement(updateImportStatusNode, "ItemId", itemId);
				updateDataStatusNode = NomUtil.parseXML("<CollectionAccountImportStatus-update></CollectionAccountImportStatus-update>");
				Node.setDataElement(updateDataStatusNode, "LogDetails", errors);
				Node.setDataElement(updateDataStatusNode, "ImportStatus", "InvalidData");
				updateImportJobRequest.addParameterAsXml(updateDataStatusNode);
				updateImportJobRequest.addParameterAsXml(updateImportStatusNode);
				updateImportStatusResponse = updateImportJobRequest.sendAndWait();
			}
		}
		finally
		{
			Utilities.cleanAll(legacyIDNode,updateImportStatusNode,updateDataStatusNode,statusItemIdResponse,updateImportStatusResponse,jobIDNode);
		}
	}

	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData, String jobId) 
	{

		boolean status = true; 
		String jobStatus = "Reading ZIPs completed";
		int count=0,createNode=0,importNode=0,child=0,collectionAccountNode=0,importStatusNode=0,PartyRegistrationNode=0,managerIdNode=0,collectionAccountIdNode=0,createImportStatusResponse=0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);//entity web service to read job details and update count value
		try
		{
			for(;count<sheetData.size();count++)
			{
				row=sheetData.get(count);
				if(row.get(getKey(ImportConstants.LEGACY_ID)) != null){
					SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/CollectionAccountImportStatus/operations", "CreateCollectionAccountImportStatus", null, null);
					createNode = NomUtil.parseXML("<CollectionAccountImportStatus-create xmlns=\"http://schemas/OpenTextContractCenterImport/CollectionAccountImportStatus\"></CollectionAccountImportStatus-create>");
					importNode = NomUtil.parseXML("<RelatedJobId></RelatedJobId>");
					child = Node.createElementNS("DataImport-id","","", "http://schemas/OpenTextContractCenterImport/DataImport", importNode);
					Node.appendToChildren(NomUtil.parseXML("<Id>"+jobId+"</Id>"),child);
					Node.appendToChildren(importNode,createNode);
					collectionAccountNode = NomUtil.parseXML("<AccountNumber>"+row.get(getKey(ImportConstants.COLLECTION_ACCOUNT))+"</AccountNumber>");
					Node.appendToChildren(collectionAccountNode,createNode);
					PartyRegistrationNode = NomUtil.parseXML("<PartyRegistrationID>"+row.get(getKey(ImportConstants.PARTY_REG_ID))+"</PartyRegistrationID>");
					Node.appendToChildren(PartyRegistrationNode,createNode);
					managerIdNode = NomUtil.parseXML("<ManagerID>"+row.get(getKey(ImportConstants.MANAGER_ID))+"</ManagerID>");
					Node.appendToChildren(managerIdNode,createNode);
					collectionAccountIdNode = NomUtil.parseXML("<LegacyID>"+row.get(getKey(ImportConstants.LEGACY_ID))+"</LegacyID>");
					Node.appendToChildren(collectionAccountIdNode,createNode);					
					importStatusNode = NomUtil.parseXML("<ImportStatus>NotStarted</ImportStatus>");
					Node.appendToChildren(importStatusNode,createNode);
					createRequest.addParameterAsXml(createNode);
					createImportStatusResponse=createRequest.sendAndWait();
				}
			}
		}
		catch (Exception e) 
		{
			status = false;
			jobStatus = "Reading ZIPs failed";
			updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump failed"
			logger._log("com.opentext.apps.cc.importhandler.collectionaccount.CollectionAccountImportHandler", Severity.ERROR, e,"Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		}
		finally 
		{
			Utilities.cleanAll(createNode,importNode,child,collectionAccountNode,importStatusNode,PartyRegistrationNode,managerIdNode,createImportStatusResponse,collectionAccountIdNode);
		}
		updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump success"
		return status;
	}
	
	protected void generateErrorReport(String jobId, Path path, ImportConfiguration importparm) {
		int response = 0, nodes[] = null;
		try {
			Map<String, Map<String, String>> rowData = super.getSheetData(path, this.getSheetName(),
					ImportConstants.LEGACY_ID);
			if (null == rowData || rowData.size() == 0)
				return;
			SOAPRequestObject importRequest = new SOAPRequestObject(
					"http://schemas/OpenTextContractCenterImport/CollectionAccountImportStatus/operations",
					"GetCAStatusbyJobID", null, null);
			int input = NomUtil.parseXML("<JobID>"+jobId+"</JobID>");
			// String input = "<Input></Input>";
			// String jobId = "<JobID>"+jodId+"</JobID>";
			//Node.appendToChildren(NomUtil.parseXML("<JobID>" + jodId + "</JobID>"), input);
			importRequest.addParameterAsXml(input);
			response = importRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//CollectionAccountImportStatus", response);
			List<Map<String, String>> errorRecords = new LinkedList<Map<String, String>>();
			for (int i : nodes) {
				String LegacyID = null, importStatus = null, logDetails = null;
				LegacyID = Node.getDataWithDefault(NomUtil.getNode(".//LegacyID", i), null);
				importStatus = Node.getDataWithDefault(NomUtil.getNode(".//ImportStatus", i), null);
				logDetails = Node.getDataWithDefault(NomUtil.getNode(".//LogDetails", i), null);
				if (rowData.get(LegacyID) != null) {
					Map<String, String> row = rowData.get(LegacyID);
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
			if (nodes != null)
				Utilities.cleanAll(nodes);
			Utilities.cleanAll(response);
		}
	}
}
