package com.opentext.apps.cc.custom.fileutils;

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
