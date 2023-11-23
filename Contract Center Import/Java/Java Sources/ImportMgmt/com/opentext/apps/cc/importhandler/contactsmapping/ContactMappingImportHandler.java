package com.opentext.apps.cc.importhandler.contactsmapping;

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

public class ContactMappingImportHandler extends AbstractImportHandler
{

	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(ContactMappingImportHandler.class);

	public ContactMappingImportHandler()
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
		ContactMappingRecord contactRecord= null;

		if(row != null)
		{
			ReportItem reportItem = validator.validate(row, metadata,importConfig.getJobId());
			if (reportItem.getErrors().isEmpty()) 
			{
				contactRecord = new ContactMappingRecord(metadata,reportItem);
				contactRecord.doWork(new ImportEvent(contactRecord,row));
			}
			else 
			{
				updateImportLogs(row.get(ImportConstants.LEGACY_ID),importConfig.getJobId(), reportItem.getErrors().toString());
			}
		}
		return contactRecord;
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
		int contactRecordNode=0,jobIdNode=0,contactImportResponse=0;
		if(records.size()>0)
		{
			contactRecordNode = NomUtil.parseXML("<Contacts></Contacts>");
			for(ImportListener record:records)
			{
				Node.appendToChildren(record.getnode(), contactRecordNode);
			}
			jobIdNode = NomUtil.parseXML("<JobID>"+super.importConfig.getJobId()+"</JobID>");
			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3","ContactsImport", null, null);
			importRequest.addParameterAsXml(jobIdNode);
			importRequest.addParameterAsXml(contactRecordNode);
			try
			{
				contactImportResponse=importRequest.execute();
			}
			catch (Exception e) 
			{
				logger._log("com.opentext.apps.cc.importhandler.contactsmapping.ContactMappingImportHandler", Severity.ERROR, e, "Error while executing ContactsImport.");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,"ContactsImport");
			}
			finally 
			{
				Utilities.cleanAll(contactRecordNode,contactImportResponse,jobIdNode);
			}
		}
	}
	private void updateImportLogs(String legacyID,String jobID, String errors)
	{
		int contactIDNode=0,statusItemIdResponse=0,updateImportStatusNode=0,updateDataStatusNode=0,updateImportStatusResponse=0,jobIDNode=0;
		try
		{
			if(!Utilities.isStringEmpty(legacyID))
			{
				SOAPRequestObject statusItemIdRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ContactImportStatus/operations", "ContactImportStatusContactIDFilter", null, null);
				contactIDNode = NomUtil.parseXML("<ContactID>"+legacyID+"</ContactID>");
				jobIDNode = NomUtil.parseXML("<JobID>"+jobID+"</JobID>");
				statusItemIdRequest.addParameterAsXml(contactIDNode);
				statusItemIdRequest.addParameterAsXml(jobIDNode);
				statusItemIdResponse = statusItemIdRequest.sendAndWait();
				String itemId=Node.getDataWithDefault(NomUtil.getNode(".//ContactImportStatus-id/ItemId", statusItemIdResponse),null);

				SOAPRequestObject updateImportJobRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ContactImportStatus/operations", "UpdateContactImportStatus", null, null);
				updateImportStatusNode = NomUtil.parseXML("<ContactImportStatus-id></ContactImportStatus-id>");
				Node.setDataElement(updateImportStatusNode, "ItemId", itemId);
				updateDataStatusNode = NomUtil.parseXML("<ContactImportStatus-update></ContactImportStatus-update>");
				Node.setDataElement(updateDataStatusNode, "LogDetails", errors);
				Node.setDataElement(updateDataStatusNode, "ImportStatus", "InvalidData");
				updateImportJobRequest.addParameterAsXml(updateDataStatusNode);
				updateImportJobRequest.addParameterAsXml(updateImportStatusNode);
				updateImportStatusResponse = updateImportJobRequest.sendAndWait();
			}
		}
		finally
		{
			Utilities.cleanAll(contactIDNode,updateImportStatusNode,updateDataStatusNode,statusItemIdResponse,updateImportStatusResponse,jobIDNode);
		}
	}

	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData, String jobId) 
	{
		boolean status = true; 
		String jobStatus = "Reading ZIPs completed";
		int count=0,createNode=0,jobIDNode=0,child=0,importStatusNode=0,legacyIDNode=0,createImportStatusResponse=0,PartyRegIDNode=0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);//entity web service to read job details and update count value
		try
		{
			for(;count<sheetData.size();count++)
			{
				row=sheetData.get(count);
				if(row.get(getKey(ImportConstants.LEGACY_ID)) != null)
				{
					SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ContactImportStatus/operations", "CreateContactImportStatus", null, null);
					createNode = NomUtil.parseXML("<ContactImportStatus-create xmlns=\"http://schemas/OpenTextContractCenterImport/ContactImportStatus\"></ContactImportStatus-create>");
					jobIDNode = NomUtil.parseXML("<RelatedJobID></RelatedJobID>");
					child = Node.createElementNS("DataImport-id","","", "http://schemas/OpenTextContractCenterImport/DataImport", jobIDNode);
					Node.appendToChildren(NomUtil.parseXML("<Id>"+jobId+"</Id>"),child);
					Node.appendToChildren(jobIDNode,createNode);
					legacyIDNode = NomUtil.parseXML("<ContactID>"+row.get(getKey(ImportConstants.LEGACY_ID))+"</ContactID>");
					PartyRegIDNode = NomUtil.parseXML("<PartyRegistrationID>"+row.get(getKey(ImportConstants.PARTY_REGISTRATION_ID))+"</PartyRegistrationID>");
					Node.appendToChildren(legacyIDNode,createNode);	
					Node.appendToChildren(PartyRegIDNode,createNode);
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
			logger._log("com.opentext.apps.cc.importhandler.contactsmapping.ContactMappingImportHandler", Severity.ERROR, e, "Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		}
		finally 
		{
			Utilities.cleanAll(createNode,jobIDNode,child,importStatusNode,legacyIDNode,PartyRegIDNode,createImportStatusResponse);
		}
		updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump success"
		return status;
	}
	@SuppressWarnings("deprecation")
	protected void generateErrorReport(String jodId, Path path,ImportConfiguration importparm) 
	{
		int response = 0, nodes[] = null;

		try
		{
			Map<String, Map<String, String>> rowData = super.getSheetData(path, this.getSheetName(), ImportConstants.LEGACY_ID);
			if(null == rowData || rowData.size() == 0) return;
			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "getFailedContactImportItemsByJobID", null, null);
			int input = NomUtil.parseXML("<Input></Input>");
			//String input = "<Input></Input>";
			//String jobId = "<JobID>"+jodId+"</JobID>";
			Node.appendToChildren(NomUtil.parseXML("<JobID>"+jodId+"</JobID>"),input);
			importRequest.addParameterAsXml(input);
			response = importRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//ContactImportStatus", response);
			List<Map<String, String>> errorRecords = new LinkedList<Map<String, String>>();
			for (int i : nodes)
			{
				String contactID=null, importStatus = null, logDetails=null;
				contactID = Node.getDataWithDefault(NomUtil.getNode(".//ContactID", i),null);
				importStatus = Node.getDataWithDefault(NomUtil.getNode(".//ImportStatus", i),null);
				logDetails =Node.getDataWithDefault(NomUtil.getNode(".//LogDetails", i),null);
				if(rowData.get(contactID) != null) {
					Map<String, String> row = rowData.get(contactID);
					row.put("ImportStatus", importStatus);
					row.put("Error", logDetails);
					errorRecords.add(row);
				}
			}
			super.createErrorFile(errorRecords,FileUtil.getDownloadReadPath()+jodId+File.separatorChar+ImportHandler.ContactMapping.name()+"_Error_Report.xlsx", this.getSheetName(),importparm);
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
