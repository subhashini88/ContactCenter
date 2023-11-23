package com.opentext.apps.cc.analytics.aggregator.sql;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.analytics.aggregator.sql.builder.DBType;
import com.opentext.apps.cc.analytics.aggregator.sql.builder.QueryBuilderFactory;
import com.opentext.apps.cc.analytics.nom.NomUtil;

public class DbUtil {

	public static String prepareYearColumn(String colName, DBType dbType) {
		if (DBType.MSSQL.equals(dbType)) {
			return "YEAR(" + colName + ")";
		} else if (DBType.POSTGRES.equals(dbType)) {
			return "EXTRACT( YEAR  FROM  " + colName + "  )";
		}
		return "";
	}

	public static String prepareQuarterColumn(String colName, DBType dbType) {
		if (DBType.MSSQL.equals(dbType)) {
			return "DATEPART(QQ, " + colName + ")";
		} else if (DBType.POSTGRES.equals(dbType)) {
			return "EXTRACT( QUARTER  FROM  " + colName + "  )";
		}
		return "";
	}

	public static String getDbType() {
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

	public static String getPresentDateStr(String format) {
		Date currentDate = Calendar.getInstance().getTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		return "'" + dateFormat.format(currentDate) + "'";
	};

	public static String getFutureDateStr(String format, int span, int months) {
		Calendar instance = Calendar.getInstance();
		instance.add(span, months);
		Date currentDate = instance.getTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		return "'" + dateFormat.format(currentDate) + "'";
	};

}
