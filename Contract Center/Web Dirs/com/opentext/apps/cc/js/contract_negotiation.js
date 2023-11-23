// Variables.
var contractDetailsItemID;
var relatedContentID1;
var loggedUserID;
var externalPartyItemID;
var checkedExternalcontacts = {};
var sharedExternalcontacts = {};
var _Initial_Containing_SectionID;

// Models.
var CommentsThreadModel = function () {
	this.comments = ko.observableArray([]);
	this.heading = ko.observable();
	this.commentContent = ko.observable();
	this.status = ko.observable();
	this.threadID = ko.observable();
}
var CommentsModel = function () {
	this.RelatedContentStatus = ko.observable();
	this.commentsThreads = ko.observableArray([]);
	this.headingName = ko.observable('');
	this.totalComments = ko.observable(0);
	this.contract_LifecycleState = ko.observable();
	this.contract_Z_INT_Status = ko.observable();
	this.neg_closeDate = ko.observable();
	this.neg_status = ko.observable('');
	this.plannedStartDate = ko.observable(null);
	this.neg_createdDate = ko.observable(null);
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

// Model variables.
var l_comments_thread_model = new CommentsThreadModel();
var l_comments_model = new CommentsModel();
var l_shared_users_model = new SharedUsersModel();

// Methods.

// Regresh comments.
function refreshComments(data) {
	loadComments(_Initial_Containing_SectionID, "", "", data.headingName());
}

// View section comments.
function viewSectionComments(initialContainingSectionID, initialContainingClauseID, sectionName) {
	hideClauseLibrary();
	$("#eqClauseList").css("display", "none");
	$("#div_CommentsAndSharedUserList").css("display", "block");
	$("#div_sectionsAndClausesContent").addClass("col-md-8 col-xs-8");
	_Initial_Containing_SectionID = initialContainingSectionID
	loadComments(initialContainingSectionID, initialContainingClauseID, null, sectionName);
}

// View Clause comments.
function viewClauseComments(initialContainingClauseID, headingName) {
	hideClauseLibrary();
	$("#eqClauseList").css("display", "none");
	$("#div_CommentsAndSharedUserList").css("display", "block");
	$("#div_sectionsAndClausesContent").addClass("col-md-8 col-xs-8");
	loadComments(null, initialContainingClauseID, null, headingName);
}

// View all comments.
function viewAllComments() {
	hideClauseLibrary();
	$("#eqClauseList").css("display", "none");
	$("#div_CommentsAndSharedUserList").css("display", "block");
	$("#div_sectionsAndClausesContent").addClass("col-md-8 col-xs-8");
	_Initial_Containing_SectionID = "";
	loadComments(null, null, null, "All comments");
	//_headerAdj(false);
}

// Get presentale user status.
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

// Clonse comments and userlist pane.
function closeCommentsAndSharedUserList() {
	$("#div_CommentsAndSharedUserList").css("display", "none");
	$("#div_sectionsAndClausesContent").removeClass("col-md-8 col-xs-8");
	$(".section_Iterator").removeClass("highlightSectionorClause");
	$(".clause_Iterator").removeClass("highlightSectionorClause");
	$(".panel-heading").removeClass("highlightSectionorClause");
	$('#editNegCloseDate').off('click');
	//_headerAdj(true);
}

// Reload shared users list.
function checkandReloadSharedUsersList() {
	if (l_shared_users_model.usersList().length > 0)
		return true;
	loadSharedUsersList();
}

// Forming a comments model.
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

// Services.

// Read negotiation details.
function readNegotiationDetails() {
	var contractID = contractItemId.substring(contractItemId.indexOf(".") + 1);
	$.cordys.ajax({
		method: "GetContractDetailsByContractID",
		namespace: "http://schemas.opentext.com/apps/ContractNegotiation/19.4",
		parameters:
		{
			'contractID': contractID
		}
	}).done(function (data) {
		if (data.data.FindZ_INT_ContractDetailsListResponse.ContractDetails) {
			contractDetailsItemID = data.data.FindZ_INT_ContractDetailsListResponse.ContractDetails['ContractDetails-id'].ItemId;
			relatedContentID1 = data.data.FindZ_INT_ContractDetailsListResponse.ContractDetails.CurrentNegotiationContentId;
		}
	}).fail(function (error) {

	});
}

// Update negotiation instance details.
function UpdateNegotiationInstance(updatedTemplateID) {
	if (l_sectionandclause_model.contract_LifecycleState == "Negotiation") {
		$.cordys.ajax({
			method: "UpdateSecClausesonContractSave",
			namespace: "http://schemas.opentext.com/apps/ContractNegotiation/19.4",
			parameters:
			{
				'contractID': contractItemId.substring(contractItemId.indexOf(".") + 1),
				'templateItemId': updatedTemplateID
			}
		}).done(function (data) {

		}).fail(function (error) {

		});
	}
}

// Add negotiation comments.
function addNegComments(l_itemToComment) {
	headingName = (l_itemToComment.type === _CLAUSE) ? l_itemToComment.clauseName() : l_itemToComment.sectionName();
	tempInitialContainingSectionID = l_itemToComment.initialContainingSectionID;
	if (contractDetailsItemID) {
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
							"ItemId": contractDetailsItemID
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
									"Id": contractDetailsItemID.substring(contractDetailsItemID.indexOf(".") + 1),
									"Id1": relatedContentID1
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
}

// Resolve a comments thread.
function resolveThread(threadModel, status) {
	if (threadModel) {
		$.cordys.ajax({
			method: "UpdateRelatedComments",
			namespace: "http://schemas/OpenTextContractNegotiation/ContractDetails.RelatedComments/operations",
			parameters:
			{
				"RelatedComments-id":
				{
					"Id": contractDetailsItemID.substring(contractDetailsItemID.indexOf(".") + 1),
					"Id1": threadModel.threadID()
				},
				"RelatedComments-update":
				{
					"Status": status
				}
			},
			success: function (responseSuccess) {
				if (responseSuccess) {
					threadModel.status(responseSuccess.RelatedComments.Status);
				} else {
					notifyError(getTranslationMessage("An error occurred while resolving thread. Contact the administrator."), 10000);
				}
			},
			error: function (responseFailure) {
				notifyError(getTranslationMessage("An error occurred while resolving thread. Contact the administrator."), 10000);
				return false;
			}
		});
	}
}

// Reply to a comment.
function replyCommentThread(threadModel) {
	if (event.srcElement.parentElement.firstElementChild.value == "") {
		notifyError(getTranslationMessage("Please enter a value in comment area"), 3000);
	}
	else {
		if (contractDetailsItemID) {
			$.cordys.ajax({
				method: "CreateRelatedComments",
				namespace: "http://schemas/OpenTextContractNegotiation/ContractDetails/operations",
				parameters:
				{
					"ContractDetails-id":
					{
						"ItemId": contractDetailsItemID
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
								"Id": contractDetailsItemID.substring(contractDetailsItemID.indexOf(".") + 1),
								"Id1": relatedContentID1
							}
						},
					}
				},
				success: function (responseSuccess) {
					if (responseSuccess) {
						threadModel.commentContent('');
						var comment = formCommentModel(responseSuccess.RelatedComments);
						threadModel.comments.push(comment);
						threadModel.status(threadModel.status() == "RESOLVED" ? "REOPENED" : threadModel.status());
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
}

// Load comments based on parameters.
function loadComments(initialContainingSectionID, initialContainingClauseID, threadID, headingName) {
	$(".section_Iterator").removeClass("highlightSectionorClause");
	$(".clause_Iterator").removeClass("highlightSectionorClause");
	$(".panel-heading").removeClass("highlightSectionorClause");
	parameters = {};
	parameters.contractID = contractItemId.substring(contractItemId.indexOf(".") + 1);
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
									//thread.heading(getTextValue(relatedComments[i].Heading) ? relatedComments[i].Heading.substring(0, 15) + '...' : relatedComments[i].Content.substring(0, 15) + '...');
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
								//thread.heading(getTextValue(relatedComments.Heading) ? relatedComments.Heading.substring(0, 15) + '...' : relatedComments.Content.substring(0, 15) + '...');
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
				l_comments_model.contract_LifecycleState(l_sectionandclause_model.contract_LifecycleState);
				l_comments_model.contract_Z_INT_Status(l_sectionandclause_model.contract_Z_INT_Status());
				if (l_comments_model.contract_Z_INT_Status() == "SentForNegotiation" || l_comments_model.contract_Z_INT_Status() == "RequestedForNegClosure") {
					getRelatedContractContent();
				}
				else {
					$("#requestNegClosure_div").hide();
					$('.comment-history').css('height', '71%');
				}

			},
			error: function (responseFailure) {
				return false;
			}
		});
}

// Load users list with whom contract is shared.
function loadSharedUsersList() {
	parameters = {};
	parameters.contractID = contractItemId.substring(contractItemId.indexOf(".") + 1);
	$.cordys.ajax(
		{
			namespace: "http://schemas.opentext.com/apps/ContractNegotiation/19.4",
			method: "GetMappedExtContactsByContractID",
			parameters: parameters,
			success: function (data) {
				if (data) {
					$('#loadingMsg').css('display', 'none');
					sharedUsers = [];
					if (data.data && data.data.FindZ_INT_RelatedExternalContactsListResponse && data.data.FindZ_INT_RelatedExternalContactsListResponse.RelatedContentAndContactsMapping) {
						sharedUsers = data.data.FindZ_INT_RelatedExternalContactsListResponse.RelatedContentAndContactsMapping;
						l_shared_users_model.usersList.removeAll();
						if (sharedUsers.length) {
							for (var i = 0; i < sharedUsers.length; i++) {
								var user = new UserModel();
								user.ID(getTextValue(sharedUsers[i]['RelatedContentAndContactsMapping-id'].Id1));
								user.userid(getTextValue(sharedUsers[i].RelatedExternalContact.ExternalUserID));
								user.username(getTextValue(sharedUsers[i].RelatedExternalContact.ExternalUserName));
								user.status(getUserStatus(getTextValue(sharedUsers[i].Status)));
								user.itemID = sharedUsers[i]['RelatedContentAndContactsMapping-id'].ItemId1
								l_shared_users_model.usersList.push(user);
							}
						} else {
							var user = new UserModel();
							user.ID(getTextValue(sharedUsers['RelatedContentAndContactsMapping-id'].Id1));
							user.userid(getTextValue(sharedUsers.RelatedExternalContact.ExternalUserID));
							user.username(getTextValue(sharedUsers.RelatedExternalContact.ExternalUserName));
							user.status(getUserStatus(getTextValue(sharedUsers.Status)));
							user.itemID = sharedUsers['RelatedContentAndContactsMapping-id'].ItemId1
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

// Add new user in a existed list of shared users.
function addNewUserforNegotiation() {
	$("#div_addNewUserforNegotiation").modal();
	$('ai-dialog-footer .btn-primary:contains("Ok")', window.parent.parent.document).attr("disabled", true);
	getExternalContactsCount();
	$('button#btn_cancelAddUser').on('click', function (_event) {
		clearExtContactsData();
		for (var extContact in checkedExternalcontacts) {
			delete checkedExternalcontacts[extContact];
		}
	});
	$('button#btn_OkAddUser').on('click', function (_event) {
		var externalUsers = [];
		for (var extContact in checkedExternalcontacts) {
			externalUser = {};
			externalUser.userID = extContact;
			externalUser.userName = checkedExternalcontacts[extContact];
			externalUsers.push(externalUser);
			delete checkedExternalcontacts[extContact];
		}
		$.cordys.ajax({
			method: "AddExternalContactToSharedList",
			namespace: "http://schemas.opentext.com/apps/ContractNegotiation/19.4",
			parameters: {
				"contractID": contractItemId.substring(contractItemId.indexOf(".") + 1),
				"externalUsers": { 'externalUser': externalUsers }
			},
		}).done(function (data) {
			loadSharedUsersList();
		}).fail(function (error) {
		});
	});
}

// Read related contract content from negotiation.
function getRelatedContractContent() {
	if (contractDetailsItemID != null && relatedContentID1 != null) {
		$.cordys.ajax({
			method: "ReadRelatedContractContent",
			namespace: "http://schemas/OpenTextContractNegotiation/ContractDetails.RelatedContractContent/operations",
			parameters:
			{
				"RelatedContractContent-id":
				{
					'Id': contractDetailsItemID.substring(contractDetailsItemID.indexOf(".") + 1),
					'Id1': relatedContentID1
				}
			}
		}).done(function (data) {
			if (data.RelatedContractContent != null) {
				l_comments_model.plannedStartDate(data.RelatedContractContent.PlannedStartDate);
				l_comments_model.neg_createdDate(moment(data.RelatedContractContent.Tracking.CreatedDate).format('MM/DD/YYYY'));
				if (data.RelatedContractContent.Deadlines.CloseNegotiation_DueDate != null) {
					l_comments_model.neg_closeDate(moment(data.RelatedContractContent.Deadlines.CloseNegotiation_DueDate).format('MM/DD/YYYY'));
				}
				else {
					l_comments_model.neg_closeDate("");
					$('#editNegCloseDate').hide();
				}
				if (data.RelatedContractContent.Status == "OPEN") {
					l_comments_model.neg_status("In progress");
				}
				else if (data.RelatedContractContent.Status == "CLOSEREQUESTED") {
					l_comments_model.neg_status("Close requested");
					enable_changeNegDate();
				}
				else if (data.RelatedContractContent.Status == "CLOSED") {
					l_comments_model.neg_status("Closed");
					$('#editNegCloseDate').off('click');
					$('#editNegCloseDate').hide();
				}
			}
		}).fail(function (error) {

		});
	}
	else {
		notifyError(getTranslationMessage("Error while reading contract details."), 3000);
	}
}