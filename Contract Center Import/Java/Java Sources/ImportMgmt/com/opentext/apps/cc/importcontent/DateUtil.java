package com.opentext.apps.cc.importcontent;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import com.cordys.cpc.bsf.busobject.exception.WSAppServerRunTimeException;
import com.cordys.cpc.bsf.util.DataConverter;
import com.eibus.util.system.Native;

public class DateUtil
{
	/*
	 * 
	* @param noOfDays: 
	*               pass the number of days to get the future Date
	* 
	*/
        private static DatatypeConfigurationException exception;
	private static volatile DatatypeFactory datatypeFactory;
        static{
            try {
                datatypeFactory  = DatatypeFactory.newInstance();
            } catch (DatatypeConfigurationException ex) {
                exception = ex;
            }
        }
        
	public static Date dateAfter(Date date, int field, int value)
	{
		Date finalDate;
		if (date == null)
		{
			finalDate = new Date();
		}
		else
		{
			finalDate = date;
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(finalDate);
		cal.add(field, value); // add 10 days  
		finalDate = cal.getTime();
		return finalDate;
	}

	public static Date getDate()
	{
		return new Date();
	}

	public static Date getTimeStamp()
	{
		return new Timestamp(System.currentTimeMillis());
	}

	public static String prepareDurationTime(Date date1, Date date2)
	{
		Duration duration = getDatTypeFactory().newDuration(date2.getTime() - date1.getTime());
		return duration.toString();
	}

	/**
	 * Calculates period in time and returns duration in XSD format. If
	 * period is less than 10 seconds, xsd period for 10 seconds is returned.
	 * 
	 * @param fromDate
	 * @param targetDate
	 * @return XSD format of the time period Ex: P5Y2M10DT15H
	 */
	public static String prepareDurationTimeForSchedule(final Date fromDate,
			final Date targetDate) {
		long difference = targetDate.getTime() - fromDate.getTime();
		if (difference <= 10000)
			difference = 10000;
		Duration duration = getDatTypeFactory().newDuration(difference);
		return duration.toString();

	}

	private static DatatypeFactory getDatTypeFactory()
	{
		if (datatypeFactory == null)
		{
                    // not able to create the DataTypeFactory,hence throw the exception
                    //which was thrown while tried to create datatypeFactory
                    throw new WSAppServerRunTimeException(exception);
		}
		return datatypeFactory;
	}

	public static String getCurrentUTCDate()
	{
		return Native.convertDateToUTCString(System.currentTimeMillis(), true);
	}

	public static String getDuration(long millisecs)
	{
		if (millisecs < 0)
		{
			millisecs = 0;
		}
		if (millisecs < 10000)
		{
			millisecs = 10000;
		}
		return getDatTypeFactory().newDuration(millisecs).toString();
	}

	public static long cordysFormatToUTCMillis(String requiredDate)
	{
		return DataConverter.String2Date(requiredDate).getTime();

	}
	
	public static String getYYYYMMDDDate(final Date inputDate)
	{
		return new SimpleDateFormat("yyyy-MM-dd").format(inputDate);
	}

}
