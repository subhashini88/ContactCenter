package com.opentext.apps.cc.importhandler.clause;
import java.util.Map;
import java.util.Objects;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.clause.ImportConstants.ClauseType;;

public class ClauseRecord implements ImportListener
{

	int clauseNode;
	MetadataInitializer metadata;
	public ClauseRecord(MetadataInitializer metadata, ReportItem reportItem) 
	{
		this.metadata = metadata;
	}

	@Override
	public void doWork(ImportEvent event)
	{
		createRequest(event.getRow());
	}

	private void createRequest(Map<String, String> row) 
	{
		clauseNode = NomUtil.parseXML("<Clause></Clause>");

		Node.setDataElement(clauseNode, ImportConstants.CLAUSE_LEGACYID, row.get(ImportConstants.CLAUSE_LEGACYID));
		if(!Utilities.isStringEmpty(row.get(ImportConstants.CLAUSE_CLAUSETYPE))) {
			ClauseType clauseType =  ClauseType.getEnumObject(row.get(ImportConstants.CLAUSE_CLAUSETYPE));
			if(Objects.nonNull(clauseType)) {
				Node.setDataElement(clauseNode, ImportConstants.CLAUSE_CLAUSETYPE, clauseType.getValue());
			}
		}
		Node.setDataElement(clauseNode, ImportConstants.CLAUSE_GLOBALCLAUSE, row.get(ImportConstants.CLAUSE_GLOBALCLAUSE).toLowerCase());
		Node.setDataElement(clauseNode, ImportConstants.CLAUSE_NAME, row.get(ImportConstants.CLAUSE_NAME));
		Node.setDataElement(clauseNode, ImportConstants.CLAUSE_PLAINCONTENT, row.get(ImportConstants.CLAUSE_PLAINCONTENT));
		
		if(!Utilities.isStringEmpty(row.get(ImportConstants.CLAUSE_HTMLCONTENT))) {
			String clauseHTMLContent = "<p>"+ (row.get(ImportConstants.CLAUSE_HTMLCONTENT)) +"</p>";
			Node.setDataElement(clauseNode, ImportConstants.CLAUSE_HTMLCONTENT, clauseHTMLContent);
		}
		else 
		{
			String plainContentAsHtmlContent = "<p>"+row.get(ImportConstants.CLAUSE_PLAINCONTENT)+"</p>";
			Node.setDataElement(clauseNode, ImportConstants.CLAUSE_HTMLCONTENT, plainContentAsHtmlContent);
		}
		
		
		Node.setDataElement(clauseNode, ImportConstants.CLAUSE_DESCRIPTION, row.get(ImportConstants.CLAUSE_DESCRIPTION));
		Node.setDataElement(clauseNode, ImportConstants.CLAUSE_COMMENTS, row.get(ImportConstants.CLAUSE_COMMENTS));
		
		if(!Utilities.isStringEmpty(row.get(ImportConstants.CLAUSE_RELATEDCLAUSECATEGORY_ID))) {
			String relatedClauseCategoryID =  metadata.clauseCategoryMap.get(row.get(ImportConstants.CLAUSE_RELATEDCLAUSECATEGORY_ID));
			Node.setDataElement(clauseNode, ImportConstants.CLAUSE_RELATEDCLAUSECATEGORY_ID, relatedClauseCategoryID);
		}

		if(!Utilities.isStringEmpty(row.get(ImportConstants.CLAUSE_RELATEDTYPE_ID))) {
			String relatedTypeID =  metadata.typeMap.get(row.get(ImportConstants.CLAUSE_RELATEDTYPE_ID));
			Node.setDataElement(clauseNode, ImportConstants.CLAUSE_RELATEDTYPE_ID, relatedTypeID);
		}
		
		Node.setDataElement(clauseNode, ImportConstants.CLAUSE_LIFECYCLE_STATE, row.get(ImportConstants.CLAUSE_LIFECYCLE_STATE));
	}
	@Override
	public void commit() 
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void postCommit() 
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object getSourceId() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getnode() 
	{
		return clauseNode;
	}

	public void doWork(ClauseRecord collectionAccount, Map<String, String> row) 
	{
		// TODO Auto-generated method stub

	}

}

