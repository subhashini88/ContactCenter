$.cordys.json.defaults.removeNamespacePrefix = true;
var oblContractId;
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
                return false;
            }
        });
    };

    self.getLifecycleTask = function (request, responseCallback) {
        $.cordys.ajax({
            method: "ReadLifecycleTask",
            namespace: "http://schemas/OpenTextCCConfigurableWorkflow/GCActivityInstances.LifecycleTask/operations",
            parameters: request,
            success: function (responseSuccess) {
                responseCallback(responseSuccess);
            },
            error: function (responseFailure) {
                responseCallback(responseFailure, "ERROR");
                return false;
            }
        });
    };
    return self;
})();

// Services end------------------------------------------------

function ObligationViewModel() {
    var self = this;
    var obligationItemId = getUrlParameterValue("obligationId", window.parent.location.search, true);
    var l_taskId = getUrlParameterValue("taskId", window.parent.location.search, true);
    var l_contractItemId = getUrlParameterValue("ctrItem", window.parent.location.search, true);
    oblContractId = getoblContractId(obligationItemId);

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
    self.Z_oblRecurType = ko.observable('');
	self.oblRecurStartDate = ko.observable('');
	self.oblRecurStartDateLocale = ko.observable('');
	self.oblRecurMonthFreq = ko.observable('');
	self.oblRecurMonthDay = ko.observable('');
	self.oblRecurWeekFreq = ko.observable('');
	self.oblRecurWeekDay = ko.observable('');
	self.oblRecurDayFreq = ko.observable('');
	self.oblRecurEndType = ko.observable('');
    self.Z_oblRecurEndType = ko.observable('');
	self.oblRecurEndinOccurences = ko.observable('');
	self.oblRecurEndDate = ko.observable('');
	self.oblRecurEndDateLocale = ko.observable('');

    self.comments = ko.observable();

    self.ctrLink = "../../../../../app/start/web/#/item/" + l_contractItemId + "/F8B156D635F3A1E89CB08DDB9883E4C8/";
    self.genCCId = ko.observable();

    function getoblContractId(obligationItemId) {
        var idArray;
        if (obligationItemId.length > 2) {
            idArray = obligationItemId.split('.');
            if (Array.isArray(idArray)) {
                return idArray;
            }
        }
    };
	
	self.openContractLink = function() {
        var l_ItemId =l_contractItemId;
		var l_Id = l_contractItemId.split(".")[1];
		navigateToInstance(l_ItemId,"Contract",{"layoutID":'F8B156D635F3A1E89CB08DDB9883E4C8', "clearBreadcrumb":false,"breadcrumbName" : ''})
    }
		
    self.readObligation = function (params) {
        cc_obligaion_services.getObligationsListService(params, function (response_data, status) {
            if (status !== "ERROR") {
                _populateDatatable(response_data);
            }
        });
    };

    self.readLifecycleTask = function (params) {
        cc_obligaion_services.getLifecycleTask(params, function (response_data, status) {
            if (status !== "ERROR") {
                if (response_data && response_data.LifecycleTask) {
                    self.comments(response_data.LifecycleTask.Comments);
                }
            }
        });
    };

    function _populateDatatable(response_data) {
        if (response_data.Response.FindContractObligationViewListResponse && response_data.Response.FindContractObligationViewListResponse.Obligations) {
            var obligationsData = response_data.Response.FindContractObligationViewListResponse.Obligations;
            _populateData(obligationsData);
        }
    };

    function _populateData(response_data) {

		self.genCCId(_getTextValue(response_data.Owner.GeneratedContractId));
		self.oblGenID(_getTextValue(response_data.GeneratedObliID));
		self.oblName(_getTextValue(response_data.Name));
		self.oblActionTobeTaken(_getTextValue(response_data.ActionToBeTaken));
		self.oblReminderTitle(_getTextValue(response_data.ReminderTitle));
		self.oblDuedateLocale(_getTextValue(response_data.DueDate) ? formateDatetoLocale(_getTextValue(response_data.DueDate)) : '');
		self.oblDuedate(_getTextValue(response_data.DueDate));
		self.oblDescription(_getTextValue(response_data.Description));
		self.oblReminderDateLocale(_getTextValue(response_data.ReminderSendDate) ? formateDatetoLocale(_getTextValue(response_data.ReminderSendDate)) : '');
		self.oblReminderDate(_getTextValue(response_data.ReminderSendDate));
		self.oblAssigneeName('');
		self.oblAssigneeId('');
		self.oblAsignRoleName('');
		self.oblAsignRoleId('');
		self.oblAsignGroupName('');
		self.oblAsignGroupId('');
		if (response_data.AssignedTo && response_data.AssignedTo["Identity-id"]) {
			self.oblAssigneeName(_getTextValue(response_data.AssignedTo.Name));
			self.oblAssigneeId(getTextAfter(_getTextValue(response_data.AssignedTo["Identity-id"].ItemId), "."));
		} else if (response_data.RemAssignedRole && response_data.RemAssignedRole["Identity-id"]) {
			self.oblAsignRoleName(_getTextValue(response_data.RemAssignedRole.Name));
			self.oblAsignRoleId(getTextAfter(_getTextValue(response_data.RemAssignedRole["Identity-id"].ItemId), "."));
		} else if (response_data.RemAssignedGroup && response_data.RemAssignedGroup["Identity-id"]) {
			self.oblAsignGroupName(_getTextValue(response_data.RemAssignedGroup.Name));
			self.oblAsignGroupId(getTextAfter(_getTextValue(response_data.RemAssignedGroup["Identity-id"].ItemId), "."));
		}
		self.oblFEName('');
		self.oblFEId('');
		self.oblFERoleName('');
		self.oblFERoleId('');
		self.oblFEGroupName('');
		self.oblFEGroupId('');
		if (response_data.FirstEscalationContact && response_data.FirstEscalationContact["Identity-id"]) {
			self.oblFEName(_getTextValue(response_data.FirstEscalationContact.Name));
			self.oblFEId(getTextAfter(_getTextValue(response_data.FirstEscalationContact["Identity-id"].ItemId), "."));
		} else if (response_data.FESCAssignedRole && response_data.FESCAssignedRole["Identity-id"]) {

			self.oblFERoleName(_getTextValue(response_data.FESCAssignedRole.Name));
			self.oblFERoleId(getTextAfter(_getTextValue(response_data.FESCAssignedRole["Identity-id"].ItemId), "."));
		} else if (response_data.FESCAssignedGroup && response_data.FESCAssignedGroup["Identity-id"]) {
			self.oblFEGroupName(_getTextValue(response_data.FESCAssignedGroup.Name));
			self.oblFEGroupId(getTextAfter(_getTextValue(response_data.FESCAssignedGroup["Identity-id"].ItemId), "."));
		}
		self.oblSEName('');
		self.oblSEId('');
		self.oblSERoleName('');
		self.oblSERoleId('');
		self.oblSEGroupName('');
		self.oblSEGroupId('');
		if (response_data.SecondEscalationContact && response_data.SecondEscalationContact["Identity-id"]) {
			self.oblSEName(_getTextValue(response_data.SecondEscalationContact.Name));
			self.oblSEId(getTextAfter(_getTextValue(response_data.SecondEscalationContact["Identity-id"].ItemId), "."));
		} else if (response_data.SESCAssignedRole && response_data.SESCAssignedRole["Identity-id"]) {
			self.oblSERoleName(_getTextValue(response_data.SESCAssignedRole.Name));
			self.oblSERoleId(getTextAfter(_getTextValue(response_data.SESCAssignedRole["Identity-id"].ItemId), "."));
		} else if (response_data.SESCAssignedGroup && response_data.SESCAssignedGroup["Identity-id"]) {
			self.oblSEGroupName(_getTextValue(response_data.SESCAssignedGroup.Name));
			self.oblSEGroupId(getTextAfter(_getTextValue(response_data.SESCAssignedGroup["Identity-id"].ItemId), "."));
		}
		self.oblAssigneeSLA(_getTextValue(response_data.AssigneeEscDuration));
		self.oblFESLA(_getTextValue(response_data.FESCDuration));
		self.oblSESLA(_getTextValue(response_data.SESCDuration));

        self.Z_oblRecurType(_getTextValue(response_data.RecurrenceType));
		self.oblRecurType(_getTextValue(response_data.RecurrenceType));
		self.oblRecurStartDateLocale(_getTextValue(response_data.RecurrenceStart) ? formateDatetoLocale(_getTextValue(response_data.RecurrenceStart)) : '');
		self.oblRecurStartDate(_getTextValue(response_data.RecurrenceStart));
		self.oblRecurMonthDay(_getTextValue(response_data.RecurDayOfMonth));
		self.oblRecurWeekDay(_getTextValue(response_data.RecurDaysOfWeek));
		if (self.Z_oblRecurType() == "REPEATMONTHLY") {
            self.oblRecurType("Repeat monthly");
			self.oblRecurMonthFreq(_getTextValue(response_data.RecurFrequency));
			self.oblRecurWeekFreq('');
			self.oblRecurDayFreq('');
		}else if (self.Z_oblRecurType() == "REPEATWEEKLY") {
            self.oblRecurType("Repeat weekly");
			self.oblRecurMonthFreq('');
			self.oblRecurWeekFreq(_getTextValue(response_data.RecurFrequency));
			self.oblRecurDayFreq('');

            switch (_getTextValue(response_data.RecurDaysOfWeek)) {
                case 1:
                    self.oblRecurWeekDay("Monday");
                    break;
                case 2:
                    self.oblRecurWeekDay("Tuesday");
                    break;
                case 3:
                    self.oblRecurWeekDay("Wednesday");
                    break;
                case 4:
                    self.oblRecurWeekDay("Thursday");
                    break;
                case 5:
                    self.oblRecurWeekDay("Friday");
                    break;
                case 6:
                    self.oblRecurWeekDay("Saturday");
                    break;
                case 7:
                    self.oblRecurWeekDay("Sunday");
                    break;
            }
		}else if (self.Z_oblRecurType() == "REPEATDAILY") {
            self.oblRecurType("Repeat daily");
			self.oblRecurMonthFreq('');
			self.oblRecurWeekFreq('');
			self.oblRecurDayFreq(_getTextValue(response_data.RecurFrequency));
		}else if (self.Z_oblRecurType() == "DONOTREPEAT") {
            self.oblRecurType("Do not repeat");
		}
		self.Z_oblRecurEndType(_getTextValue(response_data.RecurrenceEndType));
        if(self.Z_oblRecurEndType() == "AFTEROCCUR"){
            self.oblRecurEndType("After");
        }else if(self.Z_oblRecurEndType() == "ONTHISDATE"){
            self.oblRecurEndType("On this date");
        }
		self.oblRecurEndinOccurences(_getTextValue(response_data.RecurEndInOccur));
		self.oblRecurEndDateLocale(_getTextValue(response_data.RecurrenceEnd) ? formateDatetoLocale(_getTextValue(response_data.RecurrenceEnd)) : '');
		self.oblRecurEndDate(_getTextValue(response_data.RecurrenceEnd));
    };

    (function init() {
        self.readObligation({ "contractID": oblContractId[1], "obligationID": oblContractId[2] });
        self.readLifecycleTask({
            'LifecycleTask-id':
            {
                'ItemId1': l_taskId
            }
        });
    })();
}



function _getTextValue(obj) {
    return obj && obj.text ? obj.text : obj;
}

function getTextAfter(iText, imatchString) {
	if (iText && imatchString) {
		return iText.substring(iText.indexOf(imatchString) + 1);
	}
	else {
		return iText;
	}
}

var l_ObligationViewModel = new ObligationViewModel();

$(document).ready(function () {
	var i_locale = getlocale();
    var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
    translateLabels("com/opentext/apps/contractcenter/ContractCenter/ContractCenter", i_locale);
    loadRTLIfRequired(i_locale, rtl_css);
    if (window.parent.parent) {
        obligationsTaskFrame = $('[src*="obligationTaskScreen.htm"]', window.parent.document);
        if (obligationsTaskFrame) {
            obligationsTaskFrame.css('border', 'none');
        }
    }
    ko.applyBindings(l_ObligationViewModel, document.getElementById("obltask_container"));
});