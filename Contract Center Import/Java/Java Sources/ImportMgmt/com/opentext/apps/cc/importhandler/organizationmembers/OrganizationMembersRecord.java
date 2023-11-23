package com.opentext.apps.cc.importhandler.organizationmembers;

import java.util.Map;
import java.util.Objects;

import com.opentext.apps.cc.custom.Utilities;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;

public class OrganizationMembersRecord implements ImportListener {
    
	int orgMemberNode;
	MetadataInitializer metadata;
	public OrganizationMembersRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
	}

	@Override
	public void doWork(ImportEvent event) {
		createRequest(event.getRow());
	}

	private void createRequest(Map<String, String> row) {
		orgMemberNode = NomUtil.parseXML("<OrganizationMember></OrganizationMember>");
		
		Node.setDataElement(orgMemberNode, ImportConstants.START_DATE, row.get(ImportConstants.START_DATE));
		Node.setDataElement(orgMemberNode, ImportConstants.LEGACY_ID, row.get(ImportConstants.LEGACY_ID));
		Node.setDataElement(orgMemberNode, ImportConstants.JOB_TITLE, row.get(ImportConstants.JOB_TITLE));
		if(!Utilities.isStringEmpty(row.get(ImportConstants.END_DATE))) {
			Node.setDataElement(orgMemberNode, ImportConstants.END_DATE, row.get(ImportConstants.END_DATE));
		}
		
		if(!Utilities.isStringEmpty(row.get(ImportConstants.USER_ID))) {
			
			if(!Objects.isNull(metadata) && !Objects.isNull(metadata.otdsPersonsMap)
					&& !Objects.isNull(metadata.otdsPersonsMap.get(row.get(ImportConstants.USER_ID)))){
				Node.setDataElement(orgMemberNode, "participatingPerson", metadata.otdsPersonsMap.get(row.get(ImportConstants.USER_ID)));   			
			 }
		}		
		
		if(!Utilities.isStringEmpty(row.get(ImportConstants.PRIMARY_ORGANIZATION_CODE))) {
			
			if(!Objects.isNull(metadata) && !Objects.isNull(metadata.getAllOrganizationsIdMap())
					&& !Objects.isNull(metadata.getAllOrganizationsIdMap().get(row.get(ImportConstants.PRIMARY_ORGANIZATION_CODE)))){
				Node.setDataElement(orgMemberNode, "gcOrgId", metadata.getAllOrganizationsIdMap().get(row.get(ImportConstants.PRIMARY_ORGANIZATION_CODE)));   			
			 }
		}
		int specialOrgNodes = NomUtil.parseXML("<AdditionalMembership></AdditionalMembership>");
		String specialOrgCodeExp = row.get(ImportConstants.SPECIAL_ORGANIZATION_CODE);
		if(!Utilities.isStringEmpty(specialOrgCodeExp) && specialOrgCodeExp!=null){
			String[] specialOrgCodes = specialOrgCodeExp.split(";");
			for(String s : specialOrgCodes ) {
					if(!Objects.isNull(metadata) && !Objects.isNull(metadata.getAllOrganizationsIdMap())
					&& !Objects.isNull(metadata.getAllOrganizationsIdMap().get(s))){
						int member = NomUtil.parseXML("<member></member>");
						Node.setDataElement(member, "gcOrgId", metadata.getAllOrganizationsIdMap().get(s));
						Node.setDataElement(member, "participatingPerson", metadata.otdsPersonsMap.get(row.get(ImportConstants.USER_ID)));
						Node.appendToChildren(member, specialOrgNodes);
			    	}
			  }
		  }
		String val=row.get(ImportConstants.ISPRIMARYCONTACT);
		String bool="";
		if(val!=null) {
		if(val.equalsIgnoreCase("YES")) {
			bool="true";
		}else {
			bool="false";
		}}
		else bool="false";
		Node.setDataElement(orgMemberNode, ImportConstants.ISPRIMARYCONTACT, bool);
		
		if(!Utilities.isStringEmpty(row.get(ImportConstants.CONTACT_TYPE))) {
			if(!Objects.isNull(metadata) && !Objects.isNull(metadata.getAllContactTypesIdMap())
					&& !Objects.isNull(metadata.getAllContactTypesIdMap().get(row.get(ImportConstants.CONTACT_TYPE)))){
				Node.setDataElement(orgMemberNode, ImportConstants.CONTACT_TYPE, metadata.getAllContactTypesIdMap().get(row.get(ImportConstants.CONTACT_TYPE)));   			
			 }
		}
		
		Node.appendToChildren(specialOrgNodes, orgMemberNode);
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
		return orgMemberNode;
	}

	public void doWork(OrganizationMembersRecord collectionAccount, Map<String, String> row) {
		// TODO Auto-generated method stub
		
	}
}
