package com.opentext.apps.cc.importcontent;

import java.util.List;
import java.util.Map;

import com.opentext.apps.cc.importcontent.ApplicationContext;
import com.opentext.apps.cc.importcontent.ExcelReadConfiguration;

public interface ImportConfiguration {
	
	String getZipFileName();

	void setZipFileName(String fileName);

	String getJobId();
	
	void setJobId(String jobId);

	boolean isRetrigger();
	
	void setIsRetrigger(boolean isRetrigger);

	ExcelReadConfiguration getExcelReadConfiguration();

	Manager getContentManager();

	ApplicationContext getApplicationContext();

	void setTransactionID(String transactionID);

	String getTransactionId();

	void setMappedInstances(Map<String, String> mappedInstances);
	
	void setMatchContracts(boolean matchContracts);
	
	boolean isMatchContracts();
	
	public Map<String, List<Map<String, String>>> getContractLines();
	
	public Map<String, List<Map<String, String>>> getBillerPeriods();
	
	public Map<String, String> getContractActions();

	void setOrgName(String orgName);
	
	String getOrgName();

	void setIsReimportMetadata(boolean isReimportMetadata);

	boolean isReimportMetadata();
}
