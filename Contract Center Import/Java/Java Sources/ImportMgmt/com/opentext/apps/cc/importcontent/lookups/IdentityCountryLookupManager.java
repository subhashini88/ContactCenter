package com.opentext.apps.cc.importcontent.lookups;

import java.util.Map;
import java.util.WeakHashMap;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;

public class IdentityCountryLookupManager implements ILookupManager{

	Map<String, String> countryMap = new WeakHashMap<String, String>();//Key-Name of the party, Value-ItemId of the
	private static final CordysLogger logger = CordysLogger.getCordysLogger(IdentityCountryLookupManager.class);
	
	@Override
	public String lookupEntity(Map<String, String> rowData, String... data) {
		String itemId = null;
		try {
			if(Utilities.isArrayEmpty(data) || Utilities.isStringEmpty(data[0])) {
				logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.INFO, null, "Input shouldn't be null/empty");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.INPUT_SHOULD_NOT_BE_NULL);
			} if( (itemId = countryMap.get(data[0])) == null ) {
				itemId = findEntityData("http://schemas.opentext.com/apps/contractcenterimport/16.3", "GetCountryFromIdentity", data[0], ".//ItemId");
				countryMap.put(data[0], itemId);
			}
		} catch (Exception e) {
			logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.ERROR, e, "");
		}
		return itemId;
	}
}
