package com.opentext.apps.cc.importhandler.collectionaccount;

import com.opentext.apps.cc.importcontent.CommonEnums.CCBaseEnum;

public class ImportConstants {

	public static final String SHEET_NAME = "AccountNumber";
	public static final String LEGACY_ID = "LegacyID";
	public static final String COLLECTION_ACCOUNT = "AccountNumber";
	public static final String MANAGER_ID = "ManagerID";
	public static final String PARTY_REG_ID = "PartyRegistrationID";
	public static final String SAP_PAYER_ACCOUNT = "SAPPayer";
	public static final String SAP_SOLD_TO_ACCOUNT = "SAPSoldTo";
	public static final String STATUS = "Status";
	public static final String CODE = "Code";
	public static final String LAST_UPDATION_JOB = "LastUpdationJob";
	public static final String LAST_VERIFICATION_JOB = "LastVerificationJob";
	public static final String NAME = "Name";
	public static final String UPDATE_STATUS = "UpdateStatus";

	enum UpdateStatus implements CCBaseEnum {

		MANAGER_UPDATED(1, "ManagerUpdated"), UPDATE_FAILED(2, "UpdateFailed"), NO_CHANGE(3, "NoChange"),
		PERSON_NOT_FOUND(4, "PersonNotFound"), PERSON_UPDATED(5, "PersonUpdated"), NO_DATA_FOUND(6, "NoDataFound"),
		MANAGER_CREATED(7, "ManagerCreated");

		private int id;
		private String value;

		private UpdateStatus(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public int getId() {
			return id;
		}

		public String getValue() {
			return value;
		}

		public static UpdateStatus getEnumObject(String value) {
			if (null != value) {
				for (UpdateStatus item : UpdateStatus.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return item;
					}
				}
			}
			return null;
		}

		public static boolean contains(String value) {
			if (null != value) {
				for (UpdateStatus item : UpdateStatus.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}
}