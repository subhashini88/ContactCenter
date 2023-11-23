package com.opentext.apps.cc.importhandler.datamanager.tasklist;

import java.util.Map;
import java.util.Objects;

import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.internal.NOMDocumentPool;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.datamanager.tasklist.ImportConstants;
import com.opentext.apps.cc.importhandler.datamanager.tasklist.ImportConstants.AssignmentType;
import com.opentext.apps.cc.importhandler.datamanager.tasklist.ImportConstants.TransitState;
import com.opentext.apps.cc.importhandler.datamanager.tasklist.ImportConstants.TypeOfActivity;

public class TaskRecord implements ImportListener {
	int activityNode;
	MetadataInitializer metadata;

	public TaskRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
	}

	@Override
	public void doWork(ImportEvent event) {
		createRequest(event.getRow());
	}

	private void createRequest(Map<String, String> row) {
		activityNode = NomUtil.parseXML("<Activity></Activity>");
		Node.setDataElement(activityNode, ImportConstants.NAME, row.get(ImportConstants.NAME));
		Node.setDataElement(activityNode, ImportConstants.TASK_TYPE_REQ, TypeOfActivity.getEnumObject(row.get(ImportConstants.TASK_TYPE)).getReqValue());
		Node.setDataElement(activityNode, ImportConstants.DESCRIPTION, row.get(ImportConstants.DESCRIPTION));
		Node.setDataElement(activityNode, ImportConstants.TASK_ORDER, row.get(ImportConstants.TASK_ORDER));
		Node.setDataElement(activityNode, ImportConstants.CODE, row.get(ImportConstants.CODE));
		Node.setDataElement(activityNode, ImportConstants.CREATIONTYPE, row.get(ImportConstants.CREATIONTYPE));
		
		if(metadata.getActivityLists().containsKey(metadata.currentTaskListCode) && metadata.getActivityLists().get(metadata.currentTaskListCode).containsKey(row.get(ImportConstants.CODE))) {
			int idNode = NOMDocumentPool.getInstance().createElement("Activity-id");
			Node.setDataElement(idNode, "Id", metadata.getActivityLists().get(metadata.currentTaskListCode).get(row.get(ImportConstants.CODE)));
			NomUtil.appendChild(idNode, activityNode);
		}

		String taskType = row.get(ImportConstants.TASK_TYPE);
		if (taskType.equalsIgnoreCase(TypeOfActivity.STANDARD.getValue())
				|| taskType.equalsIgnoreCase(TypeOfActivity.APPROVAL.getValue())) {

			Node.setDataElement(activityNode, ImportConstants.ASSIGNMENT_TYPE,
					AssignmentType.getEnumObject(row.get(ImportConstants.ASSIGNMENT_TYPE)).getValue());

			if (!Utilities.isStringEmpty(row.get(ImportConstants.ASSIGN_TO_USER))) {// check for null
				if (!Objects.isNull(metadata) && !Objects.isNull(metadata.personsMap)
						&& !Objects.isNull(metadata.personsMap.get(row.get(ImportConstants.ASSIGN_TO_USER)))) {
					Node.setDataElement(activityNode, ImportConstants.ASSIGN_TO_USER,
							metadata.personsMap.get(row.get(ImportConstants.ASSIGN_TO_USER)));
				}
			}
			if (!Utilities.isStringEmpty(row.get(ImportConstants.ASSIGN_TO_ROLE))) {// check for null
				if (!Objects.isNull(metadata) && !Objects.isNull(metadata.rolesMap)
						&& !Objects.isNull(metadata.rolesMap.get(row.get(ImportConstants.ASSIGN_TO_ROLE)))) {
					Node.setDataElement(activityNode, ImportConstants.ASSIGN_TO_ROLE,
							metadata.rolesMap.get(row.get(ImportConstants.ASSIGN_TO_ROLE)));
				}
			}

			if (!Utilities.isStringEmpty(row.get(ImportConstants.DUE_IN))) {

				Node.setDataElement(activityNode, ImportConstants.DUE_IN, row.get(ImportConstants.DUE_IN));
				Node.setDataElement(activityNode, ImportConstants.ESCALATION_TYPE,
						AssignmentType.getEnumObject(row.get(ImportConstants.ESCALATION_TYPE)).getValue());

				if (!Utilities.isStringEmpty(row.get(ImportConstants.ESCALATION_TO_USER))) {
					if (!Objects.isNull(metadata) && !Objects.isNull(metadata.personsMap)
							&& !Objects.isNull(metadata.personsMap.get(row.get(ImportConstants.ESCALATION_TO_USER)))) {
						Node.setDataElement(activityNode, ImportConstants.ESCALATION_TO_USER,
								metadata.personsMap.get(row.get(ImportConstants.ESCALATION_TO_USER)));
					}
				} else if (!Utilities.isStringEmpty(row.get(ImportConstants.ESCALATION_TO_ROLE))) {
					if (!Objects.isNull(metadata) && !Objects.isNull(metadata.rolesMap)
							&& !Objects.isNull(metadata.rolesMap.get(row.get(ImportConstants.ESCALATION_TO_ROLE)))) {
						Node.setDataElement(activityNode, ImportConstants.ESCALATION_TO_ROLE,
								metadata.rolesMap.get(row.get(ImportConstants.ESCALATION_TO_ROLE)));
					}
				}
			}
		} else if (taskType.equalsIgnoreCase(TypeOfActivity.CUSTOM.getValue())) {
			Node.setDataElement(activityNode, ImportConstants.PROCESS_TO_EXECUTE_REQ,
					row.get(ImportConstants.PROCESS_TO_EXECUTE));
		}
		
		else if (taskType.equalsIgnoreCase(TypeOfActivity.STATE_TRANSITION.getValue())) {
			if (!Objects.isNull(metadata) && !Objects.isNull(metadata.statesMap)
					&& !Objects.isNull(metadata.statesMap.get(row.get(ImportConstants.TRANSIT_STATE_TO)))) {
			Node.setDataElement(activityNode, ImportConstants.TRANSIT_STATE_TO,
					metadata.statesMap.get(row.get(ImportConstants.TRANSIT_STATE_TO)));
			Node.setDataElement(activityNode, ImportConstants.RELATED_GC_PROCESS_REQ,
					metadata.processMap.get(row.get(ImportConstants.RELATEDINSTANCETYPE)));
			}
		}
		
	}

	@Override
	public void commit() {
		// TODO Auto-generated method stub
	}

	@Override
	public void postCommit() {
		// TODO Auto-generated method stub
	}

	@Override
	public Object getSourceId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getnode() {
		return activityNode;
	}

	public void doWork(TaskRecord collectionAccount, Map<String, String> row) {
		// TODO Auto-generated method stub

	}
}