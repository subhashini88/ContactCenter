package com.opentext.apps.cc.importhandler.manager;

import java.util.Map;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.manager.MetadataInitializer;

public class Manager implements ImportListener {

	public int managerNode;
	Manager(MetadataInitializer metadata, ReportItem reportItem){
		
	}
	@Override
	public void doWork(ImportEvent event) {
		create(event.getRow());
		
	}

	private void create(Map<String, String> row) {
		managerNode = NomUtil.parseXML("<manager></manager>");
		
		Node.setDataElement(managerNode, "inactive", row.get(ImportConstants.CM));
		Node.setDataElement(managerNode, "replacement", row.get(ImportConstants.CM_REPLACEMENT));
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
		return managerNode;
	}

}
