package com.opentext.apps.cc.importhandler.contractType;

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
import com.opentext.apps.cc.importhandler.contractType.ContractTypeImportHandler;
import com.opentext.apps.cc.importhandler.contractType.ContractTypeRecord;
import com.opentext.apps.cc.importhandler.contractType.ImportConstants;
import com.opentext.apps.cc.importhandler.contractType.ImportValidator;
import com.opentext.apps.cc.importhandler.contractType.MetadataInitializer;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;

public class ContractTypeImportHandler extends AbstractImportHandler{
	
	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(ContractTypeImportHandler.class);

	public ContractTypeImportHandler() 
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
		ContractTypeRecord contractTyperecord= null;
		if(row != null && !Utilities.isStringEmpty(row.get(ImportConstants.LEGACY_ID)))
		{
			ReportItem reportItem = validator.validate(row, metadata,importConfig.getJobId());
			if (reportItem.getErrors().isEmpty()) 
			{
				contractTyperecord = new ContractTypeRecord(metadata,reportItem);
				contractTyperecord.doWork(new ImportEvent(contractTyperecord,row));
			}
			else
			{
				updateImportLogs(row.get(ImportConstants.LEGACY_ID),importConfig.getJobId(), reportItem.getErrors().toString());
			}
		}
		return contractTyperecord;
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
		int contractTypeRecordNode=0,jobIdNode=0,contractTypesResponse=0;
		if(records.size()>0){
			contractTypeRecordNode = NomUtil.parseXML("<ContractTypes></ContractTypes>");
			
			for(ImportListener record:records){
				Node.appendToChildren(record.getnode(), contractTypeRecordNode);
			}
			
			jobIdNode = NomUtil.parseXML("<JobID>"+super.importConfig.getJobId()+"</JobID>");
			SOAPRequestObject clausesImportRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3","ContractTypesImport", null, null);
			clausesImportRequest.addParameterAsXml(jobIdNode);
			clausesImportRequest.addParameterAsXml(contractTypeRecordNode);
			
			try {
				contractTypesResponse=clausesImportRequest.execute();
			}catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.contractType", Severity.ERROR, e,"Error while executing ContractTypesImport");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,"ContractTypesImport");
			}
			finally {
				Utilities.cleanAll(contractTypeRecordNode,contractTypesResponse,jobIdNode);
			}
		}
	}
	private void updateImportLogs(String legacyID,String jobID, String errors) 
	{
		Document document = NOMDocumentPool.getInstance().lendDocument();
		int legacyIDNode=0, jobIDNode=0, getContractTypeImportStatusbyLegacyIDResponse=0, contractTypeImportStatusIDNode=0, contractTypeImportStatusUpdateNode=0, updateContractTypeImportStatusResponse=0;
		try
		{
			if(!Utilities.isStringEmpty(legacyID))
			{
				SOAPRequestObject getClauseImportStatusbyLegacyIDRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ContractTypeImportStatus/operations", "GetContractTypeImportStatusbyLegacyID", null, null);
				legacyIDNode = NomUtil.parseXML("<LegacyID>"+legacyID+"</LegacyID>");
				jobIDNode = NomUtil.parseXML("<JobID>"+jobID+"</JobID>");
				getClauseImportStatusbyLegacyIDRequest.addParameterAsXml(legacyIDNode);
				getClauseImportStatusbyLegacyIDRequest.addParameterAsXml(jobIDNode);
				getContractTypeImportStatusbyLegacyIDResponse = getClauseImportStatusbyLegacyIDRequest.sendAndWait();
				String itemId=Node.getDataWithDefault(NomUtil.getNode(".//ContractTypeImportStatus-id/ItemId", getContractTypeImportStatusbyLegacyIDResponse),null);

				SOAPRequestObject updateClauseImportStatusRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ContractTypeImportStatus/operations", "UpdateContractTypeImportStatus", null, null);
				contractTypeImportStatusIDNode = document.createElement("ContractTypeImportStatus-id");
				Node.createTextElement("ItemId", itemId, contractTypeImportStatusIDNode);
				contractTypeImportStatusUpdateNode = document.createElement("ContractTypeImportStatus-update");
				Node.createTextElement("ImportStatus", "InvalidData", contractTypeImportStatusUpdateNode);
				Node.createTextElement("LogDetails", errors, contractTypeImportStatusUpdateNode);
				updateClauseImportStatusRequest.addParameterAsXml(contractTypeImportStatusIDNode);
				updateClauseImportStatusRequest.addParameterAsXml(contractTypeImportStatusUpdateNode);
				updateContractTypeImportStatusResponse = updateClauseImportStatusRequest.sendAndWait();
			}
		}
		finally
		{
			Utilities.cleanAll(legacyIDNode,jobIDNode,getContractTypeImportStatusbyLegacyIDResponse,contractTypeImportStatusIDNode,contractTypeImportStatusUpdateNode,updateContractTypeImportStatusResponse);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
	}
	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData, String jobId)
	{

		boolean status = true; 
		String jobStatus = "Reading ZIPs completed";
		Document document = NOMDocumentPool.getInstance().lendDocument();
		int count=0, createContractTypeImportStatusResponse=0, contractTypeImportStatusCreateNode=0, dataImportIDNode=0, legacyIDNode=0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);
		try
		{
			for(;count<sheetData.size();count++)
			{
				row=sheetData.get(count);
				if(row.get(getKey(ImportConstants.LEGACY_ID)) != null){
					SOAPRequestObject createClauseImportStatusRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ContractTypeImportStatus/operations", "CreateContractTypeImportStatus", null, null);
					contractTypeImportStatusCreateNode = NomUtil.parseXML("<ContractTypeImportStatus-create></ContractTypeImportStatus-create>");
					int relatedJobIDNode = Node.createElement("RelatedJobID", contractTypeImportStatusCreateNode);
					dataImportIDNode = Node.createElement("DataImport-id", relatedJobIDNode);					
					Node.createTextElement("Id", jobId, dataImportIDNode);
					legacyIDNode = document.createElement("LegacyID");
					Node.createTextElement("LegacyID", row.get(getKey(ImportConstants.LEGACY_ID)), contractTypeImportStatusCreateNode);
					createClauseImportStatusRequest.addParameterAsXml(contractTypeImportStatusCreateNode);
					createContractTypeImportStatusResponse=createClauseImportStatusRequest.sendAndWait();
				}
			}
		}catch (Exception e) 
		{
			status = false;
			jobStatus = "Reading ZIPs failed";
			updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump failed"
			logger._log("com.opentext.apps.cc.importhandler.contractType", Severity.ERROR, e,"Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		}
		finally 
		{
			Utilities.cleanAll(createContractTypeImportStatusResponse, contractTypeImportStatusCreateNode, dataImportIDNode, legacyIDNode);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
		updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump success"
		return status;
	}
}
