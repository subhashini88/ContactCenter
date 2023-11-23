package com.opentext.apps.cc.importhandler.relatedCostCenter;

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
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.internal.NOMDocumentPool;
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

public class RelatedCostCenterImportHandler extends AbstractImportHandler{

	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(RelatedCostCenterImportHandler.class);

	public RelatedCostCenterImportHandler() 
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
		RelatedCostCenterRecord costcenterRecord= null;
		if(row != null && !Utilities.isStringEmpty(row.get(ImportConstants.LEGACY_ID)))
		{
			ReportItem reportItem = validator.validate(row, metadata,importConfig.getJobId());
			if (reportItem.getErrors().isEmpty()) 
			{
				costcenterRecord = new RelatedCostCenterRecord(metadata,reportItem);
				costcenterRecord.doWork(new ImportEvent(costcenterRecord,row));
			}
			else
			{
				updateImportLogs(row.get(ImportConstants.LEGACY_ID),importConfig.getJobId(), reportItem.getErrors().toString(),row.get(ImportConstants.CONTRACTNUMBER),row.get(ImportConstants.RELATEDCOSTCENTER_ID));
			}
		}
		return costcenterRecord;
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
		int relatedCostCenterRecordNode=0,jobIdNode=0,relatedCostCentersImportResponse=0;
		if(records.size()>0){
			relatedCostCenterRecordNode = NomUtil.parseXML("<RelatedCostCenters></RelatedCostCenters>");
			for(ImportListener record:records){
				Node.appendToChildren(record.getnode(), relatedCostCenterRecordNode);
			}
			jobIdNode = NomUtil.parseXML("<JobID>"+super.importConfig.getJobId()+"</JobID>");
			SOAPRequestObject relatedCostCentersImportRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3","RelatedCostCenterImport", null, null);
			relatedCostCentersImportRequest.addParameterAsXml(jobIdNode);
			relatedCostCentersImportRequest.addParameterAsXml(relatedCostCenterRecordNode);
			try {
				relatedCostCentersImportResponse=relatedCostCentersImportRequest.execute();
			}catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.relatedCostCenter.relatedCostCenterImportHandler", Severity.ERROR, e,"Error while executing relatedCostCentersImport");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,"relatedCostCentersImport");
			}
			finally {
				Utilities.cleanAll(relatedCostCenterRecordNode,relatedCostCentersImportResponse,jobIdNode);
			}
		}
	}
	private void updateImportLogs(String legacyID,String jobID, String errors,String ContractID,String CCID) 
	{
		Document document = NOMDocumentPool.getInstance().lendDocument();
		int legacyIDNode=0,jobIDNode=0,getrelatedCostCenterImportStatusbyLegacyIDResponse=0,relatedCostCenterImportStatusIDNode=0,relatedCostCenterImportStatusUpdateNode=0,updaterelatedCostCenterImportStatusResponse=0;
		try
		{
			if(!Utilities.isStringEmpty(legacyID))
			{
				SOAPRequestObject getrelatedCostCenterImportStatusbyLegacyIDRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/RelatedCostCenterImportStatus/operations", "RelatedCostCenterIDFilter", null, null);
				legacyIDNode = NomUtil.parseXML("<LegacyID>"+legacyID+"</LegacyID>");
				jobIDNode = NomUtil.parseXML("<JobID>"+jobID+"</JobID>");
				getrelatedCostCenterImportStatusbyLegacyIDRequest.addParameterAsXml(legacyIDNode);
				getrelatedCostCenterImportStatusbyLegacyIDRequest.addParameterAsXml(jobIDNode);
				getrelatedCostCenterImportStatusbyLegacyIDResponse = getrelatedCostCenterImportStatusbyLegacyIDRequest.sendAndWait();
				String itemId=Node.getDataWithDefault(NomUtil.getNode(".//RelatedCostCenterImportStatus-id/ItemId", getrelatedCostCenterImportStatusbyLegacyIDResponse),null);

				SOAPRequestObject updaterelatedCostCenterImportStatusRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/RelatedCostCenterImportStatus/operations", "UpdateRelatedCostCenterImportStatus", null, null);
				relatedCostCenterImportStatusIDNode = document.createElement("RelatedCostCenterImportStatus-id");
				Node.createTextElement("ItemId", itemId, relatedCostCenterImportStatusIDNode);
				relatedCostCenterImportStatusUpdateNode = document.createElement("RelatedCostCenterImportStatus-update");
				Node.createTextElement("ImportStatus", "InvalidData", relatedCostCenterImportStatusUpdateNode);
				Node.createTextElement("LogDetails", errors, relatedCostCenterImportStatusUpdateNode);
				Node.createTextElement("ContractNumber", ContractID, relatedCostCenterImportStatusUpdateNode);
				Node.createTextElement("RelatedCostCenterID", CCID, relatedCostCenterImportStatusUpdateNode);
				updaterelatedCostCenterImportStatusRequest.addParameterAsXml(relatedCostCenterImportStatusIDNode);
				updaterelatedCostCenterImportStatusRequest.addParameterAsXml(relatedCostCenterImportStatusUpdateNode);
				updaterelatedCostCenterImportStatusResponse = updaterelatedCostCenterImportStatusRequest.sendAndWait();
			}
		}
		finally
		{
			Utilities.cleanAll(legacyIDNode,jobIDNode,getrelatedCostCenterImportStatusbyLegacyIDResponse,relatedCostCenterImportStatusIDNode,relatedCostCenterImportStatusUpdateNode,updaterelatedCostCenterImportStatusResponse);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
	}
	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData, String jobId)
	{

		boolean status = true; 
		String jobStatus = "Reading ZIPs completed";
		Document document = NOMDocumentPool.getInstance().lendDocument();
		int count=0, createrelatedCostCenterImportStatusResponse=0, relatedCostCenterImportStatusCreateNode=0, dataImportIDNode=0, legacyIDNode=0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);//entity web service to read job details and update count value
		try
		{
			for(;count<sheetData.size();count++)
			{
				row=sheetData.get(count);
				if(row.get(getKey(ImportConstants.LEGACY_ID)) != null){
					SOAPRequestObject createrelatedCostCenterImportStatusRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/RelatedCostCenterImportStatus/operations", "CreateRelatedCostCenterImportStatus", null, null);
					relatedCostCenterImportStatusCreateNode = NomUtil.parseXML("<RelatedCostCenterImportStatus-create></RelatedCostCenterImportStatus-create>");
					int relatedJobIDNode = Node.createElement("RelatedJobID", relatedCostCenterImportStatusCreateNode);
					dataImportIDNode = Node.createElement("DataImport-id", relatedJobIDNode);					
					Node.createTextElement("Id", jobId, dataImportIDNode);
					legacyIDNode = document.createElement("LegacyID");
					Node.createTextElement("LegacyID", row.get(getKey(ImportConstants.LEGACY_ID)), relatedCostCenterImportStatusCreateNode);
					createrelatedCostCenterImportStatusRequest.addParameterAsXml(relatedCostCenterImportStatusCreateNode);
					createrelatedCostCenterImportStatusResponse=createrelatedCostCenterImportStatusRequest.sendAndWait();
				}
			}
		}catch (Exception e) 
		{
			status = false;
			jobStatus = "Reading ZIPs failed";
			updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump failed"
			logger._log("com.opentext.apps.cc.importhandler.relatedCostCenter.relatedCostCenterImportHandler", Severity.ERROR, e,"Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		}
		finally 
		{
			Utilities.cleanAll(createrelatedCostCenterImportStatusResponse, relatedCostCenterImportStatusCreateNode, dataImportIDNode, legacyIDNode);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
		updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump success"
		return status;
	}
	protected void generateErrorReport(String jobId, Path path,ImportConfiguration importparm)
	{
		int getFailedrelatedCostCenterImportItemsByJobIDResponse=0, jobIDNode=0, nodes[] = null;
		Document document = NOMDocumentPool.getInstance().lendDocument();
		try {
			Map<String, Map<String, String>> rowData = super.getSheetData(path, this.getSheetName(), ImportConstants.LEGACY_ID);
			if(null == rowData || rowData.size() == 0) return;
			SOAPRequestObject getFailedrelatedCostCenterImportItemsByJobIDRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/RelatedCostCenterImportStatus/operations", "ReadRelatedCostCenterImportStatusbyJobID", null, null);
			jobIDNode = NomUtil.parseXML("<JobID>"+jobId+"</JobID>");
			getFailedrelatedCostCenterImportItemsByJobIDRequest.addParameterAsXml(jobIDNode);
			getFailedrelatedCostCenterImportItemsByJobIDResponse = getFailedrelatedCostCenterImportItemsByJobIDRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RelatedCostCenterImportStatus", getFailedrelatedCostCenterImportItemsByJobIDResponse);
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
			Utilities.cleanAll(getFailedrelatedCostCenterImportItemsByJobIDResponse, jobIDNode);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
	}
}
