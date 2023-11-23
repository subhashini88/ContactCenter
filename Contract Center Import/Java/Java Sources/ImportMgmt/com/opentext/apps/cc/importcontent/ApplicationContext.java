package com.opentext.apps.cc.importcontent;

import java.util.Date;

public interface ApplicationContext {
	/**
	 * Returns the current logged in user name
	 * @return user name
	 */
	public String getUser();
	
	/**
	 * Returns the date when the current application has started
	 * @return java.util.Date object
	 */
	public Date getStartTime();	
	
	/**
	 * Returns the current organization's unique id
	 * @return unique id of current organization
	 */
	public String getDataModelId();
	
	/**
	 * Returns whether the current application is working
	 * for initializing a tenant
	 * @return true if it is a tenant initialization, otherwise false 
	 */
	public boolean getIsTenantInitialization();
		

}
