package com.opentext.apps.cc.process;

import com.opentext.apps.cc.analytics.aggregator.util.data.UserInfo;
import com.opentext.apps.cc.analytics.nom.NomUtil;

public class ReportDashboardConfig {

	private String itemId1;
	private String userId;
	private String userIdentityId;
	private boolean isAdmin;
	private String savedReportId;
	private String name;
	private String xColumn;
	private String yColumn;
	private String aggregator;
	private DataSetGroup dataSetGroup = DataSetGroup.CONTRACT;
	private String aggregatorColumn;
	private String dataSet;
	private String chartType;
	private String configType;
	private ProcessorType processorType = ProcessorType.SQL;

	private boolean isXCustomCol = false;
	private boolean isYCustomCol = false;
	private boolean isAggCustomCol = false;

	private String defaultChartType = null;
	private String defaultChartDuration = null;
	private String defaultGroupBy = null;

	private UserInfo user = null;

	public static String CUST_KEY = "ctr_custom_";

	public ReportDashboardConfig(String itemId1, String savedReportId, String name, String xColumn, String yColumn,
			String aggregator, String aggregatorColumn, String dataSet, String chartType, ProcessorType processorType,
			String configType, String userIdentityId, String userId, boolean isAdmin, DataSetGroup dataSetGroup) {
		this.itemId1 = itemId1;
		this.userId = userId;
		this.savedReportId = savedReportId;
		this.name = name;
		this.xColumn = xColumn;
		this.yColumn = yColumn;
		this.aggregator = aggregator;
		this.aggregatorColumn = aggregatorColumn;
		this.isXCustomCol = checkIfCustCol(xColumn);
		this.isYCustomCol = checkIfCustCol(yColumn);
		this.isAggCustomCol = checkIfCustCol(aggregatorColumn);
		this.dataSet = dataSet;
		this.chartType = chartType;
		this.processorType = processorType;
		this.isAdmin = isAdmin;
		this.configType = configType;
		this.userIdentityId = userIdentityId;
		this.dataSetGroup = dataSetGroup;
		this.user = new UserInfo(userIdentityId, isAdmin);
	}

	public ReportDashboardConfig(String itemId1, String savedReportId, String name, String xColumn, String yColumn,
			String aggregator, String aggregatorColumn, String dataSet, String chartType, ProcessorType processorType,
			String configType, String userIdentityId, String userId, boolean isAdmin) {
		this.itemId1 = itemId1;
		this.userId = userId;
		this.savedReportId = savedReportId;
		this.name = name;
		this.xColumn = xColumn;
		this.yColumn = yColumn;
		this.aggregator = aggregator;
		this.aggregatorColumn = aggregatorColumn;
		this.isXCustomCol = checkIfCustCol(xColumn);
		this.isYCustomCol = checkIfCustCol(yColumn);
		this.isAggCustomCol = checkIfCustCol(aggregatorColumn);
		this.dataSet = dataSet;
		this.chartType = chartType;
		this.processorType = processorType;
		this.isAdmin = isAdmin;
		this.configType = configType;
		this.userIdentityId = userIdentityId;
		this.user = new UserInfo(userIdentityId, isAdmin);
	}

	public String getDefaultChartType() {
		return defaultChartType;
	}

	public void setDefaultChartType(String defaultChartType) {
		this.defaultChartType = defaultChartType;
	}

	public String getDefaultChartDuration() {
		return defaultChartDuration;
	}

	public void setDefaultChartDuration(String defaultChartDuration) {
		this.defaultChartDuration = defaultChartDuration;
	}

	public String getDefaultGroupBy() {
		return defaultGroupBy;
	}

	public void setDefaultGroupBy(String defaultGroupBy) {
		this.defaultGroupBy = defaultGroupBy;
	}

	public UserInfo getUser() {
		return user;
	}

	public String getUserIdentityId() {
		return userIdentityId;
	}

	public String getConfigType() {
		return configType;
	}

	public boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public boolean checkIfCustCol(String colVal) {
		return colVal != null && colVal.contains(CUST_KEY) ? true : false;
	}

	public boolean isAggCustomCol() {
		return isAggCustomCol;
	}

	public boolean isXCustomCol() {
		return isXCustomCol;
	}

	public boolean isYCustomCol() {
		return isYCustomCol;
	}

	public String getSavedReportId() {
		return savedReportId;
	}

	public ProcessorType getProcessorType() {
		return processorType;
	}

	public String getName() {
		return name;
	}

	public String getxColumn() {
		return xColumn;
	}

	public String getItemId1() {
		return itemId1;
	}

	public String getyColumn() {
		return yColumn;
	}

	public String getAggregator() {
		return aggregator;
	}

	public String getAggregatorColumn() {
		return aggregatorColumn;
	}

	public String getDataSet() {
		return dataSet;
	}

	public String getChartType() {
		return chartType;
	}

	public DataSetGroup getDataSetGroup() {
		return dataSetGroup;
	}
	

}
