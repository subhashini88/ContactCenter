package com.opentext.apps.cc.importhandler.datamanager.ruleconditions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.custom.Utilities;

public class MetadataInitializer {

	private Map<String, String> allRuleConditions;
	
	public MetadataInitializer() {
		allRuleConditions = new HashMap<>();
		loadAllRuleConditions();
	}
	
	public Map<String, String> getAllRuleConditionsMap()
	{
		return allRuleConditions;
	}
	
	public void addRuleConditionToMap(String ruleName,String itemId)
	{
		if(null != ruleName && null != itemId)
		allRuleConditions.put(ruleName, itemId);
	}
	
	private void loadAllRuleConditions() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject typesRequest = new SOAPRequestObject("http://schemas/OpenTextCCConfigurableWorkflow/RuleConditionOperand/operations", "GetAllRuleConditionOperands",null, null);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RuleConditionOperand", response);
			for (int i : nodes) 
			{
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//RuleConditionOperand-id/ItemId", i), null);
				String ruleName = Node.getDataWithDefault(NomUtil.getNode(".//DisplayName", i), null);
				
				if (null != ruleName && null != itemId)
				{
					allRuleConditions.put(ruleName, itemId);
				}
			}
		}
		finally 
		{
			if (null != nodes) 
			{
				Utilities.cleanAll(response);
			}
		}
	}
}
