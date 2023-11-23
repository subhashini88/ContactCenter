package com.opentext.apps.cc.importhandler.contractlines;

import java.util.Map;
import java.util.Objects;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.contractlines.ImportConstants.Status;

public class ContractLineRecord implements ImportListener{

	private final MetadataInitializer metadata;
	public int contractLineNode;//Check when to clean this node

	public ContractLineRecord(MetadataInitializer metadata, ReportItem reportItem){
		this.metadata= metadata;
	}

	@Override
	public void doWork(ImportEvent event) {
		create(event.getRow());
	}

	private void create(Map<String, String> row) {
		String contractNumber = row.get(ImportConstants.CONTRACT_NUMBER);//CONTRACT_NUMBER(Contract Id) is Contract Number in Contract Center 16.3
		contractLineNode = NomUtil.parseXML("<createContractLine></createContractLine>");
		
		String contractItemId = metadata.contractNumberMap.get(contractNumber);
		
		Node.setDataElement(contractLineNode, "ContractItemId", contractItemId);
		Node.setDataElement(contractLineNode, "ContractNumber", row.get(ImportConstants.CONTRACT_NUMBER));
		Node.setDataElement(contractLineNode, "LineNumber", row.get(ImportConstants.LINE_NUMBER));
		//Contract line properties that are not imported in BN
		Node.setDataElement(contractLineNode, "BreachComments", row.get(ImportConstants.BREACH_COMMENTS));
		Node.setDataElement(contractLineNode, "BreachNoticePeriod", row.get(ImportConstants.BREACH_NOTICE_PERIOD));
		Node.setDataElement(contractLineNode, "BreachPenalty", row.get(ImportConstants.BREACH_PENALITY));
		Node.setDataElement(contractLineNode, "CancellationComments", row.get(ImportConstants.CANCELLATION_COMMENTS));
		Node.setDataElement(contractLineNode, "CancellationDate", row.get(ImportConstants.CANCALLATION_DATE));
		Node.setDataElement(contractLineNode, "CancellationFees", row.get(ImportConstants.CANCELLATION_FEE));
		Node.setDataElement(contractLineNode, "CancellationNoticePeriod", row.get(ImportConstants.CANCELLATION_NOTICE_PERIOD));
		Node.setDataElement(contractLineNode, "CommunicationDate", row.get(ImportConstants.COMMUNICATION_DATE));
		Node.setDataElement(contractLineNode, "ContractLineName", row.get(ImportConstants.CONTRACT_LINE_NAME));
		Node.setDataElement(contractLineNode, "Description", row.get(ImportConstants.DESCRIPTION));
		Node.setDataElement(contractLineNode, "IncidentDate", row.get(ImportConstants.INCIDENT_DATE));
		Node.setDataElement(contractLineNode, "Price", row.get(ImportConstants.PRICE));
		Node.setDataElement(contractLineNode, "Quantity", row.get(ImportConstants.QUANTITY));
		Node.setDataElement(contractLineNode, "SKUOrServiceId", row.get(ImportConstants.SKU_OR_SERVICE_ID));
		Node.setDataElement(contractLineNode, "PONumber", row.get(ImportConstants.PO_NUMBER));

		if(!Utilities.isStringEmpty(row.get(ImportConstants.STATUS))) {
			Status status =  Status.getEnumObject(row.get(ImportConstants.STATUS));
			if(Objects.nonNull(status)) {
				Node.setDataElement(contractLineNode, ImportConstants.STATUS, status.getValue());
			}
		}
		Node.setDataElement(contractLineNode, "TrialPeriod", row.get(ImportConstants.TRAIL_PERIOD));
		

		if(!Utilities.isStringEmpty(row.get(ImportConstants.PRODUCT_GROUP))) {
			String serviceItemId =  metadata.productGroupsandServicesMap.get(row.get(ImportConstants.PRODUCT_GROUP)+";"+row.get(ImportConstants.SERVICE));
			if(null != serviceItemId) {
				Node.setDataElement(contractLineNode, "ServiceItemID1", serviceItemId);
			}
		}
		
		if(!Utilities.isStringEmpty(row.get(ImportConstants.UNIT_OF_MEASUREMENT))) {
			String unitOfMeasurementId =  metadata.unitOfMeasurementsMap.get(row.get(ImportConstants.UNIT_OF_MEASUREMENT));
			if(null != unitOfMeasurementId) {
				Node.setDataElement(contractLineNode, "UnitOfMeasurementId", unitOfMeasurementId);
			}
		}
		
		if(!Utilities.isStringEmpty(row.get(ImportConstants.PO_NUMBER))) {
			String poNumberId =  metadata.poNumbersMap.get(row.get(ImportConstants.PO_NUMBER));
			if(null != poNumberId) {
				Node.setDataElement(contractLineNode, "PONumberId", poNumberId);
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
		return contractLineNode;
	}

}
