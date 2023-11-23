package com.opentext.apps.cc.importhandler.rules;

import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;

public class ErrorRecord implements ImportListener {

	@Override
	public void doWork(ImportEvent event) {
		// TODO Auto-generated method stub
		
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
		return 0;
	}
	
	private String type=null;
	private String logs=null;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLogs() {
		return logs;
	}

	public void setLogs(String logs) {
		this.logs = logs;
	}
	
}
