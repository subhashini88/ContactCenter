//Global variables.
$.cordys.json.defaults.removeNamespacePrefix = true;
var currentState;
var isExecutedContract;
var customAttributeModel = null;

// Constants.
const DATA_TYPE_BOOLEAN = "BOOLEAN";
const DATA_TYPE_NUMBER = "NUMBER";
const DATA_TYPE_TEXT = "TEXT";
const DATA_TYPE_DATE = "DATE";
const DATA_TYPE_ENUM = "ENUM";
const DEFAULT_DATE_FORMAT = { label: "MM/DD/YY", value: "mm/dd/yy" };
const DATE_FORMAT_DD_MMM_YYYY = { label: "DD-MMM-YYYY", value: "dd-M-yy" };
const DATE_FORMAT_DD_MM_YYYY = { label: "DD-MM-YYYY", value: "dd-mm-yy" };
const DATE_FORMAT_MM_DD_YYYY = { label: "MM-DD-YYYY", value: "mm-dd-yy" };
const DATE_FORMAT_MMM_DD_YYYY = { label: "MMM-DD-YYYY", value: "M-dd-yy" };
const DATE_FORMAT_YYYY_MM_DD = { label: "YYYY-MM-DD", value: "yy-mm-dd" };


// Models.
/** ----------------------Start of Models  -------------------*/



var CustomAttributesListModel = function () {
    var self = this;
    self.CustomAttributes = ko.observableArray([]);
    self.isEditable = ko.observable(false);
    self.isDirty = ko.observable(false);
    self.dataLoaded = ko.observable(false);
    self.isInitialized = false;

    self.saveAttributes = function () {
        if (self.isDirty()) {
            updateAttributeValues();
        }
    }

    // Integrating with contract details page.
    self.isLoaded = ko.observable(false);
    self.displayScreen = ko.observable(false);
    self.loadPageContent = function () {
        $('#loadingMsg').css('display', 'block');
        /*if (isEditAllowedInThisState()) {
            checkForUserRoleforEdit();
        } else {
            ListAllCustomAttributes();
        }*/
        if (_userStateEditabilityFlag) {
            self.isEditable(true);
        }
        ListAllCustomAttributes();
        self.isLoaded(true);
    }


}

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

    self.showAllDropDownOptions = function (iItem, event) {
        getAllDropdownValues(iItem.attributeId, iItem);
        event.stopPropagation();

    };
    self.updateValue = function (iItem, event) {
        // getAllDropdownValues(iItem.attributeId, iItem);
        event.stopPropagation();

    };
}


ko.bindingHandlers.datepicker = {
    init: function (element, valueAccessor, allBindingsAccessor) {
        var options = allBindingsAccessor().datepickerOptions || {},
            $el = $(element);

        //initialize datepicker with some optional options
        $el.datepicker(options);

        if (valueAccessor()()) {
            $el.datepicker('setDate', moment(valueAccessor()(), DATE_FORMAT_YYYY_MM_DD.label).toDate());
        }

        //handle the field changing
        ko.utils.registerEventHandler(element, "change", function () {
            var observable = valueAccessor();
            observable($el.datepicker("getDate"));
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

        if (value - current !== 0) {
            $el.datepicker("setDate", moment(value, DATE_FORMAT_YYYY_MM_DD.label).toDate());
        }
    }
};

ko.extenders.trackChange = function (target, track) {
    if (track) {
        target.isInitialized = false;
        target.isDirty = ko.observable(false);
        target.originalValue = target();
        target.setOriginalValue = function (startingValue) {
            target.originalValue = startingValue;
        };
        target.subscribe(function (newValue) {
            if (l_custom_attributes_info_model.isInitialized && !target.isDirty()) {
                target.isDirty(newValue != target.originalValue);
                if (target.isDirty()) {
                    l_custom_attributes_info_model.isDirty(true);
                }
            }
        });
    }
    return target;
};

/** ----------------------End of Models  ------------------- */

// Initial page loading.

$(function () {

    /** 
    contractID = getUrlParameterValue("instanceId", null, true);
    isExecutedContract = getUrlParameterValue("isExecutedContract", null, true);
    currentState = getUrlParameterValue("contractStatus", null, true);
    var i_locale = getlocale();
    var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
    translateLabels("com/opentext/apps/commoncomponents/CustomAttributes/CustomAttributes", i_locale);
    loadRTLIfRequired(i_locale, rtl_css);

    if (window.parent.parent) {
        customAttributesFrame = $('[src*="customattributes.htm"]', window.parent.parent.document);
        if (customAttributesFrame) {
            customAttributesFrame.css('border', 'none');
        }
    }
    createToastDiv();
    l_customAttributeslist_model = new CustomAttributesListModel();
    ko.applyBindings(l_customAttributeslist_model, document.getElementById("id_customAttributesData"));
    if (isEditAllowedInThisState()) {
        checkForUserRole();
    } else {
        ListAllCustomAttributes();
    }
     */
});


// Functions.
/** ----------------------Start of Functions  ------------------- */

function addDataCustomAttributesToView(iElementList, iModel) {
    if (iElementList) {
        if (iElementList.length) {
            iElementList.forEach(function (iElement) {
                customAttributeModel = formCustomAttributeModel(iElement);
                iModel.CustomAttributes.push(customAttributeModel);
            });
        }
        else {
            //iModel.totalCurrentPageCustomAttributesCount("1");
            customAttributeModel = formCustomAttributeModel(iElementList);
            iModel.CustomAttributes.push(customAttributeModel);
        }
    }
}

function formCustomAttributeModel(iElement) {

    customAttributeModel = new CustomAttributeModel();
    if (iElement) {
        if (iElement['RelatedAttributes-id']) {
            customAttributeModel.id = iElement['RelatedAttributes-id'].Id;
            customAttributeModel.id1 = iElement['RelatedAttributes-id'].Id1;
        }
        if (iElement['RelatedDefinition']) {
            customAttributeModel.attributeId = iElement.RelatedDefinition["AttributeDefinition-id"].Id;
        }
        customAttributeModel.name = getTextValue(iElement.Name);
        if (iElement.RelatedLabel) {
            customAttributeModel.label = getTextValue(iElement.RelatedLabel.Label);
        }
        customAttributeModel.dataType = getTextValue(iElement.DataType);
        customAttributeModel.selectLoaded = false;
        customAttributeModel.attributeMedadata = iElement.AttributeMetaData;
        customAttributeModel.value = ko.observable(getTextValue(iElement.Value)).extend({ trackChange: true });
        readMetaDataJSONAndFillModel(customAttributeModel);

    }
    return customAttributeModel;
}

function formSaveAttributeObject() {
    var requestObj = {};
    requestObj.CustomAttributes = [];
    if (l_custom_attributes_info_model && l_custom_attributes_info_model.CustomAttributes()) {
        l_custom_attributes_info_model.CustomAttributes().forEach(
            function (attribute_item) {
                if (attribute_item.value.isDirty()) {
                    var CustomAttribute = {};
                    CustomAttribute.id = attribute_item.id;
                    CustomAttribute.id1 = attribute_item.id1;
                    CustomAttribute.name = attribute_item.name;
                    CustomAttribute.dataType = attribute_item.dataType;
                    if (attribute_item.dataType == DATA_TYPE_DATE) {
                        CustomAttribute.value = $.datepicker.formatDate("yy-mm-dd", attribute_item.value());
                    } else if (attribute_item.dataType == DATA_TYPE_BOOLEAN) {
                        CustomAttribute.value = attribute_item.value() ? 'true' : 'false';
                    }
                    else {
                        CustomAttribute.value = attribute_item.value()
                    }
                    requestObj.CustomAttributes.push({ 'CustomAttribute': CustomAttribute });
                }
            }
        );
    }
    return requestObj;
}

function clearAllFlags() {
    l_custom_attributes_info_model.isInitialized = false;
    l_custom_attributes_info_model.CustomAttributes.removeAll();
    l_custom_attributes_info_model.isDirty(false);
}

function cancelSaveOREdit() {
    $("#cancelModal").modal();
    $('button#cancelCustAttrChanges').off("click");
    $('button#cancelCustAttrChanges').on('click', function (_event) {
        ListAllCustomAttributes();
    });
}

/** ----------------------End of Functions  ------------------- */


// Services.
/** ----------------------Start of Services  ------------------- */

// Read all custom attributes.
function ListAllCustomAttributes() {
    clearAllFlags();
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
        method: "GetMappedCustomAttributes",
        parameters: { 'ContractItemId': contractItemID },
        success: function (data) {
            $('#loadingMsg').css('display', 'none');
            l_custom_attributes_info_model.dataLoaded(true);
            if (data && data.FindZ_INT_RelatedAttributesListResponse) {
                addDataCustomAttributesToView(data.FindZ_INT_RelatedAttributesListResponse.RelatedAttributes, l_custom_attributes_info_model);
                l_custom_attributes_info_model.isInitialized = true;
            }
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while loading the custom attributes. Contact your administrator."), 10000);
            return false;
        }
    });
}


// Update custom attributes.
function updateAttributeValues() {
    if (validateFields()) {
        var requestObj = formSaveAttributeObject();
        if (requestObj) {
            $.cordys.ajax({
                method: "UpdateCustomAttributes",
                namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
                parameters: requestObj,
                success: function (responseSuccess) {
                    if (responseSuccess) {
                        successToast(3000, getTranslationMessage("Custom attributes updated"));
                        ListAllCustomAttributes();
                        // PushUpdatestoNegotiation();
                        updateStartRuleExecute();
                    } else {
                        showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while updating custom attributes. Contact your administrator."), 10000);
                    }
                },
                error: function (responseFailure) {
                    showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while updating custom attributes. Contact your administrator."), 10000);
                    return false;
                }
            });
        }
    }
}

function updateStartRuleExecute() {
    if (contractItemID) {
        $.cordys.ajax({
            method: "UpdateAuthoringRuleResults",
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            parameters: { "ContractId": contractItemID.split(".")[1] },
            success: function (responseSuccess) {
                PushUpdatestoNegotiation();
            },
            error: function (responseFailure) {
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while updating contract rules results. Contact your administrator."), 10000);
                return false;
            }
        });
    }
}

function PushUpdatestoNegotiation() {
    $.cordys.ajax({
        method: "UpdateContractDetailsonCusAttrEdit",
        namespace: "http://schemas.opentext.com/apps/ContractNegotiation/19.4",
        parameters: { 'contractItemID': contractItemID },
        success: function (responseSuccess) {
            if (responseSuccess) {

            } else {
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while updating custom attributes for external negotiator view. Contact your administrator."), 10000);
            }
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while updating custom attributes for external negotiator view. Contact your administrator."), 10000);
            return false;
        }
    });
}

// Check for user user role for edit.
function checkForUserRoleforEdit() {
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
                l_custom_attributes_info_model.isEditable(true);
            } else {
                l_custom_attributes_info_model.isEditable(false);
            }
            ListAllCustomAttributes();

        }).fail(function (error) {
            ListAllCustomAttributes();
        })
}
/** ----------------------End of Services  ------------------- */



// Util functions.
/** ----------------------Start of Util functions  ------------------- */

function readMetaDataJSONAndFillModel(model) {
    if (model && model.attributeMedadata) {
        var metaDataObj = JSON.parse(model.attributeMedadata);
        switch (model.dataType) {
            case DATA_TYPE_BOOLEAN:
                if (!model.value()) {
                    model.value(false);
                } else {
                    model.value(model.value().toLowerCase() == 'true' ? true : false);
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
                //model.value(model.value() ? moment(model.value(), "yy-mm-dd").toDate() : "");
                model.placeHolder = getDateFormatLabel(model.dateFormat);
                break;
            case DATA_TYPE_ENUM:
                model.placeHolder = "Select an option";
                if (model.value()) {
                    var choice = {};
                    choice.choice = false;

                    choice.name = model.value();
                    choice.value = model.value();
                    model.selectOptions.push(choice);
                }
                break;
        }
    }
}

function getAllDropdownValues(attributeId, iItem) {
    var dropDown = l_custom_attributes_info_model.CustomAttributes().filter(function (i) { return i.attributeId == attributeId });
    if (dropDown[0].selectLoaded) {
        return;
    }

    $.cordys.ajax({
        namespace: "http://schemas/OpenTextCustomAttributes/AttributeDefinition/operations",
        method: "GetRelatedEnums",
        parameters: {
            "AttributeDefinition-id": {
                "Id": attributeId
            }
        },
        success: function (data) {
            addOptionsToDropdown(data.RelatedEnums, attributeId);

        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("Unable to retrieve the Dropdown list. Contact your administrator."), 10000);
            return false;
        }
    });
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

function addOptionsToDropdown(iElementList, attributeId) {
    var dropDown = l_custom_attributes_info_model.CustomAttributes().filter(function (i) { return i.attributeId == attributeId });
    if (dropDown != null && dropDown.length > 0) {
        if (dropDown[0].selectOptions().length > 0) {
            dropDown[0].selectOptions.removeAll();
        }
        if (iElementList) {
            if (iElementList.length) {
                iElementList.forEach(function (option) {
                    var choice = {};
                    choice.choice = false;
                    choice.name = option.enumvalue;
                    choice.value = option.enumvalue;
                    choice.order = option.order;
                    dropDown[0].selectOptions.push(choice);
                });
                dropDown[0].selectOptions.sort(compareOptionsOrder);
            }
            else {
                var choice = {};
                choice.choice = false;
                choice.name = iElementList.enumvalue;
                choice.value = iElementList.enumvalue;
                dropDown[0].selectOptions.push(choice);
            }
        }
        dropDown[0].selectLoaded = true;
		dropDown[0].value(dropDown[0].value.originalValue);
    }
}

function isEditAllowedInThisState() {
    if (lifecycle_CurrentState == "Draft" || lifecycle_CurrentState == "Negotiation"
        || lifecycle_CurrentState == "Pre-Execution" || lifecycle_CurrentState == "Execution") {
        return true;
    }
    if (isExecutedContract && isExecutedContract == "true") {
        return true;
    }
    return false;
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

/** ----------------------End of Util functions  ------------------- */


// Validations.
/** ----------------------Start of Validations  ------------------- */

function validateFields() {
    var validationFlag = true;
    l_custom_attributes_info_model.CustomAttributes().forEach(
        function (customAttribute) {
            if (customAttribute.value.isDirty()) {
                if (customAttribute.dataType == DATA_TYPE_NUMBER) {
                    if (isNaN(customAttribute.value())) {
                        validationFlag = false;
                        $("#" + customAttribute.name).addClass("cc-error");
                        customAttribute.hasErrors(true);
                        customAttribute.errorMessage("Enter a number.");
                    }
                    if (customAttribute.allowDecimal == false || customAttribute.allowDecimal == "false") {
                        if (customAttribute.value() % 1 > 0) {
                            validationFlag = false;
                            $("#" + customAttribute.name).addClass("cc-error");
                            customAttribute.hasErrors(true);
                            customAttribute.errorMessage("Enter a whole number. Decimal values are not allowed.");
                        }
                    }
                } else if (customAttribute.dataType == DATA_TYPE_DATE) {

                } else if (customAttribute.dataType == DATA_TYPE_BOOLEAN) {
                    if (customAttribute.allowDecimal != false && customAttribute.allowDecimal != true) {
                        validationFlag = false;
                        $("#" + customAttribute.name).addClass("cc-error");
                    }
                }
            }
        }
    );
    return validationFlag;
}

/** ----------------------End of Validations  ------------------- */