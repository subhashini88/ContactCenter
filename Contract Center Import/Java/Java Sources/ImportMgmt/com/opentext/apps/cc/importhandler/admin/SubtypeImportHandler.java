package com.opentext.apps.cc.importhandler.admin;

import static com.opentext.apps.cc.importhandler.admin.AdminManagementConstants.IMPORT_SUBTYPES_SHEET_NAME;
import static com.opentext.apps.cc.importhandler.admin.AdminManagementConstants.PROPERTIES_FILE_NAME;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.cordys.cpc.bsf.busobject.BusObjectManager;
import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.internal.NOMDocumentPool;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.BatchIterator;
import com.opentext.apps.cc.importcontent.ContentImporter;
import com.opentext.apps.cc.importcontent.ImportConfiguration;
import com.opentext.apps.cc.importcontent.ImportUtils;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;


public class SubtypeImportHandler extends ContentImporter{
	
	protected ImportConfiguration importConfig;
	private Properties properties = new Properties();
	public int BatchSize = 50;
	protected final BusObjectManager busObjectManager;
	private static final String IMPORT_DOCUMENTS_FOLDER = "language.import.documents.location";
	private static final CordysLogger logger = CordysLogger.getCordysLogger(SubtypeImportHandler.class);
	public SubtypeImportHandler() {
		busObjectManager = ImportUtils.createBusObjectManager();
	}

	protected String getSheetName() {
		return IMPORT_SUBTYPES_SHEET_NAME;
	}

	protected String getPropertyNameForDocumentsFolder() {
		return IMPORT_DOCUMENTS_FOLDER;
	}

	@Override
	protected void importContent(ImportConfiguration configuration) {
		this.importConfig = configuration;
		configuration.setMappedInstances(new LinkedHashMap<String, String>());
		
		List<Map<String, String>> sheetData = super.readContent(getSheetName());
		Iterator<List<Map<String, String>>> batchIterator = new BatchIterator(sheetData, getMappingProperties(),
				BatchSize);
		while (batchIterator.hasNext()) {

			try {
				// Start transaction.
				busObjectManager.startTransaction();
				List<Map<String, String>> rows = batchIterator.next();
				if (rows.size() > 0) {
					int columnsLength = rows.get(0).size();
					createEntityInstances(columnsLength, rows);
				}
				busObjectManager.commitTransaction(true);
				busObjectManager.release();
			} catch (Exception exception) {
				busObjectManager.abortTransaction();
			}
		}
		
	}
		
		public void createEntityInstances(int noOfColumns, List<Map<String, String>> data) {
			Document document = NOMDocumentPool.getInstance().lendDocument();
			int rootElement = 0, subtypeImportResponse = 0;
			try {
				rootElement = document.createElement("CreateSubtypes");
				for (Map<String, String> map : data) {
					int contractNode = document.createElement("CreateSubtype");
					int index = 0;
					for (Map.Entry<String, String> entry : map.entrySet()) {
						if (index < noOfColumns) {
							if (entry.getValue() == null) {
								Node.createTextElement(entry.getKey().toString(), "", contractNode);
							} else {
								String key = entry.getKey().toString();
								String value = entry.getValue().toString();
								Node.createTextElement(key, value, contractNode);
							}
							index++;
						}
					}
		
					Node.appendToChildren(contractNode, rootElement);
				}
				
				SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3","SubtypeImport",null,null);
				importRequest.addParameterAsXml(rootElement);
				subtypeImportResponse = importRequest.sendAndWait();
			} catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.admin.SubtypeImportHandler", Severity.ERROR, e, "");
			} finally {
				Utilities.cleanAll(rootElement,subtypeImportResponse);
				NOMDocumentPool.getInstance().returnDocument(document);
			}
		
		}
		
		protected Properties getMappingProperties() {
			if (properties.isEmpty()) {
				Path path = getAbsolutePath(Paths.get(PROPERTIES_FILE_NAME));
				URI mappingFile = path.toUri();
				try {
					properties.load(mappingFile.toURL().openStream());
				} catch (IOException e) {
					logger._log("com.opentext.apps.cc.importhandler.admin.SubtypeImportHandler", Severity.ERROR, e, PROPERTIES_FILE_NAME + " file not found.");
					throw new ContractCenterApplicationException(ContractCenterAlertMessages.FILE_NOT_FOUND,PROPERTIES_FILE_NAME);
				}
			}
			return properties;
		}

}
