package com.opentext.apps.cc.importhandler.tasklist;

import java.util.Map;
import java.util.Objects;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;

public class TaskListRecord implements ImportListener {
	private int activityListNode, activitiesNode;
	MetadataInitializer metadata;
	
	public TaskListRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
	}

	@Override
	public void doWork(ImportEvent event) {
		createRequest(event.getRow());
	}

	private void createRequest(Map<String, String> row) {
		activityListNode = NomUtil.parseXML("<ActivityList></ActivityList>");
		Node.setDataElement(activityListNode, ImportConstants.NAME, row.get(ImportConstants.NAME));
		Node.setDataElement(activityListNode, ImportConstants.DESCRIPTION, row.get(ImportConstants.DESCRIPTION));
		Node.setDataElement(activityListNode, ImportConstants.RELATEDINSTANCETYPE, "Contract");
		
		//status
		if (!Utilities.isStringEmpty(row.get(ImportConstants.STATUS))) {
			Status status = Status.getEnumObject(row.get(ImportConstants.STATUS));
			if (Objects.nonNull(status)) {
				Node.setDataElement(activityListNode, ImportConstants.STATUS, status.getValue());
			}
		}
		Node.setDataElement(activityListNode, ImportConstants.TASKLIST_LEGACYID, row.get(ImportConstants.TASKLIST_LEGACYID));
		activitiesNode = Node.createElement("Activities", activityListNode);
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
		return activityListNode;
	}

	public void doWork(TaskRecord collectionAccount, Map<String, String> row) {
		// TODO Auto-generated method stub
	}

	public void addTaskNode(ImportListener activityNode) {
		Node.appendToChildren(activityNode.getnode(), activitiesNode);
	}
}
