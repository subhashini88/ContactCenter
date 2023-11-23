package com.opentext.apps.cc.custom.querybuilder.builder;

import com.opentext.apps.cc.custom.querybuilder.builder.data.SearchQueryData;

public interface IQueryBuilder {

	void setJsonColumnName(String columnName);

	void setJsonCustomColumnName(String columnName);

	void setJsonFixedColumnName(String columnName);

	void setOffset(int offset);

	void setLimit(int limit);

	IQueryBuilder addDBType(String dbType);

	IQueryBuilder addTableName(String tableName);

	IQueryBuilder addOrgId(String orgId);

	IQueryBuilder start();

	IQueryBuilder and();

	IQueryBuilder or();

	IQueryBuilder contains();

	IQueryBuilder addOperand(String operandName);

	void addOperand(String operandName, StringBuffer buffer);

	IQueryBuilder addOperator(String operator);

	void addOperator(String operator, StringBuffer buffer);

	IQueryBuilder addOperatorValue(String value);

	void addOperatorValue(String value, StringBuffer buffer);

	IQueryBuilder end();

	IQueryBuilder clear();

	String getSqlQuery();

	IQueryBuilder buildQuery(SearchQueryData data);

	void addOperand(String operandName, StringBuffer buffer, String attrType);

}
