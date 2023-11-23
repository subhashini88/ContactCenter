package com.opentext.apps.cc.importhandler.datamanager.tasklist;

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
import com.eibus.xml.nom.internal.NOMDocumentPool;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.BatchIterator;
import com.opentext.apps.cc.importcontent.ContentManagementRuntimeException;
import com.opentext.apps.cc.importcontent.FileUtil;
import com.opentext.apps.cc.importcontent.ImportConfiguration;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.datamanager.AbstractDataManagerImportHandler;
import com.opentext.apps.cc.importhandler.datamanager.rules.DefaultRulesImportHandler;
import com.opentext.apps.cc.importhandler.datamanager.tasklist.ImportConstants;
import com.opentext.apps.cc.importhandler.datamanager.tasklist.ImportValidator;
import com.opentext.apps.cc.importhandler.datamanager.tasklist.TaskListImportHandler;
import com.opentext.apps.cc.importhandler.datamanager.tasklist.MetadataInitializer;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;
import com.opentext.apps.cc.importhandler.datamanager.tasklist.ErrorRecord;
import com.opentext.apps.cc.importhandler.datamanager.tasklist.TaskListRecord;
import com.opentext.apps.cc.importhandler.datamanager.tasklist.TaskRecord;
import com.opentext.apps.cc.importhandler.datamanager.tasklist.ImportConstants.RecordType;
import com.opentext.apps.cc.importhandler.datamanager.tasklist.ImportConstants.TypeOfActivity;

public class TaskListImportHandler extends AbstractDataManagerImportHandler {

	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(TaskListImportHandler.class);
	private int skippedRecords = 0;
	private int failedRecords = 0;
	private int completedRecords = 0;
	private int[] rulesResult;
	
	public TaskListImportHandler() {
		metadata = new MetadataInitializer();
		validator = new ImportValidator();
	}

	@Override
	public String[] getSheetsName() {
		return new String[] { ImportConstants.TaskLists_SHEET_NAME};
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
	protected void importContent(ImportConfiguration configuration) {
		this.importConfig = configuration;
		int batchNumber;
		Properties properties = getMappingProperties();
		configuration.setMappedInstances(new LinkedHashMap<String, String>());
		//Rules List import handler
		DefaultRulesImportHandler defRuleHandler= new DefaultRulesImportHandler();
		rulesResult = defRuleHandler.invokeDefaultRulesImporthandler(configuration, properties, this);
		
		List<Map<String, String>> sheetData = super.readContent(ImportConstants.TaskLists_SHEET_NAME);
		createProcessingRecord(ImportConstants.IMPORT_DISPLAYNAME, rulesResult[0]+rulesResult[0]);
		ArrayList<Map<String, String>> tasks = new ArrayList<>();
		String jobId = configuration.getJobId();
		String Code = null;

		Iterator<List<Map<String, String>>> batchIterator = new BatchIterator(sheetData, properties, sheetData.size());
		ArrayList<ImportListener> records = new ArrayList<>();
		TreeMap<Integer, ImportListener> orderedTasks = new TreeMap<>();
		boolean newTaskList = true;
		batchNumber = 0;
		
		try {
			while (batchIterator.hasNext()) {
				batchNumber++;
				List<Map<String, String>> rows = batchIterator.next();
				TaskListRecord currentTaskListRecord = null;
				createBatchRecord(getProcessingRecordItemId(), batchNumber, rows.size());
				metadata.getActivityListSet().addAll(metadata.getActivityLists().keySet());
				for (Map<String, String> row : rows) {

					if ((Objects.nonNull(row.get(ImportConstants.CODE)) && (Objects
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
								row.put(ImportConstants.STATUS, ImportConstants.NOT_COMPLETED);
							}
						}
						reportItemSet = new ReportItem();
						newTaskList = false;
					}
					ImportListener record = processRow(configuration, row, ImportConstants.TaskLists_SHEET_NAME);
					if (null != record) {
						if (record instanceof TaskListRecord) {
							records.add(record);
							currentTaskListRecord = (TaskListRecord) record;
							Code = row.get(ImportConstants.CODE);
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
									Code = row.get(ImportConstants.CODE);
								}
							} else {
								flag = true;
							}
							if (flag) {
								if (Objects.nonNull(row.get(ImportConstants.CODE))) {
									Code = row.get(ImportConstants.CODE);
								} else {
									if (Objects.nonNull(currentTaskListRecord)) {
										updateRecordsCount(ImportConstants.STATUS_ERROR);
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
				}
				
			}

			int batchCount = 0;
			while (batchCount < records.size()) {
				int endSize = (batchCount + BATCH_SIZE) > records.size() ? records.size() : batchCount + BATCH_SIZE;
				commit(records.subList(batchCount, endSize), ImportConstants.TaskLists_SHEET_NAME);
				batchCount = endSize;
			}
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}



	public void validateTasks(ArrayList<Map<String, String>> tasksRowsMap, TaskListRecord currentTaskListRecord,
			TreeMap<Integer, ImportListener> orderedTasks, ArrayList<ImportListener> records) {
		int stCount = 0; // stateTransition tasks count
		int stPos = -1; // stateTransition task position
		int tasksCount = tasksRowsMap.size();
		Set<Integer> orderSet = new HashSet<Integer>();
		int order;
		if(metadata.getActivityLists().containsKey(metadata.currentTaskListCode)) {
			metadata.getActivitySet().addAll(metadata.getActivityLists().get(metadata.currentTaskListCode).keySet());
		}
		for (int i = 0; i < tasksCount; i++) {
			order = Integer.parseInt(tasksRowsMap.get(i).get(ImportConstants.TASK_ORDER));
			orderSet.add(order);
			if(metadata.getActivitySet().contains(tasksRowsMap.get(i).get(ImportConstants.CODE))) {
				metadata.getActivitySet().remove(tasksRowsMap.get(i).get(ImportConstants.CODE));
			}
			if (tasksRowsMap.get(i).get(ImportConstants.TASK_TYPE)
					.equalsIgnoreCase(TypeOfActivity.STATE_TRANSITION.getValue())) {
				stCount++;
				stPos = order;
			}
		}
		if (stCount > 1) {
			reportItemSet.error(ImportConstants.TASK_TYPE, "Only one state transition task must be present ");
		}
		if (stPos >= 0 && stPos != tasksCount-1) {
			reportItemSet.error(ImportConstants.TASK_TYPE,
					"The state transition task must be the last task in the list ");
		}

		if (orderSet.size() != tasksRowsMap.size()) {
			reportItemSet.error(ImportConstants.TASK_ORDER, "The order of tasks is incorrect ");
		} else {
			for (int i = 0; i <tasksCount; i++) {
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
			
			for (String ativityCode : metadata.getActivitySet()) {
				int activityNode = NomUtil.parseXML("<Activity></Activity>");
				int idNode = NOMDocumentPool.getInstance().createElement("Activity-id");
				Node.setDataElement(idNode, "Id", metadata.getActivityLists().get(metadata.currentTaskListCode).get(ativityCode));
				NomUtil.appendChild(idNode, activityNode);
				currentTaskListRecord.addTaskDeleteNode(activityNode);
	        }
			
			
		} else {
			updateRecordsCount(ImportConstants.STATUS_ERROR);
			records.remove(currentTaskListRecord);
		}
	}

	ReportItem reportItemSet = new ReportItem();
	ReportItem reportItem = new ReportItem();

	@Override
	protected ImportListener processRow(ImportConfiguration configuration, Map<String, String> row, String sheetName) {
		ImportListener record = null;
		if (row != null) {
			reportItem = validator.validate(row, metadata, importConfig.getJobId());
			if (reportItem.getErrors().isEmpty()) {
				if (!Utilities.isStringEmpty(row.get(ImportConstants.RECORD_TYPE))
						&& row.get(ImportConstants.RECORD_TYPE).equalsIgnoreCase(RecordType.TASKLIST.getValue())) {
					record = new TaskListRecord(metadata, reportItem);
					metadata.currentTaskListCode = row.get(ImportConstants.CODE);
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

	@Override
	protected void commit(Collection<ImportListener> records, String sheetName) {
		int tasklistsRecordNode = 0,rulesMainNode=0, jobIdNode = 0, tasklistImportResponse = 0, updateNode=0, batchRecordId=0,forEachNode = 0;
		if (records.size() > 0) {
			tasklistsRecordNode = NomUtil.parseXML("<ActivityLists></ActivityLists>");
			rulesMainNode = NomUtil.parseXML("<RulesList></RulesList>");
			for (ImportListener record : records) {
				forEachNode = NomUtil.parseXML("<ActivityListsInt></ActivityListsInt>");
				Node.appendToChildren(forEachNode, tasklistsRecordNode);
				Node.appendToChildren(record.getnode(), forEachNode);
			}
			for (String activityList : metadata.getActivityListSet()) {
				forEachNode = NomUtil.parseXML("<ActivityListsDel></ActivityListsDel>");
				int activityListNode = NomUtil.parseXML("<ActivityList></ActivityList>");
				int idNode = NOMDocumentPool.getInstance().createElement("ActivityList-id");
				Node.setDataElement(idNode, "Id", metadata.getActivityListsID().get(activityList));
				NomUtil.appendChild(idNode, activityListNode);
				int ActivityDelNode = Node.createElement("ActivityDel", activityListNode);
				int activitiesDeleteNode = Node.createElement("Activities", ActivityDelNode);
				for (Map.Entry<String,String> activity : metadata.getActivityLists().get(activityList).entrySet()) {
				    int activityNode = NomUtil.parseXML("<Activity></Activity>");
					int activityIdNode = NOMDocumentPool.getInstance().createElement("Activity-id");
					Node.setDataElement(activityIdNode, "Id", activity.getValue());
					NomUtil.appendChild(activityIdNode, activityNode);
					Node.appendToChildren(activityNode, activitiesDeleteNode);
				}
				NomUtil.appendChild(activityListNode, forEachNode);
				Node.appendToChildren(forEachNode, tasklistsRecordNode);
	        }
			/*Node.appendToChildren(rulesListNode, tasklistsRecordNode);*/
			Node.appendToChildren(rulesResult[2], rulesMainNode);
			jobIdNode = NomUtil.parseXML("<JobID>" + super.importConfig.getJobId() + "</JobID>");
			updateNode = NomUtil.parseXML("<BatchRecord-update></BatchRecord-update>");
			batchRecordId = NomUtil.parseXML("<BatchRecord-id></BatchRecord-id>");
			Node.setDataElement(batchRecordId, "ItemId2", getBatchRecordItemId());
			Node.setDataElement(updateNode, "FailedRecords", String.valueOf(failedRecords+rulesResult[1]));
			SOAPRequestObject importRequest = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/contractcenterimport/16.3", "DefaultActivityListsImport", null, null);
			importRequest.addParameterAsXml(jobIdNode);
			importRequest.addParameterAsXml(batchRecordId);
			importRequest.addParameterAsXml(tasklistsRecordNode);
			importRequest.addParameterAsXml(rulesMainNode);
			
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