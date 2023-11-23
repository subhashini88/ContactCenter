package com.opentext.apps.cc.importcontent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.CellType;

import com.eibus.util.logger.CordysLogger;


public abstract class DataImportHandler {
	//public abstract void write(ImportConfiguration importConfig, ImportContext context, List<Map<String, String>> sheetData);
	protected static final CordysLogger LOGGER = CordysLogger.getCordysLogger(DataImportHandler.class);

	private List<String> columns = new ArrayList<>();

	public List<Map<String, String>> readSheetData(Sheet sheet)
	{
		//get the header row number. There can be comments in the sheet at the top of the table.
		int headerRowNumber = getHeaderRowNumber(sheet);
		List<Map<String, String>> sheetData = new ArrayList<>();
		//every sheet has rows, iterate over them
		Iterator<Row> rowIterator = sheet.iterator();
		int rowNumber =0;
		Row row;
		while (rowIterator.hasNext()) 
		{     
			//Get the row object
			row = rowIterator.next();
			rowNumber = row.getRowNum();
			if(rowNumber < headerRowNumber)
			{
				continue;
			}
			if(row.getRowNum() == headerRowNumber)
			{
				//Fetch the header row which gives the column names.
				prepareHeaders(row);
			}
			else
			{
				sheetData.add(fetchRowDetails(row));
			}
		}
		return sheetData;
	}

	private int getHeaderRowNumber(Sheet sheet) 
	{
		int rows =0, tmp=0, cols=0, headerRowNumber = 0;
		rows = sheet.getPhysicalNumberOfRows();
		for(int i = 0; i < 10 || i < rows ; i++) 
		{
			Row row = sheet.getRow(i);
			if(row != null) 
			{
				tmp = sheet.getRow(i).getPhysicalNumberOfCells();
				if(tmp > cols)
				{
					cols = tmp;
					headerRowNumber = i;
				}
			}
		}
		return headerRowNumber;
	}


	private Map<String, String> fetchRowDetails(Row row) 
	{
		Map<String,String> rowData = new HashMap<>();
		for (int i=0; i<columns.size(); i++)
		{
			Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
			String value = (cell != null) ? getCellValue(cell) : null;
			rowData.put(columns.get(i), value); 
		}

		return rowData;		
	}

	private static String getCellValue(Cell cell)
	{
		switch(cell.getCellType())
		{
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case NUMERIC:
			return String.valueOf(new Double(cell.getNumericCellValue()).intValue());
		case STRING:
			return String.valueOf(cell.getStringCellValue());
		case BLANK:
			return "";
		default:
			return "";
		}
	}

	private void prepareHeaders(Row row)
	{
		for(Cell cell : row) //Reads only the defined cells in the row and iterates over them.
		{
			columns.add(getCellValue(cell));
		}
	}

	public List<String> getColumns() 
	{
		return this.columns;
	}

	//TODO: following two methods can be deleted when DataImportHandler and PCLContentImporter are merged
	/*protected String getDocumentContent(ImportContext importContext, String relativePath, boolean isBase64Encoded)
	{
		Path absolutePath = getAbsolutePath(importContext, relativePath);	

		return FileUtil.getDocumentContent(absolutePath, isBase64Encoded);
	}

	private Path getAbsolutePath(ImportContext importParam, String relativePath)
	{
		ContentManager cManager = importParam.getContentHolder();
		if (cManager instanceof WorkBookManagerImpl)
		{
			WorkBookManagerImpl workbookManager = (WorkBookManagerImpl) cManager;
			File file = new File(workbookManager.getFileName());
			Path currentFolderPath = file.getParentFile().toPath();

			return currentFolderPath.resolve(Paths.get(relativePath));

		}
		return null;
	}
	public void validateProcessName(String processName, String dataModelId)
	{
		if(!"All".equals(processName))
		{
			String masterTableProcess = CRMModelProcessesCache.getCacheInstance().getProcessId(processName, dataModelId);
			if(CommonUtil.isStringEmpty(masterTableProcess))
			{
				ContentManagementRuntimeException exception = new ContentManagementRuntimeException(Messages.PROCESS_NOT_REGISTERED, processName);
				LOGGER.error(exception,Messages.PROCESS_NOT_REGISTERED, processName);
				throw exception;
			}
		}
	}*/
}
