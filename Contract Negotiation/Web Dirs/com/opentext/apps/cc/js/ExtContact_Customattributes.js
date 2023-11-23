//Global variables.
$.cordys.json.defaults.removeNamespacePrefix = true;
var l_customAttributeslist_model;
var relatedContentItemID;

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
    self.dataLoaded = ko.observable(false);
}

var CustomAttributeModel = function () {
    var self = this;

    // Attribute properties.
    self.id = "";
    self.name = "";
    self.label = "";
    self.dataType = "";
    self.attributeMedadata = "";
    self.dateFormat = "";
    self.selectOptions = ko.observableArray([]);
    self.value = ko.observable("");
    self.errorMessage = ko.observable("");

    // Flags to handle
    self.isDirty = false;
    self.allowDecimal = false;
    self.hasErrors = ko.observable(false);

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


/** ----------------------End of Models  ------------------- */

// Initial page loading.

$(function () {
    relatedContentItemID = getUrlParameterValue("instanceId", null, true);
    var i_locale = getlocale();
    var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
    translateLabels("com/opentext/apps/commoncomponents/CustomAttributes/CustomAttributes", i_locale);
    loadRTLIfRequired(i_locale, rtl_css);

    if (window.parent.parent) {
        customAttributesFrame = $('[src*="customAttributes.htm"]', window.parent.parent.document);
        if (customAttributesFrame) {
            customAttributesFrame.css('border', 'none');
        }
    }
    createToastDiv();
    l_customAttributeslist_model = new CustomAttributesListModel();
    ko.applyBindings(l_customAttributeslist_model, document.getElementById("id_customAttributesData"));
    ListAllCustomAttributes();
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
    var customAttributeModel = null;
    customAttributeModel = new CustomAttributeModel();
    if (iElement) {
        if (iElement['RelatedAttributes-id']) {
            customAttributeModel.id = iElement['RelatedAttributes-id'].Id;
            customAttributeModel.id1 = iElement['RelatedAttributes-id'].Id1;
        }
        customAttributeModel.name = getTextValue(iElement.Name);
        if (iElement.RelatedLabel) {
            customAttributeModel.label = getTextValue(iElement.RelatedLabel.Label);
        }
        customAttributeModel.dataType = getTextValue(iElement.DataType);
        customAttributeModel.attributeMedadata = iElement.AttributeMetaData;
        customAttributeModel.value = ko.observable(getTextValue(iElement.Value)).extend({ trackChange: true });
        readMetaDataJSONAndFillModel(customAttributeModel);

    }
    return customAttributeModel;
}

/** ----------------------End of Functions  ------------------- */


// Services.
/** ----------------------Start of Services  ------------------- */

// Read all custom attributes.
function ListAllCustomAttributes() {
    l_customAttributeslist_model.CustomAttributes.removeAll();
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/ContractNegotiation/19.4",
        method: "GetRelatedContractContent",
        parameters: {
            'ItemId': relatedContentItemID,
            'IsCustomattributesRequired': 'true'
        },
        success: function (data) {
            // debugger
            $('#loadingMsg').css('display', 'none');
            l_customAttributeslist_model.dataLoaded(true);
            if (data && data.RelatedContractContent.CustomAttributesInfo.CustomAttributes &&
                data.RelatedContractContent.CustomAttributesInfo.CustomAttributes.GetMappedCustomAttributesResponse) {
                addDataCustomAttributesToView(data.RelatedContractContent.CustomAttributesInfo.CustomAttributes.GetMappedCustomAttributesResponse.FindZ_INT_RelatedAttributesListResponse.RelatedAttributes, l_customAttributeslist_model);
            }
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while loading the custom attributes. Contact your administrator."), 10000);
            return false;
        }
    });
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
                break;
            case DATA_TYPE_TEXT:
                break;
            case DATA_TYPE_DATE:
                model.dateFormat = (metaDataObj.dateformat) ? metaDataObj.dateformat : DEFAULT_DATE_FORMAT.value;                                
                break;
            case DATA_TYPE_ENUM:
                if (metaDataObj.dropdown && metaDataObj.dropdown.options) {
                    metaDataObj.dropdown.options.forEach(function (option) {
                        var choice = {};
                        choice.choice = false;
                        choice.name = option.option;
                        choice.value = option.option;
                        model.selectOptions.push(choice);
                    });
                }
                break;
        }
    }
}
