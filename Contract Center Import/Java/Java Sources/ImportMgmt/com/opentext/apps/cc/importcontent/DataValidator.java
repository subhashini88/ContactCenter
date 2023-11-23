package com.opentext.apps.cc.importcontent;

import java.text.ParseException;

public class DataValidator {
	
	
	public static boolean isValidDate(String string){
		
		boolean isValid = false;
		
		if (string == null) {
			return false;
		}
		
		try {
			ImportUtils.sdf.parse(ImportUtils.getDateString(string));
			isValid = true;
		} catch (ParseException e) {
			// TODO: do something meaningful here
		}
		return isValid;
	}
	public static boolean isInteger(String str) {
	    if (str == null) {
	        return false;
	    }
	    int length = str.length();
	    if (length == 0) {
	        return false;
	    }
	    for (int i = 0; i < length; i++) {
	        char c = str.charAt(i);
	        if (c < '0' || c > '9') {
	            return false;
	        }
	    }
	    return true;
	}
}
