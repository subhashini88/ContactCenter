package com.opentext.apps.cc.importhandler.datamanager.tasklist;

import java.util.Map;
import java.util.Objects;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ReportItem;

import com.opentext.apps.cc.importcontent.CommonEnums.Status;
import com.opentext.apps.cc.importhandler.datamanager.tasklist.ImportConstants.AssignmentType;
import com.opentext.apps.cc.importhandler.datamanager.tasklist.ImportConstants.TypeOfActivity;
import com.opentext.apps.cc.importhandler.datamanager.tasklist.ImportConstants.RecordType;

public class ImportValidator {
	ReportItem report = new ReportItem();

	public ReportItem validate(Map<String, String> rowData, MetadataInitializer metadata, String jobId) {
		if (rowData == null) {
			return report;
		}
		String recordType = rowData.get(ImportConstants.RECORD_TYPE);
		
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CODE)) && ((!Utilities.isStringEmpty(recordType) && !recordType.equalsIgnoreCase(ImportConstants.RecordType.TASK.getValue())) || Utilities.isStringEmpty(recordType))) {
			report = new ReportItem();
		}
		if (Utilities.isStringEmpty(recordType)) {
			createErrorMessage(ImportConstants.RECORD_TYPE, "Cannot be empty");
		} else if (!RecordType.contains(recordType)) {
			createErrorMessage(ImportConstants.RECORD_TYPE, "'" + rowData.get(ImportConstants.RECORD_TYPE) + "' is not valid");
		} else {
			if (recordType.equalsIgnoreCase(RecordType.TASKLIST.getValue())) {
				report = new ReportItem();
				validateTaskList(rowData, report);
			} else if (recordType.equalsIgnoreCase(RecordType.TASK.getValue())) {
				validateTask(rowData, metadata, report);
			}
		}
		return report;
	}

	public void validateTaskList(Map<String, String> rowData, ReportItem report) {
		if (Utilities.isStringEmpty(rowData.get(ImportConstants.CODE))) {
			createErrorMessage(ImportConstants.CODE, "Cannot be empty");
			
		}
		if (Utilities.isStringEmpty(rowData.get(ImportConstants.NAME))) {
			createErrorMessage(ImportConstants.NAME, "Task list name cannot be empty");
		}
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.STATUS)) && !Status.contains(rowData.get(ImportConstants.STATUS))) {
			createErrorMessage(ImportConstants.STATUS, "'" + rowData.get(ImportConstants.STATUS) + "' is not valid");
			}
	}

	public void validateTask(Map<String, String> rowData, MetadataInitializer metadata, ReportItem report) {
		String taskType = rowData.get(ImportConstants.TASK_TYPE);

		if (Utilities.isStringEmpty(rowData.get(ImportConstants.NAME))) {
			createErrorMessage(ImportConstants.NAME, "Task name cannot be empty");
		}
		if (Utilities.isStringEmpty(rowData.get(ImportConstants.TASK_ORDER))) {
			createErrorMessage(ImportConstants.TASK_ORDER, "Cannot be empty");
		} else {
			if (!rowData.get(ImportConstants.TASK_ORDER).matches(ImportConstants.REGEX_EXP)) {
				createErrorMessage(ImportConstants.TASK_ORDER, " '" + rowData.get(ImportConstants.TASK_ORDER) + "' is not valid");
			}
		}
		if (Utilities.isStringEmpty(taskType)) {
			createErrorMessage(ImportConstants.TASK_TYPE, "Cannot be empty");
		} else {
			String assignmentType = rowData.get(ImportConstants.ASSIGNMENT_TYPE);
			if (taskType.equalsIgnoreCase(TypeOfActivity.STANDARD.getValue())
					|| taskType.equalsIgnoreCase(TypeOfActivity.APPROVAL.getValue())) {

				if (Utilities.isStringEmpty(assignmentType)) {
					createErrorMessage(ImportConstants.ASSIGNMENT_TYPE, "Cannot be empty");
				} else if (!AssignmentType.contains(assignmentType)) {
					createErrorMessage(ImportConstants.ASSIGNMENT_TYPE,
							"'" + rowData.get(ImportConstants.ASSIGNMENT_TYPE) + "' is not valid");
				} else {
					if (assignmentType.equalsIgnoreCase(AssignmentType.INDIVIDUAL.getValue())) {
						if (!Utilities.isStringEmpty(rowData.get(ImportConstants.ASSIGN_TO_USER))) {
							if (Objects.isNull(metadata) || Objects.isNull(metadata.personsMap) || Objects
									.isNull(metadata.personsMap.get(rowData.get(ImportConstants.ASSIGN_TO_USER)))) {
								// validate AssignToUser
								String personId = getAssignToUser(rowData.get(ImportConstants.ASSIGN_TO_USER));
								if (personId != null) {
									metadata.personsMap.put(rowData.get(ImportConstants.ASSIGN_TO_USER), personId);
								} else {
									createErrorMessage(ImportConstants.ASSIGN_TO_USER, "The UserID '"
											+ rowData.get(ImportConstants.ASSIGN_TO_USER) + "' is not available");
								}
							}
						} else {
							createErrorMessage(ImportConstants.ASSIGN_TO_USER, "Cannot be empty");
						}
					} else if (assignmentType.equalsIgnoreCase(AssignmentType.ROLE.getValue())) {
						if (!Utilities.isStringEmpty(rowData.get(ImportConstants.ASSIGN_TO_ROLE))) {
							// validate AssignToRole
							if (Objects.isNull(metadata) || Objects.isNull(metadata.rolesMap) || Objects
									.isNull(metadata.rolesMap.get(rowData.get(ImportConstants.ASSIGN_TO_ROLE)))) {
								String IdentityId = getAssignToRole(rowData.get(ImportConstants.ASSIGN_TO_ROLE));
								if (IdentityId != null) {
									metadata.rolesMap.put(rowData.get(ImportConstants.ASSIGN_TO_ROLE), IdentityId);
								} else {
									createErrorMessage(ImportConstants.ASSIGN_TO_ROLE, "The Role '"
											+ rowData.get(ImportConstants.ASSIGN_TO_ROLE) + "' is not available");
								}
							}
						} else {
							createErrorMessage(ImportConstants.ASSIGN_TO_ROLE, "Cannot be empty");
						}
					}
				}

				if (!Utilities.isStringEmpty(rowData.get(ImportConstants.DUE_IN))
						&& rowData.get(ImportConstants.DUE_IN).matches(ImportConstants.REGEX_EXP)) {
					String escalationType = rowData.get(ImportConstants.ESCALATION_TYPE);
					if (Utilities.isStringEmpty(escalationType)) {
						createErrorMessage(ImportConstants.ESCALATION_TYPE, "Cannot be empty");
					} else if (!AssignmentType.contains(escalationType)) {
						createErrorMessage(ImportConstants.ESCALATION_TYPE,
								"'" + rowData.get(ImportConstants.ESCALATION_TYPE) + "' is not valid");
					} else {
						if (escalationType.equalsIgnoreCase(AssignmentType.INDIVIDUAL.getValue())) {
							if (!Utilities.isStringEmpty(rowData.get(ImportConstants.ESCALATION_TO_USER))) {
								if (Objects.isNull(metadata) || Objects.isNull(metadata.personsMap) || Objects.isNull(
										metadata.personsMap.get(rowData.get(ImportConstants.ESCALATION_TO_USER)))) {
									// validate AssignToUser
									String personId = getAssignToUser(rowData.get(ImportConstants.ESCALATION_TO_USER));
									if (personId != null) {
										metadata.personsMap.put(rowData.get(ImportConstants.ESCALATION_TO_USER),
												personId);
									} else {
										createErrorMessage(ImportConstants.ESCALATION_TO_USER,
												"The UserID '" + rowData.get(ImportConstants.ESCALATION_TO_USER)
														+ "' is not available");
									}
								}
							} else {
								createErrorMessage(ImportConstants.ESCALATION_TO_USER, "Cannot be empty");
							}
						} else if (escalationType.equalsIgnoreCase(AssignmentType.ROLE.getValue())) {
							if (!Utilities.isStringEmpty(rowData.get(ImportConstants.ESCALATION_TO_ROLE))) {
								// validate AssignToRole
								if (Objects.isNull(metadata) || Objects.isNull(metadata.rolesMap) || Objects.isNull(
										metadata.rolesMap.get(rowData.get(ImportConstants.ESCALATION_TO_ROLE)))) {
									String IdentityId = getAssignToRole(
											rowData.get(ImportConstants.ESCALATION_TO_ROLE));
									if (IdentityId != null) {
										metadata.rolesMap.put(rowData.get(ImportConstants.ESCALATION_TO_ROLE),
												IdentityId);
									} else {
										createErrorMessage(ImportConstants.ESCALATION_TO_ROLE,
												"The Role '" + rowData.get(ImportConstants.ESCALATION_TO_ROLE)
														+ "' is not available");
									}
								}
							} else {
								createErrorMessage(ImportConstants.ESCALATION_TO_ROLE, "Cannot be empty");
							}
						}
					}

				} else if (!Utilities.isStringEmpty(rowData.get(ImportConstants.DUE_IN))
						&& !rowData.get(ImportConstants.DUE_IN).matches(ImportConstants.REGEX_EXP)) {
					createErrorMessage(ImportConstants.DUE_IN, "The due in  days '" + rowData.get(ImportConstants.DUE_IN) + "' is not valid");
				}
			} else if (taskType.equalsIgnoreCase(TypeOfActivity.CUSTOM.getValue())) {
				if (Utilities.isStringEmpty(rowData.get(ImportConstants.PROCESS_TO_EXECUTE))) {
					createErrorMessage(ImportConstants.PROCESS_TO_EXECUTE, " Cannot be empty");
				}
			} else if (taskType.equalsIgnoreCase(TypeOfActivity.STATE_TRANSITION.getValue())) {
				String state = rowData.get(ImportConstants.TRANSIT_STATE_TO);
				if (!Utilities.isStringEmpty(state)) {
					if (Objects.isNull(metadata) || Objects.isNull(metadata.statesMap) || Objects.isNull(
							metadata.statesMap.get(state)) || Objects.isNull(metadata.processMap.get(rowData.get(ImportConstants.RELATEDINSTANCETYPE)))) {
						String GCStateId = getGCStateId(state, rowData.get(ImportConstants.RELATEDINSTANCETYPE), metadata.activityListProcesses().get(rowData.get(ImportConstants.RELATEDINSTANCETYPE)), metadata);
						if (Objects.isNull(GCStateId)) {
							createErrorMessage(ImportConstants.TRANSIT_STATE_TO, "'" + GCStateId + "' is not valid");
						}
					}
				}
				else {
					createErrorMessage(ImportConstants.TRANSIT_STATE_TO, "Cannot be empty");
				}
			} else {
				createErrorMessage(ImportConstants.TASK_TYPE, "'" + taskType + "' is not valid");
			}

		}
	}
	
	public void createErrorMessage(String columnName, String errorText) {
		String errorMsg = report.getErrors().getOrDefault(columnName, "");
		if(!errorMsg.isEmpty()) {
			errorText = errorMsg + "#$" + errorText;
		}
		report.error(columnName, errorText);
	}
	
	public String getGCStateId(String state, String PROCESS_NAME, String Purpose, MetadataInitializer metadata) {
		int response = 0;
		int nodes[] = null;
		String GCProcessId = null, stateId = null;
		String GCStateId, GCStateName, GCProcessName = null;
		try {
			String[] paramNames = { "processName", "purpose" };
			Object[] paramValues = { PROCESS_NAME, Purpose };
			SOAPRequestObject processRequest = new SOAPRequestObject(
					"http://schemas/OpenTextBasicComponents/GCProcess/operations", "GetGCProcessByNameAndPurpose", paramNames,
					paramValues);
			response = processRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//GCProcess", response);
			for (int i : nodes) {
				GCProcessId = Node.getDataWithDefault(
						NomUtil.getNode(".//GCProcess-id/Id", i), null);
				GCProcessName = Node.getDataWithDefault(
						NomUtil.getNode(".//Name", i), null);
				if(Objects.nonNull(GCProcessId) &&Objects.nonNull(GCProcessName)) {
					metadata.processMap.put(GCProcessName, GCProcessId);
				}
			}
			if(Objects.nonNull(GCProcessId)) {
				String[] paramNames2 = { "processID", "purpose" };
				Object[] paramValues2 = { GCProcessId, Purpose };
				
				SOAPRequestObject stateRequest = new SOAPRequestObject(
						"http://schemas/OpenTextBasicComponents/GCProcess.RelatedGCState/operations", "GetGCStatesByProcessIDAndPurpose", paramNames2,
						paramValues2);
				response = stateRequest.sendAndWait();
				nodes = NomUtil.getNodeList(".//RelatedGCState", response);
				for (int i : nodes) {
					GCStateId = Node.getDataWithDefault(
							NomUtil.getNode(".//RelatedGCState-id/Id1", i), null);
					GCStateName = Node.getDataWithDefault(
							NomUtil.getNode(".//Name", i), null);
					
					if(Objects.nonNull(GCStateId) && Objects.nonNull(GCStateName)) {
						metadata.statesMap.put(GCStateName, GCStateId);
					}
					if(GCStateName.equalsIgnoreCase(state)) {
						stateId = GCStateId;
					}
				}	
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
		return stateId;
	}

	public String getAssignToUser(String userId) {
		int response = 0;
		int nodes[] = null;
		String itemId = null;
		try {
			String[] paramNames = { "memUserID" };
			Object[] paramValues = { userId };
			SOAPRequestObject personRequest = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/cc/basiccomponents/20.2", "GetOrgMemberswithFilters", paramNames,
					paramValues);
			response = personRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//OrgMembers/FindZ_INT_OrgUsersListResponse/OrganizationMembers", response);
			for (int i : nodes) {
				itemId = Node.getDataWithDefault(
						NomUtil.getNode(".//ParticipatingPerson/PersonToUser/Identity-id/ItemId", i), null);
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
		return itemId;
	}

	public String getAssignToRole(String roleName) {
		int response = 0;
		int nodes[] = null;
		String itemId = null;
		String name = null;
		try {
			String[] paramNames = { "contains" };
			Object[] paramValues = { roleName };
			SOAPRequestObject roleRequest = new SOAPRequestObject(
					"http://schemas/OpenTextEntityIdentityComponents/Role/operations", "GetAllRoles", paramNames,
					paramValues);
			response = roleRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Role", response);
			for (int i : nodes) {
				name = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				if (name.equalsIgnoreCase(roleName)) {
					itemId = Node.getDataWithDefault(NomUtil.getNode(".//Identity-id/ItemId", i), null);
				}
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
		return itemId;
	}
}
