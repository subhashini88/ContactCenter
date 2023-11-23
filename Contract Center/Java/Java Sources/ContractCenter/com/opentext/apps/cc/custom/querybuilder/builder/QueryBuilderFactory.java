package com.opentext.apps.cc.custom.querybuilder.builder;

public final class QueryBuilderFactory {

	public static final String DB_MSSQL = "MSSQL";
	public static final String DB_POSTGRES = "POSTGRES";

	public static final synchronized IQueryBuilder getQueryBulder(String dbType) {

		IQueryBuilder queryBuilder = null;
		if (dbType.equalsIgnoreCase(DB_MSSQL)) {
			queryBuilder = new MSSQLQueryBuilder();
		} else if (dbType.equalsIgnoreCase(DB_POSTGRES)) {
			queryBuilder = new PostgresQueryBuilder();
		} else {
			throw new RuntimeException("No DB type query builder implementation found: " + dbType);
		}
		return queryBuilder;

	}

}
