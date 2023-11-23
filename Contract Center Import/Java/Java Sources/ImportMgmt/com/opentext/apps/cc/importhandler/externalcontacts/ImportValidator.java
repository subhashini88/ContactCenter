package com.opentext.apps.cc.importhandler.externalcontacts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map; 
import java.util.regex.Pattern;

import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.CommonEnums.PersonPrefix;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;
import com.opentext.apps.cc.importhandler.externalcontacts.ImportConstants;
import com.opentext.apps.cc.importhandler.externalcontacts.MetadataInitializer;

public class ImportValidator {
	public ReportItem validate(Map<String,String> rowData,MetadataInitializer metadata, String jobId)
	{
		ReportItem report = new ReportItem();
		if (rowData == null) return report;
		
        if(Utilities.isStringEmpty(rowData.get(ImportConstants.DISPLAY_NAME))) {
        	report.error(ImportConstants.DISPLAY_NAME, "Display name is missing");
        }
        
        /*if(Utilities.isStringEmpty(rowData.get(ImportConstants.LEGACY_ID))) {
        	report.error(ImportConstants.LEGACY_ID, "Legacy id is missing");
        }*/
        
		//E-mail validation
        String email = rowData.get(ImportConstants.EMAIL);
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$"; 
        Pattern emailPattern = Pattern.compile(emailRegex); 
        
        if(Utilities.isStringEmpty(rowData.get(ImportConstants.EMAIL))) {
        	report.error(ImportConstants.EMAIL, "Email ID is missing");
        }
        
        else 
        	if (!emailPattern.matcher(email).matches())
        	report.error(ImportConstants.EMAIL, "Invalid email ID");

        //Birth date validation
        if (!Utilities.isStringEmpty(rowData.get(ImportConstants.BIRTH_DATE)))
        	if (!isValidDate(rowData.get(ImportConstants.BIRTH_DATE)))
        		report.error(ImportConstants.BIRTH_DATE, "Invalid date of birth"); 
        
        //Phone number validation
        if (!Utilities.isStringEmpty(rowData.get(ImportConstants.PHONE)))
        	if (!isValidPhoneNumber(rowData.get(ImportConstants.PHONE)))
        		report.error(ImportConstants.PHONE, "Invalid phone number"); 
        
        //Mobile number validation
        if (!Utilities.isStringEmpty(rowData.get(ImportConstants.MOBILE)))
        	if (!isValidPhoneNumber(rowData.get(ImportConstants.MOBILE)))
        		report.error(ImportConstants.MOBILE, "Invalid mobile number");
        
        //Gender validation
//        if(!Utilities.isStringEmpty(rowData.get(ImportConstants.GENDER))) {
//        	if(metadata.genderMap.get(rowData.get(ImportConstants.GENDER)) == null)
//        	report.error(ImportConstants.GENDER, "The provided gender does not exist.");
//        	}
        
    	// Enumeration validations

		// Title.
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.TITLE))) {
			String prefixValue = rowData.get(ImportConstants.TITLE);
			if (!PersonPrefix.contains(prefixValue)) {
				report.error(ImportConstants.TITLE, "' "+rowData.get(ImportConstants.TITLE) + " ' is not valid");
			}
		}
        
		return report;
	}
	
	/*public static boolean isValidDate(String string){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setLenient(false);
		try {
			sdf.parse(string);
		} catch (ParseException e) {
			return false;
		}
		return true;
	}
*/	
	
	
	public static boolean isValidDate(String string){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setLenient(false);
		try {
			sdf.parse(string);
		} catch (ParseException e) {
			return false;
		}
		return true;
	}
	
	
	/*public static boolean isValidDate(String str) {
	
		String[] BOD = str.replace("Z", "").split("-");
		if(BOD.length !=3) {
			return false;
		}
		if (!BOD[0].matches("\\d{4}") && !BOD[1].matches("\\d{2}")  && !BOD[2].matches("\\d{2}")) 
			return false;
		int givenDay = Integer.parseInt(BOD[2]);
		int givenMonth = Integer.parseInt(BOD[1]);
		int givenYear = Integer.parseInt(BOD[0]);
		Date today = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		int currentDay = cal.get(Calendar.DAY_OF_MONTH);
		int currentMonth = cal.get(Calendar.MONTH)+1;
		int currentYear = cal.get(Calendar.YEAR);
		int[] daysOfMonths = {0,31,28,31,30,31,30,31,31,30,31,30,31};
		
        if(givenDay <= 0 | givenMonth <= 0 | givenYear <= 0)
        	return false;

		if (givenYear > currentYear || givenMonth > 12)
			return false;
		else {
			boolean dayCheck = givenDay <= daysOfMonths[givenMonth];
			if (givenYear%4 == 0) {
				if(givenMonth == 2 && givenDay<=29)
					return true;
				else if(dayCheck)
					return true;
				else
					return false;
			}
			else {
				if(dayCheck)
					return true;
				else
					return false;
			}
			
		}
		
	}*/
	
	
	private static boolean isValidPhoneNumber(String phoneNo) {
	    if (phoneNo.matches("\\d{10}"))
			return true;
		else if(phoneNo.matches("\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}"))
			return true;
		else if(phoneNo.matches("\\d{3}-\\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}"))
			return true;
		else if(phoneNo.matches("\\(\\d{3}\\)-\\d{3}-\\d{4}"))
			return true;
		else if(phoneNo.matches("^(\\+[0-9]{1,3}\\-)*[0-9]{4,14}(?:x.+)?$"))
			return true;
		else
			return false;
	}
}