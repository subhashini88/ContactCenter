$.cordys.json.defaults.removeNamespacePrefix = true;
var contractId = "";
var l_newObligModel;
var listOffsetValue = 0;
var listLimitValue = 25;
var pagination_obj;
var l_OrgID = "";
var l_oblObj=null;

var l_MemberForSelection_model;
//Member obj for pagination
var o_MemberForSelection_model;
var l_dialogFor;


//new Obligation Model
var ObligationDetailsModel = function () {

    var self = this;
    self.oblName = ko.observable('');
    self.oblDuedate = ko.observable('');
	self.oblDuedateLocale = ko.observable('');

    self.oblDescription = ko.observable('');
    self.oblActionTobeTaken = ko.observable('');
    
    self.oblReminderTitle = ko.observable('');
    self.oblReminderDate = ko.observable('');
	self.oblReminderDateLocale = ko.observable('');

    self.oblAssigneeId = ko.observable('');
	self.oblAssigneeName = ko.observable('');
	self.oblFEId = ko.observable('');
	self.oblFEName = ko.observable('');
	self.oblSEId = ko.observable('');
	self.oblSEName = ko.observable('');
    

    self.bindJqueryFunc = function () {
        var format = "yy-mm-dd";
        $("#input_oblRemDate").datepicker({
            dateFormat: format,
            orientation: "bottom",
            onSelect: function (dateText, inst) {
                self.oblReminderDate(dateText);
				self.oblReminderDateLocale(dateText);
                //self.oblReminderDateLocale(formateDatetoLocale(dateText));
            }
        });
		
		$("#input_oblDueDate").datepicker({
            dateFormat: format,
            orientation: "bottom",
            onSelect: function (dateText, inst) {
                self.oblDuedate(dateText);
				self.oblDuedateLocale(dateText);
                //self.oblDuedateLocale(formateDatetoLocale(dateText));
            }
        });
    };
}

function getCreationInput()
{
	var l_paramString = '<Name>'+l_obligationModel.oblName()+'</Name><Description>'+l_obligationModel.oblDescription()+'</Description><ActionToBeTaken>'+l_obligationModel.oblActionTobeTaken()+'</ActionToBeTaken><ReminderTitle>'+l_obligationModel.oblReminderTitle()+'</ReminderTitle><DueDate>'+l_obligationModel.oblDuedate()+'</DueDate><ReminderSendDate>'+l_obligationModel.oblReminderDate()+'</ReminderSendDate><AssignedTo><Identity-id xmlns="http://schemas/OpenTextEntityIdentityComponents/Identity"><Id>'+l_obligationModel.oblAssigneeId()+'</Id></Identity-id></AssignedTo><FirstEscalationContact><Identity-id xmlns="http://schemas/OpenTextEntityIdentityComponents/Identity"><Id>'+l_obligationModel.oblFEId()+'</Id></Identity-id></FirstEscalationContact><SecondEscalationContact><Identity-id xmlns="http://schemas/OpenTextEntityIdentityComponents/Identity"><Id>'+l_obligationModel.oblSEId()+'</Id></Identity-id></SecondEscalationContact>';
	if(l_oblObj && l_oblObj.oblId)
	{
		l_paramString = '<Obligations-id xmlns="http://schemas/OpenTextContractCenter/Contract.Obligations"><Id>'+contractId+'</Id><Id1>'+l_oblObj.obligationId+'</Id1></Obligations-id>      <Obligations-update xmlns="http://schemas/OpenTextContractCenter/Contract.Obligations">'+l_paramString+'</Obligations-update>';
	}
	else{
		l_paramString = '<Contract-id xmlns="http://schemas/OpenTextContractCenter/Contract"><Id>'+contractId+'</Id></Contract-id><Obligations-create xmlns="http://schemas/OpenTextContractCenter/Contract.Obligations">'+l_paramString+'</Obligations-create>';
	}
	return l_paramString;
}

function FillTextValuesToModel() {
	l_obligationModel.oblName(document.getElementById("input_oblName").value);
	l_obligationModel.oblDescription(document.getElementById("id_oblDescription").value);
	l_obligationModel.oblActionTobeTaken(document.getElementById("id_oblActionTaken").value);
	l_obligationModel.oblReminderTitle(document.getElementById("input_oblRemTitle").value);	
}

var UsersListModel = function () {
	var self = this;
	self.UsersList = ko.observableArray([]);
	self.selectedUserID = ko.observable('');
	self.selectedUserName = ko.observable('');
	self.UserRowClicked = function (iItem, event) {
		$(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
		$(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
		if (iItem.ParticipatingPerson.PersonToUser["Identity-id"]) {
			var l_Id = iItem.ParticipatingPerson.PersonToUser["Identity-id"].Id;
			self.selectedUserID(l_Id);
			self.selectedUserName(getTextValue(iItem.ParticipatingPerson.PersonToUser.IdentityDisplayName));
			$('ai-dialog-footer .btn-primary:contains("Assign")', window.parent.parent.document).attr("disabled", false);
		}
	}
	self.UserRowRadiobuttonClicked = function (iItem, event) {
		$(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
		$(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
		if (iItem.ParticipatingPerson.PersonToUser["Identity-id"]) {
			var l_Id = iItem.ParticipatingPerson.PersonToUser["Identity-id"].Id;
			self.selectedUserID(l_Id);
			self.selectedUserName(getTextValue(iItem.ParticipatingPerson.PersonToUser.IdentityDisplayName));
			$('ai-dialog-footer .btn-primary:contains("Assign")', window.parent.parent.document).attr("disabled", false);
		}
		event.stopPropagation();
		return true;
	}
}

$(document).ready(function () {
    var i_locale = getlocale();
    var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
    translateLabels("com/opentext/apps/contractcenter/ContractCenter/ContractCenter", i_locale, true);
    loadRTLIfRequired(i_locale, rtl_css);
	
	if(!contractId)
	{
		var cInstanceId = getUrlParameterValue("instanceId", null, true);
		contractId = cInstanceId;
	}
	if(!l_OrgID)
	{
		l_OrgID = getUrlParameterValue("orgID", null, true);
	}
	if(!l_obligationModel)
	{
		l_obligationModel = new ObligationDetailsModel(cInstanceId);
		ko.applyBindings(l_obligationModel, document.getElementById("div_CreateObligForm"));
		setTimeout(function () {
			l_obligationModel.bindJqueryFunc();
		}, 0);
	}
	if(!l_usersList_model)
	{
		l_usersList_model = new UsersListModel();
		ko.applyBindings(l_usersList_model, document.getElementById("div_usersList"));
		ListOrganizationUsers();
	}
});

function translatePlaceHolders() {
    var elems = window.parent.parent.document.getElementsByClassName("btn-translate");
    if (elems) {
        for (var ind = 0; ind < elems.length; ind++) {
            elems[ind].innerHTML = getTranslationMessage(elems[ind].innerHTML);
            elems[ind].title = elems[ind].innerHTML;
        }
    }
}

function createObligation() {
    if (checkforMandatoryFields()) {
		if(l_oblObj)
		{
			 $.cordys.ajax({
                namespace: "http://schemas/OpenTextContractCenter/Contract.Obligations/operations",
                method: "UpdateObligations",
                parameters: getCreationInput(),
                success: function (data) {
					showOrHideErrorInfo("div_formErrorInfoArea", true, getTranslationMessage("The obligation has been created."), 10000);
                },
                error: function (responseFailure) {
					// debugger;
                    showOrHideErrorInfo("div_formErrorInfoArea", true, getTranslationMessage("Unable to create Obligation. Contact your administrator."), 10000);
                    return false;
                }
            });
		}
		else
		{
            $.cordys.ajax({
                namespace: "http://schemas/OpenTextContractCenter/Contract/operations",
                method: "CreateObligations",
                parameters: getCreationInput(),
                success: function (data) {
					showOrHideErrorInfo("div_formErrorInfoArea", true, getTranslationMessage("The obligation has been created."), 10000);
                },
                error: function (responseFailure) {
					// debugger;
                    showOrHideErrorInfo("div_formErrorInfoArea", true, getTranslationMessage("Unable to create Obligation. Contact your administrator."), 10000);
                    return false;
                }
            });
        }
	}
}


function checkforMandatoryFields() {
    var validationFlag = true;
	FillTextValuesToModel();
    /*var $regexExp = /^([1-9]|[1-9][0-9]|[1-9][0-9][0-9])$/;

    if (l_cloneContractDetailsModel.contractOrgItemID() == "" || l_cloneContractDetailsModel.contractOrgItemID() == undefined) {
        $("#input_organization").addClass("cc-error");
        validationFlag = false;
    }
    if (l_cloneContractDetailsModel.contractTypeItemID() == "" || l_cloneContractDetailsModel.contractTypeItemID() == undefined) {
        $("#input_contractType").addClass("cc-error");
        validationFlag = false;
    }
    if ((l_cloneContractDetailsModel.contractTemplateItemID() == "" || l_cloneContractDetailsModel.contractTemplateItemID() == undefined) && !(l_cloneContractDetailsModel.isExecuted() == 'true' || l_cloneContractDetailsModel.isExternalContract() == 'EXTERNALDOCUMENT')) {
        $("#input_template").addClass("cc-error");
        validationFlag = false;
    }
    if (l_cloneContractDetailsModel.isPerpetual() == 'false' && (l_cloneContractDetailsModel.contractTerm() == "" || l_cloneContractDetailsModel.contractTerm() == undefined || !$("#input_contractTerm").val().match($regexExp))) {
        $("#input_contractTerm").addClass("cc-error");
        validationFlag = false;
    }
    if (l_cloneContractDetailsModel.actualStartDate() == "" || l_cloneContractDetailsModel.actualStartDate() == undefined) {
        $("#input_actualStartDate").addClass("cc-error");
        validationFlag = false;
    }

    if ((l_cloneContractDetailsModel.autoRenewDuration() == "" || l_cloneContractDetailsModel.autoRenewDuration() == undefined || !$("#input_autoRenewDuration").val().match($regexExp)) && l_cloneContractDetailsModel.autoRenewal() == 'true') {
        $("#input_autoRenewDuration").addClass("cc-error");
        validationFlag = false;
    }*/
    return validationFlag;
}
function removeErrorClass(iEvent) {
    $(iEvent).removeClass("cc-error");
}


function ListOrganizationUsers() {
	var l_userNameFilter = document.getElementById("input_userListSearchFilter");
	$.cordys.ajax({
		namespace: "http://schemas.opentext.com/apps/cc/basiccomponents/20.2",
		method: "GetOrgMemberswithFilters",
		parameters: {
			"memName": l_userNameFilter.value,
			"orgName": "",
			"orgID": l_OrgID,
			"offset": "0",
			"limit": "200"
		},
		success: function (data) {
			addDataToUsersView(data.OrgMembers.FindZ_INT_OrgUsersListResponse.OrganizationMembers, l_usersList_model);
		},
		error: function (responseFailure) {
			showOrHideErrorInfo("div_modalErrorInfoArea", true, "An error occurred while loading individuals. Contact the administrator.", 10000);
			return false;
		}
	});
}
function addDataToUsersView(iElementList, iModel) {
	iModel.UsersList.removeAll();
	if (iElementList) {
		if (iElementList.length) {
			iElementList.forEach(function (iElement) {
				iModel.UsersList.push(iElement);
			});
		}
		else {
			iModel.UsersList.push(iElementList);
		}
	}
}

function openAssigneeSelection() {
	l_dialogFor = "Assignee";
	$("#div_selectUser").modal();
	$('button#btn_userSelectionYes').off("click");
    $('button#btn_userSelectionYes').on('click', function (_event) {
		l_obligationModel.oblAssigneeId(l_usersList_model.selectedUserID());
		l_obligationModel.oblAssigneeName(l_usersList_model.selectedUserName());
	});
}

function openFirstEscalationSelection() {
	l_dialogFor = "FE";
	$("#div_selectUser").modal();
	$('button#btn_userSelectionYes').off("click");
    $('button#btn_userSelectionYes').on('click', function (_event) {
		l_obligationModel.oblFEId(l_usersList_model.selectedUserID());
		l_obligationModel.oblFEName(l_usersList_model.selectedUserName());
	});
}

function openSecondEscalationSelection() {
	l_dialogFor = "SE";
	$("#div_selectUser").modal();
	$('button#btn_userSelectionYes').off("click");
    $('button#btn_userSelectionYes').on('click', function (_event) {
		l_obligationModel.oblSEId(l_usersList_model.selectedUserID());
		l_obligationModel.oblSEName(l_usersList_model.selectedUserName());
	});
}

