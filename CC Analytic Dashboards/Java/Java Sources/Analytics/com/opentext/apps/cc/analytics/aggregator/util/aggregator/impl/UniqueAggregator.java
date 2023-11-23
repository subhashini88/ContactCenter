package com.opentext.apps.cc.analytics.aggregator.util.aggregator.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.opentext.apps.cc.analytics.aggregator.util.aggregator.Aggregator;
import com.opentext.apps.cc.analytics.aggregator.util.data.InputData;
import com.opentext.apps.cc.analytics.aggregator.util.data.RowData;
import com.opentext.apps.cc.analytics.aggregator.util.formatter.Formatter;

public class UniqueAggregator implements Aggregator<Set<Object>> {

    private final InputData data;
    private final List<String> rowKeys;
    private final List<String> colKeys;
    private Integer count = 0;

    private final Set<Object> uniques = new LinkedHashSet<>();

    private final List<String> aggCol;

    public UniqueAggregator(InputData data, List<String> rowKeys, List<String> colKeys, List<String> aggCol) {
        super();
        this.data = data;
        this.rowKeys = new ArrayList<>(rowKeys);
        this.colKeys = new ArrayList<>(colKeys);
        this.aggCol = new ArrayList<>(aggCol);
    }

    public Set<Object> push(RowData data) {
        uniques.add(data.getValue(aggCol.get(0)));
        return uniques;
    }

    public Set<Object> value() {
        return uniques;
    }

    public Formatter formatter() {
        return null;
    }

}
