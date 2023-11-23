package com.opentext.apps.cc.importcontent;

import java.util.Date;

import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ApplicationContext;

public class ApplicationContextImpl implements ApplicationContext{
	private Date startTime;
	private String dataModelId;
	private String user;
	private boolean isTenantInitializationRequest;
	
	public ApplicationContextImpl(String dataModelId, boolean isTenantInitialization)
	{
		//this.startTime = Utilities.getTimeStamp();
		this.dataModelId = dataModelId;
		this.user = Utilities.getBSFUser();
		isTenantInitializationRequest = isTenantInitialization;
	}

	@Override
	public String getUser()
	{
		return user;
	}

	@Override
	public Date getStartTime()
	{
		return startTime;
	}

	@Override
	public String getDataModelId()
	{
		return dataModelId;
	}

	@Override
	public boolean getIsTenantInitialization() 
	{
		return isTenantInitializationRequest;
	}


}
