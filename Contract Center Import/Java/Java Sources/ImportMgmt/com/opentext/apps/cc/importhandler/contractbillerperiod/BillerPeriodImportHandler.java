package com.opentext.apps.cc.importhandler.contractbillerperiod;

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
import com.opentext.apps.cc.importcontent.ImportUtils;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.ReportListener;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;

public class BillerPeriodImportHandler extends AbstractImportHandler {

	private static final CordysLogger logger = CordysLogger.getCordysLogger(BillerPeriodImportHandler.class);

	private final BusObjectManager busObjectManager;
	private final ImportValidator validator;
	public final MetadataInitializer metadata;
	private ReportItem reportItem; 

	public BillerPeriodImportHandler(){
		reportItem = new ReportItem();
		validator=new ImportValidator();
		metadata = new MetadataInitializer();
		this.busObjectManager  = new BusObjectManager(BSF.getMyContext().getObjectManager().getConfiguration(),BSF.getXMLDocument());
		new BusObjectConfig(busObjectManager, 0, com.cordys.cpc.bsf.busobject.BusObjectConfig.NEW_OBJECT);

	}
	@Override
	protected String getSheetName() {
		return ImportConstants.SHEET_NAME;
	}

	@Override
	protected ImportListener processRow(ImportConfiguration configuration, Map<String, String> row) {
		BillerPeriodRecord billerperiod = null;
		reportItem = validator.validate(row,metadata);
		if (reportItem.getErrors().isEmpty()) // No violations proceed to persist
		{
			if(configuration.isMatchContracts()) {
				//Contract biller periods need to be created only when contract is created
				if(!ImportUtils.isEmpty(configuration.getContractActions().get(row.get(ImportConstants.CONTRACT_NUMBER)))) {
					billerperiod = new BillerPeriodRecord(metadata, reportItem);
					billerperiod.doWork(new ImportEvent(billerperiod, row));
				}
			} else {
				billerperiod = new BillerPeriodRecord(metadata, reportItem);
				billerperiod.doWork(new ImportEvent(billerperiod, row));
			}
		}
		else {
			updateImportStatus(row.get(ImportConstants.BILLER_ID),super.importConfig.getJobId(), reportItem.getErrors().toString());
		}
		violationsReport.add(reportItem);
		return billerperiod;
	}

	@Override
	protected ReportListener createReportListener() {
		return new CSVReportListenerForBillerPeriods();
	}


	@Override
	protected void commit(Collection<ImportListener> records) {
		if(records.size()>0){
			int contractBillerPeriodsImportResponse=0,billerPeriods=0;
			billerPeriods = NomUtil.parseXML("<BillerPeriods></BillerPeriods>");
			SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "ContractBillerPeriodsImport", null, null);
			for(ImportListener record:records)
			{
				Node.appendToChildren(record.getnode(), billerPeriods);
			}
			Node.setDataElement(billerPeriods, "JobId", super.importConfig.getJobId());
			createRequest.addParameterAsXml(billerPeriods);
			try 
			{
				contractBillerPeriodsImportResponse = createRequest.execute();
			} 
			catch (Exception e)
			{
				// TODO Auto-generated catch block
			} 
			finally
			{
				Utilities.cleanAll(billerPeriods,contractBillerPeriodsImportResponse);
			}
		}
	}

	private void updateImportStatus(String billerId,String jobID, String errors) {
		int billerNode=0,statusItemIdResponse=0,statusNode=0,errorNode=0,billerIdNode=0,idNode=0,updatebillerPeriodImportStatusResponse=0,updateImportStatusNode=0,updateDataStatusNode=0,jobIDNode=0;
		try
		{
			if(!Utilities.isStringEmpty(billerId))
			{
				SOAPRequestObject statusItemIdRequest = new SOAPRequestObject(ImportConstants.CONTRACTS_IMPORT_NAMESPACE, "BillerPeriodsImportStatusIdFilter", null, null);
				billerNode = NomUtil.parseXML("<BillerPeriodId>"+billerId+"</BillerPeriodId>");
				jobIDNode = NomUtil.parseXML("<JobID>"+jobID+"</JobID>");
				statusItemIdRequest.addParameterAsXml(billerNode);
				statusItemIdRequest.addParameterAsXml(jobIDNode);			
				statusItemIdResponse = statusItemIdRequest.sendAndWait();//ItemId for the billerId
				String itemId=Node.getDataWithDefault(NomUtil.getNode(".//BillerPeriodsStatus-id/Id", statusItemIdResponse),null);		

				SOAPRequestObject updateImportJobRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/BillerPeriodsStatus/operations", "UpdateBillerPeriodsStatus", null, null);
				updateImportStatusNode = NomUtil.parseXML("<BillerPeriodsStatus-id></BillerPeriodsStatus-id>");
				Node.setDataElement(updateImportStatusNode, "Id", itemId);
				updateDataStatusNode = NomUtil.parseXML("<BillerPeriodsStatus-update></BillerPeriodsStatus-update>");
				Node.setDataElement(updateDataStatusNode, "LogDetails",errors);
				Node.setDataElement(updateDataStatusNode, "ImportStatus", "InvalidData");
				updateImportJobRequest.addParameterAsXml(updateDataStatusNode);
				updateImportJobRequest.addParameterAsXml(updateImportStatusNode);
				updatebillerPeriodImportStatusResponse = updateImportJobRequest.sendAndWait();			
			}
		}
		finally{
			Utilities.cleanAll(billerNode,billerIdNode,idNode,errorNode,statusNode,statusItemIdResponse,updatebillerPeriodImportStatusResponse);
		}
	}

	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData,String jobId) {
		boolean status = true; 
		String jobStatus = "Reading ZIPs completed";
		int count=0,createBillerPeriodsStatusResponse=0,billerPeriodNode=0,jobIDNode=0,dataImportIdNode=0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);//entity web service to read job details and update count value
		try
		{
			for(;count<sheetData.size();count++)//for(Map<String, String> row:sheetData)
			{
				row=sheetData.get(count);
				if(row.get(ImportConstants.BILLER_ID) != null) {
					SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/BillerPeriodsStatus/operations", "CreateBillerPeriodsStatus", null, null);
					billerPeriodNode = NomUtil.parseXML("<BillerPeriodsStatus-create></BillerPeriodsStatus-create>");
					Node.setDataElement(billerPeriodNode, "ImportStatus", "NotStarted");
					Node.setDataElement(billerPeriodNode, "JobId", jobId);
					Node.setDataElement(billerPeriodNode, "BillerPeriodId", row.get(ImportConstants.BILLER_ID));
					jobIDNode = NomUtil.parseXML("<RelatedJobID></RelatedJobID>");
					dataImportIdNode = NomUtil.parseXML("<DataImport-id></DataImport-id>");
					Node.setDataElement(dataImportIdNode, "Id", jobId);
					Node.appendToChildren(dataImportIdNode, jobIDNode);
					Node.appendToChildren(jobIDNode, billerPeriodNode);					
					createRequest.addParameterAsXml(billerPeriodNode);
					createBillerPeriodsStatusResponse = createRequest.sendAndWait();
				}

			}
		}catch (Exception e) {
			status = false;
			jobStatus = "Reading ZIPs failed";
			updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump failed"
			logger._log("com.opentext.apps.cc.importhandler.contractbillerperiod.BillerPeriodImportHandler", Severity.ERROR, e, "Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		}finally {
			Utilities.cleanAll(createBillerPeriodsStatusResponse,billerPeriodNode);
		}
		updateImportJob(jobId,jobStatus,count);
		return status;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void generateErrorReport(String jodId, Path path,ImportConfiguration importparm) {
		int response = 0, nodes[] = null;
		try {
			Map<String, Map<String, String>> rowData = super.getSheetData(path, this.getSheetName(), ImportConstants.BILLER_ID);
			if(null == rowData || rowData.size() == 0) return;
			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "getFailedBillerPeriodsImportItemsByJobId", null, null);
			String jobId = "<jobId>"+jodId+"</jobId>";
			importRequest.addParameterAsXml(NomUtil.parseXML(jobId));
			response = importRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//BillerPeriodsStatus", response);
			List<Map<String, String>> errorRecords = new LinkedList<Map<String, String>>();
			for (int i : nodes) {
				String billerPeriodId=null, importStatus = null, logDetails=null;
				billerPeriodId = Node.getDataWithDefault(NomUtil.getNode(".//BillerPeriodId", i),null);
				importStatus = Node.getDataWithDefault(NomUtil.getNode(".//ImportStatus", i),null);
				logDetails =Node.getDataWithDefault(NomUtil.getNode(".//LogDetails", i),null);
				if(rowData.get(billerPeriodId) != null) {
					Map<String, String> row = rowData.get(billerPeriodId);
					row.put("ImportStatus", importStatus);
					row.put("Error", logDetails);
					errorRecords.add(row);
				}
			}
			super.createErrorFile(errorRecords, FileUtil.getDownloadReadPath()+jodId+File.separatorChar+ImportHandler.BillerPeriod.name()+"_Error_Report.xlsx", this.getSheetName(),importparm);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ContentManagementRuntimeException(e);
		} finally { 
			if(nodes != null) Utilities.cleanAll(nodes);
			Utilities.cleanAll(response);
		}
	}
}
