package com.opentext.apps.cc.analytics.aggregator.util.data;

import java.util.ArrayList;
import java.util.List;

public class InputData {

	private final List<RowData> data = new ArrayList<>();

	public void addRow(RowData rowData) {
		data.add(rowData);
	}

	public List<RowData> getAllRows() {
		return data;
	}

}
