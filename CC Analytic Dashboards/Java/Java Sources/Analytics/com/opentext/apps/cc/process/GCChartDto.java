package com.opentext.apps.cc.process;

public class GCChartDto {

	private String relatedConfigId;
	private String relatedConfigItemId;
	private String defaultChartDuration;
	private String defaultChartType;
	private String defaultGroupByCol;
	private String userIdentityId;

	public String getRelatedConfigId() {
		return relatedConfigId;
	}

	public void setRelatedConfigId(String relatedConfigId) {
		this.relatedConfigId = relatedConfigId;
	}

	public String getRelatedConfigItemId() {
		return relatedConfigItemId;
	}

	public void setRelatedConfigItemId(String relatedConfigItemId) {
		this.relatedConfigItemId = relatedConfigItemId;
		this.relatedConfigId = relatedConfigItemId.split("\\.")[1];
	}

	public String getDefaultChartDuration() {
		return defaultChartDuration;
	}

	public void setDefaultChartDuration(String defaultChartDuration) {
		this.defaultChartDuration = defaultChartDuration;
	}

	public String getDefaultChartType() {
		return defaultChartType;
	}

	public void setDefaultChartType(String defaultChartType) {
		this.defaultChartType = defaultChartType;
	}

	public String getDefaultGroupByCol() {
		return defaultGroupByCol;
	}

	public void setDefaultGroupByCol(String defaultGroupByCol) {
		this.defaultGroupByCol = defaultGroupByCol;
	}

	public String getUserIdentityId() {
		return userIdentityId;
	}

	public void setUserIdentityId(String userIdentityId) {
		this.userIdentityId = userIdentityId;
	}

}
