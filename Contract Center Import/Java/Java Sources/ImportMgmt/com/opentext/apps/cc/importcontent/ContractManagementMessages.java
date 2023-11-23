package com.opentext.apps.cc.importcontent;
 
import com.eibus.localization.IStringResource;
import com.eibus.tools.internal.MessageBundle;
import com.eibus.tools.internal.MessageBundleGenerator;
import com.eibus.tools.internal.MessageText;
 
@MessageBundle("Opentext.bpm.spa.contractmgmt.util.messages")
public class ContractManagementMessages
{
	private static int s_fieldIndexer = 0;
 
    private static IStringResource getMessage() {
        return MessageBundleGenerator.getMessage(ContractManagementMessages.class, s_fieldIndexer++);
    }
 
    @MessageText("A {0} with the same name exists. Enter a unique name.")
    public static IStringResource UNIQUE_KEY_VIOLATION_OCCURRED = getMessage();
    
	@MessageText("There is no Report File exist.")
    public static IStringResource NO_REPORT_FILE_FOUND = getMessage();
	
    @MessageText("The status has been changed from ''{0}'' to ''{1}''")
    public static IStringResource STATE_CHANGE_MESSAGE = getMessage();
    
    @MessageText("Compare is completed with an error. Refer to logs for more information")
    public static IStringResource COMPARE_ERROR = getMessage();
    
    @MessageText("INVALID value for Priority.value of contract priority should be one of 'HIGH','LOW','MEDIUM'")
    public static IStringResource PRIORITY_ERROR = getMessage();
    
    @MessageText("Could not read the Contract Center properties file.")
    public static IStringResource ERROR_READING_PROPERTIES_FILE = getMessage();
    
    @MessageText("Could not get the generated document id. Refer to logs for more information.")
    public static IStringResource ERROR_READING_SS_DOCUMENT_ID = getMessage();
    
    @MessageText("Could not change the document state.")
    public static IStringResource ERROR_STATECHANE = getMessage();
    
    @MessageText("Unable to upload Document to Content Server")
    public static IStringResource ERROR_READING_RELEASEDOCUCMENT = getMessage();
    
    @MessageText("Could not Create Contract Document")
    public static IStringResource ERROR_CREATE_DOCUMENT = getMessage();
    
    @MessageText("Could not Store Document in Content Server")
    public static IStringResource ERROR_STORE_OTCS= getMessage();
    @MessageText("Could not get the generated document id. Request {0}, Response {1}")
    public static IStringResource SS_DOCUMENT_ID_NOT_GENEREATED = getMessage();
    
    @MessageText("There is no file exists at {0} for equivalent clauses.")
    public static IStringResource NO_EQUIVALENT_CLAUSES_FILE = getMessage();
    
    @MessageText("There is no contract with the given contractId {0}.")
    public static IStringResource NO_CONTRACT_WITH_THIS_ID = getMessage();
    
    @MessageText("Term name contains characters that are not allowed.")
    public static IStringResource INVALID_TERM_NAME = getMessage();
    
    @MessageText("The contract renewal is in progress.")
    public static IStringResource RENEWAL_INPROGRESS = getMessage();
    
    @MessageText("The contract termination is in progress.")
    public static IStringResource TERMINATE_INPROGRESS = getMessage();
    
    @MessageText("There are no sections and clauses associated to selected template.")
    public static IStringResource NO_SECTIONS_CLAUSES = getMessage();
    
    @MessageText("''{0}'' Section has been removed")
    public static IStringResource SECTION_REMOVED_MESSAGE = getMessage();
    
    @MessageText("New ''{0}'' Section has added")
    public static IStringResource SECTION_ADDED_MESSAGE = getMessage();
    
    @MessageText("''{0}'' clause/clauses has been added to ''{1}''")
    public static IStringResource CLAUSE_ADDED_MESSAGE = getMessage();

    @MessageText("''{0}''  clause/clauses has removed from ''{1}''")
    public static IStringResource CLAUSE_REMOVED_MESSAGE = getMessage();
    
    @MessageText("New ''{0}'' Section has added with following clauses ''{1}''")
    public static IStringResource SECTION_ADDED_WITH_CLAUSES_MESSAGE = getMessage();
    
    @MessageText("The clause category cannot be deleted because it is in use.")
    public static IStringResource CLAUSE_CATEGORY_UNDER_USE = getMessage();
    
    @MessageText("This term is already being used")
    public static IStringResource TERM_UNDER_USE = getMessage();
    
    @MessageText("The clause cannot be deleted because it is in use.")
    public static IStringResource CLAUSES_IS_IN_USE = getMessage();
    
    @MessageText("Are you sure you want to delete this clause?")
    public static IStringResource DELETE_CLAUSE_ALERT_MSG = getMessage();
    
    @MessageText("The template cannot be deleted because it is in use.")
    public static IStringResource TEMPLATE_NOT_DELETED = getMessage();
    
    @MessageText("The contract sub-type cannot be deleted because it is in use.")
    public static IStringResource SUB_TYPE_UNDER_USE = getMessage();
    
    @MessageText("The contract type cannot be deleted because it is in use.")
    public static IStringResource CONTRACT_TYPE_USE = getMessage();
    
    @MessageText("The contract sub-type doesn't belong the mentioned Contract Type.")
    public static IStringResource TYPE_SUBTYPE_MISMATCH = getMessage();
    
    @MessageText("The mentioned Process Name is invalid")
    public static IStringResource INVALID_PROCESS_NAME = getMessage();
    
    @MessageText("Given artifact does not exist.")
    public static IStringResource DOES_NOT_EXIST = getMessage();
    
    @MessageText("The mentioned ''{0}'' is not accessible to the current role.")
    public static IStringResource NOT_ACCESSIBLE_TO_CURRENT_ROLE = getMessage();
    
    @MessageText("The {0} cannot be deleted because it is in use.")
    public static IStringResource ATTRIBUTE_UNDER_USE = getMessage();
    
    @MessageText("Section name cannot be empty.")
    public static IStringResource EMPTY_SECTION_NAME = getMessage();
    
    @MessageText("Atleast one document must be attached to the template.")
    public static IStringResource NO_DOCUMENTS = getMessage();
    
    @MessageText("The contract template doesn't belong the mentioned Contract template Type.")
    public static IStringResource TEMPLATE_TYPE_MISMATCH = getMessage();
    
    @MessageText("Required parameters for xECM is not available in properties.")
    public static IStringResource XECM_PROPERTIES_MISSING = getMessage();
    
    @MessageText("NON-STANDARD 'ContractType/Subtype/Clausecategory' is not present.")
    public static IStringResource NON_STANDARD_MASTER_DATA = getMessage();
    
    @MessageText("Invalid 'CustomizationType', it should be EQUAL/STANDARD/NON-STANDARD.")
    public static IStringResource INVALID_CUSTOMIZATION_TYPE = getMessage();
    
    @MessageText("Invalid equivalent clause operation, source clause is missing")
    public static IStringResource SOURCE_CLAUSE_MISSING = getMessage();
    
    @MessageText("Invalid equivalent clause details.")
    public static IStringResource INVALID_EQUIVALENT_CLAUSE = getMessage();
    
    @MessageText("''{0}'' non-standard clause/clauses has been added to ''{1}''")
    public static IStringResource NON_STANDARD_CLAUSE_ADDED = getMessage();

    @MessageText("''{0}'' standard clause/clauses has been added to ''{1}''")
    public static IStringResource STANDARD_CLAUSE_ADDED = getMessage();

    @MessageText("''{0}'' is replaced with euivalent clause ''{1}'' in section ''{2}''")
    public static IStringResource EQUIVALENT_CLAUSE_ADDED = getMessage();
    
    @MessageText("The clause association cannot be deleted because clause(s) ''{0}'' is/are in use.")
    public static IStringResource CLAUSE_ASSOCIATED_IS_IN_USE = getMessage();
    
    @MessageText("Customization not allowed when contract is in review")
    public static IStringResource CUSTOMIZATION_NOT_ALLOWED = getMessage();
    
    @MessageText("xECM contract business workspace not created")
    public static IStringResource BW_NOTCREATED = getMessage();
    
    @MessageText("Please initializes Process Component Library Tenant Data for ''{0}'' organization")
    public static IStringResource PCL_IS_NOT_INITIALIZED_IN_TENANT = getMessage();
    
    @MessageText("Error occurred while initialing Contract Center Tenant Data in ''{0}'' organization")
    public static IStringResource ERROR_INITIALIZED_DATA_IN_TENANT = getMessage();
}