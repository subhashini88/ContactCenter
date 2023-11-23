package com.opentext.apps.cc.importhandler.datamanager.tasklist;
import com.opentext.apps.cc.importcontent.CommonEnums.CCBaseEnum;

public class ImportConstants {
	public static final String PROPERTIES_FILE_NAME = "Mappings.properties";
	public static final String IMPORT_FILE_NAME = "StatesAndActions";
	public static final String EXCEL_EXTENSION = ".xlsx";
	public static final String TaskLists_SHEET_NAME = "TaskList";
	public static final String IMPORT_JOBID_NAME = "ConfigurableWorkflowData";
	public static final String IMPORT_ZIP_NAME = "ConfigurableWorkflowData";

	public static final String CODE = "Code";
	public static final String RECORD_TYPE = "Type";
	public static final String TASK_ORDER = "Order";
	public static final String CREATIONTYPE = "CreationType";
	public static final String RELATEDINSTANCETYPE = "RelatedInstanceType";
	public static final String DEFAULT = "DEFAULT";
	public static final String DEFAULTIMPORTED = "DEFAULT-IMPORTED";
	
	public static final String NAME = "Name";
	public static final String DESCRIPTION = "Description";

	public static final String TASK_TYPE = "TaskType";
	public static final String TASK_TYPE_REQ = "TypeOfActivity";
	
	public static final String ASSIGNMENT_TYPE = "AssignmentType";
	public static final String ASSIGN_TO_USER = "AssignToUser";
	public static final String ASSIGN_TO_ROLE = "AssignToRole";
	
	public static final String PROCESS_TO_EXECUTE = "ProcessToExecute";
	public static final String PROCESS_TO_EXECUTE_REQ = "ProcessName";
	
	public static final String TRANSIT_STATE_TO = "TransitStateTo";
	public static final String RELATED_GC_PROCESS_REQ = "RelatedGCProcess";
	//public static final String PROCESS_NAME = "Contract";
	
	public static final String DUE_IN = "DueIn";
	public static final String ESCALATION_TYPE = "EscalationType";
	
	public static final String ESCALATION_TO_USER = "EscalateToUser";
	public static final String ESCALATION_TO_ROLE = "EscalateToRole";
	
	public static final String REPORT = "Report";
	public static final String STATUS_LOG = "StatusLog";
	public static final String STATUS_ERROR = "Error";
	public static final String STATUS_SUCESS = "Success";
	public static final String NOT_COMPLETED = "Not Completed";
	public static final String STATUS = "Status";
	public static final String RECORD_EXISTS = "Record exists";
	public static final String STATUS_COMPLETED = "Completed";
	public static final String STATUS_FAILED = "Failed";
	
	public static final String IMPORT_DISPLAYNAME="DefaultTaskListAndRules";
	
	
	public static final String REGEX_EXP = "\\b\\d+\\b";
	
	public enum RecordType implements CCBaseEnum{
		TASKLIST(1, "TASKLIST"), TASK(2, "TASK");
		
		private int id;
		private String value;
		
		private RecordType(int id, String value) {
			this.id = id;
			this.value = value;
		}

		@Override
		public int getId() {
			return id;
		}

		@Override
		public String getValue() {
			return value;
		}
		public static RecordType getEnumObject(String value) {
			if (null != value) {
				for (RecordType item : RecordType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return item;
					}
				}
			}
			return null;
		}

		public static boolean contains(String value) {
			if (null != value) {
				for (RecordType item : RecordType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}
	
	
	public enum TransitState implements CCBaseEnum {
		
		ACTIVE(1, "Active"), DRAFT(2, "Draft"), NEGOTIATION(3,"Negotiation"), EXECUTION(4,"Execution"),
		PREEXECUTION(5,"Pre-Execution");
		
		private int id;
		private String value;

		private TransitState(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public int getId() {
			return id;
		}

		public String getValue() {
			return value;
		}

		public static TransitState getEnumObject(String value) {
			if (null != value) {
				for (TransitState item : TransitState.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return item;
					}
				}
			}
			return null;
		}

		public static boolean contains(String value) {
			if (null != value) {
				for (TransitState item : TransitState.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}
	
	public enum AssignmentType implements CCBaseEnum{
		INDIVIDUAL(1, "INDIVIDUAL"), ROLE(2, "ROLE");
		
		private int id;
		private String value;
		
		private AssignmentType(int id, String value) {
			this.id = id;
			this.value = value;
		}

		@Override
		public int getId() {
			return id;
		}

		@Override
		public String getValue() {
			return value;
		}
		public static AssignmentType getEnumObject(String value) {
			if (null != value) {
				for (AssignmentType item : AssignmentType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return item;
					}
				}
			}
			return null;
		}

		public static boolean contains(String value) {
			if (null != value) {
				for (AssignmentType item : AssignmentType.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			return false;
		}
	}
	
	public enum TypeOfActivity implements CCBaseEnum{
		STANDARD(1, "STANDARD", "STANDARD"), APPROVAL(2, "APPROVAL", "APPROVAL"), CUSTOM(3, "CUSTOM","CUSTOM"), STATE_TRANSITION(4, "STATE TRANSITION", "STATE_TRANSITION");
		
		private int id;
		private String value;
		private String reqValue;
		
		private TypeOfActivity(int id, String value, String reqValue) {
			this.id = id;
			this.value = value;
			this.reqValue = reqValue;
		}

		@Override
		public int getId() {
			return id;
		}

		@Override
		public String getValue() {
			return value;
		}
		
		public String getReqValue() {
			return reqValue;
		}
		
		public static TypeOfActivity getEnumObject(String value) {
			if (null != value) {
				for (TypeOfActivity item : TypeOfActivity.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return item;
					}
				}
			}
			return null;
		}

		public static boolean contains(String value) {
			if (null != value) {
				for (TypeOfActivity item : TypeOfActivity.values()) {
					if (item.value.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			return false;
		}
		
	}
	
}
