package com.opentext.apps.cc.upgradeutils.util;

import java.util.List;

import com.opentext.apps.cc.upgradeutils.model.FieldModel;

public class SearchQueryUtil {

	private static final String contract_json_select_sql = "select info as contractsJson FROM ContractsJSONB";
	private static final String contract_json_ent_select_sql = "select info  from (SELECT data::jsonb  as info FROM public.o1opentextupgradeutilitycontractjsonnew ) as ctjson";
	private static final String contract_json_select_where = " WHERE 1=1 ";

	public String prepareSelectQuery(List<FieldModel> fieldModels) {
		StringBuffer buffer = new StringBuffer();
//		buffer.append(contract_json_select_sql);
		buffer.append(contract_json_ent_select_sql);
		buffer.append(contract_json_select_where);
		fieldModels.forEach(field -> {
			buffer.append(" and info -> 'Custom attributes' ->> " + "'" + field.getFieldName() + "'"
					+ getOperationKey(field.getOperator()) + getFieldValue(field.getValue(), field.getOperator()));
		});
		return buffer.toString();
	}

	private String getFieldValue(String value, String operator) {
		if (operator.trim().equalsIgnoreCase("EQUAL") || operator.trim().equalsIgnoreCase("NOTEQUAL")) {
			return "'" + value + "'";
		} else if (operator.trim().equalsIgnoreCase("CONTAINS")) {
			return "'%" + value + "%'";
		}
		return value;
	}

	private String getOperationKey(String operator) {
		if (operator.trim().equalsIgnoreCase("EQUAL")) {
			return " = ";
		} else if (operator.trim().equalsIgnoreCase("NOTEQUAL")) {
			return " != ";
		} else if (operator.trim().equalsIgnoreCase("CONTAINS")) {
			return " like ";
		}
		return null;
	}

}
