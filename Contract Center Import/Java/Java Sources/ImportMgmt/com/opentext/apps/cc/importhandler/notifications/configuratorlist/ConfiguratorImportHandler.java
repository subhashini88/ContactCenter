package com.opentext.apps.cc.importhandler.notifications.configuratorlist;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import com.opentext.apps.cc.importcontent.ImportConfiguration;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.notifications.AbstractNotificationsImportHandler;
import com.opentext.apps.cc.importhandler.notifications.ImportConstants;

public class ConfiguratorImportHandler extends AbstractNotificationsImportHandler {

	private final MetadataInitializer metadata;
	private final ImportValidator validator;

	public ConfiguratorImportHandler() {
		this.metadata = new MetadataInitializer();
		this.validator = new ImportValidator();
	}

	@Override
	public String[] getSheetsName() {

		return new String[] { ImportConstants.NOTIFICATION_SHEET_NAME };
	}

	@Override
	protected ImportListener processRow(ImportConfiguration configuration, Map<String, String> row, String sheetName) {

		switch (sheetName) {
		case ImportConstants.NOTIFICATION_SHEET_NAME:
			importConfigurator(configuration, row);
			break;
		default:
		}
		return null;
	}

	private ImportListener importConfigurator(ImportConfiguration configuration, Map<String, String> row) {
		ConfiguratorRecord record = null;
		if (Objects.nonNull(row) && !row.isEmpty()) {
			if (Objects.isNull(metadata) || Objects.isNull(metadata.getAllProcessMap())
					|| metadata.getAllProcessMap().isEmpty()) {
				setProcesStatesAreThere(false);
			}
			ReportItem reportItem = validator.validate(row, metadata);
			if (Objects.isNull(reportItem.getErrors()) || reportItem.getErrors().isEmpty()) {
				record = new ConfiguratorRecord(metadata, reportItem);
				record.doWork(new ImportEvent(record, row));
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

	@Override
	protected void commit(Collection<ImportListener> records, String sheetName) {

	}

	@Override
	public String getWorkBookName() {
		return ImportConstants.NOTIFICATION_CONFIGURATOR_LIST_FILE_NAME + ImportConstants.EXCEL_EXTENSION;
	}

}
