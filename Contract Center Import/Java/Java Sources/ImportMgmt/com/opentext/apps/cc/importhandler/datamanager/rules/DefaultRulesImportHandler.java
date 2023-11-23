package com.opentext.apps.cc.importhandler.datamanager.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import com.eibus.util.logger.CordysLogger;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.BatchIterator;
import com.opentext.apps.cc.importcontent.ContentImporter;
import com.opentext.apps.cc.importcontent.ImportConfiguration;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.datamanager.AbstractDataManagerImportHandler;

public class DefaultRulesImportHandler extends AbstractDataManagerImportHandler {

	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(DefaultRulesImportHandler.class);
	private int skippedRecords = 0;
	private int failedRecords = 0;
	private int completedRecords = 0;

	public DefaultRulesImportHandler() {
		metadata = new MetadataInitializer();
		validator = new ImportValidator();
	}

	@Override
	public String[] getSheetsName() {
		return new String[] { ImportConstants.RULESLIST_SHEET_NAME };
	}

	public String getZipFileName() {
		return ImportConstants.IMPORT_ZIP_NAME;
	}

	public String getMapingPropertiesFileName() {
		return ImportConstants.PROPERTIES_FILE_NAME;
	}

	public String getJobIdName() {
		return ImportConstants.IMPORT_JOBID_NAME;
	}

	@Override
	protected ImportListener processRow(ImportConfiguration configuration, Map<String, String> row, String sheetName) {

		switch (sheetName) {
		case ImportConstants.RULESLIST_SHEET_NAME:
			return importRules(configuration, row, sheetName);

		default:
		}
		return null;
	}

	public int[] invokeDefaultRulesImporthandler(ImportConfiguration configuration, Properties properties,
			ContentImporter mainImporter) {
		this.importConfig = configuration;
		int batchNumber;
		int ruleListNode = 0;
		int[] result = new int[3];
		List<Map<String, String>> sheetData = mainImporter.readContent(ImportConstants.RULESLIST_SHEET_NAME);
		ArrayList<Map<String, String>> rules = new ArrayList<>();
		String jobId = configuration.getJobId();
		String Code = null;
		Iterator<List<Map<String, String>>> batchIterator = new BatchIterator(sheetData, properties, sheetData.size());
		ArrayList<ImportListener> records = new ArrayList<>();
		batchNumber = 0;
		result[0] = sheetData.size();
		try {
			while (batchIterator.hasNext()) {
				batchNumber++;
				List<Map<String, String>> rows = batchIterator.next();
				for (Map<String, String> row : rows) {
					ImportListener record = processRow(configuration, row, ImportConstants.RULESLIST_SHEET_NAME);
					if (null != record) {
						records.add(record);
					}
				}
			}

			int batchCount = 0;
			while (batchCount < records.size()) {
				int endSize = (batchCount + BATCH_SIZE) > records.size() ? records.size() : batchCount + BATCH_SIZE;
				ruleListNode = getRulesNode(records.subList(batchCount, endSize), ImportConstants.RULESLIST_SHEET_NAME);
				batchCount = endSize;

			}
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
		result[1] = failedRecords;
		result[2] = ruleListNode;
		return result;
	}

	@Override
	public String getWorkBookName() {
		return ImportConstants.IMPORT_FILE_NAME + ImportConstants.EXCEL_EXTENSION;
	}

	private ImportListener importRules(ImportConfiguration configuration, Map<String, String> row, String sheetName) {
		DefaultRulesRecord record = null;
		if (Objects.nonNull(row) && !row.isEmpty()) {
			ReportItem reportItem = validator.validate(row, metadata);
			if (Objects.isNull(reportItem.getErrors()) || reportItem.getErrors().isEmpty()) {
				if (!Utilities.isStringEmpty(row.get(ImportConstants.TYPE))
						&& row.get(ImportConstants.TYPE).equalsIgnoreCase("Rule")) {
					record = new DefaultRulesRecord(metadata, reportItem);
					record.createRuleIfNotExists(new ImportEvent(record, row));
				}
				if (ImportConstants.STATUS_ERROR.equals(row.get(ImportConstants.STATUS))) {
					setImportStatus(false);
				}

			} else {
				setImportStatus(false);
				row.put(ImportConstants.STATUS, ImportConstants.NOT_COMPLETED);
				updateRecordsCount(row.get(ImportConstants.STATUS));
				StringBuilder str = new StringBuilder();
				for (String eStr : reportItem.getErrors().keySet()) {
					str.append(eStr).append(":").append(reportItem.getErrors().get(eStr)).append("  ");
				}
				row.put(ImportConstants.STATUS_LOG, str.toString());
			}
		}

		return record;
	}

	protected int getRulesNode(Collection<ImportListener> records, String sheetName) {
		int rulesRecordNode = 0, createOrUpdate = 0, inActiveList = 0;
		if (records.size() > 0) {
			rulesRecordNode = NomUtil.parseXML("<RulesList></RulesList>");
			createOrUpdate = NomUtil.parseXML("<CreateORUpdate></CreateORUpdate>");
			inActiveList = NomUtil.parseXML("<InActiveList></InActiveList>");
			for (ImportListener record : records) {
				Node.appendToChildren(record.getnode(), createOrUpdate);
			}
			Node.appendToChildren(createOrUpdate, rulesRecordNode);
			for (Map.Entry<String, String> inactiveRow : metadata.duplicateRules.entrySet()) {
				int ruleNode = 0;
				ruleNode = NomUtil.parseXML("<Rule></Rule>");
				Node.setDataElement(ruleNode, "Code", inactiveRow.getKey());
				Node.setDataElement(ruleNode, "RuleItemID", inactiveRow.getValue());
				Node.appendToChildren(ruleNode, inActiveList);
			}
			Node.appendToChildren(inActiveList, rulesRecordNode);
		}
		return rulesRecordNode;
	}

	private void updateRecordsCount(String status) {
		switch (status) {
		case ImportConstants.STATUS_ERROR:
			failedRecords++;
			break;
		case ImportConstants.RECORD_EXISTS:
			skippedRecords++;
			break;
		case ImportConstants.NOT_COMPLETED:
			failedRecords++;
			break;
		case ImportConstants.STATUS_SUCESS:
			completedRecords++;
			break;
		}
	}

	@Override
	protected void commit(Collection<ImportListener> records, String sheetName) {
		// TODO Auto-generated method stub

	}

}
