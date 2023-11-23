package com.opentext.apps.cc.importhandler.notifications.process;

import java.util.Map;
import java.util.Objects;

import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.notifications.ImportConstants;

public class ImportValidator {

	public ImportValidator() {

	}

	public ReportItem validate(Map<String, String> rowData, MetadataInitializer metadata, ProcessItemType type) {
		ReportItem report = new ReportItem();
		String processName = rowData.get(ImportConstants.PROCESS_NAME);
		if (Objects.isNull(rowData)) {
			report.error(ImportConstants.PROCESS_NAME, "Row is null.!");
			return report;
		}
		if (Objects.isNull(processName) || processName.isEmpty()) {
			report.error(ImportConstants.PROCESS_NAME, "Process name is empty.! ");
			return report;
		}
		if (Objects.isNull(metadata) || Objects.isNull(metadata.getAllProcessMap())
				|| Objects.isNull(metadata.getAllProcessMap().get(processName))
				|| Objects.isNull(metadata.getAllProcessMap().get(processName).getItemId())
				|| metadata.getAllProcessMap().get(processName).getItemId().isEmpty()) {
			report.error(ImportConstants.PROCESS_NAME, "Process Item id is null. ");
			return report;
		}
		String value = null;
		switch (type) {
		case ACTION:
			value = rowData.get(ImportConstants.ACTION);
			break;
		case STATE:
			value = rowData.get(ImportConstants.STATE);
			break;
		case EMAIL_TEMPLATE:
			value = rowData.get(ImportConstants.EMAIL_TEMPLATE);
			break;
		case ROLE:
			value = rowData.get(ImportConstants.PROCESS_ROLE);
			break;
		}
		if (Objects.isNull(value) || value.isEmpty()) {
			report.error(Objects.nonNull(type) ? type.name() : " Process Item type", " is Empty!");
		}
		return report;
	}

}
