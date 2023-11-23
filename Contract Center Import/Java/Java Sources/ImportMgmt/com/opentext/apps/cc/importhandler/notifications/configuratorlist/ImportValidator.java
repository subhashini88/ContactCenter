package com.opentext.apps.cc.importhandler.notifications.configuratorlist;

import java.util.Map;
import java.util.Objects;

import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.notifications.ImportConstants;
import com.opentext.apps.cc.importhandler.notifications.process.Process;

public class ImportValidator {

	public ReportItem validate(Map<String, String> rowData, MetadataInitializer metadata) {
		ReportItem report = new ReportItem();
		if (Objects.nonNull(rowData) && Objects.nonNull(metadata)) {
			Process process = metadata.getAllProcessMap().get(rowData.get(ImportConstants.PROCESS_NAME));
			if (Objects.nonNull(process) && Objects.nonNull(process.getItemId())) {
				boolean isStateTransition = Boolean.parseBoolean(rowData.get(ImportConstants.IS_STATE_TRANSITION));
				if (isStateTransition) {
					String fromStateId = process.getStates().get(rowData.get(ImportConstants.FROM_STATE));
					String fromStateName = rowData.get(ImportConstants.FROM_STATE);
					String toStateId = process.getStates().get(rowData.get(ImportConstants.TO_STATE));
					if ( (!Objects.isNull(fromStateName)) &&(Objects.isNull(fromStateId) || fromStateId.isEmpty())) {
						report.error(ImportConstants.FROM_STATE,
								rowData.get(ImportConstants.FROM_STATE) + " is not a valid state.");
					}
					if (Objects.isNull(toStateId) || toStateId.isEmpty()) {
						report.error(ImportConstants.TO_STATE,
								rowData.get(ImportConstants.TO_STATE) + " is not a valid to state.");
					}
				} else {
					String stateId = process.getStates().get(rowData.get(ImportConstants.STATE));
					String stateName = rowData.get(ImportConstants.STATE);
					String actionId = process.getActions().get(rowData.get(ImportConstants.ACTION));
					if ((!Objects.isNull(stateName)) && (Objects.isNull(stateId) || stateId.isEmpty())) {
						report.error(ImportConstants.STATE,
								rowData.get(ImportConstants.STATE) + " is not a valid state.");
					}
					if (Objects.isNull(actionId) || actionId.isEmpty()) {
						report.error(ImportConstants.ACTION,
								rowData.get(ImportConstants.ACTION) + " is not a valid to action.");
					}
				}
				String templateId = process.getEmailTemplates().get(rowData.get(ImportConstants.EMAIL_TEMPLATE));
				if (Objects.isNull(templateId) || templateId.isEmpty()) {
					report.error(ImportConstants.EMAIL_TEMPLATE,
							rowData.get(ImportConstants.EMAIL_TEMPLATE) + " is not a valid template.");
				}

				String roles = rowData.get(ImportConstants.ROLE);
				if (Objects.nonNull(roles) && !roles.isEmpty()) {
					for (String role : roles.split(",")) {
						role = role.trim();
						String roleId = metadata.getAllRolesMap().get(role);
						if (Objects.isNull(roleId) || roleId.isEmpty()) {
							report.error(ImportConstants.ROLE, role + " is not a valid role.");
						}
					}
				}

			} else {
				report.error(ImportConstants.PROCESS_NAME,
						rowData.get(ImportConstants.PROCESS_NAME) + " is not a valid process.");
			}
		}
		return report;
	}
}
