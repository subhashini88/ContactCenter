package com.opentext.apps.cc.importhandler.rules;

import java.util.Map;
import java.util.Objects;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;

public class RuleConditionRecord implements ImportListener {
	int conditionNode;
	MetadataInitializer metadata;

	public RuleConditionRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
	}

	@Override
	public void doWork(ImportEvent event) {
		createRequest(event.getRow());
	}

	private void createRequest(Map<String, String> row) {
		conditionNode = NomUtil.parseXML("<RuleCondition></RuleCondition>");
		Node.setDataElement(conditionNode, "RuleLeftOperand", metadata.contractpropertyIdMap.get(row.get(ImportConstants.CONTRACT_PROPERTY)));
		Node.setDataElement(conditionNode, "Operator", row.get(ImportConstants.CONTRACT_OPERATOR));
		Node.setDataElement(conditionNode, "Value", row.get(ImportConstants.PROPERTY_VALUE));
		Node.setDataElement(conditionNode, "Order",row.get(ImportConstants.ORDER));
		
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
		return conditionNode;
	}

	public void doWork(RuleConditionRecord collectionAccount, Map<String, String> row) {
		// TODO Auto-generated method stub

	}

	

	
}
