$.cordys.json.defaults.removeNamespacePrefix = true;
var listOffsetValue = 0;
var listLimitValue = 25;
var pagination_obj;

var l_templateForSelection_model;
var o_templateForSelection_model;
var BASEURL;

//Create Contract Model
var CreateCTRwithTEMModel = function () {
    var self = this;
    self.clickable = false;
    self.contractRequestID = ko.observable();

    self.contractTypeItemID = ko.observable('');

    self.contractTemplateName = ko.observable('');
    self.contractTemplateItemID = ko.observable('');
	self.contractTemplateType = ko.observable('Internal template');

    self.comments = ko.observable();
	
	self.onTemplateTypeChange = function(iItem, event){
        self.contractTemplateName('');
        self.contractTemplateItemID('');
    }
}

// Template related Models
var TemplateListModel = function () {
    var self = this;
    self.TemplatesList = ko.observableArray([]);
    self.selectedTemplateItemID = ko.observable('');
    self.selectedTemplateName = ko.observable('');
    self.numOfTemplatesInCurrentPage = ko.observable('');
    self.numOfPages = ko.observable('');
    self.numOfTemplates = ko.observable('');
    self.currentPage = ko.observable(1);
    self.numOfItems = ko.observable('');
    self.l_templatesFilter_model = new TemplateFilterModel();

    self.clearFilter = function () {
        self.l_templatesFilter_model.ClearFilter();
        $("#id_clearFilterActionBar_Template").css('display', 'none');
    }
    self.selectTemplateRadioButton = function (iItem, event) {
        $(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");

        if (iItem["GCTemplate-id"]) {
            var l_itemId = iItem["GCTemplate-id"].ItemId;
            self.selectedTemplateItemID(l_itemId);
            self.selectedTemplateName(iItem.Name);
        }
    }
    self.onTemplateRowRadioButtonValueChanged = function (iItem, event) {
        $(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).addClass("cc-radio-on");

        if (iItem["GCTemplate-id"]) {
            var l_itemId = iItem["GCTemplate-id"].ItemId;
            self.selectedTemplateItemID(l_itemId);
            self.selectedTemplateName(iItem.Name);
        }
        event.stopPropagation();
    }

    self.ListRecords = function () {
        $.cordys.ajax({
            namespace: "http://schemas/OpenTextContentLibrary/16.5",
            method: "GetTemplateswithFilters",
            parameters: l_templateForSelection_model.l_templatesFilter_model.getFilterObject(),

            success: function (data) {
                addDataToTemplatesLookup(data.outputResponse.FindZ_INT_TemplatesForLookupResponse.GCTemplate, l_templateForSelection_model);

                if (data.outputResponse.FindZ_INT_TemplatesForLookupResponse["@total"]) {
                    l_templateForSelection_model.numOfItems(data.outputResponse.FindZ_INT_TemplatesForLookupResponse["@total"]);
                }
                else {
                    l_templateForSelection_model.numOfItems(0);
                }

                if (l_templateForSelection_model.numOfItems() != 0) {
                    l_templateForSelection_model.numOfPages(Math.ceil(l_templateForSelection_model.numOfItems() / listLimitValue));
                } else {
                    l_templateForSelection_model.numOfPages(1);
                }

                updatePaginationParams();
            },
            error: function (responseFailure) {
                showOrHideErrorInfo("div_modalErrorInfoAreaTemplate", true, "Unable to retrieve the template list. Contact your administrator.", 10000);
                return false;
            }
        });
    }
}
var TemplateFilterModel = function () {
    var self = this;
    var l_templateNameFilterField = document.getElementById("filter_templateName");
    var l_templateIDFilterField = document.getElementById("filter_templateID");
    var l_templateDescFilterField = document.getElementById("filter_templateDescription");
    var l_contractTypeField = document.getElementById("filter_contractType");
    var l_stateFilterField = document.getElementById("filter_state");

    self.ClearFilter = function () {
        l_templateNameFilterField.value = "";
        l_templateIDFilterField.value = "";
        l_templateDescFilterField.value = "";
        l_contractTypeField.value = "";
        l_stateFilterField.value = "";
    }
    self.getFilterObject = function () {

        self.currentFilterObject = {
            "templateName": l_templateNameFilterField.value,
            "description": l_templateDescFilterField.value,
            "templateID": l_templateIDFilterField.value,
            "contractType": l_contractTypeField.value,
            "state": l_stateFilterField.value,
            "contractTypeID": getIDfromItemID(l_createCTRwithTEM_model.contractTypeItemID()),
			"templateType": l_createCTRwithTEM_model.contractTemplateType(),
            "offset": listOffsetValue,
            "limit": listLimitValue,
        };
        return self.currentFilterObject;
    }
}

function openTemplateSelectionModal() {

    $("#div_selectTemplateModal").modal({
        backdrop: 'static',
        keyboard: false
    });
    hideFilter_Template();
    clearTemplateSelectionForm();
    var ids = l_createCTRwithTEM_model.contractTypeItemID().split(".");
    pagination_obj = o_templateForSelection_model;
    pagination_obj.model_obj.ListRecords();

    listOffsetValue = 0;
    listLimitValue = 25;

    $('button#btn_selectTemplateForContractYes').off("click");
    $('button#btn_selectTemplateForContractYes').on('click', function (_event) {
        l_createCTRwithTEM_model.contractTemplateName(l_templateForSelection_model.selectedTemplateName());
        l_createCTRwithTEM_model.contractTemplateItemID(l_templateForSelection_model.selectedTemplateItemID());
        updateTemplateID(getIDfromItemID(l_createCTRwithTEM_model.contractTemplateItemID()));
        l_templateForSelection_model.clearFilter();
    });
}

function clearTemplateSelectionForm() {
    l_templateForSelection_model.selectedTemplateName('');
}

function addDataToTemplatesLookup(iElementList, iModel) {
    iModel.TemplatesList.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            iModel.numOfTemplatesInCurrentPage(iElementList.length);
            iElementList.forEach(function (iElement) {
                iModel.TemplatesList.push(iElement);
            });
        }
        else {
            iModel.numOfTemplatesInCurrentPage("1");
            iModel.TemplatesList.push(iElementList);
        }
    }
}

//Template filter related methods
function ApplyFilter_Template(event, iSrcElement) {
    pagination_obj.model_obj.ListRecords();
    if (document.getElementById("filter_templateName").value != "" || document.getElementById("filter_templateID").value != "" || document.getElementById("filter_contractType").value != "" || document.getElementById("filter_templateDescription").value != "" || document.getElementById("filter_state").value != "") {
        $("#id_clearFilterActionBar_Template").css('display', 'inline');
    } else {
        $("#id_clearFilterActionBar_Template").css('display', 'none');
    }
    hideFilter_Template();
}

function ClearFilter_Template(event, iSrcElement) {
    l_templateForSelection_model.l_templatesFilter_model.ClearFilter();
    pagination_obj.model_obj.ListRecords();
    $("#id_clearFilterActionBar_Template").css('display', 'none');
    hideFilter_Template();
}

function hideFilter_Template() {
    $("#id_templateFilter").hide();
    document.getElementById("id_templateFilter").setAttribute("apps-toggle", 'collapsed');
    $("#div_templates").removeClass("col-md-9 col-sm-9");
    $("#div_templates").addClass("col-md-12 col-sm-12");
}

function toggleFilter_template() {
    if ($("#id_templateFilter").attr('apps-toggle') == "expanded") {
        $("#id_templateFilter").toggle();
        document.getElementById("id_templateFilter").setAttribute("apps-toggle", 'collapsed');
        $("#div_templates").removeClass("col-md-9 col-sm-9");
        $("#div_templates").addClass("col-md-12 col-sm-12");
    }
    else if ($("#id_templateFilter").attr('apps-toggle') == "collapsed") {
        $("#id_templateFilter").toggle();
        document.getElementById("id_templateFilter").setAttribute("apps-toggle", 'expanded');
        $("#div_templates").removeClass("col-md-12 col-sm-12");
        $("#div_templates").addClass("col-md-9 col-sm-9");
    }
}

function hideOrShowFilterContainerBody(iElement, iShow) {
    if (iShow) {
        iElement.setAttribute("apps-toggle", 'expanded');
        iElement.lastElementChild.src = "../../../../../com/opentext/apps/utils/img/caret_up.svg";
    }
    else {
        iElement.setAttribute("apps-toggle", 'collapsed');
        iElement.lastElementChild.src = "../../../../../com/opentext/apps/utils/img/caret_down.svg";
    }
}

//common methods for pagination
function updateLimitValue(iElement) {
    listOffsetValue = 0;
    pagination_obj.model_obj.currentPage('1');
    listLimitValue = $(iElement).val();
    pagination_obj.model_obj.ListRecords();
}

function updatePaginationParams() {
    if (pagination_obj.model_obj.currentPage() == 1) {
        document.getElementById(pagination_obj.left_nav).style.display = "none";
        document.getElementById(pagination_obj.right_nav).style.display = "inline";
    }
    if (parseInt(pagination_obj.model_obj.numOfItems()) <= parseInt(listLimitValue)) {
        pagination_obj.model_obj.currentPage('1');
        document.getElementById(pagination_obj.right_nav).style.display = 'none';
        document.getElementById(pagination_obj.left_nav).style.display = 'none';
    }
}

function goToLastPage() {
    listOffsetValue = (Math.ceil(pagination_obj.model_obj.numOfItems() / listLimitValue) - 1) * listLimitValue;
    pagination_obj.model_obj.currentPage(Math.ceil(pagination_obj.model_obj.numOfItems() / listLimitValue));
    document.getElementById(pagination_obj.right_nav).style.display = 'none';
    document.getElementById(pagination_obj.left_nav).style.display = 'inline';
    pagination_obj.model_obj.ListRecords();
}

function goToFirstPage() {
    listOffsetValue = 0;
    pagination_obj.model_obj.currentPage('1');
    document.getElementById(pagination_obj.right_nav).style.display = 'inline';
    document.getElementById(pagination_obj.left_nav).style.display = 'none';
    pagination_obj.model_obj.ListRecords();
}

function goToPreviousPage() {
    if (pagination_obj.model_obj.currentPage() > 1) {
        listOffsetValue = parseInt(listOffsetValue) - parseInt(listLimitValue);
        pagination_obj.model_obj.currentPage(parseInt(pagination_obj.model_obj.currentPage()) - 1);
    }
    if (pagination_obj.model_obj.currentPage() < Math.ceil(pagination_obj.model_obj.numOfItems() / listLimitValue)) {
        document.getElementById(pagination_obj.right_nav).style.removeProperty("display");
    }
    if (pagination_obj.model_obj.currentPage() == 1) {
        document.getElementById(pagination_obj.left_nav).style.display = "none";
    }
    if (pagination_obj.model_obj.currentPage() < 1) {
        return;
    }
    pagination_obj.model_obj.ListRecords();
}

function goToNextPage() {
    if (pagination_obj.model_obj.currentPage() < Math.ceil(pagination_obj.model_obj.numOfItems() / listLimitValue)) {
        listOffsetValue = parseInt(listOffsetValue) + parseInt(listLimitValue);
        pagination_obj.model_obj.currentPage(isNaN(parseInt(pagination_obj.model_obj.currentPage())) ? 0 : parseInt(pagination_obj.model_obj.currentPage()));
        pagination_obj.model_obj.currentPage(parseInt(pagination_obj.model_obj.currentPage()) + 1);
    }
    if (pagination_obj.model_obj.currentPage() == Math.ceil(pagination_obj.model_obj.numOfItems() / listLimitValue)) {
        document.getElementById(pagination_obj.right_nav).style.display = "none";
    }
    if (pagination_obj.model_obj.currentPage() > 1) {
        document.getElementById(pagination_obj.left_nav).style.removeProperty("display");
    }
    pagination_obj.model_obj.ListRecords();
}

$(document).ready(function () {
    var i_locale = getlocale();
    var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
    translateLabels("com/opentext/apps/contractcenter/ContractCenter/ContractCenter", i_locale, true);
    loadRTLIfRequired(i_locale, rtl_css);

    var cInstanceId = getUrlParameterValue("instanceId", null, true);

    l_createCTRwithTEM_model = new CreateCTRwithTEMModel(cInstanceId);
    ko.applyBindings(l_createCTRwithTEM_model, document.getElementById("div_createCTRwithTEMForm"));

    l_templateForSelection_model = new TemplateListModel();
    ko.applyBindings(l_templateForSelection_model, document.getElementById("div_selectTemplateModal"));

    o_templateForSelection_model = { model_obj: l_templateForSelection_model, left_nav: "li_templateListLeftNavigation", right_nav: "li_templateListRightNavigation" };

    //close and open caret icons in filter pane
    $(".cc-filter-header").click(function (iEventObject) {
        var l_headerSpan = $(this);
        l_headerSpan.next().slideToggle();
        if (l_headerSpan.attr('apps-toggle') == "expanded") {
            hideOrShowFilterContainerBody(l_headerSpan[0], false);
        }
        else if (l_headerSpan.attr('apps-toggle') == "collapsed") {
            hideOrShowFilterContainerBody(l_headerSpan[0], true);
        }
    });

    filldefaultContractInitiationData(cInstanceId);
	
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

function filldefaultContractInitiationData(i_InstanceID) {
    l_createCTRwithTEM_model.clickable = true;
    $.cordys.ajax(
        {
            namespace: "http://schemas/OpenTextContractInitiation/ContractRequest/operations",
            method: "ReadContractRequest",
            parameters:
            {
                "ContractRequest-id":
                {
                    "ItemId": i_InstanceID
                }
            },
            success: function (data) {
                if (data && data.ContractRequest) {
                    var contractRequestData = data.ContractRequest;
                    l_createCTRwithTEM_model.contractRequestID(contractRequestData["ContractRequest-id"].Id);
                    l_createCTRwithTEM_model.comments(contractRequestData.Comments);
                    l_createCTRwithTEM_model.contractTypeItemID(getTextValue(contractRequestData.RelatedType["GCType-id"].ItemId));
                }
            },
            error: function (responseFailure) {
                showOrHideErrorInfo("div_formErrorInfoArea", true, getTranslationMessage("Unable to retrieve contract request details. Contact your administrator."), 10000);
                return false;
            }
        });
}

function updateCommentsValue(val) {
    if (val != "") {
        return val;
    }
    else {
        return { '@xsi:nil': 'true' };
    }
}

function updateComments(value) {
    $.cordys.ajax(
        {
            method: "UpdateContractRequest",
            namespace: "http://schemas/OpenTextContractInitiation/ContractRequest/operations",
            parameters:
            {
                "ContractRequest-id": {
                    "Id": l_createCTRwithTEM_model.contractRequestID()
                },
                "Contract-update": {
                    "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance",
                    "Comments": updateCommentsValue(value)
                }
            },
        }).done(function (data) {
            // reloadFrame();

        }).fail(function (error) {

        })
}

function updateTemplateIDValue(val) {
    if (val != "") {
        return val;
    }
    else {
        return { '@xsi:nil': 'true' };
    }
}

function updateTemplateID(value) {
    $.cordys.ajax(
        {
            method: "UpdateContractRequest",
            namespace: "http://schemas/OpenTextContractInitiation/ContractRequest/operations",
            parameters:
            {
                "ContractRequest-id": {
                    "Id": l_createCTRwithTEM_model.contractRequestID()
                },
                "Contract-update": {
                    "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance",
                    "Z_INT_TemplateId": updateTemplateIDValue(value),
					"TemplateType":l_createCTRwithTEM_model.contractTemplateType()
                }
            },
        }).done(function (data) {
            // reloadFrame();
            
        }).fail(function (error) {
            
        })
}

function checkforMandatoryFields() {
    var validationFlag = true;

    if (l_createCTRwithTEM_model.contractTemplateItemID() == "" || l_createCTRwithTEM_model.contractTemplateItemID() == undefined) {
        $("#input_template").addClass("cc-error");
        validationFlag = false;
    }

    return validationFlag;
}
function removeErrorClass(iEvent) {
    $(iEvent).removeClass("cc-error");
}