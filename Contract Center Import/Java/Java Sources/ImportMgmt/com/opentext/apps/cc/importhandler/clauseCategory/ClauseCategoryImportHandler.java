package com.opentext.apps.cc.importhandler.clauseCategory;

import java.util.Collection;
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
import com.opentext.apps.cc.importcontent.ImportConfiguration;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.ReportListener;
import com.opentext.apps.cc.importhandler.clauseCategory.ClauseCategoryImportHandler;
import com.opentext.apps.cc.importhandler.clauseCategory.ClauseCategoryRecord;
import com.opentext.apps.cc.importhandler.clauseCategory.ImportConstants;
import com.opentext.apps.cc.importhandler.clauseCategory.ImportValidator;
import com.opentext.apps.cc.importhandler.clauseCategory.MetadataInitializer;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;

public class ClauseCategoryImportHandler extends AbstractImportHandler{
	
	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(ClauseCategoryImportHandler.class);

	public ClauseCategoryImportHandler() 
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
		ClauseCategoryRecord clauseCategoryrecord= null;
		if(row != null && !Utilities.isStringEmpty(row.get(ImportConstants.LEGACY_ID)))
		{
			ReportItem reportItem = validator.validate(row, metadata,importConfig.getJobId());
			if (reportItem.getErrors().isEmpty()) 
			{
				clauseCategoryrecord = new ClauseCategoryRecord(metadata,reportItem);
				clauseCategoryrecord.doWork(new ImportEvent(clauseCategoryrecord,row));
			}
			else
			{
				updateImportLogs(row.get(ImportConstants.LEGACY_ID),importConfig.getJobId(), reportItem.getErrors().toString());
			}
		}
		return clauseCategoryrecord;
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
		int clauseCategoryRecordNode=0,jobIdNode=0,clauseCategorysResponse=0;
		if(records.size()>0){
			clauseCategoryRecordNode = NomUtil.parseXML("<ClauseCategories></ClauseCategories>");
			
			for(ImportListener record:records){
				Node.appendToChildren(record.getnode(), clauseCategoryRecordNode);
			}
			
			jobIdNode = NomUtil.parseXML("<JobID>"+super.importConfig.getJobId()+"</JobID>");
			SOAPRequestObject clausesImportRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3","ClauseCategoryImport", null, null);
			clausesImportRequest.addParameterAsXml(jobIdNode);
			clausesImportRequest.addParameterAsXml(clauseCategoryRecordNode);
			
			try {
				clauseCategorysResponse=clausesImportRequest.execute();
			}catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.clauseCategory", Severity.ERROR, e,"Error while executing ClauseCategoryImport");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,"ClauseCategoryImport");
			}
			finally {
				Utilities.cleanAll(clauseCategoryRecordNode,clauseCategorysResponse,jobIdNode);
			}
		}
	}
	private void updateImportLogs(String legacyID,String jobID, String errors) 
	{
		Document document = NOMDocumentPool.getInstance().lendDocument();
		int legacyIDNode=0, jobIDNode=0, getClauseCategoryImportStatusbyLegacyIDResponse=0, clauseCategoryImportStatusIDNode=0, clauseCategoryImportStatusUpdateNode=0, updateClauseCategoryImportStatusResponse=0;
		try
		{
			if(!Utilities.isStringEmpty(legacyID))
			{
				SOAPRequestObject getClauseImportStatusbyLegacyIDRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ClauseCategoryImportStatus/operations", "GetClauseCategoryImportStatusbyLegacyID", null, null);
				legacyIDNode = NomUtil.parseXML("<LegacyID>"+legacyID+"</LegacyID>");
				jobIDNode = NomUtil.parseXML("<JobID>"+jobID+"</JobID>");
				getClauseImportStatusbyLegacyIDRequest.addParameterAsXml(legacyIDNode);
				getClauseImportStatusbyLegacyIDRequest.addParameterAsXml(jobIDNode);
				getClauseCategoryImportStatusbyLegacyIDResponse = getClauseImportStatusbyLegacyIDRequest.sendAndWait();
				String itemId=Node.getDataWithDefault(NomUtil.getNode(".//ClauseCategoryImportStatus-id/ItemId", getClauseCategoryImportStatusbyLegacyIDResponse),null);

				SOAPRequestObject updateClauseImportStatusRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ClauseCategoryImportStatus/operations", "UpdateClauseCategoryImportStatus", null, null);
				clauseCategoryImportStatusIDNode = document.createElement("ClauseCategoryImportStatus-id");
				Node.createTextElement("ItemId", itemId, clauseCategoryImportStatusIDNode);
				clauseCategoryImportStatusUpdateNode = document.createElement("ClauseCategoryImportStatus-update");
				Node.createTextElement("ImportStatus", "InvalidData", clauseCategoryImportStatusUpdateNode);
				Node.createTextElement("LogDetails", errors, clauseCategoryImportStatusUpdateNode);
				updateClauseImportStatusRequest.addParameterAsXml(clauseCategoryImportStatusIDNode);
				updateClauseImportStatusRequest.addParameterAsXml(clauseCategoryImportStatusUpdateNode);
				updateClauseCategoryImportStatusResponse = updateClauseImportStatusRequest.sendAndWait();
			}
		}
		finally
		{
			Utilities.cleanAll(legacyIDNode,jobIDNode,getClauseCategoryImportStatusbyLegacyIDResponse,clauseCategoryImportStatusIDNode,clauseCategoryImportStatusUpdateNode,updateClauseCategoryImportStatusResponse);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
	}
	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData, String jobId)
	{

		boolean status = true; 
		String jobStatus = "Reading ZIPs completed";
		Document document = NOMDocumentPool.getInstance().lendDocument();
		int count=0, createClauseCategoryImportStatusResponse=0, clauseCategoryImportStatusCreateNode=0, dataImportIDNode=0, legacyIDNode=0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);
		try
		{
			for(;count<sheetData.size();count++)
			{
				row=sheetData.get(count);
				if(row.get(getKey(ImportConstants.LEGACY_ID)) != null){
					SOAPRequestObject createClauseImportStatusRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ClauseCategoryImportStatus/operations", "CreateClauseCategoryImportStatus", null, null);
					clauseCategoryImportStatusCreateNode = NomUtil.parseXML("<ClauseCategoryImportStatus-create></ClauseCategoryImportStatus-create>");
					int relatedJobIDNode = Node.createElement("RelatedJobID", clauseCategoryImportStatusCreateNode);
					dataImportIDNode = Node.createElement("DataImport-id", relatedJobIDNode);					
					Node.createTextElement("Id", jobId, dataImportIDNode);
					legacyIDNode = document.createElement("LegacyID");
					Node.createTextElement("LegacyID", row.get(getKey(ImportConstants.LEGACY_ID)), clauseCategoryImportStatusCreateNode);
					createClauseImportStatusRequest.addParameterAsXml(clauseCategoryImportStatusCreateNode);
					createClauseCategoryImportStatusResponse=createClauseImportStatusRequest.sendAndWait();
				}
			}
		}catch (Exception e) 
		{
			status = false;
			jobStatus = "Reading ZIPs failed";
			updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump failed"
			logger._log("com.opentext.apps.cc.importhandler.clauseCategory", Severity.ERROR, e,"Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		}
		finally 
		{
			Utilities.cleanAll(createClauseCategoryImportStatusResponse, clauseCategoryImportStatusCreateNode, dataImportIDNode, legacyIDNode);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
		updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump success"
		return status;
	}
}
