package com.opentext.apps.cc.importhandler.datamanager.masterdata;

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

public class MasterDataImportHandler extends AbstractDataManagerImportHandler {

	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(MasterDataImportHandler.class);

	public MasterDataImportHandler() {
		this.metadata = new MetadataInitializer();
		this.validator = new ImportValidator();
	}

	@Override
	public String[] getSheetsName() {

		return new String[] { ImportConstants.LINKTYPES_SHEET_NAME, ImportConstants.RELATIONTYPES_SHEET_NAME , ImportConstants.CURRENCIES_SHEET_NAME , ImportConstants.UOM_SHEET_NAME , ImportConstants.REGIONSCOUNTRIES_SHEET_NAME};
	}
	
	public  String getZipFileName() {
		return ImportConstants.IMPORT_FILE_NAME;
	}
	public  String getMapingPropertiesFileName() {
		return ImportConstants.PROPERTIES_FILE_NAME;
	}
	
	public String getJobIdName() {
		return ImportConstants.IMPORT_JOBID_NAME;
	}

	@Override
	protected ImportListener processRow(ImportConfiguration configuration, Map<String, String> row, String sheetName) {

		switch (sheetName) {
		case ImportConstants.LINKTYPES_SHEET_NAME:
			return importLinkTypes(configuration, row, sheetName);
		case ImportConstants.RELATIONTYPES_SHEET_NAME:
			return importRelationTypes(configuration, row, sheetName);
		case ImportConstants.CURRENCIES_SHEET_NAME:
			return importCurrencies(configuration, row, sheetName);
		case ImportConstants.UOM_SHEET_NAME:
			return importUOM(configuration, row, sheetName);
		case ImportConstants.REGIONSCOUNTRIES_SHEET_NAME:
			return importRegions(configuration, row, sheetName);
		default:
		}
		return null;
	}

	private ImportListener importLinkTypes(ImportConfiguration configuration, Map<String, String> row,
			String sheetName) {
		MasterDataRecord record = null;
		if (Objects.nonNull(row) && !row.isEmpty()) {
			ReportItem reportItem = validator.validateLinkTypes(row, metadata);
			if (Objects.isNull(reportItem.getErrors()) || reportItem.getErrors().isEmpty()) {
				record = new MasterDataRecord(metadata, reportItem);
				record.frameLinkTypeRecord(new ImportEvent(record, row));
				if (ImportConstants.STATUS_ERROR.equals(row.get(ImportConstants.STATUS))) {
					setImportStatus(false);
				}

			} else {
				setImportStatus(false);
				row.put(ImportConstants.STATUS, ImportConstants.NOT_COMPLETED);
				StringBuilder str = new StringBuilder();
				for (String eStr : reportItem.getErrors().keySet()) {
					str.append(eStr).append(":").append(reportItem.getErrors().get(eStr)).append("  ");
				}
				row.put(ImportConstants.STATUS_LOG, str.toString());
			}
		}

		return record;
	}

	private ImportListener importRelationTypes(ImportConfiguration configuration, Map<String, String> row,
			String sheetName) {
		MasterDataRecord record = null;
		if (Objects.nonNull(row) && !row.isEmpty()) {
			ReportItem reportItem = validator.validateRelationTypes(row, metadata);
			if (Objects.isNull(reportItem.getErrors()) || reportItem.getErrors().isEmpty()) {
				record = new MasterDataRecord(metadata, reportItem);
				record.frameRelationTypeRecord(new ImportEvent(record, row));
				if (ImportConstants.STATUS_ERROR.equals(row.get(ImportConstants.STATUS))) {
					setImportStatus(false);
				}

			} else {
				setImportStatus(false);
				row.put(ImportConstants.STATUS, ImportConstants.NOT_COMPLETED);
				StringBuilder str = new StringBuilder();
				for (String eStr : reportItem.getErrors().keySet()) {
					str.append(eStr).append(":").append(reportItem.getErrors().get(eStr)).append("  ");
				}
				row.put(ImportConstants.STATUS_LOG, str.toString());
			}
		}
		return record;
	}
	
	private ImportListener importCurrencies(ImportConfiguration configuration, Map<String, String> row,
			String sheetName) {
		MasterDataRecord record = null;
		if (Objects.nonNull(row) && !row.isEmpty()) {
			ReportItem reportItem = validator.validateCurrencies(row, metadata);
			if (Objects.isNull(reportItem.getErrors()) || reportItem.getErrors().isEmpty()) {
				record = new MasterDataRecord(metadata, reportItem);
				record.frameCurrencyTypeRecord(new ImportEvent(record, row));
				if (ImportConstants.STATUS_ERROR.equals(row.get(ImportConstants.STATUS))) {
					setImportStatus(false);
				}

			} else {
				setImportStatus(false);
				row.put(ImportConstants.STATUS, ImportConstants.NOT_COMPLETED);
				StringBuilder str = new StringBuilder();
				for (String eStr : reportItem.getErrors().keySet()) {
					str.append(eStr).append(":").append(reportItem.getErrors().get(eStr)).append("  ");
				}
				row.put(ImportConstants.STATUS_LOG, str.toString());
			}
		}
		return record;
	}
	
	private ImportListener importUOM(ImportConfiguration configuration, Map<String, String> row,
			String sheetName) {
		MasterDataRecord record = null;
		if (Objects.nonNull(row) && !row.isEmpty()) {
			ReportItem reportItem = validator.validateUOM(row, metadata);
			if (Objects.isNull(reportItem.getErrors()) || reportItem.getErrors().isEmpty()) {
				record = new MasterDataRecord(metadata, reportItem);
				record.frameUOMTypeRecord(new ImportEvent(record, row));
				if (ImportConstants.STATUS_ERROR.equals(row.get(ImportConstants.STATUS))) {
					setImportStatus(false);
				}

			} else {
				setImportStatus(false);
				row.put(ImportConstants.STATUS, ImportConstants.NOT_COMPLETED);
				StringBuilder str = new StringBuilder();
				for (String eStr : reportItem.getErrors().keySet()) {
					str.append(eStr).append(":").append(reportItem.getErrors().get(eStr)).append("  ");
				}
				row.put(ImportConstants.STATUS_LOG, str.toString());
			}
		}
		return record;
	}
	
	private ImportListener importRegions(ImportConfiguration configuration, Map<String, String> row,
			String sheetName) {
		
		MasterDataRecord record = null;
		if (Objects.nonNull(row) && !row.isEmpty()) {
			ReportItem reportItem = validator.validateRegions(row, metadata);
			if (Objects.isNull(reportItem.getErrors()) || reportItem.getErrors().isEmpty()) {
				record = new MasterDataRecord(metadata, reportItem);
				
				if(Objects.isNull(record.frameRegionRecord(new ImportEvent(record, row)))) {
					record = null;
				}

			} else {
				setImportStatus(false);
				row.put(ImportConstants.STATUS, ImportConstants.NOT_COMPLETED);
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
		int entityNode = 0, processingRecordId = 0, masterDataImportResponse = 0;
		String parentXML = null;

		if (records.size() > 0) {
			switch(sheetName) {
			case ImportConstants.LINKTYPES_SHEET_NAME:
				parentXML = "LinkTypes";
				break;
			case ImportConstants.RELATIONTYPES_SHEET_NAME:
				parentXML = "GCRelationTypes";
				break;
			case ImportConstants.CURRENCIES_SHEET_NAME:
				parentXML = "Currencies";
				break;
			case ImportConstants.UOM_SHEET_NAME:
				parentXML = "UnitsofMeasurement";
				break;
			case ImportConstants.REGIONSCOUNTRIES_SHEET_NAME:
				parentXML = "Regions";
				break;
			default:
			}
			entityNode = NomUtil.parseXML("<" + parentXML + "></" + parentXML + ">");
			for (ImportListener record : records) {
				Node.appendToChildren(record.getnode(), entityNode);
			}
			
			processingRecordId = NomUtil.parseXML("<ProcessingRecordId></ProcessingRecordId>");
			Node.setDataElement(processingRecordId, "", getBatchRecordItemId());
			
			Node.appendToChildren(processingRecordId, entityNode);
			SOAPRequestObject importRequest = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/contractcenterimport/16.3", parentXML + "Import", null, null);
			importRequest.addParameterAsXml(entityNode);
			try {
				masterDataImportResponse = importRequest.execute();
			} catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.datamanager.masterdata.MasterDataImportHandler", Severity.ERROR, e, "Error while executing master data import.");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,"master data import");
			} finally {
				Utilities.cleanAll(entityNode, processingRecordId, masterDataImportResponse);
			}
		}

	}

	@Override
	public String getWorkBookName() {
		return ImportConstants.IMPORT_FILE_NAME + ImportConstants.EXCEL_EXTENSION;
	}
}
