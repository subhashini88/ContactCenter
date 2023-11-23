package com.opentext.apps.cc.importhandler.revenueschedule;

import com.opentext.apps.cc.importcontent.CommonEnums.CCBaseEnum;

public class ImportConstants {
	public static final String ENTITY_NAME = "RevenueSchedule";// Revenue schedule Id
	public static final String ID1 = "RevenueScheduleId";// Revenue schedule Id
	public static final String ID = "ContractNumber";// Contract Id
	public static final String SHEET_NAME = "RevenueSchedule";
	public static final String START_DATE = "Startdate";
	public static final String END_DATE = "EndDate";
	public static final String BILLING_TYPE = "BillingType";
	public static final String MONTH_FROM = "MonthFrom";
	public static final String MONTH_TO = "MonthTo";
	public static final String PROCESS_FEES = "ProcessFees";
	public static final String RTSORPSSUPPORT_FEES = "RTSOrPSSupportFees";
	public static final String DEDICATION_SUPPORT = "DedicationSupport";
	public static final String TOTAL_VALUE = "TotalValue";
	public static final String PERIOD_LEVEL = "PeriodLevel";
	public static final String VOLUME_VALUE = "VolumeValue";
	public static final String VOLUME_UNIT = "VolumeUnit";
	public static final String CONTRACTS_IMPORT_NAMESPACE = "http://schemas.opentext.com/apps/contractcenterimport/16.3";

	enum BillingType implements CCBaseEnum {

		ANNUAL(1, "Annual"), MONTHLY(2, "Monthly"), QUARTERLY(3, "Quarterly");

		private int id;
		private String value;

		private BillingType(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public int getId() {
			return id;
		}

		public String getValue() {
			return value;
		}

		public static BillingType getEnumObject(String value) {
			if (null != value) {
				for (BillingType item : BillingType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return item;
					}
				}
			}
			return null;
		}

		public static boolean contains(String value) {
			if (null != value) {
				for (BillingType item : BillingType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}
}
