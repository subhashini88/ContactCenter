$.cordys.json.defaults.removeNamespacePrefix = true;
var l_contractItemID = "";

var BASEURL;
var i_locale;
var l_terminate_attributes_model;


const DEFAULT_DATE_FORMAT = { label: "MM/DD/YY", value: "mm/dd/yy" };
const DATE_FORMAT_DD_MMM_YYYY = { label: "DD-MMM-YYYY", value: "dd-M-yy" };
const DATE_FORMAT_DD_MM_YYYY = { label: "DD-MM-YYYY", value: "dd-mm-yy" };
const DATE_FORMAT_MM_DD_YYYY = { label: "MM-DD-YYYY", value: "mm-dd-yy" };
const DATE_FORMAT_MMM_DD_YYYY = { label: "MMM-DD-YYYY", value: "M-dd-yy" };
const DATE_FORMAT_YYYY_MM_DD = { label: "YYYY-MM-DD", value: "yy-mm-dd" };

const DATE_FORMAT_SUBMISSION = "YYYY-MM-DD";
const DATE_FROMAT_UI = "MM/DD/YYYY";

var TerminateModel = function () {
    var self = this;
    self.TerminationFees = ko.observable().extend({ trackValueChange: true });
    self.CancellationDate = ko.observable().extend({ trackValueChange: true });
    self.EarlyTerminationConditions = ko.observable().extend({ trackValueChange: true });
    self.TerminationNoticePeriod = ko.observable().extend({ trackValueChange: true });
    self.TerminationReason = ko.observable().extend({ trackValueChange: true });
    self.TerminationReasonID = ko.observable();
    self.TerminationReasonItemID = ko.observable();
    self.CancellationComments = ko.observable();
    self.contractData = ko.observable();
    self.terminateObligations = ko.observable(false);

    self.OnClickTerminateObligations = () => {
        self.terminateObligations(!self.terminateObligations());
    }

    self.loadData = (data) => {
        l_terminate_attributes_model.contractData(data.Contract);
        l_terminate_attributes_model.CancellationDate(data.Contract.CancellationDate ? data.Contract.CancellationDate.split("Z")[0] : "");
        l_terminate_attributes_model.TerminationFees(data.Contract.TerminationFees);
        l_terminate_attributes_model.TerminationNoticePeriod(data.Contract.TerminationNoticePeriod);
        l_terminate_attributes_model.CancellationComments(data.Contract.CancellationComments);
        l_terminate_attributes_model.EarlyTerminationConditions(data.Contract.EarlyTerminationConditions);
        l_terminate_attributes_model.terminateObligations(data.Contract.Z_INT_InActiveObligOnTermination === "TRUE" ? true : false);
        l_terminate_attributes_model.TerminationReasonID(data.Contract.TerminationReason && data.Contract.TerminationReason["TerminationReason-id"] ?
            data.Contract.TerminationReason["TerminationReason-id"].Id : "");
        if (l_terminate_attributes_model.TerminationReasonID()) {
            _loadTerminationReasonData(l_terminate_attributes_model.TerminationReasonID())
        }
    }

    function _loadTerminationReasonData(id) {
        ReadTerminationReason({
            "TerminationReason-id": {
                "Id": id
            }
        }, (data) => {
            self.TerminationReason(data.TerminationReason.Reason);
        })
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


function openTermiReasonSelModal(attrData) {
    $('#div_termiReasonLookupModal').modal({
        backdrop: 'static',
        keyboard: false
    })
    ListAllTerminationReasons();
    $('button#btn_selectTerminationReasonYes').off("click");
    $('button#btn_selectTerminationReasonYes').on('click', function (_event) {
        l_terminate_attributes_model.TerminationReason(l_terminationReasonLookup_model.selectedTerminationReasonName())
        l_terminate_attributes_model.TerminationReasonID(l_terminationReasonLookup_model.selectedTerminationReasonID())
        l_terminate_attributes_model.TerminationReasonItemID(l_terminationReasonLookup_model.selectedTerminationReasonItemID())
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

let terminate_clicked = false;
/*
function UpdateContractStatus() {
    if (!terminate_clicked) {
        terminate_clicked = true;
        let contractRequest = {
            "Contract-id": {
                "ItemId": l_contractItemID
            },
            "Contract-update": {
                "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance",
                "CancellationDate": l_terminate_attributes_model.CancellationDate()
                    ? moment(l_terminate_attributes_model.CancellationDate()).format(DATE_FORMAT_SUBMISSION) : null,
                "TerminationFees": l_terminate_attributes_model.TerminationFees()
                    ? l_terminate_attributes_model.TerminationFees() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" },
                "TerminationNoticePeriod": l_terminate_attributes_model.TerminationNoticePeriod()
                    ? l_terminate_attributes_model.TerminationNoticePeriod() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" },
                "CancellationComments": l_terminate_attributes_model.CancellationComments()
                    ? l_terminate_attributes_model.CancellationComments() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" },
                "EarlyTerminationConditions": l_terminate_attributes_model.EarlyTerminationConditions()
                    ? l_terminate_attributes_model.EarlyTerminationConditions() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" },
                "Z_INT_Status": "TerminationReview",
                "Z_INT_InActiveObligOnTermination": l_terminate_attributes_model.terminateObligations() ? "TRUE" : "FALSE"
            }
        };

        if (l_terminate_attributes_model.TerminationReasonID()) {
            contractRequest["Contract-update"].TerminationReason = { "TerminationReason-id": { "Id": l_terminate_attributes_model.TerminationReasonID() } };
        }

        $.cordys.ajax(
            {
                method: "UpdateContract",
                namespace: "http://schemas/OpenTextContractCenter/Contract/operations",
                parameters: contractRequest,
            }).done(function (data) {
                parent.window.location.reload();
                terminate_clicked = false;
            }).fail(function (error) {
                terminate_clicked = false;
            })
    }
}*/

function UpdateContractStatus() {
    if (!terminate_clicked) {
        terminate_clicked = true;
        let contractRequest = {
                "CancellationDate": l_terminate_attributes_model.CancellationDate()
                    ? moment(l_terminate_attributes_model.CancellationDate()).format(DATE_FORMAT_SUBMISSION) : null,
                "TerminationFees": l_terminate_attributes_model.TerminationFees()
                    ? l_terminate_attributes_model.TerminationFees() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" },
                "TerminationNoticePeriod": l_terminate_attributes_model.TerminationNoticePeriod()
                    ? l_terminate_attributes_model.TerminationNoticePeriod() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" },
                "CancellationComments": l_terminate_attributes_model.CancellationComments()
                    ? l_terminate_attributes_model.CancellationComments() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" },
                "EarlyTerminationConditions": l_terminate_attributes_model.EarlyTerminationConditions()
                    ? l_terminate_attributes_model.EarlyTerminationConditions() : { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" },
                "Z_INT_Status": "TerminationReview",
                "ActionName":"Terminate",
                "ContractItemID":l_contractItemID,
                "Z_INT_InActiveObligOnTermination": l_terminate_attributes_model.terminateObligations() ? "TRUE" : "FALSE"
        };

        if (l_terminate_attributes_model.TerminationReasonID()) {
            contractRequest.TerminationReason = { "TerminationReasonID": l_terminate_attributes_model.TerminationReasonID() };
        }

        $.cordys.ajax(
            {
                method: "TerminateActions",
                namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
                parameters: contractRequest,
            }).done(function (data) {
                parent.window.location.reload();
                terminate_clicked = false;
            }).fail(function (error) {
                terminate_clicked = false;
            })
    }
}

function refreshFrame() {
    if (isIE()) {
        var caFrameElement = window.frameElement.ownerDocument.getElementsByTagName('iFrame');
        for (var i = 0; i < caFrameElement.length; i++) {
            if ((caFrameElement[i].src).indexOf("terminateContract.htm") > -1) {
                caFrameElement[i].src = caFrameElement[i].src;
            }
        }
    }
    else {
        var source = $('[src*="terminateContract.htm"]', window.parent.document).attr('src');
        $('[src*="terminateContract.htm"]', window.parent.document).attr('src', source);
    }
}

function ReadTerminationReason(req, callBack) {
    $.cordys.ajax(
        {
            namespace: "http://schemas/OpenTextContractCenter/TerminationReason/operations",
            method: "ReadTerminationReason",
            parameters: req,
            success: function (data) {
                if (callBack) {
                    callBack(data)
                }
            }
        });
}
function ReadContract(callBack) {
    $.cordys.ajax(
        {
            namespace: "http://schemas/OpenTextContractCenter/Contract/operations",
            method: "ReadContract",
            parameters:
            {
                "Contract-id":
                {
                    "ItemId": l_contractItemID
                }
            },
            success: function (data) {
                if (callBack) {
                    callBack(data);

                }
                // if (data) {
                //     l_terminate_attributes_model.contractData(data.Contract);
                //     // commentsModel.comments = data.Contract.Comments;
                //     // commentsModel.contractID = data.Contract["Contract-id"].Id;
                //     // ko.applyBindings(commentsModel, document.getElementById("commentsDiv"));
                // }
            }
        });
}


function InActivateAllObligations() {
    $.cordys.ajax(
        {
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            method: "InActivateAllContractObligations",
            parameters:
            {
                "contractId": l_contractItemID.split(".")[1]
            },
            success: function (data) {
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
            valueAccessor()(($el.datepicker("getDate")));
            $el[0].value = formateDatetoLocale(moment(value, DATE_FORMAT_YYYY_MM_DD.label).format("YYYY-MM-DD"));
        }
    }
};



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
        'width': '52vw',
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

let inValidList = [];
let isActionClicked = true;

function showError(invalidErrors) {
    if (isActionClicked && invalidErrors && invalidErrors.length > 0) {
        inValidList = invalidErrors;
        updateToastContent();
        if (inValidList.length == 0) {
            isActionClicked = false;
            errorToastToggle("hide");
        }
        else {
            if (inValidList.length == 1)
                updateToastContent(getTranslationMessage("{0}", [inValidList[0]]));
            else
                updateToastContent(getTranslationMessage("{0} errors", [inValidList.length]), contentText());
            errorToastToggle("show");
        }
        setTimeout(function () {
            errorToastToggle("hide");
        }, 5000);
    }
}


function contentText() {
    str = null;
    for (i = 0; i < inValidList.length; i++)
        if (i == 0)
            str = getTranslationMessage("{0}", [inValidList[i]]);
        else
            str = str + "<br>" + getTranslationMessage("{0}", [inValidList[i]]);
    return str;
}

function validateTerminationForm() {
    let isValid = true;
    let errors = [];

    let currentDate = new Date();
    currentDate.setHours(0, 0, 0, 0);
    if (!l_terminate_attributes_model.CancellationDate()) {
        isValid = false;
        errors.push("Termination date is required.");
    }
    // if (l_terminate_attributes_model.CancellationDate() < currentDate) {
    //     isValid = false;
    //     errors.push("Termination date cannot be less than current date.");
    // }

    let minStartDate = new Date(l_terminate_attributes_model.contractData().MinStartdate.split("Z")[0]);
    minStartDate.setHours(0, 0, 0, 0);
    if (l_terminate_attributes_model.CancellationDate() < minStartDate) {
        isValid = false;
        errors.push("Termination date cannot be less than start date of contract.");
    }
    if (errors.length > 0) {
        showError(errors)
        console.log(errors);
    }
    return isValid;
}

function onTerminateClick() {
    if (validateTerminationForm()) {
        UpdateContractStatus();
    }
}

$(document).ready(function () {
    i_locale = getlocale();
    var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
    translateLabels("com/opentext/apps/contractcenter/ContractCenter/ContractCenter", i_locale, true);
    loadRTLIfRequired(i_locale, rtl_css);

    var cInstanceId = getUrlParameterValue("instanceId", null, true);
    l_contractItemID = cInstanceId;
    BASEURL = getUrlParameterValue("baseURL", null, true);

    l_terminate_attributes_model = new TerminateModel(cInstanceId);
    l_terminationReasonLookup_model = new TerminationReasonLookupModel();
    ko.applyBindings(l_terminate_attributes_model, document.getElementById("div_cloneContractForm"));
    ko.applyBindings(l_terminationReasonLookup_model, document.getElementById("div_termiReasonLookupModal"));
    platformDialogModifications("Terminate", onTerminateClick);
    ReadContract((data, status) => {
        l_terminate_attributes_model.loadData(data);
    });
});
