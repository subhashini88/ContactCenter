package com.opentext.apps.cc.importcontent;

import com.opentext.apps.cc.importcontent.ReadConfiguration;

public interface ExcelReadConfiguration extends ReadConfiguration{
	public void setSheetName(String sheetName);
	public String getSheetName();
}
