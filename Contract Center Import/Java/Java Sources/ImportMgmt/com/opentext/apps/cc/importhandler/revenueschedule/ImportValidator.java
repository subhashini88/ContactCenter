package com.opentext.apps.cc.importhandler.revenueschedule;

import java.util.Map;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.revenueschedule.ImportConstants.BillingType;

public class ImportValidator {
	
	public ReportItem validate(Map<String,String> rowData, MetadataInitializer metadata)
	{
		
		ReportItem report = new ReportItem();
		
		if (rowData == null) return report;
		
		if(Utilities.isStringEmpty(rowData.get(ImportConstants.ID1))){
			report.error(ImportConstants.ID1, "Revenue Schedule Id1 is empty");
		}
		if(Utilities.isStringEmpty(rowData.get(ImportConstants.ID))){
			report.error(ImportConstants.ID, "Contract Id is empty");
		}
		//contractNumberMap
		String contractNumber = rowData.get(ImportConstants.ID);
		String contractId= metadata.contractNumberMap.get(contractNumber);
		if(null == contractId)
		{
			int response=0,cNumberNode=0;
			try
			{
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
					report.error(ImportConstants.ID, "Contract doesn't exist in the system with the Contract Number: "+contractNumber);
				}
			}
			finally 
			{
				Utilities.cleanAll(response,cNumberNode);
			}
		}
		
		// Billing type.
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.BILLING_TYPE))) {
			String value = rowData.get(ImportConstants.BILLING_TYPE);
			if (!BillingType.contains(value)) {
				report.error(ImportConstants.BILLING_TYPE, rowData.get(ImportConstants.BILLING_TYPE) + "' is not valid");
			}
		}
		
		/*else {
			//Checking whether contract exist with the given contract name.
			if (contractLookup.isContractExist(ContractName)) {
				report.error(CONTRACT_NAME, "Contract already exist with this name : "+ContractName);
			}
		}
		if(Utilities.isStringEmpty(rowData.get(ImportConstants.START_DATE))){
			report.error(ImportConstants.START_DATE, "STARTDATE is empty");
		}else{
			if(!DataValidator.isValidDate(ImportConstants.START_DATE)){
				report.error(ImportConstants.START_DATE, "STARTDATE is not a Valid Date");
			}
		}
		if(Utilities.isStringEmpty(rowData.get(ImportConstants.BILLING_TYPE))){
			report.error(ImportConstants.BILLING_TYPE, "BILLINGTYPE is empty");
		}
		if(Utilities.isStringEmpty(rowData.get(ImportConstants.MONTH_FROM))){
			report.error(ImportConstants.MONTH_FROM, "MONTHFROM is empty");
		}else{
			if(!DataValidator.isInteger(ImportConstants.MONTH_FROM)){
				report.error(ImportConstants.MONTH_FROM, "MONTHFROM is empty");
			}
		}
		if(Utilities.isStringEmpty(rowData.get(ImportConstants.MONTH_TO))){
			report.error(ImportConstants.MONTH_TO, "MONTHTO is empty" );
		}else{
			if(!DataValidator.isInteger(ImportConstants.MONTH_TO)){
				report.error(ImportConstants.MONTH_TO, "MONTHTO is not valid Integer");
			}
		}
		if(Utilities.isStringEmpty(rowData.get(ImportConstants.PROCESS_FEES))){
			report.error(ImportConstants.PROCESS_FEES, "PROCESSFEES is empty");
		}else{
			if(!DataValidator.isInteger(ImportConstants.PROCESS_FEES)){
				report.error(ImportConstants.PROCESS_FEES, "PROCESSFEES is not valid Integer");
			}
		}
		if(Utilities.isStringEmpty(rowData.get(ImportConstants.RTSORPSSUPPORT_FEES))){
			report.error(ImportConstants.ID1, "RTSORPSSUPPORTFEES is empty");
		}else{
			if(!DataValidator.isInteger(ImportConstants.RTSORPSSUPPORT_FEES)){
				report.error(ImportConstants.RTSORPSSUPPORT_FEES, "RTSORPSSUPPORTFEES is not valid Integer");
			}
		}
		if(Utilities.isStringEmpty(rowData.get(ImportConstants.DEDICATION_SUPPORT))){
			report.error(ImportConstants.DEDICATION_SUPPORT, "DEDICATIONSUPPORT is empty");
		}else{
			if(!DataValidator.isInteger(ImportConstants.DEDICATION_SUPPORT)){
				report.error(ImportConstants.DEDICATION_SUPPORT, "DEDICATIONSUPPORT is not valid Integer");
			}
		}
		if(Utilities.isStringEmpty(rowData.get(ImportConstants.TOTAL_VALUE))){
			report.error(ImportConstants.TOTAL_VALUE, "TOTALVALUE is empty");
		}else{
			if(!DataValidator.isInteger(ImportConstants.TOTAL_VALUE)){
				report.error(ImportConstants.TOTAL_VALUE, "TOTALVALUE is not valid Integer");
			}
		}*/
		
		return report;
		
	}
}
