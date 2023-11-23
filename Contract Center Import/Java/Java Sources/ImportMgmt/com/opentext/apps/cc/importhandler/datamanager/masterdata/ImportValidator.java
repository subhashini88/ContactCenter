package com.opentext.apps.cc.importhandler.datamanager.masterdata;

import java.util.Map;
import java.util.Objects;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.datamanager.masterdata.ImportConstants;
import com.opentext.apps.cc.importhandler.datamanager.masterdata.MetadataInitializer;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;

public class ImportValidator {
	
	private static final CordysLogger logger = CordysLogger.getCordysLogger(MasterDataImportHandler.class);
	
	public ReportItem validateLinkTypes(Map<String, String> rowData, MetadataInitializer metadata) {
		ReportItem report = new ReportItem();
		if (Objects.nonNull(rowData) && Objects.nonNull(metadata)) {
			String linkType = rowData.get(ImportConstants.LinkType);
			if ((Objects.isNull(linkType) || linkType.isEmpty())) {
				report.error(ImportConstants.LinkType, rowData.get(ImportConstants.LinkType) + " is invalid data ");
			}
			String reverseLinkType = rowData.get(ImportConstants.ReverseLinkType);
			if ((Objects.isNull(reverseLinkType) || reverseLinkType.isEmpty())) {
				report.error(ImportConstants.ReverseLinkType,
						rowData.get(ImportConstants.ReverseLinkType) + " is invalid data ");
			}

		}
		return report;
	}

	public ReportItem validateRelationTypes(Map<String, String> rowData, MetadataInitializer metadata) {
		ReportItem report = new ReportItem();
		if (Objects.nonNull(rowData) && Objects.nonNull(metadata)) {
			String relationTypeName = rowData.get(ImportConstants.NAME);
			if ((Objects.isNull(relationTypeName) || relationTypeName.isEmpty())) {
				report.error(ImportConstants.NAME, rowData.get(ImportConstants.NAME) + " is invalid data ");
			}
		}
		return report;
	}
	
	public ReportItem validateCurrencies(Map<String, String> rowData, MetadataInitializer metadata) {
		ReportItem report = new ReportItem();
		if (Objects.nonNull(rowData) && Objects.nonNull(metadata)) {
			String currency = rowData.get(ImportConstants.NAME);
			String status = rowData.get(ImportConstants.STATUS);
			String Conversionrate = rowData.get(ImportConstants.ConversionRateToUSD);
			if ((Objects.isNull(currency) || currency.isEmpty())) {
				report.error(ImportConstants.NAME, rowData.get(ImportConstants.NAME) + " is invalid data ");
			}
			if ((Objects.isNull(status) || status.isEmpty())) {
				report.error(ImportConstants.STATUS, rowData.get(ImportConstants.STATUS) + " is invalid data ");
			}
			if ((Objects.isNull(Conversionrate) || Conversionrate.isEmpty())) {
				report.error(ImportConstants.ConversionRateToUSD, rowData.get(ImportConstants.ConversionRateToUSD) + " is invalid data ");
			}
		}
		return report;
	}
	
	public ReportItem validateUOM(Map<String, String> rowData, MetadataInitializer metadata) {
		ReportItem report = new ReportItem();
		if (Objects.nonNull(rowData) && Objects.nonNull(metadata)) {
			String currency = rowData.get(ImportConstants.NAME);
			String status = rowData.get(ImportConstants.STATUS);
			if ((Objects.isNull(currency) || currency.isEmpty())) {
				report.error(ImportConstants.NAME, rowData.get(ImportConstants.NAME) + " is invalid data ");
			}
			if ((Objects.isNull(status) || status.isEmpty())) {
				report.error(ImportConstants.STATUS, rowData.get(ImportConstants.STATUS) + " is invalid data ");
			}
		}
		return report;
	}
	
	public ReportItem validateRegions(Map<String, String> rowData, MetadataInitializer metadata) {
		
		ReportItem report = new ReportItem();
		String inputValue;
		
		if (Objects.nonNull(rowData) && Objects.nonNull(metadata)) {
			
			inputValue = rowData.get(ImportConstants.REGION);
			if ((Objects.isNull(inputValue) || inputValue.isEmpty())) {
				report.error(ImportConstants.REGION, "Region cannot be empty.");
			}
			inputValue = rowData.get(ImportConstants.REGION_CODE);
			if ((Objects.isNull(inputValue) || inputValue.isEmpty())) {
				report.error(ImportConstants.REGION_CODE, "RegionCode cannot be empty.");
			}
			inputValue = rowData.get(ImportConstants.COUNTRY);
			if ((Objects.isNull(inputValue) || inputValue.isEmpty())) {
				report.error(ImportConstants.COUNTRY, "Country_Name cannot be empty.");
			}
			inputValue = rowData.get(ImportConstants.COUNTRY_CODE);
			if ((Objects.isNull(inputValue) || inputValue.isEmpty())) {
				report.error(ImportConstants.COUNTRY_CODE, "ISO_country_code cannot be empty.");
			}
			if(inputValue.length() > 2) {
					report.error(ImportConstants.COUNTRY_CODE, "ISO_country_code is invalid.");
			}
			inputValue = rowData.get(ImportConstants.STATE);
			if ((Objects.isNull(inputValue) || inputValue.isEmpty())) {
				report.error(ImportConstants.STATE, "State cannot be empty.");
			}
		}
		return report;
	}
}
