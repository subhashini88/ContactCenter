package com.opentext.apps.cc.upgradeutils.dao;

import com.cordys.cpc.bsf.busobject.BusObject;
import com.cordys.cpc.bsf.busobject.BusObjectIterator;
import com.cordys.cpc.bsf.busobject.QueryObject;

public class CCUpgradeUtilsDao {

	private static final String contract_json_sql = "select info as contractsJson FROM ContractsJSONB WHERE info ->> 'contractName' like '%TR-Contract%' and info -> 'Custom attributes'->> 'cdf' like '%22323%' ;\r\n";

	public String getContractObjects(String sql) {
		String queryText = contract_json_sql;
		StringBuffer buffer = new StringBuffer();
		String data = null;
		try {
			if (null != sql && !sql.isEmpty()) {
				queryText = sql;
			}
			QueryObject query = new QueryObject(queryText);
			BusObjectIterator<BusObject> contractJsons = query.getObjects();

			while (contractJsons.hasMoreElements()) {
				BusObject contract = contractJsons.nextElement();
				org.w3c.dom.NodeList list = contract.getObjectData().getChildNodes();
				org.w3c.dom.Node node = null;
				data = null;
				for (int i = 0; i < list.getLength(); i++) {
					node = list.item(i);
					data = node.getTextContent();
					buffer.append(data + ",");
				}
			}
		} catch (Exception e) {
		}
		return "[" + buffer.toString().substring(0, buffer.toString().lastIndexOf(",")) + "]";
	}

}
