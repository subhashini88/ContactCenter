package com.opentext.apps.cc.custom.querybuilder.builder.data;

import java.util.LinkedList;
import java.util.List;

public final class ContainerNode implements IDataNode {

	public final List<IDataNode> childNodes = new LinkedList<>();

	public ContainerNode() {
	}

	public void addNode(IDataNode node) {
		childNodes.add(node);
	}

}
