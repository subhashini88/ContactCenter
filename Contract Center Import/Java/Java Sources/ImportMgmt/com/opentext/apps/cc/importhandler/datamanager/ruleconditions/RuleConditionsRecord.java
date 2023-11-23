package com.opentext.apps.cc.importhandler.datamanager.ruleconditions;

import java.util.Map;
import java.util.Objects;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importhandler.datamanager.ruleconditions.ImportConstants;
import com.opentext.apps.cc.importhandler.datamanager.ruleconditions.MetadataInitializer;


public class RuleConditionsRecord implements ImportListener{
	
	public int ruleConditionNode;
	private final MetadataInitializer metadata;
	private final ReportItem reportItem;
	
	public RuleConditionsRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
		this.reportItem = reportItem;
	}
	
	@Override
	public void doWork(ImportEvent event) {

	}

	@Override
	public void commit() {

	}

	@Override
	public void postCommit() {
	}

	@Override
	public Object getSourceId() {
		return null;
	}

	@Override
	public int getnode() {
		return this.ruleConditionNode;
	}
	
	protected void createRuleConditionIfnotExists(ImportEvent event) {

		int createNode = 0, ruleConditionResponse = 0;
		Map<String, String> row = event.getRow();

		try {
			if (Objects.nonNull(row)
					&& !metadata.getAllRuleConditionsMap().containsKey(row.get(ImportConstants.DISPLAYNAME))) {
				SOAPRequestObject createRuleCondition = new SOAPRequestObject(
						"http://schemas/OpenTextCCConfigurableWorkflow/RuleConditionOperand/operations",
						"CreateRuleConditionOperand", null, null);
				createNode = NomUtil.parseXML("<RuleConditionOperand-create></RuleConditionOperand-create>");
				Node.setDataElement(createNode, "Name", row.get(ImportConstants.RULENAME));
				Node.setDataElement(createNode, "DisplayName", row.get(ImportConstants.DISPLAYNAME));
				Node.setDataElement(createNode, "DataType", row.get(ImportConstants.DATATYPE));
				Node.setDataElement(createNode, "Path", row.get(ImportConstants.PATH));
				Node.setDataElement(createNode, "Xpath", row.get(ImportConstants.Xpath));
				Node.setDataElement(createNode, "Type", row.get(ImportConstants.TYPE));
				Node.setDataElement(createNode, "Purpose", row.get(ImportConstants.PURPOSE));
				Node.setDataElement(createNode, "IsMultiField", row.get(ImportConstants.ISMULTIVAL).toUpperCase());
				Node.setDataElement(createNode, "CreationType", Objects.isNull(row.get(ImportConstants.CREATIONTYPE).toUpperCase())?"":row.get(ImportConstants.CREATIONTYPE).toUpperCase());
				createRuleCondition.addParameterAsXml(createNode);
				ruleConditionResponse = createRuleCondition.sendAndWait();
				String itemId = Node.getDataWithDefault(NomUtil.getNode(
						".//RuleConditionOperand/RuleConditionOperand-id/ItemId", ruleConditionResponse), null);
				if (Objects.isNull(itemId)) {
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
					row.put(ImportConstants.STATUS_LOG, Node
							.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", ruleConditionResponse), null));
				} else {
					metadata.addRuleConditionToMap(row.get(ImportConstants.RULENAME), itemId);
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
					row.put(ImportConstants.STATUS_LOG, "Rule creation is success.");
				}
			} else {
				// Update existing as corresponding Display name already exists
				SOAPRequestObject updateRuleCondition = new SOAPRequestObject(
						"http://schemas/OpenTextCCConfigurableWorkflow/RuleConditionOperand/operations",
						"UpdateRuleConditionOperand", null, null);
				int idNode = 0;
				idNode = NomUtil.parseXML("<RuleConditionOperand-id></RuleConditionOperand-id>");
				Node.setDataElement(idNode, "ItemId",
						metadata.getAllRuleConditionsMap().get(row.get(ImportConstants.DISPLAYNAME)));

				createNode = NomUtil.parseXML("<RuleConditionOperand-update></RuleConditionOperand-update>");
				Node.setDataElement(createNode, "Name", row.get(ImportConstants.RULENAME));
				Node.setDataElement(createNode, "DataType", row.get(ImportConstants.DATATYPE));
				Node.setDataElement(createNode, "Path", row.get(ImportConstants.PATH));
				Node.setDataElement(createNode, "Xpath", row.get(ImportConstants.Xpath));
				Node.setDataElement(createNode, "Type", row.get(ImportConstants.TYPE));
				Node.setDataElement(createNode, "Purpose", row.get(ImportConstants.PURPOSE));
				Node.setDataElement(createNode, "IsMultiField", row.get(ImportConstants.ISMULTIVAL).toUpperCase());
				updateRuleCondition.addParameterAsXml(idNode);
				updateRuleCondition.addParameterAsXml(createNode);
				ruleConditionResponse = updateRuleCondition.sendAndWait();
				String itemId = Node.getDataWithDefault(NomUtil.getNode(
						".//RuleConditionOperand/RuleConditionOperand-id/ItemId", ruleConditionResponse), null);
				if (Objects.isNull(itemId)) {
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
					row.put(ImportConstants.STATUS_LOG, Node
							.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", ruleConditionResponse), null));
				} else {
					row.put(ImportConstants.STATUS, ImportConstants.RECORD_EXISTS);
					row.put(ImportConstants.STATUS_LOG, "Record already exists");
				}
			}
		} catch (Exception e) {
			row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
			row.put(ImportConstants.STATUS_LOG, e.getMessage());
		}
	}

}
