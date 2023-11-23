package com.opentext.apps.cc.importhandler.datamanager.statesandactions;

import java.util.Map;
import java.util.Objects;

import com.opentext.apps.cc.importcontent.ReportItem;

public class ImportValidator {
	public ImportValidator() {

	}

	public ReportItem validate(Map<String, String> rowData, MetadataInitializer metadata, ProcessItemType type) {
		ReportItem report = new ReportItem();
		String value = null;
		String processName = rowData.get(ImportConstants.PROCESSNAME);
		String purpose = rowData.get(ImportConstants.PURPOSE);
		switch (type) {
		case ACTION:
			value = rowData.get(ImportConstants.ACTION);
			break;
		case STATE:
			value = rowData.get(ImportConstants.STATE);
			break;
		case PROPERTIES:
			value = rowData.get(ImportConstants.DISPLAYNAME);
			validatePropertiesSheet(rowData, report);
			break;
		default:
			break;
		}
		if (Objects.isNull(rowData)) {
			report.error(ImportConstants.PROCESSNAME, "Row is null.!");
			return report;
		}
		if (Objects.isNull(processName) || processName.isEmpty()) {
			report.error(ImportConstants.PROCESSNAME, "Process name is empty.! ");
			return report;
		}
		if (Objects.isNull(purpose) || purpose.isEmpty()) {
			report.error(ImportConstants.PURPOSE, "Purpose name is empty.! ");
			return report;
		}

		if ((Objects.isNull(value) || value.isEmpty()) && (null != type && type != ProcessItemType.PROCESS)) {
			report.error(Objects.nonNull(type) ? type.name() : " Process Item type", " is Empty!");
		}

		if ((Objects.isNull(value) || value.isEmpty()) && (null != type && type != ProcessItemType.PROCESS)) {
			report.error(Objects.nonNull(type) ? type.name() : " Process Item type", " is Empty!");
		}

		if ((Objects.isNull(metadata) || Objects.isNull(metadata.getAllProcessMap())
				|| Objects.isNull(metadata.getAllProcessMap().get(processName))
				|| Objects.isNull(metadata.getAllProcessMap().get(processName).getItemId())
				|| metadata.getAllProcessMap().get(processName).getItemId().isEmpty())
				&& type != ProcessItemType.PROCESS) {
			report.error(ImportConstants.PROCESSNAME, "Process Item id is null. ");
			return report;
		}

		return report;
	}

	private void validatePropertiesSheet(Map<String, String> rowData, ReportItem report) {
		String name = rowData.get(ImportConstants.PROPNAME);
		String display = rowData.get(ImportConstants.DISPLAYNAME);
		String dataType = rowData.get(ImportConstants.DATATYPE);
		String xPath = rowData.get(ImportConstants.Xpath);

		if ((Objects.isNull(rowData) || name.isEmpty())) {
			report.error(ImportConstants.PROPNAME, " is Empty!");
		}
		if ((Objects.isNull(rowData) || display.isEmpty())) {
			report.error(ImportConstants.DISPLAYNAME, " is Empty!");
		}
		if ((Objects.isNull(rowData) || dataType.isEmpty())) {
			report.error(ImportConstants.DATATYPE, " is Empty!");
		}
		if ((Objects.isNull(rowData) || xPath.isEmpty())) {
			report.error(ImportConstants.Xpath, " is Empty!");
		}
	}

}
