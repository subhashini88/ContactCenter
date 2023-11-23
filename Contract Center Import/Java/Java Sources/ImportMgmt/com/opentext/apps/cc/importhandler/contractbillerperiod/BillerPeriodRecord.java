package com.opentext.apps.cc.importhandler.contractbillerperiod;

import java.util.Map;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.contractbillerperiod.ImportConstants;
import com.opentext.apps.cc.importhandler.contractbillerperiod.MetadataInitializer;

public class BillerPeriodRecord implements ImportListener{

	private final MetadataInitializer metadata;
	public int billerPeriodNode;//Check when to clean this node


	public BillerPeriodRecord(MetadataInitializer metadata, ReportItem reportItem){
		this.metadata= metadata;

	}

	@Override
	public void doWork(ImportEvent event) {
		create(event.getRow());

	}

	private void create(Map<String, String> row) {
		String contractNumber = row.get(ImportConstants.CONTRACT_NUMBER);
		String RevenueScheduleId = row.get(ImportConstants.REVENUE_ID);
		String billerPeriodInput = "<CreateBillerPeriod></CreateBillerPeriod>";
		billerPeriodNode = NomUtil.parseXML(billerPeriodInput);
		if(!Utilities.isStringEmpty(contractNumber)) {
			String contractId = metadata.contractNumberMap.get(contractNumber);
			Node.setDataElement(billerPeriodNode, "ContractItemId", contractId);
		}
		if(!Utilities.isStringEmpty(RevenueScheduleId)) {
			String revenueScheduleItemId = metadata.revenueScheduleIdMap.get(RevenueScheduleId);
			Node.setDataElement(billerPeriodNode, "revenueScheduleId", revenueScheduleItemId);
		}		
		Node.setDataElement(billerPeriodNode, "PeriodLevel", row.get(ImportConstants.PERIOD_LEVEL));
		Node.setDataElement(billerPeriodNode, "BillerDate", row.get(ImportConstants.BILLER_DATE));
		Node.setDataElement(billerPeriodNode, "Fees", row.get(ImportConstants.FEES));//recheck
		Node.setDataElement(billerPeriodNode, "VolumeAllowance", row.get(ImportConstants.VOLUME_ALLOWANCE));
		Node.setDataElement(billerPeriodNode, "BillerPeriodId", row.get(ImportConstants.BILLER_ID));
	}

	@Override
	public void commit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCommit() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getSourceId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getnode() {
		// TODO Auto-generated method stub
		return billerPeriodNode;
	}

}
