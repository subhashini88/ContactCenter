package com.opentext.apps.cc.analytics.aggregator.util.data;

import java.util.ArrayList;
import java.util.List;

public class UserInfo {

	public String id;
	public String userId;
	public List<String> targetWorklist = new ArrayList<>();
	public String targetWorklistStr;
	public boolean isAdmin;

	public UserInfo(String id, boolean isAdmin) {
		super();
		this.id = id;
		this.isAdmin = isAdmin;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public List<String> getTargetWorklist() {
		return targetWorklist;
	}

	public void setTargetWorklist(List<String> targetWorklist) {
		this.targetWorklist = targetWorklist;
	}

	public String getTargetWorklistStr() {
		if (this.targetWorklist == null || this.targetWorklist.isEmpty()) {
			return "''";
		}
		return "'" + String.join("','", this.targetWorklist) + "'";
	}

	public boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public String getId() {
		return id;
	}

}
