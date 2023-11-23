package com.opentext.apps.cc.analytics.aggregator.sql.builder;

import com.opentext.apps.cc.analytics.aggregator.sql.agg.AggregatorStrategy;
import com.opentext.apps.cc.analytics.aggregator.sql.builder.data.SearchQueryData;

public interface IQueryBuilder {

	void setAggStratergy(AggregatorStrategy aggregatorStrategy);

	void setxColumnName(String columnName);

	void setDataSet(String dataSet);

	void setyColumnName(String columnName);

	void setAggregator(String columnName);

	void setAggColumnName(String columnName);

	void setTargetWorkListStr(String targetWorkList);

	void setIsAdmin(boolean isAdmin);

	void setOffset(int offset);

	void setLimit(int limit);

	IQueryBuilder addDBType(String dbType);

	IQueryBuilder addTableName(String tableName);

	IQueryBuilder addJoinQuery(String tableName);

	IQueryBuilder addJoinQuery();

	IQueryBuilder start();

	IQueryBuilder end();

	IQueryBuilder clear();

	String getSqlQuery();

	IQueryBuilder buildQuery(SearchQueryData data);

	boolean isYColumnSelected();

	public boolean isXCustCol();

	public void setXCustCol(boolean isXCustCol);

	public boolean isYCustCol();

	public void setYCustCol(boolean isYCustCol);

	public boolean isAggCustCol();

	public void setAggCustCol(boolean isYCustCol);

}
