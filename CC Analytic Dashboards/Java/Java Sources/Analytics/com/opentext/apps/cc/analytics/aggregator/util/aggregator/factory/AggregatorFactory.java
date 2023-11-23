package com.opentext.apps.cc.analytics.aggregator.util.aggregator.factory;

import java.util.List;
import java.util.Objects;

import com.opentext.apps.cc.analytics.aggregator.util.aggregator.Aggregator;
import com.opentext.apps.cc.analytics.aggregator.util.aggregator.impl.CountAggregator;
import com.opentext.apps.cc.analytics.aggregator.util.aggregator.impl.SumAggregator;
import com.opentext.apps.cc.analytics.aggregator.util.data.InputData;

public class AggregatorFactory {

	public static Aggregator<?> getAggregator(AggregatorType type, InputData data, List<String> rowKeys,
			List<String> colKeys, List<String> aggCols) {
		Aggregator<?> aggregator = null;
		if (Objects.isNull(type)) {
			return aggregator;
		}
		if (AggregatorType.COUNT.equals(type)) {
			aggregator = new CountAggregator(data, rowKeys, colKeys, aggCols);
		} else if (AggregatorType.SUM.equals(type)) {
			aggregator = new SumAggregator(aggCols);
		}
		return aggregator;
	}

}
