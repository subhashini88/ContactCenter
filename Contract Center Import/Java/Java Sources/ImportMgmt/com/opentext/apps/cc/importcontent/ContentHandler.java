/*
  This class has been generated by the Code Generator
 */

package com.opentext.apps.cc.importcontent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.cordys.cpc.bsf.busobject.BSF;
import com.cordys.cpc.bsf.busobject.BusObjectConfig;
import com.cordys.cpc.bsf.busobject.BusObjectIterator;
import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.util.system.EIBProperties;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importhandler.datamanager.AbstractDataManagerImportHandler;
import com.opentext.apps.cc.importhandler.datamanager.landingPages.LandingPagesHandler;
import com.opentext.apps.cc.importhandler.datamanager.masterdata.MasterDataImportHandler;
import com.opentext.apps.cc.importhandler.datamanager.ruleconditions.RuleConditionsHandler;
import com.opentext.apps.cc.importhandler.datamanager.statesandactions.StatesAndActionsHandler;
import com.opentext.apps.cc.importhandler.datamanager.tasklist.TaskListImportHandler;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;
import com.opentext.apps.cc.importhandler.notifications.AbstractNotificationsImportHandler;
import com.opentext.apps.cc.importhandler.notifications.configuratorlist.ConfiguratorImportHandler;
import com.opentext.apps.cc.importhandler.notifications.process.ProcessImportHandler;

public class ContentHandler extends ContentHandlerBase {

	public static Path INSTALLATION_ROOT = Paths.get(EIBProperties.getInstallDir());
	public static Path EXCEL_FILE_LOCATION = null;

	private static final CordysLogger logger = CordysLogger.getCordysLogger(ContentHandler.class);

	public ContentHandler() {
		this((BusObjectConfig) null);
	}

	public ContentHandler(BusObjectConfig config) {
		super(config);
	}

	public static String ImportContent(String fileName, String jobId, boolean isRetrigger, boolean matchContracts,
			boolean isReimportMetadata) {
		if (fileName == null || jobId == null) {
			logger._log("com.opentext.apps.cc.custom.ContentHandler", Severity.ERROR, null, "Invalid Parameters.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.INVALID_PARAMETERS);
		}
		ImportConfiguration configImpl = new ImportConfigurationImpl(fileName);
		configImpl.setZipFileName(fileName);
		configImpl.setJobId(jobId);
		configImpl.setIsRetrigger(isRetrigger);
		configImpl.setMatchContracts(matchContracts);
		configImpl.setIsReimportMetadata(isReimportMetadata);
		new ContentManager().handleImport(configImpl);
		return "true";
	}

	public static void generateErrorReports(String jobId, String fileName, String orgName) {
		if (fileName == null || jobId == null) {
			logger._log("com.opentext.apps.cc.custom.ContentHandler", Severity.ERROR, null, "Invalid Parameters.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.INVALID_PARAMETERS);
		}
		ImportConfiguration configImpl = new ImportConfigurationImpl(fileName);
		configImpl.setOrgName(orgName);
		configImpl.setZipFileName(fileName);
		configImpl.setJobId(jobId);
		new ContentManager().generateErrorReports(configImpl);
	}

	public static String getFilesXMLforUpload(String contractNumber, String orgName, String is_xECM) {
		if (is_xECM.equalsIgnoreCase("true")) {
			return getFilesXMLforxECM(contractNumber, orgName);
		} else {
			return getFilesXML(contractNumber, orgName, null);
		}
	}

	private static String getFilesXMLforxECM(String contractNumber, String orgName) {
		final String _DOCUMENTS_PATH = ImportProperties.getImportProperty(ImportConstants.DOCUMENTS_PATH);
		final String _DOCUMENTS_URL = ImportProperties.getImportProperty(ImportConstants.DOCUMENTS_URL);
		final String _DOCUMENTS_FOLDER = ImportProperties.getImportProperty(ImportConstants.DOCUMENTS_FOLDER);

		try {
			StringBuilder xml = new StringBuilder();
			String basePath = null;
			String baseURL = null;
			if (!ImportUtils.isEmpty(_DOCUMENTS_PATH)) {
				basePath = _DOCUMENTS_PATH;
			} else {
				basePath = Paths.get(EIBProperties.getWebDir()).resolve("organization"+File.separator + orgName + File.separator+"ccimport")
						.toString();
			}
			xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
			xml.append("<ccfileinput>");
			if (contractNumber != null && orgName != null) {
				final Path CONTRACT_PATH = Paths.get(basePath + File.separator + contractNumber);
				File contractFolder = CONTRACT_PATH.toFile();
				if (contractFolder.exists()) {
					for (File childFolder : contractFolder.listFiles()) {
						if (childFolder == null) {
							continue;
						}
						if (!childFolder.isFile()) {
							for (File attachment : childFolder.listFiles()) {
								if (attachment == null) {
									continue;
								}
								if (attachment.isFile()) {
									String url = basePath + encodeURL(contractNumber + File.separator
											+ childFolder.getName() + File.separator + attachment.getName());
									xml.append("<ccfile>");
									xml.append("<filename>" + attachment.getName() + "</filename>");
									xml.append("<foldername>" + getFolderName(childFolder.getName(), _DOCUMENTS_FOLDER)
											+ "</foldername>");
									xml.append("<fileurl>" + attachment.toString() + "</fileurl>");
									xml.append(
											"<mimetype>" + Files.probeContentType(attachment.toPath()) + "</mimetype>");
									xml.append("</ccfile>");
								}

							}
						} else {
							String url = basePath + encodeURL(contractNumber + File.separator + childFolder.getName());
							xml.append("<ccfile>");
							xml.append("<filename>" + childFolder.getName() + "</filename>");
							xml.append("<foldername>" + getFolderName("", _DOCUMENTS_FOLDER) + "</foldername>");
							xml.append("<fileurl>" + childFolder.toString() + "</fileurl>");
							xml.append("<mimetype>" + Files.probeContentType(childFolder.toPath()) + "</mimetype>");
							xml.append("</ccfile>");
						}
					}
				}
			}
			xml.append("</ccfileinput>");
			return xml.toString();
		} catch (IOException e) {
			throw new RuntimeException("Provided URL is not Valid", e);
		}
	}

	public static String getFilesXML(String contractNumber, String orgName, String orgurl) {
		final String _DOCUMENTS_PATH = ImportProperties.getImportProperty(ImportConstants.DOCUMENTS_PATH);
		final String _DOCUMENTS_URL = ImportProperties.getImportProperty(ImportConstants.DOCUMENTS_URL);
		final String _DOCUMENTS_FOLDER = ImportProperties.getImportProperty(ImportConstants.DOCUMENTS_FOLDER);
		StringBuilder xml = new StringBuilder();
		String basePath = null;
		String baseURL = null;
		if (!ImportUtils.isEmpty(_DOCUMENTS_PATH)) {
			basePath = _DOCUMENTS_PATH;
		} else {
			basePath = Paths.get(EIBProperties.getWebDir()).resolve("organization"+File.separator + orgName + File.separator+"ccimport")
					.toString();
		}
		if (!ImportUtils.isEmpty(_DOCUMENTS_URL)) {
			baseURL = _DOCUMENTS_URL;
		} else {
			baseURL = getDefaultURL(orgName, orgurl);
		}

		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
		xml.append("<ccfileinput>");
		if (contractNumber != null && orgName != null) {
			final Path CONTRACT_PATH = Paths.get(basePath + File.separator + contractNumber);
			File contractFolder = CONTRACT_PATH.toFile();
			if (contractFolder.exists()) {
				for (File childFolder : contractFolder.listFiles()) {
					if (childFolder == null) {
						continue;
					}
					if (!childFolder.isFile()) {
						for (File attachment : childFolder.listFiles()) {
							if (attachment == null) {
								continue;
							}
							if (attachment.isFile()) {
								String url = baseURL + encodeURL("/" + contractNumber + "/" + childFolder.getName()
										+ '/' + attachment.getName());
								xml.append("<ccfile>");
								xml.append("<filename>" + attachment.getName() + "</filename>");
								xml.append("<foldername>" + getFolderName(childFolder.getName(), _DOCUMENTS_FOLDER)
										+ "</foldername>");
								xml.append("<fileurl>" + url + "</fileurl>");
								xml.append("</ccfile>");
							}

						}
					} else {
						String url = baseURL + encodeURL("/" + contractNumber + "/" + childFolder.getName());
						xml.append("<ccfile>");
						xml.append("<filename>" + childFolder.getName() + "</filename>");
						xml.append("<foldername>" + getFolderName("", _DOCUMENTS_FOLDER) + "</foldername>");
						xml.append("<fileurl>" + url + "</fileurl>");
						xml.append("</ccfile>");
					}
				}
			}
		}
		xml.append("</ccfileinput>");
		return xml.toString();
	}

	public static String getDefaultURL(String orgName, String orgurl) {
		String url = orgurl;
		if (null == orgurl) {
			url = Utilities.getDocumentURL(orgName, "");
		} else {
			try {
				URL urlValidator = new URL(url);
			} catch (MalformedURLException e) {
				url = Utilities.getDocumentURL(orgName, "");
			}
		}
		return url + "/ccimport";
	}

	private static String encodeURL(String url) {
		StringBuilder encodedURL = new StringBuilder();
		String nodes[] = url.split("/");
		try {
			for (int index = 0; index < nodes.length - 1; index++) {
				encodedURL.append(URLEncoder.encode(nodes[index], "UTF-8"));
				encodedURL.append("/");
			}
			encodedURL.append(URLEncoder.encode(nodes[nodes.length - 1], "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			logger._log("com.opentext.apps.cc.custom.ContentHandler", Severity.ERROR, null,
					"Provided URL is not valid.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.URL_IS_NOT_VALIED);
		}
		return encodedURL.toString();
	}

	private static String getFolderName(String folderName) {
		final String attachments = "Attachments";
		final String mainContracts = "Contract documents";
		if (attachments.equalsIgnoreCase(folderName)) {
			return attachments;
		} else {
			return mainContracts;
		}
	}

	private static String getFolderName(String folderName, String configDefaultFolder) {
		final String attachments = "Attachments";
		String mainContracts = "Contract documents";

		if (attachments.equalsIgnoreCase(folderName) || mainContracts.equalsIgnoreCase(folderName)) {
			return folderName;
		}
		if (!Utilities.isStringEmpty(configDefaultFolder)) {
			mainContracts = configDefaultFolder;
		}
		return mainContracts;
	}

	public static boolean downloadFile(String storageTicket, String jobId) {
		int documentURLNode = 0, fetchSettingNode = 0, getDocumentResponse = 0;
		String documentURL = null;
		String documentName = null;
		Map<String, String> storageMap = null;
		boolean downloadStatus = false;

		if (null != storageTicket && storageTicket.length() > 0) {
			try {
				// storageMap = convertJSONtoList(storageTicket);
				storageMap = convertJsonToMap(storageTicket);
				if (storageMap.size() == 0) {
					return false;
				}
				documentURL = storageMap.get("documentURL");// getDocumentURL(storageTicket);
				documentName = storageMap.get("documentName");
				// Document document = null;

				String documentURLInput = "<DocumentURL fetchContent=\"true\">" + documentURL + "</DocumentURL>";
				String fetchSetting = "<FetchSetting>default</FetchSetting>";
				documentURLNode = NomUtil.parseXML(documentURLInput);
				// Node.setAttribute(documentURLNode, "fetchContent", "false");
				// Node.setData(documentURLNode,
				// "PradeepDocuments/system/OpenTextContractCenterImport/DataImport/16398/AccountManager.zip");
				fetchSettingNode = NomUtil.parseXML(fetchSetting);
				// document = NOMDocumentPool.getInstance().lendDocument();

				SOAPRequestObject importRequest = new SOAPRequestObject(
						"http://schemas.cordys.com/documentstore/default/1.0", "GetDocument", null, null);
				importRequest.addParameterAsXml(documentURLNode);
				importRequest.addParameterAsXml(fetchSettingNode);
				getDocumentResponse = importRequest.sendAndWait();
				String documentPath = FileUtil.getDownloadReadPath();
				File dictory = new File(documentPath + jobId);
				boolean isFolderExists = dictory.exists() ? true : dictory.mkdir();
				if (isFolderExists) {
					Path movefrom = FileSystems.getDefault().getPath(documentPath + documentName);
					Path target = FileSystems.getDefault()
							.getPath(documentPath + jobId + File.separatorChar + documentName);
					Path target_dir = FileSystems.getDefault().getPath(documentPath + jobId);
					Files.move(movefrom, target, StandardCopyOption.REPLACE_EXISTING);
				}
				downloadStatus = true;
			} catch (Exception e) {
				logger._log("com.opentext.apps.cc.custom.ContentHandler", Severity.ERROR, e, "");
				
			} finally {
				Utilities.cleanAll(documentURLNode, fetchSettingNode, getDocumentResponse);
				// NOMDocumentPool.getInstance().returnDocument(document);
			}
		}
		return downloadStatus;
	}

	public static Map<String, String> convertJsonToMap(String storageTicket) {
		Map<String, String> map = new HashMap<String, String>();
		if (storageTicket != null && storageTicket.length() > 0) {
			String tokens[] = storageTicket.replace("\"", "").replace("{", "").replace("}", "").split(",");
			for (int i = 0; i < tokens.length; i++) {
				String[] fieldTokens = tokens[i].split(":");
				map.put(fieldTokens[0], fieldTokens[1]);
			}
		}
		return map;
	}

	public static String importConfigurator() {
		Path IMPORT_CONTENT_EXTRACTED_FOLDER = AbstractNotificationsImportHandler.extractZipFile(
				Paths.get(AbstractNotificationsImportHandler.getNotificationZipPath()), "notifications");
		AbstractNotificationsImportHandler importHandler = new ConfiguratorImportHandler();
		EXCEL_FILE_LOCATION = IMPORT_CONTENT_EXTRACTED_FOLDER.resolve(importHandler.getWorkBookName());
		Map<Integer, Integer> attachments = new HashMap<>();
		ImportConfigurationImpl configImpl = new ImportConfigurationImpl(importHandler.getWorkBookName());
		configImpl.setZipFileName(importHandler.getWorkBookName());
		configImpl.setIsRetrigger(false);
		configImpl.setIsReimportMetadata(false);
		importHandler.manageImport(configImpl, EXCEL_FILE_LOCATION, attachments);
		if (!importHandler.isProcesStatesAreThere()) {
			return "PROCESS_REGISTRATION_SHOULD_BE_FIRST";
		}
		return String.valueOf(importHandler.isImportStatus());
	}

	public static String importProcess() {
		Path IMPORT_CONTENT_EXTRACTED_FOLDER = AbstractNotificationsImportHandler.extractZipFile(
				Paths.get(AbstractNotificationsImportHandler.getNotificationZipPath()), "notifications");
		AbstractNotificationsImportHandler importHandler = new ProcessImportHandler();
		EXCEL_FILE_LOCATION = IMPORT_CONTENT_EXTRACTED_FOLDER.resolve(importHandler.getWorkBookName());
		Map<Integer, Integer> attachments = new HashMap<>();
		ImportConfigurationImpl configImpl = new ImportConfigurationImpl(importHandler.getWorkBookName());
		configImpl.setZipFileName(importHandler.getWorkBookName());
		configImpl.setIsRetrigger(false);
		configImpl.setIsReimportMetadata(false);
		importHandler.manageImport(configImpl, EXCEL_FILE_LOCATION, attachments);
		return String.valueOf(importHandler.isImportStatus());
	}

	public static String importReferenceData(String dataManagerId, String importName) {
        AbstractDataManagerImportHandler importHandler = null;
        if ("RuleProperties".equals(importName)) {
            importHandler = new RuleConditionsHandler();
            createConfigAndInitiateImport(dataManagerId, importHandler);
        } else if ("MasterData".equals(importName)) {
            importHandler = new MasterDataImportHandler();
            createConfigAndInitiateImport(dataManagerId, importHandler);
            importHandler = new StatesAndActionsHandler();
            createConfigAndInitiateImport(dataManagerId, importHandler);
            importHandler = new RuleConditionsHandler();
            createConfigAndInitiateImport(dataManagerId, importHandler);
            importHandler = new LandingPagesHandler();
            createConfigAndInitiateImport(dataManagerId, importHandler);
            importHandler = new TaskListImportHandler();
            createConfigAndInitiateImport(dataManagerId, importHandler);
        } else if ("StatesAndActions".equals(importName)) {
            importHandler = new StatesAndActionsHandler();
            createConfigAndInitiateImport(dataManagerId, importHandler);
           
        }else{
			return null;
		}
        return String.valueOf(importHandler.isImportStatus());
    }

 

    private static void createConfigAndInitiateImport(String dataManagerId,
            AbstractDataManagerImportHandler importHandler) {
        Path IMPORT_CONTENT_EXTRACTED_FOLDER = AbstractDataManagerImportHandler.extractZipFile(
                Paths.get(AbstractDataManagerImportHandler.getImportDataZipPath(importHandler.getZipFileName())),
                importHandler.getJobIdName());
        importHandler.setDataManagerId(dataManagerId);
        EXCEL_FILE_LOCATION = IMPORT_CONTENT_EXTRACTED_FOLDER.resolve(importHandler.getWorkBookName());
        Map<Integer, Integer> attachments = new HashMap<>();
        ImportConfigurationImpl configImpl = new ImportConfigurationImpl(importHandler.getWorkBookName());
        configImpl.setZipFileName(importHandler.getWorkBookName());
        configImpl.setIsRetrigger(false);
        configImpl.setIsReimportMetadata(false);
        importHandler.manageImport(configImpl, EXCEL_FILE_LOCATION, attachments);
    }
	public static String getDemoDataZipPath(String zipName) {
		String orgDN = BSF.getOrganization();
		String orgName = orgDN.substring(orgDN.indexOf('=') + 1, orgDN.indexOf(','));
		String OrgPath = Paths.get(EIBProperties.getInstallDir())
				.resolve("webroot" + File.separator + "organization" + File.separator + orgName + File.separator + "com"
						+ File.separator + "opentext" + File.separator + "apps" + File.separator + "imports"
						+ File.separator + "samples" + File.separator + zipName + ".zip")
				.toString();
		if (new File(OrgPath).exists()) {
			return OrgPath;
		} else {
			String SharedPath = Paths.get(EIBProperties.getInstallDir())
					.resolve("webroot" + File.separator + "shared" + File.separator + "com" + File.separator
							+ "opentext" + File.separator + "apps" + File.separator + "imports" + File.separator
							+ "samples" + File.separator + zipName + ".zip")
					.toString();
			if (new File(SharedPath).exists()) {
				return SharedPath;
			}
			return "";
		}
	}
	public static String downloadNotificationImportReport(String notificationItem) {
		if (Objects.isNull(notificationItem)) {
			return "NO_REPORT_AVAILABLE";
		}

		String orgDN = BSF.getOrganization();
		String orgName = orgDN.substring(orgDN.indexOf('=') + 1, orgDN.indexOf(','));
		String filePath;
		if (notificationItem.startsWith("DemoData")) {
			filePath = getDemoDataZipPath(notificationItem);
		} else {
			filePath = FileUtil.getDownloadReadPath() + orgName + File.separator + "notifications" + File.separator;
			if (notificationItem.equals("ProcessRegistration")) {
				filePath = filePath + "ProcessListReport.xlsx";
			}
			if (notificationItem.equals("ConfiguratorList")) {
				filePath = filePath + "ConfiguratorsReport.xlsx";
			} 
		}
		FileInputStream inputStream = null;
		byte[] fileContent = null;
		try {
			File file = new File(filePath);
			fileContent = new byte[(int) file.length()];
			inputStream = new FileInputStream(file);
			inputStream.read(fileContent);

		} catch (IOException e) {
			return "NO_REPORT_AVAILABLE";
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger._log("com.opentext.apps.cc.custom.ContentHandler", Severity.ERROR, null, "Exception while closing input stream.");
				}
			}
		}
		return Base64.getEncoder().encodeToString(fileContent);
	}

	public static BusObjectIterator<com.opentext.apps.cc.importcontent.ContentHandler> getContentHandlerObjects(
			com.cordys.cpc.bsf.query.Cursor cursor) {
		// TODO implement body
		return null;
	}

	public void onInsert() {
	}

	public void onUpdate() {
	}

	public void onDelete() {
	}

}
