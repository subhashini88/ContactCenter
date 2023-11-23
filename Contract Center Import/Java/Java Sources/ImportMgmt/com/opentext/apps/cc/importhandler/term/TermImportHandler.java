
package com.opentext.apps.cc.importhandler.term;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.AbstractImportHandler;
import com.opentext.apps.cc.importcontent.FileUtil;
import com.opentext.apps.cc.importcontent.ImportConfiguration;
import com.opentext.apps.cc.importcontent.ImportConstants.ImportHandler;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.ImportUtils;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.ReportListener;
import com.opentext.apps.cc.importcontent.WorkBookManager;
import com.opentext.apps.cc.importcontent.WorkBookManagerImpl;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;

public class TermImportHandler extends AbstractImportHandler {

	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(TermImportHandler.class); 
	private List<LinkedHashMap<String, String>> TermStatusRecords = new LinkedList<LinkedHashMap<String, String>>();
	public TermImportHandler() {
		metadata = new MetadataInitializer();
		validator = new ImportValidator();

	}

	@Override
	protected String getSheetName() {
		return ImportConstants.SHEET_NAME;
	}

	@Override
	protected ImportListener processRow(ImportConfiguration configuration, Map<String, String> row) 
	{
		TermRecord termRecord= null;
		if(row != null)
		{
			ReportItem reportItem = validator.validate(row, metadata,importConfig.getJobId());
			LinkedHashMap<String, String> rowData = new LinkedHashMap<String, String>();
			if(configuration.isRetrigger())
			{
				Path path=Paths.get(FileUtil.getDownloadReadPath()+super.importConfig.getJobId()+File.separatorChar+File.separatorChar+ImportHandler.Term.name()+"_Status_Report.xlsx");
				Map<String, Map<String, String>> rows = super.getSheetData(path, "Term",ImportConstants.LEGACY_ID );
				Map<String, String> retrigger_row = rows.get(row.get(ImportConstants.LEGACY_ID));
				if(retrigger_row.get("ImportStatus").equalsIgnoreCase("Invaliddata"))
				{
					if (reportItem.getErrors().isEmpty()) 
					{

						termRecord = new TermRecord(metadata,reportItem);
						termRecord.doWork(new ImportEvent(termRecord,row));
						rowData.put(ImportConstants.TERM_NAME,row.get(ImportConstants.TERM_NAME));
						rowData.put(ImportConstants.STATUS,row.get(ImportConstants.STATUS));
						rowData.put(ImportConstants.TERM_DESCRIPTION,row.get(ImportConstants.TERM_DESCRIPTION));
						rowData.put(ImportConstants.LEGACY_ID,row.get(ImportConstants.LEGACY_ID));
						rowData.put("ImportStatus", "Completed");
						rowData.put("Log","Created Successfully");
					}
					else
					{	
						rowData.put(ImportConstants.TERM_NAME,row.get(ImportConstants.TERM_NAME));
						rowData.put(ImportConstants.STATUS,row.get(ImportConstants.STATUS));
						rowData.put(ImportConstants.TERM_DESCRIPTION,row.get(ImportConstants.TERM_DESCRIPTION));
						rowData.put(ImportConstants.LEGACY_ID,row.get(ImportConstants.LEGACY_ID));
						rowData.put("ImportStatus", "Invaliddata");
						rowData.put("Log",reportItem.getErrors().toString());
					}
					TermStatusRecords.add(rowData);
				}
				else
				{
					LinkedHashMap<String,String> retriggered = new LinkedHashMap<String,String>(retrigger_row);
					TermStatusRecords.add(retriggered);
				}
			}
			else
			{
				if (reportItem.getErrors().isEmpty()) 
				{

					termRecord = new TermRecord(metadata,reportItem);
					termRecord.doWork(new ImportEvent(termRecord,row));
					rowData.put(ImportConstants.TERM_NAME,row.get(ImportConstants.TERM_NAME));
					rowData.put(ImportConstants.STATUS,row.get(ImportConstants.STATUS));
					rowData.put(ImportConstants.TERM_DESCRIPTION,row.get(ImportConstants.TERM_DESCRIPTION));
					rowData.put(ImportConstants.LEGACY_ID,row.get(ImportConstants.LEGACY_ID));
					rowData.put("ImportStatus", "Completed");
					rowData.put("Log","Created Successfully");
				}
				else
				{	
					rowData.put(ImportConstants.TERM_NAME,row.get(ImportConstants.TERM_NAME));
					rowData.put(ImportConstants.STATUS,row.get(ImportConstants.STATUS));
					rowData.put(ImportConstants.TERM_DESCRIPTION,row.get(ImportConstants.TERM_DESCRIPTION));
					rowData.put(ImportConstants.LEGACY_ID,row.get(ImportConstants.LEGACY_ID));
					rowData.put("ImportStatus", "Invaliddata");
					rowData.put("Log",reportItem.getErrors().toString());
				}
				TermStatusRecords.add(rowData);
			}
		}
		return termRecord;
	}

	@Override
	protected ReportListener createReportListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void commit(Collection<ImportListener> records) {
		int termRecordNode=0,jobIdNode=0,termImportResponse=0;

		if(records.size()>0){
			termRecordNode = NomUtil.parseXML("<Terms></Terms>");

			for(ImportListener record:records){
				Node.appendToChildren(record.getnode(), termRecordNode);
				jobIdNode = NomUtil.parseXML("<JobID>"+super.importConfig.getJobId()+"</JobID>");
				Node.append(jobIdNode, record.getnode());
			}

			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3","TermImport", null, null);
			importRequest.addParameterAsXml(termRecordNode);
			try {
				termImportResponse=importRequest.execute();
			}catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.party.PartyImportHandler", Severity.ERROR, e, "Error while executing TermsImport");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,"TermsImport");
			}
			finally {
				Utilities.cleanAll(termRecordNode,termImportResponse,jobIdNode);
			}
		}
		if(batchuploadstatus!=null)
		{
			if(batchuploadstatus.equals("Completed"))
			{
				createStatusFile(TermStatusRecords,FileUtil.getDownloadReadPath()+super.importConfig.getJobId()+File.separatorChar+ImportHandler.Term.name()+"_Status_Report.xlsx",super.importConfig.getJobId());
			}
		}

	}

	protected void createStatusFile(List<LinkedHashMap<String, String>> records,String filePath,String jobid)
	{
		int count=0;
		count = getStatusDumpCount(jobid);
		String jobStatus = "Reading ZIPs completed";
		int requestNode=0,response=0,createNode=0,itemIdNode=0,documentNode=0,docURLNode=0,folderNode=0,OverwriteNode=0;
		if (!ImportUtils.isEmpty(filePath) && !ImportUtils.isEmpty(records)) {
			WorkBookManager wbManager = new WorkBookManagerImpl(Paths.get(filePath));
			final Workbook wb = wbManager.getWorkBook();
			Collection<String> columns = records.iterator().next().keySet();
			Sheet sheet = wbManager.createSheet(this.getSheetName());
			wbManager.createRow(sheet, columns, wbManager.createHeaderStyle(wb), 0);
			int rowIndex = 1;
			Iterator<LinkedHashMap<String, String>> rows = records.iterator();
			while (rows.hasNext()) {
				Collection<String> rowData = rows.next().values();
				wbManager.createRow(sheet, rowData, null, rowIndex++);
			}
			try
			{
				wbManager.createFile(wb, filePath);
				String documentName=filePath.substring(filePath.lastIndexOf('\\')+1);

				//Process Platform only able to upload documents in webroot
				//copying file to the webroot
				//Files.copy(Paths.get(filePath), Paths.get(basePath+"/"+documentName),StandardCopyOption.REPLACE_EXISTING);

				//ItemId Required for uploading document to the entity
				SOAPRequestObject readRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/DataImport/operations", "ReadDataImport", null, null); 
				requestNode = NomUtil.parseXML("<DataImport-id><Id>"+jobid+"</Id></DataImport-id>");
				readRequest.addParameterAsXml(requestNode);
				response = readRequest.sendAndWait();
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//DataImport-id/ItemId", response),null);

				SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas.opentext.com/bps/entity/buildingblock/file", "UploadDocument", null, null);
				createNode = NomUtil.parseXML("<Create></Create>");
				itemIdNode = Node.createElementNS("ItemId",itemId,"","http://schemas.opentext.com/bps/entity/core",createNode); // NomUtil.parseXML("<ItemId>"+itemId+"</ItemId>");
				Node.appendToChildren(itemIdNode, createNode);
				documentNode = NomUtil.parseXML("<FileName>"+documentName+"</FileName>");
				Node.appendToChildren(documentNode, createNode);
				//int docURLNode = NomUtil.parseXML("<FileURL>"+Utilities.getDocumentURL(importparm.getOrgName(), "ccimportreports/"+documentName)+"</FileURL>");
				docURLNode = NomUtil.parseXML("<FileURL>"+new File(filePath).toURI()+"</FileURL>");
				Node.appendToChildren(docURLNode, createNode);
				folderNode = NomUtil.parseXML("<FolderPath>Reports</FolderPath>");
				Node.appendToChildren(folderNode, createNode);
				OverwriteNode = NomUtil.parseXML("<Overwrite>true</Overwrite>");
				Node.appendToChildren(OverwriteNode, createNode);

				createRequest.addParameterAsXml(createNode);

				try {
					createRequest.execute();
				}catch(Exception e){
					logger._log("com.opentext.apps.cc.importhandler.party.PartyImportHandler", Severity.ERROR, e, "Error while uploading file to the content building block");
					throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_UPLOADING_FILE);
				}
			}
			catch(Exception e)
			{
				jobStatus = "Reading ZIPs failed";
				updateImportJob(jobid,jobStatus,count);//update job with count and status "Dump failed"
				logger._log("com.opentext.apps.cc.importhandler.party.PartyImportHandler", Severity.ERROR, e, "Error while creating Excel file.");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_EXCEL_FILE);
			}
			finally
			{
				Utilities.cleanAll(requestNode,createNode,docURLNode,createNode,documentNode,folderNode,OverwriteNode,response);
			}
		}
		updateImportJob(jobid,jobStatus,count);
	}

	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData, String jobId) {

		return true;
	}
	protected void generateErrorReport(String jobId, Path path,ImportConfiguration importparm) {


	}
}

