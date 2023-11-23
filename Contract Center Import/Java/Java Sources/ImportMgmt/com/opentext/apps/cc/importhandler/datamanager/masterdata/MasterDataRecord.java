package com.opentext.apps.cc.importhandler.datamanager.masterdata;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;

public class MasterDataRecord implements ImportListener {

	private final MetadataInitializer metadata;
	private final ReportItem reportItem;
	private int recordNode = 0;

	public MasterDataRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
		this.reportItem = reportItem;
	}

	@Override
	public void doWork(ImportEvent event) {

	}

	@Override
	public void commit() {

	}

	@Override
	public void postCommit() {
	}

	@Override
	public Object getSourceId() {
		return null;
	}

	@Override
	public int getnode() {
		return this.recordNode;
	}

	protected void frameLinkTypeRecord(ImportEvent event) {

		Map<String, String> row = event.getRow();

		try {
			if (Objects.nonNull(row)) {
				this.recordNode = NomUtil.parseXML("<LinkTypesRecord></LinkTypesRecord>");
				Node.setDataElement(recordNode, "LinkType", row.get(ImportConstants.LinkType));
				Node.setDataElement(recordNode, "Description", row.get(ImportConstants.Description));
				Node.setDataElement(recordNode, "Status", row.get(ImportConstants.STATUS));
				Node.setDataElement(recordNode, "LinkTypeName", row.get(ImportConstants.LinkTypeName));
				Node.setDataElement(recordNode, "ReverseLinkType", row.get(ImportConstants.ReverseLinkType));
				Node.setDataElement(recordNode, "CreationType", "IMPORTED");
			}
		} catch (Exception e) {
			row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
			row.put(ImportConstants.STATUS_LOG, e.getMessage());
		}
	}

	protected void frameRelationTypeRecord(ImportEvent event) {

		Map<String, String> row = event.getRow();
		try {
			if (Objects.nonNull(row)) {
				recordNode = NomUtil.parseXML("<GCRelationTypeRecord></GCRelationTypeRecord>");
				Node.setDataElement(recordNode, "Name", row.get(ImportConstants.NAME));
				Node.setDataElement(recordNode, "Description", row.get(ImportConstants.Description));
				Node.setDataElement(recordNode, "Status", row.get(ImportConstants.STATUS));
				Node.setDataElement(recordNode, "Code", row.get(ImportConstants.CODE));
				Node.setDataElement(recordNode, "CreationType", "IMPORTED");
			}
		} catch (Exception e) {
			row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
			row.put(ImportConstants.STATUS_LOG, e.getMessage());
		}
	}
	
	protected void frameCurrencyTypeRecord(ImportEvent event) {

		Map<String, String> row = event.getRow();
		try {
			if (Objects.nonNull(row)) {
				recordNode = NomUtil.parseXML("<CurrenciesRecord></CurrenciesRecord>");
				Node.setDataElement(recordNode, "Name", row.get(ImportConstants.NAME));
				Node.setDataElement(recordNode, "CreationType", "IMPORTED");
				Node.setDataElement(recordNode, "Status", row.get(ImportConstants.STATUS));
				Node.setDataElement(recordNode, "Description", "");
				Node.setDataElement(recordNode, "ConversionRateToUSD", row.get(ImportConstants.ConversionRateToUSD));
				//Node.setDataElement(recordNode, "Code", row.get(ImportConstants.CODE));
				
			}
		} catch (Exception e) {
			row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
			row.put(ImportConstants.STATUS_LOG, e.getMessage());
		}
	}
	
	protected void frameUOMTypeRecord(ImportEvent event) {

		Map<String, String> row = event.getRow();
		try {
			if (Objects.nonNull(row)) {
				recordNode = NomUtil.parseXML("<UOMRecord></UOMRecord>");
				Node.setDataElement(recordNode, "Name", row.get(ImportConstants.NAME));
				Node.setDataElement(recordNode, "Description", "");
				Node.setDataElement(recordNode, "CreationType", "IMPORTED");
				Node.setDataElement(recordNode, "Status", row.get(ImportConstants.STATUS));
				//Node.setDataElement(recordNode, "Code", row.get(ImportConstants.CODE));
			}
		} catch (Exception e) {
			row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
			row.put(ImportConstants.STATUS_LOG, e.getMessage());
		}
	}
	
	protected String frameRegionRecord(ImportEvent event) {

		Map<String, String> row = event.getRow();
		int countryNode = 0, toLinkNode = 0;
		String toLink = "Y";
		int skipped = 0;
		String validRecord = null;
		String regionId = null, countryId = null, regionName = row.get(ImportConstants.REGION),
				countryName = row.get(ImportConstants.COUNTRY), stateName = row.get(ImportConstants.STATE);
		Set<String> countriesSet;
		
		try {
			if (Objects.nonNull(row) && Objects.nonNull(metadata)) {
				
					if (Objects.nonNull(metadata.regionsMap)) {
						
						regionId = metadata.regionsMap.get(regionName);
						
						if (Objects.isNull(regionId)) {
							
							regionId = createRegion((regionName),
									row.get(ImportConstants.REGION_CODE), row.get(ImportConstants.STATUS));
							
							if (Objects.isNull(regionId)) {
								return null;
							}
							metadata.regionsMap.put(regionName, regionId);
						} 
					} 
					if (Objects.nonNull(metadata.countriesMap)) {
						
						countryId = metadata.countriesMap.get(countryName);
						
						
						if (Objects.isNull(countryId)) {
							if (!metadata.ISOcodes.contains(row.get(ImportConstants.COUNTRY_CODE).toLowerCase())) {
								countryId = createCountry(countryName, row.get(ImportConstants.COUNTRY_CODE));
							}
							else {
								skipped++;
								validRecord = "yes";
							}
						}
						if (Objects.nonNull(countryId)) {
							metadata.countriesMap.put(countryName, countryId);
							
							if(metadata.RegionsCountriesMap.containsKey(regionName)) {
								if(metadata.RegionsCountriesMap.get(regionName).contains(countryName)) {
									toLink = "N";
								}
								else {
									metadata.RegionsCountriesMap.get(regionName).add(countryName);
								}
							}
							else {
								countriesSet = new HashSet<String>();
								countriesSet.add(countryName);
								metadata.RegionsCountriesMap.put(regionName, countriesSet);
							}
							validRecord = "yes";
						}else {
							if(Objects.isNull(validRecord))
								return null;
						}
					}
				
				recordNode = NomUtil.parseXML("<RegionRecord></RegionRecord>");
				if(skipped == 0) {
					Node.setDataElement(recordNode, "RegionItemId", regionId);
					Node.setDataElement(recordNode, "LinkedCountryItemId", countryId);
					Node.setDataElement(recordNode, "LinkedCountryName", countryName);
					Node.setDataElement(recordNode, "ToLink", toLink);
					Node.setDataElement(recordNode, "LinkedStateName", stateName);	
				}
				Node.setDataElement(recordNode, "Skipped", Integer.toString(skipped));
			}
		} catch (Exception e) {
			row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
			row.put(ImportConstants.STATUS_LOG, e.getMessage());
			validRecord = null;
		}
		return validRecord;
	}

	private String createRegion(String regionName, String regionCode, String status) {
		int RegionCreateResponse = 0, RegioncreateNode = 0;
		String itemId = null;
		int[] nodes;

		SOAPRequestObject importRequest = new SOAPRequestObject(
				"http://schemas/OpenTextContractCenter/Region/operations", "CreateRegion", null, null);
		
		RegioncreateNode = NomUtil.parseXML("<Region-create xmlns=\"http://schemas/OpenTextContractCenter/Region/operations\"></Region-create>");

		Node.setDataElement(RegioncreateNode, ImportConstants.REGION, regionName);
		Node.setDataElement(RegioncreateNode, ImportConstants.REGION_CODE, regionCode);
		Node.setDataElement(RegioncreateNode, ImportConstants.STATUS, status.toUpperCase());
		Node.setDataElement(RegioncreateNode, ImportConstants.CREATION_TYPE, ImportConstants.CREATION_TYPE_IMPORTED);
		
		importRequest.addParameterAsXml(RegioncreateNode);
		
		try {
			RegionCreateResponse = importRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Region", RegionCreateResponse);
			for (int i : nodes) {
				itemId = Node.getDataWithDefault(
						NomUtil.getNode(".//Region-id/ItemId", i), null);
			}
		} catch (Exception e) {
			itemId = null;
		} finally {
			Utilities.cleanAll(RegionCreateResponse, RegioncreateNode);
		}
		return itemId;
	}
	
	private String createCountry(String countryName, String countryCode) {
		int countryCreateResponse = 0, countryCreateNode = 0;
		String itemId = null;
		int[] nodes;

		SOAPRequestObject importRequest = new SOAPRequestObject(
				"http://schemas/OpenTextEntityIdentityComponents/Country/operations", "CreateCountry", null, null);
		
		countryCreateNode = NomUtil.parseXML("<Country-create xmlns=\"http://schemas/OpenTextEntityIdentityComponents/Country/operations\"></Country-create>");

		
		Node.setDataElement(countryCreateNode, ImportConstants.COUNTRY, countryName);
		Node.setDataElement(countryCreateNode, ImportConstants.COUNTRY_CODE, countryCode);
		
		importRequest.addParameterAsXml(countryCreateNode);
		
		try {
			countryCreateResponse = importRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//Country", countryCreateResponse);
			for (int i : nodes) {
				itemId = Node.getDataWithDefault(
						NomUtil.getNode(".//Country-id/ItemId", i), null);
			}
		} catch (Exception e) {
			itemId = null;
		} finally {
			Utilities.cleanAll(countryCreateResponse, countryCreateNode);
		}
		return itemId;	
	}
	
}
