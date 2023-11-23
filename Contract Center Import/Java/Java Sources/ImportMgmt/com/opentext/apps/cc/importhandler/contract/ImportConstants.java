package com.opentext.apps.cc.importhandler.contract;

import com.opentext.apps.cc.importcontent.CommonEnums.CCBaseEnum;

public class ImportConstants {
	public static final String IMPORT_CONTRACTS_SHEET_NAME = "Contract";

	// Import Contract Properties

	public static final String CONTRACT_NUMBER = "ContractNumber";
	public static final String CONTRACT_NAME = "ContractName";
	public static final String DESCRIPTION = "Description";
	public static final String CONTRACT_TERM = "InitialContractTenure";
	public static final String RENEWAL = "Renewal";
	public static final String AUTO_RENEW = "AutoRenew";
	public static final String AUTORENEW_DURATION = "AutoRenewDuration";
	public static final String RENEWAL_COMMENTS = "RenewalComments";
	public static final String DEAL_MANAGER = "DealManager";
	public static final String IS_EXECUTED = "IsExecuted";
	public static final String IS_EXTERNAL = "IsExternal";
	// static final String PO_NUMBER = "PONumber";
	public static final String PO_NUMBERS = "PONumbers";

	// Date attributes

	public static final String START_DATE = "StartDate";
	public static final String MIN_START_DATE = "MinStartdate";
	public static final String END_DATE = "InitialExpiryDate";
	public static final String INACTIVATION_DATE = "InactivationDate";
	public static final String CANCELLATION_DATE = "CancellationDate";
	public static final String SIGNATURE_DATE = "SignatureDate";
	public static final String MAINTADJCOMMENCEMENT_DATE = "MaintAdjCommencementDate";
	public static final String NEXT_EXPIRATION_DATE = "NextExpirationDate";
	public static final String CURRENT_START_DATE = "CurrentStartDate";
	public static final String CURRENT_EXP_DATE = "CurrentExpDate";
	public static final String PRODUCT_GOLIVE_DATE = "ProductGoLiveDate";
	public static final String PRICEPROTECTION_DATE = "PriceProtectiondate";
	public static final String VALIDATED_ON = "ValidatedOn";

	// Date attributes that are not being imported in BN

	public static final String ACTIONDURATION = "ActionDuration";

	// Fee Related attributes
	public static final String SOFTWARE_FEES = "SoftwareFees";
	public static final String UPFRONT_FEE = "UpfrontFees";
	public static final String PROFESSIONALSERVICES_FEES = "ProfessionalServicesFees";
	public static final String MAINTENANCE_FEE = "MaintenanceFees";
	public static final String TERMINATION_FEE = "TerminationFees";
	public static final String MIN_CONTRACT_VALUE = "MinimumContractValue";

	// Duration attributes
	public static final String SW_MAINTFEE_FOR_RENEWAL_TERM = "SWMaintFeesForRenewalTerm";
	public static final String MAINT_FEEADJ_DURING_CURRENTTERM = "MaintFeeAdjDuringCurrentTerm";
	public static final String SOFTWARE_LICENSE_TERM = "SoftwareLicenseTerm";

	// Duration attributes that are not being imported in BN

	public static final String NOTIFICATION_DURATION = "NotificationDuration";

	// Other attributes
	public static final String DEAL_MARGIN = "DealMargin";
	public static final String CRMOPPURTUNITY_ID = "CRMOpportunityID";
	public static final String CRMQUOTE_ID = "CRMQuoteID";
	public static final String PSPROJECT_ID = "PSProjectID";
	public static final String RELATED_TO_DOCUMENT_ID = "RelatedToDocumentID";
	public static final String CLIENT_EARLYTERM_RIGHT = "ClientEarlyTermRight";
	public static final String END_USER = "EndUser";
	public static final String AMENDMENT_SEQUENCE = "AmendmentSequence";
	public static final String TRAIL_PERIOD = "TrialPeriod";
	public static final String TERMINATION_NOTICE_PERIOD = "TerminationNoticePeriod";
	public static final String COMMENTS = "Comments";
	public static final String CANCELLATION_COMMENTS = "CancellationComments";
	public static final String BILLING_STATUS = "BillingStatus";
	public static final String PRICEPROTECTION = "PriceProtection";
	public static final String REVENUE_IMPACTIONG = "RevenueImpacting";
	public static final String VALIDATED_FLAG = "Validated";
	public static final String PREVIOUS_DOC_ID = "PreviousDocumentId";
	public static final String PO_REQD = "POReqd";
	public static final String SAP_ORDER = "SAPOrder";

	// Other attributes that are not being imported in BN

	public static final String ACTION_DATE_NOTIFY_VALUE = "ActionDateNotifyValue";
	public static final String AMENDMENT = "Amendment";
	public static final String AMENDMENT_COMMENTS = "AmendmentComments";
	public static final String AMENDMENT_TYPE = "AmendmentType";
	public static final String BILLING_ACCOUNT = "BillingAccount";
	public static final String CHILD_TYPE = "ChildType";
	public static final String CONTRACT_VALUE = "ContractValue";
	public static final String CURRENT_SALES_ACCOUNT_EXECUTIVE = "CurrentSalesAccountExecutive";
	public static final String CUSTOMER_MANAGER_COMMENTS = "CustomerManagerComments";
	public static final String DEFAULT_DOC_ID = "DefaultDocId";
	public static final String DISTRIBUTION_COMMENTS = "DistributionComments";
	public static final String EARLY_TERMINATION_CONDITIONS = "EarlyTerminationConditions";
	public static final String EMAIL = "EMail";
	public static final String INTENT_TYPE = "IntentType";
	public static final String NOTIFICATION_DATE_NOTIFY_VALUE = "NotificationDateNotifyValue";
	public static final String ORIGINAL_SALES_ACCOUNT_EXECUTIVE = "OriginalSalesAccountExecutive";
	public static final String OVERRIDE_DEFAULT_EMAIL_TEMPLATE = "OverrideDefaultEmailTemplate";

	public static final String PARENT_TYPE = "ParentType";
	public static final String PHONE = "Phone";
	public static final String PREVIOUS_CONTRACT_ID = "PreviousContractID";
	public static final String PRIORITY = "Priority";
	public static final String RENEWAL_DISCOUNT = "RenewalDiscount";
	public static final String STATUS = "Status";
	public static final String TEMPLATE_TYPE = "TemplateType";
	public static final String TERMINATION_COMMENTS = "TerminationComments";
	public static final String TERMINATION_DATE = "TerminationDate";

	// Lookup attributes
  /*public static final String FIRST_PARTY_ID = "ContractingEntity";
	public static final String FIRSTPARTY_CONTACT_ID = "InternalContact";
	public static final String SECONDPARTY_CONTACT_ID = "ExternalContact";*/
	public static final String SECOND_PARTY_ID = "Client";
	
	public static final String CONTRACT_TYPE_ID = "ContractType";
	public static final String CONTRACT_SUBTYPE_ID = "ContractSubtype";
	public static final String COUNTRY_ID = "Country";
	public static final String CURRENCY_ID = "Currency";
	public static final String RENEWALFLAG_STATUS_ID = "RenewalFlagStatus";
	public static final String CANCEL_REASONCODE_ID = "CancelReasonCode";
	public static final String CANCEL_REASON_DETAIL_ID = "CancelReasonDetail";
	public static final String COMPETITOR_ID = "Competitor";
	public static final String PRODUCT_ADOPTED_ID = "ProductAdopted";
	public static final String CANCELLATION_TYPE_ID = "CancellationType";
	public static final String STATUS_OUTCOME_ID = "StatusOutcome";
	public static final String DOCUMENT_TYPE_ID = "DocumentType";
	public static final String DOCUMENT_ORIGINATION_ID = "DocumentOrigination";
	public static final String CONTRACT_STATUS_ID = "ContractStatus";
	public static final String VALIDATED_BY = "ValidatedBy";
	public static final String TERMINATION_REASON_ID = "TerminationReason";
	public static final String TEMPLATE_ID = "RelatedTemplate";
	public static final String ORGANIZATION_ID = "Organization";
	public static final String EXTERNAL_PARTIES_AND_CONTACTS = "ExternalPartiesAndContacts";
	public static final String INTERNAL_PARTIES_AND_CONTACTS = "InternalPartiesAndContacts";
	public static final String PARTY_DELIMITER_DEFAULT =";";
	public static final String CONTACT_DELIMITER_DEFAULT = ":";
	public static final String PO_DELIMITER_DEFAULT = ";";
	public static final String PARTY_DELIMITER ="PartiesDelimiter";
	public static final String CONTACT_DELIMITER = "ContactsDelimiter";
	public static final String PO_DELIMITER = "PODelimiter";

	// public static final String CANCELLATION_REASONCODE_ID =
	// "CancellationReasonCode";CancelReasonCode
	// public static final String BILLING_STATUSCHANGE_DATE =
	// "BillingStatusChangeDate"; InactivationDate

	// public static final String LANGUAGE_ID = "Language";
	// public static final String SECONDPARTY_CONTACT_ID = "SecondPartyContact";
	// public static final String UNITOF_MEASUREMENT_ID = "UnitOfMeasurement";
	// public static final String ENTITY_NAME = "ContractImportTable";

	// Extra attributes for import
	public static final String TARGET_STATE = "TargetState";
	public static final String PERPETUAL = "Perpetual";
	//public static final String FIRST_PARTY_REG_ID = "ContractingEntityId";
	//public static final String SECOND_PARTY_REG_ID = "ClientId";

	// Used in batch upload processing
	public static final String OVERRIDE_MATCH_DOCUMENT_ID = "OverrideMatchDocumentID";
	public static final String COLLECTION_ACCOUNT = "Account#";
	public static final String BILLER_DATA_DATE = "BillerDataDate";
	public static final String CONTRACT_ITEM_ID = "ContractItemId";
	public static final String FORCE_CREATION = "ForceCreation";

	public static final String ACTION = "Action";

	public static final String ACTION_CREATE = "Create";
	public static final String ACTION_UPDATE = "Update";
	public static final String Y_FORCE_CREATION = "Y";

	public static final String CONTRACTS_CENTER_NAMESPACE = "http://schemas/OpenTextContractCenter/Contract/operations";
	public static final String CURRENCY_NAMESPACE = "http://schemas/OpenTextContractCenter/Currency/operations";
	public static final String CONTRACTS_IMPORT_NAMESPACE = "http://schemas.opentext.com/apps/contractcenterimport/16.3";
	public static final String CONTRACT_BILLING_STATUS_ACTIVE = "Active";

	public static final String CONTRACT_UPDATE_MESSAGE = "Updated contract having id: ";
	public static final String CONTRACT_CREATE_MESSAGE = "Created new contract";

	public enum AmendmentType implements CCBaseEnum {

		AMENDMENT_AGREEMENT(1, "Amendment agreement"), RENEWAL(2, "Renewal");

		private int id;
		private String value;

		private AmendmentType(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public int getId() {
			return id;
		}

		public String getValue() {
			return value;
		}

		public static AmendmentType getEnumObject(String value) {
			if (null != value) {
				for (AmendmentType item : AmendmentType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return item;
					}
				}
			}
			return null;
		}

		public static boolean contains(String value) {
			if (null != value) {
				for (AmendmentType item : AmendmentType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	public enum BillingStatus implements CCBaseEnum {

		ACTIVE(1, "Active"), IN_ACTIVE(2, "InActive");

		private int id;
		private String value;

		private BillingStatus(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public int getId() {
			return id;
		}

		public String getValue() {
			return value;
		}

		public static BillingStatus getEnumObject(String value) {
			if (null != value) {
				for (BillingStatus item : BillingStatus.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return item;
					}
				}
			}
			return null;
		}

		public static boolean contains(String value) {
			if (null != value) {
				for (BillingStatus item : BillingStatus.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	public enum ChildType implements CCBaseEnum {

		AMEND(1, "Amend"), RENEW(2, "Renew"), CANCEL(3, "Cancel"), DEFAULT(4,"");

		private int id;
		private String value;

		private ChildType(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public int getId() {
			return id;
		}

		public String getValue() {
			return value;
		}

		public static ChildType getEnumObject(String value) {
			if (null != value) {
				for (ChildType item : ChildType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return item;
					}
				}
			}
			return null;
		}

		public static boolean contains(String value) {
			if (null != value) {
				for (ChildType item : ChildType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	public enum IntentType implements CCBaseEnum {

		SELL(1, "Sell"), BUY(2, "Buy"), OTHER(3, "Other");

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

	public enum ParentType implements CCBaseEnum {

		AMEND(1, "Amend"), RENEW(2, "Renew"), CANCEL(3, "Cancel"), DEFAULT(4, "");

		private int id;
		private String value;

		private ParentType(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public int getId() {
			return id;
		}

		public String getValue() {
			return value;
		}

		public static ParentType getEnumObject(String value) {
			if (null != value) {
				for (ParentType item : ParentType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return item;
					}
				}
			}
			return null;
		}

		public static boolean contains(String value) {
			if (null != value) {
				for (ParentType item : ParentType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	public enum Priority implements CCBaseEnum {

		HIGH(1, "High"), MEDIUM(2, "Medium"), LOW(3, "Low");

		private int id;
		private String value;

		private Priority(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public int getId() {
			return id;
		}

		public String getValue() {
			return value;
		}

		public static Priority getEnumObject(String value) {
			if (null != value) {
				for (Priority item : Priority.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return item;
					}
				}
			}
			return null;
		}

		public static boolean contains(String value) {
			if (null != value) {
				for (Priority item : Priority.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	public enum TemplateType implements CCBaseEnum {

		INTERNAL_TEMPLATE(1, "Internal template"), INTERNAL_PARTY_DOCUMENT(2, "Internal party document"),
		EXTERNAL_PARTY_DOCUMENT(3, "External party document");

		private int id;
		private String value;

		private TemplateType(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public int getId() {
			return id;
		}

		public String getValue() {
			return value;
		}

		public static TemplateType getEnumObject(String value) {
			if (null != value) {
				for (TemplateType item : TemplateType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return item;
					}
				}
			}
			return null;
		}

		public static boolean contains(String value) {
			if (null != value) {
				for (TemplateType item : TemplateType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}
}
