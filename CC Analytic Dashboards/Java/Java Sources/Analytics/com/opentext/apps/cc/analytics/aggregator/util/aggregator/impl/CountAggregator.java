package com.opentext.apps.cc.analytics.aggregator.util.aggregator.impl;

import java.util.ArrayList;
import java.util.List;

import com.opentext.apps.cc.analytics.aggregator.util.aggregator.Aggregator;
import com.opentext.apps.cc.analytics.aggregator.util.data.InputData;
import com.opentext.apps.cc.analytics.aggregator.util.data.RowData;
import com.opentext.apps.cc.analytics.aggregator.util.formatter.Formatter;

public class CountAggregator implements Aggregator<Integer> {

	private final InputData data;
	private final List<String> rowKeys;
	private final List<String> colKeys;
	private Integer count = 0;
	private final List<String> aggCol;

	public CountAggregator(InputData data, List<String> rowKeys, List<String> colKeys, List<String> aggCol) {
		super();
		this.data = data;
		this.rowKeys = new ArrayList<>(rowKeys);
		this.colKeys = new ArrayList<>(colKeys);
		this.aggCol = new ArrayList<>(aggCol);
	}

	public Integer push(RowData data) {
		return count++;
	}

	public Integer value() {
		return count;
	}

	public Formatter formatter() {
		return null;
	}

}
