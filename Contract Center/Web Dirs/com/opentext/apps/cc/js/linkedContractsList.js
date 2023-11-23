$.cordys.json.defaults.removeNamespacePrefix = true;
var contractItemId;
var RelatedcontractOrgId;
var BASEURL;
var contractsListOffsetValue = 0;
var contractsListLimitValue = 25;
var selectedLinkedContractsListMap = {};
var defaultIDforSelectDropdown = ""
var LinkedContractsListModel = function () {
    var self = this;
    self.linkedContractsList = ko.observableArray([]);
	self.numOfLinkedContractsInCurrentPage = ko.observableArray('');
    self.openSelectedItem = function (iItem) {
        if (iItem["TargetContractItemId"]) {
            //navigate to contract
			
			navigateToInstance(iItem.TargetContractItemId(),"Contract");
        }
    }
    
   
    self.onLinkedContractRowCheckboxValueChanged = function (iItem, event) {
        var l_currentClassName = event.currentTarget.className;
        if (l_currentClassName == "cc-select-column cc-checkbox-off") {
            $(event.currentTarget).removeClass("cc-checkbox-off")
            $(event.currentTarget).addClass("cc-checkbox-on")
            selectedLinkedContractsListMap[iItem.ContractLinksId()] = iItem.TargetContractItemId();
            $(event.currentTarget.parentElement.parentElement).css("background-color", "#CBD3D9")
        }
        else if (l_currentClassName == "cc-select-column cc-checkbox-on") {
            $(event.currentTarget).removeClass("cc-checkbox-on")
            $(event.currentTarget).addClass("cc-checkbox-off")
            delete selectedLinkedContractsListMap[iItem.ContractLinksId()];
            $(event.currentTarget.parentElement.parentElement).css("background-color", "transparent")
        }
        event.stopPropagation();
        if (Object.keys(selectedLinkedContractsListMap).length <= 0) {
            $("#div_selectAllLinkedContracts").removeClass("cc-checkbox-select-all-partial");
            $("#div_selectAllLinkedContracts").removeClass("cc-checkbox-select-all-on");
            $("#div_selectAllLinkedContracts").addClass("cc-checkbox-select-all-off");
            $("#btn_openLinkedContractFromActionBar").css("display", "none");
			$("#btn_openNewTabLinkedContractFromActionBar").css("display", "none");
            $("#btn_deleteLinkedContractFromActionBar").css("display", "none");
        } else if (Object.keys(selectedLinkedContractsListMap).length == 1) {
            if (1 == l_LinkedContractsList_model.numOfLinkedContractsInCurrentPage()) {
                $("#div_selectAllLinkedContracts").removeClass("cc-checkbox-select-all-off");
                $("#div_selectAllLinkedContracts").removeClass("cc-checkbox-select-all-partial");
                $("#div_selectAllLinkedContracts").addClass("cc-checkbox-select-all-on");
                $("#btn_openLinkedContractFromActionBar").css("display", "inline");
				$("#btn_openNewTabLinkedContractFromActionBar").css("display", "inline");
                $("#btn_deleteLinkedContractFromActionBar").css("display", "inline");
            } else {
                $("#div_selectAllLinkedContracts").removeClass("cc-checkbox-select-all-off");
                $("#div_selectAllLinkedContracts").removeClass("cc-checkbox-select-all-on");
                $("#div_selectAllLinkedContracts").addClass("cc-checkbox-select-all-partial");
                $("#btn_openLinkedContractFromActionBar").css("display", "inline");
				$("#btn_openNewTabLinkedContractFromActionBar").css("display", "inline");
                $("#btn_deleteLinkedContractFromActionBar").css("display", "inline");
            }
        } else if (Object.keys(selectedLinkedContractsListMap).length > 1 && Object.keys(selectedLinkedContractsListMap).length < l_LinkedContractsList_model.numOfLinkedContractsInCurrentPage()) {
            $("#div_selectAllLinkedContracts").removeClass("cc-checkbox-select-all-off");
            $("#div_selectAllLinkedContracts").removeClass("cc-checkbox-select-all-on");
            $("#div_selectAllLinkedContracts").addClass("cc-checkbox-select-all-partial");
            $("#btn_openLinkedContractFromActionBar").css("display", "none");
			$("#btn_openNewTabLinkedContractFromActionBar").css("display", "none");
            $("#btn_deleteLinkedContractFromActionBar").css("display", "inline");
        } else if (Object.keys(selectedLinkedContractsListMap).length == l_LinkedContractsList_model.numOfLinkedContractsInCurrentPage()) {
            $("#div_selectAllLinkedContracts").removeClass("cc-checkbox-select-all-off");
            $("#div_selectAllLinkedContracts").removeClass("cc-checkbox-select-all-partial");
            $("#div_selectAllLinkedContracts").addClass("cc-checkbox-select-all-on");
			$("#btn_openLinkedContractFromActionBar").css("display", "none");
			$("#btn_openNewTabLinkedContractFromActionBar").css("display", "none");
            $("#btn_deleteLinkedContractFromActionBar").css("display", "inline");
        }
    }
    self.onSelectAllLinkedContractsCheckboxValueChanged = function (iItem, event) {
        var l_currentClassName = event.currentTarget.className;
        if (l_currentClassName == "cc-select-column cc-checkbox-select-all-off" || l_currentClassName == "cc-select-column cc-checkbox-select-all-partial") {
            $(event.currentTarget).removeClass("cc-checkbox-select-all-off");
            $(event.currentTarget).removeClass("cc-checkbox-select-all-partial");
            $(event.currentTarget).addClass("cc-checkbox-select-all-on");
            $("#table_linkedContractsTable").find('tbody .cc-select-column').removeClass("cc-checkbox-off");
            $("#table_linkedContractsTable").find('tbody .cc-select-column').addClass("cc-checkbox-on");
            $("#table_linkedContractsTable").find('tbody tr').css("background-color", "#CBD3D9");
			$("#btn_openLinkedContractFromActionBar").css("display", "none");
			$("#btn_openNewTabLinkedContractFromActionBar").css("display", "none");
			$("#btn_deleteLinkedContractFromActionBar").css("display", "inline");
            l_LinkedContractsList_model.linkedContractsList().forEach(function (iToken) {
                selectedLinkedContractsListMap[iToken.ContractLinksId()] = iToken.TargetContractItemId();
            });
			
			
			
        }
        else if (l_currentClassName == "cc-select-column cc-checkbox-select-all-on") {
            $(event.currentTarget).removeClass("cc-checkbox-select-all-on");
            $(event.currentTarget).addClass("cc-checkbox-select-all-off");
            $("#table_linkedContractsTable").find('tbody .cc-select-column').removeClass("cc-checkbox-on");
            $("#table_linkedContractsTable").find('tbody .cc-select-column').addClass("cc-checkbox-off");
            $("#table_linkedContractsTable").find('tbody tr').css("background-color", "transparent")
            $("#btn_openLinkedContractFromActionBar").css("display", "none");
			$("#btn_openNewTabLinkedContractFromActionBar").css("display", "none");
            $("#btn_deleteLinkedContractFromActionBar").css("display", "none");
            selectedLinkedContractsListMap = {};
        }
        event.stopPropagation();
    }
    
}

var LinkedContractDetailsModel = function () {
    var self = this;

    self.GeneratedContractId = ko.observable('');
    self.ContractName = ko.observable('');
    self.LinkType = ko.observable('');
    self.LinkDescription = ko.observable('');
    self.CreatedBy = ko.observable('');
    self.CreatedDate = ko.observable('');

    self.ContractLinksId = ko.observable('');
    self.SourceContractItemId = ko.observable(contractItemId);
	self.TargetContractItemId = ko.observable('');
}

var ContractsListModel = function () {
    var self = this;

	
    self.ContractsList = ko.observableArray([]);
    self.selectedContractName = ko.observable('');
	self.selectedDocumentId = ko.observable('');
    self.selectedNextExpiryDate = ko.observable('');
	self.selectedGeneratedContractId = ko.observable('');
    self.selectedActualStartDate = ko.observable('');
    self.selectedStatus = ko.observable('');
    self.selectedContractType = ko.observable('');
	self.selectedExternalParty = ko.observable('');

	self.currentPage = ko.observable(1);
    self.numOfContracts = ko.observable('');
    self.numOfContractsInCurrentPage = ko.observable('');
    self.numOfPages = ko.observable('');
    self.isFilterApplied = ko.observable(false);

    self.selectedContractItemId = ko.observable('');
	
	self.selectContractListRadioButton = function (iItem, event) {
        $(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
        if (iItem["Contract-id"]) {
            var l_itemId = iItem["Contract-id"].ItemId;
            self.selectedContractItemId(l_itemId);
            self.selectedContractName(iItem.ContractName);
			self.selectedDocumentId(iItem.ContractNumber);
			self.selectedNextExpiryDate(iItem.EndDate?moment(iItem.EndDate.replace('Z', '')).format('MM/DD/YYYY'):'');
			self.selectedGeneratedContractId(iItem.GeneratedContractId);
			self.selectedActualStartDate(iItem.MinStartdate?moment(iItem.MinStartdate.replace('Z', '')).format('MM/DD/YYYY'):'');
			self.selectedStatus(iItem.Lifecycle.CurrentState);
			self.selectedContractType(getTextValue(iItem.ContractType.Name));
			self.selectedExternalParty(getTextValue(iItem.RelatedCTRProps.AllExternalParties));
				   
        }
    }
    self.onContractListRowRadioButtonValueChanged = function (iItem, event) {
        $(event.currentTarget.parentElement.parentElement.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
        $(event.currentTarget).addClass("cc-radio-on");
        if (iItem["Contract-id"]) {
            var l_itemId = iItem["Contract-id"].ItemId;
            self.selectedContractItemId(l_itemId);
            self.selectedContractName(iItem.ContractName);
			self.selectedDocumentId(iItem.ContractNumber);
			self.selectedNextExpiryDate(iItem.EndDate?moment(iItem.EndDate.replace('Z', '')).format('MM/DD/YYYY'):'');
			self.selectedGeneratedContractId(iItem.GeneratedContractId);
			self.selectedActualStartDate(iItem.MinStartdate?moment(iItem.MinStartdate.replace('Z', '')).format('MM/DD/YYYY'):'');
			self.selectedStatus(iItem.Lifecycle.CurrentState);
			self.selectedContractType(getTextValue(iItem.ContractType.Name));
			self.selectedExternalParty(getTextValue(iItem.RelatedCTRProps.AllExternalParties));
        }
        event.stopPropagation();
    }
}

var LinkTypesModel = function () {
    var self = this;
	self.LinkTypestoAdd = ko.observableArray([]);
    self.selectedLinkTypeID = ko.observable('');
	self.selectedContract = ko.observable('');
	self.LinkComments = ko.observable('');
}

var ContractsListFilterModel = function () {
    var self = this;
    var l_contractNameFilterElement = document.getElementById("input_contractNameFilter");
    var l_contractIdFilterElement = document.getElementById("input_contractIdFilter");
    
    self.ClearContractsListFilter = function () {
        l_contractNameFilterElement.value = "";
        l_contractIdFilterElement.value = "";
        
    }
    self.getContractsListFilterObject = function () {
        self.CurrentFilterObject = {
            "RelatedOrgID": RelatedcontractOrgId,
            "SourceContractID": contractItemId.split('.')[1],
            "ContractName": l_contractNameFilterElement.value,
            "GeneratedContractID": l_contractIdFilterElement.value,
            "offset": contractsListOffsetValue,
			"limit": contractsListLimitValue
        };
        return self.CurrentFilterObject;
    }
}

$(function () {
    var i_locale = getlocale();
    translateLabels("com/opentext/apps/contractcenter/ContractCenter/ContractCenter", i_locale);
	var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
	loadRTLIfRequired(i_locale,rtl_css);
	$('[src*="linkedContractsList.htm"]', window.parent.parent.document).parent().css('padding-left', '0px');
	
	contractItemId = getUrlParameterValue("instanceId", null, true);
	RelatedcontractOrgId = getUrlParameterValue("relatedOrgId", null, true);
	BASEURL = getUrlParameterValue("baseURL", null, true);

    createToastDiv();
    l_LinkedContractsList_model = new LinkedContractsListModel();
    ko.applyBindings(l_LinkedContractsList_model, document.getElementById("div_linkedContractsListData"));
    l_LinkTypesList_model = new LinkTypesModel();
    ko.applyBindings(l_LinkTypesList_model, document.getElementById("div_linkTypes"));
    ko.applyBindings(l_LinkTypesList_model, document.getElementById("linkComments"));
	ko.applyBindings(l_LinkTypesList_model, document.getElementById("select_linkContract"));
	ko.applyBindings(l_LinkTypesList_model, document.getElementById("div_contractDetails"));
	l_ContractsList_model = new ContractsListModel();
    ko.applyBindings(l_ContractsList_model, document.getElementById("div_contractListData"));
	ko.applyBindings(l_ContractsList_model, document.getElementById("contractListPagination"));
	ko.applyBindings(l_ContractsList_model, document.getElementById("btn_selectContractForLinkYes"));
	l_contractsListFilter_model = new ContractsListFilterModel();
    ko.applyBindings(l_contractsListFilter_model, document.getElementById("div_contractListFilter"));

	checkForUserRole();
    hideContractsListFilter();
    listLinkedContracts();

	listLinkTypes();
	
    $("#btn_filterContractList").click(function (iEventObject) {
        if ($("#div_contractListFilter").attr('apps-toggle') == "expanded") {
            $("#div_contractListFilter").toggle();
            document.getElementById("div_contractListFilter").setAttribute("apps-toggle", 'collapsed');
            $("#div_contractListData").removeClass("col-md-9");
            $("#div_contractListData").addClass("col-md-12");
        }
        else if ($("#div_contractListFilter").attr('apps-toggle') == "collapsed") {
            $("#div_contractListFilter").toggle();
            //setTimeout(function () { $("#div_contractListFilter").toggle('slow'); }, 0);
            document.getElementById("div_contractListFilter").setAttribute("apps-toggle", 'expanded');
            $("#div_contractListData").removeClass("col-md-12");
            $("#div_contractListData").addClass("col-md-9");
        }
    });
    $(".cc-filter-header").click(function (iEventObject) {
        var l_headerSpan = $(this)
        l_headerSpan.next().slideToggle();
        if (l_headerSpan.attr('apps-toggle') == "expanded") {
            hideOrShowContractsFilterContainerBody(l_headerSpan[0], false);
        }
        else if (l_headerSpan.attr('apps-toggle') == "collapsed") {
            hideOrShowContractsFilterContainerBody(l_headerSpan[0], true);
        }
    });
});

function hideOrShowContractsFilterContainerBody(iElement, iShow) {
    if (iShow) {
        iElement.setAttribute("apps-toggle", 'expanded');
        iElement.lastElementChild.src = "../../../../../com/opentext/apps/utils/img/caret_up.svg";
    }
    else {
        iElement.setAttribute("apps-toggle", 'collapsed');
        iElement.lastElementChild.src = "../../../../../com/opentext/apps/utils/img/caret_down.svg";
    }
}

function listLinkedContracts() {
    $("#btn_openLinkedContractFromActionBar").css("display", "none");
	$("#btn_openNewTabLinkedContractFromActionBar").css("display", "none");
    $("#btn_deleteLinkedContractFromActionBar").css("display", "none");
    $("#div_selectAllLinkedContracts").removeClass("cc-checkbox-select-all-partial");
    $("#div_selectAllLinkedContracts").removeClass("cc-checkbox-select-all-on");
    $("#div_selectAllLinkedContracts").addClass("cc-checkbox-select-all-off");
    selectedLinkedContractsListMap = {};
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
        method: "GetLinkedSourceContracts",
        parameters: {
			"sourceContractID":contractItemId.split('.')[1]
		},
        success: function (data) {
            addDataToLinkedContractsListView(data.linkedContracts.FindLinkedSourceContractsResponse.ContractLinks, l_LinkedContractsList_model);
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving the linked contracts. Contact your administrator."), 10000);
            return false;
        }
    });
}

function addDataToLinkedContractsListView(iElementList, iModel) {
    iModel.linkedContractsList.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            iElementList.forEach(function (iElement) {
                iModel.linkedContractsList.push(_populateLinkedContractsListData(iElement));
            });
			iModel.numOfLinkedContractsInCurrentPage(iElementList.length);
        }
        else {
            iModel.linkedContractsList.push(_populateLinkedContractsListData(iElementList));
			iModel.numOfLinkedContractsInCurrentPage(1);
        }
    }
}

function _populateLinkedContractsListData(iElement) {
    l_LinkedContractDetails_Obj = new LinkedContractDetailsModel();
    l_LinkedContractDetails_Obj.GeneratedContractId (getTextValue(iElement.TargetContract.GeneratedContractId));
    l_LinkedContractDetails_Obj.ContractName(getTextValue(iElement.TargetContract.ContractName));
    l_LinkedContractDetails_Obj.LinkType(getTextValue(iElement.RelatedLinkType.LinkType));
    l_LinkedContractDetails_Obj.LinkDescription(getTextValue(iElement.Comments));
    l_LinkedContractDetails_Obj.CreatedBy(getTextValue(iElement.CreatedBy.Name));
    l_LinkedContractDetails_Obj.CreatedDate(dateConverter(new Date(getTextValue(iElement.Tracking.CreatedDate))));


    l_LinkedContractDetails_Obj.ContractLinksId(getTextValue(iElement["ContractLinks-id"].Id));
    l_LinkedContractDetails_Obj.TargetContractItemId(getTextValue(iElement.TargetContract["Contract-id"].ItemId));

    return l_LinkedContractDetails_Obj;
}

function openLinkedContractCreateForm() {
    $('#div_createLinkedContractsModal').modal({
        backdrop: 'static',
        keyboard: false
    })
	clearLinkContractModel();
    $('button#btn_createContractLink').off("click");
    $('button#btn_createContractLink').on('click', function (_event) {
        createContractLink();
    });
}
function createContractLink() {
    if (validateContractLinkMandatoryFields()) {
       
                $.cordys.ajax({
                    method: "CreateContractLinks",
                    namespace: "http://schemas/OpenTextContractCenter/ContractLinks/operations",
                    parameters: {
						"ContractLinks-create":{
							"@xmlns":"http://schemas/OpenTextContractCenter/ContractLinks",
							"Comments":l_LinkTypesList_model.LinkComments,
							"RelatedLinkType":{
								"LinkTypes-id":{
									"ItemId":l_LinkTypesList_model.selectedLinkTypeID()
								}
							},
							"SourceContract":{
								"Contract-id":{
									"ItemId":contractItemId
								}
							},
							"TargetContract":{
								"Contract-id":{
									"ItemId":l_LinkTypesList_model.selectedContract().contractItemId
								}
							}
						}
					},
                    success: function (responseSuccess) {
                        if (responseSuccess) {
                            $('#div_createLinkedContractsModal').modal('hide');
                            successToast(3000, "Contract link created.");
                            listLinkedContracts();
                        } else {
                            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while creating the link. Contact your administrator."), 10000);
                        }
                    },
                    error: function (responseFailure) {
						if(responseFailure.responseText.includes("Cannot create the link because a parent contract exists for the source and target contracts."))
							showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("Cannot create the link because a parent contract exists for the source and target contracts."), 10000);
						else
							showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while creating the link. Contact your administrator."), 10000);
                        return false;
                    }
                });
            
        
    }
}

function validateContractLinkMandatoryFields() {
    var validationFlag = true;
    if (l_LinkTypesList_model.selectedLinkTypeID() == "") {
        $("#select_linkType").addClass("cc-error");
        validationFlag = false;
    }
    if (l_LinkTypesList_model.selectedContract() == "") {
        $("#select_linkContract").addClass("cc-error");
        validationFlag = false;
    }
    return validationFlag;
}

function removeErrorClass(iEvent) {
    $(iEvent).removeClass("cc-error");
}

function listLinkTypes() {
    $.cordys.ajax({
        namespace: "http://schemas/OpenTextContractCenter/LinkTypes/operations",
        method: "GetAllLinkTypes",
        parameters:{},
        success: function (data) {
            addDataToLinkTypeSelectView(data.LinkTypes, l_LinkTypesList_model);
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving the linked contracts. Contact your administrator."), 10000);
            return false;
        }
    });
}
function addDataToLinkTypeSelectView(iElementList, iModel){
	iModel.LinkTypestoAdd.removeAll();
	iModel.LinkTypestoAdd.push({ "linkTypeId": "", "LinkType": getTranslationMessage("Select a value") });
    if (iElementList) {
        if (iElementList.length) {
            iElementList.forEach(function (iElement) {
                iModel.LinkTypestoAdd.push({"linkTypeId":getTextValue(iElement["LinkTypes-id"].ItemId), "LinkType":getTextValue(iElement.LinkType)});
            });
        }
        else {
             iModel.LinkTypestoAdd.push({"linkTypeId":getTextValue(iElementList["LinkTypes-id"].ItemId), "LinkType":getTextValue(iElementList.LinkType)});
        }
    }
}

function openContractSelectionModal() {
    $("#div_selectContractModal").modal({
        backdrop: 'static',
        keyboard: false
    });
	ClearContractsListFilter();
	l_ContractsList_model.currentPage('1');
    ListContracts();
    $('button#btn_selectContractForLinkYes').off("click");
    $('button#btn_selectContractForLinkYes').on('click', function (_event) {
        l_LinkTypesList_model.selectedContract(getSelectedContract());
		 $("#select_linkContract").removeClass("cc-error");
    });
	
}

function getSelectedContract(){
	var selectedContract={
		"contractName":l_ContractsList_model.selectedContractName(),
		"contractType":l_ContractsList_model.selectedContractType(),
		"externalParty":l_ContractsList_model.selectedExternalParty(),
		"actualStartDate":l_ContractsList_model.selectedActualStartDate(),
		"status":l_ContractsList_model.selectedStatus(),
		"nextExpiryDate":l_ContractsList_model.selectedNextExpiryDate(),
		"documentID":l_ContractsList_model.selectedDocumentId(),
		"generatedContractID":l_ContractsList_model.selectedGeneratedContractId(),
		"contractItemId":l_ContractsList_model.selectedContractItemId()
	}
	return selectedContract;
}

function clearContractsListSelect(){
	l_ContractsList_model.selectedContractName(''),
	l_ContractsList_model.selectedContractType(''),
	l_ContractsList_model.selectedExternalParty(''),
	l_ContractsList_model.selectedActualStartDate(''),
	l_ContractsList_model.selectedStatus(''),
	l_ContractsList_model.selectedNextExpiryDate(''),
	l_ContractsList_model.selectedDocumentId(''),
	l_ContractsList_model.selectedGeneratedContractId(''),
	l_ContractsList_model.selectedContractItemId('')
}

function clearLinkContractModel(){
	$("#select_linkContract").removeClass("cc-error");
	$("#select_linkType").removeClass("cc-error");
	l_LinkTypesList_model.selectedLinkTypeID ('');
	l_LinkTypesList_model.selectedContract('');
	l_LinkTypesList_model.LinkComments('');
}

function ListContracts() {
	clearContractsListSelect();
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
        method: "GetCTRListForLinking",
        parameters: l_contractsListFilter_model.getContractsListFilterObject(),
        success: function (data) {
			
            addDataToContractsLookup(data.FindTargetOfContractsListCLResponse.Contract, l_ContractsList_model);
			if (undefined != data.FindTargetOfContractsListCLResponse["@total"]) {
				
					l_ContractsList_model.numOfContracts(data.FindTargetOfContractsListCLResponse["@total"]);
            } else {
                l_ContractsList_model.numOfContracts(0);
            }
			
            if (l_ContractsList_model.numOfContracts() != 0) {
                l_ContractsList_model.numOfPages(Math.ceil(l_ContractsList_model.numOfContracts() / contractsListLimitValue));
            } else {
                l_ContractsList_model.numOfPages(1);
            }
            updatePaginationParams();
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving the organizations. Contact your administrator."), 10000);
            return false;
        }
    });
}

function addDataToContractsLookup(iElementList, iModel) {
    iModel.ContractsList.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            iElementList.forEach(function (iElement) {
                iModel.ContractsList.push(iElement);
            });
        }
        else {
            iModel.ContractsList.push(iElementList);
        }
    }
}

function ApplyFilterOnContractsList(event, iSrcElement) {
    if (document.getElementById("input_contractNameFilter").value != "" || document.getElementById("input_contractIdFilter").value != "") {
        $("#btn_clearFilterActionBar").css('display', 'inline');
        l_ContractsList_model.isFilterApplied(true);
    } else {
        $("#btn_clearFilterActionBar").css('display', 'none');
        l_ContractsList_model.isFilterApplied(false);
    }

	contractsListOffsetValue=0;
    ListContracts();
    hideContractsListFilter();
}
function ClearContractsListFilter(event, iSrcElement) {
    l_contractsListFilter_model.ClearContractsListFilter();
    $("#btn_clearFilterActionBar").css('display', 'none');
    l_ContractsList_model.isFilterApplied(false);
	contractsListOffsetValue=0;
    ListContracts();
    hideContractsListFilter();
}

function hideContractsListFilter() {
    $("#div_contractListFilter").hide();
    document.getElementById("div_contractListFilter").setAttribute("apps-toggle", 'collapsed');
    $("#div_contractListData").removeClass("col-md-9");
    $("#div_contractListData").addClass("col-md-12");
}

function deleteLinkFromActionBar() {
    $("#div_deleteLinkModal").modal({
        backdrop: 'static',
        keyboard: false
    });
    $("#span_NumOfLinksToDelete").text("");
    $('button#btn_deleteLinkYes').off("click");
    $('button#btn_deleteLinkYes').on('click', function (_event) {
        for (iElement in selectedLinkedContractsListMap) {
            deleteLink(iElement);
        }
    });
}

function deleteLink(iItemID) {

    $.cordys.ajax({
        namespace: "http://schemas/OpenTextContractCenter/ContractLinks/operations",
        method: "DeleteContractLinks",
        parameters:
        {
            "ContractLinks": {
				"ContractLinks-id":{
					"Id":iElement
				}					
			}
        },
        success: function (data) {
            successToast(3000, getTranslationMessage("Link(s) deleted."));
            listLinkedContracts();
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while deleting the link. Contact your administrator."), 10000);
            return false;
        }
    });
}

function callOpenLinkedContract(target) {
    if (Object.keys(selectedLinkedContractsListMap).length == 1) {
		if(target)
			window.open(BASEURL + "app/start/web/perform/item/"+ selectedLinkedContractsListMap[Object.keys(selectedLinkedContractsListMap)[0]] + "/F8B156D635F3A1E89CB08DDB9883E4C8", "_blank");
		else
			navigateToInstance(selectedLinkedContractsListMap[Object.keys(selectedLinkedContractsListMap)[0]],"Contract");
    }
}

function dateConverter(str){
   var date = new Date(str),
   mnth = ("0" + (date.getMonth()+1)).slice(-2),
   day  = ("0" + date.getDate()).slice(-2);
   hours  = ("0" + date.getHours()).slice(-2);
   minutes = ("0" + date.getMinutes()).slice(-2);
   seconds  = ("0" + date.getSeconds()).slice(-2);
   year = date.getFullYear();
   return `${year}/${mnth}/${day}`
}

function updateLimitValue(iElement) {
    contractsListOffsetValue = 0;
    l_ContractsList_model.currentPage('1');
    contractsListLimitValue = $(iElement).val();
    ListContracts();
}

function updatePaginationParams() {
	
    if (l_ContractsList_model.currentPage() == 1) {
		document.getElementById("li_contractListLeftNavigation").style.display = "none";
		document.getElementById("li_contractListRightNavigation").style.display = "inline";
    }
    if (parseInt(l_ContractsList_model.numOfContracts()) <= parseInt(contractsListLimitValue)) {
		l_ContractsList_model.currentPage('1');
        $('#li_contractListLeftNavigation,#li_contractListRightNavigation').css('display', 'none');
    }
}

function goToPreviousPage() {
    if (l_ContractsList_model.currentPage() > 1) {
        contractsListOffsetValue = parseInt(contractsListOffsetValue) - parseInt(contractsListLimitValue);
        l_ContractsList_model.currentPage(parseInt(l_ContractsList_model.currentPage()) - 1);
    }
    if (l_ContractsList_model.currentPage() < Math.ceil(l_ContractsList_model.numOfContracts() / contractsListLimitValue)) {
        document.getElementById("li_contractListRightNavigation").style.removeProperty("display");
    }
    if (l_ContractsList_model.currentPage() == 1) {
        document.getElementById("li_contractListLeftNavigation").style.display = "none";
    }
    if (l_ContractsList_model.currentPage() < 1)
        return;
    ListContracts();
}
function goToNextPage() {
    if (l_ContractsList_model.currentPage() < Math.ceil(l_ContractsList_model.numOfContracts() / contractsListLimitValue)) {
        contractsListOffsetValue = parseInt(contractsListOffsetValue) + parseInt(contractsListLimitValue);
        l_ContractsList_model.currentPage(isNaN(parseInt(l_ContractsList_model.currentPage())) ? 0 : parseInt(l_ContractsList_model.currentPage()));
        l_ContractsList_model.currentPage(parseInt(l_ContractsList_model.currentPage()) + 1);
    }
    if (l_ContractsList_model.currentPage() == Math.ceil(l_ContractsList_model.numOfContracts() / contractsListLimitValue)) {
        document.getElementById("li_contractListRightNavigation").style.display = "none";
    }
    if (l_ContractsList_model.currentPage() > 1) {
        document.getElementById("li_contractListLeftNavigation").style.removeProperty("display");
    }
    ListContracts();
}
function goToLastPage() {
    contractsListOffsetValue = (Math.ceil(l_ContractsList_model.numOfContracts() / contractsListLimitValue) - 1) * contractsListLimitValue;
    l_ContractsList_model.currentPage(Math.ceil(l_ContractsList_model.numOfContracts() / contractsListLimitValue));
    $('#li_contractListRightNavigation').css('display', 'none');
    $('#li_contractListLeftNavigation').css('display', 'inline');
    ListContracts();
}
function goToFirstPage() {
    contractsListOffsetValue = 0;
    l_ContractsList_model.currentPage('1');
    $('#li_contractListRightNavigation').css('display', 'inline');
    $('#li_contractListLeftNavigation').css('display', 'none');
    ListContracts();
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
            } else {
                document.getElementById("btn_createContractLinkButton").disabled =true;
				document.getElementById("btn_deleteLinkedContractFromActionBar").disabled =true;
            }
        }).fail(function (error) {
        })
}