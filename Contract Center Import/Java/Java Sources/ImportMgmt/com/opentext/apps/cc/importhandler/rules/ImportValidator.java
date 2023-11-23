package com.opentext.apps.cc.importhandler.rules;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.DateUtil;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportUtils;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;

public class ImportValidator {

	ReportItem report = new ReportItem();
	public ReportItem validate(Map<String, String> rowData, MetadataInitializer meta, String jobId) {

		if (rowData == null) {
			return report;
		}
		if (Utilities.isStringEmpty(rowData.get(ImportConstants.TYPE))) {
			if(Objects.nonNull(rowData.get(ImportConstants.LEGACY_ID))) {
				report=new ReportItem();
			}
			appendMessage(ImportConstants.TYPE, "Type cannot be empty");
		}else {
			if(!rowData.get(ImportConstants.TYPE).equalsIgnoreCase("Condition") && (Objects.nonNull(rowData.get(ImportConstants.LEGACY_ID)))) {
				report=new ReportItem();
			}
			if (rowData.get(ImportConstants.TYPE).equalsIgnoreCase("Rule")) {
				report=new ReportItem();
				if (Utilities.isStringEmpty(rowData.get(ImportConstants.RULE_NAME))) {
					appendMessage(ImportConstants.RULE_NAME, "Rule name cannot be empty");
				}

				if (!Utilities.isStringEmpty(rowData.get(ImportConstants.ORGANIZATION_CODE))) {
					String organization_code = rowData.get(ImportConstants.ORGANIZATION_CODE);
					if (!meta.orgMap.containsKey(organization_code)) {
						if(organization_code.equals("All")) {
							rowData.replace(ImportConstants.ORGANIZATION_CODE, "");
						}
						else{
							appendMessage(ImportConstants.ORGANIZATION_CODE,
								"The organization code '" + rowData.get(ImportConstants.ORGANIZATION_CODE) + "' is incorrect");
						}
					}
				} else {
					if(meta.duplicateOrgs.contains(rowData.get(ImportConstants.ORGANIZATION_CODE))) {
						appendMessage(ImportConstants.ORGANIZATION_CODE, "The organization code '"+rowData.get(ImportConstants.ORGANIZATION_CODE)+"' refers to more than one organization. Ensure that only one reference is made for a given organization code");
					}else {
					appendMessage(ImportConstants.ORGANIZATION_CODE, "Organization code cannot be empty");
					}
				}
				
				if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CONTRACT_TYPE))) {
					String contracttype = rowData.get(ImportConstants.CONTRACT_TYPE);
					String organization_code = rowData.get(ImportConstants.ORGANIZATION_CODE);
					if (!meta.contracttypeMap.containsKey(contracttype)) {
						appendMessage(ImportConstants.CONTRACT_TYPE,
								"The contract type '" + rowData.get(ImportConstants.CONTRACT_TYPE) + "' is incorrect");
					}
					else if(!(organization_code.equals("All") && meta.contracttypeMap.containsKey(contracttype))){
						int response = 0;
						int node = 0;
						try {
							SOAPRequestObject orgTypeMapRequest = new SOAPRequestObject(
									"http://schemas/OpenTextBasicComponents/GCOrgTypeMap/operations", "GetOrgTypeMapByOrgandType", null, null);
							int orgID = NomUtil.parseXML("<OrgID>" + meta.orgMap.get(organization_code).substring(meta.orgMap.get(organization_code).lastIndexOf(".") + 1)  + "</OrgID>");
							int typeID = NomUtil.parseXML("<TypeID>" + meta.contracttypeMap.get(contracttype) + "</TypeID>");
							orgTypeMapRequest.addParameterAsXml(orgID);
							orgTypeMapRequest.addParameterAsXml(typeID);
							response = orgTypeMapRequest.sendAndWait();
							node = NomUtil.getNode(".//GCOrgTypeMap", response);
							String orgTypeMap=Node.getDataWithDefault(node,null);
							if(orgTypeMap==null) {
								appendMessage(ImportConstants.CONTRACT_TYPE, "Contract type "+contracttype+" is not linked with organiation "+organization_code);
							}
						} finally {
							if (0 != node) {
								Utilities.cleanAll(response);
							}
						}
						
					}
				} else {
					appendMessage(ImportConstants.CONTRACT_TYPE, "Contract type cannot be empty");
				}

				if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CONTRACT_STATE))) {
					String organization_code = rowData.get(ImportConstants.CONTRACT_STATE);
					if (!meta.gcStateMap.containsKey(organization_code)) {
						appendMessage(ImportConstants.CONTRACT_STATE,
								"The contract state '" + rowData.get(ImportConstants.CONTRACT_STATE) + "' is incorrect");
					}
				} else {
					appendMessage(ImportConstants.CONTRACT_STATE, "Contract state cannot be empty");
				}
				
				if(!Utilities.isStringEmpty(rowData.get(ImportConstants.TASK_LISTID))) {
					String tasklistid=rowData.get(ImportConstants.TASK_LISTID);
					if (Objects.isNull(meta.tasklistIdMap.get(tasklistid))) {
						if (!Utilities.isStringEmpty(rowData.get(ImportConstants.TASK_LIST))) {
							String organization_code = rowData.get(ImportConstants.TASK_LIST);
							if(meta.duplicateTasklists.contains(organization_code)) {
								appendMessage(ImportConstants.TASK_LIST,
										"Task list ID '"+rowData.get(ImportConstants.TASK_LISTID)+"' for the task list '"  + rowData.get(ImportConstants.TASK_LIST) + "' is incorrect");
							}
							else if (!meta.tasklistMap.containsKey(organization_code)) {
								appendMessage(ImportConstants.TASK_LIST,
										"The task list '" + rowData.get(ImportConstants.TASK_LIST) + "' is incorrect");
							}
						} else {
							appendMessage(ImportConstants.TASK_LIST, "Task list cannot be empty");
						}
					}
				}else {
					if (!Utilities.isStringEmpty(rowData.get(ImportConstants.TASK_LIST))) {
						String organization_code = rowData.get(ImportConstants.TASK_LIST);
						if(meta.duplicateTasklists.contains(organization_code)) {
							appendMessage(ImportConstants.TASK_LIST,
									"Task list ID cannot be empty when task list name '"  + rowData.get(ImportConstants.TASK_LIST) + "' is not unique");
						}
						else if (!meta.tasklistMap.containsKey(organization_code)) {
							appendMessage(ImportConstants.TASK_LIST,
									"The task list '" + rowData.get(ImportConstants.TASK_LIST) + "' is incorrect");
						}
					} else {
						appendMessage(ImportConstants.TASK_LIST, "Task list cannot be empty");
					}
				}

				if (!Utilities.isStringEmpty(rowData.get(ImportConstants.RULE_LOGIC))) {
					String organization_code = rowData.get(ImportConstants.RULE_LOGIC);
					if (!validaterule(organization_code)) {
						appendMessage(ImportConstants.RULE_LOGIC,
								"The rule logic '" + rowData.get(ImportConstants.RULE_LOGIC) + "' is incorrect");
					}
				} else {
					appendMessage(ImportConstants.RULE_LOGIC, "Rule logic cannot be empty");
				}

				if (Utilities.isStringEmpty(rowData.get(ImportConstants.LEGACY_ID))) {
					appendMessage(ImportConstants.LEGACY_ID, "Legacy ID cannot be empty");
				}

			}

			else if (rowData.get(ImportConstants.TYPE).equalsIgnoreCase("Condition")) {
				String order=null;
				
				if (!Utilities.isStringEmpty(rowData.get(ImportConstants.ORDER))) {
					order=rowData.get(ImportConstants.ORDER);
					if(!order.matches("\\b\\d+\\b")) {
						appendMessage(ImportConstants.ORDER, "The order '"+rowData.get(ImportConstants.ORDER)+ "' is incorrect");
					}
				}else {
					appendMessage(ImportConstants.ORDER, "Order cannot be empty");
				}
				
				String organization_code = rowData.get(ImportConstants.CONTRACT_PROPERTY);
				if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CONTRACT_PROPERTY))) {
					if (meta.contractpropertyIdMap.containsKey(organization_code)) {
						if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CONTRACT_OPERATOR))) {
							List<String> a = meta.propertyOperators.get(meta.contractpropertyMap.get(organization_code));
							if ((Objects.nonNull(a) && a.contains(rowData.get(ImportConstants.CONTRACT_OPERATOR)))) {
								if (Utilities.isStringEmpty(rowData.get(ImportConstants.PROPERTY_VALUE))) {
									if (!(rowData.get(ImportConstants.CONTRACT_OPERATOR).equals("EMPTY")
											|| rowData.get(ImportConstants.CONTRACT_OPERATOR).equals("NOTEMPTY"))) {
										if(order==null) {
										appendMessage(ImportConstants.PROPERTY_VALUE, "Contract property value cannot be empty");
										}else {
											appendMessage(ImportConstants.PROPERTY_VALUE, "Contract property value with order '"+order+"' cannot be empty");
										}
									}
								} else if (!Utilities.isStringEmpty(rowData.get(ImportConstants.PROPERTY_VALUE))) {
									String type = meta.contractpropertyMap.get(organization_code);
									String val = rowData.get(ImportConstants.PROPERTY_VALUE);
									if(Objects.nonNull(type)) {
									if (type.equalsIgnoreCase("BOOLEAN")) {
										if (!(val.equals("Yes") || val.equals("No"))) {
											appendMessage(ImportConstants.PROPERTY_VALUE, "The contract property value '"
													+ rowData.get(ImportConstants.PROPERTY_VALUE) + "' is incorrect");
										}
									} else if (type.equalsIgnoreCase("NUMBER")) {
										if (!val.matches("\\b\\d+\\b")) {
											appendMessage(ImportConstants.PROPERTY_VALUE, "The contract property value '"
													+ rowData.get(ImportConstants.PROPERTY_VALUE) + "' is incorrect");
										}
									} else if (type.equalsIgnoreCase("DECIMAL") || type.equalsIgnoreCase("DURATION")) {
										if (!isDouble(val)) {
											appendMessage(ImportConstants.PROPERTY_VALUE, "The contract property value '"
													+ rowData.get(ImportConstants.PROPERTY_VALUE) + "' is incorrect");
										}
									} else if (type.equalsIgnoreCase("DATE")) {
										if(!isValidDate(val)) {
											appendMessage(ImportConstants.PROPERTY_VALUE, "The contract property value '"
													+ rowData.get(ImportConstants.PROPERTY_VALUE) + "' is incorrect");
										}else {
											String d=(val.substring(5,7)+"/"+val.substring(8,10)+"/"+val.substring(0,4));
											rowData.replace(ImportConstants.PROPERTY_VALUE, d);
										}
									}
								}
								}
							}
							else {
								appendMessage(ImportConstants.CONTRACT_OPERATOR, "The contract operator '"
										+ rowData.get(ImportConstants.CONTRACT_OPERATOR) + "' is incorrect");
							}
						} else {
							if(order==null) {
							appendMessage(ImportConstants.CONTRACT_OPERATOR, "Contract operator cannot be empty");
							}else {
								appendMessage(ImportConstants.CONTRACT_OPERATOR, "Contract operator with order '"+order+"' cannot be empty");
							}
						}
					}
					else {
						appendMessage(ImportConstants.CONTRACT_PROPERTY, "The contract property '"
								+ rowData.get(ImportConstants.CONTRACT_PROPERTY) + "' is incorrect");
					}
				} else {
					if(order==null) {
					appendMessage(ImportConstants.CONTRACT_PROPERTY, "Contract property value cannot be empty.");
					}else {
						appendMessage(ImportConstants.CONTRACT_PROPERTY, "Contract property with order '"+order+"' cannot be empty.");
					}
				}
			}
			else {
				appendMessage(ImportConstants.TYPE, "Type is incorrect");
			}
		} 
		return report;
	}

	private String expression;
	private int position;
	private char l;
	// For error detection.
	private int errorPosition;
	private String originalExpression;

	public boolean validaterule(String expression) {
		boolean result = false;

		try {
			// Check for null, empty and ($ , &, | and Z) symbols.
			if (Objects.isNull(expression) || expression.isBlank()) {
				errorPosition = 0;
				return result;
			}
			errorPosition = expression.indexOf("$");
			if (errorPosition >= 0) {
				return false;
			}
			errorPosition = expression.indexOf("&");
			if (errorPosition >= 0) {
				return false;
			}
			errorPosition = expression.indexOf("|");
			if (errorPosition >= 0) {
				return false;
			}
			errorPosition = expression.indexOf("Z");
			if (errorPosition >= 0) {
				return false;
			}
			errorPosition = expression.indexOf("z");
			if (errorPosition >= 0) {
				return false;
			}

			// Replace all integers with D.
			String exp = expression.replaceAll("(\\d)+", "z");

			// Replace and with &, or with |.
			exp = exp.toLowerCase().replaceAll("and", "&").replaceAll("or", "|");

			// Remove all spaces.
			exp = exp.replaceAll(" ", "");

			// Initialize;
			this.expression = exp + '$'; // Append $ at the end.
			this.originalExpression = expression + '$';
			this.position = 0;
			l = this.expression.charAt(position);
			this.errorPosition = 0;

			// Start execution.
			result = E();
			if (result)
				errorPosition = -1;
		} catch (Exception e) {
			report.error(ImportConstants.RULE_LOGIC, String.valueOf(e));

		}
		return result;
	}

	private boolean E() {
		if ('z' == l) {
			match();
			return E1();
		} else if ('(' == l) {
			match();
			E();
			if (')' == l) {
				match();
				return E1();
			} else {
				return false;
			}

		}
		skipSpacesInOriginalExpression();
		return false;
	}

	private boolean E1() {
		if ('&' == l || '|' == l) {
			match();
			boolean matched = E();
			return matched ? E1() : matched;
		}
		return (l == '$' && (position + 1) == expression.length());
	}

	private void match() {
		updateErrorPosition();
		if (position < expression.length() - 1) {
			l = expression.charAt(++position);
		}
	}

	private void updateErrorPosition() {
		// Skip space in original string.
		skipSpacesInOriginalExpression();
		if (position < expression.length() - 1) {
			if (l == '(' || l == ')')
				errorPosition++;
			else if (l == '&')
				errorPosition = errorPosition + 3;
			else if (l == '|')
				errorPosition = errorPosition + 2;
			else if (l == 'z') {
				// Skip continuous digits in original string.
				while (Character.isDigit(originalExpression.charAt(errorPosition))) {
					errorPosition++;
				}
			}
		}
		skipSpacesInOriginalExpression();
	}

	private void skipSpacesInOriginalExpression() {
		while (originalExpression.charAt(errorPosition) == ' ') {
			errorPosition++;
		}
	}

	public int getErrorPosition() {
		return errorPosition;
	}

	public boolean isValidDate(String string) {
		boolean isValid = false;
		if (string == null) {
		return false;
		}
		try {
		ImportUtils._YYYY_MM_DD.parse(string);
		isValid = true;
		} catch (ParseException e) {
		isValid = false;
		}
		return isValid;
		}
	 
	
	  public Boolean isDouble(String string) {
		  Boolean ReturnValue = false; 
		  try {
			  Double d = Double.parseDouble(string);
			  ReturnValue = true;
			  }
		  catch (Exception e) { 
			  ReturnValue = false; 
			  } return ReturnValue;
			  }
	
	public void appendMessage(String constantName,String errorText) {
			String error=report.getErrors().getOrDefault(constantName, "");
			if(error.length()>0) {
				errorText=error+" #$ "+errorText;
			}
				report.error(constantName, errorText);
	}
}