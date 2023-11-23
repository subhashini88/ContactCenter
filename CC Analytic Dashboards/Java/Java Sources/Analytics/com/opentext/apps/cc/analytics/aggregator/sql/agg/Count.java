package com.opentext.apps.cc.analytics.aggregator.sql.agg;

import java.util.Objects;

import com.opentext.apps.cc.analytics.aggregator.sql.builder.AbstractQueryBuilder;
import com.opentext.apps.cc.analytics.aggregator.sql.builder.DBType;

public class Count extends AbstractAggregatorStat {

	public Count(DBType dbType) {
		super(dbType);
	}

	@Override
	public String buildQuery(AbstractQueryBuilder queryBuilder, String workList) {

		if (Objects.isNull(queryBuilder.getJoinQuery())) {
			return "select  " + queryBuilder.getxColumnName() + " as xcol," + prepareYCol(queryBuilder) + " count(  "
					+ queryBuilder.getAggColumnName() + ") as agg from " + queryBuilder.getTableName() + " where 1=1 "
					+ workList + " group by " + queryBuilder.getxColumnName() + prepareYColGroup(queryBuilder);
		}

		return "select  " + queryBuilder.getxColumnName() + " as xcol," + prepareYCol(queryBuilder) + " count(  "
				+ queryBuilder.getAggColumnName() + ") as agg from (" + queryBuilder.getJoinQuery()
				+ ") as tab  where 1=1 " + workList + " group by " + queryBuilder.getxColumnName()
				+ prepareYColGroup(queryBuilder);
	}

}
