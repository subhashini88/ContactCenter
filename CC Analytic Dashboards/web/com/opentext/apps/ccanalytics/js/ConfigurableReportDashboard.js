
var tabId = 0;
var tileId = 0;
const paginationParams = [
    { name: '1 per page', value: 1 },
    { name: '10 per page', value: 10 },
    { name: '25 per page', value: 25 },
]

const filterOperators = [
    { name: 'Equal to', value: 'EQUALTO', queryOp: '=', operandDataTypes: "ALL" },
    { name: 'Not equal to', value: 'NOTEQUALTO', queryOp: '!=', operandDataTypes: "ALL" },
    { name: 'Contains', value: 'CONTAINS', queryOp: 'LIKE', operandDataTypes: "TEXT;LONGTEXT;LOOKUP;" },
    { name: 'Not contains', value: 'NOTCONTAINS', queryOp: 'NOT LIKE', operandDataTypes: "" },
    { name: 'Empty', value: 'EMPTY', queryOp: 'IS NULL', operandDataTypes: "TEXT;LONGTEXT;ENUMERATEDTEXT;LOOKUP;ENUM;DATE;DECIMAL;DURATION;NUMBER;FLOAT;INTEGER;" },
    { name: 'Not empty', value: 'NOTEMPTY', queryOp: 'IS NOT NULL', operandDataTypes: "TEXT;LONGTEXT;ENUMERATEDTEXT;LOOKUP;ENUM;DATE;DECIMAL;DURATION;NUMBER;FLOAT;INTEGER;" },
    { name: 'Less than equal', value: 'LESSTHANEQUAL', queryOp: '<=', operandDataTypes: "DATE;DECIMAL;DURATION;NUMBER;FLOAT;INTEGER;" },
    { name: 'Greater than equal', value: 'GREATERTHANEQUAL', queryOp: '>=', operandDataTypes: "DATE;DECIMAL;DURATION;NUMBER;FLOAT;INTEGER;" },
];

const dataSetGroupMap = {
    "Contract": "CONTRACT",
    "ActiveContract": "CONTRACT",
    "DraftContract": "CONTRACT",
    "TerminatedContract": "CONTRACT",
    "ExecutedContract": "CONTRACT",
    "ExpiredContract": "CONTRACT",
    "ContractObligations": "OBLIGATION",
}

const contractProperties = {};
const contractPropertiesDataType = {};

const myReports = "MYREPORTS";
const sharedReports = "SHAREDREPORTS";
const cust_key = "ctr_custom_";


//Do not change the order of status
var tileStatus = ["SAVED_UNPROCESS", "SAVED_PROCESSED", "SAVED_INPROCESS", "SAVED_ERROR", "UNSAVED"]
var tilesSavedUnProcess = "SAVED_UNPROCESS";
var tilesUnSaved = "UNSAVED";
const reportsMap = {
    "PIE": PieReportData, "BARSINGLE": BarGroupedReportData, "BARSINGLEHORIZONTAL": BarGroupedReportData, "BAR": BarGroupedReportData, "STACKEDBAR": BarGroupedReportData,
    "HORIZONTALSTACKEDBAR": BarGroupedReportData, "AREA": AreaReportData, "LINE": LineReportData,
    "SUNBURST": SunBurstReportData
};

const TYPE_DATE = "DATE";
const TYPE_NUMBER = "NUMBER";
const TYPE_TEXT = "TEXT";
const TYPE_DURATION = "TEXT";


const aggValColMap = {
    "count": [TYPE_DATE, TYPE_NUMBER, TYPE_TEXT, TYPE_DURATION],
    "sum": [TYPE_NUMBER], "countunique": [TYPE_DATE, TYPE_NUMBER, TYPE_TEXT, TYPE_DURATION],
    "min": [TYPE_NUMBER], "max": [TYPE_NUMBER], "avg": [TYPE_NUMBER]
};

const obligationDataSet = 'ContractObligations';

const oblColumns_list = [{ name: "", label: "", custom: false, type: "", disabled: true },
{ name: "compliance_status", label: "Obligation status", custom: false, type: TYPE_TEXT, disabled: false }];

const contractColumns = [
    { name: "MinStartdate", label: "Start date", custom: false, type: TYPE_DATE },
    { name: "ContractType", label: "Contract type", custom: false, type: TYPE_TEXT },
    { name: "ContractValueUSD", label: "Contract value USD", custom: false, type: TYPE_NUMBER },
    { name: "ContractValue", label: "Contract value", custom: false, type: TYPE_NUMBER },
    { name: "CurrentState", label: "Contract state", custom: false, type: TYPE_TEXT },
    { name: "InitialContractTenure", label: "Contract term", custom: false, type: TYPE_TEXT },
    { name: "Currency", label: "Currency", custom: false, type: TYPE_TEXT },
    { name: "Region", label: "Region", custom: false, type: TYPE_TEXT },
    { name: "Country", label: "Country", custom: false, type: TYPE_TEXT },
    { name: "AmendType", label: "Amend type", custom: false, type: TYPE_TEXT },
    { name: "TerminationReason", label: "Termination reason", custom: false, type: TYPE_TEXT },
    { name: "RelatedOrganization", label: "Related organization", custom: false, type: TYPE_TEXT },
    { name: "RenewalFlagStatus", label: "Renewal flag status", custom: false, type: TYPE_TEXT },
    { name: "Priority", label: "Priority", custom: false, type: TYPE_TEXT },
    { name: "IntentType", label: "Intent type", custom: false, type: TYPE_TEXT },
    { name: "AutoRenewDuration", label: "Auto renew duration", custom: false, type: TYPE_TEXT },
    { name: "RenewalDiscount", label: "Renewal discount", custom: false, type: TYPE_NUMBER },
    { name: "AutoRenew", label: "Auto renew", custom: false, type: TYPE_TEXT },
    { name: "IsExecuted", label: "Is executed", custom: false, type: TYPE_TEXT },
    { name: "Perpetual", label: "Perpetual", custom: false, type: TYPE_TEXT },
    { name: "StartDate", label: "Planned start date", custom: false, type: TYPE_DATE },
    { name: "CurrentStartDate", label: "Current term start date", custom: false, type: TYPE_DATE },
    { name: "CurrentEndDate", label: "Current term expiration date", custom: false, type: TYPE_DATE }
];


function getDataType(colName, xColVal) {
    let type = "TEXT";
    if ('compliance_status' === colName) {
        type = TYPE_TEXT;
    } else if (contractPropertiesDataType[colName]) {
        type = contractPropertiesDataType[colName];
    } else if ((null === xColVal || isNaN(xColVal))) {
        type = "DECIMAL";
    }
    return type
}


function convertValue(l_dataType, l_val) {
    if (l_dataType === "DATE") {
        l_val = moment(l_val).format("YYYY-MM-DD");

    } else if (l_dataType === "DURATION") {
        if (l_val) {
            l_val = l_val.indexOf("P") >= 0 ? l_val.split("P")[1] : l_val;
            let days = 0, mindex = 0;
            if (l_val.indexOf('M') > -1) {
                days = parseInt(l_val.substring(0, l_val.indexOf('M'))) * 30;
                mindex = l_val.indexOf('M') + 1;
            }
            if (l_val.indexOf('D') > -1) {
                days = days + parseInt(l_val.substring(mindex, l_val.indexOf('D')));
            }
            l_val = days;
        }

        if (!l_val) {
            l_val = 0;
        }
    }
    return l_val;
}

function buildExpression(xCol, xColVal, order, parentId, operand) {
    let type = getDataType(xCol, xColVal);
    type = (xCol === 'InitialContractTenure') ? 'DURATION' : type;
    let expression = {
        Id: order,
        Type: "EXPRESSION",
        Order: order,
        ParentElement: parentId,
        AttrType: "GENERAL",
        Expression: {
            OperandName: xCol,
            Operand: "=",
            OperandValue: xColVal && xColVal != 'null' ? convertValue(type, xColVal) : null,
            OperandDataType: type
        }
    };
    if (operand) {
        expression.Expression.Operand = operand;
    }
    if (!operand && ((!xColVal || xColVal == 'null') || (xCol === "compliance_status" && xColVal === "None"))) {
        expression.Expression.Operand = "IS NULL";
    }
    if (xCol && xCol.indexOf(cust_key) == 0) {
        expression.AttrType = "CUSTOM";
        expression.Expression.OperandName = xCol.split(cust_key)[1];
    }
    return expression;
}

function buildConnector(order, parentId, connector = "AND") {
    return {
        Id: order,
        Type: "CONNECTOR",
        Order: order,
        ParentElement: parentId,
        Connector: connector
    }
}

function _prepareSearchTermAndExpirationInputReq(init_data, xColVal, yColVal, reportType = "EXPIRATION") {
    var req = {
        xmlNomNode: {
            SearchQuery: { QueryElement: [] }
        }
    };
    var order = 0;
    req.xmlNomNode.SearchQuery.QueryElement.push({ Id: order, Type: "CONTAINER", Order: order, ParentElement: null });
    var parentId = order;
    req.xmlNomNode.SearchQuery.QueryElement.push(buildExpression("CurrentState", "Active", ++order, parentId));
    req.xmlNomNode.SearchQuery.QueryElement.push(buildConnector(++order, parentId));
    req.xmlNomNode.SearchQuery.QueryElement.push(buildExpression("Perpetual", "false", ++order, parentId));
    let dateName = reportType === "EXPIRATION" ? "NextExpirationDate" : "CancellationDate";
    req.xmlNomNode.SearchQuery.QueryElement.push(buildConnector(++order, parentId));
    req.xmlNomNode.SearchQuery.QueryElement.push(buildExpression(dateName, "", ++order, parentId, "IS NOT NULL"));
    req.xmlNomNode.SearchQuery.QueryElement.push(buildConnector(++order, parentId));
    req.xmlNomNode.SearchQuery.QueryElement.push(buildExpression(dateName, xColVal, ++order, parentId));
    if (yColVal) {
        req.xmlNomNode.SearchQuery.QueryElement.push(buildConnector(++order, parentId));
        req.xmlNomNode.SearchQuery.QueryElement.push(buildExpression(init_data.defaultGroupByCol(), yColVal, ++order, parentId));
    }
    if (reportType !== "EXPIRATION") {
        let contParent = order++;
        req.xmlNomNode.SearchQuery.QueryElement.push(buildConnector(++order, parentId));
        req.xmlNomNode.SearchQuery.QueryElement.push({ Id: contParent, Type: "CONTAINER", Order: contParent, ParentElement: parentId });
        req.xmlNomNode.SearchQuery.QueryElement.push(buildExpression("ContractStatus", "TERMINATION_INPROGRESS", ++order, contParent));
        req.xmlNomNode.SearchQuery.QueryElement.push(buildConnector(++order, contParent, "OR"));
        req.xmlNomNode.SearchQuery.QueryElement.push(buildExpression("ContractStatus", "TerminationCancelReview", ++order, contParent));
    }
    return req;
}


function _prepareSearchInputReq(xCol, xColVal, yCol, yColVal) {
    var req = {
        xmlNomNode: {
            SearchQuery: { QueryElement: [] }
        }
    };
    var order = 0;
    req.xmlNomNode.SearchQuery.QueryElement.push({ Id: order, Type: "CONTAINER", Order: order, ParentElement: null });
    var parentId = order;
    if (xCol) {
        req.xmlNomNode.SearchQuery.QueryElement.push(buildExpression(xCol, xColVal, ++order, parentId));
    }
    if (yCol) {
        order++;
        req.xmlNomNode.SearchQuery.QueryElement.push({
            Id: order,
            Type: "CONNECTOR",
            Order: order,
            ParentElement: parentId,
            Connector: "and"
        });
        req.xmlNomNode.SearchQuery.QueryElement.push(buildExpression(yCol, yColVal, ++order, parentId));

    }
    return req;
}


function TileNode(init_data) {
    let self = this;
    self.tileItemId = ko.observable();
    self.action = ko.observable();
    self.relatedConfigItemId = ko.observable();
    self.rawReportData = ko.observable();
    self.reportData = ko.observable();
    self.reportType = ko.observable();
    self.name = ko.observable();
    self.tileState = ko.observable(tilesSavedUnProcess);
    self.defaultChartDuration = ko.observable();
    self.defaultChartType = ko.observable();
    self.defaultGroupByCol = ko.observable();
    self.height = 0;
    self.width = 0;
    if (init_data) {
        self.relatedConfigItemId(init_data.itemId());
        self.reportData(init_data.reportData());
        self.reportType(init_data.reportType());
        self.name(init_data.name());
        if (init_data.reportGroup() === "DEFAULT") {
            self.name(init_data.shrdChartName());
            self.defaultChartDuration(init_data.defaultDuration());
            self.defaultChartType(init_data.yColumn() ? "STACKEDBAR" : "BARSINGLE");
            self.defaultGroupByCol(init_data.yColumn());
        }

        self.height = init_data.height;
        self.width = init_data.width;
    }
    self.lastupdated = ko.observable();
    self.reportConfiguration = ko.observable();
    self.tileDivId = ko.observable();
    self.isEdit = ko.observable(false);
    self.order = ko.observable(0);
    self.classList = ko.observable("flex-cn-cl-25");
    self.isMaximized = ko.observable(false);

    self.loadReportData = () => {
        var reportDataFun = reportsMap[self.reportType()];
        self.reportData(new reportDataFun(self));
    }

}

function PieReportData(init_data) {
    let self = this;
    self.data = ko.observable();
    self.layout = ko.observable();

    function _addClickEvents(divId) {
        let element = document.getElementById(divId);
        $('#' + divId).off('plotly_click');
        element.on('plotly_click', function (data) {
            _drillDownPieChart(data, divId);
        });
    }

    function _drillDownPieChart(data, divId) {
        let xColumn = getTextValue(init_data.reportConfiguration().XColumn)
        let xColumnVal = data.points[0].label;
        let req = _prepareSearchInputReq(xColumn, xColumnVal);
        getContractJSONDataTable(req, (response, status) => {
            //console.log(response);
            l_drillDownListModel.openModal(response.tuple.old, xColumn, xColumnVal);
        });
    }

    self.loadData = () => {
        let data = {
            values: [],
            labels: [],
            type: 'pie'
        }
        if (init_data.rawReportData && init_data.rawReportData()) {
            let reportRawData = JSON.parse(init_data.rawReportData());
            data.values = reportRawData.data.map(el => +el.agg);
            data.labels = reportRawData.data.map(el => _getNullStringVal(el.xcol));
        }
        self.data([data]);
        let layoutData = { title: init_data.name() };
        if (init_data.height) {
            layoutData.height = init_data.height;
        };
        if (init_data.width) {
            layoutData.width = init_data.width;
        };
        self.layout(layoutData);
    }

    self.renderReport = (divId) => {
        setTimeout(() => {
            if (document.getElementById(divId)) {
                var layout = {
                    height: 400,
                    width: 500,
                    ...self.layout()
                };
                //console.log("div :" + divId + " height:" + layout.height + " width:" + layout.width);
                Plotly.newPlot(divId, self.data(), layout, { displaylogo: false, scrollZoom: true });
                _addClickEvents(divId);
            }
        }, 100);

    }

    //inti method to load the modal data
    (function init() {
        self.loadData();
    })()

}

function SunBurstReportData(init_data) {
    let self = this;
    self.data = ko.observable();
    self.layout = ko.observable();

    function _addClickEvents(divId) {
        let element = document.getElementById(divId);
        $('#' + divId).off('plotly_click');
        element.on('plotly_click', function (data) {
            _drillDownChart(data, divId);
        });
    }

    function _drillDownChart(data, divId) {
        let xColumn = getTextValue(init_data.reportConfiguration().XColumn)
        let xColumnVal = data.points[0].label;
        let yColumn = getTextValue(init_data.reportConfiguration().YColumn)
        let yColumnVal = data.points[0].data.name;
        let req = _prepareSearchInputReq(xColumn, xColumnVal, yColumn, yColumnVal);
        getContractJSONDataTable(req, (response, status) => {
            //console.log(response);
            l_drillDownListModel.openModal(response.tuple.old, xColumn, xColumnVal, yColumn, yColumnVal);
        });
    }


    self.loadData = () => {
        var data = [];

        if (init_data.rawReportData && init_data.rawReportData()) {
            _prepareData(data, init_data.rawReportData());
        }
        self.data(data);
        let layoutData = { title: init_data.name() };
        if (init_data.height) {
            layoutData.height = init_data.height > 50 ? init_data.height - 50 : init_data.height;
        };
        if (init_data.width) {
            layoutData.width = init_data.width;
        };
        self.layout(layoutData);
    }

    function _prepareData(data, rawData) {
        let reportRawData = JSON.parse(rawData);
        let traceMap = new Map();
        let layoutData = { ids: [], labels: [], parents: [], values: [], type: 'sunburst' };
        reportRawData.data.forEach(el => {
            el.xcol = _getNullStringVal(el.xcol);
            el.ycol = _getNullStringVal(el.ycol);
            if (traceMap.has(el.xcol)) {
                traceMap.get(el.xcol).y.push(el.ycol);
                traceMap.get(el.xcol).val.push(+el.agg);
                traceMap.get(el.xcol).total += (+el.agg);
            } else {
                traceMap.set(el.xcol, { y: [], val: [], total: +el.agg });
                traceMap.get(el.xcol).y.push(el.ycol);
                traceMap.get(el.xcol).val.push(+el.agg);
            }
        }
        );
        traceMap.forEach((el, key) => {
            layoutData.ids.push(key);
            layoutData.labels.push(key);
            layoutData.parents.push("");
            layoutData.values.push(+el.total);
            el.y && el.y.forEach((ys, index) => {
                layoutData.ids.push(key + " - " + ys);
                layoutData.labels.push(ys);
                layoutData.parents.push(key);
                layoutData.values.push(+(el.val[index]));
            })
        })
        data.push(layoutData);
    }

    self.renderReport = (divId) => {
        setTimeout(() => {
            if (document.getElementById(divId)) {
                var layout = {
                    margin: { l: 0, r: 0, b: 0, t: 0 },
                    ...self.layout()
                };
                //console.log("div :" + divId + " height:" + layout.height + " width:" + layout.width);
                Plotly.newPlot(divId, self.data(), layout, { displaylogo: false });
                // _addClickEvents(divId);
            }
        }, 100);
    }

    //inti method to load the modal data
    (function init() {
        self.loadData();
    })()

}

function BarGroupedReportData(init_data) {
    let self = this;
    self.data = ko.observable();
    self.layout = ko.observable();

    function _addClickEvents(divId) {
        if (init_data.reportConfiguration() && getTextValue(init_data.reportConfiguration().ConfigType) !== "DEFAULTSQL") {
            let element = document.getElementById(divId);
            $('#' + divId).off('plotly_click');
            element.on('plotly_click', function (data) {
                _drillDownChart(data, divId);
            });
        } else if (init_data.reportConfiguration() && getTextValue(init_data.reportConfiguration().ConfigType) === "DEFAULTSQL") {
            let element = document.getElementById(divId);
            $('#' + divId).off('plotly_click');
            element.on('plotly_click', function (data) {
                _drillDownDefaultChart(data, divId);
            });
        }
    }


    function _drillDownDefaultChart(data, divId) {
        // console.log(data);
        let xColumnVal = data.points[0].label;
        let yColumnVal = data.points[0].data.name;
        let req = null;
        let type = "EXPIRATION";
        if (getTextValue(init_data.reportConfiguration().Name.text) === 'Upcoming contract terminations') {
            type = "TERMINATION";
        }
        req = _prepareSearchTermAndExpirationInputReq(init_data, xColumnVal, yColumnVal, type);
        fetchAndOpenDrillDown(req, "", "", "", "");
    }
    function _drillDownChart(data, divId) {
        let xColumn = getTextValue(init_data.reportConfiguration().XColumn)
        let xColumnVal = data.points[0].label;
        let yColumn = getTextValue(init_data.reportConfiguration().YColumn)
        let yColumnVal = data.points[0].data.name;
        let req = _prepareSearchInputReq(xColumn, xColumnVal, yColumn, yColumnVal);
        fetchAndOpenDrillDown(req, xColumn, xColumnVal, yColumn, yColumnVal);
    }

    function fetchAndOpenDrillDown(req, xColumn, xColumnVal, yColumn, yColumnVal) {
        getContractJSONDataTable(req, (response, status) => {
            l_drillDownListModel.openModal(response.tuple.old, xColumn, xColumnVal, yColumn, yColumnVal);
        });
    }


    self.loadData = () => {
        var data = [];
        //console.log(init_data.name());
        if (init_data.rawReportData && init_data.rawReportData()) {
            _prepareData(data, init_data.rawReportData());
        }
        self.data(data);
        let layoutData = { title: init_data.name() };
        if (init_data.height) {
            layoutData.height = init_data.height - 20;
        };
        if (init_data.width) {
            layoutData.width = init_data.width;
        };
        self.layout(layoutData);
    }

    function _prepareData(data, rawData) {
        let reportRawData = JSON.parse(rawData);
        let traceMap = new Map();
        if (reportRawData.data) {
            reportRawData.data.forEach(el => {
                if (init_data.reportType() === "HORIZONTALSTACKEDBAR") {
                    horizontalStackData(traceMap, el);
                } else if (init_data.reportType() === "BARSINGLE") {
                    normalSingleBarData(traceMap, el);
                } else if (init_data.reportType() === "BARSINGLEHORIZONTAL") {
                    normalSingleHBarData(traceMap, el);
                } else {
                    normalBarData(traceMap, el);
                }
            }
            );
            traceMap.forEach(el => {
                data.push(el);
            })
        }
    }

    function normalBarData(traceMap, el) {
        if (traceMap.has(_getNullStringVal(el.ycol))) {
            traceMap.get(_getNullStringVal(el.ycol)).x.push(_getNullStringVal(el.xcol));
            traceMap.get(_getNullStringVal(el.ycol)).y.push(el.agg);
        } else {
            traceMap.set(_getNullStringVal(el.ycol), { x: [], y: [], name: _getNullStringVal(el.ycol), type: 'bar' });
            traceMap.get(_getNullStringVal(el.ycol)).x.push(_getNullStringVal(el.xcol));
            traceMap.get(_getNullStringVal(el.ycol)).y.push(el.agg);
        }
    }

    function normalSingleBarData(traceMap, el, hOrientation) {
        let key = "CHART"
        if (traceMap.has(key)) {
            traceMap.get(key).x.push(_getNullStringVal(el.xcol));
            traceMap.get(key).y.push(el.agg);
        } else {
            traceMap.set(key, { x: [], y: [], type: 'bar' });
            traceMap.get(key).x.push(_getNullStringVal(el.xcol));
            traceMap.get(key).y.push(el.agg);
        }
    }

    function normalSingleHBarData(traceMap, el) {
        let key = "CHART"
        if (traceMap.has(key)) {
            traceMap.get(key).y.push(_getNullStringVal(el.xcol));
            traceMap.get(key).x.push(el.agg);
        } else {
            traceMap.set(key, { x: [], y: [], type: 'bar', orientation: 'h' });
            traceMap.get(key).y.push(_getNullStringVal(el.xcol));
            traceMap.get(key).x.push(el.agg);
        }
    }

    function horizontalStackData(traceMap, el) {
        if (traceMap.has(_getNullStringVal(el.ycol))) {
            traceMap.get(_getNullStringVal(el.ycol)).x.push(el.agg);
            traceMap.get(_getNullStringVal(el.ycol)).y.push(_getNullStringVal(el.xcol));
        } else {
            traceMap.set(_getNullStringVal(el.ycol), { x: [], y: [], name: _getNullStringVal(el.ycol), type: 'bar', orientation: 'h' });
            traceMap.get(_getNullStringVal(el.ycol)).x.push(el.agg);
            traceMap.get(_getNullStringVal(el.ycol)).y.push(_getNullStringVal(el.xcol));
        }
    }




    self.renderReport = (divId) => {
        setTimeout(() => {
            if (document.getElementById(divId)) {
                var layout = {
                    ...self.layout()
                };

                if (init_data.reportType() === "STACKEDBAR") {
                    layout.barmode = 'stack';
                } else if (init_data.reportType() === "HORIZONTALSTACKEDBAR") {
                    layout.barmode = 'group';
                }


                layout.xaxis = {
                    'showticklabels': true,
                    'tickangle': 30,
                    'exponentformat': 'si',
                    'showexponent': 'all',
                    automargin: true,
                };

                layout.yaxis = {
                    'showticklabels': true,
                    'tickangle': 0,
                    'exponentformat': 'si',
                    'showexponent': 'all',
                    automargin: true,
                }

                //console.log("div :" + divId + " height:" + layout.height + " width:" + layout.width);
                Plotly.newPlot(divId, self.data(), layout, { displaylogo: false });
                _addClickEvents(divId);
            }
        }, 100);
    }

    //inti method to load the modal data
    (function init() {
        self.loadData();
    })()

}

function AreaReportData(init_data) {
    let self = this;
    self.data = ko.observable();
    self.layout = ko.observable();

    function _addClickEvents(divId) {
        let element = document.getElementById(divId);
        $('#' + divId).off('plotly_click');
        element.on('plotly_click', function (data) {
            _drillDownChart(data, divId);
        });
    }

    function _drillDownChart(data, divId) {
        let xColumn = getTextValue(init_data.reportConfiguration().XColumn)
        let xColumnVal = data.points[0].x;
        let yColumn = getTextValue(init_data.reportConfiguration().YColumn)
        let yColumnVal = data.points[0].data.name;
        let req = _prepareSearchInputReq(xColumn, xColumnVal, yColumn, yColumnVal);
        getContractJSONDataTable(req, (response, status) => {
            //console.log(response);
            l_drillDownListModel.openModal(response.tuple.old, xColumn, xColumnVal, yColumn, yColumnVal);
        });
    }


    self.loadData = () => {
        var data = [];
        if (init_data.rawReportData && init_data.rawReportData()) {
            _prepareData(data, init_data.rawReportData());
        }
        self.data(data);
        let layoutData = { title: init_data.name() };
        if (init_data.height) {
            layoutData.height = init_data.height - 20;
        };
        if (init_data.width) {
            layoutData.width = init_data.width;
        };
        self.layout(layoutData);
    }

    function _prepareData(data, rawData) {
        let reportRawData = JSON.parse(rawData);
        let traceMap = new Map();
        reportRawData.data.forEach(el => {
            el.xcol = _getNullStringVal(el.xcol);
            el.ycol = _getNullStringVal(el.ycol);
            if (traceMap.has(el.ycol)) {
                traceMap.get(el.ycol).x.push(el.xcol);
                traceMap.get(el.ycol).y.push(el.agg);
            } else {
                traceMap.set(el.ycol, {
                    x: [], y: [], name: el.ycol, fill: 'tonexty',
                    type: 'scatter'
                });
                traceMap.get(el.ycol).x.push(el.xcol);
                traceMap.get(el.ycol).y.push(el.agg);
            }
        }
        );
        traceMap.forEach(el => {
            data.push(el);
        })
    }

    self.renderReport = (divId) => {
        setTimeout(() => {
            if (document.getElementById(divId)) {
                var layout = {
                    barmode: 'group',
                    ...self.layout()
                };


                layout.xaxis = {
                    'showticklabels': true,
                    'tickangle': 30,
                    'exponentformat': 'si',
                    'showexponent': 'all',
                    automargin: true,
                };

                layout.yaxis = {
                    'showticklabels': true,
                    'tickangle': 0,
                    'exponentformat': 'si',
                    'showexponent': 'all',
                    automargin: true,
                }

                //console.log("div :" + divId + " height:" + layout.height + " width:" + layout.width);
                Plotly.newPlot(divId, self.data(), layout, { displaylogo: false });
                _addClickEvents(divId);
            }
        }, 100);
    }

    //inti method to load the modal data
    (function init() {
        self.loadData();
    })()

}

function LineReportData(init_data) {
    let self = this;
    self.data = ko.observable();
    self.layout = ko.observable();

    function _addClickEvents(divId) {
        let element = document.getElementById(divId);
        $('#' + divId).off('plotly_click');
        element.on('plotly_click', function (data) {
            _drillDownChart(data, divId);
        });
    }

    function _drillDownChart(data, divId) {
        let xColumn = getTextValue(init_data.reportConfiguration().XColumn)
        let xColumnVal = data.points[0].x;
        let yColumn = getTextValue(init_data.reportConfiguration().YColumn)
        let yColumnVal = data.points[0].data.name;
        let req = _prepareSearchInputReq(xColumn, xColumnVal, yColumn, yColumnVal);
        getContractJSONDataTable(req, (response, status) => {
            //console.log(response);
            l_drillDownListModel.openModal(response.tuple.old, xColumn, xColumnVal, yColumn, yColumnVal);
        });
    }


    self.loadData = () => {
        var data = [];
        if (init_data.rawReportData && init_data.rawReportData()) {
            _prepareData(data, init_data.rawReportData());
        }
        self.data(data);
        let layoutData = { title: init_data.name() };
        if (init_data.height) {
            layoutData.height = init_data.height - 20;
        };
        if (init_data.width) {
            layoutData.width = init_data.width;
        };
        self.layout(layoutData);
    }

    function _prepareData(data, rawData) {
        let reportRawData = JSON.parse(rawData);
        let traceMap = new Map();
        reportRawData.data.forEach(el => {
            el.xcol = _getNullStringVal(el.xcol);
            el.ycol = _getNullStringVal(el.ycol);
            if (traceMap.has(el.ycol)) {
                traceMap.get(el.ycol).x.push(el.xcol);
                traceMap.get(el.ycol).y.push(el.agg);
            } else {
                traceMap.set(el.ycol, { x: [], y: [], name: el.ycol, type: 'scatter' });
                traceMap.get(el.ycol).x.push(el.xcol);
                traceMap.get(el.ycol).y.push(el.agg);
            }
        }
        );
        traceMap.forEach(el => {
            data.push(el);
        })
    }

    self.renderReport = (divId) => {
        setTimeout(() => {
            if (document.getElementById(divId)) {
                var layout = {
                    barmode: 'group',
                    ...self.layout()
                };


                layout.xaxis = {
                    'showticklabels': true,
                    'tickangle': 30,
                    'exponentformat': 'si',
                    'showexponent': 'all',
                    automargin: true,
                };

                layout.yaxis = {
                    'showticklabels': true,
                    'tickangle': 0,
                    'exponentformat': 'si',
                    'showexponent': 'all',
                    automargin: true,
                }

                //console.log("div :" + divId + " height:" + layout.height + " width:" + layout.width);
                Plotly.newPlot(divId, self.data(), layout, { displaylogo: false });
                _addClickEvents(divId);
            }
        }, 100);
    }

    //inti method to load the modal data
    (function init() {
        self.loadData();
    })()

}

function SavedReport(data) {
    let self = this;
    self.selected = ko.observable(false);
    self.isNew = ko.observable(data.isNew);
    self.reportData = ko.observable();

    self.id = data["CCUserSavedReports-id"] ? ko.observable(data["CCUserSavedReports-id"].Id) : ko.observable("");
    self.itemId = data["CCUserSavedReports-id"] ? ko.observable(data["CCUserSavedReports-id"].ItemId) : ko.observable("");
    self.name = ko.observable(data.Name);
    self.shrdChartName = ko.observable(data.Name);
    self.dataSet = ko.observable(data.DataSet);
    self.oblColumns = ko.observableArray(oblColumns_list);

    self.xLabel = ko.observable('X Axis');
    self.yLabel = ko.observable('Group by');

    self.reportType = ko.observable();

    //SHARED/MYREPORTS/DEFAULT
    self.reportGroup = ko.observable();


    self.xColumn = ko.observable(data.XColumn);
    self.yColumn = ko.observable(data.YColumn);
    self.defaultDuration = ko.observable("WEEK");
    self.aggregator = ko.observable(data.Aggregator);
    self.aggregatorColumn = ko.observable(data.AggregatorColumn);

    self.reportType.subscribe(function (newVal) {
        //console.log(newVal);
        _changeLabelOfColumns(newVal);
    });

    if (data.ChartType) {
        self.reportType(data.ChartType);
    }

    if (data.reportGroup) {
        self.reportGroup(data.reportGroup)
    }


    self.aggColumns = ko.observableArray([]);

    self.aggregator.subscribe(function (newVal) {
        self.aggColumns.removeAll();
        self.aggregatorColumn("");
        self.aggColumns.push({ name: "", label: "", custom: false, type: "", disabled: true });
        self.aggColumns.push({ name: "General attributes", label: "General attributes", custom: false, type: "", disabled: true });
        contractColumns.forEach(ctr => {
            if (!newVal || aggValColMap[newVal].indexOf(ctr.type) >= 0) {
                self.aggColumns.push({ name: ctr.name, label: ctr.label, custom: false, type: ctr.type });
            }
        });
        self.aggColumns.push({ name: "", label: "", custom: false, type: "", disabled: 'true' });
        self.aggColumns.push({ name: "Custom attributes", label: "Custom attributes", custom: false, type: "", disabled: 'true' });
        l_configurableReportModel.custFieldList().forEach(cust => {
            if (!newVal || aggValColMap[newVal].indexOf(cust.fieldType()) >= 0) {
                self.aggColumns.push({ name: cust_key + cust.name(), label: "" + cust.displayName(), custom: true, type: cust.fieldType() });
            }
        });
    });

    self.dataSet.subscribe(function (newVal) {
        console.log("data set changed" + newVal)
        if (newVal === obligationDataSet) {
            self.xColumn('');
        }
    });

    function _populateAllAggCols() {
        self.aggColumns.removeAll();
        self.aggColumns.push({ name: "", label: "", custom: false, type: "", disabled: true });
        self.aggColumns.push({ name: "General attributes", label: "General attributes", custom: false, type: "", disabled: true });
        contractColumns.forEach(ctr => {
            self.aggColumns.push({ name: ctr.name, label: ctr.label, custom: false, type: ctr.type });
        });
        self.aggColumns.push({ name: "", label: "", custom: false, type: "", disabled: 'true' });
        self.aggColumns.push({ name: "Custom attributes", label: "Custom attributes", custom: false, type: "", disabled: 'true' });
        l_configurableReportModel.custFieldList().forEach(cust => {
            self.aggColumns.push({ name: cust_key + cust.name(), label: "" + cust.displayName(), custom: true, type: cust.fieldType() });
        });
    }

    function _changeLabelOfColumns(newVal) {
        self.xLabel('X Axis');
        self.yLabel('Group by column');
        if (newVal === "HORIZONTALSTACKEDBAR") {
            self.xLabel('Y Axis');
        } else if (newVal === "BARSINGLEHORIZONTAL") {
            self.xLabel('Y Axis');
        } else if (newVal === "SUNBURST") {
            self.xLabel('Hierarchical level 1');
            self.yLabel('Hierarchical level 2');
        } else if (newVal === "LINE" || newVal === "AREA") {
            self.xLabel('X Axis');
            self.yLabel('Line variable column');
        }
    }


    self.renderChart = () => {
        if (self.reportType()) {
            let reportFunc = reportsMap[self.reportType()];
            _clearDiv();
            (new reportFunc({ name: self.name })).renderReport("output_preview");
        }
    }


    function _loadReportData() {
        if (self.reportType()) {
            var reportDataFun = reportsMap[self.reportType()];
            self.reportData(new reportDataFun(self));
        }
    }

    (function init() {
        _populateAllAggCols();
        _loadReportData();
    })()

}

function SelectReportModal(init_data) {
    let self = this;
    self.myReportSelected = ko.observable(true);
    self.activeTab = ko.observable("MYREPORTS");
    self.myReports = ko.observableArray([]);
    self.sharedReports = ko.observableArray([]);


    self.tileEdit = ko.observable();
    self.tileData = ko.observable();

    self.reportName = ko.observable();
    self.chartSelected = ko.observable();
    self.listSelected = ko.observable();
    self.xAxisSelected = ko.observable();
    self.yAxisSelected = ko.observable();
    self.aggregSelected = ko.observable();
    self.aggregatorColSelected = ko.observable();
    self.contractColumns = ko.observableArray([]);
    self.contractDefChartColumns = ko.observableArray([]);
    _addCustCols();
    self.selectedReport = ko.observable();
    self.isNewReport = ko.observable(false);
    self.isSavedChart = ko.observable(false);

    if (init_data) {
        self.tileEdit(init_data.tileEdit ? true : false);
        if (init_data.tileData) {
            self.tileData(init_data.tileData);
        }
    }

    self.activeTab.subscribe(function (newVal) {
        self.loadInitalReportsData(newVal);
    });

    function _addCustCols() {
        self.contractColumns.removeAll();
        self.contractColumns.push({ name: "", label: "", custom: false, type: "", disabled: true });
        self.contractColumns.push({ name: "General attributes", label: "General attributes", custom: false, type: "", disabled: true });
        contractColumns.forEach(ctr => {
            self.contractColumns.push({ name: ctr.name, label: ctr.label, custom: false, type: ctr.type });
        });
        self.contractColumns.push({ name: "", label: "", custom: false, type: "", disabled: 'true' });
        self.contractColumns.push({ name: "Custom attributes", label: "Custom attributes", custom: false, type: "", disabled: 'true' });
        l_configurableReportModel.custFieldList().forEach(cust => {
            self.contractColumns.push({ name: cust_key + cust.name(), label: "" + cust.displayName(), custom: true, type: cust.fieldType() });
        });


        self.contractDefChartColumns.removeAll();
        self.contractDefChartColumns.push({ name: "", label: "", custom: false, type: "", disabled: true });
        self.contractDefChartColumns.push({ name: "General attributes", label: "General attributes", custom: false, type: "", disabled: true });
        contractColumns.forEach(ctr => {
            self.contractDefChartColumns.push({ name: ctr.name, label: ctr.label, custom: false, type: ctr.type });
        });
    }

    self.loadInitalReportsData = (activeTab) => {
        _clearCreateReportForm();
        if (activeTab === "MYREPORTS") {
            _loadMyReports();
        } else {
            _loadSharedReports();
        }
    }

    self.selectSavedReport = (data) => {
        self.myReports().forEach(report => {
            report.selected(false);
        })
        data.selected(true);
        if (!data.isNew()) {
            self.isSavedChart(true);
            // data.reportData().renderReport("model_root_layout1");
        }
        self.isNewReport(false);

        self.selectedReport(data);
        _clearDiv();
    }

    self.selectSharedReport = (data) => {
        //console.log("cancel save chart");
        self.sharedReports().forEach(report => {
            report.selected(false);
        })
        data.selected(true);
        self.isSavedChart(true);
        self.isNewReport(false);
        self.selectedReport(data);
        _clearDiv();
    }

    self.renderChart = () => {
        if (self.chartSelected()) {
            let reportFunc = reportsMap[self.chartSelected()];
            _clearDiv();
            (new reportFunc({ name: self.reportName })).renderReport("output_preview");
        }
    }

    self.cancelNewChart = (iItem) => {
        //console.log("cancel save chart");
        self.isNewReport(false);
        _clearCreateReportForm();
        _loadMyReports();

    }
    self.saveChart = (iItem) => {
        if (_validateSavedReportForm(iItem)) {
            saveChartService(iItem, function (status) {

                if (status == SUCCESS) {
                    self.isSavedChart(true);
                    successToast(3000, getTranslationMessage("Configuration saved."));
                }
            });
        } else {
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("Mandatory fields are missing."), 10000);
        }
    }

    function _validateSavedReportForm(data) {
        let validForm = true;
        if (!data.name() || !data.dataSet() ||
            !data.reportType() || !data.xColumn() || !data.aggregator() ||
            !data.aggregatorColumn() ||
            (["PIE", "BARSINGLE", "BARSINGLEHORIZONTAL"].indexOf(data.reportType()) < 0 && !data.yColumn())) {
            validForm = false;
        }
        return validForm;
    }

    self.deleteChart = (iItem) => {
        if (!iItem.isNew()) {
            deleteChartService(iItem, function (resp, status) {
                if (resp.Response && resp.Response.text && resp.Response.text.indexOf("ERROR") > 0) {
                    showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("Unable to delete, Configuration is used in reports."), 10000);
                } else if (status == SUCCESS) {
                    self.isSavedChart(true);
                    successToast(3000, getTranslationMessage("Deleted successfully."));
                    _loadMyReports();
                    _clearCreateReportForm();
                }
            });
        } else {
            _clearCreateReportForm();
            _loadMyReports();
        }
    }

    function _clearCreateReportForm() {
        self.reportName("");
        self.isSavedChart(false);
        self.chartSelected("");
        self.listSelected("");
        self.xAxisSelected("");
        self.yAxisSelected("");
        self.aggregSelected("");
        self.aggregatorColSelected("");
        self.isNewReport(false);
        self.selectedReport(null);
    }

    self.selectSubPage = (tab) => {
        self.activeTab(tab);
    }

    self.newMyReport = () => {
        self.activeTab(tab);
    }

    self.createNewReport = () => {
        self.isNewReport(true);
        let reportData = { "Name": "Untitled", chart: self.chartSelected(), isNew: true };
        let saveRep = new SavedReport(reportData);
        self.myReports.push(saveRep);
        self.selectSavedReport(saveRep);
    }

    self.saveNewReport = () => {
        self.isNewReport(false);
        _loadMyReports();
    }

    self.addReportToDashboard = () => {
        if (self.activeTab() === myReports) {
            let length = l_configurableReportModel.selectedTab().tiles().length;
            var reportSelected = self.myReports().find(report => report.selected());
            $('#div_editLayoutTileModal').modal('hide');
            if (reportSelected) {
                _addSelectedReport(reportSelected, length);
            }
        } else if (self.activeTab() === sharedReports) {
            let length = l_configurableReportModel.selectedTab().tiles().length;
            var reportSelected = self.sharedReports().find(report => report.selected());
            if (reportSelected && validateShareAddReport(reportSelected)) {
                $('#div_editLayoutTileModal').modal('hide');
                if (reportSelected.itemId()) {
                    _addSelectedReport(reportSelected, length);
                } else {
                    saveUserReportsService(_prepareSaveReporReq(reportSelected), (resp, status) => {
                        if (resp.CCUserSavedReports) {
                            reportSelected.id(resp.CCUserSavedReports["CCUserSavedReports-id"].Id);
                            reportSelected.itemId(resp.CCUserSavedReports["CCUserSavedReports-id"].ItemId);
                            _addSelectedReport(reportSelected, length);
                        }
                    });
                }
            }

        }
    }

    function validateShareAddReport(reportSelected) {
        let isValid = true;
        if (!reportSelected.shrdChartName()) {
            isValid = false;
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("Mandatory fields are missing."), 10000);
        }
        return isValid;
    }


    function _prepareSaveReporReq(savedRep) {
        return { "CCUserSavedReports-create": { Name: savedRep.name(), ChartType: "BARSINGLE", ConfigType: "DEFAULTSQL", Aggregator: "SUM", DataSet: "ALLCONTRACTS" } };
    }

    function _addSelectedReport(reportSelected, length) {
        if (reportSelected) {
            let tile = new TileNode(reportSelected);
            tile.tileState(tilesUnSaved);
            if (!self.tileEdit()) {
                tile.tileDivId("NEW_Tile_ID_" + tileId++);
                length++;
                l_configurableReportModel.selectedTab().isEdit(true);
                l_configurableReportModel.selectedTab().tiles.push(tile);
                l_configurableReportModel.selectedTab().reDrawDashboard();
            } else {
                tile.tileDivId(self.tileData().tileDivId());
                var l_targetIndex = l_configurableReportModel.selectedTab().tiles().indexOf(self.tileData());
                l_configurableReportModel.selectedTab().tiles.remove(self.tileData());
                l_configurableReportModel.selectedTab().addToDeleteList(self.tileData());
                l_configurableReportModel.selectedTab().tiles.splice(l_targetIndex, 0, tile);
                l_configurableReportModel.selectedTab().reDrawDashboard();
            }
        }
    }

    function _loadMyReports() {
        self.myReports.removeAll();
        loadUserChartsService(function (charts, status) {
            if (status == SUCCESS) {
                if (!Array.isArray(charts)) {
                    charts = new Array(charts);
                }
                charts.forEach(chart => self.myReports.push(new SavedReport(chart)));
                _checkAndSelectEditChart();
            }
        });
    }

    function _checkAndSelectEditChart() {
        if (self.tileEdit() && self.tileData().reportConfiguration() &&
            self.tileData().reportConfiguration()["CCUserSavedReports-id"].Id &&
            self.tileData().reportConfiguration()["CCUserSavedReports-id"].Id.indexOf("NEW") < 0) {
            let selected = self.myReports().find(report => (report.id() ===
                self.tileData().reportConfiguration()["CCUserSavedReports-id"].Id));
            if (selected) {
                self.selectSavedReport(selected)
            }
        }
    }

    function _loadSharedReports() {
        //console.log("My Reports loaded");
        self.sharedReports.removeAll();
        let sharedReports = ["Upcoming contract expirations", "Upcoming contract terminations"];
        sharedReports.forEach(repName => {
            self.sharedReports.push(new SavedReport({ Name: repName, ChartType: "BARSINGLE", reportGroup: "DEFAULT" }));
        });
        getDefaultReportsService({}, (res, status) => {
            if (res.CCUserSavedReports && Array.isArray(res.CCUserSavedReports)) {
                res.CCUserSavedReports.forEach(savedRep => {
                    _populateSavedRep(savedRep);
                });
            } else if (res.CCUserSavedReports) {
                _populateSavedRep(res.CCUserSavedReports);
            }
        });
    }

    function _populateSavedRep(savedRep) {
        self.sharedReports().forEach(rep => {
            if (savedRep.Name === rep.name()) {
                rep.id(savedRep["CCUserSavedReports-id"].Id);
                rep.itemId(savedRep["CCUserSavedReports-id"].ItemId);
            }
        });
    }

    //initial load of modal, will load the data of reports on load.
    (function init() {
        self.loadInitalReportsData(self.activeTab());
    })();


}

function TabNode() {
    let self = this;
    self.dashboardId = ko.observable();
    self.tabId1 = ko.observable();
    self.tabItemId = ko.observable();
    self.tabItemId1 = ko.observable();
    self.name = ko.observable();
    self.action = ko.observable();
    self.order = ko.observable(0);
    self.display = ko.observable(false);
    self.modalReportName = ko.observable();
    self.isTilesLoaded = ko.observable(false);
    self.tiles = ko.observableArray([]);
    self.showOptions = ko.observable(false);
    self.sourceRuleIndex = -1;
    self.targetRuleIndex = -1;
    self.drillDownlist = ko.observableArray([]);
    self.tobeDeletedTileName = ko.observable();
    self.deletedTiles = ko.observableArray([]);
    self.enableAddReportButton = ko.observable(true);
    self.isEdit = ko.observable(false);
    self.selectReportModal = ko.observable();
    self.visibleButtonOptions = ko.observable(false);

    self.openButtonOptions = function () {
        self.visibleButtonOptions(!self.visibleButtonOptions());
    };

    self.isEdit.subscribe((newVal) => {
        if (newVal) {
            self.visibleButtonOptions(false);
        }
    });

    self.cancelChanges = () => {
        self.isEdit(!self.isEdit());
        l_configurableReportModel.refreshReport();
        _closeOptions();
    }

    self.tabNameEdit = (data) => {
        self.isEdit(true);
    }


    self.reDrawDashboard = () => {
        if (self.tiles().length < 5) {
            self.enableAddReportButton(true);
        } else {
            self.enableAddReportButton(false);
        }
        _realalignAllTiles();
    }

    self.display.subscribe(function (newVal) {
        if (newVal) {
            // self.reDrawDashboard();
        }
    });

    self.toggleEdit = (iItem) => {
        if (self.isEdit()) {

            saveReportService(iItem, function (status) {
                if (status == SUCCESS) {
                    self.isEdit(false);
                    _closeOptions();
                }
            });
        } else {
            self.isEdit(true);
        }
        self.toggleOptions();
    }



    self.refreshDashboard = (data) => {
        rereshTabDashboardService({ "TabId": data.dashboardId(), "TabId1": data.tabId1() }, function (data, status) {
            if (status == SUCCESS) {
                self.isEdit(false);
                l_configurableReportModel.refreshReport();
                successToastDiv("successToastMain", getTranslationMessage("Report refreshed."), 3000);
            }
        });
        _closeOptions();
    }


    self.saveDashBoard = (iItem) => {
        if (self.isEdit() && _validationSaveTab()) {
            let unsavedDashboard = _addSaveReportWarning(iItem);
            if (!unsavedDashboard) {
                let req = _prepareSaveDashboardReq(iItem);
                _saveReportDashBoard(req);
            } else {
                $("#id_CofirmSaveDashboard").modal({
                    backdrop: 'static',
                    keyboard: false
                });
                $('button#confirmYes').off("click");
                $('button#confirmYes').on('click', function (_event) {
                    // console.log("Saved successfully");
                    setTimeout(() => {
                        let req = _prepareSaveDashboardReq(iItem);
                        _saveReportDashBoard(req);
                    }, 500)

                });
            }

        } else {
            showOrHideErrorInfo("div_mainErrorInfoArea", true, getTranslationMessage("Dashboard name is empty."), 10000);
        }
        _closeOptions();
    }

    self.saveAndRunDashBoard = (iItem) => {
        if (self.isEdit() && _validationSaveTab()) {
            let unsavedDashboard = _addSaveReportWarning(iItem);
            if (!unsavedDashboard) {
                let req = _prepareSaveDashboardReq(iItem);
                req.runReports = 'true';
                _saveReportDashBoard(req);
            } else {
                $("#id_CofirmSaveDashboard").modal({
                    backdrop: 'static',
                    keyboard: false
                });
                $('button#confirmYes').off("click");
                $('button#confirmYes').on('click', function (_event) {
                    // console.log("Saved successfully");
                    setTimeout(() => {
                        let req = _prepareSaveDashboardReq(iItem);
                        req.runReports = 'true';
                        _saveReportDashBoard(req);
                    }, 500)

                });
            }

        } else {
            showOrHideErrorInfo("div_mainErrorInfoArea", true, getTranslationMessage("Dashboard name is empty."), 10000);
        }
        _closeOptions();
    }


    function _addSaveReportWarning(iItem) {
        let unsavedDashboard = l_configurableReportModel.allReportsTabs().find(ele => ele != iItem && !ele.tabItemId1());
        if (unsavedDashboard) {
            $("#id_SaveReport_warning").removeClass("hidden");
            $("#id_SaveReport_warning").text(getTranslationMessage("Unsaved dashboards will be permanently deleted and cannot be restored."));
        }
        return unsavedDashboard;
    }

    function _validationSaveTab() {
        let valid = true;
        if (!self.name()) {
            valid = false;
        }
        return valid;
    }

    function _saveReportDashBoard(req) {
        //console.log(req);
        createOrUpdateReport(req, function (data, status) {
            if (status == SUCCESS) {
                self.isEdit(false);
                l_configurableReportModel.refreshReport();
                successToastDiv("successToastMain", getTranslationMessage("Report saved."), 3000);
            }
        });
    }

    function _prepareSaveDashboardReq(tab) {
        let req = { runReports: 'false', AllReportNodes: {} };
        if (self.deletedTiles().length) {
            req.AllReportNodes.DeletedReportNodes = [];
            self.deletedTiles().forEach(tile => req.AllReportNodes.DeletedReportNodes.push({ "ReportNode": { "ItemId1": tile.tileItemId() } }))
        }

        req.Tab = {
            Name: self.name(),
            Order: self.order(),
            Id1: self.tabId1(),
            Id: self.dashboardId(),
            ItemId: self.tabItemId(),
        };

        if (self.tiles().length) {
            req.AllReportNodes.ReportNodes = { ReportNode: [] };
            self.tiles().forEach(tile => {
                let reportNode = {};
                reportNode[ACTION] = tile.action();
                if (!tile.tileItemId()) {
                    reportNode[ACTION] = CRT_TILE;
                } else if (tile.isEdit()) {
                    reportNode[ACTION] = UPD_TILE;
                }
                reportNode[TYPE] = TILE_TYPE;
                reportNode["ItemId1"] = tile.tileItemId();
                reportNode["Id"] = self.dashboardId();
                reportNode["Order"] = tile.order();
                reportNode["Name"] = tile.name();
                reportNode["SourceReportNodeItemId"] = self.tabItemId();
                reportNode["RelatedConfigItemId"] = tile.relatedConfigItemId();
                reportNode["DefaultChartDuration"] = tile.defaultChartDuration();
                reportNode["DefaultChartType"] = tile.defaultChartType();
                reportNode["DefaultGroupByCol"] = tile.defaultGroupByCol();
                req.AllReportNodes.ReportNodes.ReportNode.push(reportNode);
            });
        }
        return req;
    }


    self.deleteLayoutTile = (tileNode, event) => {
        self.tobeDeletedTileName(tileNode.name());
        $("#id_DeleteTile").modal({
            backdrop: 'static',
            keyboard: false
        });
        $('button#confirmYes').off("click");
        $('button#confirmYes').on('click', function (_event) {
            self.tiles.remove(tileNode);
            _updateTilesActionAndOrder();
            self.addToDeleteList(tileNode);
            self.reDrawDashboard();
        });
    }

    self.addToDeleteList = (tileNode) => {
        if (tileNode.tileItemId()) {
            self.deletedTiles.push(tileNode);
        }
    }

    function _updateTilesActionAndOrder() {
        self.tiles().forEach(tile => {
            tile.action(UPD_TILE);
        });
    }

    self.maximiseLayout = (tile, event) => {
        self.tiles().forEach(ele => ele.classList("hidden"));
        tile.classList("flex-cn-cl-100-t1");
        tile.isMaximized(true);
        _adjustAllTilesHeightAndWidth();
        self.reDrawAllTiles();
    }

    self.minimiseLayout = (tile, event) => {
        self.reDrawDashboard();
    }

    self.reloadUserReport = (tile, event) => {
        readRelatedReportService({ 'RelatedReportNode-id': { 'ItemId1': tile.tileItemId() } }, function (data, status) {
            //console.log(data);
            if (data.RelatedReportNode && data.RelatedReportNode.ProcessedReportData) {
                _updateTileDataAndDraw(tile, data.RelatedReportNode.ProcessedReportData);
            }
        });
    }

    self.refreshUserReport = (tile, event) => {
        //console.log(tile);
        tile.tileState(tileStatus[2]);
        refreshUserReport({ itemId1: tile.tileItemId() }, (resp, status) => {
            // self.reloadUserReport(tile);
            l_configurableReportModel.refreshReport();
            // console.log(resp)
            // if (resp.tuple.old.processUserReport && resp.tuple.old.processUserReport.processUserReport) {
            //     _updateTileDataAndDraw(tile, (resp.tuple.old.processUserReport.processUserReport));
            // }
        })
    }

    function _updateTileDataAndDraw(tile, processedData) {
        tile.rawReportData(processedData);
        tile.loadReportData();
        tile.tileState(tileStatus[1]);
        tile.reportData() ? tile.reportData().renderReport(tile.tileDivId()) : null;
    }

    self.editLayoutTile = (tileNode, event) => {
        //console.log(tileNode)
        self.selectReportModal(new SelectReportModal({ tileEdit: true, tileData: tileNode }));
        $("#div_editLayoutTileModal").modal({
            backdrop: 'static',
            keyboard: false
        });
    }


    self.toggleOptions = () => {
        self.showOptions(!self.showOptions());
    }

    function _closeOptions() {
        self.showOptions(false);
    }

    self.editReportName = () => {
        self.modalReportName(self.name());
        $("#id_changeReportName").modal({
            backdrop: 'static',
            keyboard: false
        });
        $('button#confirmYes').off("click");
        $('button#confirmYes').on('click', function (_event) {
            self.name(self.modalReportName());
        });
        $('button#confirmNo').on('click', function (_event) {
            self.modalReportName(self.name());
        });
        self.showOptions(false);
    }

    self.deleteReport = (iItem) => {
        self.modalReportName(self.name());
        let unsavedTabs = l_configurableReportModel.allReportsTabs().find(ele => ele != iItem && !ele.tabItemId1());

        $("#id_DeleteReport").modal({
            backdrop: 'static',
            keyboard: false
        });
        $("#id_DeleteReport_warning").text("");
        $("#id_DeleteReport_warning").addClass("hidden");
        if (unsavedTabs) {
            $("#id_DeleteReport_warning").removeClass("hidden");
            $("#id_DeleteReport_warning").text(getTranslationMessage("Unsaved dashboards will be permanently deleted and cannot be restored."));

        }
        $('button#confirmYes').off("click");
        $('button#confirmYes').on('click', function (_event) {
            if (iItem.tabItemId1()) {
                deleteTabService(iItem, function (status) {
                    if (status == SUCCESS) {
                        self.showOptions(false);

                    }
                    _refreshDashboard();
                });
            } else {
                self.showOptions(false);
                _refreshDashboard();
            }

        });
        _closeOptions();
    }

    function _refreshDashboard() {
        setTimeout(() => {
            loadAllTabsForUserService(function (resp, status) {
                if (status == SUCCESS) {
                    l_configurableReportModel.allReportsTabs.removeAll();
                    if (resp && resp.Response && resp.Response.GetRelatedReportNodeByTypeResponse && resp.Response.GetRelatedReportNodeByTypeResponse.RelatedReportNode)
                        l_configurableReportModel.loadAllTabsForUsers(resp.Response.GetRelatedReportNodeByTypeResponse.RelatedReportNode);
                    l_configurableReportModel.selectedTab(null);
                }
            })
        }, 200);
    }

    self.addReportTile = () => {
        if (self.enableAddReportButton()) {
            self.selectReportModal(new SelectReportModal());
            $("#div_editLayoutTileModal").modal({
                backdrop: 'static',
                keyboard: false
            });
            _closeOptions();
        }
    }

    function _realalignAllTiles() {
        let length = self.tiles().length;
        self.tiles().forEach((tile, index) => {
            tile.order(index);
            _reAlignTile(tile, index, length);
        });
        _adjustAllTilesHeightAndWidth();
        self.reDrawAllTiles();
    }

    function _adjustAllTilesHeightAndWidth() {
        self.tiles().forEach((tile, index) => {
            if (document.getElementById(tile.tileDivId())) {
                tile.height = ($("#" + tile.tileDivId()).parent().parent().parent()[0].clientHeight);
                tile.width = (document.getElementById(tile.tileDivId()).clientWidth);
                tile.loadReportData();
            }
        });
    }

    function sortOnOrder(l_ele1, l_ele2) {
        return l_ele1.order() - l_ele2.order();
    }

    function _checkAndAddTileStatus(tileNode) {
        if (tileNode.rawReportData()) {
            tileNode.tileState(tileStatus[1]);
        } else if ((!tileNode.rawReportData())) {
            tileNode.tileState(tileStatus[0]);
        }
    }

    self.refreshTiles = () => {
        function populateTileNode(relatedReportNodes) {
            relatedReportNodes.forEach(tile => {
                let length = relatedReportNodes.length;
                let tileNode = new TileNode();
                tileNode.name(getTextValue(tile.RelatedConfig.Name));
                tileNode.tileItemId(tile["RelatedReportNode-id"].ItemId1);
                tileNode.tileDivId("tile_id" + index);;
                tileNode.order(tile.Order);
                tileNode.rawReportData(tile.ProcessedReportData);
                tileNode.reportType(getTextValue(tile.RelatedConfig.ChartType));
                if (tile.RelatedConfig.ConfigType && getTextValue(tile.RelatedConfig.ConfigType) === "DEFAULTSQL") {
                    tileNode.name(getTextValue(tile.Name));
                    tileNode.reportType(getTextValue(tile.DefaultChartType));
                    tileNode.defaultChartDuration(tile.DefaultChartDuration);
                    tileNode.defaultChartType(tile.DefaultChartType);
                    tileNode.defaultGroupByCol(tile.DefaultGroupByCol);
                }
                tileNode.reportConfiguration(tile.RelatedConfig);
                tileNode.lastupdated(tile.Tracking.LastModifiedDate ? new Date(tile.Tracking.LastModifiedDate) : "");
                tileNode.relatedConfigItemId(tile.RelatedConfig["CCUserSavedReports-id"].ItemId);
                _checkAndAddTileStatus(tileNode)
                self.tiles.push(tileNode);
                _reAlignTile(tileNode, index, length);
                index++;
            });
            self.tiles.sort(sortOnOrder);
        }

        if (!self.tabId1()) {
            return;
        }
        self.tiles.removeAll();
        let index = 0;
        loadTilesInTabService({ TabId: self.dashboardId(), TabId1: self.tabId1() }, function (resp, status) {
            if (status == SUCCESS) {
                let relatedReportNodes = resp.RelatedReportNode;
                if (relatedReportNodes) {
                    if (!Array.isArray(relatedReportNodes)) {
                        relatedReportNodes = new Array(relatedReportNodes);
                    }
                    populateTileNode(relatedReportNodes);
                    self.reDrawDashboard();
                    self.isTilesLoaded(true);
                }
            }
        });
    }

    self.realignAllTiles = () => {
        let length = self.tiles().length;
        for (let index = 0; index < length; index++) {
            _reAlignTile(self.tiles()[index], index, length);
        }
        // for (let index = 0; index < length; index++) {
        //     _reRenderTile(self.tiles()[index]);
        // }
    }

    self.reDrawAllTiles = () => {
        self.tiles().forEach((tile, index) => {
            tile.reportData() ? tile.reportData().renderReport(tile.tileDivId()) : null;
        })
    }

    function _reAlignTile(tile, index, length) {
        tile.isMaximized(false);
        if (length >= 3) {// all length > 2 tile
            if (index === 0 && length < 5) { // index 0 of < 5 tile length
                tile.classList("flex-cn-cl-100")
            } else if (index === 0 && length > 4) {// index 0 > 4 tile length
                tile.classList("flex-cn-cl-66")
            }
            else if (length === 3) {// all 3 length remaing >0 index
                tile.classList("flex-cn-cl-50")
            } else if (length === 4) {// all 4 length remaining >0 index
                tile.classList("flex-cn-cl-33")
            } else if (length === 5 && index === 1) { //index 1 of 5 length tiles
                tile.classList("flex-cn-cl-33-t5i1")
            }
            else if (length === 5) { // all remaining 5 length tile
                tile.classList("flex-cn-cl-33")
            }
        } else if (length == 2) {// all two length tile
            tile.classList("flex-cn-cl-50-t2")
        } else { // 1 tile 
            tile.classList("flex-cn-cl-100-t1")
        }
    }


    self.rowDrag = function (data, event, index) {
        self.sourceRuleIndex = index;
    }
    self.rowDrop = function (data, event, index) {
        var l_targetIndex = index;
        var l_sourceIndex = self.sourceRuleIndex;
        if (self.sourceRuleIndex !== -1 && (l_targetIndex !== l_sourceIndex)) {
            var l_startIndex = (l_targetIndex > l_sourceIndex) ? l_sourceIndex : l_targetIndex;
            var l_endIndex = (l_targetIndex < l_sourceIndex) ? l_sourceIndex : l_targetIndex;
            var ruleOrderMap = {};
            for (var i = l_startIndex; i <= l_endIndex; i++) {
                ruleOrderMap[i] = self.tiles()[i].Order;
            }
            var l_draggedRuleData = self.tiles()[l_sourceIndex];
            self.tiles.remove(l_draggedRuleData);
            self.tiles.splice(l_targetIndex, 0, l_draggedRuleData);
            self.tiles().forEach(tile => { tile.action(UPD_TILE) })
            self.isEdit(true);
            self.reDrawDashboard();
        }
        self.sourceRuleIndex = -1;
        self.targetRuleIndex = -1;
    }
    self.rowDragEnter = function (data, event, index) {
        self.targetRuleIndex = index;
        event.preventDefault();
    }
    self.rowDragLeave = function (data, event) {
        self.targetRuleIndex = -1;
        event.preventDefault();
    }
    self.rowDragOver = function (data, event, index) {
        self.targetRuleIndex = index;
        event.preventDefault();
    }
    self.allowDrop = function (event) {
        event.preventDefault();
    }
    self.preventDrop = function (event) {
        event.preventDefault();
    }

}

function CustAttribute(data) {
    var self = {};
    self.Id = ko.observable(getTextValue(data["AttributeDefinition-id"].Id));
    self.ItemId = ko.observable(getTextValue(data["AttributeDefinition-id"].ItemId));
    self.displayName = ko.observable(getTextValue(data.RelatedLabel.Label));
    self.name = ko.observable(getTextValue(data.Name));
    self.fieldType = ko.observable(getTextValue(data.DataType));
    return self;
}


function RowData(data) {
    let self = this;
    self.rowList = ko.observableArray();
}

function DrillDownReportModal() {
    let self = this;
    self.columns = ko.observableArray();
    self.rows = ko.observableArray();

    self.paginationPerPage = ko.observableArray(paginationParams);
    self.limitValue = ko.observable(paginationParams[2].value);
    self.numOfPages = ko.observable(1);
    self.currentPage = ko.observable(1);
    self.numOfContractsInCurrentPage = ko.observable('');
    self.totalCurrentPageContractsCount = ko.observable('');
    self.totalContractsCount = ko.observable(0);


    self.xColumn = ko.observable('');
    self.xColumnVal = ko.observable('');

    self.yColumn = ko.observable('');
    self.yColumnVal = ko.observable('');
    self.checkedAllContracts = ko.observable(false);

    self.onPerPageCountChange = function () {
        if (self.rows().length > 0) {
            self.resetPaginationParams();
            self.renderContractsList();
        }
    }

    self.resetPaginationParams = function () {
        self.currentPage(1);
        var totalPages = Math.ceil(self.totalContractsCount() / self.limitValue());
        self.numOfPages(totalPages < 1 ? 1 : totalPages);
    }

    self.renderContractsList = function () {
        refreshList(self.limitValue(), self.limitValue() * (self.currentPage() - 1));
    }

    function refreshList(limit = 25, offset = 0) {
        let req = _prepareSearchInputReq(self.xColumn(), self.xColumnVal(), self.yColumn(), self.yColumnVal());
        req.limit = limit;
        req.offset = offset;
        getContractJSONDataTable(req, (response, status) => {
            //console.log(response);
            _loadDataFromResp(response.tuple.old);
        });
    }

    self.goToFirstPage = function () {
        self.currentPage(1);
        self.renderContractsList();
    }
    self.goToPreviousPage = function () {
        self.currentPage(self.currentPage() - 1);
        self.renderContractsList();
    }
    self.goToNextPage = function () {
        self.currentPage(self.currentPage() + 1);
        self.renderContractsList();
    }
    self.goToLastPage = function () {
        self.currentPage(self.numOfPages());
        self.renderContractsList();
    }


    self.clearModal = () => {
        self.columns.removeAll();
        self.rows.removeAll();
        self.xColumn('');
        self.xColumnVal('');
        self.yColumn('');
        self.yColumnVal('');
        self.paginationPerPage(paginationParams);
        self.limitValue(paginationParams[2].value);
        self.numOfPages(1);
        self.currentPage(1);
        self.numOfContractsInCurrentPage('');
        self.totalCurrentPageContractsCount('');
        self.totalContractsCount(0);
    }

    self.loadData = (data) => {

    }

    self.openContract = function (iItem) {
        var url = "../../../../../app/start/web/perform/item/005056C00008A1E795653A59509D399D." + iItem.contractId;
        window.open(url, '_blank');
    }

    self.openModal = (data, xColumn, xColumnVal, yColumn, yColumnVal) => {
        self.clearModal();
        self.xColumn(xColumn);
        self.xColumnVal(xColumnVal);
        self.yColumn(yColumn);
        self.yColumnVal(yColumnVal);
        _loadDataFromResp(data);
        $("#div_viewDrillDownListModal").modal({
            backdrop: 'static',
            keyboard: false
        });
    }


    function _loadDataFromResp(data) {
        if (data && data.getContractJsonData) {
            let constractData = JSON.parse(data.getContractJsonData.getContractJsonData);
            self.numOfContractsInCurrentPage(constractData.data.length);
            self.totalContractsCount(constractData.count);
            self.numOfPages(Math.ceil(self.totalContractsCount() / self.limitValue()));
            self.rows.removeAll();
            constractData.data.forEach(contract => {
                let modalObj = populateContractModal(contract);
                modalObj.checked = ko.observable(false);
                self.rows.push(modalObj);
            })
        }
    }

    self.closeModal = () => {
        $('#div_viewDrillDownListModal').modal('hide');
    }

    self.onContractRowCheckboxValueChanged = (data, event) => {
        event.stopPropagation();
        self.checkedAllContracts(false);
        data.checked(!data.checked());
        let count = 0;
        self.rows().forEach(ele => ele.checked() ? (count++) : null);
        if (count > 0 && count === self.rows().length) {
            self.checkedAllContracts(true);
        }
    }

    self.selectAllContracts = () => {
        self.checkedAllContracts(!self.checkedAllContracts());
        self.rows().forEach(ele => ele.checked(self.checkedAllContracts()));
    }
    self.openContractFromActionBar = () => {
        self.rows().forEach(ele => {
            if (ele.checked()) {
                var url = "../../../../../app/start/web/perform/item/005056C00008A1E795653A59509D399D."
                    + ele.contractId;
                window.open(url, '_blank');
            }
        });
    }

    self.exportSelectedContracts = () => {
        let reqContractProps = null;
        let ctrObj = {};
        ctrObj.contracts = {};
        ctrObj.contracts.contract = []
        ctrObj.contracts.contract.push(JSON.stringify(contractProperties));
        self.rows().filter(ele => ele.checked()).forEach(function (ctr) {
            reqContractProps = Object.assign({}, contractProperties);
            Object.entries(contractProperties).forEach(([key, val]) => reqContractProps[key] = ctr.contractData[key] ? ctr.contractData[key] : "");
            ctrObj.contracts.contract.push(JSON.stringify(reqContractProps));
        });
        downloadReport(ctrObj);
    }

    self.exportAllContracts = () => {
        let req = _prepareSearchInputReq(self.xColumn(), self.xColumnVal(), self.yColumn(), self.yColumnVal());
        req.limit = 1000000;
        req.offset = 0;
        getContractJSONDataTable(req, function (data, status) {
            let contractModals = [];
            JSON.parse(data.tuple.old.getContractJsonData.getContractJsonData).data.forEach(ele => {
                contractModals.push(populateContractModal(ele));
            })
            let reqContractProps = null;
            let ctrObj = {};
            ctrObj.contracts = {};
            ctrObj.contracts.contract = []
            ctrObj.contracts.contract.push(JSON.stringify(contractProperties));
            contractModals.forEach(function (ctr) {
                reqContractProps = Object.assign({}, contractProperties);
                Object.entries(contractProperties).forEach(([key, val]) => reqContractProps[key] = ctr.contractData[key] ? ctr.contractData[key] : "");
                ctrObj.contracts.contract.push(JSON.stringify(reqContractProps));
            });
            downloadReport(ctrObj);
        });
    }

}

function ConfigurableReportModal() {
    let self = this;
    self.id = ko.observable();

    self.allReportsTabs = ko.observableArray([]);
    self.custFieldList = ko.observableArray([]);
    self.showLeftNav = ko.observable(true);
    self.selectedTab = ko.observable();


    self.populateCustAttribute = function () {
        getCustomAttributesListService({}, function (data, status) {
            if (status === "SUCCESS") {
                self.custFieldList.removeAll();
                var custAttrList = data.FindZ_INT_CustAttrListResponse.AttributeDefinition;
                if (null != custAttrList && Array.isArray(custAttrList)) {
                    for (let index = 0; index < custAttrList.length; index++) {
                        const element = custAttrList[index];
                        self.custFieldList.push(CustAttribute(element));
                    }
                } else if (null != custAttrList) {
                    self.custFieldList.push(CustAttribute(custAttrList));
                }
                //console.log(self.custFieldList())
            }
        });
    }

    self.loadAllTabsForUsers = (tabs) => {
        self.allReportsTabs.removeAll();
        if (!Array.isArray(tabs)) {
            tabs = new Array(tabs);
        }
        tabs.forEach(tab => {
            let tabNode = new TabNode();
            tabNode.dashboardId(tab["RelatedReportNode-id"].Id);
            tabNode.tabId1(tab["RelatedReportNode-id"].Id1);
            tabNode.tabItemId(tab["RelatedReportNode-id"].ItemId);
            tabNode.tabItemId1(tab["RelatedReportNode-id"].ItemId1);
            tabNode.name(tab.Name);
            tabNode.order(tab.Order);
            self.allReportsTabs.push(tabNode);
            self.id(tab["RelatedReportNode-id"].Id);
        });
        self.allReportsTabs.sort((rep1, rep2) => rep1.order() - rep2.order())
    }

    self.selectTab = (data, event) => {
        if (!data.display() && data.action() !== CRT_TAB) {
            self.allReportsTabs().forEach(ele => ele.display(false));
            self.selectedTab(data);
            data.display(true);
            data.refreshTiles(data.tabId1());
        } else if (!data.display()) {
            self.allReportsTabs().forEach(ele => ele.display(false));
            self.selectedTab(data);
            data.display(true);
        }
    }

    self.moveUp = (data, event) => {
        let index = self.allReportsTabs().indexOf(data);
        if (index > 0) {
            self.allReportsTabs.remove(data);
            self.allReportsTabs.splice(index - 1, 0, data);
        }
        data.showOptions(false);
        _saveTabsOrder();
    }

    function _saveTabsOrder() {
        let req = { ReportsTabs: [] };
        _reArrangeTabOrder();
        self.allReportsTabs().forEach((el, index) => {
            req.ReportsTabs.push({
                TabItemId1: el.tabItemId1(),
                Order: index,
                DashboardId: el.dashboardId(),
                TabName: el.name(),
            });
        })
        updateOrdersOfReportTabsService(req, (res, stat) => {
            //console.log(stat);
            l_configurableReportModel.refreshReport();
        });
    }

    function _reArrangeTabOrder() {
        self.allReportsTabs().forEach((tab, index) => tab.order(index));
    }


    self.moveDown = (data, event) => {
        let index = self.allReportsTabs().indexOf(data);
        if (index < self.allReportsTabs().length - 1) {
            self.allReportsTabs.remove(data);
            self.allReportsTabs.splice(index + 1, 0, data);
            data.showOptions(false);
        }
        data.showOptions(false);
        _saveTabsOrder();
    }




    self.toggleLeftNav = () => {
        self.showLeftNav(!self.showLeftNav());
    }

    self.toggleEdit = () => {
        self.isEdit(!self.isEdit());
        _closeOptions();
    }

    self.refreshReport = () => {
        let selectTab = self.selectedTab();
        self.selectedTab(null);
        loadAllTabsForUserService(function (resp, status) {
            if (status == SUCCESS) {
                if (resp && resp.Response && resp.Response.GetRelatedReportNodeByTypeResponse && resp.Response.GetRelatedReportNodeByTypeResponse.RelatedReportNode)
                    self.loadAllTabsForUsers(resp.Response.GetRelatedReportNodeByTypeResponse.RelatedReportNode);

                if (selectTab) {
                    self.allReportsTabs().forEach(el => {
                        if (el.name() === selectTab.name()) {
                            self.selectTab(el);
                        }
                    })
                }
            }
        });
    }

    self.toggleOptions = (data) => {
        data.showOptions(!data.showOptions());
        self.allReportsTabs().forEach(tab => {
            if (data != tab) {
                tab.showOptions(false);
            }
        });
    }

    self.addNewReportTab = () => {
        let tab = new TabNode();
        let tabID = "NEW_Tab_ID_" + tabId++;
        tab.dashboardId(self.id());
        tab.action(CRT_TAB);
        tab.order(-1);
        tab.tabId1(tabID);
        tab.name("Untitled " + self.allReportsTabs().length);
        self.allReportsTabs.splice(0, 0, tab);
        if (self.allReportsTabs().length > 1) {
            tab.order(self.allReportsTabs()[1].order() - 1);
        }
        self.selectTab(tab);
    }

    self.init = () => {
        self.refreshReport();
        self.populateCustAttribute();
    }

    self.init(); // Modal initialize call
}

function _clearDiv() {
    if (document.getElementById("output_preview")) {
        document.getElementById("output_preview").innerHTML = "";
    }
    if (document.getElementById("model_root_layout1")) {
        document.getElementById("model_root_layout1").innerHTML = "";
    }
}

function _getNullStringVal(name) {
    return name ? name : 'null';
}


function populateContractModal(iElement) {

    var model = {};
    const currentLoc = window.location.href;
    const homeInd = currentLoc.lastIndexOf("/home/") + 6;
    let contractData = iElement.ctrpropjson_cjson;
    let customData = iElement.custpropjson_cjson;
    let ctrFixedData = iElement.fixedpropjson_cjson;
    if (!ctrFixedData) {
        ctrFixedData = {};
    }

    //Parsing boolean values
    contractData.AutoRenew = contractData.AutoRenew === "true" ? "Yes" : "No";
    contractData.ClientEarlyTermRight = contractData.ClientEarlyTermRight === "true" ? "Yes" : "No";
    contractData.IsExecuted = ctrFixedData.IsExecuted === "true" ? "Yes" : "No";
    contractData.ContractDocumentType = ctrFixedData.ContractDocumentType === "true" ? "Yes" : "No";
    contractData.Perpetual = contractData.Perpetual === "true" ? "Yes" : "No";
    contractData.Validated = contractData.Validated === "true" ? "Yes" : "No";
    contractData.PriceProtection = contractData.PriceProtection === "1" ? "Yes" : "No";


    //Parsing Date values
    _parseDate('MinStartdate');
    _parseDate('CurrentEndDate');
    _parseDate('CurrentStartDate');
    _parseDate('InitialExpiryDate');
    _parseDate('NextExpirationDate');
    _parseDate('StartDate');
    _parseDate('PriceProtectionDate');
    _parseDate('SignatureDate');
    _parseDate('CancellationDate');
    _parseDate('ValidatedOn');

    //Parsing Duration values
    _parseDuration('AutoRenewDuration', contractData.AutoRenewDuration);
    _parseDuration('InitialContractTenure', contractData.InitialContractTenure);

    model.contractterm = (contractData.InitialContractTenure);
    model.contractData = (contractData);
    model.customData = (customData);
    model.fixedData = (ctrFixedData)
    model.contractId = (contractData.ID);
    model.genContractId = (ctrFixedData.GeneratedContractId);
    model.liClass = ("nav-item");
    model.tabPaneClass = ("tab-pane fade");

    model.previewDocUrl = ("/home/" + currentLoc.substring(homeInd, homeInd + currentLoc.substring(homeInd).indexOf("/")) + "/app/start/web/item/"
        + "005056C00008A1E795653A59509D399D." + contractData.ID + "/005056C00008A1E7AA661CA0C93D1C4A");
    model.templateName = (ctrFixedData.Template);
    model.contractName = (contractData.ContractName);
    model.contractIdAttr = ("#" + contractData.ID);
    model.contractType = (ctrFixedData.ContractType);

    model.startDate = (contractData.MinStartdate);
    model.organization = (ctrFixedData.RelatedOrganization);
    model.isexternal = (ctrFixedData.ContractDocumentType);
    model.isexecuted = (ctrFixedData.IsExecuted);

    function _parseDuration(name, value) {
        let durationArray = [];
        if (value) {
            if (value.includes('P') && value.includes('M')) {
                let months = parseInt(value.substring(value.indexOf('P') + 1, value.indexOf('M')));
                if (months > 0) {
                    durationArray.push(months.toString().concat(months > 1 ? " months" : " month"));
                }
            }
            if (value.includes('M') && value.includes('D')) {
                const days = parseInt(value.substring(value.indexOf('M') + 1, value.indexOf('D')));
                if (days > 0) {
                    durationArray.push(days.toString().concat(days > 1 ? " days" : " day"));
                }
            }
            contractData[name] = durationArray.length > 0 ? durationArray.join(', ') : "No duration sepcified."
        }
    }

    function _parseDate(name) {
        if (contractData[name]) {
            contractData[name] = formateDatetoLocale(contractData[name]);
        }
    }
    return model;
}


function loadGeneralCTRNameMap() {
    try {
        getGeneralCTRPreviewDetails((res, status) => {
            if (res) {
                if (Array.isArray(res)) {
                    res.forEach(function (iElement) {
                        contractProperties[iElement.Name] = iElement.DisplayName;
                        contractPropertiesDataType[iElement.Name] = iElement.DataType;
                    });
                } else {
                    contractProperties[res.Name] = res.DisplayName;
                    contractPropertiesDataType[res.Name] = res.DataType;
                }
            }
        })
    } catch (e) {
        console.error(e)
    }
}

const l_configurableReportModel = new ConfigurableReportModal();
const l_drillDownListModel = new DrillDownReportModal();

$(document).ready(() => {
    var i_locale = getlocale();
    var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
    translateLabels("com/opentext/apps/contractcenter/CCAnalyticsDashboard/CCAnalyticsDashboard", i_locale);
    loadRTLIfRequired(i_locale, rtl_css);
    ko.applyBindings(l_drillDownListModel, document.getElementById("div_viewDrillDownListModal"));
    ko.applyBindings(l_configurableReportModel, document.getElementById("div_configurableReportForm"));
    l_configurableReportModel.refreshReport();
    loadGeneralCTRNameMap();
}); 