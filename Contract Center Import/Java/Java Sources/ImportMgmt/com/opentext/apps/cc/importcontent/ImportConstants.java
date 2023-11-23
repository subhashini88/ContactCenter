package com.opentext.apps.cc.importcontent;

import java.util.HashMap;
import java.util.Map;

public class ImportConstants {
	public static final String ADDRESS_HANDLER = "com.opentext.apps.cc.importhandler.addresses.AddressImportHandler";
	public static final String CONTRACT_HANDLER = "com.opentext.apps.cc.importhandler.contract.ContractImportHandler";
	public static final String ACCOUNTMANAGER_HANDLER = "com.opentext.apps.cc.importhandler.accountmanager.AccountManagerImportHandler";
	public static final String ACCOUNTNUMBER_HANDLER = "com.opentext.apps.cc.importhandler.collectionaccount.CollectionAccountImportHandler";
	public static final String PARTY_HANDLER = "com.opentext.apps.cc.importhandler.party.PartyImportHandler";
	public static final String TERM_HANDLER = "com.opentext.apps.cc.importhandler.term.TermImportHandler";	
	public static final String REVENUE_SCHEDULE_HANDLER = "com.opentext.apps.cc.importhandler.revenueschedule.RevenueImportHandler";
	public static final String CONTRACT_LINES = "com.opentext.apps.cc.importhandler.contractlines.ContractLinesImportHandler";
	public static final String BILLER_PERIODS = "com.opentext.apps.cc.importhandler.contractbillerperiod.BillerPeriodImportHandler";
	public static final String CONTACT_MAPPING_HANDLER = "com.opentext.apps.cc.importhandler.contactsmapping.ContactMappingImportHandler";
	public static final String LANGUAGE_HANDLER = "com.opentext.apps.cc.importhandler.admin.LanguageImportHandler";
	public static final String TYPE_HANDLER = "com.opentext.apps.cc.importhandler.admin.TypeImportHandler";
	public static final String SUBTYPE_HANDLER = "com.opentext.apps.cc.importhandler.admin.SubtypeImportHandler";
	public static final String MANAGER_HANDLER = "com.opentext.apps.cc.importhandler.manager.ManagerImportHandler";
	public static final String CLAUSE_HANDLER = "com.opentext.apps.cc.importhandler.clause.ClauseImportHandler";
	public static final String EXTERNAL_CONTACT_HANDLER = "com.opentext.apps.cc.importhandler.externalcontacts.ExternalContactImportHandler";
    public static final String ORGANIZATION_HANDLER ="com.opentext.apps.cc.importhandler.organizations.OrganizationImportHandler";
    public static final String ORGANIZATION_MEMBERS_HANDLER = "com.opentext.apps.cc.importhandler.organizationmembers.OrganizationMembersImportHandler";
    public static final String TASKLIST_HANDLER = "com.opentext.apps.cc.importhandler.tasklist.TaskListImportHandler";
    public static final String RULE_HANDLER ="com.opentext.apps.cc.importhandler.rules.RuleImportHandler";
    public static final String CONTRACT_TYPE_HANDLER ="com.opentext.apps.cc.importhandler.contractType.ContractTypeImportHandler";
    public static final String CLAUSE_CATEGORY_HANDLER ="com.opentext.apps.cc.importhandler.clauseCategory.ClauseCategoryImportHandler";
    public static final String CONTRACT_RELATEDDEPARTMENT_HANDLER ="com.opentext.apps.cc.importhandler.contract.relateddepartment.RelatedDepartmentsImportHandler";
    public static final String RELATEDCOSTCENTER_TYPE_HANDLER ="com.opentext.apps.cc.importhandler.relatedCostCenter.RelatedCostCenterImportHandler";
    public static final String RELATED_DEPARTMENT_MEMBERS_HANDLER ="com.opentext.apps.cc.importhandler.contract.relateddepmembers.RelDepMemberImportHandler";
	

	public static final String DOCUMENTS_URL = "DocumentsURL";
	public static final String DOCUMENTS_PATH = "DocumentsPath";
	public static final String DOCUMENTS_FOLDER = "DocumentsDefaultFolder";
	
	public static final String PROPERTIES_FILE_NAME = "Mappings.properties";
	public static final String DOWNLOAD_READ_PATH = "com.eibus.web.tools.download.DownloadReadPath";
	
	public enum ImportHandler{
		Address(ADDRESS_HANDLER),
		Contract(CONTRACT_HANDLER),
		ContractLine(CONTRACT_LINES),
		BillerPeriod(BILLER_PERIODS),
		Party(PARTY_HANDLER),
		Term(TERM_HANDLER),
		AccountManager(ACCOUNTMANAGER_HANDLER),
		AccountNumber(ACCOUNTNUMBER_HANDLER),
		RevenueSchedule(REVENUE_SCHEDULE_HANDLER),
		ContactMapping(CONTACT_MAPPING_HANDLER),
		Language(LANGUAGE_HANDLER),
		Type(TYPE_HANDLER),
		Subtype(SUBTYPE_HANDLER),
		Managers(MANAGER_HANDLER),
		Clause(CLAUSE_HANDLER),
		ExternalContact(EXTERNAL_CONTACT_HANDLER),
                Organization(ORGANIZATION_HANDLER),
		Member(ORGANIZATION_MEMBERS_HANDLER),
                Rule(RULE_HANDLER),
		TaskList(TASKLIST_HANDLER),
		ContractType(CONTRACT_TYPE_HANDLER),
		ClauseCategory(CLAUSE_CATEGORY_HANDLER),
                RelatedCostCenter(RELATEDCOSTCENTER_TYPE_HANDLER),
		ContractRelatedDepartment(CONTRACT_RELATEDDEPARTMENT_HANDLER),
		RelatedDepartmentMembers(RELATED_DEPARTMENT_MEMBERS_HANDLER);
		
		
		private String value;
		private static final Map<String, String> lookupByValue = new HashMap<String, String>();
		 static {
		      for(ImportHandler ct : ImportHandler.values()){
		    	  lookupByValue.put(ct.name(),ct.getValue());
		      }
		 }

		ImportHandler(String value){
			this.value = value;
		}
		
		public String getValue(){
			return value;
		}
		
		public static String lookup(String value){
			return lookupByValue.get(value);
		}
	}
	
}
