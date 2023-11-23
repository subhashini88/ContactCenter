package com.opentext.apps.cc.importhandler.datamanager.statesandactions;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportConfiguration;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.datamanager.AbstractDataManagerImportHandler;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;

public class StatesAndActionsHandler extends AbstractDataManagerImportHandler {

	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(StatesAndActionsHandler.class);
	private int skippedRecords = 0;
	private int failedRecords = 0;
	private int completedRecords = 0;

	public StatesAndActionsHandler() {
		this.metadata = new MetadataInitializer();
		this.validator = new ImportValidator();
	}

	@Override
	public String[] getSheetsName() {
		return new String[] { ImportConstants.PROCESS_SHEET_NAME, ImportConstants.RELATEDSTATES_SHEET_NAME,
				ImportConstants.RELATEDACTIONS_SHEET_NAME, ImportConstants.RELATEDPROPS_SHEET_NAME };
	}

	@Override
	public String getWorkBookName() {
		return ImportConstants.IMPORT_FILE_NAME + ImportConstants.EXCEL_EXTENSION;
	}

	@Override
	public String getZipFileName() {
		return ImportConstants.IMPORT_ZIP_NAME;
	}

	@Override
	public String getMapingPropertiesFileName() {
		return ImportConstants.PROPERTIES_FILE_NAME;
	}

	@Override
	public String getJobIdName() {
		return ImportConstants.IMPORT_JOBID_NAME;
	}

	@Override
	protected ImportListener processRow(ImportConfiguration configuration, Map<String, String> row, String sheetName) {
		ProcessItemType type = null;
		StatesAndActionsRecord record = null;
		switch (sheetName) {
		case ImportConstants.RELATEDACTIONS_SHEET_NAME:
			type = ProcessItemType.ACTION;
			break;
		case ImportConstants.RELATEDSTATES_SHEET_NAME:
			type = ProcessItemType.STATE;
			break;
		case ImportConstants.PROCESS_SHEET_NAME:
			type = ProcessItemType.PROCESS;
			break;
		case ImportConstants.RELATEDPROPS_SHEET_NAME:
			type = ProcessItemType.PROPERTIES;
			break;
		}
		if (Objects.nonNull(row) && !row.isEmpty()) {
			try {
				ReportItem reportItem = validator.validate(row, metadata, type);
				if (Objects.isNull(reportItem.getErrors()) || reportItem.getErrors().isEmpty()) {
					record = new StatesAndActionsRecord(metadata, reportItem, type);
					record.doWork(new ImportEvent(record, row));
					updateRecordsCount(row.get(ImportConstants.STATUS));
				} else {
					row.put(ImportConstants.STATUS, ImportConstants.NOT_COMPLETED);
					updateRecordsCount(row.get(ImportConstants.STATUS));
					StringBuilder str = new StringBuilder();
					for (String eStr : reportItem.getErrors().keySet()) {
						str.append(eStr).append(":").append(reportItem.getErrors().get(eStr)).append("  ");
					}
					row.put(ImportConstants.STATUS_LOG, str.toString());
				}
			} catch (Exception e) {
				row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
				row.put(ImportConstants.STATUS_LOG, e.getMessage());
			}
		}
		return record;
	}

	@Override
	protected void commit(Collection<ImportListener> records, String sheetName) {
		int updateNode = 0, batchRecordId = 0, response = 0;
		if (records.size() > 0) {
			SOAPRequestObject updateBatchRecords = new SOAPRequestObject(
					"http://schemas/OpenTextContractCenterImport/DataManager.ProcessingRecords.BatchRecord/operations",
					"UpdateBatchRecord", null, null);
			batchRecordId = NomUtil.parseXML("<BatchRecord-id></BatchRecord-id>");
			Node.setDataElement(batchRecordId, "ItemId2", getBatchRecordItemId());
			updateNode = NomUtil.parseXML("<BatchRecord-update></BatchRecord-update>");
			Node.setDataElement(updateNode, "TotalRecords", String.valueOf(getTotalRecords()));
			Node.setDataElement(updateNode, "CompletedRecords", String.valueOf(completedRecords));
			Node.setDataElement(updateNode, "FailedRecords", String.valueOf(failedRecords));
			Node.setDataElement(updateNode, "SkippedRecords", String.valueOf(skippedRecords));

			if (failedRecords > 0 || (skippedRecords + completedRecords != getTotalRecords()))
				Node.setDataElement(updateNode, "ImportStatus", ImportConstants.STATUS_FAILED);
			else
				Node.setDataElement(updateNode, "ImportStatus", ImportConstants.STATUS_COMPLETED);

			updateBatchRecords.addParameterAsXml(batchRecordId);
			updateBatchRecords.addParameterAsXml(updateNode);
			try {
				response = updateBatchRecords.execute();
			} catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.datamanager.statesandactions.StatesAndActionsHandler",
						Severity.ERROR, e, "Error while executing states and actions data import.");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,
						"states and actions data import");
			} finally {
				Utilities.cleanAll(updateNode, batchRecordId, response);
			}
		}
		completedRecords = 0;
		failedRecords = 0;
		skippedRecords = 0;
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
		default:
		}
	}
}
