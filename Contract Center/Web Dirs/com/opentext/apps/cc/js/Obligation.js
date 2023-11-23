$.cordys.json.defaults.removeNamespacePrefix = true;
var l_obligationModel;
var l_usersList_model;
var l_rolesandGroupsList_model;
var l_oblLibrary_model;
var l_oblSetComplianceModel;
var l_oblNonComplianceListModal;
var l_oblComplHistoryModal;
var selectedOBRow = {};
var listOffsetValue = 0;
var listLimitValue = 25;
var pagination_obj;
var l_OrgID = "";
var l_oblObj = {};

var l_MemberForSelection_model;
//Member obj for pagination
var o_MemberForSelection_model;
var l_dialogFor;

var contractId;
var contractObj;
var ctypeId;

const processStatusAlias = Object.freeze({
	'Open':'Open',
	'InProgress':'In progress',
	'Completed': 'Completed',
	'InProgress-Escalated': 'In progress - Escalated',
	'Completed-Corrected':'Completed - Corrected',
	'Completed-Override':'Completed - Override',
	'Completed-PostEscalation':'Completed - Post escalation',
	'Met-ReviewInProgress':'Met - Review in progress',
	'NotMet-ReviewInProgress':'Not met - Review in progress'

});
const complianceLevelAlias = Object.freeze({
	'Open':'Open',
	'Met-Override':'Met - Override',
	'Met-Corrected':'Met - Corrected',
	'Met':'Met',
	'Met-PostEscalation':'Met - Post escalation',
	'InProgress':'In progress',
	'InProgress-Escalated':'In progress - Escalated',
	'Met-ReviewInProgress':'Met - Review in progress',
	'NotMet-ReviewInProgress':'Not met - Review in progress',
	'NotMet':'Not met',
	'Met-InRecurrence':'Met - In recurrence',
	'Met-PendingRecurrences':'Met - Pending recurrences',
	'NotMet-PendingRecurrences':'Not met - Pending recurrences',
	'Met-Override-PendingRecurrences':'Met - Override - Pending recurrences',
	'Met-Corrected-PendingRecurrences':'Met - Corrected - Pending recurrences',
	'Met-PostEscalation-PendingRecurrences':'Met - Post escalation - Pending recurrences'

});
// Services start----------------------------------------------
var cc_obligaion_services = (function () {
	var self = {};
	self.getObligationsListService = function (request, responseCallback) {
		$.cordys.ajax({
			method: "getObligationsList",
			namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
			parameters: request,
			success: function (responseSuccess) {
				responseCallback(responseSuccess);
			},
			error: function (responseFailure) {
				responseCallback(responseFailure, "ERROR");
				showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while loading Obligations. Contact the administrator."), 10000);
				return false;
			}
		});
	};
	self.inActivateObligation = function (request, responseCallback) {
		$.cordys.ajax({
			method: "TriggerInactivateObl",
			namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
			parameters: request,
			success: function (responseSuccess) {
				responseCallback(responseSuccess);
			},
			error: function (responseFailure) {
				responseCallback(responseFailure, "ERROR");
				showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while inactivating obligation. Contact the administrator."), 10000);
				return false;
			}
		});
	};
	self.invalidateObligation = function (request, responseCallback) {
		$.cordys.ajax({
			method: "TriggerInvalidateObl",
			namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
			parameters: request,
			success: function (responseSuccess) {
				responseCallback(responseSuccess);
			},
			error: function (responseFailure) {
				responseCallback(responseFailure, "ERROR");
				showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while invalidating obligation. Contact the administrator."), 10000);
				return false;
			}
		});
	};
	self.createOBLComplianceService = function (request, responseCallback) {
		$.cordys.ajax({
			method: "SetComplianceState",
			namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
			parameters: request,
			success: function (responseSuccess) {
				responseCallback(responseSuccess);
			},
			error: function (responseFailure) {
				responseCallback(responseFailure, "ERROR");
				showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while setting compliance. Contact the administrator."), 10000);
				return false;
			}
		});
	};
	self.getOBLComplianceList = function (request, responseCallback) {
		$.cordys.ajax({
			namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
			method: "getOBLComplianceList",
			parameters: request,
			success: function (responseSuccess) {
				responseCallback(responseSuccess);
			},
			error: function (responseFailure) {
				responseCallback(responseFailure, "ERROR");
				showOrHideErrorInfo("div_formErrorInfoArea", true, getTranslationMessage("An error occurred while loading compliance history. Contact the administrator."), 10000);
				return false;
			}
		});
	}
	return self;
})();

// Services end------------------------------------------------

//Gloabal variables, Static Data


const filterList = [
	{ key: '--Select--', val: '' },
	{ key: 'Equal to', val: 'EQUALTO' },
	{ key: 'Range', val: 'RANGE' },
	{ key: 'Not equal to', val: 'NOTEQUALTO' },
	{ key: 'Contains', val: 'CONTAINS' },
	{ key: 'Empty', val: 'EMPTY' },
	{ key: 'Not empty', val: 'NOTEMPTY' },
	{ key: 'Any of(;)', val: 'ANYOF' }
];

const defaultFilterVal = "CONTAINS";

function ObligationListViewModel() {
	var self = this;
	self.obligationsList = ko.observableArray();
	self.numOfOBRows = ko.observable('');
	self.filterExpand = ko.observable(false);
	self.filterObligation = ko.observable(defaultFilterVal);
	self.filterOptions = ko.observableArray(filterList);
	self.hasCreateObligation = ko.observable(false);
	self.hasSetCompliance = ko.observable(false);
	self.disableSetCompliane = ko.observable(false);
	self.filterObligationInput = ko.observable();

	self.filterObligationExpand = ko.observable(true);

	contractId = getUrlParameterValue("instanceId", null, true);

	if (contractId.indexOf(".") < 0) {
		contractId = '005056C00008A1E795653A59509D399D.' + contractId;
	}
	ctypeId = getUrlParameterValue("ctypeId", null, true);
	if (window.top.document.body.aurelia) {
		contractObj = window.top.document.body.aurelia.container.viewModel.sSOService.itemState.items[contractId];
	}
	contractId = contractId.substring(contractId.indexOf(".") + 1);


	self.showOptionsVisibility = function (data, event) {
		data.visibleButtonOptions(true);
	}
	self.hideOptionsVisibility = function (data, event) {
		data.visibleButtonOptions(false);
	};

	self.openFilter = function () {
		self.filterExpand(!self.filterExpand());
		if (!self.filterExpand()) {
			self.clearFilter();
		};
	};

	self.ApplyFilter = function (data, event) {
		var filterParams = {
			"contractID": contractId,
			"name": self.filterObligationInput
		}
		self.fetchObligationList(filterParams);
		self.filterExpand(!self.filterExpand());
		if (filterParams.name().length > 0) {
			$("#btn_clearFilterActionBar").css('display', 'inline');
		} else {
			$("#btn_clearFilterActionBar").css('display', 'none');
		}
	}

	self.clearFilter = function (data, event) {
		self.filterExpand(false);
		self.filterObligationInput("");
		self.fetchObligationList({ "contractID": contractId });
		$("#btn_clearFilterActionBar").css('display', 'none');
	};
	self.openCreateForm = function () {
		if (!self.hasCreateObligation()) {
			return;
		}
		l_oblObj = {};
		window.makeObligationDialogReady();
		$("#obligationsDetails_Dialog").modal({
			backdrop: 'static',
			keyboard: false
		});
		$("#btn_createorUpdateObligation span").text("Create");
		window.makeOBLibraryReady();
	};
	self.openEditForm = function (data) {
		l_oblObj = data;
		$("#obligationsDetails_Dialog").modal({
			backdrop: 'static',
			keyboard: false
		});
		window.makeObligationDialogReady();
		$("#btn_createorUpdateObligation span").text("Update");
	};
	self.viewComplHistory = function(data) {
		l_oblObj = data;
		if (!l_oblComplHistoryModal) {
			l_oblComplHistoryModal = new OBLComplHistoryModel();
			ko.applyBindings(l_oblComplHistoryModal, document.getElementById("div_complHistoryDialog"));
		}
		cc_obligaion_services.getOBLComplianceList({"obligationId1": l_oblObj.obligationId1()}, function (response_data, status) {
			if (status !== "ERROR") {
				addDataToComplianceHistory(response_data.Response.FindZ_INT_OBLComplianceListResponse.OBLComplianceData, l_oblComplHistoryModal);
				$("#div_complHistoryDialog").modal({
					backdrop: 'static',
					keyboard: false
				});
			}
		});

		
		
	}
	self.openInActiveConfirmModal = function (data) {
		if (data.Status() === 'INACTIVE' || data.Status() === 'INVALID' || data.Status()==="INACTIVEINPROGRESS" || data.Status()==="INVALIDINPROGRESS") {
			return;
		}
		l_oblObj = data;
		$("#obligationInActive_Dialog").modal();
	};
	self.openInvalidConfirmModal = function (data) {
		if (data.Status() === 'INACTIVE' || data.Status() === 'INVALID' || data.Status()==="INACTIVEINPROGRESS" || data.Status()==="INVALIDINPROGRESS") {
			return;
		}
		l_oblObj = data;
		$("#obligationInvalid_Dialog").modal();
	};

	self.deleteFromSelection = function (data, event) {

		$("#div_deleteDialog").modal();
	};

	self.toggleSelectAllButton = function (data, event) {
		var l_currentClassName = event.currentTarget.className;
        if (l_currentClassName == "cc-select-column cc-checkbox-select-all-off" || l_currentClassName == "cc-select-column cc-checkbox-select-all-partial") {
            $(event.currentTarget).removeClass("cc-checkbox-select-all-off");
            $(event.currentTarget).removeClass("cc-checkbox-select-all-partial");
            $(event.currentTarget).addClass("cc-checkbox-select-all-on");
            $("#id_ObligationListTable").find('tbody .cc-select-column').removeClass("cc-checkbox-off");
            $("#id_ObligationListTable").find('tbody .cc-select-column').addClass("cc-checkbox-on");
            $("#id_ObligationListTable").find('tbody tr').css("background-color", "#CBD3D9");
			$("#btn_deactivateListActionBar").css("display", "none");
			$("#btn_invalidateListActionBar").css("display", "none");
			$("#id_deleteListActionBar").css("display", "none");
            self.obligationsList().forEach(function (iToken) {
                selectedOBRow[iToken.ObligationGeneratedID()] = iToken;
            });
			
			
			
        }
        else if (l_currentClassName == "cc-select-column cc-checkbox-select-all-on") {
            $(event.currentTarget).removeClass("cc-checkbox-select-all-on");
            $(event.currentTarget).addClass("cc-checkbox-select-all-off");
            $("#id_ObligationListTable").find('tbody .cc-select-column').removeClass("cc-checkbox-on");
            $("#id_ObligationListTable").find('tbody .cc-select-column').addClass("cc-checkbox-off");
            $("#id_ObligationListTable").find('tbody tr').css("background-color", "transparent")
            $("#btn_deactivateListActionBar").css("display", "none");
			$("#btn_invalidateListActionBar").css("display", "none");
            $("#id_deleteListActionBar").css("display", "none");
            selectedOBRow = {};
        }
        event.stopPropagation();
	};

	self.deleteObligation = function (data, event) {

		l_oblObj = data;
		if (l_oblObj.isDeletabale()) {
			$("#div_deleteDialog").modal();
		}
		else {
			$("#div_deletionInfo").modal();
		}
	};

	/*self.toggleDeleteButton = function (data, event) {
		data.deleteSelected(data.deleteSelected() == false);

	};*/

	self.onOBRowCheckboxValueChanged = function (iItem, event) {
        var l_currentClassName = event.currentTarget.className;
        if (l_currentClassName == "cc-select-column cc-checkbox-off") {
            $(event.currentTarget).removeClass("cc-checkbox-off")
            $(event.currentTarget).addClass("cc-checkbox-on")
            selectedOBRow[iItem.obligationItemId1()] = iItem;
            $(event.currentTarget.parentElement.parentElement).css("background-color", "#CBD3D9")
        }
        else if (l_currentClassName == "cc-select-column cc-checkbox-on") {
            $(event.currentTarget).removeClass("cc-checkbox-on")
            $(event.currentTarget).addClass("cc-checkbox-off")
            delete selectedOBRow[iItem.obligationItemId1()];
            $(event.currentTarget.parentElement.parentElement).css("background-color", "transparent")
			$("#btn_invalidateListActionBar").prop("disabled", false);
			$("#btn_deactivateListActionBar").prop("disabled", false);
			if(Object.keys(selectedOBRow).length == 1){
				let OblStatus = selectedOBRow[Object.keys(selectedOBRow)[0]].Status();
				if (OblStatus === 'INACTIVE' || OblStatus === 'INVALID' || OblStatus === "INACTIVEINPROGRESS" || OblStatus === "INVALIDINPROGRESS" || !l_ObligationListViewModel.hasSetCompliance()) {
					$("#btn_deactivateListActionBar").prop("disabled", true);
				}
				else {
					$("#btn_deactivateListActionBar").prop("disabled", false);
				}
				if (OblStatus === 'INACTIVE' || OblStatus === 'INVALID' || OblStatus === "INACTIVEINPROGRESS" || OblStatus === "INVALIDINPROGRESS" || !l_ObligationListViewModel.hasSetCompliance() || selectedOBRow[Object.keys(selectedOBRow)[0]].RecurCycle() === '0') {
					$("#btn_invalidateListActionBar").prop("disabled", true);
				}
				else {
					$("#btn_invalidateListActionBar").prop("disabled", false);
				}
				let cmplStatuus = selectedOBRow[Object.keys(selectedOBRow)[0]].ComplianceLevel();
				if (cmplStatuus === 'Open' || OblStatus === 'INACTIVE' || OblStatus === 'INVALID' || !l_ObligationListViewModel.hasSetCompliance()) {
					$("#btn_openOblcomplianceModal").prop("disabled", true);
				}
				else{
					$("#btn_openOblcomplianceModal").prop("disabled", false);
				}
			}
        }
        event.stopPropagation();
        if (Object.keys(selectedOBRow).length <= 0) {
            $("#id_selectAllList").removeClass("cc-checkbox-select-all-partial");
            $("#id_selectAllList").removeClass("cc-checkbox-select-all-on");
            $("#id_selectAllList").addClass("cc-checkbox-select-all-off");
            $("#btn_openOblcomplianceModal").css("display", "none");
			$("#btn_deactivateListActionBar").css("display", "none");
			$("#btn_invalidateListActionBar").css("display", "none");
            $("#id_deleteListActionBar").css("display", "none");
        } else if (Object.keys(selectedOBRow).length == 1) {
            if (1 == self.numOfOBRows()) {
                $("#id_selectAllList").removeClass("cc-checkbox-select-all-off");
                $("#id_selectAllList").removeClass("cc-checkbox-select-all-partial");
                $("#id_selectAllList").addClass("cc-checkbox-select-all-on");
                $("#btn_openOblcomplianceModal").css("display", "inline")
				$("#btn_deactivateListActionBar").css("display", "inline");
				$("#btn_invalidateListActionBar").css("display", "inline");
				let OblStatus = selectedOBRow[Object.keys(selectedOBRow)[0]].Status();
				if (OblStatus === 'INACTIVE' || OblStatus === 'INVALID' || OblStatus === "INACTIVEINPROGRESS" || OblStatus === "INVALIDINPROGRESS" || !l_ObligationListViewModel.hasSetCompliance()) {
					$("#btn_deactivateListActionBar").prop("disabled", true);
				}
				else {
					$("#btn_deactivateListActionBar").prop("disabled", false);
				}
				if (OblStatus === 'INACTIVE' || OblStatus === 'INVALID' || OblStatus === "INACTIVEINPROGRESS" || OblStatus === "INVALIDINPROGRESS" || !l_ObligationListViewModel.hasSetCompliance() || selectedOBRow[Object.keys(selectedOBRow)[0]].RecurCycle() === '0') {
					$("#btn_invalidateListActionBar").prop("disabled", true);
				}
				else {
					$("#btn_invalidateListActionBar").prop("disabled", false);
				}
				let cmplStatuus = selectedOBRow[Object.keys(selectedOBRow)[0]].ComplianceLevel();
				if (cmplStatuus === 'Open' || OblStatus === 'INACTIVE' || OblStatus === 'INVALID' || !l_ObligationListViewModel.hasSetCompliance()) {
					$("#btn_openOblcomplianceModal").prop("disabled", true);
				}
				else{
					$("#btn_openOblcomplianceModal").prop("disabled", false);
				}
                $("#id_deleteListActionBar").css("display", "inline");
            } else {
                $("#id_selectAllList").removeClass("cc-checkbox-select-all-off");
                $("#id_selectAllList").removeClass("cc-checkbox-select-all-on");
                $("#id_selectAllList").addClass("cc-checkbox-select-all-partial");
                $("#btn_openOblcomplianceModal").css("display", "inline")
				$("#btn_deactivateListActionBar").css("display", "inline");
				$("#btn_invalidateListActionBar").css("display", "inline");
				let OblStatus = selectedOBRow[Object.keys(selectedOBRow)[0]].Status();
				if (OblStatus === 'INACTIVE' || OblStatus === 'INVALID' || OblStatus === "INACTIVEINPROGRESS" || OblStatus === "INVALIDINPROGRESS" || !l_ObligationListViewModel.hasSetCompliance()) {
					$("#btn_deactivateListActionBar").prop("disabled", true);
				}
				else {
					$("#btn_deactivateListActionBar").prop("disabled", false);
				}
				if (OblStatus === 'INACTIVE' || OblStatus === 'INVALID' || OblStatus === "INACTIVEINPROGRESS" || OblStatus === "INVALIDINPROGRESS" || !l_ObligationListViewModel.hasSetCompliance() || selectedOBRow[Object.keys(selectedOBRow)[0]].RecurCycle() === '0') {
					$("#btn_invalidateListActionBar").prop("disabled", true);
				}
				else {
					$("#btn_invalidateListActionBar").prop("disabled", false);
				}
				let cmplStatuus = selectedOBRow[Object.keys(selectedOBRow)[0]].ComplianceLevel();
				if (cmplStatuus === 'Open' || OblStatus === 'INACTIVE' || OblStatus === 'INVALID' || !l_ObligationListViewModel.hasSetCompliance()) {
					$("#btn_openOblcomplianceModal").prop("disabled", true);
				}
				else{
					$("#btn_openOblcomplianceModal").prop("disabled", false);
				}
                $("#id_deleteListActionBar").css("display", "inline");
            }
        } else if (Object.keys(selectedOBRow).length > 1 && Object.keys(selectedOBRow).length < self.numOfOBRows()) {
            $("#id_selectAllList").removeClass("cc-checkbox-select-all-off");
            $("#id_selectAllList").removeClass("cc-checkbox-select-all-on");
            $("#id_selectAllList").addClass("cc-checkbox-select-all-partial");
            $("#btn_openOblcomplianceModal").css("display", "none");
			$("#btn_deactivateListActionBar").css("display", "none");
			$("#btn_invalidateListActionBar").css("display", "none");
            $("#id_deleteListActionBar").css("display", "none");
        } else if (Object.keys(selectedOBRow).length == self.numOfOBRows()) {
            $("#id_selectAllList").removeClass("cc-checkbox-select-all-off");
            $("#id_selectAllList").removeClass("cc-checkbox-select-all-partial");
            $("#id_selectAllList").addClass("cc-checkbox-select-all-on");
            $("#btn_openOblcomplianceModal").css("display", "none");
			$("#btn_deactivateListActionBar").css("display", "none");
			$("#btn_invalidateListActionBar").css("display", "none");
            $("#id_deleteListActionBar").css("display", "none");
        }
    }

	self.openOBLComplianceModal = function (data, event) {

		if (!l_oblSetComplianceModel) {
			l_oblSetComplianceModel = new OBLSetComplianceModel();
			ko.applyBindings(l_oblSetComplianceModel, document.getElementById("div_SetOBLComplianceForm"));
		}

		$("#div_setOBLComplianceDialog").modal({
			backdrop: 'static',
			keyboard: false
		});
	};


	self.fetchObligationList = function (params) {

		cc_obligaion_services.getObligationsListService(params, function (response_data, status) {
			if (status !== "ERROR") {
				_populateDatatable(response_data);
			}
		});
	};

	function _populateDatatable(response_data) {
		if (response_data.Response.FindContractObligationViewListResponse && response_data.Response.FindContractObligationViewListResponse.Obligations) {
			var obligationsData = response_data.Response.FindContractObligationViewListResponse.Obligations;
			self.obligationsList.removeAll();
			if (Array.isArray(obligationsData)) {
				self.numOfOBRows(obligationsData.length);
				for (var i = 0; i < obligationsData.length; i++) {
					self.obligationsList.push(ko.mapping.fromJS(_populateData(obligationsData[i])));
				}
			} else {
				self.numOfOBRows(1);
				self.obligationsList.push(ko.mapping.fromJS(_populateData(obligationsData)));
			}

		} else {
			self.numOfOBRows(0);
			self.obligationsList.removeAll();
		}
	};

	function _populateData(response_data) {
		var obligationObj = {};
		obligationObj.obligationId1 = response_data["Obligations-id"].Id1;
		obligationObj.obligationItemId1 = response_data["Obligations-id"].ItemId1;
		obligationObj.ObligationGeneratedID = _getTextValue(response_data.GeneratedObliID);
		obligationObj.ObligationName = _getTextValue(response_data.Name);
		obligationObj.ObligationAction = _getTextValue(response_data.ActionToBeTaken);
		obligationObj.ObligationReminder = _getTextValue(response_data.ReminderTitle);
		obligationObj.Due_Date = _getTextValue(response_data.DueDate) ? formateDatetoLocale(_getTextValue(response_data.DueDate)) : '';
		obligationObj.Z_DueDate = _getTextValue(response_data.DueDate);
		obligationObj.Description = _getTextValue(response_data.Description);
		obligationObj.RemainderSetFor = _getTextValue(response_data.ReminderSendDate) ? formateDatetoLocale(_getTextValue(response_data.ReminderSendDate)) : '';
		obligationObj.Z_RemainderSetFor = _getTextValue(response_data.ReminderSendDate);
		obligationObj.ComplianceLevel = _getTextValue(response_data.ComplianceLevel);
		obligationObj.CompliaLvlDisplay = complianceLevelAlias[obligationObj.ComplianceLevel];
		obligationObj.LatestComplItemID2 = response_data.LatestOBLComplianceData ? _getTextValue(response_data.LatestOBLComplianceData["OBLComplianceData-id"].ItemId2):'';
		obligationObj.AssignedTo = '';
		obligationObj.AssignedToId = '';
		obligationObj.AssignedRole = '';
		obligationObj.AssignedRoleId = '';
		obligationObj.AssignedGroup = '';
		obligationObj.AssignedGroupId = '';
		if (response_data.AssignedTo && response_data.AssignedTo["Identity-id"]) {
			obligationObj.AssignedTo = _getTextValue(response_data.AssignedTo.Name);
			obligationObj.AssignedToId = getTextAfter(_getTextValue(response_data.AssignedTo["Identity-id"].ItemId), ".");
		} else if (response_data.RemAssignedRole && response_data.RemAssignedRole["Identity-id"]) {
			obligationObj.AssignedRole = _getTextValue(response_data.RemAssignedRole.Name);
			obligationObj.AssignedRoleId = getTextAfter(_getTextValue(response_data.RemAssignedRole["Identity-id"].ItemId), ".");
		} else if (response_data.RemAssignedGroup && response_data.RemAssignedGroup["Identity-id"]) {
			obligationObj.AssignedGroup = _getTextValue(response_data.RemAssignedGroup.Name);
			obligationObj.AssignedGroupId = getTextAfter(_getTextValue(response_data.RemAssignedGroup["Identity-id"].ItemId), ".");
		}
		obligationObj.FirstEscalationContact = '';
		obligationObj.FirstEscalationContactId = '';
		obligationObj.FESCAssignedRole = '';
		obligationObj.FESCAssignedRoleId = '';
		obligationObj.FESCAssignedGroup = '';
		obligationObj.FESCAssignedGroupId = '';
		if (response_data.FirstEscalationContact && response_data.FirstEscalationContact["Identity-id"]) {
			obligationObj.FirstEscalationContact = _getTextValue(response_data.FirstEscalationContact.Name);
			obligationObj.FirstEscalationContactId = getTextAfter(_getTextValue(response_data.FirstEscalationContact["Identity-id"].ItemId), ".");
		} else if (response_data.FESCAssignedRole && response_data.FESCAssignedRole["Identity-id"]) {

			obligationObj.FESCAssignedRole = _getTextValue(response_data.FESCAssignedRole.Name);
			obligationObj.FESCAssignedRoleId = getTextAfter(_getTextValue(response_data.FESCAssignedRole["Identity-id"].ItemId), ".");
		} else if (response_data.FESCAssignedGroup && response_data.FESCAssignedGroup["Identity-id"]) {

			obligationObj.FESCAssignedGroup = _getTextValue(response_data.FESCAssignedGroup.Name);
			obligationObj.FESCAssignedGroupId = getTextAfter(_getTextValue(response_data.FESCAssignedGroup["Identity-id"].ItemId), ".");
		}
		obligationObj.SecondEscalationContact = '';
		obligationObj.SecondEscalationContactId = '';
		obligationObj.SESCAssignedRole = '';
		obligationObj.SESCAssignedRoleId = '';
		obligationObj.SESCAssignedGroup = '';
		obligationObj.SESCAssignedGroupId = '';
		if (response_data.SecondEscalationContact && response_data.SecondEscalationContact["Identity-id"]) {
			obligationObj.SecondEscalationContact = _getTextValue(response_data.SecondEscalationContact.Name);
			obligationObj.SecondEscalationContactId = getTextAfter(_getTextValue(response_data.SecondEscalationContact["Identity-id"].ItemId), ".");
		} else if (response_data.SESCAssignedRole && response_data.SESCAssignedRole["Identity-id"]) {
			obligationObj.SESCAssignedRole = _getTextValue(response_data.SESCAssignedRole.Name);
			obligationObj.SESCAssignedRoleId = getTextAfter(_getTextValue(response_data.SESCAssignedRole["Identity-id"].ItemId), ".");
		} else if (response_data.SESCAssignedGroup && response_data.SESCAssignedGroup["Identity-id"]) {
			obligationObj.SESCAssignedGroup = _getTextValue(response_data.SESCAssignedGroup.Name);
			obligationObj.SESCAssignedGroupId = getTextAfter(_getTextValue(response_data.SESCAssignedGroup["Identity-id"].ItemId), ".");
		}
		obligationObj.AssigneeESCDuration = _getTextValue(response_data.AssigneeEscDuration);
		obligationObj.FESCDuration = _getTextValue(response_data.FESCDuration);
		obligationObj.SESCDuration = _getTextValue(response_data.SESCDuration);

		obligationObj.RecurCycle = _getTextValue(response_data.RecurrenceCycle);
		obligationObj.Status = (_getTextValue(response_data.Status ? response_data.Status : "ACTIVE"));
		switch (obligationObj.Status) {
		  case 'ACTIVE':
			obligationObj.StatusLabel = 'Active';
			break;
		  case 'INACTIVE':
			obligationObj.StatusLabel = 'Inactive';
			break;
		  case 'INVALID':
			obligationObj.StatusLabel = 'Invalid';
			break;
		  case 'INACTIVEINPROGRESS':
			obligationObj.StatusLabel = 'Inactive review in progress';
			break;
		  case 'INVALIDINPROGRESS':
			obligationObj.StatusLabel = 'Invalid review in progress';
			break;
		}
		obligationObj.RecurType = _getTextValue(response_data.RecurrenceType);
		obligationObj.IsRecur = obligationObj.RecurType == "DONOTREPEAT" ? "No" : "Yes";
		obligationObj.RecurStartDate = _getTextValue(response_data.RecurrenceStart) ? formateDatetoLocale(_getTextValue(response_data.RecurrenceStart)) : '';
		obligationObj.Z_RecurStartDate = _getTextValue(response_data.RecurrenceStart);
		obligationObj.RecurMonthDay = _getTextValue(response_data.RecurDayOfMonth);
		obligationObj.RecurWeekDay = _getTextValue(response_data.RecurDaysOfWeek);
		if (obligationObj.RecurType == "REPEATMONTHLY") {
			obligationObj.RecurMonthFreq = _getTextValue(response_data.RecurFrequency);
			obligationObj.RecurWeekFreq = "";
			obligationObj.RecurDayFreq = "";
		}
		if (obligationObj.RecurType == "REPEATWEEKLY") {
			obligationObj.RecurMonthFreq = "";
			obligationObj.RecurWeekFreq = _getTextValue(response_data.RecurFrequency);
			obligationObj.RecurDayFreq = "";
		}
		if (obligationObj.RecurType == "REPEATDAILY") {
			obligationObj.RecurWeekFreq = "";
			obligationObj.RecurMonthFreq = "";
			obligationObj.RecurDayFreq = _getTextValue(response_data.RecurFrequency);
		}
		obligationObj.RecurEndType = _getTextValue(response_data.RecurrenceEndType);
		obligationObj.RecurEndinOccurences = _getTextValue(response_data.RecurEndInOccur);
		obligationObj.RecurEndDate = _getTextValue(response_data.RecurrenceEnd) ? formateDatetoLocale(_getTextValue(response_data.RecurrenceEnd)) : '';
		obligationObj.Z_RecurEndDate = _getTextValue(response_data.RecurrenceEnd);

		obligationObj.isDeletabale = true;
		obligationObj.stage = 0;
		if (response_data.CurrentGCActivityInstance && response_data.CurrentGCActivityInstance["GCActivityInstances-id"]) {
			obligationObj.isDeletabale = false;
			obligationObj.stage = 1;
			if (response_data.CurrentGCActivityInstance.LatestEscalation && response_data.CurrentGCActivityInstance.LatestEscalation["GCActivityInstances-id"]) {
				obligationObj.stage = 2;
				if (response_data.CurrentGCActivityInstance.LatestEscalation.LatestEscalation && response_data.CurrentGCActivityInstance.LatestEscalation.LatestEscalation["GCActivityInstances-id"]) {
					obligationObj.stage = 3;
					if (["Active", "Created", "Unassigned"].indexOf(response_data.CurrentGCActivityInstance.LatestEscalation.LatestEscalation.Lifecycle.CurrentState) < 0) {
						obligationObj.stage = 0;
					}
				}
				else if (["Active", "Created", "Unassigned"].indexOf(response_data.CurrentGCActivityInstance.LatestEscalation.Lifecycle.CurrentState) < 0) {
					obligationObj.stage = 0;
				}
			}
			else if (["Active", "Created", "Unassigned"].indexOf(response_data.CurrentGCActivityInstance.Lifecycle.CurrentState) < 0) {
				obligationObj.stage = 0;
			}
		}
		obligationObj.deleteSelected = false;
		obligationObj.visibleButtonOptions = ko.observable(false);
		return obligationObj;
	};

	// function to initializa data of SubOrg List screen
	(function init() {
		self.fetchObligationList({ "contractID": contractId });
		checkForUserRole((data) => {
			self.hasCreateObligation(getTextValue(data.IsCurrentUserRoles) === 'true' ? true : false)
		});
		
		checkForUserRole2((data) => {
			self.hasSetCompliance(getTextValue(data.IsCurrentUserRoles) === 'true' ? true : false)
		});
	})();

}

function getTextAfter(iText, imatchString) {
	if (iText && imatchString) {
		return iText.substring(iText.indexOf(imatchString) + 1);
	}
	else {
		return iText;
	}
}

function _getTextValue(obj) {
	if (obj) {
		return obj && obj.text ? obj.text : obj;
	}
	else {
		return '';
	}
}

function toggle(data) {
	data(!data());
}

var l_ObligationListViewModel = new ObligationListViewModel();

$(document).ready(function () {
	var i_locale = getlocale();
    var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
    translateLabels("com/opentext/apps/contractcenter/ContractCenter/ContractCenter", i_locale);
    loadRTLIfRequired(i_locale, rtl_css);
	if (window.parent.parent) {
		obligationsListFrame = $('[src*="ObligationList.htm"]', window.parent.parent.document);
		if (obligationsListFrame) {
			obligationsListFrame.css('border', 'none');
		}
	}
	ko.applyBindings(l_ObligationListViewModel, document.getElementById("List_container"));
	createToastDiv();
});
$(document).on('focus', ".cc-datepicker", function () {
	var format = "yy-mm-dd";
	$("#input_oblRemDate").datepicker({
		dateFormat: format,
		orientation: "bottom",
		onSelect: function (dateText, inst) {
			l_obligationModel.oblReminderDate(dateText);
			l_obligationModel.oblReminderDateLocale(formateDatetoLocale(dateText));
			$("#input_oblRemDate").removeClass("cc-error");
			$("#input_oblAssigneeSLA").removeClass("cc-error");
			calculateAssigneeSLA();
		}
	});

	$("#input_oblDueDate").datepicker({
		dateFormat: format,
		orientation: "bottom",
		onSelect: function (dateText, inst) {
			l_obligationModel.oblDuedate(dateText);
			l_obligationModel.oblDuedateLocale(formateDatetoLocale(dateText));
			$("#input_oblDueDate").removeClass("cc-error");
			$("#input_oblAssigneeSLA").removeClass("cc-error");
			calculateAssigneeSLA();
		}
	});

	$("#input_oblRecurStartDate").datepicker({
		dateFormat: format,
		orientation: "bottom",
		onSelect: function (dateText, inst) {
			l_obligationModel.oblRecurStartDate(dateText);
			l_obligationModel.oblRecurStartDateLocale(formateDatetoLocale(dateText));
			l_obligationModel.oblReminderDate(dateText);
			l_obligationModel.oblReminderDateLocale(formateDatetoLocale(dateText));
			l_obligationModel.calculateDueDate();
			$("#input_oblRecurStartDate").removeClass("cc-error");
		}
	});

	$("#input_oblRecurEndDate").datepicker({
		dateFormat: format,
		orientation: "auto",
		beforeShow: function (input, inst) {
			// setTimeout(function () {
			// 	inst.dpDiv.css({
			// 		top: $("#input_oblRecurEndDate").offset().top + 25,
			// 		left: $("#input_oblRecurEndDate").offset().left
			// 	});
			// }, 0);
			var rect = input.getBoundingClientRect();
			setTimeout(function () {
				//Set your datepicker possition
				inst.dpDiv.css({ top: rect.top - 215, left: rect.left + 0 });
			}, 0);
		},
		onSelect: function (dateText, inst) {
			l_obligationModel.oblRecurEndDate(dateText);
			l_obligationModel.oblRecurEndDateLocale(formateDatetoLocale(dateText));
			$("#input_oblRecurEndDate").removeClass("cc-error");
		}
	});
});
function calculateAssigneeSLA() {
	if (l_obligationModel.oblReminderDate() != "" && l_obligationModel.oblDuedate != "") {
		var l_oblDuedateObj = new Date(l_obligationModel.oblDuedate());
		var l_oblRemdateObj = new Date(l_obligationModel.oblReminderDate());
		if (l_oblDuedateObj.getTime() > l_oblRemdateObj.getTime()) {
			l_obligationModel.oblAssigneeSLA((l_oblDuedateObj.getTime() - l_oblRemdateObj.getTime()) / (1000 * 60 * 60 * 24));
		} else {
			l_obligationModel.oblAssigneeSLA('');
		}
	} else {
		l_obligationModel.oblAssigneeSLA('');
	}
}
function updateandDisableRecProp() {
	if ($('#select_oblRecurType').val() != "DONOTREPEAT") {
		if (!l_obligationModel.oblRecurStartDate()) {
			l_obligationModel.oblRecurStartDate(l_obligationModel.oblReminderDate ? l_obligationModel.oblReminderDate() : '');
			l_obligationModel.oblRecurStartDateLocale(l_obligationModel.oblReminderDateLocale ? l_obligationModel.oblReminderDateLocale() : '');
		}
		$("#input_oblDueDate").attr("disabled", true);
		$("#input_oblRemDate").attr("disabled", true);
	} else {
		$("#input_oblDueDate").attr("disabled", false);
		$("#input_oblRemDate").attr("disabled", false);
	}
}
//new Obligation Model
var ObligationDetailsModel = function () {

	var self = this;
	self.oblGenID = ko.observable('');
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

	self.oblAssigneeSLA = ko.observable('');
	self.oblFESLA = ko.observable('');
	self.oblSESLA = ko.observable('');

	self.oblAsignRoleId = ko.observable('');
	self.oblAsignRoleName = ko.observable('');
	self.oblFERoleId = ko.observable('');
	self.oblFERoleName = ko.observable('');
	self.oblSERoleId = ko.observable('');
	self.oblSERoleName = ko.observable('');
	
	self.oblAsignGroupId = ko.observable('');
	self.oblAsignGroupName = ko.observable('');
	self.oblFEGroupId = ko.observable('');
	self.oblFEGroupName = ko.observable('');
	self.oblSEGroupId = ko.observable('');
	self.oblSEGroupName = ko.observable('');

	self.oblRecurType = ko.observable('');
	self.oblRecurStartDate = ko.observable('');
	self.oblRecurStartDateLocale = ko.observable('');
	self.oblRecurMonthFreq = ko.observable('');
	self.oblRecurMonthDay = ko.observable('');
	self.oblRecurWeekFreq = ko.observable('');
	self.oblRecurWeekDay = ko.observable('');
	self.oblRecurDayFreq = ko.observable('');
	self.oblRecurEndType = ko.observable('');
	self.oblRecurEndinOccurences = ko.observable('');
	self.oblRecurEndDate = ko.observable('');
	self.oblRecurEndDateLocale = ko.observable('');

	self.stage = ko.observable(0);
	self.Status = ko.observable('');

	self.updateRecProp = function () {
		/*if(self.oblRecurType() != "DONOTREPEAT"){
			if(!self.oblRecurStartDate){
				self.oblRecurStartDate(self.oblReminderDate ? self.oblReminderDate() : '');
				self.oblRecurStartDateLocale(self.oblReminderDateLocale ? self.oblReminderDateLocale() : '');
			}
			$("#input_oblDueDate").attr("disabled", true);
			$("#input_oblRemDate").attr("disabled", true);
		} else {
			$("#input_oblDueDate").attr("disabled", false);
			$("#input_oblRemDate").attr("disabled", false);
		}*/
	}

	self.calculateDueDate = function () {
		if (self.oblAssigneeSLA() != "" && self.oblReminderDate() != "") {
			var l_oblRemdateObj = new Date(self.oblReminderDate());
			var l_oblDuedateObj = new Date(self.oblReminderDate());
			l_oblDuedateObj.setDate(l_oblRemdateObj.getDate() + Number(self.oblAssigneeSLA()));
			self.oblDuedate(l_oblDuedateObj.toISOString().split('T')[0]);
			self.oblDuedateLocale(formateDatetoLocale(self.oblDuedate()))
		} else {

		}
	}

	self.resetSlectedValues = function () {
		self.oblReminderDate('');
		self.oblDuedate('');
		self.oblAssigneeId('');
		self.oblFEId('');
		self.oblSEId('');
		self.oblAsignRoleId('');
		self.oblFERoleId('');
		self.oblSERoleId('');
		self.oblAssigneeSLA('');
		self.oblFESLA('');
		self.oblSESLA('');
		self.oblRecurType('DONOTREPEAT');
		self.oblRecurStartDate('');
		self.oblRecurStartDateLocale('');
		self.oblRecurMonthFreq('');
		self.oblRecurMonthDay('');
		self.oblRecurWeekFreq('');
		self.oblRecurWeekDay('');
		self.oblRecurDayFreq('');
		self.oblRecurEndType('ONTHISDATE');
		self.oblRecurEndinOccurences('');
		self.oblRecurEndDate('');
		self.oblRecurEndDateLocale('');
		self.stage = ko.observable(0);
		self.Status = ko.observable('');
	}
}

function getCreationInput() {
	var l_paramObj = {};
	var l_oblParamObj = {};
	var l_hasChanges = false;
	if (!l_oblObj.obligationId1) {
		l_hasChanges = true;
		l_oblParamObj["Name"] = l_obligationModel.oblName();
		l_oblParamObj["Description"] = l_obligationModel.oblDescription();
		l_oblParamObj["ActionTobeTaken"] = l_obligationModel.oblActionTobeTaken();
		l_oblParamObj["ReminderTitle"] = l_obligationModel.oblReminderTitle();
		l_oblParamObj["DueDate"] = l_obligationModel.oblDuedate();
		l_oblParamObj["ReminderDate"] = l_obligationModel.oblReminderDate();
		l_oblParamObj["AssignedTo"] = l_obligationModel.oblAssigneeId();
		l_oblParamObj["FEContact"] = l_obligationModel.oblFEId();
		l_oblParamObj["SEContact"] = l_obligationModel.oblSEId();
		l_oblParamObj["AssigneeRole"] = l_obligationModel.oblAsignRoleId();
		l_oblParamObj["FERole"] = l_obligationModel.oblFERoleId();
		l_oblParamObj["SERole"] = l_obligationModel.oblSERoleId();
		l_oblParamObj["AssigneeGroup"] = l_obligationModel.oblAsignGroupId();
		l_oblParamObj["FEGroup"] = l_obligationModel.oblFEGroupId();
		l_oblParamObj["SEGroup"] = l_obligationModel.oblSEGroupId();
		l_oblParamObj["AssigneeESCDuration"] = l_obligationModel.oblAssigneeSLA();
		l_oblParamObj["FESCDuration"] = l_obligationModel.oblFESLA();
		l_oblParamObj["SESCDuration"] = l_obligationModel.oblSESLA();
		l_oblParamObj["RecurrenceType"] = l_obligationModel.oblRecurType();
		l_oblParamObj["RecurrenceStart"] = l_obligationModel.oblRecurStartDate();
		l_oblParamObj["RecurDayOfMonth"] = l_obligationModel.oblRecurMonthDay();
		l_oblParamObj["RecurDaysOfWeek"] = l_obligationModel.oblRecurWeekDay();
		if (l_obligationModel.oblRecurType() == "REPEATMONTHLY") {
			l_oblParamObj["RecurFrequency"] = l_obligationModel.oblRecurMonthFreq();
		} else if (l_obligationModel.oblRecurType() == "REPEATWEEKLY") {
			l_oblParamObj["RecurFrequency"] = l_obligationModel.oblRecurWeekFreq();
		} else if (l_obligationModel.oblRecurType() == "REPEATDAILY") {
			l_oblParamObj["RecurFrequency"] = l_obligationModel.oblRecurDayFreq();
		}
		l_oblParamObj["RecurrenceEndType"] = l_obligationModel.oblRecurEndType();
		l_oblParamObj["RecurEndInOccur"] = l_obligationModel.oblRecurEndinOccurences();
		l_oblParamObj["RecurrenceEnd"] = l_obligationModel.oblRecurEndDate();
		l_oblParamObj["Status"] = l_obligationModel.Status();
	}
	else {
		if (l_obligationModel.oblName() != l_oblObj.ObligationName()) {
			l_oblParamObj["Name"] = l_obligationModel.oblName();
			l_hasChanges = true;
		}
		if (l_obligationModel.oblDescription() != l_oblObj.Description()) {
			l_hasChanges = true;
			if (l_obligationModel.oblDescription()) {
				l_oblParamObj["Description"] = l_obligationModel.oblDescription();
			}
			else {
				l_oblParamObj["Description"] = { '@nil': 'true' };
			}
		}
		if (l_obligationModel.oblActionTobeTaken() != l_oblObj.ObligationAction()) {
			l_hasChanges = true;
			if (l_obligationModel.oblActionTobeTaken()) {
				l_oblParamObj["ActionTobeTaken"] = l_obligationModel.oblActionTobeTaken();
			}
			else {
				l_oblParamObj["ActionTobeTaken"] = { '@nil': 'true' };
			}
		}
		if (l_obligationModel.oblReminderTitle() != l_oblObj.ObligationReminder()) {
			l_hasChanges = true;
			l_oblParamObj["ReminderTitle"] = l_obligationModel.oblReminderTitle();
		}
		if (l_obligationModel.oblDuedate() != l_oblObj.Z_DueDate()) {
			l_hasChanges = true;
			l_oblParamObj["DueDate"] = l_obligationModel.oblDuedate();
		}
		if (l_obligationModel.oblReminderDate() != l_oblObj.Z_RemainderSetFor()) {
			l_hasChanges = true;
			l_oblParamObj["ReminderDate"] = l_obligationModel.oblReminderDate();
		}
		if (l_obligationModel.oblAssigneeId() != l_oblObj.AssignedToId()) {
			l_hasChanges = true;
			if (l_obligationModel.oblAssigneeId()) {
				l_oblParamObj["AssignedTo"] = l_obligationModel.oblAssigneeId();
			}
			else {
				l_oblParamObj["AssignedTo"] = { '@nil': 'true' };
			}
		}
		if (l_obligationModel.oblAsignRoleId() != l_oblObj.AssignedRoleId()) {
			l_hasChanges = true;
			if (l_obligationModel.oblAsignRoleId()) {
				l_oblParamObj["AssigneeRole"] = l_obligationModel.oblAsignRoleId();
			}
			else {
				l_oblParamObj["AssigneeRole"] = { '@nil': 'true' };
			}
		}
		if (l_obligationModel.oblAsignGroupId() != l_oblObj.AssignedGroupId()) {
			l_hasChanges = true;
			if (l_obligationModel.oblAsignGroupId()) {
				l_oblParamObj["AssigneeGroup"] = l_obligationModel.oblAsignGroupId();
			}
			else {
				l_oblParamObj["AssigneeGroup"] = { '@nil': 'true' };
			}
		}
		if (l_obligationModel.oblFEId() != l_oblObj.FirstEscalationContactId()) {
			l_hasChanges = true;
			if (l_obligationModel.oblFEId()) {
				l_oblParamObj["FEContact"] = l_obligationModel.oblFEId();
			}
			else {
				l_oblParamObj["FEContact"] = { '@nil': 'true' };
			}
		}
		if (l_obligationModel.oblFERoleId() != l_oblObj.FESCAssignedRoleId()) {
			l_hasChanges = true;
			if (l_obligationModel.oblFERoleId()) {
				l_oblParamObj["FERole"] = l_obligationModel.oblFERoleId();
			}
			else {
				l_oblParamObj["FERole"] = { '@nil': 'true' };
			}
		}
		if (l_obligationModel.oblFEGroupId() != l_oblObj.FESCAssignedGroupId()) {
			l_hasChanges = true;
			if (l_obligationModel.oblFEGroupId()) {
				l_oblParamObj["FEGroup"] = l_obligationModel.oblFEGroupId();
			}
			else {
				l_oblParamObj["FEGroup"] = { '@nil': 'true' };
			}
		}
		if (l_obligationModel.oblSEId() != l_oblObj.SecondEscalationContactId()) {
			l_hasChanges = true;
			if (l_obligationModel.oblSEId()) {
				l_oblParamObj["SEContact"] = l_obligationModel.oblSEId();
			}
			else {
				l_oblParamObj["SEContact"] = { '@nil': 'true' };
			}
		}
		if (l_obligationModel.oblSERoleId() != l_oblObj.SESCAssignedRoleId()) {
			l_hasChanges = true;
			if (l_obligationModel.oblSERoleId()) {
				l_oblParamObj["SERole"] = l_obligationModel.oblSERoleId();
			}
			else {
				l_oblParamObj["SERole"] = { '@nil': 'true' };
			}
		}
		if (l_obligationModel.oblSEGroupId() != l_oblObj.SESCAssignedGroupId()) {
			l_hasChanges = true;
			if (l_obligationModel.oblSEGroupId()) {
				l_oblParamObj["SEGroup"] = l_obligationModel.oblSEGroupId();
			}
			else {
				l_oblParamObj["SEGroup"] = { '@nil': 'true' };
			}
		}
		if (l_obligationModel.oblRecurType() != l_oblObj.RecurType()) {
			l_hasChanges = true;
			l_oblParamObj["RecurrenceType"] = l_obligationModel.oblRecurType();
		}
		if (l_obligationModel.oblRecurStartDate() != l_oblObj.Z_RecurStartDate()) {
			l_hasChanges = true;
			l_oblParamObj["RecurrenceStart"] = l_obligationModel.oblRecurStartDate();
		}
		if (l_obligationModel.oblRecurMonthDay() != l_oblObj.RecurMonthDay()) {
			l_hasChanges = true;
			l_oblParamObj["RecurDayOfMonth"] = l_obligationModel.oblRecurMonthDay();
		}
		if (l_obligationModel.oblRecurWeekDay() != l_oblObj.RecurWeekDay()) {
			l_hasChanges = true;
			l_oblParamObj["RecurDaysOfWeek"] = l_obligationModel.oblRecurWeekDay();
		}
		if (l_obligationModel.oblRecurEndType() != l_oblObj.RecurEndType()) {
			l_hasChanges = true;
			l_oblParamObj["RecurrenceEndType"] = l_obligationModel.oblRecurEndType();
		}
		if (l_obligationModel.oblRecurEndDate() != l_oblObj.Z_RecurEndDate()) {
			l_hasChanges = true;
			l_oblParamObj["RecurrenceEnd"] = l_obligationModel.oblRecurEndDate();
		}
		if (l_obligationModel.oblRecurEndinOccurences() != l_oblObj.RecurEndinOccurences()) {
			l_hasChanges = true;
			l_oblParamObj["RecurEndInOccur"] = l_obligationModel.oblRecurEndinOccurences();
		}
		if (l_obligationModel.oblRecurType() == "REPEATMONTHLY") {
			if (l_oblObj.RecurMonthFreq) {
				if (l_obligationModel.oblRecurMonthFreq() != l_oblObj.RecurMonthFreq()) {
					l_hasChanges = true;
					l_oblParamObj["RecurFrequency"] = l_obligationModel.oblRecurMonthFreq();
				}
			} else {
				l_hasChanges = true;
				l_oblParamObj["RecurFrequency"] = l_obligationModel.oblRecurMonthFreq();
			}
		}
		if (l_obligationModel.oblRecurType() == "REPEATWEEKLY") {
			if (l_oblObj.RecurWeekFreq) {
				if (l_obligationModel.oblRecurWeekFreq() != l_oblObj.RecurWeekFreq()) {
					l_hasChanges = true;
					l_oblParamObj["RecurFrequency"] = l_obligationModel.oblRecurWeekFreq();
				}
			} else {
				l_hasChanges = true;
				l_oblParamObj["RecurFrequency"] = l_obligationModel.oblRecurWeekFreq();
			}
		}
		if (l_obligationModel.oblRecurType() == "REPEATDAILY") {
			if (l_oblObj.RecurDayFreq) {
				if (l_obligationModel.oblRecurDayFreq() != l_oblObj.RecurDayFreq()) {
					l_hasChanges = true;
					l_oblParamObj["RecurFrequency"] = l_obligationModel.oblRecurDayFreq();
				}
			} else {
				l_hasChanges = true;
				l_oblParamObj["RecurFrequency"] = l_obligationModel.oblRecurDayFreq();
			}
		}
		if (l_obligationModel.oblAssigneeSLA() != l_oblObj.AssigneeESCDuration()) {
			l_hasChanges = true;
			if (l_obligationModel.oblAssigneeSLA()) {
				l_oblParamObj["AssigneeESCDuration"] = l_obligationModel.oblAssigneeSLA();
			}
			else {
				l_oblParamObj["AssigneeESCDuration"] = { '@nil': 'true' };
			}
		}
		if (l_obligationModel.oblFESLA() != l_oblObj.FESCDuration()) {
			l_hasChanges = true;
			if (l_obligationModel.oblFESLA()) {
				l_oblParamObj["FESCDuration"] = l_obligationModel.oblFESLA();
			}
			else {
				l_oblParamObj["FESCDuration"] = { '@nil': 'true' };
			}
		}
		if (l_obligationModel.oblSESLA() != l_oblObj.SESCDuration()) {
			l_hasChanges = true;
			if (l_obligationModel.oblSESLA()) {
				l_oblParamObj["SESCDuration"] = l_obligationModel.oblSESLA();
			}
			else {
				l_oblParamObj["SESCDuration"] = { '@nil': 'true' };
			}
		}
	}
	if (l_hasChanges) {
		if (l_oblObj.obligationId1) {
			l_oblParamObj["ObligationId"] = l_oblObj.obligationId1;
		}
		l_oblParamObj["ContractId"] = contractId;
		l_paramObj["Obligation"] = l_oblParamObj;
	}
	return l_paramObj;
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
		$(event.currentTarget.parentElement).find('.cc-radio-on').removeClass("cc-radio-on");
		$(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
		if (iItem.ParticipatingPerson.PersonToUser["Identity-id"]) {
			var l_Id = iItem.ParticipatingPerson.PersonToUser["Identity-id"].Id;
			self.selectedUserID(l_Id);
			self.selectedUserName(getTextValue(iItem.ParticipatingPerson.PersonToUser.IdentityDisplayName));
			$("#btn_userSelectionYes").attr("disabled", false);
		}
	}
	self.UserRowRadiobuttonClicked = function (iItem, event) {
		$(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-radio-on').removeClass("cc-radio-on");
		$(event.currentTarget).addClass("cc-radio-on");
		if (iItem.ParticipatingPerson.PersonToUser["Identity-id"]) {
			var l_Id = iItem.ParticipatingPerson.PersonToUser["Identity-id"].Id;
			self.selectedUserID(l_Id);
			self.selectedUserName(getTextValue(iItem.ParticipatingPerson.PersonToUser.IdentityDisplayName));
			$("#btn_userSelectionYes").attr("disabled", false);
		}
		event.stopPropagation();
		return true;
	}

}

var RolesandGroupsListModel = function () {
	var self = this;
	
	self.RolesType = ko.observable('Groups');
	self.RolesList = ko.observableArray([]);
	self.GroupsList = ko.observableArray([]);
	self.selectedRoleItemID = ko.observable('');
	self.selectedRoleName = ko.observable('');
	self.selectedGroupItemID = ko.observable('');
	self.selectedGroupName = ko.observable('');
	
	self.selectRoleRadioButton = function (iItem, event) {
		$(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
		$(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
		if (iItem["Identity-id"]) {
			self.selectedRoleItemID(iItem["Identity-id"].Id);
			self.selectedRoleName(getTextValue(iItem.Name));
			$("#btn_userSelectionYes").attr("disabled", false);
		}
	}
	self.roleRowRadioClicked = function (iItem, event) {
		$(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
		$(event.currentTarget).addClass("cc-radio-on");
		if (iItem["Identity-id"]) {
			self.selectedRoleItemID(iItem["Identity-id"].Id);
			self.selectedRoleName(getTextValue(iItem.Name));
			$("#btn_userSelectionYes").attr("disabled", false);
		}
		event.stopPropagation();
		return true;
	}
	self.selectGroupRadioButton = function (iItem, event) {
		$(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
		$(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
		if (iItem["Identity-id"]) {
			self.selectedGroupItemID(iItem["Identity-id"].Id);
			self.selectedGroupName(getTextValue(iItem.Name));
			$("#btn_userSelectionYes").attr("disabled", false);
		}
	}
	self.groupRowRadioClicked = function (iItem, event) {
		$(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
		$(event.currentTarget).addClass("cc-radio-on");
		if (iItem["Identity-id"]) {
			self.selectedGroupItemID(iItem["Identity-id"].Id);
			self.selectedGroupName(getTextValue(iItem.Name));
			$("#btn_userSelectionYes").attr("disabled", false);
		}
		event.stopPropagation();
		return true;
	}
	
	
	self.updateRolesType = function (value, iItem, event) {
        self.RolesType(value);
		if ($('#input_roleListSearchFilter')) {
			$('#input_roleListSearchFilter').val('')
		}
		if ($('#input_groupListSearchFilter')) {
			$('#input_groupListSearchFilter').val('')
		}
        ListAllRoles();
		ListAllGroups();
		
        event.stopPropagation();
    }
}

var OBLLibraryListModel = function () {
	var self = this;
	self.OBLibList = ko.observableArray([]);
	self.selectedOBItemID = ko.observable('');
	self.selectedOBName = ko.observable('');
	self.selectedOBDesc = ko.observable('');
	self.selectedOBAction = ko.observable('');
	self.selectedOBTitle = ko.observable('');
	self.OBRadioButton = function (iItem, event) {
		$(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
		$(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
		if (iItem["ObligationLibrary-id"]) {
			self.selectedOBItemID(iItem["ObligationLibrary-id"].Id);
			self.selectedOBName(getTextValue(iItem.Name));
			self.selectedOBDesc(getTextValue(iItem.Description));
			self.selectedOBAction(getTextValue(iItem.ActionToBeTaken));
			self.selectedOBTitle(getTextValue(iItem.ReminderTitle));
			$("#btn_OBSelectionYes").attr("disabled", false);
		}
	}
	self.OBRowRadioClicked = function (iItem, event) {
		$(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
		$(event.currentTarget).addClass("cc-radio-on");
		if (iItem["ObligationLibrary-id"]) {
			self.selectedOBItemID(iItem["ObligationLibrary-id"].Id);
			self.selectedOBName(getTextValue(iItem.Name));
			self.selectedOBDesc(getTextValue(iItem.Description));
			self.selectedOBAction(getTextValue(iItem.ActionToBeTaken));
			self.selectedOBTitle(getTextValue(iItem.ReminderTitle));
			$("#btn_OBSelectionYes").attr("disabled", false);
		}
		event.stopPropagation();
		return true;
	}
}

var OBLSetComplianceModel = function () {
	var self = this;

	self.metType = ko.observable();
	self.oblnonCompliance = ko.observable('');
	self.complComments = ko.observable('');
	self.oblnonComplianceItem = ko.observable('');
	self.complianceStatus = ko.observable('');
	self.hasErrors = ko.observable(false);
	self.metTypeError = ko.observable(false);
	self.complDropDown = ko.observableArray([
		{ value: "Met", label: "Met" },
		{ value: "NotMet", label: "Not-Met" }
	]);
	self.onComplChange = (iItem)=>{
		if(iItem.complianceStatus()!=undefined){
			self.hasErrors(false);
		}
	}
	self.processDrpDown = ko.observableArray([
		{ value: "Completed-Corrected", label: "Corrected", disable: ko.observable(true) },
		{ value: "Completed-Override", label: "Override", disable: ko.observable(true) }
	]);
	self.onmetTypeChange = (iItem)=>{
		if(iItem.metType()!=undefined){
			self.metTypeError(false);
		}
	}
	
	
	self.setOptionDisable= function (option, item) {
		let cmps = selectedOBRow[Object.keys(selectedOBRow)[0]].ComplianceLevel();
		if(item!=undefined){
        if(item.value === "Completed-Corrected"){
			if(cmps!='NotMet' && cmps!='NotMet-PendingRecurrences'){
				self.processDrpDown()[0].disable(true);
			}else {
				self.processDrpDown()[0].disable(false);
			}	
		}
		if(item.value === "Completed-Override"){
			if(cmps==='InProgress-Escalated' || cmps==='InProgress' || cmps==='Met-ReviewInProgress' || cmps==='NotMet-ReviewInProgress') {
				self.processDrpDown()[1].disable(false);
			}else {
				self.processDrpDown()[1].disable(true);
			}	
		} 
		ko.applyBindingsToNode(option, {
			disable: item.disable
		}, item);	
	 }
    }
}

var OBLNonComplianceListModel = function () {
	var self = this;
	self.nonComplianceList = ko.observableArray([]);
	self.selectedNCItemID = ko.observable('');
	self.selectedNCName = ko.observable('');
	self.selectedNCDesc = ko.observable('');

	self.NCRadioButton = function (iItem, event) {
		$(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
		$(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
		if (iItem["OBLNonCompliance-id"]) {
			self.selectedNCItemID(iItem["OBLNonCompliance-id"].Id);
			self.selectedNCName(getTextValue(iItem.Name));
			self.selectedNCDesc(getTextValue(iItem.Description));
			$("#btn_ncYes").attr("disabled", false);
		}
	}
	self.NCRowRadioClicked = function (iItem, event) {
		$(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
		$(event.currentTarget).addClass("cc-radio-on");
		if (iItem["OBLNonCompliance-id"]) {
			self.selectedNCItemID(iItem["OBLNonCompliance-id"].Id);
			self.selectedNCName(getTextValue(iItem.Name));
			self.selectedNCDesc(getTextValue(iItem.Description));
			$("#btn_ncYes").attr("disabled", false);
		}
		event.stopPropagation();
		return true;
	}

}

var OBLComplHistoryModel = function () {
	var self = this;
	self.complHistoryList = ko.observableArray([]);

	self.closeComplHistoryDialog = function() {
		$("#div_complHistoryDialog").modal('hide');
		self.complHistoryList.removeAll();
	}
}

function makeObligationDialogReady() {

	if (!contractId) {
		var cInstanceId = getUrlParameterValue("instanceId", null, true);
		contractId = cInstanceId;
	}
	if (!l_OrgID) {
		if (contractObj) {
			l_OrgID = contractObj.item.RelatedOrganization.relatedItemId;
			l_OrgID = l_OrgID.substring(l_OrgID.indexOf(".") + 1);
		}
		else {
			l_OrgID = getUrlParameterValue("orgID", null, true);
		}
	}
	if (!l_obligationModel) {
		l_obligationModel = new ObligationDetailsModel(cInstanceId);
		ko.applyBindings(l_obligationModel, document.getElementById("div_CreateObligForm"));
		//setTimeout(function () {
		//	l_obligationModel.bindJqueryFunc();
		//}, 0);
		//l_obligationModel.oblRecurType('DONOTREPEAT');
		//l_obligationModel.oblRecurEndType('NOENDDATE');
	}
	if (!l_usersList_model) {
		l_usersList_model = new UsersListModel();
		ko.applyBindings(l_usersList_model, document.getElementById("div_usersList"));
		ListOrganizationUsers();
	}
	if (!l_rolesandGroupsList_model) {
		l_rolesandGroupsList_model = new RolesandGroupsListModel();
		ko.applyBindings(l_rolesandGroupsList_model, document.getElementById("div_roleList"));
		ListAllRoles();
		ListAllGroups();
	}

	l_obligationModel.oblGenID(l_oblObj.ObligationGeneratedID ? l_oblObj.ObligationGeneratedID() : '');
	l_obligationModel.oblName(l_oblObj.ObligationName ? l_oblObj.ObligationName() : '');
	l_obligationModel.oblDescription(l_oblObj.Description ? l_oblObj.Description() : '');
	l_obligationModel.oblActionTobeTaken(l_oblObj.ObligationAction ? l_oblObj.ObligationAction() : '');
	l_obligationModel.oblReminderTitle(l_oblObj.ObligationReminder ? l_oblObj.ObligationReminder() : '');
	l_obligationModel.oblReminderDateLocale(l_oblObj.RemainderSetFor ? l_oblObj.RemainderSetFor() : '');
	l_obligationModel.oblDuedateLocale(l_oblObj.Due_Date ? l_oblObj.Due_Date() : '');
	l_obligationModel.oblSEName(l_oblObj.SecondEscalationContact ? l_oblObj.SecondEscalationContact() : '');
	l_obligationModel.oblFEName(l_oblObj.FirstEscalationContact ? l_oblObj.FirstEscalationContact() : '');
	l_obligationModel.oblAssigneeName(l_oblObj.AssignedTo ? l_oblObj.AssignedTo() : '');
	l_obligationModel.oblAsignRoleName(l_oblObj.AssignedRole ? l_oblObj.AssignedRole() : '');
	l_obligationModel.oblFERoleName(l_oblObj.FESCAssignedRole ? l_oblObj.FESCAssignedRole() : '');
	l_obligationModel.oblSERoleName(l_oblObj.SESCAssignedRole ? l_oblObj.SESCAssignedRole() : '');
	l_obligationModel.oblAsignGroupName(l_oblObj.AssignedGroup ? l_oblObj.AssignedGroup() : '');
	l_obligationModel.oblFEGroupName(l_oblObj.FESCAssignedGroup ? l_oblObj.FESCAssignedGroup() : '');
	l_obligationModel.oblSEGroupName(l_oblObj.SESCAssignedGroup ? l_oblObj.SESCAssignedGroup() : '');
	l_obligationModel.resetSlectedValues();
	l_obligationModel.oblSEId(l_oblObj.SecondEscalationContactId ? l_oblObj.SecondEscalationContactId() : '');
	l_obligationModel.oblFEId(l_oblObj.FirstEscalationContactId ? l_oblObj.FirstEscalationContactId() : '');
	l_obligationModel.oblAssigneeId(l_oblObj.AssignedToId ? l_oblObj.AssignedToId() : '');
	l_obligationModel.oblAsignRoleId(l_oblObj.AssignedRoleId ? l_oblObj.AssignedRoleId() : '');
	l_obligationModel.oblFERoleId(l_oblObj.FESCAssignedRoleId ? l_oblObj.FESCAssignedRoleId() : '');
	l_obligationModel.oblSERoleId(l_oblObj.SESCAssignedRoleId ? l_oblObj.SESCAssignedRoleId() : '');
	l_obligationModel.oblAsignGroupId(l_oblObj.AssignedGroupId ? l_oblObj.AssignedGroupId() : '');
	l_obligationModel.oblFEGroupId(l_oblObj.FESCAssignedGroupId ? l_oblObj.FESCAssignedGroupId() : '');
	l_obligationModel.oblSEGroupId(l_oblObj.SESCAssignedGroupId ? l_oblObj.SESCAssignedGroupId() : '');
	l_obligationModel.oblAssigneeSLA(l_oblObj.AssigneeESCDuration ? l_oblObj.AssigneeESCDuration() : '');
	l_obligationModel.oblFESLA(l_oblObj.FESCDuration ? l_oblObj.FESCDuration() : '');
	l_obligationModel.oblSESLA(l_oblObj.SESCDuration ? l_oblObj.SESCDuration() : '');
	l_obligationModel.oblReminderDate(l_oblObj.Z_RemainderSetFor ? l_oblObj.Z_RemainderSetFor() : '');
	l_obligationModel.oblDuedate(l_oblObj.Z_DueDate ? l_oblObj.Z_DueDate() : '');

	l_obligationModel.oblRecurType(l_oblObj.RecurType ? l_oblObj.RecurType() : '');
	l_obligationModel.oblRecurStartDate(l_oblObj.Z_RecurStartDate ? l_oblObj.Z_RecurStartDate() : '');
	l_obligationModel.oblRecurStartDateLocale(l_oblObj.RecurStartDate ? l_oblObj.RecurStartDate() : '');
	l_obligationModel.oblRecurMonthFreq(l_oblObj.RecurMonthFreq ? l_oblObj.RecurMonthFreq() : '');
	l_obligationModel.oblRecurMonthDay(l_oblObj.RecurMonthDay ? l_oblObj.RecurMonthDay() : '');
	l_obligationModel.oblRecurWeekFreq(l_oblObj.RecurWeekFreq ? l_oblObj.RecurWeekFreq() : '');
	l_obligationModel.oblRecurWeekDay(l_oblObj.RecurWeekDay ? l_oblObj.RecurWeekDay() : '');
	l_obligationModel.oblRecurDayFreq(l_oblObj.RecurDayFreq ? l_oblObj.RecurDayFreq() : '');
	l_obligationModel.oblRecurEndType(l_oblObj.RecurEndType ? l_oblObj.RecurEndType() : '');
	l_obligationModel.oblRecurEndinOccurences(l_oblObj.RecurEndinOccurences ? l_oblObj.RecurEndinOccurences() : '');
	l_obligationModel.oblRecurEndDate(l_oblObj.Z_RecurEndDate ? l_oblObj.Z_RecurEndDate() : '');
	l_obligationModel.oblRecurEndDateLocale(l_oblObj.RecurEndDate ? l_oblObj.RecurEndDate() : '');

	l_obligationModel.stage(l_oblObj.stage ? l_oblObj.stage() : 0);
	l_obligationModel.Status(l_oblObj.Status ? l_oblObj.Status() : '');
	enableDisableFields();
}
function enableDisableFields() {
	removeErrorClass("#input_oblName");
	removeErrorClass("#input_oblDueDate");
	removeErrorClass("#input_oblRemTitle");
	removeErrorClass("#input_oblRemDate");
	removeErrorClass("#input_oblAssignee");
	removeErrorClass(".cc-input-browse-icon.cc-error");
	$("#div_formErrorInfoArea").hide();
	if(l_obligationModel.Status()=="INACTIVE" || l_obligationModel.Status()=="INVALID" || l_obligationModel.Status()=="INACTIVEINPROGRESS" || l_obligationModel.Status()=="INVALIDINPROGRESS"){
		$("#input_oblRemTitle, #input_oblRemDate, #input_oblAssignee, #input_oblAsignRole, #input_oblAsignGroup, #input_oblName, #input_oblDueDate, #input_OblFE, #input_OblFERole, #input_OblFEGroup, #id_oblDescription, #id_oblActionTaken, #input_oblAssigneeSLA, #input_oblFirstEscSLA, #input_OblSE, #input_oblSERole, #input_OblSEGroup, #input_oblSecEscSLA, #select_oblRecurType, .oblRecurProp").prop("disabled", true);
		return;
	}
	$("#input_OblSE, #input_oblSERole, #input_OblSEGroup").prop("disabled", (l_obligationModel.stage() > 2));
	$("#input_OblSE, #input_oblSERole, #input_OblSEGroup").next().prop("disabled", (l_obligationModel.stage() > 2));
	$("#input_OblSE, #input_oblSERole, #input_OblSEGroup").next().next().prop("disabled", (l_obligationModel.stage() > 2));
	$("#input_OblFE, #input_oblDueDate, #input_OblFERole, #input_OblFEGroup").prop("disabled", (l_obligationModel.stage() > 1));
	$("#input_OblFE, #input_OblFERole, #input_OblFEGroup").next().prop("disabled", (l_obligationModel.stage() > 1));
	$("#input_OblFE, #input_OblFERole, #input_OblFEGroup").next().next().prop("disabled", (l_obligationModel.stage() > 1));
	$("#input_oblRemTitle, #input_oblRemDate, #input_oblAssignee, #input_oblAsignRole, #input_oblAsignGroup").prop("disabled", (l_obligationModel.stage() > 0));
	$("#input_oblAssignee, #input_oblAsignRole, #input_oblAsignGroup").next().prop("disabled", (l_obligationModel.stage() > 0));
	$(".oblRecurProp").prop("disabled", (l_obligationModel.stage() > 0 && l_obligationModel.oblRecurType != "DONOTREPEAT"));
	//$("#input_oblDueDate, #input_oblRemDate").attr("disabled",l_obligationModel.stage() < 1 && l_obligationModel.oblRecurType() != "DONOTREPEAT");
}
function makeOBLibraryReady() {

	if (!l_oblLibrary_model) {
		l_oblLibrary_model = new OBLLibraryListModel();
		ko.applyBindings(l_oblLibrary_model, document.getElementById("div_OBLList"));
	}

}
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
	if (checkforMandatoryFields() & checkforFieldInputs()) {
		var l_paramInput = getCreationInput();
		if (l_paramInput && l_paramInput["Obligation"]) {
			$.cordys.ajax({
				namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
				method: "ValidateAndUpdateObligation",
				parameters: l_paramInput,
				success: function (data) {
					if (l_oblObj && l_oblObj.obligationId1) {
						successToast(3000, getTranslationMessage("Obligation updated."));
					}
					else {
						successToast(3000, getTranslationMessage("Obligation created."));
					}
					l_ObligationListViewModel.clearFilter();
					$("#obligationsDetails_Dialog").modal('hide');
				},
				error: function (responseFailure) {
					if (responseFailure.responseJSON.faultcode.text && (responseFailure.responseJSON.faultcode.text.indexOf("CC_APP_ERROR") >= 0)) {
						showOrHideErrorInfo("div_formErrorInfoArea", true, getTranslationMessage(responseFailure.responseJSON.faultstring.text), 10000);
					}
					else if (l_oblObj && l_oblObj.obligationId1) {
						showOrHideErrorInfo("div_formErrorInfoArea", true, getTranslationMessage("An error occured while updating the obligation. Contact the administrator."), 10000);
					}
					else {
						showOrHideErrorInfo("div_formErrorInfoArea", true, getTranslationMessage("An error occured while creating the obligation. Contact the administrator."), 10000);
					}
					return false;
				}
			});
		}
	}
}


function checkforFieldInputs() {
	_clearCheckforFieldInputs();
	var validationFlag = true;
	if (l_obligationModel.oblFESLA() && isNaN(parseInt(l_obligationModel.oblFESLA()))) {
		$("#input_oblFirstEscSLA").addClass("cc-error");
		validationFlag = false;
	}
	if (l_obligationModel.oblSESLA() && isNaN(parseInt(l_obligationModel.oblSESLA()))) {
		$("#input_oblSecEscSLA").addClass("cc-error");
		validationFlag = false;
	}

	setTimeout(function () {
		_clearCheckforFieldInputs();
	}, 5000);

	return validationFlag;
}

function _clearCheckforFieldInputs() {
	$("#input_oblFirstEscSLA").removeClass("cc-error");
	$("#input_oblSecEscSLA").removeClass("cc-error");
}

function checkforMandatoryFields() {
	var validationFlag = true;
	FillTextValuesToModel();
	if (!l_obligationModel.oblName()) {
		$("#input_oblName").addClass("cc-error");
		$("#input_oblName").next().addClass("cc-error");
		validationFlag = false;
	}
	if (!l_obligationModel.oblReminderTitle()) {
		$("#input_oblRemTitle").addClass("cc-error");
		validationFlag = false;
	}
	if ($('#select_oblRecurType').val() == "DONOTREPEAT" && !l_obligationModel.oblReminderDate()) {
		$("#input_oblRemDate").addClass("cc-error");
		validationFlag = false;
	}
	if ($('#select_oblRecurType').val() == "DONOTREPEAT" && !l_obligationModel.oblDuedate()) {
		$("#input_oblDueDate").addClass("cc-error");
		validationFlag = false;
	}
	if (!(l_obligationModel.oblAssigneeId() || l_obligationModel.oblAsignRoleId() || l_obligationModel.oblAsignGroupId())) {
		$("#input_oblAssignee").addClass("cc-error");
		$("#input_oblAssignee").next().addClass("cc-error");
		validationFlag = false;
	}
	if (l_obligationModel.oblAssigneeSLA()=="" || isNaN(parseInt(l_obligationModel.oblAssigneeSLA()))) {
		$("#input_oblAssigneeSLA").addClass("cc-error");
		$("#input_oblAssigneeSLA").next().addClass("cc-error");
		validationFlag = false;
	}
	if (l_obligationModel.oblRecurType() != "DONOTREPEAT") {
		if (!l_obligationModel.oblRecurStartDate()) {
			$("#input_oblRecurStartDate").addClass("cc-error");
			validationFlag = false;
		}
		if (l_obligationModel.oblRecurType() == "REPEATMONTHLY") {
			if (!l_obligationModel.oblRecurMonthFreq()) {
				$("#input_oblRecurMonthFreq").addClass("cc-error");
				validationFlag = false;
			}
			if (!l_obligationModel.oblRecurMonthDay()) {
				$("#select_oblRecurMonthDay").addClass("cc-error");
				validationFlag = false;
			}
		}
		if (l_obligationModel.oblRecurType() == "REPEATWEEKLY") {
			if (!l_obligationModel.oblRecurWeekFreq()) {
				$("#input_oblRecurWeekFreq").addClass("cc-error");
				validationFlag = false;
			}
			if (!l_obligationModel.oblRecurWeekDay()) {
				$("#select_oblRecurWeekDay").addClass("cc-error");
				validationFlag = false;
			}
		}
		if (l_obligationModel.oblRecurType() == "REPEATDAILY") {
			if (!l_obligationModel.oblRecurDayFreq()) {
				$("#input_oblRecurDayFreq").addClass("cc-error");
				validationFlag = false;
			}
		}
		if (l_obligationModel.oblRecurEndType() == "AFTEROCCUR") {
			if (!l_obligationModel.oblRecurEndinOccurences()) {
				$("#input_oblRecurEndinOccurences").addClass("cc-error");
				validationFlag = false;
			}
		}
		if (l_obligationModel.oblRecurEndType() == "ONTHISDATE") {
			if (!l_obligationModel.oblRecurEndDate()) {
				$("#input_oblRecurEndDate").addClass("cc-error");
				validationFlag = false;
			}
			var l_oblRecStart = new Date(l_obligationModel.oblRecurStartDate());
			var l_oblRecEnd = new Date(l_obligationModel.oblRecurEndDate());
			if (l_oblRecEnd.getTime() < l_oblRecStart.getTime()) {
				displayValidationError(getTranslationMessage("End date should be greater than start date."));
				validationFlag = false;
			}
		}
	}
	if (validationFlag) {
		if (l_obligationModel.oblSEId() || l_obligationModel.oblSERoleId() || l_obligationModel.oblSEGroupId()) {
			if (!(l_obligationModel.oblFEId() || l_obligationModel.oblFERoleId() || l_obligationModel.oblFEGroupId())) {
				validationFlag = false;
				displayValidationError(getTranslationMessage("First escalation contact needs to be filled to provide second escalation contact."));
			}
			if ((l_obligationModel.oblFEId() && (l_obligationModel.oblFEId() == l_obligationModel.oblSEId())) || (l_obligationModel.oblFERoleId() && (l_obligationModel.oblFERoleId() == l_obligationModel.oblSERoleId())) || (l_obligationModel.oblFEGroupId() && (l_obligationModel.oblFEGroupId() == l_obligationModel.oblSEGroupId()))) {
				displayValidationError(getTranslationMessage("First escalation and second escalation contacts cannot be same."));
				validationFlag = false;
			}
		}
		if ((l_obligationModel.oblAssigneeId() && (l_obligationModel.oblAssigneeId() == l_obligationModel.oblFEId())) || (l_obligationModel.oblFERoleId() && (l_obligationModel.oblFERoleId() == l_obligationModel.oblAsignRoleId())) || (l_obligationModel.oblFEGroupId() && (l_obligationModel.oblFEGroupId() == l_obligationModel.oblAsignGroupId()))) {
			displayValidationError(getTranslationMessage("Assignee and first escalation contact cannot be same."));
			validationFlag = false;
		}
		if ((l_obligationModel.oblAssigneeId() && (l_obligationModel.oblAssigneeId() == l_obligationModel.oblSEId())) || (l_obligationModel.oblSERoleId() && (l_obligationModel.oblSERoleId() == l_obligationModel.oblAsignRoleId())) || (l_obligationModel.oblSEGroupId() && (l_obligationModel.oblSEGroupId() == l_obligationModel.oblAsignGroupId()))) {
			displayValidationError(getTranslationMessage("Assignee and second escalation contact cannot be same."));
			validationFlag = false;
		}
		var l_oblDue = new Date(l_obligationModel.oblDuedate());
		var l_oblReminder = new Date(l_obligationModel.oblReminderDate());
		if (l_oblDue.getTime() < l_oblReminder.getTime()) {
			displayValidationError(getTranslationMessage("Due date should be greater than Reminder date."));
			validationFlag = false;
		}
	}
	else {
		displayValidationError(getTranslationMessage("Mandatory fields cannot be empty."));
	}
	return validationFlag;
}
function removeErrorClass(iEvent) {
	$(iEvent).removeClass("cc-error");
}

function displayValidationError(iValidationErrorMsg) {
	showOrHideErrorInfo("div_formErrorInfoArea", true, iValidationErrorMsg, 10000);
}

function ListOrganizationUsers() {
	var l_userNameFilter = document.getElementById("input_userListSearchFilter");
	$.cordys.ajax({
		namespace: "http://schemas.opentext.com/apps/cc/basiccomponents/20.2",
		method: "GetOrgMemberswithFilters",
		parameters: {
			"memUserID": l_userNameFilter?l_userNameFilter.value:"",
			"orgName": "",
			"orgID": l_OrgID,
			"offset": "0",
			"limit": "200"
		},
		success: function (data) {
			addDataToUsersView(data.OrgMembers.FindZ_INT_OrgUsersListResponse.OrganizationMembers, l_usersList_model);
		},
		error: function (responseFailure) {
			showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while loading Organization members. Contact the administrator."), 10000);
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

function ListAllRoles() {
	var l_roleNameFilter = document.getElementById("input_roleListSearchFilter");
	$.cordys.ajax({
		namespace: "http://schemas/OpenTextEntityIdentityComponents/Role/operations",
		method: "GetAllRoles",
		parameters: {
			"contains": l_roleNameFilter?l_roleNameFilter.value:""
		},
		success: function (data) {
			addDataToRolesView(data.Role, l_rolesandGroupsList_model);
		},
		error: function (responseFailure) {
			showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while loading the roles. Contact your administrator."), 10000);
			return false;
		}
	});
}

function addDataToRolesView(iElementList, iModel) {
	iModel.RolesList.removeAll();
	if (iElementList) {
		if (iElementList.length) {
			iElementList.forEach(function (iElement) {
				if (iElement.Package_Name.indexOf("Cordys@Work") == -1) {
					iModel.RolesList.push(iElement);
				}
			});
		}
		else {
			if (iElementList.Package_Name.indexOf("Cordys@Work") == -1) {
				iModel.RolesList.push(iElementList);
			}
		}
	}
}

function ListAllGroups() {
	var l_groupNameFilter = document.getElementById("input_groupListSearchFilter");
	$.cordys.ajax({
		namespace: "http://schemas/OpenTextEntityIdentityComponents/Group/operations",
		method: "GetAllGroups",
		parameters: {
			"contains": l_groupNameFilter?l_groupNameFilter.value:""
		},
		success: function (data) {
			addDataToGroupsView(data.Group, l_rolesandGroupsList_model);
		},
		error: function (responseFailure) {
			showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while loading the groups. Contact your administrator."), 10000);
			return false;
		}
	});
}

function addDataToGroupsView(iElementList, iModel) {
	iModel.GroupsList.removeAll();
	if (iElementList) {
		if (iElementList.length) {
			iElementList.forEach(function (iElement) {
				iModel.GroupsList.push(iElement);
			});
		}
		else {
			iModel.GroupsList.push(iElementList);
			
		}
	}
}

function ListObligationsLibrary() {
	var l_OBLNameFilter = document.getElementById("input_OBLLIBSearch");
	$.cordys.ajax({
		namespace: "http://schemas/OpenTextContractCenter/ObligationLibrary/operations",
		method: "GetObligationsByContractType",
		parameters: {
			"ContractTypeID": ctypeId,
			"ObligationName": l_OBLNameFilter.value
		},
		success: function (data) {
			addDataToOBLibraryView(data.ObligationLibrary, l_oblLibrary_model);
		},
		error: function (responseFailure) {
			showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while loading Obligations library. Contact the administrator."), 10000);
			return false;
		}
	});
}

function ListNonCompliances() {
	var l_nonComplianceFilter = document.getElementById("input_NonComplSearch");
	$.cordys.ajax({
		namespace: "http://schemas/OpenTextContractCenter/OBLNonCompliance/operations",
		method: "GetNonComplianceStates",
		parameters: {
			"SearchName": l_nonComplianceFilter.value
		},
		success: function (data) {
			addDataToNonComplianceView(data.OBLNonCompliance, l_oblNonComplianceListModal);
		},
		error: function (responseFailure) {
			showOrHideErrorInfo("div_formErrorInfoArea", true, getTranslationMessage("An error occurred while loading Obligation non compliances. Contact the administrator."), 10000);
			return false;
		}
	});
}


function addDataToOBLibraryView(iElementList, iModel) {
	iModel.OBLibList.removeAll();
	if (iElementList) {
		if (iElementList.length) {
			iElementList.forEach(function (iElement) {
				iModel.OBLibList.push(iElement);
			});
		}
		else {
			iModel.OBLibList.push(iElementList);
		}
	}
}

function addDataToNonComplianceView(iElementList, iModel) {
	iModel.nonComplianceList.removeAll();
	if (iElementList) {
		if (iElementList.length) {
			iElementList.forEach(function (iElement) {
				iModel.nonComplianceList.push(iElement);
			});
		}
		else {
			iModel.nonComplianceList.push(iElementList);
			debugger;
		}
	}
}

function addDataToComplianceHistory(iElementList, iModel) {
	
	if (iElementList) {
		iModel.complHistoryList.removeAll();
		if (Array.isArray(iElementList)) {
			for (var i = 0; i < iElementList.length; i++) {
				iModel.complHistoryList.push(ko.mapping.fromJS(_populateComplData(iElementList[i])));
			}
		} else {
			iModel.complHistoryList.push(ko.mapping.fromJS(_populateComplData(iElementList)));
		}

	} else {
		iModel.complHistoryList.removeAll();
	}
}

function fetchDisplayName(myobject, l_value) {
    let actualname = Object.keys(myobject).find(key => myobject[key] === l_value);
    return actualname;
}

function _populateComplData(response_data){
	var complObj = {};

	complObj.cycleNumber = (response_data.RelatedOBLCycle) ? _getTextValue(response_data.RelatedOBLCycle.CycleNumber):'Manual';
	complObj.ComplianceStatus = (response_data.ComplianceStatus) ? _getTextValue(response_data.ComplianceStatus):'-';
    complObj.itemID2 = _getTextValue(response_data["OBLComplianceData-id"].ItemId2);
	complObj.isLatest = l_oblObj.LatestComplItemID2 ? l_oblObj.LatestComplItemID2()=== complObj.itemID2:false;
	complObj.UpdatedTime = (response_data.UpdatedTime) ? moment(response_data.UpdatedTime).format("YYYY-MM-DD HH:mm"):'-';
	complObj.ProcessStatus = processStatusAlias[response_data.ProcessStatus];
	complObj.non_compliance = (response_data.RelatedNonCompliance) ?_getTextValue(response_data.RelatedNonCompliance.Name):'';
	complObj.Comments = response_data.Comments;
		return complObj;
		
}

function openAssigneeSelection() {
	if (l_obligationModel.stage() > 0 || l_obligationModel.Status()=="INVALID" || l_obligationModel.Status()=="INACTIVE" || l_obligationModel.Status()=="INACTIVEINPROGRESS" || l_obligationModel.Status()=="INVALIDINPROGRESS") {
		return;
	}
	l_dialogFor = "Assignee";
	showUserSelectionDialog();
	$("#btn_userSelectionYes").attr("disabled", true);
	$('button#btn_userSelectionYes').off("click");
	$('button#btn_userSelectionYes').on('click', function (_event) {
		if (l_rolesandGroupsList_model.selectedRoleItemID()) {
			l_obligationModel.oblAsignRoleId(l_rolesandGroupsList_model.selectedRoleItemID());
			l_obligationModel.oblAsignRoleName(l_rolesandGroupsList_model.selectedRoleName());
			l_obligationModel.oblAssigneeId('');
			l_obligationModel.oblAssigneeName('');
			l_obligationModel.oblAsignGroupId('');
			l_obligationModel.oblAsignGroupName('');
			$("#input_oblAsignRole").removeClass("cc-error");
			$("#input_oblAsignRole").next().removeClass("cc-error");
			HideUserSelectionDialog();
		}
		else if (l_rolesandGroupsList_model.selectedGroupItemID()) {
			l_obligationModel.oblAsignGroupId(l_rolesandGroupsList_model.selectedGroupItemID());
			l_obligationModel.oblAsignGroupName(l_rolesandGroupsList_model.selectedGroupName());
			l_obligationModel.oblAssigneeId('');
			l_obligationModel.oblAssigneeName('');
			l_obligationModel.oblAsignRoleId('');
			l_obligationModel.oblAsignRoleName('');
			$("#input_oblAsignRole").removeClass("cc-error");
			$("#input_oblAsignRole").next().removeClass("cc-error");
			HideUserSelectionDialog();
		}
		else if (l_usersList_model.selectedUserID()) {
			l_obligationModel.oblAssigneeId(l_usersList_model.selectedUserID());
			l_obligationModel.oblAssigneeName(l_usersList_model.selectedUserName());
			l_obligationModel.oblAsignRoleId('');
			l_obligationModel.oblAsignRoleName('');
			l_obligationModel.oblAsignGroupId('');
			l_obligationModel.oblAsignGroupName('');
			$("#input_oblAssignee").removeClass("cc-error");
			$("#input_oblAssignee").next().removeClass("cc-error");
			HideUserSelectionDialog();
		}

	});
}

function openFirstEscalationSelection() {
if (l_obligationModel.stage() > 1 || l_obligationModel.Status()=="INVALID" || l_obligationModel.Status()=="INACTIVE" || l_obligationModel.Status()=="INACTIVEINPROGRESS" || l_obligationModel.Status()=="INVALIDINPROGRESS") {
		return;
	}
	l_dialogFor = "FE";
	showUserSelectionDialog();
	$("#btn_userSelectionYes").attr("disabled", true);
	$('button#btn_userSelectionYes').off("click");
	$('button#btn_userSelectionYes').on('click', function (_event) {
		if (l_rolesandGroupsList_model.selectedRoleItemID()) {
			l_obligationModel.oblFERoleId(l_rolesandGroupsList_model.selectedRoleItemID());
			l_obligationModel.oblFERoleName(l_rolesandGroupsList_model.selectedRoleName());
			l_obligationModel.oblFEId('');
			l_obligationModel.oblFEName('');
			l_obligationModel.oblFEGroupId('');
			l_obligationModel.oblFEGroupName('');
			HideUserSelectionDialog();
		}
		else if (l_rolesandGroupsList_model.selectedGroupItemID()) {
			l_obligationModel.oblFEGroupId(l_rolesandGroupsList_model.selectedGroupItemID());
			l_obligationModel.oblFEGroupName(l_rolesandGroupsList_model.selectedGroupName());
			l_obligationModel.oblFEId('');
			l_obligationModel.oblFEName('');
			l_obligationModel.oblFERoleId('');
			l_obligationModel.oblFERoleName('');
			HideUserSelectionDialog();
		}
		else if (l_usersList_model.selectedUserID()) {
			l_obligationModel.oblFEId(l_usersList_model.selectedUserID());
			l_obligationModel.oblFEName(l_usersList_model.selectedUserName());
			l_obligationModel.oblFERoleId('');
			l_obligationModel.oblFERoleName('');
			l_obligationModel.oblFEGroupId('');
			l_obligationModel.oblFEGroupName('');
			HideUserSelectionDialog();
		}
	});
}

function openSecondEscalationSelection() {
	if (l_obligationModel.stage() > 2 || l_obligationModel.Status()=="INVALID" || l_obligationModel.Status()=="INACTIVE" || l_obligationModel.Status()=="INACTIVEINPROGRESS" || l_obligationModel.Status()=="INVALIDINPROGRESS") {
		return;
	}
	l_dialogFor = "SE";
	showUserSelectionDialog();
	$("#btn_userSelectionYes").attr("disabled", true);
	$('button#btn_userSelectionYes').off("click");
	$('button#btn_userSelectionYes').on('click', function (_event) {
		if (l_rolesandGroupsList_model.selectedRoleItemID()) {
			l_obligationModel.oblSERoleId(l_rolesandGroupsList_model.selectedRoleItemID());
			l_obligationModel.oblSERoleName(l_rolesandGroupsList_model.selectedRoleName());
			l_obligationModel.oblSEId('');
			l_obligationModel.oblSEName('');
			l_obligationModel.oblSEGroupId('');
			l_obligationModel.oblSEGroupName('');
			HideUserSelectionDialog();
		}
		else if (l_rolesandGroupsList_model.selectedGroupItemID()) {
			l_obligationModel.oblSEGroupId(l_rolesandGroupsList_model.selectedGroupItemID());
			l_obligationModel.oblSEGroupName(l_rolesandGroupsList_model.selectedGroupName());
			l_obligationModel.oblSEId('');
			l_obligationModel.oblSEName('');
			l_obligationModel.oblSERoleId('');
			l_obligationModel.oblSERoleName('');
			HideUserSelectionDialog();
		}
		else if (l_usersList_model.selectedUserID()) {
			l_obligationModel.oblSEId(l_usersList_model.selectedUserID());
			l_obligationModel.oblSEName(l_usersList_model.selectedUserName());
			l_obligationModel.oblSERoleId('');
			l_obligationModel.oblSERoleName('');
			l_obligationModel.oblSEGroupId('');
			l_obligationModel.oblSEGroupName('');
			HideUserSelectionDialog();
		}
	});
}
function HideUserSelectionDialog() {
	$("#div_selectUser").modal('hide');
	l_usersList_model.selectedUserID('');
	l_usersList_model.selectedUserName('');
	l_rolesandGroupsList_model.selectedRoleItemID('');
	l_rolesandGroupsList_model.selectedRoleName('');
	l_rolesandGroupsList_model.selectedGroupItemID('');
	l_rolesandGroupsList_model.selectedGroupName('');
	$('#usersListTable').find('.cc-radio-on').removeClass("cc-radio-on");
	$('#roleListTable').find('.cc-radio-on').removeClass("cc-radio-on");
}

function showUserSelectionDialog() {
	$("#div_selectUser").modal();
	$("#selectTabs").tabs();
	//Clear the existing filter.
	if ($('#input_userListSearchFilter')) {
		$('#input_userListSearchFilter').val('')
	}
	if ($('#input_roleListSearchFilter')) {
		$('#input_roleListSearchFilter').val('')
	}
	if ($('#input_groupListSearchFilter')) {
		$('#input_groupListSearchFilter').val('')
	}
	// List all organization users.
	ListOrganizationUsers();
	ListAllRoles();
	ListAllGroups();
}

function ClearFirstEscalation() {
	if (l_obligationModel.stage() > 1 || l_obligationModel.Status()=="INVALID" || l_obligationModel.Status()=="INACTIVE" || l_obligationModel.Status()=="INACTIVEINPROGRESS" || l_obligationModel.Status()=="INVALIDINPROGRESS") {
		return;
	}
	l_obligationModel.oblFEId('');
	l_obligationModel.oblFEName('');
	l_obligationModel.oblFERoleId('');
	l_obligationModel.oblFERoleName('');
	l_obligationModel.oblFEGroupId('');
	l_obligationModel.oblFEGroupName('');
}

function ClearSecondEscalation() {
	if (l_obligationModel.stage() > 2 || l_obligationModel.Status()=="INVALID" || l_obligationModel.Status()=="INACTIVE" || l_obligationModel.Status()=="INACTIVEINPROGRESS" || l_obligationModel.Status()=="INVALIDINPROGRESS") {
		return;
	}
	l_obligationModel.oblSEId('');
	l_obligationModel.oblSEName('');
	l_obligationModel.oblSERoleId('');
	l_obligationModel.oblSERoleName('');
	l_obligationModel.oblSEGroupId('');
	l_obligationModel.oblSEGroupName('');
}

function openObligationDetails(iEvent, iEventObject) {
	$("#obligationsDetails_Dialog").modal({
        backdrop: 'static',
        keyboard: false
    });
	$('#btn_createorUpdateObligation span').text("Update")
}

function openOBSelection(iEvent, iEventObject) {
	if (l_obligationModel.Status()=="INVALID" || l_obligationModel.Status()=="INACTIVE" || l_obligationModel.Status()=="INACTIVEINPROGRESS" || l_obligationModel.Status()=="INVALIDINPROGRESS") {
		return;
	}
	$("#modal_selectOB").modal();
	if ($('#input_OBLLIBSearch')) {
		$('#input_OBLLIBSearch').val('')
	}
	ListObligationsLibrary();
	$("#btn_OBSelectionYes").attr("disabled", true);
	$('button#btn_OBSelectionYes').off("click");
	$('button#btn_OBSelectionYes').on('click', function (_event) {
		if (l_oblLibrary_model.selectedOBItemID()) {
			l_obligationModel.oblName(l_oblLibrary_model.selectedOBName());
			l_obligationModel.oblDescription(l_oblLibrary_model.selectedOBDesc());
			l_obligationModel.oblActionTobeTaken(l_oblLibrary_model.selectedOBAction());
			l_obligationModel.oblReminderTitle(l_oblLibrary_model.selectedOBTitle());
			$("#input_oblName").removeClass("cc-error");
			$("#input_oblName").next().removeClass("cc-error");
			$("#input_oblRemTitle").removeClass("cc-error");
			closeOBSelection();
		}

	});

}
function closeOBSelection(data, event) {
	$("#modal_selectOB").modal('hide');
	l_oblLibrary_model.selectedOBName('');
	l_oblLibrary_model.selectedOBDesc('');
	l_oblLibrary_model.selectedOBAction('');
	l_oblLibrary_model.selectedOBTitle('');
}

function closeUserSelection(data, event) {
	$("#div_selectUser").modal('hide');
}
function clearUserSelect() {
	l_usersList_model.selectedUserID('');
	l_usersList_model.selectedUserName('');
	$('#usersListTable').find('.cc-radio-on').removeClass("cc-radio-on");
}
function clearRoleSelect() {
	l_rolesandGroupsList_model.selectedRoleItemID('');
	l_rolesandGroupsList_model.selectedRoleName('');
	$('#roleListTable').find('.cc-radio-on').removeClass("cc-radio-on");
}
function openCreateForm() {
	l_oblObj = {};
	$("#obligationsDetails_Dialog").modal({
        backdrop: 'static',
        keyboard: false
    });
	$('#btn_createorUpdateObligation span').text("Create")

}

function openNonComplSel(iEvent, iEventObject) {
	/*if (l_obligationModel.stage() > 0 || l_obligationModel.Status()=="INVALID" || l_obligationModel.Status()=="INACTIVE" || l_obligationModel.Status()=="INACTIVEINPROGRESS" || l_obligationModel.Status()=="INVALIDINPROGRESS") {
		return;
	}*/

	if (!l_oblNonComplianceListModal) {
		l_oblNonComplianceListModal = new OBLNonComplianceListModel();
		ko.applyBindings(l_oblNonComplianceListModal, document.getElementById("modal_selectNonCompl"));
	}

	$("#modal_selectNonCompl").modal({
		backdrop: 'static',
		keyboard: false
	});
	if ($('#input_NonComplSearch')) {
		$('#input_NonComplSearch').val('')
	}
	ListNonCompliances();
	$("#btn_ncYes").attr("disabled", true);
	$('button#btn_ncYes').off("click");
	$('button#btn_ncYes').on('click', function (_event) {
		if (l_oblNonComplianceListModal.selectedNCItemID()) {
			l_oblSetComplianceModel.oblnonCompliance(l_oblNonComplianceListModal.selectedNCName());
			l_oblSetComplianceModel.oblnonComplianceItem(l_oblNonComplianceListModal.selectedNCItemID());
			closeNonComplSel();
		}

	});

}

function closeNonComplSel(data, event) {
	$("#modal_selectNonCompl").modal('hide');
	l_oblNonComplianceListModal.selectedNCName('');
	l_oblNonComplianceListModal.selectedNCItemID('');
	l_oblNonComplianceListModal.selectedNCDesc('');
}

function clearNonComplSel() {
	l_oblSetComplianceModel.oblnonCompliance('');
	l_oblSetComplianceModel.oblnonComplianceItem('');
}

function onSetComplConfirm() {
	if(l_oblSetComplianceModel.complianceStatus() === undefined){
		l_oblSetComplianceModel.hasErrors(true);
		return ;
	}
	if(l_oblSetComplianceModel.complianceStatus() === 'Met' && l_oblSetComplianceModel.metType()===undefined){
		l_oblSetComplianceModel.metTypeError(true);
		return ;
	}
	if(l_oblSetComplianceModel.complianceStatus() && Object.keys(selectedOBRow).length == 1){

		let complianceInp = {
			'Obligations-id':{
				'ItemId1': Object.keys(selectedOBRow)[0]
			},
			'OBLComplianceData-create' :{
				'ComplianceStatus': l_oblSetComplianceModel.complianceStatus(),
				'UpdatedTime':moment.utc().format(),
				'Comments':l_oblSetComplianceModel.complComments(),
				'UpdateType':'Manual',
				'ProcessStatus' :  l_oblSetComplianceModel.complianceStatus()==='Met'?l_oblSetComplianceModel.metType():'Completed'
			}
		}
		if(!l_oblSetComplianceModel.oblnonComplianceItem()) {
			complianceInp["RelatedNonCompliance"] = { '@nil': 'true' };
		}else {
			complianceInp["OBLComplianceData-create"].RelatedNonCompliance = {
				'OBLNonCompliance-id':{
					'Id':l_oblSetComplianceModel.oblnonComplianceItem()
				}
			}
		}
		
		cc_obligaion_services.createOBLComplianceService(complianceInp, function (response_data, status) {
			if (status !== "ERROR") {
				//do nothing
			}
		});
		closeSetComplConfirm();
	}
}

function closeSetComplConfirm() {
	l_oblSetComplianceModel.complianceStatus('');
	l_oblSetComplianceModel.metType('');
	l_oblSetComplianceModel.oblnonCompliance('');
	l_oblSetComplianceModel.oblnonComplianceItem('');
	l_oblSetComplianceModel.complComments('')
	$("#div_setOBLComplianceDialog").modal('hide');
}

function closeDeletionConfirmation(data, event) {
	$("#div_deleteDialog").modal('hide');
}
function onDeletionConfirmed() {
	if (l_oblObj && l_oblObj.obligationId1) {
		deleteObligationObject(l_oblObj, true);
	}
}

function callDeactivateObligation() {
	if (Object.keys(selectedOBRow).length == 1) {
		let oblStatus = selectedOBRow[Object.keys(selectedOBRow)[0]].Status();
		if (oblStatus === 'INACTIVE' || oblStatus === 'INVALID' || oblStatus === "INACTIVEINPROGRESS" || oblStatus ==="INVALIDINPROGRESS") {
			return;
		}
		l_oblObj = selectedOBRow[Object.keys(selectedOBRow)[0]];
		$("#obligationInActive_Dialog").modal();
    }
};

function onInActivateConfirmed(data) {
	cc_obligaion_services.inActivateObligation({ ObligationItemId1: l_oblObj.obligationItemId1() }, () => {
		l_ObligationListViewModel.clearFilter();
		clearselectedOBRow();
		setTimeout(() => { onInActivateCancel(); }, 300);
	});
};

function callInvalidateObligation() {
	if (Object.keys(selectedOBRow).length == 1) {
		let oblStatus = selectedOBRow[Object.keys(selectedOBRow)[0]].Status();
		if (oblStatus === 'INACTIVE' || oblStatus === 'INVALID' || oblStatus === "INACTIVEINPROGRESS" || oblStatus ==="INVALIDINPROGRESS") {
			return;
		}
		l_oblObj = selectedOBRow[Object.keys(selectedOBRow)[0]];
		$("#obligationInvalid_Dialog").modal();
    }
};

function onInvalidateConfirmed(data) {
	cc_obligaion_services.invalidateObligation({ ObligationItemId1: l_oblObj.obligationItemId1() }, () => {
		l_ObligationListViewModel.clearFilter();
		clearselectedOBRow();
		setTimeout(() => { onInvalidateCancel(); }, 300);
	});
};

function clearselectedOBRow() {
	selectedOBRow = {};
	$("#btn_deactivateListActionBar").css("display", "none");
	$("#btn_invalidateListActionBar").css("display", "none");
    $("#id_deleteListActionBar").css("display", "none");
};

function onInActivateCancel() {
	l_oblObj = {};
	$("#obligationInActive_Dialog").modal('hide');
};

function onInvalidateCancel() {
	l_oblObj = {};
	$("#obligationInvalid_Dialog").modal('hide');
};

function getDeletionInput(iOblObject) {
	var l_paramInput = '<Contract-id xmlns="http://schemas/OpenTextContractCenter/Contract"><Id>' + contractId + '</Id></Contract-id>' + '<Obligations xmlns="http://schemas/OpenTextContractCenter/Contract.Obligations"><Obligations-id ><Id>' + contractId + '</Id><Id1>' + iOblObject.obligationId1() + '</Id1></Obligations-id></Obligations>';
	return l_paramInput;
}
function deleteObligationObject(iOblObject, iClosureConfirmation) {
	$.cordys.ajax({
		namespace: "http://schemas/OpenTextContractCenter/Contract/operations",
		method: "DeleteObligations",
		parameters: getDeletionInput(iOblObject),
		success: function (data) {
			if (iClosureConfirmation) {
				successToast(3000, getTranslationMessage("Obligation deleted."));
				l_ObligationListViewModel.clearFilter();
				closeDeletionConfirmation();
			}
		},
		error: function (responseFailure) {

			if (iClosureConfirmation) {
				showOrHideErrorInfo("div_formErrorInfoArea", true, getTranslationMessage("An error occured while deleting the obligation. Contact the administrator."), 10000);
				return false;
			}
		}
	});
}

// Verifying the user role.
function checkForUserRole(callBackFunc) {
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
						"Obligation Creator"
					]
				}
			},
		}).done(function (data) {
			callBackFunc(data);
		}).fail(function (error) {
		})
}

// Verifying the user role.
function checkForUserRole2(callBackFunc) {
	$.cordys.ajax(
		{
			method: "CheckCurrentUserInRoles",
			namespace: "http://schemas.opentext.com/apps/cc/configworkflow/20.2",
			parameters:
			{
				"Roles": {
					"Role": [
						"Contract Administrator",
						"Contract Manager"
					]
				}
			},
		}).done(function (data) {
			callBackFunc(data);
		}).fail(function (error) {
		})
}