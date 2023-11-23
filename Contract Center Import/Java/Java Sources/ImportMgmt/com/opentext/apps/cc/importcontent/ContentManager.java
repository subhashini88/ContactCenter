package com.opentext.apps.cc.importcontent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.util.system.EIBProperties;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.internal.NOMDocumentPool;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportConstants.ImportHandler;

public class ContentManager {
	public static Path INSTALLATION_ROOT = Paths.get(EIBProperties.getInstallDir());
	private Path IMPORT_CONTENT_EXTRACTED_FOLDER;
	private static String fileType = ".xls";
	private static String manifestFileName = "Manifest";
	public static Path ZIP_FILE_LOCATION = null;
	private ImportConfigurationImpl importParameter;
	private Map<Integer,Integer> attachments = new HashMap<>();
	private static final String manifestColumnName = "WORKBOOK_NAME";
	private List<String> manifest = new ArrayList<>();
	private static final CordysLogger logger = CordysLogger.getCordysLogger(ContentManager.class);
		
	public final void handleImport(ImportConfiguration importParameter) {
		boolean isCommitSuccess = false;
		String filePath = importParameter.getJobId() + File.separator + importParameter.getZipFileName();
		ZIP_FILE_LOCATION = Paths.get(FileUtil.getDownloadReadPath()+filePath);
		extractZip(importParameter.isRetrigger(),importParameter.getJobId());
		initilize(importParameter);
		try
		{
			invokeImportHandlers();
			isCommitSuccess = true;
		}
		catch(Exception e)
		{		
		    if(!isCommitSuccess)
		    {
	  			ContentManagementRuntimeException contentMgtException = new ContentManagementRuntimeException(e,Messages.EXCEPTION_PERSISTING_IMPORTED_CONTENT,importParameter.getZipFileName());
	  			throw contentMgtException;
			}
			throw new ContentManagementRuntimeException(e);	
		}		
	}	

	public void invokeImportHandlers() {
		List<File> files = new ArrayList<>();
		FileUtil.getFiles(IMPORT_CONTENT_EXTRACTED_FOLDER.toFile(), fileType, files);
		readImportManifest(files);
		LinkedHashMap<String, File> maifestFileMappings = arrangeFilesUsingManifest(files);
		if (importParameter.isMatchContracts()) {// We need contractLines and revenue schedule information for match process while contract import, hence we will get the required data and pass it on to ContractImport
			for (String manifestEntry : manifest) {
				File file = maifestFileMappings.get(manifestEntry);
				try (InputStream s = Files.newInputStream(file.toPath());) {
					WorkBookManagerImpl manager = new WorkBookManagerImpl(file.toPath(), s);
					if (ImportHandler.ContractLine.name().equalsIgnoreCase(manifestEntry)) {
						 Map<String, List<Map<String, String>>> contractLines = manager.readAsListMap(com.opentext.apps.cc.importhandler.contractlines.ImportConstants.SHEET_NAME,com.opentext.apps.cc.importhandler.contractlines.ImportConstants.CONTRACT_NUMBER);
						 if(contractLines!=null) importParameter.setContractLines(contractLines);
					} /*else if (ImportHandler.RevenueSchedule.name().equalsIgnoreCase(manifestEntry)) {
						Map<String, List<Map<String, String>>>  revenueSchedule = manager.readAsListMap(com.opentext.apps.cc.importhandler.revenueschedule.ImportConstants.SHEET_NAME,com.opentext.apps.cc.importhandler.revenueschedule.ImportConstants.ID);
						if(revenueSchedule!=null) importParameter.setRevenueSchedule(revenueSchedule);
					}*/ else if (ImportHandler.BillerPeriod.name().equalsIgnoreCase(manifestEntry)) {
						Map<String, List<Map<String, String>>>  billerPeriods = manager.readAsListMap(com.opentext.apps.cc.importhandler.contractbillerperiod.ImportConstants.SHEET_NAME,com.opentext.apps.cc.importhandler.contractbillerperiod.ImportConstants.BILLER_ID);
						if(billerPeriods!=null) importParameter.setBillerPeriods(billerPeriods);
					}
				} catch (IOException e) {
					throw new ContentManagementRuntimeException(e);
				}
			}
		}
		for (String manifestEntry : manifest) {// The entry set for maifestFileMappings is always the manifest
			try {
				File file = maifestFileMappings.get(manifestEntry);
				String importHandler = ImportHandler.lookup(file.getName().substring(0, file.getName().indexOf('.')));
				if (!Utilities.isStringEmpty(importHandler)) {
					Object object = initiateClass(importHandler);
					if (object instanceof ContentImporter) {
						ContentImporter contentImporter = (ContentImporter) object;
						importParameter.setArtifact(file.getName().substring(0, file.getName().lastIndexOf('.')));
						contentImporter.manageImport(importParameter, file.toPath(), attachments);
					}
				}
			} catch (RuntimeException e) {
				ContentManagementRuntimeException contentMgtException = new ContentManagementRuntimeException(e, Messages.ERROR_PROCESSING_ARTIFACT, importParameter.getZipFileName());
				throw contentMgtException;
			}
		}
	}

	public void generateErrorReports(ImportConfiguration importParameter) {
		String filePath = importParameter.getJobId() + File.separator + importParameter.getZipFileName();
		ZIP_FILE_LOCATION = Paths.get(FileUtil.getDownloadReadPath()+filePath);
		extractZip(importParameter.isRetrigger(),filePath);
		initilize(importParameter);
		List<File> files = new ArrayList<>();
		FileUtil.getFiles(IMPORT_CONTENT_EXTRACTED_FOLDER.toFile(), fileType, files);
		readImportManifest(files);
		LinkedHashMap<String, File> maifestFileMappings = arrangeFilesUsingManifest(files);
		for (String manifestEntry : manifest) {// The entry set for maifestFileMappings is always the manifest
			File file = maifestFileMappings.get(manifestEntry);
			String importHandler = ImportHandler.lookup(file.getName().substring(0, file.getName().indexOf('.')));
			if (!Utilities.isStringEmpty(importHandler)) {
				Object object = initiateClass(importHandler);
				if (object instanceof ContentImporter) {
					ContentImporter contentImporter = (ContentImporter) object;
					this.importParameter.setArtifact(file.getName().substring(0, file.getName().lastIndexOf('.')));
					contentImporter.generateErrorReport(importParameter.getJobId(), file.toPath(),importParameter);
				}
			}
		}
	}
	
	public void extractZip(Boolean isretrigger,String jobID)
	{
		if(isretrigger){
			int zipIndex = ZIP_FILE_LOCATION.toString().indexOf(".zip");
			IMPORT_CONTENT_EXTRACTED_FOLDER = Paths.get(ZIP_FILE_LOCATION.toString().substring(0, zipIndex));
		}else{
			IMPORT_CONTENT_EXTRACTED_FOLDER = ZipUtil.extractZipFile(ZIP_FILE_LOCATION,jobID);
		}
	}
	private void readImportManifest(List<File> files) 
	{
		boolean importFileRead = false;
		for(File file:files)
		{
			if(file.getName().indexOf(manifestFileName) >= 0)
  			{
				Object object = new ManifestImport();	
			    ContentImporter contentImporter = (ContentImporter)object;
				contentImporter.manageImport(importParameter,file.toPath(),attachments);
				importFileRead = true;
				files.remove(file);
				break;
  			}
		}
		if(!importFileRead)
		{
			Document document = NOMDocumentPool.getInstance().lendDocument();
			int idElement = 0,updateElement=0;
			try {
				idElement = document.createElement("DataImport-id");
				Node.createTextElement("Id", importParameter.getJobId(), idElement);
				updateElement = document.createElement("DataImport-update");
				Node.createTextElement("ImportStatus", "Manifest file is missing", updateElement);
				SOAPRequestObject updateDataImportRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/DataImport/operations","UpdateDataImport", null, null);
				updateDataImportRequest.addParameterAsXml(idElement);
				updateDataImportRequest.addParameterAsXml(updateElement);
				updateDataImportRequest.execute();
			} catch (Exception e) {
				logger._log("com.opentext.apps.cc.custom.ContentManager", Severity.ERROR, e, "Exception while updating the status import.");
			}finally {
				Utilities.cleanAll(idElement);
				Utilities.cleanAll(updateElement);
				NOMDocumentPool.getInstance().returnDocument(document);
			}
			ContentManagementRuntimeException exception = new ContentManagementRuntimeException(Messages.MANIFEST_DOESNOT_EXISTS);
	    	throw exception;
		}
	}
	private class ManifestImport  extends ContentImporter
	{
		private ImportConfiguration importConfig = null;
		private List<Map<String,String>> rows = new ArrayList<>();

		private void initialize(ImportConfiguration importConfiguration)
		{
	  		this.importConfig = importConfiguration;
		}
		
		protected void readExcelSheet(String sheet)
		{		 
			ExcelReadConfiguration readConfig = importConfig.getExcelReadConfiguration();
			readConfig.setSheetName(sheet);
			WorkBookManager manager =(WorkBookManager)importConfig.getContentManager();
			rows  = manager.read(readConfig);   
		}

		@Override
		protected void importContent(ImportConfiguration importConfiguration) 
		{
			initialize(importConfiguration);
			readExcelSheet(manifestFileName);
			processRows();
		}

		private void processRows() 
		{
			for(Map<String,String> column:rows)
			{
				String columnValue = column.get(manifestColumnName);
				manifest.add(columnValue);
				//System.out.println(columnValue);
			}
			
		}
	}	
	public void initilize(ImportConfiguration importParameter)
	{
		this.importParameter = (ImportConfigurationImpl)importParameter;		
	}	
	private LinkedHashMap<String, File> arrangeFilesUsingManifest(List<File> files) 
	{
		LinkedHashMap<String,File> processedManifestFileMappings = new LinkedHashMap<>();
		for(String manifestEntry:manifest)
		{
			boolean ismanifestEntryExists = false;
			for(File file:files)
			{
				if(file.getName().indexOf(manifestEntry) >= 0)
	  			{
					processedManifestFileMappings.put(manifestEntry, file);
					ismanifestEntryExists = true;
					break;
	  			}
			}
			if(!ismanifestEntryExists)
			{
				ContentManagementRuntimeException exception = new ContentManagementRuntimeException(Messages.NO_WORKBOOK_FOR_MANIFEST_ENTRY, manifestEntry);
		    	throw exception;
			}
		}
		return processedManifestFileMappings;
	}
    private Object initiateClass(String className)
    {
	    Object object = null;
    	try
    	{
    		object = com.opentext.apps.cc.importcontent.ClassLoader.initiateClass(className);
    	}
    	catch(RuntimeException e)
    	{
    		ContentManagementRuntimeException exception = new ContentManagementRuntimeException(e,Messages.ERROR_LOADING_CLASS,className);
  			throw exception;
    	} 
    	return object;
    }
}
