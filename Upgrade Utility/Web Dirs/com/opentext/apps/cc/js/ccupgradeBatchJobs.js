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
const cc_versions = ["", "19.2", "19.4", "20.2", "22.2", "22.3"];

var cc_jobs_services = (function () {
    var self = {};
    self.getjobsDefaultListService = function (params, responseCallback) {
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
    // self.getjobsListService = function (params, responseCallback) {
    //     $.cordys.ajax({
    //         method: "getUpgradeBatchJobsFilteredList",
    //         namespace: "http://schemas.opentext.com/UpgradeUtils/22.3",
    //         parameters: params,
    //         success: function (responseSuccess) {
    //             responseCallback(responseSuccess);
    //         },
    //         error: function (responseFailure) {
    //             responseCallback(responseFailure, "ERROR");
    //             showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while upgrading contract center. Contact the administrator."), 10000);
    //             return false;
    //         }
    //     });
    // };
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
    self.ccUpgradeBatchDefaultListService = function (params, responseCallback) {
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

    self.ccUpgradeAllBatchListService = function (params, responseCallback) {
        $.cordys.ajax({
            method: "getUpgradeBatchJobsFilteredList",
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



function BatchJobsListViewModel() {
    var self = this;
    self.batchJobsList = ko.observableArray();
    self.clauseContainerList = ko.observableArray();
    self.modalInstanceList = ko.observableArray();
    //Pagination START
    self.totalListsCount = ko.observable(0);
    self.currentPage = ko.observable(1);
    self.hideDecrement = ko.observable(false);
    self.hideIncrement = ko.observable(false);


    //filter start
    self.filterUpgradeJobId = ko.observable(defaultFilterVal);
    self.filterUpgradeJobIdInput = ko.observable("");
    self.filterUpgradeJobIdExpand = ko.observable(true);

    self.filterUpgradeParentJobId = ko.observable(defaultFilterVal);
    self.filterUpgradeParentJobIdInput = ko.observable("");
    self.filterUpgradeParentJobIdExpand = ko.observable(true);

    self.filterUpgradeStatus = ko.observable(defaultFilterVal);
    self.filterUpgradeStatusInput = ko.observable("");
    self.filterUpgradeStatusExpand = ko.observable(true);

    self.filterExpand = ko.observable(false);
    self.filterOptions = ko.observableArray(filterList);
    //filter end

    self.openUpgradeJobInstListModal = function (data, event) {
        _populateInstanceListData(data);
        $("#create_batchjob_modal").modal({
            backdrop: 'static',
            keyboard: false
        });
        event.stopPropagation();
        translatePage();
    }

    function _populateInstanceListData(data) {
        self.modalInstanceList.removeAll();
        var instaceArr = data.AllInstanceIds.split(";");
        if (instaceArr.length > 0) {
            instaceArr.forEach(inst => {
                var status = '';
                var message = '';
                if (!data.ErrorData || !JSON.parse(data.ErrorData)[inst]) {
                    status = "SUCCESS";
                } else {
                    status = "ERROR";
                    message = JSON.stringify(JSON.parse(data.ErrorData)[inst]["Errors"]);
                }
                if (inst) {
                    self.modalInstanceList.push({
                        InstanceId: inst,
                        Status: status,
                        Message: message
                    });
                }
            });
        }
    }


    self.refreshTable = function (params) {
        var offset = (self.currentPage() - 1) * defaultPerPage;
        self.fetchjobsList(params, offset, defaultPerPage);
    };

    function populateJobListRequest(input) {
        var request = {};
        if (input && input.applyFilter) {
            request["jobId"] = input.filterUpgradeJobId;
            request["parentJobId"] = input.filterUpgradeParentJobId;
            request["upgradeStatus"] = input.filterUpgradeStatus;
        }
        return request;
    }

    self.fetchjobsList = function (params, offset, limit) {
        var req = populateJobListRequest(params);
        req.offset = offset;
        req.limit = limit;
        cc_jobs_services.ccUpgradeAllBatchListService(req, function (response_data, status) {
            if (status !== "ERROR") {
                var jobsData = response_data.Response.FindZ_INT_UpgradeBatchJobListResponse.UpgradeBatchJob;
                _populateDatatable(response_data);
                if (jobsData) {
                    self.totalListsCount(response_data.Response.FindZ_INT_UpgradeBatchJobListResponse["@total"]);
                } else {
                    self.totalListsCount(0);
                }
                self.currentPage(offset / defaultPerPage + 1);
            }
        });

    };

    function _populateDatatable(response_data) {
        if (response_data.Response.FindZ_INT_UpgradeBatchJobListResponse.UpgradeBatchJob) {
            var jobsData = response_data.Response.FindZ_INT_UpgradeBatchJobListResponse.UpgradeBatchJob;
            self.batchJobsList.removeAll();
            if (Array.isArray(jobsData)) {
                for (var i = 0; i < jobsData.length; i++) {
                    self.batchJobsList.push(_populateData(jobsData[i]));
                }
            } else {
                self.batchJobsList.push(_populateData(jobsData));
            }
        } else {
            self.batchJobsList.removeAll();
        }
    };

    function _populateData(response_data) {
        var jobsObj = {};
        jobsObj.Id = response_data["UpgradeBatchJob-id"].Id;
        // jobsObj.ItemId = response_data["UpgradeBatchJob-id"].ItemId;
        jobsObj.ParentId = response_data["RelatedJob"]["UpgradeJob-id"].Id;
        jobsObj.UpgradeStatus = _getTextValue(response_data.JobStatus);
        jobsObj.TotalCount = _getTextValue(response_data.TotalInstanceCount);
        jobsObj.ErrorCount = _getTextValue(response_data.ErrorCount);
        jobsObj.ErrorData = _getTextValue(response_data.ErrorJson);
        jobsObj.SuccessCount = _getTextValue(response_data.SuccessCount);
        jobsObj.SuccessIds = _getTextValue(response_data.SuccessIds);
        jobsObj.AllInstanceIds = _getTextValue(response_data.AllInstanceIds);
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
        filterParams.filterUpgradeJobId = self.filterUpgradeJobIdInput();
        filterParams.filterUpgradeParentJobId = self.filterUpgradeParentJobIdInput();
        filterParams.filterUpgradeStatus = self.filterUpgradeStatusInput();
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
        self.filterUpgradeJobIdInput("");
        self.filterUpgradeParentJobIdInput("");
        self.filterUpgradeStatusInput("");
    }
    function _defaultPagination() {
        self.currentPage(1);
    }
}

function ccSelectUpgradeVersion() {
    var select = document.getElementById("sectionList");
    l_JobsListViewModel.selectedVersion(select.value);
}


function ccAppendVersionOptions() {
    var select = document.getElementById("sectionList");
    var opt = document.createElement("option");
    opt.text = "--select--";
    opt.value = "";
    select.add(opt);
    cc_versions.forEach(val => {
        opt = document.createElement("option");
        opt.text = val;
        opt.value = val;
        select.add(opt);
    });
}


function ccUpgrade() {
    // ccSelectUpgradeVersion();
    cc_jobs_services.ccUpgradeService({ 'fromVersion': l_JobsListViewModel.selectedVersion() }, function (response_data, status) {
        if (status !== "ERROR") {
            l_JobsListViewModel.refreshTable();
            successToast(3000, "Upgrade process started.");
        }
    });
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

var l_BatchJobsListViewModel = new BatchJobsListViewModel();
var l_ClauseContainer_List = [];
var l_contract_rule_result = new Map();


$(function () {
    i_locale = getlocale();
    translateLabels("com/opentext/apps/contractcenter/ContractCenter/ContractCenter", i_locale);
    var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
    loadRTLIfRequired(i_locale, rtl_css);
    $('[src*="ccupgrade.htm"]', window.parent.parent.document).parent().css('padding-left', '0px');
    cInstanceId = getUrlParameterValue("instanceId", null, true);
    defaultContainingSectionId = getUrlParameterValue("defaultContSectionId", null, true);
    relatedTemplateId = getUrlParameterValue("relatedTemplateId", null, true);
    jobRelatedStatus = getUrlParameterValue("Z_INT_CTRInfo", null, true);
    l_BatchJobsListViewModel.refreshTable();
    ko.applyBindings(l_BatchJobsListViewModel, document.getElementById("BatchList_container"));
});
