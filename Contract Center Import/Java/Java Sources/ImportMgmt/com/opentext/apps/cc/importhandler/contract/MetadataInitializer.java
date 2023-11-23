package com.opentext.apps.cc.importhandler.contract;

import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;

public class MetadataInitializer {
			
	public WeakHashMap<String, WeakHashMap<String,HashSet<String>>> externalPartiesAndContacts = new WeakHashMap<String, WeakHashMap<String,HashSet<String>>>();
	public WeakHashMap<String, WeakHashMap<String,HashSet<String>>> internalPartiesAndContacts = new WeakHashMap<String, WeakHashMap<String,HashSet<String>>>();
	public Map<String, String> contractingEntityMap = new WeakHashMap<String, String>();
	public WeakHashMap<String, WeakHashMap<String, String> > ipContactMap = new WeakHashMap<String, WeakHashMap<String, String>>();
	public WeakHashMap<String, WeakHashMap<String, String> > epContactMap = new WeakHashMap<String, WeakHashMap<String, String>>();
	public Map<String, String> clientMap = new WeakHashMap<String, String>();
	public Map<String, String> templateMap = new WeakHashMap<String, String>();
	public Map<String, String> organizationsMap = new WeakHashMap<String, String>();
	public Map<String, String> typeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> subtypeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> countryMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> currencyMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> renewalFlagStatusMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> cancellationReasonCodeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> cancellationReasonDetailMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> competitorMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> productAdoptedMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> cancellationTypeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> statusOutcomeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> docTypeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> documentOriginationMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> documentStatusMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> relatedTemplateMap = new WeakHashMap<String, String>();
	
	public Map<Integer, String> contractMetadataLifecycleNotcompletedMap = new TreeMap<>();
	public Map<Integer, String> contractAllStatusNotcompletedMap = new TreeMap<>();
	
	public Map<String, String> terminationReasonMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	//public Map<String, String> languageMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	//public Map<String, String> volumeAllowanceUnitsMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	//public Map<String, String> unitOfMeasurementsMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	//public Map<String, String> partyMap = new WeakHashMap<String, String>();
	//public Map<String, String> contractingEntityIdMap = new WeakHashMap<String, String>();
	public Map<String, String> validatedByIdMap = new WeakHashMap<String, String>();
	
	public MetadataInitializer() {
		this.getAllTypes();
		this.getAllSubtypes();
		this.getALLCountries();
		this.getAllCurrencies();
		this.getAllRenewalFlagStatus();
		this.getAllCancellationReasonCodes();
		this.getAllCancellationReasonDetails();
		this.getAllCompetitors();
		this.getAllProductAdopted();
		this.getAllCancellationTypes();
		this.getAllStatusOutcomes();
		this.getAllDocumentTypes();
		this.getAllDocumentOriginations();
		this.getAllDocumentStatuses();
		this.getAllTerminationReasons();
		this.getAllTemplates();
		//this.getAllLanguages();
		//this.getALLVolumeAllowanceUnits();
		//this.getAllUnitOfMeasurements();
	}
	
	private void getAllTemplates() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject templateRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetAllGCTemplates", null, null);
			int cursorNode = NomUtil.parseXML("<Cursor></Cursor>");
			Node.setDataElement(cursorNode,"Offset","0");
			Node.setDataElement(cursorNode,"Limit","200");
			templateRequest.addParameterAsXml(cursorNode);
			response = templateRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//GCTemplate", response);
			for (int i : nodes) {
				String TemplateId=Node.getDataWithDefault(NomUtil.getNode(".//TemplateId", i),null);
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//GCTemplate-id/ItemId", i),null);
				if(null != TemplateId && null != itemId){
					relatedTemplateMap.put(TemplateId,itemId);
				}
			}
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}		
		
	}
	
	private void getAllTypes() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject typesRequest = new SOAPRequestObject("http://schemas/OpenTextBasicComponents/GCType/operations", "GetAllTypes", null, null);
			response = typesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//GCType", response);
			for (int i : nodes) {
				String name=Node.getDataWithDefault(NomUtil.getNode(".//Name", i),null);
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//GCType-id/ItemId", i),null);
				if(null != name && null != itemId){
					typeMap.put(name,itemId);
				}
			}
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}		
		
	}

	private void getAllSubtypes() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject subtypesRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetAllSubtypes", null, null);
			response = subtypesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Subtype", response);
			for (int i : nodes) {
				String type=Node.getDataWithDefault(NomUtil.getNode(".//Owner/Name", i),null);
				String subtype=Node.getDataWithDefault(NomUtil.getNode(".//Name", i),null);
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//Subtype-id/ItemId1", i),null);
				if(null != subtype && null != type && null != itemId){
					subtypeMap.put(type+"-"+subtype,itemId);
				}
			}
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}	
	}

	private void getALLCountries() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject countriesRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetAllCountries", null, null);
			response = countriesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RelatedCountries", response);
			for (int i : nodes) {
				String name=Node.getDataWithDefault(NomUtil.getNode(".//Country_Name", i),null);
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//RelatedCountries-id/ItemId1", i),null);
				if(null != name && null != itemId){
					countryMap.put(name,itemId);
				}
			}		
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}	
	}
	
	private void getAllCurrencies() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject currenciesRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "getAllCurrencies", null, null);
			response = currenciesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Currency", response);
			for (int i : nodes) {
				String currency=null,itemId=null;
				currency= Node.getDataWithDefault(NomUtil.getNode(".//Name", i),null);
				itemId=Node.getDataWithDefault(NomUtil.getNode(".//Currency-id/ItemId", i),null);
				if(null != itemId && null != currency){
					this.currencyMap.put(currency, itemId);
				}
			}
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}		
		
	}
	
	private void getAllRenewalFlagStatus() {
		//Service call to get all the Renewal flag status
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject renewalflagRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "getAllRenewalFlagStatus", null, null);
			response = renewalflagRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//RenewalFlagStatus", response);
			for (int i : nodes) {
				String renewalFlagStatus=null,itemId=null;
				renewalFlagStatus= Node.getDataWithDefault(NomUtil.getNode(".//Name", i),null);
				itemId=Node.getDataWithDefault(NomUtil.getNode(".//RenewalFlagStatus-id/ItemId", i),null);
				if(null != itemId && null != renewalFlagStatus){
					this.renewalFlagStatusMap.put(renewalFlagStatus, itemId);
				}
			}
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);
			}
		}			
	}
	
	private void getAllCancellationReasonCodes() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject cancellationReasonCodeRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetAllCancellationReasonCodes", null, null);
			response = cancellationReasonCodeRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//CancellationCode", response);
			for (int i : nodes) {
				String cancellationReasonCode=null,Id=null;
				cancellationReasonCode= Node.getDataWithDefault(NomUtil.getNode(".//Name", i),null);
				Id=Node.getDataWithDefault(NomUtil.getNode(".//CancellationCode-id/Id", i),null);
				if(null != Id && null != cancellationReasonCode){
					this.cancellationReasonCodeMap.put(cancellationReasonCode, Id);
				}
			}
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}		
	}
	
	private void getAllCancellationReasonDetails() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject cancellationReasonDetailRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenter/CancellationCode.CancelReasonDetail/operations", "GetAllCancelReasonDetails", null, null);
			response = cancellationReasonDetailRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//CancelReasonDetail", response);
			for (int i : nodes) {
				String cancelReasonDetail=null,Id=null,Id1=null;
				cancelReasonDetail= Node.getDataWithDefault(NomUtil.getNode(".//Name", i),null);
				Id=Node.getDataWithDefault(NomUtil.getNode(".//CancelReasonDetail-id/Id", i),null);
				Id1=Node.getDataWithDefault(NomUtil.getNode(".//CancelReasonDetail-id/Id1", i),null);
				if(null != Id && null != Id1 && null != cancelReasonDetail){
					this.cancellationReasonDetailMap.put(Id+"-"+cancelReasonDetail, Id1);
				}
			}
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}		
	}
	
	private void getAllCompetitors() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject competitorsRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenter/CancellationCode.Competitor/operations", "GetAllCompetitors", null, null);
			response = competitorsRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Competitor", response);
			for (int i : nodes) {
				String competitor=null,Id=null,Id1=null;
				competitor= Node.getDataWithDefault(NomUtil.getNode(".//Name", i),null);
				Id=Node.getDataWithDefault(NomUtil.getNode(".//Competitor-id/Id", i),null);
				Id1=Node.getDataWithDefault(NomUtil.getNode(".//Competitor-id/Id1", i),null);
				if(null != Id && null != Id1 && null != competitor){
					this.competitorMap.put(Id+"-"+competitor, Id1);
				}
			}
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}		
	}
	
	private void getAllProductAdopted() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject productAdoptedRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenter/CancellationCode.ProductAdopted/operations", "GetAllProductAdopted", null, null);
			response = productAdoptedRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//ProductAdopted", response);
			for (int i : nodes) {
				String productAdopted=null,Id=null,Id1=null;
				productAdopted= Node.getDataWithDefault(NomUtil.getNode(".//Name", i),null);
				Id=Node.getDataWithDefault(NomUtil.getNode(".//ProductAdopted-id/Id", i),null);
				Id1=Node.getDataWithDefault(NomUtil.getNode(".//ProductAdopted-id/Id1", i),null);
				if(null != Id && null != Id1 && null != productAdopted){
					this.productAdoptedMap.put(Id+"-"+productAdopted, Id1);
				}
			}
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}		
	}
	
	private void getAllCancellationTypes() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject cancellationTypesRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenter/CancellationType/operations", "GetAllCancellationTypes", null, null);
			response = cancellationTypesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//CancellationType", response);
			for (int i : nodes) {
				String cancellationType=null,Id=null;
				cancellationType= Node.getDataWithDefault(NomUtil.getNode(".//Name", i),null);
				Id=Node.getDataWithDefault(NomUtil.getNode(".//CancellationType-id/Id", i),null);
				if(null != Id && null != cancellationType){
					this.cancellationTypeMap.put(cancellationType, Id);
				}
			}
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}		
	}
	
	private void getAllStatusOutcomes() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject statusOutcomeRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenter/Outcome/operations", "getAllOutcomes", null, null);
			response = statusOutcomeRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Outcome", response);
			for (int i : nodes) {
				String outcome=null,Id=null;
				outcome= Node.getDataWithDefault(NomUtil.getNode(".//Name", i),null);
				Id=Node.getDataWithDefault(NomUtil.getNode(".//Outcome-id/Id", i),null);
				if(null != Id && null != outcome){
					this.statusOutcomeMap.put(outcome, Id);
				}
			}
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}		
	}

	private void getAllDocumentTypes() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject docTypesRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetAllDocumentTypes", null, null);
			response = docTypesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//DocumentType", response);
			for (int i : nodes) {
				String name=Node.getDataWithDefault(NomUtil.getNode(".//Name", i),null);
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//DocumentType-id/ItemId", i),null);
				if(null != name && null != itemId){
					docTypeMap.put(name,itemId);
				}
			}
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}
		
	}

	private void getAllDocumentOriginations() {
		//Service call to get all the Document Originations
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject docOriginationRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "getAllDocumentOriginations", null, null);
			response = docOriginationRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//DocumentOrigination", response);
			for (int i : nodes) {
				String docOrigination=null,itemId=null;
				docOrigination= Node.getDataWithDefault(NomUtil.getNode(".//Name", i),null);
				itemId=Node.getDataWithDefault(NomUtil.getNode(".//DocumentOrigination-id/ItemId", i),null);
				if(null != itemId && null != docOrigination){
					this.documentOriginationMap.put(docOrigination, itemId);
				}
			}
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);
			}
		}	
	}
	
	private void getAllDocumentStatuses() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject docstatusRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetAllDocumentStatuses", null, null);
			response = docstatusRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//DocumentStatus", response);
			for (int i : nodes) {
				String unit=null,itemId=null;
				unit= Node.getDataWithDefault(NomUtil.getNode(".//DocStatus", i),null);
				itemId=Node.getDataWithDefault(NomUtil.getNode(".//DocumentStatus-id/ItemId", i),null);
				if(null != itemId && null != unit){
					documentStatusMap.put(unit, itemId);
				}
			}
		}finally {
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}
	}
	
	private void getAllTerminationReasons() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject terminationReasonCodeRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenter/TerminationReason/operations", "AllTerminationReasonsForContract", null, null);
			response = terminationReasonCodeRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//TerminationReason", response);
			for (int i : nodes) {
				String terminationReasonCode=null,itemId=null;
				terminationReasonCode= Node.getDataWithDefault(NomUtil.getNode(".//Reason", i),null);
				itemId=Node.getDataWithDefault(NomUtil.getNode(".//TerminationReason-id/ItemId", i),null);
				if(null != itemId && null != terminationReasonCode){
					this.terminationReasonMap.put(terminationReasonCode, itemId);
				}
			}
		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}
/*	private void getAllLanguages() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetAllLanguages", null, null);
			response = createRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Language", response);
			for (int i : nodes) {
				String language=null,itemId=null;
				language= Node.getDataWithDefault(NomUtil.getNode(".//Name", i),null);
				itemId=Node.getDataWithDefault(NomUtil.getNode(".//Language-id/ItemId", i),null);
				if(null != itemId){
					this.languageMap.put(language, itemId);
				}
			}

		}finally{
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}	
		
	}
	
	private void getAllUnitOfMeasurements() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetAllUOMs", null, null);
			response = createRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//UnitOfMeasurement", response);
			for (int i : nodes) {
				String unit=null,itemId=null;
				unit= Node.getDataWithDefault(NomUtil.getNode(".//Unit", i),null);
				itemId=Node.getDataWithDefault(NomUtil.getNode(".//UnitOfMeasurement-id/ItemId", i),null);
				if(null != itemId){
					unitOfMeasurementsMap.put(unit, itemId);
				}
			}
		}finally {
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}
	}

	private void getALLVolumeAllowanceUnits() {
		int response = 0;
		int nodes[] = null;
		try
		{
			SOAPRequestObject createRequest = new SOAPRequestObject("http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetAllVolumeAllowanceUnits", null, null);
			response = createRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//VolumeAllowanceUnits", response);
			for (int i : nodes) {
				String unit=null,itemId=null;
				unit= Node.getDataWithDefault(NomUtil.getNode(".//Unit", i),null);
				itemId=Node.getDataWithDefault(NomUtil.getNode(".//VolumeAllowanceUnits-id/ItemId", i),null);
				if(null != itemId){
					volumeAllowanceUnitsMap.put(unit, itemId);
				}
			}
		}finally {
			if(null!=nodes)
			{
				Utilities.cleanAll(response);	
			}
		}
	}*/
	
		
	}
}