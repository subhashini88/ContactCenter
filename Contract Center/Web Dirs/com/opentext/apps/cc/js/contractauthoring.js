$.cordys.json.defaults.removeNamespacePrefix = true;
var contractTypeID;
var clauserelatedId;
var binding_cancel_counter = 0;
var offsetValue = 0;
var limitValue = 10;
var eqOffsetValue = 0;
var eqLimitValue = 10;
var extContactsOffsetValue = 0;
var extContactsLimitValue = 5;
var clauselibrary_is_last_page_clicked = 0;
var clauselibrary_is_first_page_clicked = 0;
var ext_contacts_is_last_page_clicked = 0;
var ext_contacts_is_first_page_clicked = 0;
var srcsecorder;
var trgsecorder;
var srcclorder = null;
var srcsecorder_clause;
var srcclorder_clause;
var srcsecIndex;
var srcclauseIndex;
var clauseselectedarray = [];
var templateItemId = null;
var RTEIds = [];
var editor;
var eqSrcSectionIndex;
var eqSrcClauseIndex;
var sourceClauseIdforEq;
var is_eqlast_page_clicked = 0;
var is_eqfirst_page_clicked = 0;
var trackingStack = [];
var isDirty = false;
var contractItemId;
var doItemRefresh = false;
var termRelatedStatus;
const _TRUE = 'true';
const _FALSE = 'false';

//New variables for drag-drop sections and clauses
var dragSectionOrder;
var dropSectionOrder;
var dragClauseOrder;
var dropClauseOrder;



var l_current_numberingFormat = _DEFAULT_NUMBERING_FORMAT;



var TermsListModel = function () {
	var self = this;
	self.TermsList = ko.observableArray([]);
	self.currentPage = ko.observable(1);
	self.total_terms_count = ko.observable('');
	self.onCheckboxValueChanged = function (iItem, event) {
		var l_checked = event.currentTarget.checked;
		if (l_checked) {
			tokenObject[iItem["GCTerm-id"].Id] = iItem.TermToken;
		}
		else {
			tokenObject[iItem["GCTerm-id"].Id] = "";
		}
	}
	self.onCheckAllValueChanged = function (iItem, event) {
		var l_checked = event.currentTarget.checked;
		if (l_checked) {
			iItem.TermsList().forEach(function (iToken) {
				tokenObject[iToken["GCTerm-id"].Id] = iToken.TermToken;
			});
		}
		else {
			tokenObject = {};
		}
	}
}
var ClauseListModel = function () {
	self = this;
	this.Listed_Clauses = ko.observableArray([]);
	this.currentPage = ko.observable(1);
	this.clauses_selected_count = ko.observable('');
	this.total_clauses_count = ko.observable();
}
var EquivalentClauseModel = function () {
	self = this;
	this.equivalentClauseList = ko.observableArray([]);
	this.currentPage = ko.observable(1);
	this.equivalentClauseCount = ko.observable();
}




var l_contract_rule_result = new Map();
var l_sectionandclause_model = new SectionsAndClausesDataModel();
var l_terms_model = new TermsListModel();
var clause_list_model = new ClauseListModel();
var eq_clause_list_model = new EquivalentClauseModel();




l_sectionandclause_model.DeletedClauses.Clauses = {};
l_sectionandclause_model.DeletedClauses.Clauses.Clause = [];
l_sectionandclause_model.DeletedSections.Sections = {};
l_sectionandclause_model.DeletedSections.Sections.Section = [];

function translateOnLoad() {
	if (document.getElementById("CA_Section_name") != null)
		document.getElementById("CA_Section_name").placeholder = getTranslationMessage("Section name");
	if (document.getElementById("CA_Section_name2") != null)
		document.getElementById("CA_Section_name2").placeholder = getTranslationMessage("Section name");
	if (document.getElementById("myInput") != null)
		document.getElementById("myInput").placeholder = getTranslationMessage("Search by clause name");
	if (document.getElementById("eqClauseSearchInput") != null)
		document.getElementById("eqClauseSearchInput").placeholder = getTranslationMessage("Search by clause name");
	if (document.getElementById("id_searchElement") != null)
		document.getElementById("id_searchElement").placeholder = getTranslationMessage("Search by name");
	if (document.getElementById("message-to-send") != null)
		document.getElementById("message-to-send").placeholder = getTranslationMessage("Type your comment");
	if (document.getElementById("id_searchExtContactsInput") != null)
		document.getElementById("id_searchExtContactsInput").placeholder = getTranslationMessage("Search by name");
	if (document.getElementById("CA_AddNegComments") != null)
		document.getElementById("CA_AddNegComments").title = getTranslationMessage("Add negotiation comments");
	if (document.getElementById("CA_ViewNegComments") != null)
		document.getElementById("CA_ViewNegComments").title = getTranslationMessage("View negotiation comments");
	if (document.getElementById("CA_NonStandardClause") != null)
		document.getElementById("CA_NonStandardClause").title = getTranslationMessage("Add non standard clause");
	if (document.getElementById("CA_MoveUp") != null)
		document.getElementById("CA_MoveUp").title = getTranslationMessage("Move up");
	if (document.getElementById("CA_MoveDown") != null)
		document.getElementById("CA_MoveDown").title = getTranslationMessage("Move down");
	if (document.getElementById("CA_DeleteSection") != null)
		document.getElementById("CA_DeleteSection").title = getTranslationMessage("Delete section");
	if (document.getElementById("CA_ViewNegComments2") != null)
		document.getElementById("CA_ViewNegComments2").title = getTranslationMessage("View negotiation comments");
	if (document.getElementById("CA_Preview") != null)
		document.getElementById("CA_Preview").title = getTranslationMessage("Preview");
	if (document.getElementById("CA_Replace") != null)
		document.getElementById("CA_Replace").title = getTranslationMessage("Replace");
	if (document.getElementById("CA_RefreshComments") != null)
		document.getElementById("CA_RefreshComments").title = getTranslationMessage("Refresh comments");
	if (document.getElementById("CA_AddNewUser") != null)
		document.getElementById("CA_AddNewUser").title = getTranslationMessage("Add new user");
	if (document.getElementById("number_style") != null)
		document.getElementById("number_style").title = getTranslationMessage("Number style");
	if (document.getElementById("decrease_Indent") != null)
		document.getElementById("decrease_Indent").title = getTranslationMessage("Decrease indent");
	if (document.getElementById("increase_Indent") != null)
		document.getElementById("increase_Indent").title = getTranslationMessage("Increase indent");
	if (document.getElementById("multilevel_ON") != null)
		document.getElementById("multilevel_ON").title = getTranslationMessage("Multilevel numbering: ON");
	if (document.getElementById("multilevel_OFF") != null)
		document.getElementById("multilevel_OFF").title = getTranslationMessage("Multilevel numbering: OFF");
	if (document.getElementById("sectionList") != null)
		document.getElementById("sectionList").title = getTranslationMessage("Navigate to section");
	if (document.getElementById("show_Clause_Lib") != null)
		document.getElementById("show_Clause_Lib").title = getTranslationMessage("Show clause library");
	if (document.getElementById("hide_Clause_Lib") != null)
		document.getElementById("hide_Clause_Lib").title = getTranslationMessage("Hide clause library");
	if (document.getElementById("view_All_Comments") != null)
		document.getElementById("view_All_Comments").title = getTranslationMessage("View all negotiation comments");
	if (document.getElementById("expand_All_Sections") != null)
		document.getElementById("expand_All_Sections").title = getTranslationMessage("Expand all sections");
	if (document.getElementById("collapse_All_Sections") != null)
		document.getElementById("collapse_All_Sections").title = getTranslationMessage("Collapse all sections");
	if (document.getElementById("view_all_nego_comments") != null)
		document.getElementById("view_all_nego_comments").title = getTranslationMessage("View all negotiation comments");
	if (document.getElementById("add_section") != null)
		document.getElementById("add_section").title = getTranslationMessage("Add section");
	if (document.getElementById("add_section1") != null)
		document.getElementById("add_section1").title = getTranslationMessage("Add section");
	if (document.getElementById("expand_allsections") != null)
		document.getElementById("expand_allsections").title = getTranslationMessage("Expand all sections");
	if (document.getElementById("collapse_allsections") != null)
		document.getElementById("collapse_allsections").title = getTranslationMessage("Collapse all sections");
	if (document.getElementById("edit_clause") != null)
		document.getElementById("edit_clause").title = getTranslationMessage("Edit clause");
	if (document.getElementById("cut_clause") != null)
		document.getElementById("cut_clause").title = getTranslationMessage("Cut clause");
	if (document.getElementById("move_clauseup") != null)
		document.getElementById("move_clauseup").title = getTranslationMessage("Move clause up");
	if (document.getElementById("move_clausedown") != null)
		document.getElementById("move_clausedown").title = getTranslationMessage("Move clause down");
	if (document.getElementById("remove_clause") != null)
		document.getElementById("remove_clause").title = getTranslationMessage("Remove clause");
	if (document.getElementById("replace_equi_clause") != null)
		document.getElementById("replace_equi_clause").title = getTranslationMessage("Replace with equivalent clause");
	if (document.getElementById("add_nego_comments") != null)
		document.getElementById("add_nego_comments").title = getTranslationMessage("Add negotiation comments");
	if (document.getElementById("view_nego_comments") != null)
		document.getElementById("view_nego_comments").title = getTranslationMessage("View negotiation comments");
	if (document.getElementById("view_nego_comments1") != null)
		document.getElementById("view_nego_comments1").title = getTranslationMessage("View negotiation comments");
	if (document.getElementById("multilevl_arabic") != null)
		document.getElementById("multilevl_arabic").title = getTranslationMessage("Multilevel Arabic numerals");
	if (document.getElementById("multilevl_lower_L") != null)
		document.getElementById("multilevl_lower_L").title = getTranslationMessage("Multilevel Lowercase letters");
	if (document.getElementById("multilevl_upper_L") != null)
		document.getElementById("multilevl_upper_L").title = getTranslationMessage("Multilevel Uppercase letters");
	if (document.getElementById("multilevl_lower_R") != null)
		document.getElementById("multilevl_lower_R").title = getTranslationMessage("Multilevel Lowercase Roman numerals");
	if (document.getElementById("multilevl_upper_R") != null)
		document.getElementById("multilevl_upper_R").title = getTranslationMessage("Multilevel Uppercase Roman numerals");
	translatePage();
}

$(document).ready(function () {
	REFRESH_CK_EDITOR_INSTANCE = true;
	//$('[src*="contractauthoring.htm"]', window.parent.parent.document).parent().css('padding-left', '0px');
	if (window.parent.parent) {
		authoringPageIframe = $('[src*="contractauthoring.htm"]', window.parent.parent.document);
		if (authoringPageIframe) {
			authoringPageIframe.css('border', 'none');
		}
	}
	createToastDiv();
	CKEDITOR.config.skin = 'moono-lisa';
	CKEDITOR.disableAutoInline = true;
	CKEDITOR.config.title = false;
	loggedUserID = getUrlParameterValue('userid', null, true);
	readContractandLoadSectionsandClauses();

	$(document).on('mouseenter', '.clause_Iterator', function () {
		// $(this).find(":button").show();
	}).on('mouseleave', '.clause_Iterator', function () {
		// $(this).find(":button").hide();
	});

	$(document).on('click', '#logoUpload-ok', function () {
		var imgURL = "../../../../../logo/" + $('#logoUpload-input').val();
		$("#contractLogo > img").attr('src', imgURL);
	});

	$('#sectionList').on('change', function (e) {
		if (e.originalEvent !== undefined) {
			selectSection($(this).val());

		}
	});
	//Check for Chrome browser and enable dragging for sections and clauses
	if (navigator.userAgent.search("Chrome") >= 0 && navigator.userAgent.search("Edge") < 0) {
		$(".dragSection").each(function (_index, obj) {
			obj.setAttribute("draggable", "true");
		});
		$(".dragClause").each(function (_index, obj) {
			obj.setAttribute("draggable", "true");
		});
	}
	attacheventstoCheckBox();
	ko.applyBindings(clause_list_model, document.getElementById("clausediv"));
	ko.applyBindings(eq_clause_list_model, document.getElementById("eqClauseList"));
	// ko.applyBindings(l_terms_model, document.getElementById("termsDiv"));
	ko.applyBindings(l_comments_model, document.getElementById("div_CommentsList"));
	ko.applyBindings(l_shared_users_model, document.getElementById("div_SharedUserList"));
	ko.applyBindings(l_externalContacts_model, document.getElementById("div_addNewUserforNegotiation"));
});


function _processContractRuleResult(contractId, callBackFunc) {
	callBackFunc();
	// $.cordys.ajax(
	// 	{
	// 		method: "UpdateAuthoringRuleResults",
	// 		namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
	// 		parameters:
	// 		{
	// 			"ContractId": contractId

	// 		},
	// 	}).done(function (data) {
	// 		callBackFunc();

	// 	}).fail(function (error) {
	// 		// loadDocPreviewLayout();
	// 	})
}


function attacheventstoCheckBox() {
	$("#ckbCheckAll").click(function () {
		$(".checkBoxClass").prop('checked', $(this).prop('checked'));
	});
}



function readContractandLoadSectionsandClauses() {
	l_sectionandclause_model.DeletedClauses.Clauses = {};
	l_sectionandclause_model.DeletedClauses.Clauses.Clause = [];
	l_sectionandclause_model.DeletedSections.Sections = {};
	l_sectionandclause_model.DeletedSections.Sections.Section = [];
	l_sectionandclause_model.clearcutPasteData();
	contractItemId = getUrlParameterValue("instanceId", null, true);

	$.cordys.ajax({
		method: "ReadContract",
		namespace: "http://schemas/OpenTextContractCenter/Contract/operations",
		parameters:
		{
			'Contract-id':
			{
				'ItemId': contractItemId
			}
		}
	}).done(function (data) {
		//externalPartyItemID = data.Contract["SecondParty"]["Party-id"].ItemId;
		termRelatedStatus = data.Contract.Z_INT_CTRInfo;
		if (getTextValue(data.Contract.StylingAttributes)) {
			var cascadeFlagFromContract = JSON.parse(getTextValue(data.Contract.StylingAttributes)).cascading;
			if (cascadeFlagFromContract && _CASCADE_OFF === cascadeFlagFromContract) {
				l_sectionandclause_model.cascadeDocLevel(_CASCADE_OFF);
			} else {
				l_sectionandclause_model.cascadeDocLevel(_CASCADE_ON);
			}
		}
		_processContractRuleResult(contractItemId.split(".")[1], () => {
			getContractRuleResultMap(contractItemId.split(".")[1], function () {
				loadSectionsAndClausesData(data);
			});
		});
	}).fail(function (error) {

	});
}


function getContractRuleResultMap(contractId, callBack) {
	$.cordys.ajax({
		method: "getContractRuleResults",
		namespace: "http://schemas/OpenTextContractCenter/Contract.RuleResult/operations",
		parameters:
		{
			'contractId': contractId,
		}
	}).done(function (data) {
		if (data.RuleResult && data.RuleResult.length > 0) {
			data.RuleResult.forEach(ele => {
				l_contract_rule_result.set(ele.Rule["Rule-id"].Id, ele.Result);
			});
		} else if (data.RuleResult) {
			l_contract_rule_result.set(data.RuleResult.Rule["Rule-id"].Id, data.RuleResult.Result);
		}
		callBack();
	}).fail(function (error) {

	});
}

function loadSectionsAndClausesData(contractData) {
	if (contractData.Contract.IsExecuted == "false" && contractData.Contract.ContractDocumentType == "INTERNALTEMPLATE") {
		if (contractData.Contract.RelatedTemplate == undefined) {
			$('#loadingMsg').css('display', 'none');
			$('#requestedContract').css('display', 'block');
		}
		else {
			templateItemId = contractData.Contract.RelatedTemplate["GCTemplate-id"].ItemId
			cInstanceId = getId(templateItemId);
			l_sectionandclause_model.DeletedClauses.Clauses.Clause.splice(0, l_sectionandclause_model.DeletedClauses.Clauses.Clause.length)
			if (cInstanceId) {
				SectionsAndClausesDataModel = $.cordys.ajax(
					{
						namespace: "http://schemas/OpenTextContentLibrary/16.5",
						method: "GetTemplateDetails",
						parameters:
						{
							"TemplateItemId": templateItemId
						},
						success: function (data) {
							if (data) {
								if(data.TemplateDetails.Details.GCTemplate.TemplateType == "Internal template"){
									$('#loadingMsg').css('display', 'none');
									$('#authoringPage').css('display', 'block');
								}
								else if(data.TemplateDetails.Details.GCTemplate.TemplateType == "Internal party document"){
									$('#loadingMsg').css('display', 'none');
									$('#internalPartyDocumentType').css('display', 'block');
								}
								else if(data.TemplateDetails.Details.GCTemplate.TemplateType == "External party document"){
									$('#loadingMsg').css('display', 'none');
									$('#externalPartyDocumentType').css('display', 'block');
								}
									var SCMappingsFromRead;
									if (!data.TemplateDetails.Details.GCTemplate.DefaultContainingSection) {
										_IS_NEW_SCHEMA = false;
									}
									l_sectionandclause_model.template_LifecycleState = data.TemplateDetails.Details.GCTemplate.Lifecycle.CurrentState;
									l_sectionandclause_model.template_Z_INT_Status = data.TemplateDetails.Details.GCTemplate.Z_INT_Status;
									l_sectionandclause_model.contract_LifecycleState = contractData.Contract.Lifecycle.CurrentState;
									l_sectionandclause_model.contract_Z_INT_Status(contractData.Contract.Z_INT_Status);
									contractTypeID = data.TemplateDetails.Details.GCTemplate.Type["GCType-id"].Id;
									var l_scmappings = data.TemplateDetails.ContainingClauses.ContainingClauses;
									// Stylling and numbering information.
									if (data.TemplateDetails.Details.GCTemplate.StylingAttributes && !data.TemplateDetails.Details.GCTemplate.StylingAttributes["@nil"]
										&& JSON.parse(data.TemplateDetails.Details.GCTemplate.StylingAttributes)) {
										l_current_numberingFormat = getNumberingStyle(JSON.parse(data.TemplateDetails.Details.GCTemplate.StylingAttributes).numberingStyle);
										var cascadeFlagFromTemplate = JSON.parse(data.TemplateDetails.Details.GCTemplate.StylingAttributes).cascading;
										if (cascadeFlagFromTemplate && _CASCADE_ON === cascadeFlagFromTemplate) {
											l_sectionandclause_model.cascadeDocLevel(_CASCADE_ON);
										} else {
											l_sectionandclause_model.cascadeDocLevel(_CASCADE_OFF);
										}

									}
									if (l_scmappings) {
										if (l_scmappings.length) {
											SCMappingsFromRead = l_scmappings;
										}
										else {
											SCMappingsFromRead = [];
											SCMappingsFromRead.push(l_scmappings);
										}
										l_sectionandclause_model.SCMappingsToBind.removeAll();
										_clearReplaceMap();
										document.getElementById("defaultContainer").style.display = "none";
										_populateContainerRuleResult(SCMappingsFromRead);
										_populateRootSectionsAndClausesAuthoring(l_sectionandclause_model.SCMappingsToBind, SCMappingsFromRead, data.TemplateDetails.Details.GCTemplate.DefaultContainingSection);
										if (binding_cancel_counter != 1) {
											l_sectionandclause_model.optionsCaption(undefined);
											ko.applyBindings(l_sectionandclause_model, document.getElementById("m_secandcls"));
											ko.applyBindings(l_sectionandclause_model, document.getElementById("div_sectionsAndClausesHeader"));
										}
									}
									else {
										if (l_sectionandclause_model.contract_LifecycleState == "Pre-Execution" || l_sectionandclause_model.contract_LifecycleState == "Negotiation" || l_sectionandclause_model.contract_LifecycleState == "Draft") {
											l_sectionandclause_model.SCMappingsToBind.removeAll();
											l_sectionandclause_model.optionsCaption(getTranslationMessage("No sections added"));
											document.getElementById("defaultContainer").style.display = "block";
											if (binding_cancel_counter != 1) {
												ko.applyBindings(l_sectionandclause_model, document.getElementById("m_secandcls"));
												ko.applyBindings(l_sectionandclause_model, document.getElementById("div_sectionsAndClausesHeader"));
											}
										} else {
											if(data.TemplateDetails.Details.GCTemplate.TemplateType == "Internal template"){
												document.getElementById("afterActiveStateContract").style.display = "block";
												document.getElementById("defaultContainer").style.display = "none";
												document.getElementById("m_secandcls").style.display = "none";
												document.getElementById("div_sectionsAndClausesHeader").style.display = "none";
											}
										}
									}
									if (doItemRefresh) {
										if (window.parent && window.parent.ot_App && window.parent.ot_App.itemManager && window.parent.ot_App.itemManager.refreshItemFromServer) {
											window.parent.ot_App.itemManager.refreshItemFromServer(contractItemId);
										}
										doItemRefresh = false;
									}
								
							}
							setTimeout(function () {
								$(".decimal-numbering").removeClass("decimal-numbering").addClass(l_current_numberingFormat + "-numbering");
								$(".decimal-numbering-ol-li").removeClass("decimal-numbering-ol-li").addClass(l_current_numberingFormat + "-numbering-ol-li");
								_loadCKInstanceOnLoad();
								translateOnLoad();
							}, 0);
							// Check for authorization and then load negotiation details.
							checkForUserRole();
						}
					});

			}
		}
	}
	else if (contractData.Contract.ContractDocumentType == "EXTERNALDOCUMENT") {
		$('#loadingMsg').css('display', 'none');
		$('#externalContract').css('display', 'block');
	}
	else if (contractData.Contract.IsExecuted == "true") {
		$('#loadingMsg').css('display', 'none');
		$('#executedContract').css('display', 'block');
	}
}

function _loadCKInstanceOnLoad() {
	_destroyAllCKInstances();
	loadCKEditor("default-div-foreditor");
	_initializeCKInstance(0, l_sectionandclause_model.SCMappingsToBind().length, l_sectionandclause_model.SCMappingsToBind());
}

function _populateContainerRuleResult(SCMappingsFromRead) {
	var conditionInstRes = cc_conditionauth_util.processConditionalCont(_populateConditionContData(SCMappingsFromRead), l_contract_rule_result);
	_populateVisibleContainers(conditionInstRes, SCMappingsFromRead);
	// SCMappingsFromRead.forEach(container => {
	// 	container.showContainer = _TRUE;
	// 	if (container.RelatedCondition) {
	// 		var ruleResult = l_contract_rule_result.get(container.RelatedCondition.ConditionRule["Rule-id"].Id);
	// 		if (ruleResult && ruleResult === _TRUE && container.RelatedCondition.Action === 'HIDE') {
	// 			container.showContainer = _FALSE;
	// 		}
	// 	}
	// });
}

function _populateVisibleContainers(conditionInstRes, allContainers) {
	allContainers.forEach(container => {
		container.showContainer = _TRUE;
		if (conditionInstRes.has(container["ContainingClauses-id"].Id2) && !conditionInstRes.get(container["ContainingClauses-id"].Id2)) {
			container.showContainer = _FALSE;
		}
	})
}

function _initializeCKInstance(index, len, container) {
	if (index >= len) {
		return;
	}
	var clauseOrsection = container[index];
	if (clauseOrsection.type === _CLAUSE && clauseOrsection.showContainer === _TRUE) {
		loadCKEditor(clauseOrsection.containerID + "_clauseHTMLContent");
	}
	var contianerlen = clauseOrsection.container().length;
	if (contianerlen > 0 && clauseOrsection.showContainer === _TRUE) {
		_initializeCKInstance(0, contianerlen, clauseOrsection.container());
	}
	_initializeCKInstance(index + 1, len, container);
}







function _prepareStdClause(l_listElement, l_sectionOrClause, l_clausesLength) {
	var l_newContClauseId = getNewContainingClauseId();
	var l_clauseName = l_listElement.name;
	var l_clauseHTMLContent = l_listElement.htmlcontent ? l_listElement.htmlcontent : "";
	var l_clauseID = l_listElement.id;
	var l_clauseItemID = l_listElement.itemid;
	var l_generatedClauseID = l_listElement.clauseid;
	var l_clause = {
		type: _CLAUSE,
		showContainer: _TRUE,
		action: _NEW_LINKED_CLAUSE,
		cascade: ko.observable(_getCascadeFlagInherit(true, l_sectionOrClause)),
		inherited: ko.observable(true),
		nonumbering: ko.observable(false),
		numberingStyle: ko.observable(""),
		parentContainerID: l_sectionOrClause.containerID ? l_sectionOrClause.containerID : "",
		parentContainer: l_sectionOrClause,
		containingClauseID: l_newContClauseId + "",
		containerID: l_newContClauseId,
		initialContainingClauseID: l_newContClauseId,
		sectionOrder: sectionList.selectedSectionOrder + "",
		clauseOrder: l_clausesLength + "",
		clauseID: l_clauseID + "",
		clauseItemID: l_clauseItemID + "",
		generatedClauseID: l_generatedClauseID + "",
		clauseName: ko.observable(l_clauseName + ""),
		clauseHTMLContent: l_clauseHTMLContent + "",
		isNew: true,
		isStandard: ko.observable(true),
		isDirty: ko.observable(true),
		showConvertToNonStandardBtn: ko.observable(true),
		showReplacewithEqClauseBtn: ko.observable(true),
		container: ko.observableArray([])
	};
	l_clause.sectionName = ko.computed(function () {
		return l_clause.clauseName();
	}, l_clause);
	return l_clause;
}

// section drag-drop functions

function dragstartli(event, element) {
	event.stopPropagation();
	event.dataTransfer.setData('text/plain', '');
	dragSectionOrder = element.getAttribute('sectionOrder');
	element.style.opacity = "0.4";
	dragClauseOrder = null;
}

function dragendli(event, element) {
	element.style.opacity = "1";
}

function dragenterli(event, _element) {
	event.stopPropagation();
	if (dragSectionOrder != null) {
		if (dragSectionOrder == _element.getAttribute('sectionOrder')) {
		}
		else {
			event.preventDefault();
		}
	}
}

function dragoverli(event, element) {
	event.stopPropagation();
	if (dragSectionOrder != null) {
		if (dragClauseOrder != null) {
			var l_section = ko.dataFor(element);
			if (dragSectionOrder == element.getAttribute('sectionOrder') && (+dragClauseOrder + 1) == l_section.clausesAndSections().length) {
			}
			else {
				event.preventDefault();
				element.style.border = "3px dotted green";
				element.placeholder = getTranslationMessage("Drop Here");
				return true;
			}
		}
		else {
			if (dragSectionOrder == element.getAttribute('sectionOrder')) {
			}
			else {
				event.preventDefault();
				element.style.border = "3px dotted green";
				element.placeholder = getTranslationMessage("Drop Here");
				return true;
			}
		}
	}
	element.style.border = "";
	element.placeholder = "none";
	return false;
}

function dragleaveli(event, element) {
	event.stopPropagation();
	element.style.border = "";
	element.placeholder = "none";
}

function ondropli(event, element) {
	event.preventDefault();
	event.stopPropagation();
	dropSectionOrder = (+element.getAttribute('sectionOrder'));
	var l_sections = l_sectionandclause_model.SCMappingsToBind();
	var l_targetSection = l_sections[dropSectionOrder];
	if ((!dragSectionOrder && !dropSectionOrder) || (!l_targetSection)) {
		element.style.border = "";
		element.placeholder = "none";
		dragSectionOrder = null;
		dragClauseOrder = null;
	}
	else if (l_targetSection) {
		var l_dragSectionOrder = (+dragSectionOrder);
		var l_dragSection = l_sections[l_dragSectionOrder];
		if (dragClauseOrder == null) {
			if (dropSectionOrder != l_dragSectionOrder) {
				isDirty = true;
				l_sectionandclause_model.SCMappingsToBind.splice(l_dragSectionOrder, 1);   //droptarget
				l_sectionandclause_model.SCMappingsToBind.splice(dropSectionOrder, 0, l_dragSection);
				if (l_dragSectionOrder < dropSectionOrder) {
					for (var i = l_dragSectionOrder; i <= dropSectionOrder; i++) {
						if (!l_sections[i].action) {
							l_sections[i].action = _UPDATE_SECTION;
						}
					}
				} else {
					for (var i = l_dragSectionOrder; i >= dropSectionOrder; i--) {
						if (!l_sections[i].action) {
							l_sections.action = _UPDATE_SECTION;
						}
					}
				}
				disableSaveandCancel(false);
			}
			selectSection(l_dragSection.containingSectionID, true, true);
		}
		else if (l_dragSection) {
			var l_dragClauseOrder = (+dragClauseOrder);
			dragClause = l_sections[l_dragSectionOrder].clausesAndSections()[l_dragClauseOrder];
			if (dragClause) {
				if (l_dragSection != l_targetSection || (1 + l_dragClauseOrder) != l_dragSection.clausesAndSections().length) {
					isDirty = true;
					dragClause.clauseName($("#" + dragClause.containingSectionID + "_" + dragClause.containingClauseID + "_clauseName").text());
					dragClause.clauseHTMLContent = CKEDITOR.instances[dragClause.initialContainingSectionID + "_" + dragClause.initialContainingClauseID + "_clauseHTMLContent"].getData();
					l_dragSection.clauses.splice(l_dragClauseOrder, 1);

					for (var i = l_dragClauseOrder; i < l_dragSection.clausesAndSections().length; i++) {
						if (!l_dragSection.clausesAndSections()[i].action) {
							l_dragSection.clausesAndSections()[i].action = _UPDATE_ORDER;
						}
					}

					if (l_dragSection == l_targetSection) {
						if (!dragClause.action) {
							dragClause.action = _UPDATE_ORDER;
						}
						l_targetSection.clausesAndSections.push(dragClause);
					}
					else {
						if (!dragClause.action || dragClause.action == _UPDATE_ORDER) {
							dragClause.action = _MOVE_CLAUSE_BETWEEN_SECTIONS;
						}
						else if (dragClause.action == _CONVERT_TO_NONSTANDARD) {
							dragClause.action = _MOVE_CLAUSE_BETWEEN_SECTIONS_AND_CONVERT_TO_NONSTANDARD;
						}
						else if (dragClause.action == _UPDATE_LINKED_CLAUSE) {
							dragClause.action = _NEW_LINKED_CLAUSE;
						}
						else if (dragClause.action == _UPDATE_NONSTANDARD_CLAUSE) {
							dragClause.action = _NEW_NONSTANDARD_CLAUSE;
						}

						destroyCkEditorInstance(dragClause.containerID);
						checkAndDeleteClause(dragClause);
						dragClause.containingSectionID = l_targetSection.containingSectionID;
						dragClause.isNew = true;
						dragClause.isDirty(true);
						l_targetSection.clausesAndSections.push(dragClause);
						var l_htmlContId = dragClause.containerID + '_clauseHTMLContent';
						loadCKEditor(l_htmlContId);
					}
				}
			}
			selectSection(l_targetSection.containingSectionID, true, true);
		}
		dragSectionOrder = null;
		dragClauseOrder = null;
	}
}



// clause drag-drop functions

function dragstartliclause(event, element) {
	event.dataTransfer.setData('text/plain', '');
	event.stopPropagation();
	dragSectionOrder = element.getAttribute('sectionOrder');
	dragClauseOrder = element.getAttribute('clauseOrder');
	element.style.opacity = "0.4";
}

function dragendliclause(event, element) {
	element.style.opacity = "1";
}

function dragenterliclause(event, _element) {
	event.stopPropagation();
	if (dragClauseOrder != null && dragSectionOrder != null) {
		if (dragSectionOrder.sectionOrder == _element.getAttribute('sectionOrder') && dragClauseOrder == _element.getAttribute('clauseOrder')) {
		}
		else {
			event.preventDefault();
		}
	}
}

function dragoverliclause(event, element) {
	event.stopPropagation();
	if (dragClauseOrder != null && dragSectionOrder != null) {
		if (dragSectionOrder == element.getAttribute('sectionOrder') && dragClauseOrder == element.getAttribute('clauseOrder')) {
		}
		else {
			event.preventDefault();
			element.style.border = "3px dotted green";
			element.placeholder = getTranslationMessage("Drop Here");
			return true;
		}
	}
	element.style.border = "";
	element.placeholder = "";
	return false;
}

function dragleaveliclause(event, element) {
	event.stopPropagation();
	element.style.border = "";
	element.placeholder = "none";
}

function ondropliclause(event, element) {
	event.preventDefault();
	event.stopPropagation();
	disableSaveandCancel(false);
	if (dragClauseOrder != null) {
		var l_dragClauseOrder = (+dragClauseOrder);
		var l_dragSectionOrder = (+dragSectionOrder);
		var dropSectionOrder = (+element.getAttribute('sectionOrder'));
		var dropClauseOrder = (+element.getAttribute('clauseOrder'));
		var l_targetSection = l_sectionandclause_model.SCMappingsToBind()[dropSectionOrder];
		if (l_dragClauseOrder != dropClauseOrder || l_dragSectionOrder != dropSectionOrder) {
			dragClause = l_sectionandclause_model.SCMappingsToBind()[l_dragSectionOrder].clausesAndSections()[l_dragClauseOrder];
			dragClause.clauseName($("#" + dragClause.containingSectionID + "_" + dragClause.containingClauseID + "_clauseName").text());
			dragClause.clauseHTMLContent = CKEDITOR.instances[dragClause.initialContainingSectionID + "_" + dragClause.initialContainingClauseID + "_clauseHTMLContent"].getData();
			var l_dragSection = l_sectionandclause_model.SCMappingsToBind()[l_dragSectionOrder];
			l_dragSection.clausesAndSections.splice(l_dragClauseOrder, 1);

			if (l_dragSectionOrder == dropSectionOrder) {
				l_targetSection.clausesAndSections.splice(dropClauseOrder, 0, dragClause);
				for (var i = Math.min(dropClauseOrder, l_dragClauseOrder); i <= Math.max(dropClauseOrder, l_dragClauseOrder); i++) {
					if (!l_targetSection.clausesAndSections()[i].action) {
						l_targetSection.clausesAndSections()[i].action = _UPDATE_ORDER;
					}
				}
			}
			else {
				for (var i = dropClauseOrder + 1; i < l_targetSection.clausesAndSections().length; i++) {
					if (!l_targetSection.clausesAndSections()[i].action) {
						l_targetSection.clausesAndSections()[i].action = _UPDATE_ORDER;
					}
				}
				for (var i = l_dragClauseOrder; i < l_dragSection.clausesAndSections().length; i++) {

					if (!l_dragSection.clausesAndSections()[i].action) {
						l_dragSection.clausesAndSections()[i].action = _UPDATE_ORDER;
					}
				}
				if (!dragClause.action || dragClause.action == _UPDATE_ORDER) {
					dragClause.action = _MOVE_CLAUSE_BETWEEN_SECTIONS;
				}
				else if (dragClause.action == _CONVERT_TO_NONSTANDARD) {
					dragClause.action = _MOVE_CLAUSE_BETWEEN_SECTIONS_AND_CONVERT_TO_NONSTANDARD;
				}
				else if (dragClause.action == _UPDATE_LINKED_CLAUSE) {
					dragClause.action = _NEW_LINKED_CLAUSE;
				}
				else if (dragClause.action == _UPDATE_NONSTANDARD_CLAUSE) {
					dragClause.action = _NEW_NONSTANDARD_CLAUSE;
				}

				destroyCkEditorInstance(dragClause.containerID);
				checkAndDeleteClause(dragClause);
				dragClause.containingSectionID = l_targetSection.containingSectionID;
				dragClause.isNew = true;
				dragClause.isDirty(true);
				l_targetSection.clausesAndSections.splice(dropClauseOrder, 0, dragClause);
				var l_htmlContId = dragClause.containerID + '_clauseHTMLContent';
				loadCKEditor(l_htmlContId);
			}
			isDirty = true;
		}
		selectSection(l_targetSection.containingSectionID);
	}
	element.style.border = "";
	element.placeholder = "none";
	dragClauseOrder = null;
	dragSectionOrder = null;
}


function onCancel() {
	$("#cancelModal").modal();
	$('button#cancelChanges').off("click");
	disableSaveandCancel(false);
	$('button#cancelChanges').on('click', function (_event) {
		binding_cancel_counter = 1;
		hideClauseLibrary();
		readContractandLoadSectionsandClauses();
		isDirty = false;
		l_sectionandclause_model.clearcutPasteData();
	});

}

function getAllAttributes(elem) {
	var attributes = elem.prop("attributes");
	var listOfAttr = "";
	if (elem.length) {
		$.each(attributes, function () {
			listOfAttr = listOfAttr + this.name + " = " + this.value + " ";
		});
	}
	return listOfAttr;
}
function updateSectionandClauseMapping() {
	if (!isDirty) {
		$("#no_changes_to_Save").modal();
		return isDirty;
	}
	// Checking required fields.....
	for (var k = 0; k < document.getElementsByClassName('sectionNameInput').length; k++) {
		if (document.getElementsByClassName('sectionNameInput')[k].value == '') {
			notifyError(getTranslationMessage("Provide a section name!!"), 3000); return;
		}
	}

	// Check term creation status.
	if (termRelatedStatus && termRelatedStatus.includes("TERMS:INPROGRESS")) {

		//Check latest status by reading contract again.
		$.cordys.ajax({
			method: "ReadContract",
			namespace: "http://schemas/OpenTextContractCenter/Contract/operations",
			parameters:
			{
				'Contract-id':
				{
					'ItemId': contractItemId
				}
			}
		}).done(function (data) {
			termRelatedStatus = data.Contract.Z_INT_CTRInfo;
			if (termRelatedStatus && termRelatedStatus.includes("TERMS:INPROGRESS")) {
				$("#terms_creation_inprogress").modal();
				return isDirty;
			} else {
				saveAuthoringContent();
			}
		}).fail(function (error) {

		});

	} else {
		saveAuthoringContent();
	}
}

function saveAuthoringContent() {
	if (l_sectionandclause_model && l_sectionandclause_model.SCMappingsToBind() && l_sectionandclause_model.SCMappingsToBind().length >= 0) {
		cInstanceId = getId(templateItemId);
		SCMappingsToUpdate = {};
		SCMappingsToUpdate.templateid = cInstanceId;
		SCMappingsToUpdate.templateitemid = templateItemId;
		sectionsTempToUpdate = [];
		_syncVisibleSourceToTargetContainer();
		_syncHiddenSourceToTargetContainer();
		for (var i = 0; i < l_sectionandclause_model.SCMappingsToBind().length; i++) {
			var l_updatedSection = l_sectionandclause_model.SCMappingsToBind()[i];
			if (l_updatedSection) {
				_prepareRequestForUpdateContainer(sectionsTempToUpdate, l_sectionandclause_model.SCMappingsToBind()[i], i);
			}
		}
		_prepareRequestForDeleted(sectionsTempToUpdate);
		SCMappingsToUpdate.Containers = sectionsTempToUpdate;
		var jsonStylingString = JSON.stringify({ "numberingStyle": l_current_numberingFormat, cascading: l_sectionandclause_model.cascadeDocLevel() });
		$.cordys.ajax({
			method: "SaveAuthoredContent",
			namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
			parameters: {
				"Containers": { 'Container': sectionsTempToUpdate },
				"templateItemID": templateItemId,
				"contractItemID": contractItemId,
				"StylingAttributes": jsonStylingString
			}
		}).done(function (_data) {
			doItemRefresh = true;
			successToast(3000, getTranslationMessage("Sections and their clauses are saved."));
			isDirty = false;
			RTEIds = [];
			for (instance in CKEDITOR.instances) {
				CKEDITOR.instances[instance].destroy(true);
			}
			$(".cke_top").remove();
			$("#successToast").css("top", "42px");
			disableSaveandCancel(true);
			binding_cancel_counter = 1;
			UpdateNegotiationInstance(getTextValue(_data.updatedTemplateID));
			readContractandLoadSectionsandClauses();
			//updateDefaultNumberingStyle();
		}).fail(function (error) {
			notifyError(getTranslationMessage("Error: {0}, while updating the Section & Clause mappings", [error]), 10000);
		});
	}
}

function _syncVisibleSourceToTargetContainer() {
	replaceContainersMap.forEach((val, key) => {
		if (val.replaceContainer.showContainer === _FALSE && (val.parentContainer.indexOf(val.replaceContainer) > -1)) {
			val.replaceContainer.clauseOrder = val.sourceContainer.clauseOrder;
			val.replaceContainer.action = val.sourceContainer.action;
			val.replaceContainer.isMovedToRoot = val.sourceContainer.isMovedToRoot;
			val.replaceContainer.parentContainerID = val.sourceContainer.parentContainerID;
		}
	});
}

function _syncHiddenSourceToTargetContainer() {
	replaceContainersMap.forEach((val, key) => {
		if (val.replaceContainer.showContainer === _TRUE && (val.parentContainer.indexOf(val.replaceContainer) > -1)) {
			val.replaceContainer.container().forEach(cont => {
				cont.parentContainerID = val.sourceContainer.containerID;
			});
		}
	});
}

//@TODO Remove this method
function _addReplaceContainerToSaveReq(sectionTempToUpdate) {
	replaceContainersMap.forEach((val, key) => {
		if (val.replaceContainer.showContainer === _TRUE && (val.parentContainer.indexOf(val.replaceContainer) > -1)) {
			val.sourceContainer.clauseOrder = val.replaceContainer.clauseOrder;
			val.sourceContainer.action = val.replaceContainer.action;
			val.sourceContainer.parentContainerID = val.replaceContainer.parentContainerID;
			clauseOrSection = _prepareClauseUpdateRequest(val.sourceContainer);
			sectionTempToUpdate.push(clauseOrSection);
		}
	});
}


function _prepareRequestForDeleted(sectionTempToUpdate) {
	if (l_sectionandclause_model.DeletedClauses.Clauses && l_sectionandclause_model.DeletedClauses.Clauses.Clause) {
		l_sectionandclause_model.DeletedClauses.Clauses.Clause.forEach((secOrClause, index) => {
			secOrClause.action = _DELETE_CONTAINER;
			_prepareRequestForUpdateContainer(sectionTempToUpdate, secOrClause, index, _DELETE_CONTAINER);
		});
	}
	if (l_sectionandclause_model.DeletedSections.Sections && l_sectionandclause_model.DeletedSections.Sections.Section) {
		l_sectionandclause_model.DeletedSections.Sections.Section.forEach((secOrClause, index) => {
			secOrClause.action = _DELETE_CONTAINER;
			_prepareRequestForUpdateContainer(sectionTempToUpdate, secOrClause, index, _DELETE_CONTAINER);
		});
	}
}


function _prepareReqForSecOrClause(sectionTempToUpdate, l_updatedSection, order) {
	/** if (l_updatedSection.showContainer === _FALSE) {return;} **/
	var clauseOrSection = null;
	if (l_updatedSection && l_updatedSection.type === _CLAUSE) {
		clauseOrSection = _prepareClauseUpdateRequest(l_updatedSection);
		_addReplaceHiddenSourceContainer(l_updatedSection, sectionTempToUpdate);
		_addHiddenReplaceContainer(l_updatedSection, sectionTempToUpdate);
	} else {
		clauseOrSection = _prepareSectionUpdateRequest(l_updatedSection);
	}
	if (order !== null && !isNaN(order)) {
		clauseOrSection.Order = order;
	}

	if (!(l_updatedSection.action === _DELETE_CONTAINER && replaceContainersMap.has(l_updatedSection.containerID)
		// && l_updatedSection.showContainer === _FALSE
	)) {
		sectionTempToUpdate.push(clauseOrSection);
	}

}


function _addReplaceHiddenSourceContainer(l_updatedSection, sectionTempToUpdate) {
	if (replaceContainersMap.has(l_updatedSection.containerID) && (replaceContainersMap.get(l_updatedSection.containerID).replaceContainer.showContainer === _TRUE)) {
		var sourceCont = replaceContainersMap.get(l_updatedSection.containerID).sourceContainer;
		sourceCont.clauseOrder = l_updatedSection.clauseOrder;
		sourceCont.action = l_updatedSection.action;
		sourceCont.isMovedToRoot = l_updatedSection.isMovedToRoot;
		if (l_updatedSection.parentContainerID) {
			sourceCont.parentContainerID = l_updatedSection.parentContainerID;
		} else {
			sourceCont.parentContainerID = "";
		}
		clauseOrSectionSource = _prepareClauseUpdateRequest(sourceCont);
		sectionTempToUpdate.push(clauseOrSectionSource);
	}
}

function _addHiddenReplaceContainer(l_updatedSection, sectionTempToUpdate) {
	if (sourceReplaceMap.has(l_updatedSection.containerID) &&
		replaceContainersMap.has(sourceReplaceMap.get(l_updatedSection.containerID)) &&
		(replaceContainersMap.get(sourceReplaceMap.get(l_updatedSection.containerID)).sourceContainer.showContainer === _TRUE)) {
		var replaceCont = replaceContainersMap.get(sourceReplaceMap.get(l_updatedSection.containerID)).replaceContainer;
		replaceCont.clauseOrder = l_updatedSection.clauseOrder;
		replaceCont.action = l_updatedSection.action;
		replaceCont.isMovedToRoot = l_updatedSection.isMovedToRoot;
		replaceCont.parentContainerID = l_updatedSection.parentContainerID;
		clauseOrSectionSource = _prepareClauseUpdateRequest(replaceCont);

		if (!(l_updatedSection.action === _DELETE_CONTAINER && replaceContainersMap.has(l_updatedSection.containerID)
			// && l_updatedSection.showContainer === _FALSEF
		)) {
			sectionTempToUpdate.push(clauseOrSectionSource);
		}

	}
}

function _prepareRequestForUpdateContainer(sectionTempToUpdate, l_updatedSection, order, action) {
	if (action) {
		l_updatedSection.action = action;
	}
	_prepareReqForSecOrClause(sectionTempToUpdate, l_updatedSection, order);
	if (l_updatedSection.container().length > 0) {
		l_updatedSection.container().forEach((clauseOrSectionEle, index) => {
			_prepareRequestForUpdateContainer(sectionTempToUpdate, clauseOrSectionEle, index, action);
		});
	}
}

function _prepareClauseUpdateRequest(l_updatedClause) {
	/** if (l_updatedClause.showContainer === _FALSE) {return;}**/
	clauseTempToUpdate = {};
	if (!l_updatedClause.isStandard() && l_updatedClause.action != _DELETE_CONTAINER) {
		if (document.getElementById(l_updatedClause.containerID + "_clauseName").value == '') {
			notifyError(getTranslationMessage("Please enter a name for the non-standard clause."), 3000);
			isDirty = true;
			return;
		}
		else {
			clauseTempToUpdate.ClauseName = document.getElementById(l_updatedClause.containerID + "_clauseName").value
		}
		clauseTempToUpdate.ClauseHTMLContent = CKEDITOR.instances[l_updatedClause.containerID + "_clauseHTMLContent"].getData();
		clauseTempToUpdate.ClausePlainContent = jQuery(clauseTempToUpdate.ClauseHTMLContent).text();
	} else {
		clauseTempToUpdate.ClauseName = "";
		clauseTempToUpdate.ClauseHTMLContent = "";
		clauseTempToUpdate.ClausePlainContent = "";
	}
	clauseTempToUpdate.LinkedSectionID = "";
	clauseTempToUpdate.StylingAttributes = JSON.stringify({
		cascading: l_updatedClause.inherited() ? _CASCADE_INHERITED : l_updatedClause.cascade(),
		nonumbering: l_updatedClause.nonumbering(),
		numberingstyle: l_updatedClause.numberingStyle()
	});
	clauseTempToUpdate.SectionName = "";
	clauseTempToUpdate.InitialContainingSectionID = "";
	clauseTempToUpdate.InitialContainingClauseID = l_updatedClause.initialContainingClauseID;
	clauseTempToUpdate.isStandard = l_updatedClause.isStandard();
	clauseTempToUpdate.clauseType = l_updatedClause.clauseType;
	clauseTempToUpdate.cascade = l_updatedClause.cascade();
	clauseTempToUpdate.InitialContainingClauseID = l_updatedClause.initialContainingClauseID;
	clauseTempToUpdate.Order = l_updatedClause.clauseOrder;
	if (l_updatedClause.containerID != "") {
		clauseTempToUpdate.ContainingClauseID = l_updatedClause.containerID;
		clauseTempToUpdate.ParentContainerID = l_updatedClause.parentContainerID;
	}
	clauseTempToUpdate.LinkedClauseID = l_updatedClause.clauseID;
	clauseTempToUpdate.isMovedToRoot = l_updatedClause.isMovedToRoot;
	clauseTempToUpdate["@action"] = l_updatedClause.action;
	clauseTempToUpdate["@type"] = _CLAUSE;
	if (!l_updatedClause.isStandard() && l_updatedClause.isDirty() && l_updatedClause.isNew) {
		clauseTempToUpdate.LinkedClauseID = "";
	}
	return clauseTempToUpdate;
}

function _prepareSectionUpdateRequest(l_updatedSection) {
	sectionTempToUpdate = {};
	sectionTempToUpdate.ContainingClauseID = l_updatedSection.containerID;
	sectionTempToUpdate.ParentContainerID = l_updatedSection.parentContainerID;
	sectionTempToUpdate.LinkedSectionID = l_updatedSection.containingSectionID;
	sectionTempToUpdate.InitialContainingSectionID = l_updatedSection.initialContainingSectionID;
	sectionTempToUpdate.InitialContainingClauseID = "";
	sectionTempToUpdate.Order = l_updatedSection.clauseOrder;
	sectionTempToUpdate.isMovedToRoot = l_updatedSection.isMovedToRoot;
	sectionTempToUpdate.LinkedClauseID = "";
	sectionTempToUpdate.SectionName = l_updatedSection.sectionName();
	sectionTempToUpdate.ClauseName = "";
	sectionTempToUpdate.ClausePlainContent = "";
	sectionTempToUpdate.ClauseHTMLContent = "";
	sectionTempToUpdate.isStandard = "";
	sectionTempToUpdate.clauseType = "";
	sectionTempToUpdate.StylingAttributes = JSON.stringify({
		cascading: l_updatedSection.inherited() ? _CASCADE_INHERITED : l_updatedSection.cascade(),
		nonumbering: l_updatedSection.nonumbering(),
		numberingstyle: l_updatedSection.numberingStyle()
	});
	sectionTempToUpdate["@action"] = l_updatedSection.action;
	sectionTempToUpdate["@type"] = _SECTION;
	return sectionTempToUpdate;
}


function addDataToView(iElementList, iModel) {
	iModel.Listed_Clauses.removeAll();
	clause = {};
	clause.typeid = clauserelatedId;
	if (iElementList) {
		if (iElementList.length != undefined) {
			for (var i = 0; i < (iElementList.length); i++) {
				clause = {};
				clause.name = iElementList[i].Name;
				clause.plaincontent = iElementList[i].PlainContent;
				clause.htmlcontent = iElementList[i].HtmlContent;
				clause.id = iElementList[i]["GCClause-id"].Id;
				clause.itemid = iElementList[i]["GCClause-id"].ItemId;
				clause.clauseid = iElementList[i].ClauseId;
				clause.isglobal = iElementList[i].GlobalClause;
				clause.typeid = clauserelatedId;
				clause.clauseCategory = iElementList[i].RelatedClauseCategory["GCClauseCategory-id"].ItemId;
				iModel.Listed_Clauses.push(clause);
			}
		}
		else {
			clause.name = iElementList.Name;
			clause.plaincontent = iElementList.PlainContent;
			clause.htmlcontent = iElementList.HtmlContent;
			clause.id = iElementList["GCClause-id"].Id;
			clause.itemid = iElementList["GCClause-id"].ItemId;
			clause.clauseid = iElementList.ClauseId;
			clause.isglobal = iElementList.GlobalClause;
			clause.typeid = clauserelatedId;
			clause.clauseCategory = iElementList.RelatedClauseCategory["GCClauseCategory-id"].ItemId;
			iModel.Listed_Clauses.push(clause);
		}
		attachevents_to_Clause_Library();  //attaching events to clause library
	}

}

function getClauseCategory(clauseCategoryItemID) {
	$.cordys.ajax({
		method: "ReadGCClauseCategory",
		namespace: "http://schemas/OpenTextContentLibrary/GCClauseCategory/operations",
		parameters:
		{
			'GCClauseCategory-id':
			{
				'ItemId': clauseCategoryItemID
			}
		}
	}).done(function (data) {
		$("#clausecategory_textbox").text(data.GCClauseCategory.Name);
	}).fail(function (error) {

	});
}

function attachevents_to_Clause_Library() {
	var selectedID;
	$('.closebtn').on('click', function () {
		$('#clausePreview').css({ 'width': '0', 'padding-left': '0' });
		$(".list-group-item").css('background-color', '');
	})
	//Attaching  event on click of clause item
	$(".list-group-item").on("click", function () {
		//event = event || window.event;
		var clickedID = this.getAttribute('id');
		getClauseCategory(this.getAttribute('clauseCategory'));
		if (clickedID === selectedID) {
			$('#clausePreview').css({ 'width': '0', 'padding-left': '0' });
			$(".list-group-item").css('background-color', '');
			selectedID = undefined;
		}
		else {
			$('#clausePreview').css({ 'width': '400px', 'padding-left': '8px' });
			$("#clausename_textbox").text(this.getAttribute('clausename'));
			$("#clausecontent_textbox").html(this.getAttribute('clausecontent'));
			$(".list-group-item").css('background-color', '');
			if ($(this).css('background-color') === 'rgb(255, 255, 255)' && $(this).children()[0].checked == false) {
				$(this).css('background-color', 'darkgrey');
			}
			else {
				$(this).css('background-color', '');
			}
			selectedID = clickedID;
		}
	});

	$('.clause_library').click(function () {
		event.stopPropagation();
	})
}
function onClauseSelected(iData, iEvent) {
	if (iEvent.currentTarget.checked) {
		if (iData && iData.id) {
			clauseselectedarray["clause_" + iData.id] = iData;
			clause_list_model.clauses_selected_count(++clauseselectedarray.length);
			$("#insert_selected_btn").prop("disabled", false);
		}
	}
	else {
		if (iData && iData.id && clauseselectedarray["clause_" + iData.id]) {
			delete clauseselectedarray["clause_" + iData.id];
			if ((--clauseselectedarray.length) == 0) {
				clause_list_model.clauses_selected_count('');
				$("#insert_selected_btn").prop("disabled", true);
			}
			else {
				clause_list_model.clauses_selected_count(clauseselectedarray.length);
			}
		}
	}
}

function ListAllClauses() {
	var searchelmnt = $('#myInput').val();
	var isGlobal = document.getElementById("checkbox_global").checked;
	var local_contracttype = contractTypeID;
	limitValue = 10;
	if (clauselibrary_is_first_page_clicked) {
		offsetValue = 0;
		clause_list_model.currentPage('1');
		clauselibrary_is_first_page_clicked = 1;
		$('#clauselibrary_incrementer').css('display', 'inline');
		$('#clauselibrary_decrementer').css('display', 'none');
		clauselibrary_is_first_page_clicked = 0;
	}
	if (clauselibrary_is_last_page_clicked) {
		offsetValue = (Math.ceil(clause_list_model.total_clauses_count() / 10) - 1) * 10;
		clause_list_model.currentPage(Math.ceil(clause_list_model.total_clauses_count() / 10));
		clauselibrary_is_last_page_clicked = 0;
		$('#clauselibrary_incrementer').css('display', 'none');
		$('#clauselibrary_decrementer').css('display', 'inline');
		clauselibrary_is_last_page_clicked = 0;
	}
	if (isGlobal == true) {
		local_contracttype = "";
	}
	if (searchelmnt && clause_list_model.currentPage() === 1) {
		offsetValue = 0;
	}
	if (clause_list_model.total_clauses_count() <= 10) {
		clause_list_model.currentPage('1');
		$('#clauselibrary_decrementer,#clauselibrary_incrementer').css('display', 'none');
	}

	ContractListServiceModel = $.cordys.ajax({
		namespace: "http://schemas/OpenTextContentLibrary/GCClause/operations",
		method: "GetClausesbyNameTypeandState",
		parameters: {
			"TypeID": local_contracttype,
			"State": "Active",
			"ClauseName": searchelmnt,
			"GlobalClause": isGlobal ? "true" : "false",
			"Cursor": {
				'@xmlns': 'http://schemas.opentext.com/bps/entity/core',
				'@offset': offsetValue,
				'@limit': limitValue
			}
		},
		success: function (data) {
			if (data.GCClause != undefined) {
				addDataToView(data.GCClause, clause_list_model);
				$("#clauselibrary_noresults").css('display', 'none');
				$("#clauselibrary_paginationID").css({ 'display': 'block', 'height': '17%' });
				$("#clauseListID").css({ 'display': 'block', 'height': '70%' });
			}
			if (data.GCClause === undefined) {
				clause_list_model.Listed_Clauses.removeAll();
				$("#clauselibrary_noresults").css('display', 'block');
				$("#clauselibrary_paginationID").css({ 'display': 'none', 'height': '0' });
				$("#clauseListID").css({ 'display': 'none', 'height': '0' });
			}
		}
	});
}





function expandAllSections() {
	$(".collapseAllSections").css('display', 'inline-block');
	$(".expandAllSections").css('display', 'none');
	$(".panel-collapse").addClass("in");
	$(".accordion-toggle").removeClass("collapsed");
}

function collapseAllSections() {
	$(".collapseAllSections").css('display', 'none');
	$(".expandAllSections").css('display', 'inline-block');
	$(".panel-collapse").removeClass("in");
	$(".accordion-toggle").addClass("collapsed");

}

function togglePanel(panelNumber) {
	if ($('#Accordian_' + panelNumber).hasClass('collapsed')) {
		$('#Panel_' + panelNumber).addClass("in");
		$('#Accordian_' + panelNumber).removeClass("collapsed");
	}
	else {
		$('#Panel_' + panelNumber).removeClass("in");
		$('#Accordian_' + panelNumber).addClass("collapsed");
	}
}
function toggleThread(threadId) {
	if ($('#TAccordian_' + threadId).hasClass('collapsed')) {
		$('#Thread_' + threadId).addClass("in");
		$('#TAccordian_' + threadId).removeClass("collapsed");
	}
	else {
		$('#Thread_' + threadId).removeClass("in");
		$('#TAccordian_' + threadId).addClass("collapsed");
	}
}

function toggleOptions(elementId, className) {
	elementId = "#" + elementId;
	if ($(elementId).hasClass(className)) {
		$(elementId).removeClass(className);
	}
	else {
		$(elementId).addClass(className);
	}
}

function uncheck_modal() {
	document.getElementById("myInput").value = "";
	clause_list_model.clauses_selected_count('');
	$(".list-group-item").css('background-color', '');
	var ul = document.getElementById("ul-list-group");
	var items = ul.getElementsByTagName("li");
	for (var i = 0; i < (items.length); i++) {
		if (items[i].children[0].checked == true) {
			items[i].children[0].checked = false;
		}
	}
}

function getClauseRelatedTypeId(ClauseId) {
	$.cordys.ajax({
		method: "ReadGCType",
		namespace: "http://schemas/OpenTextBasicComponents/GCType/operations",
		parameters: {
			"GCType-id":
			{
				"Id": ClauseId
			}
		}
	}).done(function (data) {
		clauserelatedId = data.GCType.Name;
	}).fail(function (error) {
		notifyError(error, 10000);

	});

}

//get Clauses Count
function getClausesCount() {
	var isGlobal = document.getElementById("checkbox_global").checked;
	var local_contracttype = contractTypeID;
	var temp_clausename = "";
	if (isGlobal == true) {
		local_contracttype = "";
	}
	if ($('#myInput').val()) {
		temp_clausename = $('#myInput').val();
	}
	$.cordys.ajax({
		method: "getClauseCount",
		namespace: "http://schemas/OpenTextContentLibrary/18.4",
		parameters: {
			"TypeID": local_contracttype,
			"State": "Active",
			"ClauseName": temp_clausename,
			"GlobalClause": isGlobal ? "true" : "false"
		}
	}).done(function (data) {
		clauseselectedarray = [];
		clause_list_model.clauses_selected_count('');
		clause_list_model.total_clauses_count(getTextValue(data.Count));
		ListAllClauses();
	}).fail(function (error) {

	});
}



function clearSearchFilter() {
	document.getElementById("myInput").value = "";
}

function showClauseLibrary() {
	getClauseRelatedTypeId(contractTypeID);
	//$("#showClauseListDiv").css("display", "none");
	//$("#hideClauseListDiv").css("display", "inline-block");
	$(".clause_Iterator").removeClass("clauseSelected");
	$(".showClauseLibrary").css("display", "none");
	$(".hideClauseLibrary").css("display", "inline-block");
	$("#clausediv").css("display", "block");
	$("#div_sectionsAndClausesContent").addClass("col-md-8 col-xs-8 rtl-float-right");
	attachEvents();
	getClausesCount();
	if (clause_list_model.currentPage() == 1) {
		document.getElementById("clauselibrary_decrementer").style.display = "none";
	}
	//_headerAdj(false);
}

function hideClauseLibrary() {
	$("#clausediv").css("display", "none");
	$(".showClauseLibrary").css("display", "inline-block");
	$(".hideClauseLibrary").css("display", "none");
	//	$("#showClauseListDiv").css("display", "inline-block");
	$("#div_sectionsAndClausesContent").removeClass("col-md-8 col-xs-8 rtl-float-right");
	clause_list_model.clauses_selected_count('');
	$('#clausePreview').css({ 'width': '0', 'padding-left': '0' });
	$(".list-group-item").css('background-color', '');
	//_headerAdj(true);
}

function _headerAdj(max) {
	if (max) {
		$("#div_clauseEditorActions").removeClass("col-md-4");
		$("#div_clauseEditorActions").addClass("col-md-5");
		$("#div_clauseLibraryActions").removeClass("col-md-2");
		$("#div_clauseLibraryActions").addClass("col-md-1");
	} else {
		$("#div_clauseEditorActions").removeClass("col-md-5");
		$("#div_clauseEditorActions").addClass("col-md-4");
		$("#div_clauseLibraryActions").removeClass("col-md-1");
		$("#div_clauseLibraryActions").addClass("col-md-2");
	}
}


function attachEvents() {
	$('#input_img').click(function () {
		clause_list_model.currentPage('1');
		offsetValue = 0;
		$('#clauselibrary_decrementer').css('display', 'none');
		$('#clauselibrary_incrementer').css('display', 'inline');
		getClausesCount();
	});
	$('#myInput').keypress(function (e) {
		var key = e.which;
		if (key == 13)  // the enter key code
		{
			clause_list_model.currentPage('1');
			offsetValue = 0;
			$('#clauselibrary_decrementer').css('display', 'none');
			$('#clauselibrary_incrementer').css('display', 'inline');
			getClausesCount();
			return false;
		}
	});
	$('#checkbox_global').change(function () {
		offsetValue = 0;
		delete clauseselectedarray;
		clauseselectedarray = [];
		clause_list_model.clauses_selected_count('');
		clause_list_model.currentPage('1');
		$('#clauselibrary_decrementer').css('display', 'none');
		$('#clauselibrary_incrementer').css('display', 'inline');
		getClausesCount();
	});
}
function clauselibrary_incrementOffsetLimit() {
	if (clause_list_model.currentPage() < Math.ceil(clause_list_model.total_clauses_count() / 10)) {
		offsetValue = offsetValue + 10;
		clause_list_model.currentPage(isNaN(parseInt(clause_list_model.currentPage())) ? 0 : parseInt(clause_list_model.currentPage()));
		clause_list_model.currentPage(parseInt(clause_list_model.currentPage()) + 1);
	}
	if (clause_list_model.currentPage() == Math.ceil(clause_list_model.total_clauses_count() / 10)) {
		document.getElementById("clauselibrary_incrementer").style.display = "none";
	}
	if (clause_list_model.currentPage() > 1) {
		document.getElementById("clauselibrary_decrementer").style.removeProperty("display");
	}
	ListAllClauses();
}

function clauselibrary_decrementOffsetLimit() {
	if (clause_list_model.currentPage() > 1) {
		offsetValue = offsetValue - 10;
		clause_list_model.currentPage(parseInt(clause_list_model.currentPage()) - 1);
	}
	if (clause_list_model.currentPage() < Math.ceil(clause_list_model.total_clauses_count() / 10)) {
		document.getElementById("clauselibrary_incrementer").style.removeProperty("display");
	}
	if (clause_list_model.currentPage() == 1) {
		document.getElementById("clauselibrary_decrementer").style.display = "none";
	}
	if (clause_list_model.currentPage() < 1)
		return;
	ListAllClauses();
}

function clauselibrary_incrementToLast() {
	clauselibrary_is_last_page_clicked = 1;
	getClausesCount();
}

function clauselibrary_decrementToLast() {
	clauselibrary_is_first_page_clicked = 1;
	getClausesCount()
}


// Equivalent Clauses -- sboddire
function showEqClausesList(l_parentIndex, l_index, iClauseID) {
	if (l_input_conditionInstMap.has(l_sectionandclause_model.addSectionData().containerID)) {
		return;
	}
	eqSrcSectionIndex = l_parentIndex;
	eqSrcClauseIndex = l_index;
	iClauseID = l_sectionandclause_model.addSectionData().clauseID;
	hideClauseLibrary();
	$("#eqClauseList").css("display", "block");
	$("#div_sectionsAndClausesContent").addClass("col-md-8 col-xs-8 rtl-float-right");
	$('#eqClauseSearchInput').val("");
	$(".clause_Iterator").removeClass("clauseSelected");
	// $("#" + l_parentIndex + "_" + l_index + "_Clause").addClass("clauseSelected")
	attachEventstoEqClauseList(iClauseID);
	getEqClausesCount(iClauseID);
	if (eq_clause_list_model.currentPage() == 1) {
		document.getElementById("eqDecrementer").style.display = "none";
	}
}

function attachEventstoEqClauseList(clauseId) {
	$('#eqClauseSearchInput_Img').click(function () {
		eq_clause_list_model.currentPage('1');
		eqOffsetValue = 0;
		$('#eqDecrementer').css('display', 'none');
		$('#eqIncrementer').css('display', 'inline');
		getEqClausesCount(clauseId);
	});
	$('#eqClauseSearchInput').keypress(function (e) {
		var key = e.which;
		if (key == 13)  // the enter key code
		{
			eq_clause_list_model.currentPage('1');
			eqOffsetValue = 0;
			$('#eqDecrementer').css('display', 'none');
			$('#eqIncrementer').css('display', 'inline');
			getEqClausesCount(clauseId);
			return false;
		}
	});
	$('#eqCloseButton').on('click', function () {
		$("#eqClauseList").css("display", "none");
		$("#div_sectionsAndClausesContent").removeClass("col-md-8 col-xs-8 rtl-float-right");
		//$(".clause_Iterator").css('background-color', '')
		$(".clause_Iterator").removeClass("clauseSelected");
	});
}

function incrementEqClauseOffsetLimit() {
	if (eq_clause_list_model.currentPage() < Math.ceil(eq_clause_list_model.equivalentClauseCount() / 10)) {
		eqOffsetValue = eqOffsetValue + 10;
		eq_clause_list_model.currentPage(isNaN(parseInt(eq_clause_list_model.currentPage())) ? 0 : parseInt(eq_clause_list_model.currentPage()));
		eq_clause_list_model.currentPage(parseInt(eq_clause_list_model.currentPage()) + 1);
	}
	if (eq_clause_list_model.currentPage() == Math.ceil(eq_clause_list_model.equivalentClauseCount() / 10)) {
		document.getElementById("eqIncrementer").style.display = "none";
	}
	if (eq_clause_list_model.currentPage() > 1) {
		document.getElementById("eqDecrementer").style.removeProperty("display");
	}
	listEquivalentClauses(ClauseIdforEq, $('#eqClauseSearchInput').val());
}
function decrementEqClauseOffsetLimit() {
	if (eq_clause_list_model.currentPage() > 1) {
		eqOffsetValue = eqOffsetValue - 10;
		eq_clause_list_model.currentPage(parseInt(eq_clause_list_model.currentPage()) - 1);
	}
	if (eq_clause_list_model.currentPage() < Math.ceil(eq_clause_list_model.total_clauses_count() / 10)) {
		document.getElementById("eqIncrementer").style.removeProperty("display");
	}
	if (eq_clause_list_model.currentPage() == 1) {
		document.getElementById("eqDecrementer").style.display = "none";
	}
	if (eq_clause_list_model.currentPage() < 1)
		return;
	listEquivalentClauses(sourceClauseIdforEq, $('#eqClauseSearchInput').val());
}
function incrementEqClauseToLast() {
	is_eqlast_page_clicked = 1;
	getEqClausesCount(sourceClauseIdforEq);
}
function decrementEqClauseToFirst() {
	is_eqfirst_page_clicked = 1;
	getEqClausesCount(sourceClauseIdforEq);
}

function getEqClausesCount(clauseId) {
	var searchEle = "";
	if ($('#eqClauseSearchInput').val()) {
		var searchEle = $('#eqClauseSearchInput').val();
	}
	paramObject = getEqClausesCountParameters(clauseId, searchEle),
		$.cordys.ajax({
			method: "GetEqClausesCount",
			namespace: "http://schemas.opentext.com/apps/contentlibrary/19.2",
			parameters: paramObject,
		}).done(function (data) {
			eq_clause_list_model.equivalentClauseCount(getTextValue(data.eqClausesCount));
			if (getTextValue(data.eqClausesCount) === undefined) {
				$("#eqClauseResults").css('display', 'none');
				$("#eqNoresults").css('display', 'block');
			}
			else {
				$("#eqClauseResults").css('display', 'block');
				$("#eqNoresults").css('display', 'none');
				listEquivalentClauses(clauseId, searchEle);
			}
		}).fail(function (error) {

		});
}

function getEqClausesCountParameters(clauseId, searchEle) {
	return createParametersObject =
	{
		SourceID: clauseId,
		Cursor:
		{
			offset: eqOffsetValue,
			limit: eqLimitValue,
		},
		searchElement: searchEle
	};
}

function listEquivalentClauses(clauseId, searchEle) {
	eqLimitValue = 10;
	if (is_eqfirst_page_clicked) {
		eqOffsetValue = 0;
		eq_clause_list_model.currentPage('1');
		is_eqfirst_page_clicked = 1;
		$('#eqIncrementer').css('display', 'inline');
		$('#eqDecrementer').css('display', 'none');
		is_eqfirst_page_clicked = 0;
	}
	if (is_eqlast_page_clicked) {
		eqOffsetValue = (Math.ceil(eq_clause_list_model.equivalentClauseCount() / 10) - 1) * 10;
		eq_clause_list_model.currentPage(Math.ceil(eq_clause_list_model.equivalentClauseCount() / 10));
		is_eqlast_page_clicked = 0;
		$('#eqIncrementer').css('display', 'none');
		$('#eqDecrementer').css('display', 'inline');
		is_eqlast_page_clicked = 0;
	}
	if (searchEle && eq_clause_list_model.currentPage() === 1) {
		eqOffsetValue = 0;
	}
	if (eq_clause_list_model.equivalentClauseCount() <= 10) {
		eq_clause_list_model.currentPage('1');
		$('#eqDecrementer,#eqIncrementer').css('display', 'none');
	}

	EquivalentClauseListModel = $.cordys.ajax({
		namespace: "http://schemas.opentext.com/apps/contentlibrary/19.2",
		method: "GetEqClausesByID",
		parameters:
		{
			"SourceID": clauseId,
			"Cursor": {
				"offset": eqOffsetValue,
				"limit": eqLimitValue
			},
			"searchElement": searchEle
		},
		success: function (data) {
			if (data.FindZ_INT_EqClausesListResponse != undefined) {
				addDataToEqClausesListView(data.FindZ_INT_EqClausesListResponse.GCClauseRelations, eq_clause_list_model);
				$("#eqNoresults").css('display', 'none');
				$("#eqPaginationID").css({ 'display': 'block', 'height': '10%' });
				$("#eqClauseTable").css({ 'display': 'block', 'height': '65%' });
			}
			if (data.FindZ_INT_EqClausesListResponse === undefined) {
				eq_clause_list_model.equivalentClauseList.removeAll();
				$("#eqNoresults").css('display', 'block');
				$("#eqPaginationID").css({ 'display': 'none', 'height': '0' });
				$("#eqClauseTable").css({ 'display': 'none', 'height': '0' });
			}
		}
	});
}

function addDataToEqClausesListView(iElementList, iModel) {
	iModel.equivalentClauseList.removeAll();
	eqClause = {};
	if (iElementList) {
		if (iElementList.length != undefined) {
			for (var i = 0; i < (iElementList.length); i++) {
				eqClause = {};
				eqClause.clauseRelationsID = iElementList[i]["GCClauseRelations-id"].Id;
				eqClause.clauseRelationsItemID = iElementList[i]["GCClauseRelations-id"].ItemId;
				eqClause.sourceClauseID = iElementList[i].SourceClause["GCClause-id"].Id;
				eqClause.sourceClauseItemID = iElementList[i].SourceClause["GCClause-id"].ItemId;
				eqClause.sourceClauseName = getTextValue(iElementList[i].SourceClause.Name);
				eqClause.targetClauseID = iElementList[i].TargetClause["GCClause-id"].Id;
				eqClause.targetClauseItemID = iElementList[i].TargetClause["GCClause-id"].ItemId;
				//eqClause.targetClauseName = iElementList[i].TargetClause.Name.text;
				if (iElementList[i].TargetClause.Name === null) {
					eqClause.targetClauseName = ""
				}
				else {
					eqClause.targetClauseName = getTextValue(iElementList[i].TargetClause.Name);
				}
				//eqClause.targetClauseClauseCategoryName = iElementList[i].TargetClause.RelatedClauseCategory.Name.text;
				if (iElementList[i].TargetClause.RelatedClauseCategory === null) {
					eqClause.targetClauseClauseCategoryName = ""
				}
				else {
					eqClause.targetClauseClauseCategoryName = getTextValue(iElementList[i].TargetClause.RelatedClauseCategory.Name);
				}
				//eqClause.targetClausePlainContent = iElementList[i].TargetClause.PlainContent.text;
				if (iElementList[i].TargetClause.PlainContent === null) {
					eqClause.targetClausePlainContent = ""
				}
				else {
					eqClause.targetClausePlainContent = getTextValue(iElementList[i].TargetClause.PlainContent);
				}
				//eqClause.targetClauseHTMLContent = iElementList[i].TargetClause.HtmlContent.text;
				if (iElementList[i].TargetClause.HtmlContent === null) {
					eqClause.targetClauseHTMLContent = ""
				}
				else {
					eqClause.targetClauseHTMLContent = getTextValue(iElementList[i].TargetClause.HtmlContent);
				}
				iModel.equivalentClauseList.push(eqClause);
			}
		}
		else {
			eqClause.clauseRelationsID = iElementList["GCClauseRelations-id"].Id;
			eqClause.clauseRelationsItemID = iElementList["GCClauseRelations-id"].ItemId;
			eqClause.sourceClauseID = iElementList.SourceClause["GCClause-id"].Id;
			eqClause.sourceClauseItemID = iElementList.SourceClause["GCClause-id"].ItemId;
			eqClause.sourceClauseName = getTextValue(iElementList.SourceClause.Name);
			eqClause.targetClauseID = iElementList.TargetClause["GCClause-id"].Id;
			eqClause.targetClauseItemID = iElementList.TargetClause["GCClause-id"].ItemId;
			//eqClause.targetClauseName = iElementList.TargetClause.Name.text;
			//eqClause.targetClauseClauseCategoryName = iElementList.TargetClause.RelatedClauseCategory.Name.text;
			//eqClause.targetClausePlainContent = iElementList.TargetClause.PlainContent.text;
			//eqClause.targetClauseHTMLContent = iElementList.TargetClause.HtmlContent.text;
			if (iElementList.TargetClause.Name === null) {
				eqClause.targetClauseName = ""
			}
			else {
				eqClause.targetClauseName = getTextValue(iElementList.TargetClause.Name);
			}
			if (iElementList.TargetClause.RelatedClauseCategory === null) {
				eqClause.targetClauseClauseCategoryName = ""
			}
			else {
				eqClause.targetClauseClauseCategoryName = getTextValue(iElementList.TargetClause.RelatedClauseCategory.Name);
			}
			if (iElementList.TargetClause.PlainContent === null) {
				eqClause.targetClausePlainContent = ""
			}
			else {
				eqClause.targetClausePlainContent = getTextValue(iElementList.TargetClause.PlainContent);
			}
			if (iElementList.TargetClause.HtmlContent === null) {
				eqClause.targetClauseHTMLContent = ""
			}
			else {
				eqClause.targetClauseHTMLContent = getTextValue(iElementList.TargetClause.HtmlContent);
			}
			iModel.equivalentClauseList.push(eqClause);
		}
		attachEventstoEqClausesList();  //attaching events to clause library
	}
}

function attachEventstoEqClausesList() {
	$('#eqPreviewCloseButton').on('click', function () {
		//$("#eqClausePreview").removeClass("showeqClausePreview");
		//$("#eqClausePreview").addClass("hideeqClausePreview");
		$('#eqClausePreview').css({ 'width': '0', 'padding-left': '0' });
		$(".list-group-item").css('background-color', '');
	})
	$(".eqPreview").on("click", function () {
		//event = event || window.event;
		$(this.parentElement).css('background-color', 'darkgrey');
		//$("#eqClausePreview").addClass("showeqClausePreview");
		//$("#eqClausePreview").removeClass("hideeqClausePreview");
		$('#eqClausePreview').css({ 'width': '30%', 'padding-left': '8px' });
		$("#eqClauseName").text(this.parentElement.getAttribute('targetClauseName'));
		$("#eqClauseCategory").text(this.parentElement.getAttribute('targetClauseClauseCategoryName'));
		$("#eqClauseContent").html(this.parentElement.getAttribute('targetClauseHTMLContent'));
		$(".eqPreview").css('background-color', '');
		if ($(this).css('background-color') === 'rgb(255, 255, 255)' && $(this).children()[0].checked == false) {
			$(this).css('background-color', 'darkgrey');
		}
		else {
			$(this).css('background-color', '');
		}
	});
}



function _populateRootSectionsAndClausesAuthoring(SCMappings, SCMappingsFromRead, newRecord) {
	if (!newRecord && l_sectionandclause_model) {
		l_sectionandclause_model.cascadeDocLevel(_CASCADE_OFF);
		l_current_numberingFormat = _DECIMAL_SIMPLE_FORMAT;
	}
	for (var i = 0; i < SCMappingsFromRead.length; i++) {
		var sectionOrClauseTemp = {};
		var sectionOrClauseFromRead = SCMappingsFromRead[i];
		if (sectionOrClauseFromRead.LinkedSection && !sectionOrClauseFromRead.ParentContainer) {
			_populateSection(sectionOrClauseTemp, sectionOrClauseFromRead, SCMappingsFromRead, null);
			SCMappings.push(sectionOrClauseTemp);
		}
		else if ((sectionOrClauseFromRead.LinkedClause && !sectionOrClauseFromRead.ParentContainer)) {
			if (sectionOrClauseFromRead.LinkedClause) {
				_populateClause(sectionOrClauseTemp, sectionOrClauseFromRead, SCMappingsFromRead, null);
				_addToReplaceContainerMap(
					sectionOrClauseTemp,
					SCMappings(),
					sectionOrClauseFromRead.RelatedCondition ? getTextValue(sectionOrClauseFromRead.RelatedCondition.Action) : "",
					sectionOrClauseFromRead.RelatedCondition ? sectionOrClauseFromRead.RelatedCondition.SourceContainer["ContainingClauses-id"].Id2 : ""
				);
			}
			if (!newRecord) {
				_populateOldSectionRecord(SCMappings, sectionOrClauseFromRead, sectionOrClauseTemp);
			} else {
				sectionOrClauseTemp.sectionName = sectionOrClauseTemp.clauseName;
				SCMappings.push(sectionOrClauseTemp);
			}
		}
		else if (!sectionOrClauseFromRead.LinkedClause && sectionOrClauseFromRead.Owner["ContainingSections-id"].Id && !newRecord) {
			_populateOldSectionRecord(SCMappings, sectionOrClauseFromRead, sectionOrClauseTemp);
		}
	}
	_sortArrayOnClauseOrder(SCMappings);
	_addChildrenReplaceContainers();
}



// function _addChildrenReplaceContainers(containers) {
// 	var replaceContainers = containers.filter(container => container.RelatedCondition && container.RelatedCondition.Action === 'REPLACE');
// 	var sourceContArr = _findContainerWithContainerId(containers, container.RelatedCondition.SourceContainer["ContainingClauses-id"].Id2);
// 	replaceContainersMap.forEach(container => {
// 		var sourceContArr = _findContainerWithContainerId(containers, container.RelatedCondition.SourceContainer["ContainingClauses-id"].Id2);
// 		// var sourceContArr = containers.filter(ele => ele["ContainingClauses-id"].Id2 === container.RelatedCondition.SourceContainer["ContainingClauses-id"].Id2);
// 		if (sourceContArr.length > 0) {
// 			var sourceCont = sourceContArr[0];
// 			if (sourceCont.container.length > 0) {
// 				container.container.removeAll();
// 				container.container = sourceCont.container;
// 			}
// 		}
// 	});
// }

// function _findContainerWithContainerId(containers, containerId) {
// 	var sourceContArr = containers.filter(ele => ele["ContainingClauses-id"].Id2 === containerId);
// 	if (sourceContArr.length > 0) {
// 		return sourceContArr[0];
// 	} else {
// 		containers.forEach(container_paren => {
// 			if (container_paren.container().length > 0) {
// 				return _findContainerWithContainerId(container_paren.container().length, containerId);
// 			} else {
// 				return [];
// 			}
// 		});
// 	}
// }

function UpdateEqclauseIDandItemID(_event, element) {
	var l_clause = l_sectionandclause_model.addSectionData();

	l_clause.clauseID = element.targetClauseID;
	l_clause.id = element.targetClauseID;
	l_clause.generatedClauseID = "CLA-" + element.targetClauseID;
	l_clause.clauseItemID = element.targetClauseItemID;
	l_clause.clauseName(element.targetClauseName);
	l_clause.clauseHTMLContent = element.targetClauseHTMLContent;
	l_clause.isDirty(true);
	// if (l_clause.action == _MOVE_CLAUSE_BETWEEN_SECTIONS) {
	// 	checkAndDeleteClause(l_clause);
	// 	l_clause.isNew = true;
	// 	l_clause.action = _NEW_LINKED_CLAUSE;
	// }
	// else 
	if (!l_clause.action) {
		l_clause.action = _UPDATE_LINKED_CLAUSE;
	} else if (l_clause.action == _UPDATE_ORDER) {
		l_clause.action = _UPDATE_LINKED_CLAUSE_ORDER;
	}
	CKEDITOR.instances[l_clause.containerID + '_clauseHTMLContent'].setData(element.targetClauseHTMLContent);
	$("#div_sectionsAndClausesContent").removeClass("col-md-8 col-xs-8 rtl-float-right");
	$(".clause_Iterator").removeClass("clauseSelected");
	isDirty = true;
}


/**
 * All the code from here for loading and inserting terms.
 */

var contentDIVIdForInsertTerms;

function callOpenTermsList(clauseContentDivID) {
	contentDIVIdForInsertTerms = clauseContentDivID;
	tokenObject = {};
	// $("#id_insertTermsDialog").modal();
	// getActiveTermsCount();
}


function addDataToTermsView(iElementList, iModel) {
	iModel.TermsList.removeAll();
	if (iElementList) {
		if (iElementList.length) {
			iElementList.forEach(function (iElement) {
				iModel.TermsList.push(iElement);
			});
		}
		else {
			iModel.TermsList.push(iElementList);
		}
	}
}

function callInsertTerm(termsTableID, _clauseContentDivID) {
	$("#id_searchElement").val("");
	var l_appendText = '';
	for (elem in tokenObject) {
		if (tokenObject[elem] != "") {
			l_appendText += tokenObject[elem] + " ";
		}
	}
	if (l_appendText) {
		insertAtCursor(contentDIVIdForInsertTerms, l_appendText.substring(0, l_appendText.length - 1));
	}
	var tableid = $('#' + termsTableID).parent("table").prop("id");
	$('#' + tableid + ' input[type=checkbox]:checked').removeAttr('checked');
	$('#selectAll').checked = false;
	offsetValue = 0;
	l_terms_model.currentPage(1);
	$("#id_canceleditClause, #id_saveClauseonEdit").prop("disabled", false);
}

function insertAtCursor(areaId, text) {
	var editor = CKEDITOR.instances[areaId];
	if (editor && editor.getSelection() && editor.getSelection().getRanges()[0]) {
		var container = editor.getSelection().getRanges()[0].startContainer;
		if (container) {
			var existingText = container.getText();
			var insertIndex = editor.getSelection().getRanges()[0].startOffset;
			var randomStr = createRandomString();
			existingText = existingText.substring(0, insertIndex) + randomStr + existingText.substring(insertIndex);
			container.setText(existingText);
			var element = editor.getSelection().getStartElement();
			var existingHtml = element.getHtml();
			var index = existingHtml.indexOf(randomStr);
			existingHtml = existingHtml.substring(0, index) + '<span class="termInsert_editor" style="none" contenteditable="false">' + text + '</span>' + existingHtml.substring(randomStr.length + index);
			element.setHtml(existingHtml);
			isDirty = true;
		}
	}
}

function createRandomString() {
	return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
}

function clearData() {
	offsetValue = 0;
	l_terms_model.currentPage(1);
	$("#id_searchElement").val("");
	var tableid = $('#id_termsTableBody').parent("table").prop("id");
	$('#' + tableid + ' input[type=checkbox]:checked').removeAttr('checked');
	$('#selectAll').checked = false;
}

function getPlainTextFromEditorData(iEditor) {
	var l_text = "";
	if (iEditor) {
		var l_textNodes = iEditor.element.$.childNodes;
		if (l_textNodes && (l_textNodes.length > 0)) {
			l_text += l_textNodes.item(0).textContent;
			for (var indx = 1; indx < l_textNodes.length; indx++) {
				l_text += '\n' + l_textNodes.item(indx).textContent;
			}
		}
	}
	return l_text;
}

function showOrHideInfo(iShow, iExceptionMsg, iType, iVisibleTime) {
	var l_infoArea = document.getElementById("InfoArea");
	if (iShow) {
		l_infoArea.style.display = "inline";
		l_infoArea.lastElementChild.innerText = iExceptionMsg;
		if (iVisibleTime) {
			setTimeout(showOrHideInfo, iVisibleTime);
		}
	}
	else {
		l_infoArea.style.display = "none";
		l_infoArea.lastElementChild.innerText = "";
	}
}


function highlightClauseorSection(l_event, l_data) {
	var l_ContainingSectionID = getTextValue(l_data.comments()[0].InitialContainingSectionID());
	var l_ContainingClauseID = getTextValue(l_data.comments()[0].InitialContainingClauseID());
	_selectForCommentContainer(l_ContainingSectionID, l_ContainingClauseID, l_event);
}

function _selectForCommentContainer(l_ContainingSectionID, l_ContainingClauseID, event) {
	var data = {
		ContainingSectionID: l_ContainingSectionID,
		ContainingClauseID: l_ContainingClauseID,
		ContainingSectionData: null,
		ContainingClauseData: null,
		parentData: {},
		parentParentData: {}
	};
	_checkCommentContainer(data, 0, l_sectionandclause_model.SCMappingsToBind().length, l_sectionandclause_model.SCMappingsToBind(), l_sectionandclause_model, {});
	var selectedContainer = data.ContainingClauseData ? data.ContainingClauseData : data.ContainingSectionData;
	l_sectionandclause_model.selectContainer(selectedContainer, data.parentData, data.parentParentData);
	$('#m_secandcls').animate({
		scrollTop: $("#" + selectedContainer.containerID + "_Container")[0].offsetTop - 70
	}, 500);
}

function _checkCommentContainer(data, index, len, container, parent, grandParent) {
	if (index >= len) {
		return;
	}
	var clauseOrsection = container[index];
	if (clauseOrsection.initialContainingSectionID && (clauseOrsection.initialContainingSectionID === data.ContainingSectionID)) {
		data.ContainingSectionData = clauseOrsection;
		data.parentData = parent;
		data.parentParentData = grandParent;
	}
	if (clauseOrsection.initialContainingClauseID && (clauseOrsection.initialContainingClauseID === data.ContainingClauseID)) {
		data.ContainingClauseData = clauseOrsection;
		data.parentData = parent;
		data.parentParentData = grandParent;
	}
	var contianerlen = clauseOrsection.container().length;
	if (contianerlen > 0) {
		_checkCommentContainer(data, 0, contianerlen, clauseOrsection.container(), clauseOrsection, parent);
	}
	_checkCommentContainer(data, index + 1, len, container, parent, grandParent);
}



function makeItReadOnly() {
	l_sectionandclause_model.contract_Z_INT_Status(l_sectionandclause_model.contract_Z_INT_Status() + 'Review');
	l_comments_model.contract_Z_INT_Status(l_comments_model.contract_Z_INT_Status() + 'Review');
	$(".cke").hide();
}

function enable_changeNegDate() {
	$('#editNegCloseDate').off('click');
	$("#editNegCloseDate").on("click", function () {
		var input = $('<input />', { 'type': 'text', 'class': 'datePicker', 'width': '80px', 'value': $(".editableDateTxt").html() });
		var parent = $(".editableDateTxt").parent();
		$(this).hide(500);
		parent.append(input);
		$(".editableDateTxt").hide();
		parent.find('.datePicker').datepicker({
			dateFormat: "yy-mm-dd",
			onSelect: function (date, i) {
				var validLastDate = i.lastVal ? (moment(date).format('YYYY/MM/DD') !== moment(i.lastVal).format('YYYY/MM/DD')) : true;
				if (validLastDate && validate_ChangeNegDate(date)) {
					$(".editableDateTxt").html(formateDatetoLocale(date)).show();
				}
				$(".editableDateTxt").show();
				$(this).datepicker('hide');
				$(this).remove();
				$("#editNegCloseDate").show(500);
			},
			onClose: function (date) {
				$(".editableDateTxt").show();
				$(this).datepicker('hide');
				$(this).remove();
				$("#editNegCloseDate").show(500);
			}

		}).datepicker('show');

	});
}
if (mBundle) {
	mBundle.translate();
}

function validate_ChangeNegDate(date) {
	var inputDate = new Date(date);
	var todaysDate = new Date();
	if (inputDate.setHours(0, 0, 0, 0) < todaysDate.setHours(0, 0, 0, 0)) {
		notifyError(getTranslationMessage("Closure date must be a date after today's date."), 3000);
		return false;

	} else {
		$.cordys.ajax({
			method: "RequestNegotiationClosure",
			namespace: "http://schemas.opentext.com/apps/ContractNegotiation/19.4",
			parameters: {
				"contractID": contractItemId.substring(contractItemId.indexOf(".") + 1),
				"dueDate": moment(date).format('YYYY-MM-DD')
			},
			success: function (responseSuccess) {
				if (!responseSuccess) {
					notifyError(getTranslationMessage("Error while requesting negotiation closure."), 3000);
					return false;
				}
				if (mBundle) {
					mBundle.translate();
				}
			},
			error: function (responseFailure) {
				notifyError(true, getTranslationMessage("Error while requesting negotiation closure."), 3000);
				return false;
			}
		});
	}
	return true;

}

// Verifying the user role.
function checkForUserRole() {
	$.cordys.ajax(
		{
			method: "CheckCurrentUserInRoles",
			namespace: "http://schemas.opentext.com/apps/cc/configworkflow/20.2",
			parameters:
			{
				"Roles": {
					"Role": [
						"Contract Administrator",
						"Contract Manager",
						"Contract Author"
					]
				}
			},
		}).done(function (data) {
			var result = getTextValue(data.IsCurrentUserRoles);
			if (result && result.toLowerCase() == "true") {
				readNegotiationDetails();
			} else {
				makeItReadOnly();
			}
		}).fail(function (error) {
			readNegotiationDetails();
		})
}


var l_input_conditionInstMap = new Map();
function _populateConditionContData(allContainers) {

	allContainers.forEach(container => {
		if (container.RelatedCondition) {
			if (getTextValue(container.RelatedCondition.Action) === 'HIDE') {
				l_input_conditionInstMap.set(container["ContainingClauses-id"].Id2, { action: 'HIDE', ruleId: container.RelatedCondition.ConditionRule['Rule-id'].Id });
			} else if (getTextValue(container.RelatedCondition.Action) === 'REPLACE') {
				_populateReplaceContainerCond(container, allContainers, l_input_conditionInstMap);
			}
		}
	});
	return l_input_conditionInstMap;
}


function _populateReplaceContainerCond(replaceContainer, allContainers, l_input_conditionInstMap) {
	var sourceContArr = allContainers.filter(ele => ele["ContainingClauses-id"].Id2 === replaceContainer.RelatedCondition.SourceContainer["ContainingClauses-id"].Id2);
	if (sourceContArr.length > 0) {
		var sourceCont = sourceContArr[0];
		l_input_conditionInstMap.set(sourceCont["ContainingClauses-id"].Id2, { action: 'HIDE', ruleId: replaceContainer.RelatedCondition.ConditionRule['Rule-id'].Id });
		replaceContainer.ClauseOrder = sourceCont.ClauseOrder;
		if (sourceCont.ParentContainer) {
			replaceContainer.ParentContainer = sourceCont.ParentContainer;
		}
		if (sourceCont.StylingAttributes) {
			replaceContainer.StylingAttributes = sourceCont.StylingAttributes;
		}
	}
	l_input_conditionInstMap.set(replaceContainer["ContainingClauses-id"].Id2, { action: 'SHOW', ruleId: replaceContainer.RelatedCondition.ConditionRule['Rule-id'].Id });
}



