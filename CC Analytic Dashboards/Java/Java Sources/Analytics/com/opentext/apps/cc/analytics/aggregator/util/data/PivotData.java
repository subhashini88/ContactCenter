package com.opentext.apps.cc.analytics.aggregator.util.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.opentext.apps.cc.analytics.aggregator.util.aggregator.Aggregator;
import com.opentext.apps.cc.analytics.aggregator.util.aggregator.factory.AggregatorFactory;
import com.opentext.apps.cc.analytics.aggregator.util.aggregator.factory.AggregatorType;
import com.opentext.apps.cc.analytics.aggregator.util.aggregator.impl.EmptyAggregator;

public class PivotData {

    private final InputData input;
    private final List<String> rowAttrs;
    private final List<String> colAttrs;
    private final List<String> aggCols;
    private final List<String> rowKeys = new ArrayList<>();
    private final List<String> colKeys = new ArrayList<>();
    private final AggregatorType aggregatorName;
    private final Aggregator allTotal;
    private final Map<String, Aggregator> rowTotals = new HashMap<>();
    private final Map<String, Aggregator> colTotals = new HashMap<>();
    private final Map<String, Map<String, Aggregator>> tree = new HashMap<>();
    private final ChartType type;

    public PivotData(InputData input, AggregatorType aggregatorName, ChartType type, List<String> rowAttrs, List<String> colAttrs, List<String> aggCols) {
        this.input = input;
        this.aggregatorName = aggregatorName;
        this.aggCols = new ArrayList<>(aggCols);
        this.type = type;
        this.colAttrs = new ArrayList<>(colAttrs);
        this.rowAttrs = new ArrayList<>(rowAttrs);
        allTotal = new EmptyAggregator(input);
        processData();
    }

    private void processData() {
        forEachRecord();
    }

    private void forEachRecord() {
        input.getAllRows().forEach(row -> {
            processRecord(row);
        });
    }

    public ChartType getType() {
        return type;
    }

    public Aggregator getAggregator(String rowKey, String colKey) {
        Aggregator agg = new EmptyAggregator(input);
        String flatRowKey = rowKey;
        String flatColKey = colKey;
        if (rowKey.isBlank() && colKey.isBlank()) {
            agg = this.allTotal;
        } else if (rowKey.isBlank()) {
            agg = this.colTotals.get(flatColKey);
        } else if (colKey.isBlank()) {
            agg = this.rowTotals.get(flatRowKey);
        } else {
            agg = this.tree.get(flatRowKey).get(flatColKey);
        }

        return agg;
    }

    public List<String> getColKeys() {
        return colKeys;
    }

    public List<String> getRowKeys() {
        return rowKeys;
    }

    private void processRecord(RowData row) {
        Object ref1, ref3;
        List<String> colKey = new ArrayList<>();
        List<String> rowKey = new ArrayList<>();
        List<String> ref = this.colAttrs;
        for (int index = 0, len1 = ref.size(); index < len1; index++) {
            String x = ref.get(index);
            colKey.add((ref1 = row.getValue(x)) != null ? ref1.toString() : "null");
        }
        List<String> ref2 = this.rowAttrs;
        for (int index = 0, len2 = ref2.size(); index < len2; index++) {
            String x = ref2.get(index);
            rowKey.add((ref3 = row.getValue(x)) != null ? ref3.toString() : "null");
        }
        String flatRowKey = rowKey.get(0).toString();
        String flatColKey = colKey.get(0).toString();
        this.allTotal.push(row);
        if (!rowKey.isEmpty()) {
            if (Objects.isNull(this.rowTotals.get(flatRowKey))) {
                this.rowKeys.add(rowKey.get(0));
                this.rowTotals.put(flatRowKey, this.getAggregator(rowKey, new ArrayList<>()));
            }
            this.rowTotals.get(flatRowKey).push(row);
        }

        if (!colKey.isEmpty()) {
            if (Objects.isNull(this.colTotals.get(flatColKey))) {
                this.colKeys.add(colKey.get(0));
                this.colTotals.put(flatColKey, this.getAggregator(new ArrayList<>(), colKey));
            }
            this.colTotals.get(flatColKey).push(row);
        }
        if (!colKey.isEmpty() && !rowKey.isEmpty()) {
            if (Objects.isNull(this.tree.get(flatRowKey))) {
                this.tree.put(flatRowKey, new HashMap<>());
            }
            if (Objects.isNull(this.tree.get(flatRowKey).get(flatColKey))) {
                this.tree.get(flatRowKey).put(flatColKey, this.getAggregator(rowKey, colKey));
            }
            this.tree.get(flatRowKey).get(flatColKey).push(row);
        }
    }

    private Aggregator getAggregator(List<String> colKeys, List<String> rowKeys) {
        return AggregatorFactory.getAggregator(aggregatorName, input, rowKeys, colKeys, aggCols);
    }

}
