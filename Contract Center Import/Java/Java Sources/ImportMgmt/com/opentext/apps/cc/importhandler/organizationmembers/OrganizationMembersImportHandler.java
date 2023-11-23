package com.opentext.apps.cc.importhandler.organizationmembers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.ReportListener;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;



public class OrganizationMembersImportHandler extends AbstractImportHandler {
	
	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(OrganizationMembersImportHandler.class);

	public OrganizationMembersImportHandler()
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
		OrganizationMembersRecord organizationMembersRecord= null;
		if(row != null)
		{
			ReportItem reportItem = validator.validate(row, metadata,importConfig.getJobId());
			if (reportItem.getErrors().isEmpty()) 
			{
				organizationMembersRecord = new OrganizationMembersRecord(metadata,reportItem);
				organizationMembersRecord.doWork(new ImportEvent(organizationMembersRecord,row));
			}
			else
			{
				updateImportLogs(row.get(ImportConstants.LEGACY_ID),importConfig.getJobId(), reportItem.getErrors().toString());
			}
		}
		return organizationMembersRecord;
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
		int orgMemberRecordNode=0,jobIdNode=0,orgMemberImportResponse=0;
		if(records.size()>0)
		{
			orgMemberRecordNode = NomUtil.parseXML("<OrganizationMembers></OrganizationMembers>");
			for(ImportListener record:records)
			{
				Node.appendToChildren(record.getnode(), orgMemberRecordNode);
			}
			jobIdNode = NomUtil.parseXML("<JobID>"+super.importConfig.getJobId()+"</JobID>");
			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3","OrganizationMembersImport", null, null);
			importRequest.addParameterAsXml(jobIdNode);
			importRequest.addParameterAsXml(orgMemberRecordNode);
			try
			{
				orgMemberImportResponse=importRequest.execute();
			}
			catch (Exception e) 
			{
				logger._log("com.opentext.apps.cc.importhandler.organizationmembers.OrganizationMembersImportHandler", Severity.ERROR, e, "Error while executing OrganizationMembersImport");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,"OrganizationMembersImport");
			}
			finally
			{
				Utilities.cleanAll(orgMemberRecordNode,orgMemberImportResponse,jobIdNode);
			}
		}
	}
	private void updateImportLogs(String LegacyID,String jobID, String errors) 
	{
		int orgMemberLegacyID=0,statusItemIdResponse=0,updateImportStatusNode=0,updateDataStatusNode=0,updateImportStatusResponse=0,jobIDNode=0;
		try
		{
			if(!Utilities.isStringEmpty(LegacyID))
			{
				SOAPRequestObject statusItemIdRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/OrganizationMembersImportStatus/operations", "OrgMemberImportStatusByLegacyID", null, null);
				orgMemberLegacyID = NomUtil.parseXML("<LegacyID>"+LegacyID+"</LegacyID>");
				jobIDNode = NomUtil.parseXML("<JobID>"+jobID+"</JobID>");
				statusItemIdRequest.addParameterAsXml(orgMemberLegacyID);
				statusItemIdRequest.addParameterAsXml(jobIDNode);
				statusItemIdResponse = statusItemIdRequest.sendAndWait();
				String itemId=Node.getDataWithDefault(NomUtil.getNode(".//OrganizationMembersImportStatus-id/ItemId", statusItemIdResponse),null);

				SOAPRequestObject updateImportJobRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/OrganizationMembersImportStatus/operations", "UpdateOrganizationMembersImportStatus", null, null);
				updateImportStatusNode = NomUtil.parseXML("<OrganizationMembersImportStatus-id></OrganizationMembersImportStatus-id>");
				Node.setDataElement(updateImportStatusNode, "ItemId", itemId);
				updateDataStatusNode = NomUtil.parseXML("<OrganizationMembersImportStatus-update></OrganizationMembersImportStatus-update>");
				Node.setDataElement(updateDataStatusNode, "LogDetails", errors);
				Node.setDataElement(updateDataStatusNode, "ImportStatus", "InvalidData");
				updateImportJobRequest.addParameterAsXml(updateDataStatusNode);
				updateImportJobRequest.addParameterAsXml(updateImportStatusNode);
				updateImportStatusResponse = updateImportJobRequest.sendAndWait();
			}
		}
		finally
		{
			Utilities.cleanAll(orgMemberLegacyID,updateImportStatusNode,updateDataStatusNode,statusItemIdResponse,updateImportStatusResponse,jobIDNode);
		}
	}
	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData, String jobId) 
	{

		boolean status = true; 
		String jobStatus = "Reading ZIPs completed";
		int count=0,createNode=0,importNode=0,child=0,importStatusNode=0,legacyIdNode=0,createImportStatusResponse=0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);//entity web service to read job details and update count value
		try
		{
			for(;count<sheetData.size();count++)
			{
				row=sheetData.get(count);
				if(row.get(getKey(ImportConstants.LEGACY_ID)) != null)
				{
					SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/OrganizationMembersImportStatus/operations", "CreateOrganizationMembersImportStatus", null, null);
					createNode = NomUtil.parseXML("<OrganizationMembersImportStatus-create xmlns=\"http://schemas/OpenTextContractCenterImport/OrganizationMembersImportStatus\"></OrganizationMembersImportStatus-create>");
					importNode = NomUtil.parseXML("<RelatedJobID></RelatedJobID>");
					child = Node.createElementNS("DataImport-id","","", "http://schemas/OpenTextContractCenterImport/DataImport", importNode);
					Node.appendToChildren(NomUtil.parseXML("<Id>"+jobId+"</Id>"),child);
					Node.appendToChildren(importNode,createNode);
					legacyIdNode = NomUtil.parseXML("<LegacyID>"+row.get(getKey(ImportConstants.LEGACY_ID))+"</LegacyID>");		
					Node.appendToChildren(legacyIdNode,createNode);
					Node.appendToChildren(NomUtil.parseXML("<PrimaryOrganizationCode>"+row.get(getKey(ImportConstants.PRIMARY_ORGANIZATION_CODE))+"</PrimaryOrganizationCode>"),createNode);
					if(Objects.nonNull(row.get(getKey(ImportConstants.USER_ID)))) {
					Node.appendToChildren(NomUtil.parseXML("<UserID>"+row.get(getKey(ImportConstants.USER_ID))+"</UserID>"),createNode);
					}
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
			logger._log("com.opentext.apps.cc.importhandler.organizatrionembers.OrganizationMembersImportHandler", Severity.ERROR, e, "Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		}
		finally 
		{
			Utilities.cleanAll(createNode,importNode,child,importStatusNode,legacyIdNode,createImportStatusResponse);
		}
		updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump success"
		return status;
	}
	protected void generateErrorReport(String jobId, Path path,ImportConfiguration importparm)
	{
		int response = 0, nodes[] = null;
		int createNode=0;
		try {
			Map<String, Map<String, String>> rowData = super.getSheetData(path, this.getSheetName(), ImportConstants.LEGACY_ID);
			if(null == rowData || rowData.size() == 0) return;
			String[] paramNames = { "JobID"};
			Object[] paramValues = { jobId};
			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/OrganizationMembersImportStatus/operations", "GetFailedOrgMemberImportsByJobID", paramNames, paramValues);
			//createNode = NomUtil.parseXML("<JobID>"+jobId+"</JobID>");
			//importRequest.addParameterAsXml(createNode);
			response = importRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//OrganizationMembersImportStatus", response);
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
			super.createErrorFile(errorRecords,FileUtil.getDownloadReadPath()+jobId+File.separatorChar+this.getSheetName()+"_Error_Report.xlsx", this.getSheetName(),importparm);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ContentManagementRuntimeException(e);
		} finally { 
			if(nodes != null) Utilities.cleanAll(nodes);
			Utilities.cleanAll(response);
		}
	}
}
