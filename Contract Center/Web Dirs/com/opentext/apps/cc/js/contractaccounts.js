$.cordys.json.defaults.removeNamespacePrefix = true;
var l_addedINTParties = {};
var l_addedINTPartyContacts = {};
var INTPartiesOffsetValue = 0;
var INTPartiesLimitValue = Number.MAX_VALUE;
var l_addedEXTParties = {};
var l_addedEXTPartyContacts = {};
var EXTPartiesOffsetValue = 0;
var EXTPartiesLimitValue = Number.MAX_VALUE;
var l_selectedPartyType = "INTERNAL";
var l_selectediPartyID;
var l_selectediIndex;
var l_selectediPartyType;
var InternalPartiesModel = function () {
    var self = this;
    self.INTParties = ko.observableArray([]);
	self.New_INTParties = ko.observableArray([]);
	self.Remove_INTParties = ko.observableArray([]);
	self.View_INTParties = ko.observableArray([]);
    self.currentPage = ko.observable(1);
    self.numOfINTParties = ko.observable('');
    self.numOfINTPartiesInCurrentPage = ko.observable('');
    self.numOfPages = ko.observable('');
    self.isFilterApplied = ko.observable(false);
    self.confirmINTPartyRemove = function (iItem, event) {
		
        if (iItem.INTPartyID()) {
			removePartyFromRow(iItem);
        }
        event.stopPropagation();
    }
}

var InternalPartiesFilterModel = function () {
    var self = this;
    var l_INTPartyRegIDFilterElement = document.getElementById("input_INTPartyRegIDFilter");
    var l_INTPartyNameFilterElement = document.getElementById("input_INTPartyNameFilter");
    //var l_INTPartyRegNameFilterElement = document.getElementById("input_INTPartyRegNameFilter");
    self.ClearINTPartiesFilter = function () {
        l_INTPartyRegIDFilterElement.value = "";
        l_INTPartyNameFilterElement.value = "";
        //l_INTPartyRegNameFilterElement.value = "";
    }
    self.getINTPartiesFilterObject = function () {
		l_INTPartyRegIDFilterElement = document.getElementById("input_INTPartyRegIDFilter");
		l_INTPartyNameFilterElement = document.getElementById("input_INTPartyNameFilter");
        self.CurrentFilterObject = {
            "partyName": l_INTPartyNameFilterElement ? l_INTPartyNameFilterElement.value : "",
            //"partyRegName": l_INTPartyRegNameFilterElement ? l_INTPartyRegNameFilterElement.value : "",
            "partyRegID": l_INTPartyRegIDFilterElement ? l_INTPartyRegIDFilterElement.value : "",
            "partyType": "INTERNAL",
            "contractID": contractID
         //   "offset": INTPartiesOffsetValue,
           // "limit": INTPartiesLimitValue,
        };
        return self.CurrentFilterObject;
    }
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
	self.New_INTPartyContacts = ko.observableArray([]);
	self.Remove_INTPartyContacts = ko.observableArray([]);

    self.confirmINTPartyContactRemove = function (iPartyItem,iItem, event) {
        if (iItem.INTPartyContactID()) {
            removePartyContact(iPartyItem, iItem, "INTERNAL");
        }
        event.stopPropagation();
    }

    self.addINTPartyContact = function (i_Item) {
        if (i_Item.INTPartyID()) {
            $('#div_addContacttoContractModal').modal({
                backdrop: 'static',
                keyboard: false
            })
            $("#input_contactListSearchFilter").val('');
			l_contract_accounts_model.l_addContacttoContractSelection_model.clearSelection();
            ListContactstoAdd(i_Item.INTPartyID(), l_contract_accounts_model.l_INTParties_model.New_INTParties.indexOf(i_Item), i_Item.CTRPartyRelationType());
            $('button#btn_selectContacttoAddYes').off("click");
			
			//nik old add contact
            // $('button#btn_selectContacttoAddYes').on('click', function (_event) {
                // $.cordys.ajax({
                    // namespace: "http://schemas/OpenTextContractCenter/CTRContacts/operations",
                    // method: "CreateCTRContacts",
                    // parameters: {
                        // "CTRContacts-create": {
                            // "RelatedContacts": {
                                // "RelatedContacts-id": {
                                    // "Id": i_Item.INTPartyID(),
                                    // "Id1": l_contract_accounts_model.l_addContacttoContractSelection_model.selectedContactID()
                                // },
                            // },
                            // "RelatedCTRParty": {
                                // "CTRParties-id": { "Id": i_Item.CTRPartyID() },
                            // },
                            // "RelatedToCTR": {
                                // "Contract-id": { "Id": contractID },
                            // }
                        // }
                    // },
                    // success: function (data) {
                        // l_contract_accounts_model.l_addContacttoContractSelection_model.clearSelection();
                        // $("#input_contactListSearchFilter").val('');
                        // successToast(3000, getTranslationMessage("Internal contact added"));
                        // listINTParties();
                        // UpdateNegotiationInstance();
                    // },
                    // error: function (responseFailure) {
                        // showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while adding a contact. Contact your administrator."), 10000);
                        // return false;
                    // }
                // });
                // l_addedINTPartyContacts = {};
                // l_addedEXTPartyContacts = {};
            // 
			$('button#btn_selectContacttoAddYes').on('click', function (_event) {
				iIndex= l_contract_accounts_model.l_INTParties_model.New_INTParties.indexOf(i_Item);
				if(i_Item.New_INTPartyContacts().length==1 && i_Item.New_INTPartyContacts()[0].INTPartyContactID().length==0){
					i_Item.New_INTPartyContacts.pop();
				}
				if(i_Item.Remove_INTPartyContacts().length){
					var removedContactFound=false;;
					i_Item.Remove_INTPartyContacts().some(function(INTPartyContact){
					if(INTPartyContact.INTPartyContactID()==l_contract_accounts_model.l_addContacttoContractSelection_model.selectedContactID()){
						i_Item.New_INTPartyContacts.push(Object.assign({}, INTPartyContact));
						i_Item.Remove_INTPartyContacts.remove(INTPartyContact);
						removedContactFound=true;
						return true;
					}	
					}); 
					if(!removedContactFound)
					{
						_populatePartiesContactsListData(l_contract_accounts_model.l_addContacttoContractSelection_model,"INTERNAL",i_Item);
					}
				}
				else{
					_populatePartiesContactsListData(l_contract_accounts_model.l_addContacttoContractSelection_model,"INTERNAL",i_Item);
				}
				$("#input_contactListSearchFilter").val('');
				l_contract_accounts_model.l_addContacttoContractSelection_model.clearSelection();
				INTParty_UpdatetPages();
				l_addedINTPartyContacts = {};
				l_addedEXTPartyContacts = {};
				trackChange("INTERNAL");
				$('.modal').modal('hide');
			});
        }
    };
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
	self.INTPartyContactMiddleName = ko.observable('');
    self.INTPartyFullName = ko.observable('');
    self.INTPartyContactEmail = ko.observable('');
    self.INTPartyContactPhone = ko.observable('');
	self.INTPartyContactMobile = ko.observable('');
	self.INTPartyStreetAddress = ko.observable('');
    self.INTPartyCity = ko.observable('');
    self.INTPartyState = ko.observable('');
    self.INTPartyCountry = ko.observable('');
	self.INTPartyPostalCode = ko.observable('');
	self.INTPartyFullAddress = ko.observable('');
}

var ExternalPartiesModel = function () {
    var self = this;
    self.EXTParties = ko.observableArray([]);
	self.New_EXTParties = ko.observableArray([]);
	self.Remove_EXTParties = ko.observableArray([]);
	self.View_EXTParties = ko.observableArray([]);
    self.currentPage = ko.observable(1);
    self.numOfEXTParties = ko.observable('');
    self.numOfEXTPartiesInCurrentPage = ko.observable('');
    self.numOfPages = ko.observable('');
    self.isFilterApplied = ko.observable(false);
    self.confirmEXTPartyRemove = function (iItem, event) {
        if (iItem.EXTPartyID()) {
			removePartyFromRow(iItem);
        }
        event.stopPropagation();
    }
}

var ExternalPartiesFilterModel = function () {
    var self = this;
    var l_EXTPartyRegIDFilterElement = document.getElementById("input_EXTPartyRegIDFilter");
    var l_EXTPartyNameFilterElement = document.getElementById("input_EXTPartyNameFilter");
    //var l_EXTPartyRegNameFilterElement = document.getElementById("input_EXTPartyRegNameFilter");
    self.ClearEXTPartiesFilter = function () {
        l_EXTPartyRegIDFilterElement.value = "";
        l_EXTPartyNameFilterElement.value = "";
        // l_EXTPartyRegNameFilterElement.value = "";
    }
    self.getEXTPartiesFilterObject = function () {
		l_EXTPartyRegIDFilterElement = document.getElementById("input_EXTPartyRegIDFilter");
		l_EXTPartyNameFilterElement = document.getElementById("input_EXTPartyNameFilter");
        self.CurrentFilterObject = {
            "partyName": l_EXTPartyNameFilterElement ? l_EXTPartyNameFilterElement.value : "",
            //"partyRegName": l_EXTPartyRegNameFilterElement ? l_EXTPartyRegNameFilterElement.value : "",
            "partyRegID": l_EXTPartyRegIDFilterElement ? l_EXTPartyRegIDFilterElement.value : "",
            "partyType": "EXTERNAL",
            "contractID": contractID
           // "offset": EXTPartiesOffsetValue,
          //  "limit": EXTPartiesLimitValue,
        };
        return self.CurrentFilterObject;
    }
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
	self.EXTPartyAcctMgrUserIds = ko.observable('');
	self.EXTPartyBusinessWorkspaceId = ko.observable('');

    self.EXTPartyContacts = ko.observableArray([]);
	self.New_EXTPartyContacts = ko.observableArray([]);
	self.Remove_EXTPartyContacts = ko.observableArray([]);

    self.confirmEXTPartyContactRemove = function (iPartyItem, iItem, event) {
        if (iItem.EXTPartyContactID()) {
            removePartyContact(iPartyItem, iItem, "EXTERNAL");
        }
        event.stopPropagation();
    }

    self.addEXTPartyContact = function (i_Item) {
        if (i_Item.EXTPartyID()) {
            $('#div_addContacttoContractModal').modal({
                backdrop: 'static',
                keyboard: false
            })
            $("#input_contactListSearchFilter").val('');
			l_contract_accounts_model.l_addContacttoContractSelection_model.clearSelection();
            ListContactstoAdd(i_Item.EXTPartyID(), l_contract_accounts_model.l_EXTParties_model.New_EXTParties.indexOf(i_Item), i_Item.CTRPartyRelationType());
            $('button#btn_selectContacttoAddYes').off("click");
			//nik old contact add
            // $('button#btn_selectContacttoAddYes').on('click', function (_event) {
                // $.cordys.ajax({
                    // namespace: "http://schemas/OpenTextContractCenter/CTRContacts/operations",
                    // method: "CreateCTRContacts",
                    // parameters: {
                        // "CTRContacts-create": {
                            // "RelatedContacts": {
                                // "RelatedContacts-id": {
                                    // "Id": i_Item.EXTPartyID(),
                                    // "Id1": l_contract_accounts_model.l_addContacttoContractSelection_model.selectedContactID()
                                // },
                            // },
                            // "RelatedCTRParty": {
                                // "CTRParties-id": { "Id": i_Item.CTRPartyID() },
                            // },
                            // "RelatedExtContactToCTR": {
                                // "Contract-id": { "Id": contractID },
                            // }
                        // }
                    // },
                    // success: function (data) {
                        // l_contract_accounts_model.l_addContacttoContractSelection_model.clearSelection();
                        // successToast(3000, getTranslationMessage("External contact added"));
                        // $("#input_contactListSearchFilter").val('');
                        // listEXTParties();
                        // UpdateNegotiationInstance();
                    // },
                    // error: function (responseFailure) {
                        // showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while adding a contact. Contact your administrator."), 10000);
                        // return false;
                    // }
                // });
                // l_addedEXTPartyContacts = {};
            // 
			$('button#btn_selectContacttoAddYes').on('click', function (_event) {
				iIndex = l_contract_accounts_model.l_EXTParties_model.New_EXTParties.indexOf(i_Item);
				if(i_Item.New_EXTPartyContacts().length==1 && i_Item.New_EXTPartyContacts()[0].EXTPartyContactID().length==0){
					i_Item.New_EXTPartyContacts.pop();
				}
				if(i_Item.Remove_EXTPartyContacts().length){
					var removedContactFound=false;;
					i_Item.Remove_EXTPartyContacts().some(function(EXTPartyContact){
					if(EXTPartyContact.EXTPartyContactID()==l_contract_accounts_model.l_addContacttoContractSelection_model.selectedContactID()){
						i_Item.New_EXTPartyContacts.push(Object.assign({}, EXTPartyContact));
						i_Item.Remove_EXTPartyContacts.remove(EXTPartyContact);
						removedContactFound=true;
						return true;
					}	
					}); 
					if(!removedContactFound)
					{
						_populatePartiesContactsListData(l_contract_accounts_model.l_addContacttoContractSelection_model,"EXTERNAL",i_Item);
					}
				}
				else{
					_populatePartiesContactsListData(l_contract_accounts_model.l_addContacttoContractSelection_model,"EXTERNAL",i_Item);
				}
				$("#input_contactListSearchFilter").val('');
				l_contract_accounts_model.l_addContacttoContractSelection_model.clearSelection();
				EXTParty_UpdatetPages();
				l_addedINTPartyContacts = {};
				l_addedEXTPartyContacts = {};
				trackChange("EXTERNAL");
				$('.modal').modal('hide');
			});
        }
    };
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
	self.EXTPartyContactMiddleName = ko.observable('');
    self.EXTPartyFullName = ko.observable('');
    self.EXTPartyContactEmail = ko.observable('');
    self.EXTPartyContactPhone = ko.observable('');
	self.EXTPartyContactMobile = ko.observable('');
	self.EXTPartyStreetAddress = ko.observable('');
    self.EXTPartyCity = ko.observable('');
    self.EXTPartyState = ko.observable('');
    self.EXTPartyCountry = ko.observable('');
	self.EXTPartyPostalCode = ko.observable('');
	self.EXTPartyFullAddress = ko.observable('');
}

var PartiesToAddModel = function () {
    var self = this;
    self.PartiestoAdd = ko.observableArray([]);
    self.totalNoOfParties = ko.observable('');
    self.selectedPartyID = ko.observable('');
    self.selectedPartyName = ko.observable('');
	self.selectedPartyAcctMgrUserIds = ko.observable('');
	self.selectedPartyBWId = ko.observable('');

    self.togglePartiestoAddFilter = function(iEventObject) {
        if ($("#div_partiestoAddFilter").attr('apps-toggle') == "expanded") {
            $("#div_partiestoAddFilter").toggle();
            document.getElementById("div_partiestoAddFilter").setAttribute("apps-toggle", 'collapsed');
            $("#div_partyList").removeClass("col-md-9");
            $("#div_partyList").addClass("col-md-12");
        }
        else if ($("#div_partiestoAddFilter").attr('apps-toggle') == "collapsed") {
            $("#div_partiestoAddFilter").toggle();
            document.getElementById("div_partiestoAddFilter").setAttribute("apps-toggle", 'expanded');
            $("#div_partyList").removeClass("col-md-12");
            $("#div_partyList").addClass("col-md-9");
        }
    };

    self.ClearPartiestoAddFilter = function () {
        $("#input_partiestoAddNameFilter").val(''),
        $("#input_partiestoAddRegIDFilter").val('')
    }

    self.selectPartyRadioButton = function (iItem, event) {
        $(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
        if (iItem["Party-id"]) {
            var l_Id = iItem["Party-id"].Id;
            self.selectedPartyID(l_Id);
            self.selectedPartyName(iItem.Name);
			self.selectedPartyAcctMgrUserIds(iItem.Z_INT_ManagerUserId!=null?iItem.Z_INT_ManagerUserId:'');
			self.selectedPartyBWId(iItem.BW_ID!=null?iItem.BW_ID:'');
        }
    }
    self.onPartyRowRadioButtonValueChanged = function (iItem, event) {
        $(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).addClass("cc-radio-on");
        if (iItem["Party-id"]) {
            var l_Id = iItem["Party-id"].Id;
            self.selectedPartyID(l_Id);
            self.selectedPartyName(iItem.Name);
			self.selectedPartyAcctMgrUserIds(iItem.Z_INT_ManagerUserId!=null?iItem.Z_INT_ManagerUserId:'');
			self.selectedPartyBWId(iItem.BW_ID!=null?iItem.BW_ID:'');
        }
        event.stopPropagation();
    }
    self.clearSelection = function () {
        self.selectedPartyID("");
        self.selectedPartyName("");
		self.selectedPartyAcctMgrUserIds("");
		self.selectedPartyBWId("");
    }
}

var ContactsToAddModel = function () {
    var self = this;
    self.ContactstoAdd = ko.observableArray([]);
    self.totalNoOfContacts = ko.observable('');
    self.selectedContactID = ko.observable('');
	self.selectedContactUserID = ko.observable('');
    self.selectedContactName = ko.observable('');
	self.selectedContactFirstName = ko.observable('');
	self.selectedContactLastName = ko.observable('');
	self.selectedContactEmail = ko.observable('');
	self.selectedContactPhone = ko.observable('');
	self.selectedContactStreetAddress = ko.observable('');
	self.selectedContactCity = ko.observable('');
	self.selectedContactState = ko.observable('');
	self.selectedContactCountry = ko.observable('');
	self.selectedContactPostalCode = ko.observable('');
	self.selectedContactFullAddress = ko.observable('');

    self.selectContactRadioButton = function (iItem, event) {
        $(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
        if (iItem["RelatedContacts-id"]) {
            var l_Id = iItem["RelatedContacts-id"].Id1;
            self.selectedContactID(l_Id);
			fillContactData(iItem);
        }
    }
    self.onContactRowRadioButtonValueChanged = function (iItem, event) {
        $(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).addClass("cc-radio-on");
        if (iItem["RelatedContacts-id"]) {
            var l_Id = iItem["RelatedContacts-id"].Id1;
            self.selectedContactID(l_Id);
            fillContactData(iItem);
        }
        event.stopPropagation();
    }
    self.clearSelection = function () {
        self.selectedContactID("");
        self.selectedContactName("");
    }
	
	function fillContactData(iItem){
		  self.selectedContactName(getTextValue(iItem.ContainingPerson.DisplayName));
			self.selectedContactUserID(getTextValue(iItem.ContainingPerson.User_ID));
			self.selectedContactFirstName(getTextValue(iItem.ContainingPerson.FirstName));
			self.selectedContactLastName(getTextValue(iItem.ContainingPerson.LastName));
			self.selectedContactEmail(getTextValue(iItem.ContainingPerson.Email));
			self.selectedContactPhone(getTextValue(iItem.ContainingPerson.Phone));
			if(iItem.ContainingPerson.WorkAddress){
				self.selectedContactStreetAddress(getTextValue(iItem.ContainingPerson.WorkAddress.StreetAddress?iItem.ContainingPerson.WorkAddress.StreetAddress:""));
				self.selectedContactCity(getTextValue(iItem.ContainingPerson.WorkAddress.City?iItem.ContainingPerson.WorkAddress.City:""));
				self.selectedContactState(getTextValue(iItem.ContainingPerson.WorkAddress.ToState?iItem.ContainingPerson.WorkAddress.ToState.Name:""));
				self.selectedContactCountry(getTextValue(iItem.ContainingPerson.WorkAddress.ToCountry?iItem.ContainingPerson.WorkAddress.ToCountry.Country_Name:""));
				self.selectedContactPostalCode(getTextValue(iItem.ContainingPerson.WorkAddress.PostalCode?iItem.ContainingPerson.WorkAddress.PostalCode:""));
				self.selectedContactFullAddress((self.selectedContactStreetAddress()!=""?(self.selectedContactStreetAddress()+", "):"")+(self.selectedContactCity()!=""?(self.selectedContactCity()+", "):"")+(self.selectedContactState()!=""?(self.selectedContactState()+", "):"")+self.selectedContactCountry()+"\n"+self.selectedContactPostalCode());
			}
			else{
				self.selectedContactFullAddress("");
			}
	}
}

function addDataToINTPartiesView(iElementList, iModel) {
    iModel.INTParties.removeAll();
	iModel.New_INTParties.removeAll();
    if (iElementList) {
        if (Array.isArray(iElementList)) {
            for (var i = 0; i < iElementList.length; i++) {
                iModel.INTParties.push(_populateINTPartiesListData(iElementList[i]));
            }
        } else {

            iModel.INTParties.push(_populateINTPartiesListData(iElementList));
        }
		iModel.New_INTParties(iModel.INTParties.slice());
    }
}

function addPartyToPartiesView(iElement, iModel,i_PartyType) {
	
    if (iElement) {
		if(i_PartyType=="INTERNAL"){
		iModel.New_INTParties.push(_populatePartyData(iElement,i_PartyType));
		}
		else if(i_PartyType=="EXTERNAL"){
		iModel.New_EXTParties.push(_populatePartyData(iElement,i_PartyType));
		}
    }
	
		
		
}

function removePartyFromPartiesView(iElement, iModel,i_PartyType) {
	
    if (iElement) {
		if(i_PartyType=="INTERNAL"){
		iModel.New_INTParties.push(_populatePartyData(iElement,i_PartyType));
		}
		else if(i_PartyType=="EXTERNAL"){
		iModel.New_EXTParties.push(_populatePartyData(iElement,i_PartyType));
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
	l_INTPartyDetails_Obj.New_INTPartyContacts(l_INTPartyDetails_Obj.INTPartyContacts.slice());
    return l_INTPartyDetails_Obj;
}

function _populatePartyData(iElement,i_PartyType) {
	
	if(i_PartyType=="INTERNAL"){
		l_INTPartyDetails_Obj = new InternalPartyDetailsModel();
		
		l_INTPartyDetails_Obj.INTPartyID(iElement.selectedPartyID());
		l_INTPartyDetails_Obj.CTRPartyRelationType(i_PartyType);
		l_INTPartyDetails_Obj.INTPartyName(iElement.selectedPartyName());
		var l_INTPartyContacts = iElement.CTRContacts;
        l_INTPartyDetails_Obj.New_INTPartyContacts.push(_populateINTPartiesContactsListData(""));
    
		return l_INTPartyDetails_Obj;
	}
	else if(i_PartyType=="EXTERNAL"){
		l_EXTPartyDetails_Obj = new ExternalPartyDetailsModel();
   
		l_EXTPartyDetails_Obj.EXTPartyID(iElement.selectedPartyID());
		l_EXTPartyDetails_Obj.CTRPartyRelationType(i_PartyType);
		l_EXTPartyDetails_Obj.EXTPartyName(iElement.selectedPartyName());
		l_EXTPartyDetails_Obj.EXTPartyAcctMgrUserIds(iElement.selectedPartyAcctMgrUserIds());
		l_EXTPartyDetails_Obj.EXTPartyBusinessWorkspaceId(iElement.selectedPartyBWId());
		var l_EXTPartyContacts = iElement.CTRContacts;
   
        l_EXTPartyDetails_Obj.New_EXTPartyContacts.push(_populateEXTPartiesContactsListData(""));
    
		return l_EXTPartyDetails_Obj;
	}
	return;
}

function _populateINTPartiesContactsListData(iElement) {

    var l_INTPartyContacts_Obj = new InternalPartyContactsModel();
    if (iElement != "") {
        l_INTPartyContacts_Obj.CTRContactID(getTextValue(iElement['CTRContacts-id'].Id));
        l_INTPartyContacts_Obj.CTRContactItemID(getTextValue(iElement['CTRContacts-id'].ItemId));

        l_INTPartyContacts_Obj.INTPartyContactID(getTextValue(iElement.RelatedContacts['RelatedContacts-id'].Id1));
        l_INTPartyContacts_Obj.INTPartyContactItemID(getTextValue(iElement.RelatedContacts['RelatedContacts-id'].ItemId1));

        l_INTPartyContacts_Obj.INTPartyContactUserID(getTextValue(iElement.RelatedContacts.ContainingPerson.User_ID));
        l_INTPartyContacts_Obj.INTPartyContactFirstName(getTextValue(iElement.RelatedContacts.ContainingPerson.FirstName));
        l_INTPartyContacts_Obj.INTPartyContactLastName(getTextValue(iElement.RelatedContacts.ContainingPerson.LastName));
        l_INTPartyContacts_Obj.INTPartyContactDisplayName(getTextValue(iElement.RelatedContacts.ContainingPerson.DisplayName));
		l_INTPartyContacts_Obj.INTPartyContactMiddleName(getTextValue(iElement.RelatedContacts.ContainingPerson.MiddleName));
		l_INTPartyContacts_Obj.INTPartyContactEmail(getTextValue(iElement.RelatedContacts.ContainingPerson.Email));
		l_INTPartyContacts_Obj.INTPartyContactPhone(getTextValue(iElement.RelatedContacts.ContainingPerson.Phone));
		l_INTPartyContacts_Obj.INTPartyContactMobile(getTextValue(iElement.RelatedContacts.ContainingPerson.Mobile));
		l_INTPartyContacts_Obj.INTPartyFullName(l_INTPartyContacts_Obj.INTPartyContactFirstName()+" "+l_INTPartyContacts_Obj.INTPartyContactLastName());
		if(iElement.RelatedContacts.ContainingPerson.WorkAddress){
			l_INTPartyContacts_Obj.INTPartyStreetAddress(getTextValue(iElement.RelatedContacts.ContainingPerson.WorkAddress.StreetAddress?iElement.RelatedContacts.ContainingPerson.WorkAddress.StreetAddress:""));
			l_INTPartyContacts_Obj.INTPartyCity(getTextValue(iElement.RelatedContacts.ContainingPerson.WorkAddress.City?iElement.RelatedContacts.ContainingPerson.WorkAddress.City:""));
			l_INTPartyContacts_Obj.INTPartyState(getTextValue(iElement.RelatedContacts.ContainingPerson.WorkAddress.ToState?iElement.RelatedContacts.ContainingPerson.WorkAddress.ToState.Name:""));
			l_INTPartyContacts_Obj.INTPartyCountry(getTextValue(iElement.RelatedContacts.ContainingPerson.WorkAddress.ToCountry?iElement.RelatedContacts.ContainingPerson.WorkAddress.ToCountry.Country_Name:""));
			l_INTPartyContacts_Obj.INTPartyPostalCode(getTextValue(iElement.RelatedContacts.ContainingPerson.WorkAddress.PostalCode?iElement.RelatedContacts.ContainingPerson.WorkAddress.PostalCode:""));
			l_INTPartyContacts_Obj.INTPartyFullAddress(l_INTPartyContacts_Obj.INTPartyStreetAddress()+(l_INTPartyContacts_Obj.INTPartyStreetAddress()!=""?", ":"")+l_INTPartyContacts_Obj.INTPartyCity()+(l_INTPartyContacts_Obj.INTPartyCity()!=""?", ":"")+l_INTPartyContacts_Obj.INTPartyState()+(l_INTPartyContacts_Obj.INTPartyState()!=""?", ":"")+l_INTPartyContacts_Obj.INTPartyCountry()+"\n"+l_INTPartyContacts_Obj.INTPartyPostalCode());
		}
    } else {
        l_INTPartyContacts_Obj.INTPartyContactFirstName(getTextValue("Select a contact"));

    }
    return l_INTPartyContacts_Obj;
}

function _populatePartiesContactsListData(iElement,i_PartyType,i_partyItem) {
	if(i_PartyType=="INTERNAL"){
		var l_INTPartyContacts_Obj = new InternalPartyContactsModel();
		l_INTPartyContacts_Obj.INTPartyContactID(iElement.selectedContactID());
		
		l_INTPartyContacts_Obj.INTPartyContactUserID(iElement.selectedContactUserID());
		l_INTPartyContacts_Obj.INTPartyContactDisplayName(iElement.selectedContactName());
		l_INTPartyContacts_Obj.INTPartyContactFirstName(iElement.selectedContactFirstName());
		l_INTPartyContacts_Obj.INTPartyContactLastName(iElement.selectedContactLastName());
		l_INTPartyContacts_Obj.INTPartyContactEmail(iElement.selectedContactEmail());
		l_INTPartyContacts_Obj.INTPartyContactPhone(iElement.selectedContactPhone());
		l_INTPartyContacts_Obj.INTPartyFullName(iElement.selectedContactFirstName()+" "+iElement.selectedContactLastName());
		l_INTPartyContacts_Obj.INTPartyFullAddress(iElement.selectedContactFullAddress());

		i_partyItem.New_INTPartyContacts.push(l_INTPartyContacts_Obj);
		
	}
	else if(i_PartyType=="EXTERNAL"){
		 var l_EXTPartyContacts_Obj = new ExternalPartyContactsModel();
   
        l_EXTPartyContacts_Obj.EXTPartyContactID(iElement.selectedContactID());
		
		l_EXTPartyContacts_Obj.EXTPartyContactUserID(iElement.selectedContactUserID());
		l_EXTPartyContacts_Obj.EXTPartyContactDisplayName(iElement.selectedContactName());
        l_EXTPartyContacts_Obj.EXTPartyContactFirstName(iElement.selectedContactFirstName());
		l_EXTPartyContacts_Obj.EXTPartyContactLastName(iElement.selectedContactLastName());
		l_EXTPartyContacts_Obj.EXTPartyContactEmail(iElement.selectedContactEmail());
		l_EXTPartyContacts_Obj.EXTPartyContactPhone(iElement.selectedContactPhone());
		l_EXTPartyContacts_Obj.EXTPartyFullName(iElement.selectedContactFirstName()+" "+iElement.selectedContactLastName());
		l_EXTPartyContacts_Obj.EXTPartyFullAddress(iElement.selectedContactFullAddress());
		
		i_partyItem.New_EXTPartyContacts.push(l_EXTPartyContacts_Obj);
	}
}

function updateINTPartyPaginationParams() {
    if (l_contract_accounts_model.l_INTParties_model.currentPage() == 1) {
        document.getElementById("li_INTPartiesLeftNavigation").style.display = "none";
        document.getElementById("li_INTPartiesRightNavigation").style.display = "inline";
    }
	else if (l_contract_accounts_model.l_INTParties_model.currentPage() < Math.ceil(l_contract_accounts_model.l_INTParties_model.numOfINTParties() / INTPartiesLimitValue)) {
        document.getElementById("li_INTPartiesRightNavigation").style.display = "inline";
    }
    if (parseInt(l_contract_accounts_model.l_INTParties_model.numOfINTParties()) <= parseInt(INTPartiesLimitValue)) {
        l_contract_accounts_model.l_INTParties_model.currentPage('1');
        $('#li_INTPartiesLeftNavigation,#li_INTPartiesRightNavigation').css('display', 'none');
    }
	
}

function updateINTPartyLimitValue(iElement) {
    INTPartiesOffsetValue = 0;
    l_contract_accounts_model.l_INTParties_model.currentPage('1');
    INTPartiesLimitValue = $(iElement).val();
    //listINTParties();
	INTParty_UpdatetPages();
}

function INTParty_goToPreviousPage() {
    if (l_contract_accounts_model.l_INTParties_model.currentPage() > 1) {
        INTPartiesOffsetValue = parseInt(INTPartiesOffsetValue) - parseInt(INTPartiesLimitValue);
        l_contract_accounts_model.l_INTParties_model.currentPage(parseInt(l_contract_accounts_model.l_INTParties_model.currentPage()) - 1);
    }
    if (l_contract_accounts_model.l_INTParties_model.currentPage() < Math.ceil(l_contract_accounts_model.l_INTParties_model.numOfINTParties() / INTPartiesLimitValue)) {
        document.getElementById("li_INTPartiesRightNavigation").style.removeProperty("display");
    }
    if (l_contract_accounts_model.l_INTParties_model.currentPage() == 1) {
        document.getElementById("li_INTPartiesLeftNavigation").style.display = "none";
    }
    if (l_contract_accounts_model.l_INTParties_model.currentPage() < 1)
        return;
   // listINTParties();
   INTParty_UpdatetPages();
}

function INTParty_goToNextPage() {
    if (l_contract_accounts_model.l_INTParties_model.currentPage() < Math.ceil(l_contract_accounts_model.l_INTParties_model.numOfINTParties() / INTPartiesLimitValue)) {
        INTPartiesOffsetValue = parseInt(INTPartiesOffsetValue) + parseInt(INTPartiesLimitValue);
        l_contract_accounts_model.l_INTParties_model.currentPage(isNaN(parseInt(l_contract_accounts_model.l_INTParties_model.currentPage())) ? 0 : parseInt(l_contract_accounts_model.l_INTParties_model.currentPage()));
        l_contract_accounts_model.l_INTParties_model.currentPage(parseInt(l_contract_accounts_model.l_INTParties_model.currentPage()) + 1);
    }
    if (l_contract_accounts_model.l_INTParties_model.currentPage() == Math.ceil(l_contract_accounts_model.l_INTParties_model.numOfINTParties() / INTPartiesLimitValue)) {
        document.getElementById("li_INTPartiesRightNavigation").style.display = "none";
    }
    if (l_contract_accounts_model.l_INTParties_model.currentPage() > 1) {
        document.getElementById("li_INTPartiesLeftNavigation").style.removeProperty("display");
    }
    //listINTParties();
	INTParty_UpdatetPages();
}

function INTParty_goToLastPage() {
    INTPartiesOffsetValue = (Math.ceil(l_contract_accounts_model.l_INTParties_model.numOfINTParties() / INTPartiesLimitValue) - 1) * INTPartiesLimitValue;
    l_contract_accounts_model.l_INTParties_model.currentPage(Math.ceil(l_contract_accounts_model.l_INTParties_model.numOfINTParties() / INTPartiesLimitValue));
    $('#li_INTPartiesRightNavigation').css('display', 'none');
    $('#li_INTPartiesLeftNavigation').css('display', 'inline');
    //listINTParties();
	INTParty_UpdatetPages();
}

function INTParty_goToFirstPage() {
    INTPartiesOffsetValue = 0;
    l_contract_accounts_model.l_INTParties_model.currentPage('1');
    $('#li_INTPartiesRightNavigation').css('display', 'inline');
    $('#li_INTPartiesLeftNavigation').css('display', 'none');
   // listINTParties()
   INTParty_UpdatetPages();
}

function listINTParties(updateAccounts) {
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
        method: "getPartieswithFilters",
        parameters: l_contract_accounts_model.l_INTPartiesFilter_model.getINTPartiesFilterObject(),
        success: function (data) {
            addDataToINTPartiesView(data.parties.FindZ_INT_CTRPartiesListResponse.CTRParties, l_contract_accounts_model.l_INTParties_model);
            if (undefined != data.parties.FindZ_INT_CTRPartiesListResponse['@total']) {
                l_contract_accounts_model.l_INTParties_model.numOfINTParties(data.parties.FindZ_INT_CTRPartiesListResponse['@total']);
            } else {
                l_contract_accounts_model.l_INTParties_model.numOfINTParties(0);
            }
            if (l_contract_accounts_model.l_INTParties_model.numOfINTParties() != 0) {
                l_contract_accounts_model.l_INTParties_model.numOfPages(Math.ceil(l_contract_accounts_model.l_INTParties_model.numOfINTParties() / INTPartiesLimitValue));
            } else {
                l_contract_accounts_model.l_INTParties_model.numOfPages(1);
            }
			l_contract_accounts_model.INTDataLoaded(true);
			if( typeof updateAccounts == "function" )
				updateAccounts();
            INTParty_UpdatetPages();
			
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving the parties. Contact your administrator."), 10000);
            return false;
        }
    });
}

function listALLParties() {
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
        method: "getPartieswithFilters",
        parameters: l_contract_accounts_model.l_INTPartiesFilter_model.getINTPartiesFilterObject(),
        success: function (data) {
            addDataToINTPartiesView(data.parties.FindZ_INT_CTRPartiesListResponse.CTRParties, l_contract_accounts_model.l_INTParties_model);
            if (undefined != data.parties.FindZ_INT_CTRPartiesListResponse['@total']) {
                l_contract_accounts_model.l_INTParties_model.numOfINTParties(data.parties.FindZ_INT_CTRPartiesListResponse['@total']);
            } else {
                l_contract_accounts_model.l_INTParties_model.numOfINTParties(0);
            }
            if (l_contract_accounts_model.l_INTParties_model.numOfINTParties() != 0) {
                l_contract_accounts_model.l_INTParties_model.numOfPages(Math.ceil(l_contract_accounts_model.l_INTParties_model.numOfINTParties() / INTPartiesLimitValue));
            } else {
                l_contract_accounts_model.l_INTParties_model.numOfPages(1);
            }
			l_contract_accounts_model.INTDataLoaded(true);
            INTParty_UpdatetPages();
			listEXTParties();
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving the parties. Contact your administrator."), 10000);
            return false;
        }
    });
}

function hideINTPartiesListFilter() {
    $("#div_INTPartiesListFilter").hide();
    document.getElementById("div_INTPartiesListFilter").setAttribute("apps-toggle", 'collapsed');
    $("#div_INTPartiesList").removeClass("col-md-9");
    $("#div_INTPartiesList").addClass("col-md-12");
}

function ApplyFilterOnINTPartiesList(event, iSrcElement) {
	markPartiesToAlreadyAdded("INTERNAL");
    if (document.getElementById("input_INTPartyRegIDFilter").value != "" || document.getElementById("input_INTPartyNameFilter").value != "" || document.getElementById("input_INTPartyRegNameFilter").value != "") {
        $("#btn_clearINTFilterActionBar").css('display', 'inline');
        l_contract_accounts_model.l_INTParties_model.isFilterApplied(true);
    } else {
        $("#btn_clearINTFilterActionBar").css('display', 'none');
        l_contract_accounts_model.l_INTParties_model.isFilterApplied(false);
    }
    listINTParties();
    hideINTPartiesListFilter();
}

function ClearINTPartiesListFilter(event, iSrcElement) {
    l_contract_accounts_model.l_INTPartiesFilter_model.ClearINTPartiesFilter();
    $("#btn_clearINTFilterActionBar").css('display', 'none');
    l_contract_accounts_model.l_INTParties_model.isFilterApplied(false);
    listINTParties();
    hideINTPartiesListFilter();
}

function openAddPartyForm(i_PartyType) {
    l_selectedPartyType = i_PartyType;
    $('#div_addPartytoContractModal').modal({
        backdrop: 'static',
        keyboard: false
    })
	l_contract_accounts_model.l_addPartytoContractSelection_model.clearSelection();
    ListPartiestoAdd(i_PartyType);
    $('button#btn_selectPartytoAddYes').off("click");
	//Nik old add party
    // $('button#btn_selectPartytoAddYes').on('click', function (_event) {
        // $.cordys.ajax({
            // namespace: "http://schemas/OpenTextContractCenter/CTRParties/operations",
            // method: "CreateCTRParties",
            // parameters: getCreateCTRPartiesObj(i_PartyType),
            // success: function (data) {
                // l_contract_accounts_model.l_addPartytoContractSelection_model.clearSelection();
                // $("#input_partyListSearchFilter").val('');
                // successToast(3000, getTranslationMessage(i_PartyType == "INTERNAL" ? "Internal account added" : "External account added"));
                // listINTParties();
                // listEXTParties();
                // UpdateNegotiationInstance();
            // },
            // error: function (responseFailure) {
                // showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while adding a party. Contact your administrator."), 10000);
                // return false;
            // }
        // });
        // l_addedINTParties = {};
        // l_addedEXTParties = {};
    // });
	$('button#btn_selectPartytoAddYes').on('click', function (_event) {
		if(i_PartyType == "INTERNAL"){
			
				if(l_contract_accounts_model.l_INTParties_model.Remove_INTParties().length){
					var removedPartyFound=false;
					 l_contract_accounts_model.l_INTParties_model.Remove_INTParties().some(function(INTParty){
					if(INTParty.INTPartyID()==l_contract_accounts_model.l_addPartytoContractSelection_model.selectedPartyID()){
						l_contract_accounts_model.l_INTParties_model.New_INTParties.push(Object.assign({}, INTParty));
						l_contract_accounts_model.l_INTParties_model.Remove_INTParties.remove(INTParty);
						removedPartyFound=true;
						return true;
					}
					 }); 
					if(!removedPartyFound){
					addPartyToPartiesView(l_contract_accounts_model.l_addPartytoContractSelection_model, l_contract_accounts_model.l_INTParties_model,i_PartyType);
					}
				 }
				 else{
					addPartyToPartiesView(l_contract_accounts_model.l_addPartytoContractSelection_model, l_contract_accounts_model.l_INTParties_model,i_PartyType);
				}
			
			INTParty_UpdatetPages("ADD");
			trackChange(i_PartyType);
		}
		else if(i_PartyType == "EXTERNAL"){
			
				if(l_contract_accounts_model.l_EXTParties_model.Remove_EXTParties().length){
					var removedPartyFound=false;
					 l_contract_accounts_model.l_EXTParties_model.Remove_EXTParties().some(function(EXTParty){
					if(EXTParty.EXTPartyID()==l_contract_accounts_model.l_addPartytoContractSelection_model.selectedPartyID()){
						l_contract_accounts_model.l_EXTParties_model.New_EXTParties.push(Object.assign({}, EXTParty));
						l_contract_accounts_model.l_EXTParties_model.Remove_EXTParties.remove(EXTParty);
						removedPartyFound=true;
						return true;
					} 
				 } );
				 if(!removedPartyFound){
						addPartyToPartiesView(l_contract_accounts_model.l_addPartytoContractSelection_model, l_contract_accounts_model.l_EXTParties_model,i_PartyType);
				 }
				}
				else{
						addPartyToPartiesView(l_contract_accounts_model.l_addPartytoContractSelection_model, l_contract_accounts_model.l_EXTParties_model,i_PartyType);
				}
			
			EXTParty_UpdatetPages("ADD");
			trackChange(i_PartyType);
		}

		ClearPartiestoAddListFilter();
		l_contract_accounts_model.l_addPartytoContractSelection_model.clearSelection();
        
		l_addedINTParties = {};
        l_addedEXTParties = {};
		
		$('.modal').modal('hide');
	});
}

function getCreateCTRPartiesObj(i_PartyType) {
    var createCTRPartiesObj;
    if (i_PartyType == "INTERNAL") {
        createCTRPartiesObj = {
            "CTRParties-create": {
                "PartyName": l_contract_accounts_model.l_addPartytoContractSelection_model.selectedPartyName(),
                "RelationType": i_PartyType,
                "RelatedParty": {
                    "Party-id": { "Id": l_contract_accounts_model.l_addPartytoContractSelection_model.selectedPartyID() },
                },
                "RelatedToCTR": {
                    "Contract-id": { "Id": contractID },
                }
            }
        }

    } else if (i_PartyType == "EXTERNAL") {
        createCTRPartiesObj = {
            "CTRParties-create": {
                "PartyName": l_contract_accounts_model.l_addPartytoContractSelection_model.selectedPartyName(),
                "RelationType": i_PartyType,
                "RelatedParty": {
                    "Party-id": { "Id": l_contract_accounts_model.l_addPartytoContractSelection_model.selectedPartyID() },
                },
                "RelatedExtPartyToCTR": {
                    "Contract-id": { "Id": contractID },
                }
            }
        }
    }
    return createCTRPartiesObj;
}

function getAccountsSaveObj() {
    var createCTRPartiesObj;
    
        createCTRPartiesObj = {
			
          
                "contractID": contractID,
                "allAccounts": {
					"@xmlns": "http://schemas.opentext.com/apps/contractcenter/16.3",
					"addInternalParties":{
						"internalParty":getAllPartiesData(l_contract_accounts_model.l_INTParties_model.New_INTParties(),"INTERNAL")
					},
					"removeInternalParties":{
						"internalParty":getAllPartiesData(l_contract_accounts_model.l_INTParties_model.Remove_INTParties(),"INTERNAL")
					},
					"addExternalParties":{
						"externalParty":getAllPartiesData(l_contract_accounts_model.l_EXTParties_model.New_EXTParties(),"EXTERNAL")
					},
					"removeExternalParties":{
						"externalParty":getAllPartiesData(l_contract_accounts_model.l_EXTParties_model.Remove_EXTParties(),"EXTERNAL")
					}
                }
            
        }

    
    return createCTRPartiesObj;
}

function getAllPartiesData(parties,i_PartyType){
	var allParties=[];
	if(i_PartyType=="INTERNAL"){
		parties.forEach(function (party) {
			var partyDetails;
			partyDetails={
				CTRPartyID:party.CTRPartyID(),
				CTRPartyItemID:party.CTRPartyItemID(),
				CTRPartyName:party.CTRPartyName(),
				CTRPartyRelationType:party.CTRPartyRelationType(),
				INTPartyID:party.INTPartyID(),
				INTPartyItemID:party.INTPartyItemID(),
				INTPartyRegID:party.INTPartyRegID(),
				INTPartyName:party.INTPartyName(),
				INTPartyRegName:party.INTPartyRegName(),
				INTPartyContacts:party.INTPartyContacts(),
				New_INTPartyContacts:party.New_INTPartyContacts(),
				Remove_INTPartyContacts:party.Remove_INTPartyContacts()	
			}
			allParties.push(partyDetails);
		});
		
	}else if(i_PartyType=="EXTERNAL"){
		parties.forEach(function (party) {
			var partyDetails;
			partyDetails={
				CTRPartyID:party.CTRPartyID(),
				CTRPartyItemID:party.CTRPartyItemID(),
				CTRPartyName:party.CTRPartyName(),
				CTRPartyRelationType:party.CTRPartyRelationType(),
				EXTPartyID:party.EXTPartyID(),
				EXTPartyItemID:party.EXTPartyItemID(),
				EXTPartyRegID:party.EXTPartyRegID(),
				EXTPartyName:party.EXTPartyName(),
				EXTPartyRegName:party.EXTPartyRegName(),
				EXTPartyAcctMgrUserIds:party.EXTPartyAcctMgrUserIds(),
				EXTPartyBusinessWorkspaceId:party.EXTPartyBusinessWorkspaceId(),
				EXTPartyContacts:party.EXTPartyContacts(),
				New_EXTPartyContacts:party.New_EXTPartyContacts(),
				Remove_EXTPartyContacts:party.Remove_EXTPartyContacts()
			}
			allParties.push(partyDetails);
		});		
	}
	return allParties;
}

function ClearPartiestoAddListFilter(){
    l_contract_accounts_model.l_addPartytoContractSelection_model.ClearPartiestoAddFilter();
    if ($("#div_partiestoAddFilter").attr('apps-toggle') == "expanded") {
		$("#div_partiestoAddFilter").toggle();
		document.getElementById("div_partiestoAddFilter").setAttribute("apps-toggle", 'collapsed');
		$("#div_partyList").removeClass("col-md-9");
		$("#div_partyList").addClass("col-md-12");
    }
    $("#btn_clearPartiestoAddActionBar").css('display', 'none');
    ListPartiestoAdd();
}

function FilterPartiestoAdd(){
    $("#btn_clearPartiestoAddActionBar").css('display', 'inline');
    ListPartiestoAdd();
	l_contract_accounts_model.l_addPartytoContractSelection_model.togglePartiestoAddFilter();
}

function ListPartiestoAdd(i_PartyType) {
    if (i_PartyType == undefined) {
        i_PartyType = l_selectedPartyType;
    }
    $.cordys.ajax({
        namespace: "http://schemas.cordys.com/default",
        method: "getPartieswithFilters",
        parameters: {
            "partyName": $("#input_partiestoAddNameFilter").val(),
            "partyRegID": $("#input_partiestoAddRegIDFilter").val(),
            "partyRegName": "",
            "partyType": i_PartyType,
            "offset": "0",
            "limit": "200"
        },
        success: function (data) {
            if (data.FindZ_INT_AllAccountsListResponse) {
                if (data.FindZ_INT_AllAccountsListResponse['@total'] == undefined) {
                    l_contract_accounts_model.l_addPartytoContractSelection_model.totalNoOfParties("0");
                } else {
                    l_contract_accounts_model.l_addPartytoContractSelection_model.totalNoOfParties(data.FindZ_INT_AllAccountsListResponse['@total']);
                }
                addDataToPartyLookup(data.FindZ_INT_AllAccountsListResponse.Party, l_contract_accounts_model.l_addPartytoContractSelection_model, i_PartyType);
            }
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving the Parties. Contact your administrator."), 10000);
            return false;
        }
    });
}

function addDataToPartyLookup(iElementList, iModel, i_PartyType) {
	markPartiesToAlreadyAdded(i_PartyType);
    iModel.PartiestoAdd.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            iElementList.forEach(function (iElement) {
                if (i_PartyType == "INTERNAL") {
                    iElement.isAddedAlready = ko.computed(function () {
                        return (l_addedINTParties[getTextValue(iElement['Party-id'].Id)]) ? true : false;
                    });
                } else if (i_PartyType == "EXTERNAL") {
                    iElement.isAddedAlready = ko.computed(function () {
                        return (l_addedEXTParties[getTextValue(iElement['Party-id'].Id)]) ? true : false;
                    });
                }
                iModel.PartiestoAdd.push(iElement);
            });
        }
        else {
            if (i_PartyType == "INTERNAL") {
                iElementList.isAddedAlready = ko.computed(function () {
                    return (l_addedINTParties[getTextValue(iElementList['Party-id'].Id)]) ? true : false;
                });
            } else if (i_PartyType == "EXTERNAL") {
                iElementList.isAddedAlready = ko.computed(function () {
                    return (l_addedEXTParties[getTextValue(iElementList['Party-id'].Id)]) ? true : false;
                });
            }
            iModel.PartiestoAdd.push(iElementList);
        }
    }
    $("#tbody_partiesToAdd [isAddedAlready=true]").css('pointer-events', 'none');
    $("#tbody_partiesToAdd [isAddedAlready=true]").css("background-color", "#f4f4f4");
    $("#tbody_partiesToAdd [isAddedAlready=true]").css("color", "#757575");
}

function markPartiesToAlreadyAdded(i_PartyType){
	if (i_PartyType == "INTERNAL") {
        if (l_contract_accounts_model.l_INTParties_model.New_INTParties()) {
            if (l_contract_accounts_model.l_INTParties_model.New_INTParties().length) {
                l_contract_accounts_model.l_INTParties_model.New_INTParties().forEach(function (iToken) {
                    l_addedINTParties[getTextValue(iToken.INTPartyID())] = getTextValue(iToken.INTPartyName());
                });
            }
        }
    } else if (i_PartyType == "EXTERNAL") {
        if (l_contract_accounts_model.l_EXTParties_model.New_EXTParties()) {
            if (l_contract_accounts_model.l_EXTParties_model.New_EXTParties().length) {
                l_contract_accounts_model.l_EXTParties_model.New_EXTParties().forEach(function (iToken) {
                    l_addedEXTParties[getTextValue(iToken.EXTPartyID())] = getTextValue(iToken.EXTPartyName());
                });
            }
        }
    }
}
function ListContactstoAdd(iPartyID, iIndex, iPartyType) {
    if (iPartyID == undefined || iIndex == undefined || iPartyType == undefined) {
        iPartyID = l_selectediPartyID;
        iIndex = l_selectediIndex;
        iPartyType = l_selectediPartyType;
    } else {
        l_selectediPartyID = iPartyID;
        l_selectediIndex = iIndex;
        l_selectediPartyType = iPartyType;
    }
    $.cordys.ajax({
        namespace: "http://schemas.cordys.com/default",
        method: "getContactswithFilters",
        parameters: {
            "partyID": iPartyID,
            "contactUserID": $("#input_contactListSearchFilter").val(),
            "contactFirstName": "",
            "contactLastName": "",
            "contactDisplayName": "",
            "offset": "0",
            "limit": "200"
        },
        success: function (data) {
            if (data.FindZ_INT_ContactListResponse['@total'] == undefined) {
                l_contract_accounts_model.l_addContacttoContractSelection_model.totalNoOfContacts("0");
            } else {
                l_contract_accounts_model.l_addContacttoContractSelection_model.totalNoOfContacts(data.FindZ_INT_ContactListResponse['@total']);
            }
            addDataToContactLookup(data.FindZ_INT_ContactListResponse.RelatedContacts, l_contract_accounts_model.l_addContacttoContractSelection_model, iIndex, iPartyType);
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving the contacts. Contact your administrator."), 10000);
            return false;
        }
    });
}

function addDataToContactLookup(iElementList, iModel, iIndex, iPartyType) {
	l_addedINTPartyContacts = {};
	l_addedEXTPartyContacts = {};	
    if (iPartyType == "INTERNAL") {
		
        if (l_contract_accounts_model.l_INTParties_model.New_INTParties()[iIndex]) {
            if (l_contract_accounts_model.l_INTParties_model.New_INTParties()[iIndex].New_INTPartyContacts().length) {
				
                l_contract_accounts_model.l_INTParties_model.New_INTParties()[iIndex].New_INTPartyContacts().forEach(function (iToken) {
                    l_addedINTPartyContacts[getTextValue(iToken.INTPartyContactID())] = getTextValue(iToken.INTPartyContactDisplayName());
                });
				
            }
        }
    } else if (iPartyType == "EXTERNAL") {
        if (l_contract_accounts_model.l_EXTParties_model.New_EXTParties()[iIndex]) {
            if (l_contract_accounts_model.l_EXTParties_model.New_EXTParties()[iIndex].New_EXTPartyContacts().length) {
                l_contract_accounts_model.l_EXTParties_model.New_EXTParties()[iIndex].New_EXTPartyContacts().forEach(function (iToken) {
                    l_addedEXTPartyContacts[getTextValue(iToken.EXTPartyContactID())] = getTextValue(iToken.EXTPartyContactDisplayName());
                });
				
            }
        }
    }
    iModel.ContactstoAdd.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            iElementList.forEach(function (iElement) {
                if (iElement.ContainingPerson) {
                    if (iPartyType == "INTERNAL") {
                        iElement.isAddedAlready = ko.computed(function () {
                            return (l_addedINTPartyContacts[getTextValue(iElement['RelatedContacts-id'].Id1)]) ? true : false;
                        });
                    } else if (iPartyType == "EXTERNAL") {
                        iElement.isAddedAlready = ko.computed(function () {
                            return (l_addedEXTPartyContacts[getTextValue(iElement['RelatedContacts-id'].Id1)]) ? true : false;
                        });
                    }
                    iModel.ContactstoAdd.push(iElement);
                }
            });
        }
        else {
            if (iElementList.ContainingPerson) {
                if (iPartyType == "INTERNAL") {
                    iElementList.isAddedAlready = ko.computed(function () {
                        return (l_addedINTPartyContacts[getTextValue(iElementList['RelatedContacts-id'].Id1)]) ? true : false;
                    });
                } else if (iPartyType == "EXTERNAL") {
                    iElementList.isAddedAlready = ko.computed(function () {
                        return (l_addedEXTPartyContacts[getTextValue(iElementList['RelatedContacts-id'].Id1)]) ? true : false;
                    });
                }
                iModel.ContactstoAdd.push(iElementList);
            }
        }
    }
    $("#tbody_contactsToAdd [isAddedAlready=true]").css('pointer-events', 'none');
    $("#tbody_contactsToAdd [isAddedAlready=true]").css("background-color", "#f4f4f4");
    $("#tbody_contactsToAdd [isAddedAlready=true]").css("color", "#757575");
}

function removePartyFromRow(iItem) {
    $("#div_removePartyModal").modal({
        backdrop: 'static',
        keyboard: false
    });
	i_PartyType=iItem.CTRPartyRelationType();
	//removePartyFromRow(iItem.INTPartyID(), iItem.INTPartyName(), iItem.CTRPartyRelationType());
	if(i_PartyType=="INTERNAL")
		$("#span_partyToRemove").text(iItem.INTPartyName());
	else if(i_PartyType=="EXTERNAL")
		$("#span_partyToRemove").text(iItem.EXTPartyName());
    $('button#btn_removePartyYes').off("click");
	//nik old remove party
    // $('button#btn_removePartyYes').on('click', function (_event) {
        // $.cordys.ajax({
            // namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            // method: "DelCTRParties",
            // parameters: {
                // "contractID": contractID,
                // "ctrPartyID": i_CTRPartyID
            // },
            // success: function (data) {
                // successToast(3000, getTranslationMessage(i_PartyType == "INTERNAL" ? "Internal account removed" : "External account removed"));
                // listINTParties();
                // listEXTParties();
                // UpdateNegotiationInstance();
            // },
            // error: function (responseFailure) {
                // showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while deleting the party. Contact your administrator."), 10000);
                // return false;
            // }
        // });
    // });
	$('button#btn_removePartyYes').on('click', function (_event) {
		if(i_PartyType == "INTERNAL"){
			iIndex= l_contract_accounts_model.l_INTParties_model.New_INTParties.indexOf(iItem);
			INTParty= iItem;
			
				if(INTParty.CTRPartyID().length>0){
					INTParty.New_INTPartyContacts().some(function(INTPartyContact){
					if(INTPartyContact.CTRContactID().length>0)
						INTParty.Remove_INTPartyContacts.push(Object.assign({}, INTPartyContact));
					});
					INTParty.New_INTPartyContacts.removeAll();
					INTParty.New_INTPartyContacts.push(_populateINTPartiesContactsListData(""));
					l_contract_accounts_model.l_INTParties_model.Remove_INTParties.push(Object.assign({}, INTParty));
					l_contract_accounts_model.l_INTParties_model.New_INTParties.remove(INTParty);
				}
				else{
					l_contract_accounts_model.l_INTParties_model.New_INTParties.remove(INTParty);
				}
				 
			
			INTParty_UpdatetPages("REMOVE");
			trackChange(i_PartyType);
		}
		else if(i_PartyType == "EXTERNAL"){
			iIndex= l_contract_accounts_model.l_EXTParties_model.New_EXTParties.indexOf(iItem);
			EXTParty= iItem;
			
				if(EXTParty.CTRPartyID().length>0){
					EXTParty.New_EXTPartyContacts().some(function(EXTPartyContact){
					if(EXTPartyContact.CTRContactID().length>0)
						EXTParty.Remove_EXTPartyContacts.push(Object.assign({}, EXTPartyContact));
					});
					EXTParty.New_EXTPartyContacts.removeAll();
					EXTParty.New_EXTPartyContacts.push(_populateEXTPartiesContactsListData(""));
					l_contract_accounts_model.l_EXTParties_model.Remove_EXTParties.push(Object.assign({}, EXTParty));
					l_contract_accounts_model.l_EXTParties_model.New_EXTParties.remove(EXTParty);
					}
					else{
						 l_contract_accounts_model.l_EXTParties_model.New_EXTParties.remove(EXTParty);
					 }
			
			EXTParty_UpdatetPages("REMOVE");
			trackChange(i_PartyType);
		}
		$("#input_partyListSearchFilter").val('');
		l_contract_accounts_model.l_addPartytoContractSelection_model.clearSelection();
       
		l_addedINTParties = {};
        l_addedEXTParties = {};
		
		$('.modal').modal('hide');
	});
}

function removePartyContact(i_PartyItem, i_Item, i_PartyType) {
    $("#div_removeContactModal").modal({
        backdrop: 'static',
        keyboard: false
    });
	//i_PartyID
    $("#span_ContactToRemove").text(i_PartyType=="INTERNAL"?i_Item.INTPartyContactDisplayName():i_Item.EXTPartyContactDisplayName());
    $('button#btn_removeContactYes').off("click");
	//nik old remove contact
    // $('button#btn_removeContactYes').on('click', function (_event) {
        // $.cordys.ajax({
            // namespace: "http://schemas/OpenTextContractCenter/CTRContacts/operations",
            // method: "DeleteCTRContacts",
            // parameters: {
                // "CTRContacts": {
                    // "CTRContacts-id": { "Id": i_CTRContactID },
                // }
            // },
            // success: function (data) {
                // successToast(3000, getTranslationMessage(i_PartyType == "INTERNAL" ? "Internal contact removed" : "External contact removed"));
                // listINTParties();
                // listEXTParties();
                // UpdateNegotiationInstance();
            // },
            // error: function (responseFailure) {
                // showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while removing the contact. Contact your administrator."), 10000);
                // return false;
            // }
        // });
    // });
	$('button#btn_removeContactYes').on('click', function (_event) {
		if(i_PartyType == "INTERNAL"){
			iIndex= l_contract_accounts_model.l_INTParties_model.New_INTParties.indexOf(i_PartyItem);
			INTParty= i_PartyItem;
					if(i_Item.CTRContactID().length>0){
						INTParty.Remove_INTPartyContacts.push(Object.assign({}, i_Item))
						INTParty.New_INTPartyContacts.remove(i_Item);
					}
					else{
						INTParty.New_INTPartyContacts.remove(i_Item);
					}
					if(INTParty.New_INTPartyContacts().length==0){
						INTParty.New_INTPartyContacts.push(_populateINTPartiesContactsListData(""));
					}
				
			INTParty_UpdatetPages();
			trackChange(i_PartyType);
		}
		else if(i_PartyType == "EXTERNAL"){
				iIndex= l_contract_accounts_model.l_EXTParties_model.New_EXTParties.indexOf(i_PartyItem);
			EXTParty= i_PartyItem;
					if(i_Item.CTRContactID().length>0){
						EXTParty.Remove_EXTPartyContacts.push(Object.assign({}, i_Item))
						EXTParty.New_EXTPartyContacts.remove(i_Item);
					}
					else{
						EXTParty.New_EXTPartyContacts.remove(i_Item);
					}
					
					if(EXTParty.New_EXTPartyContacts().length==0){
						EXTParty.New_EXTPartyContacts.push(_populateEXTPartiesContactsListData(""));
					}
				 
			EXTParty_UpdatetPages();
			trackChange(i_PartyType);
		}
		
	});
};

//External party

function addDataToEXTPartiesView(iElementList, iModel) {
    iModel.EXTParties.removeAll();
	iModel.New_EXTParties.removeAll();
    if (iElementList) {
        if (Array.isArray(iElementList)) {
            for (var i = 0; i < iElementList.length; i++) {
                iModel.EXTParties.push(_populateEXTPartiesListData(iElementList[i]));
            }
        } else {

            iModel.EXTParties.push(_populateEXTPartiesListData(iElementList));
        }
		iModel.New_EXTParties(iModel.EXTParties.slice());
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
	l_EXTPartyDetails_Obj.EXTPartyAcctMgrUserIds(getTextValue(iElement.RelatedParty.Z_INT_ManagerUserId));
	l_EXTPartyDetails_Obj.EXTPartyBusinessWorkspaceId(getTextValue(iElement.RelatedParty.BW_ID));
	
	
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
	l_EXTPartyDetails_Obj.New_EXTPartyContacts(l_EXTPartyDetails_Obj.EXTPartyContacts.slice());
    return l_EXTPartyDetails_Obj;
}

function _populateEXTPartiesContactsListData(iElement) {
    var l_EXTPartyContacts_Obj = new ExternalPartyContactsModel();
    if (iElement != "") {
        l_EXTPartyContacts_Obj.CTRContactID(getTextValue(iElement['CTRContacts-id'].Id));
        l_EXTPartyContacts_Obj.CTRContactItemID(getTextValue(iElement['CTRContacts-id'].ItemId));

        l_EXTPartyContacts_Obj.EXTPartyContactID(getTextValue(iElement.RelatedContacts['RelatedContacts-id'].Id1));
        l_EXTPartyContacts_Obj.EXTPartyContactItemID(getTextValue(iElement.RelatedContacts['RelatedContacts-id'].ItemId1));

        l_EXTPartyContacts_Obj.EXTPartyContactUserID(getTextValue(iElement.RelatedContacts.ContainingPerson.User_ID));
        l_EXTPartyContacts_Obj.EXTPartyContactFirstName(getTextValue(iElement.RelatedContacts.ContainingPerson.FirstName));
        l_EXTPartyContacts_Obj.EXTPartyContactLastName(getTextValue(iElement.RelatedContacts.ContainingPerson.LastName));
        l_EXTPartyContacts_Obj.EXTPartyContactDisplayName(getTextValue(iElement.RelatedContacts.ContainingPerson.DisplayName));
		l_EXTPartyContacts_Obj.EXTPartyContactMiddleName(getTextValue(iElement.RelatedContacts.ContainingPerson.MiddleName));
		l_EXTPartyContacts_Obj.EXTPartyContactEmail(getTextValue(iElement.RelatedContacts.ContainingPerson.Email));
		l_EXTPartyContacts_Obj.EXTPartyContactPhone(getTextValue(iElement.RelatedContacts.ContainingPerson.Phone));
		l_EXTPartyContacts_Obj.EXTPartyContactMobile(getTextValue(iElement.RelatedContacts.ContainingPerson.Mobile));
		l_EXTPartyContacts_Obj.EXTPartyFullName(l_EXTPartyContacts_Obj.EXTPartyContactFirstName()+" "+l_EXTPartyContacts_Obj.EXTPartyContactLastName());
		if(iElement.RelatedContacts.ContainingPerson.WorkAddress){
			l_EXTPartyContacts_Obj.EXTPartyStreetAddress(getTextValue(iElement.RelatedContacts.ContainingPerson.WorkAddress.StreetAddress?iElement.RelatedContacts.ContainingPerson.WorkAddress.StreetAddress:""));
			l_EXTPartyContacts_Obj.EXTPartyCity(getTextValue(iElement.RelatedContacts.ContainingPerson.WorkAddress.City?iElement.RelatedContacts.ContainingPerson.WorkAddress.City:""));
			l_EXTPartyContacts_Obj.EXTPartyState(getTextValue(iElement.RelatedContacts.ContainingPerson.WorkAddress.ToState?iElement.RelatedContacts.ContainingPerson.WorkAddress.ToState.Name:""));
			l_EXTPartyContacts_Obj.EXTPartyCountry(getTextValue(iElement.RelatedContacts.ContainingPerson.WorkAddress.ToCountry?iElement.RelatedContacts.ContainingPerson.WorkAddress.ToCountry.Country_Name:""));
			l_EXTPartyContacts_Obj.EXTPartyPostalCode(getTextValue(iElement.RelatedContacts.ContainingPerson.WorkAddress.PostalCode?iElement.RelatedContacts.ContainingPerson.WorkAddress.PostalCode:""));
			l_EXTPartyContacts_Obj.EXTPartyFullAddress(l_EXTPartyContacts_Obj.EXTPartyStreetAddress()+(l_EXTPartyContacts_Obj.EXTPartyStreetAddress()!=""?", ":"")+l_EXTPartyContacts_Obj.EXTPartyCity()+(l_EXTPartyContacts_Obj.EXTPartyCity()!=""?", ":"")+l_EXTPartyContacts_Obj.EXTPartyState()+(l_EXTPartyContacts_Obj.EXTPartyState()!=""?", ":"")+l_EXTPartyContacts_Obj.EXTPartyCountry()+"\n"+l_EXTPartyContacts_Obj.EXTPartyPostalCode());
		}
    } else {
        l_EXTPartyContacts_Obj.EXTPartyContactFirstName(getTextValue("Select a contact"));

    }
    return l_EXTPartyContacts_Obj;
}

function updateEXTPartyPaginationParams() {
    if (l_contract_accounts_model.l_EXTParties_model.currentPage() == 1) {
        document.getElementById("li_EXTPartiesLeftNavigation").style.display = "none";
        document.getElementById("li_EXTPartiesRightNavigation").style.display = "inline";
    }
	else if (l_contract_accounts_model.l_EXTParties_model.currentPage() < Math.ceil(l_contract_accounts_model.l_EXTParties_model.numOfEXTParties() / EXTPartiesLimitValue)) {
        document.getElementById("li_EXTPartiesRightNavigation").style.display = "inline";
    }
    if (parseInt(l_contract_accounts_model.l_EXTParties_model.numOfEXTParties()) <= parseInt(EXTPartiesLimitValue)) {
        l_contract_accounts_model.l_EXTParties_model.currentPage('1');
        $('#li_EXTPartiesLeftNavigation,#li_EXTPartiesRightNavigation').css('display', 'none');
    }
}

function updateEXTPartyLimitValue(iElement) {
    EXTPartiesOffsetValue = 0;
    l_contract_accounts_model.l_EXTParties_model.currentPage('1');
    EXTPartiesLimitValue = $(iElement).val();
    //listEXTParties();
	EXTParty_UpdatetPages();
}

function EXTParty_goToPreviousPage() {
    if (l_contract_accounts_model.l_EXTParties_model.currentPage() > 1) {
        EXTPartiesOffsetValue = parseInt(EXTPartiesOffsetValue) - parseInt(EXTPartiesLimitValue);
        l_contract_accounts_model.l_EXTParties_model.currentPage(parseInt(l_contract_accounts_model.l_EXTParties_model.currentPage()) - 1);
    }
    if (l_contract_accounts_model.l_EXTParties_model.currentPage() < Math.ceil(l_contract_accounts_model.l_EXTParties_model.numOfEXTParties() / EXTPartiesLimitValue)) {
        document.getElementById("li_EXTPartiesRightNavigation").style.removeProperty("display");
    }
    if (l_contract_accounts_model.l_EXTParties_model.currentPage() == 1) {
        document.getElementById("li_EXTPartiesLeftNavigation").style.display = "none";
    }
    if (l_contract_accounts_model.l_EXTParties_model.currentPage() < 1)
        return;
    //listEXTParties();
	EXTParty_UpdatetPages();
}

function EXTParty_goToNextPage() {
    if (l_contract_accounts_model.l_EXTParties_model.currentPage() < Math.ceil(l_contract_accounts_model.l_EXTParties_model.numOfEXTParties() / EXTPartiesLimitValue)) {
        EXTPartiesOffsetValue = parseInt(EXTPartiesOffsetValue) + parseInt(EXTPartiesLimitValue);
        l_contract_accounts_model.l_EXTParties_model.currentPage(isNaN(parseInt(l_contract_accounts_model.l_EXTParties_model.currentPage())) ? 0 : parseInt(l_contract_accounts_model.l_EXTParties_model.currentPage()));
        l_contract_accounts_model.l_EXTParties_model.currentPage(parseInt(l_contract_accounts_model.l_EXTParties_model.currentPage()) + 1);
    }
    if (l_contract_accounts_model.l_EXTParties_model.currentPage() == Math.ceil(l_contract_accounts_model.l_EXTParties_model.numOfEXTParties() / EXTPartiesLimitValue)) {
        document.getElementById("li_EXTPartiesRightNavigation").style.display = "none";
    }
    if (l_contract_accounts_model.l_EXTParties_model.currentPage() > 1) {
        document.getElementById("li_EXTPartiesLeftNavigation").style.removeProperty("display");
    }
    //listEXTParties();
	EXTParty_UpdatetPages();
}

function EXTParty_goToLastPage() {
    EXTPartiesOffsetValue = (Math.ceil(l_contract_accounts_model.l_EXTParties_model.numOfEXTParties() / EXTPartiesLimitValue) - 1) * EXTPartiesLimitValue;
    l_contract_accounts_model.l_EXTParties_model.currentPage(Math.ceil(l_contract_accounts_model.l_EXTParties_model.numOfEXTParties() / EXTPartiesLimitValue));
    $('#li_EXTPartiesRightNavigation').css('display', 'none');
    $('#li_EXTPartiesLeftNavigation').css('display', 'inline');
   // listEXTParties();
   EXTParty_UpdatetPages();
}

function EXTParty_goToFirstPage() {
    EXTPartiesOffsetValue = 0;
    l_contract_accounts_model.l_EXTParties_model.currentPage('1');
    $('#li_EXTPartiesRightNavigation').css('display', 'inline');
    $('#li_EXTPartiesLeftNavigation').css('display', 'none');
   // listEXTParties()
   EXTParty_UpdatetPages();
}

function listEXTParties(updateAccounts) {
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
        method: "getPartieswithFilters",
        parameters: l_contract_accounts_model.l_EXTPartiesFilter_model.getEXTPartiesFilterObject(),
        success: function (data) {
            addDataToEXTPartiesView(data.parties.FindZ_INT_CTRPartiesListResponse.CTRParties, l_contract_accounts_model.l_EXTParties_model);
            if (undefined != data.parties.FindZ_INT_CTRPartiesListResponse['@total']) {
                l_contract_accounts_model.l_EXTParties_model.numOfEXTParties(data.parties.FindZ_INT_CTRPartiesListResponse['@total']);
            } else {
                l_contract_accounts_model.l_EXTParties_model.numOfEXTParties(0);
            }
            if (l_contract_accounts_model.l_EXTParties_model.numOfEXTParties() != 0) {
                l_contract_accounts_model.l_EXTParties_model.numOfPages(Math.ceil(l_contract_accounts_model.l_EXTParties_model.numOfEXTParties() / EXTPartiesLimitValue));
            } else {
                l_contract_accounts_model.l_EXTParties_model.numOfPages(1);
            }
			l_contract_accounts_model.EXTDataLoaded(true);
			if( typeof updateAccounts == "function" )
				updateAccounts();
            EXTParty_UpdatetPages();
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving the parties. Contact your administrator."), 10000);
            return false;
        }
    });
}

function hideEXTPartiesListFilter() {
    $("#div_EXTPartiesListFilter").hide();
    document.getElementById("div_EXTPartiesListFilter").setAttribute("apps-toggle", 'collapsed');
    $("#div_EXTPartiesList").removeClass("col-md-9");
    $("#div_EXTPartiesList").addClass("col-md-12");
}

function ApplyFilterOnEXTPartiesList(event, iSrcElement) {
	markPartiesToAlreadyAdded("EXTERNAL");
    if (document.getElementById("input_EXTPartyRegIDFilter").value != "" || document.getElementById("input_EXTPartyNameFilter").value != "" || document.getElementById("input_EXTPartyRegNameFilter").value != "") {
        $("#btn_clearEXTFilterActionBar").css('display', 'inline');
        l_contract_accounts_model.l_EXTParties_model.isFilterApplied(true);
    } else {
        $("#btn_clearEXTFilterActionBar").css('display', 'none');
        l_contract_accounts_model.l_EXTParties_model.isFilterApplied(false);
    }
    listEXTParties();
    hideEXTPartiesListFilter();
}

function ClearEXTPartiesListFilter(event, iSrcElement) {
    l_contract_accounts_model.l_EXTPartiesFilter_model.ClearEXTPartiesFilter();
    $("#btn_clearEXTFilterActionBar").css('display', 'none');
    l_contract_accounts_model.l_EXTParties_model.isFilterApplied(false);
    listEXTParties();
    hideEXTPartiesListFilter();
}

// Update negotiation instance details.
function UpdateNegotiationInstance() {
    $.cordys.ajax({
        method: "UpdateContractDetailsonAccEdit",
        namespace: "http://schemas.opentext.com/apps/ContractNegotiation/19.4",
        parameters:
        {
            'contractID': contractID,
        }
    }).done(function (data) {

    }).fail(function (error) {

    });
}

// Check for user user role for edit.
function checkForUserRole() {
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
                l_contract_accounts_model.isEditable(true);
            } else {
                l_contract_accounts_model.isEditable(false);
            }
        }).fail(function (error) {
        })
}

function cancelAcctsSaveOREdit() {
    $("#cancelModal").modal();
    $('button#cancelAccountsChanges').off("click");
    $('button#cancelAccountsChanges').on('click', function (_event) {
        refreshAccountsViewModel();
    });
}

function updateAccounts(){
	if(l_contract_accounts_model.l_INTParties_model.isFilterApplied()){
		l_contract_accounts_model.l_INTPartiesFilter_model.ClearINTPartiesFilter();
		$("#btn_clearINTFilterActionBar").css('display', 'none');
		l_contract_accounts_model.l_INTParties_model.isFilterApplied(false);
		listINTParties(function(){
			$.cordys.ajax({
			 namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
			 method: "UpdateAccounts",
			 parameters:getAccountsSaveObj(),
			success: function (data) {
				refreshAccountsViewModel();
				successToast(3000, getTranslationMessage("Changes saved successfully."));
			 },
			 error: function (responseFailure) {
				showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while saving the accounts. Contact your administrator."), 10000);
				return false;
			 }
			});
		});
		hideINTPartiesListFilter();
	}
	else if(l_contract_accounts_model.l_EXTParties_model.isFilterApplied()){
		l_contract_accounts_model.l_EXTPartiesFilter_model.ClearEXTPartiesFilter();
		$("#btn_clearEXTFilterActionBar").css('display', 'none');
		l_contract_accounts_model.l_EXTParties_model.isFilterApplied(false);
		listEXTParties(function(){
			$.cordys.ajax({
			 namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
			 method: "UpdateAccounts",
			 parameters:getAccountsSaveObj(),
			success: function (data) {
				refreshAccountsViewModel();
				successToast(3000, getTranslationMessage("Changes saved successfully."));
			 },
			 error: function (responseFailure) {
				showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while saving the accounts. Contact your administrator."), 10000);
				return false;
			 }
			});
		});
		hideEXTPartiesListFilter();
	}
     else{
		$.cordys.ajax({
			 namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
			 method: "UpdateAccounts",
			 parameters:getAccountsSaveObj(),
			success: function (data) {
				refreshAccountsViewModel();
				successToast(3000, getTranslationMessage("Changes saved successfully."));
			 },
			 error: function (responseFailure) {
				showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while saving the accounts. Contact your administrator."), 10000);
				return false;
			 }
			});
	 }		 
}


function trackChange(i_PartyType){
	var changeFlag=false;
	if(i_PartyType=="INTERNAL"){
		if((l_contract_accounts_model.l_INTParties_model.INTParties().length!=l_contract_accounts_model.l_INTParties_model.New_INTParties().length || 		l_contract_accounts_model.l_INTParties_model.Remove_INTParties().length!=0)){	
			changeFlag=true;
		}
		if(!changeFlag){
			l_contract_accounts_model.l_INTParties_model.New_INTParties().some(function(INTParty){
				if(INTParty.INTPartyContacts().length!=INTParty.New_INTPartyContacts().length || INTParty.Remove_INTPartyContacts().length!=0 || (INTParty.New_INTPartyContacts().length==1&& INTParty.INTPartyContacts()[0].INTPartyContactID()!=INTParty.New_INTPartyContacts()[0].INTPartyContactID() && INTParty.New_INTPartyContacts()[0].INTPartyContactID().length>0)){
						 changeFlag=true;
						 return true;
					 }
				 } );
		}
		l_contract_accounts_model.isINTDirty(changeFlag);
	}
	else if(i_PartyType=="EXTERNAL"){
		if(l_contract_accounts_model.l_EXTParties_model.EXTParties().length!=l_contract_accounts_model.l_EXTParties_model.New_EXTParties().length || l_contract_accounts_model.l_EXTParties_model.Remove_EXTParties().length!=0){ 
			changeFlag=true;
		}
		if(!changeFlag){
			l_contract_accounts_model.l_EXTParties_model.New_EXTParties().some(function(EXTParty){
					 if(EXTParty.EXTPartyContacts().length!=EXTParty.New_EXTPartyContacts().length || EXTParty.Remove_EXTPartyContacts().length!=0 || (EXTParty.New_EXTPartyContacts().length==1 && EXTParty.EXTPartyContacts()[0].EXTPartyContactID()!=EXTParty.New_EXTPartyContacts()[0].EXTPartyContactID() && EXTParty.New_EXTPartyContacts()[0].EXTPartyContactID().length>0)){
						 changeFlag=true;
						 return true;
					 }
				 } );
		
		}
		l_contract_accounts_model.isEXTDirty(changeFlag);
	}
}

function refreshAccountsViewModel() {
	l_contract_accounts_model.isINTDirty(false);
	l_contract_accounts_model.isEXTDirty(false);
	listALLParties();
	l_contract_accounts_model.l_INTParties_model.Remove_INTParties.removeAll();
	l_contract_accounts_model.l_EXTParties_model.Remove_EXTParties.removeAll();
}

function INTParty_UpdatetPages(type){
	
	if(type=="ADD")
		l_contract_accounts_model.l_INTParties_model.numOfINTParties(Number(l_contract_accounts_model.l_INTParties_model.numOfINTParties())+1);
	else if(type=="REMOVE")
		l_contract_accounts_model.l_INTParties_model.numOfINTParties(Number(l_contract_accounts_model.l_INTParties_model.numOfINTParties())-1);
	
	if (l_contract_accounts_model.l_INTParties_model.numOfINTParties() != 0) {
		l_contract_accounts_model.l_INTParties_model.numOfPages(Math.ceil(l_contract_accounts_model.l_INTParties_model.numOfINTParties() / INTPartiesLimitValue));
	} else {
		l_contract_accounts_model.l_INTParties_model.numOfPages(1);
	}
	l_contract_accounts_model.l_INTParties_model.View_INTParties(l_contract_accounts_model.l_INTParties_model.New_INTParties.slice(INTPartiesOffsetValue,INTPartiesOffsetValue+Number(INTPartiesLimitValue)));
	//updateINTPartyPaginationParams();
	
}

function EXTParty_UpdatetPages(type){
	
	if(type=="ADD")
		l_contract_accounts_model.l_EXTParties_model.numOfEXTParties(Number(l_contract_accounts_model.l_EXTParties_model.numOfEXTParties())+1);
	else if(type=="REMOVE")
		l_contract_accounts_model.l_EXTParties_model.numOfEXTParties(Number(l_contract_accounts_model.l_EXTParties_model.numOfEXTParties())-1);

	if (l_contract_accounts_model.l_EXTParties_model.numOfEXTParties() != 0) {
		l_contract_accounts_model.l_EXTParties_model.numOfPages(Math.ceil(l_contract_accounts_model.l_EXTParties_model.numOfEXTParties() / EXTPartiesLimitValue));
	} else {
		l_contract_accounts_model.l_EXTParties_model.numOfPages(1);
	}
	l_contract_accounts_model.l_EXTParties_model.View_EXTParties(l_contract_accounts_model.l_EXTParties_model.New_EXTParties.slice(EXTPartiesOffsetValue,EXTPartiesOffsetValue+Number(EXTPartiesLimitValue)));
	//updateEXTPartyPaginationParams();
	
}
function setTitle(i_PartyType){
	if(!l_contract_accounts_model.isEditable())
		return "You do not have permission to edit this field";
	else if(l_contract_accounts_model.l_INTParties_model.isFilterApplied() && i_PartyType=="INTERNAL")
		return "Clear the filter to edit";
	else if(l_contract_accounts_model.l_EXTParties_model.isFilterApplied()  && i_PartyType=="EXTERNAL")
		return "Clear the filter to edit";
}

function displayContactPreview(i_PartyIndex,i_ContactIndex,i_PartyType){
	if(i_PartyType=="INTERNAL"){
		$('.table_internalPartyContacts').each(function(i, obj) {
			if(i==i_PartyIndex){
				if($(this).find(".InternalPreviewModel").eq(i_ContactIndex).css("display")=="none"){
					$(this).find(".InternalPreviewModel").eq(i_ContactIndex).css("display","block");
					var heightInPX=($(this).find(".InternalPreviewModel").eq(i_ContactIndex).find(".ContactPreview").css("height"));
					var height=(heightInPX.substring(0, heightInPX.length - 2)/2)-10;
					if(i_PartyIndex<=1 && i_ContactIndex<=2)
						height=(height/2)+10;
					$(this).find(".InternalPreviewModel").eq(i_ContactIndex).find(".ContactPreview").css("top",(-(height))+"px");
				}
				else
					$(this).find(".InternalPreviewModel").eq(i_ContactIndex).css("display","none");
				$(this).find(".InternalPreviewModel").each(function(i, obj) {
					if(i!=i_ContactIndex)
					$(this).css("display","none");
				});
			}
			else{
				$(this).find(".InternalPreviewModel").each(function(i, obj) {
					$(this).css("display","none");
				});
			}
		});
		$('.table_externalPartyContacts').each(function(i, obj) {
			
				$(this).find(".ExternalPreviewModel").each(function(i, obj) {
						$(this).css("display","none");
				});
			
		});
	}
	else if(i_PartyType=="EXTERNAL"){
		$('.table_externalPartyContacts').each(function(i, obj) {
			if(i==i_PartyIndex){
				if($(this).find(".ExternalPreviewModel").eq(i_ContactIndex).css("display")=="none"){
					$(this).find(".ExternalPreviewModel").eq(i_ContactIndex).css("display","block");
					var heightInPX=($(this).find(".ExternalPreviewModel").eq(i_ContactIndex).find(".ContactPreview").css("height"));
					var height=(heightInPX.substring(0, heightInPX.length - 2)/2)-10;
					if(i_PartyIndex<=1 && i_ContactIndex<=2)
						height=(height/2)+10;
					$(this).find(".ExternalPreviewModel").eq(i_ContactIndex).find(".ContactPreview").css("top",(-(height))+"px");
				}
				else
					$(this).find(".ExternalPreviewModel").eq(i_ContactIndex).css("display","none");
				$(this).find(".ExternalPreviewModel").each(function(i, obj) {
					if(i!=i_ContactIndex)
						$(this).css("display","none");
				});
			}
			else{
				$(this).find(".ExternalPreviewModel").each(function(i, obj) {
						$(this).css("display","none");
				});
			}
			
		});
		$('.table_internalPartyContacts').each(function(i, obj) {
			
				$(this).find(".InternalPreviewModel").each(function(i, obj) {
						$(this).css("display","none");
				});
			
		});
	}
}
