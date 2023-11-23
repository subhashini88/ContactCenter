$.cordys.json.defaults.removeNamespacePrefix = true;

var ContractDetailsDataModel = function () {
    var self = this;
    self.ContractID = ko.observable();
    self.ContractItemID = ko.observable();
    self.CurrentState = ko.observable();
    self.GeneratedContractId = ko.observable();
    self.InitialContractTenure = ko.observable();
    self.ContractName = ko.observable();
    self.Perpetual = ko.observable();
    self.StartDate = ko.observable();
    self.MinStartdate = ko.observable();
    self.CurrentStartDate = ko.observable();
    self.CurrentEndDate = ko.observable();
    self.InitialExpiryDate = ko.observable();
    self.NextExpirationDate = ko.observable();
    self.Description = ko.observable();
    self.Priority = ko.observable();
    self.ContractNumber = ko.observable();
    self.SignatureDate = ko.observable();
    self.IsExecuted = ko.observable();
    self.ContractDocumentType = ko.observable();
    self.ContractValueUSD = ko.observable();
    self.ContractValue = ko.observable();
    self.AutoRenew = ko.observable();
    self.AutoRenewDuration = ko.observable();
    self.RenewalDiscount = ko.observable();
    self.RenewalComments = ko.observable();
    self.SecondPartyRegisteredName = ko.observable();
    self.SecondPartyZ_INT_ManagerName = ko.observable();
    self.SecondPartyZ_Int_CollectionAccount = ko.observable();
    self.SecondPartyContactName = ko.observable();
    self.RelatedTemplateName = ko.observable();
    self.RelatedTemplateId = ko.observable();
    self.RelatedTemplateItemId = ko.observable();
    self.FirstPartyRegisteredName = ko.observable();
    self.FirstPartyContactName = ko.observable();
    self.ContractTypeName = ko.observable();
    self.CurrencyName = ko.observable();
    self.Country = ko.observable();
    self.Region = ko.observable();
}
var l_contractdetails_model = new ContractDetailsDataModel();
$(function () {
    relatedContentItemID = getUrlParameterValue("instanceId", null, true);
    $('[src*="ExtContact_ContractDetails.htm"]', window.parent.parent.document).parent().css('padding-left', '0px');
    loadContractDetails(relatedContentItemID);
    $("input").prop('disabled', true);
    $("input").css('background-color', 'white');
    $("input").css('border', 'none');
    $("input").css('box-shadow', 'none');
    $("input").css('padding', '0px');
    ko.applyBindings(l_contractdetails_model, document.getElementById("id_contractDetails"));
});
function loadContractDetails(relatedContentItemID) {
    ContractDetailsDataModel = $.cordys.ajax(
        {
            namespace: "http://schemas.opentext.com/apps/ContractNegotiation/19.4",
            method: "GetMetaDatabyRelatedContentItemID",
            parameters: {
                "relatedContentItemID": relatedContentItemID
            },
            success: function (data) {
                if (data) {
                    addDataToContractDetailsView(data.tuple.old.GetMetaDatabyRelatedContentItemID.GetMetaDatabyRelatedContentItemID.data.GetContractDataforRulesResponse, l_contractdetails_model);
                }
            },
            error: function (responseFailure) {
                return false;
            }
        })
}
function addDataToContractDetailsView(iElement, iModel) {
    iModel.ContractID(getTextValue(iElement.Contract["Contract-id"].Id));
    iModel.ContractItemID(getTextValue(iElement.Contract["Contract-id"].ItemId));
    iModel.CurrentState(getTextValue(iElement.Contract["Lifecycle"].CurrentState));
    iModel.GeneratedContractId(getTextValue(iElement.Contract.GeneratedContractId));
    var contractTerm = getTextValue(iElement.Contract.InitialContractTenure);
    if (contractTerm != "") {
        if(contractTerm.lastIndexOf("M") > 0 && contractTerm.lastIndexOf("D") > 0){
            var l_contractTermDuration_Months = getTextValue(contractTerm.substring(contractTerm.lastIndexOf("P") + 1, contractTerm.lastIndexOf("M")));
            var l_contractTermDuration_Days = getTextValue(contractTerm.substring(contractTerm.lastIndexOf("M") + 1, contractTerm.lastIndexOf("D")));
            if(l_contractTermDuration_Months > 0 && l_contractTermDuration_Days > 0){
                iModel.InitialContractTenure(l_contractTermDuration_Months + " months, " + l_contractTermDuration_Days + " days");
            }else if(l_contractTermDuration_Months > 0){
                iModel.InitialContractTenure(l_contractTermDuration_Months + " months");
            }else if(l_contractTermDuration_Days > 0){
                iModel.InitialContractTenure(l_contractTermDuration_Days + "days");
            }else{
                iModel.InitialContractTenure("No duration specified.");
            }
        } else if (contractTerm.lastIndexOf("M") > 0 && getTextValue(contractTerm.substring(contractTerm.lastIndexOf("P") + 1, contractTerm.lastIndexOf("M"))) > 0) {
			iModel.InitialContractTenure(getTextValue(contractTerm.substring(contractTerm.lastIndexOf("P") + 1, contractTerm.lastIndexOf("M"))) + " months");
		} else if (contractTerm.lastIndexOf("D") > 0 && getTextValue(contractTerm.substring(contractTerm.lastIndexOf("P") + 1, contractTerm.lastIndexOf("D"))) > 0) {
			iModel.InitialContractTenure(getTextValue(contractTerm.substring(contractTerm.lastIndexOf("P") + 1, contractTerm.lastIndexOf("D"))) + " days");
		} else {
			iModel.InitialContractTenure("No duration specified.");
		}
    }
    iModel.ContractName(getTextValue(iElement.Contract.ContractName));
    iModel.Perpetual(getTextValue(iElement.Contract.Perpetual) == "true");
    if (getTextValue(iElement.Contract.StartDate)!="") {
        iModel.StartDate(formateDatetoLocale(getTextValue(iElement.Contract.StartDate)));
    }
    if (getTextValue(iElement.Contract.MinStartdate)!="") {
        iModel.MinStartdate(formateDatetoLocale(getTextValue(iElement.Contract.MinStartdate)));
    }
    if (getTextValue(iElement.Contract.CurrentStartDate)!="") {
        iModel.CurrentStartDate(formateDatetoLocale(getTextValue(iElement.Contract.CurrentStartDate)));
    }
    if (getTextValue(iElement.Contract.CurrentEndDate)!="") {
        iModel.CurrentEndDate(formateDatetoLocale(getTextValue(iElement.Contract.CurrentEndDate)));
    }
    if (getTextValue(iElement.Contract.InitialExpiryDate)!="") {
        iModel.InitialExpiryDate(formateDatetoLocale(getTextValue(iElement.Contract.InitialExpiryDate)));
    }
    if (getTextValue(iElement.Contract.NextExpirationDate)!="") {
        iModel.NextExpirationDate(formateDatetoLocale(getTextValue(iElement.Contract.NextExpirationDate)));
    }
    iModel.Description(getTextValue(iElement.Contract.Description));
    iModel.Priority(getTextValue(iElement.Contract.Priority));
    iModel.ContractNumber(getTextValue(iElement.Contract.ContractNumber));
    if (getTextValue(iElement.Contract.SignatureDate) != "") {
        iModel.SignatureDate(formateDatetoLocale(getTextValue(iElement.Contract.SignatureDate)));
    }
    iModel.IsExecuted(getTextValue(iElement.Contract.IsExecuted));
    iModel.ContractDocumentType(getTextValue(iElement.Contract.ContractDocumentType));
    iModel.ContractValueUSD((getTextValue(iElement.Contract.ContractValueUSD) != "") ? getFormattedBigDecimaltoLocale(getTextValue(iElement.Contract.ContractValueUSD)) : getTextValue(iElement.Contract.ContractValueUSD));
    iModel.ContractValue((getTextValue(iElement.Contract.ContractValue) != null) ? getFormattedBigDecimaltoLocale(getTextValue(iElement.Contract.ContractValue)) : getTextValue(iElement.Contract.ContractValue));
    iModel.AutoRenew(getTextValue(iElement.Contract.AutoRenew));
    var autoRenewDuration = getTextValue(iElement.Contract.AutoRenewDuration)
    if (autoRenewDuration != "") {
        if(autoRenewDuration.lastIndexOf("M") > 0 && autoRenewDuration.lastIndexOf("D") > 0){
            var l_autoRenewDuration_Months = getTextValue(autoRenewDuration.substring(autoRenewDuration.lastIndexOf("P") + 1, autoRenewDuration.lastIndexOf("M")));
            var l_autoRenewDuration_Days = getTextValue(autoRenewDuration.substring(autoRenewDuration.lastIndexOf("M") + 1, autoRenewDuration.lastIndexOf("D")));
            if(l_autoRenewDuration_Months > 0 && l_autoRenewDuration_Days > 0){
                iModel.AutoRenewDuration(l_autoRenewDuration_Months + " months, " + l_autoRenewDuration_Days + " days");
            }else if(l_autoRenewDurationDuration_Months > 0){
                iModel.AutoRenewDuration(l_autoRenewDuration_Months + " months");
            }else if(l_autoRenewDurationDuration_Days > 0){
                iModel.AutoRenewDuration(l_autoRenewDuration_Days + "days");
            }else{
                iModel.AutoRenewDuration("No duration specified.");
            }
        } else if (autoRenewDuration.lastIndexOf("M") > 0 && getTextValue(autoRenewDuration.substring(autoRenewDuration.lastIndexOf("P") + 1, autoRenewDuration.lastIndexOf("M"))) > 0) {
			iModel.AutoRenewDuration(getTextValue(autoRenewDuration.substring(autoRenewDuration.lastIndexOf("P") + 1, autoRenewDuration.lastIndexOf("M"))) + " months");
		} else if (autoRenewDuration.lastIndexOf("D") > 0 && getTextValue(autoRenewDuration.substring(autoRenewDuration.lastIndexOf("P") + 1, autoRenewDuration.lastIndexOf("D"))) > 0) {
			iModel.AutoRenewDuration(getTextValue(autoRenewDuration.substring(autoRenewDuration.lastIndexOf("P") + 1, autoRenewDuration.lastIndexOf("D"))) + " days");
		} else {
			iModel.AutoRenewDuration("No duration specified.");
		}

    }
    iModel.RenewalDiscount(getTextValue(iElement.Contract.RenewalDiscount));
    iModel.RenewalComments(getTextValue(iElement.Contract.RenewalComments));
    if (getTextValue(iElement.Contract["SecondParty"])!="") {
        iModel.SecondPartyRegisteredName(getTextValue(iElement.Contract["SecondParty"].RegisteredName));
        iModel.SecondPartyZ_INT_ManagerName(getTextValue(iElement.Contract["SecondParty"].Z_INT_ManagerName));
        iModel.SecondPartyZ_Int_CollectionAccount(getTextValue(iElement.Contract["SecondParty"].Z_Int_CollectionAccount));
    }
    if (getTextValue(iElement.Contract["SecondPartyContact"])!="") {
        iModel.SecondPartyContactName(getTextValue(iElement.Contract["SecondPartyContact"].ContainingPerson.DisplayName));
    }
    if (getTextValue(iElement.Contract["FirstPartyContact"])!="") {
        iModel.FirstPartyContactName(getTextValue(iElement.Contract["FirstPartyContact"].ContainingPerson.DisplayName));
    }
    if(getTextValue(iElement.Contract["RelatedTemplate"])!=""){        
        iModel.RelatedTemplateName(getTextValue(iElement.Contract["RelatedTemplate"].Name));
    }   
    if(getTextValue(iElement.Contract["RelatedTemplate"])!="" && getTextValue(iElement.Contract["RelatedTemplate"]["GCTemplate-id"])!=""){        
        iModel.RelatedTemplateId(getTextValue(iElement.Contract["RelatedTemplate"]["GCTemplate-id"].Id));
        iModel.RelatedTemplateItemId(getTextValue(iElement.Contract["RelatedTemplate"]["GCTemplate-id"].ItemId));
    }
    if(getTextValue(iElement.Contract["FirstParty"])!=""){
        iModel.FirstPartyRegisteredName(getTextValue(iElement.Contract["FirstParty"].RegisteredName));
    }   
    if(getTextValue(iElement.Contract["ContractType"])!=""){
        iModel.ContractTypeName(getTextValue(iElement.Contract["ContractType"].Name));
    }
    if (getTextValue(iElement.Contract["Currency"])!="") {
        iModel.CurrencyName(getTextValue(iElement.Contract["Currency"].Name));
    }
    if (getTextValue(iElement.Contract.Country)!="") {
        if (iElement.Contract.Country.LinkedCountry) {
            iModel.Country(getTextValue(iElement.Contract.Country.LinkedCountry.Country_Name));
        }
        if (iElement.Contract.Country.Owner) {
            iModel.Region(getTextValue(iElement.Contract.Country.Owner.Name));
        }
    }
}

function getTextValue(obj) {
    if (obj && obj["@nil"] !== "true") {
        if (obj.text) {
            return obj.text;
        } else {
            return obj;
        }
    }
    else {
        return "";
    }
}