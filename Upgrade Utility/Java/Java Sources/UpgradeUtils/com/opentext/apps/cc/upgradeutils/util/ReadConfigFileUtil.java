package com.opentext.apps.cc.upgradeutils.util;

public class ReadConfigFileUtil {

	/***
	 * if version1 < version2 true else false
	 **/
	public static boolean compareVersion(String version1, String version2) {
		/*
		 * boolean isLess = true; String[] indVersions1 = StringUtils.split(version1,
		 * "."); String[] indVersions2 = StringUtils.split(version2, "."); if (null !=
		 * version1 && null != version2 && version1.equals(version2)) { return false; }
		 * for (int i = 0; i < indVersions2.length; i++) { if (i < indVersions1.length
		 * && indVersions1[i].compareTo(indVersions2[i]) == 0) { continue; } else if (i
		 * < indVersions1.length && indVersions1[i].compareTo(indVersions2[i]) > 0) {
		 * return false; } else if (i < indVersions1.length &&
		 * indVersions1[i].compareTo(indVersions2[i]) < 0) { return true; } } return
		 * isLess;
		 */

		// vnum stores each numeric part of version
		int vnum1 = 0, vnum2 = 0;

		// loop until both String are processed
		for (int i = 0, j = 0; (i < version1.length() || j < version2.length());) {
			// Storing numeric part of
			// version 1 in vnum1
			while (i < version1.length() && version1.charAt(i) != '.') {
				vnum1 = vnum1 * 10 + (version1.charAt(i) - '0');
				i++;
			}

			// storing numeric part
			// of version 2 in vnum2
			while (j < version2.length() && version2.charAt(j) != '.') {
				vnum2 = vnum2 * 10 + (version2.charAt(j) - '0');
				j++;
			}

			if (vnum1 > vnum2)
				return false;
			if (vnum2 > vnum1)
				return true;

			// if equal, reset variables and
			// go for next numeric part
			vnum1 = vnum2 = 0;
			i++;
			j++;
		}
		return false;

	}

	public static boolean equals(String version1, String version2) {
		if (null != version1 && null != version2 && version1.equals(version2)) {
			return true;
		}
		return false;
	}

}
