package com.opentext.apps.cc.analytics.aggregator.util.aggregator;

import com.opentext.apps.cc.analytics.aggregator.util.data.RowData;
import com.opentext.apps.cc.analytics.aggregator.util.formatter.Formatter;

public interface Aggregator<T> {

	T push(RowData data);

	T value();

	Formatter formatter();
}
