package com.opentext.apps.cc.importhandler.revenueschedule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

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
import com.opentext.apps.cc.importcontent.ImportProperties;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.ReportListener;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;

public class RevenueImportHandler extends AbstractImportHandler {
	private static final CordysLogger logger = CordysLogger.getCordysLogger(RevenueImportHandler.class);

	private final ImportValidator validator;
	public final MetadataInitializer metadata;
	ImportProperties importconfigproperties;
	private ReportItem reportItem; 

	public RevenueImportHandler(){
		reportItem = new ReportItem();
		validator=new ImportValidator();
		metadata = new MetadataInitializer();
		this.importconfigproperties = new ImportProperties();
	}

	@Override
	protected String getSheetName() {
		return ImportConstants.SHEET_NAME;
	}

	@Override
	protected ImportListener processRow(ImportConfiguration configuration, Map<String, String> row) {
		RevenueSheduleRecord revenueSchedule = null;
		reportItem = validator.validate(row,metadata);
		if (reportItem.getErrors().isEmpty()) { // No violations proceed to persist
			if(configuration.isMatchContracts()) {
				//Contract is matched and got updated, no need to create these for update
				if(com.opentext.apps.cc.importhandler.contract.ImportConstants.ACTION_CREATE.equals(configuration.getContractActions().get(row.get(ImportConstants.ID)))) {
					revenueSchedule = new RevenueSheduleRecord(metadata, reportItem);
					revenueSchedule.doWork(new ImportEvent(revenueSchedule, row));
				}
			}
			else {
				revenueSchedule = new RevenueSheduleRecord(metadata, reportItem);
				revenueSchedule.doWork(new ImportEvent(revenueSchedule, row));
			}
		}else {
			updateImportStatus(row.get(ImportConstants.ID1),super.importConfig.getJobId(),reportItem.getErrors().toString());//Update RevenueImportStatus Entity
		}
		violationsReport.add(reportItem);
		return revenueSchedule;
	}

	private void updateImportStatus(String revenueId,String jobID, String errors) {
		int revenueNode=0,statusItemIdResponse=0,updateImportStatusNode=0,updateDataStatusNode=0,updateContractImportStatusResponse=0,jobIDNode=0;
		try
		{
			if(!Utilities.isStringEmpty(revenueId))
			{
				SOAPRequestObject statusItemIdRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "RevenueImportStatusIdFilter", null, null);
				revenueNode = NomUtil.parseXML("<revenueId>"+revenueId+"</revenueId>");
				jobIDNode = NomUtil.parseXML("<jobId>"+jobID+"</jobId>");
				statusItemIdRequest.addParameterAsXml(revenueNode);
				statusItemIdRequest.addParameterAsXml(jobIDNode);
				statusItemIdResponse = statusItemIdRequest.sendAndWait();//ItemId for the RevenueId
				String itemId=Node.getDataWithDefault(NomUtil.getNode(".//RevenueScheduleImportStatus-id/ItemId", statusItemIdResponse),null);

				SOAPRequestObject updateImportJobRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/RevenueScheduleImportStatus/operations", "UpdateRevenueScheduleImportStatus", null, null);
				updateImportStatusNode = NomUtil.parseXML("<RevenueScheduleImportStatus-id></RevenueScheduleImportStatus-id>");
				Node.setDataElement(updateImportStatusNode, "ItemId", itemId);
				updateDataStatusNode = NomUtil.parseXML("<RevenueScheduleImportStatus-update></RevenueScheduleImportStatus-update>");
				Node.setDataElement(updateDataStatusNode, "LogDetails", errors);
				Node.setDataElement(updateDataStatusNode, "ImportStatus", "InvalidData");
				updateImportJobRequest.addParameterAsXml(updateDataStatusNode);
				updateImportJobRequest.addParameterAsXml(updateImportStatusNode);
				updateContractImportStatusResponse = updateImportJobRequest.sendAndWait();	
			}
		}
		finally{
			Utilities.cleanAll(revenueNode,statusItemIdResponse,updateImportStatusNode,updateDataStatusNode,updateContractImportStatusResponse,jobIDNode);
		}
	}
	@Override
	protected ReportListener createReportListener() {
		return new CSVReportListenerForRevenueSchedule();
	}

	@Override
	protected void commit(Collection<ImportListener> records) {
		if(records.size()>0){
			int revenueSchedulesResponse=0,revenueSchedules=0,jobIdnode=0;
			jobIdnode = NomUtil.parseXML("<JobID>"+super.importConfig.getJobId()+"</JobID>");
			revenueSchedules = NomUtil.parseXML("<RevenueSchedules></RevenueSchedules>");
			SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "RevenueSchedulesImport", null, null);
			for(ImportListener record:records){
				Node.appendToChildren(record.getnode(), revenueSchedules);
			}
			createRequest.addParameterAsXml(revenueSchedules);
			createRequest.addParameterAsXml(jobIdnode);
			try {
				revenueSchedulesResponse=createRequest.execute();
			}catch (Exception e) {
				throw new RuntimeErrorException(null);
			}
			finally {
				Utilities.cleanAll(revenueSchedules,revenueSchedulesResponse);
			}
		}

	}

	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData,String jobId) {

		boolean status = true; 
		String jobStatus = "Reading ZIPs completed";
		int count=0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);//entity web service to read job details and update count value
		int revenueIdNode=0,importNode=0,child=0,createNode=0,importStatusNode=0,createRevenueScheduleImportStatusResponse=0;
		try
		{
			for(;count<sheetData.size();count++)
			{
				row=sheetData.get(count);
				if(row.get(ImportConstants.ID1) != null){
					SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/RevenueScheduleImportStatus/operations", "CreateRevenueScheduleImportStatus", null, null);
					createNode = NomUtil.parseXML("<RevenueScheduleImportStatus-create xmlns=\"http://schemas/OpenTextContractCenterImport/RevenueScheduleImportStatus\"></RevenueScheduleImportStatus-create>");
					importNode = NomUtil.parseXML("<DataImportJob></DataImportJob>");
					child = Node.createElementNS("DataImport-id","","", "http://schemas/OpenTextContractCenterImport/DataImport", importNode);
					Node.appendToChildren(NomUtil.parseXML("<Id>"+jobId+"</Id>"),child);
					Node.appendToChildren(importNode,createNode);
					revenueIdNode = NomUtil.parseXML("<RevenueId>"+row.get(ImportConstants.ID1)+"</RevenueId>");
					importStatusNode = NomUtil.parseXML("<ImportStatus>NotStarted</ImportStatus>");
					Node.appendToChildren(revenueIdNode,createNode);
					Node.appendToChildren(importStatusNode,createNode);
					createRequest.addParameterAsXml(createNode);
					createRevenueScheduleImportStatusResponse=createRequest.sendAndWait();
				}
			}
		}
		catch (Exception e) 
		{
			status = false;
			jobStatus = "Reading ZIPs failed";
			updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump failed"
			logger._log("com.opentext.apps.cc.importhandler.revenueschedule.RevenueImportHandler", Severity.ERROR, e, "Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		}
		finally 
		{
			Utilities.cleanAll(revenueIdNode,importNode,child,createNode,importStatusNode,createRevenueScheduleImportStatusResponse);
		}
		updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump success"
		return status;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void generateErrorReport(String jodId, Path path,ImportConfiguration importparm) {
		int response = 0, nodes[] = null;
		try {
			Map<String, Map<String, String>> rowData = super.getSheetData(path, this.getSheetName(), ImportConstants.ID1);
			if(null == rowData || rowData.size() == 0) return;
			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "getFailedRevenueimportItemsByJobId", null, null);
			String jobId = "<jobId>"+jodId+"</jobId>";
			importRequest.addParameterAsXml(NomUtil.parseXML(jobId));
			response = importRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RevenueScheduleImportStatus", response);
			List<Map<String, String>> errorRecords = new LinkedList<Map<String, String>>();
			for (int i : nodes) {
				String revenueId=null, importStatus = null, logDetails=null;
				revenueId = Node.getDataWithDefault(NomUtil.getNode(".//RevenueId", i),null);
				importStatus = Node.getDataWithDefault(NomUtil.getNode(".//ImportStatus", i),null);
				logDetails =Node.getDataWithDefault(NomUtil.getNode(".//LogDetails", i),null);
				if(rowData.get(revenueId) != null) {
					Map<String, String> row = rowData.get(revenueId);
					row.put("ImportStatus", importStatus);
					row.put("Error", logDetails);
					errorRecords.add(row);
				}
			}
			super.createErrorFile(errorRecords, FileUtil.getDownloadReadPath()+jodId+File.separatorChar+ImportHandler.RevenueSchedule.name()+"_Error_Report.xlsx", this.getSheetName(),importparm);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ContentManagementRuntimeException(e);
		} finally { 
			if(nodes != null) Utilities.cleanAll(nodes);
			Utilities.cleanAll(response);
		}
	}
}
