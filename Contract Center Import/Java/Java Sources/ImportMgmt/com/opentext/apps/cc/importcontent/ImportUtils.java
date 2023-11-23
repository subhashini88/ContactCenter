package com.opentext.apps.cc.importcontent;

import static java.lang.reflect.Modifier.STATIC;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.cordys.cpc.bsf.busobject.BSF;
import com.cordys.cpc.bsf.busobject.BusObjectManager;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;

public class ImportUtils {
	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	// public static final SimpleDateFormat sdf = new
	// SimpleDateFormat("yyyy-MM-dd");
        public static final DateFormat _MM_DD_YYYY = new SimpleDateFormat("MM-dd-yyyy");
 	private static final DateFormat _YYYYMMDD = new SimpleDateFormat("YYYYMMDD");
	private static final DateFormat _DD_Mon_YYYY = new SimpleDateFormat("DD-MMM-YYYY");
	private static final DateFormat _YYYY_MM_DD_TIME = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
	public static final DateFormat _YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd");
	private static final CordysLogger logger = CordysLogger.getCordysLogger(ImportUtils.class);

	static {
		synchronized (sdf) {
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			sdf.setLenient(true);
		}
	}

	public static String getDateString(String dateString) {
		return ContentManagementUtil.isDoubleValue(dateString) ? ContentManagementUtil.getExcelDateAsString(dateString)
				: dateString;
	}

	public static Date getDate(String dateString) {

		if (Utilities.isStringEmpty(dateString))
			return null;

		dateString = ContentManagementUtil.isDoubleValue(dateString)
				? ContentManagementUtil.getExcelDateAsString(dateString)
				: dateString;
		try {
			synchronized (sdf) {
				return new Date(sdf.parse(dateString).getTime());
			}
		} catch (ParseException exception) {
			logger._log("com.opentext.apps.cc.custom.ImportUtils", Severity.ERROR, exception,"Exception while parsing the given date.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.DATE_PARSE_EXCEPTION);
		}
	}

	public static BusObjectManager createBusObjectManager() {
		return new BusObjectManager(BSF.getMyContext().getObjectManager().getConfiguration(), BSF.getXMLDocument());
	}

	/*
	 * This method return all the static string as a Collection for the given class
	 */
	public static Set<String> getSystemConstants(Class<?> clazz) {
		Set<String> constants = new LinkedHashSet<>();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			int modifiers = field.getModifiers();
			// Checking for static and String type
			if ((modifiers & STATIC) == STATIC && field.getType().isAssignableFrom(String.class)) {
				try {
					constants.add((String) field.get(null));
				} catch (IllegalArgumentException | IllegalAccessException exception) {
					throw new RuntimeException(exception);
				}
			}
		}
		return constants;
	}

	public static <E> boolean isEmpty(Map<String, String> map) {
		return map == null || map.isEmpty();
	}

	public static <E> boolean isEmpty(Collection<E> collection) {
		return collection == null || collection.isEmpty();
	}

	public static boolean isEmpty(int[] intArr) {
		return intArr == null || intArr.length == 0;
	}

	public static boolean isEmpty(String[] strArr) {
		return strArr == null || strArr.length == 0;
	}

	public static boolean isEmpty(String s) {
		return s == null || s.trim().length() == 0;
	}

	public static int getIntValue(String s) {
		int retVal = 0;
		if (!isEmpty(s)) {
			retVal = Integer.parseInt(s);
		}
		return retVal;
	}

	public static double getDoubleValue(String s) {
		double retVal = 0;
		if (!isEmpty(s)) {
			retVal = Double.parseDouble(s);
		}
		return retVal;
	}

	public static String convertYYYYMMDD_To_DD_Mon_YYYY(String date) {
		String converterDate = "";
		try {
			converterDate = isEmpty(date) ? "" : _DD_Mon_YYYY.format(_YYYYMMDD.parse(date));
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage());
		}
		return converterDate;
	}

	/*
	 * dateString is in YYYYMMDD format we should return date string in yyyy-mm-dd
	 * hh:mm:ss after adding months
	 */
	public static String addMonthsToYYYYMMDD(String dateString, String nbMonths) {
		String converterDate = "";
		try {
			if (isEmpty(nbMonths))
				return "";
			int noOfMonths = 0;
			if (!isEmpty(nbMonths)) {
				noOfMonths = Integer.parseInt(nbMonths);
			}
			Date dateAsObj = _YYYYMMDD.parse(dateString);
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateAsObj);
			cal.add(Calendar.MONTH, noOfMonths);
			Date dateAsObjAfterAMonth = cal.getTime();
			converterDate = _YYYY_MM_DD_TIME.format(dateAsObjAfterAMonth);
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage());
		}
		return converterDate;
	}

}
