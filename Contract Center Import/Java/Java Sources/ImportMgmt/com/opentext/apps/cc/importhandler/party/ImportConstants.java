package com.opentext.apps.cc.importhandler.party;

import com.opentext.apps.cc.importcontent.CommonEnums.CCBaseEnum;

public class ImportConstants {
	public static final String SHEET_NAME = "Party";
	public static final String PARTY_REGISTERED_NAME = "RegisteredName";
	public static final String PARTY_COUNTRY = "Country";
	public static final String PARTY_NAME = "Name";
	public static final String PARTY_ACCOUNT_TYPE = "AccountType";
	public static final String PARTY_DESCRIPTION = "Description";
	public static final String PARTY_REGISTRATION_ID = "RegistrationID";
	public static final String CODE = "Code";
	public static final String CREATION_TYPE = "CreationType";
	public static final String EMAIL = "Email";
	public static final String IDENTIFICATION_NUMBER = "IdentificationNumber";
	public static final String INCEPTION_DATE = "InceptionDate";
	public static final String MASTER_ID = "MasterID";
	public static final String STATUS = "Status";
	public static final String WEBSITE = "Website";

	public enum AccountType implements CCBaseEnum {

		EXTERNAL(1, "EXTERNAL"), INTERNAL(2, "INTERNAL");
		private int id;
		private String value;

		private AccountType(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public int getId() {
			return id;
		}

		public String getValue() {
			return value;
		}

		public static AccountType getEnumObject(String value) {
			if (null != value) {
				for (AccountType item : AccountType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return item;
					}
				}
			}
			return null;
		}

		public static boolean contains(String value) {
			if (null != value) {
				for (AccountType item : AccountType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}
}
