package com.opentext.apps.cc.analytics.aggregator.sql.builder;

import com.opentext.apps.cc.process.DataSetGroup;

public final class QueryBuilderFactory {

	public static final String DB_MSSQL = "MSSQL";
	public static final String DB_POSTGRES = "POSTGRES";

	public static final synchronized IQueryBuilder getQueryBulder(String dbType, DataSetGroup dataSetGroup) {

		IQueryBuilder queryBuilder = null;
		if (dataSetGroup.equals(DataSetGroup.CONTRACT) && dbType.equalsIgnoreCase(DBType.MSSQL.toString())) {
			queryBuilder = new ContractMSSQLQueryBuilder();
		} else if (dataSetGroup.equals(DataSetGroup.CONTRACT) && dbType.equalsIgnoreCase(DBType.POSTGRES.toString())) {
			queryBuilder = new ContractPostgresQueryBuilder();
		} else if (dataSetGroup.equals(DataSetGroup.OBLIGATION) && dbType.equalsIgnoreCase(DBType.MSSQL.toString())) {
			queryBuilder = new ObligationMSSQLQueryBuilder();
		} else if (dataSetGroup.equals(DataSetGroup.OBLIGATION)
				&& dbType.equalsIgnoreCase(DBType.POSTGRES.toString())) {
			queryBuilder = new ObligationPostgresQueryBuilder();
		} else {
			throw new RuntimeException("No DB type query builder implementation found: " + dbType);
		}
		return queryBuilder;
	}

}
