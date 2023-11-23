package com.opentext.apps.cc.analytics.aggregator.sql.dao;

import com.cordys.cpc.bsf.busobject.BusObject;
import com.cordys.cpc.bsf.busobject.BusObjectIterator;
import com.cordys.cpc.bsf.busobject.QueryObject;

public class CCCustomJsonDao {

	public String getDbJsonObjects(String sql) {
		String queryText = "";
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
				StringBuffer columnBuff = new StringBuffer();
				for (int i = 0; i < list.getLength(); i++) {
					node = list.item(i);
					String nodeName = node.getNodeName();
					data = node.getTextContent();
					if (nodeName.toLowerCase().contains("opentext")) {
						nodeName = node.getFirstChild().getNodeName();
					}
					if (null != data) {
						columnBuff.append("\"" + nodeName + "\"" + ":\"" + data.replaceAll("\"", "") + "\",");
					} else {
						columnBuff.append("\"" + nodeName + "\"" + ":" + data + ",");
					}
				}
				buffer.append(
						"{ " + columnBuff.toString().substring(0, columnBuff.toString().lastIndexOf(",")) + " } ,");
			}
		} catch (Exception e) {
		}
		if (buffer.toString().isEmpty()) {
			return "[]";
		} else {
			return "[" + buffer.toString().substring(0, buffer.toString().lastIndexOf(",")) + "]";
		}
	}

	public int getDataTotalCount(String sql) {
		String queryText = "select count(1) as total from (	" + sql + ") as tab  ";
		String data = null;
		int totalCount = 0;
		try {
			QueryObject query = new QueryObject(queryText);
			BusObjectIterator<BusObject> contractJsons = query.getObjects();
			while (contractJsons.hasMoreElements()) {
				BusObject contract = contractJsons.nextElement();
				org.w3c.dom.NodeList list = contract.getObjectData().getChildNodes();
				org.w3c.dom.Node node = null;
				data = null;
//				for (int i = 0; i < list.getLength(); i++) {
				node = list.item(0);
				data = node.getTextContent();
				totalCount = Integer.parseInt(data);
//					break;
//				}
			}
		} catch (Exception e) {
		}
		return totalCount;
	}

}
