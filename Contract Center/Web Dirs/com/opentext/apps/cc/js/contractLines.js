$.cordys.json.defaults.removeNamespacePrefix = true;
var l_serviceLookup_model;
var l_UOMLookup_model;
var l_POLookup_model;
var l_contractLines_model;
//var l_contractLinesFilter_model;
var l_contractLineDetails_model;
var contractLinesOffsetValue = 0;
var contractLinesLimitValue = 25;

var ContractLinesModel = function () {
    var self = this;
    self.ContractLines = ko.observableArray([]);
    self.currentPage = ko.observable(1);
    self.numOfContractLines = ko.observable('');
    self.numOfContractLinesInCurrentPage = ko.observable('');
    self.numOfPages = ko.observable('');
    self.isFilterApplied = ko.observable(false);
    self.isEditable = ko.observable(false);
    self.loadPageContent = function () {
        self.l_contractLinesFilter_model = new ContractLinesFilterModel();
        //hideCTRLinesFilter();
        self.isEditable(_userStateEditabilityFlag);
        listContractLines();
    }
    self.onCTRLineRowCheckboxValueChanged = function (iItem, event) {
        var l_currentClassName = event.currentTarget.className;
        if (l_currentClassName == "cc-select-column cc-checkbox-off") {
            $(event.currentTarget).removeClass("cc-checkbox-off")
            $(event.currentTarget).addClass("cc-checkbox-on")
            selectedContractLinesListMap[iItem.ctrLineItemID1] = iItem.serviceName;
            $(event.currentTarget.parentElement.parentElement).css("background-color", "#CBD3D9")
        }
        else if (l_currentClassName == "cc-select-column cc-checkbox-on") {
            $(event.currentTarget).removeClass("cc-checkbox-on")
            $(event.currentTarget).addClass("cc-checkbox-off")
            delete selectedContractLinesListMap[iItem.ctrLineItemID1];
            $(event.currentTarget.parentElement.parentElement).css("background-color", "transparent")
        }
        event.stopPropagation();
        if (Object.keys(selectedContractLinesListMap).length <= 0) {
            $("#div_selectAllContractLines").removeClass("cc-checkbox-select-all-partial");
            $("#div_selectAllContractLines").removeClass("cc-checkbox-select-all-on");
            $("#div_selectAllContractLines").addClass("cc-checkbox-select-all-off");
            $("#btn_editContractLineFromActionBar").css("display", "none");
            $("#btn_deleteContractLinesFromActionBar").css("display", "none");
        } else if (Object.keys(selectedContractLinesListMap).length == 1) {
            if (1 == l_contractLines_model.numOfContractLinesInCurrentPage()) {
                $("#div_selectAllContractLines").removeClass("cc-checkbox-select-all-off");
                $("#div_selectAllContractLines").removeClass("cc-checkbox-select-all-partial");
                $("#div_selectAllContractLines").addClass("cc-checkbox-select-all-on");
                $("#btn_editContractLineFromActionBar").css("display", "inline");
                $("#btn_deleteContractLinesFromActionBar").css("display", "inline");
            } else {
                $("#div_selectAllContractLines").removeClass("cc-checkbox-select-all-off");
                $("#div_selectAllContractLines").removeClass("cc-checkbox-select-all-on");
                $("#div_selectAllContractLines").addClass("cc-checkbox-select-all-partial");
                $("#btn_editContractLineFromActionBar").css("display", "inline");
                $("#btn_deleteContractLinesFromActionBar").css("display", "inline");
            }
        } else if (Object.keys(selectedContractLinesListMap).length > 1 && Object.keys(selectedContractLinesListMap).length < l_contractLines_model.numOfContractLinesInCurrentPage()) {
            $("#div_selectAllContractLines").removeClass("cc-checkbox-select-all-off");
            $("#div_selectAllContractLines").removeClass("cc-checkbox-select-all-on");
            $("#div_selectAllContractLines").addClass("cc-checkbox-select-all-partial");
            $("#btn_editContractLineFromActionBar").css("display", "none");
            $("#btn_deleteContractLinesFromActionBar").css("display", "inline");
        } else if (Object.keys(selectedContractLinesListMap).length == l_contractLines_model.numOfContractLinesInCurrentPage()) {
            $("#div_selectAllContractLines").removeClass("cc-checkbox-select-all-off");
            $("#div_selectAllContractLines").removeClass("cc-checkbox-select-all-partial");
            $("#div_selectAllContractLines").addClass("cc-checkbox-select-all-on");
        }
    }
    self.onSelectAllCTRLinesCheckboxValueChanged = function (iItem, event) {
        var l_currentClassName = event.currentTarget.className;
        if (l_currentClassName == "cc-select-column cc-checkbox-select-all-off" || l_currentClassName == "cc-select-column cc-checkbox-select-all-partial") {
            $(event.currentTarget).removeClass("cc-checkbox-select-all-off");
            $(event.currentTarget).removeClass("cc-checkbox-select-all-partial");
            $(event.currentTarget).addClass("cc-checkbox-select-all-on");
            $("#table_ContractLinesTable").find('tbody .cc-select-column').removeClass("cc-checkbox-off");
            $("#table_ContractLinesTable").find('tbody .cc-select-column').addClass("cc-checkbox-on");
            $("#table_ContractLinesTable").find('tbody tr').css("background-color", "#CBD3D9");
            $("#btn_editContractLineFromActionBar").css("display", "none");
            $("#btn_deleteContractLinesFromActionBar").css("display", "inline");
            l_contractLines_model.ContractLines().forEach(function (iToken) {
                selectedContractLinesListMap[iToken.ctrLineItemID1] = iToken.serviceName;
            });
        }
        else if (l_currentClassName == "cc-select-column cc-checkbox-select-all-on") {
            $(event.currentTarget).removeClass("cc-checkbox-select-all-on");
            $(event.currentTarget).addClass("cc-checkbox-select-all-off");
            $("#table_ContractLinesTable").find('tbody .cc-select-column').removeClass("cc-checkbox-on");
            $("#table_ContractLinesTable").find('tbody .cc-select-column').addClass("cc-checkbox-off");
            $("#table_ContractLinesTable").find('tbody tr').css("background-color", "transparent")
            $("#btn_editContractLineFromActionBar").css("display", "none");
            $("#btn_deleteContractLinesFromActionBar").css("display", "none");
            selectedContractLinesListMap = {};
        }
        event.stopPropagation();
    }
}

var ContractLinesFilterModel = function () {
    var self = this;
    var l_productOrServiceFilterElement = document.getElementById("input_productOrServiceFilter");
    var l_productOrServiceGrpFilterElement = document.getElementById("input_productOrServiceGrpFilter");
    var l_skuOrServiceIDFilterElement = document.getElementById("input_skuOrServiceIDFilter");
    var l_priceFilterElement = document.getElementById("input_priceFilter");
    var l_quantityFilterElement = document.getElementById("input_quantityFilter");
    var l_unitsFilterElement = document.getElementById("input_unitsFilter");
    var l_poNumberFilterElement = document.getElementById("input_poNumberFilter");
    self.ClearContractLinesFilter = function () {
        l_productOrServiceFilterElement.value = "";
        l_productOrServiceGrpFilterElement.value = "";
        l_skuOrServiceIDFilterElement.value = "";
        l_priceFilterElement.value = "";
        l_quantityFilterElement.value = "";
        l_unitsFilterElement.value = "";
        l_poNumberFilterElement.value = "";
    }
    self.getContractLinesFilterObject = function () {
        self.CurrentFilterObject = {
            "contractID": contractID,
            "serviceName": l_productOrServiceFilterElement ? l_productOrServiceFilterElement.value : "",
            "serviceGroupName": l_productOrServiceGrpFilterElement ? l_productOrServiceGrpFilterElement.value : "",
            "skuOrServiceId": l_skuOrServiceIDFilterElement ? l_skuOrServiceIDFilterElement.value : "",
            "price": l_priceFilterElement ? l_priceFilterElement.value : "",
            "quantity": l_quantityFilterElement ? l_quantityFilterElement.value : "",
            "units": l_unitsFilterElement ? l_unitsFilterElement.value : "",
            "poNumber": l_poNumberFilterElement ? l_poNumberFilterElement.value : "",
            "offset": contractLinesOffsetValue,
            "limit": contractLinesLimitValue,
        };
        return self.CurrentFilterObject;
    }
}

var ContractLineDetailsModel = function () {
    var self = this;

    self.ctrLineID = ko.observable('');
    self.ctrLineItemID = ko.observable('');
    self.ctrLineID1 = ko.observable('');
    self.ctrLineItemID1 = ko.observable('');

    self.serviceName = ko.observable('');
    self.serviceID = ko.observable('');
    self.serviceItemID = ko.observable('');

    self.serviceGroupName = ko.observable('');
    self.serviceGroupID = ko.observable('');
    self.serviceGroupItemID = ko.observable('');

    self.skuOrServiceId = ko.observable('');
    self.price = ko.observable('');
    self.quantity = ko.observable('');

    self.units = ko.observable('');
    self.unitsID = ko.observable('');
    self.unitsItemID = ko.observable('');

    self.poNumber = ko.observable('');
    self.poNumberID = ko.observable('');
    self.poNumberItemID = ko.observable('');
}

function listContractLines() {
    $("#btn_editContractLineFromActionBar").css("display", "none");
    $("#btn_deleteContractLinesFromActionBar").css("display", "none");
    $("#div_selectAllContractLines").removeClass("cc-checkbox-select-all-partial");
    $("#div_selectAllContractLines").removeClass("cc-checkbox-select-all-on");
    $("#div_selectAllContractLines").addClass("cc-checkbox-select-all-off");
    selectedContractLinesListMap = {};
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
        method: "GetContractLineswithFilters",
        parameters: l_contractLines_model.l_contractLinesFilter_model.getContractLinesFilterObject(),
        success: function (data) {
            addDataToContractLinesListView(data.FindZ_INT_CTRLinesResponse.ContractLines, l_contractLines_model);
            if (undefined != data.FindZ_INT_CTRLinesResponse["@total"]) {
                l_contractLines_model.numOfContractLines(data.FindZ_INT_CTRLinesResponse["@total"]);
            } else {
                l_contractLines_model.numOfContractLines(0);
            }
            if (l_contractLines_model.numOfContractLines() != 0) {
                l_contractLines_model.numOfPages(Math.ceil(l_contractLines_model.numOfContractLines() / contractLinesLimitValue));
            } else {
                l_contractLines_model.numOfPages(1);
            }
            updatePaginationParams();
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving contract lines. Contact your administrator."), 10000);
            return false;
        }
    });
}

function addDataToContractLinesListView(iElementList, iModel) {
    iModel.ContractLines.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            iModel.numOfContractLinesInCurrentPage(iElementList.length);
            iElementList.forEach(function (iElement) {
                iModel.ContractLines.push(formCTRLinelistdata(iElement));
            });
        }
        else {
            iModel.numOfContractLinesInCurrentPage("1");
            iModel.ContractLines.push(formCTRLinelistdata(iElementList));
        }
    }
}

function formCTRLinelistdata(iElement) {
    var properties = {};
    if (iElement) {

        properties.ctrLineID = iElement['ContractLines-id'].Id;
        properties.ctrLineItemID = iElement['ContractLines-id'].ItemId;
        properties.ctrLineID1 = iElement['ContractLines-id'].Id1;
        properties.ctrLineItemID1 = iElement['ContractLines-id'].ItemId1;

        if (iElement.Services) {
            properties.serviceName = iElement.Services.Name;
            properties.serviceID = iElement.Services['Service-id'].Id1;
            properties.serviceItemID = iElement.Services['Service-id'].ItemId1;

            properties.serviceGroupName = iElement.Services.Owner.Name;
            properties.serviceGroupID = iElement.Services['Service-id'].Id;
            properties.serviceGroupItemID = iElement.Services['Service-id'].ItemId;
        } else {
            properties.serviceName = "";
            properties.serviceID = "";
            properties.serviceItemID = "";

            properties.serviceGroupName = "";
            properties.serviceGroupID = "";
            properties.serviceGroupItemID = "";
        }

        properties.skuOrServiceId = iElement.SKUOrServiceId;
        properties.price = iElement.Price;
        properties.quantity = iElement.Quantity;

        if (iElement.ToUnitOfMeasurement) {
            properties.units = iElement.ToUnitOfMeasurement.Unit;
            properties.unitsID = iElement.ToUnitOfMeasurement['UnitOfMeasurement-id'].Id;
            properties.unitsItemID = iElement.ToUnitOfMeasurement['UnitOfMeasurement-id'].ItemId;
        } else {
            properties.units = "";
            properties.unitsID = "";
            properties.unitsItemID = "";
        }

        if (iElement.RelatedPONumber) {
            properties.poNumber = iElement.RelatedPONumber.PONumber;
            properties.poNumberID = iElement.RelatedPONumber['RelatedPOs-id'].Id;
            properties.poNumberItemID = iElement.RelatedPONumber['RelatedPOs-id'].ItemId;
        } else {
            properties.poNumber = "";
            properties.poNumberID = "";
            properties.poNumberItemID = "";
        }
    }
    return properties;
}

function hideCTRLinesFilter() {
    $("#div_contractLinesFilter").hide();
    document.getElementById("div_contractLinesFilter").setAttribute("apps-toggle", 'collapsed');
    $("#div_contractLinesData").removeClass("col-md-9");
    $("#div_contractLinesData").addClass("col-md-12");
}

function hideOrShowCTRLinesFilterContainerBody(iElement, iShow) {
    if (iShow) {
        iElement.setAttribute("apps-toggle", 'expanded');
        iElement.lastElementChild.src = "../../../../../com/opentext/apps/utils/img/caret_up.svg";
    }
    else {
        iElement.setAttribute("apps-toggle", 'collapsed');
        iElement.lastElementChild.src = "../../../../../com/opentext/apps/utils/img/caret_down.svg";
    }
}

function updateLimitValue(iElement) {
    contractLinesOffsetValue = 0;
    l_contractLines_model.currentPage('1');
    contractLinesLimitValue = $(iElement).val();
    listContractLines();
}

function updatePaginationParams() {
    if (l_contractLines_model.currentPage() == 1) {
        document.getElementById("li_contractLinesLeftNavigation").style.display = "none";
        document.getElementById("li_contractLinesRightNavigation").style.display = "inline";
    }
    if (parseInt(l_contractLines_model.numOfContractLines()) <= parseInt(contractLinesLimitValue)) {
        l_contractLines_model.currentPage('1');
        $('#li_contractLinesLeftNavigation,#li_contractLinesRightNavigation').css('display', 'none');
    }
}

function goToPreviousPage() {
    if (l_contractLines_model.currentPage() > 1) {
        contractLinesOffsetValue = parseInt(contractLinesOffsetValue) - parseInt(contractLinesLimitValue);
        l_contractLines_model.currentPage(parseInt(l_contractLines_model.currentPage()) - 1);
    }
    if (l_contractLines_model.currentPage() < Math.ceil(l_contractLines_model.numOfContractLines() / contractLinesLimitValue)) {
        document.getElementById("li_contractLinesRightNavigation").style.removeProperty("display");
    }
    if (l_contractLines_model.currentPage() == 1) {
        document.getElementById("li_contractLinesLeftNavigation").style.display = "none";
    }
    if (l_contractLines_model.currentPage() < 1)
        return;
    listContractLines();
}

function goToNextPage() {
    if (l_contractLines_model.currentPage() < Math.ceil(l_contractLines_model.numOfContractLines() / contractLinesLimitValue)) {
        contractLinesOffsetValue = parseInt(contractLinesOffsetValue) + parseInt(contractLinesLimitValue);
        l_contractLines_model.currentPage(isNaN(parseInt(l_contractLines_model.currentPage())) ? 0 : parseInt(l_contractLines_model.currentPage()));
        l_contractLines_model.currentPage(parseInt(l_contractLines_model.currentPage()) + 1);
    }
    if (l_contractLines_model.currentPage() == Math.ceil(l_contractLines_model.numOfContractLines() / contractLinesLimitValue)) {
        document.getElementById("li_contractLinesRightNavigation").style.display = "none";
    }
    if (l_contractLines_model.currentPage() > 1) {
        document.getElementById("li_contractLinesLeftNavigation").style.removeProperty("display");
    }
    listContractLines();
}

function goToLastPage() {
    contractLinesOffsetValue = (Math.ceil(l_contractLines_model.numOfContractLines() / contractLinesLimitValue) - 1) * contractLinesLimitValue;
    l_contractLines_model.currentPage(Math.ceil(l_contractLines_model.numOfContractLines() / contractLinesLimitValue));
    $('#li_contractLinesRightNavigation').css('display', 'none');
    $('#li_contractLinesLeftNavigation').css('display', 'inline');
    listContractLines();
}

function goToFirstPage() {
    contractLinesOffsetValue = 0;
    l_contractLines_model.currentPage('1');
    $('#li_contractLinesRightNavigation').css('display', 'inline');
    $('#li_contractLinesLeftNavigation').css('display', 'none');
    listContractLines()
}

function ApplyFilterOnContractLines(event, iSrcElement) {
    if (checkifFilterValueExists()) {
        $("#btn_clearFilterActionBar").css('display', 'inline');
        l_contractLines_model.isFilterApplied(true);
    } else {
        $("#btn_clearFilterActionBar").css('display', 'none');
        l_contractLines_model.isFilterApplied(false);
    }
    listContractLines();
    hideCTRLinesFilter();
}

function checkifFilterValueExists() {
    if (l_contractLines_model.l_contractLinesFilter_model.getContractLinesFilterObject().poNumber != "") {
        return true;
    } else if (l_contractLines_model.l_contractLinesFilter_model.getContractLinesFilterObject().serviceName != "") {
        return true;
    } else if (l_contractLines_model.l_contractLinesFilter_model.getContractLinesFilterObject().serviceGroupName != "") {
        return true;
    } else if (l_contractLines_model.l_contractLinesFilter_model.getContractLinesFilterObject().skuOrServiceId != "") {
        return true;
    } else if (l_contractLines_model.l_contractLinesFilter_model.getContractLinesFilterObject().price != "") {
        return true;
    } else if (l_contractLines_model.l_contractLinesFilter_model.getContractLinesFilterObject().quantity != "") {
        return true;
    } else if (l_contractLines_model.l_contractLinesFilter_model.getContractLinesFilterObject().units != "") {
        return true;
    } else {
        return false;
    }
}

function ClearContractLinesFilter(event, iSrcElement) {
    l_contractLines_model.l_contractLinesFilter_model.ClearContractLinesFilter();
    $("#btn_clearFilterActionBar").css('display', 'none');
    l_contractLines_model.isFilterApplied(false);
    listContractLines();
    hideCTRLinesFilter();
}

function createOrUpdateContractLine() {
    if (l_contractLineDetails_model && l_contractLineDetails_model.serviceID() != "") {
        if (l_contractLineDetails_model.ctrLineItemID1()) {
            updateContractLine();
        } else {
            createContractLine();
        }
    } else {
        showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("Please select a Product or service."), 5000);
    }
}
function updateContractLine() {
    $.cordys.ajax({
        namespace: "http://schemas/OpenTextContractCenter/Contract.ContractLines/operations",
        method: "UpdateContractLines",
        parameters: {
            "ContractLines-id": {
                "Id": l_contractLineDetails_model.ctrLineID(),
                "Id1": l_contractLineDetails_model.ctrLineID1(),
            },
            "ContractLines-update": {
                "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance",
                "SKUOrServiceId": l_contractLineDetails_model.skuOrServiceId() ? l_contractLineDetails_model.skuOrServiceId() : { '@xsi:nil': 'true' },
                "Price": l_contractLineDetails_model.price() ? l_contractLineDetails_model.price() : { '@xsi:nil': 'true' },
                "Quantity": l_contractLineDetails_model.quantity() ? l_contractLineDetails_model.quantity() : { '@xsi:nil': 'true' },
                "Services": {
                    "Service-id": {
                        "Id": l_contractLineDetails_model.serviceGroupID(),
                        "Id1": l_contractLineDetails_model.serviceID()
                    },
                },
                "ToUnitOfMeasurement": {
                    "UnitOfMeasurement-id": l_contractLineDetails_model.unitsID() ? { "Id": l_contractLineDetails_model.unitsID() } : {},
                },
                "RelatedPONumber": {
                    "RelatedPOs-id": l_contractLineDetails_model.poNumberID() ? { "Id": l_contractLineDetails_model.poNumberID() } : {},
                }
            }
        },
        success: function (data) {
            $('#div_createOrUpdateContractLineModal').modal('hide');
            successToast(3000, getTranslationMessage("Contract line updated succesfully."));
            listContractLines();
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while updating contract line. Contact your administrator."), 10000);
            return false;
        }
    });
}

function createContractLine() {
    $.cordys.ajax({
        namespace: "http://schemas/OpenTextContractCenter/Contract/operations",
        method: "CreateContractLines",
        parameters: {
            "Contract-id": {
                "Id": contractID,
            },
            "ContractLines-create": {
                "SKUOrServiceId": l_contractLineDetails_model.skuOrServiceId(),
                "Price": l_contractLineDetails_model.price(),
                "Quantity": l_contractLineDetails_model.quantity(),
                "Services": {
                    "Service-id": {
                        "Id": l_contractLineDetails_model.serviceGroupID(),
                        "Id1": l_contractLineDetails_model.serviceID()
                    }
                },
                "ToUnitOfMeasurement": {
                    "UnitOfMeasurement-id": l_contractLineDetails_model.unitsID() ? { "Id": l_contractLineDetails_model.unitsID() } : {},
                },
                "RelatedPONumber": {
                    "RelatedPOs-id": l_contractLineDetails_model.poNumberID() ? { "Id": l_contractLineDetails_model.poNumberID() } : {},
                }
            }
        },
        success: function (data) {
            $('#div_createOrUpdateContractLineModal').modal('hide');
            successToast(3000, getTranslationMessage("Contract line created succesfully."));
            listContractLines();
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while creating contract line. Contact your administrator."), 10000);
            return false;
        }
    });
}

function callOpenCTRLineCreateForm() {
    openCTRLineCreateOrEditForm();
}

function callOpenCTRLineSummaryForm() {
    if (Object.keys(selectedContractLinesListMap).length == 1) {
        openCTRLineCreateOrEditForm(l_contractLines_model.ContractLines().filter(ele => ele.ctrLineItemID1 === Object.keys(selectedContractLinesListMap)[0])[0]);
    }
}

function openCTRLineCreateOrEditForm(i_ctrItem) {
    $('#div_createOrUpdateContractLineModal').modal({
        backdrop: 'static',
        keyboard: false
    })
    clearCTRLineDetailsModelData();
    if (i_ctrItem) {
        $("#span_createOrEditModalHeading").text(getTranslationMessage("Edit") + " ");
        $("#btn_createOrUpdateContractLine").text(getTranslationMessage("Update contract line"));
        loadCTRLineDetails(i_ctrItem);
    } else {
        $("#span_createOrEditModalHeading").text(getTranslationMessage("Create") + " ");
        $("#btn_createOrUpdateContractLine").text(getTranslationMessage("Create contract line"));
    }
}

function loadCTRLineDetails(i_ctrItem) {
    if (i_ctrItem) {
        l_contractLineDetails_model.ctrLineID(getTextValue(i_ctrItem.ctrLineID));
        l_contractLineDetails_model.ctrLineItemID(getTextValue(i_ctrItem.ctrLineItemID));
        l_contractLineDetails_model.ctrLineID1(getTextValue(i_ctrItem.ctrLineID1));
        l_contractLineDetails_model.ctrLineItemID1(getTextValue(i_ctrItem.ctrLineItemID1));

        l_contractLineDetails_model.serviceName(getTextValue(i_ctrItem.serviceName));
        l_contractLineDetails_model.serviceID(getTextValue(i_ctrItem.serviceID));
        l_contractLineDetails_model.serviceItemID(getTextValue(i_ctrItem.serviceItemID));

        l_contractLineDetails_model.serviceGroupName(getTextValue(i_ctrItem.serviceGroupName));
        l_contractLineDetails_model.serviceGroupID(getTextValue(i_ctrItem.serviceGroupID));
        l_contractLineDetails_model.serviceGroupItemID(getTextValue(i_ctrItem.serviceGroupItemID));

        l_contractLineDetails_model.skuOrServiceId(getTextValue(i_ctrItem.skuOrServiceId));
        l_contractLineDetails_model.price(getTextValue(i_ctrItem.price));
        l_contractLineDetails_model.quantity(getTextValue(i_ctrItem.quantity));

        l_contractLineDetails_model.units(getTextValue(i_ctrItem.units));
        l_contractLineDetails_model.unitsID(getTextValue(i_ctrItem.unitsID));
        l_contractLineDetails_model.unitsItemID(getTextValue(i_ctrItem.unitsItemID));

        l_contractLineDetails_model.poNumber(getTextValue(i_ctrItem.poNumber));
        l_contractLineDetails_model.poNumberID(getTextValue(i_ctrItem.poNumberID));
        l_contractLineDetails_model.poNumberItemID(getTextValue(i_ctrItem.poNumberItemID));
    }
}

function clearCTRLineDetailsModelData() {
    l_contractLineDetails_model.ctrLineID("");
    l_contractLineDetails_model.ctrLineItemID("");
    l_contractLineDetails_model.ctrLineID1("");
    l_contractLineDetails_model.ctrLineItemID1("");

    l_contractLineDetails_model.serviceName("");
    l_contractLineDetails_model.serviceID("");
    l_contractLineDetails_model.serviceItemID("");

    l_contractLineDetails_model.serviceGroupName("");
    l_contractLineDetails_model.serviceGroupID("");
    l_contractLineDetails_model.serviceGroupItemID("");

    l_contractLineDetails_model.skuOrServiceId("");
    l_contractLineDetails_model.price("");
    l_contractLineDetails_model.quantity("");

    l_contractLineDetails_model.units("");
    l_contractLineDetails_model.unitsID("");
    l_contractLineDetails_model.unitsItemID("");

    l_contractLineDetails_model.poNumber("");
    l_contractLineDetails_model.poNumberID("");
    l_contractLineDetails_model.poNumberItemID("");
}

function deleteContractLineFromActionBar() {
    $("#div_deleteCTRLineModal").modal({
        backdrop: 'static',
        keyboard: false
    });
    $("#span_NumOfCTRLinesToDelete").text(" (" + Object.keys(selectedContractLinesListMap).length + " items)");
    $('button#btn_deleteCTRLineYes').off("click");
    $('button#btn_deleteCTRLineYes').on('click', function (_event) {
        for (iElement in selectedContractLinesListMap) {
            $.cordys.ajax({
                namespace: "http://schemas/OpenTextContractCenter/Contract/operations",
                method: "DeleteContractLines",
                parameters: {
                    "Contract-id": {
                        "Id": contractID,
                    },
                    "ContractLines": {
                        "ContractLines-id": {
                            "ItemId1": iElement
                        }
                    }
                },
                success: function (data) {
                    $('#div_deleteCTRLineModal').modal('hide');
                    successToast(3000, getTranslationMessage("Contract line deleted succesfully."));
                },
                error: function (responseFailure) {
                    showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while deleting contract line. Contact your administrator."), 10000);
                    return false;
                }
            });
        }
        listContractLines();
    });
}

// -------code for lookups start------ 
function openServiceSelectionModal() {
    $('#div_serviceLookupModal').modal({
        backdrop: 'static',
        keyboard: false
    })
    ListAllServices();
    $('button#btn_selectServiceYes').off("click");
    $('button#btn_selectServiceYes').on('click', function (_event) {
        l_contractLineDetails_model.serviceName(l_serviceLookup_model.selectedServiceName())
        l_contractLineDetails_model.serviceID(l_serviceLookup_model.selectedServiceID())
        l_contractLineDetails_model.serviceItemID(l_serviceLookup_model.selectedServiceItemID())

        l_contractLineDetails_model.serviceGroupName(l_serviceLookup_model.selectedServiceGroupName())
        l_contractLineDetails_model.serviceGroupID(l_serviceLookup_model.selectedServiceGroupID())
        l_contractLineDetails_model.serviceGroupItemID(l_serviceLookup_model.selectedServiceGroupItemID())
    });
}

function ListAllServices() {
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
        method: "getServiceswithfilters",
        parameters: {
            "serviceName": $("#input_serviceSearchFilter").val(),
            "offset": "0",
            "limit": "200"
        },
        success: function (data) {
            addDataToServicesLookup(data.servicesAndGroups.FindZ_INT_ServicesResponse.Service, l_serviceLookup_model);
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving services and service groups. Contact your administrator."), 10000);
            return false;
        }
    });
}

function addDataToServicesLookup(iElementList, iModel) {
    iModel.Services.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            iElementList.forEach(function (iElement) {
                iModel.Services.push(iElement);
            });
        }
        else {
            iModel.Services.push(iElementList);
        }
    }
}

var ServicesLookupModel = function () {
    var self = this;
    self.Services = ko.observableArray([]);
    self.selectedServiceName = ko.observable('');
    self.selectedServiceID = ko.observable('');
    self.selectedServiceItemID = ko.observable('');
    self.selectedServiceGroupName = ko.observable('');
    self.selectedServiceGroupID = ko.observable('');
    self.selectedServiceGroupItemID = ko.observable('');
    self.onSelectServiceRow = function (iItem, event) {
        $(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
        self.selectedServiceName(getTextValue(iItem.Name));
        self.selectedServiceID(getTextValue(iItem['Service-id'].Id1));
        self.selectedServiceItemID(getTextValue(iItem['Service-id'].ItemId1));
        self.selectedServiceGroupName(getTextValue(iItem.Owner.Name));
        self.selectedServiceGroupID(getTextValue(iItem['Service-id'].Id));
        self.selectedServiceGroupItemID(getTextValue(iItem['Service-id'].ItemId));
    }
    self.onSelectServiceRadioButton = function (iItem, event) {
        $(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).addClass("cc-radio-on");
        self.selectedServiceName(getTextValue(iItem.Name));
        self.selectedServiceID(getTextValue(iItem['Service-id'].Id1));
        self.selectedServiceItemID(getTextValue(iItem['Service-id'].ItemId1));
        self.selectedServiceGroupName(getTextValue(iItem.Owner.Name));
        self.selectedServiceGroupID(getTextValue(iItem['Service-id'].Id));
        self.selectedServiceGroupItemID(getTextValue(iItem['Service-id'].ItemId));
        event.stopPropagation();
    }
};

function openUOMSelectionModal() {
    $('#div_UOMLookupModal').modal({
        backdrop: 'static',
        keyboard: false
    })
    ListAllUOMs();
    $('button#btn_selectUOMYes').off("click");
    $('button#btn_selectUOMYes').on('click', function (_event) {
        l_contractLineDetails_model.units(l_UOMLookup_model.selectedUOMName())
        l_contractLineDetails_model.unitsID(l_UOMLookup_model.selectedUOMID())
        l_contractLineDetails_model.unitsItemID(l_UOMLookup_model.selectedUOMItemID())
    });
}

function ListAllUOMs() {
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
        method: "GetUOMswithfilters",
        parameters: {
            "UOM": $("#input_UOMSearchFilter").val(),
            "offset": "0",
            "limit": "200"
        },
        success: function (data) {
            addDataToUOMsLookup(data.UOMs.FindZ_INT_UOMListResponse.UnitOfMeasurement, l_UOMLookup_model);
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving UOMs. Contact your administrator."), 10000);
            return false;
        }
    });
}

function addDataToUOMsLookup(iElementList, iModel) {
    iModel.UOMs.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            iElementList.forEach(function (iElement) {
                iModel.UOMs.push(iElement);
            });
        }
        else {
            iModel.UOMs.push(iElementList);
        }
    }
}

var UOMLookupModel = function () {
    var self = this;
    self.UOMs = ko.observableArray([]);
    self.selectedUOMName = ko.observable('');
    self.selectedUOMID = ko.observable('');
    self.selectedUOMItemID = ko.observable('');
    self.onSelectUOMRow = function (iItem, event) {
        $(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
        self.selectedUOMName(getTextValue(iItem.Unit));
        self.selectedUOMID(getTextValue(iItem['UnitOfMeasurement-id'].Id));
        self.selectedUOMItemID(getTextValue(iItem['UnitOfMeasurement-id'].ItemId));
    }
    self.onSelectUOMRadioButton = function (iItem, event) {
        $(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).addClass("cc-radio-on");
        self.selectedUOMName(getTextValue(iItem.Unit));
        self.selectedUOMID(getTextValue(iItem['UnitOfMeasurement-id'].Id));
        self.selectedUOMItemID(getTextValue(iItem['UnitOfMeasurement-id'].ItemId));
        event.stopPropagation();
    }
};

function openPOsSelectionModal() {
    $('#div_POsLookupModal').modal({
        backdrop: 'static',
        keyboard: false
    })
    ListAllPOs();
    $('button#btn_selectPOYes').off("click");
    $('button#btn_selectPOYes').on('click', function (_event) {
        l_contractLineDetails_model.poNumber(l_POLookup_model.selectedPOName())
        l_contractLineDetails_model.poNumberID(l_POLookup_model.selectedPOID())
        l_contractLineDetails_model.poNumberItemID(l_POLookup_model.selectedPOItemID())
    });
}

function ListAllPOs() {
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
        method: "GetPOswithfilters",
        parameters: {
            "contractID": contractID,
            "PONumber": $("#input_POSearchFilter").val(),
            "offset": "0",
            "limit": "200"
        },
        success: function (data) {
            addDataToPOsLookup(data.PoNumbers.FindZ_INT_PONumbersResponse.RelatedPOs, l_POLookup_model);
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving PO numbers. Contact your administrator."), 10000);
            return false;
        }
    });
}

function addDataToPOsLookup(iElementList, iModel) {
    iModel.POs.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            iElementList.forEach(function (iElement) {
                iModel.POs.push(iElement);
            });
        }
        else {
            iModel.POs.push(iElementList);
        }
    }
}

var POLookupModel = function () {
    var self = this;
    self.POs = ko.observableArray([]);
    self.selectedPOName = ko.observable('');
    self.selectedPOID = ko.observable('');
    self.selectedPOItemID = ko.observable('');
    self.onSelectPORow = function (iItem, event) {
        $(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
        self.selectedPOName(getTextValue(iItem.PONumber));
        self.selectedPOID(getTextValue(iItem['RelatedPOs-id'].Id));
        self.selectedPOItemID(getTextValue(iItem['RelatedPOs-id'].ItemId));
    }
    self.onSelectPORadioButton = function (iItem, event) {
        $(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).addClass("cc-radio-on");
        self.selectedPOName(getTextValue(iItem.PONumber));
        self.selectedPOID(getTextValue(iItem['RelatedPOs-id'].Id));
        self.selectedPOItemID(getTextValue(iItem['RelatedPOs-id'].ItemId));
        event.stopPropagation();
    }
};
// -------code for lookups end------ 
$(function () {
    /*
    createToastDiv();

    l_serviceLookup_model = new ServicesLookupModel();
    ko.applyBindings(l_serviceLookup_model, document.getElementById("div_serviceLookupModal"));
    l_UOMLookup_model = new UOMLookupModel();
    ko.applyBindings(l_UOMLookup_model, document.getElementById("div_UOMLookupModal"));
    l_POLookup_model = new POLookupModel();
    ko.applyBindings(l_POLookup_model, document.getElementById("div_POsLookupModal"));

    l_contractLines_model = new ContractLinesModel();
    ko.applyBindings(l_contractLines_model, document.getElementById("div_contractLinesData"));
    l_contractLinesFilter_model = new ContractLinesFilterModel();
    ko.applyBindings(l_contractLinesFilter_model, document.getElementById("div_contractLinesFilter"));
    l_contractLineDetails_model = new ContractLineDetailsModel();
    ko.applyBindings(l_contractLineDetails_model, document.getElementById("div_createOrUpdateContractLineModal"));
    hideCTRLinesFilter();
    listContractLines();

    $("#btn_filterContractLines").click(function (iEventObject) {
        if ($("#div_contractLinesFilter").attr('apps-toggle') == "expanded") {
            $("#div_contractLinesFilter").toggle();
            document.getElementById("div_contractLinesFilter").setAttribute("apps-toggle", 'collapsed');
            $("#div_contractLinesData").removeClass("col-md-9");
            $("#div_contractLinesData").addClass("col-md-12");
        }
        else if ($("#div_contractLinesFilter").attr('apps-toggle') == "collapsed") {
            $("#div_contractLinesFilter").toggle();
            //setTimeout(function () { $("#div_contractLinesFilter").toggle('slow'); }, 0);
            document.getElementById("div_contractLinesFilter").setAttribute("apps-toggle", 'expanded');
            $("#div_contractLinesData").removeClass("col-md-12");
            $("#div_contractLinesData").addClass("col-md-9");
        }
    });
    $(".cc-filter-header").click(function (iEventObject) {
        var l_headerSpan = $(this)
        l_headerSpan.next().slideToggle();
        if (l_headerSpan.attr('apps-toggle') == "expanded") {
            hideOrShowCTRLinesFilterContainerBody(l_headerSpan[0], false);
        }
        else if (l_headerSpan.attr('apps-toggle') == "collapsed") {
            hideOrShowCTRLinesFilterContainerBody(l_headerSpan[0], true);
        }
    });*/
});