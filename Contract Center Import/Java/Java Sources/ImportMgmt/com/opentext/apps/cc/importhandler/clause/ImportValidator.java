package com.opentext.apps.cc.importhandler.clause;

import java.util.Map;

import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.clause.ImportConstants.ClauseType;

public class ImportValidator {
	public ReportItem validate(Map<String,String> rowData,MetadataInitializer metadata, String jobId)
	{
		ReportItem report = new ReportItem();
		if (rowData == null) return report;

		if(Utilities.isStringEmpty(rowData.get(ImportConstants.CLAUSE_GLOBALCLAUSE))) 
		{
			report.error(ImportConstants.CLAUSE_GLOBALCLAUSE, "Mandatory field '"+ImportConstants.CLAUSE_GLOBALCLAUSE+"' is missing");
		}
		else
		{
			if(rowData.get(ImportConstants.CLAUSE_GLOBALCLAUSE).equalsIgnoreCase("true"))
			{
				if(!Utilities.isStringEmpty(rowData.get(ImportConstants.CLAUSE_RELATEDTYPE_ID))) 
				{
					report.error(ImportConstants.CLAUSE_GLOBALCLAUSE, "If Global Clause is true there should be no Contract type associated with the clause");
				}
			}
			else
			{
				if(Utilities.isStringEmpty(rowData.get(ImportConstants.CLAUSE_RELATEDTYPE_ID)))
				{
					report.error(ImportConstants.CLAUSE_RELATEDTYPE_ID, "Mandatory field '"+ImportConstants.CLAUSE_RELATEDTYPE_ID+"' is missing");
				}
				else
				{
					String relatedTypeID = metadata.typeMap.get(rowData.get(ImportConstants.CLAUSE_RELATEDTYPE_ID));
					if(Utilities.isStringEmpty(relatedTypeID))
					{
						report.error(ImportConstants.CLAUSE_RELATEDTYPE_ID, "Contract type with the Name: '"+rowData.get(ImportConstants.CLAUSE_RELATEDTYPE_ID)+"' doesn't exist in the system");
					}
				}
			}
		}

		if(Utilities.isStringEmpty(rowData.get(ImportConstants.CLAUSE_RELATEDCLAUSECATEGORY_ID)))
		{
			report.error(ImportConstants.CLAUSE_RELATEDCLAUSECATEGORY_ID, "Mandatory field '"+ImportConstants.CLAUSE_RELATEDCLAUSECATEGORY_ID+"' is missing");
		}
		else
		{
			String relatedClauseCategoryID = metadata.clauseCategoryMap.get(rowData.get(ImportConstants.CLAUSE_RELATEDCLAUSECATEGORY_ID));
			if(Utilities.isStringEmpty(relatedClauseCategoryID))
			{
				report.error(ImportConstants.CLAUSE_RELATEDCLAUSECATEGORY_ID, "Clause category with the Name: '"+rowData.get(ImportConstants.CLAUSE_RELATEDCLAUSECATEGORY_ID)+"' doesn't exist in the system");
			}
		}


		if(Utilities.isStringEmpty(rowData.get(ImportConstants.CLAUSE_NAME))) 
		{
			report.error(ImportConstants.CLAUSE_NAME, "Mandatory field '"+ImportConstants.CLAUSE_NAME+"' is missing");
		}

		if(Utilities.isStringEmpty(rowData.get(ImportConstants.CLAUSE_LIFECYCLE_STATE))) 
		{
			report.error(ImportConstants.CLAUSE_LIFECYCLE_STATE, "Mandatory field '"+ImportConstants.CLAUSE_LIFECYCLE_STATE+"' is missing");
		}
		
		// Enumeration validations
		
		// Clause type.
		if (!Utilities.isStringEmpty(rowData.get(ImportConstants.CLAUSE_CLAUSETYPE))) {
			String value = rowData.get(ImportConstants.CLAUSE_CLAUSETYPE);
			if (!ClauseType.contains(value)) {
				report.error(ImportConstants.CLAUSE_CLAUSETYPE, rowData.get(ImportConstants.CLAUSE_CLAUSETYPE) + "' is not valid");
			}
		}

		return report;
	}
}
