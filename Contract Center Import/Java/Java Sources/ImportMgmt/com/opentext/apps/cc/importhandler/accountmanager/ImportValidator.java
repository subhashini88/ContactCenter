package com.opentext.apps.cc.importhandler.accountmanager;

import java.util.Map;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.internal.NOMDocumentPool;
import com.eibus.xml.xpath.XPath;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.CommonEnums.Location;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;

public class ImportValidator {
	public ReportItem validate(Map<String,String> rowData,MetadataInitializer metadata, String jobId)
	{
		ReportItem report = new ReportItem();
		if (rowData == null) return report;
		validateLookupAttrs(rowData,metadata, report);
		validateEnumerations(rowData, report);
		return report;
	}


	private void validateLookupAttrs(Map<String, String> rowData, MetadataInitializer metadata, ReportItem report) {

		if(!Utilities.isStringEmpty(rowData.get(ImportConstants.MANAGER_NAME))) {
			String contactId = metadata.accountManagerMap.get(rowData.get(ImportConstants.MANAGER_NAME));
			if(contactId == null) {
				String managerName = rowData.get(ImportConstants.MANAGER_NAME);
				if(managerName != null)
				{			
					String id = findEntityDatawithOneFilter("http://schemas.opentext.com/apps/contractcenterimport/16.3","GetPersonByName","contactName",managerName,".//Person-id/Id");
					if(id != null) {
						metadata.accountManagerMap.put(rowData.get(ImportConstants.MANAGER_NAME), id);
					}else {
						report.error(ImportConstants.MANAGER_NAME, "Account manager with the Name: '"+rowData.get(ImportConstants.MANAGER_NAME)+"' doesn't exist in the system");
					}
				}
			}
		}
		else
		{
			report.error(ImportConstants.MANAGER_NAME, "Mandatory information '"+ImportConstants.MANAGER_NAME+"' is missing in excel");
		}
	}
	public String findEntityDatawithOneFilter(final String nameSpace, final String serviceName, final String filterElement, final String filter, final String lookupFor){
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
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			Utilities.cleanAll(nameElement, response);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
		return value;
	}
	
	private void validateEnumerations(Map<String, String> rowData, ReportItem report) {

		// Enumeration validations

		// Location.
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.LOCATION))) {
			String value = rowData.get(ImportConstants.LOCATION);
			if (!Location.contains(value)) {
				report.error(ImportConstants.LOCATION, rowData.get(ImportConstants.LOCATION) + "' is not valid");
			}
		}

		// Status.
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.STATUS))) {
			String value = rowData.get(ImportConstants.STATUS);
			if (!Status.contains(value)) {
				report.error(ImportConstants.STATUS, rowData.get(ImportConstants.STATUS) + "' is not valid");
			}
		}
	}

}