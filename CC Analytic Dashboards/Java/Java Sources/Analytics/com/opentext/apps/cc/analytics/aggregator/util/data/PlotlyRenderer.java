package com.opentext.apps.cc.analytics.aggregator.util.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.opentext.apps.cc.analytics.aggregator.util.aggregator.Aggregator;


public class PlotlyRenderer {

    private final PivotData pivotData;
    private final List<Trace> traces = new ArrayList<>();

    public PlotlyRenderer(PivotData pivotData) {
        this.pivotData = pivotData;
        processData();
    }


    private void processData() {
        pivotData.getRowKeys().forEach(rowKey -> {
            Trace trace = processTrace(rowKey);
            traces.add(trace);
        });
    }

    private Trace processTrace(String key) {
        Trace trace = new Trace(pivotData.getType(), key);
        pivotData.getColKeys().forEach(colKey -> {
            Aggregator aggregator = pivotData.getAggregator(key, colKey);
            if (Objects.isNull(aggregator)) {
                trace.addValues(0);
            } else {
                trace.addValues(aggregator.value());
            }
            trace.addLabels(colKey);
        });
        trace.setY(trace.getValues());
        trace.setX(trace.getLabels());
        return trace;
    }

    public List<Trace> getTraces() {
        return traces;
    }

}
