$.cordys.json.defaults.removeNamespacePrefix = true;
var l_contractItemID = "";
var l_OKBtnElement = "";
var l_amendContractDetailsModel;
var BASEURL;
var newContractId;
var i_locale;

var AmendContractDetailsModel = function () {

    var self = this;
    self.l_formAction = getUrlParameterValue("formAction", null, true);
    self.contractTerm = ko.observable('');
    self.isPerpetual = ko.observable('false');
    self.actualStartDate = ko.observable('');
    self.actualStartDatetoLocale = ko.observable('');
    self.currentEndDate = ko.observable('');
    self.autoRenewal = ko.observable('');
    self.autoRenewDuration = ko.observable('');
    self.AmendTypestoAdd = ko.observableArray([]);
    self.selectedAmendTypeID = ko.observable('');
    self.cloneDocument = ko.observable('false');
    self.createWSShortcuts = ko.observable('false')
    self.clickable = true;

    self.amendedContractItemId = ko.observable('');
    self.amendedContractId = ko.observable('');

    self.autoRenewal.subscribe(function (currVal) {
        if (currVal == 'false') {
            self.autoRenewDuration('');
            $('#input_autoRenewDuration').attr('readonly', true);
            $('#input_autoRenewDuration').attr("style", $('#input_autoRenewDuration').attr("style") + "background-color : #EEEEEE !important; cursor : not-allowed");
        }
        else {
            $('#input_autoRenewDuration').attr('readonly', false);
            $("#input_autoRenewDuration").css('cursor', '');
            $('#input_autoRenewDuration').css('background-color', '');
        }
        $('#input_autoRenewDuration').removeClass("cc-error");
    });

    self.isPerpetual.subscribe(function (currVal) {
        if (currVal == 'true') {
            self.contractTerm('');
            self.autoRenewDuration('');
            self.autoRenewal('false');
            $('#input_contractTerm').attr('readonly', true);
            $('#input_contractTerm').attr("style", $('#input_contractTerm').attr("style") + "background-color : #EEEEEE !important; cursor : not-allowed");
        }
        else {
            $('#input_contractTerm').attr('readonly', false);
            $("#input_contractTerm").css('cursor', '');
            $('#input_contractTerm').css('background-color', '');
        }
        $('#input_contractTerm').removeClass("cc-error");
    });

    self.onAutoRenewRadioButtonChanged = function (iItem, event) {
        if ($(event.currentTarget.firstElementChild).hasClass("cc-radio-off")) {
            if (event.currentTarget.parentElement.id == "div_autoRenewInputs") {
                self.autoRenewal(self.autoRenewal() == 'true' ? 'false' : 'true');
                if (self.autoRenewal() == 'false')
                    closeAutoRenewDurModal();
            }
        }
        event.stopPropagation();
    }

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

    self.onCreateWSShortcutsCheckboxChanged = function (iItem, event) {
        if ($(event.currentTarget).hasClass("cc-checkbox-on")) {
            self.createWSShortcuts("false");
        }
        else if ($(event.currentTarget).hasClass("cc-checkbox-off")) {
            self.createWSShortcuts("true");

        }
        event.stopPropagation();
    }

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

$(document).ready(function () {

    i_locale = getlocale();
    var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
    translateLabels("com/opentext/apps/contractcenter/ContractCenter/ContractCenter", i_locale, true);
    loadRTLIfRequired(i_locale, rtl_css);

    var g_parentWindow = window.parent;

    platformDialogModifications((getUrlParameterValue("formAction", null, true) != "RwA") ? "Amend" : "Amend and renew", amendWithUpdates);

    var cInstanceId = getUrlParameterValue("instanceId", null, true);
    l_contractItemID = cInstanceId;
    BASEURL = getUrlParameterValue("baseURL", null, true);

    l_amendContractDetailsModel = new AmendContractDetailsModel(cInstanceId);
    ko.applyBindings(l_amendContractDetailsModel, document.getElementById("div_amendContractForm"));

    filldefaultContractData(cInstanceId);
    listAmendTypes();
    /*
        $(window).on("unload", function (e) {
            g_parentWindow.location.reload();
        });
    */
    setTimeout(function () {
        l_amendContractDetailsModel.bindJqueryFunc();
    }, 0);
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

function modifyFormAfterAmend(i_newButtonName, generatedContractId) {

    $('ai-dialog-header', window.parent.document).prepend("<div id=\"successToast\" style=\"font-size: 14px;color: #211e1eeb;border-bottom: 3px solid green;background-color: white;text-align: center;transition: visibility 0s 0.5s, opacity 0.5s linear;position:absolute;border-radius:3px;padding:16px;left:38%\"></div> <div id=\"errorToast\"> <div id=\"toastHeading\"> <div id=\"headingContent\"></div> <span style=\"position:absolute;cursor:pointer;right:1em;top:1.3em\" id=\"arrowBtn\" class=\"down\"></span> </div> <div id=\"toastContent\" style=\"display: none;clear:both;\"> <div class=\"horizontal-rule\"></div> <div id=\"contentText\" style=\"margin-top: 6px;padding-left: 6px\" align=\"left\">" +
        getTranslationMessage("") + "</div></div></div>");

    var msg = "Contract has been amended with specific values";
    var toastObj = window.parent.document.getElementById("successToast");
    toastObj.innerHTML = getTranslationMessage(msg);
    //toastObj.innerHTML = '<img src=\"../../../../../com/opentext/apps/utils/img/notification_success.svg\" width=\"25px\" height=\"25px\" align=\"middle\" style=\"margin-right:5px;\">' + getTranslationMessage(msg)

    $('ai-dialog-header', window.parent.document).append("<div style=\"font-size: 14px;color: #211e1eeb;margin-left: 4%;\">Document upload to the amended contract is optional</div>")

    var cancelButton = $('ai-dialog-footer .btn-secondary:contains("Cancel")', window.parent.document);
    var newBtn = document.createElement("button");
    newBtn.ccPublicAPIProvider = getPublicAPIProvider(window);
    newBtn.innerHTML = getTranslationMessage(i_newButtonName);
    newBtn.className = "btn btn-secondary btn-translate";
    newBtn.style.setProperty("margin", "12px 12px", "important");
    newBtn.title = generatedContractId;
    newBtn.setAttribute("onclick", "this.ccPublicAPIProvider.navigate('" + l_amendContractDetailsModel.amendedContractItemId + "',{'layoutID':'F8B156D635F3A1E89CB08DDB9883E4C8', 'clearBreadcrumb':false,'breadcrumbName':''})");
    cancelButton.before(newBtn);

    var closeBtn = document.createElement("button");
    closeBtn.innerHTML = getTranslationMessage("Close");
    closeBtn.className = "btn btn-secondary btn-translate";
    closeBtn.title = "Close";
    cancelButton.before(closeBtn);
    cancelButton.hide();
    closeBtn.setAttribute("onclick", "window.parent.location.reload()")

    $('ai-dialog', window.parent.document).animate({
        'max-height': '90vh',
        'max-width': '80vw',
        'width': '80vw',
        'height': '90vh'
    }, 500);
    
    $('ai-dialog-body', window.parent.document).css({
        'max-height': 'calc(80vh - 40px)',
        'overflow-y': 'inherit',
        'height': '100%',
    });

}

function platformDialogModifications(i_newButtonName, i_newButtonAction) {

    //hide OK button
    hideOKButton();
    //Save changes action in footer
    var newBtn = document.createElement("Button");
    newBtn.innerHTML = getTranslationMessage(i_newButtonName);
    newBtn.className = "btn btn-primary btn-translate";
    newBtn.onclick = i_newButtonAction;
    $('ai-dialog-footer .btn-primary', window.parent.document).before(newBtn);

    $('ai-dialog', window.parent.document).animate({
        'max-height': '80vh',
        'max-width': '60vw',
        'width': '40vw',
        'height': '60vh'
    }, 500);

    //Dialog content style enhancements             
    $('ai-dialog-body iframe', window.parent.document).css({
        'width': '100%',
        'height': 'calc(100% - 0px)'
    });

    $('ai-dialog-body', window.parent.document).css({
        'max-height': 'calc(60vh - 40px)',
        'overflow-y': 'inherit',
        'height': '100%',
    });

    $('.layout-panel .panel-container', window.parent.document).css({
        'padding-left': '0px'
    });

    $('panel-container iframe', window.parent.document).css({
        'height': 'calc(100% - 0px)',
        'width': '100%',
        'border': '0px'
    });


}

function filldefaultContractData(i_InstanceID) {
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
                    var l_contractTermDuration = getTextValue(contractData.InitialContractTenure);
                    if (l_contractTermDuration.lastIndexOf("M") > 0 && l_contractTermDuration.lastIndexOf("D") > 0 && getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("M"))) > 0 && getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("M") + 1, l_contractTermDuration.lastIndexOf("D"))) > 0) {
                        l_amendContractDetailsModel.contractTerm(getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("M"))) + " month(s), " + getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("M") + 1, l_contractTermDuration.lastIndexOf("D"))) + " day(s)");
                    }
                    else if (l_contractTermDuration.lastIndexOf("M") > 0 && getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("M"))) > 0) {
                        l_amendContractDetailsModel.contractTerm(getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("M"))) + " month(s)");
                    }
                    else if (l_contractTermDuration.lastIndexOf("D") > 0 && getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("D"))) > 0) {
                        l_amendContractDetailsModel.contractTerm(getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("D"))) + " day(s)");
                    } else {
                        l_amendContractDetailsModel.contractTerm("");
                    }
                    l_amendContractDetailsModel.currentEndDate(getTextValue(contractData.CurrentEndDate).replace('Z', ''));
                    l_amendContractDetailsModel.actualStartDate(getTextValue(contractData.MinStartdate).replace('Z', ''));
                    l_amendContractDetailsModel.actualStartDatetoLocale(formateDatetoLocale(getTextValue(contractData.MinStartdate)));
                    var l_autoRenewDuration = getTextValue(contractData.AutoRenewDuration)
                    if (l_autoRenewDuration.lastIndexOf("M") > 0 && l_autoRenewDuration.lastIndexOf("D") > 0 && getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("M"))) > 0 && getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("M") + 1, l_autoRenewDuration.lastIndexOf("D"))) > 0) {
                        l_amendContractDetailsModel.autoRenewDuration(getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("M"))) + " month(s), " + getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("M") + 1, l_autoRenewDuration.lastIndexOf("D"))) + " day(s)");
                    }
                    else if (l_autoRenewDuration.lastIndexOf("M") > 0 && getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("M"))) > 0) {
                        l_amendContractDetailsModel.autoRenewDuration(getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("M"))) + " month(s)");
                    }
                    else if (l_autoRenewDuration.lastIndexOf("D") > 0 && getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("D"))) > 0) {
                        l_amendContractDetailsModel.autoRenewDuration(getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("D"))) + " day(s)");
                    } else {
                        l_amendContractDetailsModel.autoRenewDuration("");
                    }
                    l_amendContractDetailsModel.autoRenewal(getTextValue(contractData.AutoRenew));
                    l_amendContractDetailsModel.isPerpetual(getTextValue(contractData.Perpetual));
                    updateActualStartDate();
                }
                translatePage();
            },
            error: function (responseFailure) {
                showOrHideErrorInfo("div_formErrorInfoArea", true, getTranslationMessage("Unable to retrieve contract details. Contact your administrator."), 10000);
                return false;
            }

        });
}

function amendWithUpdates() {
    if (checkforMandatoryFields()) {
        if (l_amendContractDetailsModel.clickable) {
            l_amendContractDetailsModel.clickable = false;
            $('ai-dialog-footer .btn-primary:contains("Amend")', window.parent.document).attr("style", "opacity: 1;cursor: not-allowed;background-color: #999999 !important; color: #ffffff;border-color: #999999;");
            var l_contractTerm, l_autoRenewDuration;
            if ((l_amendContractDetailsModel.autoRenewDuration() != undefined && l_amendContractDetailsModel.autoRenewDuration() != "")) {
                var months = "";
                var days = "";
                if (l_amendContractDetailsModel.autoRenewDuration().lastIndexOf("month(s)") > 0) {
                    months = l_amendContractDetailsModel.autoRenewDuration().substring(0, l_amendContractDetailsModel.autoRenewDuration().lastIndexOf(" month(s)"))
                }
                if (l_amendContractDetailsModel.autoRenewDuration().lastIndexOf("day(s)") > 0) {
                    if (months != "") {
                        days = l_amendContractDetailsModel.autoRenewDuration().substring(l_amendContractDetailsModel.autoRenewDuration().lastIndexOf("month(s), ") + "month(s), ".length, l_amendContractDetailsModel.autoRenewDuration().lastIndexOf(" day(s)"))
                        l_autoRenewDuration = ("P" + months + "M" + days + "D");
                    }
                    else {
                        days = l_amendContractDetailsModel.autoRenewDuration().substring(0, l_amendContractDetailsModel.autoRenewDuration().lastIndexOf(" day(s)"))
                        l_autoRenewDuration = ("P" + days + "D");
                    }
                }
                else {
                    l_autoRenewDuration = ("P" + months + "M");
                }
            } else {
                l_autoRenewDuration = "";
            }
            if ((l_amendContractDetailsModel.contractTerm() != undefined && l_amendContractDetailsModel.contractTerm() != "")) {
                var months = "";
                var days = "";
                if (l_amendContractDetailsModel.contractTerm().lastIndexOf("month(s)") > 0) {
                    months = l_amendContractDetailsModel.contractTerm().substring(0, l_amendContractDetailsModel.contractTerm().lastIndexOf(" month(s)"))
                }
                if (l_amendContractDetailsModel.contractTerm().lastIndexOf("day(s)") > 0) {
                    if (months != "") {
                        days = l_amendContractDetailsModel.contractTerm().substring(l_amendContractDetailsModel.contractTerm().lastIndexOf("month(s), ") + "month(s), ".length, l_amendContractDetailsModel.contractTerm().lastIndexOf(" day(s)"))
                        l_contractTerm = ("P" + months + "M" + days + "D");
                    }
                    else {
                        days = l_amendContractDetailsModel.contractTerm().substring(0, l_amendContractDetailsModel.contractTerm().lastIndexOf(" day(s)"))
                        l_contractTerm = ("P" + days + "D");
                    }
                }
                else {
                    l_contractTerm = ("P" + months + "M");
                }
            } else {
                l_contractTerm = "";
            }
            $.cordys.ajax({
                namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
                method: "AmendContract",
                parameters: {
                    "contractItemID": l_contractItemID,
                    "contractTerm": l_contractTerm,
                    "isPerpetual": l_amendContractDetailsModel.isPerpetual(),
                    "actualStartDate": l_amendContractDetailsModel.actualStartDate(),
                    "autoRenewal": l_amendContractDetailsModel.autoRenewal(),
                    "autoRenewDuration": l_autoRenewDuration,
                    "amendTypeItemID": l_amendContractDetailsModel.selectedAmendTypeID(),
                    "cloneDocument": l_amendContractDetailsModel.cloneDocument(),
                    "createWSShortcuts": l_amendContractDetailsModel.createWSShortcuts()
                },
                success: function (data) {

                    $('#div_amendContractForm').css('display', 'none');
                    $('ai-dialog-footer .btn-primary:contains("Amend")', window.parent.document).hide();

                    l_amendContractDetailsModel.amendedContractId = data.outputResponse.ID;
                    l_amendContractDetailsModel.amendedContractItemId = l_contractItemID.split(".")[0] + '.' + data.outputResponse.ID;
                    newContractId = l_contractItemID.split(".")[0] + '.' + data.outputResponse.ID;
                    modifyFormAfterAmend("Open amended contract", data.outputResponse.GeneratedContractId);

                    setTimeout(function () {
                        window.parent.document.getElementById("successToast").style.display = "none";
                        openDocumentsPanel();
                    }, 2000);
                },
                error: function (responseFailure) {
                    showOrHideErrorInfo("div_formErrorInfoArea", true, getTranslationMessage("Unable to amend the contract. Contact your administrator."), 10000);
                    // l_amendContractDetailsModel.clickable = true;
                    return false;
                }
            });
        }
    }
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
                            //var redirection_URL = "/home/MyOrg/app/start/web/item/005056C00008A1E795653A59509D399D." + newContractId + "/98FA9B1A7A8BA1ED8B990B5C409FC465";
                            if (window.navigator.language !== i_locale) {
                                redirection_URL = setUrlParameter(redirection_URL, 'language', i_locale);
                            }
                            window.location.href = redirection_URL;
                        }
                        else if (data.GCProperties[0].value == 'BUSINESS_WORKSPACE') {
                            var redirection_URL = "../../../../../app/start/web/perform/item/" + newContractId + "/FC7774E8DA63A1ED8C8D213D90668466";
                            //var redirection_URL =  "../../../../../app/start/web/perform/item/"+newContractId+"/B4B676CD53D8A1E8B2E9A54D7EEE0876?openContentInParentWindow=true";

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

function listAmendTypes() {
    $.cordys.ajax({
        namespace: "http://schemas/OpenTextContractCenter/AmendmentType/operations",
        method: "GetActiveAmendmentTypes",
        parameters: {},
        success: function (data) {
            addDataToAmendTypeSelectView(data.AmendmentType, l_amendContractDetailsModel);
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_formErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving the Amendment types. Contact your administrator."), 10000);
            return false;
        }
    });
}
function addDataToAmendTypeSelectView(iElementList, iModel) {
    iModel.AmendTypestoAdd.removeAll();
    iModel.AmendTypestoAdd.push({ "amendTypeId": "", "AmendType": getTranslationMessage("Select a value") });
    if (iElementList) {
        if (iElementList.length) {
            iElementList.forEach(function (iElement) {
                iModel.AmendTypestoAdd.push({ "amendTypeId": getTextValue(iElement["AmendmentType-id"].ItemId), "AmendType": getTextValue(iElement.Name) });
            });
        }
        else {
            iModel.AmendTypestoAdd.push({ "amendTypeId": getTextValue(iElementList["AmendmentType-id"].ItemId), "AmendType": getTextValue(iElementList.Name) });
        }
    }
}

function openClonedContract() {
    var l_ItemId = l_amendContractDetailsModel.amendedContractItemId;
    var l_Id = l_amendContractDetailsModel.amendedContractId;
    navigateToInstance(l_ItemId, "Contract", { "layoutID": 'F8B156D635F3A1E89CB08DDB9883E4C8', "clearBreadcrumb": false, "breadcrumbName": '' })
}

function updateActualStartDate() {
    if (l_amendContractDetailsModel.l_formAction == "RwA") {
        var l_date = new Date(l_amendContractDetailsModel.currentEndDate());
        l_date.setDate(l_date.getDate() + 1);
        l_date = l_date.toISOString();
        l_amendContractDetailsModel.actualStartDate(l_date.substring(0, l_date.indexOf("T")));
        l_amendContractDetailsModel.actualStartDatetoLocale(formateDatetoLocale(l_date.substring(0, l_date.indexOf("T"))));
    }
}

function amendContractURL(BASEURL, CHILDID) {
    return BASEURL + "app/start/web/perform/item/005056C00008A1E795653A59509D399D." + CHILDID + "/F8B156D635F3A1E89CB08DDB9883E4C8";
}

function checkforMandatoryFields() {
    var validationFlag = true;
    var $regexExp = /^([1-9]|[1-9][0-9]|[1-9][0-9][0-9])$/;

    if (l_amendContractDetailsModel.isPerpetual() == 'false' && (l_amendContractDetailsModel.contractTerm() == "" || l_amendContractDetailsModel.contractTerm() == undefined || $("#input_contractTerm").val() == "")) {
        $("#input_contractTerm").addClass("cc-error");
        validationFlag = false;
    }

    if (l_amendContractDetailsModel.actualStartDate() == "" || l_amendContractDetailsModel.actualStartDate() == undefined) {
        $("#input_actualStartDate").addClass("cc-error");
        validationFlag = false;
    }

    if (l_amendContractDetailsModel.autoRenewal() == 'true' && (l_amendContractDetailsModel.autoRenewDuration() == "" || l_amendContractDetailsModel.autoRenewDuration() == undefined || $("#input_autoRenewDuration").val() == "")) {
        $("#input_autoRenewDuration").addClass("cc-error");
        validationFlag = false;
    }
    return validationFlag;
}

function removeErrorClass(iEvent) {
    $(iEvent).removeClass("cc-error");
}

//Contract term preview modal
function openContractTermModal(attrData) {
    if ($('#ContractTermPreviewModel').css("display") == "none" && l_amendContractDetailsModel.isPerpetual() == "false")
        $('#ContractTermPreviewModel').css("display", "block");
    else
        $('#ContractTermPreviewModel').css("display", "none");

    addDataToContractTermLookup();
    $('button#btn_contractTermYes').off("click");
    $('button#btn_contractTermYes').on('click', function (_event) {
        if (!$("#input_contractTermMonths").val().match(/^0*$/) && !$("#input_contractTermDays").val().match(/^0*$/)) {
            l_amendContractDetailsModel.contractTerm($("#input_contractTermMonths").val() + " month(s), " + $("#input_contractTermDays").val() + " day(s)");
        }
        else if (!$("#input_contractTermMonths").val().match(/^0*$/)) {
            l_amendContractDetailsModel.contractTerm($("#input_contractTermMonths").val() + " month(s)");
        }
        else if (!$("#input_contractTermDays").val().match(/^0*$/)) {
            l_amendContractDetailsModel.contractTerm($("#input_contractTermDays").val() + " day(s)");
        }
        else {
            l_amendContractDetailsModel.contractTerm("");
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
    if (l_amendContractDetailsModel.contractTerm().lastIndexOf("month(s)") > 0) {
        months = l_amendContractDetailsModel.contractTerm().substring(0, l_amendContractDetailsModel.contractTerm().lastIndexOf(" month(s)"))
    }
    if (l_amendContractDetailsModel.contractTerm().lastIndexOf("day(s)") > 0) {
        if (months != "") {
            days = l_amendContractDetailsModel.contractTerm().substring(l_amendContractDetailsModel.contractTerm().lastIndexOf("month(s), ") + "month(s), ".length, l_amendContractDetailsModel.contractTerm().lastIndexOf(" day(s)"))
        }
        else {
            days = l_amendContractDetailsModel.contractTerm().substring(0, l_amendContractDetailsModel.contractTerm().lastIndexOf(" day(s)"))
        }
    }
    $("#input_contractTermMonths").val(months);
    $("#input_contractTermDays").val(days);
}

//Auto renew duration modal
function openAutoRenewDurModal(attrData) {
    if ($('#AutoRenewDurPreviewModel').css("display") == "none" && l_amendContractDetailsModel.autoRenewal() == "true")
        $('#AutoRenewDurPreviewModel').css("display", "block");
    else
        $('#AutoRenewDurPreviewModel').css("display", "none");

    addDataToAutoRenewDurLookup();
    $('button#btn_autoRenewDurYes').off("click");
    $('button#btn_autoRenewDurYes').on('click', function (_event) {
        if (!$("#input_autoRenewDurMonths").val().match(/^0*$/) && !$("#input_autoRenewDurDays").val().match(/^0*$/)) {
            l_amendContractDetailsModel.autoRenewDuration($("#input_autoRenewDurMonths").val() + " month(s), " + $("#input_autoRenewDurDays").val() + " day(s)");
        }
        else if (!$("#input_autoRenewDurMonths").val().match(/^0*$/)) {
            l_amendContractDetailsModel.autoRenewDuration($("#input_autoRenewDurMonths").val() + " month(s)");
        }
        else if (!$("#input_autoRenewDurDays").val().match(/^0*$/)) {
            l_amendContractDetailsModel.autoRenewDuration($("#input_autoRenewDurDays").val() + " day(s)");
        }
        else {
            l_amendContractDetailsModel.autoRenewDuration("");
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
    if (l_amendContractDetailsModel.autoRenewDuration().lastIndexOf("month(s)") > 0) {
        months = l_amendContractDetailsModel.autoRenewDuration().substring(0, l_amendContractDetailsModel.autoRenewDuration().lastIndexOf(" month(s)"))
    }
    if (l_amendContractDetailsModel.autoRenewDuration().lastIndexOf("day(s)") > 0) {
        if (months != "") {
            days = l_amendContractDetailsModel.autoRenewDuration().substring(l_amendContractDetailsModel.autoRenewDuration().lastIndexOf("month(s), ") + "month(s), ".length, l_amendContractDetailsModel.autoRenewDuration().lastIndexOf(" day(s)"))
        }
        else {
            days = l_amendContractDetailsModel.autoRenewDuration().substring(0, l_amendContractDetailsModel.autoRenewDuration().lastIndexOf(" day(s)"))
        }
    }
    $("#input_autoRenewDurMonths").val(months);
    $("#input_autoRenewDurDays").val(days);
}