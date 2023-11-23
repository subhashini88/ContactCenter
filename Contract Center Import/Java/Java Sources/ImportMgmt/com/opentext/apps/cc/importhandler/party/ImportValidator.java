package com.opentext.apps.cc.importhandler.party;

import java.text.ParseException;
import java.util.Map;
import java.util.regex.Pattern;

import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;
import com.opentext.apps.cc.importcontent.DateUtil;
import com.opentext.apps.cc.importcontent.ImportUtils;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.party.ImportConstants.AccountType;

public class ImportValidator {
	public ReportItem validate(Map<String, String> rowData, MetadataInitializer metadata, String jobId) {
		ReportItem report = new ReportItem();
		if (rowData == null)
			return report;
		if (Utilities.isStringEmpty(rowData.get(ImportConstants.PARTY_REGISTERED_NAME))) {
			report.error(ImportConstants.PARTY_REGISTERED_NAME,
					"Mandatory field '" + ImportConstants.PARTY_REGISTERED_NAME + "' is missing");
		}

		if (Utilities.isStringEmpty(rowData.get(ImportConstants.PARTY_ACCOUNT_TYPE))) {
			report.error(ImportConstants.PARTY_ACCOUNT_TYPE,
					"Mandatory field '" + ImportConstants.PARTY_ACCOUNT_TYPE + "' is missing");
		}

		if (Utilities.isStringEmpty(rowData.get(ImportConstants.PARTY_NAME))) {
			report.error(ImportConstants.PARTY_NAME, "Mandatory field '" + ImportConstants.PARTY_NAME + "' is missing");
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.EMAIL))) {
			boolean validEmailFlag = Pattern.matches(
					"^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$",
					rowData.get(ImportConstants.EMAIL));
			if (!validEmailFlag) {
				report.error(ImportConstants.EMAIL,
						"The email Id  '" + rowData.get(ImportConstants.EMAIL) + "' is not valid");
			}
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.PARTY_COUNTRY))) {
			String countryId = metadata.countryMap.get(rowData.get(ImportConstants.PARTY_COUNTRY));
			if (Utilities.isStringEmpty(countryId)) {
				report.error(ImportConstants.PARTY_COUNTRY, "Country with the Name: '"
						+ rowData.get(ImportConstants.PARTY_COUNTRY) + "' doesn't exist in the system");
			}
		}

		// Enumeration validations

		// Status.
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.STATUS))) {
			String statusValue = rowData.get(ImportConstants.STATUS);
			if (!Status.contains(statusValue)) {
				report.error(ImportConstants.STATUS, rowData.get(ImportConstants.STATUS) + "' is not valid");
			}
		}

		// Account Type.
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.PARTY_ACCOUNT_TYPE))) {
			String statusValue = rowData.get(ImportConstants.PARTY_ACCOUNT_TYPE);
			if (!AccountType.contains(statusValue)) {
				report.error(ImportConstants.PARTY_ACCOUNT_TYPE, rowData.get(ImportConstants.PARTY_ACCOUNT_TYPE) + "' is not valid");
			}
		}

		// Inception date.
				if (!Utilities.isStringEmpty(rowData.get(ImportConstants.INCEPTION_DATE))) {
					String inceptionDate = rowData.get(ImportConstants.INCEPTION_DATE);
					if (!isValidDate(inceptionDate )) {
						report.error(ImportConstants.INCEPTION_DATE, String.format("The mentioned %s is not in a valid format ('yyyy-MM-dd hh:mm:ss')", inceptionDate));
					}
				}
				
		return report;
	}
	
	
	private boolean isValidDate(String string)
	{
		boolean isValid = false;
		if (string == null)
		{
			return false;
		}
		try
		{
			ImportUtils.sdf.parse(string);
			isValid = true;
		} 
		catch (ParseException e)
		{
			isValid = false;
		}
		return isValid;
	}
}
