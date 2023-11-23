package com.opentext.apps.cc.custom.querybuilder.builder.data;

import java.util.LinkedList;
import java.util.List;

public class SearchQueryData {

	public static final String COMPLIANCE_STATUS = "compliance_status";

	private boolean hasComplainceStatus = false;

	public final List<IDataNode> containers = new LinkedList<>();

	public void addContainerNode(IDataNode dataNode) {
		containers.add(dataNode);
	}

	public boolean isHasComplainceStatus() {
		return hasComplainceStatus;
	}

	public void setHasComplainceStatus(boolean hasComplainceStatus) {
		if (hasComplainceStatus)
			this.hasComplainceStatus = hasComplainceStatus;
	}

}
