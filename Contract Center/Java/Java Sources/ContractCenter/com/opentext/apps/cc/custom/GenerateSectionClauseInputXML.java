package com.opentext.apps.cc.custom;

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Period;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import com.cordys.cpc.bsf.busobject.BSF;
import com.cordys.cpc.bsf.busobject.BusObjectConfig;
import com.cordys.cpc.bsf.busobject.BusObjectIterator;
import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.util.system.EIBProperties;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.internal.NOMDocumentPool;
import com.opentext.apps.cc.custom.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.custom.exceptions.ContractCenterApplicationException;
import com.opentext.apps.cc.custom.model.AbstractGCContainerModel;
import com.opentext.apps.cc.custom.model.DocGenModelUtil;
import com.opentext.apps.cc.custom.model.GCClauseContainerModel;
import com.opentext.apps.cc.custom.model.GCSectionContainerModel;

public class GenerateSectionClauseInputXML extends GenerateSectionClauseInputXMLBase {
	private static final CordysLogger logger = CordysLogger.getCordysLogger(GenerateSectionClauseInputXML.class);

	public GenerateSectionClauseInputXML() {
		this((BusObjectConfig) null);
	}

	public GenerateSectionClauseInputXML(BusObjectConfig config) {
		super(config);
	}

	public static boolean DeleteGenerateddocFromFS(String fileLocation) {
		String orgDN = BSF.getOrganization();
		String orgName = orgDN.substring(orgDN.indexOf('=') + 1, orgDN.indexOf(','));
		String basePath = Paths.get(EIBProperties.getInstallDir()).resolve(
				"content" + File.separator + "docgen" + File.separator + orgName + File.separator + fileLocation)
				.toString();
		logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.INFO, null,
				"Deleting generated document from file system at " + basePath);
		File fileDir = new File(basePath);

		if (fileDir.isDirectory() == false) {
			return false;
		}
		File[] listFiles = fileDir.listFiles();
		for (File file : listFiles) {
			if (!file.delete()) {
				return false;
			}
		}
		if (fileDir.delete()) {
			return true;
		} else {
			return false;
		}
	}

	public static String getFilePathURI(String fileLocation) {
		String fileURI = null;
		File fileLoc = new File(fileLocation);
		if (fileLoc.exists()) {
			return fileLoc.toURI().toString();
		}
		return fileURI;
	}

	public static int GenerateInputXML(String itemID) {
		int itemIDNode = 0, contractIDNode = 0, readContractResponse = 0;
		int inputXMLNode = NomUtil.parseXML("<Mapping></Mapping>");
		try {
			SOAPRequestObject readContractRequest = new SOAPRequestObject(
					"http://schemas/OpenTextContractCenter/Contract/operations", "ReadContract", null, null);
			contractIDNode = NOMDocumentPool.getInstance().createElement("Contract-id");
			Node.createTextElement("ItemId", itemID, contractIDNode);
			readContractRequest.addParameterAsXml(contractIDNode);
			readContractResponse = readContractRequest.sendAndWait();
			logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.INFO, null,
					"Initiating ReadContract web service");
			addCustomAttributesToXML(itemID, inputXMLNode);
			addPartiesToXML(itemID, inputXMLNode, readContractResponse);
			createInputXML(readContractResponse, inputXMLNode);
			addPONumbersToXML(itemID, inputXMLNode);
			addContractLinesToXML(itemID, inputXMLNode);

		} catch (Exception e) {
			logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.ERROR, e,
					ContractCenterAlertMessages.WEBSERVICE_FAILURE_READ_CONTRACT);
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.WEBSERVICE_FAILURE_READ_CONTRACT);
		} finally {
			Utilities.cleanAll(itemIDNode, contractIDNode, readContractResponse);
		}
		return inputXMLNode;
	}

	private static void createInputXML(int readContractResponse, int inputXMLNode) {

		// Adding Contract Properties.
		addContractPropertiestoXML(readContractResponse, inputXMLNode);

		// Adding Contract Relationships.
		addContractRelationsToXML(readContractResponse, inputXMLNode);

		// Adding Sections and Clauses.
		addRepetableElements(readContractResponse, inputXMLNode);
	}

	private static void addContractPropertiestoXML(int readContractResponse, int inputXMLNode) {
		logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.INFO, null,
				"Adding contract properties in xml");
		addParameter(inputXMLNode, Node.getDataWithDefault(NomUtil.getNode(".//Amendment", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_AMENDMENT.toUpperCase());
//		addParameter(inputXMLNode,
//				Node.getDataWithDefault(NomUtil.getNode(".//AmendmentType", readContractResponse), null),
//				GenerateSectionClauseConstants.CONTRACT_AMENDMENTTYPE.toUpperCase());
		addParameter(inputXMLNode, Node.getDataWithDefault(NomUtil.getNode(".//AutoRenew", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_AUTORENEW.toUpperCase());
		int autoRenewDuration = Period
				.parse(Node.getDataWithDefault(NomUtil.getNode(".//AutoRenewDuration", readContractResponse), "P0M"))
				.getMonths();
		addParameter(inputXMLNode, String.valueOf(autoRenewDuration) + " Month(s)",
				GenerateSectionClauseConstants.CONTRACT_AUTORENEWDURATION.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//CancellationComments", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_CANCELLATIONCOMMENTS.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//ClientEarlyTermRight", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_CLIENTEARLYTERMRIGHT.toUpperCase());
		addParameter(inputXMLNode, Node.getDataWithDefault(NomUtil.getNode(".//Comments", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_COMMENTS.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//ContractName", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_CONTRACTNAME.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//ContractNumber", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_CONTRACTNUMBER.toUpperCase());
		int contractTerm = Period
				.parse(Node.getDataWithDefault(NomUtil.getNode(".//ContractTerm", readContractResponse), "P0M"))
				.getMonths();
		addParameter(inputXMLNode, String.valueOf(contractTerm) + " Month(s)",
				GenerateSectionClauseConstants.CONTRACT_CONTRACTTERM.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//ContractValue", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_CONTRACTVALUE.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//ContractValueUSD", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_CONTRACTVALUEUSD.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//CRMOpportunityID", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_CRMOPPORTUNITYID.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//CRMQuoteID", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_CRMQUOTEID.toUpperCase());
		String currentEndDate = getFormattedDate(
				Node.getDataWithDefault(NomUtil.getNode(".//CurrentEndDate", readContractResponse), null));
		addParameter(inputXMLNode, currentEndDate,
				GenerateSectionClauseConstants.CONTRACT_CURRENTENDDATE.toUpperCase());
		String currentStartDate = getFormattedDate(
				Node.getDataWithDefault(NomUtil.getNode(".//CurrentStartDate", readContractResponse), null));
		addParameter(inputXMLNode, currentStartDate,
				GenerateSectionClauseConstants.CONTRACT_CURRENTSTARTDATE.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//CustomerManagerComments", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_CUSTOMERMANAGERCOMMENTS.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//DealManager", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_DEALMANAGER.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//Description", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_DESCRIPTION.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//EarlyTerminationConditions", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_EARLYTERMINATIONCONDITIONS.toUpperCase());
		addParameter(inputXMLNode, Node.getDataWithDefault(NomUtil.getNode(".//EndUser", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_ENDUSER.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//GeneratedContractId", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_GENERATEDCONTRACTID.toUpperCase());
		String initialExpiryDate = getFormattedDate(
				Node.getDataWithDefault(NomUtil.getNode(".//InitialExpiryDate", readContractResponse), null));
		addParameter(inputXMLNode, initialExpiryDate,
				GenerateSectionClauseConstants.CONTRACT_INITIALEXPIRYDATE.toUpperCase());
		String minStartdate = getFormattedDate(
				Node.getDataWithDefault(NomUtil.getNode(".//MinStartdate", readContractResponse), null));
		addParameter(inputXMLNode, minStartdate, GenerateSectionClauseConstants.CONTRACT_MINSTARTDATE.toUpperCase());
		String nextExpirationDate = getFormattedDate(
				Node.getDataWithDefault(NomUtil.getNode(".//NextExpirationDate", readContractResponse), null));
		addParameter(inputXMLNode, nextExpirationDate,
				GenerateSectionClauseConstants.CONTRACT_NEXTEXPIRATIONDATE.toUpperCase());
		addParameter(inputXMLNode, Node
				.getDataWithDefault(NomUtil.getNode(".//OriginalSalesAccountExecutive", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_ORIGINALSALESACCOUNTEXECUTIVE.toUpperCase());
		addParameter(inputXMLNode, Node.getDataWithDefault(NomUtil.getNode(".//Perpetual", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_PERPETUAL.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//PreviousContractID", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_PREVIOUSCONTRACTID.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//PriceProtection", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_PRICEPROTECTION.toUpperCase());
		String priceProtectionDate = getFormattedDate(
				Node.getDataWithDefault(NomUtil.getNode(".//PriceProtectionDate", readContractResponse), null));
		addParameter(inputXMLNode, priceProtectionDate,
				GenerateSectionClauseConstants.CONTRACT_PRICEPROTECTIONDATE.toUpperCase());
		addParameter(inputXMLNode, Node.getDataWithDefault(NomUtil.getNode(".//Priority", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_PRIORITY.toUpperCase());
		String productGoLiveDate = getFormattedDate(
				Node.getDataWithDefault(NomUtil.getNode(".//ProductGoLiveDate", readContractResponse), null));
		addParameter(inputXMLNode, productGoLiveDate,
				GenerateSectionClauseConstants.CONTRACT_PRODUCTGOLIVEDATE.toUpperCase());
		addParameter(inputXMLNode, Node.getDataWithDefault(NomUtil.getNode(".//Renewal", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_RENEWAL.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//RenewalComments", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_RENEWALCOMMENTS.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//RenewalDiscount", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_RENEWALDISCOUNT.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//SAPOrderID", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_SAPORDERID.toUpperCase());
		String signatureDate = getFormattedDate(
				Node.getDataWithDefault(NomUtil.getNode(".//SignatureDate", readContractResponse), null));
		addParameter(inputXMLNode, signatureDate, GenerateSectionClauseConstants.CONTRACT_SIGNATUREDATE.toUpperCase());
		String startDate = getFormattedDate(
				Node.getDataWithDefault(NomUtil.getNode(".//StartDate", readContractResponse), null));
		addParameter(inputXMLNode, startDate, GenerateSectionClauseConstants.CONTRACT_STARTDATE.toUpperCase());
		String terminationDate = getFormattedDate(
				Node.getDataWithDefault(NomUtil.getNode(".//CancellationDate", readContractResponse), null));
		addParameter(inputXMLNode, terminationDate,
				GenerateSectionClauseConstants.CONTRACT_TERMINATIONDATE.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//TerminationFees", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_TERMINATIONFEES.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//TerminationNoticePeriod", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_TERMINATIONNOTICEPERIOD.toUpperCase());
		addParameter(inputXMLNode, Node.getDataWithDefault(NomUtil.getNode(".//Validated", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_VALIDATED.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//ValidatedBy", readContractResponse), null),
				GenerateSectionClauseConstants.CONTRACT_VALIDATEDBY.toUpperCase());
		String validatedOn = getFormattedDate(
				Node.getDataWithDefault(NomUtil.getNode(".//ValidatedOn", readContractResponse), null));
		addParameter(inputXMLNode, validatedOn, GenerateSectionClauseConstants.CONTRACT_VALIDATEDON.toUpperCase());
	}

	private static void addContractRelationsToXML(int readContractResponse, int inputXMLNode) {
		logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.INFO, null,
				"Adding contract relationships in xml");
		int readRelationshipsResponse = 0;
		try {
			readRelationshipsResponse = readRelationships(readContractResponse);
			if (readContractResponse != 0) {
				addParameter(inputXMLNode, Node
						.getDataWithDefault(NomUtil.getNode(".//ContractTypeName", readRelationshipsResponse), null),
						GenerateSectionClauseConstants.CONTRACT_CONTRACTTYPE_NAME.toUpperCase());
				addParameter(inputXMLNode,
						Node.getDataWithDefault(NomUtil.getNode(".//CountryName", readRelationshipsResponse), null),
						GenerateSectionClauseConstants.CONTRACT_COUNTRY_NAME.toUpperCase());
				addParameter(inputXMLNode,
						Node.getDataWithDefault(NomUtil.getNode(".//CurrencyName", readRelationshipsResponse), null),
						GenerateSectionClauseConstants.CONTRACT_CURRENCY_NAME.toUpperCase());
				addParameter(inputXMLNode,
						Node.getDataWithDefault(
								NomUtil.getNode(".//DocumentOriginationName", readRelationshipsResponse), null),
						GenerateSectionClauseConstants.CONTRACT_DOCUMENTORIGINATION_NAME.toUpperCase());
				addParameter(inputXMLNode,
						Node.getDataWithDefault(NomUtil.getNode(".//RegionName", readRelationshipsResponse), null),
						GenerateSectionClauseConstants.CONTRACT_REGION_NAME.toUpperCase());

				addParameter(inputXMLNode,
						Node.getDataWithDefault(NomUtil.getNode(".//TerminationReasonName", readRelationshipsResponse),
								null),
						GenerateSectionClauseConstants.CONTRACT_TERMINATIONREASON_NAME.toUpperCase());
				addParameter(inputXMLNode, Node
						.getDataWithDefault(NomUtil.getNode(".//OrganizationName", readRelationshipsResponse), null),
						GenerateSectionClauseConstants.CONTRACT_ORGANIZATION_NAME.toUpperCase());
				addParameter(inputXMLNode,
						Node.getDataWithDefault(
								NomUtil.getNode(".//OrganizationCostCenterId", readRelationshipsResponse), null),
						GenerateSectionClauseConstants.CONTRACT_ORGANIZATION_COSTCENTERID.toUpperCase());
				addParameter(inputXMLNode,
						Node.getDataWithDefault(
								NomUtil.getNode(".//AmendmentTypeName", readRelationshipsResponse), null),
						GenerateSectionClauseConstants.CONTRACT_AMENDMENT_TYPE.toUpperCase());
			}
		} finally {
			if (readRelationshipsResponse != 0) {
				Utilities.cleanAll(readRelationshipsResponse);
			}
		}
	}

	private static void addRepetableElements(int readContractResponse, int inputXMLNode) {
		logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.INFO, null,
				"Adding sections and clauses in xml");
		int templateNode = 0;
		int repeatableElements = NomUtil.getNode(".//RepeatableElements", inputXMLNode);
		if (repeatableElements == 0) {
			repeatableElements = Node.createElement("RepeatableElements", inputXMLNode);
		}

		try {
			if (Node.getDataWithDefault(
					NomUtil.getNode(".//RelatedTemplate/GCTemplate-id/ItemId", readContractResponse), null) != null) {
				int sectionsNode = Node.createElement("ContentRoot", repeatableElements);// NomUtil.parseXML("<Sections></Sections>");
				String contractID = Node.getDataWithDefault(NomUtil.getNode(".//Contract-id/Id", readContractResponse),
						null);
				templateNode = readTemplate(Node.getDataWithDefault(
						NomUtil.getNode(".//RelatedTemplate/GCTemplate-id/ItemId", readContractResponse), null));
				String defaultSectionId = Node.getDataWithDefault(NomUtil.getNode(
						".//TemplateDetails/Details/GCTemplate/DefaultContainingSection/ContainingSections-id/Id1",
						templateNode), null);

				String styllinAttributes = Node.getDataWithDefault(
						NomUtil.getNode(".//TemplateDetails/Details/GCTemplate/StylingAttributes", templateNode),
						DocGenModelUtil.DEFAULT_STYLLING_ATTRIBUTES);

				// Adding numbering style and getting cascading info.
				String cascadeDoclevelFlag = addStyllingInfo(sectionsNode, styllinAttributes);

				boolean isNestedStructureThere = (Objects.nonNull(defaultSectionId) && !defaultSectionId.isBlank());
				if (!isNestedStructureThere) {
					// Old template format, without the containers structure.
					oldTemplatesGenerateInputXML(inputXMLNode, templateNode, sectionsNode, contractID);
				} else {
					// Latest template format, with containers structure.
					latestTemplateGenerateInputXML(inputXMLNode, cascadeDoclevelFlag, templateNode, sectionsNode,
							contractID);
				}
			}
		} finally {
			if (templateNode != 0) {
				Utilities.cleanAll(templateNode);
			}
		}
	}

	private static String addStyllingInfo(int sectionsNode, String styllinAttributes) {
		String cascadeDoclevelFlag = DocGenModelUtil.CASCADE_CONTINUE;
		if (Objects.nonNull(styllinAttributes) && !styllinAttributes.isBlank()) {

			// Numbering style.
			String numberingStyle = DocGenModelUtil.getTokenValue(styllinAttributes, DocGenModelUtil.NUMBERING_STYLE);
			numberingStyle = DocGenModelUtil.getAgumentedStyle(numberingStyle);
			Node.setAttribute(sectionsNode, "class", numberingStyle);

			// Cascading information.
			String cascadingInfo = DocGenModelUtil.getTokenValue(styllinAttributes, DocGenModelUtil.CASCADING_INFO);
			if (!cascadingInfo.isBlank()) {
				if (DocGenModelUtil.CASCADE_OFF.equalsIgnoreCase(cascadingInfo)) {
					Node.setAttribute(sectionsNode, "numbering", DocGenModelUtil.CASCADE_OFF);
					cascadeDoclevelFlag = DocGenModelUtil.CASCADE_NEW;
				} else {
					Node.setAttribute(sectionsNode, "numbering", DocGenModelUtil.CASCADE_NEW);
				}
			} else {
				Node.setAttribute(sectionsNode, "numbering", DocGenModelUtil.CASCADE_NEW);
			}
		}
		return cascadeDoclevelFlag;
	}

	private static void latestTemplateGenerateInputXML(int inputXMLNode, String cascadeDoclevelFlag, int templateNode,
			int sectionsNode, String contractID) {
		int allContainerNodes[] = null;
		allContainerNodes = Utilities.getNodeList(".//ContainingClauses", templateNode);
		Map<Integer, AbstractGCContainerModel> allContainerNodesMap = new HashMap<Integer, AbstractGCContainerModel>();
		Map<Integer, Boolean> ruleResultsMap = getRuleResultsMap(Integer.valueOf(contractID));
		for (int containerNode : allContainerNodes) {
			AbstractGCContainerModel gcContainerModel = createCCModel(containerNode);
			if (Objects.nonNull(gcContainerModel)) {
				allContainerNodesMap.put(gcContainerModel.getId(), gcContainerModel);
			}
			if (Objects.nonNull(ruleResultsMap) && ruleResultsMap.size() > 0 && gcContainerModel.getRuleId() > 0) {
				boolean ruleResult = ruleResultsMap.get(gcContainerModel.getRuleId());
				if ((DocGenModelUtil.CONDITIONAL_ACTION_HIDE.equalsIgnoreCase(gcContainerModel.getConditionAction())
						&& (Objects.nonNull(ruleResult) && ruleResult))) {
					gcContainerModel.setEligibleForDocument(false);
					continue;
				} else if ((DocGenModelUtil.CONDITIONAL_ACTION_REPLACE.equalsIgnoreCase(
						gcContainerModel.getConditionAction()) && (Objects.nonNull(ruleResult) && ruleResult))) {
					gcContainerModel.setEligibleForDocument(true);
				}
			}
			if (gcContainerModel instanceof GCClauseContainerModel) {
				replaceMetaValuesInHTMLContent((GCClauseContainerModel) gcContainerModel, inputXMLNode);
				replaceTermValuesInHTMLContent((GCClauseContainerModel) gcContainerModel, contractID);
			}

		}

		allContainerNodesMap.values().stream()
		.filter(c -> Objects.nonNull(c)
				&& DocGenModelUtil.CONDITIONAL_ACTION_REPLACE.equalsIgnoreCase(c.getConditionAction())
				&& c.isEligibleForDocument())
		.forEach(c -> {
			AbstractGCContainerModel model = allContainerNodesMap.get(c.getSourceContainerId());
			if (Objects.nonNull(c) && Objects.nonNull(model) && model instanceof GCClauseContainerModel
					&& c instanceof GCClauseContainerModel) {
				GCClauseContainerModel targetModel = (GCClauseContainerModel) model;
				GCClauseContainerModel sourceModel = (GCClauseContainerModel) c;
				targetModel.setHtmlContent(sourceModel.getHtmlContent());
				targetModel.setName(sourceModel.getName());
				c.setEligibleForDocument(false);
			}
		});

		// Base holder for all the containers. at level -1.
		GCSectionContainerModel root = new GCSectionContainerModel();
		root.setLevel(-1);
		root.setCascadingInfo(DocGenModelUtil.CASCADE_CONTINUE);
		root.setContentCascadingInfo(cascadeDoclevelFlag);
		for (AbstractGCContainerModel model : allContainerNodesMap.values()) {
			if (Objects.nonNull(model.getId()) && 0 != model.getId()) {
				if (Objects.nonNull(model.getParentId()) && 0 != model.getParentId()) {
					AbstractGCContainerModel parent = allContainerNodesMap.get(model.getParentId());
					if (Objects.nonNull(parent)) {
						parent.addChildModel(model);
					}
				} else {
					root.addChildModel(model);
				}
			}
		}

		sortAllChildContainersInTree(root);
		formSectionsAndClauses(root, sectionsNode);
	}

	private static Map<Integer, Boolean> getRuleResultsMap(int contractId) {
		Map<Integer, Boolean> ruleRulestMap = new HashMap<Integer, Boolean>();
		int readRuleResultsParams = 0, ruleResultsResponse = 0;
		try {
			SOAPRequestObject readTemplateDetailsRequest = new SOAPRequestObject(
					"http://schemas/OpenTextContractCenter/Contract.RuleResult/operations", "getContractRuleResults",
					null, null);
			readRuleResultsParams = NomUtil.parseXML("<contractId>" + contractId + "</contractId>");
			readTemplateDetailsRequest.addParameterAsXml(readRuleResultsParams);
			ruleResultsResponse = readTemplateDetailsRequest.sendAndWait();
			logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.INFO, null,
					"Initiating Get rule results web service");
			int ruleResults[] = null;
			ruleResults = NomUtil.getNodeList(".//RuleResult", ruleResultsResponse);
			for (int ruleResult : ruleResults) {
				String result = Node.getDataWithDefault(NomUtil.getNode(".//Result", ruleResult), null);
				String ruleId = Node.getDataWithDefault(NomUtil.getNode(".//Rule/Rule-id/Id", ruleResult), null);
				if (Objects.nonNull(ruleId) && !ruleId.isBlank()) {
					ruleRulestMap.put(Integer.valueOf(ruleId), Boolean.parseBoolean(result));
				}
			}
		} catch (Exception e) {
			logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.ERROR, e,
					ContractCenterAlertMessages.WEBSERVICE_FAILURE_GET_CUSTOM_ATTRIBUTES);
			throw new ContractCenterApplicationException(
					ContractCenterAlertMessages.WEBSERVICE_FAILURE_GET_CUSTOM_ATTRIBUTES);
		} finally {
			Utilities.cleanAll(readRuleResultsParams, ruleResultsResponse);
		}
		return ruleRulestMap;
	}

	private static void oldTemplatesGenerateInputXML(int inputXMLNode, int templateNode, int sectionsNode,
			String contractID) {
		int clauseNodes[] = null;
		clauseNodes = NomUtil.getNodeList(".//ContainingClauses", templateNode);
		int sectionNode = 0;
		TreeMap<Integer, TreeMap<Integer, Integer>> lSectionMap = new TreeMap<Integer, TreeMap<Integer, Integer>>();
		for (int clNode : clauseNodes) {
			String lSectionOrder = Node.getDataWithDefault(NomUtil.getNode(".//Owner/SectionOrder", clNode), null);
			String lClauseOrder = Node.getDataWithDefault(NomUtil.getNode(".//ClauseOrder", clNode), null);
			int clasOrder = (lClauseOrder == null || "".equals(lClauseOrder)) ? 0 : Integer.parseInt(lClauseOrder);
			int secOrder = (lSectionOrder == null || "".equals(lSectionOrder)) ? 0 : Integer.parseInt(lSectionOrder);
			TreeMap<Integer, Integer> lClauseMap = lSectionMap.get(secOrder);
			if (lClauseMap == null) {
				lClauseMap = new TreeMap<Integer, Integer>();
				int ownerNode = NomUtil.getNode(".//Owner", clNode);
				if (Node.isValidNode(ownerNode) && ownerNode != 0) {
					lSectionMap.put(secOrder, lClauseMap);
				}

			}
			lClauseMap.put(clasOrder, clNode);
		}
		Iterator<Entry<Integer, TreeMap<Integer, Integer>>> lSectIterator = lSectionMap.entrySet().iterator();
		while (lSectIterator.hasNext()) {
			TreeMap<Integer, Integer> lClausMap = lSectIterator.next().getValue();
			Iterator<Entry<Integer, Integer>> lClIterator = lClausMap.entrySet().iterator();
			while (lClIterator.hasNext()) {
				int lClauseNode = lClIterator.next().getValue();
				String clauseID = Node
						.getDataWithDefault(NomUtil.getNode(".//LinkedClause/GCClause-id/Id", lClauseNode), null);
				String sectionID = Node.getDataWithDefault(NomUtil.getNode(".//ContainingClauses-id/Id1", lClauseNode),
						null);
				String sectionOrder = Node.getDataWithDefault(NomUtil.getNode(".//Owner/SectionOrder", lClauseNode),
						null);
				String sectionName = Node.getDataWithDefault(NomUtil.getNode(".//Owner/Name", lClauseNode), null);
				String clauseName = Node.getDataWithDefault(NomUtil.getNode(".//LinkedClause/Name", lClauseNode), null);
				String clauseOrder = Node.getDataWithDefault(NomUtil.getNode(".//ClauseOrder", lClauseNode), null);
				String clauseContent = Node
						.getDataWithDefault(NomUtil.getNode(".//LinkedClause/PlainContent", lClauseNode), null);
				String htmlClauseContent = Node
						.getDataWithDefault(NomUtil.getNode(".//LinkedClause/HtmlContent", lClauseNode), null);
				if (clauseID != null) {
					int termReferenceResponse = 0;
					try {
						termReferenceResponse = getTermReferencesbyClauseID(clauseID);
						int termReferenceNodes[] = null;
						termReferenceNodes = NomUtil.getNodeList(".//TermReference", termReferenceResponse);
						if (clauseContent != null) {
							for (int j : termReferenceNodes) {
								String termReferenceID = Node
										.getDataWithDefault(NomUtil.getNode(".//TermReference-id/Id1", j), null);
								String termID = Node
										.getDataWithDefault(NomUtil.getNode(".//ReferringTo/GCTerm-id/Id", j), null);
								String termToken = getTermTokenbyTermID(termID);
								String termValue = getTermInstanceValue(clauseOrder, sectionOrder, termReferenceID,
										clauseID, contractID);
								if (termToken != null && termValue != null) {
									clauseContent = clauseContent.replace(termToken, termValue);
									htmlClauseContent = htmlClauseContent.replace(termToken,
											StringEscapeUtils.escapeHtml4(termValue));
								}
							}

							// Parsing clause content to fetch contract metadata tokens
							Pattern pattern = Pattern.compile("\\[#+[a-zA-Z_]+#\\]");// Contract metadata
							// format
							// Search

							Matcher matcher = pattern.matcher(clauseContent);

							// Finds and replaces If atleast one token is found in clause content
							while (matcher.find()) {
								String contractmetadataToken = matcher.group();
								String contractmetadataName = contractmetadataToken
										.substring(2, contractmetadataToken.length() - 2).toUpperCase();
								String contractmetadataValue = Node.getAttribute(NomUtil.getNode(
										".//Attribute[@key='" + contractmetadataName + "']", inputXMLNode), "value");
								if (contractmetadataValue != null) {
									clauseContent = clauseContent.replace(contractmetadataToken, contractmetadataValue);
									htmlClauseContent = htmlClauseContent.replace(contractmetadataToken,
											StringEscapeUtils.escapeHtml4(contractmetadataValue));
								}
							}

							// This code should be altered according the requirement.
							htmlClauseContent = htmlClauseContent
									.replaceAll("style=\"font-family:", "style=\"text-family:")
									.replaceAll("style=\"font-size:", "style=\"text-size:");
						}

					} finally {
						if (termReferenceResponse != 0) {
							Utilities.cleanAll(termReferenceResponse);
						}
					}
				}
				sectionNode = NomUtil.getNode(".//Section[SectionID/text()='" + sectionID + "']", sectionsNode);
				if (!Utilities._isNull(sectionNode)) {
					addClausetoSection(clauseID, sectionNode, clauseName, clauseOrder, clauseContent,
							htmlClauseContent);
				} else {
					sectionNode = addNewSection(clauseID, sectionsNode, sectionID, sectionName, sectionOrder,
							clauseName, clauseOrder, clauseContent, htmlClauseContent);
				}
			}
		}
	}

	private static void addContractLinesToXML(String itemID, int inputXMLNode) {
		if (Objects.nonNull(itemID) && !itemID.isBlank()) {
			int readContractLinesResponse = getContractLines(
					Integer.parseInt(itemID.substring(itemID.indexOf(".") + 1)));
			if (Objects.nonNull(readContractLinesResponse)) {
				int contractLineNodes[] = null;
				int repeatableElements = NomUtil.getNode(".//RepeatableElements", inputXMLNode);
				if (repeatableElements == 0) {
					repeatableElements = Node.createElement("RepeatableElements", inputXMLNode);
				}
				int contractLinesNode = Node.createElement("ContractLines", repeatableElements);
				contractLineNodes = NomUtil.getNodeList(".//ContractLines/ContractLine", readContractLinesResponse);
				for (int contractLineDataNode : contractLineNodes) {
					addContractLinetoMappingNode(contractLineDataNode, contractLinesNode);
				}

			}
		}
	}

	private static int getContractLines(int contractID) {
		int readContractLinesResponse = 0;
		if (Objects.nonNull(contractID)) {
			int contractIDNode;
			contractIDNode = NOMDocumentPool.getInstance().createElement("ContractId");
			Node.setDataElement(contractIDNode, "", String.valueOf(contractID));
			try {
				SOAPRequestObject getContractLinesRequest = new SOAPRequestObject(
						"http://schemas.opentext.com/apps/contractcenter/18.4", "GetCTRLinesRelationsforDocGen", null,
						null);
				getContractLinesRequest.addParameterAsXml(contractIDNode);
				readContractLinesResponse = getContractLinesRequest.sendAndWait();

			} catch (Exception e) {
				logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.ERROR, e,
						"Error while trigerring GetContractLines Web service");
				throw new ContractCenterApplicationException(
						ContractCenterAlertMessages.ERROR_WHILE_TRIGGERING_WEBSERVICE, "GetContractLines");
			} finally {
				Utilities.cleanAll(contractIDNode);
			}
		}
		return readContractLinesResponse;
	}

	private static void addPONumbersToXML(String itemID, int inputXMLNode) {

		int contractId = Integer.parseInt(itemID.substring(itemID.indexOf(".") + 1));
		if (contractId !=0 && itemID != null) {
			int readPONumbersResponse = getPONumbers(contractId);

			if (Objects.nonNull(readPONumbersResponse)) {

				int poNumberNodes[] = null;
				int repeatableElements = NomUtil.getNode(".//RepeatableElements", inputXMLNode);
				if (repeatableElements == 0) {
					repeatableElements = Node.createElement("RepeatableElements", inputXMLNode);
				}
				int PONumbersNode = Node.createElement("PONumbers", repeatableElements);
				poNumberNodes = NomUtil.getNodeList(".//relatedPOs/PONumber", readPONumbersResponse);

				for (int poNumberDataNode : poNumberNodes) {
					addPONumbertoMappingNode(poNumberDataNode, PONumbersNode);
				}
			}
		}
	}

	private static int getPONumbers(int contractID) {

		int readPONumbersResponse = 0;
		if (Objects.nonNull(contractID)) {
			int contractIDNode;
			contractIDNode = NOMDocumentPool.getInstance().createElement("contractID");
			Node.setDataElement(contractIDNode, "", String.valueOf(contractID));
			try {
				SOAPRequestObject getPONumbersRequest = new SOAPRequestObject(
						"http://schemas.opentext.com/apps/contractcenter/16.3", "GetContractDetailsById", null,
						null);
				getPONumbersRequest.addParameterAsXml(contractIDNode);
				readPONumbersResponse = getPONumbersRequest.sendAndWait();

			} catch (Exception e) {
				logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.ERROR, e,
						"Error while trigerring GetContractLines Web service");
				throw new ContractCenterApplicationException(
						ContractCenterAlertMessages.ERROR_WHILE_TRIGGERING_WEBSERVICE, "GetPONumbers");
			} finally {
				Utilities.cleanAll(contractIDNode);
			}
		}
		return readPONumbersResponse;
	}

	private static void addPartiesToXML(String itemID, int inputXMLNode, int readContractResponse) {

		if (Objects.nonNull(itemID) && !itemID.isBlank()) {
			int contractId = Integer.parseInt(itemID.substring(itemID.indexOf(".") + 1));
			int partiesNodes[] = null;
			int contactsNodes[] = null;
			int partyDataNode = 0;
			int contactDataNode = 0;

			int readInternalPartiesResponse = getParties(contractId, GenerateSectionClauseConstants.INTERNAL_PARTY_TYPE);
			partiesNodes = NomUtil.getNodeList(".//FindZ_INT_CTRPartiesListResponse/CTRParties",
					readInternalPartiesResponse);

			//Cases where upgrade from older versions of CC have only one Internal party
				if (partiesNodes != null && partiesNodes.length > 0) {
					partyDataNode = partiesNodes[0];
					contactsNodes = NomUtil.getNodeList(".//CTRContacts/RelatedContacts", partyDataNode);
					
					if (contactsNodes != null && contactsNodes.length > 0) {
						contactDataNode = contactsNodes[0];
						if (partiesNodes.length <= 1 && contactsNodes.length <= 1) {
							addInternalPartyPropertiesToXML(partyDataNode, contactDataNode, inputXMLNode);
						} 
					}
			}
			createPartiesNode(readInternalPartiesResponse, inputXMLNode, "InternalParties", partiesNodes);

			int readExternalPartiesResponse = getParties(contractId, GenerateSectionClauseConstants.EXTERNAL_PARTY_TYPE);
			partiesNodes = NomUtil.getNodeList(".//FindZ_INT_CTRPartiesListResponse/CTRParties", readExternalPartiesResponse);
			
			//Cases where upgrade from older versions of CC have only External party
				if(partiesNodes != null && partiesNodes.length > 0) {				
					partyDataNode = partiesNodes[0];
					contactsNodes = NomUtil.getNodeList(".//CTRContacts/RelatedContacts", partyDataNode);
					
					if(contactsNodes != null && contactsNodes.length > 0) {
						contactDataNode = contactsNodes[0];
						if(partiesNodes.length <= 1 && contactsNodes.length <= 1) {
							addExternalPartyPropertiesToXML(partyDataNode, contactDataNode, inputXMLNode);
						}
					}

			}
			createPartiesNode(readExternalPartiesResponse, inputXMLNode, "ExternalParties", partiesNodes);

		}
	}

	private static void addInternalPartyPropertiesToXML(int partyDataNode, int contactDataNode, int inputXMLNode) {

		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//RelatedParty/Description", partyDataNode),
						null),
				GenerateSectionClauseConstants.CONTRACT_FIRSTPARTY_DESCRIPTION.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//RelatedParty/Email", partyDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_FIRSTPARTY_EMAIL.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(
						NomUtil.getNode(".//RelatedParty/IdentificationNumber", partyDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_FIRSTPARTY_IDENTIFICATIONNUMBER.toUpperCase());
		String firstPartyInceptionDate = getFormattedDate(Node.getDataWithDefault(
				NomUtil.getNode(".//RelatedParty/InceptionDate", partyDataNode), null));
		addParameter(inputXMLNode, firstPartyInceptionDate,
				GenerateSectionClauseConstants.CONTRACT_FIRSTPARTY_INCEPTIONDATE.toUpperCase());
		addParameter(inputXMLNode, Node
				.getDataWithDefault(NomUtil.getNode(".//RelatedParty/MasterID", partyDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_FIRSTPARTY_MASTERID.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//RelatedParty/Name", partyDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_FIRSTPARTY_NAME.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(
						NomUtil.getNode(".//RelatedParty/RegisteredName", partyDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_FIRSTPARTY_REGISTEREDNAME.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(
						NomUtil.getNode(".//RelatedParty/RegistrationID", partyDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_FIRSTPARTY_REGISTRATIONID.toUpperCase());
		addParameter(inputXMLNode, Node
				.getDataWithDefault(NomUtil.getNode(".//RelatedParty/Website", partyDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_FIRSTPARTY_WEBSITE.toUpperCase());

		String firstPartyContactBirthDate = getFormattedDate(Node.getDataWithDefault(
				NomUtil.getNode(".//ContainingPerson/Birthdate", contactDataNode), null));
		addParameter(inputXMLNode, firstPartyContactBirthDate,
				GenerateSectionClauseConstants.CONTRACT_FIRSTPARTYCONTACT_BIRTHDATE.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(
						NomUtil.getNode(".//ContainingPerson/DisplayName", contactDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_FIRSTPARTYCONTACT_DISPLAYNAME.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//ContainingPerson/Email", contactDataNode),
						null),
				GenerateSectionClauseConstants.CONTRACT_FIRSTPARTYCONTACT_EMAIL.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(
						NomUtil.getNode(".//ContainingPerson/FirstName", contactDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_FIRSTPARTYCONTACT_FIRSTNAME.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(
						NomUtil.getNode(".//ContainingPerson/LastName", contactDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_FIRSTPARTYCONTACT_LASTNAME.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(
						NomUtil.getNode(".//ContainingPerson/Mobile", contactDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_FIRSTPARTYCONTACT_MOBILE.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//ContainingPerson/Phone", contactDataNode),
						null),
				GenerateSectionClauseConstants.CONTRACT_FIRSTPARTYCONTACT_PHONE.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(
						NomUtil.getNode(".//ContainingPerson/Prefix", contactDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_FIRSTPARTYCONTACT_PREFIX.toUpperCase());

	}

	private static void addExternalPartyPropertiesToXML(int partyDataNode, int contactDataNode, int inputXMLNode) {
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//RelatedParty/Description", partyDataNode),
						null),
				GenerateSectionClauseConstants.CONTRACT_SECONDPARTY_DESCRIPTION.toUpperCase());
		addParameter(inputXMLNode, Node
				.getDataWithDefault(NomUtil.getNode(".//RelatedParty/Email", partyDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_SECONDPARTY_EMAIL.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(
						NomUtil.getNode(".//RelatedParty/IdentificationNumber", partyDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_SECONDPARTY_IDENTIFICATIONNUMBER.toUpperCase());
		String secondPartyInceptionDate = getFormattedDate(Node.getDataWithDefault(
				NomUtil.getNode(".//RelatedParty/InceptionDate", partyDataNode), null));
		addParameter(inputXMLNode, secondPartyInceptionDate,
				GenerateSectionClauseConstants.CONTRACT_SECONDPARTY_INCEPTIONDATE.toUpperCase());
		addParameter(inputXMLNode, Node
				.getDataWithDefault(NomUtil.getNode(".//RelatedParty/MasterID", partyDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_SECONDPARTY_MASTERID.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(NomUtil.getNode(".//RelatedParty/Name", partyDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_SECONDPARTY_NAME.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(
						NomUtil.getNode(".//RelatedParty/RegisteredName", partyDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_SECONDPARTY_REGISTEREDNAME.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(
						NomUtil.getNode(".//RelatedParty/RegistrationID", partyDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_SECONDPARTY_REGISTRATIONID.toUpperCase());
		addParameter(inputXMLNode, Node
				.getDataWithDefault(NomUtil.getNode(".//RelatedParty/Website", partyDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_SECONDPARTY_WEBSITE.toUpperCase());

		String secondPartyContactBirthDate = getFormattedDate(Node.getDataWithDefault(
				NomUtil.getNode(".//ContainingPerson/Birthdate", contactDataNode), null));
		addParameter(inputXMLNode, secondPartyContactBirthDate,
				GenerateSectionClauseConstants.CONTRACT_SECONDPARTYCONTACT_BIRTHDATE.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(
						NomUtil.getNode(".//ContainingPerson/DisplayName", contactDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_SECONDPARTYCONTACT_DISPLAYNAME.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(
						NomUtil.getNode(".//ContainingPerson/Email", contactDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_SECONDPARTYCONTACT_EMAIL.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(
						NomUtil.getNode(".//ContainingPerson/FirstName", contactDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_SECONDPARTYCONTACT_FIRSTNAME.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(
						NomUtil.getNode(".//ContainingPerson/LastName", contactDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_SECONDPARTYCONTACT_LASTNAME.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(
						NomUtil.getNode(".//ContainingPerson/Mobile", contactDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_SECONDPARTYCONTACT_MOBILE.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(
						NomUtil.getNode(".//ContainingPerson/Phone", contactDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_SECONDPARTYCONTACT_PHONE.toUpperCase());
		addParameter(inputXMLNode,
				Node.getDataWithDefault(
						NomUtil.getNode(".//ContainingPerson/Prefix", contactDataNode), null),
				GenerateSectionClauseConstants.CONTRACT_SECONDPARTYCONTACT_PREFIX.toUpperCase());

		int[] collAccNodes = NomUtil.getNodeList(".//CollectionAccount", partyDataNode);
		if (collAccNodes.length <= 1) {
			int collAccData = collAccNodes.length > 0 ? collAccNodes[0]: 0;
			addParameter(inputXMLNode,
					Node.getDataWithDefault(NomUtil.getNode(".//AccountNumber", collAccData), null),
					GenerateSectionClauseConstants.CONTRACT_SECONDPARTY_ACCOUNTNUMBER.toUpperCase());
			addParameter(inputXMLNode,
					Node.getDataWithDefault(NomUtil.getNode(".//ContainingPerson/DisplayName", collAccData), null),
					GenerateSectionClauseConstants.CONTRACT_SECONDPARTY_ACCOUNTMANAGER.toUpperCase());
		}
	}

	private static void createPartiesNode(int readPartiesResponse, int inputXMLNode, String nodeType, int[] partiesNodes) {

		if (Objects.nonNull(readPartiesResponse)) {


			int repeatableElements = NomUtil.getNode(".//RepeatableElements", inputXMLNode);
			if (repeatableElements == 0) {
				repeatableElements = Node.createElement("RepeatableElements", inputXMLNode);
			}
			int partiesNode = Node.createElement(nodeType, repeatableElements);

			for (int partyDataNode : partiesNodes) {
				addPartytoMappingNode(partyDataNode, partiesNode, nodeType);
			}
		}
	}

	private static int getParties(int contractID, String partyType) {
		int readPartiesResponse = 0;
		if (Objects.nonNull(contractID)) {
			int contractIDNode, partyTypeNode;
			contractIDNode = NOMDocumentPool.getInstance().createElement("contractID");
			partyTypeNode = NOMDocumentPool.getInstance().createElement("partyType");
			Node.setDataElement(contractIDNode, "", String.valueOf(contractID));
			Node.setDataElement(partyTypeNode, "", partyType);
			try {
				SOAPRequestObject getPartiesRequest = new SOAPRequestObject(
						"http://schemas.opentext.com/apps/contractcenter/16.3", "getPartieswithFilters", null,
						null);
				getPartiesRequest.addParameterAsXml(contractIDNode);
				getPartiesRequest.addParameterAsXml(partyTypeNode);
				readPartiesResponse = getPartiesRequest.sendAndWait();

			} catch (Exception e) {
				logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.ERROR, e,
						"Error while trigerring GetContractLines Web service");
				throw new ContractCenterApplicationException(
						ContractCenterAlertMessages.ERROR_WHILE_TRIGGERING_WEBSERVICE, "getMultipleParties");
			} finally {
				Utilities.cleanAll(contractIDNode);
			}
		}
		return readPartiesResponse;
	}

	private static void sortAllChildContainersInTree(AbstractGCContainerModel root) {
		if (Objects.nonNull(root)) {
			root.sortChildrenContainers();
			for (AbstractGCContainerModel model : root.getChildrenModels()) {
				if (model.getChildrenModels().size() > 0) {
					sortAllChildContainersInTree(model);
				}
			}
		}
	}

	private static void replaceMetaValuesInHTMLContent(GCClauseContainerModel gcContainerModel, int mappingNode) {
		if (Objects.nonNull(gcContainerModel) && Objects.nonNull(gcContainerModel.getContent())) {
			// Parsing clause content to fetch contract metadata tokens
			Pattern pattern = Pattern.compile("\\[#+[a-zA-Z_ ]+#\\]");// Contract metadata format Search
			Matcher matcher = pattern.matcher(gcContainerModel.getContent());

			// Finds and replaces If at least one token is found in clause content
			while (matcher.find()) {
				String contractmetadataToken = matcher.group();
				String contractmetadataName = contractmetadataToken.substring(2, contractmetadataToken.length() - 2)
						.toUpperCase();
				String contractmetadataValue = Node.getAttribute(
						NomUtil.getNode(".//Attribute[@key='" + contractmetadataName + "']", mappingNode), "value");
				if (contractmetadataValue != null) {
					gcContainerModel.setContent(
							gcContainerModel.getContent().replace(contractmetadataToken, contractmetadataValue));
					gcContainerModel.setHtmlContent(gcContainerModel.getHtmlContent().replace(contractmetadataToken,
							StringEscapeUtils.escapeHtml4(contractmetadataValue)));
				}

			}
		}
	}

	private static void replaceTermValuesInHTMLContent(GCClauseContainerModel gcContainerModel, String contractID) {
		int termReferenceResponse = 0;

		if (Objects.nonNull(gcContainerModel) && Objects.nonNull(gcContainerModel.getContent())
				&& Objects.nonNull(gcContainerModel.getHtmlContent())) {
			try {
				termReferenceResponse = getTermReferencesbyClauseID(String.valueOf(gcContainerModel.getClauseId()));
				int termReferenceNodes[] = null;
				termReferenceNodes = NomUtil.getNodeList(".//TermReference", termReferenceResponse);
				for (int j : termReferenceNodes) {
					String termReferenceID = Node.getDataWithDefault(NomUtil.getNode(".//TermReference-id/Id1", j),
							null);
					String termID = Node.getDataWithDefault(NomUtil.getNode(".//ReferringTo/GCTerm-id/Id", j), null);
					String termToken = getTermTokenbyTermID(termID);
					String termValue = getTermInstanceValue(String.valueOf(gcContainerModel.getOrder()), "0",
							termReferenceID, String.valueOf(gcContainerModel.getClauseId()), contractID);
					if (Objects.nonNull(termToken) && Objects.nonNull(termToken)) {
						gcContainerModel.setContent(gcContainerModel.getContent().replace(termToken, termValue));
						gcContainerModel.setHtmlContent(gcContainerModel.getHtmlContent().replace(termToken,
								StringEscapeUtils.escapeHtml4(termValue)));
					}
				}

				// This code should be altered according the requirement.
				gcContainerModel.setHtmlContent(
						gcContainerModel.getHtmlContent().replaceAll("style=\"font-family:", "style=\"text-family:")
						.replaceAll("style=\"font-size:", "style=\"text-size:"));
			} catch (Exception e) {

			}
		}
	}

	private static void formSectionsAndClauses(AbstractGCContainerModel root, int sectionsNode) {
		if (Objects.nonNull(root)) {
			for (AbstractGCContainerModel model : root.getChildrenModels()) {
				int containerNode = 0;

				// Checking rule results.
				if (!model.isEligibleForDocument())
					continue;

				if (model instanceof GCSectionContainerModel) {
					containerNode = addSectionToXML((GCSectionContainerModel) model, sectionsNode);
				} else if (model instanceof GCClauseContainerModel) {
					containerNode = addClauseToXML((GCClauseContainerModel) model, sectionsNode);
				}
				if (containerNode != 0 && model.getChildrenModels().size() > 0) {
					formSectionsAndClauses(model, containerNode);
				}
			}
		}
	}

	private static int addSectionToXML(GCSectionContainerModel model, int parentNode) {
		int sectionNode = 0;
		if (Objects.nonNull(model)) {
			sectionNode = Node.createElement("Section", parentNode);
			Node.setAttribute(sectionNode, "level", String.valueOf(model.getLevel()));
			int sectionIDNode = Node.createElement("SectionID", sectionNode);
			Node.setDataElement(sectionIDNode, "", String.valueOf(model.getSectionId()));
			int sectionNameNode = Node.createElement("SectionName", sectionNode);
			Node.setDataElement(sectionNameNode, "", model.getName());
			int sectionOrderNode = Node.createElement("Order", sectionNode);
			Node.setDataElement(sectionOrderNode, "", String.valueOf(model.getOrder()));
			if (Objects.nonNull(model.getNumberingStyle()) && !model.getNumberingStyle().isBlank()) {
				Node.setAttribute(sectionNode, "class", model.getNumberingStyle());
			}
			if (Objects.nonNull(model.getCascadingInfo()) && !model.getCascadingInfo().isBlank()) {
				Node.setAttribute(sectionNode, "numbering", model.getCascadingInfo());
			}
			int stylingAttributes = Node.createElement("StylingAttributes", sectionNode);
			Node.setDataElement(stylingAttributes, "", model.getStyleingAttributes());
		}
		return sectionNode;
	}

	private static int addClauseToXML(GCClauseContainerModel model, int parentNode) {
		int clauseNode = 0;
		if (Objects.nonNull(model) && Objects.nonNull(model.getClauseId()) && model.getClauseId() != 0) {
			clauseNode = Node.createElement("Clause", parentNode);
			Node.setAttribute(clauseNode, "level", String.valueOf(model.getLevel()));
			int clauseNameNode = Node.createElement("ClauseName", clauseNode);
			Node.setDataElement(clauseNameNode, "", model.getName());
			int clauseOrderNode = Node.createElement("Order", clauseNode);
			Node.setDataElement(clauseOrderNode, "", String.valueOf(model.getOrder()));
			int clauseContentNode = Node.createElement("ClauseContent", clauseNode);
			Node.setDataElement(clauseContentNode, "", model.getContent());
			int htmlClauseContentNode = Node.createElement("RTEClauseContent", clauseNode);
			Node.setCDataElement(htmlClauseContentNode, "",
					(Objects.nonNull(model.getHtmlContent())) ? model.getHtmlContent() : "");
			Node.setAttribute(htmlClauseContentNode, "datatype", "html");
			if (Objects.nonNull(model.getNumberingStyle()) && !model.getNumberingStyle().isBlank()) {
				Node.setAttribute(clauseNode, "class", model.getNumberingStyle());
			}
			if (Objects.nonNull(model.getCascadingInfo()) && !model.getCascadingInfo().isBlank()) {
				Node.setAttribute(clauseNode, "numbering", model.getCascadingInfo());
			}
			if (Objects.nonNull(model.getContentCascadingInfo()) && !model.getContentCascadingInfo().isBlank()) {
				Node.setAttribute(htmlClauseContentNode, "numbering", model.getContentCascadingInfo());
			}

			int stylingAttributes = Node.createElement("StylingAttributes", clauseNode);
			Node.setDataElement(stylingAttributes, "", model.getStyleingAttributes());
		}
		return clauseNode;
	}

	private static AbstractGCContainerModel createCCModel(int containerNode) {
		AbstractGCContainerModel gcContainerModel = null;

		String linkedSectionID = Node
				.getDataWithDefault(NomUtil.getNode(".//LinkedSection/ContainingSections-id/Id1", containerNode), null);
		if (Objects.nonNull(linkedSectionID) && !linkedSectionID.isBlank()) {
			gcContainerModel = createSectionContainer(containerNode);
		}

		if (Objects.isNull(gcContainerModel)) {
			String linkedClauseID = Node
					.getDataWithDefault(NomUtil.getNode(".//LinkedClause/GCClause-id/Id", containerNode), null);
			if (Objects.nonNull(linkedClauseID) && !linkedClauseID.isBlank()) {
				gcContainerModel = createClauseContainer(containerNode);
			}
		}

		if (Objects.nonNull(gcContainerModel)) {

			// Container id.
			String containerId = Node.getDataWithDefault(NomUtil.getNode(".//ContainingClauses-id/Id2", containerNode),
					null);
			if (Objects.nonNull(containerId) && !containerId.isBlank()) {
				gcContainerModel.setId(Integer.parseInt(containerId));
			}

			// Parent container id.
			String parentContianerId = Node.getDataWithDefault(
					NomUtil.getNode(".//ParentContainer/ContainingClauses-id/Id2", containerNode), null);
			if (Objects.nonNull(parentContianerId) && !parentContianerId.isBlank()) {
				gcContainerModel.setParentId(Integer.parseInt(parentContianerId));
			}
			String styleingAttributes = Node.getDataWithDefault(NomUtil.getNode(".//StylingAttributes", containerNode),
					null);
			gcContainerModel.setStyleingAttributes(styleingAttributes);
		}
		return gcContainerModel;
	}

	private static AbstractGCContainerModel createSectionContainer(int containerNode) {
		GCSectionContainerModel model = new GCSectionContainerModel();
		String tempNodeValue = Node
				.getDataWithDefault(NomUtil.getNode(".//LinkedSection/ContainingSections-id/Id1", containerNode), null);
		if (Objects.nonNull(tempNodeValue) && !tempNodeValue.isBlank()) {
			model.setSectionId(Integer.parseInt(tempNodeValue));
		}
		tempNodeValue = Node.getDataWithDefault(NomUtil.getNode(".//ClauseOrder", containerNode), null);
		if (Objects.nonNull(tempNodeValue) && !tempNodeValue.isBlank()) {
			model.setOrder(Integer.parseInt(tempNodeValue));
		}
		model.setName(Node.getDataWithDefault(NomUtil.getNode(".//LinkedSection/Name", containerNode), null));
		addConditionalClauseContent(model, containerNode);
		return model;
	}

	private static GCClauseContainerModel createClauseContainer(int containerNode) {
		GCClauseContainerModel model = new GCClauseContainerModel();
		String tempNodeValue = Node.getDataWithDefault(NomUtil.getNode(".//LinkedClause/GCClause-id/Id", containerNode),
				null);
		if (Objects.nonNull(tempNodeValue) && !tempNodeValue.isBlank()) {
			model.setClauseId(Integer.parseInt(tempNodeValue));
		}
		tempNodeValue = Node.getDataWithDefault(NomUtil.getNode(".//ClauseOrder", containerNode), null);
		if (Objects.nonNull(tempNodeValue) && !tempNodeValue.isBlank()) {
			model.setOrder(Integer.parseInt(tempNodeValue));
		}
		model.setName(Node.getDataWithDefault(NomUtil.getNode(".//LinkedClause/Name", containerNode), null));
		model.setContent(Node.getDataWithDefault(NomUtil.getNode(".//LinkedClause/PlainContent", containerNode), null));
		model.setHtmlContent(
				Node.getDataWithDefault(NomUtil.getNode(".//LinkedClause/HtmlContent", containerNode), null));
		addConditionalClauseContent(model, containerNode);
		return model;
	}

	private static void addConditionalClauseContent(AbstractGCContainerModel model, int containerNode) {
		if (Objects.nonNull(model)) {
			String conditionalAction = Node
					.getDataWithDefault(NomUtil.getNode(".//RelatedCondition/Action", containerNode), null);
			if (Objects.nonNull(conditionalAction) && !conditionalAction.isBlank()) {
				switch (conditionalAction.toUpperCase()) {
				case DocGenModelUtil.CONDITIONAL_ACTION_HIDE:
					model.setEligibleForDocument(true);
					model.setConditionAction(conditionalAction.toUpperCase());
					break;
				case DocGenModelUtil.CONDITIONAL_ACTION_REPLACE:
				case DocGenModelUtil.CONDITIONAL_ACTION_ADD_AFTER:
				case DocGenModelUtil.CONDITIONAL_ACTION_ADD_BEFORE:
					model.setEligibleForDocument(false);
					model.setConditionAction(conditionalAction.toUpperCase());
					break;
				}
				String tempNodeValue = Node.getDataWithDefault(
						NomUtil.getNode(".//RelatedCondition/SourceContainer/ContainingClauses-id/Id2", containerNode),
						null);
				if (Objects.nonNull(tempNodeValue) && !tempNodeValue.isBlank()) {
					model.setSourceContainerId(Integer.parseInt(tempNodeValue));
				}
				tempNodeValue = Node.getDataWithDefault(
						NomUtil.getNode(".//RelatedCondition/TargetContainer/ContainingClauses-id/Id2", containerNode),
						null);
				if (Objects.nonNull(tempNodeValue) && !tempNodeValue.isBlank()) {
					model.setTargetContainerId(Integer.parseInt(tempNodeValue));
				}
				tempNodeValue = Node.getDataWithDefault(
						NomUtil.getNode(".//RelatedCondition/ConditionRule/Rule-id/Id", containerNode), null);
				if (Objects.nonNull(tempNodeValue) && !tempNodeValue.isBlank()) {
					model.setRuleId(Integer.parseInt(tempNodeValue));
				}
			}
		}

	}

	private static void addContractLinetoMappingNode(int contractLineDataNode, int contractLinesNode) {
		if (contractLineDataNode != 0) {
			int contractLineNode = Node.createElement("ContractLine", contractLinesNode);
			int serviceNameNode = Node.createElement("ServiceName", contractLineNode);
			NomUtil.setData(serviceNameNode,
					Node.getDataWithDefault(NomUtil.getNode(".//ServicesName", contractLineDataNode), null));
			int productGroupNode = Node.createElement("ProductGroup", contractLineNode);
			NomUtil.setData(productGroupNode,
					Node.getDataWithDefault(NomUtil.getNode(".//ProductGroupName", contractLineDataNode), null));
			int skuNode = Node.createElement("SKU", contractLineNode);
			NomUtil.setData(skuNode,
					Node.getDataWithDefault(NomUtil.getNode(".//SKUOrServiceId", contractLineDataNode), null));
			int priceNode = Node.createElement("Price", contractLineNode);
			NomUtil.setData(priceNode,
					Node.getDataWithDefault(NomUtil.getNode(".//Price", contractLineDataNode), null));
			int quantityNode = Node.createElement("Quantity", contractLineNode);
			NomUtil.setData(quantityNode,
					Node.getDataWithDefault(NomUtil.getNode(".//Quantity", contractLineDataNode), null));
			int UnitNameNode = Node.createElement("Unit", contractLineNode);
			NomUtil.setData(UnitNameNode,
					Node.getDataWithDefault(NomUtil.getNode(".//UnitOfMeasurementName", contractLineDataNode), null));
			int PONumberNode = Node.createElement("PONumber", contractLineNode);
			NomUtil.setData(PONumberNode,
					Node.getDataWithDefault(NomUtil.getNode(".//PONumber", contractLineDataNode), null));
		}
	}

	private static void addPONumbertoMappingNode(int poNumberDataNode, int poNumbersNode) {
		if (poNumberDataNode != 0) {
			int poNumberNode = Node.createElement("PONumber", poNumbersNode);
			int numberNode = Node.createElement("number", poNumberNode);
			NomUtil.setData(numberNode,
					Node.getDataWithDefault(NomUtil.getNode(".", poNumberDataNode), null));
		}
	}

	private static void addPartytoMappingNode(int partyDataNode, int partiesNode, String nodeType) {
		if (partyDataNode != 0) {
			int partyNode;
			if(nodeType.equals(GenerateSectionClauseConstants.EXTERNAL_PARTY_NODE_TYPE)) {				
				partyNode = Node.createElement("ExternalParty", partiesNode);
			}else {
				partyNode = Node.createElement("InternalParty", partiesNode);
			}
			Node.setAttribute(partyNode, "Accounts", "true");

			int partyNameNode = Node.createElement("Name", partyNode);
			int partyRegisteredNameNode = Node.createElement("RegisteredName", partyNode);
			int partyRegistrationIDNode = Node.createElement("RegistrationID", partyNode);
			int partyWebsiteNode = Node.createElement("Website", partyNode);
			int partyMasterIDNode = Node.createElement("MasterID", partyNode);
			int partyInceptionDateNode = Node.createElement("InceptionDate", partyNode);
			int partyIdentificationNumberNode = Node.createElement("IdentificationNumber", partyNode);
			int partyEmailNode = Node.createElement("Email", partyNode);
			int partyDescriptionNode = Node.createElement("Description", partyNode);

			NomUtil.setData(partyNameNode,
					Node.getDataWithDefault(NomUtil.getNode(".//RelatedParty/Name", partyDataNode), null));
			NomUtil.setData(partyRegisteredNameNode,
					Node.getDataWithDefault(NomUtil.getNode(".//RelatedParty/RegisteredName", partyDataNode), null));
			NomUtil.setData(partyRegistrationIDNode,
					Node.getDataWithDefault(NomUtil.getNode(".//RelatedParty/RegistrationID", partyDataNode), null));
			NomUtil.setData(partyWebsiteNode,
					Node.getDataWithDefault(NomUtil.getNode(".//RelatedParty/Website", partyDataNode), null));
			NomUtil.setData(partyMasterIDNode,
					Node.getDataWithDefault(NomUtil.getNode(".//RelatedParty/MasterID", partyDataNode), null));
			NomUtil.setData(partyInceptionDateNode,
					getFormattedDate(Node.getDataWithDefault(NomUtil.getNode(".//RelatedParty/InceptionDate", partyDataNode), null)));

			NomUtil.setData(partyIdentificationNumberNode,
					Node.getDataWithDefault(NomUtil.getNode(".//RelatedParty/IdentificationNumber", partyDataNode), null));
			NomUtil.setData(partyEmailNode,
					Node.getDataWithDefault(NomUtil.getNode(".//RelatedParty/Email", partyDataNode), null));
			NomUtil.setData(partyDescriptionNode,
					Node.getDataWithDefault(NomUtil.getNode(".//RelatedParty/Description", partyDataNode), null));

			int[] contactsNodes = NomUtil.getNodeList(".//CTRContacts/RelatedContacts", partyDataNode);
			int[] collAccNodes = NomUtil.getNodeList(".//CollectionAccount", partyDataNode);

			for (int contactDataNode : contactsNodes) {
				addContactstoPartyNode(contactDataNode, partyNode, nodeType);
			}

			if(nodeType.equals(GenerateSectionClauseConstants.EXTERNAL_PARTY_NODE_TYPE)) {				
				for (int collAccDataNode : collAccNodes) {
					addCollectionAcctoPartyNode(collAccDataNode, partyNode);
				}
			}

		}
	}

	private static void addContactstoPartyNode(int contactDataNode, int partyNode, String nodeType) {

		if (contactDataNode != 0) {
			int contactNode;
			if(nodeType.equals(GenerateSectionClauseConstants.EXTERNAL_PARTY_NODE_TYPE)) {				
				contactNode = Node.createElement("ExternalContact", partyNode);
			}else {
				contactNode = Node.createElement("InternalContact", partyNode);
			}
			Node.setAttribute(contactNode, "Accounts", "true");

			int contBirthdateNode = Node.createElement("BirthDate", contactNode);
			int contDisplaynameNode = Node.createElement("DisplayName", contactNode);
			int contEmailNode = Node.createElement("EmailAddress", contactNode);
			int contFirstnameIDNode = Node.createElement("FirstName", contactNode);
			int contLastnameNode = Node.createElement("LastName", contactNode);
			int contMobileNode = Node.createElement("Mobile", contactNode);
			int contPhoneNode = Node.createElement("Phone", contactNode);
			int contPrefixNode = Node.createElement("Prefix", contactNode);

			NomUtil.setData(contBirthdateNode,
					getFormattedDate(Node.getDataWithDefault(NomUtil.getNode(".//ContainingPerson/Birthdate", contactDataNode), null)));
			NomUtil.setData(contDisplaynameNode,
					Node.getDataWithDefault(NomUtil.getNode(".//ContainingPerson/DisplayName", contactDataNode), null));
			NomUtil.setData(contEmailNode,
					Node.getDataWithDefault(NomUtil.getNode(".//ContainingPerson/Email", contactDataNode), null));
			NomUtil.setData(contFirstnameIDNode,
					Node.getDataWithDefault(NomUtil.getNode(".//ContainingPerson/FirstName", contactDataNode), null));
			NomUtil.setData(contLastnameNode,
					Node.getDataWithDefault(NomUtil.getNode(".//ContainingPerson/LastName", contactDataNode), null));
			NomUtil.setData(contMobileNode,
					Node.getDataWithDefault(NomUtil.getNode(".//ContainingPerson/Mobile", contactDataNode), null));
			NomUtil.setData(contPhoneNode,
					Node.getDataWithDefault(NomUtil.getNode(".//ContainingPerson/Phone", contactDataNode), null));
			NomUtil.setData(contPrefixNode,
					Node.getDataWithDefault(NomUtil.getNode(".//ContainingPerson/Prefix", contactDataNode), null));
		}
	}

	private static void addCollectionAcctoPartyNode(int collAccDataNode, int partyNode) {

		if (collAccDataNode != 0) {
			int contactNode = Node.createElement("Account", partyNode);
			Node.setAttribute(contactNode, "Accounts", "true");

			int contBirthdateNode = Node.createElement("AccountNumber", contactNode);
			int contDisplaynameNode = Node.createElement("AccountManager", contactNode);

			NomUtil.setData(contBirthdateNode,
					Node.getDataWithDefault(NomUtil.getNode(".//AccountNumber", collAccDataNode), null));
			NomUtil.setData(contDisplaynameNode,
					Node.getDataWithDefault(NomUtil.getNode(".//ContainingPerson/DisplayName", collAccDataNode), null));
		}
	}

	private static String getFormattedDate(String dateString) {
		String l_dateString = "";
		if (dateString != null) {
			Date parsedDate = getParsedDate(dateString);
			l_dateString = (new SimpleDateFormat("MMMM dd, yyyy")).format(parsedDate);
		}
		return l_dateString;
	}

	private static Date getParsedDate(String dateString) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date parsedDate = null;
		try {
			parsedDate = sdf.parse(dateString);
		} catch (Exception e) {
			logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.ERROR, e,
					ContractCenterAlertMessages.PARSING_FAILURE_DATE);
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.PARSING_FAILURE_DATE);
		}
		return parsedDate;
	}

	private static String getTermTokenbyTermID(String termID) {
		int termIDNode = 0, readTermResponse = 0;
		try {
			SOAPRequestObject readTermRequest = new SOAPRequestObject(
					"http://schemas/OpenTextBasicComponents/GCTerm/operations", "ReadGCTerm", null, null);
			termIDNode = NOMDocumentPool.getInstance().createElement("GCTerm-id");
			Node.createTextElement("Id", termID, termIDNode);
			readTermRequest.addParameterAsXml(termIDNode);
			readTermResponse = readTermRequest.sendAndWait();
			String lTermToken = Node.getDataWithDefault(NomUtil.getNode(".//GCTerm/TermToken", readTermResponse), null);
			logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.INFO, null,
					"Initiating ReadGCTerm web service");
			return lTermToken;
		} catch (Exception e) {
			logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.ERROR, e,
					ContractCenterAlertMessages.WEBSERVICE_FAILURE_READ_GC_TERM);
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.WEBSERVICE_FAILURE_READ_GC_TERM);
		} finally {
			Utilities.cleanAll(termIDNode, readTermResponse);
		}
	}

	private static String getTermInstanceValue(String clauseOrder, String sectionOrder, String termReferenceID,
			String clauseID, String contractID) {
		int clauseOrderNode = 0, sectionOrderNode = 0, termReferenceIDNode = 0, clauseIDNode = 0, contractIDNode = 0,
				readTermInstanceValueResponse = 0;
		try {
			SOAPRequestObject readTermInstanceValueRequest = new SOAPRequestObject(
					"http://schemas/OpenTextContractCenter/Contract.TermInstance/operations",
					"GetTermInstbyCLAandSECorder", null, null);

			clauseOrderNode = NOMDocumentPool.getInstance().createElement("clauseOrder");
			NomUtil.setData(clauseOrderNode, clauseOrder);
			sectionOrderNode = NOMDocumentPool.getInstance().createElement("sectionOrder");
			NomUtil.setData(sectionOrderNode, sectionOrder);
			termReferenceIDNode = NOMDocumentPool.getInstance().createElement("termReferenceID");
			NomUtil.setData(termReferenceIDNode, termReferenceID);
			clauseIDNode = NOMDocumentPool.getInstance().createElement("clauseID");
			NomUtil.setData(clauseIDNode, clauseID);
			contractIDNode = NOMDocumentPool.getInstance().createElement("contractID");
			NomUtil.setData(contractIDNode, contractID);
			readTermInstanceValueRequest.addParameterAsXml(clauseOrderNode);
			readTermInstanceValueRequest.addParameterAsXml(sectionOrderNode);
			readTermInstanceValueRequest.addParameterAsXml(termReferenceIDNode);
			readTermInstanceValueRequest.addParameterAsXml(clauseIDNode);
			readTermInstanceValueRequest.addParameterAsXml(contractIDNode);
			readTermInstanceValueResponse = readTermInstanceValueRequest.sendAndWait();
			String lTermValue = Node
					.getDataWithDefault(NomUtil.getNode(".//TermInstance/Value", readTermInstanceValueResponse), null);
			logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.INFO, null,
					"Initiating GetTermInstbyCLAandSECorder web service");
			return lTermValue;
		} catch (Exception e) {
			logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.ERROR, e,
					ContractCenterAlertMessages.WEBSERVICE_FAILURE_GET_TERM_INST_BY_CLA_AND_SEC_ORDER);
			throw new ContractCenterApplicationException(
					ContractCenterAlertMessages.WEBSERVICE_FAILURE_GET_TERM_INST_BY_CLA_AND_SEC_ORDER);
		} finally {
			Utilities.cleanAll(clauseOrderNode, sectionOrderNode, termReferenceIDNode, clauseIDNode, contractIDNode,
					readTermInstanceValueResponse);
		}
	}

	private static int getTermReferencesbyClauseID(String clauseID) {
		int clauseIDNode = 0, readTermReferenceResponse = 0;
		try {
			SOAPRequestObject readTermReferenceRequest = new SOAPRequestObject(
					"http://schemas/OpenTextContentLibrary/GCClause/operations", "GetTermReference", null, null);
			clauseIDNode = NOMDocumentPool.getInstance().createElement("GCClause-id");
			Node.createTextElement("Id", clauseID, clauseIDNode);
			readTermReferenceRequest.addParameterAsXml(clauseIDNode);
			readTermReferenceResponse = readTermReferenceRequest.sendAndWait();
			logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.INFO, null,
					"Initiating GetTermReference web service");
			return readTermReferenceResponse;
		} catch (Exception e) {
			logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.ERROR, e,
					ContractCenterAlertMessages.WEBSERVICE_FAILURE_GET_TERM_REFERENCE);
			throw new ContractCenterApplicationException(
					ContractCenterAlertMessages.WEBSERVICE_FAILURE_GET_TERM_REFERENCE);
		} finally {
			Utilities.cleanAll(clauseIDNode);
		}
	}

	private static int readRelationships(int readContractResponse) {
		int readRelationshipsResponse = 0, contractId = 0;
		if (Node.getDataWithDefault(NomUtil.getNode(".//Contract-id/Id", readContractResponse), null) != null) {
			contractId = NomUtil.parseXML("<ContractID xmlns=\"http://schemas/OpenTextContractCenter/18.4\">"
					+ Node.getDataWithDefault(NomUtil.getNode(".//Contract-id/Id", readContractResponse), null)
					+ "</ContractID>");
		}
		try {
			SOAPRequestObject readContractRelationships = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/contractcenter/18.4", "GetCTRRelationsforDocGen", null, null);
			readContractRelationships.addParameterAsXml(contractId);
			readRelationshipsResponse = readContractRelationships.sendAndWait();
			logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.INFO, null,
					"Initiating GetCTRRelationsforDocGen web service");
			return readRelationshipsResponse;
		} catch (Exception e) {
			logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.ERROR, e,
					ContractCenterAlertMessages.WEBSERVICE_FAILURE_GET_CONTRACT_RELATIONS_FOR_DOCGEN);
			throw new ContractCenterApplicationException(
					ContractCenterAlertMessages.WEBSERVICE_FAILURE_GET_CONTRACT_RELATIONS_FOR_DOCGEN);
		} finally {
			Utilities.cleanAll(contractId);
		}
	}

	private static void addClausetoSection(String clauseID, int sectionNode, String clauseName, String clauseOrder,
			String clauseContent, String htmlClauseContent) {
		if (clauseID != null) {
			int clauseNode = Node.createElement("Clause", sectionNode);// NomUtil.parseXML("<Clause></Clause>");
			Node.setAttribute(clauseNode, "level", "1");
			int clauseNameNode = Node.createElement("ClauseName", clauseNode);
			NomUtil.setData(clauseNameNode, clauseName);
			int clauseOrderNode = Node.createElement("ClauseOrder", clauseNode);
			NomUtil.setData(clauseOrderNode, clauseOrder);
			int clauseContentNode = Node.createElement("ClauseContent", clauseNode);
			NomUtil.setData(clauseContentNode, clauseContent);
			int htmlClauseContentNode = Node.createElement("RTEClauseContent", clauseNode);
			Node.setAttribute(clauseNode, "numbering", DocGenModelUtil.CASCADE_CONTINUE);
			if (htmlClauseContent != null) {
				NomUtil.setCData(htmlClauseContentNode, htmlClauseContent);
				Node.setAttribute(htmlClauseContentNode, "datatype", "html");
				Node.setAttribute(htmlClauseContentNode, "numbering", DocGenModelUtil.CASCADE_NEW);
			}
			// return clauseNode;
		}
	}

	private static int addNewSection(String clauseID, int sectionsNode, String sectionID, String sectionName,
			String sectionOrder, String clauseName, String clauseOrder, String clauseContent,
			String htmlClauseContent) {
		int sectionNode = Node.createElement("Section", sectionsNode);
		Node.setAttribute(sectionNode, "level", "0");
		int sectionIDNode = Node.createElement("SectionID", sectionNode);
		NomUtil.setData(sectionIDNode, sectionID);
		int sectionNameNode = Node.createElement("SectionName", sectionNode);
		NomUtil.setData(sectionNameNode, sectionName);
		int sectionOrderNode = Node.createElement("SectionOrder", sectionNode);
		NomUtil.setData(sectionOrderNode, sectionOrder);
		Node.setAttribute(sectionNode, "numbering", DocGenModelUtil.CASCADE_CONTINUE);
		addClausetoSection(clauseID, sectionNode, clauseName, clauseOrder, clauseContent, htmlClauseContent);
		return sectionNode;
	}

	private static void addParameter(int inputXMLNode, String value, String key) {
		int attributeNode = Node.createElement("Attribute", inputXMLNode);
		Node.setAttribute(attributeNode, "key", key);
		if (value != null) {
			Node.setAttribute(attributeNode, "value", value);
		} else {
			Node.setAttribute(attributeNode, "value", "");
		}
	}

	public static int readTemplate(String ItemID) {
		int templateIDNode = 0, readTemplateResponse = 0;
		try {
			SOAPRequestObject readTemplateDetailsRequest = new SOAPRequestObject(
					"http://schemas/OpenTextContentLibrary/16.5", "GetTemplateDetails", null, null);
			templateIDNode = NOMDocumentPool.getInstance().createElement("TemplateItemId");
			NomUtil.setData(templateIDNode, ItemID);
			readTemplateDetailsRequest.addParameterAsXml(templateIDNode);
			readTemplateResponse = readTemplateDetailsRequest.sendAndWait();
			logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.INFO, null,
					"Initiating GetTemplateDetails web service");
			return readTemplateResponse;
		} catch (Exception e) {
			logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.ERROR, e,
					ContractCenterAlertMessages.WEBSERVICE_FAILURE_GET_TEMPLATE_DETAILS);
			throw new ContractCenterApplicationException(
					ContractCenterAlertMessages.WEBSERVICE_FAILURE_GET_TEMPLATE_DETAILS);
		} finally {
			Utilities.cleanAll(templateIDNode);
		}
	}

	public static BusObjectIterator<com.opentext.apps.cc.custom.GenerateSectionClauseInputXML> getGenerateSectionClauseInputXMLObjects(
			com.cordys.cpc.bsf.query.Cursor cursor) {
		// TODO implement body
		return null;
	}

	public void onInsert() {
	}

	public void onUpdate() {
	}

	public void onDelete() {
	}

	private static int readCustomAttributes(String contractItemId) {
		int readContractRequestParams = 0, customAttributesResponse = 0;
		try {
			SOAPRequestObject readTemplateDetailsRequest = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/contractcenter/16.3", "GetMappedCustomAttributes", null, null);
			readContractRequestParams = NomUtil.parseXML("<ContractItemId>" + contractItemId + "</ContractItemId>");
			readTemplateDetailsRequest.addParameterAsXml(readContractRequestParams);
			customAttributesResponse = readTemplateDetailsRequest.sendAndWait();
			logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.INFO, null,
					"Initiating GetMappedCustomAttributes web service");
			return customAttributesResponse;
		} catch (Exception e) {
			logger._log("com.opentext.apps.cc.custom.GenerateSectionClauseInputXML", Severity.ERROR, e,
					ContractCenterAlertMessages.WEBSERVICE_FAILURE_GET_CUSTOM_ATTRIBUTES);
			throw new ContractCenterApplicationException(
					ContractCenterAlertMessages.WEBSERVICE_FAILURE_GET_CUSTOM_ATTRIBUTES);
		} finally {
			Utilities.cleanAll(readContractRequestParams);
		}
	}

	private static void addCustomAttributesToXML(String contractItemId, int inputXMLNode) {
		int customAttributesResponse = readCustomAttributes(contractItemId);
		int customAttributes[] = null;
		try {
			customAttributes = NomUtil.getNodeList(".//FindZ_INT_RelatedAttributesListResponse/RelatedAttributes",
					customAttributesResponse);
			for (int customAttributeNode : customAttributes) {
				String attributeName = Node.getDataWithDefault(NomUtil.getNode(".//Name", customAttributeNode), null);
				String attributeValue = Node.getDataWithDefault(NomUtil.getNode(".//Value", customAttributeNode), null);
				String attributeMedadata = Node
						.getDataWithDefault(NomUtil.getNode(".//AttributeMetaData", customAttributeNode), null);
				String dataType = Node.getDataWithDefault(NomUtil.getNode(".//DataType", customAttributeNode), null);
				if ("DATE".equalsIgnoreCase(dataType)) {
					String dateValue = "";
					String outputDateFormat = "";

					if (Objects.nonNull(attributeMedadata) && !attributeMedadata.isBlank()
							&& attributeMedadata.contains("\"dateformat\":\"")) {
						outputDateFormat = attributeMedadata.split("\"dateformat\":\"")[1];
						outputDateFormat = outputDateFormat.substring(0, outputDateFormat.indexOf("\""));
						outputDateFormat = getDateFormat(outputDateFormat);
					}
					if (!"Y-MM-dd".equals(outputDateFormat)) {
						SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
						SimpleDateFormat myFormat = new SimpleDateFormat(outputDateFormat);
						dateValue = myFormat.format(inputFormat.parse(attributeValue));

					} else {
						dateValue = attributeValue;
					}
					addParameter(inputXMLNode, dateValue, attributeName.toUpperCase());
				} else if ("BOOLEAN".equalsIgnoreCase(dataType)) {
					addParameter(inputXMLNode, "true".equalsIgnoreCase(attributeValue) ? "True" : "False",
							attributeName.toUpperCase());
				} else {
					addParameter(inputXMLNode, attributeValue, attributeName.toUpperCase());
				}

			}
		} catch (Exception e) {
			// Not throwing any exception, because document generation should be
			// proceeded even there in an error in custom attributes.
		}
	}

	private static String getDateFormat(String dateFormInput) {
		String dateFormat = "MM/dd/Y";
		if (Objects.nonNull(dateFormInput)) {
			switch (dateFormInput) {
			case "dd-M-yy":
				dateFormat = "dd-MMM-Y";
				break;
			case "dd-mm-yy":
				dateFormat = "dd-MM-Y";
				break;
			case "mm-dd-yy":
				dateFormat = "MM-dd-Y";
				break;
			case "M-dd-yy":
				dateFormat = "MMM-dd-Y";
				break;
			case "yy-mm-dd":
				dateFormat = "Y-MM-dd";
				break;
			}
		}
		return dateFormat;
	}
}
