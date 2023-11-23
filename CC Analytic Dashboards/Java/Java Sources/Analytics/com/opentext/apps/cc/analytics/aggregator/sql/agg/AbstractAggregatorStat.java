package com.opentext.apps.cc.analytics.aggregator.sql.agg;

import java.util.Objects;

import com.opentext.apps.cc.analytics.aggregator.sql.builder.AbstractQueryBuilder;
import com.opentext.apps.cc.analytics.aggregator.sql.builder.DBType;

public abstract class AbstractAggregatorStat implements AggregatorStrategy {

	protected DBType dbType = null;

	public AbstractAggregatorStat(DBType dbType) {
		this.dbType = dbType;
	}

	@Override
	public DBType getDbType() {
		return this.dbType;
	}

	protected String prepareYCol(AbstractQueryBuilder queryBuilder) {
		return hasYCol(queryBuilder) ? (queryBuilder.getyColumnName() + " as ycol,") : "";
	}

	protected String prepareYColGroup(AbstractQueryBuilder queryBuilder) {
		return hasYCol(queryBuilder) ? ", " + queryBuilder.getyColumnName() : "";
	}

	private boolean hasYCol(AbstractQueryBuilder queryBuilder) {
		return !(Objects.isNull(queryBuilder.getyColumnName())
				|| "--select--".equalsIgnoreCase(queryBuilder.getyColumnName()));
	}

}
