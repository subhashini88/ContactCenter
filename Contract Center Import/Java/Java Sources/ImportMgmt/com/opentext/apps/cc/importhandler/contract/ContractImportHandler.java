package com.opentext.apps.cc.importhandler.contract;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import com.opentext.apps.cc.importcontent.ImportUtils;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.ReportListener;
import com.opentext.apps.cc.importhandler.contract.match.ContractMatchException;
import com.opentext.apps.cc.importhandler.contract.match.ContractMatcher;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;


public class ContractImportHandler extends AbstractImportHandler {
	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private final ContractMatcher matcher;
	protected  Properties properties;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(ContractImportHandler.class);

	private boolean statusFlag = false;

	public ContractImportHandler() {
		metadata = new MetadataInitializer();
		validator = new ImportValidator();
		matcher = new ContractMatcher();
	}

	@Override
	protected ContractRecord processRow(ImportConfiguration importConfig, Map<String, String> row) 
	{  
		ContractRecord contract = null;
		if (importConfig.isReimportMetadata()) {
			if (!statusFlag) {
				getAllMetadataLifecycleNotCompleted();
				statusFlag = true;
			}
			if (metadata.contractMetadataLifecycleNotcompletedMap.containsValue((row.get(ImportConstants.CONTRACT_NUMBER)))) {
				contract = processRowAfterCheck(importConfig, row);
			} 
		}
		else if (importConfig.isRetrigger()) {
			if (!statusFlag) {
				getAllStatusesNotCompleted();
				statusFlag = true;
			}
			if (metadata.contractAllStatusNotcompletedMap.containsValue((row.get(ImportConstants.CONTRACT_NUMBER)))) {
				contract = processRowAfterCheck(importConfig, row);
			} 
		}
		else 
		{
			contract = processRowAfterCheck(importConfig, row);
		}
		return contract;
	}

	protected ContractRecord processRowAfterCheck(ImportConfiguration importConfig, Map<String, String> row) 
	{
		ContractRecord contract = null;
		ReportItem reportItem = validator.validate(row, metadata,importConfig.getJobId(), super.properties);
		if (reportItem.getErrors().isEmpty()) 
		{
			boolean processed = false;
			if(importConfig.isMatchContracts() && !ImportConstants.Y_FORCE_CREATION.equalsIgnoreCase(row.get(ImportConstants.FORCE_CREATION))) 
			{//Contracts needs to be matched
				String itemId = null;
				try 
				{
					itemId = matcher.matchContract(row, 
							importConfig.getContractLines() != null ? importConfig.getContractLines().get(row.get(ImportConstants.CONTRACT_NUMBER)) : null, 
									importConfig.getBillerPeriods() != null ? importConfig.getBillerPeriods().get(row.get(ImportConstants.CONTRACT_NUMBER)): null);
					if(!ImportUtils.isEmpty(itemId))
					{
						row.put(ImportConstants.CONTRACT_ITEM_ID, itemId);
						contract = new ContractRecord(metadata, reportItem, validator);
						contract.doWork(new ImportEvent(contract,row), importConfig.isMatchContracts(), true);
						processed = true;
						importConfig.getContractActions().put(row.get(ImportConstants.CONTRACT_NUMBER), ImportConstants.ACTION_UPDATE);
						updateImportLogs(row.get(ImportConstants.CONTRACT_NUMBER),importConfig.getJobId(), ImportConstants.CONTRACT_UPDATE_MESSAGE+itemId);
					}
				}
				catch (ContractMatchException e) 
				{
					updateImportLogs(row.get(ImportConstants.CONTRACT_NUMBER),importConfig.getJobId(), e.getMessage());
					processed=true;
				}
			}
			if(!processed){
				contract = new ContractRecord(metadata, reportItem,validator);
				contract.doWork(new ImportEvent(contract,row), importConfig.isMatchContracts(), false);
				importConfig.getContractActions().put(row.get(ImportConstants.CONTRACT_NUMBER), ImportConstants.ACTION_CREATE);
			}
		}
		else 
		{
			updateImportLogs(row.get(ImportConstants.CONTRACT_NUMBER),importConfig.getJobId(), reportItem.getErrors().toString());
		}
		violationsReport.add(reportItem);
		return contract;
	}

	private void getAllMetadataLifecycleNotCompleted() {
		int response = 0, index = 0, jobIDNode = 0 ;
		int nodes[] = null;
		try
		{
			SOAPRequestObject getStatusesRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetAllMetadataLifecycleNotCompleted", null, null);
			jobIDNode = NomUtil.parseXML("<JobId>"+super.importConfig.getJobId()+"</JobId>");
			getStatusesRequest.addParameterAsXml(jobIDNode);
			response = getStatusesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//ContractImportStatus", response);
			for (int i : nodes) {
				String contractNumber=Node.getDataWithDefault(NomUtil.getNode(".//ContractNumber", i),null);
				String contractLifecycleStatus = Node.getDataWithDefault(NomUtil.getNode(".//ContractLifecycleStatus", i),null);
				String contractMetadataStatus = Node.getDataWithDefault(NomUtil.getNode(".//ContractMetadataStatus", i),null);
				if(null != contractNumber){
					if(!contractMetadataStatus.equalsIgnoreCase("Completed") || !contractLifecycleStatus.equalsIgnoreCase("Completed"))
					{
						++index;
						metadata.contractMetadataLifecycleNotcompletedMap.put(index,contractNumber);
					}
				}
			}
		}
		catch (Exception e) {
			logger._log("com.opentext.apps.cc.importhandler.contract.ContractImportHandler", Severity.ERROR, e,"Error while trigerring GetAllMetadataLifecycleNotCompleted Web service");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_TRIGGERING_WEBSERVICE,"GetAllMetadataLifecycleNotCompleted");
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}		

	}

	private void getAllStatusesNotCompleted() {
		int response = 0, index = 0, jobIDNode = 0 ;
		int nodes[] = null;
		try
		{
			SOAPRequestObject getStatusesRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetAllStatusesNotCompleted", null, null);
			jobIDNode = NomUtil.parseXML("<JobId>"+super.importConfig.getJobId()+"</JobId>");
			getStatusesRequest.addParameterAsXml(jobIDNode);
			response = getStatusesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//ContractImportStatus", response);
			for (int i : nodes) {
				String contractNumber=Node.getDataWithDefault(NomUtil.getNode(".//ContractNumber", i),null);
				String contractLifecycleStatus = Node.getDataWithDefault(NomUtil.getNode(".//ContractLifecycleStatus", i),null);
				String contractMetadataStatus = Node.getDataWithDefault(NomUtil.getNode(".//ContractMetadataStatus", i),null);
				String contractDocumentStatus = Node.getDataWithDefault(NomUtil.getNode(".//ContractDocumentStatus", i),null);
				if(null != contractNumber){
					if(!contractMetadataStatus.equalsIgnoreCase("Completed") || !contractLifecycleStatus.equalsIgnoreCase("Completed") || !contractDocumentStatus.equalsIgnoreCase("Completed"))
					{
						++index;
						metadata.contractAllStatusNotcompletedMap.put(index,contractNumber);
					}
				}
			}
		}
		catch (Exception e) {
			logger._log("com.opentext.apps.cc.importhandler.contract.ContractImportHandler", Severity.ERROR, e,"Error while trigerring GetAllStatusesNotCompleted Web service");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_TRIGGERING_WEBSERVICE,"GetAllStatusesNotCompleted");
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}		

	}

	private void updateImportLogs(String contractNumber,String jobID, String errors) {
		int contractNumberNode=0,statusItemIdResponse=0,updateImportStatusNode=0,updateDataStatusNode=0,updateContractImportStatusResponse=0,jobIDNode=0;
		try
		{
			if(!Utilities.isStringEmpty(contractNumber))
			{
				SOAPRequestObject statusItemIdRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ContractImportStatus/operations", "GetContractImportStatus", null, null);
				contractNumberNode = NomUtil.parseXML("<ContractNumber>"+contractNumber+"</ContractNumber>");
				jobIDNode = NomUtil.parseXML("<JobID>"+jobID+"</JobID>");
				statusItemIdRequest.addParameterAsXml(contractNumberNode);
				statusItemIdRequest.addParameterAsXml(jobIDNode);
				statusItemIdResponse = statusItemIdRequest.sendAndWait();
				String itemId=Node.getDataWithDefault(NomUtil.getNode(".//ContractImportStatus-id/ItemId", statusItemIdResponse),null);

				SOAPRequestObject updateImportJobRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ContractImportStatus/operations", "UpdateContractImportStatus", null, null);
				updateImportStatusNode = NomUtil.parseXML("<ContractImportStatus-id></ContractImportStatus-id>");
				Node.setDataElement(updateImportStatusNode, "ItemId", itemId);
				updateDataStatusNode = NomUtil.parseXML("<ContractImportStatus-update></ContractImportStatus-update>");
				Node.setDataElement(updateDataStatusNode, "LogDetails", errors);
				Node.setDataElement(updateDataStatusNode, "ContractMetadataStatus", "InvalidData");
				updateImportJobRequest.addParameterAsXml(updateDataStatusNode);
				updateImportJobRequest.addParameterAsXml(updateImportStatusNode);
				updateContractImportStatusResponse = updateImportJobRequest.sendAndWait();
			}
		}
		finally
		{
			Utilities.cleanAll(contractNumberNode,updateImportStatusNode,updateDataStatusNode,statusItemIdResponse,updateContractImportStatusResponse,jobIDNode);
		}
	}

	@Override
	protected String getSheetName() {
		return ImportConstants.IMPORT_CONTRACTS_SHEET_NAME;
	}

	@Override
	protected ReportListener createReportListener() {
		return new CSVReportListenerForContracts();
	}

	@SuppressWarnings("deprecation")
	private void invokeCaseTriggeringBPM(Collection<ImportListener> records) {
		logger.log(Severity.ERROR, "Start time of batch : "+System.currentTimeMillis());
		int contracts = 0, jobIdNode = 0, contractDataImportResponse = 0, isRetriggerNode = 0,isMetadataReimport = 0;
		contracts = NomUtil.parseXML("<Contracts></Contracts>");
		SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "ContractDataImport", null, null);
		for(ImportListener record:records){
			Node.appendToChildren(record.getnode(), contracts);
		}
		jobIdNode = NomUtil.parseXML("<JobId>"+super.importConfig.getJobId()+"</JobId>");
		isRetriggerNode = NomUtil.parseXML("<isRetrigger>"+super.importConfig.isRetrigger()+"</isRetrigger>");
		isMetadataReimport = NomUtil.parseXML("<isMetadataReimport>"+super.importConfig.isReimportMetadata()+"</isMetadataReimport>");
		createRequest.addParameterAsXml(jobIdNode);
		createRequest.addParameterAsXml(isRetriggerNode);
		createRequest.addParameterAsXml(isMetadataReimport);
		createRequest.addParameterAsXml(contracts);
		try {
			contractDataImportResponse = createRequest.execute();
		}catch(Exception e){
			logger._log("com.opentext.apps.cc.importhandler.contract.ContractImportHandler", Severity.ERROR, e,"Error while trigerring ContractDataImport BPM");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_TRIGGERING_BPM,"ContractDataImport");
		}
		finally{
			Utilities.cleanAll(contracts,jobIdNode,contractDataImportResponse,isRetriggerNode,isMetadataReimport);
		}		
	}

	@Override
	protected void commit(Collection<ImportListener> records) {
		if(records.size()>0){
			invokeCaseTriggeringBPM(records);
		}
	}

	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData,String jobId) {
		boolean status = true; 
		String jobStatus = "Reading ZIPs completed";
		int count=0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);//entity web service to read job details and update count value
		int contractNumberNode=0,jobIDNode=0,dataImportIdNode=0,createContractImportStatusResponse=0;
		try
		{
			for(;count<sheetData.size();count++) {
				row=sheetData.get(count);
				if(row.get(getKey(ImportConstants.CONTRACT_NUMBER)) != null) {
					try {
						SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ContractImportStatus/operations", "CreateContractImportStatus", null, null);
						contractNumberNode = NomUtil.parseXML("<ContractImportStatus-create></ContractImportStatus-create>");
						Node.setDataElement(contractNumberNode, "ContractLifecycleStatus", "NotStarted");
						Node.setDataElement(contractNumberNode, "ContractMetadataStatus", "NotStarted");
						Node.setDataElement(contractNumberNode, "ContractNumber", row.get(getKey(ImportConstants.CONTRACT_NUMBER)));
						Node.setDataElement(contractNumberNode, "ContractDocumentStatus ", "NotStarted");
						jobIDNode = NomUtil.parseXML("<JobID></JobID>");
						dataImportIdNode = NomUtil.parseXML("<DataImport-id></DataImport-id>");
						Node.setDataElement(dataImportIdNode, "Id", jobId);
						Node.appendToChildren(dataImportIdNode, jobIDNode);
						Node.appendToChildren(jobIDNode, contractNumberNode);
						createRequest.addParameterAsXml(contractNumberNode);
						createContractImportStatusResponse = createRequest.sendAndWait();
					} finally {
						Utilities.cleanAll(createContractImportStatusResponse);
					}
				}
			}    
		}catch (Exception e) {
			status = false;
			jobStatus = "Reading ZIPs failed";
			updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump failed"
			logger._log("com.opentext.apps.cc.importhandler.contract.ContractImportHandler", Severity.ERROR, e,"Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		}finally {
			Utilities.cleanAll(contractNumberNode,jobIDNode,dataImportIdNode);
		}
		updateImportJob(jobId,jobStatus,count);
		return status;
	}	

	@SuppressWarnings("deprecation")
	@Override
	protected void generateErrorReport(String jodId, Path path,ImportConfiguration importparm) {
		int response = 0, nodes[] = null;

		try {
			Map<String, Map<String, String>> rowData = super.getSheetData(path, this.getSheetName(), ImportConstants.CONTRACT_NUMBER);
			if(null == rowData || rowData.size() == 0) return;
			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "getFailedContractsByJobId", null, null);
			String jobId = "<jobId>"+jodId+"</jobId>";
			importRequest.addParameterAsXml(NomUtil.parseXML(jobId));
			response = importRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//ContractImportStatus", response);
			List<Map<String, String>> errorRecords = new LinkedList<Map<String, String>>();
			for (int i : nodes) {
				String contractNumber=null, contractMetadataStatus = null, contractLifecycleStatus = null, contractDocumentStatus = null, logDetails=null;
				contractNumber = Node.getDataWithDefault(NomUtil.getNode(".//ContractNumber", i),null);
				contractMetadataStatus = Node.getDataWithDefault(NomUtil.getNode(".//ContractMetadataStatus", i),null);
				contractLifecycleStatus = Node.getDataWithDefault(NomUtil.getNode(".//ContractLifecycleStatus", i),null);
				contractDocumentStatus = Node.getDataWithDefault(NomUtil.getNode(".//ContractDocumentStatus", i),null);
				logDetails =Node.getDataWithDefault(NomUtil.getNode(".//LogDetails", i),null);
				if(rowData.get(contractNumber) != null) {
					Map<String, String> row = rowData.get(contractNumber);
					row.put("ContractMetadataStatus", contractMetadataStatus);
					row.put("ContractLifecycleStatus", contractLifecycleStatus);
					row.put("ContractDocumentStatus", contractDocumentStatus);
					row.put("Error", logDetails);
					errorRecords.add(row);
				}
			}
			super.createErrorFile(errorRecords,FileUtil.getDownloadReadPath()+jodId+File.separatorChar+ImportHandler.Contract.name()+"_Error_Report.xlsx", this.getSheetName(),importparm);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ContentManagementRuntimeException(e);
		} finally { 
			if(nodes != null) Utilities.cleanAll(nodes);
			Utilities.cleanAll(response);
		}
	}

	
}
