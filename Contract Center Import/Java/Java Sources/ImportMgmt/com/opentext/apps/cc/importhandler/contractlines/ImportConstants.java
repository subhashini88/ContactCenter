package com.opentext.apps.cc.importhandler.contractlines;

import com.opentext.apps.cc.importcontent.CommonEnums.CCBaseEnum;

public class ImportConstants {

	public static final String ENTITY_NAME = "ContractLine";
	public static final String SHEET_NAME = "ContractLine";
	public static final String CONTRACTS_IMPORT_NAMESPACE = "http://schemas.opentext.com/apps/contractcenterimport/16.3";
	public static final String LINE_ID = "LineId";
	public static final String CONTRACT_NUMBER = "ContractNumber";
	public static final String SERVICE = "Service";
	public static final String PRODUCT_GROUP = "ProductGroup";
	public static final String BREACH_COMMENTS = "BreachComments";
	public static final String CANCELLATION_COMMENTS = "CancellationComments";
	public static final String CANCALLATION_DATE = "CancellationDate";
	public static final String COMMUNICATION_DATE = "CommunicationDate";
	public static final String DESCRIPTION = "Description";
	public static final String INCIDENT_DATE = "IncidentDate";
	public static final String LINE_NUMBER = "LineNumber";
	public static final String PRICE_PROTECTION = "PriceProtection";
	public static final String PRICE_PROTECTION_DATE = "PriceProtectionDate";
	public static final String SKU_OR_SERVICE_ID = "SKUOrServiceId";
	public static final String STATUS = "Status";
	public static final String CONTRACT_LINE_NAME = "ContractLineName";
	public static final String PRICE = "Price";
	public static final String QUANTITY = "Quantity";
	public static final String UNIT_OF_MEASUREMENT = "UnitOfMeasurement";
	public static final String TRAIL_PERIOD = "TrialPeriod";
	public static final String CANCELLATION_NOTICE_PERIOD = "CancellationNoticePeriod";
	public static final String CANCELLATION_FEE = "CancellationFees";
	public static final String BREACH_NOTICE_PERIOD = "BreachNoticePeriod";
	public static final String BREACH_PENALITY = "BreachPenalty";
	public static final String CANCELLATION_REASON = "CancellationReason";
	public static final String PO_NUMBER = "PONumber";

	public enum Status implements CCBaseEnum {
		PENDING(1, "Pending"), ACTIVE(2, "Active"), CANCELLED(3,"Cancelled");

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

}
