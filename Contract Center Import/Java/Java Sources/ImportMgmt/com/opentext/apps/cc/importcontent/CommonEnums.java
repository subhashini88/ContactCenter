package com.opentext.apps.cc.importcontent;

public class CommonEnums {

	public interface CCBaseEnum {

		public int getId();

		public String getValue();
	}

	public enum Status implements CCBaseEnum {

		ACTIVE(1, "ACTIVE"), INACTIVE(2, "INACTIVE");

		private int id;
		private String value;

		private Status(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public int getId() {
			return id;
		}

		public String getValue() {
			return value;
		}

		public static Status getEnumObject(String value) {
			if (null != value) {
				for (Status item : Status.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return item;
					}
				}
			}
			return null;
		}

		public static boolean contains(String value) {
			if (null != value) {
				for (Status item : Status.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}
	
	
	public enum IntentType implements CCBaseEnum {

		BUY(1, "Buy"), SELL(2, "Sell"), OTHER(3, "Other");

		private int id;
		private String value;

		private IntentType(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public int getId() {
			return id;
		}

		public String getValue() {
			return value;
		}

		public static IntentType getEnumObject(String value) {
			if (null != value) {
				for (IntentType item : IntentType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return item;
					}
				}
			}
			return null;
		}

		public static boolean contains(String value) {
			if (null != value) {
				for (IntentType item : IntentType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	public enum CreationType implements CCBaseEnum {
		INTERNAL(1, "INTERNAL"), EXTERNAL(2, "EXTERNAL"), IMPORTED(3, "IMPORTED"), SALESFORCE(4, "SALESFORCE");

		private int id;
		private String value;

		private CreationType(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public int getId() {
			return id;
		}

		public String getValue() {
			return value;
		}

		public static CreationType getEnumObject(String value) {
			if (null != value) {
				for (CreationType item : CreationType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return item;
					}
				}
			}
			return null;
		}

		public static boolean contains(String value) {
			if (null != value) {
				for (CreationType item : CreationType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	public enum Location implements CCBaseEnum {
		OFF_SHORE(1, "Offshore"), ON_SHORE(2, "Onshore");

		private int id;
		private String value;

		private Location(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public int getId() {
			return id;
		}

		public String getValue() {
			return value;
		}

		public static Location getEnumObject(String value) {
			if (null != value) {
				for (Location item : Location.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return item;
					}
				}
			}
			return null;
		}

		public static boolean contains(String value) {
			if (null != value) {
				for (Location item : Location.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	public enum PersonPrefix implements CCBaseEnum {
		None(1, "None"), MR(2, "Mr"), MS(3, "Ms"), MRS(4, "Mrs");

		private int id;
		private String value;

		private PersonPrefix(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public int getId() {
			return id;
		}

		public String getValue() {
			return value;
		}

		public static PersonPrefix getEnumObject(String value) {
			if (null != value) {
				for (PersonPrefix item : PersonPrefix.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return item;
					}
				}
			}
			return null;
		}

		public static boolean contains(String value) {
			if (null != value) {
				for (PersonPrefix item : PersonPrefix.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}
}
