package com.opentext.apps.cc.custom.querybuilder.search;


import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opentext.apps.cc.custom.NomUtil;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.custom.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.custom.exceptions.ContractCenterApplicationException;
import com.opentext.apps.cc.custom.querybuilder.builder.data.ExpressionNode;
import com.opentext.apps.cc.custom.querybuilder.builder.data.IDataNode;
import com.opentext.apps.cc.custom.querybuilder.builder.data.SearchQueryData;

public class SearchServices {

	private final int id;
	private String searchType = null;
	private HashMap<String, String> allGeneralAttrMap = new HashMap<String, String>();
	private HashMap<String, String> allCustAttrMap = new HashMap<String, String>();
	private SearchQueryData queryData = null;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(SearchServices.class);
	public SearchServices(int id) {
		this.id = id;
	}

	public SearchServices(String expression) {
		this.id = -1;
	}
	
	public SearchQueryData getqueryData() {
		return this.queryData;
	}

	public String readSavedSearchExpression() {
		int searchId = 0;
		int readSearchResponse=0;
		try {
			SOAPRequestObject readSavedSearch = new SOAPRequestObject(
					"http://schemas/OpenTextBasicComponents/GCSavedSearch/operations", "ReadGCSavedSearch", null, null);
			searchId = NomUtil.parseXML("<GCSavedSearch-id></GCSavedSearch-id>");
			Node.setDataElement(searchId, "Id", this.id + "");
			readSavedSearch.addParameterAsXml(searchId);

			readSearchResponse = readSavedSearch.sendAndWait();
			String advancedSearchInp = Node.getDataWithDefault(
					NomUtil.getNode(".//GCSavedSearch//AdvancedSearchExpression", readSearchResponse), null);
			String basicSearchInp = Node.getDataWithDefault(
					NomUtil.getNode(".//GCSavedSearch//FilterParametersJSON", readSearchResponse), null);
			this.searchType = Node
					.getDataWithDefault(NomUtil.getNode(".//GCSavedSearch//SearchType", readSearchResponse), null);
			this.searchType = ( Objects.isNull(this.searchType)|| this.searchType == null) ? "Basic" : this.searchType;
			fetchGeneralAttrList();
			fetchCustomAttrList();
			return this.searchType.equals("Basic") ? basicSearchInp : advancedSearchInp;

		} catch (Exception e) {
			logger._log("com.opentext.apps.cc.custom.querybuilder.search.SearchServices", Severity.ERROR, e,
					ContractCenterAlertMessages.ERROR_WHILE_EXECUTING);
		}finally {
			Utilities.cleanAll(searchId, readSearchResponse);
		}

		return null;
	}

	public int parseSearchRequest(String input) {
		
		String expressionReplacer=null;
		if(this.searchType.equals("Basic")) {
			return parseBasciSearch(input);
		}
		try {
		String regex = "(\\w+)\\s*(=|!=|<=|>=|CONTAINS|IS )\\s*(\".*?\"|EMPTY|NOTEMPTY)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		expressionReplacer = input;
		String fieldDataType=null, propType="GENERAL";
		queryData = new SearchQueryData();
		int index = -1;
		while (matcher.find()) {
			index = index + 1;
			String expression = matcher.group(0);
			String field = matcher.group(1);
			String operator = matcher.group(2);
			String value = matcher.group(3);

			operator = (operator.equals("IS ")) ? "IS" : operator;
			// Remove quotes from value if present
			if (value.startsWith("\"") && value.endsWith("\"")) {
				value = value.substring(1, value.length() - 1);
			}
			// Remove quotes from value if present
			if (field.startsWith("\"") && field.endsWith("\"")) {
				field = field.substring(1, field.length() - 1);
			}
			// Replace expression with numbers
			expressionReplacer = expressionReplacer.replaceFirst(expression, Integer.toString(index) + ' ');
			// String aliasDisplayname = field;
			String actualName = SearchUtil.fetchNameFromAlias(field);
			field = actualName != null ? actualName : field;
			// propObj = self.generalAttrList().find(o => o.Name === field);
			fieldDataType = allGeneralAttrMap.get(field);
			if(fieldDataType!=null && !fieldDataType.isEmpty()) {
				propType = "GENERAL";
				
			}else if(allCustAttrMap.get(field)!=null) {
				propType = "CUSTOM";
				fieldDataType = allCustAttrMap.get(field);
			}
			
			fieldDataType = SearchUtil.changeDataType(fieldDataType);
			operator = SearchUtil.changeOperator(fieldDataType, operator, value);
			value = SearchUtil.changeEmptyToNull(fieldDataType, operator, value);
			value = SearchUtil.convertValue(field, fieldDataType, value, this.searchType);
			IDataNode dataNode = new ExpressionNode(operator, field,value, propType, fieldDataType);
			queryData.addContainerNode(dataNode);

		}
		}catch (Exception e) {
			logger._log("com.opentext.apps.cc.custom.querybuilder.search.SearchServices", Severity.ERROR, e,
					ContractCenterAlertMessages.ERROR_WHILE_EXECUTING);
        	throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING);
		}
		return SearchUtil.expressionParser(expressionReplacer, queryData);
	}
	
	public int parseBasciSearch(String input) {
		queryData = new SearchQueryData();
		 try {
			 	ObjectMapper mapper = new ObjectMapper();
	            JsonNode jsonObj = mapper.readTree(input);

	            jsonObj.fields().forEachRemaining(entry -> {
	                String key = entry.getKey();
	                JsonNode filterNode = entry.getValue();

	                String type = filterNode.get("type").asText();
	                String fieldDataType = filterNode.get("fieldDataType").asText();
	                String fieldName = filterNode.get("fieldName").asText();
	                String operator = SearchUtil.convertOperand(fieldDataType,filterNode.get("operator").asText());
	                String value =SearchUtil.convertValue(fieldName, fieldDataType, filterNode.get("value").asText(), this.searchType);
	                IDataNode dataNode = new ExpressionNode(operator, fieldName,value, type, fieldDataType);
	    			queryData.addContainerNode(dataNode);

	            });
	        } catch (Exception e) {
	        	logger._log("com.opentext.apps.cc.custom.querybuilder.search.SearchServices", Severity.ERROR, e,
						ContractCenterAlertMessages.ERROR_WHILE_EXECUTING);
	        	throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING);
	        }
		 
		 return SearchUtil.basicExpressionParser(queryData);
	}

	public void fetchGeneralAttrList() {
		int nodes[] = null;
		String propName, propDataType;
		try {
			SOAPRequestObject getGeneralAttr = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/cc/basiccomponents/20.2", "GetGeneralAttrWithFilters", null,
					null);
			nodes = NomUtil.getNodeList(".//RelatedGCProps", getGeneralAttr.sendAndWait());
			for (int i : nodes) {
				propName = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				propDataType = Node.getDataWithDefault(NomUtil.getNode(".//DataType", i), null);
				allGeneralAttrMap.put(propName, propDataType);
			}

		} catch (Exception e) {
			logger._log("com.opentext.apps.cc.custom.querybuilder.search.SearchServices", Severity.ERROR, e,
					ContractCenterAlertMessages.ERROR_WHILE_EXECUTING);
		}finally {
			Utilities.cleanAll(nodes);
		}
	}

	public void fetchCustomAttrList() {
		int nodes[] = null;
		String propName, propDataType;
		try {
			SOAPRequestObject getGeneralAttr = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/cc/customattributes/21.4", "GetCustAttrWithFilters", null, null);
			// searchId = NomUtil.parseXML("<GCSavedSearch-id></GCSavedSearch-id>");
			// Node.setDataElement(searchId, "Id", this.id + "");
			// getGeneralAttr.addParameterAsXml();
			nodes = NomUtil.getNodeList(".//AttributeDefinition", getGeneralAttr.sendAndWait());
			for (int i : nodes) {
				propName = Node.getDataWithDefault(NomUtil.getNode(".//Name", i), null);
				propDataType = Node.getDataWithDefault(NomUtil.getNode(".//DataType", i), null);
				allCustAttrMap.put(propName, propDataType);
			}

		} catch (Exception e) {
			logger._log("com.opentext.apps.cc.custom.querybuilder.search.SearchServices", Severity.ERROR, e,
					ContractCenterAlertMessages.ERROR_WHILE_EXECUTING);
		}finally {
			Utilities.cleanAll(nodes);
		}
	}

}
