package com.opentext.apps.cc.importhandler.contract;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.ImportUtils;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.contract.ImportConstants.AmendmentType;
import com.opentext.apps.cc.importhandler.contract.ImportConstants.BillingStatus;
import com.opentext.apps.cc.importhandler.contract.ImportConstants.ChildType;
import com.opentext.apps.cc.importhandler.contract.ImportConstants.IntentType;
import com.opentext.apps.cc.importhandler.contract.ImportConstants.ParentType;
import com.opentext.apps.cc.importhandler.contract.ImportConstants.Priority;
import com.opentext.apps.cc.importhandler.contract.ImportConstants.TemplateType;


public class ContractRecord implements ImportListener {

	public int contractNode;
	private final MetadataInitializer metadata;
	private final ReportItem reportItem;
	private final ImportValidator validator;

	public ContractRecord(MetadataInitializer metadata, ReportItem reportItem, ImportValidator validator ) {
		this.reportItem = reportItem;
		this.metadata= metadata;
		this.validator = validator;
	}

	@Override
	public void doWork(ImportEvent event) {
		this.createRequest(event, false);
	}

	public void doWork(ImportEvent event, boolean isBatchUpload, boolean update) {
		if(update) {
			this.updateRequest(event, isBatchUpload);
		} else {
			this.createRequest(event, isBatchUpload);
		}
	}

	public ReportItem getReportItem() {
		return this.reportItem;
	}

	private void createRequest(ImportEvent event, boolean isBatchUpload) {
		Map<String, String> row = event.getRow();

		contractNode = NomUtil.parseXML("<Contract></Contract>");

		Node.setDataElement(contractNode, ImportConstants.CONTRACT_NUMBER, row.get(ImportConstants.CONTRACT_NUMBER));
		Node.setDataElement(contractNode, ImportConstants.CONTRACT_NAME, row.get(ImportConstants.CONTRACT_NAME));
		Node.setDataElement(contractNode, ImportConstants.DESCRIPTION, row.get(ImportConstants.DESCRIPTION));
		Node.setDataElement(contractNode, ImportConstants.CONTRACT_TERM, row.get(ImportConstants.CONTRACT_TERM));
		Node.setDataElement(contractNode, ImportConstants.RENEWAL, convertToLowerCase(row.get(ImportConstants.RENEWAL)));
		Node.setDataElement(contractNode, ImportConstants.AUTO_RENEW, convertToLowerCase(row.get(ImportConstants.AUTO_RENEW)));
		Node.setDataElement(contractNode, ImportConstants.AUTORENEW_DURATION, row.get(ImportConstants.AUTORENEW_DURATION));
		Node.setDataElement(contractNode, ImportConstants.RENEWAL_COMMENTS, row.get(ImportConstants.RENEWAL_COMMENTS));
		Node.setDataElement(contractNode, ImportConstants.DEAL_MANAGER, row.get(ImportConstants.DEAL_MANAGER));
		Node.setDataElement(contractNode, ImportConstants.IS_EXECUTED, convertToLowerCase(row.get(ImportConstants.IS_EXECUTED)));
		Node.setDataElement(contractNode, ImportConstants.IS_EXTERNAL, row.get(ImportConstants.IS_EXTERNAL));

		Node.setDataElement(contractNode, ImportConstants.START_DATE, row.get(ImportConstants.START_DATE));
		Node.setDataElement(contractNode, ImportConstants.MIN_START_DATE, row.get(ImportConstants.MIN_START_DATE));
		String endDate = ""; 
		if(isBatchUpload) {
			if(!ImportUtils.isEmpty(row.get(ImportConstants.END_DATE))) {
				endDate = row.get(ImportConstants.END_DATE);
			} else {
				if(!ImportUtils.isEmpty(row.get(ImportConstants.START_DATE)) && !ImportUtils.isEmpty(row.get(ImportConstants.CONTRACT_TERM))){
					endDate = ImportUtils.addMonthsToYYYYMMDD(row.get(ImportConstants.START_DATE), row.get(ImportConstants.CONTRACT_TERM));//If enddate is empty calculate
				}
			}
		} else {
			endDate = row.get(ImportConstants.END_DATE);
		}
		Node.setDataElement(contractNode, ImportConstants.END_DATE, endDate);
		Node.setDataElement(contractNode, ImportConstants.INACTIVATION_DATE, row.get(ImportConstants.INACTIVATION_DATE));
		Node.setDataElement(contractNode, ImportConstants.CANCELLATION_DATE, row.get(ImportConstants.CANCELLATION_DATE));
		Node.setDataElement(contractNode, ImportConstants.SIGNATURE_DATE, row.get(ImportConstants.SIGNATURE_DATE));
		Node.setDataElement(contractNode, ImportConstants.MAINTADJCOMMENCEMENT_DATE, row.get(ImportConstants.MAINTADJCOMMENCEMENT_DATE));
		Node.setDataElement(contractNode, ImportConstants.NEXT_EXPIRATION_DATE, row.get(ImportConstants.NEXT_EXPIRATION_DATE ));
		Node.setDataElement(contractNode, ImportConstants.CURRENT_START_DATE, row.get(ImportConstants.CURRENT_START_DATE ));
		Node.setDataElement(contractNode, ImportConstants.CURRENT_EXP_DATE, row.get(ImportConstants.CURRENT_EXP_DATE ));
		Node.setDataElement(contractNode, ImportConstants.PRODUCT_GOLIVE_DATE, row.get(ImportConstants.PRODUCT_GOLIVE_DATE ));
		Node.setDataElement(contractNode, ImportConstants.PRICEPROTECTION_DATE, row.get(ImportConstants.PRICEPROTECTION_DATE));
		Node.setDataElement(contractNode, ImportConstants.VALIDATED_ON, row.get(ImportConstants.VALIDATED_ON));		
		Node.setDataElement(contractNode, ImportConstants.ACTIONDURATION, row.get(ImportConstants.ACTIONDURATION));

		Node.setDataElement(contractNode, ImportConstants.SOFTWARE_FEES, row.get(ImportConstants.SOFTWARE_FEES));
		Node.setDataElement(contractNode, ImportConstants.UPFRONT_FEE, row.get(ImportConstants.UPFRONT_FEE));
		Node.setDataElement(contractNode, ImportConstants.PROFESSIONALSERVICES_FEES, row.get(ImportConstants.PROFESSIONALSERVICES_FEES));
		Node.setDataElement(contractNode, ImportConstants.MAINTENANCE_FEE, row.get(ImportConstants.MAINTENANCE_FEE));
		Node.setDataElement(contractNode, ImportConstants.TERMINATION_FEE, row.get(ImportConstants.TERMINATION_FEE));
		Node.setDataElement(contractNode, ImportConstants.MIN_CONTRACT_VALUE, row.get(ImportConstants.MIN_CONTRACT_VALUE));

		Node.setDataElement(contractNode, ImportConstants.SW_MAINTFEE_FOR_RENEWAL_TERM, row.get(ImportConstants.SW_MAINTFEE_FOR_RENEWAL_TERM));
		Node.setDataElement(contractNode, ImportConstants.MAINT_FEEADJ_DURING_CURRENTTERM, row.get(ImportConstants.MAINT_FEEADJ_DURING_CURRENTTERM));
		Node.setDataElement(contractNode, ImportConstants.SOFTWARE_LICENSE_TERM, row.get(ImportConstants.SOFTWARE_LICENSE_TERM));
		Node.setDataElement(contractNode, ImportConstants.NOTIFICATION_DURATION, row.get(ImportConstants.NOTIFICATION_DURATION));

		Node.setDataElement(contractNode, ImportConstants.DEAL_MARGIN, row.get(ImportConstants.DEAL_MARGIN));
		Node.setDataElement(contractNode, ImportConstants.CRMOPPURTUNITY_ID, row.get(ImportConstants.CRMOPPURTUNITY_ID));
		Node.setDataElement(contractNode, ImportConstants.CRMQUOTE_ID, row.get(ImportConstants.CRMQUOTE_ID));
		Node.setDataElement(contractNode, ImportConstants.PSPROJECT_ID, row.get(ImportConstants.PSPROJECT_ID));
		Node.setDataElement(contractNode, ImportConstants.RELATED_TO_DOCUMENT_ID, row.get(ImportConstants.RELATED_TO_DOCUMENT_ID));
		Node.setDataElement(contractNode, ImportConstants.CLIENT_EARLYTERM_RIGHT, convertToLowerCase(row.get(ImportConstants.CLIENT_EARLYTERM_RIGHT)));
		Node.setDataElement(contractNode, ImportConstants.END_USER, row.get(ImportConstants.END_USER));
		Node.setDataElement(contractNode, ImportConstants.AMENDMENT_SEQUENCE, row.get(ImportConstants.AMENDMENT_SEQUENCE));
		Node.setDataElement(contractNode, ImportConstants.TRAIL_PERIOD, row.get(ImportConstants.TRAIL_PERIOD));
		Node.setDataElement(contractNode, ImportConstants.TERMINATION_NOTICE_PERIOD, row.get(ImportConstants.TERMINATION_NOTICE_PERIOD));

		Node.setDataElement(contractNode, ImportConstants.ACTION_DATE_NOTIFY_VALUE, row.get(ImportConstants.ACTION_DATE_NOTIFY_VALUE));
		Node.setDataElement(contractNode, ImportConstants.AMENDMENT, convertToLowerCase(row.get(ImportConstants.AMENDMENT)));
		Node.setDataElement(contractNode, ImportConstants.AMENDMENT_COMMENTS, row.get(ImportConstants.AMENDMENT_COMMENTS));
		if(!Utilities.isStringEmpty(row.get(ImportConstants.AMENDMENT_TYPE))) {
			AmendmentType amendmentType =  AmendmentType.getEnumObject(row.get(ImportConstants.AMENDMENT_TYPE));
			if(Objects.nonNull(amendmentType)) {
				Node.setDataElement(contractNode, ImportConstants.AMENDMENT_TYPE, amendmentType.getValue());
			}
		}
		Node.setDataElement(contractNode, ImportConstants.BILLING_ACCOUNT, row.get(ImportConstants.BILLING_ACCOUNT));
		if(!Utilities.isStringEmpty(row.get(ImportConstants.CHILD_TYPE))) {
			ChildType childType =  ChildType.getEnumObject(row.get(ImportConstants.CHILD_TYPE));
			if(Objects.nonNull(childType)) {
				Node.setDataElement(contractNode, ImportConstants.CHILD_TYPE, childType.getValue());
			}
		}
		Node.setDataElement(contractNode, ImportConstants.CONTRACT_VALUE, row.get(ImportConstants.CONTRACT_VALUE));
		Node.setDataElement(contractNode, ImportConstants.CURRENT_SALES_ACCOUNT_EXECUTIVE, row.get(ImportConstants.CURRENT_SALES_ACCOUNT_EXECUTIVE));
		Node.setDataElement(contractNode, ImportConstants.CUSTOMER_MANAGER_COMMENTS, row.get(ImportConstants.CUSTOMER_MANAGER_COMMENTS));
		Node.setDataElement(contractNode, ImportConstants.DEFAULT_DOC_ID, row.get(ImportConstants.DEFAULT_DOC_ID));
		Node.setDataElement(contractNode, ImportConstants.DISTRIBUTION_COMMENTS, row.get(ImportConstants.DISTRIBUTION_COMMENTS));
		Node.setDataElement(contractNode, ImportConstants.EARLY_TERMINATION_CONDITIONS, row.get(ImportConstants.EARLY_TERMINATION_CONDITIONS));
		Node.setDataElement(contractNode, ImportConstants.EMAIL, row.get(ImportConstants.EMAIL));
		if(!Utilities.isStringEmpty(row.get(ImportConstants.INTENT_TYPE))) {
			IntentType intentType =  IntentType.getEnumObject(row.get(ImportConstants.INTENT_TYPE));
			if(Objects.nonNull(intentType)) {
				Node.setDataElement(contractNode, ImportConstants.INTENT_TYPE, intentType.getValue());
			}
		}
		Node.setDataElement(contractNode, ImportConstants.NOTIFICATION_DATE_NOTIFY_VALUE, row.get(ImportConstants.NOTIFICATION_DATE_NOTIFY_VALUE));
		Node.setDataElement(contractNode, ImportConstants.ORIGINAL_SALES_ACCOUNT_EXECUTIVE, row.get(ImportConstants.ORIGINAL_SALES_ACCOUNT_EXECUTIVE));
		Node.setDataElement(contractNode, ImportConstants.OVERRIDE_DEFAULT_EMAIL_TEMPLATE, convertToLowerCase(row.get(ImportConstants.OVERRIDE_DEFAULT_EMAIL_TEMPLATE)));
		
		if(!Utilities.isStringEmpty(row.get(ImportConstants.PARENT_TYPE))) {
			ParentType parentType =  ParentType.getEnumObject(row.get(ImportConstants.PARENT_TYPE));
			if(Objects.nonNull(parentType)) {
				Node.setDataElement(contractNode, ImportConstants.PARENT_TYPE, parentType.getValue());
			}
		}
		Node.setDataElement(contractNode, ImportConstants.PHONE, row.get(ImportConstants.PHONE));
		Node.setDataElement(contractNode, ImportConstants.PREVIOUS_CONTRACT_ID, row.get(ImportConstants.PREVIOUS_CONTRACT_ID));
		if(!Utilities.isStringEmpty(row.get(ImportConstants.PRIORITY))) {
			Priority priority =  Priority.getEnumObject(row.get(ImportConstants.PRIORITY));
			if(Objects.nonNull(priority)) {
				Node.setDataElement(contractNode, ImportConstants.PRIORITY, priority.getValue());
			}
		}
		
		
		Node.setDataElement(contractNode, ImportConstants.RENEWAL_DISCOUNT, row.get(ImportConstants.RENEWAL_DISCOUNT));
		Node.setDataElement(contractNode, ImportConstants.STATUS, row.get(ImportConstants.STATUS));	
		if(!Utilities.isStringEmpty(row.get(ImportConstants.TEMPLATE_TYPE))) {
			TemplateType templateType =  TemplateType.getEnumObject(row.get(ImportConstants.TEMPLATE_TYPE));
			if(Objects.nonNull(templateType)) {
				Node.setDataElement(contractNode, ImportConstants.TEMPLATE_TYPE, templateType.getValue());
			}
		}
		Node.setDataElement(contractNode, ImportConstants.TERMINATION_COMMENTS, row.get(ImportConstants.TERMINATION_COMMENTS));
		Node.setDataElement(contractNode, ImportConstants.TERMINATION_DATE, row.get(ImportConstants.TERMINATION_DATE));
		Node.setDataElement(contractNode, ImportConstants.TERMINATION_REASON_ID, row.get(ImportConstants.TERMINATION_REASON_ID));


		
		String comments = "";
		if(isBatchUpload)
		{
			if(!ImportUtils.isEmpty(row.get(ImportConstants.COMMENTS))) 
			{//Format comments
				comments = "...Biller Notes ("+ImportUtils.convertYYYYMMDD_To_DD_Mon_YYYY(row.get(ImportConstants.BILLER_DATA_DATE))+"): "+row.get(ImportConstants.COMMENTS)+"..."; //script - nvl2(mapped_rec.comments,'...Biller Notes (' ||to_char(mapped_rec.biller_data_date, 'DD-Mon-YYYY') || '): ' ||mapped_rec.comments || '...', null)
			}
		} 
		else 
		{
			comments = row.get(ImportConstants.COMMENTS);
		}
		
		Node.setDataElement(contractNode, ImportConstants.COMMENTS, comments);
		Node.setDataElement(contractNode, ImportConstants.CANCELLATION_COMMENTS, row.get(ImportConstants.CANCELLATION_COMMENTS));
		if(!Utilities.isStringEmpty(row.get(ImportConstants.BILLING_STATUS))) {
			BillingStatus billingStatus =  BillingStatus.getEnumObject(row.get(ImportConstants.BILLING_STATUS));
			if(Objects.nonNull(billingStatus)) {
				Node.setDataElement(contractNode, ImportConstants.BILLING_STATUS, billingStatus.getValue());
			}
		}
		Node.setDataElement(contractNode, ImportConstants.PRICEPROTECTION, row.get(ImportConstants.PRICEPROTECTION));
		Node.setDataElement(contractNode, ImportConstants.REVENUE_IMPACTIONG, row.get(ImportConstants.REVENUE_IMPACTIONG));
		Node.setDataElement(contractNode, ImportConstants.VALIDATED_FLAG, convertToLowerCase(row.get(ImportConstants.VALIDATED_FLAG)));
		Node.setDataElement(contractNode, ImportConstants.PREVIOUS_DOC_ID, row.get(ImportConstants.PREVIOUS_DOC_ID));
		Node.setDataElement(contractNode, ImportConstants.PO_REQD, row.get(ImportConstants.PO_REQD));
		Node.setDataElement(contractNode, ImportConstants.SAP_ORDER, row.get(ImportConstants.SAP_ORDER));

		Node.setDataElement(contractNode, ImportConstants.TARGET_STATE, row.get(ImportConstants.TARGET_STATE));
		Node.setDataElement(contractNode, ImportConstants.PERPETUAL, convertToLowerCase(row.get(ImportConstants.PERPETUAL)));
		//Node.setDataElement(contractNode, ImportConstants.PO_NUMBER, row.get(ImportConstants.PO_NUMBER));
		
		int PONumberNodes = NomUtil.parseXML("<PONumbers></PONumbers>");
		String relatedPONumbers = row.get(ImportConstants.PO_NUMBERS);
		
		if(!Utilities.isStringEmpty(relatedPONumbers) && relatedPONumbers!=null){
			String[] allPONumbers = relatedPONumbers.split(validator.getMappingPropertyValue(ImportConstants.PO_DELIMITER));
			for(String PO : allPONumbers ) {
				int poNumberNode = NomUtil.parseXML("<PONumber>" + PO + "</PONumber>");
				Node.appendToChildren(poNumberNode, PONumberNodes);
			  }
		  }
		Node.appendToChildren(PONumberNodes, contractNode);

		if(!Utilities.isStringEmpty(row.get(ImportConstants.TEMPLATE_ID))) {
			String templateId =  metadata.relatedTemplateMap.get(row.get(ImportConstants.TEMPLATE_ID));
			Node.setDataElement(contractNode, ImportConstants.TEMPLATE_ID, templateId);
		}
		
		if(!Utilities.isStringEmpty(row.get(ImportConstants.ORGANIZATION_ID))) {
			String organizationId =  metadata.organizationsMap.get(row.get(ImportConstants.ORGANIZATION_ID));
			Node.setDataElement(contractNode, ImportConstants.ORGANIZATION_ID, organizationId);
		}
		//Deprecated in cc22.3 bcz of multiparties
		/*if(!Utilities.isStringEmpty(row.get(ImportConstants.FIRST_PARTY_ID)) && !Utilities.isStringEmpty(row.get(ImportConstants.FIRST_PARTY_REG_ID))) {
			String firstPartyId =  metadata.contractingEntityMap.get(row.get(ImportConstants.FIRST_PARTY_REG_ID));
			Node.setDataElement(contractNode, ImportConstants.FIRST_PARTY_ID, firstPartyId);
		}

		if(!Utilities.isStringEmpty(row.get(ImportConstants.FIRSTPARTY_CONTACT_ID))) {
			String firstPartyContactId =  metadata.contactMap.get(row.get(ImportConstants.FIRSTPARTY_CONTACT_ID));
			Node.setDataElement(contractNode, ImportConstants.FIRSTPARTY_CONTACT_ID, firstPartyContactId);
		}

		if(!Utilities.isStringEmpty(row.get(ImportConstants.SECOND_PARTY_ID)) && !Utilities.isStringEmpty(row.get(ImportConstants.SECOND_PARTY_REG_ID))) {
			String secondPartyId =  metadata.clientMap.get(row.get(ImportConstants.SECOND_PARTY_REG_ID));
			Node.setDataElement(contractNode, ImportConstants.SECOND_PARTY_ID, secondPartyId);
		}
		
		if(!Utilities.isStringEmpty(row.get(ImportConstants.SECONDPARTY_CONTACT_ID))) {
			String secondPartyContactId =  metadata.contactMap.get(row.get(ImportConstants.SECONDPARTY_CONTACT_ID));
			Node.setDataElement(contractNode, ImportConstants.SECONDPARTY_CONTACT_ID, secondPartyContactId);
		}*/
		
		
		if(!Utilities.isStringEmpty(row.get(ImportConstants.CONTRACT_NUMBER))) {
			createPartiesNode(row,"InternalParties",  metadata.internalPartiesAndContacts.get(row.get(ImportConstants.CONTRACT_NUMBER)));
			createPartiesNode(row,"ExternalParties",  metadata.externalPartiesAndContacts.get(row.get(ImportConstants.CONTRACT_NUMBER)));
		}
		
		

		if(!Utilities.isStringEmpty(row.get(ImportConstants.CONTRACT_TYPE_ID))) {
			String contractTypeId =  metadata.typeMap.get(row.get(ImportConstants.CONTRACT_TYPE_ID));
			Node.setDataElement(contractNode, ImportConstants.CONTRACT_TYPE_ID, contractTypeId);
		}

		if(!Utilities.isStringEmpty(row.get(ImportConstants.CONTRACT_SUBTYPE_ID))) {
			String contractSubtypeId =  metadata.subtypeMap.get(row.get(ImportConstants.CONTRACT_TYPE_ID)+"-"+row.get(ImportConstants.CONTRACT_SUBTYPE_ID));
			Node.setDataElement(contractNode, ImportConstants.CONTRACT_SUBTYPE_ID, contractSubtypeId);
		}

		if(!Utilities.isStringEmpty(row.get(ImportConstants.COUNTRY_ID))) {
			String countryId =  metadata.countryMap.get(row.get(ImportConstants.COUNTRY_ID));
			Node.setDataElement(contractNode, ImportConstants.COUNTRY_ID, countryId);
		}

		if(!Utilities.isStringEmpty(row.get(ImportConstants.CURRENCY_ID))) {
			String currencyId =  metadata.currencyMap.get(row.get(ImportConstants.CURRENCY_ID));
			Node.setDataElement(contractNode, ImportConstants.CURRENCY_ID, currencyId);
		}

		if(!Utilities.isStringEmpty(row.get(ImportConstants.RENEWALFLAG_STATUS_ID))) {
			String renewalflagstatusId =  metadata.renewalFlagStatusMap.get(row.get(ImportConstants.RENEWALFLAG_STATUS_ID));
			Node.setDataElement(contractNode, ImportConstants.RENEWALFLAG_STATUS_ID, renewalflagstatusId);
		}

		if(!Utilities.isStringEmpty(row.get(ImportConstants.CANCEL_REASONCODE_ID))) {
			String cancellationReasonCodeId =  metadata.cancellationReasonCodeMap.get(row.get(ImportConstants.CANCEL_REASONCODE_ID));
			Node.setDataElement(contractNode, ImportConstants.CANCEL_REASONCODE_ID, cancellationReasonCodeId);
		}

		if(!Utilities.isStringEmpty(row.get(ImportConstants.CANCEL_REASON_DETAIL_ID))) {
			String cancelReasonDetailId =  metadata.cancellationReasonDetailMap.get(metadata.cancellationReasonCodeMap.get(row.get(ImportConstants.CANCEL_REASONCODE_ID))+"-"+row.get(ImportConstants.CANCEL_REASON_DETAIL_ID));
			Node.setDataElement(contractNode, ImportConstants.CANCEL_REASON_DETAIL_ID, cancelReasonDetailId);
		}

		if(!Utilities.isStringEmpty(row.get(ImportConstants.COMPETITOR_ID))) {
			String competitorId =  metadata.competitorMap.get(metadata.cancellationReasonCodeMap.get(row.get(ImportConstants.CANCEL_REASONCODE_ID))+"-"+row.get(ImportConstants.COMPETITOR_ID));
			Node.setDataElement(contractNode, ImportConstants.COMPETITOR_ID, competitorId);
		}

		if(!Utilities.isStringEmpty(row.get(ImportConstants.PRODUCT_ADOPTED_ID))) {
			String productAdoptedId =  metadata.productAdoptedMap.get(metadata.cancellationReasonCodeMap.get(row.get(ImportConstants.CANCEL_REASONCODE_ID))+"-"+row.get(ImportConstants.PRODUCT_ADOPTED_ID));
			Node.setDataElement(contractNode, ImportConstants.PRODUCT_ADOPTED_ID, productAdoptedId);
		}

		if(!Utilities.isStringEmpty(row.get(ImportConstants.CANCELLATION_TYPE_ID))) {
			String competitorId =  metadata.cancellationTypeMap.get(row.get(ImportConstants.CANCELLATION_TYPE_ID));
			Node.setDataElement(contractNode, ImportConstants.CANCELLATION_TYPE_ID, competitorId);
		}

		if(!Utilities.isStringEmpty(row.get(ImportConstants.STATUS_OUTCOME_ID))) {
			String productAdoptedId =  metadata.statusOutcomeMap.get(row.get(ImportConstants.STATUS_OUTCOME_ID));
			Node.setDataElement(contractNode, ImportConstants.STATUS_OUTCOME_ID, productAdoptedId);
		}

		if(!Utilities.isStringEmpty(row.get(ImportConstants.DOCUMENT_TYPE_ID))) {
			String documentTypeId =  metadata.docTypeMap.get(row.get(ImportConstants.DOCUMENT_TYPE_ID));
			Node.setDataElement(contractNode, ImportConstants.DOCUMENT_TYPE_ID, documentTypeId);
		}

		if(!Utilities.isStringEmpty(row.get(ImportConstants.DOCUMENT_ORIGINATION_ID))) {
			String documentoriginationId =  metadata.documentOriginationMap.get(row.get(ImportConstants.DOCUMENT_ORIGINATION_ID));
			Node.setDataElement(contractNode, ImportConstants.DOCUMENT_ORIGINATION_ID, documentoriginationId);
		}

		if(!Utilities.isStringEmpty(row.get(ImportConstants.CONTRACT_STATUS_ID))) {
			String documentstatusId =  metadata.documentStatusMap.get(row.get(ImportConstants.CONTRACT_STATUS_ID));
			Node.setDataElement(contractNode, ImportConstants.CONTRACT_STATUS_ID, documentstatusId);
		}

		if(!Utilities.isStringEmpty(row.get(ImportConstants.VALIDATED_BY))) {
			String validatedByUserId =  metadata.validatedByIdMap.get(row.get(ImportConstants.VALIDATED_BY));
			Node.setDataElement(contractNode, ImportConstants.VALIDATED_BY, validatedByUserId);
		}
		
		if(!Utilities.isStringEmpty(row.get(ImportConstants.TERMINATION_REASON_ID))) {
			String terminationreasonId =  metadata.terminationReasonMap.get(row.get(ImportConstants.TERMINATION_REASON_ID));
			Node.setDataElement(contractNode, ImportConstants.TERMINATION_REASON_ID, terminationreasonId);
		}
		Node.setDataElement(contractNode, ImportConstants.ACTION, ImportConstants.ACTION_CREATE);

	}
	
	private void createPartiesNode(Map<String, String> row,String partyName, WeakHashMap<String,HashSet<String>>  partiesAndContacts) {
		
		if(!partiesAndContacts.isEmpty()) {
			int ipMainNode = NomUtil.parseXML("<"+partyName+"></"+partyName+">");
			for (Map.Entry<String, HashSet<String>> entry : partiesAndContacts.entrySet()) {
				String key = entry.getKey();
				HashSet<String> val = entry.getValue();
				int partiesNode = NomUtil.parseXML("<parties></parties>");
				int partyIDNode = NomUtil.parseXML("<partyId>"+ key +"</partyId>");
				Node.appendToChildren(partyIDNode, partiesNode);
				int contactsNode = NomUtil.parseXML("<Contacts></Contacts>");
				for (String contcId : val) {
					int contactIdNode = NomUtil.parseXML("<ContactID>" + contcId + "</ContactID>");
					Node.appendToChildren(contactIdNode, contactsNode);
				}
				Node.appendToChildren(contactsNode, partiesNode);
				Node.appendToChildren(partiesNode, ipMainNode);
			}
			Node.appendToChildren(ipMainNode, contractNode);
	    }
	}

	/*
	 Only below can be updated for a contract after matching is done
	 1. Find internal contact with given name & country, if exists update contract.internalcontact_id
	 2. If contract.billing_status is not null && input billing_status is 'active'/'inactive', then update contract.billing_status to input value
	 3. If contract.currency is null && input currency is present, then update contract.currency
	 4. If contract.cnumber is empty, then update it with input contract_description
	 5. If contract.contract_term is empty, then update it with input contract_term
	 6. If contract.autorenew id is empty, then update it with input auto_renew
	 7. If contract.auto_renew_months is empty, then update it with input auto_renew_months
	 8. If input comments present, 
	 			if contract.comments has ...Biller Notes, then update contract.comments = regexp_replace(comments, '(.*)\.\.\.Biller Notes.*\.\.\.(.*)', '\1 ...Biller Notes (' || to_char(hdr_rec.biller_data_date, 'DD-Mon-YYYY') || '):' || hdr_rec.comments || '...\2')
	 			else update comments = comments || '...Biller Notes (' || to_char(hdr_rec.biller_data_date, 'DD-Mon-YYYY') || '):' || hdr_rec.comments || '...'
	 9. Delete contract_biller_periods for the matched contract
	 */
	private void updateRequest(ImportEvent event, boolean isBatchUpload) {
		Map<String, String> row = event.getRow();

		contractNode = NomUtil.parseXML("<Contract></Contract>");
		Node.setDataElement(contractNode, ImportConstants.CONTRACT_NUMBER, row.get(ImportConstants.CONTRACT_NUMBER));
		Node.setDataElement(contractNode, ImportConstants.CONTRACT_TERM, row.get(ImportConstants.CONTRACT_TERM));
		Node.setDataElement(contractNode, ImportConstants.AUTO_RENEW, convertToLowerCase(row.get(ImportConstants.AUTO_RENEW)));
		Node.setDataElement(contractNode, ImportConstants.AUTORENEW_DURATION, row.get(ImportConstants.AUTORENEW_DURATION));
		String comments = "";
		if(!ImportUtils.isEmpty(row.get(ImportConstants.COMMENTS))) {
			comments = "...Biller Notes ("+ImportUtils.convertYYYYMMDD_To_DD_Mon_YYYY(row.get(ImportConstants.BILLER_DATA_DATE))+"): "+row.get(ImportConstants.COMMENTS)+"..."; //script - nvl2(mapped_rec.comments,'...Biller Notes (' ||to_char(mapped_rec.biller_data_date, 'DD-Mon-YYYY') || '): ' ||mapped_rec.comments || '...', null)
		}
		Node.setDataElement(contractNode, "Comments", comments);
		Node.setDataElement(contractNode, ImportConstants.BILLING_STATUS, ImportUtils.isEmpty(row.get(ImportConstants.BILLING_STATUS))? "" : row.get(ImportConstants.BILLING_STATUS).trim().toLowerCase());

		//String firstPartyContactId =  metadata.contactMap.get(row.get(ImportConstants.FIRSTPARTY_CONTACT_ID));
		//Node.setDataElement(contractNode, ImportConstants.FIRSTPARTY_CONTACT_ID, firstPartyContactId);

		String currencyId =  metadata.currencyMap.get(row.get(ImportConstants.CURRENCY_ID));
		Node.setDataElement(contractNode, ImportConstants.CURRENCY_ID, currencyId);		
		Node.setDataElement(contractNode, ImportConstants.ACTION,  ImportConstants.ACTION_UPDATE);
		Node.setDataElement(contractNode, ImportConstants.CONTRACT_ITEM_ID, row.get(ImportConstants.CONTRACT_ITEM_ID));
		Node.setDataElement(contractNode, ImportConstants.DESCRIPTION, row.get(ImportConstants.DESCRIPTION));
		//Node.setDataElement(contractNode, ImportConstants.PO_NUMBER, row.get(ImportConstants.PO_NUMBER));
		
		int PONumberNodes = NomUtil.parseXML("<PONumbers></PONumbers>");
		String relatedPONumbers = row.get(ImportConstants.PO_NUMBERS);
		if(!Utilities.isStringEmpty(relatedPONumbers) && relatedPONumbers!=null){
			String[] allPONumbers = relatedPONumbers.split(";");
			for(String PO : allPONumbers ) {
				int poNumberNode = NomUtil.parseXML("<PONumber>" + PO + "</PONumber>");
				Node.appendToChildren(poNumberNode, PONumberNodes);
			  }
		  }
		
		Node.appendToChildren(PONumberNodes, contractNode);
	}
	
	private String convertToLowerCase(String inp) {
		
		if(!Utilities.isStringEmpty(inp)) {
			return inp.toLowerCase();
		}
		return inp;
	}

	@Override
	public void postCommit() {
	}

	@Override
	public int getnode() {
		return contractNode;
	}

	@Override
	public void commit() {
	}

	@Override
	public Object getSourceId() {
		return null;
	}
}
