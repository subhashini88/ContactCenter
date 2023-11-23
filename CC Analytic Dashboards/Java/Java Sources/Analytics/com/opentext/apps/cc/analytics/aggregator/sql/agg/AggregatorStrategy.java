package com.opentext.apps.cc.analytics.aggregator.sql.agg;

import com.opentext.apps.cc.analytics.aggregator.sql.builder.AbstractQueryBuilder;
import com.opentext.apps.cc.analytics.aggregator.sql.builder.DBType;

public interface AggregatorStrategy {

	DBType getDbType();

	String buildQuery(AbstractQueryBuilder queryBuilder, String workList);

}
