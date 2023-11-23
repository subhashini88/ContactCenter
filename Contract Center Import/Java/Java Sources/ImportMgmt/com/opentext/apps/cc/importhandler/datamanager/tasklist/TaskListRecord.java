package com.opentext.apps.cc.importhandler.datamanager.tasklist;

import java.util.Map;
import java.util.Objects;

import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.internal.NOMDocumentPool;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;

public class TaskListRecord implements ImportListener {
	private int activityListNode, activitiesNode, activitiesDeleteNode;
	MetadataInitializer metadata;
	
	public TaskListRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
	}

	@Override
	public void doWork(ImportEvent event) {
		createRequest(event.getRow());
	}

	private void createRequest(Map<String, String> row) {
		if(metadata.getActivityListSet().contains(row.get(ImportConstants.CODE))) {
			metadata.getActivityListSet().remove(row.get(ImportConstants.CODE));
		}
		activityListNode = NomUtil.parseXML("<ActivityList></ActivityList>");
		Node.setDataElement(activityListNode, ImportConstants.NAME, row.get(ImportConstants.NAME));
		Node.setDataElement(activityListNode, ImportConstants.DESCRIPTION, row.get(ImportConstants.DESCRIPTION));
		Node.setDataElement(activityListNode, ImportConstants.CODE, row.get(ImportConstants.CODE));
		Node.setDataElement(activityListNode, ImportConstants.CREATIONTYPE, row.get(ImportConstants.CREATIONTYPE));
		Node.setDataElement(activityListNode, ImportConstants.RELATEDINSTANCETYPE, row.get(ImportConstants.RELATEDINSTANCETYPE));
		
		if(metadata.getActivityLists().containsKey(row.get(ImportConstants.CODE))) {
			int idNode = NOMDocumentPool.getInstance().createElement("ActivityList-id");
			Node.setDataElement(idNode, "Id", metadata.getActivityListsID().get(row.get(ImportConstants.CODE)));
			NomUtil.appendChild(idNode, activityListNode);
		}
		
		//status
		if (!Utilities.isStringEmpty(row.get(ImportConstants.STATUS))) {
			Status status = Status.getEnumObject(row.get(ImportConstants.STATUS));
			if (Objects.nonNull(status)) {
				Node.setDataElement(activityListNode, ImportConstants.STATUS, status.getValue());
			}
		}
		Node.setDataElement(activityListNode, ImportConstants.CODE, row.get(ImportConstants.CODE));
		int ActivityListsIntNode = Node.createElement("ActivityInt", activityListNode);
		int ActivityListsDelNode = Node.createElement("ActivityDel", activityListNode);
		activitiesNode = Node.createElement("Activities", ActivityListsIntNode);
		activitiesDeleteNode = Node.createElement("Activities", ActivityListsDelNode);
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
	public void addTaskDeleteNode(int activityNode) {
		Node.appendToChildren(activityNode, activitiesDeleteNode);
	}
}
