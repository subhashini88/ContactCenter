package com.opentext.apps.cc.analytics.aggregator.util.data;

import java.util.ArrayList;
import java.util.List;

public class Trace {

    private final List<String> labels = new ArrayList<>();
    private final List<Object> values = new ArrayList<>();

    private List<String> x = new ArrayList<>();
    private List<Object> y = new ArrayList<>();

    private final ChartType type;

    private final String name;

    public Trace(ChartType type, String name) {
        this.type = type;
        this.name = name;
    }

    public ChartType getType() {
        return type;
    }

    public void addLabels(String label) {
        labels.add(label);
    }

    public void addValues(Object value) {
        values.add(value);
    }

    public List<String> getLabels() {
        return labels;
    }

    public List<Object> getValues() {
        return values;
    }

    public List<String> getX() {
        return new ArrayList<>(x);
    }

    public List<Object> getY() {
        return new ArrayList<>(y);
    }

    public void setX(List<String> x) {
        this.x = new ArrayList<>(x);
    }

    public void setY(List<Object> y) {
        this.y = new ArrayList<>(y);
    }

    public void processTrace() {
        labels.forEach(label -> {
            x.add(label);
        });
        values.forEach(label -> {
            y.add(label);
        });
    }

    @Override
    public java.lang.String toString() {
        return "[name:]" + this.name + "],{labels:" + this.labels.toString() + "},{values:" + this.values.toString() + "}";
    }
}
