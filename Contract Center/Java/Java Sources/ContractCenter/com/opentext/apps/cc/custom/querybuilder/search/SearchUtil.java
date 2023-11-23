package com.opentext.apps.cc.custom.querybuilder.search;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.NomUtil;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.custom.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.custom.exceptions.ContractCenterApplicationException;
import com.opentext.apps.cc.custom.querybuilder.builder.data.ExpressionNode;
import com.opentext.apps.cc.custom.querybuilder.builder.data.SearchQueryData;

public class SearchUtil {

	private static final CordysLogger logger = CordysLogger.getCordysLogger(SearchUtil.class);
	private static final Map<String, String> filterOperators = new HashMap<String, String>() {
		{
			put("EQUALTO", "=");
			put("NOTEQUALTO", "!=");
			put("CONTAINS", "LIKE");
			put("NOTCONTAINS", "NOT LIKE");
			put("EMPTY", "IS NULL");
			put("NOTEMPTY", "IS NOT NULL");
			put("LESSTHANEQUAL", "<=");
			put("GREATERTHANEQUAL", ">=");
		}
	};

	private static final Map<String, String> aliasNamesMap = initMap();

	private static Map<String, String> initMap() {
		Map<String, String> aliasNames = new HashMap<>();
		aliasNames.put("ActBeforeExpirationInDays", "ActionDuration");
		aliasNames.put("AmendmentType", "AmendType");
		aliasNames.put("TerminationComments", "CancellationComments");
		aliasNames.put("TerminationDate", "CancellationDate");
		aliasNames.put("ClientEarlyTerminationRight", "ClientEarlyTermRight");
		aliasNames.put("IsExternal", "ContractDocumentType");
		aliasNames.put("OpportunityID", "CRMOpportunityID");
		aliasNames.put("QuoteID", "CRMQuoteID");
		aliasNames.put("CurrentTermEndDate", "CurrentEndDate");
		aliasNames.put("CurrentTermStartDate", "CurrentStartDate");
		aliasNames.put("AccountManagerComments", "CustomerManagerComments");
		aliasNames.put("ContractOrigination", "DocumentOrigination");
		aliasNames.put("TerminationConditions", "EarlyTerminationConditions");
		aliasNames.put("EndUserSegment", "EndUser");
		aliasNames.put("ContractID", "GeneratedContractId");
		aliasNames.put("ContractTerm", "InitialContractTenure");
		aliasNames.put("InitialExpirationDate", "InitialExpiryDate");
		aliasNames.put("IsExecutedContract", "IsExecuted");
		aliasNames.put("ActualStartDate", "MinStartdate");
		aliasNames.put("NotifyBeforeExpirationInDays", "NotificationDuration");
		aliasNames.put("Organization", "RelatedOrganization");
		aliasNames.put("DiscountOnRenewal", "RenewalDiscount");
		aliasNames.put("RenewalFlag", "RenewalFlagStatus");
		aliasNames.put("OrderID", "SAPOrderID");
		aliasNames.put("PlannedStartDate", "StartDate");
		aliasNames.put("TerminationNoticePeriodInDays", "TerminationNoticePeriod");
		aliasNames.put("ValidatedDate", "ValidatedOn");

		return Collections.unmodifiableMap(aliasNames);
	}

	public static String fetchNameFromAlias(String aliasName) {
		return aliasNamesMap.get(aliasName);
	}

	public static int expressionParser(String indexExpression, SearchQueryData queryData) {

		String andParser = indexExpression.replaceAll("(?i)AND", "&");
		String orParser = andParser.replaceAll("(?i)OR", "|");
		String trimSpaces = orParser.replaceAll("\\s+", "");
		String finalExpression = trimSpaces.replaceAll("\\d+", "\\$");
		char[] expressionPattern = finalExpression.toCharArray();
		int xmlNomNode = NomUtil.parseXML("<xmlNomNode></xmlNomNode>");
		int searchQuery = NomUtil.parseXML("<SearchQuery></SearchQuery>");

		int queryEle = NomUtil.parseXML("<QueryElement></QueryElement>");
		Node.setDataElement(queryEle, "Id", "0");
		Node.setDataElement(queryEle, "Type", "CONTAINER");
		Node.setDataElement(queryEle, "Order", "0");
		Node.setDataElement(queryEle, "ParentElement", null);
		Node.appendToChildren(queryEle, searchQuery);
		int parentId = 0, prevParentId = 0;
		int order = 0, expressIndex = 0;
		for (int index = 0; index < expressionPattern.length; index++) {
			int queryEle2 = NomUtil.parseXML("<QueryElement></QueryElement>");

			if (Character.compare(expressionPattern[index], '(') == 0) {
				prevParentId = parentId;
				Node.setDataElement(queryEle2, "Id", Integer.toString(index + 1));
				Node.setDataElement(queryEle2, "Type", "CONTAINER");
				Node.setDataElement(queryEle2, "Order", "0");
				Node.setDataElement(queryEle2, "ParentElement", Integer.toString(prevParentId));
				Node.appendToChildren(queryEle2, searchQuery);
				parentId = index + 1;

			} else if (Character.compare(expressionPattern[index], '$') == 0) {
				ExpressionNode expressionNode = ((ExpressionNode) queryData.containers.get(expressIndex));
				int expression = NomUtil.parseXML("<Expression></Expression>");
				Node.setDataElement(expression, "OperandName", expressionNode.operandName);
				Node.setDataElement(expression, "Operand", expressionNode.expOperator);
				Node.setDataElement(expression, "OperandValue", expressionNode.operandValue);
				Node.setDataElement(expression, "OperandDataType", expressionNode.expDataType);

				Node.setDataElement(queryEle2, "Id", Integer.toString(index + 1));
				Node.setDataElement(queryEle2, "Type", "EXPRESSION");
				Node.setDataElement(queryEle2, "Order", Integer.toString(index + 1));
				Node.setDataElement(queryEle2, "ParentElement", Integer.toString(parentId));
				Node.appendToChildren(expression, queryEle2);
				Node.appendToChildren(queryEle2, searchQuery);
				++expressIndex;
			} else if (Character.compare(expressionPattern[index], '&') == 0) {
				Node.setDataElement(queryEle2, "Id", Integer.toString(index + 1));
				Node.setDataElement(queryEle2, "Type", "CONNECTOR");
				Node.setDataElement(queryEle2, "Order", Integer.toString(index + 1));
				Node.setDataElement(queryEle2, "ParentElement", Integer.toString(parentId));
				Node.setDataElement(queryEle2, "Connector", "and");
				Node.appendToChildren(queryEle2, searchQuery);
			} else if (Character.compare(expressionPattern[index], '|') == 0) {
				Node.setDataElement(queryEle2, "Id", Integer.toString(index + 1));
				Node.setDataElement(queryEle2, "Type", "CONNECTOR");
				Node.setDataElement(queryEle2, "Order", Integer.toString(index + 1));
				Node.setDataElement(queryEle2, "ParentElement", Integer.toString(parentId));
				Node.setDataElement(queryEle2, "Connector", "or");
				Node.appendToChildren(queryEle2, searchQuery);
			} else {
				parentId = prevParentId;
			}

		}
		Node.appendToChildren(searchQuery, xmlNomNode);
		return xmlNomNode;

	}

	public static int basicExpressionParser(SearchQueryData queryData) {

		int xmlNomNode = NomUtil.parseXML("<xmlNomNode></xmlNomNode>");
		int searchQuery = NomUtil.parseXML("<SearchQuery></SearchQuery>");

		int id = 0, order = 0;
		int queryEle = NomUtil.parseXML("<QueryElement></QueryElement>");
		Node.setDataElement(queryEle, "Id", "0");
		Node.setDataElement(queryEle, "Type", "CONTAINER");
		Node.setDataElement(queryEle, "Order", "0");
		Node.setDataElement(queryEle, "ParentElement", null);
		Node.appendToChildren(queryEle, searchQuery);

		int parentId = id;
		int filterlen = queryData.containers.size();
		for (int index = 0; index < queryData.containers.size(); index++) {
			id++;
			order++;
			int queryEle2 = NomUtil.parseXML("<QueryElement></QueryElement>");

			ExpressionNode expressionNode = ((ExpressionNode) queryData.containers.get(index));
			int expression = NomUtil.parseXML("<Expression></Expression>");
			Node.setDataElement(expression, "OperandName", expressionNode.operandName);
			Node.setDataElement(expression, "Operand", expressionNode.expOperator);
			Node.setDataElement(expression, "OperandValue", expressionNode.operandValue);
			Node.setDataElement(expression, "OperandDataType", expressionNode.expDataType);

			Node.setDataElement(queryEle2, "Id", Integer.toString(id));
			Node.setDataElement(queryEle2, "Type", "EXPRESSION");
			Node.setDataElement(queryEle2, "Order", Integer.toString(order));
			Node.setDataElement(queryEle2, "ParentElement", Integer.toString(parentId));
			Node.appendToChildren(expression, queryEle2);
			Node.appendToChildren(queryEle2, searchQuery);

			if (index < (filterlen - 1)) {
				id++;
				order++;
				int queryEle3 = NomUtil.parseXML("<QueryElement></QueryElement>");
				Node.setDataElement(queryEle3, "Id", Integer.toString(id));
				Node.setDataElement(queryEle3, "Type", "CONNECTOR");
				Node.setDataElement(queryEle3, "Order", Integer.toString(order));
				Node.setDataElement(queryEle3, "ParentElement", Integer.toString(parentId));
				Node.setDataElement(queryEle3, "Connector", "and");
				Node.appendToChildren(queryEle3, searchQuery);

			}

		}
		Node.appendToChildren(searchQuery, xmlNomNode);
		return xmlNomNode;

	}

	public static String changeDataType(String iType) {
		if (iType.equals("NUMBER")) {
			return "DECIMAL";
		} else if (iType.equals("ENUM")) {
			return "ENUMERATEDTEXT";
		} else {
			return iType;
		}
	}

	public static String changeOperator(String l_dataType, String l_oper, String l_value) {
		if (l_dataType.equals("DURATION")) {
			if (l_oper.equals("IS")) {
				l_oper = (l_value.equals("EMPTY")) ? "=" : "!=";
			}
		}
		if (l_oper.equals("IS")) {
			l_oper = (l_value.equals("EMPTY")) ? "IS NULL" : "IS NOT NULL";
		}
		l_oper = (l_oper.equals("CONTAINS")) ? "LIKE" : l_oper;

		return l_oper;
	}

	public static String changeEmptyToNull(String l_dataType, String l_oper, String l_value) {
		if (l_dataType.equals("DURATION")) {
			l_value = (l_value.equals("EMPTY") || l_value.equals("NOTEMPTY")) ? "0" : l_value;
		} else {
			l_value = (l_oper.equals("IS NULL") || l_oper.equals("IS NOT NULL")) ? "" : l_value;
		}
		return l_value;
	}

	public static String convertValue(String l_name, String l_dataType, String l_val, String searchType) {
		if (l_dataType.equals("BOOLEAN")) {
			l_val = l_val.equals("Yes") ? "true" : "false";
		} else if (l_dataType.equals("DATE")) {
			//LocalDate date = LocalDate.parse(l_val);
			//l_val = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			l_val = (!l_val.isBlank())?getParsedDate(l_val): l_val;
			
		} else if (l_dataType.equals("DURATION")) {
			if (l_val != null) {
				int days = 0, mindex=0;
				if (l_val.indexOf('M') > -1) {
					days = Integer.parseInt(l_val.substring(0, l_val.indexOf('M'))) * 30;
					mindex = l_val.indexOf('M')+1;
				}
				if (l_val.indexOf('D') > -1) {
					days += Integer.parseInt(l_val.substring(mindex, l_val.indexOf('D')));
				}
				l_val = String.valueOf(days);
			}
			if (l_val == null) {
				l_val = "0";
			}
		} else if (l_dataType.equals("ENUMERATEDTEXT")) {
			if (l_name.equals("PriceProtection")) {
				l_val = l_val.equals("Yes") ? "1" : "0";
			}
		}
		return l_val;
	}

	public static String convertOperand(String dataType, String oper) {
		if (dataType.equals("DURATION")) {
			oper = oper.equals("NOTEMPTY") ? "NOTEQUALTO" : oper.equals("EMPTY") ? "EQUALTO" : oper;
		}
		return filterOperators.get(oper);
	}
	
	private static String getParsedDate(String dateString) {
		
		String sameDate = null;
		try {
			final SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
			final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd");
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(inputFormat.parse(dateString));
			sameDate = outputFormat.format(calendar.getTime());
		} catch (Exception e) {
			logger._log("com.opentext.apps.cc.custom.querybuilder.search.SearchServices", Severity.ERROR, e,
					ContractCenterAlertMessages.PARSING_FAILURE_DATE);
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.PARSING_FAILURE_DATE);
		}
		return sameDate;
	}
	
	

}
