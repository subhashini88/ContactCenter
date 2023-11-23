package com.opentext.apps.cc.importcontent;

import com.opentext.apps.cc.importcontent.ExcelReadConfiguration;

public class ExcelReadConfigurationImpl implements ExcelReadConfiguration
{
	private String sheetName;

	@Override
	public String getSheetName()
	{
		return sheetName;
	}

	@Override
	public void setSheetName(String sheetName)
	{
		this.sheetName = sheetName;
	}
	
}
