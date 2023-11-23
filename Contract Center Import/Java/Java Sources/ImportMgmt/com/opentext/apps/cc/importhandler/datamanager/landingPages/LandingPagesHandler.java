package com.opentext.apps.cc.importhandler.datamanager.landingPages;

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

public class LandingPagesHandler extends AbstractDataManagerImportHandler {

	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(LandingPagesHandler.class);
	private int skippedRecords = 0;
	private int failedRecords = 0;
	private int completedRecords = 0;

	public LandingPagesHandler() {
		this.metadata = new MetadataInitializer();
		this.validator = new ImportValidator();
	}

	@Override
	public String[] getSheetsName() {
		return new String[] { ImportConstants.GCLISTS_SHEET_NAME};//, ImportConstants.GCCHARTS_SHEET_NAME
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
		switch (sheetName) {
		case ImportConstants.GCLISTS_SHEET_NAME:
			return importGCLists(configuration, row, sheetName);
		default:
			break;
		}
		return null;
	}

	private ImportListener importGCLists(ImportConfiguration configuration, Map<String, String> row,
			String sheetName) {
		LandingPagesRecord record = null;
		if (Objects.nonNull(row) && !row.isEmpty()) {
			ReportItem reportItem = validator.validateLists(row, metadata);
			if (Objects.isNull(reportItem.getErrors()) || reportItem.getErrors().isEmpty()) {
				record = new LandingPagesRecord(metadata, reportItem, LandingPageViewType.GCLIST);
				record.doWork(new ImportEvent(record, row));
				updateRecordsCount(row.get(ImportConstants.STATUS));
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
