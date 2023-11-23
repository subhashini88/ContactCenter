package com.opentext.apps.cc.importhandler.clause;

import com.opentext.apps.cc.importcontent.CommonEnums.CCBaseEnum;

public class ImportConstants {
	public static final String SHEET_NAME = "Clause";
	public static final String CLAUSE_LEGACYID = "LegacyID";

	public static final String CLAUSE_CLAUSETYPE = "ClauseType";
	public static final String CLAUSE_GLOBALCLAUSE = "GlobalClause";
	public static final String CLAUSE_NAME = "Name";
	public static final String CLAUSE_PLAINCONTENT = "PlainContent";
	public static final String CLAUSE_HTMLCONTENT = "HTMLContent";
	public static final String CLAUSE_DESCRIPTION = "Description";
	public static final String CLAUSE_COMMENTS = "Comments";
	public static final String CLAUSE_RELATEDCLAUSECATEGORY_ID = "RelatedClauseCategory";
	public static final String CLAUSE_RELATEDTYPE_ID = "RelatedType";
	public static final String CLAUSE_LIFECYCLE_STATE = "LifecycleState";

	public enum ClauseType implements CCBaseEnum {
		NON_STANDARD(1, "NONSTANDARD"), STANDARD(2, "STANDARD");

		private int id;
		private String value;

		private ClauseType(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public int getId() {
			return id;
		}

		public String getValue() {
			return value;
		}

		public static ClauseType getEnumObject(String value) {
			if (null != value) {
				for (ClauseType item : ClauseType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return item;
					}
				}
			}
			return null;
		}

		public static boolean contains(String value) {
			if (null != value) {
				for (ClauseType item : ClauseType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}
}
