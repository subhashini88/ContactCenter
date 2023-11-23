package com.opentext.apps.cc.importhandler.clauseCategory;

import java.util.Map;
import java.util.Objects;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.CommonEnums.CreationType;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;
import com.opentext.apps.cc.importhandler.clauseCategory.ImportConstants;
import com.opentext.apps.cc.importhandler.clauseCategory.MetadataInitializer;

public class ClauseCategoryRecord implements ImportListener{
	
	
	int clauseCatTypeRecord;
	MetadataInitializer metadata;
	
	public ClauseCategoryRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
	}
	
	@Override
	public void doWork(ImportEvent event) {
		// TODO Auto-generated method stub
		createRequest(event.getRow());
	}
	
	private void createRequest(Map<String, String> row) {
		clauseCatTypeRecord = NomUtil.parseXML("<ClauseCategory></ClauseCategory>");
		
		Node.setDataElement(clauseCatTypeRecord, ImportConstants.LEGACY_ID, row.get(ImportConstants.LEGACY_ID));
		Node.setDataElement(clauseCatTypeRecord, ImportConstants.CLAUSE_CAT_NAME, row.get(ImportConstants.CLAUSE_CAT_NAME));
		
		if(!Utilities.isStringEmpty(row.get(ImportConstants.CLAUSE_CAT_DESCRIPTION))) {
			Node.setDataElement(clauseCatTypeRecord, ImportConstants.CLAUSE_CAT_DESCRIPTION, row.get(ImportConstants.CLAUSE_CAT_DESCRIPTION));
			}
		
		if (!Utilities.isStringEmpty(row.get(ImportConstants.STATUS))) {
			Status status = Status.getEnumObject(row.get(ImportConstants.STATUS));
			if(status.getValue() == "ACTIVE") {
				Node.setDataElement(clauseCatTypeRecord, ImportConstants.STATUS, "Active");
			}
			else {
				Node.setDataElement(clauseCatTypeRecord, ImportConstants.STATUS, "Inactive");
			}
		}
		CreationType creationType = CreationType.getEnumObject("IMPORTED");
		Node.setDataElement(clauseCatTypeRecord, ImportConstants.CREATION_TYPE, creationType.getValue());
	}

	@Override
	public void commit() {
		// TODO Auto-generated method stub
	}

	@Override
	public void postCommit() {
		// TODO Auto-generated method stub
	}

	@Override
	public Object getSourceId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getnode() {
		// TODO Auto-generated method stub
		return clauseCatTypeRecord;
	}
}
