package com.opentext.apps.cc.importcontent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.eibus.util.logger.CordysLogger;
import com.opentext.apps.cc.custom.Utilities;

public class WorkBookManagerImpl implements WorkBookManager{
	private Path filePath;
	private Workbook workbook;
	private CellStyle headerStyle;
	private CellStyle disabledColumnStyle;
	private CellStyle normalColumnStyle;
	private static final CordysLogger LOGGER = CordysLogger.getCordysLogger(WorkBookManagerImpl.class);	

	public WorkBookManagerImpl(final Path filePath)
	{
		this.filePath = filePath;
		this.workbook = createWorkBook();
		initializeStyle();
	}

	public WorkBookManagerImpl(final Path filePath,InputStream s)
	{
		this.filePath = filePath;
		this.workbook = createWorkBook(s);
		initializeStyle();
	}

	public WorkBookManagerImpl(final Path filePath, boolean openBook)
	{
		this.filePath = filePath;
		this.workbook = openWorkBook(filePath.toString());
	}

	private void initializeStyle()
	{
		createHeaderStyle();
		createReferenceStyle();
		createNormalColumnStyle();	
	}
	//Creates a Workbook
	private Workbook createWorkBook()
	{
		Workbook workbook;
		if (filePath.toString().endsWith("xlsx"))
		{
			workbook = new XSSFWorkbook();
		}
		else
			if (filePath.toString().endsWith("xls"))
			{
				workbook = new HSSFWorkbook();
			}
			else
			{
				ContentManagementRuntimeException exception = new ContentManagementRuntimeException(Messages.INVALID_FILE);
				LOGGER.error(exception, Messages.INVALID_FILE);
				throw exception;
			}
		return workbook;
	}

	private Workbook createWorkBook(InputStream s)
	{
		Workbook workbook;
		try
		{
			if (filePath.toString().endsWith("xlsx"))
			{
				workbook = new XSSFWorkbook(s);
				//workbook = new SXSSFWorkbook(s);
			}
			else
				if (filePath.toString().endsWith("xls"))
				{
					workbook = new HSSFWorkbook(s);
				}
				else
				{
					ContentManagementRuntimeException exception = new ContentManagementRuntimeException(Messages.INVALID_FILE);
					LOGGER.error(exception, Messages.INVALID_FILE);
					throw exception;
				}
		}
		catch(IOException e)
		{
			ContentManagementRuntimeException exception = new ContentManagementRuntimeException(e,Messages.INVALID_FILE);
			LOGGER.error(exception, Messages.INVALID_FILE);
			throw exception;
		}
		return workbook;
	}

	//Opens an existing workbook
	private Workbook openWorkBook(String fileName)
	{
		Workbook workbook = null;
		try(FileInputStream fis = new FileInputStream(fileName);)
		{	    	
			//Create Workbook instance for xlsx/xls file input stream
			if(fileName.toLowerCase(Locale.US).endsWith("xlsx"))
			{
				workbook = new XSSFWorkbook(fis);
			}
			else if(fileName.toLowerCase(Locale.US).endsWith("xls"))
			{
				workbook = new HSSFWorkbook(fis);
			}
			else
			{
				ContentManagementRuntimeException exception = new ContentManagementRuntimeException(Messages.INVALID_FILE);
				LOGGER.error(exception, Messages.INVALID_FILE);
				throw exception;
			}        

		}
		catch(IOException ex)
		{
			ContentManagementRuntimeException exception = new ContentManagementRuntimeException(ex,Messages.EXCEPTION_OPENING_WORKBOOK);
			LOGGER.error(exception, Messages.EXCEPTION_OPENING_WORKBOOK);
			throw exception;
		}
		return workbook;
	}

	//Creates a sheet in the specified workbook
	public Sheet createSheet(final String sheetName)
	{
		Sheet sheet = null;
		try
		{
			sheet = workbook.createSheet(sheetName);
		}
		catch(IllegalArgumentException e)
		{
			ContentManagementRuntimeException exception = new ContentManagementRuntimeException(e,Messages.EXCEPTION_CREATING_SHEET,sheetName);
			LOGGER.error(exception, Messages.EXCEPTION_CREATING_SHEET,sheetName);
			throw exception;
		}
		return sheet;
	}

	public String getFileName()
	{
		return filePath.toString();
	}

	public Path getFilePAth()
	{
		return filePath;
	}

	@Override
	public Workbook getWorkBook()
	{
		return workbook;
	}

	//Creates a header CellStyle
	private void createHeaderStyle()
	{
		headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
		//headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);        //CellStyle.SOLID_FOREGROUND);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);//  CellStyle.ALIGN_CENTER);
		Font hSSFFont = workbook.createFont();
		hSSFFont.setFontName(HSSFFont.FONT_ARIAL);
		hSSFFont.setColor(IndexedColors.WHITE.getIndex());
		hSSFFont.setFontHeightInPoints((short) 10);
		hSSFFont.setBold(true);//HSSFFont.BOLDWEIGHT_BOLD);
		headerStyle.setFont(hSSFFont);
	}

	public CellStyle getHeaderStyle()
	{
		return headerStyle;
	}


	public void setHeaderStyle(CellStyle headerStyle)
	{
		this.headerStyle = headerStyle;
	}


	public CellStyle getDisabledColumnStyle()
	{
		return disabledColumnStyle;
	}


	public void setDisabledColumnStyle(CellStyle disabledColumnStyle)
	{
		this.disabledColumnStyle = disabledColumnStyle;
	}


	public CellStyle getNormalColumnStyle()
	{
		return normalColumnStyle;
	}


	public void setNormalColumnStyle(CellStyle normalColumnStyle)
	{
		this.normalColumnStyle = normalColumnStyle;
	}


	//Creates a reference CellStyle
	private void createReferenceStyle()
	{
		disabledColumnStyle = workbook.createCellStyle();
		disabledColumnStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
		disabledColumnStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	}

	/**
	 * This method will return style for Normal Cell
	 * @return
	 */

	private void createNormalColumnStyle()
	{
		normalColumnStyle = workbook.createCellStyle();
		normalColumnStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		normalColumnStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		normalColumnStyle.setBorderBottom(BorderStyle.THIN);
		normalColumnStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		normalColumnStyle.setBorderLeft(BorderStyle.THIN);
		normalColumnStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		normalColumnStyle.setBorderRight(BorderStyle.THIN);
		normalColumnStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
		normalColumnStyle.setBorderTop(BorderStyle.THIN);
		normalColumnStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
		normalColumnStyle.setAlignment(HorizontalAlignment.CENTER);
	}


	@Override
	public List<Map<String,String>> read(ReadConfiguration readConfiguration)
	{
		List<Map<String,String>> records = new ArrayList<>();
		Sheet sheet = workbook.getSheet(((ExcelReadConfigurationImpl)readConfiguration).getSheetName());

		if(sheet != null)
		{
			Iterator<Row> rowIterator = sheet.iterator();
			List<String> columnHeaders = new ArrayList<>();
			boolean isHeader = true;
			while (rowIterator.hasNext()) 
			{     
				//Get the row object
				Row row = rowIterator.next();
				if(isHeader)
				{
					for(Cell cell : row) //Reads only the defined cells in the row and iterates over them.
					{
						String headerValue = getCellValue(cell);
						if(!Utilities.isStringEmpty(headerValue))
						{
							columnHeaders.add(getCellValue(cell));
						}
					}
					isHeader = false;
				}
				else
				{
					Map<String,String> rowData = new HashMap<>();
					int emptyCellCount = 0;
					for (int i=0; i<columnHeaders.size(); i++)
					{

						Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
						String value = (cell != null) ? getCellValue(cell) : null;  			
						if(Utilities.isStringEmpty(value))
						{
							emptyCellCount++;
						}        					
						rowData.put(columnHeaders.get(i), value); 
					}
					if(emptyCellCount < columnHeaders.size())//If all cells are empty dont add the row
					{
						records.add(rowData);   
					}
				}
			}

		}
		return records;
	}

	private String getCellValue(Cell cell)
	{
		cell.setCellType(CellType.STRING);
		return String.valueOf(cell.getStringCellValue());
		//return String.valueOf(cell.getDateCellValue());
		/*switch(cell.getCellType())
  		 {
		     case BOOLEAN:
	        	 	return String.valueOf(cell.getBooleanCellValue());
	         case NUMERIC:
	        	 	return String.valueOf(cell.getNumericCellValue());
	         case STRING:
	        	 	return String.valueOf(cell.getStringCellValue());
	        case BLANK:
	        		return "";
	         default:
	        	 	return "";
  		 }*/
	}


	public void persist()
	{		
		FileUtil.writeWorkBookToFile(filePath, workbook);
	}

	public List<String> getExcelSheets() {
		List<String> sheetNames = new ArrayList<>();
		for (int i=0; i<this.workbook.getNumberOfSheets(); i++) 
		{
			sheetNames.add( this.workbook.getSheetName(i) );
		}
		return sheetNames;
	}

	public Map<String, List<Map<String, String>>> readAsListMap(final String sheetName, final String mapKey) {
		Map<String,List<Map<String, String>>> rows = new HashMap<String,List<Map<String, String>>>();
		Sheet sheet = workbook.getSheet(sheetName);

		if (sheet != null) {
			Iterator<Row> rowIterator = sheet.iterator();
			List<String> columnHeaders = new ArrayList<>();
			boolean isHeader = true;
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				if (isHeader) {// 1st row is header
					for (Cell cell : row) { // Reads only the defined cells in the row and iterates over them.
						String headerValue = getCellValue(cell);
						if (!Utilities.isStringEmpty(headerValue)) {
							columnHeaders.add(getCellValue(cell));
						}
					}
					isHeader = false;
				} else {
					Map<String, String> rowData = new HashMap<String, String>(); 
					int emptyCellCount = 0;
					for (int i = 0; i < columnHeaders.size(); i++) {
						Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
						String value = (cell != null) ? getCellValue(cell) : null;
						if (Utilities.isStringEmpty(value)) {
							emptyCellCount++;
						}
						rowData.put(columnHeaders.get(i), value);
					}
					if (emptyCellCount < columnHeaders.size()) {// If all cells are empty dont add the row
						String documentReferenceId = rowData.get(mapKey);
						if(documentReferenceId != null) {
							List<Map<String, String>> list = rows.get(documentReferenceId);
							if(list == null) {
								list = new ArrayList<Map<String, String>>();
								list.add(rowData);
								rows.put(documentReferenceId, list);
							} else {
								list.add(rowData);
							}
						} else {
							//TODO - Log error
						}
					}
				}
			}
		}
		return rows;
	}

	public Map<String, Map<String, String>> readAsMap(final String sheetName, final String mapKey) {
		Map<String,Map<String, String>> rows = new LinkedHashMap<String,Map<String, String>>();
		Sheet sheet = workbook.getSheet(sheetName);

		if (sheet != null) {
			Iterator<Row> rowIterator = sheet.iterator();
			List<String> columnHeaders = new ArrayList<>();
			boolean isHeader = true;
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				if (isHeader) {// 1st row is header
					for (Cell cell : row) { // Reads only the defined cells in the row and iterates over them.
						String headerValue = getCellValue(cell);
						if (!Utilities.isStringEmpty(headerValue)) {
							columnHeaders.add(getCellValue(cell));
						}
					}
					isHeader = false;
				} else {
					Map<String, String> rowData = new LinkedHashMap<String, String>(); 
					int emptyCellCount = 0;
					for (int i = 0; i < columnHeaders.size(); i++) {
						Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
						String value = (cell != null) ? getCellValue(cell) : null;
						if (Utilities.isStringEmpty(value)) {
							emptyCellCount++;
						}
						rowData.put(columnHeaders.get(i), value);
					}
					if (emptyCellCount < columnHeaders.size() && rowData.get(mapKey) != null) {// If all cells are empty dont add the row
						rows.put(rowData.get(mapKey), rowData);	
					}
				}
			}
		}
		return rows;
	}
	
	public List<List<Map<String, String>>> readAsMapSet(final String sheetName, final String mapKey, String recordName, String subRecordName) {
		List<List<Map<String, String>>> rows = new ArrayList<>();
		Sheet sheet = workbook.getSheet(sheetName);
		
		if (sheet != null) {
			Iterator<Row> rowIterator = sheet.iterator();
			List<String> columnHeaders = new ArrayList<>();
			List<Map<String, String>> arraySet = new ArrayList<>();
			boolean isHeader = true;
			boolean newtasklist = true;
			String recordType = null;
			Row row;
			while (rowIterator.hasNext()) {
				row = rowIterator.next();
				if (isHeader) {// 1st row is header
					for (Cell cell : row) { // Reads only the defined cells in the row and iterates over them.
						String headerValue = getCellValue(cell);
						if (!Utilities.isStringEmpty(headerValue)) {
							columnHeaders.add(getCellValue(cell));
						}
					}
					isHeader = false;
				} else {
					Map<String, String> rowData = new LinkedHashMap<String, String>();
					
					int emptyCellCount = 0;
					for (int i = 0; i < columnHeaders.size(); i++) {
						Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
						String value = (cell != null) ? getCellValue(cell) : null;
						if (Utilities.isStringEmpty(value)) {
							emptyCellCount++;
						}
						rowData.put(columnHeaders.get(i), value);
					}
					recordType = rowData.get("Type");
					if(emptyCellCount < columnHeaders.size() ) {
						if ((!Utilities.isStringEmpty(rowData.get(mapKey)) && (recordType == null || !recordType.equalsIgnoreCase(subRecordName))) || (!Utilities.isStringEmpty(recordType) && recordType.equalsIgnoreCase(recordName))) {
							if (newtasklist == false) {
								rows.add(arraySet);
								arraySet = new ArrayList<>();							
							}
							newtasklist = false;
						}
						if (newtasklist == false)
							arraySet.add(rowData);
					}
				}
			}
			if (!rowIterator.hasNext()) {
				rows.add(arraySet);
			}
		}
		return rows;
	}

	@Override
	public void write(WriteConfiguration writeConfiguration) {
		// TODO Auto-generated method stub

	}

	public CellStyle createHeaderStyle(Workbook wb) {
		CellStyle style = null;
		;
		Font headerFont = wb.createFont();
		headerFont.setBold(true);//setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerFont.setFontHeightInPoints((short) 10);
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setFont(headerFont);
		return style;
	}
	public void createRow(Sheet sheet, Collection<String> data, CellStyle style, int rowIndex) {
		// ,int columnIndex) {
		Row row = sheet.createRow(rowIndex);
		int columnIndex = 0;
		for (String column : data) {
			Cell cell = row.createCell(columnIndex++);
			cell.setCellValue(column);
			// sheet.autoSizeColumn(columnIndex);
			if (style != null) {
				cell.setCellStyle(style);
			}
		}
	}

	public void createFile(Workbook wb, String workbookName) throws IOException {
		File file = new File(workbookName);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream exportStream = new FileOutputStream(file);
		wb.write(exportStream);
		exportStream.close();
	}
}
