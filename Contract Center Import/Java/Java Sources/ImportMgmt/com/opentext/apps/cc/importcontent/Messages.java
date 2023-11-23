package com.opentext.apps.cc.importcontent;

import com.eibus.localization.message.Message;
import com.eibus.localization.message.MessageSet;

public class Messages {
	public static final MessageSet MESSAGE_SET = MessageSet.getMessageSet("ContractCenter.ContentManagement");

	/** An error occurred while creating the file ''{0}''. Check folder permissions and try again. */
	public static final Message EXCEPTION_CREATING_FILE = MESSAGE_SET.getMessage("exceptionCreatingFile");

	/** An error occurred while opening the workbook ''{0}''. Check whether the file is a valid workbook. */
	public static final Message EXCEPTION_OPENING_WORKBOOK = MESSAGE_SET.getMessage("exceptionOpeningWorkbook");
	
	/** An error occurred while reading content from the file ''{0}''. */
	public static final Message EXCEPTION_READING_FILE_CONTENT = MESSAGE_SET.getMessage("exceptionReadingFileContent");
	
	/** Provide a file with .xls or .xlsx extension. */
	public static final Message INVALID_FILE = MESSAGE_SET.getMessage("invalidFile");
	
	/** The ProcessName ''{0}'' is not registered in the CRM_Model_Processes table of the target machine. */
	public static final Message PROCESS_NOT_REGISTERED = MESSAGE_SET.getMessage("processNotRegistered");
	
	/** Unable to find the entry reference ''{0}'' for the data ''{1}'' in the target system. */
	public static final Message REFERENCE_NOT_FOUND = MESSAGE_SET.getMessage("referenceNotFound");
	
	/** Sheet name ''{0}'' for the configurable object ''{1}'' is invalid because the name does not end with ''#Tab'', ''#Button'', or ''#InitiationItem''. */
	public static final Message INVALID_SHEET_NAME_FOR_CONFIG_OBJECT = MESSAGE_SET.getMessage("invalidSheetNameForConfigObject");
	
	/** Sheet name ''{0}'' for the configurable object ''{1}'' is invalid because the name does not end with ''Attribute Creation'', ''Attribute Creation#Translation'', ''Attribute Creation#Options'', or ''Attribute Creation#OptionsT''. */
	public static final Message INVALID_SHEET_NAME_FOR_ATTRIBUTE_IMPORT = MESSAGE_SET.getMessage("invalidSheetNameForAttributeImport");
	
	/** An error occurred while reading the cache value for the key ''{0}''. The error is ''{1}''.  */
	public static final Message CACHE_READ_ERROR = MESSAGE_SET.getMessage("cacheReadError");
	
	/** Export query for the object ''{0}'' is null. */
	public static final Message NULL_EXPORT_QUERY = MESSAGE_SET.getMessage("nullExportQuery");
	
	/** An error occurred while loading the class ''{0}''. Check the class path and try again. */
	public static final Message ERROR_LOADING_CLASS = MESSAGE_SET.getMessage("errorLoadingClass");

	/** An error occurred while processing the artifact ''{0}''.  */
	public static final Message ERROR_PROCESSING_ARTIFACT = MESSAGE_SET.getMessage("errorProcessingArtifact");

	/** Month ''{0}'' is invalid. */
	public static final Message INVALID_MONTH = MESSAGE_SET.getMessage("invalidMonth");
	
	/** Unable to find the configurable object ''{0}'' because it is not registered. */
	public static final Message INVALID_EXPORT_OBJECT =  MESSAGE_SET.getMessage("invalidExportObject");

	/** Completed exporting the object ''{0}'' */
	public static final Message COMPLETED_EXPORTING_OBJECT =  MESSAGE_SET.getMessage("completedExportingObject");

	/** Started exporting the object ''{0}'' */
	public static final Message STARTED_EXPORTING_OBJECT =  MESSAGE_SET.getMessage("startedExportingObject");

	/** Week ''{0}'' is invalid. */
	public static final Message INVALID_WEEK  =  MESSAGE_SET.getMessage("invalidWeek");

	/** Day ''{0}'' is invalid. */
	public static  final Message INVALID_DAY  =  MESSAGE_SET.getMessage("invalidDay");

	/** There is no data to export for object ''{0}''. */
	public static final Message NO_DATA =  MESSAGE_SET.getMessage("noData");

	/** ImportHandler is not specified for the object ''{0}''. */
	public static final Message NO_IMPORTHANDLER  =  MESSAGE_SET.getMessage("noImporthandler");

	/** ExportHandler is not specified for the object ''{0}''. Processing with default ExportHandler. */
	public static final Message NO_EXPORTHANDLER  =  MESSAGE_SET.getMessage("noExporthandler");
	
	/** An error occurred while creating the sheet ''{0}''. */
	public static final Message EXCEPTION_CREATING_SHEET = MESSAGE_SET.getMessage("exceptionCreatingSheet");
	
	/** An exception occurred while Persisting Imported Content ''{0}''. */
	public static final Message EXCEPTION_PERSISTING_IMPORTED_CONTENT = MESSAGE_SET.getMessage("exceptionPersistingImportedContent");
	
	/** An error occurred while executing the query ''{0}'' for exporting the object ''{1}'' */
	public static final Message EXCEPTION_EXECUTING_QUERY = MESSAGE_SET.getMessage("exceptionExecutingQuery");
	

	public static final Message COMPLETED_EXPORTING_CONTENT =  MESSAGE_SET.getMessage("CompletedExportingContent");
	
	public static final Message COMPLETED_IMPORTING_CONTENT =  MESSAGE_SET.getMessage("CompletedImportingContent");

	public static final Message STARTED_EXPORTING_ARTIFACT =  MESSAGE_SET.getMessage("StartedExportingArtifact");
	
	public static final Message COMPLETED_EXPORTING_ARTIFACT =  MESSAGE_SET.getMessage("CompletedExportingArtifact");
	
	public static final Message STARTED_IMPORTING_ARTIFACT =  MESSAGE_SET.getMessage("StartedImportingArtifact");
	
	public static final Message COMPLETED_IMPORTING_ARTIFACT =  MESSAGE_SET.getMessage("CompletedImportingArtifact");
	
	public static final Message ROLL_BACK_EXPORT_FILES =  MESSAGE_SET.getMessage("rollBackExportFiles");
	
	/** Unable to publish message through event service ''{0}''. */
	public static final Message PUBLISH_MESSAGE_FAILED =  MESSAGE_SET.getMessage("publishMessageFailed");
	
	/**Exporting application content has started.*/
	public static final Message STARTED_EXPORTING_CONTENT =  MESSAGE_SET.getMessage("StartedtedExportingContent");
	
	/**Importing application content has started.*/
	public static final Message STARTED_IMPORTING_CONTENT =  MESSAGE_SET.getMessage("StartedImportingContent");
	
	/** Sheet name ''{0}'' for the configurable object ''{1}'' is invalid because the name does not end with ''#Cul'', ''#Attribute'', or ''#AttriCult''. */
	public static final Message INVALID_SHEET_NAME_FOR_OBJECT_CONFIGURATION = MESSAGE_SET.getMessage("invalidSheetNameForObjectConfiguration");
	
	/** Import has failed because data does not exist in column ''{0}'' of the manifest. */
	public static final Message EMPTY_MANIFEST_WORKBOOK_NAME = MESSAGE_SET.getMessage("EmptyManifestWorkBookName");
	
	/** Import has failed because the import file does not have the manifest workbook.*/
	public static final Message MANIFEST_DOESNOT_EXISTS = MESSAGE_SET.getMessage("ManifestDoesnotExist");
	
	/** Import has failed because manifest entry ''{0}'' is unable to resolve to a workbook in the import file. */
	public static final Message NO_WORKBOOK_FOR_MANIFEST_ENTRY = MESSAGE_SET.getMessage("NoWorkBookForManifestEntry");
	
	/** Unable to insert the record ''{0}'' in the table ''{1}'' because the data already exists. */
	public static final Message TENANT_DATA_ALREADY_EXISTS = MESSAGE_SET.getMessage("tenantDataAlreadyExists");
	
	/** Tenant already initialized for organization */
	public static final Message TENANT_INITIALIZED = MESSAGE_SET.getMessage("tenantDataAlreadyInitialized");
	

}
