package com.opentext.apps.cc.analytics.exceptions;

import com.eibus.localization.message.Message;
import com.eibus.localization.message.MessageSet;

public class ContractCenterAlertMessages {
	public static final MessageSet CC_MESSAGE_SET = MessageSet.getMessageSet("com.opentext.apps.contractcenter.Messages");

	public static final Message WEBSERVICE_FAILURE_READ_CONTRACT = CC_MESSAGE_SET.getMessage("webserviceFailureReadContract");
	public static final Message WEBSERVICE_FAILURE_GET_TEMPLATE_DETAILS = CC_MESSAGE_SET.getMessage("webserviceFailureGetTemplateDetails");
	public static final Message WEBSERVICE_FAILURE_GET_CONTRACT_RELATIONS_FOR_DOCGEN = CC_MESSAGE_SET.getMessage("webserviceFailureGetContractRelationsForDocgen");
	public static final Message WEBSERVICE_FAILURE_READ_GC_TERM = CC_MESSAGE_SET.getMessage("webserviceFailureReadGCTerm");
	public static final Message WEBSERVICE_FAILURE_GET_TERM_INST_BY_CLA_AND_SEC_ORDER = CC_MESSAGE_SET.getMessage("webserviceFailureGetTermInstByClaAndSecOrder");
	public static final Message WEBSERVICE_FAILURE_GET_TERM_REFERENCE = CC_MESSAGE_SET.getMessage("webserviceFailureGetTermReference");
	public static final Message WEBSERVICE_FAILURE_GET_CUSTOM_ATTRIBUTES = CC_MESSAGE_SET.getMessage("webserviceFailureGetCustomAttributes");
	public static final Message WEBSERVICE_FAILURE_GET_CTR_DETAILS = CC_MESSAGE_SET.getMessage("webserviceFailureGetCTRDetailsBy");
	public static final Message PARSING_FAILURE_DATE = CC_MESSAGE_SET.getMessage("parsingFailureDate");
	public static final Message INPUT_SHOULD_NOT_BE_NULL = CC_MESSAGE_SET.getMessage("inputShouldNotBeNullOrEmpty");
	public static final Message ERROR_WHILE_EXECUTING = CC_MESSAGE_SET.getMessage("errorWhileExecuting");
	public static final Message ERROR_WHILE_TRIGGERING_WEBSERVICE = CC_MESSAGE_SET.getMessage("errorWhileTriggeringWebService");
	public static final Message ENTITY_INSTANCE_NOT_FOUND = CC_MESSAGE_SET.getMessage("entityInstanceNotFound");
	public static final Message ERROR_IN_PROCESS_CREATION_SERVICE = CC_MESSAGE_SET.getMessage("errorInPrcessCreationService");
	public static final Message ERROR_WHILE_CREATING_DUMP = CC_MESSAGE_SET.getMessage("errorWhileCreatingDump");	
	public static final Message ERROR_WHILE_CREATING_EXCEL_FILE = CC_MESSAGE_SET.getMessage("errorWhileCreatingExcelFile");
	public static final Message ERROR_WHILE_MAPPING_ROLES = CC_MESSAGE_SET.getMessage("errorWhileMappingRoles");	
	public static final Message ERROR_WHILE_OPENING_FILE = CC_MESSAGE_SET.getMessage("errorWhileOpeningFile");
	public static final Message ERROR_WHILE_TRIGGERING_BPM = CC_MESSAGE_SET.getMessage("errorWhileTriggeringBPM");	
	public static final Message ERROR_WHILE_UPLOADING_FILE = CC_MESSAGE_SET.getMessage("errorWhileUploadingFile");	
	public static final Message DATE_PARSE_EXCEPTION = CC_MESSAGE_SET.getMessage("dateParseException");	
	public static final Message INVALID_PARAMETERS = CC_MESSAGE_SET.getMessage("invalidParameters");
	public static final Message URL_IS_NOT_VALIED = CC_MESSAGE_SET.getMessage("urlIsNotValid");	
	public static final Message STATUS_DUMP_FAILED = CC_MESSAGE_SET.getMessage("statusDumpFailed");
	public static final Message UNABLE_TO_INITIALIZE_HANDLER = CC_MESSAGE_SET.getMessage("unableToInitializeHandler");	
	public static final Message FILE_NOT_FOUND = CC_MESSAGE_SET.getMessage("fileNotFound");
	
	public static final Message RESTSERVICE_FAILURE_ADD_RELATIONTOBW = CC_MESSAGE_SET.getMessage("errorAddRelationToBW");
	public static final Message RESTSERVICE_FAILURE_REMOVE_RELATION_FROM_BW = CC_MESSAGE_SET.getMessage("errorRemoveRelationFromBW");
}
