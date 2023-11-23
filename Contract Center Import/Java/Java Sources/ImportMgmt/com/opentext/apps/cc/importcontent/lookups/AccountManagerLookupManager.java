package com.opentext.apps.cc.importcontent.lookups;

import java.util.Map;
import java.util.WeakHashMap;

import com.opentext.apps.cc.custom.Utilities;

public class AccountManagerLookupManager implements ILookupManager{

	private static final Map<String, String> accountManagerMap = new WeakHashMap<String, String>();//Key-Name of the party, Value-ItemId of the 
	private static final Map<String, String> accountManagerMapByManagerId = new WeakHashMap<String, String>();//Key-Name of the party, Value-ItemId of the 
	
	@Override
	public String lookupEntity(Map<String, String> rowData,String... data) {
		String itemId = null;
		if(Utilities.isArrayEmpty(data) || Utilities.isStringEmpty(data[0])) {
			return null;//TODO- What to do here ?
		} if( (itemId = accountManagerMap.get(data[0])) == null ) {
			itemId = findEntityData("http://schemas.cordys.com/default", "GetAccountManagerByName", data[0], ".//ItemId");
	    	accountManagerMap.put(data[0], itemId);
		}
		return itemId;
	}
	
	public String lookupEntityByManagerId(Map<String, String> rowData,String... data) {
		String itemId = null;
		if(Utilities.isArrayEmpty(data) || Utilities.isStringEmpty(data[0])) {
			return null;//TODO- What to do here ?
		} if( (itemId = accountManagerMapByManagerId.get(data[0])) == null ) {
			itemId = findEntityData("http://schemas.cordys.com/default", "GetAccountManagerByManagerId", data[0], ".//ItemId");
			accountManagerMapByManagerId.put(data[0], itemId);
		}
		return itemId;
	}
}
