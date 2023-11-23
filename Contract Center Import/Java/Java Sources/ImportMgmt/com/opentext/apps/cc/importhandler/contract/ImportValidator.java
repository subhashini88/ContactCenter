package com.opentext.apps.cc.importhandler.contract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.internal.NOMDocumentPool;
import com.eibus.xml.xpath.XPath;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportUtils;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.contract.ImportConstants.AmendmentType;
import com.opentext.apps.cc.importhandler.contract.ImportConstants.BillingStatus;
import com.opentext.apps.cc.importhandler.contract.ImportConstants.ChildType;
import com.opentext.apps.cc.importhandler.contract.ImportConstants.IntentType;
import com.opentext.apps.cc.importhandler.contract.ImportConstants.ParentType;
import com.opentext.apps.cc.importhandler.contract.ImportConstants.Priority;
import com.opentext.apps.cc.importhandler.contract.ImportConstants.TemplateType;

public class ImportValidator {
	private final Set<String> yesOrNoAttrs;
	private final Set<String> durationAttrs;
	private final Set<String> integerAttrs;
	private final Set<String> decimalAttrs;
	private final Set<String> dateAttrs;
	private final Set<String> trueOrFalseAttrs;
	private Properties mappingProperties;

	public ImportValidator() {
		yesOrNoAttrs = getYesOrNoAttrs();
		durationAttrs = getDurationAttrs();
		integerAttrs = getIntegerAttrs();
		decimalAttrs = getDecimalAttrs();
		dateAttrs = getDateAttrs();
		trueOrFalseAttrs = getTrueOrFalseAttrs();
	}

	private boolean getMetadataStatus(String contractNumber, String jobID) {
		int contractNumberNode = 0, statusItemIdResponse = 0, updateImportStatusNode = 0, updateDataStatusNode = 0,
				updateContractImportStatusResponse = 0, jobIDNode = 0;
		String metadataStatus = null;
		try {
			SOAPRequestObject statusItemIdRequest = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/contractcenterimport/16.3",
					"ContractImportStatusContractNumberFilter", null, null);
			contractNumberNode = NomUtil.parseXML("<ContractNumber>" + contractNumber + "</ContractNumber>");
			jobIDNode = NomUtil.parseXML("<JobID>" + jobID + "</JobID>");
			statusItemIdRequest.addParameterAsXml(contractNumberNode);
			statusItemIdRequest.addParameterAsXml(jobIDNode);
			statusItemIdResponse = statusItemIdRequest.sendAndWait();
			metadataStatus = Node.getDataWithDefault(NomUtil.getNode(".//ContractMetadataStatus", statusItemIdResponse),
					null);
		} catch (Exception e) {
		} finally {
			Utilities.cleanAll(contractNumberNode, updateImportStatusNode, updateDataStatusNode, statusItemIdResponse,
					updateContractImportStatusResponse, jobIDNode);
		}
		if (null != metadataStatus && metadataStatus.equals("Completed")) {
			return true;
		} else {
			return false;
		}
	}

	public ReportItem validate(Map<String, String> rowData, MetadataInitializer metadata, String jobId,
			Properties properties) {
		ReportItem report = new ReportItem();
		if (rowData == null)
			return report;
		if (!getMetadataStatus(rowData.get(ImportConstants.CONTRACT_NUMBER), jobId)) {
			this.mappingProperties = properties;
			validateDateAttrs(rowData, report);
			validateTrueOrFalseAttrs(rowData, report);
			validateYesOrNoAttrs(rowData, report);
			validateIntergerAttrs(rowData, report);
			validateDurationAttrs(rowData, report);
			validateDecimalAttrs(rowData, report);
			validateLookupAttrs(rowData, metadata, report);
			validateState(rowData, report);

			validateEnumerations(rowData, report);
		}
		return report;
	}

	protected String getMappingPropertyValue(String name) {
		String propertyVal = null;
		propertyVal = (String) mappingProperties.get(name);
		if (propertyVal == null) {
			switch (name) {
			case "PartiesDelimiter":
				propertyVal = ImportConstants.PARTY_DELIMITER_DEFAULT;
				break;
			case "ContactsDelimiter":
				propertyVal = ImportConstants.CONTACT_DELIMITER_DEFAULT;
				break;
			case "PODelimiter":
				propertyVal = ImportConstants.PO_DELIMITER_DEFAULT;
				break;
			}
		}
		return propertyVal;
	}

	private void validateEnumerations(Map<String, String> rowData, ReportItem report) {

		// Enumeration validations

		// Amendment type.
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.AMENDMENT_TYPE))) {
			String value = rowData.get(ImportConstants.AMENDMENT_TYPE);
			if (!AmendmentType.contains(value)) {
				report.error(ImportConstants.AMENDMENT_TYPE,
						rowData.get(ImportConstants.AMENDMENT_TYPE) + "' is not valid");
			}
		}

		// Billing status.
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.BILLING_STATUS))) {
			String value = rowData.get(ImportConstants.BILLING_STATUS);
			if (!BillingStatus.contains(value)) {
				report.error(ImportConstants.BILLING_STATUS,
						rowData.get(ImportConstants.BILLING_STATUS) + "' is not valid");
			}
		}

		// Child type.
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CHILD_TYPE))) {
			String value = rowData.get(ImportConstants.CHILD_TYPE);
			if (!ChildType.contains(value)) {
				report.error(ImportConstants.CHILD_TYPE, rowData.get(ImportConstants.CHILD_TYPE) + "' is not valid");
			}
		}

		// Intent type.
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.INTENT_TYPE))) {
			String value = rowData.get(ImportConstants.INTENT_TYPE);
			if (!IntentType.contains(value)) {
				report.error(ImportConstants.INTENT_TYPE, rowData.get(ImportConstants.INTENT_TYPE) + "' is not valid");
			}
		}

		// Parent type.
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.PARENT_TYPE))) {
			String value = rowData.get(ImportConstants.PARENT_TYPE);
			if (!ParentType.contains(value)) {
				report.error(ImportConstants.PARENT_TYPE, rowData.get(ImportConstants.PARENT_TYPE) + "' is not valid");
			}
		}

		// Priority.
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.PRIORITY))) {
			String value = rowData.get(ImportConstants.PRIORITY);
			if (!Priority.contains(value)) {
				report.error(ImportConstants.PRIORITY, rowData.get(ImportConstants.PRIORITY) + "' is not valid");
			}
		}

		// Template type.
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.TEMPLATE_TYPE))) {
			String value = rowData.get(ImportConstants.TEMPLATE_TYPE);
			if (!TemplateType.contains(value)) {
				report.error(ImportConstants.TEMPLATE_TYPE,
						rowData.get(ImportConstants.TEMPLATE_TYPE) + "' is not valid");
			}
		}
	}

	private void validateState(Map<String, String> rowData, ReportItem report) {
		if (ContractState.searchValidState(rowData.get(ImportConstants.TARGET_STATE)) == null) {
			report.error(ImportConstants.TARGET_STATE,
					"Given target state '" + rowData.get(ImportConstants.TARGET_STATE) + "' is invalid");
		} else if ("terminated".equalsIgnoreCase(rowData.get(ImportConstants.TARGET_STATE))
				&& Utilities.isStringEmpty(rowData.get(ImportConstants.TERMINATION_DATE))) {
			report.error(ImportConstants.TERMINATION_DATE,
					"Termination date must be specified for terminated contracts");
		}
	}

	private Set<String> getDateAttrs() {
		Set<String> dateAttrs = new TreeSet<>();
		Collections.addAll(dateAttrs,
				new String[] { ImportConstants.START_DATE, ImportConstants.MIN_START_DATE, ImportConstants.END_DATE,
						ImportConstants.INACTIVATION_DATE, ImportConstants.CANCELLATION_DATE,
						ImportConstants.SIGNATURE_DATE, ImportConstants.NEXT_EXPIRATION_DATE,
						ImportConstants.MAINTADJCOMMENCEMENT_DATE, ImportConstants.NEXT_EXPIRATION_DATE,
						ImportConstants.CURRENT_START_DATE, ImportConstants.CURRENT_EXP_DATE,
						ImportConstants.PRODUCT_GOLIVE_DATE, ImportConstants.PRICEPROTECTION_DATE,
						ImportConstants.VALIDATED_ON, ImportConstants.TERMINATION_DATE });
		return dateAttrs;
	}

	public boolean isValidDate(String string) {
		boolean isValid = false;
		if (string == null) {
			return false;
		}
		try {
			ImportUtils.sdf.parse(string);
			isValid = true;
		} catch (ParseException e) {
			isValid = false;
		}
		return isValid;
	}

	private void validateDateAttrs(Map<String, String> rowData, ReportItem report) {
		for (String dateAttrName : dateAttrs) {
			String dateAttrValue = rowData.get(dateAttrName);
			if (!Utilities.isStringEmpty(dateAttrValue) && !isValidDate(dateAttrValue)) {
				report.error(dateAttrName, String
						.format("The mentioned %s is not in a valid format ('yyyy-MM-dd hh:mm:ss')", dateAttrName));
			}
		}
		if (Utilities.isStringEmpty(rowData.get(ImportConstants.START_DATE))) {
			report.error(ImportConstants.START_DATE, "Mandatory information 'Start date' is missing in excel");
		}

		// Perpetual ---> Term/Enddate
		if ("true".equalsIgnoreCase(rowData.get(ImportConstants.PERPETUAL))) {
			if (!Utilities.isStringEmpty(rowData.get(ImportConstants.END_DATE))) {
				report.error(ImportConstants.PERPETUAL,
						"InitialExpiryDate must not be specified for perpetual contracts.");
			}
			if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CONTRACT_TERM))) {
				report.error(ImportConstants.CONTRACT_TERM,
						"InitialContractTenure must not be specified for perpetual contracts.");
			}
		}
		if ("false".equalsIgnoreCase(rowData.get(ImportConstants.PERPETUAL))
				|| Utilities.isStringEmpty(rowData.get(ImportConstants.PERPETUAL))) {
			try {
				if (!Utilities.isStringEmpty(rowData.get(ImportConstants.END_DATE))) {
					Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(rowData.get(ImportConstants.START_DATE));
					Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(rowData.get(ImportConstants.END_DATE));
					int termMonths = Integer.parseInt(rowData.get(ImportConstants.CONTRACT_TERM));
					Calendar startDatePlusTerm = Calendar.getInstance();
					startDatePlusTerm.setTime(startDate);
					Calendar endDateC = Calendar.getInstance();
					endDateC.setTime(endDate);
					startDatePlusTerm.add(Calendar.MONTH, +termMonths);
					startDatePlusTerm.add(Calendar.DATE, -1);
					if ((!(startDatePlusTerm.get(Calendar.YEAR) == endDateC.get(Calendar.YEAR)))
							|| (!(startDatePlusTerm.get(Calendar.MONTH) == endDateC.get(Calendar.MONTH)))
							|| (!(startDatePlusTerm.get(Calendar.DATE) == endDateC.get(Calendar.DATE)))) {
						report.error(ImportConstants.END_DATE,
								"Provided " + ImportConstants.END_DATE + " is wrong. " + ImportConstants.END_DATE
										+ " must be a sum of " + ImportConstants.START_DATE + " and "
										+ ImportConstants.CONTRACT_TERM + ".");
					}
				}
				if (Utilities.isStringEmpty(rowData.get(ImportConstants.CONTRACT_TERM))) {
					report.error(ImportConstants.CONTRACT_TERM,
							"InitialContractTenure must be specified for non-perpetual contracts.");
				}
			} catch (ParseException e) {
				if (!Utilities.isStringEmpty(rowData.get(ImportConstants.END_DATE))) {
					report.error(ImportConstants.END_DATE,
							String.format("The mentioned value of " + ImportConstants.END_DATE + " is not valid.",
									rowData.get(ImportConstants.END_DATE)));
				}
			}
		}
		if (Utilities.isStringEmpty(rowData.get(ImportConstants.PERPETUAL))) {
			if (Utilities.isStringEmpty(rowData.get(ImportConstants.CONTRACT_TERM))) {
				report.error(ImportConstants.CONTRACT_TERM,
						"Since Perpetual is 'FALSE' by default, contract should have a CONTRACT_TERM value provided.");
			}
		}
	}

	private Set<String> getTrueOrFalseAttrs() {
		Set<String> trueOrFalseAttrs = new TreeSet<>();
		trueOrFalseAttrs.add(ImportConstants.AUTO_RENEW);
		trueOrFalseAttrs.add(ImportConstants.RENEWAL);
		trueOrFalseAttrs.add(ImportConstants.CLIENT_EARLYTERM_RIGHT);
		trueOrFalseAttrs.add(ImportConstants.VALIDATED_FLAG);
		trueOrFalseAttrs.add(ImportConstants.AMENDMENT);
		trueOrFalseAttrs.add(ImportConstants.OVERRIDE_DEFAULT_EMAIL_TEMPLATE);
		trueOrFalseAttrs.add(ImportConstants.PERPETUAL);
		trueOrFalseAttrs.add(ImportConstants.IS_EXECUTED);
		return trueOrFalseAttrs;
	}

	private boolean isNotTrueOrFalse(String value) {
		return (!"TRUE".equalsIgnoreCase(value)) && (!"FALSE".equalsIgnoreCase(value));
	}

	private void validateTrueOrFalseAttrs(Map<String, String> rowData, ReportItem report) {
		for (String trueOrFalseAttrName : trueOrFalseAttrs) {
			String trueOrFalseAttrValue = rowData.get(trueOrFalseAttrName);
			if (!Utilities.isStringEmpty(trueOrFalseAttrValue) && isNotTrueOrFalse(trueOrFalseAttrValue)) {
				report.error(trueOrFalseAttrName,
						String.format("The value of %s should either be true or false", trueOrFalseAttrName));
			}
		}
	}

	private Set<String> getYesOrNoAttrs() {
		Set<String> yesOrNoAttrs = new TreeSet<>();
		yesOrNoAttrs.add(ImportConstants.REVENUE_IMPACTIONG);
		return yesOrNoAttrs;
	}

	private boolean isNotYesOrNo(String value) {
		return (!"YES".equalsIgnoreCase(value)) && (!"NO".equalsIgnoreCase(value));
	}

	private void validateYesOrNoAttrs(Map<String, String> rowData, ReportItem report) {
		for (String yesOrNoAttrName : yesOrNoAttrs) {
			String yesOrNoAttrValue = rowData.get(yesOrNoAttrName);
			if (!Utilities.isStringEmpty(yesOrNoAttrValue) && isNotYesOrNo(yesOrNoAttrValue)) {
				report.error(yesOrNoAttrName,
						String.format("The value of %s should either be yes or no", yesOrNoAttrName));
			}
		}
		// Auto renewal ---> Auto renew duration
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.AUTO_RENEW))) {
			if (rowData.get(ImportConstants.AUTO_RENEW).equals("true")) {
				if (Utilities.isStringEmpty(rowData.get(ImportConstants.AUTORENEW_DURATION))) {
					report.error(ImportConstants.AUTORENEW_DURATION,
							"AutoRenewDuration must be specified for automatically renewed contracts.");
				}
			}
			if (rowData.get(ImportConstants.AUTO_RENEW).equals("false")) {
				if (!Utilities.isStringEmpty(rowData.get(ImportConstants.AUTORENEW_DURATION))) {
					report.error(ImportConstants.AUTORENEW_DURATION,
							"AutoRenewDuration must be specified for contracts with AutoRenew 'true'.");
				}
			}
		} else if (Utilities.isStringEmpty(rowData.get(ImportConstants.AUTO_RENEW))) {
			if (!Utilities.isStringEmpty(rowData.get(ImportConstants.AUTORENEW_DURATION))) {
				report.error(ImportConstants.AUTORENEW_DURATION,
						"AutoRenew must be specified for contracts with auto renew duration.");
			}
		}

		// PriceProtection ---> PriceProtectionDate
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.PRICEPROTECTION))) {
			if (rowData.get(ImportConstants.PRICEPROTECTION).equals("1")) {
				if (Utilities.isStringEmpty(rowData.get(ImportConstants.PRICEPROTECTION_DATE))) {
					report.error(ImportConstants.PRICEPROTECTION_DATE,
							"PriceProtection should have a PRICEPROTECTION_DATE value.");
				}
			}
			if (rowData.get(ImportConstants.PRICEPROTECTION).equals("0")) {
				if (!Utilities.isStringEmpty(rowData.get(ImportConstants.PRICEPROTECTION_DATE))) {
					report.error(ImportConstants.PRICEPROTECTION_DATE,
							"PRICEPROTECTION_DATE value must not be provided for contract with PriceProtection as '0'.");
				}
			}
		} else if (Utilities.isStringEmpty(rowData.get(ImportConstants.PRICEPROTECTION))) {
			if (!Utilities.isStringEmpty(rowData.get(ImportConstants.PRICEPROTECTION_DATE))) {
				report.error(ImportConstants.PRICEPROTECTION_DATE,
						"To Provide price protection date, please provide price protection as '1', as it is '0' by default.");
			}
		}

		// Validated ---> Validated On,By, Comments
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.VALIDATED_FLAG))) {
			if (rowData.get(ImportConstants.VALIDATED_FLAG).equals("true")) {
				if (Utilities.isStringEmpty(rowData.get(ImportConstants.VALIDATED_BY))) {
					report.error(ImportConstants.VALIDATED_FLAG, "Validated contract must have a 'ValidatedBy' value.");
				}
				if (Utilities.isStringEmpty(rowData.get(ImportConstants.VALIDATED_ON))) {
					report.error(ImportConstants.VALIDATED_FLAG, "Validated contract must have a 'ValidatedOn' value.");
				}
			}
			if (rowData.get(ImportConstants.VALIDATED_FLAG).equals("false")) {
				if (!Utilities.isStringEmpty(rowData.get(ImportConstants.VALIDATED_BY))) {
					report.error(ImportConstants.VALIDATED_BY,
							"ValidatedBy should be provided to a validated contract.");
				}
				if (!Utilities.isStringEmpty(rowData.get(ImportConstants.VALIDATED_ON))) {
					report.error(ImportConstants.VALIDATED_ON,
							"ValidatedOn should be provided to a validated contract.");
				}
				if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CUSTOMER_MANAGER_COMMENTS))) {
					report.error(ImportConstants.CUSTOMER_MANAGER_COMMENTS,
							"CustomerManagerComments should be provided to a validated contract.");
				}
			}
		} else if (Utilities.isStringEmpty(rowData.get(ImportConstants.VALIDATED_FLAG))) {
			if (!Utilities.isStringEmpty(rowData.get(ImportConstants.VALIDATED_BY))) {
				report.error(ImportConstants.VALIDATED_BY,
						"Since Validated is 'FALSE' by default, ValidatedBy can be provided to a validated contract.");
			}
			if (!Utilities.isStringEmpty(rowData.get(ImportConstants.VALIDATED_ON))) {
				report.error(ImportConstants.VALIDATED_ON,
						"Since Validated is 'FALSE' by default, ValidatedOn can be provided to a validated contract.");
			}
			if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CUSTOMER_MANAGER_COMMENTS))) {
				report.error(ImportConstants.CUSTOMER_MANAGER_COMMENTS,
						"Since Validated is 'FALSE' by default, CustomerManagerComments can be provided to a validated contract.");
			}
		}
	}

	private Set<String> getIntegerAttrs() {
		Set<String> integerAttrs = new TreeSet<>();
		Collections.addAll(integerAttrs, new String[] { ImportConstants.AMENDMENT_SEQUENCE,
				ImportConstants.TERMINATION_NOTICE_PERIOD, ImportConstants.SOFTWARE_LICENSE_TERM });
		return integerAttrs;
	}

	private void validateIntergerAttrs(Map<String, String> rowData, ReportItem report) {
		for (String intergerAttrName : integerAttrs) {
			String integerAttrValue = rowData.get(intergerAttrName);
			try {
				if (!Utilities.isStringEmpty(integerAttrValue)) {
					Integer.parseInt(integerAttrValue);
				}
			} catch (NumberFormatException e) {
				report.error(intergerAttrName,
						String.format("The mentioned value of %s is not integer", intergerAttrName));
			}
		}
	}

	private Set<String> getDurationAttrs() {
		Set<String> durationAttrs = new TreeSet<>();
		Collections.addAll(durationAttrs,
				new String[] { ImportConstants.CONTRACT_TERM, ImportConstants.AUTORENEW_DURATION, });
		return durationAttrs;
	}

	private void validateDurationAttrs(Map<String, String> rowData, ReportItem report) {
		for (String durationAttrName : durationAttrs) {
			String durationAttrValue = rowData.get(durationAttrName);
			if (!Utilities.isStringEmpty(durationAttrValue)) {
				try {
					Integer.parseInt(durationAttrValue);
					rowData.replace(durationAttrName, "P" + durationAttrValue + "M");
				} catch (Exception e) {
					Pattern durationPatternMD = Pattern.compile("^P\\d{1,3}M\\d{1,3}D$", Pattern.CASE_INSENSITIVE);
					Pattern durationPatternM = Pattern.compile("^P\\d{1,3}M$", Pattern.CASE_INSENSITIVE);
					Pattern durationPatternD = Pattern.compile("^P\\d{1,3}D$", Pattern.CASE_INSENSITIVE);
					if (!(durationPatternMD.matcher(durationAttrValue).find()
							|| durationPatternM.matcher(durationAttrValue).find()
							|| durationPatternD.matcher(durationAttrValue).find())) {
						report.error(durationAttrName,
								String.format("The mentioned value of %s is not duration", durationAttrName));
					}
				}
			}
		}
	}

	private Set<String> getDecimalAttrs() {
		Set<String> decimalAttrs = new TreeSet<>();
		Collections.addAll(decimalAttrs, new String[] { ImportConstants.SOFTWARE_FEES, ImportConstants.UPFRONT_FEE,
				ImportConstants.PROFESSIONALSERVICES_FEES, ImportConstants.MAINTENANCE_FEE,
				ImportConstants.TERMINATION_FEE, ImportConstants.MIN_CONTRACT_VALUE, ImportConstants.DEAL_MARGIN,
				ImportConstants.SW_MAINTFEE_FOR_RENEWAL_TERM, ImportConstants.MAINT_FEEADJ_DURING_CURRENTTERM });
		return decimalAttrs;
	}

	private void validateDecimalAttrs(Map<String, String> rowData, ReportItem report) {
		for (String decimalAttrName : decimalAttrs) {
			String decimalAttrValue = rowData.get(decimalAttrName);
			try {
				if (!Utilities.isStringEmpty(decimalAttrValue)) {
					Double.parseDouble(decimalAttrValue);
				}
			} catch (NumberFormatException e) {
				report.error(decimalAttrName,
						String.format("The mentioned value of %s is not decimal", decimalAttrName));
			}
		}
	}

	public String findEntityDatawithOneFilter(final String nameSpace, final String serviceName,
			final String filterElement, final String filter, final String lookupFor) {
		String value = null;
		Document document = NOMDocumentPool.getInstance().lendDocument();
		int nameElement = 0;
		int response = 0;
		try {
			nameElement = document.createElement(filterElement);
			document.createText(filter, nameElement);
			SOAPRequestObject importRequest = new SOAPRequestObject(nameSpace, serviceName, null, null);
			importRequest.addParameterAsXml(nameElement);
			response = importRequest.sendAndWait();
			int itemNode = XPath.getFirstMatch(lookupFor, null, response);
			value = Node.getData(itemNode);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			Utilities.cleanAll(nameElement, response);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
		return value;
	}

	public String findEntityDatawithTwoFilters(final String nameSpace, final String serviceName, final String filterOne,
			final String filterTwo, final String lookupFor) {
		String value = null;
		Document document = NOMDocumentPool.getInstance().lendDocument();
		int filterOneElement = 0, filterTwoElement = 0;
		int response = 0;
		try {
			filterOneElement = document.createElementNS("name", null, null, "http://schemas.cordys.com/default", 0);
			filterTwoElement = document.createElementNS("ownerItemId", null, null, "http://schemas.cordys.com/default",
					0);
			document.createText(filterOne, filterOneElement);
			document.createText(filterTwo, filterTwoElement);
			SOAPRequestObject importRequest = new SOAPRequestObject(nameSpace, serviceName, null, null);
			importRequest.addParameterAsXml(filterOneElement);
			importRequest.addParameterAsXml(filterTwoElement);
			response = importRequest.sendAndWait();
			int itemNode = XPath.getFirstMatch(lookupFor, null, response);
			value = Node.getData(itemNode);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			Utilities.cleanAll(filterOneElement, filterTwoElement, response);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
		return value;
	}

	public String findEntityDatawithTwoFilters(final String nameSpace, final String serviceName,
			final String filterElementOne, final String filterOne, final String filterElementTwo,
			final String filterTwo, final String lookupFor) {
		String value = null;
		Document document = NOMDocumentPool.getInstance().lendDocument();
		int filterElement1 = 0, filterElement2 = 0;
		int response = 0;
		try {
			filterElement1 = document.createElement(filterElementOne);
			document.createText(filterOne, filterElement1);
			filterElement2 = document.createElement(filterElementTwo);
			document.createText(filterTwo, filterElement2);
			SOAPRequestObject importRequest = new SOAPRequestObject(nameSpace, serviceName, null, null);
			importRequest.addParameterAsXml(filterElement1);
			importRequest.addParameterAsXml(filterElement2);
			response = importRequest.sendAndWait();
			int itemNode = XPath.getFirstMatch(lookupFor, null, response);
			value = Node.getData(itemNode);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			Utilities.cleanAll(filterElement1, filterElement2, response);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
		return value;
	}

	private void parsingIPAndEP(String partyAndcontctsInput, String contract_num,
			WeakHashMap<String, WeakHashMap<String, HashSet<String>>> IPCndEPC, Map<String, String> partyMap,
			WeakHashMap<String, WeakHashMap<String, String>> contactMap, ReportItem report, String partyNameSpace,
			String partyServiceName, String partyType) {
		String[] partyAndCntcs = partyAndcontctsInput.split(getMappingPropertyValue(ImportConstants.PARTY_DELIMITER));
		WeakHashMap<String, HashSet<String>> partyAndItsContactsMap = new WeakHashMap<String, HashSet<String>>();
		for (String ele : partyAndCntcs) {
			String[] partyWithContacts = ele.split(getMappingPropertyValue(ImportConstants.CONTACT_DELIMITER));
			String party_reg_Id = partyWithContacts[0];
			String partyItemID = validateParty(party_reg_Id, partyMap, report, partyNameSpace, partyServiceName,
					partyType);
			if (!Utilities.isStringEmpty(partyItemID)) {
				if (partyAndItsContactsMap.get(partyItemID) == null) {
					String contactItemId1 = null;
					HashSet<String> contactItems = new HashSet<String>();
					for (int i = 1; i < partyWithContacts.length; i++) {
						contactItemId1 = validateContact(partyWithContacts[i], partyItemID, contactMap, report,
								partyType, party_reg_Id);
						if (contactItemId1 != null) {
							contactItems.add(contactItemId1);
						}
					}
					partyAndItsContactsMap.put(partyItemID, contactItems);

				} else {
					report.error(partyType,
							"Party with '" + party_reg_Id + "' is not allowed to add more than once in a contract");
				}
			}
		}
		if (!partyAndItsContactsMap.isEmpty()) {
			IPCndEPC.put(contract_num, partyAndItsContactsMap);
		} else {
			report.error(partyType, "Atleast single party and a contact is required for a contract");
		}
	}

	private String validateParty(String party_reg_Id, Map<String, String> partyMap, ReportItem report,
			String partyNameSpace, String partyServiceName, String partyType) {

		String partyId = null;
		if (!Utilities.isStringEmpty(party_reg_Id)) {
			partyId = partyMap.get(party_reg_Id);
			if (partyId == null) {

				String ItemId = findEntityDatawithOneFilter(partyNameSpace, partyServiceName, "registrationId",
						party_reg_Id, ".//Party-id/ItemId");
				if (ItemId != null) {
					partyMap.put(party_reg_Id, ItemId);
					partyId = ItemId;
				} else {

					report.error(partyType, "'RegistrationId: '" + party_reg_Id + "' doesn't exist in the system");
				}
			}
		} else {
			report.error(partyType, "Mandatory information 'Party registration Id' is missing in excel");
		}
		return partyId;
	}

	private String validateContact(String contactFName, String partyItemID,
			WeakHashMap<String, WeakHashMap<String, String>> contactMap, ReportItem report, String partyType,
			String party_reg_Id) {
		String partyContactId = null;
		if (!Utilities.isStringEmpty(contactFName)) {
			WeakHashMap<String, String> contactsWithIds = contactMap.get(partyItemID) != null
					? contactMap.get(partyItemID)
					: new WeakHashMap<String, String>();
			partyContactId = contactsWithIds.get(contactFName);
			if (partyContactId == null) {
				String partyId = partyItemID.substring(partyItemID.lastIndexOf(".") + 1);
				String ItemId1 = findEntityDatawithTwoFilters("http://schemas.cordys.com/default",
						"GetContactByNameAndParty", contactFName, partyId, ".//ItemId1");
				if (ItemId1 != null) {
					contactsWithIds.put(contactFName, ItemId1);
					partyContactId = ItemId1;
					contactMap.put(partyItemID, contactsWithIds);
				} else {
					report.error(partyType, "contact with the Name: '" + contactFName
							+ "' doesn't exist in the system under the party registration id: '" + party_reg_Id + "'");
				}
			}
		} else {
			report.error(partyType, "Mandatory information 'contact first name' is missing in excel");
		}
		return partyContactId;
	}

	private void validateLookupAttrs(Map<String, String> rowData, MetadataInitializer metadata, ReportItem report) {

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.TEMPLATE_ID))) {
			String relatedTemplateId = rowData.get(ImportConstants.TEMPLATE_ID);
			String itemId = metadata.relatedTemplateMap.get(relatedTemplateId);
			if (Utilities.isStringEmpty(itemId)) {
				itemId = findEntityDatawithOneFilter("http://schemas/OpenTextContentLibrary/GCTemplate/operations",
						"GetTemplateByTemplateID", "TemplateID", relatedTemplateId, ".//GCTemplate-id/ItemId");
				if (Utilities.isStringEmpty(itemId)) {
					report.error(ImportConstants.TEMPLATE_ID, "Related template with the template id: '"
							+ rowData.get(ImportConstants.TEMPLATE_ID) + "' doesn't exist in the system");
				} else {
					metadata.relatedTemplateMap.put(relatedTemplateId, itemId);
				}
			}
		} else if (Utilities.isStringEmpty(rowData.get(ImportConstants.TEMPLATE_ID))) {
			if (!((Utilities.isStringEmpty(rowData.get(ImportConstants.IS_EXTERNAL)))
					|| (Utilities.isStringEmpty(rowData.get(ImportConstants.IS_EXECUTED))))) {
				if (!((rowData.get(ImportConstants.IS_EXTERNAL).equalsIgnoreCase("EXTERNALDOCUMENT"))
						|| (rowData.get(ImportConstants.IS_EXECUTED).equalsIgnoreCase("TRUE")))) {
					report.error(ImportConstants.TEMPLATE_ID,
							"Mandatory information 'Related template' is missing in excel");
				}
			}
			if ((!(Utilities.isStringEmpty(rowData.get(ImportConstants.IS_EXTERNAL))))
					&& (Utilities.isStringEmpty(rowData.get(ImportConstants.IS_EXECUTED)))) {
				if (!(rowData.get(ImportConstants.IS_EXTERNAL).equalsIgnoreCase("EXTERNALDOCUMENT"))) {
					report.error(ImportConstants.TEMPLATE_ID,
							"Mandatory information 'Related template' is missing in excel");
				}
			}
			if ((!(Utilities.isStringEmpty(rowData.get(ImportConstants.IS_EXECUTED))))
					&& (Utilities.isStringEmpty(rowData.get(ImportConstants.IS_EXTERNAL)))) {
				if (!(rowData.get(ImportConstants.IS_EXECUTED).equalsIgnoreCase("TRUE"))) {
					report.error(ImportConstants.TEMPLATE_ID,
							"Mandatory information 'Related template' is missing in excel");
				}
			} else if (((Utilities.isStringEmpty(rowData.get(ImportConstants.IS_EXTERNAL)))
					&& (Utilities.isStringEmpty(rowData.get(ImportConstants.IS_EXECUTED))))) {
				report.error(ImportConstants.TEMPLATE_ID,
						"Mandatory information 'Related template' is missing in excel");
			}
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.ORGANIZATION_ID))) {
			String organizationId = metadata.organizationsMap.get(rowData.get(ImportConstants.ORGANIZATION_ID));
			if (organizationId == null) {
				String organizationName = rowData.get(ImportConstants.ORGANIZATION_ID);
				if (organizationName != null) {
					String id = findEntityDatawithOneFilter(
							"http://schemas/OpenTextBasicComponents/GCOrganization/operations", "GetOrgByName",
							"orgName", organizationName, ".//GCOrganization-id/Id");
					if (id != null) {
						metadata.organizationsMap.put(rowData.get(ImportConstants.ORGANIZATION_ID), id);
					} else {
						report.error(ImportConstants.ORGANIZATION_ID, "Organization with the Name: '"
								+ rowData.get(ImportConstants.ORGANIZATION_ID) + "' doesn't exist in the system");
					}
				}
			}
		} else {
			report.error(ImportConstants.ORGANIZATION_ID, "Mandatory information 'Organization' is missing in excel");
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.INTERNAL_PARTIES_AND_CONTACTS))) {
			parsingIPAndEP(rowData.get(ImportConstants.INTERNAL_PARTIES_AND_CONTACTS),
					rowData.get(ImportConstants.CONTRACT_NUMBER), metadata.internalPartiesAndContacts,
					metadata.contractingEntityMap, metadata.ipContactMap, report,
					"http://schemas/OpenTextPartyManagement/Party/operations", "GetContractingEntityByRegId",
					ImportConstants.INTERNAL_PARTIES_AND_CONTACTS);
		} else {
			report.error(ImportConstants.INTERNAL_PARTIES_AND_CONTACTS,
					"Mandatory information 'Internal parties and contacts' is missing in excel");
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.EXTERNAL_PARTIES_AND_CONTACTS))) {
			parsingIPAndEP(rowData.get(ImportConstants.EXTERNAL_PARTIES_AND_CONTACTS),
					rowData.get(ImportConstants.CONTRACT_NUMBER), metadata.externalPartiesAndContacts,
					metadata.clientMap, metadata.epContactMap, report,
					"http://schemas/OpenTextPartyManagement/Party/operations", "GetClientByRegId",
					ImportConstants.EXTERNAL_PARTIES_AND_CONTACTS);
		} else {
			report.error(ImportConstants.EXTERNAL_PARTIES_AND_CONTACTS,
					"Mandatory information 'External parties and contacts' is missing in excel");
		}

		/* Deprecated in CC22.3 bcz of multiparty import */
		/*
		 * if(!Utilities.isStringEmpty(rowData.get(ImportConstants.FIRST_PARTY_REG_ID)))
		 * { String firstPartyId =
		 * metadata.contractingEntityMap.get(rowData.get(ImportConstants.
		 * FIRST_PARTY_REG_ID)); if(firstPartyId == null) { //String firstPartyName =
		 * rowData.get(ImportConstants.FIRST_PARTY_ID); String firstPartyRegId =
		 * rowData.get(ImportConstants.FIRST_PARTY_REG_ID); if(firstPartyRegId != null)
		 * { String id = findEntityDatawithOneFilter(
		 * "http://schemas/OpenTextPartyManagement/Party/operations",
		 * "GetContractingEntityByRegId","registrationId",firstPartyRegId,
		 * ".//Party-id/Id"); if(id != null) {
		 * metadata.contractingEntityMap.put(rowData.get(ImportConstants.
		 * FIRST_PARTY_REG_ID), id); }else {
		 * report.error(ImportConstants.FIRST_PARTY_ID,
		 * "Contracting entity with the Name: '"+rowData.get(ImportConstants.
		 * FIRST_PARTY_ID)+"' and RegistrationId: '"+rowData.get(ImportConstants.
		 * FIRST_PARTY_REG_ID)+"' doesn't exist in the system"); } } } } else {
		 * report.error(ImportConstants.FIRST_PARTY_ID,
		 * "Mandatory information 'Contracting entity' is missing in excel"); }
		 * 
		 * if(!Utilities.isStringEmpty(rowData.get(ImportConstants.FIRSTPARTY_CONTACT_ID
		 * ))) { String firstPartyContactId =
		 * metadata.contactMap.get(rowData.get(ImportConstants.FIRSTPARTY_CONTACT_ID));
		 * if(firstPartyContactId == null) { String firstPartyContactName =
		 * rowData.get(ImportConstants.FIRSTPARTY_CONTACT_ID); String firstPartyId =
		 * metadata.contractingEntityMap.get(rowData.get(ImportConstants.
		 * FIRST_PARTY_REG_ID)); if(firstPartyId != null && firstPartyContactName !=
		 * null) { String ItemId =
		 * findEntityDatawithTwoFilters("http://schemas.cordys.com/default",
		 * "GetContactByNameAndParty",firstPartyContactName,firstPartyId,".//ItemId1");
		 * if(ItemId != null) {
		 * metadata.contactMap.put(rowData.get(ImportConstants.FIRSTPARTY_CONTACT_ID),
		 * ItemId); }else { report.error(ImportConstants.FIRSTPARTY_CONTACT_ID,
		 * "Internal contact with the Name: '"+rowData.get(ImportConstants.
		 * FIRSTPARTY_CONTACT_ID)
		 * +"' doesn't exist in the system under the Contracting entity '"+rowData.get(
		 * ImportConstants.FIRST_PARTY_ID)+"'"); } } } }
		 * 
		 * if(!Utilities.isStringEmpty(rowData.get(ImportConstants.SECOND_PARTY_REG_ID))
		 * ) { String secondPartyId =
		 * metadata.clientMap.get(rowData.get(ImportConstants.SECOND_PARTY_REG_ID));
		 * if(secondPartyId == null) { //String secondPartyName =
		 * rowData.get(ImportConstants.SECOND_PARTY_ID); String secondPartyregId =
		 * rowData.get(ImportConstants.SECOND_PARTY_REG_ID); if(secondPartyregId !=
		 * null) { String ItemId = findEntityDatawithOneFilter(
		 * "http://schemas/OpenTextPartyManagement/Party/operations",
		 * "GetClientByRegId","registrationId", secondPartyregId, ".//Party-id/ItemId");
		 * if(ItemId != null) {
		 * metadata.clientMap.put(rowData.get(ImportConstants.SECOND_PARTY_REG_ID),
		 * ItemId); }else { report.error(ImportConstants.SECOND_PARTY_ID,
		 * "Client with the Name: '"+rowData.get(ImportConstants.SECOND_PARTY_ID)
		 * +"' and RegistrationId: '"+rowData.get(ImportConstants.SECOND_PARTY_REG_ID)
		 * +"' doesn't exist in the system"); } } } } else {
		 * report.error(ImportConstants.FIRST_PARTY_ID,
		 * "Mandatory information 'Client' is missing in excel"); }
		 * 
		 * if(!Utilities.isStringEmpty(rowData.get(ImportConstants.
		 * SECONDPARTY_CONTACT_ID))) { String secondPartyContactId =
		 * metadata.contactMap.get(rowData.get(ImportConstants.SECONDPARTY_CONTACT_ID));
		 * if(secondPartyContactId == null) { String secondPartyContactName =
		 * rowData.get(ImportConstants.SECONDPARTY_CONTACT_ID); String secondPartyItemId
		 * = metadata.clientMap.get(rowData.get(ImportConstants.SECOND_PARTY_REG_ID));
		 * if(secondPartyItemId != null && secondPartyContactName != null) { String
		 * secondPartyId =
		 * secondPartyItemId.substring(secondPartyItemId.lastIndexOf(".") + 1); String
		 * ItemId = findEntityDatawithTwoFilters("http://schemas.cordys.com/default",
		 * "GetContactByNameAndParty",secondPartyContactName,secondPartyId,".//ItemId1")
		 * ; if(ItemId != null) {
		 * metadata.contactMap.put(rowData.get(ImportConstants.SECONDPARTY_CONTACT_ID),
		 * ItemId); }else { report.error(ImportConstants.SECONDPARTY_CONTACT_ID,
		 * "External contact with the Name: '"+rowData.get(ImportConstants.
		 * SECONDPARTY_CONTACT_ID)
		 * +"' doesn't exist in the system under the Contracting entity '"+rowData.get(
		 * ImportConstants.SECOND_PARTY_ID)+"'"); } } } }
		 */

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CONTRACT_TYPE_ID))) {
			String contractTypeId = metadata.typeMap.get(rowData.get(ImportConstants.CONTRACT_TYPE_ID));
			if (Utilities.isStringEmpty(contractTypeId)) {
				report.error(ImportConstants.CONTRACT_TYPE_ID, "Contract Type with the Name: '"
						+ rowData.get(ImportConstants.CONTRACT_TYPE_ID) + "' doesn't exist in the system");
			} else {
				String organizationId = metadata.organizationsMap.get(rowData.get(ImportConstants.ORGANIZATION_ID));
				if(!Utilities.isStringEmpty(organizationId)) {
				int response = 0;
				int node = 0;
				try {
					SOAPRequestObject orgTypeMapRequest = new SOAPRequestObject(
							"http://schemas/OpenTextBasicComponents/GCOrgTypeMap/operations",
							"GetOrgTypeMapByOrgandType", null, null);
					int orgID = NomUtil.parseXML("<OrgID>" + organizationId + "</OrgID>");
					int typeID = NomUtil.parseXML(
							"<TypeID>" + contractTypeId.substring(contractTypeId.lastIndexOf(".") + 1) + "</TypeID>");
					orgTypeMapRequest.addParameterAsXml(orgID);
					orgTypeMapRequest.addParameterAsXml(typeID);
					response = orgTypeMapRequest.sendAndWait();
					node = NomUtil.getNode(".//GCOrgTypeMap", response);
					String orgTypeMap = Node.getDataWithDefault(node, null);
					if (orgTypeMap == null) {
						report.error(ImportConstants.CONTRACT_TYPE_ID,
								" Contract type " + rowData.get(ImportConstants.CONTRACT_TYPE_ID)
										+ " is not linked with organization "
										+ rowData.get(ImportConstants.ORGANIZATION_ID) + "'");
					}
				} finally {
					if (0 != node) {
						Utilities.cleanAll(response);
					}
				}
				}
			}
		} else {
			report.error(ImportConstants.CONTRACT_TYPE_ID,
					"Mandatory information 'Contract Type' is missing in excel ");
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CONTRACT_SUBTYPE_ID))) {
			String contractSubtypeId = metadata.subtypeMap.get(rowData.get(ImportConstants.CONTRACT_TYPE_ID) + "-"
					+ rowData.get(ImportConstants.CONTRACT_SUBTYPE_ID));
			if (Utilities.isStringEmpty(contractSubtypeId)) {
				report.error(ImportConstants.CONTRACT_SUBTYPE_ID,
						"Contract Subtype with the Name: '" + rowData.get(ImportConstants.CONTRACT_SUBTYPE_ID)
								+ "' doesn't exist in the system under Contract Type '"
								+ rowData.get(ImportConstants.CONTRACT_TYPE_ID) + "'");
			}
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.COUNTRY_ID))) {
			String countryId = metadata.countryMap.get(rowData.get(ImportConstants.COUNTRY_ID));
			if (Utilities.isStringEmpty(countryId)) {
				report.error(ImportConstants.COUNTRY_ID, "Country with the Name: '"
						+ rowData.get(ImportConstants.COUNTRY_ID) + "' is not mapped with any Region");
			}
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CURRENCY_ID))) {
			String currencyId = metadata.currencyMap.get(rowData.get(ImportConstants.CURRENCY_ID));
			if (Utilities.isStringEmpty(currencyId)) {
				report.error(ImportConstants.CURRENCY_ID, "Currency with the Name: '"
						+ rowData.get(ImportConstants.CURRENCY_ID) + "' doesn't exist in the system");
			}
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.RENEWALFLAG_STATUS_ID))) {
			String renewalFlagStatusId = metadata.renewalFlagStatusMap
					.get(rowData.get(ImportConstants.RENEWALFLAG_STATUS_ID));
			if (Utilities.isStringEmpty(renewalFlagStatusId)) {
				report.error(ImportConstants.RENEWALFLAG_STATUS_ID, "Renewal flag status with the Name: '"
						+ rowData.get(ImportConstants.RENEWALFLAG_STATUS_ID) + "' doesn't exist in the system");
			}
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CANCEL_REASONCODE_ID))) {
			String cancellationReasonCodeId = metadata.cancellationReasonCodeMap
					.get(rowData.get(ImportConstants.CANCEL_REASONCODE_ID));
			if (Utilities.isStringEmpty(cancellationReasonCodeId)) {
				report.error(ImportConstants.CANCEL_REASONCODE_ID, "Cancellation reason with the Name: '"
						+ rowData.get(ImportConstants.CANCEL_REASONCODE_ID) + "' doesn't exist in the system");
			}
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CANCEL_REASON_DETAIL_ID))) {
			if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CANCEL_REASONCODE_ID))) {
				String cancellationReasonCodeId = metadata.cancellationReasonCodeMap
						.get(rowData.get(ImportConstants.CANCEL_REASONCODE_ID));
				String cancellationReasondetailId = metadata.cancellationReasonDetailMap
						.get(cancellationReasonCodeId + "-" + rowData.get(ImportConstants.CANCEL_REASON_DETAIL_ID));
				if (Utilities.isStringEmpty(cancellationReasondetailId)) {
					report.error(ImportConstants.CANCEL_REASON_DETAIL_ID,
							"Cancellation reason detail with the Name: '"
									+ rowData.get(ImportConstants.CANCEL_REASON_DETAIL_ID)
									+ "' doesn't exist in the system under Cancellation reason "
									+ rowData.get(ImportConstants.CANCEL_REASONCODE_ID));
				}
			} else {
				report.error(ImportConstants.CANCEL_REASON_DETAIL_ID, "Corresponding CancelReasonCode for '"
						+ rowData.get(ImportConstants.CANCEL_REASON_DETAIL_ID) + "' is missing in excel");
			}
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.COMPETITOR_ID))) {
			if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CANCEL_REASONCODE_ID))) {
				String cancellationReasonCodeId = metadata.cancellationReasonCodeMap
						.get(rowData.get(ImportConstants.CANCEL_REASONCODE_ID));
				String competitorId = metadata.competitorMap
						.get(cancellationReasonCodeId + "-" + rowData.get(ImportConstants.COMPETITOR_ID));
				if (Utilities.isStringEmpty(competitorId)) {
					report.error(ImportConstants.COMPETITOR_ID,
							"Competitor with the Name: '" + rowData.get(ImportConstants.COMPETITOR_ID)
									+ "' doesn't exist in the system under Cancellation reason "
									+ rowData.get(ImportConstants.CANCEL_REASONCODE_ID));
				}
			} else {
				report.error(ImportConstants.COMPETITOR_ID, "Corresponding CancelReasonCode for '"
						+ rowData.get(ImportConstants.COMPETITOR_ID) + "' is missing in excel");
			}
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.PRODUCT_ADOPTED_ID))) {
			if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CANCEL_REASONCODE_ID))) {
				String cancellationReasonCodeId = metadata.cancellationReasonCodeMap
						.get(rowData.get(ImportConstants.CANCEL_REASONCODE_ID));
				String productAdoptedId = metadata.productAdoptedMap
						.get(cancellationReasonCodeId + "-" + rowData.get(ImportConstants.PRODUCT_ADOPTED_ID));
				if (Utilities.isStringEmpty(productAdoptedId)) {
					report.error(ImportConstants.PRODUCT_ADOPTED_ID,
							"Product adopted with the Name: '" + rowData.get(ImportConstants.PRODUCT_ADOPTED_ID)
									+ "' doesn't exist in the system under Cancellation reason "
									+ rowData.get(ImportConstants.CANCEL_REASONCODE_ID));
				}
			} else {
				report.error(ImportConstants.PRODUCT_ADOPTED_ID, "Corresponding CancelReasonCode for '"
						+ rowData.get(ImportConstants.PRODUCT_ADOPTED_ID) + "' is missing in excel");
			}
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CANCELLATION_TYPE_ID))) {
			String cancellationTypeId = metadata.cancellationTypeMap
					.get(rowData.get(ImportConstants.CANCELLATION_TYPE_ID));
			if (Utilities.isStringEmpty(cancellationTypeId)) {
				report.error(ImportConstants.CANCELLATION_TYPE_ID, "Cancellation type with the Name: '"
						+ rowData.get(ImportConstants.CANCELLATION_TYPE_ID) + "' doesn't exist in the system");
			}
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.STATUS_OUTCOME_ID))) {
			String statusOutcomeId = metadata.statusOutcomeMap.get(rowData.get(ImportConstants.STATUS_OUTCOME_ID));
			if (Utilities.isStringEmpty(statusOutcomeId)) {
				report.error(ImportConstants.STATUS_OUTCOME_ID, "Outcome with the Name: '"
						+ rowData.get(ImportConstants.STATUS_OUTCOME_ID) + "' doesn't exist in the system");
			}
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.DOCUMENT_TYPE_ID))) {
			String docTypeId = metadata.docTypeMap.get(rowData.get(ImportConstants.DOCUMENT_TYPE_ID));
			if (Utilities.isStringEmpty(docTypeId)) {
				report.error(ImportConstants.DOCUMENT_TYPE_ID, "Document Type with the Name: '"
						+ rowData.get(ImportConstants.DOCUMENT_TYPE_ID) + "' doesn't exist in the system");
			}
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.DOCUMENT_ORIGINATION_ID))) {
			String documentOriginationId = metadata.documentOriginationMap
					.get(rowData.get(ImportConstants.DOCUMENT_ORIGINATION_ID));
			if (Utilities.isStringEmpty(documentOriginationId)) {
				report.error(ImportConstants.DOCUMENT_ORIGINATION_ID, "Document Origination with the Name: '"
						+ rowData.get(ImportConstants.DOCUMENT_ORIGINATION_ID) + "' doesn't exist in the system");
			}
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CONTRACT_STATUS_ID))) {
			String contractStatusId = metadata.documentStatusMap.get(rowData.get(ImportConstants.CONTRACT_STATUS_ID));
			if (Utilities.isStringEmpty(contractStatusId)) {
				report.error(ImportConstants.CONTRACT_STATUS_ID, "Contract Status with the Name: '"
						+ rowData.get(ImportConstants.CONTRACT_STATUS_ID) + "' doesn't exist in the system");
			}
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.TERMINATION_REASON_ID))) {
			String terminationReasonId = metadata.terminationReasonMap
					.get(rowData.get(ImportConstants.TERMINATION_REASON_ID));
			if (Utilities.isStringEmpty(terminationReasonId)) {
				report.error(ImportConstants.TERMINATION_REASON_ID, "Termination Reason with the Name: '"
						+ rowData.get(ImportConstants.TERMINATION_REASON_ID) + "' doesn't exist in the system");
			}
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.VALIDATED_BY))) {
			String validatedById = metadata.validatedByIdMap.get(rowData.get(ImportConstants.VALIDATED_BY));
			if (validatedById == null) {
				String validatedByEmailId = rowData.get(ImportConstants.VALIDATED_BY);
				if (validatedByEmailId != null) {
					// String UserId =
					// findEntityDatawithOneFilter("http://schemas.opentext.com/apps/contractcenterimport/16.3",
					// "GetPersonDetailsbyEmailId","EmailId", validatedByEmailId, ".//User_ID");
					String UserId = findEntityDatawithTwoFilters(
							"http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetPersonDetailsbyEmailId",
							"EmailId", validatedByEmailId, "Is_Internal", "false", ".//User_ID");
					if (UserId != null) {
						metadata.validatedByIdMap.put(rowData.get(ImportConstants.VALIDATED_BY), UserId);
					} else {
						report.error(ImportConstants.VALIDATED_BY, "User with the Name: '"
								+ rowData.get(ImportConstants.VALIDATED_BY) + "' doesn't exist in the system");
					}
				}
			}
		}

		// IsExecuted ---> Template/State[Active]
		if (!((Utilities.isStringEmpty(rowData.get(ImportConstants.IS_EXTERNAL)))
				|| (Utilities.isStringEmpty(rowData.get(ImportConstants.IS_EXECUTED))))) {
			if (rowData.get(ImportConstants.IS_EXECUTED).equalsIgnoreCase("TRUE")
					&& rowData.get(ImportConstants.IS_EXTERNAL).equalsIgnoreCase("EXTERNALDOCUMENT")) {
				String relatedTemplateId = metadata.relatedTemplateMap.get(rowData.get(ImportConstants.TEMPLATE_ID));
				if (!Utilities.isStringEmpty(relatedTemplateId)) {
					report.error(ImportConstants.TEMPLATE_ID,
							"Since IsExecuted is 'TRUE' and IsExternal is 'TRUE' template ID shouldn't be given.");
				}
				if (!String.valueOf(ContractState.ACTIVE).equalsIgnoreCase(rowData.get(ImportConstants.TARGET_STATE))) {
					report.error(ImportConstants.TARGET_STATE,
							"Given target state '" + rowData.get(ImportConstants.TARGET_STATE)
									+ "' is invalid. Contract with IsExecuted 'TRUE' can only be an ACTIVE contract.");
				}
			}

			if (rowData.get(ImportConstants.IS_EXECUTED).equalsIgnoreCase("FALSE")
					&& rowData.get(ImportConstants.IS_EXTERNAL).equalsIgnoreCase("INTERNALTEMPLATE")) {
				String relatedTemplateId = metadata.relatedTemplateMap.get(rowData.get(ImportConstants.TEMPLATE_ID));
				if (Utilities.isStringEmpty(relatedTemplateId)) {
					report.error(ImportConstants.TEMPLATE_ID, "Provide a valid template ID");
				}
			}
		}
		if ((Utilities.isStringEmpty(rowData.get(ImportConstants.IS_EXTERNAL)))
				&& (Utilities.isStringEmpty(rowData.get(ImportConstants.IS_EXECUTED)))) {
			String relatedTemplateId = metadata.relatedTemplateMap.get(rowData.get(ImportConstants.TEMPLATE_ID));
			if (Utilities.isStringEmpty(relatedTemplateId)) {
				report.error(ImportConstants.TEMPLATE_ID, "Provide a valid template ID.");
			}
		}
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.IS_EXTERNAL))) {
			if (rowData.get(ImportConstants.IS_EXTERNAL).equalsIgnoreCase("EXTERNALDOCUMENT")) {
				String relatedTemplateId = metadata.relatedTemplateMap.get(rowData.get(ImportConstants.TEMPLATE_ID));
				if (!Utilities.isStringEmpty(relatedTemplateId)) {
					report.error(ImportConstants.TEMPLATE_ID,
							"Template value must not be specified for external contracts.");
				}
			}
		}
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.IS_EXECUTED))) {
			if (rowData.get(ImportConstants.IS_EXECUTED).equalsIgnoreCase("TRUE")) {
				String relatedTemplateId = metadata.relatedTemplateMap.get(rowData.get(ImportConstants.TEMPLATE_ID));
				if (!Utilities.isStringEmpty(relatedTemplateId)) {
					report.error(ImportConstants.TEMPLATE_ID,
							"Template value must not be specified for executed contracts.");
				}
				if (!String.valueOf(ContractState.ACTIVE).equalsIgnoreCase(rowData.get(ImportConstants.TARGET_STATE))) {
					report.error(ImportConstants.TARGET_STATE,
							"Given target state '" + rowData.get(ImportConstants.TARGET_STATE)
									+ "' is invalid. Contract with IsExecuted 'TRUE' can only be an ACTIVE contract.");
				}
			}
		}
	}
}
