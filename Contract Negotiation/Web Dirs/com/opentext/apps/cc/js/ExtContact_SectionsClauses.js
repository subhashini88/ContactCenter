$.cordys.json.defaults.removeNamespacePrefix = true;

var replaceContainersMap = new Map();
var SectionsAndClausesDataModel = function () {
	this.SCMappingsToBind = ko.observableArray([]);
	this.RelatedContentStatus = ko.observable();
	this.optionsCaption = ko.observable("No sections added");
	this.selectedContainer = ko.observable();
	this.selectedSectionMenu = ko.observable();
	this.cascadeDocLevel = ko.observable(_CASCADE_ON);

	this.change_dropDown = function (_item, event) {
		$('#sectionList').val(_item.UIcontainerID);
		selectSection(_item.UIcontainerID, false, false, true);
		event.stopPropagation();
	}

	this.selectContainer = function (data, event, dontSetMenu) {
		this.selectedContainer(data.UIcontainerID);
		if (!dontSetMenu) {
			this.selectedSectionMenu("");
		}
		event.stopPropagation();
	}

	this.selectSectionMenu = function (data, event) {
		if (this.selectedSectionMenu() == data.UIcontainerID) {
			this.selectedSectionMenu("");
		} else {
			this.selectedSectionMenu(data.UIcontainerID);
		}
		this.selectContainer(data, event, true);
		event.stopPropagation();
	}
}

var CommentsModel = function () {
	this.RelatedContentStatus = ko.observable();
	this.commentsThreads = ko.observableArray([]);
	this.headingName = ko.observable('');
	this.totalComments = ko.observable(0);
	this.neg_closeDate = ko.observable();
	this.neg_status = ko.observable('');
	this.neg_createdDate = ko.observable();
}

var CommentsThreadModel = function () {
	this.comments = ko.observableArray([]);
	this.heading = ko.observable();
	this.commentContent = ko.observable();
	this.status = ko.observable();
	this.threadID = ko.observable();
}

var CommentModel = function () {
	this.ClauseID = ko.observable();
	this.ContainingClauseID = ko.observable();
	this.ContainingSectionID = ko.observable();
	this.InitialContainingClauseID = ko.observable();
	this.InitialContainingSectionID = ko.observable();
	this.Content = ko.observable();
	this.PreviousComment = ko.observable();
	this.Status = ko.observable();
	this.UniqueID = ko.observable();
	this.ID = ko.observable();
	this.ItemID = ko.observable();
	this.createdByUserID = ko.observable();
	this.createdByUsername = ko.observable();
	this.createdDate = ko.observable();
	this.sameUser = ko.observable(false);
}

var SharedUsersModel = function () {
	this.usersList = ko.observableArray([]);
}

var UserModel = function () {
	this.ID = ko.observable();
	this.userid = ko.observable();
	this.username = ko.observable();
	this.status = ko.observable();
}
var l_sectionandclause_model = new SectionsAndClausesDataModel();
var l_comments_model = new CommentsModel();
var l_comments_thread_model = new CommentsThreadModel();
var l_shared_users_model = new SharedUsersModel();
var l_contract_Neg_rule_result = new Map();
var loggedUserID;
var contractID;
var _Initial_Containing_SectionID;
var l_current_numberingFormat = _DEFAULT_NUMBERING_FORMAT;


$(document).ready(function () {
	var i_locale = getlocale();
	translateLabels("com/opentext/apps/contractcenter/ContractNegotiation/ContractNegotiation", i_locale, true);
	var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
	loadRTLIfRequired(i_locale, rtl_css);


	$('[src*="ExtContact_SectionsClauses.htm"]', window.parent.parent.document).parent().css('padding-left', '0px');
	createToastDiv();
	relatedContentItemID = getUrlParameterValue("instanceId", null, true);
	contractDetailsItemID1 = getUrlParameterValue("contractDetails", null, true);
	loggedUserID = getUrlParameterValue('userid', null, true);
	readRelatedContentStatus(relatedContentItemID);



	$(document).on('click', '#logoUpload-ok', function () {
		var imgURL = "../../../../../logo/" + $('#logoUpload-input').val();
		$("#contractLogo > img").attr('src', imgURL);
	});

	$('#sectionList').on('change', function (e) {
		if (e.originalEvent !== undefined) {
			selectSection($(this).val());
		}
	});

	ko.applyBindings(l_comments_model, document.getElementById("div_CommentsList"));
	ko.applyBindings(l_shared_users_model, document.getElementById("div_SharedUserList"));

});

function selectSection(iValue, bSetSelection, bSetValue, bNoScrolling) {
	$(".droptarget_section").css('border', '');
	$(".droptarget_clause").css('border', '');
	var l_sectionElement = document.getElementById(iValue + '_Container');
	if (l_sectionElement) {
		if (!bNoScrolling) {
			$('#m_secandcls').animate({
				scrollTop: l_sectionElement.offsetTop - 40
			}, 500);
		}
		if (bSetSelection) {
			sectionList.selectedSectionOrder = l_sectionElement.getAttribute("sectionorder");
		}
		if (bSetValue) {
			sectionList.value = iValue;
		}
		$('#' + iValue + '_Container').css({ 'border-style': 'solid', 'border-color': 'rgba(3, 122, 252, 0.89)' });
	}
}

function getID1fromItemID1(ItemID1) {
	return ItemID1.substring(ItemID1.split(".", 2).join(".").length + 1);
}

function readRelatedContentStatus(relatedContentItemID) {
	var relatedContentID1 = getID1fromItemID1(relatedContentItemID);
	$.cordys.ajax({
		method: "GetRelatedContentStatus",
		namespace: "http://schemas.opentext.com/apps/ContractNegotiation/19.4",
		parameters:
		{
			"relatedContentID1": relatedContentID1
		},
		success: function (responseSuccess) {
			if (responseSuccess) {
				l_sectionandclause_model.RelatedContentStatus(getTextValue(responseSuccess.contentStatus));
				l_comments_model.RelatedContentStatus(getTextValue(responseSuccess.contentStatus));
				contractID = getTextValue(responseSuccess.contractID);
				getContractNegRuleResultMap(function () {
					loadSectionsAndClauses(relatedContentItemID);
				});

			} else {
				notifyError(responseSuccess, 10000);
			}
		},
		error: function (responseFailure) {
			notifyError(responseFailure, 10000);
			return false;
		}
	});
}

function _populateNegotContainerRuleResult(SCMappingsFromRead) {
	var conditionInstRes = cc_conditionauth_util.processConditionalCont(_populateConditionContData(SCMappingsFromRead), l_contract_Neg_rule_result);
	_populateVisibleContainers(conditionInstRes, SCMappingsFromRead);
}


function _populateVisibleContainers(conditionInstRes, allContainers) {
	allContainers.forEach(container => {
		container.showContainer = 'true';
		if (conditionInstRes.has(container["ContainingClauses-id"].Id2) && !conditionInstRes.get(container["ContainingClauses-id"].Id2)) {
			container.showContainer = 'false';
		}
	})
}

function _populateConditionContData(allContainers) {
	var l_input_conditionInstMap = new Map();
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



function getContractNegRuleResultMap(callBack) {
	// var relatedContentID1 = getID1fromItemID1(relatedContentItemID);
	$.cordys.ajax({
		method: "GetRuleResultbyRelatedContentItemID",
		namespace: "http://schemas.opentext.com/apps/ContractNegotiation/19.4",
		parameters:
		{
			"relatedContentItemID": relatedContentItemID
		}
	}).done(function (data) {
		var ruleResult = data.tuple.old.GetRuleResultbyRelatedContentItemID.GetRuleResultbyRelatedContentItemID.RuleResult;
		if (ruleResult && ruleResult.length > 0) {
			data.RuleResult.forEach(ele => {
				l_contract_Neg_rule_result.set(ele.Rule["Rule-id"].Id, ele.Result);
			});
		} else if (ruleResult) {
			l_contract_Neg_rule_result.set(ruleResult.Rule["Rule-id"].Id, ruleResult.Result);
		}
		callBack();
	}).fail(function (error) {

	});
}

function loadSectionsAndClauses() {
	SectionsAndClausesDataModel = $.cordys.ajax(
		{
			namespace: "http://schemas.opentext.com/apps/ContractNegotiation/19.4",
			method: "GetContentbyRelatedContentItemID",
			parameters: {
				"relatedContentItemID": relatedContentItemID
			},
			success: function (data) {
				if (data) {
					$('#loadingMsg').css('display', 'none');
					$('#div_SectionsClauses').css('display', 'block');
					var SCMappingsFromRead;
					var l_scmappings;
					if (!data.tuple.old.GetContentbyRelatedContentItemID.GetContentbyRelatedContentItemID.GetTemplateDetailsResponse.TemplateDetails.Details.GCTemplate.DefaultContainingSection) {
						_IS_NEW_SCHEMA = false;
					}
					if (data.tuple.old.GetContentbyRelatedContentItemID.GetContentbyRelatedContentItemID.GetTemplateDetailsResponse.TemplateDetails.ContainingClauses.FindZ_INT_SectionClauseMappingListResponse) {
						l_scmappings = data.tuple.old.GetContentbyRelatedContentItemID.GetContentbyRelatedContentItemID.GetTemplateDetailsResponse.TemplateDetails.ContainingClauses.FindZ_INT_SectionClauseMappingListResponse.ContainingClauses;
					} else {
						l_scmappings = data.tuple.old.GetContentbyRelatedContentItemID.GetContentbyRelatedContentItemID.GetTemplateDetailsResponse.TemplateDetails.ContainingClauses.ContainingClauses;
					}
					if (l_scmappings) {
						if (l_scmappings.length) {
							SCMappingsFromRead = l_scmappings;
						}
						else {
							SCMappingsFromRead = [];
							SCMappingsFromRead.push(l_scmappings);
						}
						var styleProp = data.tuple.old.GetContentbyRelatedContentItemID.GetContentbyRelatedContentItemID.GetTemplateDetailsResponse.TemplateDetails.Details.GCTemplate.StylingAttributes;
						if (styleProp && !styleProp["@nil"] && JSON.parse(styleProp)) {
							var styleJson = JSON.parse(styleProp);
							l_current_numberingFormat = styleJson.numberingStyle;

							if (styleJson.cascading && _CASCADE_OFF === styleJson.cascading) {
								l_sectionandclause_model.cascadeDocLevel(_CASCADE_OFF);
							} else {
								l_sectionandclause_model.cascadeDocLevel(_CASCADE_ON);
							}
						}
						var SCMappingsTemp = [];
						l_sectionandclause_model.SCMappingsToBind.removeAll();
						_populateNegotContainerRuleResult(SCMappingsFromRead);
						_populateRootSectionsAndClauses(l_sectionandclause_model.SCMappingsToBind, SCMappingsFromRead,
							data.tuple.old.GetContentbyRelatedContentItemID.GetContentbyRelatedContentItemID.GetTemplateDetailsResponse.TemplateDetails.Details.GCTemplate.DefaultContainingSection);

						SCMappingsTemp.forEach(function (iSection) {
							l_sectionandclause_model.SCMappingsToBind.push(iSection);
						});
						l_sectionandclause_model.optionsCaption(undefined);
						ko.applyBindings(l_sectionandclause_model, document.getElementById("m_secandcls"));
						ko.applyBindings(l_sectionandclause_model, document.getElementById("sectionHeader"));
					}
					else {
						l_sectionandclause_model.SCMappingsToBind.removeAll();
						l_sectionandclause_model.optionsCaption("No sections added");
						ko.applyBindings(l_sectionandclause_model, document.getElementById("m_secandcls"));
						ko.applyBindings(l_sectionandclause_model, document.getElementById("sectionHeader"));
					}
					if (l_current_numberingFormat) {
						$(".decimal-numbering").removeClass("decimal-numbering").addClass(l_current_numberingFormat + "-numbering");
						$(".decimal-numbering-ol-li").removeClass("decimal-numbering-ol-li").addClass(l_current_numberingFormat + "-numbering-ol-li");
					}
				}
			},
			error: function (responseFailure) {
				return false;
			}
		});
}

function addNegComments(l_itemToComment) {
	headingName = (l_itemToComment.type === _CLAUSE) ? l_itemToComment.clauseName() : l_itemToComment.sectionName();
	tempInitialContainingSectionID = l_itemToComment.initialContainingSectionID;
	$("#div_addNegCommentsModel").modal();
	$('button#btn_postComment').off("click");
	$('button#btn_postComment').on('click', function (_event) {
		if ($("#textarea_message_to_send").val() == "") {
			notifyError(getTranslationMessage("Comment cannot be empty."), 10000);
			$('#input_threadHeader').val('');
			$('#textarea_message_to_send').val('');
		} else {
			$.cordys.ajax({
				method: "CreateRelatedComments",
				namespace: "http://schemas/OpenTextContractNegotiation/ContractDetails/operations",
				parameters:
				{
					"ContractDetails-id":
					{
						"ItemId": contractDetailsItemID1
					},
					"RelatedComments-create":
					{
						"ContainingClauseID": l_itemToComment.containingClauseID ? l_itemToComment.containingClauseID : "",
						"ContainingSectionID": (!_IS_NEW_SCHEMA && l_itemToComment.containingSectionID) ? l_itemToComment.containingSectionID : "",
						"InitialContainingClauseID": l_itemToComment.initialContainingClauseID ? l_itemToComment.initialContainingClauseID : "",
						"InitialContainingSectionID": (!_IS_NEW_SCHEMA && l_itemToComment.initialContainingSectionID) ? l_itemToComment.initialContainingSectionID : "",
						"ClauseID": l_itemToComment.clauseID ? l_itemToComment.clauseID : "",
						"Heading": $("#input_threadHeader").val(),
						"Content": $("#textarea_message_to_send").val(),
						"RelatedContractContent":
						{
							"RelatedContractContent-id":
							{
								"ItemId1": relatedContentItemID
							}
						},
					}
				},
				success: function (responseSuccess) {
					if (responseSuccess) {
						$('#input_threadHeader').val('');
						$('#textarea_message_to_send').val('');
						viewSectionComments(tempInitialContainingSectionID, responseSuccess.RelatedComments.InitialContainingClauseID, headingName)
					} else {
						notifyError(getTranslationMessage("An error occurred while adding comment. Contact the administrator."), 10000);
					}
				},
				error: function (responseFailure) {
					notifyError(getTranslationMessage("An error occurred while adding comment. Contact the administrator."), 10000);
					return false;
				}
			});
		}
	});
}
function viewClauseComments(initialContainingClauseID, headingName) {
	$("#div_CommentsAndSharedUserList").css("display", "block");
	$("#div_SectionsClauses").addClass("col-md-8 col-xs-8");
	loadComments(null, initialContainingClauseID, null, headingName);
}
function viewSectionComments(initialContainingSectionID, initialContainingClauseID, sectionName) {
	$("#div_CommentsAndSharedUserList").css("display", "block");
	$("#div_SectionsClauses").addClass("col-md-8 col-xs-8");
	_Initial_Containing_SectionID = initialContainingSectionID;
	loadComments(initialContainingSectionID, initialContainingClauseID, null, sectionName);
}
function viewAllComments() {
	$("#div_CommentsAndSharedUserList").css("display", "block");
	$("#div_SectionsClauses").addClass("col-md-8 col-xs-8");
	_Initial_Containing_SectionID = "";
	loadComments(null, null, null, "All comments");
}

function replyCommentThread(threadModel) {
	if (event.srcElement.parentElement.firstElementChild.value == "") {
		notifyError(getTranslationMessage("Please enter a value in comment area"), 3000);
	}
	else {
		$.cordys.ajax({
			method: "CreateRelatedComments",
			namespace: "http://schemas/OpenTextContractNegotiation/ContractDetails/operations",
			parameters:
			{
				"ContractDetails-id":
				{
					"ItemId": contractDetailsItemID1
				},
				"RelatedComments-create":
				{
					"ContainingClauseID": threadModel.comments()[threadModel.comments().length - 1].ContainingClauseID(),
					"ContainingSectionID": threadModel.comments()[threadModel.comments().length - 1].ContainingSectionID(),
					"InitialContainingClauseID": threadModel.comments()[threadModel.comments().length - 1].InitialContainingClauseID(),
					"InitialContainingSectionID": threadModel.comments()[threadModel.comments().length - 1].InitialContainingSectionID(),
					"ClauseID": threadModel.comments()[threadModel.comments().length - 1].ClauseID(),
					"PreviousComment": threadModel.comments()[threadModel.comments().length - 1].ID(),
					"UniqueID": threadModel.comments()[threadModel.comments().length - 1].UniqueID(),
					"Content": threadModel.commentContent(),
					"RelatedContractContent":
					{
						"RelatedContractContent-id":
						{
							"ItemId1": relatedContentItemID
						}
					},
				}
			},
			success: function (responseSuccess) {
				if (responseSuccess) {
					threadModel.commentContent('');
					var comment = formCommentModel(responseSuccess.RelatedComments);
					threadModel.status(responseSuccess.RelatedComments.Status == "ACTIVE" ? "OPEN" : responseSuccess.RelatedComments.Status);
					threadModel.comments.push(comment);
					$('#textarea_message_to_send').val('');
				} else {
					notifyError(getTranslationMessage("An error occured while replying to the comment"), 10000);
				}
			},
			error: function (responseFailure) {
				notifyError(getTranslationMessage("An error occured while replying to the comment"), 10000);
				return false;
			}
		});
	}
}
function refreshComments(data) {
	loadComments(_Initial_Containing_SectionID, "", "", data.headingName());
}
function loadComments(initialContainingSectionID, initialContainingClauseID, threadID, headingName) {
	$(".section_Iterator").removeClass("highlightSectionorClause");
	$(".clause_Iterator").removeClass("highlightSectionorClause");
	$(".panel-heading").removeClass("highlightSectionorClause");
	parameters = {};
	// parameters.contentItemID = relatedContentItemID;
	parameters.contractID = contractID;
	if (initialContainingClauseID)
		parameters.initialContainingClauseID = initialContainingClauseID;
	if (initialContainingSectionID)
		parameters.initialContainingSectionID = initialContainingSectionID;
	if (threadID)
		parameters.threadID = threadID;
	$.cordys.ajax(
		{
			namespace: "http://schemas.opentext.com/apps/ContractNegotiation/19.4",
			method: "GetNegotiationComments",
			parameters: parameters,
			success: function (data) {
				if (data) {
					$('#loadingMsg').css('display', 'none');
					relatedComments = [];
					if (data.data) {
						relatedComments = data.data.RelatedComments;
					}
					if (relatedComments) {
						$('#div_NoComments').css('display', 'none');
						$('#div_CommentsResults').css('display', 'block');
						l_comments_model.commentsThreads.removeAll();
						l_comments_model.headingName(headingName);
						var iThread = 0;
						var threadMap = [];
						l_comments_model.totalComments(relatedComments.length);
						if (relatedComments.length) {
							for (var i = 0; i < relatedComments.length; i++) {
								if (relatedComments[i]['RelatedComments-id'].Id1 === relatedComments[i].UniqueID) {
									var thread = new CommentsThreadModel();
									iThread++;
									// thread.heading('Thread' + iThread);
									//thread.heading(relatedComments[i].Heading ? relatedComments[i].Heading.substring(0, 15) + '...' : relatedComments[i].Content.substring(0, 15) + '...');
									if (relatedComments[i].Heading && relatedComments[i].Heading["@nil"] !== 'true') {
										thread.heading(relatedComments[i].Heading.length > 15 ? relatedComments[i].Heading.substring(0, 15) + '...' : relatedComments[i].Heading);
									} else {
										thread.heading(relatedComments[i].Content.length > 15 ? relatedComments[i].Content.substring(0, 15) + '...' : relatedComments[i].Content);
									}
									thread.status(relatedComments[i].Status == "ACTIVE" ? "OPEN" : relatedComments[i].Status);
									thread.threadID(relatedComments[i]['RelatedComments-id'].Id1);
									l_comments_model.commentsThreads.push(thread);
									if (!threadMap[relatedComments[i].UniqueID]) {
										threadMap[relatedComments[i].UniqueID] = thread;
									}
								}
								var comment = formCommentModel(relatedComments[i]);
								threadMap[relatedComments[i].UniqueID].comments.push(comment);
							}
						} else {
							if (relatedComments['RelatedComments-id'].Id1 === relatedComments.UniqueID) {
								var thread = new CommentsThreadModel();
								iThread++;
								//thread.heading(relatedComments.Heading ? relatedComments.Heading.substring(0, 15) + '...' : relatedComments.Content.substring(0, 15) + '...');
								if (relatedComments.Heading && relatedComments.Heading["@nil"] !== 'true') {
									thread.heading(relatedComments.Heading.length > 15 ? relatedComments.Heading.substring(0, 15) + '...' : relatedComments.Heading);
								} else {
									thread.heading(relatedComments.Content.length > 15 ? relatedComments.Content.substring(0, 15) + '...' : relatedComments.Content);
								}
								thread.status(relatedComments.Status == "ACTIVE" ? "OPEN" : relatedComments.Status);
								thread.threadID(relatedComments['RelatedComments-id'].Id1);
								l_comments_model.commentsThreads.push(thread);
								if (!threadMap[relatedComments.UniqueID]) {
									threadMap[relatedComments.UniqueID] = thread;
								}
							}
							var comment = formCommentModel(relatedComments);
							threadMap[relatedComments.UniqueID].comments.push(comment);
						}

					}
					else {
						$('#div_NoComments').css('display', 'block');
						$('#div_CommentsResults').css('display', 'none');
						l_comments_model.commentsThreads.removeAll();
					}
				}
				getRelatedContractContent();
			},
			error: function (responseFailure) {
				return false;
			}
		});
}
function formCommentModel(relatedComment) {
	var comment = new CommentModel();
	comment.ContainingClauseID(relatedComment.ContainingClauseID);
	comment.ContainingSectionID(relatedComment.ContainingSectionID);
	comment.InitialContainingClauseID(relatedComment.InitialContainingClauseID);
	comment.InitialContainingSectionID(relatedComment.InitialContainingSectionID);
	comment.Content(relatedComment.Content);
	comment.PreviousComment(relatedComment.PreviousComment);
	comment.UniqueID(relatedComment.UniqueID);
	comment.Status(relatedComment.Status);
	comment.ClauseID(relatedComment.ClauseID);
	comment.ID(relatedComment['RelatedComments-id'].Id1);
	comment.ItemID(relatedComment['RelatedComments-id'].ItemId1);
	if (relatedComment.CreatedBy) {
		comment.createdByUserID(getTextValue(relatedComment.CreatedBy.UserId));
		if (loggedUserID === getTextValue(relatedComment.CreatedBy.UserId)) {
			comment.sameUser(true);
		}
		if (relatedComment.CreatedBy.IdentityDisplayName) {
			comment.createdByUsername(getTextValue(relatedComment.CreatedBy.IdentityDisplayName));
		} else if (relatedComment.CreatedBy.FullName) {
			comment.createdByUsername(getTextValue(relatedComment.CreatedBy.FullName));
		} else {
			comment.createdByUsername(getTextValue(relatedComment.CreatedBy.UserId));
		}
	} else {
		comment.sameUser(true);
		comment.createdByUsername(loggedUserID.substring(0, loggedUserID.indexOf("@")));
	}

	if (relatedComment.Tracking && relatedComment.Tracking.CreatedDate) {
		comment.createdDate(moment.utc(relatedComment.Tracking.CreatedDate.replace("Z", "")).local().format('MM/DD/YYYY, hh:mm A'));
	}
	return comment;
}

function highlightClauseorSection(l_event, l_data) {
	var l_ContainingSectionID = getTextValue(l_data.comments()[0].InitialContainingSectionID());
	var l_ContainingClauseID = getTextValue(l_data.comments()[0].InitialContainingClauseID());
	if (l_ContainingClauseID) {
		$(".section_Iterator").removeClass("highlightSectionorClause");
		$(".clause_Iterator").removeClass("highlightSectionorClause");
		$(".panel-heading").removeClass("highlightSectionorClause");
		if ($("#clause" + l_ContainingClauseID + "_Container")[0]) {
			$('#m_secandcls').animate({
				scrollTop: $("#clause" + l_ContainingClauseID + "_Container")[0].offsetTop - 40
			}, 500);
		}
		$("#clause" + l_ContainingClauseID + "_Container").addClass("highlightSectionorClause");
		$(l_event.currentTarget.getElementsByClassName("panel-heading")[0]).addClass("highlightSectionorClause");
	} else if (l_ContainingSectionID) {
		$(".section_Iterator").removeClass("highlightSectionorClause");
		$(".clause_Iterator").removeClass("highlightSectionorClause");
		$(".panel-heading").removeClass("highlightSectionorClause");
		if ($("#section" + l_ContainingSectionID + "_Container")[0]) {
			$('#m_secandcls').animate({
				scrollTop: $("#section" + l_ContainingSectionID + "_Container")[0].offsetTop - 40
			}, 500);
		}
		$("#section" + l_ContainingSectionID + "_Container").addClass("highlightSectionorClause");
		$(l_event.currentTarget.getElementsByClassName("panel-heading")[0]).addClass("highlightSectionorClause");
	}
}

function closeCommentsAndSharedUserList() {
	$("#div_CommentsAndSharedUserList").css("display", "none");
	$("#div_SectionsClauses").removeClass("col-md-8 col-xs-8");
	$(".section_Iterator").removeClass("highlightSectionorClause");
	$(".clause_Iterator").removeClass("highlightSectionorClause");
	$(".panel-heading").removeClass("highlightSectionorClause");
}

function loadSharedUsersList() {
	if (l_shared_users_model.usersList().length > 0)
		return true;
	parameters = {};
	parameters.contentItemID = relatedContentItemID;
	$.cordys.ajax(
		{
			namespace: "http://schemas.opentext.com/apps/ContractNegotiation/19.4",
			method: "GetMappedExtContactsByContentItemId",
			parameters: parameters,
			success: function (data) {
				if (data) {
					$('#loadingMsg').css('display', 'none');
					sharedUsers = [];
					sharedUsers = data.data.FindZ_INT_RelatedExternalContactsListResponse.RelatedContentAndContactsMapping;
					if (sharedUsers) {
						l_shared_users_model.usersList.removeAll();
						if (sharedUsers.length) {
							for (var i = 0; i < sharedUsers.length; i++) {
								var user = new UserModel();
								user.ID(getTextValue(sharedUsers[i]['RelatedContentAndContactsMapping-id'].Id1));
								user.userid(getTextValue(sharedUsers[i].RelatedExternalContact.ExternalUserID));
								user.username(getTextValue(sharedUsers[i].RelatedExternalContact.ExternalUserName));
								user.status(getUserStatus(getTextValue(sharedUsers[i].Status)));
								l_shared_users_model.usersList.push(user);
							}
						} else {
							var user = new UserModel();
							user.ID(getTextValue(sharedUsers['RelatedContentAndContactsMapping-id'].Id1));
							user.userid(getTextValue(sharedUsers.RelatedExternalContact.ExternalUserID));
							user.username(getTextValue(sharedUsers.RelatedExternalContact.ExternalUserName));
							user.status(getUserStatus(getTextValue(sharedUsers.Status)));
							l_shared_users_model.usersList.push(user);
						}
					}
					else {
						l_shared_users_model.usersList.removeAll();
					}
				}
			},
			error: function (responseFailure) {
				return false;
			}
		});
}
function getUserStatus(iStatus) {
	var lStatus;
	switch (iStatus) {
		case 'OPEN':
			lStatus = "Open";
			break;
		case 'APPROVED':
			lStatus = "Accepted";
			break;
		case 'REJECTED':
			lStatus = "Rejected";
			break;
		case 'DL_ABSOLUTE':
			lStatus = "Closed";
			break;
		case 'SR_ABSOLUTE':
			lStatus = "Closed";
			break;
		case 'SN_ABSOLUTE':
			lStatus = "Closed";
			break;
	}
	return lStatus;
}

function getRelatedContractContent() {
	if (relatedContentItemID != null) {
		$.cordys.ajax({
			method: "ReadRelatedContractContent",
			namespace: "http://schemas/OpenTextContractNegotiation/ContractDetails.RelatedContractContent/operations",
			parameters:
			{
				"RelatedContractContent-id":
				{
					'ItemId1': relatedContentItemID
				}
			}
		}).done(function (data) {
			if (data.RelatedContractContent != null) {
				l_comments_model.neg_createdDate(moment(data.RelatedContractContent.Tracking.CreatedDate).format('MM/DD/YYYY'));
				if (data.RelatedContractContent.Deadlines.CloseNegotiation_DueDate != null) {
					l_comments_model.neg_closeDate(moment(data.RelatedContractContent.Deadlines.CloseNegotiation_DueDate).format('MM/DD/YYYY'));
				}
				else {
					l_comments_model.neg_closeDate("");
				}
				if (data.RelatedContractContent.Status == "OPEN") {
					l_comments_model.neg_status("Inprogress");
				}
				else if (data.RelatedContractContent.Status == "CLOSEREQUESTED") {
					l_comments_model.neg_status("Close requested");
				}
				else if (data.RelatedContractContent.Status == "CLOSED") {
					l_comments_model.neg_status("Closed");
				}
			}
		}).fail(function (error) {

		});
	}
	else {
		notifyError(getTranslationMessage("Error while reading contract details."), 3000);
	}
}

function _populateRootSectionsAndClauses(SCMappings, SCMappingsFromRead, newRecord) {
	for (var i = 0; i < SCMappingsFromRead.length; i++) {
		var sectionOrClauseTemp = {};
		var sectionOrClauseFromRead = SCMappingsFromRead[i];
		if (sectionOrClauseFromRead.LinkedSection && !sectionOrClauseFromRead.ParentContainer) {
			_populateSection(sectionOrClauseTemp, sectionOrClauseFromRead, SCMappingsFromRead, null);
			SCMappings.push(sectionOrClauseTemp);
		} else if (sectionOrClauseFromRead.LinkedClause && !sectionOrClauseFromRead.ParentContainer) {
			_populateClause(sectionOrClauseTemp, sectionOrClauseFromRead, SCMappingsFromRead, null);
			_addToReplaceContainerMap(
				sectionOrClauseTemp,
				SCMappings(),
				sectionOrClauseFromRead.RelatedCondition ? getTextValue(sectionOrClauseFromRead.RelatedCondition.Action) : "",
				sectionOrClauseFromRead.RelatedCondition ? sectionOrClauseFromRead.RelatedCondition.SourceContainer["ContainingClauses-id"].Id2 : ""
			);
			if (!newRecord) {
				_populateOldSectionRecord(SCMappings, sectionOrClauseFromRead, sectionOrClauseTemp);
			} else {

				// Adding code for drop down selection.
				sectionOrClauseTemp.sectionName = sectionOrClauseTemp.clauseName;
				/////////////////////////////////// Just for selection drop down.

				SCMappings.push(sectionOrClauseTemp);
			}
		}
	}
	_sortArrayOnClauseOrder(SCMappings);
	_addChildrenReplaceContainers();
}

function _addToReplaceContainerMap(replaceClause, parentContainer, action, sourceContainerId) {
	if (action === 'REPLACE') {
		replaceContainersMap.set(replaceClause.containingClauseID, {
			sourceContainerId: sourceContainerId, replaceContainer: replaceClause, parentContainer: parentContainer,
			sourceContainer: null
		});
	}
};

function _addChildrenReplaceContainers() {
	replaceContainersMap.forEach((val, key) => {
		if (val.parentContainer) {
			val.parentContainer.forEach(cont => {
				if (cont.containingClauseID === val.sourceContainerId) {
					val.sourceContainer = cont;
					cont.container().forEach(ele => {
						val.replaceContainer.container.push(ele);
					});
				}
			})
		}
	});
}


function _populateOldSectionRecord(SCMappings, clauseFromRead, clauseTemp) {
	var sectionArr = SCMappings().filter(section => section.type === _SECTION ? (section.sectionOrder === (+clauseFromRead.Owner.SectionOrder)) : false);
	var sectionTemp = {};
	if (Array.isArray(sectionArr) && sectionArr.length > 0) {
		sectionTemp = sectionArr[0];
	}
	if (clauseFromRead.Owner.SectionOrder) {
		sectionTemp.sectionOrder = clauseFromRead.Owner.SectionOrder ? parseInt(getTextValue(clauseFromRead.Owner.SectionOrder)) : 0;
		sectionTemp.clauseOrder = sectionTemp.sectionOrder;
	}
	addNumberingAndCascadeInfo(clauseTemp, null, null, l_sectionandclause_model, l_current_numberingFormat);
	if (Array.isArray(sectionArr) && sectionArr.length === 0) {
		sectionTemp.type = _SECTION;
		sectionTemp.showContainer = 'true';
		addNumberingAndCascadeInfo(sectionTemp, null, null, l_sectionandclause_model, l_current_numberingFormat);
		if (clauseFromRead.Owner.Name) {
			sectionTemp.sectionName = clauseFromRead.Owner.Name ? ko.observable(getTextValue(clauseFromRead.Owner.Name)) : "";
			sectionTemp.sectionName.subscribe(function (_newText) {
				isDirty = true;
			});
		}
		sectionTemp.containingSectionID = clauseFromRead['ContainingClauses-id'].Id1;
		if (clauseFromRead.Owner.InitialContainingSectionID) {
			sectionTemp.initialContainingSectionID = getTextValue(clauseFromRead.Owner.InitialContainingSectionID);
			sectionTemp.UIcontainerID = 'section' + sectionTemp.initialContainingSectionID;
			sectionTemp.containerID = 'section' + sectionTemp.initialContainingSectionID;
		}
		else {
			sectionTemp.initialContainingSectionID = clauseFromRead['ContainingClauses-id'].Id1;
			sectionTemp.UIcontainerID = 'section' + clauseFromRead['ContainingClauses-id'].Id1;
			sectionTemp.containerID = 'section' + clauseFromRead['ContainingClauses-id'].Id1;
		}
		sectionTemp.isNew = false;
		if (sectionTemp.container == undefined) {
			sectionTemp.container = ko.observableArray([]);
		}
	}
	clauseTemp.sectionOrder = sectionTemp.sectionOrder;
	clauseTemp.showContainer = 'true';
	clauseTemp.type = _CLAUSE;
	clauseTemp.sectionName = sectionTemp.sectionName;
	clauseTemp.initialContainingSectionID = sectionTemp.initialContainingSectionID;
	// For making all at same page.old and new section object.
	sectionTemp.initialContainingClauseID = "";
	clauseTemp.clauseOrder = parseInt(clauseFromRead.ClauseOrder);
	if (clauseFromRead.InitialContainingClauseID) {
		clauseTemp.initialContainingClauseID = clauseFromRead.InitialContainingClauseID;
		clauseTemp.UIcontainerID = 'clause' + clauseFromRead.InitialContainingClauseID;
		clauseTemp.containerID = clauseFromRead.InitialContainingClauseID;
	}
	else {

		clauseTemp.initialContainingClauseID = clauseFromRead['ContainingClauses-id'].Id2;
		clauseTemp.UIcontainerID = 'clause' + clauseFromRead['ContainingClauses-id'].Id2;
		clauseTemp.containerID = clauseFromRead['ContainingClauses-id'].Id2;
	}
	sectionTemp.parentContainer = "";
	sectionTemp.container.push(clauseTemp);
	if (Array.isArray(sectionArr) && sectionArr.length === 0) {
		SCMappings.push(sectionTemp);
	}
}

function _populateSection(sectionTemp, sectionFromRead, SCMappingsFromRead, parentCascade) {
	if (sectionFromRead.LinkedSection) {
		sectionTemp.type = _SECTION;
		sectionTemp.showContainer = sectionFromRead.showContainer ? sectionFromRead.showContainer : 'true';
		addNumberingAndCascadeInfo(sectionTemp, sectionFromRead, parentCascade, l_sectionandclause_model, l_current_numberingFormat);
		if (sectionFromRead.LinkedSection.SectionOrder) {
			sectionTemp.sectionOrder = sectionFromRead.LinkedSection.SectionOrder ? parseInt(getTextValue(sectionFromRead.LinkedSection.SectionOrder)) : 0;
		}
		sectionTemp.clauseOrder = sectionFromRead.ClauseOrder;
		if (sectionFromRead.LinkedSection.Name) {
			sectionTemp.sectionName = sectionFromRead.LinkedSection.Name ? ko.observable(getTextValue(sectionFromRead.LinkedSection.Name)) : "";
			sectionTemp.sectionName.subscribe(function (_newText) {
				isDirty = true;
			});
		}
		if (sectionFromRead.LinkedSection.InitialContainingSectionID) {
			sectionTemp.initialContainingSectionID = getTextValue(sectionFromRead.LinkedSection.InitialContainingSectionID);
		}
		else {
			sectionTemp.initialContainingSectionID = getTextValue(sectionFromRead.LinkedSection["ContainingSections-id"].Id1);
		}

		if (sectionFromRead.LinkedSection["ContainingSections-id"]) {
			sectionTemp.containingSectionID = getTextValue(sectionFromRead.LinkedSection["ContainingSections-id"].Id1);
		}
		else {
			sectionTemp.containingSectionID = "";
		}

		sectionTemp.isNew = false;
		if (sectionFromRead.InitialContainingClauseID) {
			sectionTemp.UIcontainerID = 'clause' + sectionFromRead.InitialContainingClauseID;
			sectionTemp.containerID = sectionFromRead.InitialContainingClauseID;
			sectionTemp.initialContainingClauseID = sectionFromRead.InitialContainingClauseID;
		} else {
			sectionTemp.UIcontainerID = 'clause' + getTextValue(sectionFromRead.LinkedSection["ContainingSections-id"].Id1);
			sectionTemp.containerID = getTextValue(sectionFromRead.LinkedSection["ContainingSections-id"].Id1);
			sectionTemp.initialContainingClauseID = "";
		}

		sectionTemp.containingClauseID = sectionFromRead['ContainingClauses-id'].Id2;
		sectionTemp.clauseName = "";
		sectionTemp.clauseHTMLContent = ""
		sectionTemp.container = ko.observableArray([]);
		if (sectionFromRead.ParentContainer) {
			sectionTemp.parentContainer = sectionFromRead.ParentContainer['ContainingClauses-id'].Id2;
		}
		else {
			sectionTemp.parentContainer = "";
		}
		SCMappingsFromRead.forEach(sectionOrClause => {
			if (sectionOrClause.ParentContainer && (sectionOrClause.ParentContainer['ContainingClauses-id'].Id2 === sectionTemp.containingClauseID)) {
				var sectionOrClauseTemp = {};

				if (sectionOrClause.LinkedSection) {
					_populateSection(sectionOrClauseTemp, sectionOrClause, SCMappingsFromRead, sectionTemp.cascade());
					sectionTemp.container.push(sectionOrClauseTemp);
				} else if (sectionOrClause.LinkedClause) {
					_populateClause(sectionOrClauseTemp, sectionOrClause, SCMappingsFromRead, sectionTemp.cascade());
					sectionTemp.container.push(sectionOrClauseTemp);
					_addToReplaceContainerMap(
						sectionOrClauseTemp,
						sectionTemp.container(),
						sectionOrClause.RelatedCondition ? getTextValue(sectionOrClause.RelatedCondition.Action) : "",
						sectionOrClause.RelatedCondition ? sectionOrClause.RelatedCondition.SourceContainer["ContainingClauses-id"].Id2 : ""
					);
				}
			}
		});
	};
	_sortArrayOnClauseOrder(sectionTemp.container);
}

function _populateClause(clauseTemp, clauseFromRead, SCMappingsFromRead, parentCascade) {
	clauseTemp.type = _CLAUSE;
	clauseTemp.showContainer = clauseFromRead.showContainer ? clauseFromRead.showContainer : 'true';
	addNumberingAndCascadeInfo(clauseTemp, clauseFromRead, parentCascade, l_sectionandclause_model, l_current_numberingFormat);
	clauseTemp.clauseOrder = parseInt(clauseFromRead.ClauseOrder);
	if (clauseFromRead.InitialContainingClauseID) {
		clauseTemp.initialContainingClauseID = clauseFromRead.InitialContainingClauseID;
		clauseTemp.UIcontainerID = 'clause' + clauseFromRead.InitialContainingClauseID;
		clauseTemp.containerID = clauseFromRead.InitialContainingClauseID;
	}
	else {
		clauseTemp.UIcontainerID = 'clause' + clauseFromRead['ContainingClauses-id'].Id2;
		clauseTemp.containerID = clauseFromRead['ContainingClauses-id'].Id2;
		clauseTemp.initialContainingClauseID = clauseFromRead['ContainingClauses-id'].Id2;
	}

	clauseTemp.generatedClauseID = "";
	clauseTemp.initialContainingSectionID = 0;

	clauseTemp.containingClauseID = clauseFromRead['ContainingClauses-id'].Id2;
	clauseTemp.containingSectionID = clauseFromRead['ContainingClauses-id'].Id1;
	clauseTemp.clauseID = clauseFromRead.LinkedClause["GCClause-id"].Id;
	if (clauseFromRead.LinkedClause.ClauseType) {
		clauseTemp.clauseType = getTextValue(clauseFromRead.LinkedClause.ClauseType);
	}
	if (clauseFromRead.LinkedClause.Name) {
		clauseTemp.clauseName = ko.observable(getTextValue(clauseFromRead.LinkedClause.Name));
	} else {
		clauseTemp.clauseName = ko.observable("");
	}
	if (clauseFromRead.LinkedClause.HtmlContent) {
		clauseTemp.clauseHTMLContent = getTextValue(clauseFromRead.LinkedClause.HtmlContent);
	}
	else {
		clauseTemp.clauseHTMLContent = ""
	}
	clauseTemp.isNew = false;
	clauseTemp.isDirty = ko.observable(false);
	if (clauseTemp.clauseType === 'NONSTANDARD') {
		clauseTemp.isStandard = ko.observable(false);
		clauseTemp.showConvertToNonStandardBtn = ko.observable(false);
		clauseTemp.showReplacewithEqClauseBtn = ko.observable(false)
	}
	else {
		clauseTemp.isStandard = ko.observable(true);
		clauseTemp.showConvertToNonStandardBtn = ko.observable(true);
		clauseTemp.showReplacewithEqClauseBtn = ko.observable(true)
	}
	if (clauseFromRead.ParentContainer) {
		clauseTemp.parentContainer = clauseFromRead.ParentContainer['ContainingClauses-id'].Id2;
	}
	else {
		clauseTemp.parentContainer = "";
	}
	clauseTemp.sectionName = "";
	clauseTemp.container = ko.observableArray([]);
	SCMappingsFromRead.forEach(sectionOrClause => {
		if (sectionOrClause.ParentContainer && (sectionOrClause.ParentContainer['ContainingClauses-id'].Id2 === clauseTemp.containingClauseID)) {
			var sectionOrClauseTemp = {};
			if (sectionOrClause.LinkedSection) {
				_populateSection(sectionOrClauseTemp, sectionOrClause, SCMappingsFromRead, clauseTemp.cascade());
				clauseTemp.container.push(sectionOrClauseTemp);
			} else if (sectionOrClause.LinkedClause) {
				_populateClause(sectionOrClauseTemp, sectionOrClause, SCMappingsFromRead, clauseTemp.cascade());
				clauseTemp.container.push(sectionOrClauseTemp);
				_addToReplaceContainerMap(
					sectionOrClauseTemp,
					clauseTemp.container(),
					sectionOrClause.RelatedCondition ? getTextValue(sectionOrClause.RelatedCondition.Action) : "",
					sectionOrClause.RelatedCondition ? sectionOrClause.RelatedCondition.SourceContainer["ContainingClauses-id"].Id2 : ""
				);
			}
		}
	});
	_sortArrayOnClauseOrder(clauseTemp.container);
}

function _sortArrayOnClauseOrder(sectionOrClauseObsArr) {
	return sectionOrClauseObsArr ? sectionOrClauseObsArr.sort((e, e1) => e.clauseOrder - e1.clauseOrder) : null;
}


// Sections.

function togglePanel(panelID) {
	if ($('#Accordian_' + panelID).hasClass('collapsed')) {
		$('#Panel_' + panelID).addClass("in");
		$('#Accordian_' + panelID).removeClass("collapsed");
	}
	else {
		$('#Panel_' + panelID).removeClass("in");
		$('#Accordian_' + panelID).addClass("collapsed");
	}
}

// Comment Threads.

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


