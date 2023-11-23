package com.opentext.apps.cc.importhandler.datamanager.masterdata;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

public class MetadataInitializer {

	Map<String,String> regionsMap;
	Map<String,String> countriesMap;
	Map<String, Set<String>> RegionsCountriesMap;
	Set<String> ISOcodes;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(MasterDataImportHandler.class);
	
	
	public MetadataInitializer() {
		regionsMap = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
		countriesMap = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
		RegionsCountriesMap = new TreeMap<String, Set<String>>(String.CASE_INSENSITIVE_ORDER);
		ISOcodes = new HashSet<String>();
		loadAllCountries();
		loadAllLinkedCountries();
	}


	private void loadAllCountries() {
		int response = 0;
		int nodes[] = null;
		String itemId = null;
		String countryName = null;
		String ISO_country_code = null;
		
		try {
			
			SOAPRequestObject countryRequest = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetCountriesByName", null,
					null);
			response = countryRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//GetAllCountriesOutput/FindCountryListInternalResponse/Country", response);
			for (int i : nodes) {
				itemId = Node.getDataWithDefault(
						NomUtil.getNode(".//Country-id/ItemId", i), null);
				
				countryName = Node.getDataWithDefault(
						NomUtil.getNode(".//Country_Name", i), null);
				
				ISO_country_code = Node.getDataWithDefault(NomUtil.getNode(".//ISO_country_code", i), null);
				
				if(Objects.nonNull(itemId) && Objects.nonNull(countryName)) {
					countriesMap.putIfAbsent(countryName, itemId);
				}
				if(Objects.nonNull(ISO_country_code)) {
					ISOcodes.add(ISO_country_code.toLowerCase());
				}
			}
		} 
		catch(Exception e) {
			logger._log("com.opentext.apps.cc.importhandler.datamanager.masterdata.MasterDataImportHandler", Severity.ERROR, e, "Error while executing RegionsImport at loadAllCountries.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,
					"RegionsImport");
		}
		finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
		
	}
	
	private void loadAllLinkedCountries() {
		int response = 0;
		int nodes[] = null;
		String itemId = null;
		String RegionName = null;
		
		Set<String> relatedCountriesSet = null;
		
		try {
			
			SOAPRequestObject regionRequest = new SOAPRequestObject(
					"http://schemas/OpenTextContractCenter/Region/operations", "GetRegionsbyName", null,
					null);
			response = regionRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Region", response);
			
			for (int i : nodes) {
				itemId = Node.getDataWithDefault(
						NomUtil.getNode(".//Region-id/ItemId", i), null);
				RegionName = Node.getDataWithDefault(
						NomUtil.getNode(".//Name", i), null);
				
				if (Objects.nonNull(itemId) && Objects.nonNull(RegionName)) {
					
					if(!regionsMap.containsKey(RegionName)) {
						
						regionsMap.put(RegionName, itemId);
						relatedCountriesSet = getAllCountries(itemId);
						
						if(!relatedCountriesSet.isEmpty()) {
							RegionsCountriesMap.put(RegionName, relatedCountriesSet);
						}
					}
				}
			}
		} 
		catch(Exception e) {
			logger._log("com.opentext.apps.cc.importhandler.datamanager.masterdata.MasterDataImportHandler", Severity.ERROR, e, "Error while executing RegionsImport at loadAllLinkedCountries.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,
					"RegionsImport");
		}
		finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
		
	}
	
	public Set<String> getAllCountries(String itemId){
		Set<String> relatedCountriesSet = new HashSet<>();
		int RelatedCountriesNode = 0;
		int response = 0;
		int nodes[] = null;
		String countryName = null;
		
		SOAPRequestObject countryRequest = new SOAPRequestObject(
				"http://schemas/OpenTextContractCenter/Region/operations", "GetRelatedCountries", null, null);
		
		RelatedCountriesNode = NomUtil.parseXML("<Region-id xmlns=\"http://schemas/OpenTextContractCenter/Region\"></Region-id>");
		Node.setDataElement(RelatedCountriesNode, "ItemId", itemId);
		countryRequest.addParameterAsXml(RelatedCountriesNode);
		
		try {
			response = countryRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Title", response);
			
			for (int i : nodes) {
				countryName = Node.getDataWithDefault(
						NomUtil.getNode(".//Value", i), null);
				
				if (Objects.nonNull(countryName)) {
					relatedCountriesSet.add(countryName);
				}
			}
		}
		catch(Exception e) {
			logger._log("com.opentext.apps.cc.importhandler.datamanager.masterdata.MasterDataImportHandler", Severity.ERROR, e, "Error while executing RegionsImport at getAllCountries.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,
					"RegionsImport");
		}
		finally {
			if (null != nodes) 
			{
				Utilities.cleanAll(response);
			}
		}
			
		return relatedCountriesSet;
		
	}
	
	
}
