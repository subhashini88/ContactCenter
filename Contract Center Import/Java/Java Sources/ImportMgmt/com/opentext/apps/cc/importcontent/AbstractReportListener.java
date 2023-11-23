package com.opentext.apps.cc.importcontent;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.opentext.apps.cc.importcontent.ContractManagementMessages;
import com.opentext.apps.cc.importcontent.ContractMgmtRuntimeException;

public abstract class AbstractReportListener implements ReportListener {
	private boolean headerNotPrepared = true;

	@Override
	public void prepareReport(ReportEvent event) {
		try
		{	
			if (headerNotPrepared) {
				prepareHeader(event);
			}

			prepareContent(event);

		}
		catch (Exception e)
		{
			throw new ContractMgmtRuntimeException(e, ContractManagementMessages.ERROR_READING_PROPERTIES_FILE);
		}
	}

	private void prepareHeader(ReportEvent event) throws IOException {		
		Writer out = null;
		try {
			out = event.getWriter();
			out.write(getImportTypeColumnName());
			out.append(',');
			out.write(getStatusColumnName());
			out.append(',');
			if (null != getImportedRecordIdColumnName()) {
				out.write(getImportedRecordIdColumnName());
				out.append(',');
			}
			out.write(getReportColumnName() +"\n");
			headerNotPrepared = false;
		} catch (Exception e) {
			throw new ContractMgmtRuntimeException(e, ContractManagementMessages.ERROR_READING_PROPERTIES_FILE);
		}
		finally {
			out.close();
		}
	}

	private void prepareContent(ReportEvent event) throws IOException {
		Writer out = null;
		try
		{
			out = event.getWriter();
			for (int count = 0; count < event.getSheet().size(); count++)
			{
				ReportItem report = event.getSummary().get(count);
				Map<String, String> row = event.getSheet().get(count);
				if (row.get(getImportEntryName()) == null) {
					out.write(" ");
				}
				else {					
					out.write(row.get(getImportEntryName()));
				}
				out.append(',');
				out.write((report.isValid() ? "SUCCESS" : "FAILURE"));
				out.append(',');
				if (null != getImportedRecordIdColumnName()) {
					out.write(report.getSourceId().toString());
					out.append(',');
				}

				out.write( report+"\n");
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		finally {
			out.close();
		}
	}

	protected abstract String getImportedRecordIdColumnName();

	protected abstract String getImportTypeColumnName();

	protected abstract String getStatusColumnName();

	protected abstract String getReportColumnName();

	protected abstract String getImportEntryName();
}
