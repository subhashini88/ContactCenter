package com.opentext.apps.cc.importcontent;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class ReportItem {
	private final Map<String, String> warnings = new TreeMap<>();
	private final Map<String, String> errors = new TreeMap<>();
	private Object sourceId="";
	
	public void error(final String columnName,final String errorText)
	{
		errors.put(columnName, errorText);
	}
	
	public void warning(final String columnName,final String warningText)
	{
		warnings.put(columnName, warningText);
	}
	
	public Map<String, String> getErrors()
	{
		return Collections.unmodifiableMap(this.errors);
	}
	
	public Map<String, String> getWarnings()
	{
		return Collections.unmodifiableMap(this.warnings);
	}
	
	public boolean isValid()
	{
		return errors.isEmpty();
	}
	
	public void setSouceId(Object sourceId)
	{
		this.sourceId = sourceId;
	}
	
	public Object getSourceId()
	{
		return this.sourceId;
	}
	
	@Override
	public String toString() {
		//replacing #$ for accommodating multiple errors in a single column (applies for rules/task lists)
		return "Errors : "+errors.toString().replace(',', ';').replace("#$", ",");
	}
}
