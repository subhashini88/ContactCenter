package com.opentext.apps.cc.importhandler.organizations;

import java.util.Map;
import java.util.Objects;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;

public class OrganizationRecord implements ImportListener {
	int organizationNode;
	MetadataInitializer metadata;
	private String orgCode = null;
	private String parentOrgCode = null;
	private String legacyId = null;

	public OrganizationRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
	}

	@Override
	public void doWork(ImportEvent event) {
		createRequest(event.getRow());
	}

	private void createRequest(Map<String, String> row) {
		organizationNode = NomUtil.parseXML("<Organization></Organization>");
		Node.setDataElement(organizationNode, ImportConstants.ORGANIZATION_NAME, row.get(ImportConstants.ORGANIZATION_NAME));
		Node.setDataElement(organizationNode, ImportConstants.ORGANIZATION_CODE, row.get(ImportConstants.ORGANIZATION_CODE));
		this.orgCode = row.get(ImportConstants.ORGANIZATION_CODE);
		this.parentOrgCode = row.get(ImportConstants.PARENT_ORGANIZATION_CODE);
		this.legacyId=row.get(ImportConstants.LEGACY_ID);
		Node.setDataElement(organizationNode, ImportConstants.ORGANIZATION_DESCRIPTION, row.get(ImportConstants.ORGANIZATION_DESCRIPTION));
		Node.setDataElement(organizationNode, ImportConstants.PARENT_ORGANIZATION_CODE, row.get(ImportConstants.PARENT_ORGANIZATION_CODE));
		if (!Utilities.isStringEmpty(row.get(ImportConstants.STATUS))) {
			Status status = Status.getEnumObject(row.get(ImportConstants.STATUS));
			if (Objects.nonNull(status)) {
				Node.setDataElement(organizationNode, ImportConstants.STATUS, status.getValue());
			}
		}
		Node.setDataElement(organizationNode, ImportConstants.LEGACY_ID, row.get(ImportConstants.LEGACY_ID));
		Node.setDataElement(organizationNode, ImportConstants.PARENT_ORG_ID,metadata.orgMap.get(this.parentOrgCode));
		Node.setDataElement(organizationNode, ImportConstants.COSTCENTER_ID, row.get(ImportConstants.COSTCENTER_ID));
		Node.setDataElement(organizationNode, ImportConstants.BUSINESSWORKSPACE_TEMPLATE_ID, row.get(ImportConstants.BUSINESSWORKSPACE_TEMPLATE_ID));

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
		return organizationNode;
	}

	public void doWork(OrganizationRecord collectionAccount, Map<String, String> row) {
		// TODO Auto-generated method stub

	}

	public String getOrgCode() {
		return orgCode;
	}

	public String getParentOrgCode() {
		return parentOrgCode;
	}

	public String getlegacyId() {
		// TODO Auto-generated method stub
		return legacyId;
	}
}
