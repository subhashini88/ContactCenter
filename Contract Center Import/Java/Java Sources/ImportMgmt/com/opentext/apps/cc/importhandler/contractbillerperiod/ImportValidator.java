package com.opentext.apps.cc.importhandler.contractbillerperiod;

import java.util.Map;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.contractbillerperiod.ImportConstants;
import com.opentext.apps.cc.importhandler.contractbillerperiod.MetadataInitializer;

public class ImportValidator 
{
	public ReportItem validate(Map<String,String> rowData, MetadataInitializer metadata)
	{
		ReportItem report = new ReportItem();
		if (rowData == null) return report;
		if(Utilities.isStringEmpty(rowData.get(ImportConstants.BILLER_ID)))
		{
			report.error(ImportConstants.BILLER_ID, "Biller period id is empty");
		}
		if(Utilities.isStringEmpty(rowData.get(ImportConstants.CONTRACT_NUMBER)))
		{
			report.error(ImportConstants.CONTRACT_NUMBER, "Contract Id is empty");
		}
		//contractNumberMap
		String contractNumber = rowData.get(ImportConstants.CONTRACT_NUMBER);
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
					report.error(ImportConstants.CONTRACT_NUMBER, "Contract doesn't exist in the system with the Contract Number: "+contractNumber);
				}
			}
			finally
			{
				Utilities.cleanAll(response,cNumberNode);
			}
		}
		String revenueScheduleId = rowData.get(ImportConstants.REVENUE_ID);
		String revenueScheduleItemId= metadata.revenueScheduleIdMap.get(revenueScheduleId);	
		if(null == revenueScheduleItemId)
		{
			int response=0,rNumberNode=0;
			String revenueId=null;
			try{
				SOAPRequestObject request = new SOAPRequestObject(ImportConstants.CONTRACTS_IMPORT_NAMESPACE,"GetRevenueScheduleByRevenueId",null,null);
				String rNumber = "<revenueId>" + revenueScheduleId + "</revenueId>";
				rNumberNode = NomUtil.parseXML(rNumber);
				request.addParameterAsXml(rNumberNode);
				response = request.sendAndWait();
				revenueId = Node.getDataWithDefault(NomUtil.getNode(".//RevenueSchedule-id/Id1", response), null);
				if(null != revenueId) 
				{
					metadata.revenueScheduleIdMap.put(revenueScheduleId, revenueId);
				}
				/*else 
				{
					report.error(ImportConstants.REVENUE_ID, "Revenue schedule doesn't exist in the system with the Revenue schedule Id: "+revenueScheduleId);
				}*/				
			}finally {
				Utilities.cleanAll(response,rNumberNode);
			}	
		}
		return report;
	}
}
