package com.opentext.apps.cc.importcontent;

import java.io.IOException;
import java.util.Collection;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


public interface WorkBookManager extends Manager{
	/**
	 * This method helps in getting the workbook
	 * to work with, while export/import of data
	 * using excel 
	 * @return the workbook 
	 */	
	public Workbook getWorkBook();
	
	public Sheet createSheet(final String sheetName);

	public CellStyle createHeaderStyle(Workbook wb);
	
	public void createRow(Sheet sheet, Collection<String> data, CellStyle style, int rowIndex);
	
    public void createFile(Workbook wb, String workbookName) throws IOException;

}
