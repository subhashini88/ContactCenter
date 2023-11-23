package com.opentext.apps.cc.analytics.aggregator.sql.builder.data;

import java.util.LinkedList;
import java.util.List;

public class SearchQueryData {

	public final List<IDataNode> containers = new LinkedList<>();

	public void addContainerNode(IDataNode dataNode) {
		containers.add(dataNode);
	}

}
