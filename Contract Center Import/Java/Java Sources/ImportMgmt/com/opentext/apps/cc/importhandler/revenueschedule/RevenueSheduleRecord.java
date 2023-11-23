package com.opentext.apps.cc.importhandler.revenueschedule;

import java.util.Map;
import java.util.Objects;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.revenueschedule.ImportConstants.BillingType;

public class RevenueSheduleRecord implements ImportListener{

	private final MetadataInitializer metadata;
	public int revenueScheduleNode;//Check when to clean this node
	
	
	public RevenueSheduleRecord(MetadataInitializer metadata, ReportItem reportItem){
		this.metadata= metadata;
	}
	
	@Override
	public void doWork(ImportEvent event) {
		create(event.getRow());
		
	}

	private void create(Map<String, String> row) {
		String contractNumber = row.get(ImportConstants.ID);//ID(Contract Id) is Contract Number in Contract Center 16.3
		revenueScheduleNode = NomUtil.parseXML("<CreateRevenueSchedule></CreateRevenueSchedule>");
		
		Node.setDataElement(revenueScheduleNode, "ProcessFees", row.get(ImportConstants.PROCESS_FEES));
		Node.setDataElement(revenueScheduleNode, "RTSOrPSSupportFees", row.get(ImportConstants.RTSORPSSUPPORT_FEES));
		Node.setDataElement(revenueScheduleNode, "DedicationSupport", row.get(ImportConstants.DEDICATION_SUPPORT));//recheck
		//service related lookups need to check
		Node.setDataElement(revenueScheduleNode, "RevenueId", row.get(ImportConstants.ID1));//BN Revenue schedule Id	
		if(!Utilities.isStringEmpty(row.get(ImportConstants.BILLING_TYPE))) {
			BillingType billingType =  BillingType.getEnumObject(row.get(ImportConstants.BILLING_TYPE));
			if(Objects.nonNull(billingType)) {
				Node.setDataElement(revenueScheduleNode, ImportConstants.BILLING_TYPE, billingType.getValue());
			}
		}
		Node.setDataElement(revenueScheduleNode, "MonthFrom", row.get(ImportConstants.MONTH_FROM));
		Node.setDataElement(revenueScheduleNode, "MonthTo", row.get(ImportConstants.MONTH_TO));
		Node.setDataElement(revenueScheduleNode, "TotalValue", row.get(ImportConstants.TOTAL_VALUE));
		Node.setDataElement(revenueScheduleNode, "StartDate", row.get(ImportConstants.START_DATE));
		Node.setDataElement(revenueScheduleNode, "VolumeAllowanceValue", row.get(ImportConstants.VOLUME_VALUE));
		
		Node.setDataElement(revenueScheduleNode, "contractItemId", metadata.contractNumberMap.get(contractNumber));
		
		if(!Utilities.isStringEmpty(row.get(ImportConstants.VOLUME_UNIT))) {
			String volumeItemId =  metadata.uomMap.get(row.get(ImportConstants.VOLUME_UNIT));
			if(null != volumeItemId) {
				Node.setDataElement(revenueScheduleNode, "VolumeAllowanceUnitsItemId", volumeItemId);
			}
		}
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
		return revenueScheduleNode;
	}

}
