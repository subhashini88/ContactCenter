package com.opentext.apps.cc.importcontent;


import java.util.Date;

import com.cordys.cpc.bsf.util.DataConverter;
import com.opentext.apps.cc.importcontent.DateUtil;


public final class ContentManagementUtil {
	private ContentManagementUtil()
	{
		
	}
	
	/**
	 * Returns date of type java.util.Date from
	 * the given dateString which is read from 
	 * excel.
	 * 
	 * @param dateString
	 * @return date object which represents the given date in string
	 */
	public static Date getExcelDate(final String dateString)
	{
		return org.apache.poi.ss.usermodel.DateUtil.getJavaDate(DataConverter.String2double(dateString), false);
	}
	
	/**
	 * Returns date of type java.lang.String from
	 * the given dateString in 'yyyy-MM-dd' format
	 * 
	 * @param dateString
	 * @return string which represents the given date in 'yyyy-MM-dd' format
	 */
	public static String getExcelDateAsString(final String dateString)
	{	
		Date excelDate = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(DataConverter.String2double(dateString), false);
		return DateUtil.getYYYYMMDDDate(excelDate);

	}
	
	/**
	 * Returns true if the dateString value is a double 
	 * value, otherwise, false.
	 * 
	 * @param dateString
	 * @return true if dateString is double, otherwise false
	 */
	public static boolean isDoubleValue(final String dateString)
	{
		return dateString.matches("\\d+(\\.\\d+)?");
	}

}
