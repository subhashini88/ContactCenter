// Global variables.
var isReportFirstTimeLoading = true;
var dateRanges = [];
var calenderOrientaion = ['down', 'down', 'down', 'up', 'up', 'up'];
var showHeaderAndFooter = [false, false, false, false, false, false];
var charObjs = {};
var IS_SESSION_TIMED_OUT = false;

//Constannts.
const REPORT_BODY_DIV = 'cardbody';
const COUNT = 'count';
const VALUE = 'value';
const DATE_FORMAT_SUBMISSION = "YYYY-MM-DD";
const DATE_FROMAT_UI = "MM/DD/YYYY";
const NUMBER_OF_YEARS = 20; // Number of years to consider per calender dropdown.


// Contact objects.
const RSA_VALUE_REPORT = { name: "Revenue / Spend analytics", URL: l_RSA_Value_ReportPath };
const RSA_COUNT_REPORT = { name: "Revenue / Spend analytics", URL: l_RSA_Count_ReportPath };
const UR_VALUE_REPORT = { name: "Upcoming renewals", URL: l_UR_Value_ReportPath };
const UR_COUNT_REPORT = { name: "Upcoming renewals", URL: l_UR_Count_ReportPath };
const CTA_VALUE_REPORT = { name: "Cycle time analytics", URL: l_CTA_Value_ReportPath };
const CTA_COUNT_REPORT = { name: "Cycle time analytics", URL: l_CTA_Count_ReportPath };
const NVR_VALUE_REPORT = { name: "New Vs Renewals", URL: l_NVR_Value_ReportPath };
const NVR_COUNT_REPORT = { name: "New Vs Renewals", URL: l_NVR_Count_ReportPath };
const RVC_VALUE_REPORT = { name: "Renewals Vs Cancellations", URL: l_RVC_Value_ReportPath };
const RVC_COUNT_REPORT = { name: "Renewals Vs Cancellations", URL: l_RVC_Count_ReportPath };
const YYA_VALUE_REPORT = { name: "YoY analytics", URL: l_YYA_Value_ReportPath };
const YYA_COUNT_REPORT = { name: "YoY analytics", URL: l_YYA_Count_ReportPath };
const FILTER_ALL_QUARTER = { label: "All quarter", value: "AllQuarters" };
const FILTER_FIRST_QUARTER = { label: "First quarter", value: "1" };
const FILTER_SECOND_QUARTER = { label: "Second quarter", value: "2" };
const FILTER_THIRD_QUARTER = { label: "Third quarter", value: "3" };
const FILTER_FOURTH_QUARTER = { label: "Fourth quarter", value: "4" };


/** Models.  */

var ReportSize = function () {
    var self = this;

    // Variables.
    self.height = 0;
    self.width = 0;
}

var DateRange = function () {
    var self = this;
    var selectedYear = moment().year();

    // Variables.
    self.id = '';
}

var ReportParams = function () {
    var self = this;
}

var AppliedFiltersModel = function () {
    var self = this;

    // Observables.
    self.IntentType = ko.observableArray([]);
    self.Category = ko.observable('');
    self.ContractTypes = ko.observableArray([]);
    self.Regions = ko.observableArray([]);
    self.ValueRanges = ko.observableArray([]);
    self.IncludeTerminations = ko.observable(false);
    self.IncludeAutorenewals = ko.observable(false);
    self.DataType = ko.observable();
    self.IncludeActive = ko.observable(false);
}


var ReportModel = function () {
    var self = this;

    // Variables.
    self.countReportName = '';
    self.valueReportName = '';
    self.countReportURL = '';
    self.valueReportURL = '';
    self.reportURL = '';
    self.reportType = '';
    self.reportParms = null;
    self.appliedFilters = new AppliedFiltersModel();

    // Observables.
    self.reportName = ko.observable();
    self.contractValue = ko.observable();
}


var Reports = function () {
    var self = this;

    // Observables.
    self.reports = ko.observableArray([]);
    self.refreshedTime = ko.observable('');

    // Behaviors.
    self.removeItemFromSelectedList = function (reportIndex, filterType) {
        if (self.reports()[reportIndex].appliedFilters[filterType]) {
            self.reports()[reportIndex].appliedFilters[filterType].remove(this);
        }
    }

}

var ContractTypesModel = function () {
    var self = this;

    // Variables.
    self.cardIndex = 0;

    // Observables.
    self.ContractTypesList = ko.observableArray([]);
    self.ContractTypesSelectedList = ko.observableArray([]);

    // Computed observables.
    self.selectedAll = ko.pureComputed({
        read: function () {
            return self.ContractTypesSelectedList().length === self.ContractTypesList().length;
        },
        write: function (value) {
            self.ContractTypesSelectedList(value ? self.ContractTypesList().slice(0) : []);
        },
        owner: self
    });

    // Behaviour methods.
    self.passContractTypeFilters = function () {
        updateContractTypeFilters(self.cardIndex, self.ContractTypesSelectedList());
    };
    self.clearContractTypesModalData = function () {
        self.cardIndex = 0;
        self.ContractTypesSelectedList.removeAll();
        $("#id_contractTypesSearchElement").val("");
    }
    self.removeItemFromSelectedList = function () {
        self.ContractTypesSelectedList.remove(this);
    }
}



var RegionsModel = function () {
    var self = this;

    // Variables.
    self.cardIndex = 0;

    // Observables.
    self.RegionsList = ko.observableArray([]);
    self.RegionsSelectedList = ko.observableArray([]);

    // Computed observables.
    self.selectedAll = ko.pureComputed({
        read: function () {
            return self.RegionsSelectedList().length === self.RegionsList().length;
        },
        write: function (value) {
            self.RegionsSelectedList(value ? self.RegionsList().slice(0) : []);
        },
        owner: self
    });

    // Behaviour methods.
    self.passRegionFilters = function () {
        updateRegionFilters(self.cardIndex, self.RegionsSelectedList());
    };
    self.clearRegionModalData = function () {
        self.cardIndex = 0;
        self.RegionsSelectedList.removeAll();
        $("#id_regionsSearchElement").val("");
    }
    self.removeItemFromSelectedList = function () {
        self.RegionsSelectedList.remove(this);
    }
}

var ContractValueRangesModel = function () {
    var self = this;

    // Variables.
    self.cardIndex = 0;

    // Observables.
    self.ContractValueRangesList = ko.observableArray([]);
    self.ContractValueRangeSelectedList = ko.observableArray([]);

    // Computed observables.
    self.selectedAll = ko.pureComputed({
        read: function () {
            return self.ContractValueRangeSelectedList().length === self.ContractValueRangesList().length;
        },
        write: function (value) {
            self.ContractValueRangeSelectedList(value ? self.ContractValueRangesList().slice(0) : []);
        },
        owner: self
    });

    // Behaviour methods.
    self.passValueRangeFilters = function () {
        updateValueRangeFilters(self.cardIndex, self.ContractValueRangeSelectedList());
    };
    self.clearValueRangeModalData = function () {
        self.cardIndex = 0;
        self.ContractValueRangeSelectedList.removeAll();
        $("#id_contractValueRangesSearchElement").val("");
    }
    self.removeItemFromSelectedList = function () {
        self.ContractValueRangeSelectedList.remove(this);
    }
}


// Global model objects.

var i_reports_obj = new Reports();
var g_report_size = new ReportSize();
var l_regions_model = new RegionsModel();
var l_contractTypes_model = new ContractTypesModel();
var l_contractValueRanges_model = new ContractValueRangesModel();