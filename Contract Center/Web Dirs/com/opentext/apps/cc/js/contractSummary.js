$.cordys.json.defaults.removeNamespacePrefix = true;
var l_contractSummary_model;
var l_countryLookup_model;
var l_currencyLookup_model;
var l_amendTypeLookup_model;
var l_renewalFlagStatusLookup_model;
var l_terminationReasonLookup_model;
var l_documentOriginationLookup_model;
var l_contract_accounts_model;
var l_contract_lines_info_model;
var l_contract_tags_info_model;
var l_contract_PONumbers_model;

var _userStateEditabilityFlag = false;
var _userEditabilityFlag = false;
var currentTabIndex = 0;
var selectedPOListMap = {};

var contractID = getUrlParameterValue("instanceId", null, true).split(".")[1];
var contractItemID = getUrlParameterValue("instanceId", null, true);
var lifecycle_CurrentState = getUrlParameterValue("contractStatus", null, true);
var lifecycle_InternalState = getUrlParameterValue("ContractIntStatus", null, true);
var isExecutedContract = getUrlParameterValue("isExecutedContract", null, true);
// Constants.
const DATA_TYPE_BOOLEAN = "BOOLEAN";
const DATA_TYPE_NUMBER = "NUMBER";
const DATA_TYPE_TEXT = "TEXT";
const DATA_TYPE_DATE = "DATE";
const DATA_TYPE_ENUM = "ENUM";
const DATE_FORMAT_DD_MM_YYYY_1 = { label: "DD/MM/YYYY", value: "dd/mm/yy" };
const DEFAULT_DATE_FORMAT = { label: "MM/DD/YY", value: "mm/dd/yy" };
const DATE_FORMAT_DD_MMM_YYYY = { label: "DD-MMM-YYYY", value: "dd-M-yy" };
const DATE_FORMAT_DD_MM_YYYY = { label: "DD-MM-YYYY", value: "dd-mm-yy" };
const DATE_FORMAT_MM_DD_YYYY = { label: "MM-DD-YYYY", value: "mm-dd-yy" };
const DATE_FORMAT_MMM_DD_YYYY = { label: "MMM-DD-YYYY", value: "M-dd-yy" };
const DATE_FORMAT_YYYY_MM_DD = { label: "YYYY-MM-DD", value: "yy-mm-dd" };

const GROUP_ENABLE_TYPES = ["TERMINATE_INREVIEW_ENABLE"];
const GROUP_ENABLE_FIELDS = {
    "TERMINATE_INREVIEW_ENABLE": [
        "CancellationComments",
        "EarlyTerminationConditions",
        "TerminationFees",
        "TerminationNoticePeriod",
        "TerminationReason",
    ]
};

var contractSummary_Services = function () {
    var self = {};

    self.GetContractDetailsById = function (inreq, callbackfunc) {
        $.cordys.ajax({
            method: "GetContractDetailsById",
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            parameters: { "contractID": inreq },
            success: function (data) {
                callbackfunc(data, "SUCCESS");
            },
            error: function (responseFailure) {
                callbackfunc(null, "ERROR");
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving the contract details. Contact your administrator."), 10000);
                return false;
            }
        });
    };

    self.GetCustomAttrDetailsById = function (inreq, callbackfunc) {
        $.cordys.ajax({
            method: "GetMappedCustomAttributes",
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            parameters: { 'ContractItemId': getUrlParameterValue("instanceId", null, true) },
            success: function (data) {
                callbackfunc(data, "SUCCESS");
            },
            error: function (responseFailure) {
                callbackfunc(null, "ERROR");
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while loading the custom attributes. Contact your administrator."), 10000);
                return false;
            }
        });
    };

    self.getAllAttributesMappedtoLayoutService = (inreq, callbackfunc) => {
        $.cordys.ajax({
            namespace: "http://schemas.opentext.com/apps/cc/customattributes/21.4",
            method: "getAllLayoutRelAttrDef",
            parameters: inreq,
            success: function (data) {
                callbackfunc(data, "SUCCESS");
            },
            error: function (responseFailure) {
                callbackfunc(null, "ERROR");
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving the layout configuration. Contact your administrator."), 10000);
                return false;
            }
        });
    }

    self.getRelatedLayoutDataService = (inreq, callbackfunc) => {
        $.cordys.ajax({
            namespace: "http://schemas/OpenTextCustomAttributes/LayoutConfig/operations",
            method: "ReadLayoutConfig",
            parameters: inreq,
            success: function (data) {
                callbackfunc(data, "SUCCESS");
            },
            error: function (responseFailure) {
                callbackfunc(null, "ERROR");
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving the layout configuration. Contact your administrator."), 10000);
                return false;
            }
        });
    }

    self.GetContractLinesById = function (contractID, responseCallback) {
        $.cordys.ajax({
            method: "GetCTRLinesRelationsforDocGen ",
            namespace: "http://schemas.opentext.com/apps/contractcenter/18.4",
            parameters: { "ContractId": contractID },
            success: function (responseSuccess) {
                responseCallback(responseSuccess);
            },
            error: function (responseFailure) {
                responseCallback(responseFailure, "ERROR");
                return false;
            }
        });
    };

    self.AddContractRelatedTag = function (parameters, responseCallback) {
        $.cordys.ajax({
            method: "AddToContractRelatedTags",
            namespace: "http://schemas/OpenTextContractCenter/Contract/operations",
            parameters: parameters,
            success: function (responseSuccess) {
                responseCallback(responseSuccess);
            },
            error: function (responseFailure) {
                responseCallback(responseFailure, "ERROR");
                return false;
            }
        });
    };

    self.RemoveContractRelatedTag = function (parameters, responseCallback) {
        $.cordys.ajax({
            method: "RemoveFromContractRelatedTags",
            namespace: "http://schemas/OpenTextContractCenter/Contract/operations",
            parameters: parameters,
            success: function (responseSuccess) {
                responseCallback(responseSuccess);
            },
            error: function (responseFailure) {
                responseCallback(responseFailure, "ERROR");
                return false;
            }
        });
    };

    self.CreateNewTag = function (parameters, responseCallback) {
        $.cordys.ajax({
            method: "CreateGCTag",
            namespace: "http://schemas/OpenTextBasicComponents/GCTag/operations",
            parameters: parameters,
            success: function (responseSuccess) {
                responseCallback(responseSuccess);
            },
            error: function (responseFailure) {
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("Duplicate tags cannot be created."), 10000);
                responseCallback(responseFailure, "ERROR");
                return false;
            }
        });
    };

    self.GetContractCustomTabDetails = function (property_name, responseCallback) {
        $.cordys.ajax({
            method: "GetPropertyByName",
            namespace: "http://schemas/OpenTextBasicComponents/GCProperties/operations",
            parameters: { "Name": property_name },
            success: function (responseSuccess) {
                responseCallback(responseSuccess);
            },
            error: function (responseFailure) {
                responseCallback(responseFailure, "ERROR");
                return false;
            }
        });
    };

    self.UpdateCtrAttrValues = function (ctrAttrProps, responseCallback) {
        $.cordys.ajax({
            method: "UpdateCTRAttrValues",
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            parameters: ctrAttrProps,
            success: function (data) {
                responseCallback(data, "SUCCESS");
               
            },
            error: function (responseFailure) {
                responseCallback(null, "ERROR");
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while updating the contract data. Contact your administrator."), 10000);
                return false;
            }
        });
    };

    self.UpdateRuleAndNegInstance = function (contractItemID) {

        var l_params = {
            "type": "definition",
            "receiver": "com/opentext/apps/cc/contract/UpdateContractOnChange",
            "rootEntityInstanceId": contractItemID,
            "message": ""
        };

        $.cordys.ajax({
            method: "ExecuteProcess",
            namespace: "http://schemas.cordys.com/bpm/execution/1.0",
            parameters: l_params,
            success: function (data) {
            },
            error: function (responseFailure) {

            }
        });
    };

    self.RemoveWatcherToContractById = function (contractID, responseCallback) {
        $.cordys.ajax({
            method: "removeWatcherToContract",
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            parameters: { "contractId": contractID },
            success: function (responseSuccess) {
                responseCallback(responseSuccess);
            },
            error: function (responseFailure) {
                responseCallback(responseFailure, "ERROR");
                return false;
            }
        });
    };
    self.AddWatcherToContractById = function (contractID, responseCallback) {
        $.cordys.ajax({
            method: "addWatcherToContract",
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            parameters: { "contractId": contractID },
            success: function (responseSuccess) {
                responseCallback(responseSuccess);
            },
            error: function (responseFailure) {
                responseCallback(responseFailure, "ERROR");
                return false;
            }
        });
    };
    self.GetContractWatchers = function (parameters, responseCallback) {
        $.cordys.ajax({
            method: "getContractWatchers",
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            parameters: parameters,
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
}();

function RelatedAttrDef(data, parentLayout) {
    var self = {};
    self.Name = ko.observable();
    self.Id = ko.observable();
    self.Id1 = ko.observable();
    self.ItemId = ko.observable();
    self.ItemId1 = ko.observable();
    self.Order = ko.observable();
    self.Type = ko.observable();
    self.IsMandatory = ko.observable();
    self.GroupID = ko.observable();
    self.GroupName = ko.observable();
    self.IsReadOnly = ko.observable();
    self.isEditable = ko.observable(true);
    self.AttrDefId = ko.observable();
    self.AttrDefType = ko.observable();
    self.AttrDefItemId = ko.observable();
    self.ParentRelAttrDefId = ko.observable();
    self.ParentRelAttrDefId1 = ko.observable();
    self.ParentRelAttrDefItemId = ko.observable();
    self.ParentRelAttrDefItemId1 = ko.observable();
    self.ParentLayout = ko.observable(parentLayout);

    self.dateFormat = ko.observable();

    self.AttributeName = ko.observable()
    self.AttributeValue = ko.observable().extend({ trackValueChange: true });
    self.AttrSelectOptions = ko.observableArray([]);
    self.AttributeLookupMethod = ko.observable();
	self.AttributeClearMethod = ko.observable();
    self.LookupAttrID = ko.observable();
    self.LookupAttrItemID = ko.observable();

    self.CustAttributeId = ko.observable();
    self.CustAttributeId1 = ko.observable();

    // Custom attribute properties.
    self.id = "";
    self.name = "";
    self.label = "";

    self.attributeMedadata = "";
    self.attributeId = "";
    self.placeHolder = "";

    self.Style = ko.observable({ 'Class': ko.observable('flex-cn-cl-25') });

    self.openLookupforselection = function (attrData, event) {
        var l_modalToOpen = attrData.attr().AttributeLookupMethod();
        window[l_modalToOpen](attrData);
    }
	
	self.clearLookupselection = function (attrData, parent, event) {
        var l_modalToOpen = attrData.attr().AttributeClearMethod();
        window[l_modalToOpen](attrData,event);
    }

    // Flags to handle
    self.isDirty = false;
    self.allowDecimal = false;
    self.hasErrors = ko.observable(false);

    //behavior.
    self.changeRadioButton = function (value, iItem, event) {
        iItem.attr().AttributeValue(value.toString());
        if (iItem.attr().AttributeName() == "Perpetual") {
            var contractTerm = l_contractSummary_model.layoutModal().mappedAllRelAttList().filter(ele => ele.AttributeName() == "InitialContractTenure");
            var AutoRenew = l_contractSummary_model.layoutModal().mappedAllRelAttList().filter(ele => ele.AttributeName() == "AutoRenew");
            var AutoRenewDuration = l_contractSummary_model.layoutModal().mappedAllRelAttList().filter(ele => ele.AttributeName() == "AutoRenewDuration");
            if (iItem.attr().AttributeValue() == "true") {
                contractTerm.forEach(function (element, index) {
                    element.IsReadOnly('TRUE');
                    element.IsMandatory("FALSE");
                    element.AttributeValue(undefined);
                });
                AutoRenew.forEach(function (element, index) {
                    element.IsReadOnly('TRUE');
                    element.IsMandatory("FALSE");
                    element.AttributeValue('false');
                });
                AutoRenewDuration.forEach(function (element, index) {
                    element.IsReadOnly('TRUE');
                    element.IsMandatory("FALSE");
                    element.AttributeValue(undefined);
                });
                $(".ContractTermPreviewModel").css("display", "none");
                $(".AutoRenewDurPreviewModel").css("display", "none");
            }
            else {
                contractTerm.forEach(function (element, index) {
                    element.IsReadOnly('FALSE');
                    element.IsMandatory("TRUE");
                });
                AutoRenew.forEach(function (element, index) {
                    element.IsReadOnly('FALSE');
                    element.IsMandatory("TRUE");
                });
                AutoRenewDuration.forEach(function (element, index) {
                    if (element.AttributeValue() == "true") {
                        element.IsReadOnly('FALSE');
                        element.IsMandatory("TRUE");
                    }
                    else {
                        element.IsReadOnly('TRUE');
                        element.IsMandatory("FALSE");
                    }
                });

            }

        }

        if (iItem.attr().AttributeName() == "AutoRenew") {
            var AutoRenewDuration = l_contractSummary_model.layoutModal().mappedAllRelAttList().filter(ele => ele.AttributeName() == "AutoRenewDuration");
            if (iItem.attr().AttributeValue() == "true") {
                AutoRenewDuration.forEach(function (element, index) {
                    element.IsReadOnly('FALSE');
                    element.IsMandatory("TRUE");
                });

            }
            else {
                AutoRenewDuration.forEach(function (element, index) {
                    element.IsReadOnly('TRUE');
                    element.IsMandatory("FALSE");
                    element.AttributeValue(undefined);
                });
                $(".AutoRenewDurPreviewModel").css("display", "none");
            }

        }
        event.stopPropagation();
    }

    self.changeEnumText = function (value, iItem, event) {
        if (iItem.attr().AttributeName() == "PriceProtection") {
            var contractTerm = l_contractSummary_model.layoutModal().mappedAllRelAttList().filter(ele => ele.AttributeName() == "PriceProtectionDate");
            if (iItem.attr().AttributeValue() == "1") {
                contractTerm.forEach(function (element, index) {
                    element.IsReadOnly('FALE');
                    element.IsMandatory("TRUE");
                })

            }
            else {
                contractTerm.forEach(function (element, index) {
                    element.IsReadOnly('TRUE');
                    element.AttrDefType("TEXT");
                    element.AttributeValue("");
                    element.AttrDefType("DATE");
                    element.IsMandatory("FALSE");
                })

            }

        }
        event.stopPropagation();
    }


    self.removeErrorClass = function (data, iEvent) {
        if (data.hasErrors()) {
            $(iEvent.target).removeClass("cc-error");
            data.hasErrors(false);
        }
    }

    self.updateDropdownValues = function (attributeId) {
        $.cordys.ajax({
            namespace: "http://schemas/OpenTextCustomAttributes/AttributeDefinition/operations",
            method: "GetRelatedEnums",
            async: false,
            parameters: {
                "AttributeDefinition-id": {
                    "Id": attributeId
                }
            },
            success: function (data) {
                addOptionsToDropdown(data.RelatedEnums);

            },
            error: function (responseFailure) {
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("Unable to retrieve the Dropdown list. Contact your administrator."), 10000);
                return false;
            }
        });


    }

    function addOptionsToDropdown(iElementList) {
        if (self.AttrSelectOptions().length > 0) {
            self.AttrSelectOptions.removeAll();
        }
        if (iElementList) {
			var choice = {};
            choice.choice = false;
            choice.name = "Select an option";
            choice.id = "select an option";
            choice.order = 0;
            self.AttrSelectOptions.push(choice);
            if (iElementList.length) {
                iElementList.forEach(function (option) {
					choice = {};
                    choice.choice = false;
                    choice.name = option.enumvalue;
                    choice.id = option.enumvalue;
                    choice.order = option.order;
                    self.AttrSelectOptions.push(choice);
                });
            }
            else {
                var choice = {};
                choice.choice = false;
                choice.name = iElementList.enumvalue;
                choice.id = iElementList.enumvalue;
                self.AttrSelectOptions.push(choice);
            }
			 self.AttrSelectOptions.sort(compareOptionsOrder);
        }
    }
    function compareOptionsOrder(a, b) {
        if (parseInt(a.order) < parseInt(b.order)) {
            return -1;
        }
        if (parseInt(a.order) > parseInt(b.order)) {
            return 1;
        }
        return 0;
    }

    if (data) {
        self.Id(data["RelatedAttrDefinition-id"].Id);
        self.Id1(data["RelatedAttrDefinition-id"].Id1);
        self.ItemId(data["RelatedAttrDefinition-id"].ItemId);
        self.ItemId1(data["RelatedAttrDefinition-id"].ItemId1);
        self.Order(data.Order);
        self.Type(data.Type);
        //self.isEditable(data.IsEditable === "FALSE" ? false : true);
        //self.IsReadOnly(data.IsEditable === "FALSE" ? "TRUE" : "FALSE")
        if (data.Type === "GROUP" || data.Type === "TAB") {
            self.Name(data.RelatedLabel.Label ? getTextValue(data.RelatedLabel.Label) : "");
        } else if (data.Type === "CUSTATTRIBUTE") {
            self.dateFormat(DEFAULT_DATE_FORMAT.value);
            self.IsReadOnly("FALSE");
            self.AttrDefId(data.RelatedDef ? data.RelatedDef["AttributeDefinition-id"].Id : "");
            self.AttrDefItemId(data.RelatedDef ? data.RelatedDef["AttributeDefinition-id"].ItemId : "");
            self.AttrDefType(data.RelatedDef ? getTextValue(data.RelatedDef.DataType) : "");
            self.AttributeName(data.RelatedDef ? getTextValue(data.RelatedDef.Name) : "");
            if (data.RelatedDef && data.RelatedDef.Name) {
                self.CustAttributeId(l_contractSummary_model.customProperties[data.RelatedDef.Name.text].id);
                self.CustAttributeId1(l_contractSummary_model.customProperties[data.RelatedDef.Name.text].id1);
                self.AttributeValue(l_contractSummary_model.customProperties[data.RelatedDef.Name.text].value());
                self.Name(l_contractSummary_model.customProperties[data.RelatedDef.Name.text].label);
                if (data.RelatedDef.DataType.text == "ENUM") {
                    //self.AttrSelectOptions(l_contractSummary_model.customProperties[data.RelatedDef.Name.text].selectOptions());
                    self.updateDropdownValues(self.AttrDefId());
                }
                if (data.RelatedDef.DataType.text == "DATE") {
                    self.dateFormat(l_contractSummary_model.customProperties[data.RelatedDef.Name.text].dateFormat);
					self.AttributeClearMethod(l_contractSummary_model.clearProperties["CustomAttrDate"]);
                }
            }
        } else {
            self.dateFormat(DATE_FORMAT_DD_MM_YYYY_1.value);
            self.IsReadOnly(getTextValue(data['RelatedAttrGCProp'].IsReadOnly) === "TRUE" ? "TRUE" : "FALSE");
            self.IsMandatory(getTextValue(data['RelatedAttrGCProp'].IsMandatory) === "TRUE" ? "TRUE" : "FALSE");
            self.AttrDefId(data.RelatedAttrGCProp ? data.RelatedAttrGCProp["RelatedGCProps-id"].Id1 : "");
            self.AttrDefItemId(data.RelatedAttrGCProp ? data.RelatedAttrGCProp["RelatedGCProps-id"].ItemId1 : "");
            self.AttrDefType(data.RelatedAttrGCProp ? getTextValue(data.RelatedAttrGCProp.DataType) : "");
            self.Name(getTextValue(data.RelatedLabel.Name));
            if (data.RelatedAttrGCProp && data.RelatedAttrGCProp.Name) {
                var l_attrName = getTextValue(data.RelatedAttrGCProp.Name);
                self.AttributeName(l_attrName);
                self.GroupName("ALL");
                if (GROUP_ENABLE_FIELDS["TERMINATE_INREVIEW_ENABLE"].indexOf(self.AttributeName()) != -1) {
                    self.GroupName("TERMINATE_INREVIEW_ENABLE");
                }
                self.AttributeValue(l_contractSummary_model.contractProperties[l_attrName]);
                if (l_attrName == "Priority") {
                    self.AttrSelectOptions = [
						{ name: 'Select an option', id: "select an option" },
                        { name: 'High', id: "High" },
                        { name: 'Medium', id: "Medium" },
                        { name: 'Low', id: "Low" }
                    ];
                }
                if (l_attrName == "TemplateType") {
                    self.AttrSelectOptions = [
                        { name: 'None', id: "None" },
                        { name: 'Internal template', id: "Internal template" },
                        { name: 'Internal party document', id: "Internal party document" },
                        { name: 'External party document', id: "External party document" }
                    ];
                }
                if (l_attrName == "PriceProtection") {
                    self.AttrSelectOptions = [
						{ name: 'Select an option', id: "select an option" },
                        { name: 'Yes', id: "1" },
                        { name: 'No', id: "0" }
                    ];
                }
                if (l_attrName == "PriceProtectionDate") {
                    if (l_contractSummary_model.contractProperties["PriceProtection"] == false) {
                        self.IsReadOnly("TRUE");
                    }
                    else {
                        self.IsMandatory("TRUE");
                    }
                }
                if (l_attrName == "Country" || l_attrName == "Currency" || l_attrName == "AmendType" || l_attrName == "RenewalFlagStatus" || l_attrName == "TerminationReason" || l_attrName == "DocumentOrigination") {
                    self.AttributeLookupMethod(l_contractSummary_model.lookUpProperties[l_attrName]);
                }
				if (l_attrName == "Country" || l_attrName == "Currency" || l_attrName == "AmendType" || l_attrName == "RenewalFlagStatus" || l_attrName == "TerminationReason" || l_attrName == "DocumentOrigination" || l_attrName == "MinStartdate" || l_attrName == "StartDate" || l_attrName == "PriceProtectionDate" || l_attrName == "SignatureDate") {
                    self.AttributeClearMethod(l_contractSummary_model.clearProperties[l_attrName]);
                }
                if (l_attrName == "InitialContractTenure") {
                    if (l_contractSummary_model.contractProperties["Perpetual"] == "true") {
                        self.IsReadOnly("TRUE");
                        self.IsMandatory("FALSE");
                    }
                }
                if (l_attrName == "AutoRenew") {
                    if (l_contractSummary_model.contractProperties["Perpetual"] == "true") {
                        self.IsReadOnly("TRUE");
                        self.IsMandatory("FALSE");
                    }
                }
                if (l_attrName == "AutoRenewDuration") {
                    if (l_contractSummary_model.contractProperties["Perpetual"] == "true") {
                        self.IsReadOnly("TRUE");
                        self.IsMandatory("FALSE");
                    }
                    else {
                        if (l_contractSummary_model.contractProperties["AutoRenew"] == "true") {
                            self.IsReadOnly("FALSE");
                            self.IsMandatory("TRUE");
                        }
                        else {
                            self.IsReadOnly("TRUE");
                            self.IsMandatory("FLASE");
                        }
                    }
                }
            }
        }
        self.ParentRelAttrDefId(data.SourceAttrDef ? data.SourceAttrDef["RelatedAttrDefinition-id"].Id : "");
        self.ParentRelAttrDefId1(data.SourceAttrDef ? data.SourceAttrDef["RelatedAttrDefinition-id"].Id1 : "");
        self.ParentRelAttrDefItemId(data.SourceAttrDef ? data.SourceAttrDef["RelatedAttrDefinition-id"].ItemId : '');
        self.ParentRelAttrDefItemId1(data.SourceAttrDef ? data.SourceAttrDef["RelatedAttrDefinition-id"].ItemId1 : '');
    }
    return self;
}

function ContainerNode(data) {
    var self = {};
    self.attr = ko.observable(data);
    self.container = ko.observableArray();
    self.action = ko.observable("NONE");
    self.display = ko.observable(false);
    self.isEditable = ko.observable(data && !data.isEditable() ? false : true);

    self.accounts = ko.observable();
    self.contractLines = ko.observable();
    self.tags = ko.observable();
    self.poNumbers = ko.observable();

    self.showOptions = ko.observable(false);

    return self;
}

function LayoutConfig(data) {
    var self = {};
    self.Id = ko.observable(data ? getTextValue(data["LayoutConfig-id"].Id) : "");
    self.ItemId = ko.observable(data ? getTextValue(data["LayoutConfig-id"].ItemId) : "");
    self.Status = ko.observable(data ? getTextValue(data.Status) : "INACTIVE");
    self.Version = ko.observable(data ? getTextValue(data.Version) : "");
    self.Colsize = ko.observable(data ? +getTextValue(data.Colsize) : 2);
    self.Name = ko.observable(data ? getTextValue(data.Name) : "Untitled layout");
    self.attributeList = ko.observableArray();

    self.showCustAttrList = ko.observable(true);
    self.showContractAttrList = ko.observable(true);

    self.custAttributesList = ko.observableArray();
    self.contractAttributesList = ko.observableArray();

    self.mappedAllRelAttList = ko.observableArray();
    self.fieldGroupsOriginal = ko.observableArray();
    self.fieldGroups = ko.observableArray();
    self.fieldGroupsGp = ko.observableArray();
    self.columnSizeOptions = ko.observableArray([2, 3, 4]);
    self.showRightPanelFields = ko.observable(false);
    self.showRightPanelDetails = ko.observable(false);
    self.isRightPanelEdit = ko.observable(false);
    self.displayAttrList = ko.observableArray();

    self.selectedGroup = ko.observable();
    self.selectedAttribute = ko.observable();
    self.showOptions = ko.observable(false);



    self.adjustColLayout = function () {
        var colSize = self.Colsize();
        self.displayAttrList().forEach(dispTab => {
            dispTab.container().forEach(ele => {
                var count = 0;
                ele.container().forEach(fld => {
                    fld.attr().Style().Class('flex-cn-cl-25');
                    if ((count + 1) % (colSize) === 0) {
                        if (colSize === 3) {
                            fld.attr().Style().Class('flex-cn-cl-50');
                        } else if (colSize === 2) {
                            fld.attr().Style().Class('flex-cn-cl-75');
                        }
                    }
                    count++;
                });
            });
        });
    }

    self.getAllAttributesMappedtoLayout = function (contractSummary_model) {
        if (self.Id()) {
            contractSummary_Services.getAllAttributesMappedtoLayoutService({ layoutId: self.Id() }, function (response, status) {
                if (status === "SUCCESS") {
                    _populateMappedAttrList(response, contractSummary_model);
                }
            });
        }
    }

    function _populateMappedAttrList(response, contractSummary_model) {
        var relAttDefList = response.Response.FindZ_INT_AllRelatedAttrDefinitionResponse.RelatedAttrDefinition;
        self.mappedAllRelAttList.removeAll();
        if (relAttDefList && Array.isArray(relAttDefList)) {
            for (let index = 0; index < relAttDefList.length; index++) {
                const element = relAttDefList[index];
                self.mappedAllRelAttList.push(RelatedAttrDef(element, self));
            }
        } else if (relAttDefList) {
            self.mappedAllRelAttList.push(RelatedAttrDef(relAttDefList, self));
        }
        self.displayAttrList.removeAll();
        var tabList = self.mappedAllRelAttList().filter(ele => ele.Type() === "TAB");
        for (let index = 0; index < tabList.length; index++) {
            const element = tabList[index];
            addDisplayContainer(self.displayAttrList, element);
        }
        self.adjustColLayout();
        self.displayAttrList.sort(sortOnOrder);

        //self.displayAttrList()[currentTabIndex].display(true);
        self.displayTab(self.displayAttrList()[currentTabIndex]);
        l_contractSummary_model.startTrackValueChanges(true);
        if (isExecutedContract && isExecutedContract == "true" && (lifecycle_CurrentState == "Pending Activation" || lifecycle_CurrentState == "Active")) {
            var contractTerm = l_contractSummary_model.layoutModal().mappedAllRelAttList().filter(ele => ele.AttributeName() == "InitialContractTenure");
            contractTerm.forEach(function (element, index) {
                element.IsReadOnly('TRUE');
            });
            var perpetual = l_contractSummary_model.layoutModal().mappedAllRelAttList().filter(ele => ele.AttributeName() == "Perpetual");
            perpetual.forEach(function (element, index) {
                element.IsReadOnly('TRUE');
            });
            var plannedStartDate = l_contractSummary_model.layoutModal().mappedAllRelAttList().filter(ele => ele.AttributeName() == "MinStartdate");
            plannedStartDate.forEach(function (element, index) {
                element.IsReadOnly('TRUE');
            });
			var autoRenew = l_contractSummary_model.layoutModal().mappedAllRelAttList().filter(ele => ele.AttributeName() == "AutoRenew");
            autoRenew.forEach(function (element, index) {
                element.IsReadOnly('TRUE');
            });
			var autoRenewDuration = l_contractSummary_model.layoutModal().mappedAllRelAttList().filter(ele => ele.AttributeName() == "AutoRenewDuration");
            autoRenewDuration.forEach(function (element, index) {
                element.IsReadOnly('TRUE');
            });
			
        }
        if (!_userStateEditabilityFlag) {
            self.mappedAllRelAttList().forEach(ele => {
                ele.IsReadOnly("TRUE")
                if ((lifecycle_CurrentState !== "Terminated" && lifecycle_CurrentState !== "Expired") && ele.GroupName() === "TERMINATE_INREVIEW_ENABLE" && _userEditabilityFlag) {
                    ele.IsReadOnly("FALSE")
                }
            }
            );
        }

        document.getElementsByClassName('cc-loading-overlay')[0].classList.toggle('is-active');
    }

    function addDisplayContainer(obsList, attrDefObs) {
        var attrDef = ContainerNode(attrDefObs);
        obsList.push(attrDef);
        var childContainers = self.mappedAllRelAttList()
            .filter(ele => ele.ParentRelAttrDefItemId &&
                ele.ParentRelAttrDefItemId1() === attrDefObs.ItemId1());
        for (let index = 0; index < childContainers.length; index++) {
            const element = childContainers[index];
            addDisplayContainer(attrDef.container, element);
        }
        attrDef.container.sort(sortOnOrder);
    }

    function sortOnOrder(l_ele1, l_ele2) {
        return l_ele1.attr().Order() - l_ele2.attr().Order();
    }

    self.displayTab = function (data, event) {
        if (l_contractSummary_model.valueChangesExists() || l_contract_accounts_model.isINTDirty() || l_contract_accounts_model.isEXTDirty()) {
            $("#unsavedWarningModal").modal();
            event.stopPropagation();
            event.preventDefault();
        }
        else if (data.isEditable()) {
            currentTabIndex = self.displayAttrList().indexOf(data);
            _hideAllTabs();
            data.display(true);
            l_contractSummary_model.valueChangesExists(false)
            if (data.attr().Name() === "Accounts") {
                l_contract_accounts_model.loadPageContent();
                data.accounts(l_contract_accounts_model);
                $("#div_saveCancelFooter").css("display", "none");
            }
            else if (data.attr().Name() === "Contract lines") {
                l_contractLines_model.loadPageContent();
                data.contractLines(l_contractLines_model);
                $("#div_saveCancelFooter").css("display", "none");
            }
            else if (data.attr().Name() === "Tags") {
                l_contract_tags_info_model.loadPageContent();
                data.tags(l_contract_tags_info_model);
                $("#div_saveCancelFooter").css("display", "none");
            }
            else if (data.attr().Name() === "PO numbers") {
                l_contract_PONumbers_model.loadPageContent();
                data.poNumbers(l_contract_PONumbers_model);
                $("#div_saveCancelFooter").css("display", "none");
            }
            else {
                $("#div_saveCancelFooter").css("display", "block");
            }
        }

    }

    function _hideAllTabs() {
        self.displayAttrList().forEach(ele => ele.display(false));
    }



    return self;
}

var ContractSummaryModel = function () {
    var self = this;
    self.contractID = ko.observable();
    self.layoutConfigID = ko.observable();
    self.layoutModal = ko.observable();
    self.watchersList = ko.observableArray();
    self.watcherAdded = ko.observable();
    self.contractAttributeList = ko.observableArray();

    self.contractProperties = {};
    self.lookUpProperties = {
        'Country': "openCountrySelModal",
        'Currency': "openCurrencySelModal",
        'AmendType': "openAmendTypeSelModal",
        'RenewalFlagStatus': "openRenewalFlagSelModal",
        'TerminationReason': "openTermiReasonSelModal",
        'DocumentOrigination': "openDocOrigSelModal"
    };
	self.clearProperties = {
        'Country': "clearLookupselData",
        'Currency': "clearLookupselData",
        'AmendType': "clearLookupselData",
        'RenewalFlagStatus': "clearLookupselData",
        'TerminationReason': "clearLookupselData",
        'DocumentOrigination': "clearLookupselData",
		'SignatureDate': "clearDateData",
		'ProductGoLiveDate': "clearDateData",
		'StartDate': "clearDateData",
		'MinStartdate': "clearDateData",
		'CustomAttrDate' : "clearDateData"
    };
    self.PONumbers = ko.observableArray();
    self.customProperties = {};

    self.startTrackValueChanges = ko.observable(false);
    self.valueChangesExists = ko.observable(false);

    window.parent.document.addEventListener("click", e => {
        if (self.valueChangesExists()) {
            $("#unsavedWarningModal").modal();
            e.stopPropagation();
            e.preventDefault();
        }
    }, true);

    self.fetchContractAttrDetails = function (callBackFun) {
        contractSummary_Services.GetContractDetailsById(self.contractID, function (data, status) {
            if (status === "SUCCESS") {
                _populateContractAttrData(data.Contract);
                if (callBackFun) {
                    callBackFun();
                }
            }
        });
    }

    function _populateContractAttrData(iContract) {

        self.contractProperties["ActionDuration"] = getTextValue(iContract.ActionDuration) ? formateNumbertoLocale(iContract.ActionDuration) : '';
        if (iContract.AmendType) {
            self.contractProperties["AmendType"] = getTextValue(iContract.AmendType.Name);
        }
        self.contractProperties["AutoRenew"] = getTextValue(iContract.AutoRenew);
        var l_autoRenewDuration = getTextValue(iContract.AutoRenewDuration);
        if (l_autoRenewDuration.lastIndexOf("M") > 0 && l_autoRenewDuration.lastIndexOf("D") > 0 && getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("M"))) > 0 && getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("M") + 1, l_autoRenewDuration.lastIndexOf("D"))) > 0) {
            self.contractProperties["AutoRenewDuration"] = (getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("M"))) + " month(s), " + getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("M") + 1, l_autoRenewDuration.lastIndexOf("D"))) + " day(s)");
        }
        else if (l_autoRenewDuration.lastIndexOf("M") > 0 && getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("M"))) > 0) {
            self.contractProperties["AutoRenewDuration"] = (getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("M"))) + " month(s)");
        }
        else if (l_autoRenewDuration.lastIndexOf("D") > 0 && getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("D"))) > 0) {
            self.contractProperties["AutoRenewDuration"] = (getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("D"))) + " day(s)");
        } else {
            self.contractProperties["AutoRenewDuration"] = ("");
        }
        self.contractProperties["CancellationDate"] = getTextValue(iContract.CancellationDate).split("Z")[0];
        //self.contractProperties["CancellationDate"] = getTextValue(iContract.CancellationDate) ? formateDatetoLocale(getTextValue(iContract.CancellationDate)) : '';
        self.contractProperties["CancellationComments"] = getTextValue(iContract.CancellationComments);
        self.contractProperties["ClientEarlyTermRight"] = getTextValue(iContract.ClientEarlyTermRight);
        self.contractProperties["Comments"] = getTextValue(iContract.Comments);
        self.contractProperties["ContractName"] = getTextValue(iContract.ContractName);
        self.contractProperties["ContractNumber"] = getTextValue(iContract.ContractNumber);
        if (iContract.ContractType) {
            self.contractProperties["ContractType"] = getTextValue(iContract.ContractType.Name);
        }
        self.contractProperties["ContractValue"] = getTextValue(iContract.ContractValue);
        //self.contractProperties["ContractValue"] = getTextValue(iContract.ContractValue) ? formateNumbertoLocale(iContract.ContractValue) : '';
        self.contractProperties["ContractValueUSD"] = getTextValue(iContract.ContractValueUSD);
        //self.contractProperties["ContractValueUSD"] = getTextValue(iContract.ContractValueUSD) ? formateCurrencyInUSD(iContract.ContractValueUSD) : '';
        if (iContract.Country) {
            self.contractProperties["Country"] = getTextValue(iContract.Country.LinkedCountry.Country_Name);
        }
        self.contractProperties["CRMOpportunityID"] = getTextValue(iContract.CRMOpportunityID);
        self.contractProperties["CRMQuoteID"] = getTextValue(iContract.CRMQuoteID);
        if (iContract.Currency) {
            self.contractProperties["Currency"] = getTextValue(iContract.Currency.Name);
        }
        self.contractProperties["CurrentEndDate"] = getTextValue(iContract.CurrentEndDate).split("Z")[0];
        //self.contractProperties["CurrentEndDate"] = getTextValue(iContract.CurrentEndDate) ? formateDatetoLocale(getTextValue(iContract.CurrentEndDate)) : '';
        self.contractProperties["CurrentStartDate"] = getTextValue(iContract.CurrentStartDate).split("Z")[0];
        //self.contractProperties["CurrentStartDate"] = getTextValue(iContract.CurrentStartDate) ? formateDatetoLocale(getTextValue(iContract.CurrentStartDate)) : '';
        self.contractProperties["CustomerManagerComments"] = getTextValue(iContract.CustomerManagerComments);
        self.contractProperties["DealManager"] = getTextValue(iContract.DealManager);
        self.contractProperties["Description"] = getTextValue(iContract.Description);
        if (iContract.DocumentOrigination) {
            self.contractProperties["DocumentOrigination"] = getTextValue(iContract.DocumentOrigination.Name);
        }
        self.contractProperties["EarlyTerminationConditions"] = getTextValue(iContract.EarlyTerminationConditions);
        self.contractProperties["EndUser"] = getTextValue(iContract.EndUser);
        self.contractProperties["GeneratedContractId"] = getTextValue(iContract.GeneratedContractId);
        var l_contractTermDuration = getTextValue(iContract.InitialContractTenure);
        if (l_contractTermDuration.lastIndexOf("M") > 0 && l_contractTermDuration.lastIndexOf("D") > 0 && getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("M"))) > 0 && getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("M") + 1, l_contractTermDuration.lastIndexOf("D"))) > 0) {
            self.contractProperties["InitialContractTenure"] = (getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("M"))) + " month(s), " + getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("M") + 1, l_contractTermDuration.lastIndexOf("D"))) + " day(s)");
        }
        else if (l_contractTermDuration.lastIndexOf("M") > 0 && getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("M"))) > 0) {
            self.contractProperties["InitialContractTenure"] = (getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("M"))) + " month(s)");
        }
        else if (l_contractTermDuration.lastIndexOf("D") > 0 && getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("D"))) > 0) {
            self.contractProperties["InitialContractTenure"] = (getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("D"))) + " day(s)");
        } else {
            self.contractProperties["InitialContractTenure"] = ("");
        }
        self.contractProperties["InitialExpiryDate"] = getTextValue(iContract.InitialExpiryDate).split("Z")[0];
        //self.contractProperties["InitialExpiryDate"] = getTextValue(iContract.InitialExpiryDate) ? formateDatetoLocale(getTextValue(iContract.InitialExpiryDate)) : '';
        self.contractProperties["IsExecuted"] = getTextValue(iContract.IsExecuted);
        self.contractProperties["ContractDocumentType"] = iContract.ContractDocumentType == 'EXTERNALDOCUMENT' ? 'true' : 'false';
        self.contractProperties["MinStartdate"] = getTextValue(iContract.MinStartdate).split("Z")[0];
        //self.contractProperties["MinStartdate"] = getTextValue(iContract.MinStartdate) ? formateDatetoLocale(getTextValue(iContract.MinStartdate)) : '';
        self.contractProperties["NextExpirationDate"] = getTextValue(iContract.NextExpirationDate).split("Z")[0];
        //self.contractProperties["NextExpirationDate"] = getTextValue(iContract.NextExpirationDate) ? formateDatetoLocale(getTextValue(iContract.NextExpirationDate)) : '';
        self.contractProperties["NotificationDuration"] = getTextValue(iContract.NotificationDuration) ? formateNumbertoLocale(iContract.NotificationDuration) : ''
        self.contractProperties["OriginalSalesAccountExecutive"] = getTextValue(iContract.OriginalSalesAccountExecutive);
        self.contractProperties["Perpetual"] = getTextValue(iContract.Perpetual);
        self.contractProperties["PriceProtection"] = getTextValue(iContract.PriceProtection == '1') ? true : getTextValue(iContract.PriceProtection == '0') ? false : '';
        self.contractProperties["PriceProtectionDate"] = getTextValue(iContract.PriceProtectionDate).split("Z")[0];
        //self.contractProperties["PriceProtectionDate"] = getTextValue(iContract.PriceProtectionDate) ? formateDatetoLocale(getTextValue(iContract.PriceProtectionDate)) : '';
        self.contractProperties["Priority"] = getTextValue(iContract.Priority);
        self.contractProperties["ProductGoLiveDate"] = getTextValue(iContract.ProductGoLiveDate).split("Z")[0];
        //self.contractProperties["ProductGoLiveDate"] = getTextValue(iContract.ProductGoLiveDate) ? formateDatetoLocale(getTextValue(iContract.ProductGoLiveDate)) : '';
        if (iContract.Country) {
            self.contractProperties["Region"] = getTextValue(iContract.Country.Owner.Name);
        }
        if (iContract.RelatedOrganization) {
            self.contractProperties["RelatedOrganization"] = getTextValue(iContract.RelatedOrganization.Name);
        }
        self.contractProperties["RenewalComments"] = getTextValue(iContract.RenewalComments);
        if (iContract.RenewalFlagStatus) {
            self.contractProperties["RenewalFlagStatus"] = getTextValue(iContract.RenewalFlagStatus.Name);
        }
        self.contractProperties["RenewalDiscount"] = getTextValue(iContract.RenewalDiscount);
        //self.contractProperties["RenewalDiscount"] = getTextValue(iContract.RenewalDiscount) ? formateNumbertoLocale(iContract.RenewalDiscount) : '';
        self.contractProperties["SAPOrderID"] = getTextValue(iContract.SAPOrderID);
        self.contractProperties["SignatureDate"] = getTextValue(iContract.SignatureDate).split("Z")[0];
        //self.contractProperties["SignatureDate"] = getTextValue(iContract.SignatureDate) ? formateDatetoLocale(getTextValue(iContract.SignatureDate)) : '';
        self.contractProperties["StartDate"] = getTextValue(iContract.StartDate).split("Z")[0];
        //self.contractProperties["StartDate"] = getTextValue(iContract.StartDate) ? formateDatetoLocale(getTextValue(iContract.StartDate)) : '';
        if (iContract.RelatedTemplate) {
            self.contractProperties["Template"] = getTextValue(iContract.RelatedTemplate.Name);
            self.contractProperties["TemplateType"] = getTextValue(iContract.RelatedTemplate.TemplateType);
        }
        self.contractProperties["TerminationFees"] = getTextValue(iContract.TerminationFees);
        //self.contractProperties["TerminationFees"] = getTextValue(iContract.TerminationFees) ? formateNumbertoLocale(iContract.TerminationFees) : '';
        self.contractProperties["TerminationNoticePeriod"] = getTextValue(iContract.TerminationNoticePeriod);
        //self.contractProperties["TerminationNoticePeriod"] = getTextValue(iContract.TerminationNoticePeriod) ? formateNumbertoLocale(iContract.TerminationNoticePeriod) : '';
        if (iContract.TerminationReason) {
            self.contractProperties["TerminationReason"] = getTextValue(iContract.TerminationReason.Reason);
        }
        self.contractProperties["Validated"] = getTextValue(iContract.Validated);
        self.contractProperties["ValidatedOn"] = getTextValue(iContract.ValidatedOn).split("Z")[0];
        //self.contractProperties["ValidatedOn"] = getTextValue(iContract.ValidatedOn) ? formateDatetoLocale(getTextValue(iContract.ValidatedOn)) : '';
        self.contractProperties["ValidatedBy"] = getTextValue(iContract.ValidatedBy);
        self.watcherAdded(getTextValue(iContract.watcheradded));
        if (iContract.relatedPOs && iContract.relatedPOs.PONumber) {
            if (iContract.relatedPOs.PONumber.length) {
                iContract.relatedPOs.PONumber.forEach(function (iElement) {
                    self.PONumbers.push(getTextValue(iElement));

                });
            } else {
                self.PONumbers.push(getTextValue(iContract.relatedPOs.PONumber));
            }
        }

        self.fetchCustomAttrDetails();
        self.getContractWatchers();
    }

    self.fetchCustomAttrDetails = function (callBackFun) {
        contractSummary_Services.GetCustomAttrDetailsById(self.contractID, function (data, status) {
            if (status === "SUCCESS") {
                _populateCustomAttrData(data);
                if (callBackFun) {
                    callBackFun();
                }
            }
        });

    }

    function _populateCustomAttrData(iCustAttrData) {
        if (iCustAttrData.FindZ_INT_RelatedAttributesListResponse) {
            l_custAttrbutes = iCustAttrData.FindZ_INT_RelatedAttributesListResponse.RelatedAttributes
            if (l_custAttrbutes) {
                if (l_custAttrbutes.length) {
                    l_custAttrbutes.forEach(function (iElement) {
                        l_custAttrModelObj = formCustAttrModelObj(iElement);
                        self.customProperties[l_custAttrModelObj.name] = l_custAttrModelObj;
                    });
                }
                else {
                    l_custAttrModelObj = formCustAttrModelObj(l_custAttrbutes);
                    self.customProperties[l_custAttrModelObj.name] = l_custAttrModelObj;
                }
            }
        }
        self.populateRelatedLayoutData();
    }

    function formCustAttrModelObj(iElement) {
        l_custAttrModelObj = new CustomAttributeModel();
        if (iElement) {
            if (iElement['RelatedAttributes-id']) {
                l_custAttrModelObj.id = iElement['RelatedAttributes-id'].Id;
                l_custAttrModelObj.id1 = iElement['RelatedAttributes-id'].Id1;
            }
            if (iElement['RelatedDefinition']) {
                l_custAttrModelObj.attributeId = iElement.RelatedDefinition["AttributeDefinition-id"].Id;
            }
            l_custAttrModelObj.name = getTextValue(iElement.Name);
            if (iElement.RelatedLabel) {
                l_custAttrModelObj.label = getTextValue(iElement.RelatedLabel.Label);
            }
            l_custAttrModelObj.dataType = getTextValue(iElement.DataType);
            l_custAttrModelObj.selectLoaded = false;
            l_custAttrModelObj.attributeMedadata = iElement.AttributeMetaData;
            l_custAttrModelObj.value = ko.observable(getTextValue(iElement.Value)).extend({ trackValueChange: true });
            readMetaDataJSONAndFillModel(l_custAttrModelObj);
        }
        return l_custAttrModelObj;
    }

    function readMetaDataJSONAndFillModel(model) {
        if (model && model.attributeMedadata) {
            var metaDataObj = JSON.parse(model.attributeMedadata);
            switch (model.dataType) {
                case DATA_TYPE_BOOLEAN:
                    if (!model.value()) {
                        model.value('false');
                    } else {
                        model.value(model.value().toLowerCase() == 'true' ? 'true' : 'false');
                    }
                    break;
                case DATA_TYPE_NUMBER:
                    model.allowDecimal = metaDataObj.decimal;
                    if (model.allowDecimal) {
                        model.placeHolder = "Enter a numeric value";
                    } else {
                        model.placeHolder = "Enter a numeric value";
                    }
                    break;
                case DATA_TYPE_TEXT:
                    model.placeHolder = "Enter a text value";
                    break;
                case DATA_TYPE_DATE:
                    model.dateFormat = (metaDataObj.dateformat) ? metaDataObj.dateformat : DEFAULT_DATE_FORMAT.value;
                    model.placeHolder = getDateFormatLabel(model.dateFormat);
                    break;
                case DATA_TYPE_ENUM:
                    model.placeHolder = "Select an option";
                    if (model.value()) {
                        var choice = {};
                        choice.choice = false;

                        choice.name = model.value();
                        choice.id = model.value();
                        model.selectOptions.push(choice);
                    }
                    break;
            }
        }
    }

    function getDateFormatLabel(dateFormat) {
        var dateFormatLabel = DEFAULT_DATE_FORMAT.label;
        if (dateFormat) {
            switch (dateFormat) {
                case DATE_FORMAT_DD_MMM_YYYY.value:
                    dateFormatLabel = DATE_FORMAT_DD_MMM_YYYY.label;
                    break;
                case DATE_FORMAT_DD_MM_YYYY.value:
                    dateFormatLabel = DATE_FORMAT_DD_MM_YYYY.label;
                    break;
                case DATE_FORMAT_MMM_DD_YYYY.value:
                    dateFormatLabel = DATE_FORMAT_MMM_DD_YYYY.label;
                    break;
                case DATE_FORMAT_MM_DD_YYYY.value:
                    dateFormatLabel = DATE_FORMAT_MM_DD_YYYY.label;
                    break;
                case DATE_FORMAT_YYYY_MM_DD.value:
                    dateFormatLabel = DATE_FORMAT_YYYY_MM_DD.label;
                    break;
            }
        }
        return dateFormatLabel;
    }

    self.populateRelatedLayoutData = function (callBackFun) {
        contractSummary_Services.getRelatedLayoutDataService({ "LayoutConfig-id": { "Id": self.layoutConfigID() } }, function (data, status) {
            if (status === "SUCCESS") {
                var layoutConfigData = data.LayoutConfig;
                var layoutConfigObj = LayoutConfig(layoutConfigData);
                layoutConfigObj.adjustColLayout();
                self.layoutModal(layoutConfigObj);
                layoutConfigObj.getAllAttributesMappedtoLayout(self);
                if (callBackFun) {
                    callBackFun();
                }
            }
        });
    }

    self.addWatcher = function () {
        contractSummary_Services.AddWatcherToContractById(contractID, function (response_data, status) {
            if (status !== "ERROR") {
                successToast(2000, "You have successfully added yourself as watcher");
                self.watcherAdded("true");
                self.getContractWatchers();
            }
        });
    }
    self.removeWatcher = function () {
        contractSummary_Services.RemoveWatcherToContractById(contractID, function (response_data, status) {
            if (status !== "ERROR") {
                successToast(2000, "You have successfully removed yourself as watcher");
                self.watcherAdded("false");
                self.getContractWatchers();
            }
        });
    }
    self.getContractWatchers = function () {
        contractSummary_Services.GetContractWatchers({ "contractId": contractID }, function (response_data, status) {
            if (status !== "ERROR") {
                self.watchersList.removeAll();
                if (response_data.Response.FindZ_INT_WatchersResponse.Watchers && Array.isArray(response_data.Response.FindZ_INT_WatchersResponse.Watchers)) {
                    response_data.Response.FindZ_INT_WatchersResponse.Watchers.forEach(watcher => {
                        self.watchersList.push({
                            "Name": getTextValue(watcher.RelatedUser.toPerson.FirstName),
                            "Email": getTextValue(watcher.RelatedUser.toPerson.Email)
                        })
                    });
                } else if (response_data.Response.FindZ_INT_WatchersResponse.Watchers) {
                    self.watchersList.push({
                        "Name": getTextValue(response_data.Response.FindZ_INT_WatchersResponse.Watchers.RelatedUser.toPerson.FirstName),
                        "Email": getTextValue(response_data.Response.FindZ_INT_WatchersResponse.Watchers.RelatedUser.toPerson.Email)
                    })
                }
            }
        });
    }

    self.openWatchersList = function () {
        if (self.watchersList().length > 0) {
            $("#id_watchersListDialog").modal();
            self.getContractWatchers();
        }
    }
};

var CustomAttributeModel = function () {
    var self = this;

    // Attribute properties.
    self.id = "";
    self.name = "";
    self.label = "";
    self.dataType = "";
    self.attributeMedadata = "";
    self.attributeId = "";
    self.dateFormat = "";
    self.placeHolder = "";
    self.selectOptions = ko.observableArray([]);
    self.selectedOption = ko.observable('');
    self.value = ko.observable("");
    self.errorMessage = ko.observable("");

    // Flags to handle
    self.isDirty = false;
    self.isEditable = false;
    self.allowDecimal = false;
    self.hasErrors = ko.observable(false);

    //behavior.
    self.changeRadioButton = function (value, iItem, event) {
        iItem.value(value);
        event.stopPropagation();
    }

    self.removeErrorClass = function (data, iEvent) {
        if (data.hasErrors()) {
            $(iEvent.target).removeClass("cc-error");
            data.hasErrors(false);
        }
    }
}

//code for lookups ----start----
function openCountrySelModal(attrData) {
    $('#div_countryLookupModal').modal({
        backdrop: 'static',
        keyboard: false
    })
    ListAllCountries();
    $('button#btn_selectCountryYes').off("click");
    $('button#btn_selectCountryYes').on('click', function (_event) {
        attrData.attr().AttributeValue(l_countryLookup_model.selectedCountryName())
        attrData.attr().LookupAttrID(l_countryLookup_model.selectedCountryID())
        attrData.attr().LookupAttrItemID(l_countryLookup_model.selectedCountryItemID())
       // l_contractSummary_model.layoutModal().mappedAllRelAttList().filter(ele => ele.AttributeName() == "Region")[0].AttributeValue(l_countryLookup_model.selectedRegionName())
    });
}

function ListAllCountries() {
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
        method: "getCountrieswithfilters",
        parameters: {
            "countryName": $("#input_countrySearchFilter").val(),
            "regionName": "",
            "offset": "0",
            "limit": "200"
        },
        success: function (data) {
            addDataToCountriesLookup(data.countries.FindZ_INT_CountryListResponse.RelatedCountries, l_countryLookup_model);
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving countries. Contact your administrator."), 10000);
            return false;
        }
    });
}

function addDataToCountriesLookup(iElementList, iModel) {
    iModel.Countries.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            iElementList.forEach(function (iElement) {
                if (iElement.LinkedCountry) {
                    iModel.Countries.push(iElement);
                }
            });
        }
        else {
            iModel.Countries.push(iElementList);
        }
    }
}

var CountryLookupModel = function () {
    var self = this;
    self.Countries = ko.observableArray([]);
    self.selectedCountryName = ko.observable('');
    self.selectedCountryID = ko.observable('');
    self.selectedCountryItemID = ko.observable('');
    self.selectedRegionName = ko.observable('');
    self.selectedRegionID = ko.observable('');
    self.selectedRegionItemID = ko.observable('');
    self.onSelectCountryRow = function (iItem, event) {
        $(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
        self.selectedCountryName(getTextValue(iItem.LinkedCountry.Country_Name));
        self.selectedCountryID(getTextValue(iItem['RelatedCountries-id'].Id1));
        self.selectedCountryItemID(getTextValue(iItem['RelatedCountries-id'].ItemId1));
        self.selectedRegionName(getTextValue(iItem.Owner.Name));
        self.selectedRegionID(getTextValue(iItem.Owner['Region-id'].Id));
        self.selectedRegionItemID(getTextValue(iItem.Owner['Region-id'].ItemId));
    }
    self.onSelectCountryRadioButton = function (iItem, event) {
        $(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).addClass("cc-radio-on");
        self.selectedCountryName(getTextValue(iItem.LinkedCountry.Country_Name));
        self.selectedCountryID(getTextValue(iItem['RelatedCountries-id'].Id1));
        self.selectedCountryItemID(getTextValue(iItem['RelatedCountries-id'].ItemId1));
        self.selectedRegionName(getTextValue(iItem.Owner.Name));
        self.selectedRegionID(getTextValue(iItem.Owner['Region-id'].Id));
        self.selectedRegionItemID(getTextValue(iItem.Owner['Region-id'].ItemId));
        event.stopPropagation();
    }
};

function openCurrencySelModal(attrData) {
    $('#div_currencyLookupModal').modal({
        backdrop: 'static',
        keyboard: false
    })
    ListAllCurrencies();
    $('button#btn_selectCurrencyYes').off("click");
    $('button#btn_selectCurrencyYes').on('click', function (_event) {
        attrData.attr().AttributeValue(l_currencyLookup_model.selectedCurrencyName())
        attrData.attr().LookupAttrID(l_currencyLookup_model.selectedCurrencyID())
        attrData.attr().LookupAttrItemID(l_currencyLookup_model.selectedCurrencyItemID())
    });
}

function ListAllCurrencies() {
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
        method: "getCurrencieswithfilters",
        parameters: {
            "currencyName": $("#input_currencySearchFilter").val(),
            "offset": "0",
            "limit": "200"
        },
        success: function (data) {
            addDataToCurrenciesLookup(data.currencies.FindZ_INT_CurrencyListResponse.Currency, l_currencyLookup_model);
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving currencies. Contact your administrator."), 10000);
            return false;
        }
    });
}

function addDataToCurrenciesLookup(iElementList, iModel) {
    iModel.Currencies.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            iElementList.forEach(function (iElement) {
                iModel.Currencies.push(iElement);
            });
        }
        else {
            iModel.Currencies.push(iElementList);
        }
    }
}

var CurrencyLookupModel = function () {
    var self = this;
    self.Currencies = ko.observableArray([]);
    self.selectedCurrencyName = ko.observable('');
    self.selectedCurrencyID = ko.observable('');
    self.selectedCurrencyItemID = ko.observable('');
    self.onSelectCurrencyRow = function (iItem, event) {
        $(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
        self.selectedCurrencyName(getTextValue(iItem.Name));
        self.selectedCurrencyID(getTextValue(iItem['Currency-id'].Id));
        self.selectedCurrencyItemID(getTextValue(iItem['Currency-id'].ItemId));
    }
    self.onSelectCurrencyRadioButton = function (iItem, event) {
        $(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).addClass("cc-radio-on");
        self.selectedCurrencyName(getTextValue(iItem.Name));
        self.selectedCurrencyID(getTextValue(iItem['Currency-id'].Id));
        self.selectedCurrencyItemID(getTextValue(iItem['Currency-id'].ItemId));
        event.stopPropagation();
    }
};

function openAmendTypeSelModal(attrData) {
    $('#div_amendmentTypeLookupModal').modal({
        backdrop: 'static',
        keyboard: false
    })
    ListAllAmendTypes();
    $('button#btn_selectAmendTypeYes').off("click");
    $('button#btn_selectAmendTypeYes').on('click', function (_event) {
        attrData.attr().AttributeValue(l_amendTypeLookup_model.selectedAmendTypeName())
        attrData.attr().LookupAttrID(l_amendTypeLookup_model.selectedAmendTypeID())
        attrData.attr().LookupAttrItemID(l_amendTypeLookup_model.selectedAmendTypeItemID())
    });
}

function ListAllAmendTypes() {
    $.cordys.ajax({
        namespace: "http://schemas/OpenTextContractCenter/AmendmentType/operations",
        method: "GetActiveAmendmentTypes",
        parameters: {},
        success: function (data) {
            addDataToAmendTypesLookup(data.AmendmentType, l_amendTypeLookup_model);
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving amendment types. Contact your administrator."), 10000);
            return false;
        }
    });
}

function addDataToAmendTypesLookup(iElementList, iModel) {
    iModel.AmendTypes.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            iElementList.forEach(function (iElement) {
                iModel.AmendTypes.push(iElement);
            });
        }
        else {
            iModel.AmendTypes.push(iElementList);
        }
    }
}

var AmendTypeLookupModel = function () {
    var self = this;
    self.AmendTypes = ko.observableArray([]);
    self.selectedAmendTypeName = ko.observable('');
    self.selectedAmendTypeID = ko.observable('');
    self.selectedAmendTypeItemID = ko.observable('');
    self.onSelectAmendTypeRow = function (iItem, event) {
        $(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
        self.selectedAmendTypeName(getTextValue(iItem.Name));
        self.selectedAmendTypeID(getTextValue(iItem['AmendmentType-id'].Id));
        self.selectedAmendTypeItemID(getTextValue(iItem['AmendmentType-id'].ItemId));
    }
    self.onSelectAmendTypeRadioButton = function (iItem, event) {
        $(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).addClass("cc-radio-on");
        self.selectedAmendTypeName(getTextValue(iItem.Name));
        self.selectedAmendTypeID(getTextValue(iItem['AmendmentType-id'].Id));
        self.selectedAmendTypeItemID(getTextValue(iItem['AmendmentType-id'].ItemId));
        event.stopPropagation();
    }
};

function openContractTermModal(event) {
    if ($(event.currentTarget.parentElement.nextElementSibling.children[0]).css("display") == "none" && ko.dataFor(event.currentTarget.parentElement.previousElementSibling).attr().IsReadOnly() == "FALSE")
        $(event.currentTarget.parentElement.nextElementSibling.children[0]).css("display", "block");
    else
        $(event.currentTarget.parentElement.nextElementSibling.children[0]).css("display", "none");
    addDataToContractTermLookup(event);
    $('button#btn_contractTermYes').off("click");
    $('button#btn_contractTermYes').on('click', function (_event) {
        var contractTerm = l_contractSummary_model.layoutModal().mappedAllRelAttList().filter(ele => ele.AttributeName() == "InitialContractTenure");
        var ContractTermValue;
        if (!$(_event.currentTarget.parentElement.parentElement.getElementsByClassName("input_contractTermMonths")[0]).val().match(/^0*$/) && !$(_event.currentTarget.parentElement.parentElement.getElementsByClassName("input_contractTermDays")[0]).val().match(/^0*$/)) {
            ContractTermValue = ($(_event.currentTarget.parentElement.parentElement.getElementsByClassName("input_contractTermMonths")[0]).val() + " month(s), " + $(_event.currentTarget.parentElement.parentElement.getElementsByClassName("input_contractTermDays")[0]).val() + " day(s)");
        }
        else if (!$(_event.currentTarget.parentElement.parentElement.getElementsByClassName("input_contractTermMonths")[0]).val().match(/^0*$/)) {
            ContractTermValue = ($(_event.currentTarget.parentElement.parentElement.getElementsByClassName("input_contractTermMonths")[0]).val() + " month(s)");
        }
        else if (!$(_event.currentTarget.parentElement.parentElement.getElementsByClassName("input_contractTermDays")[0]).val().match(/^0*$/)) {
            ContractTermValue = ($(_event.currentTarget.parentElement.parentElement.getElementsByClassName("input_contractTermDays")[0]).val() + " day(s)");
        }
        else {
            ContractTermValue = ("");
        }
        contractTerm.forEach(function (element, index) {
            element.AttributeValue(ContractTermValue);
        });
        $(_event.currentTarget.closest(".ContractTermPreviewModel")).css("display", "none");
    });
}

function closeContractTermModal(event) {
    $(event.currentTarget.closest(".ContractTermPreviewModel")).css("display", "none");
}

function addDataToContractTermLookup(event) {
    var months = "";
    var days = "";
    var InitialContractTenure = ko.dataFor(event.currentTarget.parentElement.previousElementSibling).attr().AttributeValue();
    if (InitialContractTenure && InitialContractTenure.lastIndexOf("month(s)") > 0) {
        months = InitialContractTenure.substring(0, InitialContractTenure.lastIndexOf(" month(s)"))
    }
    if (InitialContractTenure && InitialContractTenure.lastIndexOf("day(s)") > 0) {
        if (months != "") {
            days = InitialContractTenure.substring(InitialContractTenure.lastIndexOf("month(s), ") + "month(s), ".length, InitialContractTenure.lastIndexOf(" day(s)"))
        }
        else {
            days = InitialContractTenure.substring(0, InitialContractTenure.lastIndexOf(" day(s)"))
        }
    }
    $(event.currentTarget.parentElement.nextElementSibling.getElementsByClassName("input_contractTermMonths")[0]).val(months);
    $(event.currentTarget.parentElement.nextElementSibling.getElementsByClassName("input_contractTermDays")[0]).val(days);
}

function openAutoRenewDurModal(event) {
    if ($(event.currentTarget.parentElement.nextElementSibling.children[0]).css("display") == "none" && ko.dataFor(event.currentTarget.parentElement.previousElementSibling).attr().IsReadOnly() == "FALSE")
        $(event.currentTarget.parentElement.nextElementSibling.children[0]).css("display", "block");
    else
        $(event.currentTarget.parentElement.nextElementSibling.children[0]).css("display", "none");

    addDataToAutoRenewDurLookup(event);
    $('button#btn_autoRenewDurYes').off("click");
    $('button#btn_autoRenewDurYes').on('click', function (_event) {
        var contractTerm = l_contractSummary_model.layoutModal().mappedAllRelAttList().filter(ele => ele.AttributeName() == "AutoRenewDuration");
        var ContractTermValue;
        if (!$(_event.currentTarget.parentElement.parentElement.getElementsByClassName("input_autoRenewDurMonths")[0]).val().match(/^0*$/) && !$(_event.currentTarget.parentElement.parentElement.getElementsByClassName("input_autoRenewDurDays")[0]).val().match(/^0*$/)) {
            ContractTermValue = ($(_event.currentTarget.parentElement.parentElement.getElementsByClassName("input_autoRenewDurMonths")[0]).val() + " month(s), " + $(_event.currentTarget.parentElement.parentElement.getElementsByClassName("input_autoRenewDurDays")[0]).val() + " day(s)");
        }
        else if (!$(_event.currentTarget.parentElement.parentElement.getElementsByClassName("input_autoRenewDurMonths")[0]).val().match(/^0*$/)) {
            ContractTermValue = ($(_event.currentTarget.parentElement.parentElement.getElementsByClassName("input_autoRenewDurMonths")[0]).val() + " month(s)");
        }
        else if (!$(_event.currentTarget.parentElement.parentElement.getElementsByClassName("input_autoRenewDurDays")[0]).val().match(/^0*$/)) {
            ContractTermValue = ($(_event.currentTarget.parentElement.parentElement.getElementsByClassName("input_autoRenewDurDays")[0]).val() + " day(s)");
        }
        else {
            ContractTermValue = ("");
        }
        contractTerm.forEach(function (element, index) {
            element.AttributeValue(ContractTermValue);
        });
        $(_event.currentTarget.closest(".AutoRenewDurPreviewModel")).css("display", "none");
    });
}

function closeAutoRenewDurModal(event) {
    $(event.currentTarget.closest(".AutoRenewDurPreviewModel")).css("display", "none");
}

function addDataToAutoRenewDurLookup(event) {
    var months = "";
    var days = "";
    var InitialContractTenure = ko.dataFor(event.currentTarget.parentElement.previousElementSibling).attr().AttributeValue();
    if (InitialContractTenure && InitialContractTenure.lastIndexOf("month(s)") > 0) {
        months = InitialContractTenure.substring(0, InitialContractTenure.lastIndexOf(" month(s)"))
    }
    if (InitialContractTenure && InitialContractTenure.lastIndexOf("day(s)") > 0) {
        if (months != "") {
            days = InitialContractTenure.substring(InitialContractTenure.lastIndexOf("month(s), ") + "month(s), ".length, InitialContractTenure.lastIndexOf(" day(s)"))
        }
        else {
            days = InitialContractTenure.substring(0, InitialContractTenure.lastIndexOf(" day(s)"))
        }
    }
    $(event.currentTarget.parentElement.nextElementSibling.getElementsByClassName("input_autoRenewDurMonths")[0]).val(months);
    $(event.currentTarget.parentElement.nextElementSibling.getElementsByClassName("input_autoRenewDurDays")[0]).val(days);
}

function openRenewalFlagSelModal(attrData) {
    $('#div_renewalFlagLookupModal').modal({
        backdrop: 'static',
        keyboard: false
    })
    ListAllRenewalFlags();
    $('button#btn_selectRenewalFlagYes').off("click");
    $('button#btn_selectRenewalFlagYes').on('click', function (_event) {
        attrData.attr().AttributeValue(l_renewalFlagStatusLookup_model.selectedRenewalFlagName())
        attrData.attr().LookupAttrID(l_renewalFlagStatusLookup_model.selectedRenewalFlagID())
        attrData.attr().LookupAttrItemID(l_renewalFlagStatusLookup_model.selectedRenewalFlagItemID())
    });
}

function ListAllRenewalFlags() {
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
        method: "getRenewalFlagswithfilters",
        parameters: {
            "renewalFlagName": $("#input_renewalFlagSearchFilter").val(),
            "offset": "0",
            "limit": "200"
        },
        success: function (data) {
            addDataToRenewalFlagsLookup(data.renewalFlags.FindZ_INT_RenewalFlagResponse.RenewalFlagStatus, l_renewalFlagStatusLookup_model);
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving renewal flags. Contact your administrator."), 10000);
            return false;
        }
    });
}

function addDataToRenewalFlagsLookup(iElementList, iModel) {
    iModel.RenewalFlags.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            iElementList.forEach(function (iElement) {
                iModel.RenewalFlags.push(iElement);
            });
        }
        else {
            iModel.RenewalFlags.push(iElementList);
        }
    }
}

var RenewalFlagLookupModel = function () {
    var self = this;
    self.RenewalFlags = ko.observableArray([]);
    self.selectedRenewalFlagName = ko.observable('');
    self.selectedRenewalFlagID = ko.observable('');
    self.selectedRenewalFlagItemID = ko.observable('');
    self.onSelectRenewalFlagRow = function (iItem, event) {
        $(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
        self.selectedRenewalFlagName(getTextValue(iItem.Name));
        self.selectedRenewalFlagID(getTextValue(iItem['RenewalFlagStatus-id'].Id));
        self.selectedRenewalFlagItemID(getTextValue(iItem['RenewalFlagStatus-id'].ItemId));
    }
    self.onSelectRenewalFlagRadioButton = function (iItem, event) {
        $(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).addClass("cc-radio-on");
        self.selectedRenewalFlagName(getTextValue(iItem.Name));
        self.selectedRenewalFlagID(getTextValue(iItem['RenewalFlagStatus-id'].Id));
        self.selectedRenewalFlagItemID(getTextValue(iItem['RenewalFlagStatus-id'].ItemId));
        event.stopPropagation();
    }
};

function openTermiReasonSelModal(attrData) {
    $('#div_termiReasonLookupModal').modal({
        backdrop: 'static',
        keyboard: false
    })
    ListAllTerminationReasons();
    $('button#btn_selectTerminationReasonYes').off("click");
    $('button#btn_selectTerminationReasonYes').on('click', function (_event) {
        attrData.attr().AttributeValue(l_terminationReasonLookup_model.selectedTerminationReasonName())
        attrData.attr().LookupAttrID(l_terminationReasonLookup_model.selectedTerminationReasonID())
        attrData.attr().LookupAttrItemID(l_terminationReasonLookup_model.selectedTerminationReasonItemID())
    });
}

function ListAllTerminationReasons() {
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
        method: "getTermiReasonswithfilters",
        parameters: {
            "termiReasonName": $("#input_terminationReasonSearchFilter").val(),
            "offset": "0",
            "limit": "200"
        },
        success: function (data) {
            addDataToTerminationReasonsLookup(data.terminationReasons.FindZ_INT_TerminationReasonResponse.TerminationReason, l_terminationReasonLookup_model);
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving termination reasons. Contact your administrator."), 10000);
            return false;
        }
    });
}

function addDataToTerminationReasonsLookup(iElementList, iModel) {
    iModel.TerminationReasons.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            iElementList.forEach(function (iElement) {
                iModel.TerminationReasons.push(iElement);
            });
        }
        else {
            iModel.TerminationReasons.push(iElementList);
        }
    }
}

var TerminationReasonLookupModel = function () {
    var self = this;
    self.TerminationReasons = ko.observableArray([]);
    self.selectedTerminationReasonName = ko.observable('');
    self.selectedTerminationReasonID = ko.observable('');
    self.selectedTerminationReasonItemID = ko.observable('');
    self.onSelectTerminationReasonRow = function (iItem, event) {
        $(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
        self.selectedTerminationReasonName(getTextValue(iItem.Reason));
        self.selectedTerminationReasonID(getTextValue(iItem['TerminationReason-id'].Id));
        self.selectedTerminationReasonItemID(getTextValue(iItem['TerminationReason-id'].ItemId));
    }
    self.onSelectTerminationReasonRadioButton = function (iItem, event) {
        $(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).addClass("cc-radio-on");
        self.selectedTerminationReasonName(getTextValue(iItem.Reason));
        self.selectedTerminationReasonID(getTextValue(iItem['TerminationReason-id'].Id));
        self.selectedTerminationReasonItemID(getTextValue(iItem['TerminationReason-id'].ItemId));
        event.stopPropagation();
    }
};

function openDocOrigSelModal(attrData) {
    $('#div_docOriginLookupModal').modal({
        backdrop: 'static',
        keyboard: false
    })
    ListAllDocOrigins();
    $('button#btn_selectDocOriginYes').off("click");
    $('button#btn_selectDocOriginYes').on('click', function (_event) {
        attrData.attr().AttributeValue(l_documentOriginationLookup_model.selectedDocOriginName())
        attrData.attr().LookupAttrID(l_documentOriginationLookup_model.selectedDocOriginID())
        attrData.attr().LookupAttrItemID(l_documentOriginationLookup_model.selectedDocOriginItemID())
    });
}

function ListAllDocOrigins() {
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
        method: "getDocOriginswithfilters",
        parameters: {
            "docOriginName": $("#input_docOriginSearchFilter").val(),
            "offset": "0",
            "limit": "200"
        },
        success: function (data) {
            addDataToDocOriginsLookup(data.docOrigins.FindZ_INT_DocumentOriginationResponse.DocumentOrigination, l_documentOriginationLookup_model);
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving contract originations. Contact your administrator."), 10000);
            return false;
        }
    });
}

function addDataToDocOriginsLookup(iElementList, iModel) {
    iModel.DocOrigins.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            iElementList.forEach(function (iElement) {
                iModel.DocOrigins.push(iElement);
            });
        }
        else {
            iModel.DocOrigins.push(iElementList);
        }
    }
}

var DocumentOriginationLookupModel = function () {
    var self = this;
    self.DocOrigins = ko.observableArray([]);
    self.selectedDocOriginName = ko.observable('');
    self.selectedDocOriginID = ko.observable('');
    self.selectedDocOriginItemID = ko.observable('');
    self.onSelectDocOriginRow = function (iItem, event) {
        $(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
        self.selectedDocOriginName(getTextValue(iItem.Name));
        self.selectedDocOriginID(getTextValue(iItem['DocumentOrigination-id'].Id));
        self.selectedDocOriginItemID(getTextValue(iItem['DocumentOrigination-id'].ItemId));
    }
    self.onSelectDocOriginRadioButton = function (iItem, event) {
        $(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).addClass("cc-radio-on");
        self.selectedDocOriginName(getTextValue(iItem.Name));
        self.selectedDocOriginID(getTextValue(iItem['DocumentOrigination-id'].Id));
        self.selectedDocOriginItemID(getTextValue(iItem['DocumentOrigination-id'].ItemId));
        event.stopPropagation();
    }
};
//code for lookups ----end----

//code for clear ----start----

function clearLookupselData(attrData) {
	if(attrData.attr().LookupAttrItemID()!=""){
		attrData.attr().AttributeValue('');
        attrData.attr().LookupAttrID('');
        attrData.attr().LookupAttrItemID('');
	}
}

function clearDateData(attrData, event) {
	if(attrData.attr().AttributeValue()!=""){
		var parentElement = event.currentTarget.parentElement;
		var inputElement = parentElement.previousElementSibling;
		inputElement.value="";
		 attrData.attr().AttributeValue('');
	}
}

//code for clear ----end----

function ContractAccountsInfoModel() {
    var self = this;

    self.isLoaded = ko.observable(false);
    self.displayScreen = ko.observable(false);

    //Save action checks
    self.isEditable = ko.observable(false);
    self.isINTDirty = ko.observable(false);
    self.isEXTDirty = ko.observable(false);
    self.INTDataLoaded = ko.observable(false);
    self.EXTDataLoaded = ko.observable(false);

    self.l_INTParties_model = new InternalPartiesModel();
    self.l_EXTParties_model = new ExternalPartiesModel();
    self.l_addPartytoContractSelection_model = new PartiesToAddModel();
    self.l_addContacttoContractSelection_model = new ContactsToAddModel();

    self.loadPageContent = function () {
        self.isLoaded(true);
        self.l_INTPartiesFilter_model = new InternalPartiesFilterModel();
        self.l_EXTPartiesFilter_model = new ExternalPartiesFilterModel();
        //checkForUserRole();
        self.isEditable(_userStateEditabilityFlag);
        listINTParties();
        listEXTParties();

        self.saveAttributes = function () {

            if (self.isINTDirty() || self.isEXTDirty()) {
                updateAccounts();
            }
        }
        self.toggleIntPartiesFilter = function (iEventObject) {
            if ($("#div_INTPartiesListFilter").attr('apps-toggle') == "expanded") {
                $("#div_INTPartiesListFilter").toggle();
                document.getElementById("div_INTPartiesListFilter").setAttribute("apps-toggle", 'collapsed');
                $("#div_INTPartiesList").removeClass("col-md-9");
                $("#div_INTPartiesList").addClass("col-md-12");
            }
            else if ($("#div_INTPartiesListFilter").attr('apps-toggle') == "collapsed") {
                $("#div_INTPartiesListFilter").toggle();
                document.getElementById("div_INTPartiesListFilter").setAttribute("apps-toggle", 'expanded');
                $("#div_INTPartiesList").removeClass("col-md-12");
                $("#div_INTPartiesList").addClass("col-md-9");
            }
        };
        self.toggleExtPartiesFilter = function (iEventObject) {
            if ($("#div_EXTPartiesListFilter").attr('apps-toggle') == "expanded") {
                $("#div_EXTPartiesListFilter").toggle();
                document.getElementById("div_EXTPartiesListFilter").setAttribute("apps-toggle", 'collapsed');
                $("#div_EXTPartiesList").removeClass("col-md-9");
                $("#div_EXTPartiesList").addClass("col-md-12");
            }
            else if ($("#div_EXTPartiesListFilter").attr('apps-toggle') == "collapsed") {
                $("#div_EXTPartiesListFilter").toggle();
                document.getElementById("div_EXTPartiesListFilter").setAttribute("apps-toggle", 'expanded');
                $("#div_EXTPartiesList").removeClass("col-md-12");
                $("#div_EXTPartiesList").addClass("col-md-9");
            }
        };
        $(".cc-filter-header").click(function (iEventObject) {
            var l_headerSpan = $(this)
            l_headerSpan.next().slideToggle();
            if (l_headerSpan.attr('apps-toggle') == "expanded") {
                hideOrShowFilterContainerBody(l_headerSpan[0], false);
            }
            else if (l_headerSpan.attr('apps-toggle') == "collapsed") {
                hideOrShowFilterContainerBody(l_headerSpan[0], true);
            }
        });

        window.parent.document.addEventListener("click", e => {
            if (self.isINTDirty() || self.isEXTDirty()) {
                $("#unsavedWarningModal").modal();
                e.stopPropagation();
                e.preventDefault();
            }
        }, true);
    }
}

function ContractLinesInfoModel() {
    var self = this;
    self.contractLines = ko.observableArray([]);

    self.isLoaded = ko.observable(false);
    self.displayScreen = ko.observable(false);
    self.loadPageContent = function () {
        self.fetchContractLines();
    }

    self.fetchContractLines = function () {
        self.isLoaded(true);
        contractSummary_Services.GetContractLinesById(contractID, function (response_data, status) {
            if (status !== "ERROR") {
                _addContractLinesData(response_data.ContractLines, self);
            }
        });
    }

    function _addContractLinesData(iElementList, iModel) {
        iModel.contractLines.removeAll();
        if (iElementList && iElementList.ContractLine) {
            if (iElementList.ContractLine.length) {
                // iModel.numOfTypesInCurrentPage(iElementList.length);
                iElementList.ContractLine.forEach(function (iElement) {
                    iModel.contractLines.push(iElement);
                });
            }
            else {
                // iModel.numOfTypesInCurrentPage("1");
                iModel.contractLines.push(iElementList.ContractLine);
            }
        }
    }
}

function ContractTagsInfoModel() {
    var self = this;

    self.ContractTagsList = ko.observableArray([]);
    self.ContractTagsSelectedList = ko.observableArray([]);
    self.currentSearchTag = ko.observable();
    self.newTagName = ko.observable();
    self.isAllowTagCreate = ko.observable(false);
    self.isEditable = ko.observable(false);

    self.isLoaded = ko.observable(false);
    self.displayScreen = ko.observable(false);
    self.loadPageContent = function () {
        self.isEditable(_userStateEditabilityFlag);
        loadSelectedCTRTags();
        self.AllowTagCreateProp();
    }
    self.computeTagsList = ko.computed(function () {
        if (!self.currentSearchTag()) {
            return self.ContractTagsList();
        } else {
            return ko.utils.arrayFilter(self.ContractTagsList(), function (ele) {
                return ele.Name.includes(self.currentSearchTag());
            });
        }
    });
    self.filterTagsList = function () {
        self.currentSearchTag(document.getElementById("searchTag").value);
        self.newTagName(document.getElementById("searchTag").value);
    }

    self.AddSelectedTag = function (iItem, allowToast) {
        var params = {
            "Contract-id": {
                'ItemId': contractItemID
            },
            "ContractRelatedTags": {
                "GCTag-id": {
                    'Id': iItem.id
                }
            }
        };
        contractSummary_Services.AddContractRelatedTag(params, function (response_data, status) {
            if (status !== "ERROR") {
                var l_TagObj = {};
                l_TagObj.id = iItem.id;
                l_TagObj.Name = iItem.Name;
                document.getElementById("searchTag").value = '';
                self.ContractTagsSelectedList.push(l_TagObj);
                self.ContractTagsList.remove(iItem);
                self.filterTagsList();
                if (allowToast != false) {
                    successToast(2000, "You have successfully added a tag.");
                }
            }
        });
        return true;
    }

    self.RemoveExistingTag = function (iItem) {
        //var obj=this;
        var params = {
            "Contract-id": {
                'ItemId': contractItemID
            },
            "ContractRelatedTags": {
                "GCTag-id": {
                    'Id': iItem.id
                }
            }
        };
        contractSummary_Services.RemoveContractRelatedTag(params, function (response_data, status) {
            if (status !== "ERROR") {
                var l_TagObj = {};
                l_TagObj.id = iItem.id;
                l_TagObj.Name = iItem.Name;
                self.ContractTagsSelectedList.remove(iItem);
                self.ContractTagsList.push(l_TagObj);
                successToast(2000, "You have successfully removed a tag.");
            }
        });
        toggleDropDown();
        return true;
    }
    self.CreateAndAddTag = function () {
        var params = {
            "GCTag-create": {
                'TagName': document.getElementById("searchTag").value
            }
        };
        contractSummary_Services.CreateNewTag(params, function (response_data, status) {
            if (status !== "ERROR" && response_data && response_data.GCTag) {
                var l_TagObj = {};
                l_TagObj.id = response_data.GCTag["GCTag-id"].Id
                l_TagObj.Name = response_data.GCTag.TagName;
                self.AddSelectedTag(l_TagObj, false);
                self.newTagName('');
                successToast(2000, "You have successfully created and added a tag.");
            } else {
                document.getElementById("searchTag").value = '';
                self.newTagName('');
            }
        });
        return true;
    }

    self.AllowTagCreateProp = function () {
        contractSummary_Services.GetContractCustomTabDetails("ALLOW_TAG_CREATE", function (response_data, status) {
            if (status !== "ERROR") {
                if (response_data && response_data.GCProperties) {
                    var isAllow = getTextValue(response_data.GCProperties.value)
                    if (isAllow === "Yes") {
                        self.isAllowTagCreate(true);
                    }
                }
            }
        });
    }
}

function ContractPONumbersModel() {
    var self = this;
    self.POs = ko.observableArray([]);
    self.numOfPOs = ko.observable('');
    self.PONumber = ko.observable();
    self.Description = ko.observable();
    self.PONumberID = ko.observable();
    self.PONumberItemID = ko.observable();
    self.isEditable = ko.observable(false);
    self.loadPageContent = function () {
        self.isEditable(_userStateEditabilityFlag);
        ListAllPONumbers();
    }
    self.onPORowCheckboxValueChanged = function (iItem, event) {
        var l_currentClassName = event.currentTarget.className;
        if (l_currentClassName == "cc-select-column cc-checkbox-off") {
            $(event.currentTarget).removeClass("cc-checkbox-off")
            $(event.currentTarget).addClass("cc-checkbox-on")
            selectedPOListMap[iItem["RelatedPOs-id"].Id] = iItem.PONumber;
            $(event.currentTarget.parentElement.parentElement).css("background-color", "#CBD3D9")
        }
        else if (l_currentClassName == "cc-select-column cc-checkbox-on") {
            $(event.currentTarget).removeClass("cc-checkbox-on")
            $(event.currentTarget).addClass("cc-checkbox-off")
            delete selectedPOListMap[iItem["RelatedPOs-id"].Id];
            $(event.currentTarget.parentElement.parentElement).css("background-color", "transparent")
        }
        event.stopPropagation();
        if (Object.keys(selectedPOListMap).length <= 0) {
            $("#div_selectAllPos").removeClass("cc-checkbox-select-all-partial");
            $("#div_selectAllPos").removeClass("cc-checkbox-select-all-on");
            $("#div_selectAllPos").addClass("cc-checkbox-select-all-off");
            $("#btn_deletePOFromActionBar").css("display", "none");
        } else if (Object.keys(selectedPOListMap).length >= 1 && Object.keys(selectedPOListMap).length < l_contract_PONumbers_model.numOfPOs()) {
            $("#div_selectAllPos").removeClass("cc-checkbox-select-all-off");
            $("#div_selectAllPos").removeClass("cc-checkbox-select-all-on");
            $("#div_selectAllPos").addClass("cc-checkbox-select-all-partial");
            $("#btn_deletePOFromActionBar").css("display", "inline");
        } else if (Object.keys(selectedPOListMap).length == l_contract_PONumbers_model.numOfPOs()) {
            $("#div_selectAllPos").removeClass("cc-checkbox-select-all-off");
            $("#div_selectAllPos").removeClass("cc-checkbox-select-all-partial");
            $("#div_selectAllPos").addClass("cc-checkbox-select-all-on");
            $("#btn_deletePOFromActionBar").css("display", "inline");
        }
    }
    self.onSelectAllPosCheckboxValueChanged = function (iItem, event) {
        var l_currentClassName = event.currentTarget.className;
        if (l_currentClassName == "cc-select-column cc-checkbox-select-all-off" || l_currentClassName == "cc-select-column cc-checkbox-select-all-partial") {
            $(event.currentTarget).removeClass("cc-checkbox-select-all-off");
            $(event.currentTarget).removeClass("cc-checkbox-select-all-partial");
            $(event.currentTarget).addClass("cc-checkbox-select-all-on");
            $("#table_POs").find('tbody .cc-select-column').removeClass("cc-checkbox-off");
            $("#table_POs").find('tbody .cc-select-column').addClass("cc-checkbox-on");
            $("#table_POs").find('tbody tr').css("background-color", "#CBD3D9");
            $("#btn_deletePOFromActionBar").css("display", "inline");
            l_contract_PONumbers_model.POs().forEach(function (iToken) {
                selectedPOListMap[iToken["RelatedPOs-id"].Id] = iToken.PONumber;
            });
        }
        else if (l_currentClassName == "cc-select-column cc-checkbox-select-all-on") {
            $(event.currentTarget).removeClass("cc-checkbox-select-all-on");
            $(event.currentTarget).addClass("cc-checkbox-select-all-off");
            $("#table_POs").find('tbody .cc-select-column').removeClass("cc-checkbox-on");
            $("#table_POs").find('tbody .cc-select-column').addClass("cc-checkbox-off");
            $("#table_POs").find('tbody tr').css("background-color", "transparent")
            $("#btn_deletePOFromActionBar").css("display", "none");
            selectedPOListMap = {};
        }
        event.stopPropagation();
    }
}

function ListAllPONumbers() {
    $("#btn_deletePOFromActionBar").css("display", "none");
    $("#div_selectAllPos").removeClass("cc-checkbox-select-all-partial");
    $("#div_selectAllPos").removeClass("cc-checkbox-select-all-on");
    $("#div_selectAllPos").addClass("cc-checkbox-select-all-off");
    selectedPOListMap = {};
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
        method: "GetPOswithfilters",
        parameters: {
            "contractID": contractID,
            "PONumber": "",
            "offset": "0",
            "limit": "200"
        },
        success: function (data) {

            addDataToPOsList(data.PoNumbers.FindZ_INT_PONumbersResponse.RelatedPOs, l_contract_PONumbers_model);
            if (undefined != data.PoNumbers.FindZ_INT_PONumbersResponse["@total"]) {
                l_contract_PONumbers_model.numOfPOs(data.PoNumbers.FindZ_INT_PONumbersResponse["@total"]);
            } else {
                l_contract_PONumbers_model.numOfPOs(0);
            }
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_POlistErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving PO numbers. Contact your administrator."), 10000);
            return false;
        }
    });
}

function addDataToPOsList(iElementList, iModel) {
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

function createPONumber() {
    if (document.getElementById("input_PONumber") && document.getElementById("input_PONumber").value) {
        $.cordys.ajax({
            namespace: "http://schemas/OpenTextContractCenter/RelatedPOs/operations",
            method: "CreateRelatedPOs",
            parameters: {
                "RelatedPOs-create": {
                    "PONumber": document.getElementById("input_PONumber") ? document.getElementById("input_PONumber").value : "",
                    "Description": document.getElementById("input_Description") ? document.getElementById("input_Description").value : "",
                    "RelatedContract": {
                        "Contract-id": {
                            "Id": contractID,
                        },
                    },
                }
            },
            success: function (data) {
                $('#div_createPOModal').modal('hide');
                successToast(3000, getTranslationMessage("PO number created succesfully."));
                ListAllPONumbers();
            },
            error: function (responseFailure) {
                showOrHideErrorInfo("div_POmodalErrorInfoArea", true, getTranslationMessage("An error occurred while creating PO number. Contact your administrator."), 10000);
                return false;
            }
        });
    }
}

function openPOCreateForm(i_ctrItem) {
    $('#div_createPOModal').modal({
        backdrop: 'static',
        keyboard: false
    })
    document.getElementById("input_PONumber") ? document.getElementById("input_PONumber").value = "" : "";
    document.getElementById("input_Description") ? document.getElementById("input_Description").value = "" : "";
}

function deletePOFromActionBar() {
    $("#div_deletePOModal").modal({
        backdrop: 'static',
        keyboard: false
    });
    $("#span_NumOfPOsToDelete").text(" (" + Object.keys(selectedPOListMap).length + " items)");
    $('button#btn_deletePOYes').off("click");
    $('button#btn_deletePOYes').on('click', function (_event) {
        for (iElement in selectedPOListMap) {
            $.cordys.ajax({
                namespace: "http://schemas/OpenTextContractCenter/RelatedPOs/operations",
                method: "DeleteRelatedPOs",
                parameters: {
                    "RelatedPOs-id": {
                        "Id": iElement,
                    },
                },
                success: function (data) {
                    successToast(3000, getTranslationMessage("PO number deleted succesfully."));
                },
                error: function (responseFailure) {
                    $('#div_deletePOModal').modal('hide');
                    showOrHideErrorInfo("div_POlistErrorInfoArea", true, getTranslationMessage("An error occurred while deleting PO number. Contact your administrator."), 10000);
                    return false;
                }
            });
            $('#div_deletePOModal').modal('hide');
        }
        ListAllPONumbers();
    });
}

function toggleDropDown() {
    if (_userStateEditabilityFlag) {
        document.getElementById("dropdown-box").classList.toggle("show");
    }
}

function loadSelectedCTRTags() {
    $.cordys.ajax({
        method: "GetContractRelatedTags",
        namespace: "http://schemas/OpenTextContractCenter/Contract/operations",
        parameters:
        {
            "Contract-id": {
                'ItemId': contractItemID
            }
        },
        success: function (data) {
            if (data && data.GCTag) {
                l_contract_tags_info_model.ContractTagsSelectedList.removeAll();
                var iElementList = data.GCTag;
                if (iElementList) {
                    if (iElementList.length) {
                        iElementList.forEach(function (iElement) {
                            var l_TagObj = {};
                            l_TagObj.id = iElement['GCTag-id'].Id;
                            l_TagObj.Name = getTextValue(iElement.TagName);
                            l_contract_tags_info_model.ContractTagsSelectedList.push(l_TagObj);
                        });
                    }
                    else {
                        var l_TagObj = {};
                        l_TagObj.id = iElementList['GCTag-id'].Id;
                        l_TagObj.Name = getTextValue(iElementList.TagName);
                        l_contract_tags_info_model.ContractTagsSelectedList.push(l_TagObj);
                    }
                }
            }
            loadContractTags();
        },
        error: function (responseFailure) {

            return false;
        }
    });
}

function loadContractTags() {
    $.cordys.ajax({
        method: "FetchAllTags",
        namespace: "http://schemas/OpenTextBasicComponents/GCTag/operations",

        success: function (data) {
            if (data) {
                //responseCallback(responseSuccess);
                addDataToTagsDropDown(data.GCTag, l_contract_tags_info_model);
            }
        },
        error: function (responseFailure) {
            responseCallback(responseFailure, "ERROR");

            return false;
        }
    });
}

function addDataToTagsDropDown(iElementList, iModel) {
    iModel.ContractTagsList.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            for (var i = 0; i < iElementList.length; i++) {
                var iElement = iElementList[i];
                var l_TagObj = {};
                l_TagObj.id = iElement['GCTag-id'].Id;
                l_TagObj.Name = getTextValue(iElement.TagName);
                var foundElement = existsInArray(l_contract_tags_info_model.ContractTagsSelectedList(), l_TagObj.id);
                if (!foundElement) {
                    iModel.ContractTagsList.push(l_TagObj);
                }
            }
        }
        else {
            var l_TagObj = {};
            l_TagObj.id = iElementList['GCTag-id'].Id;
            l_TagObj.Name = getTextValue(iElementList.TagName);
            var foundElement = existsInArray(l_contract_tags_info_model.ContractTagsSelectedList(), l_TagObj.id);
            if (!foundElement) {
                iModel.ContractTagsList.push(l_TagObj);
            }
        }
    }
    l_contract_tags_info_model.isLoaded(true);
}

function existsInArray(array, id) {
    for (var _i = 0; _i < array.length; _i++) {
        if (array[_i].id == id) {
            return true;
        }
    }
    return false;
}

ko.bindingHandlers.datepicker = {
    init: function (element, valueAccessor, allBindingsAccessor) {
        var options = allBindingsAccessor().datepickerOptions || {},
            $el = $(element);

        //initialize datepicker with some optional options
        $el.datepicker(options);

        if (valueAccessor()()) {
            $el.datepicker('setDate', moment(valueAccessor()(), DATE_FORMAT_YYYY_MM_DD.label).toDate());
            $el[0].value = formateDatetoLocale(valueAccessor()());
        }

        //handle the field changing
        ko.utils.registerEventHandler(element, "change", function () {
            var observable = valueAccessor();
            observable(($el.datepicker("getDate")));
            element.value = formateDatetoLocale($.datepicker.formatDate('yy-mm-dd', $el.datepicker("getDate")));
        });

        //handle disposal (if KO removes by the template binding)
        ko.utils.domNodeDisposal.addDisposeCallback(element, function () {
            $el.datepicker("destroy");
        });

    },
    update: function (element, valueAccessor) {
        var value = ko.utils.unwrapObservable(valueAccessor()),
            $el = $(element),
            current = $el.datepicker("getDate");

        if (value && (value - current !== 0)) {
            $el.datepicker("setDate", (moment(value, DATE_FORMAT_YYYY_MM_DD.label).toDate()));
            $el[0].value = formateDatetoLocale(moment(value, DATE_FORMAT_YYYY_MM_DD.label).format("YYYY-MM-DD"));
        }
    }
};

ko.extenders.trackValueChange = function (target, track) {
    if (track) {
        target.hasValueChanged = ko.observable(false);
        target.originalValue = target();
        target.subscribe(function (newValue) {
            if (l_contractSummary_model.startTrackValueChanges()) {
                target.hasValueChanged(newValue != target.originalValue);
            }
            if (target.hasValueChanged()) {
                l_contractSummary_model.valueChangesExists(true);
            }
        });
    }
    return target;
};

function saveCurrentTabData() {
    var ctrPropTags = {};
    var customPropTags = {};
    customPropTags.CustomAttributes = [];
    var ctrPropstoUpdate = l_contractSummary_model.layoutModal().mappedAllRelAttList().filter(ele => ele.AttributeValue.hasValueChanged() == true);
    if (checkforMandatoryProps(ctrPropstoUpdate)) {
        if (checkforActionNotifValues(ctrPropstoUpdate)) {
            for (let index = 0; index < ctrPropstoUpdate.length; index++) {
                const ctrProp = ctrPropstoUpdate[index];
                if (ctrProp.Type() === "CONTRACTATTRIBUTE") {
                    if (ctrProp.AttrDefType() == "LOOKUP") {
						if(ctrProp.AttributeName()=="DefaultDocument" || ctrProp.AttributeName()=="Country")
							ctrPropTags[ctrProp.AttributeName()] = getInstanceID(ctrProp.LookupAttrItemID(), "ItemId1");
						else
							ctrPropTags[ctrProp.AttributeName()] = getInstanceID(ctrProp.LookupAttrItemID(), "ItemId");
                    } else if (ctrProp.AttrDefType() == "DURATION") {
                        var months = "";
                        var days = "";
                        if (ctrProp.AttributeValue().length > 0) {
                            if (ctrProp.AttributeValue().lastIndexOf("month(s)") > 0) {
                                months = ctrProp.AttributeValue().substring(0, ctrProp.AttributeValue().lastIndexOf(" month(s)"))
                            }
                            if (ctrProp.AttributeValue().lastIndexOf("day(s)") > 0) {
                                if (months != "") {
                                    days = ctrProp.AttributeValue().substring(ctrProp.AttributeValue().lastIndexOf("month(s), ") + "month(s), ".length, ctrProp.AttributeValue().lastIndexOf(" day(s)"))
                                    ctrPropTags[ctrProp.AttributeName()] = ("P" + months + "M" + days + "D");
                                }
                                else {
                                    days = ctrProp.AttributeValue().substring(0, ctrProp.AttributeValue().lastIndexOf(" day(s)"))
                                    ctrPropTags[ctrProp.AttributeName()] = ("P" + days + "D");
                                }
                            }
                            else {
                                ctrPropTags[ctrProp.AttributeName()] = ("P" + months + "M");
                            }
                        }
                        else {
                            ctrPropTags[ctrProp.AttributeName()] = "";
                        }
                    } else if (ctrProp.AttrDefType() == DATA_TYPE_DATE) {
                        ctrPropTags[ctrProp.AttributeName()] = ctrProp.AttributeValue() != "" ? $.datepicker.formatDate("yy-mm-dd", ctrProp.AttributeValue()) : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
                    } else {
					ctrPropTags[ctrProp.AttributeName()] = (ctrProp.AttributeValue() != "" &&  ctrProp.AttributeValue() != "select an option")? ctrProp.AttributeValue() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
                    }
                } else if (ctrProp.Type() === "CUSTATTRIBUTE") {
                    var CustomAttribute = {};
                    CustomAttribute.id = ctrProp.CustAttributeId();
                    CustomAttribute.id1 = ctrProp.CustAttributeId1();
                    CustomAttribute.name = ctrProp.AttributeName();
                    CustomAttribute.dataType = ctrProp.AttrDefType();
                    if (ctrProp.AttrDefType() == DATA_TYPE_DATE) {
                        CustomAttribute.value = $.datepicker.formatDate("yy-mm-dd", ctrProp.AttributeValue());
                    } else if (ctrProp.AttrDefType() == DATA_TYPE_BOOLEAN) {
                        CustomAttribute.value = ctrProp.AttributeValue() == "true" ? 'true' : 'false';
                    }
                    else {
                        CustomAttribute.value = (ctrProp.AttributeValue() != "" &&  ctrProp.AttributeValue() != "select an option") ? ctrProp.AttributeValue() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
                    }
                    customPropTags.CustomAttributes.push({ 'CustomAttribute': CustomAttribute });
                }
            }
            var ctrAttrProps = {};
            ctrAttrProps.contractID = l_contractSummary_model.contractID();
            ctrAttrProps.ctrAttributes = ctrPropTags;
            ctrAttrProps.custAttributes = customPropTags;
            contractSummary_Services.UpdateCtrAttrValues(ctrAttrProps, function (data, status) {
                if (status === "SUCCESS") {
                    successToast(2000, "Changes saved successfully");
                    document.getElementsByClassName('cc-loading-overlay')[0].classList.toggle('is-active');
                    l_contractSummary_model.startTrackValueChanges(false);
                    l_contractSummary_model.valueChangesExists(false);
					l_contractSummary_model.contractProperties = {};
                    l_contractSummary_model.fetchContractAttrDetails(function () { });
                    if (ctrAttrProps.ctrAttributes["ContractName"])
                        window.top.publicAPIProvider.getItemData(contractItemID, { "refresh": true });
                }
            });
        } else {
            showOrHideErrorInfo("div_ErrorInfoArea", true, getTranslationMessage("'Notify before expiration (days)' must be greater than 'Act before expiration (days)'."), 5000);
        }
    } else {
        showOrHideErrorInfo("div_ErrorInfoArea", true, getTranslationMessage("Mandatory fields cannot be empty."), 5000);
    }
}

function checkforActionNotifValues(ctrPropstoUpdate) {
    l_actionDurationtoUpdate = ctrPropstoUpdate.filter(ele => ele.AttributeName() == "ActionDuration");
    l_notifDurationtoUpdate = ctrPropstoUpdate.filter(ele => ele.AttributeName() == "NotificationDuration");
    if (l_actionDurationtoUpdate.length > 0 && l_notifDurationtoUpdate.length > 0) {
        if (parseInt(l_actionDurationtoUpdate[0].AttributeValue()) > parseInt(l_notifDurationtoUpdate[0].AttributeValue())) {
            return false;
        }
    } else if (l_actionDurationtoUpdate.length > 0) {
        l_notifDurationValue = l_contractSummary_model.layoutModal().mappedAllRelAttList().filter(ele => ele.AttributeName() == "NotificationDuration");
        if (l_notifDurationValue.length > 0) {
            if (parseInt(l_actionDurationtoUpdate[0].AttributeValue()) > parseInt(l_notifDurationValue[0].AttributeValue())) {
                return false;
            }
        }
    } else if (l_notifDurationtoUpdate.length > 0) {
        l_actionDurationValue = l_contractSummary_model.layoutModal().mappedAllRelAttList().filter(ele => ele.AttributeName() == "ActionDuration");
        if (l_actionDurationValue.length > 0) {
            if (parseInt(l_actionDurationValue[0].AttributeValue()) > parseInt(l_notifDurationtoUpdate[0].AttributeValue())) {
                return false;
            }
        }
    }
    return true;
}

function checkforMandatoryProps(ctrPropstoUpdate) {
    var mandatoryProps = l_contractSummary_model.layoutModal().mappedAllRelAttList().filter(ele => ele.IsMandatory() == "TRUE");
    var mandatoryEditableProps = mandatoryProps.filter(ele => ele.IsReadOnly() == "FALSE");
    for (let index = 0; index < mandatoryEditableProps.length; index++) {
        const mandatoryEditProp = mandatoryEditableProps[index];
        if (mandatoryEditProp.AttributeValue() == "" || mandatoryEditProp.AttributeValue() == undefined) {
            return false;
        }
    }
    return true;
}

function cancelCurrentTabChanges() {
    l_contractSummary_model.startTrackValueChanges(false);
    l_contractSummary_model.valueChangesExists(false);
    l_contractSummary_model.populateRelatedLayoutData();
}

function checkForEditability(callBack) {
    isUserEligibletoEdit(function (userEligibilityFlag) {
        var stateEligibilityFlag = isStateEligibleforEdit();
        if (userEligibilityFlag && stateEligibilityFlag) {
            _userStateEditabilityFlag = true;
        }
        if (callBack) {
            callBack();
        }
    });
}

function isUserEligibletoEdit(callbackfunc) {
    $.cordys.ajax(
        {
            method: "CheckCurrentUserInRoles",
            namespace: "http://schemas.opentext.com/apps/cc/configworkflow/20.2",
            async: false,
            parameters:
            {
                "Roles": {
                    "Role": [
                        "Contract Administrator",
                        "Contract Manager",
                        "Contract Author"
                    ]
                }
            },
        }).done(function (data) {
            var result = getTextValue(data.IsCurrentUserRoles);
            if (result && result.toLowerCase() == "true") {
                _userEditabilityFlag = true;
                callbackfunc(true);
            } else {
                callbackfunc(false);
            }
        }).fail(function (error) {
        })
}

function isStateEligibleforEdit() {
    if (lifecycle_CurrentState == "Draft" || lifecycle_CurrentState == "Negotiation" || lifecycle_CurrentState == "Pre-Execution" || lifecycle_CurrentState == "Execution") {
        return true;
    }
    if (isExecutedContract && isExecutedContract == "true" && (lifecycle_CurrentState == "Pending Activation" || lifecycle_CurrentState == "Active")) {
        return true;
    }
    return false;
}

function collapseExpandGroup(currEle) {
	if (currEle.getAttribute("class").includes('collapsed')) {
		currEle.parentElement.parentElement.nextElementSibling.classList.toggle("in");
		currEle.classList.toggle("collapsed")
	}
	else {
		currEle.parentElement.parentElement.nextElementSibling.classList.toggle("in");
		currEle.classList.toggle("collapsed")
	}
}

function getInstanceID(ID, tagName) {
    if (ID !== "") {
        return { [tagName]: ID };
    } else {
        return { [tagName]: { '@xsi:nil': 'true', '@xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance' } };
    }
}

$(function () {
    var i_locale = getlocale();
    var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
    translateLabels("com/opentext/apps/cc/htm/contractSummary", i_locale);
    loadRTLIfRequired(i_locale, rtl_css);
    if (window.parent.parent) {
        var contractSummary = $('[src*="contractSummary.htm"]', window.parent.parent.document);
        if (contractSummary) {
            contractSummary.css('border', 'none');
        }
    }

    document.getElementsByClassName('cc-loading-overlay')[0].classList.toggle('is-active');

    l_contractSummary_model = new ContractSummaryModel();
    ko.applyBindings(l_contractSummary_model, document.getElementById("div_contractSummary"));
    ko.applyBindings(l_contractSummary_model, document.getElementById("id_watchersListDialog"));
    l_countryLookup_model = new CountryLookupModel();
    ko.applyBindings(l_countryLookup_model, document.getElementById("div_countryLookupModal"));
    l_currencyLookup_model = new CurrencyLookupModel();
    ko.applyBindings(l_currencyLookup_model, document.getElementById("div_currencyLookupModal"));
    l_amendTypeLookup_model = new AmendTypeLookupModel();
    ko.applyBindings(l_amendTypeLookup_model, document.getElementById("div_amendmentTypeLookupModal"));
    l_renewalFlagStatusLookup_model = new RenewalFlagLookupModel();
    ko.applyBindings(l_renewalFlagStatusLookup_model, document.getElementById("div_renewalFlagLookupModal"));
    l_terminationReasonLookup_model = new TerminationReasonLookupModel();
    ko.applyBindings(l_terminationReasonLookup_model, document.getElementById("div_termiReasonLookupModal"));
    l_documentOriginationLookup_model = new DocumentOriginationLookupModel();
    ko.applyBindings(l_documentOriginationLookup_model, document.getElementById("div_docOriginLookupModal"));

    l_contractLineDetails_model = new ContractLineDetailsModel();
    ko.applyBindings(l_contractLineDetails_model, document.getElementById("div_createOrUpdateContractLineModal"));
    l_serviceLookup_model = new ServicesLookupModel();
    ko.applyBindings(l_serviceLookup_model, document.getElementById("div_serviceLookupModal"));
    l_UOMLookup_model = new UOMLookupModel();
    ko.applyBindings(l_UOMLookup_model, document.getElementById("div_UOMLookupModal"));
    l_POLookup_model = new POLookupModel();
    ko.applyBindings(l_POLookup_model, document.getElementById("div_POsLookupModal"));

    l_contractLines_model = new ContractLinesModel();

    l_contract_accounts_model = new ContractAccountsInfoModel();
    l_contract_tags_info_model = new ContractTagsInfoModel();
    l_contract_PONumbers_model = new ContractPONumbersModel();

    l_contractSummary_model.contractID(getUrlParameterValue("instanceId", null, true).split(".")[1]);
    l_contractSummary_model.layoutConfigID(getUrlParameterValue("configID", null, true));
    checkForEditability(() => {
        l_contractSummary_model.fetchContractAttrDetails(() => { });
    });


    createToastDiv();
    var styleAttr = document.getElementById("successToast").getAttribute("style");
    document.getElementById("successToast").setAttribute("style", styleAttr + ";z-index:5999");


});