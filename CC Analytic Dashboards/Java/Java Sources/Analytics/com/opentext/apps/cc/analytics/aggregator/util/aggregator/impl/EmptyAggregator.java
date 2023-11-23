package com.opentext.apps.cc.analytics.aggregator.util.aggregator.impl;

import java.util.ArrayList;
import java.util.List;

import com.opentext.apps.cc.analytics.aggregator.util.aggregator.Aggregator;
import com.opentext.apps.cc.analytics.aggregator.util.data.InputData;
import com.opentext.apps.cc.analytics.aggregator.util.data.RowData;
import com.opentext.apps.cc.analytics.aggregator.util.formatter.Formatter;

public class EmptyAggregator implements Aggregator<Integer> {

	private final InputData data;
	private final List<String> rowKeys;
	private final List<String> colKeys;
	private Integer count = 0;
	private final List<String> aggCol;

	public EmptyAggregator(InputData data) {
		super();
		this.data = data;
		this.rowKeys = new ArrayList<>();
		this.colKeys = new ArrayList<>();
		this.aggCol = new ArrayList<>();
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
