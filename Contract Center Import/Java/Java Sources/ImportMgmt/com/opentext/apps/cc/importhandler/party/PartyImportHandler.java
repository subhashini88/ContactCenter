package com.opentext.apps.cc.importhandler.party;

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

public class PartyImportHandler extends AbstractImportHandler
{

	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(PartyImportHandler.class);

	public PartyImportHandler()
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
		PartyRecord partyRecord= null;
		if(row != null)
		{
			ReportItem reportItem = validator.validate(row, metadata,importConfig.getJobId());
			if (reportItem.getErrors().isEmpty()) 
			{
				partyRecord = new PartyRecord(metadata,reportItem);
				partyRecord.doWork(new ImportEvent(partyRecord,row));
			}
			else
			{
				updateImportLogs(row.get(ImportConstants.PARTY_REGISTRATION_ID),importConfig.getJobId(), reportItem.getErrors().toString());
			}
		}
		return partyRecord;
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
		int partyRecordNode=0,jobIdNode=0,partyImportResponse=0;
		if(records.size()>0)
		{
			partyRecordNode = NomUtil.parseXML("<Parties></Parties>");
			for(ImportListener record:records)
			{
				Node.appendToChildren(record.getnode(), partyRecordNode);
			}
			jobIdNode = NomUtil.parseXML("<JobID>"+super.importConfig.getJobId()+"</JobID>");
			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3","PartiesImport", null, null);
			importRequest.addParameterAsXml(jobIdNode);
			importRequest.addParameterAsXml(partyRecordNode);
			try
			{
				partyImportResponse=importRequest.execute();
			}
			catch (Exception e) 
			{
				logger._log("com.opentext.apps.cc.importhandler.party.PartyImportHandler", Severity.ERROR, e, "Error while executing PartiesImport");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,"PartiesImport");
			}
			finally
			{
				Utilities.cleanAll(partyRecordNode,partyImportResponse,jobIdNode);
			}
		}
	}
	private void updateImportLogs(String partyRegID,String jobID, String errors) 
	{
		int partyRegIDNode=0,statusItemIdResponse=0,updateImportStatusNode=0,updateDataStatusNode=0,updateImportStatusResponse=0,jobIDNode=0;
		try
		{
			if(!Utilities.isStringEmpty(partyRegID))
			{
				SOAPRequestObject statusItemIdRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/PartyImportStatus/operations", "PartyImportStatusRegIDFilter", null, null);
				partyRegIDNode = NomUtil.parseXML("<RegistrationID>"+partyRegID+"</RegistrationID>");
				jobIDNode = NomUtil.parseXML("<JobID>"+jobID+"</JobID>");
				statusItemIdRequest.addParameterAsXml(partyRegIDNode);
				statusItemIdRequest.addParameterAsXml(jobIDNode);
				statusItemIdResponse = statusItemIdRequest.sendAndWait();
				String itemId=Node.getDataWithDefault(NomUtil.getNode(".//PartyImportStatus-id/ItemId", statusItemIdResponse),null);

				SOAPRequestObject updateImportJobRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/PartyImportStatus/operations", "UpdatePartyImportStatus", null, null);
				updateImportStatusNode = NomUtil.parseXML("<PartyImportStatus-id></PartyImportStatus-id>");
				Node.setDataElement(updateImportStatusNode, "ItemId", itemId);
				updateDataStatusNode = NomUtil.parseXML("<PartyImportStatus-update></PartyImportStatus-update>");
				Node.setDataElement(updateDataStatusNode, "LogDetails", errors);
				Node.setDataElement(updateDataStatusNode, "ImportStatus", "InvalidData");
				updateImportJobRequest.addParameterAsXml(updateDataStatusNode);
				updateImportJobRequest.addParameterAsXml(updateImportStatusNode);
				updateImportStatusResponse = updateImportJobRequest.sendAndWait();
			}
		}
		finally
		{
			Utilities.cleanAll(partyRegIDNode,updateImportStatusNode,updateDataStatusNode,statusItemIdResponse,updateImportStatusResponse,jobIDNode);
		}
	}
	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData, String jobId) 
	{

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
				if(row.get(getKey(ImportConstants.PARTY_REGISTRATION_ID)) != null)
				{
					SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/PartyImportStatus/operations", "CreatePartyImportStatus", null, null);
					createNode = NomUtil.parseXML("<PartyImportStatus-create xmlns=\"http://schemas/OpenTextContractCenterImport/PartyImportStatus\"></PartyImportStatus-create>");
					importNode = NomUtil.parseXML("<RelatedJobId></RelatedJobId>");
					child = Node.createElementNS("DataImport-id","","", "http://schemas/OpenTextContractCenterImport/DataImport", importNode);
					Node.appendToChildren(NomUtil.parseXML("<Id>"+jobId+"</Id>"),child);
					Node.appendToChildren(importNode,createNode);
					PartyRegistrationNode = NomUtil.parseXML("<PartyRegistrationID>"+row.get(getKey(ImportConstants.PARTY_REGISTRATION_ID))+"</PartyRegistrationID>");
					Node.appendToChildren(PartyRegistrationNode,createNode);				
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
			logger._log("com.opentext.apps.cc.importhandler.party.PartyImportHandler", Severity.ERROR, e, "Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		}
		finally 
		{
			Utilities.cleanAll(createNode,importNode,child,importStatusNode,PartyRegistrationNode,createImportStatusResponse);
		}
		updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump success"
		return status;
	}
	protected void generateErrorReport(String jodId, Path path,ImportConfiguration importparm)
	{
		int response = 0, nodes[] = null;
		try {
			Map<String, Map<String, String>> rowData = super.getSheetData(path, this.getSheetName(), ImportConstants.PARTY_REGISTRATION_ID);
			if(null == rowData || rowData.size() == 0) return;
			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "getFailedPartyImportItemsByJobID", null, null);
			int input = NomUtil.parseXML("<Input></Input>");
			//String input = "<Input></Input>";
			//String jobId = "<JobID>"+jodId+"</JobID>";
			Node.appendToChildren(NomUtil.parseXML("<JobID>"+jodId+"</JobID>"),input);
			importRequest.addParameterAsXml(input);
			response = importRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//PartyImportStatus", response);
			List<Map<String, String>> errorRecords = new LinkedList<Map<String, String>>();
			for (int i : nodes) 
			{
				String partyRegistrationID=null, importStatus = null, logDetails=null;
				partyRegistrationID = Node.getDataWithDefault(NomUtil.getNode(".//PartyRegistrationID", i),null);
				importStatus = Node.getDataWithDefault(NomUtil.getNode(".//ImportStatus", i),null);
				logDetails =Node.getDataWithDefault(NomUtil.getNode(".//LogDetails", i),null);
				if(rowData.get(partyRegistrationID) != null) 
				{
					Map<String, String> row = rowData.get(partyRegistrationID);
					row.put("ImportStatus", importStatus);
					row.put("Error", logDetails);
					errorRecords.add(row);
				}
			}
			super.createErrorFile(errorRecords,FileUtil.getDownloadReadPath()+jodId+File.separatorChar+ImportHandler.Party.name()+"_Error_Report.xlsx", this.getSheetName(),importparm);
		} 
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
			throw new ContentManagementRuntimeException(e);
		} 
		finally 
		{ 
			if(nodes != null) Utilities.cleanAll(nodes);
			Utilities.cleanAll(response);
		}
	}
}
