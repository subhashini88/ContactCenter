package com.opentext.apps.cc.importhandler.datamanager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.cordys.cpc.bsf.busobject.BSF;
import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.util.system.EIBProperties;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.BatchIterator;
import com.opentext.apps.cc.importcontent.ContentImporter;
import com.opentext.apps.cc.importcontent.ImportConfiguration;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;

public abstract class AbstractDataManagerImportHandler extends ContentImporter {

	protected List<ReportItem> violationsReport = new LinkedList<>();
	private Properties properties = new Properties();
	protected ImportConfiguration importConfig;
	public final int BATCH_SIZE = 200;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(AbstractDataManagerImportHandler.class);
	private boolean importStatus = true;
	private String processingRecordItemId = null;
	private String batchRecordItemId = null;
	private String dataManagerItemId = null;
    private int totalRecords = 0;
	private StringBuilder statusLog = null;

	public AbstractDataManagerImportHandler() {
		
	}

	protected void importContent(ImportConfiguration configuration) {
		this.importConfig = configuration;
		this.statusLog = new StringBuilder();
		int batchNumber;
		if (Objects.nonNull(this.importConfig)) {
			properties = getMappingProperties();
			configuration.setMappedInstances(new LinkedHashMap<String, String>());
			Map<String, List<Map<String, String>>> sheetsWiseReport = new HashMap<>();
			
			for (String sheetName : getSheetsName()) {
				List<Map<String, String>> sheetData = super.readContent(sheetName);
				createProcessingRecord(sheetName, sheetData.size());
				try {
					Iterator<List<Map<String, String>>> batchIterator = new BatchIterator(sheetData, properties,
							BATCH_SIZE);
					batchNumber = 0;
					while (batchIterator.hasNext()) {
						batchNumber++;
						Collection<ImportListener> records = new LinkedList<>();
						List<Map<String, String>> rows = batchIterator.next();
						createBatchRecord(this.processingRecordItemId, batchNumber, rows.size());
						
						for (Map<String, String> row : rows) {
							ImportListener record = processRow(configuration, row, sheetName);
							if (null != record) {
								records.add(record);
							}
						}
						commit(records, sheetName);

					}
					updateReadCompleteStatus(); //processingRecord

				} catch (Exception exception) {
					if (statusLog.length() > 0) {
						statusLog.append(", ");
					}
					statusLog.append(sheetName).append(" : ").append(exception.getMessage());
					importStatus = false;
				}
			}

			updateStatusLog();

		} else {
			importStatus = false;
		}
	}

	public abstract String[] getSheetsName();

	public abstract String getWorkBookName();

	protected abstract ImportListener processRow(ImportConfiguration configuration, Map<String, String> row,
			String sheetName);

	protected abstract void commit(Collection<ImportListener> records, String sheetName);

	public boolean isImportStatus() {
		return importStatus;
	}

	protected void setImportStatus(boolean importStatus) {
		this.importStatus = importStatus;
	}

	public void setDataManagerId(String id) {
		this.dataManagerItemId = id;
	}
	
	public String getDataManagerId() {
		return this.dataManagerItemId;
	}

	protected String getProcessingRecordItemId() {
		return this.processingRecordItemId;
	}
	
	protected String getBatchRecordItemId() {
		return this.batchRecordItemId;
	}
	
	protected void setBatchRecordItemId(String itemId) {
		this.batchRecordItemId = itemId;
	}
	
	protected int getTotalRecords() {
		return this.totalRecords;
	}
	
	public abstract String getZipFileName();
	
	public abstract String getMapingPropertiesFileName();
	
	public abstract String getJobIdName();
	
	
	private void updateReadCompleteStatus() {
		int processingRecordResponse = 0, datamanagerIdNode = 0, processingRecordNode = 0;
		int processingRecordNodes[] = null;

		try {

			SOAPRequestObject updateProcessingRecordRequest = new SOAPRequestObject(
					"http://schemas/OpenTextContractCenterImport/DataManager.ProcessingRecords/operations", "UpdateProcessingRecords",
					null, null);
			datamanagerIdNode = NomUtil.parseXML("<ProcessingRecords-id></ProcessingRecords-id>");
			Node.setDataElement(datamanagerIdNode, "ItemId1", this.processingRecordItemId);
			processingRecordNode = NomUtil.parseXML("<ProcessingRecords-update></ProcessingRecords-update>");
			Node.setDataElement(processingRecordNode, "ExcelReadStatus", "Completed");
			
			updateProcessingRecordRequest.addParameterAsXml(datamanagerIdNode);
			updateProcessingRecordRequest.addParameterAsXml(processingRecordNode);
			processingRecordResponse = updateProcessingRecordRequest.sendAndWait();

		} catch (Exception e) {
			logger._log("com.opentext.apps.cc.importhandler.datamanager.AbstractDataManagerImportHandler", Severity.ERROR, e,"Error while invoking data import at updateReadCompleteStatus.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_INVOKING_DATA_IMPORT);
		} finally {
			if (processingRecordNodes != null) {
				Utilities.cleanAll(processingRecordResponse, datamanagerIdNode, processingRecordNode);
			}
		}
		
	}

	protected void createProcessingRecord(String sheetName, int totalRecords) {

		int processingRecordResponse = 0, datamanagerIdNode = 0, processingRecordNode = 0;
		int processingRecordNodes[] = null;
		this.totalRecords = totalRecords;
		try {

			SOAPRequestObject createProcessingRecordRequest = new SOAPRequestObject(
					"http://schemas/OpenTextContractCenterImport/DataManager/operations", "CreateProcessingRecords",
					null, null);
			datamanagerIdNode = NomUtil.parseXML("<DataManager-id></DataManager-id>");
			Node.setDataElement(datamanagerIdNode, "ItemId", dataManagerItemId);
			processingRecordNode = NomUtil.parseXML("<ProcessingRecords-create></ProcessingRecords-create>");
			Node.setDataElement(processingRecordNode, "EntityName", sheetName);
			Node.setDataElement(processingRecordNode, "TotalRecords", String.valueOf(totalRecords));
			Node.setDataElement(processingRecordNode, "CompletedRecords", String.valueOf(0));
			Node.setDataElement(processingRecordNode, "ImportStatus", "InProgress");
			createProcessingRecordRequest.addParameterAsXml(datamanagerIdNode);
			createProcessingRecordRequest.addParameterAsXml(processingRecordNode);
			processingRecordResponse = createProcessingRecordRequest.sendAndWait();
			processingRecordNodes = NomUtil.getNodeList(".//ProcessingRecords", processingRecordResponse);
			if (Objects.nonNull(processingRecordNodes) && processingRecordNodes.length > 0) {
				this.processingRecordItemId = Node.getDataWithDefault(
						NomUtil.getNode(".//ProcessingRecords-id/ItemId1", processingRecordNodes[0]), null);
			}

		} catch (Exception e) {
			logger._log("com.opentext.apps.cc.importhandler.datamanager.AbstractDataManagerImportHandler", Severity.ERROR, e,"Error while invoking data import.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_INVOKING_DATA_IMPORT);
		} finally {
			if (processingRecordNodes != null) {
				Utilities.cleanAll(processingRecordResponse, datamanagerIdNode, processingRecordNode);
			}
		}
	}
	
	protected void createBatchRecord(String processingRecordId, int batchNumber, int batchSize) {

		int batchRecordResponse = 0, processingRecordIdNode = 0, batchRecordNode = 0;
		int batchRecordNodes[] = null; String itemId2;
		try {

			SOAPRequestObject createBatchRecordRequest = new SOAPRequestObject(
					"http://schemas/OpenTextContractCenterImport/DataManager.ProcessingRecords/operations", "CreateBatchRecord",
					null, null);
			processingRecordIdNode = NomUtil.parseXML("<ProcessingRecords-id></ProcessingRecords-id>");
			Node.setDataElement(processingRecordIdNode, "ItemId1", processingRecordId);
			
			batchRecordNode = NomUtil.parseXML("<BatchRecord-create></BatchRecord-create>");
			Node.setDataElement(batchRecordNode, "TotalRecords", String.valueOf(batchSize));
			Node.setDataElement(batchRecordNode, "ImportStatus", "InProgress");
			Node.setDataElement(batchRecordNode, "BatchNumber", String.valueOf(batchNumber));
			createBatchRecordRequest.addParameterAsXml(processingRecordIdNode);
			createBatchRecordRequest.addParameterAsXml(batchRecordNode);
			batchRecordResponse = createBatchRecordRequest.sendAndWait();
			batchRecordNodes = NomUtil.getNodeList(".//BatchRecord", batchRecordResponse);
			
			if (Objects.nonNull(batchRecordNodes) && batchRecordNodes.length > 0) {
				itemId2 = Node.getDataWithDefault(
						NomUtil.getNode(".//BatchRecord-id/ItemId2", batchRecordNodes[0]), null);
				setBatchRecordItemId(itemId2);
			}

		} catch (Exception e) {
			logger._log("com.opentext.apps.cc.importhandler.datamanager.AbstractDataManagerImportHandler", Severity.ERROR, e,"Error while invoking BatchRecord create service.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_INVOKING_DATA_IMPORT);
		} finally {
			if (batchRecordNodes != null) {
				Utilities.cleanAll(batchRecordResponse, processingRecordIdNode, batchRecordNode);
				
			}
		}
	}

	private void updateStatusLog() {

		if (Objects.nonNull(statusLog) && statusLog.length() > 0) {
			int processingRecordResponse = 0, datamanagerIdNode = 0, updateDataManager = 0;
			int processingRecordNodes[] = null;
			try {

				SOAPRequestObject createProcessingRecordRequest = new SOAPRequestObject(
						"http://schemas/OpenTextContractCenterImport/DataManager/operations", "UpdateDataManager", null,
						null);
				datamanagerIdNode = NomUtil.parseXML("<DataManager-id></DataManager-id>");
				Node.setDataElement(datamanagerIdNode, "ItemId", dataManagerItemId);
				updateDataManager = NomUtil.parseXML("<DataManager-update></DataManager-update>");
				Node.setDataElement(updateDataManager, "LogDetails", statusLog.toString());
				createProcessingRecordRequest.addParameterAsXml(datamanagerIdNode);
				createProcessingRecordRequest.addParameterAsXml(updateDataManager);
				processingRecordResponse = createProcessingRecordRequest.sendAndWait();

			} catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.datamanager.AbstractDataManagerImportHandler", Severity.ERROR, e,"Error while invoking data import.");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_INVOKING_DATA_IMPORT);
			} finally {
				if (processingRecordNodes != null) {
					Utilities.cleanAll(processingRecordResponse, datamanagerIdNode, updateDataManager);
				}
			}
		}
	}

	protected Properties getMappingProperties() {
		if (properties.isEmpty()) {
			Path path = getAbsolutePath(Paths.get("Mappings.properties"));
			URI mappingFile = path.toUri();
			try {
				properties.load(mappingFile.toURL().openStream());
			} catch (IOException e) {
				logger._log("com.opentext.apps.cc.importhandler.datamanager.AbstractDataManagerImportHandler", Severity.ERROR, e,"Mappings.properties" + " file not found.");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.FILE_NOT_FOUND,"Mappings.properties");
			}
		}
		return properties;
	}

	public static String getImportDataZipPath(String fileName) {
		String orgDN = BSF.getOrganization();
		String orgName = orgDN.substring(orgDN.indexOf('=') + 1, orgDN.indexOf(','));
		String OrgPath = Paths.get(EIBProperties.getInstallDir())
				.resolve("webroot" + File.separator + "organization" + File.separator + orgName + File.separator + "com"
						+ File.separator + "opentext" + File.separator + "apps" + File.separator + "data"
						+ File.separator + fileName + ".zip")
				.toString();
		if (new File(OrgPath).exists()) {
			return OrgPath;
		} else {
			String SharedPath = Paths.get(EIBProperties.getInstallDir())
					.resolve("webroot" + File.separator + "shared" + File.separator + "com" + File.separator
							+ "opentext" + File.separator + "apps" + File.separator + "data" + File.separator
							+ fileName + ".zip")
					.toString();
			if (new File(SharedPath).exists()) {
				return SharedPath;
			}
			return "";
		}
	}

	public static Path extractZipFile(Path zipFileName, String jobID) {
		Path zipContentFolder;
		try {
			int zipIndex = zipFileName.toString().indexOf(".zip");
			zipContentFolder = unzip(zipFileName.toFile(), jobID);
		}

		catch (IOException e) {
			logger._log("com.opentext.apps.cc.importhandler.datamanager.AbstractDataManagerImportHandler", Severity.ERROR, e,zipFileName + " file not found.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.FILE_NOT_FOUND,zipFileName);
		}

		return zipContentFolder;
	}

	public static final Path unzip(File zip, String jobID) throws IOException {
		String contentPath = null;
		String orgDN = BSF.getOrganization();
		String orgName = orgDN.substring(orgDN.indexOf('=') + 1, orgDN.indexOf(','));
		try (ZipFile archive = new ZipFile(zip);) {
			Enumeration<? extends ZipEntry> e = archive.entries();
			while (e.hasMoreElements()) {
				ZipEntry entry = e.nextElement();
				contentPath = Paths.get(EIBProperties.getInstallDir()).resolve("content" + File.separator
						+ "downloadcontent" + File.separator + orgName + File.separator + jobID).toString();
				File file = new File(contentPath, entry.getName());
				if (entry.isDirectory() && !file.exists()) {
					file.mkdirs();
				} else {
					if (!file.getParentFile().exists()) {
						file.getParentFile().mkdirs();
					}
					if (!file.isDirectory()) {
						try (InputStream in = archive.getInputStream(entry);
								BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));) {
							byte[] buffer = new byte[8192];
							int read;
							while (-1 != (read = in.read(buffer))) {
								out.write(buffer, 0, read);
							}
						}
					}
				}
			}
		}
		return Paths.get(contentPath);
	}
}
