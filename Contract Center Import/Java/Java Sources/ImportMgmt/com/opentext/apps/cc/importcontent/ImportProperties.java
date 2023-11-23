package com.opentext.apps.cc.importcontent;

import java.util.HashMap;
import java.util.Map;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;

public class ImportProperties {

	private static final CordysLogger LOGGER = CordysLogger.getCordysLogger(ImportProperties.class);	
	public static final Map<String, String> importPropMap = new HashMap<String, String>();//Key-Name of the party, Value-Value of the property 
	private static boolean propertiesLoaded = false;

	/*static {
		ImportProperties.loadProperties();
	}*/

	private static void loadProperties() {
		int response = 0,nodes[] = null;
		try {
			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ImportConfig/operations", "GetImportConfigProperties", null, null);
			response = importRequest.execute();
			nodes = NomUtil.getNodeList(".//ImportConfig", response);
			for (int i : nodes) {
				propertiesLoaded = true;
				String value=null,name=null;
				value= Node.getDataWithDefault(NomUtil.getNode(".//Value", i),null);
				name=Node.getDataWithDefault(NomUtil.getNode(".//Name", i),null);
				if(null != name){
					importPropMap.put(name,value);
				}
			}
		} catch(Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}finally {
			Utilities.cleanAll(response);
		}
	}

	public static String getImportProperty(final String key) {
		if(!propertiesLoaded) {
			ImportProperties.loadProperties();
		}
		String value = null;
		if(importPropMap.containsKey(key)) {
			value = importPropMap.get(key);
		}
		return value;
	}
}
