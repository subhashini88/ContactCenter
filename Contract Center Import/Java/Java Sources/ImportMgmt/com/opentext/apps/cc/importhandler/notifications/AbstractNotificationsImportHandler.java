package com.opentext.apps.cc.importhandler.notifications;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.cordys.cpc.bsf.busobject.BSF;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.util.system.EIBProperties;
import com.opentext.apps.cc.importcontent.BatchIterator;
import com.opentext.apps.cc.importcontent.ContentImporter;
import com.opentext.apps.cc.importcontent.FileUtil;
import com.opentext.apps.cc.importcontent.ImportConfiguration;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.WorkBookManager;
import com.opentext.apps.cc.importcontent.WorkBookManagerImpl;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;

public abstract class AbstractNotificationsImportHandler extends ContentImporter {

	protected List<ReportItem> violationsReport = new LinkedList<>();
	private Properties properties = new Properties();
	protected ImportConfiguration importConfig;
	public final int BATCH_SIZE = 500;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(AbstractNotificationsImportHandler.class);
	private boolean importStatus = true;
	private boolean isProcesStatesAreThere = true;

	public AbstractNotificationsImportHandler() {
	}

	protected void importContent(ImportConfiguration configuration) {
		this.importConfig = configuration;
		if (Objects.nonNull(this.importConfig)) {
			properties = getMappingProperties();
			configuration.setMappedInstances(new LinkedHashMap<String, String>());
			Map<String, List<Map<String, String>>> sheetsWiseReport = new HashMap<>();
			for (String sheetName : getSheetsName()) {
				List<Map<String, String>> sheetData = super.readContent(sheetName);
				try {
					Iterator<List<Map<String, String>>> batchIterator = new BatchIterator(sheetData, properties,
							BATCH_SIZE);
					List<Map<String, String>> reportData = new ArrayList<Map<String, String>>();
					while (batchIterator.hasNext()) {
						Collection<ImportListener> records = new LinkedList<>();

						List<Map<String, String>> rows = batchIterator.next();
						for (Map<String, String> row : rows) {
							ImportListener record = processRow(configuration, row, sheetName);
							if (null != record) {
								records.add(record);
							}
							reportData.add(row);
						}
						commit(records, sheetName);

					}
					sheetsWiseReport.put(sheetName, reportData);
				} catch (Exception exception) {
					importStatus = false;
				}
			}
			generateReports(sheetsWiseReport);

		} else {
			importStatus = false;
		}
	}

	protected void generateReports(Map<String, List<Map<String, String>>> reportData) {

		String orgDN = BSF.getOrganization();
		String orgName = orgDN.substring(orgDN.indexOf('=') + 1, orgDN.indexOf(','));

		if (Objects.nonNull(reportData) && !reportData.isEmpty()) {
			try {
				String filePath = FileUtil.getDownloadReadPath() + orgName + File.separator + "notifications"
						+ File.separator + getWorkBookName();
				filePath = filePath.substring(0, filePath.lastIndexOf(ImportConstants.EXCEL_EXTENSION))
						+ ImportConstants.REPORT + ImportConstants.EXCEL_EXTENSION;
				WorkBookManager wbManager = new WorkBookManagerImpl(Paths.get(filePath));
				final Workbook wb = wbManager.getWorkBook();
				for (String sheetName : reportData.keySet()) {
					List<Map<String, String>> sheetData = reportData.get(sheetName);
					if (Objects.nonNull(sheetData) && !sheetData.isEmpty()) {
						Collection<String> columns = sheetData.iterator().next().keySet();
						Sheet sheet = wbManager.createSheet(sheetName);
						wbManager.createRow(sheet, columns, wbManager.createHeaderStyle(wb), 0);
						int rowIndex = 1;
						Iterator<Map<String, String>> rows = sheetData.iterator();
						while (rows.hasNext()) {
							Collection<String> rowData = rows.next().values();
							wbManager.createRow(sheet, rowData, null, rowIndex++);

						}
					}
				}
				wbManager.createFile(wb, filePath);
			} catch (IOException e) {
				logger._log("com.opentext.apps.cc.importhandler.notifications.AbstractNotificationsImportHandler",
						Severity.ERROR, e, "Error while generating the report for : " + getWorkBookName());
			}
		}
	}

	public abstract String[] getSheetsName();

	public abstract String getWorkBookName();

	protected abstract ImportListener processRow(ImportConfiguration configuration, Map<String, String> row,
			String sheetName);

	protected abstract void commit(Collection<ImportListener> records, String sheetName);

	protected Properties getMappingProperties() {
		if (properties.isEmpty()) {
			Path path = getAbsolutePath(Paths.get(ImportConstants.PROPERTIES_FILE_NAME));
			URI mappingFile = path.toUri();
			try {
				properties.load(mappingFile.toURL().openStream());
			} catch (IOException e) {
				logger._log("com.opentext.apps.cc.importhandler.notifications.AbstractNotificationsImportHandler",
						Severity.ERROR, e, ImportConstants.PROPERTIES_FILE_NAME + " file not found.");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.FILE_NOT_FOUND,
						ImportConstants.PROPERTIES_FILE_NAME);
			}
		}
		return properties;
	}

	public boolean isImportStatus() {
		return importStatus;
	}

	protected void setImportStatus(boolean importStatus) {
		this.importStatus = importStatus;
	}

	public boolean isProcesStatesAreThere() {
		return isProcesStatesAreThere;
	}

	public void setProcesStatesAreThere(boolean isProcesStatesAreThere) {
		this.isProcesStatesAreThere = isProcesStatesAreThere;
	}

	public static String getNotificationZipPath() {
		String orgDN = BSF.getOrganization();
		String orgName = orgDN.substring(orgDN.indexOf('=') + 1, orgDN.indexOf(','));
		String OrgPath = Paths.get(EIBProperties.getInstallDir())
				.resolve("webroot" + File.separator + "organization" + File.separator + orgName + File.separator + "com"
						+ File.separator + "opentext" + File.separator + "apps" + File.separator + "notifications"
						+ File.separator + "samples" + File.separator + "Notifications.zip")
				.toString();
		if (new File(OrgPath).exists()) {
			return OrgPath;
		} else {
			String SharedPath = Paths.get(EIBProperties.getInstallDir())
					.resolve("webroot" + File.separator + "shared" + File.separator + "com" + File.separator
							+ "opentext" + File.separator + "apps" + File.separator + "notifications" + File.separator
							+ "samples" + File.separator + "Notifications.zip")
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
			logger._log("com.opentext.apps.cc.importhandler.notifications.AbstractNotificationsImportHandler",
					Severity.ERROR, e, zipFileName + " file not found.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.FILE_NOT_FOUND, zipFileName);
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
				contentPath = Paths.get(FileUtil.getDownloadReadPath() + orgName + File.separator + jobID).toString();
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
