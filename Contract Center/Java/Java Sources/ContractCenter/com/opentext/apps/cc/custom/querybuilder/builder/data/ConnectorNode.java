package com.opentext.apps.cc.custom.querybuilder.builder.data;

public final class ConnectorNode implements IDataNode {
	public boolean isFirstConnector = false;
	public final Connector connector;

	public ConnectorNode(Connector connector) {
		this.connector = connector;
	}

	public ConnectorNode(boolean isFirstConnector, Connector connector) {
		super();
		this.isFirstConnector = isFirstConnector;
		this.connector = connector;
	}

}
