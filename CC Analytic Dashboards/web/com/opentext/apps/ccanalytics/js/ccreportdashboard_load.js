/** Loading report configuration */

function init() {
	if(window.frameElement)
	{
		window.frameElement.style.minWidth="1000px";
	}

    // Loading the iHUB configuration.
    getiHUbConfiguration(loadReports, loadDashboard);

}

// Load all reports after the configuration. (Call back method1 ).
function loadReports() {

    // iHUB code | configuration.
    actuate.load("viewer");
    actuate.load("parameter");

    var isAlreadyFullSize = false;
    var fullReportCardNumber = 0;
    for (i = 1; i <= i_reports_obj.reports().length; i++) {
        if (isReportFullSize(i)) {
            isAlreadyFullSize = true;
            fullReportCardNumber = i;
            break;
        }
    }

    if (!isAlreadyFullSize || IS_SESSION_TIMED_OUT) {
        for (i = 1; i <= i_reports_obj.reports().length; i++) {
            loadReport(i, false, getReportSize(getReportBodyDivID(i), false, false, i));
        }
    } else {
        loadReport(fullReportCardNumber, false, getReportSize(getReportBodyDivID(fullReportCardNumber), false, false, fullReportCardNumber));
    }
    isReportFirstTimeLoading = false;
}

// Call back method2.
function loadDashboard() {

}

// Initializing reports with required data.
function initilizeReports() {

    // Initialize reports one by one.
    initializeReport1();
    initializeReport2();
    initializeReport3();
    initializeReport4();
    initializeReport5();
    initializeReport6();
}


// Forming the report request before submitting.
function loadReport(reportNumber, isChangeReportType, reportSize) {
    reportNumber = parseInt(reportNumber);
    var iReport = {};
    if (isChangeReportType) {
        if (i_reports_obj.reports()[reportNumber - 1].reportType == VALUE) {
            i_reports_obj.reports()[reportNumber - 1].reportName(iReport.reportName = i_reports_obj.reports()[reportNumber - 1].countReportName);
            i_reports_obj.reports()[reportNumber - 1].reportURL = iReport.reportName = i_reports_obj.reports()[reportNumber - 1].countReportURL;
            i_reports_obj.reports()[reportNumber - 1].reportType = COUNT;
        } else {
            i_reports_obj.reports()[reportNumber - 1].reportName(iReport.reportName = i_reports_obj.reports()[reportNumber - 1].valueReportName);
            i_reports_obj.reports()[reportNumber - 1].reportURL = iReport.reportName = i_reports_obj.reports()[reportNumber - 1].valueReportURL;
            i_reports_obj.reports()[reportNumber - 1].reportType = VALUE;
        }
    }

    // Form i Report with its params.
    iReport.reportName = i_reports_obj.reports()[reportNumber - 1].reportURL;
    iReport.divID = getReportBodyDivID(reportNumber);
    iReport.params = formReportParams(reportNumber);
    iReport.reportSize = reportSize;
    iReport.uiOptions = null;
    iReport.reportNumber = reportNumber;
    //if (reportNumber == 1) {
    //  iReport.uiOptions = new actuate.viewer.UIOptions();
    // iReport.uiOptions.disableChartProperty(HTMLLegendElement)
    //}

    // Submit the report.
    submitReport(iReport);
}

// Submitting the report to iHUB.
function submitReport(iReport) {
    if (!iReport.reportName || !iReport.divID) {
        return;
    }
    //var parent = $('#' + iReport.divID).parent();
    //$('#' + iReport.divID).remove();
    //var htmlString = '<div id="' + iReport.divID + '" class="cc-cardReport-body card-' + iReport.reportNumber + '-report" cardid=' + iReport.reportNumber + '></div>';
    //$(htmlString).insertBefore(parent.children()[0]);
	var viewer = actuate.getViewer(iReport.divID);
	if(!viewer)
	{
		viewer = new actuate.Viewer(iReport.divID);
		viewer.registerEventHandler(actuate.viewer.EventConstants.ON_EXCEPTION, onExceptionInReport);
		viewer.registerEventHandler(actuate.viewer.EventConstants.ON_SESSION_TIMEOUT, onSessionTimeOut);
	}    
    if (!iReport.uiOptions) {
        iReport.uiOptions = new actuate.viewer.UIOptions();
    }
    iReport.uiOptions.enableEditReport(false);
    iReport.uiOptions.enableToolBar(false);
    iReport.uiOptions.enableToolbarContextMenu(false)
    iReport.uiOptions.spacingLeft = 0;
    iReport.uiOptions.spacingRight = 0;
    iReport.uiOptions.spacingBottom = 0;
    iReport.uiOptions.spacingTop = 0;
    viewer.setUIOptions(iReport.uiOptions);
    viewer.setReportName(iReport.reportName);
    iReport.params.l_baseURL = getApplicationURL();
    iReport.params.reportNumber = iReport.reportNumber;
    viewer.setParameters(iReport.params);
    
    var height = (isReportFullSize(iReport.reportNumber)) ? iReport.reportSize.height - (iReport.reportSize.height / 8) : iReport.reportSize.height - 10;
    var width = (iReport.reportSize.width - 10);
    viewer.setHeight(height);
    viewer.setWidth(width);
	var l_contElement = document.getElementById(iReport.divID);
    // viewer.setWidth(iReport.reportSize.width);
    // viewer.setHeight(iReport.reportSize.height);

    // Request submit to iHUB.
    // viewer.submit();

    var resizereport = function (reportObj) {
        loadJqueryIfRequired();
        var reportIndex;
        if (reportObj && reportObj._ && reportObj._._reportParameters) {
            for (var i = 0; i < reportObj._._reportParameters.length; i++) {
                if (reportObj._._reportParameters[i] && reportObj._._reportParameters[i]._name == 'reportNumber') {
                    reportIndex = reportObj._._reportParameters[i]._value;
                    charObjs[reportIndex] = reportObj;
                    break;
                }
            }
        }
        if (reportIndex == 1) {
            var value = '$ ' + currencyRounding(window.RPT_RSA_ContractVal);
            i_reports_obj.reports()[0].contractValue(value);
        } else if (reportIndex == 3) {
            var value = '$ ' + currencyRounding(window.RPT_CT_ContractVal);
            i_reports_obj.reports()[2].contractValue(value);
        } else if (reportIndex == 2) {
            var value = '$ ' + currencyRounding(window.RPT_UR_ContractVal);
            i_reports_obj.reports()[1].contractValue(value);
        }

        try {
            var refreshedTime = window.RPT_last_scheduledTime.replace(/,/g, "");
            i_reports_obj.refreshedTime(moment(new Date(parseInt(refreshedTime)).toUTCString()).format('DD-MMM-YYYY HH:mm'));
        } catch (err) {

        }
        var bookMarkName = iReport.reportName.substring(iReport.reportName.lastIndexOf('\/') + 1, iReport.reportName.lastIndexOf('.'));
        var l_chartObj = viewer.getChart(bookMarkName);
        if (l_chartObj) {
            var l_clientChart = l_chartObj.getClientChart();
            if ((l_chartObj._ && l_chartObj._._htmlElement)) {
                removeWhiteSpace(l_chartObj._._htmlElement);
            }
            if (l_clientChart) {
                var l_exstHeight = l_clientChart.getChartHeight();
                var l_exstWidth = l_clientChart.getChartWidth();
                /* l_chartObj.setSize(Math.min(l_exstWidth, iReport.reportSize.width - 15),
                 Math.min(l_exstHeight, iReport.reportSize.height - 20)); */
                l_clientChart.setSize(Math.min(l_exstWidth, l_contElement.offsetWidth-5),
                    Math.min(l_exstHeight, l_contElement.offsetHeight - 30));
                l_clientChart._option.chart.spacingTop = 0;
                l_clientChart._option.chart.spacingLeft = 0;
                l_clientChart._option.chart.spacingRight = 0;
                l_clientChart._option.chart.spacingBottom = 0;
                // l_clientChart.redraw();
            }
            // l_chartObj.submit();
            //l_clientChart.redraw();
            // l_chartObj.refresh();
        }
    }

    // Request submit to iHUB.
    // if(iReport.reportNumber!=1){
    // log(iReport.params);
    viewer.submit(resizereport);
    //}
}

function removeWhiteSpace(_htmlElement) {
    var levels = 10;
    var tableEncounted = 0;
    for (i = 0; i < levels && tableEncounted < 2 && _htmlElement && _htmlElement.parentElement; i++) {
        _htmlElement = _htmlElement.parentElement;
        if (_htmlElement.nodeName == 'TABLE' || _htmlElement.nodeName == 'table') {
            tableEncounted++;
        }
    }
    if (_htmlElement && _htmlElement.nodeName == 'TABLE' && tableEncounted == 2) {
        _htmlElement.style.height = '';
    }
}

// Initialize report 1.
function initializeReport1() {
    var iReport = new ReportModel();

    // Initialize params.
    var iParams = initilizeParams1();

    iReport.reportName(RSA_VALUE_REPORT.name);
    iReport.countReportName = RSA_VALUE_REPORT.name;
    iReport.valueReportName = RSA_VALUE_REPORT.name;
    iReport.reportParms = iParams;
    iReport.countReportURL = RSA_COUNT_REPORT.URL;
    iReport.valueReportURL = RSA_VALUE_REPORT.URL;
    iReport.reportURL = RSA_VALUE_REPORT.URL;
    iReport.reportType = VALUE;
    iReport.appliedFilters.IntentType.removeAll();
    if (iParams.IntentType() && iParams.IntentType().length) {
        for (var i = 0; i < iParams.IntentType().length; i++) {
            iReport.appliedFilters.IntentType.push(String(iParams.IntentType()[i]));
        }
    }
    iReport.appliedFilters.Category(iParams.Category());

    i_reports_obj.reports().push(iReport);
}

// Initialize report 2.
function initializeReport2() {
    var iReport = new ReportModel();

    // Initialize params.
    var iParams = initilizeParams2();

    iReport.reportName(UR_VALUE_REPORT.name);
    iReport.countReportName = UR_COUNT_REPORT.name;
    iReport.valueReportName = UR_VALUE_REPORT.name;
    iReport.reportParms = iParams;
    iReport.countReportURL = UR_COUNT_REPORT.URL;
    iReport.valueReportURL = UR_VALUE_REPORT.URL;
    iReport.reportURL = UR_VALUE_REPORT.URL;
    iReport.reportType = VALUE;
    if (iParams.IntentType() && iParams.IntentType().length) {
        for (var i = 0; i < iParams.IntentType().length; i++) {
            iReport.appliedFilters.IntentType.push(String(iParams.IntentType()[i]));
        }
    }
    if (iParams.ContractTypes() && iParams.ContractTypes().length) {
        for (var i = 0; i < iParams.ContractTypes().length; i++) {
            iReport.appliedFilters.ContractTypes.push(String(iParams.ContractTypes()[i]));
        }
    }
    if (iParams.Regions() && iParams.Regions().length) {
        for (var i = 0; i < iParams.Regions().length; i++) {
            iReport.appliedFilters.Regions.push(String(iParams.Regions()[i]));
        }
    }
    if (iParams.ValueRanges() && iParams.ValueRanges().length) {
        for (var i = 0; i < iParams.ValueRanges().length; i++) {
            iReport.appliedFilters.ValueRanges.push(String(iParams.ValueRanges()[i]));
        }

    }
    iReport.appliedFilters.IncludeTerminations(iParams.IncludeTerminations);
    iReport.appliedFilters.IncludeAutorenewals(iParams.IncludeAutorenewals);

    i_reports_obj.reports().push(iReport);
}

// Initialize report 3.
function initializeReport3() {
    var iReport = new ReportModel();

    // Initialize params.
    var iParams = initilizeParams3();

    iReport.reportName(CTA_VALUE_REPORT.name);
    iReport.countReportName = CTA_COUNT_REPORT.name;
    iReport.valueReportName = CTA_VALUE_REPORT.name;
    iReport.reportParms = iParams;
    iReport.countReportURL = CTA_COUNT_REPORT.URL;
    iReport.valueReportURL = CTA_VALUE_REPORT.URL;
    iReport.reportURL = CTA_VALUE_REPORT.URL;
    iReport.reportType = VALUE;
    if (iParams.IntentType() && iParams.IntentType().length) {
        for (var i = 0; i < iParams.IntentType().length; i++) {
            iReport.appliedFilters.IntentType.push(String(iParams.IntentType()[i]));
        }
    }
    if (iParams.ContractTypes() && iParams.ContractTypes().length) {
        for (var i = 0; i < iParams.ContractTypes().length; i++) {
            iReport.appliedFilters.ContractTypes.push(String(iParams.ContractTypes()[i]));
        }
    }
    if (iParams.Regions() && iParams.Regions().length) {
        for (var i = 0; i < iParams.Regions().length; i++) {
            iReport.appliedFilters.Regions.push(String(iParams.Regions()[i]));
        }
    }
    if (iParams.ValueRanges() && iParams.ValueRanges().length) {
        for (var i = 0; i < iParams.ValueRanges().length; i++) {
            iReport.appliedFilters.ValueRanges.push(String(iParams.ValueRanges()[i]));
        }
    }
    iReport.appliedFilters.IncludeActive(iParams.includeActive);
    i_reports_obj.reports().push(iReport);
}

// Initialize report 4.
function initializeReport4() {
    var iReport = new ReportModel();

    // Initialize params.
    var iParams = initilizeParams4();

    iReport.reportName(NVR_VALUE_REPORT.name);
    iReport.countReportName = NVR_COUNT_REPORT.name;
    iReport.valueReportName = NVR_VALUE_REPORT.name;
    iReport.reportParms = iParams;
    iReport.countReportURL = NVR_COUNT_REPORT.URL;
    iReport.valueReportURL = NVR_VALUE_REPORT.URL;
    iReport.reportURL = NVR_VALUE_REPORT.URL;
    iReport.reportType = VALUE;
    if (iParams.IntentType() && iParams.IntentType().length) {
        for (var i = 0; i < iParams.IntentType().length; i++) {
            iReport.appliedFilters.IntentType.push(String(iParams.IntentType()[i]));
        }
    }
    if (iParams.ContractTypes() && iParams.ContractTypes().length) {
        for (var i = 0; i < iParams.ContractTypes().length; i++) {
            iReport.appliedFilters.ContractTypes.push(String(iParams.ContractTypes()[i]));
        }
    }
    if (iParams.Regions() && iParams.Regions().length) {
        for (var i = 0; i < iParams.Regions().length; i++) {
            iReport.appliedFilters.Regions.push(String(iParams.Regions()[i]));
        }
    }
    if (iParams.ValueRanges() && iParams.ValueRanges().length) {
        for (var i = 0; i < iParams.ValueRanges().length; i++) {
            iReport.appliedFilters.ValueRanges.push(String(iParams.ValueRanges()[i]));
        }
    }
    iReport.appliedFilters.IncludeAutorenewals(iParams.IncludeAutorenewals);

    i_reports_obj.reports().push(iReport);
}

// Initialize report 5.
function initializeReport5() {
    var iReport = new ReportModel();

    // Initialize params.
    var iParams = initilizeParams5();

    iReport.reportName(RVC_VALUE_REPORT.name);
    iReport.countReportName = RVC_COUNT_REPORT.name;
    iReport.valueReportName = RVC_VALUE_REPORT.name;
    iReport.reportParms = iParams;
    iReport.countReportURL = RVC_COUNT_REPORT.URL;
    iReport.valueReportURL = RVC_VALUE_REPORT.URL;
    iReport.reportURL = RVC_VALUE_REPORT.URL;
    iReport.reportType = VALUE;
    if (iParams.IntentType() && iParams.IntentType().length) {
        for (var i = 0; i < iParams.IntentType().length; i++) {
            iReport.appliedFilters.IntentType.push(String(iParams.IntentType()[i]));
        }
    }
    if (iParams.ContractTypes() && iParams.ContractTypes().length) {
        for (var i = 0; i < iParams.ContractTypes().length; i++) {
            iReport.appliedFilters.ContractTypes.push(String(iParams.ContractTypes()[i]));
        }
    }
    if (iParams.Regions() && iParams.Regions().length) {
        for (var i = 0; i < iParams.Regions().length; i++) {
            iReport.appliedFilters.Regions.push(String(iParams.Regions()[i]));
        }
    }
    if (iParams.ValueRanges() && iParams.ValueRanges().length) {
        for (var i = 0; i < iParams.ValueRanges().length; i++) {
            iReport.appliedFilters.ValueRanges.push(String(iParams.ValueRanges()[i]));
        }
    }
    iReport.appliedFilters.IncludeAutorenewals(iParams.IncludeAutorenewals);

    i_reports_obj.reports().push(iReport);
}

// Initialize report 6.
function initializeReport6() {
    var iReport = new ReportModel();

    // Initialize params.
    var iParams = initilizeParams6();

    iReport.reportName(YYA_VALUE_REPORT.name);
    iReport.countReportName = YYA_COUNT_REPORT.name;
    iReport.valueReportName = YYA_VALUE_REPORT.name;
    iReport.reportParms = iParams;
    iReport.countReportURL = YYA_COUNT_REPORT.URL;
    iReport.valueReportURL = YYA_VALUE_REPORT.URL;
    iReport.reportURL = YYA_VALUE_REPORT.URL;
    iReport.reportType = VALUE;
    if (iParams.IntentType() && iParams.IntentType().length) {
        for (var i = 0; i < iParams.IntentType().length; i++) {
            iReport.appliedFilters.IntentType.push(String(iParams.IntentType()[i]));
        }
    }
    if (iParams.ContractTypes() && iParams.ContractTypes().length) {
        for (var i = 0; i < iParams.ContractTypes().length; i++) {
            iReport.appliedFilters.ContractTypes.push(String(iParams.ContractTypes()[i]));
        }
    }
    if (iParams.Regions() && iParams.Regions().length) {
        for (var i = 0; i < iParams.Regions().length; i++) {
            iReport.appliedFilters.Regions.push(String(iParams.Regions()[i]));
        }
    }
    if (iParams.ValueRanges() && iParams.ValueRanges().length) {
        for (var i = 0; i < iParams.ValueRanges().length; i++) {
            iReport.appliedFilters.ValueRanges.push(String(iParams.ValueRanges()[i]));
        }
    }
    iReport.appliedFilters.DataType(iParams.DataType);
    i_reports_obj.reports().push(iReport);
}

/** Initializing the report params with default values. */

// Initializing parameters of report 1 with default values.
function initilizeParams1() {
    var iParams = new ReportParams();
    iParams.Quarter = FILTER_ALL_QUARTER.label;
    iParams.Category = ko.observable("Contract type");
    iParams.Year = moment().year();
    iParams.IntentType = ko.observableArray(["Buy", "Sell", "Other"]);
    iParams.Duration = "Period";
    iParams.FromDate = moment().year(iParams.Year).startOf('year').format(DATE_FROMAT_UI);
    iParams.ToDate = moment().year(iParams.Year).endOf('year').format(DATE_FROMAT_UI);
    return iParams;
}

// Initializing parameters of report 2 with default values.
function initilizeParams2() {
    var iParams = new ReportParams();
    iParams.Quarter = FILTER_ALL_QUARTER.label;
    iParams.ValueRanges = ko.observableArray([]);
    iParams.IncludeTerminations = false;
    iParams.IncludeAutorenewals = false;
    iParams.ContractTypes = ko.observableArray([]);
    iParams.IntentType = ko.observableArray(["Buy", "Sell", "Other"]);
    iParams.Regions = ko.observableArray([]);
    iParams.Year = moment().year();
    iParams.Duration = "Period";
    iParams.FromDate = moment().year(iParams.Year).startOf('year').format(DATE_FROMAT_UI);
    iParams.ToDate = moment().year(iParams.Year).endOf('year').format(DATE_FROMAT_UI);
    return iParams;
}

// Initializing parameters of report 3 with default values.
function initilizeParams3() {
    var iParams = new ReportParams();
    iParams.Quarter = FILTER_ALL_QUARTER.label;
    iParams.ValueRanges = ko.observableArray([]);
    iParams.includeActive = false;
    iParams.ContractTypes = ko.observableArray([]);;
    iParams.IntentType = ko.observableArray(["Buy", "Sell", "Other"]);
    iParams.Regions = ko.observableArray([]);;
    iParams.Year = moment().year();
    iParams.Duration = "Period";
    iParams.FromDate = moment().year(iParams.Year).startOf('year').format(DATE_FROMAT_UI);
    iParams.ToDate = moment().year(iParams.Year).endOf('year').format(DATE_FROMAT_UI);
    return iParams;
}

// Initializing parameters of report 4 with default values.
function initilizeParams4() {
    var iParams = new ReportParams();
    iParams.Quarter = FILTER_ALL_QUARTER.label;
    iParams.ValueRanges = ko.observableArray([]);;
    iParams.IncludeAutorenewals = true;
    iParams.ContractTypes = ko.observableArray([]);;
    iParams.IntentType = ko.observableArray(["Buy", "Sell", "Other"]);
    iParams.Regions = ko.observableArray([]);;
    iParams.Year = moment().year();
    iParams.Duration = "Period";
    iParams.FromDate = moment().year(iParams.Year).startOf('year').format(DATE_FROMAT_UI);
    iParams.ToDate = moment().year(iParams.Year).endOf('year').format(DATE_FROMAT_UI);
    return iParams;
}

// Initializing parameters of report 5 with default values.
function initilizeParams5() {
    var iParams = new ReportParams();
    iParams.Quarter = FILTER_ALL_QUARTER.label;
    iParams.ValueRanges = ko.observableArray([]);;
    iParams.IncludeAutorenewals = true;
    iParams.ContractTypes = ko.observableArray([]);;
    iParams.IntentType = ko.observableArray(["Buy", "Sell", "Other"]);
    iParams.Regions = ko.observableArray([]);;
    iParams.Year = moment().year();
    iParams.Duration = "Period";
    iParams.FromDate = moment().year(iParams.Year).startOf('year').format(DATE_FROMAT_UI);
    iParams.ToDate = moment().year(iParams.Year).endOf('year').format(DATE_FROMAT_UI);
    return iParams;
}

// Initializing parameters of report 6 with default values.
function initilizeParams6() {
    var iParams = new ReportParams();
    iParams.ValueRanges = ko.observableArray([]);;
    iParams.ContractTypes = ko.observableArray([]);;
    iParams.IntentType = ko.observableArray(["Buy", "Sell", "Other"]);
    iParams.DataType = "NEW";
    iParams.Regions = ko.observableArray([]);;
    iParams.FirstYear = ko.observable(moment().year());
    iParams.SecondYear = ko.observable((moment().year() - 1));
    return iParams;
}


// Forming the report parameters based on the report number.
function formReportParams(reportNumber) {

    var index = reportNumber - 1;
    switch (reportNumber) {

        // Report 1.
        case 1:
            var paramsObj = {};
            paramsObj.Quarter = (i_reports_obj.reports()[index].reportParms.Quarter) ?
                getQuarterFilter(i_reports_obj.reports()[index].reportParms.Quarter) : FILTER_ALL_QUARTER.value;
            paramsObj.Category = (i_reports_obj.reports()[index].reportParms.Category()) ? i_reports_obj.reports()[index].reportParms.Category() : "";
            paramsObj.Year = (i_reports_obj.reports()[index].reportParms.Year) ? i_reports_obj.reports()[index].reportParms.Year : moment().year();
            paramsObj.Duration = (i_reports_obj.reports()[index].reportParms.Duration) ? i_reports_obj.reports()[index].reportParms.Duration : "Period";
            paramsObj.FromDate = (i_reports_obj.reports()[index].reportParms.FromDate) ?
                moment(i_reports_obj.reports()[index].reportParms.FromDate, DATE_FROMAT_UI).format(DATE_FORMAT_SUBMISSION) :
                moment().startOf('year').format(DATE_FORMAT_SUBMISSION);
            paramsObj.ToDate = (i_reports_obj.reports()[index].reportParms.ToDate) ?
                moment(i_reports_obj.reports()[index].reportParms.ToDate, DATE_FROMAT_UI).format(DATE_FORMAT_SUBMISSION) :
                moment().endOf('year').format(DATE_FORMAT_SUBMISSION);
            paramsObj["Intent type"] = (i_reports_obj.reports()[index].reportParms.IntentType() &&
                i_reports_obj.reports()[index].reportParms.IntentType().length > 0) ? i_reports_obj.reports()[index].reportParms.IntentType() : [];
            return paramsObj;

        // Report 2.
        case 2:
            var paramsObj = {};
            paramsObj.Quarter = (i_reports_obj.reports()[index].reportParms.Quarter) ?
                getQuarterFilter(i_reports_obj.reports()[index].reportParms.Quarter) : FILTER_ALL_QUARTER.value;
            paramsObj.Year = (i_reports_obj.reports()[index].reportParms.Year) ? i_reports_obj.reports()[index].reportParms.Year : moment().year();
            paramsObj.FromDate = (i_reports_obj.reports()[index].reportParms.FromDate) ?
                moment(i_reports_obj.reports()[index].reportParms.FromDate, DATE_FROMAT_UI).format(DATE_FORMAT_SUBMISSION) :
                moment().startOf('year').format(DATE_FORMAT_SUBMISSION);
            paramsObj.ToDate = (i_reports_obj.reports()[index].reportParms.ToDate) ?
                moment(i_reports_obj.reports()[index].reportParms.ToDate, DATE_FROMAT_UI).format(DATE_FORMAT_SUBMISSION) :
                moment().endOf('year').format(DATE_FORMAT_SUBMISSION);
            paramsObj.Duration = "DateRange";
            paramsObj["Include terminations"] = (i_reports_obj.reports()[index].reportParms.IncludeTerminations) ? "True" : "False";
            paramsObj["Include autorenewals"] = (i_reports_obj.reports()[index].reportParms.IncludeAutorenewals) ? "True" : "False";
            paramsObj["Contract value"] = getIdAsArray(i_reports_obj.reports()[index].reportParms.ValueRanges());
            paramsObj["Contract type"] = getIdAsArray(i_reports_obj.reports()[index].reportParms.ContractTypes());
            paramsObj["Intent type"] = (i_reports_obj.reports()[index].reportParms.IntentType() &&
                i_reports_obj.reports()[index].reportParms.IntentType().length > 0) ? i_reports_obj.reports()[index].reportParms.IntentType() : [];
            paramsObj.Region = getIdAsArray(i_reports_obj.reports()[index].reportParms.Regions());
            return paramsObj;

        // Report 3.
        case 3:
            var paramsObj = {};
            paramsObj.Quarter = (i_reports_obj.reports()[index].reportParms.Quarter) ?
                getQuarterFilter(i_reports_obj.reports()[index].reportParms.Quarter) : FILTER_ALL_QUARTER.value;
            paramsObj.Year = (i_reports_obj.reports()[index].reportParms.Year) ? i_reports_obj.reports()[index].reportParms.Year : moment().year();
            paramsObj.Duration = (i_reports_obj.reports()[index].reportParms.Duration) ? i_reports_obj.reports()[index].reportParms.Duration : "Period";
            paramsObj.FromDate = (i_reports_obj.reports()[index].reportParms.FromDate) ?
                moment(i_reports_obj.reports()[index].reportParms.FromDate, DATE_FROMAT_UI).format(DATE_FORMAT_SUBMISSION) :
                moment().startOf('year').format(DATE_FORMAT_SUBMISSION);
            paramsObj.ToDate = (i_reports_obj.reports()[index].reportParms.ToDate) ?
                moment(i_reports_obj.reports()[index].reportParms.ToDate, DATE_FROMAT_UI).format(DATE_FORMAT_SUBMISSION) :
                moment().endOf('year').format(DATE_FORMAT_SUBMISSION);
            paramsObj["Contract value"] = getIdAsArray(i_reports_obj.reports()[index].reportParms.ValueRanges());
            paramsObj["Contract type"] = getIdAsArray(i_reports_obj.reports()[index].reportParms.ContractTypes());
            paramsObj["Include active"] = (i_reports_obj.reports()[index].reportParms.includeActive) ? "True" : "False";
            paramsObj["Intent type"] = (i_reports_obj.reports()[index].reportParms.IntentType() &&
                i_reports_obj.reports()[index].reportParms.IntentType().length > 0) ? i_reports_obj.reports()[index].reportParms.IntentType() : [];
            paramsObj.Region = getIdAsArray(i_reports_obj.reports()[index].reportParms.Regions());
            return paramsObj;

        // Report 4.
        case 4:
            var paramsObj = {};
            paramsObj.Quarter = (i_reports_obj.reports()[index].reportParms.Quarter) ?
                getQuarterFilter(i_reports_obj.reports()[index].reportParms.Quarter) : FILTER_ALL_QUARTER.value;
            paramsObj.Year = (i_reports_obj.reports()[index].reportParms.Year) ? i_reports_obj.reports()[index].reportParms.Year : moment().year();
            paramsObj.FromDate = (i_reports_obj.reports()[index].reportParms.FromDate) ?
                moment(i_reports_obj.reports()[index].reportParms.FromDate, DATE_FROMAT_UI).format(DATE_FORMAT_SUBMISSION) :
                moment().startOf('year').format(DATE_FORMAT_SUBMISSION);
            paramsObj.ToDate = (i_reports_obj.reports()[index].reportParms.ToDate) ?
                moment(i_reports_obj.reports()[index].reportParms.ToDate, DATE_FROMAT_UI).format(DATE_FORMAT_SUBMISSION) :
                moment().endOf('year').format(DATE_FORMAT_SUBMISSION);
            paramsObj.Duration = (i_reports_obj.reports()[index].reportParms.Duration) ? i_reports_obj.reports()[index].reportParms.Duration : "Period";
            paramsObj["Include autorenewals"] = (i_reports_obj.reports()[index].reportParms.IncludeAutorenewals) ? "True" : "False";
            paramsObj["Contract value"] = getIdAsArray(i_reports_obj.reports()[index].reportParms.ValueRanges());
            paramsObj["Contract type"] = getIdAsArray(i_reports_obj.reports()[index].reportParms.ContractTypes());
            paramsObj["Intent type"] = (i_reports_obj.reports()[index].reportParms.IntentType() &&
                i_reports_obj.reports()[index].reportParms.IntentType().length > 0) ? i_reports_obj.reports()[index].reportParms.IntentType() : [];
            paramsObj.Region = getIdAsArray(i_reports_obj.reports()[index].reportParms.Regions());
            return paramsObj;

        // Report 5.
        case 5:
            var paramsObj = {};
            paramsObj.Quarter = (i_reports_obj.reports()[index].reportParms.Quarter) ?
                getQuarterFilter(i_reports_obj.reports()[index].reportParms.Quarter) : FILTER_ALL_QUARTER.value;
            paramsObj.Year = (i_reports_obj.reports()[index].reportParms.Year) ? i_reports_obj.reports()[index].reportParms.Year : moment().year();
            paramsObj.FromDate = (i_reports_obj.reports()[index].reportParms.FromDate) ?
                moment(i_reports_obj.reports()[index].reportParms.FromDate, DATE_FROMAT_UI).format(DATE_FORMAT_SUBMISSION) :
                moment().startOf('year').format(DATE_FORMAT_SUBMISSION);
            paramsObj.ToDate = (i_reports_obj.reports()[index].reportParms.ToDate) ?
                moment(i_reports_obj.reports()[index].reportParms.ToDate, DATE_FROMAT_UI).format(DATE_FORMAT_SUBMISSION) :
                moment().endOf('year').format(DATE_FORMAT_SUBMISSION);
            paramsObj.Duration = (i_reports_obj.reports()[index].reportParms.Duration) ? i_reports_obj.reports()[index].reportParms.Duration : "Period";
            paramsObj["Include autorenewals"] = (i_reports_obj.reports()[index].reportParms.IncludeAutorenewals) ? "True" : "False";
            paramsObj["Contract value"] = getIdAsArray(i_reports_obj.reports()[index].reportParms.ValueRanges());
            paramsObj["Contract type"] = getIdAsArray(i_reports_obj.reports()[index].reportParms.ContractTypes());
            paramsObj["Intent type"] = (i_reports_obj.reports()[index].reportParms.IntentType() &&
                i_reports_obj.reports()[index].reportParms.IntentType().length > 0) ? i_reports_obj.reports()[index].reportParms.IntentType() : [];
            paramsObj.Region = getIdAsArray(i_reports_obj.reports()[index].reportParms.Regions());
            return paramsObj;

        //Report 6.
        case 6:
            var paramsObj = {};
            paramsObj.FirstYear = (i_reports_obj.reports()[index].reportParms.FirstYear()) ? i_reports_obj.reports()[index].reportParms.FirstYear() : moment().year();
            paramsObj.SecondYear = (i_reports_obj.reports()[index].reportParms.SecondYear()) ? i_reports_obj.reports()[index].reportParms.SecondYear() : moment().year();
            paramsObj['Data type'] = (i_reports_obj.reports()[index].reportParms.DataType) ? i_reports_obj.reports()[index].reportParms.DataType : '';
            paramsObj["Contract value"] = getIdAsArray(i_reports_obj.reports()[index].reportParms.ValueRanges());
            paramsObj["Contract type"] = getIdAsArray(i_reports_obj.reports()[index].reportParms.ContractTypes());
            paramsObj["Intent type"] = (i_reports_obj.reports()[index].reportParms.IntentType() &&
                i_reports_obj.reports()[index].reportParms.IntentType().length > 0) ? i_reports_obj.reports()[index].reportParms.IntentType() : [];
            paramsObj.Region = getIdAsArray(i_reports_obj.reports()[index].reportParms.Regions());
            return paramsObj;

        // Default.
        default:
            return {};
    }
}

function onExceptionInReport(iData, iException) {
}

function onSessionTimeOut() {
    IS_SESSION_TIMED_OUT = true;
    init();
    IS_SESSION_TIMED_OUT = false;

}