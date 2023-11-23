package com.opentext.apps.cc.importhandler.tasklist;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
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
import com.opentext.apps.cc.importhandler.tasklist.ImportConstants.RecordType;
import com.opentext.apps.cc.importhandler.tasklist.ImportConstants.TypeOfActivity;

public class TaskListImportHandler extends AbstractImportHandler {

	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(TaskListImportHandler.class);

	public TaskListImportHandler() {
		metadata = new MetadataInitializer();
		validator = new ImportValidator();
	}

	@Override
	protected String getSheetName() {
		return ImportConstants.SHEET_NAME;
	}

	@Override
	protected ReportListener createReportListener() {
		return null;
	}

	@Override
	protected void commit(Collection<ImportListener> records) {
		int tasklistsRecordNode = 0, jobIdNode = 0, tasklistImportResponse = 0, forEachNode = 0;
		if (records.size() > 0) {
			tasklistsRecordNode = NomUtil.parseXML("<ActivityLists></ActivityLists>");
			for (ImportListener record : records) {
				forEachNode = NomUtil.parseXML("<ActivityListsInt></ActivityListsInt>");
				Node.appendToChildren(forEachNode, tasklistsRecordNode);
				Node.appendToChildren(record.getnode(), forEachNode);
			}
			jobIdNode = NomUtil.parseXML("<JobID>" + super.importConfig.getJobId() + "</JobID>");
			SOAPRequestObject importRequest = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/contractcenterimport/16.3", "ActivityListsImport", null, null);
			importRequest.addParameterAsXml(jobIdNode);
			importRequest.addParameterAsXml(tasklistsRecordNode);
			try {
				tasklistImportResponse = importRequest.execute();
			} catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.tasklist.TaskListImportHandler", Severity.ERROR, e,
						"Error while executing ActivityListsImport");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,
						"ActivityListsImport");
			} finally {
				Utilities.cleanAll(tasklistsRecordNode, tasklistImportResponse, jobIdNode);
			}
		}
	}

	@Override
	protected void importContent(ImportConfiguration configuration) {
		this.importConfig = configuration;
		Properties properties = getMappingProperties();
		configuration.setMappedInstances(new LinkedHashMap<String, String>());
		List<Map<String, String>> sheetData = super.readContent(getSheetName());
		ArrayList<Map<String, String>> tasks = new ArrayList<>();
		String jobId = configuration.getJobId();
		String taskListLegacyID = null;

		if (!configuration.isRetrigger()) {
			if (!statusDump(sheetData, jobId)) {
				logger._log("com.opentext.apps.cc.custom.AbstractImportHandler", Severity.ERROR, null,
						"Status dump is failed.");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.STATUS_DUMP_FAILED);
			}
		}

		Iterator<List<Map<String, String>>> batchIterator = new BatchIterator(sheetData, properties, sheetData.size());
		ArrayList<ImportListener> records = new ArrayList<>();
		TreeMap<Integer, ImportListener> orderedTasks = new TreeMap<>();
		boolean newTaskList = true;
		try {
			while (batchIterator.hasNext()) {
				List<Map<String, String>> rows = batchIterator.next();
				TaskListRecord currentTaskListRecord = null;

				for (Map<String, String> row : rows) {

					if ((Objects.nonNull(row.get(ImportConstants.TASKLIST_LEGACYID)) && (Objects
							.isNull(row.get(ImportConstants.RECORD_TYPE))
							|| !row.get(ImportConstants.RECORD_TYPE).equalsIgnoreCase(RecordType.TASK.getValue())))
							|| (Objects.nonNull(row.get(ImportConstants.RECORD_TYPE))
									&& row.get(ImportConstants.RECORD_TYPE)
											.equalsIgnoreCase(RecordType.TASKLIST.getValue()))) {

						if (newTaskList == false) {
							if (Objects.nonNull(currentTaskListRecord)) {
								validateTasks(tasks, currentTaskListRecord, orderedTasks, records);
							}
							if (!reportItemSet.getErrors().isEmpty()) {
								updateImportLogs(taskListLegacyID, importConfig.getJobId(), reportItemSet.toString());
							}
						}
						reportItemSet = new ReportItem();
						newTaskList = false;
					}
					ImportListener record = processRow(configuration, row);
					if (null != record) {
						if (record instanceof TaskListRecord) {
							records.add(record);
							currentTaskListRecord = (TaskListRecord) record;
							taskListLegacyID = row.get(ImportConstants.TASKLIST_LEGACYID);
							orderedTasks.clear();
							tasks.clear();
						} else if (record instanceof TaskRecord) {
							if (Objects.nonNull(currentTaskListRecord)) {
								tasks.add(row);
								orderedTasks.put(Integer.parseInt(row.get(ImportConstants.TASK_ORDER)), record);
							}
						} else {
							boolean flag = false;
							ErrorRecord errorRecord = (ErrorRecord) record;
							if (Objects.nonNull(errorRecord.getType())) {
								if (!RecordType.TASKLIST.getValue().equalsIgnoreCase(errorRecord.getType())) {
									flag = true;
								} else {
									taskListLegacyID = row.get(ImportConstants.TASKLIST_LEGACYID);
								}
							} else {
								flag = true;
							}
							if (flag) {
								if (Objects.nonNull(row.get(ImportConstants.TASKLIST_LEGACYID))) {
									taskListLegacyID = row.get(ImportConstants.TASKLIST_LEGACYID);
								} else {
									if (Objects.nonNull(currentTaskListRecord)) {
										records.remove(currentTaskListRecord);
									}
								}
							}
							currentTaskListRecord = null;
							reportItemSet = reportItem;
						}

					}

				}
				if (!batchIterator.hasNext()) {
					if (Objects.nonNull(currentTaskListRecord)) {
						validateTasks(tasks, currentTaskListRecord, orderedTasks, records);
					}
					if (!reportItemSet.getErrors().isEmpty()) {
						updateImportLogs(taskListLegacyID, importConfig.getJobId(), reportItemSet.toString());
					}
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

	private void updateImportLogs(String LegacyID, String jobID, String errors) {
		int taskListLegacyIdNode = 0, statusItemIdResponse = 0, updateImportStatusNode = 0, updateDataStatusNode = 0,
				updateImportStatusResponse = 0, jobIDNode = 0;
		try {
			if (!Utilities.isStringEmpty(LegacyID)) {
				SOAPRequestObject statusItemIdRequest = new SOAPRequestObject(
						"http://schemas/OpenTextContractCenterImport/ActivityListsImportStatus/operations",
						"ActivityListsImportStatusFilter", null, null);
				taskListLegacyIdNode = NomUtil.parseXML("<LegacyID>" + LegacyID + "</LegacyID>");
				jobIDNode = NomUtil.parseXML("<JobID>" + jobID + "</JobID>");
				statusItemIdRequest.addParameterAsXml(taskListLegacyIdNode);
				statusItemIdRequest.addParameterAsXml(jobIDNode);
				statusItemIdResponse = statusItemIdRequest.sendAndWait();
				String itemId = Node.getDataWithDefault(
						NomUtil.getNode(".//ActivityListsImportStatus-id/ItemId", statusItemIdResponse), null);

				if (Objects.nonNull(itemId)) { // changed
					SOAPRequestObject updateImportJobRequest = new SOAPRequestObject(
							"http://schemas/OpenTextContractCenterImport/ActivityListsImportStatus/operations",
							"UpdateActivityListsImportStatus", null, null);
					updateImportStatusNode = NomUtil
							.parseXML("<ActivityListsImportStatus-id></ActivityListsImportStatus-id>");
					Node.setDataElement(updateImportStatusNode, "ItemId", itemId);
					updateDataStatusNode = NomUtil
							.parseXML("<ActivityListsImportStatus-update></ActivityListsImportStatus-update>");
					Node.setDataElement(updateDataStatusNode, "LogDetails", errors);
					Node.setDataElement(updateDataStatusNode, "ImportStatus", "InvalidData");
					updateImportJobRequest.addParameterAsXml(updateDataStatusNode);
					updateImportJobRequest.addParameterAsXml(updateImportStatusNode);
					updateImportStatusResponse = updateImportJobRequest.sendAndWait();
				}
			}
		} finally {
			Utilities.cleanAll(taskListLegacyIdNode, updateImportStatusNode, updateDataStatusNode, statusItemIdResponse,
					updateImportStatusResponse, jobIDNode);
		}
	}

	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData, String jobId) {

		boolean status = true;
		String jobStatus = "Reading ZIPs completed";
		int count = 0, createNode = 0, importNode = 0, child = 0, importStatusNode = 0, taskListLegacyIdNode = 0,
				createImportStatusResponse = 0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);// entity web service to read job details and update count value
		try {
			for (; count < sheetData.size(); count++) {
				row = sheetData.get(count);

				boolean possibleTasklist = row.get(ImportConstants.RECORD_TYPE) != null
						&& row.get(ImportConstants.RECORD_TYPE).equalsIgnoreCase(RecordType.TASKLIST.getValue());

				if (row.get(getKey(ImportConstants.TASKLIST_LEGACYID)) != null && possibleTasklist) {
					SOAPRequestObject createRequest = new SOAPRequestObject(
							"http://schemas/OpenTextContractCenterImport/ActivityListsImportStatus/operations",
							"CreateActivityListsImportStatus", null, null);
					createNode = NomUtil.parseXML(
							"<ActivityListsImportStatus-create xmlns=\"http://schemas/OpenTextContractCenterImport/ActivityListsImportStatus\"></ActivityListsImportStatus-create>");
					importNode = NomUtil.parseXML("<RelatedJobID></RelatedJobID>");
					child = Node.createElementNS("DataImport-id", "", "",
							"http://schemas/OpenTextContractCenterImport/DataImport", importNode);
					Node.appendToChildren(NomUtil.parseXML("<Id>" + jobId + "</Id>"), child);
					Node.appendToChildren(importNode, createNode);
					taskListLegacyIdNode = NomUtil.parseXML(
							"<LegacyID>" + row.get(getKey(ImportConstants.TASKLIST_LEGACYID)) + "</LegacyID>");
					Node.appendToChildren(taskListLegacyIdNode, createNode);
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
			logger._log("com.opentext.apps.cc.importhandler.tasklist.TaskListImportHandler", Severity.ERROR, e,
					"Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		} finally {
			Utilities.cleanAll(createNode, importNode, child, importStatusNode, taskListLegacyIdNode,
					createImportStatusResponse);
		}
		updateImportJob(jobId, jobStatus, count);// update job with count and status "Dump success"
		return status;
	}

	public void validateTasks(ArrayList<Map<String, String>> tasksRowsMap, TaskListRecord currentTaskListRecord,
			TreeMap<Integer, ImportListener> orderedTasks, ArrayList<ImportListener> records) {
		int stCount = 0; // stateTransition tasks count
		int stPos = -1; // stateTransition task position
		int tasksCount = tasksRowsMap.size();
		Set<Integer> orderSet = new HashSet<Integer>();
		int order;
		for (int i = 0; i < tasksCount; i++) {
			order = Integer.parseInt(tasksRowsMap.get(i).get(ImportConstants.TASK_ORDER));
			orderSet.add(order);
			if (tasksRowsMap.get(i).get(ImportConstants.TASK_TYPE)
					.equalsIgnoreCase(TypeOfActivity.STATE_TRANSITION.getValue())) {
				stCount++;
				stPos = order;
			}
		}
		if (stCount > 1) {
			reportItemSet.error(ImportConstants.TASK_TYPE, "Only one state transition task must be present ");
		}
		if (stPos > 0 && stPos != tasksCount) {
			reportItemSet.error(ImportConstants.TASK_TYPE,
					"The state transition task must be the last task in the list ");
		}

		if (orderSet.size() != tasksRowsMap.size()) {
			reportItemSet.error(ImportConstants.TASK_ORDER, "The order of tasks is incorrect ");
		} else {
			for (int i = 1; i <= tasksCount; i++) {
				if (!orderSet.contains(i)) {
					reportItemSet.error(ImportConstants.TASK_ORDER, "The order of tasks is incorrect ");
					break;
				}

			}
		}
		if (reportItemSet.getErrors().isEmpty()) {
			for (Map.Entry<Integer, ImportListener> eachTask : orderedTasks.entrySet()) {
				currentTaskListRecord.addTaskNode(eachTask.getValue());
			}
		} else {
			records.remove(currentTaskListRecord);
		}
	}

	ReportItem reportItemSet = new ReportItem();
	ReportItem reportItem = new ReportItem();

	@Override
	protected ImportListener processRow(ImportConfiguration configuration, Map<String, String> row) {
		ImportListener record = null;
		if (row != null) {
			reportItem = validator.validate(row, metadata, importConfig.getJobId());
			if (reportItem.getErrors().isEmpty()) {
				if (!Utilities.isStringEmpty(row.get(ImportConstants.RECORD_TYPE))
						&& row.get(ImportConstants.RECORD_TYPE).equalsIgnoreCase(RecordType.TASKLIST.getValue())) {
					record = new TaskListRecord(metadata, reportItem);
				} else if (!Utilities.isStringEmpty(row.get(ImportConstants.RECORD_TYPE))
						&& row.get(ImportConstants.RECORD_TYPE).equalsIgnoreCase(RecordType.TASK.getValue())) {
					record = new TaskRecord(metadata, reportItem);
				}
				if (Objects.nonNull(record)) {
					record.doWork(new ImportEvent(record, row));
				}
			} else {
				ErrorRecord errorRecord = new ErrorRecord();
				errorRecord.setType(row.get(ImportConstants.RECORD_TYPE));
				record = errorRecord;
			}
		}
		return record;
	}

	protected void generateErrorReport(String jobId, Path path, ImportConfiguration importparm) {
		int response = 0, nodes[] = null;
		boolean hasError = false;
		try {
			List<List<Map<String, String>>> rowData = super.getSheetDataSet(path, this.getSheetName(),
					ImportConstants.TASKLIST_LEGACYID, RecordType.TASKLIST.getValue(), RecordType.TASK.getValue());
			if (null == rowData || rowData.size() == 0)
				return;
			SOAPRequestObject importRequest = new SOAPRequestObject(
					"http://schemas/OpenTextContractCenterImport/ActivityListsImportStatus/operations",
					"ReadActivityListsImportStatusbyJobID", null, null);
			int input = NomUtil.parseXML("<JobID>" + jobId + "</JobID>");
			importRequest.addParameterAsXml(input);
			response = importRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//ActivityListsImportStatus", response);
			List<Map<String, String>> errorRecords = new LinkedList<Map<String, String>>();

			for (List<Map<String, String>> eachSet : rowData) {
				String LegacyID = null, importStatus = "InvalidData", logDetails = null;
				hasError = false;

				if (!Utilities.isStringEmpty(eachSet.get(0).get("LegacyID"))) {
					LegacyID = Node.getDataWithDefault(NomUtil.getNode(
							".//ActivityListsImportStatus[LegacyID=" + eachSet.get(0).get("LegacyID") + "]", response),
							null);

					if (LegacyID != null) {
						hasError = true;
						importStatus = Node.getDataWithDefault(NomUtil.getNode(".//ActivityListsImportStatus[LegacyID="
								+ eachSet.get(0).get("LegacyID") + "]//ImportStatus", response), null);
						logDetails = Node.getDataWithDefault(NomUtil.getNode(".//ActivityListsImportStatus[LegacyID="
								+ eachSet.get(0).get("LegacyID") + "]//LogDetails", response), null);
					} else if (Utilities.isStringEmpty(eachSet.get(0).get("Type"))) {
						hasError = true;
						logDetails = "Errors : {Type= Cannot be empty}";
					} else if (!Utilities.isStringEmpty(eachSet.get(0).get("Type"))
							&& !eachSet.get(0).get("Type").equalsIgnoreCase(RecordType.TASKLIST.getValue())) {
						hasError = true;
						String type = eachSet.get(0).get("Type");
						logDetails = "Errors : {Type= '" + type + "' is not valid}";
					}
				} else {
					if (!Utilities.isStringEmpty(eachSet.get(0).get("Type"))) {
						if (eachSet.get(0).get("Type").equalsIgnoreCase(RecordType.TASKLIST.getValue())) {
							hasError = true;
							logDetails = "Errors : {LegacyID= Cannot be empty}";
						}
					} else {
						hasError = true;
						logDetails = "Errors : {LegacyID= Cannot be empty; Type= Cannot be empty}";
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

}