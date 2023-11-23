package com.opentext.apps.cc.importhandler.notifications.process;

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
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;
import com.opentext.apps.cc.importhandler.notifications.AbstractNotificationsImportHandler;
import com.opentext.apps.cc.importhandler.notifications.ImportConstants;

public class ProcessImportHandler extends AbstractNotificationsImportHandler {

	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(ProcessImportHandler.class);
	
	public ProcessImportHandler() {
		this.metadata = new MetadataInitializer();
		this.validator = new ImportValidator();
	}

	@Override
	public String[] getSheetsName() {
		return new String[] { ImportConstants.NOTIFICATION_PROCESS_STATES_SHEET_NAME,
				ImportConstants.NOTIFICATION_PROCESS_ACTIONS_SHEET_NAME,
				ImportConstants.NOTIFICATION_PROCESS_EMAIL_TEMPLATES_SHEET_NAME,ImportConstants.NOTIFICATION_PROCESS_ROLES_SHEET_NAME };
	}

	@Override
	protected ImportListener processRow(ImportConfiguration configuration, Map<String, String> row, String sheetName) {
		ProcessItemType type = null;
		ProcessRecord record = null;
		switch (sheetName) {
		case ImportConstants.NOTIFICATION_PROCESS_ACTIONS_SHEET_NAME:
			type = ProcessItemType.ACTION;
			break;
		case ImportConstants.NOTIFICATION_PROCESS_STATES_SHEET_NAME:
			type = ProcessItemType.STATE;
			break;
		case ImportConstants.NOTIFICATION_PROCESS_EMAIL_TEMPLATES_SHEET_NAME:
			type = ProcessItemType.EMAIL_TEMPLATE;
			break;
		case ImportConstants.NOTIFICATION_PROCESS_ROLES_SHEET_NAME:
			type = ProcessItemType.ROLE;
			break;
		}
		if (Objects.nonNull(row) && !row.isEmpty()) {
			try {
				createProcessIfNotExists(row.get(ImportConstants.PROCESS_NAME), row.get(ImportConstants.LAYOUT_ID));
				ReportItem reportItem = validator.validate(row, metadata, type);
				if (Objects.isNull(reportItem.getErrors()) || reportItem.getErrors().isEmpty()) {
					record = new ProcessRecord(metadata, reportItem, type);
					record.doWork(new ImportEvent(record, row));

				} else {
					row.put(ImportConstants.STATUS, ImportConstants.NOT_COMPLETED);
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
	}

	private void createProcessIfNotExists(String processName, String layoutID) throws Exception {

		if (Objects.nonNull(processName) && !processName.isEmpty() && Objects.nonNull(metadata)
				&& Objects.nonNull(metadata.getAllProcessMap())
				&& !metadata.getAllProcessMap().containsKey(processName)) {
			int createProcessNode = 0, processItemIdResponse = 0;
			try {
				SOAPRequestObject createProcessRequest = new SOAPRequestObject(
						"http://schemas/OpenTextNotifications/Process/operations", "CreateProcess", null, null);
				createProcessNode = NomUtil.parseXML("<Process-create></Process-create>");
				Node.setDataElement(createProcessNode, ImportConstants.NAME, processName);
				if (Objects.nonNull(layoutID) && !layoutID.isEmpty()) {
				Node.setDataElement(createProcessNode, "LayoutURI", layoutID);
				}
				createProcessRequest.addParameterAsXml(createProcessNode);
				processItemIdResponse = createProcessRequest.sendAndWait();
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//Process-id/ItemId", processItemIdResponse),
						null);
				if (Objects.isNull(itemId)) {
					logger._log("com.opentext.apps.cc.importhandler.notifications.process.ProcessImportHandler", Severity.ERROR, null,"Error in process creation service.");
					throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_IN_PROCESS_CREATION_SERVICE);
				}
				Process process = new Process(itemId, processName);
				metadata.getAllProcessMap().put(processName, process);

			} finally {
				Utilities.cleanAll(createProcessNode, processItemIdResponse);
			}
		}else if(Objects.nonNull(processName) && !processName.isEmpty() && Objects.nonNull(metadata)
				&& Objects.nonNull(metadata.getAllProcessMap())
				&& metadata.getAllProcessMap().containsKey(processName)) {
			
			updateProcessIfExists(processName, layoutID);
		}
	}
	
	private void updateProcessIfExists(String processName, String layoutID) throws Exception {
		
		int updateProcessNode = 0, processItemIdResponse = 0, processIdNode=0;
		Process process = metadata.getAllProcessMap().get(processName);
		try {
			SOAPRequestObject updateProcessRequest = new SOAPRequestObject(
					"http://schemas/OpenTextNotifications/Process/operations", "UpdateProcess", null, null);
			processIdNode = NomUtil.parseXML("<Process-id></Process-id>");
			Node.setDataElement(processIdNode, "ItemId", process.getItemId());
			updateProcessNode = NomUtil.parseXML("<Process-update></Process-update>");
			if (Objects.nonNull(layoutID) && !layoutID.isEmpty()) {
			Node.setDataElement(updateProcessNode, "LayoutURI", layoutID);
			}
			Node.setDataElement(updateProcessNode, ImportConstants.NAME, processName);
			updateProcessRequest.addParameterAsXml(processIdNode);
			updateProcessRequest.addParameterAsXml(updateProcessNode);
			processItemIdResponse = updateProcessRequest.sendAndWait();
			String itemId = Node.getDataWithDefault(NomUtil.getNode(".//Process-id/ItemId", processItemIdResponse),
					null);
			if (Objects.isNull(itemId)) {
				logger._log("com.opentext.apps.cc.importhandler.notifications.process.ProcessImportHandler", Severity.ERROR, null,"Error in process updation service.");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_IN_PROCESS_CREATION_SERVICE);
			}
			

		} finally {
			Utilities.cleanAll(updateProcessNode, processItemIdResponse, processIdNode);
		}
		
	}

	@Override
	public String getWorkBookName() {
		return ImportConstants.NOTIFICATIONS_PROCESS_LIST_FILE_NAME + ImportConstants.EXCEL_EXTENSION;
	}

}
