var instanceId;

$.cordys.json.defaults.removeNamespacePrefix = true;
var InternalPartiesModel = function () {
    var self = this;
    self.INTParties = ko.observableArray([]);
}

var InternalPartyDetailsModel = function () {
    var self = this;

    self.CTRPartyID = ko.observable('');
    self.CTRPartyItemID = ko.observable('');
    self.CTRPartyName = ko.observable('');
    self.CTRPartyRelationType = ko.observable('');

    self.INTPartyID = ko.observable('');
    self.INTPartyItemID = ko.observable('');

    self.INTPartyRegID = ko.observable('');
    self.INTPartyName = ko.observable('');
    self.INTPartyRegName = ko.observable('');

    self.INTPartyContacts = ko.observableArray([]);
}

var InternalPartyContactsModel = function () {
    var self = this;

    self.CTRContactID = ko.observable('');
    self.CTRContactItemID = ko.observable('');

    self.INTPartyContactID = ko.observable('');
    self.INTPartyContactItemID = ko.observable('');

    self.INTPartyContactUserID = ko.observable('');
    self.INTPartyContactFirstName = ko.observable('');
    self.INTPartyContactLastName = ko.observable('');
    self.INTPartyContactDisplayName = ko.observable('');
}

var ExternalPartiesModel = function () {
    var self = this;
    self.EXTParties = ko.observableArray([]);
}

var ExternalPartyDetailsModel = function () {
    var self = this;

    self.CTRPartyID = ko.observable('');
    self.CTRPartyItemID = ko.observable('');
    self.CTRPartyName = ko.observable('');
    self.CTRPartyRelationType = ko.observable('');

    self.EXTPartyID = ko.observable('');
    self.EXTPartyItemID = ko.observable('');

    self.EXTPartyRegID = ko.observable('');
    self.EXTPartyName = ko.observable('');
    self.EXTPartyRegName = ko.observable('');

    self.EXTPartyContacts = ko.observableArray([]);
}

var ExternalPartyContactsModel = function () {
    var self = this;

    self.CTRContactID = ko.observable('');
    self.CTRContactItemID = ko.observable('');

    self.EXTPartyContactID = ko.observable('');
    self.EXTPartyContactItemID = ko.observable('');

    self.EXTPartyContactUserID = ko.observable('');
    self.EXTPartyContactFirstName = ko.observable('');
    self.EXTPartyContactLastName = ko.observable('');
    self.EXTPartyContactDisplayName = ko.observable('');
}

function addDataToINTPartiesView(iElementList, iModel) {
    iModel.INTParties.removeAll();
    if (iElementList) {
        if (Array.isArray(iElementList)) {
            for (var i = 0; i < iElementList.length; i++) {
                iModel.INTParties.push(_populateINTPartiesListData(iElementList[i]));
            }
        } else {

            iModel.INTParties.push(_populateINTPartiesListData(iElementList));
        }
    }
}

function _populateINTPartiesListData(iElement) {

    l_INTPartyDetails_Obj = new InternalPartyDetailsModel();
    l_INTPartyDetails_Obj.CTRPartyID(getTextValue(iElement['CTRParties-id'].Id));
    l_INTPartyDetails_Obj.CTRPartyItemID(getTextValue(iElement['CTRParties-id'].ItemId));

    l_INTPartyDetails_Obj.CTRPartyName(getTextValue(iElement.PartyName));
    l_INTPartyDetails_Obj.CTRPartyRelationType(getTextValue(iElement.RelationType));

    l_INTPartyDetails_Obj.INTPartyID(getTextValue(iElement.RelatedParty['Party-id'].Id));
    l_INTPartyDetails_Obj.INTPartyItemID(getTextValue(iElement.RelatedParty['Party-id'].ItemId));

    l_INTPartyDetails_Obj.INTPartyRegID(getTextValue(iElement.RelatedParty.RegistrationID));
    l_INTPartyDetails_Obj.INTPartyName(getTextValue(iElement.RelatedParty.Name));
    l_INTPartyDetails_Obj.INTPartyRegName(getTextValue(iElement.RelatedParty.RegisteredName));

    var l_INTPartyContacts = iElement.CTRContacts;
    if (l_INTPartyContacts) {
        if (Array.isArray(l_INTPartyContacts)) {
            for (var i = 0; i < l_INTPartyContacts.length; i++) {
                l_INTPartyDetails_Obj.INTPartyContacts.push(_populateINTPartiesContactsListData(l_INTPartyContacts[i]));
            }
        } else {

            l_INTPartyDetails_Obj.INTPartyContacts.push(_populateINTPartiesContactsListData(l_INTPartyContacts));
        }
    } else {
        l_INTPartyDetails_Obj.INTPartyContacts.push(_populateINTPartiesContactsListData(""));
    }
    return l_INTPartyDetails_Obj;
}

function _populateINTPartiesContactsListData(iElement) {

    var l_INTPartyContacts_Obj = new InternalPartyContactsModel();
    if (iElement != "") {
        l_INTPartyContacts_Obj.CTRContactID(getTextValue(iElement['CTRContacts-id'].Id));
        l_INTPartyContacts_Obj.CTRContactItemID(getTextValue(iElement['CTRContacts-id'].ItemId));

        l_INTPartyContacts_Obj.INTPartyContactID(getTextValue(iElement.RelatedContacts['RelatedContacts-id'].Id1));
        l_INTPartyContacts_Obj.INTPartyContactItemID(getTextValue(iElement.RelatedContacts['RelatedContacts-id'].ItemId1));

        l_INTPartyContacts_Obj.INTPartyContactUserID(getTextValue(iElement.RelatedContacts.ContainingPerson.DisplayName));
        l_INTPartyContacts_Obj.INTPartyContactFirstName(getTextValue(iElement.RelatedContacts.ContainingPerson.FirstName));
        l_INTPartyContacts_Obj.INTPartyContactLastName(getTextValue(iElement.RelatedContacts.ContainingPerson.LastName));
        l_INTPartyContacts_Obj.INTPartyContactDisplayName(getTextValue(iElement.RelatedContacts.ContainingPerson.User_ID));
    } 
    return l_INTPartyContacts_Obj;
}

function addDataToEXTPartiesView(iElementList, iModel) {
    iModel.EXTParties.removeAll();
    if (iElementList) {
        if (Array.isArray(iElementList)) {
            for (var i = 0; i < iElementList.length; i++) {
                iModel.EXTParties.push(_populateEXTPartiesListData(iElementList[i]));
            }
        } else {

            iModel.EXTParties.push(_populateEXTPartiesListData(iElementList));
        }
    }
}

function _populateEXTPartiesListData(iElement) {
    l_EXTPartyDetails_Obj = new ExternalPartyDetailsModel();
    l_EXTPartyDetails_Obj.CTRPartyID(getTextValue(iElement['CTRParties-id'].Id));
    l_EXTPartyDetails_Obj.CTRPartyItemID(getTextValue(iElement['CTRParties-id'].ItemId));

    l_EXTPartyDetails_Obj.CTRPartyName(getTextValue(iElement.PartyName));
    l_EXTPartyDetails_Obj.CTRPartyRelationType(getTextValue(iElement.RelationType));

    l_EXTPartyDetails_Obj.EXTPartyID(getTextValue(iElement.RelatedParty['Party-id'].Id));
    l_EXTPartyDetails_Obj.EXTPartyItemID(getTextValue(iElement.RelatedParty['Party-id'].ItemId));

    l_EXTPartyDetails_Obj.EXTPartyRegID(getTextValue(iElement.RelatedParty.RegistrationID));
    l_EXTPartyDetails_Obj.EXTPartyName(getTextValue(iElement.RelatedParty.Name));
    l_EXTPartyDetails_Obj.EXTPartyRegName(getTextValue(iElement.RelatedParty.RegisteredName));

    var l_EXTPartyContacts = iElement.CTRContacts;
    if (l_EXTPartyContacts) {
        if (Array.isArray(l_EXTPartyContacts)) {
            for (var i = 0; i < l_EXTPartyContacts.length; i++) {
                l_EXTPartyDetails_Obj.EXTPartyContacts.push(_populateEXTPartiesContactsListData(l_EXTPartyContacts[i]));
            }
        } else {

            l_EXTPartyDetails_Obj.EXTPartyContacts.push(_populateEXTPartiesContactsListData(l_EXTPartyContacts));
        }
    } else {
        l_EXTPartyDetails_Obj.EXTPartyContacts.push(_populateEXTPartiesContactsListData(""));
    }
    return l_EXTPartyDetails_Obj;
}

function _populateEXTPartiesContactsListData(iElement) {
    var l_EXTPartyContacts_Obj = new ExternalPartyContactsModel();
    if (iElement != "") {
        l_EXTPartyContacts_Obj.CTRContactID(getTextValue(iElement['CTRContacts-id'].Id));
        l_EXTPartyContacts_Obj.CTRContactItemID(getTextValue(iElement['CTRContacts-id'].ItemId));

        l_EXTPartyContacts_Obj.EXTPartyContactID(getTextValue(iElement.RelatedContacts['RelatedContacts-id'].Id1));
        l_EXTPartyContacts_Obj.EXTPartyContactItemID(getTextValue(iElement.RelatedContacts['RelatedContacts-id'].ItemId1));

        l_EXTPartyContacts_Obj.EXTPartyContactUserID(getTextValue(iElement.RelatedContacts.ContainingPerson.DisplayName));
        l_EXTPartyContacts_Obj.EXTPartyContactFirstName(getTextValue(iElement.RelatedContacts.ContainingPerson.FirstName));
        l_EXTPartyContacts_Obj.EXTPartyContactLastName(getTextValue(iElement.RelatedContacts.ContainingPerson.LastName));
        l_EXTPartyContacts_Obj.EXTPartyContactDisplayName(getTextValue(iElement.RelatedContacts.ContainingPerson.User_ID));
    }
    return l_EXTPartyContacts_Obj;
}

function ListAccounts() {
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/ContractNegotiation/19.4",
        method: "GetAccDatabyRelatedContentItemID",
        parameters: {
            "relatedContentItemID": instanceId,
        },
        success: function (data) {
            var l_accountData = data.tuple.old.GetAccDatabyRelatedContentItemID.GetAccDatabyRelatedContentItemID.AccountData;
            if (l_accountData) {
                addDataToINTPartiesView(l_accountData.IntParties.FindZ_INT_CTRPartiesListResponse.CTRParties, l_INTParties_model);
                addDataToEXTPartiesView(l_accountData.ExtParties.FindZ_INT_CTRPartiesListResponse.CTRParties, l_EXTParties_model);
            }
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving the parties. Contact your administrator."), 10000);
            return false;
        }
    });
}

$(document).ready(function () {

    if (window.parent.parent) {
		ExtContact_AccountsFrame = $('[src*="ExtContact_Accounts.htm"]', window.parent.parent.document);
		if (ExtContact_AccountsFrame) {
			ExtContact_AccountsFrame.css('border', 'none');
		}
	}

    instanceId = getUrlParameterValue("instanceId", null, true);
    l_INTParties_model = new InternalPartiesModel();
    ko.applyBindings(l_INTParties_model, document.getElementById("div_INTPartiesList"));

    l_EXTParties_model = new ExternalPartiesModel();
    ko.applyBindings(l_EXTParties_model, document.getElementById("div_EXTPartiesList"));

    ListAccounts();
});

