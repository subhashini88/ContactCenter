package com.opentext.apps.cc.analytics.aggregator.util.report;

import java.util.List;

import com.opentext.apps.cc.analytics.aggregator.util.aggregator.factory.AggregatorType;
import com.opentext.apps.cc.analytics.aggregator.util.data.ChartType;
import com.opentext.apps.cc.analytics.aggregator.util.data.InputData;
import com.opentext.apps.cc.analytics.aggregator.util.data.PivotData;
import com.opentext.apps.cc.analytics.aggregator.util.data.PlotlyRenderer;
import com.opentext.apps.cc.analytics.aggregator.util.data.Trace;

public class ReportPivotProcessEngine {

	private List<Trace> traces = null;

	public List<Trace> processReport(InputData data, AggregatorType aggType, ChartType chartType, List<String> rowAttrs,
			List<String> colAttrs, List<String> aggCols) {
		PivotData pivotData = new PivotData(data, aggType, chartType, rowAttrs, colAttrs, aggCols);
		PlotlyRenderer renderer = new PlotlyRenderer(pivotData);
		traces = renderer.getTraces();
		return traces;
	}

	public List<Trace> getTraces() {
		return traces;
	}

}
