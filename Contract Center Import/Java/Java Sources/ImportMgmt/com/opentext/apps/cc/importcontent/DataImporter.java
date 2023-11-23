package com.opentext.apps.cc.importcontent;

import java.nio.file.Path;
import java.util.List;
import com.opentext.apps.cc.importcontent.WorkBookManagerImpl;
import com.opentext.apps.cc.importcontent.DataImportHandler;
import com.opentext.apps.cc.importcontent.ImportConfigurationImpl;

public class DataImporter {
	private WorkBookManagerImpl workBookManager;
	private List<String> importObjects;
	public DataImporter(ImportConfigurationImpl importParameter, final Path filePath)
	{
		this.workBookManager = new WorkBookManagerImpl(filePath, true);
		this.importObjects = this.workBookManager.getExcelSheets();
	}

	public void importData()
	{
		for(String importObject :importObjects)
		{
			//Object object = com.opentext.bpm.pcl.commonlibrary.utils.ClassLoader.initiateClass(importHandler);
			Object object = com.opentext.apps.cc.importcontent.ClassLoader.initiateClass("com.opentext.apps.cc.importhandler.contract.ContractImportHandler");
			if(object instanceof DataImportHandler)
			{
				DataImportHandler dataImportHandler = (DataImportHandler)object;
				dataImportHandler.readSheetData(workBookManager.getWorkBook().getSheet(importObject));
			}	
		}
	}
}
