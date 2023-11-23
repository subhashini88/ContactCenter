package com.opentext.apps.cc.importhandler.clause;

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

public class ClauseImportHandler extends AbstractImportHandler{

	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(ClauseImportHandler.class);

	public ClauseImportHandler() 
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
		ClauseRecord clauseRecord= null;
		if(row != null && !Utilities.isStringEmpty(row.get(ImportConstants.CLAUSE_LEGACYID)))
		{
			ReportItem reportItem = validator.validate(row, metadata,importConfig.getJobId());
			if (reportItem.getErrors().isEmpty()) 
			{
				clauseRecord = new ClauseRecord(metadata,reportItem);
				clauseRecord.doWork(new ImportEvent(clauseRecord,row));
			}
			else
			{
				updateImportLogs(row.get(ImportConstants.CLAUSE_LEGACYID),importConfig.getJobId(), reportItem.getErrors().toString());
			}
		}
		return clauseRecord;
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
		int clauseRecordNode=0,jobIdNode=0,clausesImportResponse=0;
		if(records.size()>0){
			clauseRecordNode = NomUtil.parseXML("<Clauses></Clauses>");
			for(ImportListener record:records){
				Node.appendToChildren(record.getnode(), clauseRecordNode);
			}
			jobIdNode = NomUtil.parseXML("<JobID>"+super.importConfig.getJobId()+"</JobID>");
			SOAPRequestObject clausesImportRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3","ClausesImport", null, null);
			clausesImportRequest.addParameterAsXml(jobIdNode);
			clausesImportRequest.addParameterAsXml(clauseRecordNode);
			try {
				clausesImportResponse=clausesImportRequest.execute();
			}catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.clause.ClauseImportHandler", Severity.ERROR, e,"Error while executing ClausesImport");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,"ClausesImport");
			}
			finally {
				Utilities.cleanAll(clauseRecordNode,clausesImportResponse,jobIdNode);
			}
		}
	}
	private void updateImportLogs(String legacyID,String jobID, String errors) 
	{
		Document document = NOMDocumentPool.getInstance().lendDocument();
		int legacyIDNode=0,jobIDNode=0,getClauseImportStatusbyLegacyIDResponse=0,clauseImportStatusIDNode=0,clauseImportStatusUpdateNode=0,updateClauseImportStatusResponse=0;
		try
		{
			if(!Utilities.isStringEmpty(legacyID))
			{
				SOAPRequestObject getClauseImportStatusbyLegacyIDRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ClauseImportStatus/operations", "GetClauseImportStatusbyLegacyID", null, null);
				legacyIDNode = NomUtil.parseXML("<LegacyID>"+legacyID+"</LegacyID>");
				jobIDNode = NomUtil.parseXML("<JobID>"+jobID+"</JobID>");
				getClauseImportStatusbyLegacyIDRequest.addParameterAsXml(legacyIDNode);
				getClauseImportStatusbyLegacyIDRequest.addParameterAsXml(jobIDNode);
				getClauseImportStatusbyLegacyIDResponse = getClauseImportStatusbyLegacyIDRequest.sendAndWait();
				String itemId=Node.getDataWithDefault(NomUtil.getNode(".//ClauseImportStatus-id/ItemId", getClauseImportStatusbyLegacyIDResponse),null);

				SOAPRequestObject updateClauseImportStatusRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ClauseImportStatus/operations", "UpdateClauseImportStatus", null, null);
				clauseImportStatusIDNode = document.createElement("ClauseImportStatus-id");
				Node.createTextElement("ItemId", itemId, clauseImportStatusIDNode);
				clauseImportStatusUpdateNode = document.createElement("ClauseImportStatus-update");
				Node.createTextElement("ImportStatus", "InvalidData", clauseImportStatusUpdateNode);
				Node.createTextElement("LogDetails", errors, clauseImportStatusUpdateNode);
				updateClauseImportStatusRequest.addParameterAsXml(clauseImportStatusIDNode);
				updateClauseImportStatusRequest.addParameterAsXml(clauseImportStatusUpdateNode);
				updateClauseImportStatusResponse = updateClauseImportStatusRequest.sendAndWait();
			}
		}
		finally
		{
			Utilities.cleanAll(legacyIDNode,jobIDNode,getClauseImportStatusbyLegacyIDResponse,clauseImportStatusIDNode,clauseImportStatusUpdateNode,updateClauseImportStatusResponse);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
	}
	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData, String jobId)
	{

		boolean status = true; 
		String jobStatus = "Reading ZIPs completed";
		Document document = NOMDocumentPool.getInstance().lendDocument();
		int count=0, createClauseImportStatusResponse=0, clauseImportStatusCreateNode=0, dataImportIDNode=0, legacyIDNode=0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);//entity web service to read job details and update count value
		try
		{
			for(;count<sheetData.size();count++)
			{
				row=sheetData.get(count);
				if(row.get(getKey(ImportConstants.CLAUSE_LEGACYID)) != null){
					SOAPRequestObject createClauseImportStatusRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ClauseImportStatus/operations", "CreateClauseImportStatus", null, null);
					clauseImportStatusCreateNode = NomUtil.parseXML("<ClauseImportStatus-create></ClauseImportStatus-create>");
					int relatedJobIDNode = Node.createElement("RelatedJobID", clauseImportStatusCreateNode);
					dataImportIDNode = Node.createElement("DataImport-id", relatedJobIDNode);					
					Node.createTextElement("Id", jobId, dataImportIDNode);
					legacyIDNode = document.createElement("LegacyID");
					Node.createTextElement("LegacyID", row.get(getKey(ImportConstants.CLAUSE_LEGACYID)), clauseImportStatusCreateNode);
					createClauseImportStatusRequest.addParameterAsXml(clauseImportStatusCreateNode);
					createClauseImportStatusResponse=createClauseImportStatusRequest.sendAndWait();
				}
			}
		}catch (Exception e) 
		{
			status = false;
			jobStatus = "Reading ZIPs failed";
			updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump failed"
			logger._log("com.opentext.apps.cc.importhandler.clause.ClauseImportHandler", Severity.ERROR, e,"Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		}
		finally 
		{
			Utilities.cleanAll(createClauseImportStatusResponse, clauseImportStatusCreateNode, dataImportIDNode, legacyIDNode);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
		updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump success"
		return status;
	}
	protected void generateErrorReport(String jobId, Path path,ImportConfiguration importparm)
	{
		int getFailedClauseImportItemsByJobIDResponse=0, jobIDNode=0, nodes[] = null;
		Document document = NOMDocumentPool.getInstance().lendDocument();
		try {
			Map<String, Map<String, String>> rowData = super.getSheetData(path, this.getSheetName(), ImportConstants.CLAUSE_LEGACYID);
			if(null == rowData || rowData.size() == 0) return;
			SOAPRequestObject getFailedClauseImportItemsByJobIDRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ClauseImportStatus/operations", "GetFailedClauseImportItemsByJobID", null, null);
			jobIDNode = NomUtil.parseXML("<JobID>"+jobId+"</JobID>");
			getFailedClauseImportItemsByJobIDRequest.addParameterAsXml(jobIDNode);
			getFailedClauseImportItemsByJobIDResponse = getFailedClauseImportItemsByJobIDRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//ClauseImportStatus", getFailedClauseImportItemsByJobIDResponse);
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
			Utilities.cleanAll(getFailedClauseImportItemsByJobIDResponse, jobIDNode);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
	}
}
