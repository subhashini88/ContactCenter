package com.opentext.apps.cc.importhandler.contract.relateddepmembers;

import java.util.Map;
import java.util.Objects;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.contract.relateddepmembers.ImportConstants;

public class ImportValidator {

	public ReportItem validate(Map<String, String> rowData, MetadataInitializer metadata, String jobId) {
		ReportItem report = new ReportItem();
		if (rowData == null) {
			return report;
		}

		if (Utilities.isStringEmpty(rowData.get(ImportConstants.LEGACY_ID))) {
			report.error(ImportConstants.LEGACY_ID, "Legacy ID cannot be empty");
		}
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CONTRACT_NUMBER))) {
			String contractId = metadata.getContractIdByContractNumber(rowData.get(ImportConstants.CONTRACT_NUMBER));
			if (contractId == null) {
				report.error(ImportConstants.CONTRACT_NUMBER,
						"Contract doesn't exist in the system with the Contract number: "
								+ rowData.get(ImportConstants.CONTRACT_NUMBER));
			}
			if (contractId == "MULTIPLE_CONTRACTS") {
				report.error(ImportConstants.CONTRACT_NUMBER,
						"Multiple contracts exist in the system with the Contract number: "
								+ rowData.get(ImportConstants.CONTRACT_NUMBER));
			}

		} else {
			report.error(ImportConstants.CONTRACT_NUMBER, "Contract number cannot be empty");
		}

		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.USER_ID))) {
			if (Objects.isNull(metadata)
					|| Objects.isNull(metadata.getOtdsPerson(rowData.get(ImportConstants.USER_ID)))) {

				report.error(ImportConstants.USER_ID,
						"The user ID '" + rowData.get(ImportConstants.USER_ID) + "' is incorrect");
			}
		} else {
			report.error(ImportConstants.USER_ID, "User ID cannot be empty");
		}

		// Enumeration validations

		return report;
	}

}
