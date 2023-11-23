package com.opentext.apps.cc.importhandler.manager;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.AbstractImportHandler;
import com.opentext.apps.cc.importcontent.ImportConfiguration;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.ImportProperties;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.ReportListener;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;



public class ManagerImportHandler extends AbstractImportHandler {

	ImportValidator validator;
	MetadataInitializer metadata;
	ImportProperties importconfigproperties;
	private ReportItem reportItem;
	
	private static final CordysLogger logger = CordysLogger.getCordysLogger(ManagerImportHandler.class);

	public ManagerImportHandler(){
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
		Manager manager = null;
		reportItem = validator.validate(row,metadata);
		if (reportItem.getErrors().isEmpty())// No violations proceed to persist
		{ 
			manager = new Manager(metadata, reportItem);
			manager.doWork(new ImportEvent(manager, row));
		}else 
		{
			updateImportStatus(row.get(ImportConstants.CM),row.get(ImportConstants.CM_REPLACEMENT),reportItem);//Update RevenueImportStatus Entity
		}
		violationsReport.add(reportItem);
		return manager;
	}

	private void updateImportStatus(String cmEmail, String replacementEmail,ReportItem report) {
		int coutomerManagerNode=0,replacementManagerNode=0,statusItemIdResponse=0,updateImportStatusNode=0,updateDataStatusNode=0,updateStatusResponse=0,statusNode=0,errorNode=0;
		try
		{
			if(!Utilities.isStringEmpty(cmEmail))
			{
				SOAPRequestObject statusItemIdRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ManagerImportStatus/operations", "findManagerImportStatusByEmailIds", null, null);
				coutomerManagerNode = NomUtil.parseXML("<managerEmail>"+cmEmail+"</managerEmail>");
				replacementManagerNode = NomUtil.parseXML("<replacementEmail>"+replacementEmail+"</replacementEmail>");
				statusItemIdRequest.addParameterAsXml(coutomerManagerNode);
				statusItemIdRequest.addParameterAsXml(replacementManagerNode);
				statusItemIdResponse = statusItemIdRequest.sendAndWait();//ItemId for the RevenueId
				String id=Node.getDataWithDefault(NomUtil.getNode(".//ManagerImportStatus-id/Id", statusItemIdResponse),null);

				//ManagerImportStatus-update
				SOAPRequestObject updateRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ManagerImportStatus/operations", "UpdateManagerImportStatus", null, null);
				updateImportStatusNode = NomUtil.parseXML("<ManagerImportStatus-id></ManagerImportStatus-id>");
				Node.setDataElement(updateImportStatusNode, "Id", id);
				updateDataStatusNode = NomUtil.parseXML("<ContractImportStatus-update></ContractImportStatus-update>");
				Node.setDataElement(updateDataStatusNode, "LogDetails", report.getErrors().toString());
				Node.setDataElement(updateDataStatusNode, "ImportStatus", "Failed");
				updateRequest.addParameterAsXml(updateImportStatusNode);
				updateRequest.addParameterAsXml(updateDataStatusNode);
				updateStatusResponse = updateRequest.sendAndWait();
			}
		}
		finally{
			Utilities.cleanAll(updateStatusResponse,coutomerManagerNode,errorNode,replacementManagerNode,statusNode,statusItemIdResponse,updateImportStatusNode,updateDataStatusNode);
		}
	}
	@Override
	protected ReportListener createReportListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void commit(Collection<ImportListener> records) {
		if(records.size()>0){
			int managerImportResponse=0,managers=0,jobIdNode=0;
			managers = NomUtil.parseXML("<managers></managers>");
			SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "ManagerImport", null, null);
			for(ImportListener record:records){
				Node.appendToChildren(record.getnode(), managers);
			}
			createRequest.addParameterAsXml(managers);
			jobIdNode = NomUtil.parseXML("<jobId>"+super.importConfig.getJobId()+"</jobId>");
			createRequest.addParameterAsXml(jobIdNode);
			try {
				managerImportResponse=createRequest.execute();
			}catch (Exception e) {
				throw new RuntimeErrorException(null);
			}
			finally {
				Utilities.cleanAll(managers,managerImportResponse);
			}
		}

	}

	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData, String jobId) {
		boolean status = true; 
		String jobStatus = "DumpSuccess";
		int count=0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);//entity web service to read job details and update count value
		int managerIdNode=0,importNode=0,child=0,createNode=0,importStatusNode=0,createManagerImportStatusResponse=0,replacementManagerNode=0;
		try
		{
			for(;count<sheetData.size();count++)
			{
				row=sheetData.get(count);
				if(row.get(ImportConstants.CM) != null){
					SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ManagerImportStatus/operations", "CreateManagerImportStatus", null, null);
					createNode = NomUtil.parseXML("<ManagerImportStatus-create xmlns=\"http://schemas/OpenTextContractCenterImport/ManagerImportStatus\"></ManagerImportStatus-create>");
					importNode = NomUtil.parseXML("<DataImportJob></DataImportJob>");
					child = Node.createElementNS("DataImport-id","","", "http://schemas/OpenTextContractCenterImport/DataImport", importNode);
					Node.appendToChildren(NomUtil.parseXML("<Id>"+jobId+"</Id>"),child);
					Node.appendToChildren(importNode,createNode);
					managerIdNode = NomUtil.parseXML("<CMEmail>"+row.get(ImportConstants.CM)+"</CMEmail>");
					replacementManagerNode = NomUtil.parseXML("<ReplacementCMEmail>"+row.get(ImportConstants.CM_REPLACEMENT)+"</ReplacementCMEmail>");
					importStatusNode = NomUtil.parseXML("<ImportStatus>NotStarted</ImportStatus>");
					Node.appendToChildren(managerIdNode,createNode);
					Node.appendToChildren(replacementManagerNode,createNode);
					Node.appendToChildren(importStatusNode,createNode);
					createRequest.addParameterAsXml(createNode);
					createManagerImportStatusResponse=createRequest.sendAndWait();
				}
			}
		}
		catch (Exception e) 
		{
			status = false;
			jobStatus = "DumpFailed";
			updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump failed"
			logger._log("com.opentext.apps.cc.importhandler.manager.ManagerImportHandler", Severity.ERROR, e, "Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		}
		finally 
		{
			Utilities.cleanAll(managerIdNode,importNode,child,createNode,importStatusNode,createManagerImportStatusResponse,replacementManagerNode);
		}
		updateImportJob(jobId,jobStatus,count);//update job with count and status "Dump success"
		return status;
	}
}
