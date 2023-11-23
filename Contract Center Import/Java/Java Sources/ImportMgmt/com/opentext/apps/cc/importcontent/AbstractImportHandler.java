package com.opentext.apps.cc.importcontent;

import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.util.system.EIBProperties;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;


public abstract class AbstractImportHandler extends ContentImporter{
	protected List<ReportItem> violationsReport = new LinkedList<>();
	private final List<ReportListener> listners = new LinkedList<>();
	protected Properties properties = new Properties();
	protected ImportConfiguration importConfig;
	public int BatchSize = 50; 
	private static final CordysLogger logger = CordysLogger.getCordysLogger(AbstractImportHandler.class);
	public String batchuploadstatus;
	@SuppressWarnings("deprecation")
	@Override
	protected void importContent(ImportConfiguration configuration) {
		this.importConfig = configuration;
		properties = getMappingProperties();
		configuration.setMappedInstances(new LinkedHashMap<String,String>());
		logger.log(Severity.INFO, "Start time of import : "+System.currentTimeMillis());
		List<Map<String, String>> sheetData = super.readContent(getSheetName());
		String jobId = configuration.getJobId();
		if(!configuration.isRetrigger())
		{
			if(!statusDump(sheetData,jobId)){
				logger._log("com.opentext.apps.cc.custom.AbstractImportHandler", Severity.ERROR, null, "Status dump is failed.");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.STATUS_DUMP_FAILED);
			}
		}

		try (CharArrayWriter arrayWriter = new CharArrayWriter();Writer reportWriter = new BufferedWriter(arrayWriter)) {
			Iterator<List<Map<String, String>>> batchIterator = new BatchIterator(sheetData, properties, BatchSize);
			while (batchIterator.hasNext()) {
				logger.log(Severity.INFO, "Start time of batch iteration : "+System.currentTimeMillis());
				try 
				{
					Collection<ImportListener> records = new LinkedList<>();
					List<Map<String, String>> rows = batchIterator.next();
					for (Map<String, String> row : rows) 
					{
						ImportListener record = processRow(configuration, row);
						if (null != record) 
						{
							records.add(record);
						}
					}
					if(!batchIterator.hasNext()) {
						batchuploadstatus="Completed";	
					}
					commit(records);
				} catch (Exception exception) {
					throw new RuntimeException(exception);
				}
			}
		} catch (IOException error) {
			throw new RuntimeException(error);
		}
	}

	protected abstract String getSheetName();

	protected abstract ImportListener processRow(ImportConfiguration configuration, Map<String, String> row);

	protected abstract ReportListener createReportListener();

	protected abstract void commit(Collection<ImportListener> records);

	protected abstract boolean statusDump(List<Map<String, String>> sheetData, String jobId);

	protected void postCommit(Collection<ImportListener> records) {
		for (ImportListener record : records) {
			record.postCommit();
		}
	}

	protected Properties getMappingProperties() {
		if (properties.isEmpty()) {
			Path path = getAbsolutePath(Paths.get(ImportConstants.PROPERTIES_FILE_NAME));
			URI mappingFile = path.toUri();
			try {
				properties.load(mappingFile.toURL().openStream());
			} catch (IOException e) {
				logger._log("com.opentext.apps.cc.custom.AbstractImportHandler", Severity.ERROR, e, ImportConstants.PROPERTIES_FILE_NAME+" file not found.");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.FILE_NOT_FOUND,ImportConstants.PROPERTIES_FILE_NAME);
			} 
		}
		return properties;
	}

	protected void addReportListener(ReportListener listener) {
		listners.add(listener);
	}

	public void updateImportJob(String jobId, String jobStatus, int count) {
		int updateImportJobNode = 0, updateDataJobNode = 0, updateDataImportResponse = 0;
		try{
			SOAPRequestObject updateImportJobRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/DataImport/operations", "UpdateDataImport", null, null);
			updateImportJobNode = NomUtil.parseXML("<DataImport-id></DataImport-id>");
			Node.setDataElement(updateImportJobNode, "Id", jobId);
			updateDataJobNode = NomUtil.parseXML("<DataImport-update></DataImport-update>");
			Node.setDataElement(updateDataJobNode, "ImportStatus", jobStatus);
			Node.setDataElement(updateDataJobNode, "StatusDumpData", getJsonToUpdateStatusDumpData(jobId, count));
			updateImportJobRequest.addParameterAsXml(updateDataJobNode);
			updateImportJobRequest.addParameterAsXml(updateImportJobNode);
			updateDataImportResponse = updateImportJobRequest.sendAndWait();
		}finally{
			Utilities.cleanAll(updateImportJobNode,updateDataJobNode,updateDataImportResponse);
		}
	}

	public int readDataImportEntity(String jobId) {
		int readImportJobNode=0, readDataImportResponse = 0;
		try{
			SOAPRequestObject readImportJobRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/DataImport/operations", "ReadDataImport", null, null);
			readImportJobNode = NomUtil.parseXML("<DataImport-id></DataImport-id>");
			Node.setDataElement(readImportJobNode, "Id", jobId);
			readImportJobRequest.addParameterAsXml(readImportJobNode);
			readDataImportResponse = readImportJobRequest.sendAndWait();
			return readDataImportResponse;//TODO - We should check count at each entity level not on a whole.
		}finally{
			Utilities.cleanAll(readImportJobNode);
		}

	}

	protected int getStatusDumpCount(String jobId) {
		int readDataImportResponse = readDataImportEntity(jobId);
		String countValue = "";
		try {
			countValue = getCountfromStatusDumpData(Node.getDataWithDefault(NomUtil.getNode(".//StatusDumpData", readDataImportResponse),null));
			if(countValue.isEmpty())
			{
				countValue = Node.getDataWithDefault(NomUtil.getNode(".//StatusDumpCount", readDataImportResponse),null);
			}
			int count=0;
			if(!Objects.isNull(countValue)) {
				count=Integer.parseInt(countValue);
			}
			return count;
		} finally {
			Utilities.cleanAll(readDataImportResponse);
		}
	}

	public String getCountfromStatusDumpData(String statusDumpData) {
		String count = "";
		String key = getSheetName();
		if (Objects.isNull(statusDumpData) || statusDumpData.isBlank()) {
			statusDumpData = "";
		}
		statusDumpData = statusDumpData.replace("{", "").replace("}", "").replace("\"", "");
		String[] tokens = statusDumpData.split(",");
		if (Objects.nonNull(key) && !key.isBlank()) {
			for (String token : tokens) {
				token = token.trim();
				String[] keyvalue = token.split(":");
				if (keyvalue[0].equalsIgnoreCase(key)) {
					count = keyvalue.length > 1 ? keyvalue[1] : "";
				}
			}
		}
		return count;
	}

	public String getJsonToUpdateStatusDumpData(String jobId, int count) {
		String statusDumpDatatoUpdate = "";
		int readDataImportResponse = readDataImportEntity(jobId);
		String statusDumpDataOld = Node.getDataWithDefault(NomUtil.getNode(".//StatusDumpData", readDataImportResponse),null);
		if (!Objects.isNull(statusDumpDataOld)) {
			String valueToUpdate = ",\""+getSheetName()+"\":\""+count+"\"}";
			statusDumpDatatoUpdate = statusDumpDataOld.replace("}", valueToUpdate);
		}
		else {
			statusDumpDatatoUpdate = "{\""+getSheetName()+"\":\""+count+"\"}";
		}
		return statusDumpDatatoUpdate;
	}
	public AbstractImportHandler() {
		this.addReportListener(createReportListener());
	}
	protected File getReportFile() {
		try {
			Path zipFilePath = Paths.get(importConfig.getZipFileName());
			String fileName = zipFilePath.getFileName().toString();
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
			String importReportDir = "com/opentext/bpm/spa/contractmgmt/import_logs/";
			Path dir = Paths.get(EIBProperties.getInstallDir()).resolve(importReportDir);
			if (!Files.isDirectory(dir)) {
				Files.createDirectory(dir);
			}
			Path orgDir = Paths.get(EIBProperties.getInstallDir()).resolve(importReportDir+Utilities.getOrganization() );
			if (!Files.isDirectory(orgDir)){
				Files.createDirectory(orgDir);
			}
			return Paths.get(orgDir.toString(), fileName + ".csv").toFile();
		} catch (IOException e) {
			throw new ContractMgmtRuntimeException(e, ContractManagementMessages.ERROR_READING_PROPERTIES_FILE);
		}
	}

	protected String getPropertyNameForDocumentsFolder() {
		return null;
	}

	protected Map<String, Map<String, String>> getSheetData(final Path path, final String sheetName, final String mapBy) {
		Map<String, Map<String, String>> rowData = null;
		try (InputStream s = Files.newInputStream(path);) {
			WorkBookManagerImpl manager = new WorkBookManagerImpl(path, s);
			rowData = manager.readAsMap(sheetName, mapBy);
		} catch (IOException e) {
			throw new ContentManagementRuntimeException(e);
		}
		return rowData;
	}

	protected List<List<Map<String, String>>> getSheetDataSet(final Path path, final String sheetName, final String mapBy, String recordName, String subRecordName) {
		List<List<Map<String, String>>> rowData = null;
		try (InputStream s = Files.newInputStream(path);) {
			WorkBookManagerImpl manager = new WorkBookManagerImpl(path, s);
			rowData = manager.readAsMapSet(sheetName, mapBy, recordName, subRecordName);
		} catch (IOException e) {
			throw new ContentManagementRuntimeException(e);
		}
		return rowData;
	}

	protected void createErrorFile(List<Map<String, String>> contract_rows, final String filePath, final String sheetName, ImportConfiguration importparm) throws IOException {
		if (!ImportUtils.isEmpty(filePath) && !ImportUtils.isEmpty(contract_rows)) {
			WorkBookManager wbManager = new WorkBookManagerImpl(Paths.get(filePath));
			final Workbook wb = wbManager.getWorkBook();
			Collection<String> columns = contract_rows.iterator().next().keySet();
			Sheet sheet = wbManager.createSheet(sheetName);
			wbManager.createRow(sheet, columns, wbManager.createHeaderStyle(wb), 0);
			int rowIndex = 1;
			Iterator<Map<String, String>> rows = contract_rows.iterator();
			while (rows.hasNext()) {
				Collection<String> rowData = rows.next().values();
				wbManager.createRow(sheet, rowData, null, rowIndex++);

			}
			wbManager.createFile(wb, filePath);
			String basePath = Paths.get(EIBProperties.getWebDir()).resolve("organization"+File.separator+importparm.getOrgName()+File.separator+"ccimportreports").toString();
			final Path CONTRACT_PATH = Paths.get(basePath);
			//File reportsFolder = CONTRACT_PATH.toFile();
			//if (!Files.isDirectory(CONTRACT_PATH)) {
			//	Files.createDirectory(CONTRACT_PATH);
			//}
			try {
				String documentName=filePath.substring(filePath.lastIndexOf(File.separator)+1);

				//Process Platform only able to upload documents in webroot
				//copying file to the webroot
				//Files.copy(Paths.get(filePath), Paths.get(basePath+"/"+documentName),StandardCopyOption.REPLACE_EXISTING);

				//ItemId Required for uploading document to the entity
				SOAPRequestObject readRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/DataImport/operations", "ReadDataImport", null, null); 
				int requestNode = NomUtil.parseXML("<DataImport-id><Id>"+importparm.getJobId()+"</Id></DataImport-id>");
				readRequest.addParameterAsXml(requestNode);
				int response = readRequest.sendAndWait();
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//DataImport-id/ItemId", response),null);

				SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas.opentext.com/bps/entity/buildingblock/file", "UploadDocument", null, null);
				int createNode = NomUtil.parseXML("<Create></Create>");
				int itemIdNode = Node.createElementNS("ItemId",itemId,"","http://schemas.opentext.com/bps/entity/core",createNode); // NomUtil.parseXML("<ItemId>"+itemId+"</ItemId>");
				Node.appendToChildren(itemIdNode, createNode);
				int documentNode = NomUtil.parseXML("<FileName>"+documentName+"</FileName>");
				Node.appendToChildren(documentNode, createNode);
				//int docURLNode = NomUtil.parseXML("<FileURL>"+Utilities.getDocumentURL(importparm.getOrgName(), "ccimportreports/"+documentName)+"</FileURL>");
				int docURLNode = NomUtil.parseXML("<FileURL>"+new File(filePath).toURI()+"</FileURL>");
				Node.appendToChildren(docURLNode, createNode);
				int folderNode = NomUtil.parseXML("<FolderPath>Reports</FolderPath>");
				Node.appendToChildren(folderNode, createNode);
				int OverwriteNode = NomUtil.parseXML("<Overwrite>false</Overwrite>");
				Node.appendToChildren(OverwriteNode, createNode);

				createRequest.addParameterAsXml(createNode);

				try {
					createRequest.execute();
				}catch(Exception e){
					logger._log("com.opentext.apps.cc.custom.AbstractImportHandler", Severity.ERROR, e, "Error while uploading file to the content building block.");
					throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_UPLOADING_FILE);
				}

			}catch(Exception e){
				logger._log("com.opentext.apps.cc.custom.AbstractImportHandler", Severity.ERROR, e, "Error while opening the file.");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_OPENING_FILE);
			}
		}
	}

	public String getKey(Object key){
		String userAttr = (String) properties.get(key);
		String sheetHeader = (String) ((null!=userAttr) ? userAttr:key);
		return sheetHeader.trim();
	}

}
