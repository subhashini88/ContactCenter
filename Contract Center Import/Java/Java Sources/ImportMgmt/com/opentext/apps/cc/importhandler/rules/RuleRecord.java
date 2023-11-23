package com.opentext.apps.cc.importhandler.rules;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;

public class RuleRecord implements ImportListener {
	private int ruleNode, conditionsNode, conditionSize = 0;
	MetadataInitializer metadata;
	private String ruleLogic = null;
	private String legacyId=null;

	public RuleRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
	}

	@Override
	public void doWork(ImportEvent event) {
		createRequest(event.getRow());
	}

	private void createRequest(Map<String, String> row) {
		ruleNode = NomUtil.parseXML("<Rule></Rule>");
		Node.setDataElement(ruleNode, "Name", row.get(ImportConstants.RULE_NAME));
		Node.setDataElement(ruleNode, "Description", row.get(ImportConstants.DESCRIPTION));
		boolean tasklistExists=false;
		if(Objects.nonNull(row.get(ImportConstants.TASK_LISTID))) {
			if(Objects.nonNull(metadata.tasklistIdMap.get(row.get(ImportConstants.TASK_LISTID)))) {
				Node.setDataElement(ruleNode, "RelatedActivityList",
						metadata.tasklistIdMap.get(row.get(ImportConstants.TASK_LISTID)));
			}
			else {
				Node.setDataElement(ruleNode, "RelatedActivityList",
						metadata.tasklistMap.get(row.get(ImportConstants.TASK_LIST)));
			}
		}
		else {
			Node.setDataElement(ruleNode, "RelatedActivityList",
					metadata.tasklistMap.get(row.get(ImportConstants.TASK_LIST)));
		}
		Node.setDataElement(ruleNode, "RelatedOrganization",
				metadata.orgMap.get(row.get(ImportConstants.ORGANIZATION_CODE)));
		Node.setDataElement(ruleNode, "RelatedGCType",
				metadata.contracttypeMap.get(row.get(ImportConstants.CONTRACT_TYPE)));
		Node.setDataElement(ruleNode, "RelatedGCProcess", metadata.gcprocessMap.get("Contract"));
		Node.setDataElement(ruleNode, "RelatedGCState",
				metadata.gcStateMap.get(row.get(ImportConstants.CONTRACT_STATE)));
		Node.setDataElement(ruleNode, "RelatedGCAction",
				metadata.gcprocessActionsMap.get("SendForReview"));
		Node.setDataElement(ruleNode, "Logic", row.get(ImportConstants.RULE_LOGIC));
		this.ruleLogic = row.get(ImportConstants.RULE_LOGIC);
		if (!Utilities.isStringEmpty(row.get(ImportConstants.STATUS))) {
			Status status = Status.getEnumObject(row.get(ImportConstants.STATUS));
			if (Objects.nonNull(status)) {
				Node.setDataElement(ruleNode, ImportConstants.STATUS, status.getValue());
			}
		}
		Node.setDataElement(ruleNode, ImportConstants.LEGACY_ID, row.get(ImportConstants.LEGACY_ID));
		this.legacyId=row.get(ImportConstants.LEGACY_ID);
		conditionsNode = Node.createElement("RuleConditions", ruleNode);
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
		return ruleNode;
	}

	public void doWork(RuleRecord collectionAccount, Map<String, String> row) {
		// TODO Auto-generated method stub

	}

	public void addConditionNode(ImportListener conditionNode) {

		Node.appendToChildren(conditionNode.getnode(), conditionsNode);
		conditionSize++;
	}


	public boolean isValidRuleLogic(TreeMap<String,ImportListener> conditions) {
		if (Objects.isNull(ruleLogic) || ruleLogic.isBlank() || conditions.size()==0)
			return false;
		for (int i = 1; i <= conditions.size(); i++) {
			if (!(ruleLogic.contains(String.valueOf(i)) && conditions.containsKey(String.valueOf(i)))) {
				return false;
			}
		}
		return true;
	}

	public String getlegacyId() {
		// TODO Auto-generated method stub
		return legacyId;
	}
	
}
