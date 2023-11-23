package com.opentext.apps.cc.importhandler.term;

import java.util.Map;
import java.util.Objects;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.CommonEnums.Status;

public class TermRecord implements ImportListener {
	int termNode;
	MetadataInitializer metadata;

	public TermRecord(MetadataInitializer metadata, ReportItem reportItem) {
		this.metadata = metadata;
	}

	@Override
	public void doWork(ImportEvent event) {
		createRequest(event.getRow());
	}

	private void createRequest(Map<String, String> row) {
		termNode = NomUtil.parseXML("<Term></Term>");
		Node.setDataElement(termNode, ImportConstants.TERM_NAME, row.get(ImportConstants.TERM_NAME));
		if (!Utilities.isStringEmpty(row.get(ImportConstants.STATUS))) {
			Status status = Status.getEnumObject(row.get(ImportConstants.STATUS));
			if (Objects.nonNull(status)) {
				Node.setDataElement(termNode, ImportConstants.STATUS, status.getValue());
			}
		}
		Node.setDataElement(termNode, ImportConstants.TERM_DESCRIPTION, row.get(ImportConstants.TERM_DESCRIPTION));

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
		return termNode;
	}

	public void doWork(TermRecord collectionAccount, Map<String, String> row) {
		// TODO Auto-generated method stub

	}
}
