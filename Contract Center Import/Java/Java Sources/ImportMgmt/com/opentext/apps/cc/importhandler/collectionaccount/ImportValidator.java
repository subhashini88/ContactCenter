package com.opentext.apps.cc.importhandler.collectionaccount;

import java.util.Map;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.internal.NOMDocumentPool;
import com.eibus.xml.xpath.XPath;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;
import com.opentext.apps.cc.importhandler.collectionaccount.ImportConstants.UpdateStatus;

public class ImportValidator
{
	public ReportItem validate(Map<String,String> rowData,MetadataInitializer metadata, String jobId)
	{
		ReportItem report = new ReportItem();
		if (rowData == null) return report;
		validateLookupAttrs(rowData,metadata, report);
		validateEnumerations(rowData, report);
		return report;
	}

	private void validateLookupAttrs(Map<String, String> rowData, MetadataInitializer metadata, ReportItem report) 
	{
		if(!Utilities.isStringEmpty(rowData.get(ImportConstants.PARTY_REG_ID)))
		{
			String partyId = metadata.partiesMap.get(rowData.get(ImportConstants.PARTY_REG_ID));
			if(partyId == null) 
			{
				String partyRegId = rowData.get(ImportConstants.PARTY_REG_ID);
				if(partyRegId != null)
				{			
					String id = findEntityDatawithOneFilter("http://schemas/OpenTextPartyManagement/Party/operations","GetPartyByRegId","registrationId",partyRegId,".//Party-id/Id");
					if(id != null) 
					{
						metadata.partiesMap.put(rowData.get(ImportConstants.PARTY_REG_ID), id);
					}else 
					{
						report.error(ImportConstants.PARTY_REG_ID, "Party with the RegistrationId: '"+rowData.get(ImportConstants.PARTY_REG_ID)+"' doesn't exist in the system");
					}
				}
			}
		}
		else
		{
			report.error(ImportConstants.PARTY_REG_ID, "Mandatory information 'Party Registration ID' is missing in excel");
		}
	}

	public String findEntityDatawithOneFilter(final String nameSpace, final String serviceName, final String filterElement, final String filter, final String lookupFor)
	{
		String value = null;
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
			int itemNode = XPath.getFirstMatch(lookupFor, null, response);
			value = Node.getData(itemNode);
		}
		catch(Exception e) 
		{
			throw new RuntimeException(e);
		}
		finally
		{
			Utilities.cleanAll(nameElement, response);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
		return value;
	}
	
	private void validateEnumerations(Map<String, String> rowData, ReportItem report) {
	
		// Enumeration validations
	
		// Status.
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.STATUS))) {
			String value = rowData.get(ImportConstants.STATUS);
			if (!Status.contains(value)) {
				report.error(ImportConstants.STATUS, rowData.get(ImportConstants.STATUS) + "' is not valid");
			}
		}
		
		// Update status.
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.UPDATE_STATUS))) {
			String value = rowData.get(ImportConstants.UPDATE_STATUS);
			if (!UpdateStatus.contains(value)) {
				report.error(ImportConstants.UPDATE_STATUS, rowData.get(ImportConstants.UPDATE_STATUS) + "' is not valid");
			}
		}		
		
	}
}

