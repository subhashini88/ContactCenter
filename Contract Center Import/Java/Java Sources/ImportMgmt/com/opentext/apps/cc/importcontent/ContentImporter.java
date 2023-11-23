package com.opentext.apps.cc.importcontent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.eibus.util.logger.CordysLogger;
import com.opentext.apps.cc.importcontent.WorkBookManagerImpl;
import com.opentext.apps.cc.importcontent.ContentManagementRuntimeException;
import com.opentext.apps.cc.importcontent.WorkBookManager;
import com.opentext.apps.cc.importcontent.ExcelReadConfiguration;
import com.opentext.apps.cc.importcontent.ImportConfiguration;
import com.opentext.apps.cc.importcontent.ImportConfigurationImpl;
import com.opentext.apps.cc.importcontent.ContentImporter;
import com.opentext.apps.cc.importcontent.Messages;

public abstract class ContentImporter {
	private static final CordysLogger LOGGER = CordysLogger.getCordysLogger(ContentImporter.class);
	protected Map<Integer, Integer> getAttachments()
	{
		return  Collections.unmodifiableMap(attachments);
	}

	public enum ArtifactType
	{
		EmailTemplate,CheckList,EscalationEvent,ServiceCatalogue,CaseTemplate,EscalationTemplate,RegularTask,TaskLibrary;	
	}
	private ImportConfigurationImpl importConfiguration;
    private Path filePath;
    private Path currentFolderPath;
    private Map<Integer,Integer> attachments;
	public  List<Map<String,String>> readContent(String sheet)
	{		
		List<Map<String,String>> rows = new ArrayList<>(); 
		ExcelReadConfiguration readConfig = importConfiguration.getExcelReadConfiguration();
		readConfig.setSheetName(sheet);
		WorkBookManager manager =(WorkBookManager)importConfiguration.getContentManager();
		rows  = manager.read(readConfig);   
		return rows;
	}
	private void initialize(ImportConfigurationImpl importConfiguration,Path filePath, Map<Integer,Integer> attachments)
    {
		this.importConfiguration = importConfiguration;
		this.filePath =filePath;
		this.attachments = attachments; 
		this.currentFolderPath = filePath.getParent();   
    }
	public void manageImport(ImportConfigurationImpl importConfiguration,Path filePath,Map<Integer,Integer> attachments)
	{
		initialize(importConfiguration,filePath,attachments);
		if(LOGGER.isInfoEnabled())
		{
			LOGGER.info(Messages.STARTED_IMPORTING_ARTIFACT,filePath.getFileName().toString());
		}
		createWorkBookManagerAndInvokeImport();
		if(LOGGER.isInfoEnabled())
		{
			LOGGER.info(Messages.COMPLETED_IMPORTING_ARTIFACT,filePath.getFileName().toString());
		}
	}
	private void createWorkBookManagerAndInvokeImport()
	{
		try(InputStream s =  Files.newInputStream(filePath);)
		{
			WorkBookManagerImpl manager = new WorkBookManagerImpl(filePath,s);
			importConfiguration.setContentManager(manager);
			importContent(importConfiguration);			
		} 
		catch(IOException e)
		{
			throw new ContentManagementRuntimeException(e);
		}
	}
	
	   protected Path getAbsolutePath(Path path)
	   {
		   Path absPath =  currentFolderPath.resolve(path);			   
		   
		   return absPath;
	   }
	   
	protected abstract void importContent(ImportConfiguration importConfiguration); 
	
	protected void generateErrorReport(String jodId, Path path,ImportConfiguration importParameter) {
		//Not all import jobs support error reporting. Only contracts and its related data(ContractLines, RevenueSchedule, ContractBillPeriods) supports error reporting.
		//As this is the super class we are providing empty method as default implementation.
		//Respective handlers needs to provide the implementation.
	} 
}
