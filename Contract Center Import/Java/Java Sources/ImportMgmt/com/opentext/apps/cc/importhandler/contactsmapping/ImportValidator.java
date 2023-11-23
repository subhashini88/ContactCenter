package com.opentext.apps.cc.importhandler.contactsmapping;

import java.util.Map;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.internal.NOMDocumentPool;
import com.eibus.xml.xpath.XPath;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.contactsmapping.ImportConstants;
import com.opentext.apps.cc.importhandler.contactsmapping.MetadataInitializer;

public class ImportValidator 
{
	public ReportItem validate(Map<String,String> rowData,MetadataInitializer metadata, String jobId)
	{
		ReportItem report = new ReportItem();
		if (rowData == null) return report;
		validateLookupAttrs(rowData,metadata, report);
		return report;
	}

	private void validateLookupAttrs(Map<String, String> rowData, MetadataInitializer metadata, ReportItem report)
	{
		String accountType = "EXTERNAL";
		if(!Utilities.isStringEmpty(rowData.get(ImportConstants.PARTY_REGISTRATION_ID))) 
		{
			String partyId = metadata.partyMap.get(rowData.get(ImportConstants.PARTY_REGISTRATION_ID));
			accountType = metadata.partyAccountTypeMap.get(rowData.get(ImportConstants.PARTY_REGISTRATION_ID));
			if(partyId == null || accountType == null) 
			{
				String partyRegId = rowData.get(ImportConstants.PARTY_REGISTRATION_ID);
				if(partyRegId != null)
				{			
					String values[] = findEntityDatawithOneFilter("http://schemas/OpenTextPartyManagement/Party/operations","GetPartyByRegId","registrationId",partyRegId,".//Party-id/Id",".//AccountType");
					String id = values[0];
					accountType = values[1];
					if(id != null)
					{
						metadata.partyMap.put(rowData.get(ImportConstants.PARTY_REGISTRATION_ID), id);
						metadata.partyAccountTypeMap.put(rowData.get(ImportConstants.PARTY_REGISTRATION_ID), accountType);
					}
					else
					{
						report.error(ImportConstants.PARTY_REGISTRATION_ID, "Party with the RegistrationId: '"+rowData.get(ImportConstants.PARTY_REGISTRATION_ID)+"' doesn't exist in the system");
					}
				}
			}
		}
		else
		{
			report.error(ImportConstants.PARTY_REGISTRATION_ID, "Mandatory information '"+ImportConstants.PARTY_REGISTRATION_ID+"' is missing in excel");
		}
		if(!Utilities.isStringEmpty(rowData.get(ImportConstants.CONTACT_EMAIL))) 
		{
			if(accountType != null)
			{
				String contactId = metadata.contactMap.get(rowData.get(ImportConstants.CONTACT_EMAIL));
				if(contactId == null)
				{
					String contactEmail = rowData.get(ImportConstants.CONTACT_EMAIL);
					if(contactEmail != null)
					{			
						String id = findEntityDatawithTwoFilters("http://schemas.opentext.com/apps/contractcenterimport/16.3","GetPersonDetailsbyEmailId","EmailId",contactEmail,"Is_Internal",accountType.equals("EXTERNAL") ? "": "false",".//Person-id/Id");
						if(id != null) 
						{
							metadata.contactMap.put(rowData.get(ImportConstants.CONTACT_EMAIL), id);
						}
						else
						{
							report.error(ImportConstants.CONTACT_EMAIL, accountType.equals("EXTERNAL") ? "External contact with the Email: '"+rowData.get(ImportConstants.CONTACT_EMAIL)+"' doesn't exist in the system": "Internal contact with the Email: '"+rowData.get(ImportConstants.CONTACT_EMAIL)+"' doesn't exist in the system");
						}
					}
				}
			}
		}
		else
		{
			report.error(ImportConstants.CONTACT_EMAIL, "Mandatory information '"+ImportConstants.CONTACT_EMAIL+"' is missing in excel");
		}
	}

	public String[] findEntityDatawithOneFilter(final String nameSpace, final String serviceName, final String filterElement, final String filter, final String lookupFor1, final String lookupFor2)
	{
		String lookupNodeValues[] = new String[2] ;
		Document document = NOMDocumentPool.getInstance().lendDocument();
		int nameElement = 0;
		int response = 0;
		try 
		{
			nameElement = document.createElement(filterElement);
			document.createText(filter, nameElement);
			SOAPRequestObject importRequest = new SOAPRequestObject(nameSpace, serviceName, null, null);
			importRequest.addParameterAsXml(nameElement);
			response = importRequest.sendAndWait();
			int lookupNode1 = XPath.getFirstMatch(lookupFor1, null, response);
			int lookupNode2 = XPath.getFirstMatch(lookupFor2, null, response);
			lookupNodeValues[0] = Node.getData(lookupNode1);
			lookupNodeValues[1] = Node.getData(lookupNode2);
		}catch(Exception e) 
		{
			throw new RuntimeException(e);
		}
		finally 
		{
			Utilities.cleanAll(nameElement, response);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
		return lookupNodeValues;
	}

	public String findEntityDatawithTwoFilters(final String nameSpace, final String serviceName, final String filterElementOne, final String filterOne, final String filterElementTwo, final String filterTwo, final String lookupFor)
	{
		String value = null;
		Document document = NOMDocumentPool.getInstance().lendDocument();
		int filterElement1=0, filterElement2 = 0;
		int response = 0;
		try 
		{
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
		}
		catch(Exception e) 
		{
			throw new RuntimeException(e);
		}
		finally 
		{
			Utilities.cleanAll(filterElement1, filterElement2, response);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
		return value;
	}
}

