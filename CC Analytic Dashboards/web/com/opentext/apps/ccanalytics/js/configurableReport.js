
const paginationParams = [
    { name: '1 per page', value: 1 },
    { name: '10 per page', value: 10 },
    { name: '25 per page', value: 25 },
]

var cc_reports_services = (function () {
    var self = {};

    self.CreateOrUpdateReports = (req, callback) => {
        $.cordys.ajax({
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            method: "GetContractJsonData",
            parameters: req,
            success: function (data) {
                callBackFunc(data, "SUCCESS");
            },
            error: function (responseFailure) {
                callBackFunc(data, "ERROR");

            },
        });
    }

    self.getContractJsonData = (callBackFunc) => {
        $.cordys.ajax({
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            method: "GetContractJsonData",
            parameters: {
                xmlNomNode: {
                    SearchQuery: {
                        QueryElement: [
                            { Id: 0, Type: "CONTAINER", Order: 0, ParentElement: null },
                            {
                                Id: 1,
                                Type: "EXPRESSION",
                                Order: 1,
                                Expression: {
                                    OperandName: "ContractName",
                                    Operand: "IS NOT NULL",
                                    OperandValue: null,
                                    OperandDataType: "ContractName",
                                },
                                ParentElement: 0,
                            },
                        ],
                    },
                },
                limit: 50000,
                offset: 0,
            },

            success: function (data) {
                console.log(data);

                let resp = JSON.parse(
                    data.tuple.old.getContractJsonData.getContractJsonData
                );
                let datalist = resp.data.map((ele) =>
                    Object.assign({}, ele.ctrpropjson_cjson, ele.custpropjson_cjson)
                );
                callBackFunc(datalist);
            },
            error: function (responseFailure) {
                showOrHideErrorInfo(
                    "div_modalErrorInfoAreaOrg",
                    true,
                    "Unable to retrieve the organization list. Contact your administrator.",
                    10000
                );
                return false;
            },
        });
    }

    return self;
})();


var ContractModel = function (data) {

    var self = this;
    self.contractItemId = ko.observable(data ? data['Contract-id'].ItemId : null);
    self.contractData = ko.observable('');
    self.customData = ko.observable('');
    self.organization = ko.observable('');
    self.isexternal = ko.observable('');
    self.isexecuted = ko.observable('');
    self.contractId = ko.observable('');
    self.genContractId = ko.observable('');
    self.contractIdAttr = ko.observable('');
    self.liClass = ko.observable('');
    self.tabPaneClass = ko.observable('');
    self.previewDocUrl = ko.observable('');

    self.templateName = ko.observable('');
    self.contractName = ko.observable('');
    self.contractType = ko.observable('');
    self.startDate = ko.observable('');
    self.contractterm = ko.observable('');

    return self;
}

const columns = [
    "-Select-",
    "Region",
    "Country",
    "EndUser",
    "Comments",
    "Currency",
    "Priority",
    "Template",
    "AmendType",
    "AutoRenew",
    "Perpetual",
    "StartDate",
    "Validated",
    "CRMQuoteID",
    "IsExecuted",
    "Party.Name",
    "SAPOrderID",
    "DealManager",
    "Description",
    "DisplayName",
    "Party.Email",
    "ValidatedBy",
    "ValidatedOn",
    "worklist_id",
    "ContractName",
    "ContractType",
    "MinStartdate",
    "TemplateType",
    "AccountNumber",
    "ContractValue",
    "Party.Website",
    "SignatureDate",
    "ActionDuration",
    "ContractNumber",
    "CurrentEndDate",
    "PriceProtection",
    "RenewalComments",
    "RenewalDiscount",
    "TerminationFees",
    "CRMOpportunityID",
    "CancellationDate",
    "ContractValueUSD",
    "CurrentStartDate",
    "AutoRenewDuration",
    "InitialExpiryDate",
    "Party.Description",
    "RenewalFlagStatus",
    "TerminationReason",
    "NextExpirationDate",
    "DocumentOrigination",
    "GeneratedContractId",
    "PriceProtectionDate",
    "RelatedOrganization",
    "CancellationComments",
    "ClientEarlyTermRight",
    "ContractDocumentType",
    "NotificationDuration",
    "Party.RegisteredName",
    "Party.RegistrationID",
    "InitialContractTenure",
    "CustomerManagerComments",
    "TerminationNoticePeriod",
    "MultiParty.External.Name",
    "MultiParty.Internal.Name",
    "EarlyTerminationConditions",
    "Party.IdentificationNumber",
    "RelatedContacts.Person.Email",
    "OriginalSalesAccountExecutive",
    "RelatedContacts.Person.LastName",
    "RelatedContacts.Person.FirstName",
    "RelatedContacts.Person.DisplayName",
    "MultiParty.External.Contact.FirstName",
    "MultiParty.Internal.Contact.FirstName"];

const ReportModal = function () {
    let self = this;
    self.name = ko.observable();
    self.modalReportName = ko.observable();
    self.display = ko.observable(false);
    self.showOptions = ko.observable(false);
    self.contractColumns = ko.observableArray();
    self.listSelected = ko.observable();

    self.xAxisSelected = ko.observable();
    self.yAxisSelected = ko.observable();
    self.chartSelected = ko.observable();
    self.aggregSelected = ko.observable();
    self.aggregatorColSelected = ko.observable();
    self.showAggCol = ko.observable(false);

    self.paginationPerPage = ko.observableArray(paginationParams);
    self.limitValue = ko.observable(paginationParams[2].value);
    self.numOfPages = ko.observable(1);
    self.currentPage = ko.observable(1);
    self.numOfContractsInCurrentPage = ko.observable('');
    self.totalCurrentPageContractsCount = ko.observable('');
    self.totalContractsCount = ko.observable(0);
    self.contractsList = ko.observableArray();

    self.display.subscribe((newVal) => {
        if (newVal) {
            _populateContractColumnData();
        }
    });

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
    }

    self.deleteReport = () => {
        self.modalReportName(self.name());
        $("#id_DeleteReport").modal({
            backdrop: 'static',
            keyboard: false
        });
        $('button#confirmYes').off("click");
        $('button#confirmYes').on('click', function (_event) {

        });
    }

    self.toggleOptions = () => {
        self.showOptions(!self.showOptions());
    }

    self.displayReport = () => {
        if (!self.display()) {
            l_chartRender_model.savedReports()
                .forEach(ele => ele.display(false));
            self.display(true);
        }
    }

    self.onSelectXchange = (data, event) => {
        console.log(data);
    }

    function _populateContractColumnData() {
        console.log("_populateContractColumnData " + self.name());
        self.contractColumns.removeAll();
        columns.forEach(ele => self.contractColumns.push(ele))
    }


    self.aggregSelected.subscribe(function (value) {
        self.showAggCol(false);
        self.aggregatorColSelected("");
        if (
            value.indexOf("sum") > -1 ||
            value.indexOf("countunique") > -1 ||
            value.indexOf("median") > -1 || value.indexOf("count") > -1
        ) {
            self.showAggCol(true);
        }
    });

    self.renderChart = function () {
        cc_reports_services.getContractJsonData(function (data) {
            _renderChart(
                data,
                [self.xAxisSelected()],
                [self.yAxisSelected()],
                [self.aggregatorColSelected()],
                self.chartSelected(),
                self.aggregSelected()
            );
        });
    };

    self.saveChart = function () {

    };

    function _prepareSearchInputReq(clickData) {
        var req = {
            xmlNomNode: {
                SearchQuery: { QueryElement: [] }
            }
        };
        var id = 0, order = 0;
        req.xmlNomNode.SearchQuery.QueryElement.push({ Id: id, Type: "CONTAINER", Order: order, ParentElement: null });
        var parentId = id;
        id++;
        order++;
        req.xmlNomNode.SearchQuery.QueryElement.push({
            Id: id,
            Type: "EXPRESSION",
            Order: order,
            ParentElement: parentId,
            AttrType: "GENERAL",
            Expression: {
                OperandName: self.xAxisSelected(),
                Operand: clickData.points[0].x === "null" ? "IS NULL" : "=",
                OperandValue: clickData.points[0].x,
                OperandDataType: "Text"
            }
        });
        id++;
        order++;
        req.xmlNomNode.SearchQuery.QueryElement.push({
            Id: id,
            Type: "CONNECTOR",
            Order: order,
            ParentElement: parentId,
            Connector: "and"
        });
        id++;
        order++;
        req.xmlNomNode.SearchQuery.QueryElement.push({
            Id: id,
            Type: "EXPRESSION",
            Order: order,
            ParentElement: parentId,
            AttrType: "GENERAL",
            Expression: {
                OperandName: self.yAxisSelected(),
                Operand: clickData.points[0].data.name === "null" ? "IS NULL" : "=",
                OperandValue: clickData.points[0].data.name,
                OperandDataType: ""
            }
        });
        return req;
    }



    self.getContractDataTable = function (inreq, callbackfunc) {
        $.cordys.ajax({
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            method: "GetContractJsonData",
            parameters: inreq,
            success: function (data) {
                callbackfunc(data, "SUCCESS");
            },
            error: function (responseFailure) {
                callbackfunc(null, "ERROR");
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving the contract properties. Contact your administrator."), 35000);
                return false;
            }
        });
    }


    self.getContractJSONDataTable = function (inreq, callbackfunc) {
        $.cordys.ajax({
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            method: "GetContractJsonData",
            parameters: inreq,
            success: function (data) {
                callbackfunc(data, "SUCCESS");
            },
            error: function (responseFailure) {
                callbackfunc(null, "ERROR");
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving the contract properties. Contact your administrator."), 35000);
                return false;
            }
        });
    }

    self.refreshList = function (limit = 25, offset = 0, clickData) {
        var req = _prepareSearchInputReq(clickData);
        req.limit = limit;
        req.offset = offset;
        self.getContractJSONDataTable(req, function (data, status) {
            if (status === "SUCCESS") {
                var elements = {};
                if (data.tuple.old.getContractJsonData) {
                    elements = JSON.parse(data.tuple.old.getContractJsonData.getContractJsonData);
                }
                self.populateDataTableFromJsonTable(elements);
                // self.enableButtonsAfterSearch();
                if (offset == 0) {
                    self.resetPaginationParams();
                }
            }
        });
    }

    self.resetPaginationParams = function () {
        self.currentPage(1);
        var totalPages = Math.ceil(self.totalContractsCount() / self.limitValue());
        self.numOfPages(totalPages < 1 ? 1 : totalPages);
    }

    self.populateDataTableFromJsonTable = function (iElement_res, clickData) {
        self.contractsList.removeAll();
        if (iElement_res) {
            const count = iElement_res.count;
            const data = iElement_res.data;
            if (count < 1) {
            }
            if (Array.isArray(data)) {
                self.totalCurrentPageContractsCount(data.length);
                data.forEach(function (iElement) {
                    var contractModal = self.populateContractModal(iElement);
                    self.contractsList.push(contractModal);
                });
            } else {
                self.totalCurrentPageContractsCount('1');
                self.contractsList.push(self.populateContractModal(data));
            }
            self.totalContractsCount(count);
            $("#id_contractListDialog").modal({
                backdrop: 'static',
                keyboard: false
            });
        }
    }

    self.populateContractModal = function (iElement) {

        var model = new ContractModel();
        const currentLoc = window.location.href;
        const homeInd = currentLoc.lastIndexOf("/home/") + 6;
        let contractData = iElement.ctrpropjson_cjson;
        let customData = iElement.custpropjson_cjson;

        //Parsing boolean values
        contractData.AutoRenew = contractData.AutoRenew === "true" ? "Yes" : "No";
        contractData.ClientEarlyTermRight = contractData.ClientEarlyTermRight === "true" ? "Yes" : "No";
        contractData.IsExecuted = contractData.IsExecuted === "true" ? "Yes" : "No";
        contractData.ContractDocumentType = contractData.ContractDocumentType === "true" ? "Yes" : "No";
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

        model.contractterm(contractData.InitialContractTenure);
        model.contractData(contractData);
        model.customData(customData);
        model.contractId(contractData.ID);
        model.genContractId(contractData.GeneratedContractId);
        model.liClass("nav-item");
        model.tabPaneClass("tab-pane fade");

        model.previewDocUrl = ko.observable("/home/" + currentLoc.substring(homeInd, homeInd + currentLoc.substring(homeInd).indexOf("/")) + "/app/start/web/item/"
            + "005056C00008A1E795653A59509D399D." + contractData.ID + "/005056C00008A1E7AA661CA0C93D1C4A");
        model.templateName(contractData.Template);
        model.contractName(contractData.ContractName);
        model.contractIdAttr = ko.observable("#" + contractData.ID);
        model.contractType(contractData.ContractType);

        model.startDate(contractData.MinStartdate);
        model.organization(contractData.RelatedOrganization);
        model.isexternal(contractData.ContractDocumentType);
        model.isexecuted(contractData.IsExecuted);

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
                contractData[name] = contractData[name];//formateDatetoLocale(contractData[name]);
            }
        }
        return model;
    }
}

const ChartRenderModal = function () {
    let self = this;
    self.savedReports = ko.observableArray();
    self.selectedReport = ko.observable();
    self.populateSavedReportsList = () => {
        let report_modal = new ReportModal();
        report_modal.name("Contract renewal Bar chart");
        report_modal.display(true);
        self.savedReports.push(report_modal);
        self.selectedReport(report_modal);
        report_modal = new ReportModal();
        report_modal.name("Obligation met line chart");
        self.savedReports.push(report_modal);
    }

    self.init = () => {
        self.populateSavedReportsList();
    }

    self.init(); // initializing method to load the data

};

function applyCustomAggregators() {
    $.pivotUtilities.aggregators.span = function (fn, formatter) {
        if (formatter == null) {
            formatter = usFmtInt;
        }
        return function (arg) {
            var attr;
            attr = arg[0];
            return function (data, rowKey, colKey) {
                data.forEach(e => {
                    let days = (new Date() - e[rowKey]) / (24 * 60 * 60 * 1000);
                    e[rowKey] = days < 0 ? 0 : days;
                });
                return {
                    uniq: [], count: 0,
                    push: function (record) {
                        var ref;
                        if (ref = record[attr], indexOf.call(this.uniq, ref) < 0) {
                            return this.uniq.push(record[attr]);
                        }
                    },
                    value: function () {
                        return fn(this.uniq);
                    },
                    format: formatter,
                    numInputs: attr != null ? 0 : 1
                };
            };
        };
    }
}

function _getAggregatorName(aggregatorName) {
    //Consider if there is only one aggregator
    switch (aggregatorName) {
        case "count": {
            aggregatorName = "Count";
            break;
        }
        case "countunique": {
            aggregatorName = "Count Unique Values";
            break;
        }
        case "listunique": {
            aggregatorName = "List Unique Values";
            break;
        }
        case "sum": {
            aggregatorName = "Sum";
            break;
        }
        case "integersum": {
            aggregatorName = "Integer Sum";
            break;
        }
        case "average": {
            aggregatorName = "Average";
            break;
        }
        case "median": {
            aggregatorName = "Median";
            break;
        }
        case "simplevariance": {
            aggregatorName = "Sample Variance";
            break;
        }
        case "simplestandarddeviation": {
            aggregatorName = "Sample Standard Deviation";
            break;
        }
        case "minimum": {
            aggregatorName = "Minimum";
            break;
        }
        case "maximum": {
            aggregatorName = "Maximum";
            break;
        }
        case "first": {
            aggregatorName = "First";
            break;
        }
        case "last": {
            aggregatorName = "Last";
            break;
        }
        case "sumasfractionoftotal": {
            aggregatorName = "Sum as Fraction of Total";
            break;
        }
        case "sumasfractionofrows": {
            aggregatorName = "Sum as Fraction of Rows";
            break;
        }
        case "sumasfractionofcols": {
            aggregatorName = "Sum as Fraction of Columns";
            break;
        }
        case "countasfractionoftotal": {
            aggregatorName = "Count as Fraction of Total";
            break;
        }
        case "countasfractionofrows": {
            aggregatorName = "Count as Fraction of Total";
            break;
        }
        case "countasfractionofcols": {
            aggregatorName = "Count as Fraction of Total";
            break;
        } case "span": {
            aggregatorName = "Span";
            break;
        }
        default: {
            aggregatorName = "Sum";
        }
    }
    return aggregatorName;
}



function _renderChart(data, cols, rows, vals, rendererName, aggregatorName) {

    data.forEach(e => {
        let days = (new Date() - new Date(e["MinStartdate"])) / (24 * 60 * 60 * 1000);
        e["MinStartdate"] = days < 0 ? 0 : Math.round(days / 7);
    });

    $("#output").empty();
    var renderers = $.extend(
        $.pivotUtilities.renderers,
        $.pivotUtilities.plotly_renderers
    );
    aggregatorName = _getAggregatorName(aggregatorName);

    $("#output").pivotUI(
        data,
        {
            renderers: renderers,
            cols: cols,
            rows: rows,
            vals: vals,
            rendererName: rendererName,
            rowOrder: "value_z_to_a",
            colOrder: "value_z_to_a",
            aggregatorName: aggregatorName,
            rendererOptions: {
                table: {
                    clickCallback: function (e, value, filters, pivotData) {
                        var fullData = [];
                        pivotData.forEachMatchingRecord(filters,
                            function (record) { fullData.push(record); });
                        console.log(fullData);
                    }
                }
            },
            onRefresh: () => {
                console.log("Refreshed");
                $("#output > table").children()[0].classList = "hidden";
                $("#output > table").children().children()[3].classList = "hidden"
                $("#output > table").children().children()[4].classList = "hidden"
            }
        },
        true
    );

    setTimeout(() => {

        $(".js-plotly-plot").on('plotly_click', function (event, data) {
            console.log(data);
            l_chartRender_model.refreshList(25, 0, data);
        });
    }, 1000)
}

const l_chartRender_model = new ChartRenderModal();

$(function () {

    applyCustomAggregators();

    ko.applyBindings(
        l_chartRender_model,
        document.getElementById("chartContainer")
    );
});