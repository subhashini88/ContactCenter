package com.opentext.apps.cc.importhandler.collectionaccount;

import java.util.Map;
import java.util.Objects;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;
import com.opentext.apps.cc.importhandler.collectionaccount.ImportConstants.UpdateStatus;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;

public class CollectionAccountRecord implements ImportListener{

	int collectionAccountNode;
	MetadataInitializer metadata;
	public CollectionAccountRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
	}

	private static final CordysLogger logger = CordysLogger.getCordysLogger(CollectionAccountRecord.class);
	
	@Override
	public void doWork(ImportEvent event) {
		int partyNode=0,response=0;
		String partyId=null,partyData=event.getRow().get(ImportConstants.PARTY_REG_ID);
		
		collectionAccountNode = NomUtil.parseXML("<CollectionAccount></CollectionAccount>");
		partyId = metadata.partiesMap.get(partyData);
		if(partyId == null){
			try{
				SOAPRequestObject getPartyIdRequest = new SOAPRequestObject("http://schemas/OpenTextPartyManagement/Party/operations","GetClientByRegId",null,null);
				partyNode = NomUtil.parseXML("<registrationId>"+partyData+"</registrationId>");
				getPartyIdRequest.addParameterAsXml(partyNode);
				response= getPartyIdRequest.sendAndWait();
				partyId = Node.getDataWithDefault(NomUtil.getNode(".//Party-id/Id", response),null);
				metadata.partiesMap.put(partyData, partyId);
			}catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.collectionaccount.CollectionAccountRecord", Severity.ERROR, e, "Error while executing GetClientByRegId");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,"GetClientByRegId");
			}finally {
				Utilities.cleanAll(response);
			}
		}
		
		Node.setDataElement(collectionAccountNode, "Party", partyId);
		Node.setDataElement(collectionAccountNode, "SAPPayer", event.getRow().get(ImportConstants.SAP_PAYER_ACCOUNT));
		Node.setDataElement(collectionAccountNode, "SAPSoldTo", event.getRow().get(ImportConstants.SAP_SOLD_TO_ACCOUNT));
		Node.setDataElement(collectionAccountNode, "ID", event.getRow().get(ImportConstants.LEGACY_ID));
		if(!Utilities.isStringEmpty(event.getRow().get(ImportConstants.STATUS))) {
			Status status =  Status.getEnumObject(event.getRow().get(ImportConstants.STATUS));
			if(Objects.nonNull(status)) {
				Node.setDataElement(collectionAccountNode, ImportConstants.STATUS, status.getValue());
			}
		}
		Node.setDataElement(collectionAccountNode, "Code", event.getRow().get(ImportConstants.CODE));
		Node.setDataElement(collectionAccountNode, "LastUpdationJob", event.getRow().get(ImportConstants.LAST_UPDATION_JOB));
		Node.setDataElement(collectionAccountNode, "LastVerificationJob", event.getRow().get(ImportConstants.LAST_VERIFICATION_JOB));
		Node.setDataElement(collectionAccountNode, "Name", event.getRow().get(ImportConstants.NAME));
		if(!Utilities.isStringEmpty(event.getRow().get(ImportConstants.UPDATE_STATUS))) {
			UpdateStatus statusUpdate =  UpdateStatus.getEnumObject(event.getRow().get(ImportConstants.UPDATE_STATUS));
			if(Objects.nonNull(statusUpdate)) {
				Node.setDataElement(collectionAccountNode, ImportConstants.UPDATE_STATUS, statusUpdate.getValue());
			}
		}
		
		if(null != event.getRow().get(ImportConstants.MANAGER_ID))
		{
			Node.setDataElement(collectionAccountNode,"Manager",metadata.managersMap.get(event.getRow().get(ImportConstants.MANAGER_ID)));
		}
		
		//Node.setDataElement(collectionAccountNode, "AccountNumber", event.getRow().get(ImportConstants.COLLECTION_ACCOUNT));
		//Node.setDataElement(collectionAccountNode, "AccountNumber_with_escape", metadata.managersMap.get(ImportConstants));
		if (event.getRow().get(ImportConstants.COLLECTION_ACCOUNT).indexOf("&") >= 0) { // || event.getRow().get(ImportConstants.COLLECTION_ACCOUNT).indexOf("=") >= 0 || event.getRow().get(ImportConstants.COLLECTION_ACCOUNT).indexOf(";") >= 0) {
			String value_with_escape = null;
			value_with_escape = event.getRow().get(ImportConstants.COLLECTION_ACCOUNT).replace("&", "\\&");
			//value_with_escape = value_with_escape.replace("=", "\\=");
			//value_with_escape = value_with_escape.replace(";", "\\;");
			Node.setDataElement(collectionAccountNode, "AccountNumber", value_with_escape);
		}else{
			Node.setDataElement(collectionAccountNode, "AccountNumber", event.getRow().get(ImportConstants.COLLECTION_ACCOUNT));
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
		return collectionAccountNode;
	}

	public void doWork(CollectionAccountRecord collectionAccount, Map<String, String> row) {
		// TODO Auto-generated method stub
		
	}

}
