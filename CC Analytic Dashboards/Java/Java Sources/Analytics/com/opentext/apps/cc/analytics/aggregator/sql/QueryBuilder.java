package com.opentext.apps.cc.analytics.aggregator.sql;

import java.util.Objects;
import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.analytics.aggregator.sql.agg.Aggregator;
import com.opentext.apps.cc.analytics.aggregator.sql.agg.Average;
import com.opentext.apps.cc.analytics.aggregator.sql.agg.Count;
import com.opentext.apps.cc.analytics.aggregator.sql.agg.CountUnique;
import com.opentext.apps.cc.analytics.aggregator.sql.agg.Maximum;
import com.opentext.apps.cc.analytics.aggregator.sql.agg.Minimum;
import com.opentext.apps.cc.analytics.aggregator.sql.agg.Sum;
import com.opentext.apps.cc.analytics.aggregator.sql.builder.DBType;
import com.opentext.apps.cc.analytics.aggregator.sql.builder.IQueryBuilder;
import com.opentext.apps.cc.analytics.aggregator.sql.builder.QueryBuilderFactory;
import com.opentext.apps.cc.analytics.nom.NomUtil;
import com.opentext.apps.cc.process.ReportDashboardConfig;

public class QueryBuilder {

	private static final String solutionName = "OpenTextContractCenter";
	private static final String entityName = "Contract";

	private ReportDashboardConfig configuration = null;

	public QueryBuilder(ReportDashboardConfig configuration) {
		this.configuration = configuration;
	}

	private void addAggregatorStratergy(IQueryBuilder queryBuilder, String aggregator, String dbType) {
		DBType dbTypeEnum = getDbType(dbType);
		if (aggregator.equalsIgnoreCase(Aggregator.COUNT.toString())) {
			queryBuilder.setAggStratergy(new Count(dbTypeEnum));
		} else if (aggregator.equalsIgnoreCase(Aggregator.SUM.toString())) {
			queryBuilder.setAggStratergy(new Sum(dbTypeEnum));
		} else if (aggregator.equalsIgnoreCase(Aggregator.COUNTUNIQUE.toString())) {
			queryBuilder.setAggStratergy(new CountUnique(dbTypeEnum));
		} else if (aggregator.equalsIgnoreCase(Aggregator.MIN.toString())) {
			queryBuilder.setAggStratergy(new Minimum(dbTypeEnum));
		} else if (aggregator.equalsIgnoreCase(Aggregator.MAX.toString())) {
			queryBuilder.setAggStratergy(new Maximum(dbTypeEnum));
		} else if (aggregator.equalsIgnoreCase(Aggregator.AVG.toString())) {
			queryBuilder.setAggStratergy(new Average(dbTypeEnum));
		}
	}

	private DBType getDbType(String dbType) {
		if (dbType.equalsIgnoreCase(DBType.MSSQL.toString())) {
			return DBType.MSSQL;
		}
		return DBType.POSTGRES;
	}

	private static String getDbType() {
		String dbType = QueryBuilderFactory.DB_POSTGRES;
		try {
			SOAPRequestObject GCPropsRequest = new SOAPRequestObject("http://schemas.cordys.com/WS-AppServer/1.0",
					"DataBaseInfo", null, null);
			int response = GCPropsRequest.sendAndWait();
			String type = Node.getDataWithDefault(NomUtil.getNode(".//tuple/old//BackEndInfo//dbProductName", response),
					null);
			if (!type.contains("PostgreSQL")) {
				dbType = QueryBuilderFactory.DB_MSSQL;
			}
		} catch (Exception e) {
		}

		return dbType;
	}

	private String getTableName() {
		return Utilities.getTableName(solutionName, entityName);
	}

	public String buildSql() {
		String dbType = getDbType();
		String tableName = getTableName();
		if (!Objects.nonNull(tableName)) {
			throw new NullPointerException("No data set found.");
		}
		IQueryBuilder queryBuilder = QueryBuilderFactory.getQueryBulder(dbType,configuration.getDataSetGroup());
		queryBuilder.setYCustCol(configuration.isYCustomCol());
		queryBuilder.setXCustCol(configuration.isXCustomCol());
		queryBuilder.setDataSet(configuration.getDataSet());
		queryBuilder.setAggCustCol(configuration.isAggCustomCol());
		queryBuilder.setxColumnName(configuration.getxColumn());
		queryBuilder.setTargetWorkListStr(
				!configuration.getUser().getIsAdmin() ? configuration.getUser().getTargetWorklistStr() : "");
		queryBuilder.setyColumnName(configuration.getyColumn());
		queryBuilder.setIsAdmin(configuration.getUser().getIsAdmin());
		queryBuilder.setAggColumnName(configuration.getAggregatorColumn());
		queryBuilder.addTableName(tableName);
		queryBuilder.addJoinQuery();
		queryBuilder.setAggregator(configuration.getAggregator());
		addAggregatorStratergy(queryBuilder, configuration.getAggregator(), dbType);
		String sql = queryBuilder.buildQuery(null).getSqlQuery();
		queryBuilder.clear();
		return sql;
	}
}
