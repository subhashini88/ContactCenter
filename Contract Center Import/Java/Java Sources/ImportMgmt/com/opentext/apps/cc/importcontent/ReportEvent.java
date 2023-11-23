package com.opentext.apps.cc.importcontent;

import java.io.Writer;
import java.util.List;
import java.util.Map;

public class ReportEvent {

	private final List<Map<String, String>> sheet;
	private final List<ReportItem> summary;
	private final ImportConfiguration importConfigration;
	private final Writer writer;

	public ReportEvent(List<Map<String, String>> sheet, List<ReportItem> summary, ImportConfiguration configuration,Writer writer) {
		this.sheet = sheet;
		this.summary = summary;
		this.importConfigration=configuration;
		this.writer=writer;
	}

	public List<Map<String, String>> getSheet() {
		return sheet;
	}

	public List<ReportItem> getSummary() {
		return summary;
	}

	public ImportConfiguration getImportConfigration() {
		return importConfigration;
	}
	
	public Writer getWriter()
	{
		return this.writer;
	}
}
