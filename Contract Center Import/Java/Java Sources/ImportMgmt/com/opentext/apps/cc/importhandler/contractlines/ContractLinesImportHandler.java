package com.opentext.apps.cc.importhandler.contractlines;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.cordys.cpc.bsf.busobject.BSF;
import com.cordys.cpc.bsf.busobject.BusObjectConfig;
import com.cordys.cpc.bsf.busobject.BusObjectManager;
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

public class ContractLinesImportHandler extends AbstractImportHandler {

	private static final CordysLogger logger = CordysLogger.getCordysLogger(ContractLinesImportHandler.class);

	private BusObjectManager busObjectManager;
	private final ImportValidator validator;
	public final MetadataInitializer metadata;
	private ReportItem reportItem; 

	public ContractLinesImportHandler(){
		reportItem = new ReportItem();
		validator=new ImportValidator();
		metadata = new MetadataInitializer();
		this.busObjectManager = new BusObjectManager(BSF.getMyContext().getObjectManager().getConfiguration(),BSF.getXMLDocument());
		new BusObjectConfig(busObjectManager, 0, com.cordys.cpc.bsf.busobject.BusObjectConfig.NEW_OBJECT);

	}
	@Override
	protected String getSheetName() {
		return ImportConstants.SHEET_NAME;
	}

	@Override
	protected ImportListener processRow(ImportConfiguration configuration, Map<String, String> row) {
		ContractLineRecord contractLine = null;
		reportItem = validator.validate(row,metadata);
		if (reportItem.getErrors().isEmpty()) {// No violations proceed to persist
			if(configuration.isMatchContracts()) {
				//Contract is matched and got updated, no need to create these for update
				if(com.opentext.apps.cc.importhandler.contract.ImportConstants.ACTION_CREATE.equals(configuration.getContractActions().get(row.get(ImportConstants.CONTRACT_NUMBER)))) {
					contractLine = new ContractLineRecord(metadata, reportItem);
					contractLine.doWork(new ImportEvent(contractLine, row));
				}
			} else {
				contractLine = new ContractLineRecord(metadata, reportItem);
				contractLine.doWork(new ImportEvent(contractLine, row));
			}
		}
		else
		{
			updateImportLogs(row.get(ImportConstants.LINE_NUMBER),super.importConfig.getJobId(), reportItem.getErrors().toString());
		}
		violationsReport.add(reportItem);
		return contractLine;
	}

	private void updateImportLogs(String lineNumber,String jobID, String errors) {
		int contractNumberNode=0,statusItemIdResponse=0,updateImportStatusNode=0,updateDataStatusNode=0,updateContractImportStatusResponse=0,jobIDNode=0;
		try
		{
			if(!Utilities.isStringEmpty(lineNumber))
			{
				SOAPRequestObject statusItemIdRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ContractLinesStatus/operations", "GetContractlineImportStatusLineNumberFilter", null, null);
				contractNumberNode = NomUtil.parseXML("<LineNumber>"+lineNumber+"</LineNumber>");
				jobIDNode = NomUtil.parseXML("<JobID>"+jobID+"</JobID>");
				statusItemIdRequest.addParameterAsXml(contractNumberNode);
				statusItemIdRequest.addParameterAsXml(jobIDNode);
				statusItemIdResponse = statusItemIdRequest.sendAndWait();
				String itemId=Node.getDataWithDefault(NomUtil.getNode(".//ContractLinesStatus-id/ItemId", statusItemIdResponse),null);

				SOAPRequestObject updateImportJobRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ContractLinesStatus/operations", "UpdateContractLinesStatus", null, null);
				updateImportStatusNode = NomUtil.parseXML("<ContractLinesStatus-id></ContractLinesStatus-id>");
				Node.setDataElement(updateImportStatusNode, "ItemId", itemId);
				updateDataStatusNode = NomUtil.parseXML("<ContractLinesStatus-update></ContractLinesStatus-update>");
				Node.setDataElement(updateDataStatusNode, "LogDetails", errors);
				Node.setDataElement(updateDataStatusNode, "ImportStatus", "InvalidData");
				updateImportJobRequest.addParameterAsXml(updateDataStatusNode);
				updateImportJobRequest.addParameterAsXml(updateImportStatusNode);
				updateContractImportStatusResponse = updateImportJobRequest.sendAndWait();
			}
		}
		finally{
			Utilities.cleanAll(contractNumberNode,updateImportStatusNode,updateDataStatusNode,statusItemIdResponse,updateContractImportStatusResponse,jobIDNode);
		}
	}	

	@Override
	protected ReportListener createReportListener() {
		return new CSVReportListenerForContractLines();
	}


	@Override
	protected void commit(Collection<ImportListener> records) {
		int contractLines=0,contractLinesImportResponse=0,jobIDNode;
		contractLines = NomUtil.parseXML("<ContractLines></ContractLines>");
		SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "ContractLinesImport", null, null);
		for(ImportListener record:records)
		{
			Node.appendToChildren(record.getnode(), contractLines);
		}
		createRequest.addParameterAsXml(contractLines);
		jobIDNode = NomUtil.parseXML("<JobID>"+ super.importConfig.getJobId() +"</JobID>");
		createRequest.addParameterAsXml(jobIDNode);
		try 
		{
			contractLinesImportResponse = createRequest.execute();
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
		}
		finally {
			Utilities.cleanAll(contractLinesImportResponse,contractLines);
		}
	}

	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData,String jobId) {
		boolean status = true; 
		String jobStatus = "Reading ZIPs completed";
		int count=0;
		int contractLineNumber=0,importNode=0,child=0,createNode=0,importStatusNode=0,createContractLinesStatusResponse=0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);//entity web service to read job details and update count value
		try
		{
			for(;count<sheetData.size();count++)//for(Map<String, String> row:sheetData)
			{
				row=sheetData.get(count);
				if(row.get(getKey(ImportConstants.LINE_NUMBER)) != null){
					SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ContractLinesStatus/operations", "CreateContractLinesStatus", null, null);
					createNode = NomUtil.parseXML("<ContractLinesStatus-create xmlns=\"http://schemas/OpenTextContractCenterImport/RevenueScheduleImportStatus\"></ContractLinesStatus-create>");
					importNode = NomUtil.parseXML("<ToJobId></ToJobId>");
					child = Node.createElementNS("DataImport-id","","", "http://schemas/OpenTextContractCenterImport/DataImport", importNode);
					Node.appendToChildren(NomUtil.parseXML("<Id>"+jobId+"</Id>"),child);
					Node.appendToChildren(importNode,createNode);
					contractLineNumber = NomUtil.parseXML("<LineNumber>"+row.get(getKey(ImportConstants.LINE_NUMBER))+"</LineNumber>");
					importStatusNode = NomUtil.parseXML("<ImportStatus>NotStarted</ImportStatus>");
					Node.appendToChildren(contractLineNumber,createNode);
					Node.appendToChildren(importStatusNode,createNode);
					createRequest.addParameterAsXml(createNode);
					createContractLinesStatusResponse = createRequest.sendAndWait();
				}
			}
		}catch (Exception e) {
			status = false;
			jobStatus = "Reading ZIPs failed";
			updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump failed"
			logger._log("com.opentext.apps.cc.importhandler.contractlines.ContractLinesImportHandler", Severity.ERROR, e, "Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		}finally {
			Utilities.cleanAll(contractLineNumber,importNode,child,createNode,importStatusNode,createContractLinesStatusResponse);
		}
		updateImportJob(jobId,jobStatus,count);
		return status;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void generateErrorReport(String jodId, Path path,ImportConfiguration importparm) {
		int response = 0, nodes[] = null;
		try {
			Map<String, Map<String, String>> rowData = super.getSheetData(path, this.getSheetName(), ImportConstants.LINE_NUMBER);
			if(null == rowData || rowData.size() == 0) return;
			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "getFailedContractLinesImportItemsByJonId", null, null);
			String jobId = "<jobId>"+jodId+"</jobId>";
			importRequest.addParameterAsXml(NomUtil.parseXML(jobId));
			response = importRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//ContractLinesStatus", response);
			List<Map<String, String>> errorRecords = new LinkedList<Map<String, String>>();
			for (int i : nodes) {
				String lineNumber=null, importStatus = null, logDetails=null;
				lineNumber = Node.getDataWithDefault(NomUtil.getNode(".//LineNumber", i),null);
				importStatus = Node.getDataWithDefault(NomUtil.getNode(".//ImportStatus", i),null);
				logDetails =Node.getDataWithDefault(NomUtil.getNode(".//LogDetails", i),null);
				if(rowData.get(lineNumber) != null) {
					Map<String, String> row = rowData.get(lineNumber);
					row.put("ImportStatus", importStatus);
					row.put("Error", logDetails);
					errorRecords.add(row);
				}
			}
			super.createErrorFile(errorRecords, FileUtil.getDownloadReadPath()+jodId+File.separatorChar+ImportHandler.ContractLine.name()+"_Error_Report.xlsx", this.getSheetName(),importparm);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ContentManagementRuntimeException(e);
		} finally { 
			if(nodes != null) Utilities.cleanAll(nodes);
			Utilities.cleanAll(response);
		}
	}

}
