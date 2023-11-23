package com.opentext.apps.cc.custom.model;

import java.util.Objects;

public class DocGenModelUtil {

	public static final String NUMBERING_STYLE = "numberingStyle";
	public static final String CASCADING_INFO = "cascading";
	public static final String NO_NUMBERING_INFO = "nonumbering";
	public static final String DEFAULT_STYLLING_ATTRIBUTES = "{\"numberingStyle\":\"decimal\",\"cascading\":\"ON\"}";
	public static final String CASCADE_ON = "ON";
	public static final String CASCADE_OFF = "OFF";
	public static final String CASCADE_CONTINUE = "CONTINUE";
	public static final String CASCADE_NEW = "NEW";
	public static final String CASCADE_INHERIT = "INHERITED";

	// Condition Actions.
	public static final String CONDITIONAL_ACTION_HIDE = "HIDE";
	public static final String CONDITIONAL_ACTION_REPLACE = "REPLACE";
	public static final String CONDITIONAL_ACTION_ADD_AFTER = "ADDAFTER";
	public static final String CONDITIONAL_ACTION_ADD_BEFORE = "ADDBEFORE";

	public static String getTokenValue(String json, String key) {
		String result = "";
		if (Objects.isNull(json) || json.isBlank()) {
			json = "";
		}
		json = json.replace("{", "").replace("}", "").replace("\"", "");
		String[] tokens = json.split(",");
		if (Objects.nonNull(key) && !key.isBlank()) {
			for (String token : tokens) {
				token = token.trim();
				String[] keyvalue = token.split(":");
				if (keyvalue[0].equalsIgnoreCase(key)) {
					result = keyvalue.length > 1 ? keyvalue[1] : "";
				}
			}
		}
		return result;
	}

	public static String getAgumentedStyle(String numberingStyle) {
		if ("decimal".equalsIgnoreCase(numberingStyle)) {
			numberingStyle = "cc-" + numberingStyle + "-nested";
		} else if ("lower-alpha".equalsIgnoreCase(numberingStyle)) {
			numberingStyle = "cc-" + numberingStyle + "-nested";
		} else if ("upper-alpha".equalsIgnoreCase(numberingStyle)) {
			numberingStyle = "cc-" + numberingStyle + "-nested";
		} else if ("lower-roman".equalsIgnoreCase(numberingStyle)) {
			numberingStyle = "cc-" + numberingStyle + "-nested";
		} else if ("upper-roman".equalsIgnoreCase(numberingStyle)) {
			numberingStyle = "cc-" + numberingStyle + "-nested";
		} else if ("decimal-simple".equalsIgnoreCase(numberingStyle)) {
			numberingStyle = "cc-" + numberingStyle;
		} else if ("lower-alpha-simple".equalsIgnoreCase(numberingStyle)) {
			numberingStyle = "cc-" + numberingStyle;
		} else if ("upper-alpha-simple".equalsIgnoreCase(numberingStyle)) {
			numberingStyle = "cc-" + numberingStyle;
		} else if ("lower-roman-simple".equalsIgnoreCase(numberingStyle)) {
			numberingStyle = "cc-" + numberingStyle;
		} else if ("upper-roman-simple".equalsIgnoreCase(numberingStyle)) {
			numberingStyle = "cc-" + numberingStyle;
		} else {
			numberingStyle = "cc-" + numberingStyle + "-nested";
		}
		return numberingStyle;
	}

	public static String getAgumentedCascadingInfo(String cascadingInfo) {
		String result = CASCADE_CONTINUE;
		if (Objects.nonNull(cascadingInfo) && !cascadingInfo.isBlank()) {
			if (CASCADE_OFF.equalsIgnoreCase(cascadingInfo)) {
				result = CASCADE_OFF;
			} else if (CASCADE_ON.equalsIgnoreCase(cascadingInfo)) {
				result = CASCADE_CONTINUE;
			} else if (CASCADE_NEW.equalsIgnoreCase(cascadingInfo)) {
				// Need to uncomment this for a new list implementation.
				// result = CASCADE_NEW;
			}
		}
		return result;
	}

	public static String getAgumentedContentCascadingInfo(String cascadingInfo) {
		String result = CASCADE_CONTINUE;
		if (Objects.nonNull(cascadingInfo) && !cascadingInfo.isBlank()) {
			if (CASCADE_OFF.equalsIgnoreCase(cascadingInfo) || CASCADE_NEW.equalsIgnoreCase(cascadingInfo)) {
				result = CASCADE_NEW;
			}
		}
		return result;
	}
}
