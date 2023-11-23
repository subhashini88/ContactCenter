package com.opentext.apps.cc.importhandler.addresses;

import java.util.Map; 
import java.util.regex.Pattern;

import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.addresses.ImportConstants;
import com.opentext.apps.cc.importhandler.addresses.MetadataInitializer;

public class ImportValidator {

	public ReportItem validate(Map<String,String> rowData,MetadataInitializer metadata, String jobId)
	{

		ReportItem report = new ReportItem();
		if (rowData == null) return report;

		//Person Check
		/*if(getPersonId(rowData.get(ImportConstants.EMAIL)) == null)
report.error(ImportConstants.EMAIL, "Contact with the given Email ID does not exist in the system");*/

		//E-mail validation
		String email = rowData.get(ImportConstants.EMAIL);
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$"; 
		Pattern emailPattern = Pattern.compile(emailRegex); 

		if(Utilities.isStringEmpty(rowData.get(ImportConstants.EMAIL))) 
		{
			report.error(ImportConstants.EMAIL, "Email ID is missing");
		}
		else 
		{
			if (email == null || !emailPattern.matcher(email).matches())
			{
				report.error(ImportConstants.EMAIL, "Invalid Email ID"); 
			}
			else
			{
				if(metadata.emailMap.get(rowData.get(ImportConstants.EMAIL)) == null)
				{
					report.error(ImportConstants.EMAIL, "A contact with the email ID '"+rowData.get(ImportConstants.EMAIL)+"' does not exist.");
				}
			}
		}

		//DEFAULT field check
		if(Utilities.isStringEmpty(rowData.get(ImportConstants.DEFAULT)))
		{
			report.error(ImportConstants.DEFAULT, "Default value is missing");
		}

		//COUNTRY_NAME field check
		if(!Utilities.isStringEmpty(rowData.get(ImportConstants.COUNTRY_NAME))) 
		{
			if(metadata.countryMap.get(rowData.get(ImportConstants.COUNTRY_NAME)) == null)
			{
				report.error(ImportConstants.COUNTRY_NAME, "The country with the name '"+rowData.get(ImportConstants.COUNTRY_NAME)+"' does not exist.");
			}
			else 
			{
				if(!Utilities.isStringEmpty(rowData.get(ImportConstants.STATE_NAME))) 
				{
					if(metadata.stateMap.get(rowData.get(ImportConstants.STATE_NAME)+rowData.get(ImportConstants.COUNTRY_NAME)) == null)
					{
						report.error(ImportConstants.STATE_NAME, "The state with the name '"+rowData.get(ImportConstants.STATE_NAME)+"' does not exist under the country '"+rowData.get(ImportConstants.COUNTRY_NAME)+"'.");
					}
				}
			}

		}
		//Given state, but country is empty.
		else 
		{
			if(!Utilities.isStringEmpty(rowData.get(ImportConstants.STATE_NAME)))
			{
				if(Utilities.isStringEmpty(rowData.get(ImportConstants.COUNTRY_NAME)))
				{
					report.error(ImportConstants.COUNTRY_NAME, "The country is missing for the provided state.");
				}
			}
		}

		return report;
	}

}