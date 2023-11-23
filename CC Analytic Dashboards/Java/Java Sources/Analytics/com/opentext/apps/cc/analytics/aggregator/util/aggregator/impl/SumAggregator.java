package com.opentext.apps.cc.analytics.aggregator.util.aggregator.impl;

import java.util.ArrayList;
import java.util.List;

import com.opentext.apps.cc.analytics.aggregator.util.aggregator.Aggregator;
import com.opentext.apps.cc.analytics.aggregator.util.data.RowData;
import com.opentext.apps.cc.analytics.aggregator.util.formatter.Formatter;

public class SumAggregator implements Aggregator<Double> {

    private Double sum = 0.0;
    private final List<String> aggCol;

    public SumAggregator(List<String> aggCol) {
        super();
        this.aggCol = new ArrayList<>(aggCol);
    }

    public Double push(RowData rowData) {
        Object val = rowData.getValue(aggCol.get(0));
        try {
            sum += Double.parseDouble(val.toString());
        } catch (Exception e) {
            sum += 0.0f;
        }

        return sum;
    }

    public Double value() {
        return sum;
    }

    public Formatter formatter() {
        return null;
    }

}
