package com.opentext.apps.cc.importhandler.rules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.AbstractImportHandler;
import com.opentext.apps.cc.importcontent.BatchIterator;
import com.opentext.apps.cc.importcontent.ContentManagementRuntimeException;
import com.opentext.apps.cc.importcontent.FileUtil;
import com.opentext.apps.cc.importcontent.ImportConfiguration;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.ReportListener;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;

public class RuleImportHandler extends AbstractImportHandler {

	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(RuleImportHandler.class);

	public RuleImportHandler() {
		metadata = new MetadataInitializer();
		validator = new ImportValidator();
	}

	@Override
	protected String getSheetName() {
		return ImportConstants.SHEET_NAME;
	}

	ReportItem reportItemset = new ReportItem();
	ReportItem reportItem = new ReportItem();

	@Override
	protected ImportListener processRow(ImportConfiguration configuration, Map<String, String> row) {
		ImportListener record = null;
		if (row != null) {
			reportItem = validator.validate(row, metadata, importConfig.getJobId());
			if (reportItem.getErrors().isEmpty()) {
				if (!Utilities.isStringEmpty(row.get(ImportConstants.TYPE))
						&& row.get(ImportConstants.TYPE).equalsIgnoreCase("Rule")) {
					record = new RuleRecord(metadata, reportItem);
				} else if (!Utilities.isStringEmpty(row.get(ImportConstants.TYPE))
						&& row.get(ImportConstants.TYPE).equalsIgnoreCase("Condition")) {
					record = new RuleConditionRecord(metadata, reportItem);
				}
				if (Objects.nonNull(record)) {
					record.doWork(new ImportEvent(record, row));
				}
			} else {
				ErrorRecord errorRecord = new ErrorRecord();
				errorRecord.setType(row.get(ImportConstants.TYPE));
				errorRecord.setLogs(reportItem.getErrors().toString());
				record = errorRecord;
			}
		}
		return record;
	}

	@Override
	protected ReportListener createReportListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void commit(Collection<ImportListener> records) {
		int organizationRecordNode = 0, jobIdNode = 0, organizationImportResponse = 0;
		if (records.size() > 0) {
			organizationRecordNode = NomUtil.parseXML("<Rules></Rules>");
			for (ImportListener record : records) {
				Node.appendToChildren(record.getnode(), organizationRecordNode);
			}
			jobIdNode = NomUtil.parseXML("<JobID>" + super.importConfig.getJobId() + "</JobID>");
			SOAPRequestObject importRequest = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/contractcenterimport/16.3", "RulesImport", null, null);
			importRequest.addParameterAsXml(jobIdNode);
			importRequest.addParameterAsXml(organizationRecordNode);
			try {
				organizationImportResponse = importRequest.execute();
			} catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.rules.RuleImportHandler", Severity.ERROR, e,
						"Error while executing RuleImport");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,
						"RuleImport");
			} finally {
				Utilities.cleanAll(organizationRecordNode, organizationImportResponse, jobIdNode);
			}
		}
	}

	private void updateImportLogs(String organizationCode, String jobID, String errors) {
		int organizationCodeNode = 0, statusItemIdResponse = 0, updateImportStatusNode = 0, updateDataStatusNode = 0,
				updateImportStatusResponse = 0, jobIDNode = 0;
		try {
			if (!Utilities.isStringEmpty(organizationCode)) {
				SOAPRequestObject statusItemIdRequest = new SOAPRequestObject(
						"http://schemas/OpenTextContractCenterImport/RulesImportStatus/operations",
						"RulesImportStatusFilter", null, null);
				organizationCodeNode = NomUtil.parseXML("<LegacyID>" + organizationCode + "</LegacyID>");
				jobIDNode = NomUtil.parseXML("<JobID>" + jobID + "</JobID>");
				statusItemIdRequest.addParameterAsXml(organizationCodeNode);
				statusItemIdRequest.addParameterAsXml(jobIDNode);
				statusItemIdResponse = statusItemIdRequest.sendAndWait();
				String itemId = Node.getDataWithDefault(
						NomUtil.getNode(".//RulesImportStatus-id/ItemId", statusItemIdResponse), null);
				if (Objects.nonNull(itemId)) {
					SOAPRequestObject updateImportJobRequest = new SOAPRequestObject(
							"http://schemas/OpenTextContractCenterImport/RulesImportStatus/operations",
							"UpdateRulesImportStatus", null, null);
					updateImportStatusNode = NomUtil.parseXML("<RulesImportStatus-id></RulesImportStatus-id>");
					Node.setDataElement(updateImportStatusNode, "ItemId", itemId);
					updateDataStatusNode = NomUtil.parseXML("<RulesImportStatus-update></RulesImportStatus-update>");
					Node.setDataElement(updateDataStatusNode, "LogDetails", errors);
					Node.setDataElement(updateDataStatusNode, "ImportStatus", "InvalidData");
					updateImportJobRequest.addParameterAsXml(updateDataStatusNode);
					updateImportJobRequest.addParameterAsXml(updateImportStatusNode);
					updateImportStatusResponse = updateImportJobRequest.sendAndWait();
				}
			}
		} finally {
			Utilities.cleanAll(organizationCodeNode, updateImportStatusNode, updateDataStatusNode, statusItemIdResponse,
					updateImportStatusResponse, jobIDNode);
		}
	}

	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData, String jobId) {

		boolean status = true;
		String jobStatus = "Reading ZIPs completed";
		int count = 0, createNode = 0, importNode = 0, child = 0, importStatusNode = 0, OrganizationNode = 0,
				createImportStatusResponse = 0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);// entity web service to read job details and update count value
		try {
			for (; count < sheetData.size(); count++) {
				row = sheetData.get(count);
				if (row.get(ImportConstants.LEGACY_ID) != null && row.get(ImportConstants.TYPE) != null
						&& row.get(ImportConstants.TYPE).equalsIgnoreCase("Rule")) {
//					 && row.get(getKey(ImportConstants.TYPE))!=null 
					SOAPRequestObject createRequest = new SOAPRequestObject(
							"http://schemas/OpenTextContractCenterImport/RulesImportStatus/operations",
							"CreateRulesImportStatus", null, null);
					createNode = NomUtil.parseXML(
							"<RulesImportStatus-create xmlns=\"http://schemas/OpenTextContractCenterImport/RulesImportStatus\"></RulesImportStatus-create>");
					importNode = NomUtil.parseXML("<RelatedJobID></RelatedJobID>");
					child = Node.createElementNS("DataImport-id", "", "",
							"http://schemas/OpenTextContractCenterImport/DataImport", importNode);
					Node.appendToChildren(NomUtil.parseXML("<Id>" + jobId + "</Id>"), child);
					Node.appendToChildren(importNode, createNode);
					OrganizationNode = NomUtil
							.parseXML("<LegacyID>" + row.get(getKey(ImportConstants.LEGACY_ID)) + "</LegacyID>");
					Node.appendToChildren(OrganizationNode, createNode);
					importStatusNode = NomUtil.parseXML("<ImportStatus>NotStarted</ImportStatus>");
					Node.appendToChildren(importStatusNode, createNode);
					createRequest.addParameterAsXml(createNode);
					createImportStatusResponse = createRequest.sendAndWait();
				}
			}
		} catch (Exception e) {
			status = false;
			jobStatus = "Reading ZIPs failed";
			updateImportJob(jobId, jobStatus, count);// update job with count and status "Dump failed"
			logger._log("com.opentext.apps.cc.importhandler.rules.RuleImportHandler", Severity.ERROR, e,
					"Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		} finally {
			Utilities.cleanAll(createNode, importNode, child, importStatusNode, OrganizationNode,
					createImportStatusResponse);
		}
		updateImportJob(jobId, jobStatus, count);// update job with count and status "Dump success"
		return status;
	}

	protected void generateErrorReport(String jobId, Path path, ImportConfiguration importparm) {
		int response = 0, nodes[] = null;
		boolean hasError = false;
		try {
			List<List<Map<String, String>>> rowData = super.getSheetDataSet(path, this.getSheetName(),
					ImportConstants.LEGACY_ID, "Rule", "Condition");
			if (null == rowData || rowData.size() == 0)
				return;
			SOAPRequestObject importRequest = new SOAPRequestObject(
					"http://schemas/OpenTextContractCenterImport/RulesImportStatus/operations",
					"ReadRulesImportStatusbyJobId", null, null);
			int input = NomUtil.parseXML("<JobId>" + jobId + "</JobId>");
			importRequest.addParameterAsXml(input);
			response = importRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RulesImportStatus", response);
			List<Map<String, String>> errorRecords = new LinkedList<Map<String, String>>();

			for (List<Map<String, String>> eachSet : rowData) {
				String LegacyID = null, importStatus = "InvalidData", logDetails = null;
				hasError = false;

				if (!Utilities.isStringEmpty(eachSet.get(0).get("LegacyID"))) {

					LegacyID = Node.getDataWithDefault(NomUtil.getNode(
							".//RulesImportStatus[LegacyID=" + eachSet.get(0).get("LegacyID") + "]", response), null);
					Map<String, String> row = eachSet.get(0);
					if (LegacyID != null) {
						hasError = true;
						importStatus = Node.getDataWithDefault(NomUtil.getNode(
								".//RulesImportStatus[LegacyID=" + eachSet.get(0).get("LegacyID") + "]//ImportStatus",
								response), null);
						logDetails = Node.getDataWithDefault(NomUtil.getNode(
								".//RulesImportStatus[LegacyID=" + eachSet.get(0).get("LegacyID") + "]//LogDetails",
								response), null);
					} else if (Utilities.isStringEmpty(eachSet.get(0).get("Type"))) {
						hasError = true;
						logDetails = "Errors : {Type=Type cannot be empty}";
					} else if (!Utilities.isStringEmpty(eachSet.get(0).get("Type"))
							&& !eachSet.get(0).get("Type").equalsIgnoreCase("Rule")) {
						hasError = true;
						logDetails = "Errors : {Type=Type is incorrect}";
					}
				} else {
					if (!Utilities.isStringEmpty(eachSet.get(0).get("Type"))) {
						if (eachSet.get(0).get("Type").equalsIgnoreCase("Rule")) {
							hasError = true;
							logDetails = "Errors : {LegacyID=Legacy ID cannot be empty}";
						}
					} else {
						hasError = true;
						importStatus = "InvalidData";
						logDetails = "Errors : {LegacyID=Legacy ID cannot be empty; Type=Type cannot be empty;}";
					}
				}
				if (hasError) {
					Map<String, String> row = eachSet.get(0);
					row.put("ImportStatus", importStatus);
					row.put("Error", logDetails);
					errorRecords.add(row);

					for (int i = 1; i < eachSet.size(); i++) {
						errorRecords.add(eachSet.get(i));
					}
				}
			}
			super.createErrorFile(errorRecords, FileUtil.getDownloadReadPath() + jobId + File.separatorChar
					+ this.getSheetName() + "_Error_Report.xlsx", this.getSheetName(), importparm);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ContentManagementRuntimeException(e);
		} finally {
			if (nodes != null)
				Utilities.cleanAll(nodes);
			Utilities.cleanAll(response);
		}
	}

	@Override
	protected void importContent(ImportConfiguration configuration) {
		this.importConfig = configuration;
		Properties properties = getMappingProperties();
		configuration.setMappedInstances(new LinkedHashMap<String, String>());
		List<Map<String, String>> sheetData = super.readContent(getSheetName());
		TreeMap<String, ImportListener> conditions = new TreeMap<>();
		String jobId = configuration.getJobId();
		if (!configuration.isRetrigger()) {
			if (!statusDump(sheetData, jobId)) {
				logger._log("com.opentext.apps.cc.custom.AbstractImportHandler", Severity.ERROR, null,
						"Status dump is failed.");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.STATUS_DUMP_FAILED);
			}
		}

		Iterator<List<Map<String, String>>> batchIterator = new BatchIterator(sheetData, properties, sheetData.size());
		ArrayList<ImportListener> records = new ArrayList<>();
		try {
			while (batchIterator.hasNext()) {
				List<Map<String, String>> rows = batchIterator.next();
				RuleRecord currentRecord = null;
				boolean flag = true;
				String legacyId = null;
				for (Map<String, String> row : rows) {
					if ((Objects.nonNull(row.get(ImportConstants.TYPE))
							&& row.get(ImportConstants.TYPE).equalsIgnoreCase("Rule"))
							|| (Objects.isNull(row.get(ImportConstants.TYPE))
									&& Objects.nonNull(row.get(ImportConstants.LEGACY_ID)))
							|| (Objects.nonNull(row.get(ImportConstants.LEGACY_ID))
									&& (!row.get(ImportConstants.TYPE).equalsIgnoreCase("Condition")))) {
						if (!flag) {
							if (Objects.nonNull(currentRecord)) {
								if (!currentRecord.isValidRuleLogic(conditions)) {
									records.remove(currentRecord);
									reportItemset.error(ImportConstants.RULE_LOGIC,
											"The rule logic you entered is incorrect");
								} else {
									for (ImportListener con : conditions.values()) {
										currentRecord.addConditionNode(con);
									}
								}
							}
							if (!reportItemset.getErrors().isEmpty()) {
								updateImportLogs(legacyId, importConfig.getJobId(), reportItemset.toString());
							}
						}
						flag = false;
						reportItemset = new ReportItem();
						conditions.clear();
					}
					ImportListener record = processRow(configuration, row);
					if (null != record) {
						if (record instanceof RuleRecord) {
							legacyId = row.get(ImportConstants.LEGACY_ID);
							records.add(record);
							currentRecord = (RuleRecord) record;
						} else if (record instanceof RuleConditionRecord) {
							if (Objects.nonNull(currentRecord)) {
								conditions.put(row.get(ImportConstants.ORDER), record);
							}
						} else {
							ErrorRecord errorRecord = (ErrorRecord) record;
							if ("Condition".equalsIgnoreCase(errorRecord.getType()) && Objects.nonNull(currentRecord)) {
								records.remove(currentRecord);
							} else if ("Rule".equalsIgnoreCase(errorRecord.getType())) {
								legacyId = row.get(ImportConstants.LEGACY_ID);
							} else {
								if (!Utilities.isStringEmpty(row.get(ImportConstants.LEGACY_ID))) {
									legacyId = row.get(ImportConstants.LEGACY_ID);
								} else {
									records.remove(currentRecord);
								}
							}
							currentRecord = null;
							reportItemset = reportItem;
						}
					}
				}
				// last rule
				if (Objects.nonNull(currentRecord)) {
					if (!currentRecord.isValidRuleLogic(conditions)) {
						records.remove(currentRecord);
						reportItemset.error(ImportConstants.RULE_LOGIC, "The rule logic you entered is incorrect");
					} else {
						for (ImportListener con : conditions.values()) {
							currentRecord.addConditionNode(con);
						}
					}
				}
				if (!reportItemset.getErrors().isEmpty()) {
					updateImportLogs(legacyId, importConfig.getJobId(), reportItemset.toString());
				}

			}

			int batchCount = 0;
			while (batchCount < records.size()) {
				int endSize = (batchCount + BatchSize) > records.size() ? records.size() : batchCount + BatchSize;
				commit(records.subList(batchCount, endSize));
				batchCount = endSize;
			}
			batchuploadstatus = "Completed";
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}

	}

}
