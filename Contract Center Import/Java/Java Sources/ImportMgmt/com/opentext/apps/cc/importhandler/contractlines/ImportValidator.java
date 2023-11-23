package com.opentext.apps.cc.importhandler.contractlines;

import java.util.Map;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.contractlines.ImportConstants.Status;

public class ImportValidator {

	public ReportItem validate(Map<String,String> rowData, MetadataInitializer metadata)
	{
		ReportItem report = new ReportItem();
		if (rowData == null) return report;
		String contractId = null;
		
		if(Utilities.isStringEmpty(rowData.get(ImportConstants.LINE_NUMBER)))
		{
			report.error(ImportConstants.LINE_NUMBER, "Mandatory information "+ImportConstants.LINE_NUMBER+" is missing in excel");
		}
		if(Utilities.isStringEmpty(rowData.get(ImportConstants.CONTRACT_NUMBER)))
		{
			report.error(ImportConstants.CONTRACT_NUMBER, "Mandatory information "+ImportConstants.CONTRACT_NUMBER+" is missing in excel");
		}
		else
		{
			String contractNumber = rowData.get(ImportConstants.CONTRACT_NUMBER);
			contractId= metadata.contractNumberMap.get(contractNumber);
			
			if(null == contractId)
			{
				int response=0,cNumberNode=0;
				try{
					SOAPRequestObject request = new SOAPRequestObject(ImportConstants.CONTRACTS_IMPORT_NAMESPACE,"GetContractDetailsByCNumber",null,null);
					String cNumber = "<contractNumber>" + contractNumber + "</contractNumber>";
					cNumberNode = NomUtil.parseXML(cNumber);
					request.addParameterAsXml(cNumberNode);
					response = request.sendAndWait();
					contractId = Node.getDataWithDefault(NomUtil.getNode(".//Contract-id/ItemId", response), null);
					if(null != contractId)
					{
						metadata.contractNumberMap.put(contractNumber, contractId);
					}
					else 
					{
						report.error(ImportConstants.CONTRACT_NUMBER, "Contract doesn't exist in the system with the Contract Number: "+contractNumber);
					}
				}
				finally 
				{
					Utilities.cleanAll(response,cNumberNode);
				}
			}
		}
		if(Utilities.isStringEmpty(rowData.get(ImportConstants.PRODUCT_GROUP)) || Utilities.isStringEmpty(rowData.get(ImportConstants.SERVICE)))
		{
			if(Utilities.isStringEmpty(rowData.get(ImportConstants.PRODUCT_GROUP)))
			{
				report.error(ImportConstants.PRODUCT_GROUP, "Mandatory information "+ImportConstants.PRODUCT_GROUP+" is missing in excel");
			}
			if( Utilities.isStringEmpty(rowData.get(ImportConstants.SERVICE)))
			{
				report.error(ImportConstants.SERVICE, "Mandatory information "+ImportConstants.SERVICE+" is missing in excel");
			}
		}
		else
		{
			String serviceItemId =  metadata.productGroupsandServicesMap.get(rowData.get(ImportConstants.PRODUCT_GROUP)+";"+rowData.get(ImportConstants.SERVICE));
			if(Utilities.isStringEmpty(serviceItemId)) {
				report.error(ImportConstants.SERVICE, "The service "+rowData.get(ImportConstants.SERVICE)+"doesn't exist in the system under the Product group "+rowData.get(ImportConstants.PRODUCT_GROUP));
			}
		}
		
		if(!Utilities.isStringEmpty(rowData.get(ImportConstants.UNIT_OF_MEASUREMENT))) {
			String unitsOfMeasurementsId = metadata.unitOfMeasurementsMap.get(rowData.get(ImportConstants.UNIT_OF_MEASUREMENT));
			if(Utilities.isStringEmpty(unitsOfMeasurementsId)) {
				report.error(ImportConstants.UNIT_OF_MEASUREMENT, "Units of Measurement with the Name: '"+rowData.get(ImportConstants.UNIT_OF_MEASUREMENT)+"' doesn't exist in the system");
			}
		}
		
		if(!Utilities.isStringEmpty(rowData.get(ImportConstants.PO_NUMBER))) {
			if(null != contractId) {
				metadata.getAllContractPONumbers(contractId.substring(contractId.indexOf('.') + 1));
			}
			String PONumberID = metadata.poNumbersMap.get(rowData.get(ImportConstants.PO_NUMBER));
			if(Utilities.isStringEmpty(PONumberID)) {
				report.error(ImportConstants.PO_NUMBER, "PO number: '"+rowData.get(ImportConstants.PO_NUMBER)+"' doesn't exist in the system");
			}
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.STATUS))) {
			String statusValue = rowData.get(ImportConstants.STATUS);
			if (!Status.contains(statusValue)) {
				report.error(ImportConstants.STATUS,rowData.get(ImportConstants.STATUS) + "' is not valid");
			}
		}

		return report;

	}
}
