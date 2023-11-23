package com.opentext.apps.cc.importhandler.relatedCostCenter;

import java.util.Map;
import java.util.Objects;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;

public class RelatedCostCenterRecord implements ImportListener {
	int costcenterNode;
	MetadataInitializer metadata;
	private String legacyId = null;

	public RelatedCostCenterRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
	}

	@Override
	public void doWork(ImportEvent event) {
		createRequest(event.getRow());
	}

	private void createRequest(Map<String, String> row) {
		costcenterNode = NomUtil.parseXML("<RelatedCostCenter></RelatedCostCenter>");
		this.legacyId=row.get(ImportConstants.LEGACY_ID);
		if (!Utilities.isStringEmpty(row.get(ImportConstants.STATUS))) {
			Status status = Status.getEnumObject(row.get(ImportConstants.STATUS));
			if (Objects.nonNull(status)) {
				Node.setDataElement(costcenterNode, ImportConstants.STATUS, status.getValue());
			}
		}
		Node.setDataElement(costcenterNode, ImportConstants.LEGACY_ID, row.get(ImportConstants.LEGACY_ID));
		Node.setDataElement(costcenterNode,"RelatedCostCenterId", row.get(ImportConstants.RELATEDCOSTCENTER_ID));
		Node.setDataElement(costcenterNode,"ContractNumber", row.get(ImportConstants.CONTRACTNUMBER));
		Node.setDataElement(costcenterNode, "ContractItemId",metadata.contractNumberMap.get(row.get(ImportConstants.CONTRACTNUMBER)));
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
		return costcenterNode;
	}

	public void doWork(RelatedCostCenterRecord collectionAccount, Map<String, String> row) {
		// TODO Auto-generated method stub

	}

	public String getlegacyId() {
		// TODO Auto-generated method stub
		return legacyId;
	}
}
