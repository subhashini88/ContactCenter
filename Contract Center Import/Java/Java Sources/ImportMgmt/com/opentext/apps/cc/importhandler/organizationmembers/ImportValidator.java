package com.opentext.apps.cc.importhandler.organizationmembers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportUtils;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.datamanager.statesandactions.ProcessItemType;

public class ImportValidator {

	public ReportItem validate(Map<String, String> rowData, MetadataInitializer metadata, String jobId) {
		ReportItem report = new ReportItem();
		if (rowData == null)
			return report;
		if (Utilities.isStringEmpty(rowData.get(ImportConstants.LEGACY_ID))) {
			report.error(ImportConstants.LEGACY_ID, "Legacy ID cannot be empty");
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.USER_ID))) {
			if (Objects.isNull(metadata) || Objects.isNull(metadata.otdsPersonsMap)
					|| Objects.isNull(metadata.otdsPersonsMap.get(rowData.get(ImportConstants.USER_ID)))) {
				String personId = getOtdsPerson(rowData.get(ImportConstants.USER_ID));
				if (personId != null) {
					metadata.otdsPersonsMap.put(rowData.get(ImportConstants.USER_ID), personId);
				} else {
					report.error(ImportConstants.USER_ID,
							"The user ID '" + rowData.get(ImportConstants.USER_ID) + "' is incorrect");
				}
			}
		} else {
			report.error(ImportConstants.USER_ID, "User ID cannot be empty");
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.PRIMARY_ORGANIZATION_CODE))) {
			String[] OrgCodes = rowData.get(ImportConstants.PRIMARY_ORGANIZATION_CODE).split(";");
			if(OrgCodes.length > 1) {
				report.error(ImportConstants.PRIMARY_ORGANIZATION_CODE, "Organization code must not have multiple values");
			}else {
			if (Objects.isNull(metadata) || Objects.isNull(metadata.getAllOrganizationsIdMap()) || (Objects.isNull(
					metadata.getAllOrganizationsIdMap().get(rowData.get(ImportConstants.PRIMARY_ORGANIZATION_CODE))))) {
				report.error(ImportConstants.PRIMARY_ORGANIZATION_CODE, "The organization code '"
						+ rowData.get(ImportConstants.PRIMARY_ORGANIZATION_CODE) + "' is incorrect");
			}
			if( metadata.duplicateOrg.contains(rowData.get(ImportConstants.PRIMARY_ORGANIZATION_CODE))) {
				report.error(ImportConstants.PRIMARY_ORGANIZATION_CODE, "The organization code '"
						+ rowData.get(ImportConstants.PRIMARY_ORGANIZATION_CODE) + "' refers to more than one organization. Ensure that only one reference is made for a given organization code");
			}
			}
		} else {
			report.error(ImportConstants.PRIMARY_ORGANIZATION_CODE,"Organization code cannot be empty");
		}

		String specialOrgCodeExp = rowData.get(ImportConstants.SPECIAL_ORGANIZATION_CODE);
		if (!Utilities.isStringEmpty(specialOrgCodeExp) && specialOrgCodeExp != null) {
			String[] specialOrgCodes = specialOrgCodeExp.split(";");
			for (String s : specialOrgCodes) {
				if (Objects.isNull(metadata) || Objects.isNull(metadata.getAllOrganizationsIdMap())
						|| Objects.isNull(metadata.getAllOrganizationsIdMap().get(s))) {
					report.error(ImportConstants.SPECIAL_ORGANIZATION_CODE,
							"Additional access '"+s+"' is incorrect");
				}
				if (s.equals(rowData.get(ImportConstants.PRIMARY_ORGANIZATION_CODE))) {
					report.error(ImportConstants.SPECIAL_ORGANIZATION_CODE,
							"Organization code '" + s + "' must not be included in additional access");
				}
				if(metadata.duplicateOrg.contains(s)) {
					report.error(ImportConstants.SPECIAL_ORGANIZATION_CODE,
							"Additional access '" + s + "' refers to more than one organization. Ensure that only one reference is made for a given organization code");
				}
			}
		}

		String startDateValue = rowData.get(ImportConstants.START_DATE);
		if (!Utilities.isStringEmpty(startDateValue)) {
			if (!isValidDate(startDateValue)) {
				report.error(ImportConstants.START_DATE,"Start date must be in the format yyyy-mm-dd");
			}

		} else {
			report.error(ImportConstants.START_DATE, "Start date cannot be empty");
		}

		String endDateValue = rowData.get(ImportConstants.END_DATE);
		if (!Utilities.isStringEmpty(endDateValue)) {
			if (!isValidDate(endDateValue)) {
				report.error(ImportConstants.END_DATE, "End date must be in the format yyyy-mm-dd");
			}
			SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
			try {
		      Date d1 = sdformat.parse(rowData.get(ImportConstants.START_DATE));
		      Date d2 = sdformat.parse(rowData.get(ImportConstants.END_DATE));
		      if(d1.compareTo(d2)>0) {
		    	  report.error(ImportConstants.END_DATE, "End date must not occur before Start date");
		      }
			}
			catch(Exception e)
			{				
			}
			
			if (!Utilities.isStringEmpty(rowData.get(ImportConstants.ISPRIMARYCONTACT))) {
				String val=rowData.get(ImportConstants.ISPRIMARYCONTACT);
				if(!(val.equalsIgnoreCase("YES") || val.equalsIgnoreCase("NO"))) {
				report.error(ImportConstants.ISPRIMARYCONTACT, "The value is incorrect");
				}
			}
		}
		
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CONTACT_TYPE))) {
			if (Objects.isNull(metadata) || Objects.isNull(metadata.getAllContactTypesIdMap()) || (Objects.isNull(
					metadata.getAllContactTypesIdMap().get(rowData.get(ImportConstants.CONTACT_TYPE))))) {
				report.error(ImportConstants.CONTACT_TYPE, "The contact type '"
						+ rowData.get(ImportConstants.CONTACT_TYPE) + "' is incorrect");
			}
		}
		
		return report;
	}

	public String getOtdsPerson(String userId) {
		int response = 0;
		int nodes[] = null;
		String itemId = null;
		String response_user=null;
		try {
			String[] paramNames = { "userID", "isInternal" };
			Object[] paramValues = { userId, "false" };
			SOAPRequestObject personRequest = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/cc/basiccomponents/20.2", "GetIdentityPersoswithFilters",
					paramNames, paramValues);
			response = personRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Persons/FindPersonListInternalResponse/Person", response);
			for (int i : nodes) {
				response_user = Node.getDataWithDefault(NomUtil.getNode(".//User_ID", i), null);
				if(response_user!=null && userId.equals(response_user))
				itemId = Node.getDataWithDefault(NomUtil.getNode(".//Person-id/ItemId", i), null);
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
		return itemId;
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
}
