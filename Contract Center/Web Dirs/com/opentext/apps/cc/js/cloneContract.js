$.cordys.json.defaults.removeNamespacePrefix = true;
var l_contractItemID = "";
var l_cloneContractDetailsModell_cloneContractDetailsModel;
var listOffsetValue = 0;
var listLimitValue = 25;
var pagination_obj;

var l_organizationsForSelection_model;
//organization obj for pagination
var o_organizationsForSelection_model;

var l_ctrTypeForSelection_model;
var o_ctrTypeForSelection_model;

var l_templateForSelection_model;
var o_templateForSelection_model;
var BASEURL;
var newContractId;
var i_locale;

//Contract Clone Model
var CloneContractDetailsModel = function () {

    var self = this;
    self.contractOrgName = ko.observable('');
    self.contractOrgItemID = ko.observable('');

    self.contractTypeName = ko.observable('');
    self.contractTypeItemID = ko.observable('');
    self.clickable = false;

    self.contractTemplateName = ko.observable('');
    self.contractTemplateItemID = ko.observable('');
    self.contractTemplateType = ko.observable('');

    self.isExecuted = ko.observable('');
    self.isExternalContract = ko.observable('');
    self.contractTerm = ko.observable('');
    self.isPerpetual = ko.observable('');
    self.actualStartDate = ko.observable('');
    self.actualStartDatetoLocale = ko.observable('');
    self.autoRenewal = ko.observable('');
    self.autoRenewDuration = ko.observable('');
    self.cloneDocument = ko.observable('false')
    
    self.clonedContractItemId = ko.observable('');
    self.clonedContractId = ko.observable('');
    
    self.contractTypeItemID.subscribe(function (newval) {
        if (newval) {
            var ids = newval.split(".");
            if (l_cloneContractDetailsModel.contractTemplateItemID) {
                l_cloneContractDetailsModel.contractTemplateItemID("");
                l_cloneContractDetailsModel.contractTemplateName("");
                if (self.isExternalContract() == 'EXTERNALDOCUMENT' || self.isExecuted() == 'true' || self.contractTemplateType()=='None') {
                    $("#input_template").next().css('cursor', 'not-allowed');
                }

            }
        }
    });

    self.autoRenewal.subscribe(function (currVal) {
        if (currVal == 'false') {
            self.autoRenewDuration('');
            $('#input_autoRenewDuration').attr('readonly', true);
            $('#input_autoRenewDuration').attr("style", $('#input_autoRenewDuration').attr("style") + "background-color : #EEEEEE !important; cursor : not-allowed");
        }
        else {
            $('#input_autoRenewDuration').attr('readonly', false);
            $("#input_autoRenewDuration").css('cursor', '');
            $('#input_autoRenewDuration').css("background-color", "");
        }
        $('#input_autoRenewDuration').removeClass("cc-error");
    });

    self.isPerpetual.subscribe(function (currVal) {
        if (currVal == 'true') {
            self.contractTerm('');
            self.autoRenewDuration('');
            self.autoRenewal('false');
            $('#input_contractTerm').attr("style", $('#input_contractTerm').attr("style") + "background-color : #EEEEEE !important; cursor : not-allowed");

        }
        else {
            $("#input_contractTerm").css('cursor', '');
            $('#input_contractTerm').css("background-color", "");
        }
        $('#input_contractTerm').removeClass("cc-error");
    });

    self.onPerpetualCheckboxChanged = function (iItem, event) {
        if ($(event.currentTarget).hasClass("cc-checkbox-on")) {
            self.isPerpetual("false");
        }
        else if ($(event.currentTarget).hasClass("cc-checkbox-off")) {
            self.isPerpetual("true");
			closeContractTermModal();
        }
        event.stopPropagation();
    }

    self.onCloneDocCheckboxChanged = function (iItem, event) {
        if ($(event.currentTarget).hasClass("cc-checkbox-on")) {
            self.cloneDocument("false");
        }
        else if ($(event.currentTarget).hasClass("cc-checkbox-off")) {
            self.cloneDocument("true");

        }
        event.stopPropagation();
    }

    self.changeRadioButton = function (iItem, event) {
        if (event.currentTarget.firstElementChild.className.indexOf("cc-radio-off") > 0) {
            if (event.currentTarget.parentElement.id == "div_isExecutedInputs") {
                self.isExecuted(self.isExecuted() == 'true' ? 'false' : 'true');
            }
            else if (event.currentTarget.parentElement.id == "div_isExternalInputs") {
                self.isExternalContract(self.isExternalContract() == 'EXTERNALDOCUMENT' ? 'INTERNALTEMPLATE' : 'EXTERNALDOCUMENT');
            }
            else if (event.currentTarget.parentElement.id == "div_autoRenewInputs") {
                self.autoRenewal(self.autoRenewal() == 'true' ? 'false' : 'true');
				if(self.autoRenewal()=='false')
					closeAutoRenewDurModal();
            }
        }
        if (self.isExecuted() == 'true' || self.isExternalContract() == 'EXTERNALDOCUMENT' || self.contractTypeName() == '') {
            $("#input_template").next().css('cursor', 'not-allowed');
            self.contractTemplateName('');
            self.contractTemplateItemID('');
            self.contractTemplateType("None");
            $("#select_templateType").prop("disabled", true);
        }
        else {
            //$("#input_template").next().css('cursor', 'pointer');
            $("#select_templateType").css('cursor', 'pointer');
            $("#select_templateType").prop("disabled", false);
        }
        event.stopPropagation();
    }
    self.onTemplateTypeChange = function(iItem, event){
        self.contractTemplateName('');
        self.contractTemplateItemID('');
    }
    self.contractTemplateType.subscribe(function (currVal) {
         if(currVal == 'None'){
            $('#input_template').attr("style", $('#input_template').attr("style") + "background-color : #EEEEEE !important; cursor : not-allowed");
            $('#input_template').next().attr("style", $('#input_template').attr("style") + "background-color : #EEEEEE !important; cursor : not-allowed");
            
         }
          else{
             $("#input_template").next().css('cursor', 'pointer');
             $('#input_template').next().css("background-color", "");
             $("#input_template").css('cursor', 'pointer');
             $('#input_template').css("background-color", "");
            
          }
    });
    
    self.bindJqueryFunc = function () {
        var format = "yy-mm-dd";
        $("#input_actualStartDate").datepicker({
            dateFormat: format,
            orientation: "bottom",
            onSelect: function (dateText, inst) {
                self.actualStartDate(dateText);
                self.actualStartDatetoLocale(formateDatetoLocale(dateText));
            }
        });
    };
}

// Organization related Models
var OrganizationsListModel = function () {
    var self = this;
    self.OrganizationsList = ko.observableArray([]);
    self.selectedOrganizationItemID = ko.observable('');
    self.selectedOrganizationName = ko.observable('');
    self.numOfOrgsInCurrentPage = ko.observable('');
    self.numOfPages = ko.observable('');
    self.numOfItems = ko.observable('');
    self.currentPage = ko.observable(1);
    self.l_organizationsFilter_model = new OrgFilterModel();

    self.clearFilter = function(){
        self.l_organizationsFilter_model.ClearFilter();
        $("#id_clearFilterActionBar_Org").css('display', 'none');
    }

    self.selectOrganizationRadioButton = function (iItem, event) {
        $(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");

        if (iItem["GCOrganization-id"]) {
            var l_itemId = iItem["GCOrganization-id"].ItemId;
            self.selectedOrganizationItemID(l_itemId);
            self.selectedOrganizationName(iItem.Name);
        }
    }
    self.onOrganizationRowRadioButtonValueChanged = function (iItem, event) {
        $(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).addClass("cc-radio-on");

        if (iItem["GCOrganization-id"]) {
            var l_itemId = iItem["GCOrganization-id"].ItemId;
            self.selectedOrganizationItemID(l_itemId);
            self.selectedOrganizationName(iItem.Name);
        }
        event.stopPropagation();
    }

    self.ListRecords = function () {
        $.cordys.ajax({
            namespace: "http://schemas.opentext.com/apps/cc/basiccomponents/20.2",
            method: "GetAllAccessibleOrganizations",
            parameters: l_organizationsForSelection_model.l_organizationsFilter_model.getFilterObject(),

            success: function (data) {

                addDataToOrganizationsLookup(data.AccessibleOrganizations.FindZ_INT_AllAccessibleOrgsResponse.GCOrganization, l_organizationsForSelection_model);

                if (data.AccessibleOrganizations.FindZ_INT_AllAccessibleOrgsResponse["@total"]) {
                    l_organizationsForSelection_model.numOfItems(data.AccessibleOrganizations.FindZ_INT_AllAccessibleOrgsResponse["@total"]);
                }
                else {
                    l_organizationsForSelection_model.numOfItems(0);
                }

                if (l_organizationsForSelection_model.numOfItems() != 0) {
                    l_organizationsForSelection_model.numOfPages(Math.ceil(l_organizationsForSelection_model.numOfItems() / listLimitValue));
                } else {
                    l_organizationsForSelection_model.numOfPages(1);
                }

                updatePaginationParams();
            },
            error: function (responseFailure) {
                showOrHideErrorInfo("div_modalErrorInfoAreaOrg", true, "Unable to retrieve the organization list. Contact your administrator.", 10000);
                return false;
            }
        });
    }
}
var OrgFilterModel = function () {
    var self = this;
    var l_orgNameFilterField = document.getElementById("filter_orgName");
    var l_parentOrgFilterField = document.getElementById("filter_parentOrg");
    var l_orgDescFilterField = document.getElementById("filter_orgDescription");

    self.ClearFilter = function () {
        l_orgNameFilterField.value = "";
        l_parentOrgFilterField.value = "";
        l_orgDescFilterField.value = "";
    }
    self.getFilterObject = function () {

        self.currentFilterObject = {
            "organizationName": l_orgNameFilterField.value,
            "organizationDescription": l_orgDescFilterField.value,
            "parentOrganization": l_parentOrgFilterField.value,
            "offset": listOffsetValue,
            "limit": listLimitValue,
        };
        return self.currentFilterObject;
    }
}

function openOrganizationSelectionModal() {
    $("#div_selectOrganizationModal").modal({
        backdrop: 'static',
        keyboard: false
    });
    hideFilter_Org();
    clearOrganizationSelectionForm();

    pagination_obj = o_organizationsForSelection_model;
    pagination_obj.model_obj.ListRecords();
    listOffsetValue = 0;
    listLimitValue = 25;

    $('button#btn_selectOrganizationForContractYes').off("click");
    $('button#btn_selectOrganizationForContractYes').on('click', function (_event) {
        l_cloneContractDetailsModel.contractOrgName(l_organizationsForSelection_model.selectedOrganizationName());
        l_cloneContractDetailsModel.contractOrgItemID(l_organizationsForSelection_model.selectedOrganizationItemID());
		l_cloneContractDetailsModel.contractTypeName("");
        l_cloneContractDetailsModel.contractTypeItemID("");
		if (!l_cloneContractDetailsModel.contractOrgName()) {
            l_cloneContractDetailsModel.contractOrgItemID("");
        }
        l_organizationsForSelection_model.clearFilter();
    });
}

function clearOrganizationSelectionForm() {
    l_organizationsForSelection_model.selectedOrganizationName('');
}

function addDataToOrganizationsLookup(iElementList, iModel) {
    iModel.OrganizationsList.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            iModel.numOfOrgsInCurrentPage(iElementList.length);
            iElementList.forEach(function (iElement) {
                if (!iElement.ParentOrganization) {
                    iElement["ParentOrganization"] = null;
                }
                iModel.OrganizationsList.push(iElement);
            });
        }
        else {
            iModel.numOfOrgsInCurrentPage("1");
            if (!iElementList.ParentOrganization) {
                iElementList["ParentOrganization"] = null;
            }
            iModel.OrganizationsList.push(iElementList);
        }
    }
}

//organization filter related methods
function ApplyFilter_Org(event, iSrcElement) {
    pagination_obj.model_obj.ListRecords();
    if (document.getElementById("filter_orgName").value != "" || document.getElementById("filter_orgDescription").value != "" || document.getElementById("filter_parentOrg").value != "") {
        $("#id_clearFilterActionBar_Org").css('display', 'inline');
    } else {
        $("#id_clearFilterActionBar_Org").css('display', 'none');
    }
    hideFilter_Org();
}

function ClearFilter_Org(event, iSrcElement) {
    l_organizationsForSelection_model.l_organizationsFilter_model.ClearFilter();
    pagination_obj.model_obj.ListRecords();
    $("#id_clearFilterActionBar_Org").css('display', 'none');
    hideFilter_Org();
}
function hideFilter_Org() {
    $("#id_organizationFilter").hide();
    document.getElementById("id_organizationFilter").setAttribute("apps-toggle", 'collapsed');
    $("#div_organizations").removeClass("col-md-9 col-sm-9");
    $("#div_organizations").addClass("col-md-12 col-sm-12");
}

function toggleFilter_Org() {
    if ($("#id_organizationFilter").attr('apps-toggle') == "expanded") {
        $("#id_organizationFilter").toggle();
        document.getElementById("id_organizationFilter").setAttribute("apps-toggle", 'collapsed');
        $("#div_organizations").removeClass("col-md-9 col-sm-9");
        $("#div_organizations").addClass("col-md-12 col-sm-12");
    }
    else if ($("#id_organizationFilter").attr('apps-toggle') == "collapsed") {
        $("#id_organizationFilter").toggle();
        document.getElementById("id_organizationFilter").setAttribute("apps-toggle", 'expanded');
        $("#div_organizations").removeClass("col-md-12 col-sm-12");
        $("#div_organizations").addClass("col-md-9 col-sm-9");
    }
}


// Contract Type related Models
var CtrTypeListModel = function () {
    var self = this;
    self.CtrTypesList = ko.observableArray([]);
    self.selectedCtrTypeItemID = ko.observable('');
    self.selectedCtrTypeName = ko.observable('');
    self.numOfTypesInCurrentPage = ko.observable('');
    self.numOfPages = ko.observable('');
    self.numOfCtrTypes = ko.observable('');
    self.currentPage = ko.observable(1);
    self.numOfItems = ko.observable('');
    self.l_ctrTypesFilter_model = new CtrTypeFilterModel();

    self.clearFilter = function(){
        self.l_ctrTypesFilter_model.ClearFilter();
        $("#id_clearFilterActionBar_CtrType").css('display', 'none');
    }

    self.selectCtrTypeRadioButton = function (iItem, event) {
        $(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");

        if (iItem["GCType-id"]) {
            var l_itemId = iItem["GCType-id"].ItemId;
            self.selectedCtrTypeItemID(l_itemId);
            self.selectedCtrTypeName(iItem.Name);
        }
    }
    self.onCtrTypeRowRadioButtonValueChanged = function (iItem, event) {
        $(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).addClass("cc-radio-on");

        if (iItem["GCType-id"]) {
            var l_itemId = iItem["GCType-id"].ItemId;
            self.selectedCtrTypeItemID(l_itemId);
            self.selectedCtrTypeName(iItem.Name);
            if (l_cloneContractDetailsModel.contractTemplateItemID) {
                l_cloneContractDetailsModel.contractTemplateItemID.value = "";
                l_cloneContractDetailsModel.contractTemplateName.value = "";
            }
        }
        event.stopPropagation();
    }

    self.ListRecords = function () {
        $.cordys.ajax({
            namespace: "http://schemas.opentext.com/apps/cc/basiccomponents/20.2",
            method: "GetFilteredCtrTypes",
            parameters: l_ctrTypeForSelection_model.l_ctrTypesFilter_model.getFilterObject(),

            success: function (data) {
                addDataToCtrTypesLookup(data.filteredTypes.FindZ_INT_FilteredTypesResponse.GCType, l_ctrTypeForSelection_model);

                if (data.filteredTypes.FindZ_INT_FilteredTypesResponse["@total"]) {
                    l_ctrTypeForSelection_model.numOfItems(data.filteredTypes.FindZ_INT_FilteredTypesResponse["@total"]);
                }
                else {
                    l_ctrTypeForSelection_model.numOfItems(0);
                }

                if (l_ctrTypeForSelection_model.numOfItems() != 0) {
                    l_ctrTypeForSelection_model.numOfPages(Math.ceil(l_ctrTypeForSelection_model.numOfItems() / listLimitValue));
                } else {
                    l_ctrTypeForSelection_model.numOfPages(1);
                }

                updatePaginationParams();
            },
            error: function (responseFailure) {
                showOrHideErrorInfo("div_modalErrorInfoAreaCtrType", true, "Unable to retrieve the contract types list. Contact your administrator.", 10000);
                return false;
            }
        });
    }
}
var CtrTypeFilterModel = function () {
    var self = this;
    var l_ctrTypeFilterField = document.getElementById("filter_ctrType");
    var l_intentTypeFilterField = document.getElementById("filter_intentType");
    var l_ctrTypeDescFilterField = document.getElementById("filter_typeDescription");
    var l_statusFilterField = document.getElementById("filter_status");

    self.ClearFilter = function () {
        l_ctrTypeFilterField.value = "";
        l_intentTypeFilterField.value = "";
        l_ctrTypeDescFilterField.value = "";
        l_statusFilterField.value = "";
    }
    self.getFilterObject = function () {
        self.currentFilterObject = {
            "Name": l_ctrTypeFilterField.value,
            "Description": l_ctrTypeDescFilterField.value,
            "IntentType": l_intentTypeFilterField.value,
            "Status": l_statusFilterField.value,
			"OrgID": l_cloneContractDetailsModel.contractOrgItemID().split('.')[1],
            "offset": listOffsetValue,
            "limit": listLimitValue,
        };
        return self.currentFilterObject;
    }
}

function openCtrTypeSelectionModal() {
    $("#div_selectCtrTypeModal").modal({
        backdrop: 'static',
        keyboard: false
    });
    hideFilter_CtrType();
    clearCtrTypeSelectionForm();

    pagination_obj = o_ctrTypeForSelection_model;
    pagination_obj.model_obj.ListRecords();

    listOffsetValue = 0;
    listLimitValue = 25;

    $('button#btn_selectCtrTypeForContractYes').off("click");
    $('button#btn_selectCtrTypeForContractYes').on('click', function (_event) {
        l_cloneContractDetailsModel.contractTypeName(l_ctrTypeForSelection_model.selectedCtrTypeName());
	
        l_cloneContractDetailsModel.contractTypeItemID(l_ctrTypeForSelection_model.selectedCtrTypeItemID());
        if (!l_cloneContractDetailsModel.contractTypeName()) {
            l_cloneContractDetailsModel.contractTemplateName("");
            l_cloneContractDetailsModel.contractTemplateItemID("");
            l_cloneContractDetailsModel.contractTypeItemID("");
        }
        l_ctrTypeForSelection_model.clearFilter();
    });
}

function clearCtrTypeSelectionForm() {
    l_ctrTypeForSelection_model.selectedCtrTypeName('');
}

function addDataToCtrTypesLookup(iElementList, iModel) {
    iModel.CtrTypesList.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            iModel.numOfTypesInCurrentPage(iElementList.length);
            iElementList.forEach(function (iElement) {
                if (iElement.Status) {
                    var status = iElement.Status;
                    iElement["Status"] = status.charAt(0) + status.substring(1).toLowerCase();
                }
                iModel.CtrTypesList.push(iElement);
            });
        }
        else {
            iModel.numOfTypesInCurrentPage("1");
            if (iElementList.Status) {
                var status = iElementList.Status;
                iElementList["Status"] = status.charAt(0) + status.substring(1).toLowerCase();
            }
            iModel.CtrTypesList.push(iElementList);
        }
    }
}

//Contract Type filter related methods
function ApplyFilter_CtrType(event, iSrcElement) {
    pagination_obj.model_obj.ListRecords();
    if (document.getElementById("filter_ctrType").value != "" || document.getElementById("filter_intentType").value != "" || document.getElementById("filter_typeDescription").value != "" || document.getElementById("filter_status").value != "") {
        $("#id_clearFilterActionBar_CtrType").css('display', 'inline');
    } else {
        $("#id_clearFilterActionBar_CtrType").css('display', 'none');
    }
    hideFilter_CtrType();
}

function ClearFilter_CtrType(event, iSrcElement) {
    l_ctrTypeForSelection_model.l_ctrTypesFilter_model.ClearFilter();
    pagination_obj.model_obj.ListRecords();
    $("#id_clearFilterActionBar_CtrType").css('display', 'none');
    hideFilter_CtrType();
}
function hideFilter_CtrType() {
    $("#id_ctrTypeFilter").hide();
    document.getElementById("id_ctrTypeFilter").setAttribute("apps-toggle", 'collapsed');
    $("#div_ctrTypes").removeClass("col-md-9 col-sm-9");
    $("#div_ctrTypes").addClass("col-md-12 col-sm-12");
}

function toggleFilter_ctrType() {
    if ($("#id_ctrTypeFilter").attr('apps-toggle') == "expanded") {
        $("#id_ctrTypeFilter").toggle();
        document.getElementById("id_ctrTypeFilter").setAttribute("apps-toggle", 'collapsed');
        $("#div_ctrTypes").removeClass("col-md-9 col-sm-9");
        $("#div_ctrTypes").addClass("col-md-12 col-sm-12");
    }
    else if ($("#id_ctrTypeFilter").attr('apps-toggle') == "collapsed") {
        $("#id_ctrTypeFilter").toggle();
        document.getElementById("id_ctrTypeFilter").setAttribute("apps-toggle", 'expanded');
        $("#div_ctrTypes").removeClass("col-md-12 col-sm-12");
        $("#div_ctrTypes").addClass("col-md-9 col-sm-9");
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

    self.clearFilter = function(){
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
        $('#input_template').removeClass("cc-error");
    }
    self.onTemplateRowRadioButtonValueChanged = function (iItem, event) {
        $(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).addClass("cc-radio-on");

        if (iItem["GCTemplate-id"]) {
            var l_itemId = iItem["GCTemplate-id"].ItemId;
            self.selectedTemplateItemID(l_itemId);
            self.selectedTemplateName(iItem.Name);
        }
        $('#input_template').removeClass("cc-error");
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
            "contractTypeID": getIDfromItemID(l_cloneContractDetailsModel.contractTypeItemID()),
            "templateType": l_cloneContractDetailsModel.contractTemplateType(),
            "offset": listOffsetValue,
            "limit": listLimitValue,
        };
        return self.currentFilterObject;
    }
}

function openTemplateSelectionModal() {
    if (!l_cloneContractDetailsModel.contractTypeName() || l_cloneContractDetailsModel.isExecuted() == "true" || l_cloneContractDetailsModel.isExternalContract() == "EXTERNALDOCUMENT" || l_cloneContractDetailsModel.contractTemplateType()=="None") {
        return;
    }

    $("#div_selectTemplateModal").modal({
        backdrop: 'static',
        keyboard: false
    });
    hideFilter_Template();
    clearTemplateSelectionForm();
    var ids = l_cloneContractDetailsModel.contractTypeItemID().split(".");
    pagination_obj = o_templateForSelection_model;
    pagination_obj.model_obj.ListRecords();

    listOffsetValue = 0;
    listLimitValue = 25;

    $('button#btn_selectTemplateForContractYes').off("click");
    $('button#btn_selectTemplateForContractYes').on('click', function (_event) {
        l_cloneContractDetailsModel.contractTemplateName(l_templateForSelection_model.selectedTemplateName());
        l_cloneContractDetailsModel.contractTemplateItemID(l_templateForSelection_model.selectedTemplateItemID());
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

//Contract term duration modal
function openContractTermModal(attrData) {
	if( $('#ContractTermPreviewModel').css("display")=="none" && l_cloneContractDetailsModel.isPerpetual()=="false")
		$('#ContractTermPreviewModel').css("display","block");
	else
		$('#ContractTermPreviewModel').css("display","none");
	
	addDataToContractTermLookup();
     $('button#btn_contractTermYes').off("click");
     $('button#btn_contractTermYes').on('click', function (_event) {
		 if(!$("#input_contractTermMonths").val().match(/^0*$/) && !$("#input_contractTermDays").val().match(/^0*$/)){
			l_cloneContractDetailsModel.contractTerm($("#input_contractTermMonths").val()+" month(s), "+$("#input_contractTermDays").val()+" day(s)");
		}
		 else if(!$("#input_contractTermMonths").val().match(/^0*$/)){
			 l_cloneContractDetailsModel.contractTerm($("#input_contractTermMonths").val()+" month(s)");
		 }
		 else if(!$("#input_contractTermDays").val().match(/^0*$/)){
			 l_cloneContractDetailsModel.contractTerm($("#input_contractTermDays").val()+" day(s)");
		 }
		 else{
			 l_cloneContractDetailsModel.contractTerm("");
		 }
		 $('#ContractTermPreviewModel').css("display","none");
		 removeErrorClass("#input_contractTerm")
     });
}

function closeContractTermModal(attrData) {
	$('#ContractTermPreviewModel').css("display","none");
}

function addDataToContractTermLookup() {
	var months="";
	var days="";
	if(l_cloneContractDetailsModel.contractTerm().lastIndexOf("month(s)") > 0){
		months=l_cloneContractDetailsModel.contractTerm().substring(0, l_cloneContractDetailsModel.contractTerm().lastIndexOf(" month(s)"))
	}
	if(l_cloneContractDetailsModel.contractTerm().lastIndexOf("day(s)") > 0){
		if(months!=""){
			days=l_cloneContractDetailsModel.contractTerm().substring(l_cloneContractDetailsModel.contractTerm().lastIndexOf("month(s), ")+"month(s), ".length, l_cloneContractDetailsModel.contractTerm().lastIndexOf(" day(s)"))
		}
		else{
			days=l_cloneContractDetailsModel.contractTerm().substring(0, l_cloneContractDetailsModel.contractTerm().lastIndexOf(" day(s)"))
		}
	}
	$("#input_contractTermMonths").val(months);
	$("#input_contractTermDays").val(days);
}

//Auto renew duration modal
function openAutoRenewDurModal(attrData) {
	if( $('#AutoRenewDurPreviewModel').css("display")=="none" && l_cloneContractDetailsModel.autoRenewal()=="true")
		$('#AutoRenewDurPreviewModel').css("display","block");
	else
		$('#AutoRenewDurPreviewModel').css("display","none");
	
	addDataToAutoRenewDurLookup();
    $('button#btn_autoRenewDurYes').off("click");
    $('button#btn_autoRenewDurYes').on('click', function (_event) {
		if(!$("#input_autoRenewDurMonths").val().match(/^0*$/) && !$("#input_autoRenewDurDays").val().match(/^0*$/)){
			l_cloneContractDetailsModel.autoRenewDuration($("#input_autoRenewDurMonths").val()+" month(s), "+$("#input_autoRenewDurDays").val()+" day(s)");
		}
		else if(!$("#input_autoRenewDurMonths").val().match(/^0*$/)){
			l_cloneContractDetailsModel.autoRenewDuration($("#input_autoRenewDurMonths").val()+" month(s)");
		}
		else if(!$("#input_autoRenewDurDays").val().match(/^0*$/)){
			l_cloneContractDetailsModel.autoRenewDuration($("#input_autoRenewDurDays").val()+" day(s)");
		}
		else{
			l_cloneContractDetailsModel.autoRenewDuration("");
		}
		$('#AutoRenewDurPreviewModel').css("display","none");
		removeErrorClass("#input_autoRenewDuration")
    });
}

function closeAutoRenewDurModal(attrData) {
	$('#AutoRenewDurPreviewModel').css("display","none");
}

function addDataToAutoRenewDurLookup() {
	var months="";
	var days="";
	if(l_cloneContractDetailsModel.autoRenewDuration().lastIndexOf("month(s)") > 0){
		months=l_cloneContractDetailsModel.autoRenewDuration().substring(0, l_cloneContractDetailsModel.autoRenewDuration().lastIndexOf(" month(s)"))
	}
	if(l_cloneContractDetailsModel.autoRenewDuration().lastIndexOf("day(s)") > 0){
		if(months!=""){
			days=l_cloneContractDetailsModel.autoRenewDuration().substring(l_cloneContractDetailsModel.autoRenewDuration().lastIndexOf("month(s), ")+"month(s), ".length, l_cloneContractDetailsModel.autoRenewDuration().lastIndexOf(" day(s)"))
		}
		else{
			days=l_cloneContractDetailsModel.autoRenewDuration().substring(0, l_cloneContractDetailsModel.autoRenewDuration().lastIndexOf(" day(s)"))
		}
	}
	$("#input_autoRenewDurMonths").val(months);
	$("#input_autoRenewDurDays").val(days);
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
    i_locale = getlocale();
    var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
    translateLabels("com/opentext/apps/contractcenter/ContractCenter/ContractCenter", i_locale, true);
    loadRTLIfRequired(i_locale, rtl_css);

    var cInstanceId = getUrlParameterValue("instanceId", null, true);
    l_contractItemID = cInstanceId;
    BASEURL = getUrlParameterValue("baseURL", null, true);

    l_cloneContractDetailsModel = new CloneContractDetailsModel(cInstanceId);
    ko.applyBindings(l_cloneContractDetailsModel, document.getElementById("div_cloneContractForm"));

    setTimeout(function () {
        l_cloneContractDetailsModel.bindJqueryFunc();
    }, 0);

    l_organizationsForSelection_model = new OrganizationsListModel();
    ko.applyBindings(l_organizationsForSelection_model, document.getElementById("div_selectOrganizationModal"));

    o_organizationsForSelection_model = { model_obj: l_organizationsForSelection_model, left_nav: "li_OrgsListLeftNavigation", right_nav: "li_OrgsListRightNavigation" };

    l_ctrTypeForSelection_model = new CtrTypeListModel();
    ko.applyBindings(l_ctrTypeForSelection_model, document.getElementById("div_selectCtrTypeModal"));

    o_ctrTypeForSelection_model = { model_obj: l_ctrTypeForSelection_model, left_nav: "li_ctrTypeListLeftNavigation", right_nav: "li_ctrTypeListRightNavigation" };

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

    platformDialogModifications("Clone", cloneWithUpdates);
    filldefaultContractData(cInstanceId);
});

function translatePlaceHolders() {
    var elems = window.parent.document.getElementsByClassName("btn-translate");
    if (elems) {
        for (var ind = 0; ind < elems.length; ind++) {
            elems[ind].innerHTML = getTranslationMessage(elems[ind].innerHTML);
            elems[ind].title = elems[ind].innerHTML;
        }
    }
}

function modifyFormAfterClone(i_newButtonName, generatedContractId) {

    $('ai-dialog-header',window.parent.document).prepend("<div id=\"successToast\" style=\"font-size: 14px;color: #211e1eeb;border-bottom: 3px solid green;background-color: white;text-align: center;transition: visibility 0s 0.5s, opacity 0.5s linear;position:absolute;border-radius:3px;padding:16px;left:38%\"></div> <div id=\"errorToast\"> <div id=\"toastHeading\"> <div id=\"headingContent\"></div> <span style=\"position:absolute;cursor:pointer;right:1em;top:1.3em\" id=\"arrowBtn\" class=\"down\"></span> </div> <div id=\"toastContent\" style=\"display: none;clear:both;\"> <div class=\"horizontal-rule\"></div> <div id=\"contentText\" style=\"margin-top: 6px;padding-left: 6px\" align=\"left\">" + 
    getTranslationMessage("") + "</div></div></div>");

    var msg = "Contract has been cloned with specific values";
    var toastObj = window.parent.document.getElementById("successToast");
    toastObj.innerHTML = getTranslationMessage(msg);
    //toastObj.innerHTML = '<img src=\"../../../../../com/opentext/apps/utils/img/notification_success.svg\" width=\"25px\" height=\"25px\" align=\"middle\" style=\"margin-right:5px;\">' + getTranslationMessage(msg)

    $('ai-dialog-header',window.parent.document).append("<div style=\"font-size: 14px;color: #211e1eeb;margin-left: 4%;\">Document upload to the cloned contract is optional</div>")
    
	
    var cancelButton = $('ai-dialog-footer .btn-secondary:contains("Cancel")', window.parent.document);
    var newBtn = document.createElement("button");
	newBtn.ccPublicAPIProvider = getPublicAPIProvider(window);
    newBtn.innerHTML = getTranslationMessage(i_newButtonName);
    newBtn.className = "btn btn-secondary btn-translate";
    newBtn.style.setProperty("margin", "12px 12px", "important");
    newBtn.title = generatedContractId;
    newBtn.setAttribute("onclick","this.ccPublicAPIProvider.navigate('"+l_cloneContractDetailsModel.clonedContractItemId+"',{'layoutID':'F8B156D635F3A1E89CB08DDB9883E4C8', 'clearBreadcrumb':false,'breadcrumbName':''})");
    cancelButton.before(newBtn);
    cancelButton.text(getTranslationMessage("Close"));

}

function openDocumentsPanel() {
    $.cordys.ajax
        ({
            namespace: "http://schemas/OpenTextBasicComponents/GCProperties/operations",
            method: "GetPropertyByName",
            parameters:
            {
                "Name": "DOCUMENT_REPOSITORY"
            },
            success: function (data) {
                if (data.GCProperties) {
                    if (data.GCProperties.length) {
                        if (data.GCProperties[0].value == 'CONTENT') {
                            var redirection_URL = "../../../../../app/start/web/perform/item/" + newContractId + "/98FA9B1A7A8BA1ED8B990B5C409FC465";
                            if (window.navigator.language !== i_locale) {
                                redirection_URL = setUrlParameter(redirection_URL, 'language', i_locale);
                            }
                            window.location.href = redirection_URL;
                        }
                        else if (data.GCProperties[0].value == 'BUSINESS_WORKSPACE') {
                            var redirection_URL = "../../../../../app/start/web/perform/item/" + newContractId + "/FC7774E8DA63A1ED8C8D213D90668466";

                            if (window.navigator.language !== i_locale) {
                                redirection_URL = setUrlParameter(redirection_URL, 'language', i_locale);
                            }
                            window.location.href = redirection_URL;
                        }
                        else if (data.GCProperties[0].value == 'BOTH') {
                            var redirection_URL = "../../../../../app/start/web/perform/item/" + newContractId + "/FC7774E8DA63A1ED8C8D213D90624466";
                            if (window.navigator.language !== i_locale) {
                                redirection_URL = setUrlParameter(redirection_URL, 'language', i_locale);
                            }
                            window.location.href = redirection_URL;
                        }
                    }
                    else {
                        if (data.GCProperties.value == 'CONTENT') {
                            var redirection_URL = "../../../../../app/start/web/perform/item/" + newContractId + "/98FA9B1A7A8BA1ED8B990B5C409FC465";

                            if (window.navigator.language !== i_locale) {
                                redirection_URL = setUrlParameter(redirection_URL, 'language', i_locale);
                            }
                            window.location.href = redirection_URL;
                        }
                        else if (data.GCProperties.value == 'BUSINESS_WORKSPACE') {
                            var redirection_URL = "../../../../../app/start/web/perform/item/" + newContractId + "/FC7774E8DA63A1ED8C8D213D90668466";
                            if (window.navigator.language !== i_locale) {
                                redirection_URL = setUrlParameter(redirection_URL, 'language', i_locale);
                            }
                            window.location.href = redirection_URL;
                        }
                        else if (data.GCProperties.value == 'BOTH') {
                            var redirection_URL = "../../../../../app/start/web/perform/item/" + newContractId + "/FC7774E8DA63A1ED8C8D213D90624466";
                            if (window.navigator.language !== i_locale) {
                                redirection_URL = setUrlParameter(redirection_URL, 'language', i_locale);
                            }
                            window.location.href = redirection_URL;
                        }
                    }
                }
                else {
                    var redirection_URL = "../../../../../app/start/web/perform/item/" + newContractId + "/B4B676CD53D8A1E8B2E9A54D7EEF8876?openContentInParentWindow=true";
                    if (window.navigator.language !== i_locale) {
                        redirection_URL = setUrlParameter(redirection_URL, 'language', i_locale);
                    }
                    window.location.href = redirection_URL;
                }
            }
        });
}

function platformDialogModifications(i_newButtonName, i_newButtonAction) {
    //hide OK button
    hideOKButton();

    //Save changes action in footer
    var newBtn = document.createElement("Button");
    newBtn.innerHTML = i_newButtonName;
    newBtn.className = "btn btn-primary btn-translate";
    newBtn.onclick = i_newButtonAction;

    $('ai-dialog-footer .btn-primary', window.parent.document).before(newBtn);

    $('ai-dialog', window.parent.document).animate({
        'max-height': '100vh',
        'max-width': '60vw',
        'width': '51vw',
        'height': '96vh'
    }, 500);

    //Dialog content style enhancements            
    $('ai-dialog-body iframe', window.parent.document).css({
        'width': '100%',
        'height': 'calc(100% - 6px)'
    });

    $('ai-dialog-body', window.parent.document).css({
        'max-height': 'calc(98vh - 7.5em)',
        'height': '100%',
    });

    $('.layout-panel .panel-container', window.parent.document).css({
        'padding-left': '0px'
    });

    $('panel-container iframe', window.parent.document).css({
        'height': 'calc(100% - 6px)',
        'width': '100%',
        'border': '0px'
    });
}

function filldefaultContractData(i_InstanceID) {
    l_cloneContractDetailsModel.clickable = true;
    $.cordys.ajax(
        {
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            method: "GetContractDataforClone",
            parameters:
            {
                "contractID": getIDfromItemID(i_InstanceID)
            },
            success: function (data) {
                if (data) {
                    var contractData = data.outputResponse.FindZ_INT_ContractListForCloneResponse.Contract;
                    l_cloneContractDetailsModel.contractOrgName(getTextValue(contractData["RelatedOrganization"].Name));
                    l_cloneContractDetailsModel.contractOrgItemID(getTextValue(contractData["RelatedOrganization"]["GCOrganization-id"].ItemId));
					
                    l_cloneContractDetailsModel.contractTypeName(getTextValue(contractData["ContractType"].Name));
                    l_cloneContractDetailsModel.contractTypeItemID(getTextValue(contractData["ContractType"]["GCType-id"].ItemId));

                    l_cloneContractDetailsModel.isExecuted(getTextValue(contractData.IsExecuted));
                    l_cloneContractDetailsModel.isExternalContract(getTextValue(contractData.ContractDocumentType));
                    var l_contractTermDuration = getTextValue(contractData.InitialContractTenure);
					if(l_contractTermDuration.lastIndexOf("M") > 0 && l_contractTermDuration.lastIndexOf("D") > 0 && getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("M"))) > 0 && getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("M") + 1, l_contractTermDuration.lastIndexOf("D"))) > 0) {
						l_cloneContractDetailsModel.contractTerm(getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("M")))+" month(s), "+getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("M") + 1, l_contractTermDuration.lastIndexOf("D")))+" day(s)");
					}
					else if (l_contractTermDuration.lastIndexOf("M") > 0 && getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("M"))) > 0) {
						l_cloneContractDetailsModel.contractTerm(getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("M")))+" month(s)");
					}
					else if(l_contractTermDuration.lastIndexOf("D") > 0 && getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("D"))) > 0){
						l_cloneContractDetailsModel.contractTerm(getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("D")))+" day(s)");
					} else {
						l_cloneContractDetailsModel.contractTerm("");
					}
                    l_cloneContractDetailsModel.actualStartDate(getTextValue(contractData.MinStartdate).replace('Z', ''));
                    l_cloneContractDetailsModel.actualStartDatetoLocale(formateDatetoLocale(getTextValue(contractData.MinStartdate)));
                    var l_autoRenewDuration = getTextValue(contractData.AutoRenewDuration);
					if(l_autoRenewDuration.lastIndexOf("M") > 0 && l_autoRenewDuration.lastIndexOf("D") > 0 && getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("M"))) > 0 && getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("M") + 1, l_autoRenewDuration.lastIndexOf("D"))) > 0) {
						l_cloneContractDetailsModel.autoRenewDuration(getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("M")))+" month(s), "+getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("M") + 1, l_autoRenewDuration.lastIndexOf("D")))+" day(s)");
					}
					else if (l_autoRenewDuration.lastIndexOf("M") > 0 && getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("M"))) > 0) {
						l_cloneContractDetailsModel.autoRenewDuration(getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("M")))+" month(s)");
					}
					else if(l_autoRenewDuration.lastIndexOf("D") > 0 && getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("D"))) > 0){
						l_cloneContractDetailsModel.autoRenewDuration(getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("D")))+" day(s)");
					} else {
						l_cloneContractDetailsModel.autoRenewDuration("");
					}
                    l_cloneContractDetailsModel.autoRenewal(getTextValue(contractData.AutoRenew));
                    l_cloneContractDetailsModel.isPerpetual(getTextValue(contractData.Perpetual));

                    if (l_cloneContractDetailsModel.isExecuted() == "true" || l_cloneContractDetailsModel.isExternalContract() == "EXTERNALDOCUMENT") {
                        l_cloneContractDetailsModel.contractTemplateName("");
                        l_cloneContractDetailsModel.contractTemplateType("");
                        l_cloneContractDetailsModel.contractTemplateItemID("");
                        $("#input_template").next().css('cursor', 'not-allowed');
                        $("#select_templateType").css('cursor', 'not-allowed');
                        $("#select_templateType").prop("disabled", true);
                    }
                    else {
                        l_cloneContractDetailsModel.contractTemplateName(getTextValue(contractData["RelatedTemplate"].Name));
                        l_cloneContractDetailsModel.contractTemplateItemID(getTextValue(contractData["RelatedTemplate"]["GCTemplate-id"].ItemId));
                        l_cloneContractDetailsModel.contractTemplateType(getTextValue(contractData.TemplateType));
                    }
                    translatePage();
                }
            },
            error: function (responseFailure) {
                showOrHideErrorInfo("div_formErrorInfoArea", true, getTranslationMessage("Unable to retrieve contract details. Contact your administrator."), 10000);
                return false;
            }
        });
}

function cloneWithUpdates() {
    if (checkforMandatoryFields()) {
        if (l_cloneContractDetailsModel.clickable) {
            l_cloneContractDetailsModel.clickable = false;
            var duration = "", term = "";
			if((l_cloneContractDetailsModel.autoRenewDuration() != undefined && l_cloneContractDetailsModel.autoRenewDuration() != "")){
				var months="";
				var days="";
				if(l_cloneContractDetailsModel.autoRenewDuration().lastIndexOf("month(s)") > 0){
					months=l_cloneContractDetailsModel.autoRenewDuration().substring(0, l_cloneContractDetailsModel.autoRenewDuration().lastIndexOf(" month(s)"))
				}
				if(l_cloneContractDetailsModel.autoRenewDuration().lastIndexOf("day(s)") > 0){
					if(months!=""){
						days=l_cloneContractDetailsModel.autoRenewDuration().substring(l_cloneContractDetailsModel.autoRenewDuration().lastIndexOf("month(s), ")+"month(s), ".length, l_cloneContractDetailsModel.autoRenewDuration().lastIndexOf(" day(s)"))
						duration = ("P" + months + "M" + days + "D");
					}
					else{
						days=l_cloneContractDetailsModel.autoRenewDuration().substring(0, l_cloneContractDetailsModel.autoRenewDuration().lastIndexOf(" day(s)"))
						duration = ("P" + days + "D");
					}
				}
				else{
					duration = ("P" + months + "M");
				}
			}else{
				duration = "";
			}
			if((l_cloneContractDetailsModel.contractTerm() != undefined && l_cloneContractDetailsModel.contractTerm() != "")){
				var months="";
				var days="";
				if(l_cloneContractDetailsModel.contractTerm().lastIndexOf("month(s)") > 0){
					months=l_cloneContractDetailsModel.contractTerm().substring(0, l_cloneContractDetailsModel.contractTerm().lastIndexOf(" month(s)"))
				}
				if(l_cloneContractDetailsModel.contractTerm().lastIndexOf("day(s)") > 0){
					if(months!=""){
						days=l_cloneContractDetailsModel.contractTerm().substring(l_cloneContractDetailsModel.contractTerm().lastIndexOf("month(s), ")+"month(s), ".length, l_cloneContractDetailsModel.contractTerm().lastIndexOf(" day(s)"))
						term = ("P" + months + "M" + days + "D");
					}
					else{
						days=l_cloneContractDetailsModel.contractTerm().substring(0, l_cloneContractDetailsModel.contractTerm().lastIndexOf(" day(s)"))
						term = ("P" + days + "D");
					}
				}
				else{
					term = ("P" + months + "M");
				}
			}else{
				term = "";
			}
            $.cordys.ajax({
                namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
                method: "CloneContractWithUpdates",
                parameters: {
                    "contractItemID": l_contractItemID,
                    "contractOrgItemID": l_cloneContractDetailsModel.contractOrgItemID(),
                    "contractTypeItemID": l_cloneContractDetailsModel.contractTypeItemID(),
                    "contractTemplateItemID": l_cloneContractDetailsModel.contractTemplateItemID(),
                    "isExecuted": l_cloneContractDetailsModel.isExecuted(),
                    "isExternalContract": l_cloneContractDetailsModel.isExternalContract(),
                    "contractTerm": term,
                    "isPerpetual": l_cloneContractDetailsModel.isPerpetual(),
                    "actualStartDate": l_cloneContractDetailsModel.actualStartDate(),
                    "autoRenewal": l_cloneContractDetailsModel.autoRenewal(),
                    "autoRenewDuration": duration,
                    "contractTemplateType":  l_cloneContractDetailsModel.contractTemplateType(),
                    "cloneDocument":  l_cloneContractDetailsModel.cloneDocument()
                },
                success: function (data) {

                    if (data.outputResponse.ID) {
                        $('#div_cloneContractForm').css('display', 'none');
                        $('ai-dialog-footer .btn-primary:contains("Clone")', window.parent.document).hide()

                        var childId = data.outputResponse.ID;
                        var generatedContractId = data.outputResponse.generatedContractId;
                        var clonedURL = clonedContractURL(BASEURL, childId);
                       // document.getElementById("clonedContractURL").setAttribute("href", clonedURL);
                        //document.getElementById("clonedContractURL").innerHTML = generatedContractId;
                        l_cloneContractDetailsModel.clonedContractId=data.outputResponse.ID;
                        l_cloneContractDetailsModel.clonedContractItemId=l_contractItemID.split(".")[0]+'.'+l_cloneContractDetailsModel.clonedContractId;
                        newContractId = l_cloneContractDetailsModel.clonedContractItemId;
                        modifyFormAfterClone("Open cloned contract", generatedContractId);
                    }
                    setTimeout(function () { 
                        window.parent.document.getElementById("successToast").style.display = "none"; 
                        openDocumentsPanel();
                    }, 2000);

                    //$('#div_cloneSuccessMessg').css('display', 'block');
                    //$('ai-dialog-footer .btn-primary:contains("Clone")', window.parent.document).hide()
                    //$('ai-dialog-footer .btn:contains("Cancel")', window.parent.document).text("Close");
                },
                error: function (responseFailure) {
                    l_cloneContractDetailsModel.clickable = true;
                    showOrHideErrorInfo("div_formErrorInfoArea", true, getTranslationMessage("Unable to clone the contract. Contact your administrator."), 10000);
                    return false;
                }
            });
        }
    }
}

function openClonedContract() {
    var l_ItemId =l_cloneContractDetailsModel.clonedContractItemId;
    var l_Id = l_cloneContractDetailsModel.clonedContractId;
    navigateToInstance(l_ItemId,"Contract",{"layoutID":'F8B156D635F3A1E89CB08DDB9883E4C8', "clearBreadcrumb":false,"breadcrumbName" : ''})
}


function clonedContractURL(BASEURL, CHILDID) {
    return BASEURL + "app/start/web/perform/item/005056C00008A1E795653A59509D399D." + CHILDID + "/F8B156D635F3A1E89CB08DDB9883E4C8";
}
function checkforMandatoryFields() {
    var validationFlag = true;
    var $regexExp = /^([1-9]|[1-9][0-9]|[1-9][0-9][0-9])$/;

    if (l_cloneContractDetailsModel.contractOrgItemID() == "" || l_cloneContractDetailsModel.contractOrgItemID() == undefined) {
        $("#input_organization").addClass("cc-error");
        validationFlag = false;
    }
    if (l_cloneContractDetailsModel.contractTypeItemID() == "" || l_cloneContractDetailsModel.contractTypeItemID() == undefined) {
        $("#input_contractType").addClass("cc-error");
        validationFlag = false;
    }
    if ((l_cloneContractDetailsModel.contractTemplateItemID() == "" || l_cloneContractDetailsModel.contractTemplateItemID() == undefined) && !(l_cloneContractDetailsModel.isExecuted() == 'true' || l_cloneContractDetailsModel.isExternalContract() == 'EXTERNALDOCUMENT')) {
        $("#input_template").addClass("cc-error");
        validationFlag = false;
    }
    if (l_cloneContractDetailsModel.isPerpetual() == 'false' && (l_cloneContractDetailsModel.contractTerm() == "" || l_cloneContractDetailsModel.contractTerm() == undefined || $("#input_contractTerm").val()=="" )) {
        $("#input_contractTerm").addClass("cc-error");
        validationFlag = false;
    }
    if (l_cloneContractDetailsModel.actualStartDate() == "" || l_cloneContractDetailsModel.actualStartDate() == undefined) {
        $("#input_actualStartDate").addClass("cc-error");
        validationFlag = false;
    }

    if ((l_cloneContractDetailsModel.autoRenewDuration() == "" || l_cloneContractDetailsModel.autoRenewDuration() == undefined || $("#input_autoRenewDuration").val()=="") && l_cloneContractDetailsModel.autoRenewal() == 'true') {
        $("#input_autoRenewDuration").addClass("cc-error");
        validationFlag = false;
    }
    return validationFlag;
}
function removeErrorClass(iEvent) {
    $(iEvent).removeClass("cc-error");
}