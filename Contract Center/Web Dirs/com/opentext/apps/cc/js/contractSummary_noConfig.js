$.cordys.json.defaults.removeNamespacePrefix = true;
var l_noConfigContractSummary_model;
var l_countryLookup_model;
var l_currencyLookup_model;
var l_renewalFlagStatusLookup_model;
var l_terminationReasonLookup_model;
var l_documentOriginationLookup_model;
var l_contract_attributes_model;
var l_custom_attributes_info_model;
var l_contract_accounts_model;
var l_contract_tags_model;
var l_contract_PONumbers_model;

var _userStateEditabilityFlag = false;
var currentTabIndex = 0;
var selectedPOListMap = {};

var contractID = getUrlParameterValue("instanceId", null, true).split(".")[1];
var contractItemID = getUrlParameterValue("instanceId", null, true);
var lifecycle_CurrentState = getUrlParameterValue("contractStatus", null, true);
var isExecutedContract = getUrlParameterValue("isExecutedContract", null, true);
// Constants.
const DATE_FORMAT_DD_MM_YYYY_1 = { label: "DD/MM/YYYY", value: "dd/mm/yy" };
const tabsList =
    [
        { Name: "Contract attributes", label: "Contract attributes", display: true },
        { Name: "Custom attributes", label: "Custom attributes", display: true },
        { Name: "Accounts", label: "Accounts", display: true },
        { Name: "Contract lines", label: "Contract lines", display: true },
        { Name: "PO numbers", label: "PO numbers", display: true },
        { Name: "Tags", label: "Tags", display: true },
        { Name: "Custom tab details", label: "Custom tab details", display: false }
    ];

const ctrProperties =
    [
        "ActionDuration ",
        "AmendmentType ",
        "AutoRenew ",
        "AutoRenewDuration ",
        "BWtemplateID ",
        "CancellationDate ",
        "CancellationComments ",
        "ClientEarlyTermRight ",
        "Comments ",
        "ContractName ",
        "ContractNumber ",
        "ContractType ",
        "ContractValue ",
        "ContractValueUSD ",
        "Country ",
        "CRMOpportunityID ",
        "CRMQuoteID ",
        "Currency ",
        "CurrentEndDate ",
        "CurrentStartDate ",
        "CustomerManagerComments ",
        "DealManager ",
        "Description ",
        "DocumentOrigination ",
        "EarlyTerminationConditions ",
        "EndUser ",
        "GeneratedContractId ",
        "InitialContractTenure ",
        "InitialExpiryDate ",
        "IsExecuted ",
        "ContractDocumentType ",
        "MinStartdate ",
        "NextExpirationDate ",
        "NotificationDuration ",
        "OriginalSalesAccountExecutive ",
        "Perpetual ",
        "PriceProtection ",
        "PriceProtectionDate ",
        "Priority ",
        "ProductGoLiveDate ",
        "Region ",
        "RelatedOrganization ",
        "RenewalComments ",
        "RenewalFlagStatus ",
        "RenewalDiscount ",
        "SAPOrderID ",
        "SignatureDate ",
        "StartDate ",
        "Template ",
        "TemplateType ",
        "TerminationFees ",
        "TerminationNoticePeriod ",
        "TerminationReason ",
        "Validated ",
        "ValidatedOn ",
        "ValidatedBy "
    ];
var contractSummary_Services = function () {
    var self = {};

    self.GetContractDetailsById = function (inreq, callbackfunc) {
        $.cordys.ajax({
            method: "GetContractDetailsById ",
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

var NoConfigContractSummaryModel = function () {
    var self = this;
    self.contractID = ko.observable();
    self.tabs = ko.mapping.fromJS(tabsList);
    self.currentTabIndex = ko.observable(0);

    self.contractAttributes = ko.observable();
    self.customAttributes = ko.observable();
    self.accounts = ko.observable();
    self.contractLines = ko.observable();
    self.tags = ko.observable();
    self.poNumbers = ko.observable();
    self.watchersList = ko.observableArray();
    self.watcherAdded = ko.observable();

    self.startTrackValueChanges = ko.observable(false);
    self.valueChangesExists = ko.observable(false);

    self.showTabData = function (index) {
        if (index != self.currentTabIndex() || index == 0) {
            if (l_contract_accounts_model.isINTDirty() || l_contract_accounts_model.isEXTDirty() || self.valueChangesExists() || l_custom_attributes_info_model.isDirty()) {
                $("#unsavedWarningModal").modal();
            }
            else {
                document.getElementsByClassName('cc-loading-overlay')[0].classList.toggle('is-active');
                self.currentTabIndex(index);
                switch (self.currentTabIndex()) {
                    case 0:
                        l_contract_attributes_model = new ContractAttributesModel();
                        l_noConfigContractSummary_model.startTrackValueChanges(false);
                        l_contract_attributes_model.loadPageContent(function () {
                            checkForEditability();
                            self.contractAttributes(l_contract_attributes_model);
                            self.getContractWatchers();
                            l_noConfigContractSummary_model.startTrackValueChanges(true);
                        });
                        break;
                    case 1:
                        l_custom_attributes_info_model.loadPageContent();
                        self.customAttributes(l_custom_attributes_info_model);
                        break;
                    case 2:
                        l_contract_accounts_model.loadPageContent();
                        self.accounts(l_contract_accounts_model);
                        break;
                    case 3:
                        l_contractLines_model.loadPageContent();
                        self.contractLines(l_contractLines_model);
                        break;
                    case 4:
                        l_contract_PONumbers_model.loadPageContent();
                        self.poNumbers(l_contract_PONumbers_model);
                        break;
                    case 5:
                        l_contract_tags_model.loadPageContent();
                        self.tags(l_contract_tags_model);
                }
                document.getElementsByClassName('cc-loading-overlay')[0].classList.toggle('is-active');
            }
        }
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

    window.parent.document.addEventListener("click", e => {
        if (self.valueChangesExists()) {
            $("#unsavedWarningModal").modal();
            e.stopPropagation();
            e.preventDefault();
        }
    }, true);
};

var ContractAttributesModel = function () {
    var self = this;

    self.ActionDuration = ko.observable().extend({ trackValueChange: true });
    self.AmendmentType = ko.observable().extend({ trackValueChange: true });
    self.AmendmentTypeID = ko.observable();
    self.AmendmentTypeItemID = ko.observable();
    self.AutoRenew = ko.observable().extend({ trackValueChange: true });
    self.AutoRenewDuration = ko.observable().extend({ trackValueChange: true });
    self.BWtemplateID = ko.observable().extend({ trackValueChange: true });
    self.CancellationDate = ko.observable().extend({ trackValueChange: true });
    self.CancellationComments = ko.observable().extend({ trackValueChange: true });
    self.ClientEarlyTermRight = ko.observable().extend({ trackValueChange: true });
    self.Comments = ko.observable().extend({ trackValueChange: true });
    self.ContractName = ko.observable().extend({ trackValueChange: true });
    self.ContractNumber = ko.observable().extend({ trackValueChange: true });
    self.ContractType = ko.observable().extend({ trackValueChange: true });
    self.ContractValue = ko.observable().extend({ trackValueChange: true });
    self.ContractValueUSD = ko.observable().extend({ trackValueChange: true });
    self.Country = ko.observable().extend({ trackValueChange: true });
    self.CountryID = ko.observable();
    self.CountryItemID = ko.observable();
    self.CRMOpportunityID = ko.observable().extend({ trackValueChange: true });
    self.CRMQuoteID = ko.observable().extend({ trackValueChange: true });
    self.Currency = ko.observable().extend({ trackValueChange: true });
    self.CurrencyID = ko.observable();
    self.CurrencyItemID = ko.observable();
    self.CurrentEndDate = ko.observable().extend({ trackValueChange: true });
    self.CurrentStartDate = ko.observable().extend({ trackValueChange: true });
    self.CustomerManagerComments = ko.observable().extend({ trackValueChange: true });
    self.DealManager = ko.observable().extend({ trackValueChange: true });
    self.DefaultDocument = ko.observable().extend({ trackValueChange: true });
    self.DefaultDocumentID = ko.observable();
    self.DefaultDocumentItemID = ko.observable();
    self.Description = ko.observable().extend({ trackValueChange: true });
    self.DocumentOrigination = ko.observable().extend({ trackValueChange: true });
    self.DocumentOriginationID = ko.observable();
    self.DocumentOriginationItemID = ko.observable();
    self.EarlyTerminationConditions = ko.observable().extend({ trackValueChange: true });
    self.EndUser = ko.observable().extend({ trackValueChange: true });
    self.GeneratedContractId = ko.observable().extend({ trackValueChange: true });
    self.InitialContractTenure = ko.observable().extend({ trackValueChange: true });
    self.InitialExpiryDate = ko.observable().extend({ trackValueChange: true });
    self.IsExecuted = ko.observable().extend({ trackValueChange: true });
    self.ContractDocumentType = ko.observable().extend({ trackValueChange: true });
    self.MinStartdate = ko.observable().extend({ trackValueChange: true });
    self.NextExpirationDate = ko.observable().extend({ trackValueChange: true });
    self.NotificationDuration = ko.observable().extend({ trackValueChange: true });
    self.OriginalSalesAccountExecutive = ko.observable().extend({ trackValueChange: true });
    self.Perpetual = ko.observable().extend({ trackValueChange: true });
    self.PriceProtection = ko.observable().extend({ trackValueChange: true });
    self.PriceProtectionDate = ko.observable("").extend({ trackValueChange: true });
    self.Priority = ko.observable().extend({ trackValueChange: true });
    self.ProductGoLiveDate = ko.observable().extend({ trackValueChange: true });
    self.Region = ko.observable().extend({ trackValueChange: true });
    self.RelatedOrganization = ko.observable().extend({ trackValueChange: true });
    self.RenewalComments = ko.observable().extend({ trackValueChange: true });
    self.RenewalFlagStatus = ko.observable().extend({ trackValueChange: true });
    self.RenewalFlagStatusID = ko.observable();
    self.RenewalFlagStatusItemID = ko.observable();
    self.RenewalDiscount = ko.observable().extend({ trackValueChange: true });
    self.SAPOrderID = ko.observable().extend({ trackValueChange: true });
    self.SignatureDate = ko.observable().extend({ trackValueChange: true });
    self.StartDate = ko.observable().extend({ trackValueChange: true });
    self.Template = ko.observable().extend({ trackValueChange: true });
    self.TemplateType = ko.observable().extend({ trackValueChange: true });
    self.TerminationFees = ko.observable().extend({ trackValueChange: true });
    self.TerminationNoticePeriod = ko.observable().extend({ trackValueChange: true });
    self.TerminationReason = ko.observable().extend({ trackValueChange: true });
    self.TerminationReasonID = ko.observable();
    self.TerminationReasonItemID = ko.observable();
    self.Validated = ko.observable().extend({ trackValueChange: true });
    self.ValidatedOn = ko.observable().extend({ trackValueChange: true });
    self.ValidatedBy = ko.observable().extend({ trackValueChange: true });


    self.isEditable = ko.observable(false);

    self.updatePerpectual = function (value, iItem, event) {
        if (!(isExecutedContract && isExecutedContract == "true" && (lifecycle_CurrentState == "Pending Activation" || lifecycle_CurrentState == "Active"))) {
            self.Perpetual(value);

            if (value == 'true') {
                l_contract_attributes_model.InitialContractTenure("");
                l_contract_attributes_model.AutoRenew("false");
                l_contract_attributes_model.AutoRenewDuration("");
                $('#ContractTermPreviewModel').css("display", "none");
                $('#AutoRenewDurPreviewModel').css("display", "none");
            }
        }
        event.stopPropagation();
    }

    self.updateAutoRenew = function (value, iItem, event) {
        if (!(isExecutedContract && isExecutedContract == "true" && (lifecycle_CurrentState == "Pending Activation" || lifecycle_CurrentState == "Active")) && l_contract_attributes_model.Perpetual() == "false") {
            self.AutoRenew(value);
            if (value == 'false')
                l_contract_attributes_model.AutoRenewDuration("")
            $('#AutoRenewDurPreviewModel').css("display", "none");

        }
        event.stopPropagation();
    }

    self.updatePriceProtection = function (value, iItem, event) {
        self.PriceProtection(value);
        if (value == "false") {
            self.PriceProtectionDate(undefined);
            $("#input_PriceProtectionDate").val("");
        }
        event.stopPropagation();
    }

    self.updateClientEarlyTermRight = function (value, iItem, event) {
        self.ClientEarlyTermRight(value);
        event.stopPropagation();
    }

    self.loadPageContent = function (callBackFun) {
        self.isEditable(_userStateEditabilityFlag);
        self.fetchContractAttrDetails(callBackFun);
    }

    self.fetchContractAttrDetails = function (callBackFun) {
        contractSummary_Services.GetContractDetailsById(contractID, function (data, status) {
            if (status === "SUCCESS") {
                _populateContractAttrData(data.Contract);
                if (callBackFun) {
                    callBackFun();
                }
            }
        });
    }

    function _populateContractAttrData(iContract) {
        self.ActionDuration(getTextValue(iContract.ActionDuration) ? formateNumbertoLocale(iContract.ActionDuration) : '');
        if (iContract.AmendType) {
            self.AmendmentType(getTextValue(iContract.AmendType.Name));
        }
        self.AutoRenew(getTextValue(iContract.AutoRenew));
        var l_autoRenewDuration = getTextValue(iContract.AutoRenewDuration);
        if (l_autoRenewDuration.lastIndexOf("M") > 0 && l_autoRenewDuration.lastIndexOf("D") > 0 && getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("M"))) > 0 && getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("M") + 1, l_autoRenewDuration.lastIndexOf("D"))) > 0) {
            self.AutoRenewDuration(getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("M"))) + " month(s), " + getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("M") + 1, l_autoRenewDuration.lastIndexOf("D"))) + " day(s)");
        }
        else if (l_autoRenewDuration.lastIndexOf("M") > 0 && getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("M"))) > 0) {
            self.AutoRenewDuration(getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("M"))) + " month(s)");
        }
        else if (l_autoRenewDuration.lastIndexOf("D") > 0 && getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("D"))) > 0) {
            self.AutoRenewDuration(getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("D"))) + " day(s)");
        } else {
            self.AutoRenewDuration("");
        }
        self.BWtemplateID(getTextValue(iContract.BWtemplateID));
        self.CancellationDate(getTextValue(iContract.CancellationDate).split("Z")[0]);
        self.CancellationComments(getTextValue(iContract.CancellationComments));
        self.ClientEarlyTermRight(getTextValue(iContract.ClientEarlyTermRight));
        self.Comments(getTextValue(iContract.Comments));
        self.ContractName(getTextValue(iContract.ContractName));
        self.ContractNumber(getTextValue(iContract.ContractNumber));
        if (iContract.ContractType) {
            self.ContractType(getTextValue(iContract.ContractType.Name));
        }
        self.ContractValue(getTextValue(iContract.ContractValue));
        self.ContractValueUSD(getTextValue(iContract.ContractValueUSD));
        if (iContract.Country) {
            self.Country(getTextValue(iContract.Country.LinkedCountry.Country_Name));
        }
        self.CRMOpportunityID(getTextValue(iContract.CRMOpportunityID));
        self.CRMQuoteID(getTextValue(iContract.CRMQuoteID));
        if (iContract.Currency) {
            self.Currency(getTextValue(iContract.Currency.Name));
        }
        self.CurrentEndDate(getTextValue(iContract.CurrentEndDate).split("Z")[0]);
        self.CurrentStartDate(getTextValue(iContract.CurrentStartDate).split("Z")[0]);
        self.CustomerManagerComments(getTextValue(iContract.CustomerManagerComments));
        self.DealManager(getTextValue(iContract.DealManager));
        if (iContract.DefaultDocRelation)
            self.DefaultDocument(getTextValue(iContract.DefaultDocRelation.File.FileName));
        self.Description(getTextValue(iContract.Description));
        if (iContract.DocumentOrigination) {
            self.DocumentOrigination(getTextValue(iContract.DocumentOrigination.Name));
        }
        self.EarlyTerminationConditions(getTextValue(iContract.EarlyTerminationConditions));
        self.EndUser(getTextValue(iContract.EndUser));
        self.GeneratedContractId(getTextValue(iContract.GeneratedContractId));
        var l_contractTermDuration = getTextValue(iContract.InitialContractTenure);
        if (l_contractTermDuration.lastIndexOf("M") > 0 && l_contractTermDuration.lastIndexOf("D") > 0 && getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("M"))) > 0 && getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("M") + 1, l_contractTermDuration.lastIndexOf("D"))) > 0) {
            self.InitialContractTenure(getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("M"))) + " month(s), " + getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("M") + 1, l_contractTermDuration.lastIndexOf("D"))) + " day(s)");
        }
        else if (l_contractTermDuration.lastIndexOf("M") > 0 && getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("M"))) > 0) {
            self.InitialContractTenure(getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("M"))) + " month(s)");
        }
        else if (l_contractTermDuration.lastIndexOf("D") > 0 && getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("D"))) > 0) {
            self.InitialContractTenure(getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("D"))) + " day(s)");
        } else {
            self.InitialContractTenure("");
        }

        self.InitialExpiryDate(getTextValue(iContract.InitialExpiryDate).split("Z")[0]);
        self.IsExecuted(getTextValue(iContract.IsExecuted));
        self.ContractDocumentType(iContract.ContractDocumentType == 'EXTERNALDOCUMENT' ? 'true' : 'false');
        self.MinStartdate(getTextValue(iContract.MinStartdate).split("Z")[0]);
        self.NextExpirationDate(getTextValue(iContract.NextExpirationDate).split("Z")[0]);
        self.NotificationDuration(getTextValue(iContract.NotificationDuration) ? formateNumbertoLocale(iContract.NotificationDuration) : '');
        self.OriginalSalesAccountExecutive(getTextValue(iContract.OriginalSalesAccountExecutive));
        self.Perpetual(getTextValue(iContract.Perpetual));
        self.PriceProtection(getTextValue(iContract.PriceProtection == '1') ? 'true' : getTextValue(iContract.PriceProtection == '0') ? 'false' : "");
        self.PriceProtectionDate(getTextValue(iContract.PriceProtectionDate).split("Z")[0]);
        self.Priority(getTextValue(iContract.Priority));
        self.ProductGoLiveDate(getTextValue(iContract.ProductGoLiveDate).split("Z")[0]);
        if (iContract.Country) {
            self.Region(getTextValue(iContract.Country.Owner.Name));
        }
        if (iContract.RelatedOrganization) {
            self.RelatedOrganization(getTextValue(iContract.RelatedOrganization.Name));
        }
        self.RenewalComments(getTextValue(iContract.RenewalComments));
        if (iContract.RenewalFlagStatus) {
            self.RenewalFlagStatus(getTextValue(iContract.RenewalFlagStatus.Name));
        }
        self.RenewalDiscount(getTextValue(iContract.RenewalDiscount));
        self.SAPOrderID(getTextValue(iContract.SAPOrderID));
        self.SignatureDate(getTextValue(iContract.SignatureDate).split("Z")[0]);
        self.StartDate(getTextValue(iContract.StartDate).split("Z")[0]);
        if (iContract.RelatedTemplate) {
            self.Template(getTextValue(iContract.RelatedTemplate.Name));
            self.TemplateType(getTextValue(iContract.RelatedTemplate.TemplateType));
        }
        self.TerminationFees(getTextValue(iContract.TerminationFees));
        self.TerminationNoticePeriod(getTextValue(iContract.TerminationNoticePeriod));
        if (iContract.TerminationReason) {
            self.TerminationReason(getTextValue(iContract.TerminationReason.Reason));
        }
        self.Validated(getTextValue(iContract.Validated));
        self.ValidatedOn(getTextValue(iContract.ValidatedOn).split("Z")[0]);
        self.ValidatedBy(getTextValue(iContract.ValidatedBy));
        l_noConfigContractSummary_model.watcherAdded(getTextValue(iContract.watcheradded))

        //l_noConfigContractSummary_model.startTrackValueChanges(true);
    }



};

//code for lookups ----start----
function openCountrySelModal() {
    $('#div_countryLookupModal').modal({
        backdrop: 'static',
        keyboard: false
    })
    ListAllCountries();
    $('button#btn_selectCountryYes').off("click");
    $('button#btn_selectCountryYes').on('click', function (_event) {
        l_contract_attributes_model.Country(l_countryLookup_model.selectedCountryName())
        l_contract_attributes_model.CountryID(l_countryLookup_model.selectedCountryID())
        l_contract_attributes_model.CountryItemID(l_countryLookup_model.selectedCountryItemID())
        l_contract_attributes_model.Region(l_countryLookup_model.selectedRegionName())
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
        l_contract_attributes_model.Currency(l_currencyLookup_model.selectedCurrencyName())
        l_contract_attributes_model.CurrencyID(l_currencyLookup_model.selectedCurrencyID())
        l_contract_attributes_model.CurrencyItemID(l_currencyLookup_model.selectedCurrencyItemID())
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

function openRenewalFlagSelModal(attrData) {
    $('#div_renewalFlagLookupModal').modal({
        backdrop: 'static',
        keyboard: false
    })
    ListAllRenewalFlags();
    $('button#btn_selectRenewalFlagYes').off("click");
    $('button#btn_selectRenewalFlagYes').on('click', function (_event) {
        l_contract_attributes_model.RenewalFlagStatus(l_renewalFlagStatusLookup_model.selectedRenewalFlagName())
        l_contract_attributes_model.RenewalFlagStatusID(l_renewalFlagStatusLookup_model.selectedRenewalFlagID())
        l_contract_attributes_model.RenewalFlagStatusItemID(l_renewalFlagStatusLookup_model.selectedRenewalFlagItemID())
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
        l_contract_attributes_model.TerminationReason(l_terminationReasonLookup_model.selectedTerminationReasonName())
        l_contract_attributes_model.TerminationReasonID(l_terminationReasonLookup_model.selectedTerminationReasonID())
        l_contract_attributes_model.TerminationReasonItemID(l_terminationReasonLookup_model.selectedTerminationReasonItemID())
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
        l_contract_attributes_model.DocumentOrigination(l_documentOriginationLookup_model.selectedDocOriginName())
        l_contract_attributes_model.DocumentOriginationID(l_documentOriginationLookup_model.selectedDocOriginID())
        l_contract_attributes_model.DocumentOriginationItemID(l_documentOriginationLookup_model.selectedDocOriginItemID())
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

function openAmendTypeSelModal(attrData) {
    $('#div_amendTypeLookupModal').modal({
        backdrop: 'static',
        keyboard: false
    })
    ListAllAmendTypes();
    $('button#btn_selectAmendTypeYes').off("click");
    $('button#btn_selectAmendTypeYes').on('click', function (_event) {
        l_contract_attributes_model.AmendmentType(l_amendTypeLookup_model.selectedAmendTypeName())
        l_contract_attributes_model.AmendmentTypeID(l_amendTypeLookup_model.selectedAmendTypeID())
        l_contract_attributes_model.AmendmentTypeItemID(l_amendTypeLookup_model.selectedAmendTypeItemID())
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

function openDefaultDocSelModal(attrData) {
    $('#div_defaultDocLookupModal').modal({
        backdrop: 'static',
        keyboard: false
    })
    ListAllDocuments();
    $('button#btn_selectDefaultDocYes').off("click");
    $('button#btn_selectDefaultDocYes').on('click', function (_event) {
        l_contract_attributes_model.DefaultDocument(l_defaultDocLookup_model.selectedDocName())
        l_contract_attributes_model.DefaultDocumentID(l_defaultDocLookup_model.selectedDocID())
        l_contract_attributes_model.DefaultDocumentItemID(l_defaultDocLookup_model.selectedDocItemID())
    });
}

function ListAllDocuments() {
    $.cordys.ajax({
        namespace: "http://schemas/OpenTextContractCenter/Contract.Contents/operations",
        method: "GetDocswithfilters",
        parameters: {
            "fileName": $("#input_defaultDocSearchFilter").val(),
            "contractID": contractID,
        },
        success: function (data) {
            addDataToDefaultDocLookup(data.Contents, l_defaultDocLookup_model);
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving contract documents. Contact your administrator."), 10000);
            return false;
        }
    });
}

function addDataToDefaultDocLookup(iElementList, iModel) {
    iModel.DocumentsList.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            iElementList.forEach(function (iElement) {
                iModel.DocumentsList.push(iElement);
            });
        }
        else {
            iModel.DocumentsList.push(iElementList);
        }
    }
}

var DefaultDocLookupModel = function () {
    var self = this;
    self.DocumentsList = ko.observableArray([]);
    self.selectedDocName = ko.observable('');
    self.selectedDocID = ko.observable('');
    self.selectedDocItemID = ko.observable('');
    self.onSelectDocRow = function (iItem, event) {
        $(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
        self.selectedDocName(getTextValue(iItem.File.FileName));
        self.selectedDocID(getTextValue(iItem['Contents-id'].Id1));
        self.selectedDocItemID(getTextValue(iItem['Contents-id'].ItemId1));
    }
    self.onSelectDocRadioButton = function (iItem, event) {
        $(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).addClass("cc-radio-on");
        self.selectedDocName(getTextValue(iItem.File.FileName));
        self.selectedDocID(getTextValue(iItem['Contents-id'].Id1));
        self.selectedDocItemID(getTextValue(iItem['Contents-id'].ItemId1));
        event.stopPropagation();
    }
};

function openContractTermModal(attrData) {
    if ($('#ContractTermPreviewModel').css("display") == "none" && l_contract_attributes_model.Perpetual() == "false" && _userStateEditabilityFlag && !(isExecutedContract && isExecutedContract == "true" && (lifecycle_CurrentState == "Pending Activation" || lifecycle_CurrentState == "Active")))
        $('#ContractTermPreviewModel').css("display", "block");
    else
        $('#ContractTermPreviewModel').css("display", "none");

    addDataToContractTermLookup();
    $('button#btn_contractTermYes').off("click");
    $('button#btn_contractTermYes').on('click', function (_event) {
        if (!$("#input_contractTermMonths").val().match(/^0*$/) && !$("#input_contractTermDays").val().match(/^0*$/)) {
            l_contract_attributes_model.InitialContractTenure($("#input_contractTermMonths").val() + " month(s), " + $("#input_contractTermDays").val() + " day(s)");
        }
        else if (!$("#input_contractTermMonths").val().match(/^0*$/)) {
            l_contract_attributes_model.InitialContractTenure($("#input_contractTermMonths").val() + " month(s)");
        }
        else if (!$("#input_contractTermDays").val().match(/^0*$/)) {
            l_contract_attributes_model.InitialContractTenure($("#input_contractTermDays").val() + " day(s)");
        }
        else {
            l_contract_attributes_model.InitialContractTenure("");
        }
        $('#ContractTermPreviewModel').css("display", "none");
    });
}

function closeContractTermModal(attrData) {
    $('#ContractTermPreviewModel').css("display", "none");
}

function addDataToContractTermLookup() {
    var months = "";
    var days = "";
    if (l_contract_attributes_model.InitialContractTenure() && l_contract_attributes_model.InitialContractTenure().lastIndexOf("month(s)") > 0) {
        months = l_contract_attributes_model.InitialContractTenure().substring(0, l_contract_attributes_model.InitialContractTenure().lastIndexOf(" month(s)"))
    }
    if (l_contract_attributes_model.InitialContractTenure() && l_contract_attributes_model.InitialContractTenure().lastIndexOf("day(s)") > 0) {
        if (months != "") {
            days = l_contract_attributes_model.InitialContractTenure().substring(l_contract_attributes_model.InitialContractTenure().lastIndexOf("month(s), ") + "month(s), ".length, l_contract_attributes_model.InitialContractTenure().lastIndexOf(" day(s)"))
        }
        else {
            days = l_contract_attributes_model.InitialContractTenure().substring(0, l_contract_attributes_model.InitialContractTenure().lastIndexOf(" day(s)"))
        }
    }
    $("#input_contractTermMonths").val(months);
    $("#input_contractTermDays").val(days);
}

function openAutoRenewDurModal(attrData) {
    if ($('#AutoRenewDurPreviewModel').css("display") == "none" && l_contract_attributes_model.AutoRenew() == "true" && _userStateEditabilityFlag && !(isExecutedContract && isExecutedContract == "true" && (lifecycle_CurrentState == "Pending Activation" || lifecycle_CurrentState == "Active")))
        $('#AutoRenewDurPreviewModel').css("display", "block");
    else
        $('#AutoRenewDurPreviewModel').css("display", "none");

    addDataToAutoRenewDurLookup();
    $('button#btn_autoRenewDurYes').off("click");
    $('button#btn_autoRenewDurYes').on('click', function (_event) {
        if (!$("#input_autoRenewDurMonths").val().match(/^0*$/) && !$("#input_autoRenewDurDays").val().match(/^0*$/)) {
            l_contract_attributes_model.AutoRenewDuration($("#input_autoRenewDurMonths").val() + " month(s), " + $("#input_autoRenewDurDays").val() + " day(s)");
        }
        else if (!$("#input_autoRenewDurMonths").val().match(/^0*$/)) {
            l_contract_attributes_model.AutoRenewDuration($("#input_autoRenewDurMonths").val() + " month(s)");
        }
        else if (!$("#input_autoRenewDurDays").val().match(/^0*$/)) {
            l_contract_attributes_model.AutoRenewDuration($("#input_autoRenewDurDays").val() + " day(s)");
        }
        else {
            l_contract_attributes_model.AutoRenewDuration("");
        }
        $('#AutoRenewDurPreviewModel').css("display", "none");
    });
}

function closeAutoRenewDurModal(attrData) {
    $('#AutoRenewDurPreviewModel').css("display", "none");
}

function addDataToAutoRenewDurLookup() {
    var months = "";
    var days = "";
    if (l_contract_attributes_model.AutoRenewDuration() && l_contract_attributes_model.AutoRenewDuration().lastIndexOf("month(s)") > 0) {
        months = l_contract_attributes_model.AutoRenewDuration().substring(0, l_contract_attributes_model.AutoRenewDuration().lastIndexOf(" month(s)"))
    }
    if (l_contract_attributes_model.AutoRenewDuration() && l_contract_attributes_model.AutoRenewDuration().lastIndexOf("day(s)") > 0) {
        if (months != "") {
            days = l_contract_attributes_model.AutoRenewDuration().substring(l_contract_attributes_model.AutoRenewDuration().lastIndexOf("month(s), ") + "month(s), ".length, l_contract_attributes_model.AutoRenewDuration().lastIndexOf(" day(s)"))
        }
        else {
            days = l_contract_attributes_model.AutoRenewDuration().substring(0, l_contract_attributes_model.AutoRenewDuration().lastIndexOf(" day(s)"))
        }
    }
    $("#input_autoRenewDurMonths").val(months);
    $("#input_autoRenewDurDays").val(days);
}

//code for lookups ----end----

//Code for clears ---start----
function clearCountrySelection() {
	if(l_contract_attributes_model.CountryItemID()!=""){
		l_contract_attributes_model.Country('');
        l_contract_attributes_model.CountryID('');
        l_contract_attributes_model.CountryItemID('');
        l_contract_attributes_model.Region('');
	}
}

function clearRenewalFlagSelection() {
	if(l_contract_attributes_model.RenewalFlagStatusItemID()!=""){
		l_contract_attributes_model.RenewalFlagStatus('');
        l_contract_attributes_model.RenewalFlagStatusID('');
        l_contract_attributes_model.RenewalFlagStatusItemID('');
	}
}

function clearTermiReasonSelection() {
	if(l_contract_attributes_model.TerminationReasonItemID()!=""){
		l_contract_attributes_model.TerminationReason('');
        l_contract_attributes_model.TerminationReasonID('');
        l_contract_attributes_model.TerminationReasonItemID('');
	}
}

function clearAmendTypeSelection() {
	if(l_contract_attributes_model.AmendmentTypeItemID()!=""){
		l_contract_attributes_model.AmendmentType('');
        l_contract_attributes_model.AmendmentTypeID('');
        l_contract_attributes_model.AmendmentTypeItemID('');
	}
}

function clearDocOrigSelection() {
	if(l_contract_attributes_model.DocumentOriginationItemID()!=""){
		l_contract_attributes_model.DocumentOrigination('');
        l_contract_attributes_model.DocumentOriginationID('');
        l_contract_attributes_model.DocumentOriginationItemID('');
	}
}

function clearDefaultDocSelection() {
	if(l_contract_attributes_model.DefaultDocumentItemID()!=""){
		l_contract_attributes_model.DefaultDocument('');
        l_contract_attributes_model.DefaultDocumentID('');
        l_contract_attributes_model.DefaultDocumentItemID('');
	}
}

function clearSignatureDate() {
	if(l_contract_attributes_model.SignatureDate()!=""){
		l_contract_attributes_model.SignatureDate('');
		$("#input_SignatureDate").val("");
	}
}

function clearProductGoLiveDate() {
	if(l_contract_attributes_model.ProductGoLiveDate()!=""){
		l_contract_attributes_model.ProductGoLiveDate('');
		$("#input_ProductGoLiveDate").val("");
	}
}

function clearCustAttrDate(attrData, event) {
	if(attrData.value()!=""){
		var parentElement = event.currentTarget.parentElement;
		var inputElement = parentElement.previousElementSibling;
		inputElement.value="";
		 attrData.value('');
	}
}

//code for clears ---end---

function ContractAccountsModel() {
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
    }

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

function ContractTagsModel() {
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
    self.PONumber = ko.observable('');
    self.Description = ko.observable('');
    self.PONumberID = ko.observable('');
    self.PONumberItemID = ko.observable('');
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
                l_contract_tags_model.ContractTagsSelectedList.removeAll();
                var iElementList = data.GCTag;
                if (iElementList) {
                    if (iElementList.length) {
                        iElementList.forEach(function (iElement) {
                            var l_TagObj = {};
                            l_TagObj.id = iElement['GCTag-id'].Id;
                            l_TagObj.Name = getTextValue(iElement.TagName);
                            l_contract_tags_model.ContractTagsSelectedList.push(l_TagObj);
                        });
                    }
                    else {
                        var l_TagObj = {};
                        l_TagObj.id = iElementList['GCTag-id'].Id;
                        l_TagObj.Name = getTextValue(iElementList.TagName);
                        l_contract_tags_model.ContractTagsSelectedList.push(l_TagObj);
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
                addDataToTagsDropDown(data.GCTag, l_contract_tags_model);
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
                var foundElement = existsInArray(l_contract_tags_model.ContractTagsSelectedList(), l_TagObj.id);
                if (!foundElement) {
                    iModel.ContractTagsList.push(l_TagObj);
                }
            }
        }
        else {
            var l_TagObj = {};
            l_TagObj.id = iElementList['GCTag-id'].Id;
            l_TagObj.Name = getTextValue(iElementList.TagName);
            var foundElement = existsInArray(l_contract_tags_model.ContractTagsSelectedList(), l_TagObj.id);
            if (!foundElement) {
                iModel.ContractTagsList.push(l_TagObj);
            }
        }
    }
    l_contract_tags_model.isLoaded(true);
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
            if (l_noConfigContractSummary_model.startTrackValueChanges()) {
                target.hasValueChanged(newValue != target.originalValue);
                if (target.hasValueChanged()) {
                    l_noConfigContractSummary_model.valueChangesExists(true);
                }
            }
        });
    }
    return target;
};

function saveCTRAttributesData() {
    var ctrPropTags = {};
    if (checkforMandatoryProps()) {
        /*for (var i = 0; i < ctrProperties.length; i++) {
            if(eval('l_contract_attributes_model.'+ctrProperties[i]+'.hasValueChanged()')){
                ctrPropTags[ctrProperties[i]] = eval('l_contract_attributes_model.'+ctrProperties[i]+'()');
            }
        }*/
        if (l_contract_attributes_model.ActionDuration.hasValueChanged()) {
            if (parseInt(l_contract_attributes_model.ActionDuration()) > parseInt(l_contract_attributes_model.NotificationDuration())) {
                showOrHideErrorInfo("div_ErrorInfoArea", true, getTranslationMessage("'Notify before expiration (days)' must be greater than 'Act before expiration (days)'."), 5000);
                return;
            } else {
                ctrPropTags['ActionDuration'] = l_contract_attributes_model.ActionDuration();
            }
        } if (l_contract_attributes_model.AmendmentType.hasValueChanged()) {
            ctrPropTags['AmendType'] = getInstanceID(l_contract_attributes_model.AmendmentTypeItemID(), "ItemId");
        } if (l_contract_attributes_model.AutoRenew.hasValueChanged()) {
            ctrPropTags['AutoRenew'] = l_contract_attributes_model.AutoRenew() != "" ? l_contract_attributes_model.AutoRenew() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.AutoRenewDuration.hasValueChanged()) {
            if (l_contract_attributes_model.AutoRenewDuration() != undefined && l_contract_attributes_model.AutoRenewDuration() != "") {
                var months = "";
                var days = "";
                if (l_contract_attributes_model.AutoRenewDuration().lastIndexOf("month(s)") > 0) {
                    months = l_contract_attributes_model.AutoRenewDuration().substring(0, l_contract_attributes_model.AutoRenewDuration().lastIndexOf(" month(s)"))
                }
                if (l_contract_attributes_model.AutoRenewDuration().lastIndexOf("day(s)") > 0) {
                    if (months != "") {
                        days = l_contract_attributes_model.AutoRenewDuration().substring(l_contract_attributes_model.AutoRenewDuration().lastIndexOf("month(s), ") + "month(s), ".length, l_contract_attributes_model.AutoRenewDuration().lastIndexOf(" day(s)"))
                        ctrPropTags['AutoRenewDuration'] = ("P" + months + "M" + days + "D");
                    }
                    else {
                        days = l_contract_attributes_model.AutoRenewDuration().substring(0, l_contract_attributes_model.AutoRenewDuration().lastIndexOf(" day(s)"))
                        ctrPropTags['AutoRenewDuration'] = ("P" + days + "D");
                    }
                }
                else {
                    ctrPropTags['AutoRenewDuration'] = ("P" + months + "M");
                }
            }
            else {
                ctrPropTags['AutoRenewDuration'] = "";
            }
        } if (l_contract_attributes_model.CancellationDate.hasValueChanged()) {
            ctrPropTags['CancellationDate'] = $.datepicker.formatDate("yy-mm-dd", l_contract_attributes_model.CancellationDate());
        } if (l_contract_attributes_model.CancellationComments.hasValueChanged()) {
            ctrPropTags['CancellationComments'] = l_contract_attributes_model.CancellationComments() != "" ? l_contract_attributes_model.CancellationComments() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.ClientEarlyTermRight.hasValueChanged()) {
            ctrPropTags['ClientEarlyTermRight'] = l_contract_attributes_model.ClientEarlyTermRight() != "" ? l_contract_attributes_model.ClientEarlyTermRight() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.Comments.hasValueChanged()) {
            ctrPropTags['Comments'] = l_contract_attributes_model.Comments() != "" ? l_contract_attributes_model.Comments() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.ContractName.hasValueChanged()) {
            ctrPropTags['ContractName'] = l_contract_attributes_model.ContractName() != "" ? l_contract_attributes_model.ContractName() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.ContractNumber.hasValueChanged()) {
            ctrPropTags['ContractNumber'] = l_contract_attributes_model.ContractNumber() != "" ? l_contract_attributes_model.ContractNumber() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.ContractValue.hasValueChanged()) {
            ctrPropTags['ContractValue'] = l_contract_attributes_model.ContractValue() != "" ? l_contract_attributes_model.ContractValue() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.Country.hasValueChanged()) {
            ctrPropTags['Country'] = getInstanceID(l_contract_attributes_model.CountryItemID(), "ItemId1");
        } if (l_contract_attributes_model.CRMOpportunityID.hasValueChanged()) {
            ctrPropTags['CRMOpportunityID'] = l_contract_attributes_model.CRMOpportunityID() != "" ? l_contract_attributes_model.CRMOpportunityID() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.CRMQuoteID.hasValueChanged()) {
            ctrPropTags['CRMQuoteID'] = l_contract_attributes_model.CRMQuoteID() != "" ? l_contract_attributes_model.CRMQuoteID() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.Currency.hasValueChanged()) {
            ctrPropTags['Currency'] = getInstanceID(l_contract_attributes_model.CurrencyItemID(), "ItemId");
        } if (l_contract_attributes_model.DealManager.hasValueChanged()) {
            ctrPropTags['DealManager'] = l_contract_attributes_model.DealManager() != "" ? l_contract_attributes_model.DealManager() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.DefaultDocument.hasValueChanged()) {
            ctrPropTags['DefaultDocument'] = getInstanceID(l_contract_attributes_model.DefaultDocumentItemID(), "ItemId1");
        } if (l_contract_attributes_model.Description.hasValueChanged()) {
            ctrPropTags['Description'] = l_contract_attributes_model.Description() != "" ? l_contract_attributes_model.Description() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.DocumentOrigination.hasValueChanged()) {
            ctrPropTags['DocumentOrigination'] = getInstanceID(l_contract_attributes_model.DocumentOriginationItemID(), "ItemId");
        } if (l_contract_attributes_model.EarlyTerminationConditions.hasValueChanged()) {
            ctrPropTags['EarlyTerminationConditions'] = l_contract_attributes_model.EarlyTerminationConditions() != "" ? l_contract_attributes_model.EarlyTerminationConditions() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.EndUser.hasValueChanged()) {
            ctrPropTags['EndUser'] = l_contract_attributes_model.EndUser() != "" ? l_contract_attributes_model.EndUser() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.InitialContractTenure.hasValueChanged()) {
            if ((l_contract_attributes_model.InitialContractTenure() != undefined && l_contract_attributes_model.InitialContractTenure() != "")) {
                var months = "";
                var days = "";
                if (l_contract_attributes_model.InitialContractTenure().lastIndexOf("month(s)") > 0) {
                    months = l_contract_attributes_model.InitialContractTenure().substring(0, l_contract_attributes_model.InitialContractTenure().lastIndexOf(" month(s)"))
                }
                if (l_contract_attributes_model.InitialContractTenure().lastIndexOf("day(s)") > 0) {
                    if (months != "") {
                        days = l_contract_attributes_model.InitialContractTenure().substring(l_contract_attributes_model.InitialContractTenure().lastIndexOf("month(s), ") + "month(s), ".length, l_contract_attributes_model.InitialContractTenure().lastIndexOf(" day(s)"))
                        ctrPropTags['InitialContractTenure'] = ("P" + months + "M" + days + "D");
                    }
                    else {
                        days = l_contract_attributes_model.InitialContractTenure().substring(0, l_contract_attributes_model.InitialContractTenure().lastIndexOf(" day(s)"))
                        ctrPropTags['InitialContractTenure'] = ("P" + days + "D");
                    }
                }
                else {
                    ctrPropTags['InitialContractTenure'] = ("P" + months + "M");
                }
            } else {
                ctrPropTags['InitialContractTenure'] = "";
            }
        } if (l_contract_attributes_model.MinStartdate.hasValueChanged()) {
            ctrPropTags['MinStartdate'] = $.datepicker.formatDate("yy-mm-dd", l_contract_attributes_model.MinStartdate());
        } if (l_contract_attributes_model.NotificationDuration.hasValueChanged()) {
            if (parseInt(l_contract_attributes_model.ActionDuration()) > parseInt(l_contract_attributes_model.NotificationDuration())) {
                showOrHideErrorInfo("div_ErrorInfoArea", true, getTranslationMessage("'Notify before expiration (days)' must be greater than 'Act before expiration (days)'."), 5000);
                return;
            } else {
                ctrPropTags['NotificationDuration'] = l_contract_attributes_model.NotificationDuration();
            }
        } if (l_contract_attributes_model.OriginalSalesAccountExecutive.hasValueChanged()) {
            ctrPropTags['OriginalSalesAccountExecutive'] = l_contract_attributes_model.OriginalSalesAccountExecutive() != "" ? l_contract_attributes_model.OriginalSalesAccountExecutive() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.Perpetual.hasValueChanged()) {
            ctrPropTags['Perpetual'] = l_contract_attributes_model.Perpetual() != "" ? l_contract_attributes_model.Perpetual() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.PriceProtection.hasValueChanged()) {
            ctrPropTags['PriceProtection'] = l_contract_attributes_model.PriceProtection() == "true" ? "1" : "0";
        } if (l_contract_attributes_model.PriceProtectionDate.hasValueChanged()) {
            if (l_contract_attributes_model.PriceProtectionDate() != "")
                ctrPropTags['PriceProtectionDate'] = $.datepicker.formatDate("yy-mm-dd", l_contract_attributes_model.PriceProtectionDate());
            else
                ctrPropTags['PriceProtectionDate'] = { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.Priority.hasValueChanged()) {
            ctrPropTags['Priority'] = l_contract_attributes_model.Priority() != "" ? l_contract_attributes_model.Priority() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.ProductGoLiveDate.hasValueChanged()) {
            ctrPropTags['ProductGoLiveDate'] = l_contract_attributes_model.ProductGoLiveDate() != "" ? $.datepicker.formatDate("yy-mm-dd", l_contract_attributes_model.ProductGoLiveDate()) : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.RenewalComments.hasValueChanged()) {
            ctrPropTags['RenewalComments'] = l_contract_attributes_model.RenewalComments() != "" ? l_contract_attributes_model.RenewalComments() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.RenewalFlagStatus.hasValueChanged()) {
            ctrPropTags['RenewalFlagStatus'] = getInstanceID(l_contract_attributes_model.RenewalFlagStatusItemID(), "ItemId");
        } if (l_contract_attributes_model.RenewalDiscount.hasValueChanged()) {
            ctrPropTags['RenewalDiscount'] = l_contract_attributes_model.RenewalDiscount() != "" ? l_contract_attributes_model.RenewalDiscount() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.SAPOrderID.hasValueChanged()) {
            ctrPropTags['SAPOrderID'] = l_contract_attributes_model.SAPOrderID() != "" ? l_contract_attributes_model.SAPOrderID() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.SignatureDate.hasValueChanged()) {
            ctrPropTags['SignatureDate'] = l_contract_attributes_model.SignatureDate() != "" ? $.datepicker.formatDate("yy-mm-dd", l_contract_attributes_model.SignatureDate()) : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.StartDate.hasValueChanged()) {
            ctrPropTags['StartDate'] = $.datepicker.formatDate("yy-mm-dd", l_contract_attributes_model.StartDate());
        } if (l_contract_attributes_model.TerminationFees.hasValueChanged()) {
            ctrPropTags['TerminationFees'] = l_contract_attributes_model.TerminationFees() != "" ? l_contract_attributes_model.TerminationFees() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.TerminationNoticePeriod.hasValueChanged()) {
            ctrPropTags['TerminationNoticePeriod'] = l_contract_attributes_model.TerminationNoticePeriod() != "" ? l_contract_attributes_model.TerminationNoticePeriod() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" };
        } if (l_contract_attributes_model.TerminationReason.hasValueChanged()) {
            ctrPropTags['TerminationReason'] = getInstanceID(l_contract_attributes_model.TerminationReasonItemID(), "ItemId");
        }

        var ctrAttrProps = {};
        ctrAttrProps.contractID = l_noConfigContractSummary_model.contractID();
        ctrAttrProps.ctrAttributes = ctrPropTags;
        contractSummary_Services.UpdateCtrAttrValues(ctrAttrProps, function (data, status) {
            if (status === "SUCCESS") {
                successToast(2000, "Changes saved successfully");
                l_noConfigContractSummary_model.startTrackValueChanges(false);
                l_noConfigContractSummary_model.valueChangesExists(false);
                l_noConfigContractSummary_model.showTabData(0);
                window.top.publicAPIProvider.getItemData(contractItemID, { "refresh": true });
            }
        });
    } else {
        showOrHideErrorInfo("div_ErrorInfoArea", true, getTranslationMessage("Mandatory fields cannot be empty."), 5000);
    }
}

function checkforMandatoryProps(ctrPropstoUpdate) {
    if (l_contract_attributes_model.ContractName() == "" || l_contract_attributes_model.ContractName() == undefined) return false;
    if (l_contract_attributes_model.RelatedOrganization() == "" || l_contract_attributes_model.RelatedOrganization() == undefined) return false;
    if (l_contract_attributes_model.ContractType() == "" || l_contract_attributes_model.ContractType() == undefined) return false;
    if (l_contract_attributes_model.IsExecuted() == "" || l_contract_attributes_model.IsExecuted() == undefined) return false;
    if (l_contract_attributes_model.ContractDocumentType() == "" || l_contract_attributes_model.ContractDocumentType() == undefined) return false;
    if (l_contract_attributes_model.TemplateType() == "" || l_contract_attributes_model.TemplateType() == undefined)
        return false;
    else {
        if (l_contract_attributes_model.TemplateType() != "None" && (l_contract_attributes_model.Template() == "" || l_contract_attributes_model.Template() == undefined)) {
            return false;
        }
    }
    if (l_contract_attributes_model.Perpetual() == "" || l_contract_attributes_model.Perpetual() == undefined)
        return false;
    else {
        if (l_contract_attributes_model.Perpetual() == "false" && (l_contract_attributes_model.InitialContractTenure() == "" || l_contract_attributes_model.InitialContractTenure() == undefined)) {
            return false;
        }
    }

    if (l_contract_attributes_model.AutoRenew() == "true" && (l_contract_attributes_model.AutoRenewDuration() == "" || l_contract_attributes_model.AutoRenewDuration() == undefined)) {
        return false;
    }
    if (l_contract_attributes_model.StartDate() == "" || l_contract_attributes_model.StartDate() == undefined) return false;
    if (l_contract_attributes_model.MinStartdate() == "" || l_contract_attributes_model.MinStartdate() == undefined) return false;
    if (l_contract_attributes_model.Currency() == "" || l_contract_attributes_model.Currency() == undefined) return false;
    if (l_contract_attributes_model.PriceProtection() == "true" && (l_contract_attributes_model.PriceProtectionDate() == "" || l_contract_attributes_model.PriceProtectionDate() == undefined)) {
        return false;
    }
    return true;
}

function cancelCTRAttributesChanges() {
    l_noConfigContractSummary_model.startTrackValueChanges(false);
    l_noConfigContractSummary_model.valueChangesExists(false);
    l_noConfigContractSummary_model.showTabData(0);
}

function checkForEditability() {
    isUserEligibletoEdit(function (userEligibilityFlag) {
        var stateEligibilityFlag = isStateEligibleforEdit();
        if (userEligibilityFlag && stateEligibilityFlag) {
            _userStateEditabilityFlag = true;
        }
        else {
            $("input").each(function (i, obj) {
                $(this).attr("disabled", true);
                if ($(this).next()){
                    $(this).next().css("display", "none");
					$(this).next().next().css("display", "none");
				}
                $(this).parent().css("display", "block");
            });
            $("textarea").each(function (i, obj) {
                $(this).attr("disabled", "true");
            });
            $("select").each(function (i, obj) {
                $(this).attr("disabled", "true");
            });
            $(".cc-select-column").each(function (i, obj) {
                $(this).parent().unbind("click");
            });
            enableSpecificFieldsOnState();
        }
    });
}

function enableSpecificFieldsOnState() {
    if (!(lifecycle_CurrentState === 'Terminated' || lifecycle_CurrentState === 'Expired')) {
        $("#input_TerminationNoticePeriod").removeAttr('disabled');
        $("#input_TerminationFees").removeAttr('disabled');
        $("#input_TerminationReason").removeAttr('disabled');
        $("#TerminationReason_block").css("display", "flex");
        $("#TerminationReason_SearchIcon").show();
        $("#input_EarlyTerminationConditions").removeAttr('disabled');
        $("#input_CancellationComments").removeAttr('disabled');
    }
}

function isUserEligibletoEdit(callbackfunc) {
    $.cordys.ajax(
        {
            method: "CheckCurrentUserInRoles",
            namespace: "http://schemas.opentext.com/apps/cc/configworkflow/20.2",
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
        $("#perpetualYes").unbind("click");
        $("#perpetualNo").unbind("click");
        $("#input_MinStartdate").attr("disabled", "true");
        $("#input_InitialContractTenure").attr("disabled", "true");
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

$(function () {
    var i_locale = getlocale();
    var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
    translateLabels("com/opentext/apps/cc/htm/contractSummary", i_locale);
    loadRTLIfRequired(i_locale, rtl_css);
    if (window.parent.parent) {
        var contractSummary = $('[src*="contractSummary_noConfig.htm"]', window.parent.parent.document);
        if (contractSummary) {
            contractSummary.css('border', 'none');
        }
    }

    l_noConfigContractSummary_model = new NoConfigContractSummaryModel();
    ko.applyBindings(l_noConfigContractSummary_model, document.getElementById("div_contractSummarynoConfig"));
    ko.applyBindings(l_noConfigContractSummary_model, document.getElementById("id_watchersListDialog"));
    l_countryLookup_model = new CountryLookupModel();
    ko.applyBindings(l_countryLookup_model, document.getElementById("div_countryLookupModal"));
    l_currencyLookup_model = new CurrencyLookupModel();
    ko.applyBindings(l_currencyLookup_model, document.getElementById("div_currencyLookupModal"));

    l_renewalFlagStatusLookup_model = new RenewalFlagLookupModel();
    ko.applyBindings(l_renewalFlagStatusLookup_model, document.getElementById("div_renewalFlagLookupModal"));
    l_terminationReasonLookup_model = new TerminationReasonLookupModel();
    ko.applyBindings(l_terminationReasonLookup_model, document.getElementById("div_termiReasonLookupModal"));
    l_documentOriginationLookup_model = new DocumentOriginationLookupModel();
    ko.applyBindings(l_documentOriginationLookup_model, document.getElementById("div_docOriginLookupModal"));
    l_amendTypeLookup_model = new AmendTypeLookupModel();
    ko.applyBindings(l_amendTypeLookup_model, document.getElementById("div_amendTypeLookupModal"));
    l_defaultDocLookup_model = new DefaultDocLookupModel();
    ko.applyBindings(l_defaultDocLookup_model, document.getElementById("div_defaultDocLookupModal"));
    l_contractLineDetails_model = new ContractLineDetailsModel();
    ko.applyBindings(l_contractLineDetails_model, document.getElementById("div_createOrUpdateContractLineModal"));
    l_serviceLookup_model = new ServicesLookupModel();
    ko.applyBindings(l_serviceLookup_model, document.getElementById("div_serviceLookupModal"));
    l_UOMLookup_model = new UOMLookupModel();
    ko.applyBindings(l_UOMLookup_model, document.getElementById("div_UOMLookupModal"));
    l_POLookup_model = new POLookupModel();
    ko.applyBindings(l_POLookup_model, document.getElementById("div_POsLookupModal"));


    l_contract_attributes_model = new ContractAttributesModel();
    l_custom_attributes_info_model = new CustomAttributesListModel();
    l_contract_accounts_model = new ContractAccountsModel();
    l_contractLines_model = new ContractLinesModel();
    l_contract_PONumbers_model = new ContractPONumbersModel();
    l_contract_tags_model = new ContractTagsModel();

    l_noConfigContractSummary_model.contractID(getUrlParameterValue("instanceId", null, true).split(".")[1]);

    createToastDiv();
    var styleAttr = document.getElementById("successToast").getAttribute("style");
    document.getElementById("successToast").setAttribute("style", styleAttr + ";z-index:5999");

    checkForEditability();
    l_noConfigContractSummary_model.showTabData(0);
});

function getInstanceID(ID, tagName) {
    if (ID !== "") {
        return { [tagName]: ID };
    } else {
        return { [tagName]: { '@xsi:nil': 'true', '@xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance' } };
    }
}