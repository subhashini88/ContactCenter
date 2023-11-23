package com.opentext.apps.cc.importhandler.datamanager.rules;

import java.util.Map;
import java.util.Objects;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;

public class DefaultRulesRecord implements ImportListener {

	public int ruleNode;
	private final MetadataInitializer metadata;
	private final ReportItem reportItem;

	public DefaultRulesRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
		this.reportItem = reportItem;
	}

	@Override
	public void doWork(ImportEvent event) {
		// TODO Auto-generated method stub

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
		return null;
	}

	@Override
	public int getnode() {

		return this.ruleNode;
	}

	protected void createRuleIfNotExists(ImportEvent event) {
		Map<String, String> row = event.getRow();
		ruleNode = NomUtil.parseXML("<Rule></Rule>");
		Node.setDataElement(ruleNode, "RuleName", row.get(ImportConstants.RULE_NAME));
		Node.setDataElement(ruleNode, "Description", row.get(ImportConstants.DESCRIPTION));
		Node.setDataElement(ruleNode, "RuleOrder", row.get(ImportConstants.RULE_ORDER));
		if (Objects.nonNull(metadata.getRuleByCodeName(row.get(ImportConstants.CODE)))) {
			Node.setDataElement(ruleNode, "RuleItemId", metadata.getRuleByCodeName(row.get(ImportConstants.CODE)));
		}
		Node.setDataElement(ruleNode, "TaskListCode", row.get(ImportConstants.TASK_LISTCODE));
		Node.setDataElement(ruleNode, "Code", row.get(ImportConstants.CODE));
		Node.setDataElement(ruleNode, "CreationType", row.get(ImportConstants.CREATIONTYPE));
		Node.setDataElement(ruleNode, "RelatedInstanceType", row.get(ImportConstants.RELATED_INSTANCE_TYPE));
		Node.setDataElement(ruleNode, "RelatedGCState", metadata
				.getRelatedStateItemId1(row.get(ImportConstants.PROCESS_NAME), row.get(ImportConstants.RELATED_STATE)));
		Node.setDataElement(ruleNode, "RelatedGCProcess",
				metadata.gcprocessMap.get(row.get(ImportConstants.PROCESS_NAME)));
		Node.setDataElement(ruleNode, "RelatedGCAction", metadata.getRelatedActionItemId1(
				row.get(ImportConstants.PROCESS_NAME), row.get(ImportConstants.RELATED_ACTION)));
		if (!Utilities.isStringEmpty(row.get(ImportConstants.STATUS))) {
			Status status = Status.getEnumObject(row.get(ImportConstants.STATUS));
			if (Objects.nonNull(status)) {
				Node.setDataElement(ruleNode, ImportConstants.STATUS, status.getValue());
			}
		}
		if (Objects.nonNull(metadata.duplicateRules)) {
			if (metadata.duplicateRules.containsKey(row.get(ImportConstants.CODE))) {
				metadata.duplicateRules.remove(row.get(ImportConstants.CODE));
			}
		}

	}

}
