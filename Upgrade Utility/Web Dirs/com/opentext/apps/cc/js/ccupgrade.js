$.cordys.json.defaults.removeNamespacePrefix = true;
var cInstanceId, defaultContainingSectionId, relatedTemplateId, jobRelatedStatus, i_locale;


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
const defaultPerPage = 25;
const listPerPageArr = [
    { key: 0, val: "Show all" },
    { key: 100, val: "100 per page" },
    { key: 50, val: "50 per page" },
    { key: 25, val: "25 per page" }];
const cc_versions = ["", "19.2", "19.4", "20.4", "22.2", "22.3", "23.1","23.2","23.3","23.4","24.1"];

var cc_jobs_services = (function () {
    var self = {};
    self.getjobsListDefaultService = function (params, responseCallback) {
        $.cordys.ajax({
            method: "getAllUpgradeJobs",
            namespace: "http://schemas/OpentextUpgradeUtility/UpgradeJob/operations",
            parameters: params,
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
    self.getjobsListService = function (params, responseCallback) {
        $.cordys.ajax({
            method: "getUpgradeJobsFilteredList",
            namespace: "http://schemas.opentext.com/UpgradeUtils/22.3",
            parameters: params,
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

    self.updateMainJobStatus = function (params, responseCallback) {
        $.cordys.ajax({
            method: "checkAndUpdateMainJobStatus",
            namespace: "http://schemas.opentext.com/UpgradeUtils/22.3",
            parameters: params,
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

    self.ccUpgradeService = function (params, responseCallback) {
        $.cordys.ajax({
            method: "MainUpgradeJob",
            namespace: "http://schemas.opentext.com/UpgradeUtils/22.3",
            parameters: params,
            success: function (responseSuccess) {
                responseCallback(responseSuccess);
            },
            error: function (responseFailure) {
                responseCallback(responseFailure, "ERROR");
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while upgrading contract center. Contact the administrator."), 10000);
                return false;
            }
        });
    };
    self.ccUpgradeBatchListService = function (params, responseCallback) {
        $.cordys.ajax({
            method: "getUpgradeBatchJobsWithParent",
            namespace: "http://schemas/OpentextUpgradeUtility/UpgradeBatchJob/operations",
            parameters: params,
            success: function (responseSuccess) {
                responseCallback(responseSuccess);
            },
            error: function (responseFailure) {
                responseCallback(responseFailure, "ERROR");
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while upgrading contract center. Contact the administrator."), 10000);
                return false;
            }
        });
    };
    self.ccGetCCToVersionService = function (params, responseCallback) {
        $.cordys.ajax({
            method: "GetCCToVersion",
            namespace: "http://schemas.opentext.com/UpgradeUtils/22.3",
            parameters: params,
            success: function (responseSuccess) {
                responseCallback(responseSuccess);
            },
            error: function (responseFailure) {
                responseCallback(responseFailure, "ERROR");
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while upgrading contract center. Contact the administrator."), 10000);
                return false;
            }
        });
    };
    self.ccGetCCFromVersionService = function (params, responseCallback) {
        $.cordys.ajax({
            method: "getCCCurrentFromVersion",
            namespace: "http://schemas/OpentextUpgradeUtility/UpgradeJob/operations",
            parameters: {
                "Cursor": {
                    '@xmlns': 'http://schemas.opentext.com/bps/entity/core',
                    '@offset': 0,
                    '@limit': 1
                }
            },
            success: function (responseSuccess) {
                responseCallback(responseSuccess);
            },
            error: function (responseFailure) {
                responseCallback(responseFailure, "ERROR");
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while upgrading contract center. Contact the administrator."), 10000);
                return false;
            }
        });
    };
    self.ContractUpgradeRetriggerService = function (params, responseCallback) {
        $.cordys.ajax({
            method: "ContractUpgrade",
            namespace: "http://schemas.opentext.com/UpgradeUtils/22.3",
            parameters: params,
            success: function (responseSuccess) {
                responseCallback(responseSuccess);
            },
            error: function (responseFailure) {
                responseCallback(responseFailure, "ERROR");
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while upgrading contract center. Contact the administrator."), 10000);
                return false;
            }
        });
    };
    self.MainUpgradeRetriggerService = function (params, responseCallback) {
        $.cordys.ajax({
            method: "MainUpgradeJob",
            namespace: "http://schemas.opentext.com/UpgradeUtils/22.3",
            parameters: params,
            success: function (responseSuccess) {
                responseCallback(responseSuccess);
            },
            error: function (responseFailure) {
                responseCallback(responseFailure, "ERROR");
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while upgrading contract center. Contact the administrator."), 10000);
                return false;
            }
        });
    };

    return self;
})();


function CreateJobsModel() {
    var self = this;
    self.fromVersionSelect = ko.observableArray(cc_versions);
    self.fromVersion = ko.observable();
    self.toVersion = ko.observable();
    self.selectDisabled = ko.observable(true);
    self.createUpgradeJob = function () {
        console.log("createUpgradeJob");
    };
    self.cancelUpgradeJob = function () {
        console.log("cancelUpgradeJob");
    };
}

function JobsListViewModel() {
    var self = this;
    self.jobsList = ko.observableArray();
    self.selectedId = ko.observable();
    self.selectedVersion = ko.observable();
    self.clauseContainerList = ko.observableArray();
    self.ccversionlist = ko.observableArray(cc_versions);

    //Pagination START
    self.totalListsCount = ko.observable(0);
    self.currentPage = ko.observable(1);
    self.hideDecrement = ko.observable(false);
    self.hideIncrement = ko.observable(false);

    //filter start
    self.filterUpgradeType = ko.observable(defaultFilterVal);
    self.filterUpgradeTypeInput = ko.observable("");
    self.filterUpgradeTypeExpand = ko.observable(true);

    self.filterFromVersion = ko.observable(defaultFilterVal);
    self.filterFromVersionInput = ko.observable("");
    self.filterFromVersionExpand = ko.observable(true);

    self.filterToVersion = ko.observable(defaultFilterVal);
    self.filterToVersionInput = ko.observable("");
    self.filterToVersionExpand = ko.observable(true);

    self.filterExpand = ko.observable(false);
    self.filterOptions = ko.observableArray(filterList);
    //filter end

    // self.isFromVersionUpdated = ko.observable(false);
    // self.isToVersionUpdated = ko.observable(false);

    self.selectVersion = function (data, event) {
        self.selectedVersion(data);
    };
    self.populateBatchJobList = function (data, event) {
        self.selectedId(data.Id);
    };
    self.refreshTable = function (params) {
        self.updateFromAndToVersion();
        var offset = (self.currentPage() - 1) * defaultPerPage;
        self.fetchjobsList(params, offset, defaultPerPage);
    };

    function populateJobListRequest(input) {
        var request = {};
        if (input && input.applyFilter) {
            request["jobType"] = input.filterUpgradeType;
            request["fromVersion"] = input.filterFromVersion;
            request["toVersion"] = input.filterToVersion;
        }
        return request;
    }
    self.openUpgradeJobCreateForm = function (data, event) {
        // hideOptions(event);
        // self.subOrgCreateOrgModel(new CreateSubOrgModel(self.currentOrgDisplay, false, self));
        $("#create_batchjob_modal").modal({
            backdrop: 'static',
            keyboard: false
        });
        event.stopPropagation();
        translatePage();
    }

    self.updateFromAndToVersion = function () {
        cc_jobs_services.ccGetCCToVersionService(null, function (response_data1, status1) {
            if (response_data1 && response_data1.tuple) {
                _populateCreateSelectToVersion(response_data1.tuple.old.getCCToVersion.getCCToVersion);
            }
        });
        cc_jobs_services.ccGetCCFromVersionService(null, function (response_data1, status1) {
            if (response_data1 && response_data1.UpgradeJob) {
                _populateCreateSelectFromVersion(response_data1.UpgradeJob.ToVersion);
            }
        });
    }


    self.fetchjobsList = function (params, offset, limit) {
        var req = populateJobListRequest(params);
        req.offset = offset;
        req.limit = limit;
        cc_jobs_services.updateMainJobStatus(req, function (response_status, status_main) {
            cc_jobs_services.getjobsListService(req, function (response_data, status) {
                if (status !== "ERROR") {
                    _populateDatatable(response_data);
                    var jobsData = response_data.Response.FindZ_INT_UpgradeJobListResponse.UpgradeJob;
                    // cc_jobs_services.ccGetCCToVersionService(null, function (response_data1, status1) {
                    //     if (response_data1 && response_data1.tuple) {
                    //         _populateCreateSelectToVersion(response_data1.tuple.old.getCCToVersion.getCCToVersion);
                    //     }
                    // });
                    if (jobsData) {
                        // $("#select_upgrade").hide();
                        l_FormCreateJobsModel.selectDisabled(true);
                        Array.isArray(jobsData) ? self.totalListsCount(response_data.Response.FindZ_INT_UpgradeJobListResponse['@total']) : self.totalListsCount(1);
                    } else {
                        // $("#select_upgrade").show();
                        l_FormCreateJobsModel.selectDisabled(false);
                        self.totalListsCount(0);
                    }
                    self.currentPage(offset / defaultPerPage + 1);
                }
            });
        });
    };

    function _populateDatatable(response_data) {
        if (response_data.Response.FindZ_INT_UpgradeJobListResponse.UpgradeJob) {
            var jobsData = response_data.Response.FindZ_INT_UpgradeJobListResponse.UpgradeJob;
            self.jobsList.removeAll();
            if (Array.isArray(jobsData)) {
                // _populateCreateSelectFromVersion(jobsData[0]);
                for (var i = 0; i < jobsData.length; i++) {
                    self.jobsList.push(_populateData(jobsData[i]));
                }
            } else {
                self.jobsList.push(_populateData(jobsData));
                // _populateCreateSelectFromVersion(jobsData);
            }
        } else {
            self.jobsList.removeAll();
        }
    };

    function _populateCreateSelectFromVersion(version) {
        l_FormCreateJobsModel.fromVersion(_getTextValue(version));
    }

    function _populateCreateSelectToVersion(toVersion) {
        l_FormCreateJobsModel.toVersion(_getTextValue(toVersion));
    }


    function _populateData(response_data) {
        var jobsObj = {};
        jobsObj.Id = response_data["UpgradeJob-id"].Id;
        // jobsObj.ItemId = response_data["UpgradeJob-id"].ItemId;
        jobsObj.UpgradeStatus = ko.observable(_getTextValue(response_data.UpgradeStatus));
        jobsObj.UpgradeEntityType = _getTextValue(response_data.UpgradeEntityType);
        jobsObj.FromVersion = _getTextValue(response_data.FromVersion);
        jobsObj.ToVersion = _getTextValue(response_data.ToVersion);
        return jobsObj;
    };


    self.decrementToLast = function () {
        self.currentPage(1);
        self.hideDecrement(true);
        self.hideIncrement(false);
        self.refreshTable();
    }

    self.decrementOffsetLimit = function () {
        if (self.currentPage() > 1) {
            self.currentPage(self.currentPage() - 1);
        }
        if (self.currentPage() < Math.ceil(self.totalListsCount() / defaultPerPage)) {
            self.hideIncrement(false);
        }
        if (self.currentPage() == 1) {
            self.hideDecrement(true);
        }
        if (self.currentPage() < 1) {
            return;
        }
        self.refreshTable();
    }

    self.incrementOffsetLimit = function () {
        var totalPages = Math.ceil(self.totalListsCount() / defaultPerPage);
        if (self.currentPage() < Math.ceil(self.totalListsCount() / defaultPerPage)) {
            self.currentPage(self.currentPage() + 1);
        }
        if (self.currentPage() == Math.ceil(self.totalListsCount() / defaultPerPage)) {
            self.hideIncrement(true);
        }
        if (self.currentPage() > 1) {
            self.hideDecrement(false);
        }
        self.refreshTable();
    }

    self.incrementToLast = function () {
        self.currentPage(Math.ceil(self.totalListsCount() / defaultPerPage));
        self.hideDecrement(false);
        self.hideIncrement(true);
        self.refreshTable();
    }
    //Pagination END

    self.openFilter = function () {
        self.filterExpand(!self.filterExpand());
        if (!self.filterExpand()) {
            self.ClearFilter();
        }
    }

    self.ApplyFilter = function () {
        var filterParams = {};
        filterParams.applyFilter = true;
        filterParams.filterUpgradeType = self.filterUpgradeTypeInput();
        filterParams.filterFromVersion = self.filterFromVersionInput();
        filterParams.filterToVersion = self.filterToVersionInput();
        _defaultPagination();
        self.refreshTable(filterParams);
    }
    self.ClearFilter = function () {
        self.filterExpand(false);
        _clearFilterInputs();
        _defaultPagination();
        self.refreshTable();
    }

    function _clearFilterInputs() {
        self.filterUpgradeTypeInput("");
        self.filterFromVersionInput("");
        self.filterToVersionInput("");
    }
    function _defaultPagination() {
        self.currentPage(1);
    }


    self.ContractRetriggerUpgrade = function (data, event) {
        // ccSelectUpgradeVersion();
        if (data.FromVersion && data.ToVersion) {
            cc_jobs_services.MainUpgradeRetriggerService({
                'fromVersion': data.FromVersion,
                'toVersion': data.ToVersion,
                'entityType': data.UpgradeEntityType,
				'jobId':data.Id
            }, function (response_data, status) {
                if (status !== "ERROR") {
                    l_JobsListViewModel.refreshTable();
                    successToast(3000, "Upgrade process started.");
                    $('#create_batchjob_modal').modal('hide');
                }
            });
        } else {
            // errorToast(3000, getTranslationMessage('Select upgrade from version.'));
            showOrHideErrorInfo("div_modalCreateErrorInfoArea", true, getTranslationMessage('Select upgrade from version.'), 3000);
        }
    };

}



function ccSelectUpgradeVersion() {
    var select = document.getElementById("sectionList");
    l_JobsListViewModel.selectedVersion(select.value);
}


function ccAppendVersionOptions() {
    // var select = document.getElementById("sectionList");
    // var opt = document.createElement("option");
    // opt.text = "--select--";
    // opt.value = "";
    // select.add(opt);
    // cc_versions.forEach(val => {
    //     opt = document.createElement("option");
    //     opt.text = val;
    //     opt.value = val;
    //     select.add(opt);
    // });
}


function ccUpgrade() {
    // ccSelectUpgradeVersion();
    if (l_FormCreateJobsModel.fromVersion()) {
        cc_jobs_services.ccUpgradeService({ 'fromVersion': l_FormCreateJobsModel.fromVersion() }, function (response_data, status) {
            if (status !== "ERROR") {
                l_JobsListViewModel.refreshTable();
                successToast(3000, "Upgrade process started.");
                $('#create_batchjob_modal').modal('hide');
            }
        });
    } else {
        // errorToast(3000, getTranslationMessage('Select upgrade from version.'));
        showOrHideErrorInfo("div_modalCreateErrorInfoArea", true, getTranslationMessage('Select upgrade from version.'), 3000);
    }
};


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

var l_JobsListViewModel = new JobsListViewModel();
var l_FormCreateJobsModel = new CreateJobsModel();
var l_ClauseContainer_List = [];
var l_contract_rule_result = new Map();


$(function () {
    i_locale = getlocale();
    ccAppendVersionOptions();
    translateLabels("com/opentext/apps/contractcenter/ContractCenter/ContractCenter", i_locale);
    var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
    loadRTLIfRequired(i_locale, rtl_css);
    $('[src*="ccupgrade.htm"]', window.parent.parent.document).parent().css('padding-left', '0px');
    cInstanceId = getUrlParameterValue("instanceId", null, true);
    defaultContainingSectionId = getUrlParameterValue("defaultContSectionId", null, true);
    relatedTemplateId = getUrlParameterValue("relatedTemplateId", null, true);
    jobRelatedStatus = getUrlParameterValue("Z_INT_CTRInfo", null, true);
    l_JobsListViewModel.refreshTable();

    ko.applyBindings(l_JobsListViewModel, document.getElementById("List_container"));
    // ko.applyBindings(l_BatchJobsListViewModel, document.getElementById("BatchList_container"));
    ko.applyBindings(l_FormCreateJobsModel, document.getElementById("create_batchjob_modal"));
});
