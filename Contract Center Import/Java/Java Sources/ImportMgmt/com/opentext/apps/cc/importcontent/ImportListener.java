package com.opentext.apps.cc.importcontent;


public interface ImportListener {
	void doWork(ImportEvent event);
	void commit();
	void postCommit();
	public Object getSourceId();
	public int getnode();
}
