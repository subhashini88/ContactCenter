package com.opentext.apps.cc.importcontent;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opentext.apps.cc.importcontent.ApplicationContext;
import com.opentext.apps.cc.importcontent.ApplicationContextImpl;
import com.opentext.apps.cc.importcontent.ExcelReadConfigurationImpl;
import com.opentext.apps.cc.importcontent.Manager;


public class ImportConfigurationImpl implements ImportConfiguration {
	
	private ExcelReadConfigurationImpl readParamImpl;
	private Manager cManager;
	private String zipFileName = null;
	private String jobId = null;
	private String orgName = null;
	private boolean isRetrigger = false;
	private ApplicationContextImpl applicationContext;
	private String currentArtifact;
	private String transactionID;
	private boolean matchContracts;
	private boolean isReimportMetadata = false;
	private Map<String, List<Map<String, String>>> contractLines;
	private Map<String, List<Map<String, String>>> billerPeriods;
	private Map<String, String> contractActions = new HashMap<String, String>();
	
	public String getCurrentArtifact()
	{
		return currentArtifact;
	}
	public ImportConfigurationImpl(String fileName){
		this.zipFileName = fileName;
		this.readParamImpl = new ExcelReadConfigurationImpl();
	}

	public void setArtifact(String currentArtifact)
	{
		this.currentArtifact = currentArtifact;
	}

   	
   	public ImportConfigurationImpl()
   	{
   	}
   	
  	public void setApplicationContext(ApplicationContextImpl context) 
  	{
  		this.applicationContext = context;
  	}
  	
    public void setZipFileName (String zipFileName )
  	{
  		this.zipFileName = zipFileName;
  	}  	
  	
	@Override
	public ApplicationContext getApplicationContext()
	{
		return applicationContext;
	}
   	
	@Override
	public String getZipFileName() {
	
		return zipFileName;
	} 		
		
	@Override
	public ExcelReadConfiguration getExcelReadConfiguration() 
	{
		return readParamImpl;
	}

	@Override
	public Manager getContentManager() 
	{
		return cManager;
	}
	
	public void setContentManager(Manager cmanager) 
	{
		this.cManager = cmanager;
	}

	@Override
	public void setTransactionID(String transactionID)
	{
		this.transactionID = transactionID;		
	}


	@Override
	public String getTransactionId() 
	{		
		return transactionID;
	}


	@Override
	public void setMappedInstances(Map<String, String> mappedInstances) {
		// TODO Auto-generated method stub
		
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public boolean isRetrigger() {
		return isRetrigger;
	}
	public void setIsRetrigger(boolean isRetrigger) {
		this.isRetrigger = isRetrigger;
	}
	@Override
	public void setMatchContracts(boolean matchContracts) {
		this.matchContracts = matchContracts;
	}
	@Override
	public void setIsReimportMetadata(boolean isReimportMetadata) {
		this.isReimportMetadata = isReimportMetadata;
	}
	@Override
	public boolean isReimportMetadata() {
		return isReimportMetadata;
	}
	@Override
	public boolean isMatchContracts() {
		return matchContracts;
	}
	
	public Map<String, List<Map<String, String>>> getContractLines() {
		return contractLines;
	}
	
	public void setContractLines(Map<String, List<Map<String, String>>> contractLines) {
		this.contractLines = contractLines;
	}
	
	public Map<String, List<Map<String, String>>> getBillerPeriods() {
		return billerPeriods;
	}
	public void setBillerPeriods(Map<String, List<Map<String, String>>> billerPeriods) {
		this.billerPeriods = billerPeriods;
	}

	public Map<String, String> getContractActions() {
		return contractActions;
	}
	@Override
	public void setOrgName(String orgName) {
		this.orgName=orgName;
	}
	
	@Override
	public String getOrgName() {
		// TODO Auto-generated method stub
		return orgName;
	}
}
