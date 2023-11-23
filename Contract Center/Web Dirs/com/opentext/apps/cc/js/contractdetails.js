$.cordys.json.defaults.removeNamespacePrefix = true;
var contractItemID;
var contractID;

const CUSTOM_TAB_URL = "CUSTOM_TAB_URL";
const CUSTOM_TAB_NAME = "CUSTOM_TAB_NAME";
const PARAM_CONTRACT_ID = "{item.Identity.Id}";
const PARAM_CONTRACT_ITEMID = "{item.Identity.ItemId}";

const LOAD_ON_TAB_CHANGE = false;
const leftNavList =
	[
		{ Name: "General", label: "General", display: true },
		{ Name: "Accounts", label: "Accounts", display: true },
		{ Name: "Custom attributes", label: "Custom attributes", display: true },
		{ Name: "Contract lines", label: "Contract lines", display: true },
		{ Name: "Renewal", label: "Renewal", display: true },
		{ Name: "Termination", label: "Termination", display: true },
		{ Name: "Other details", label: "Other details", display: true },
		{ Name: "Tags", label: "Tags", display: true },
		{ Name: "Custom tab details", label: "Custom tab details", display: false }
	];

function ContractDetailsNavModel() {
	var self = this;

	// All the contract details tabs.
	self.leftNavList = ko.mapping.fromJS(leftNavList);

	// By default first tab will be selected.
	self.currentTabIndex = ko.observable(0);

	// Select the tab and load content of the tab.
	self.selectTab = function (index) {
		if(index!=self.currentTabIndex()){
			if(index==8){
				$("#custom_tab_div").show();
			}
			if(self.currentTabIndex()==8){
				$("#custom_tab_div").hide()
			}else{
				all_page_models[self.currentTabIndex()].displayScreen(false);
			}
			self.currentTabIndex(index);
			all_page_models[self.currentTabIndex()].displayScreen(true);
			if (LOAD_ON_TAB_CHANGE || (!LOAD_ON_TAB_CHANGE && !all_page_models[self.currentTabIndex()].isLoaded())) {
				all_page_models[index].loadPageContent();
			}
		}
	};

}

function ContractDetailsInfoModel() {
	var self = this;
	self.contractID = ko.observable("");
	self.contractName = ko.observable("");
	self.description = ko.observable("");
	self.organizationName = ko.observable("");
	self.PONumbers = ko.observableArray();
	self.priority = ko.observable("");
	self.contractNumber = ko.observable("");
	self.isExecutedContract = ko.observable("");
	self.isExternal = ko.observable("");
	self.contractType = ko.observable("");
	self.templateName = ko.observable("");
	self.templateType = ko.observable("");
	self.internalPartyName = ko.observable("");
	self.internalContactName = ko.observable("");
	self.externalPartyName = ko.observable("");
	self.externalContactName = ko.observable("");
	self.accountManagerName = ko.observable("");
	self.accountNumber = ko.observable("");
	self.country = ko.observable("");
	self.region = ko.observable("");
	self.contractTermInMonths = ko.observable("");
	self.isPerpetual = ko.observable("");
	self.plannedStartDate = ko.observable("");
	self.actualStartDate = ko.observable("");
	self.currentTermStartDate = ko.observable("");
	self.currentTermExpirationDate = ko.observable("");
	self.initialExpirationDate = ko.observable("");
	self.nextExpirationDate = ko.observable("");
	self.notifyBeforeExpirationInDays = ko.observable("");
	self.actBeforeExpirationInDdays = ko.observable("");
	self.signatureDate = ko.observable("");
	self.currency = ko.observable("");
	self.contractValueInUSD = ko.observable("");
	self.contractValue = ko.observable("");
	self.watchersList = ko.observableArray();
	self.watcherAdded = ko.observableArray();

	self.isLoaded = ko.observable(false);
	self.displayScreen = ko.observable(true);

	self.addWatcher = function () {
		cc_details_services.AddWatcherToContractById(contractID, function (response_data, status) {
			if (status !== "ERROR") {
				successToast(2000, "You have successfully added yourself as watcher");
				self.watcherAdded(true);
			}
		});
	}
	self.removeWatcher = function () {
		cc_details_services.RemoveWatcherToContractById(contractID, function (response_data, status) {
			if (status !== "ERROR") {
				successToast(2000, "You have successfully removed yourself as watcher");
				self.watcherAdded(false);
			}
		});
	}
	self.getContractWatchers = function () {
		cc_details_services.GetContractWatchers({ "contractId": contractID }, function (response_data, status) {
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
		$("#id_watchersListDialog").modal();
		self.getContractWatchers();
	}

	self.loadPageContent = function () {
		self.fetchContractDetails();
	}

	self.fetchContractDetails = function () {
		if (LOAD_ON_TAB_CHANGE || (!LOAD_ON_TAB_CHANGE && !self.isLoaded())) {
			self.isLoaded(true);
			cc_details_services.GetContractDetailsById(contractID, function (response_data, status) {
				if (status !== "ERROR") {
					$('#loadingMsg').css('display', 'none');
					_populateProcessResponseData(response_data.Contract, self);
				}
			});
		}
	}

	function _populateProcessResponseData(iContract, contractModel) {

		contractModel.contractID(getTextValue(iContract.GeneratedContractId));
		contractModel.contractName(getTextValue(iContract.ContractName));
		contractModel.description(getTextValue(iContract.Description));

		if(iContract.relatedPOs && iContract.relatedPOs.PONumber){
			if(iContract.relatedPOs.PONumber.length){
				iContract.relatedPOs.PONumber.forEach(function(iElement){
					contractModel.PONumbers.push(getTextValue(iElement));

			});
		}else{
			contractModel.PONumbers.push(getTextValue(iContract.relatedPOs.PONumber));
		}
	}
		contractModel.priority(getTextValue(iContract.Priority));
		contractModel.contractNumber(getTextValue(iContract.ContractNumber));
		contractModel.watcherAdded('true' === getTextValue(iContract.watcheradded) ? true : false);

		// change
		contractModel.isExecutedContract(getTextValue(iContract.IsExecuted));
		contractModel.isExternal(getTextValue(iContract.ContractDocumentType));
		var l_contractTermDuration = getTextValue(iContract.InitialContractTenure)
		if (l_contractTermDuration.lastIndexOf("M") > 0 && l_contractTermDuration.lastIndexOf("D") > 0) {
			var l_contractTermDuration_Months = getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("M")));
			var l_contractTermDuration_Days = getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("M") + 1, l_contractTermDuration.lastIndexOf("D")));
			if (l_contractTermDuration_Months > 0 && l_contractTermDuration_Days > 0) {
				contractModel.contractTermInMonths(l_contractTermDuration_Months + " months, " + l_contractTermDuration_Days + " days");
			} else if (l_contractTermDuration_Months > 0) {
				contractModel.contractTermInMonths(l_contractTermDuration_Months + " months");
			} else if (l_contractTermDuration_Days > 0) {
				contractModel.contractTermInMonths(l_contractTermDuration_Days + " days");
			} else {
				contractModel.contractTermInMonths("No duration specified.");
			}
		} else if (l_contractTermDuration.lastIndexOf("M") > 0 && getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("M"))) > 0) {
			contractModel.contractTermInMonths(getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("M"))) + " months");
		} else if (l_contractTermDuration.lastIndexOf("D") > 0 && getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("D"))) > 0) {
			contractModel.contractTermInMonths(getTextValue(l_contractTermDuration.substring(l_contractTermDuration.lastIndexOf("P") + 1, l_contractTermDuration.lastIndexOf("D"))) + " days");
		} else {
			contractModel.contractTermInMonths("No duration specified.");
		}
		contractModel.isPerpetual(getTextValue(iContract.Perpetual));
		contractModel.contractValueInUSD(getTextValue(iContract.ContractValueUSD) ? formateCurrencyInUSD(iContract.ContractValueUSD) : '');
		contractModel.contractValue(getTextValue(iContract.ContractValue) ? formateNumbertoLocale(iContract.ContractValue) : '');

		if (iContract.RelatedOrganization) {
			contractModel.organizationName(getTextValue(iContract.RelatedOrganization.Name));
		}

		if (iContract.ContractType) {
			contractModel.contractType(getTextValue(iContract.ContractType.Name));
		}

		if (iContract.RelatedTemplate) {
			contractModel.templateName(getTextValue(iContract.RelatedTemplate.Name));
			contractModel.templateType(getTextValue(iContract.RelatedTemplate.TemplateType));
		}
		if (iContract.FirstParty) {
			contractModel.internalPartyName(getTextValue(iContract.FirstParty.RegisteredName));
		}
		if (iContract.FirstPartyContact && iContract.FirstPartyContact.ContainingPerson) {
			contractModel.internalContactName(getTextValue(iContract.FirstPartyContact.ContainingPerson.DisplayName));
		}

		if (iContract.SecondParty) {
			contractModel.externalPartyName(getTextValue(iContract.SecondParty.RegisteredName));
			// Managers or Z_INT_ManagerName
			contractModel.accountManagerName(getTextValue(iContract.SecondParty.Z_INT_ManagerName));
			contractModel.accountNumber(getTextValue(iContract.SecondParty.Z_Int_CollectionAccount));
		}
		if (iContract.SecondPartyContact && iContract.SecondPartyContact.ContainingPerson) {
			contractModel.externalContactName(getTextValue(iContract.SecondPartyContact.ContainingPerson.DisplayName));
		}
		if (iContract.Country) {
			if (iContract.Country.LinkedCountry) {
				contractModel.country(getTextValue(iContract.Country.LinkedCountry.Country_Name));
			}
			if (iContract.Country.Owner) {
				contractModel.region(getTextValue(iContract.Country.Owner.Name));
			}
		}

		if (iContract.Currency) {
			contractModel.currency(getTextValue(iContract.Currency.Name));
		}
		// Dates
		contractModel.plannedStartDate(getTextValue(iContract.StartDate) ? formateDatetoLocale(getTextValue(iContract.StartDate)) : '');
		contractModel.actualStartDate(getTextValue(iContract.MinStartdate) ? formateDatetoLocale(getTextValue(iContract.MinStartdate)) : '');
		contractModel.currentTermStartDate(getTextValue(iContract.CurrentStartDate) ? formateDatetoLocale(getTextValue(iContract.CurrentStartDate)) : '');
		contractModel.currentTermExpirationDate(getTextValue(iContract.CurrentEndDate) ? formateDatetoLocale(getTextValue(iContract.CurrentEndDate)) : '');
		contractModel.initialExpirationDate(getTextValue(iContract.InitialExpiryDate) ? formateDatetoLocale(getTextValue(iContract.InitialExpiryDate)) : '');
		contractModel.nextExpirationDate(getTextValue(iContract.NextExpirationDate) ? formateDatetoLocale(getTextValue(iContract.NextExpirationDate)) : '');
		contractModel.signatureDate(getTextValue(iContract.SignatureDate) ? formateDatetoLocale(getTextValue(iContract.SignatureDate)) : '');
		contractModel.notifyBeforeExpirationInDays(getTextValue(iContract.NotificationDuration) ? formateNumbertoLocale(iContract.NotificationDuration) : '');
		contractModel.actBeforeExpirationInDdays(getTextValue(iContract.ActionDuration) ? formateNumbertoLocale(iContract.ActionDuration) : '');

		// Loading renewal Information.
		l_contract_renewal_info_model.isAutoRenew(getTextValue(iContract.AutoRenew));
		var l_autoRenewDuration = getTextValue(iContract.AutoRenewDuration)
		if (l_autoRenewDuration.lastIndexOf("M") > 0 && l_autoRenewDuration.lastIndexOf("D") > 0) {
			var l_autoRenewDuration_Months = getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("M")));
			var l_autoRenewDuration_Days = getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("M") + 1, l_autoRenewDuration.lastIndexOf("D")))
			if (l_autoRenewDuration_Months > 0 && l_autoRenewDuration_Days > 0) {
				l_contract_renewal_info_model.autoRenewDurationInMonths(l_autoRenewDuration_Months + " months, " + l_autoRenewDuration_Days + " days");
			} else if (l_autoRenewDuration_Months > 0) {
				l_contract_renewal_info_model.autoRenewDurationInMonths(l_autoRenewDuration_Months + " months");
			} else if (l_autoRenewDuration_Days > 0) {
				l_contract_renewal_info_model.autoRenewDurationInMonths(l_autoRenewDuration_Days + " days");
			} else {
				l_contract_renewal_info_model.autoRenewDurationInMonths("No duration specified.");
			}
		} else if (l_autoRenewDuration.lastIndexOf("M") > 0 && getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("M"))) > 0) {
			l_contract_renewal_info_model.autoRenewDurationInMonths(getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("M"))) + " months");
		} else if (l_autoRenewDuration.lastIndexOf("D") > 0 && getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("D"))) > 0) {
			l_contract_renewal_info_model.autoRenewDurationInMonths(getTextValue(l_autoRenewDuration.substring(l_autoRenewDuration.lastIndexOf("P") + 1, l_autoRenewDuration.lastIndexOf("D"))) + " days");
		} else {
			l_contract_renewal_info_model.autoRenewDurationInMonths("No duration specified.");
		}
		l_contract_renewal_info_model.renewalDiscount(getTextValue(iContract.RenewalDiscount) ? formateNumbertoLocale(iContract.RenewalDiscount) : '');
		l_contract_renewal_info_model.renewalComments(getTextValue(iContract.RenewalComments));
		if (iContract.RenewalFlagStatus) {
			l_contract_renewal_info_model.renewalFlag(getTextValue(iContract.RenewalFlagStatus.Name));
		}
		l_contract_renewal_info_model.isLoaded(true);

		// Loading termination information.
		if (iContract.ClientEarlyTermRight) {
			l_contract_termination_info_model.clientEarlyTermRight(iContract.ClientEarlyTermRight);
		}
		if (iContract.TerminationReason) {
			l_contract_termination_info_model.terminationReason(getTextValue(iContract.TerminationReason.Reason));
		}
		l_contract_termination_info_model.terminationFees(getTextValue(iContract.TerminationFees) ? formateNumbertoLocale(iContract.TerminationFees) : '');
		l_contract_termination_info_model.earlyTerminationConditions(getTextValue(iContract.EarlyTerminationConditions));
		l_contract_termination_info_model.cancellationDate(getTextValue(iContract.CancellationDate) ? formateDatetoLocale(getTextValue(iContract.CancellationDate)) : '');
		l_contract_termination_info_model.cancellationComments(getTextValue(iContract.CancellationComments));
		l_contract_termination_info_model.terminationNoticePeriod(getTextValue(iContract.TerminationNoticePeriod) ? formateNumbertoLocale(iContract.TerminationNoticePeriod) : '');
		l_contract_termination_info_model.isLoaded(true);

		// Loading other details of contract.
		l_contract_other_details_info_model.originalSalesAccountExecutive(getTextValue(iContract.OriginalSalesAccountExecutive));
		if (iContract.DocumentOrigination) {

			l_contract_other_details_info_model.documentOriginationName(getTextValue(iContract.DocumentOrigination.Name));
		}
		l_contract_other_details_info_model.dealManager(getTextValue(iContract.DealManager));
		l_contract_other_details_info_model.comments(getTextValue(iContract.Comments));
		l_contract_other_details_info_model.endUser(getTextValue(iContract.EndUser));
		l_contract_other_details_info_model.SAPOrderID(getTextValue(iContract.SAPOrderID));
		if (iContract.PriceProtection) {
			l_contract_other_details_info_model.priceProtection(getTextValue(iContract.PriceProtection == '1') ? true : false);
		}
		l_contract_other_details_info_model.CRMQuoteID(getTextValue(iContract.CRMQuoteID));
		if (iContract.DefaultDocRelation && iContract.DefaultDocRelation.File) {
			l_contract_other_details_info_model.defaultDocRelationFileName(getTextValue(iContract.DefaultDocRelation.File.FileName));
		}
		l_contract_other_details_info_model.productGoLiveDate(getTextValue(iContract.ProductGoLiveDate) ? formateDatetoLocale(getTextValue(iContract.ProductGoLiveDate)) : '');
		l_contract_other_details_info_model.priceProtectionDate(getTextValue(getTextValue(iContract.PriceProtectionDate) ? formateDatetoLocale(getTextValue(iContract.PriceProtectionDate)) : ''));
		l_contract_other_details_info_model.CRMOpportunityID(getTextValue(iContract.CRMOpportunityID));

		if (iContract.Validated) {
			l_contract_other_details_info_model.validated(getTextValue(iContract.Validated));
		}
		l_contract_other_details_info_model.validatedOn(getTextValue(iContract.ValidatedOn) ? formateDatetoLocale(getTextValue(iContract.ValidatedOn)) : '');
		l_contract_other_details_info_model.validatedBy(getTextValue(iContract.ValidatedBy));
		l_contract_other_details_info_model.customerManagerComments(getTextValue(iContract.CustomerManagerComments));
		l_contract_other_details_info_model.isLoaded(true);
		
		if (iContract.AmendType) {
			l_contract_other_details_info_model.amendmentType(getTextValue(iContract.AmendType.Name));
		}
		
		if (iContract.BWtemplateID) {
			l_contract_other_details_info_model.BWTemplateID(getTextValue(iContract.BWtemplateID));
		}
		// Get custom tab details.
		l_contract_custom_tab_info_model.fetchContractCustomTabDetails();
		
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
		if (LOAD_ON_TAB_CHANGE || (!LOAD_ON_TAB_CHANGE && !self.isLoaded())) {
			self.isLoaded(true);
			cc_details_services.GetContractLinesById(contractID, function (response_data, status) {
				if (status !== "ERROR") {
					_addContractLinesData(response_data.ContractLines, self);
				}
			});
		}
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

function ContractRenewalInfoModel() {
	var self = this;

	self.isAutoRenew = ko.observable(false);
	self.autoRenewDurationInMonths = ko.observable("");
	self.renewalDiscount = ko.observable("");
	self.renewalComments = ko.observable("");
	self.renewalFlag = ko.observable("");

	self.isLoaded = ko.observable(false);
	self.displayScreen = ko.observable(false);
	self.loadPageContent = function () {
	}
}

function ContractTerminationInfoModel() {
	var self = this;

	self.clientEarlyTermRight = ko.observable(false);
	self.terminationFees = ko.observable("");
	self.earlyTerminationConditions = ko.observable("");
	self.cancellationDate = ko.observable("");
	self.terminationReason = ko.observable("");
	self.cancellationComments = ko.observable("");
	self.terminationNoticePeriod = ko.observable("");

	self.isLoaded = ko.observable(false);
	self.displayScreen = ko.observable(false);
	self.loadPageContent = function () {
	}
}

function ContractOtherDetailsInfoModel() {
	var self = this;

	self.isLoaded = ko.observable(false);
	self.displayScreen = ko.observable(false);
	self.originalSalesAccountExecutive = ko.observable("");
	self.documentOriginationName = ko.observable("");
	self.dealManager = ko.observable("");
	self.comments = ko.observable("");
	self.endUser = ko.observable("");
	self.SAPOrderID = ko.observable("");
	self.priceProtection = ko.observable(false);
	self.CRMQuoteID = ko.observable("");
	self.defaultDocRelationFileName = ko.observable("");
	self.amendmentType = ko.observable("");
	self.BWTemplateID = ko.observable("");
	self.productGoLiveDate = ko.observable("");
	self.priceProtectionDate = ko.observable("");
	self.CRMOpportunityID = ko.observable("");
	self.validated = ko.observable('false');
	self.validatedOn = ko.observable("");
	self.validatedBy = ko.observable("");
	self.customerManagerComments = ko.observable("");

	self.loadPageContent = function () {
	}
}

function ContractTagsInfoModel() {
	var self = this;
	
	self.ContractTagsList = ko.observableArray([]);
    self.ContractTagsSelectedList = ko.observableArray([]);
	self.currentSearchTag = ko.observable();
	self.newTagName = ko.observable();
	self.isAllowTagCreate = ko.observable(false);

	self.isLoaded = ko.observable(false);
	self.displayScreen = ko.observable(false);
	self.loadPageContent = function () {
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
			"ContractRelatedTags" : {
				"GCTag-id" : {
					'Id': iItem.id
				}
			}
		};
        cc_details_services.AddContractRelatedTag(params, function (response_data, status) {
			if (status !== "ERROR") {
				var l_TagObj = {};
				l_TagObj.id = iItem.id;
				l_TagObj.Name = iItem.Name;
				document.getElementById("searchTag").value='';
				self.ContractTagsSelectedList.push(l_TagObj);
				self.ContractTagsList.remove(iItem);
				self.filterTagsList();
				if(allowToast!=false){
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
			"ContractRelatedTags" : {
				"GCTag-id" : {
					'Id': iItem.id
				}
			}
		};
        cc_details_services.RemoveContractRelatedTag(params, function (response_data, status) {
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
	self.CreateAndAddTag =  function () {
		var params = {  
			"GCTag-create": {
				 'TagName': document.getElementById("searchTag").value
			}
		};
        cc_details_services.CreateNewTag(params, function (response_data, status) {
			if (status !== "ERROR" && response_data && response_data.GCTag) {
				var l_TagObj = {};
				l_TagObj.id = response_data.GCTag["GCTag-id"].Id
				l_TagObj.Name = response_data.GCTag.TagName;
				self.AddSelectedTag(l_TagObj, false);
				self.newTagName('');
				successToast(2000, "You have successfully created and added a tag.");
			}else {
				document.getElementById("searchTag").value='';
				self.newTagName('');
			}
		}); 		
		return true;
	}

	self.AllowTagCreateProp = function(){
		cc_details_services.GetContractCustomTabDetails("ALLOW_TAG_CREATE", function (response_data, status) {
			if (status !== "ERROR") {
				if(response_data && response_data.GCProperties){
					var isAllow = getTextValue(response_data.GCProperties.value)
					if(isAllow==="Yes"){
						self.isAllowTagCreate(true);
					}
				}
			}
		});
	}
}

function ContractCCustomTabInfoModel(){
	var self = this;

	self.isLoaded = ko.observable(false);
	self.displayScreen = ko.observable(false);
	self.custom_tab_url = ko.observable("");
	self.custom_tab_name = ko.observable("");

	self.loadPageContent = function () {
		// self.fetchContractCustomTabDetails();
		// self.displayScreen(true);
	}

	self.fetchContractCustomTabDetails = function () {
		if (LOAD_ON_TAB_CHANGE || (!LOAD_ON_TAB_CHANGE && !self.isLoaded())) {
			self.isLoaded(true);
			cc_details_services.GetContractCustomTabDetails(CUSTOM_TAB_URL, function (response_data, status) {
				if (status !== "ERROR") {
					$('#loadingMsg').css('display', 'none');
					_populateCustomTabResponseData(response_data.GCProperties, self,CUSTOM_TAB_URL);
				}
			});
			cc_details_services.GetContractCustomTabDetails(CUSTOM_TAB_NAME, function (response_data, status) {
				if (status !== "ERROR") {
					$('#loadingMsg').css('display', 'none');
					_populateCustomTabResponseData(response_data.GCProperties, self,CUSTOM_TAB_NAME);
				}
			});
		}
	}

	function _populateCustomTabResponseData(iGCProperty, customTabInfoModel, property) {
		if (iGCProperty) {
			if (CUSTOM_TAB_URL == property) {
				var custom_tab_url = getTextValue(iGCProperty.value);
				if (custom_tab_url) {
					l_contract_details_nav_model.leftNavList()[8].display(true);
					if (isItRelativeURL(custom_tab_url)) {
						if (!custom_tab_url.startsWith("/")) {
							custom_tab_url = "/" + custom_tab_url;
						}
						if(custom_tab_url.indexOf(PARAM_CONTRACT_ID)!=-1){
							custom_tab_url=custom_tab_url.replace(PARAM_CONTRACT_ID,contractID);
						}
						if(custom_tab_url.indexOf(PARAM_CONTRACT_ITEMID)!=-1){
							custom_tab_url=custom_tab_url.replace(PARAM_CONTRACT_ITEMID,contractItemID);
						}
						custom_tab_url = "../../../../.." + custom_tab_url + "?instanceId=" + contractItemID;
						customTabInfoModel.custom_tab_url(custom_tab_url);
						customTabInfoModel.isLoaded(true);
					}

				}
			}
			else if (CUSTOM_TAB_NAME == property) {
				var tabName = getTextValue(iGCProperty.value);
				if(tabName){
					l_contract_details_nav_model.leftNavList()[8].Name(tabName);
				}
			}
		}
	}

	function isItRelativeURL(url_value) {
		const pattern = RegExp("^(?!www\.|(?:http|ftp)s?://|[A-Za-z]:\\|//).*");
		return pattern.test(url_value);
	}
}

// Services start----------------------------------------------
var cc_details_services = (function () {
	var self = {};
	self.GetContractDetailsById = function (contractID, responseCallback) {
		$.cordys.ajax({
			method: "GetContractDetailsById ",
			namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
			parameters: { "contractID": contractID },
			success: function (responseSuccess) {
				responseCallback(responseSuccess);
			},
			error: function (responseFailure) {
				responseCallback(responseFailure, "ERROR");
				return false;
			}
		});
	};
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

	self.AddContractRelatedTag = function(parameters, responseCallback){
		$.cordys.ajax({
			method: "AddToContractRelatedTags",
			namespace: "http://schemas/OpenTextContractCenter/Contract/operations",
			parameters:parameters,
				success: function (responseSuccess) {
					responseCallback(responseSuccess);
				},
				error: function (responseFailure) {
					responseCallback(responseFailure, "ERROR");
					return false;
				}
		});
	};

	self.RemoveContractRelatedTag = function(parameters, responseCallback){
		$.cordys.ajax({
			method: "RemoveFromContractRelatedTags",
			namespace: "http://schemas/OpenTextContractCenter/Contract/operations",
			parameters:parameters,
				success: function (responseSuccess) {
					responseCallback(responseSuccess);
				},
				error: function (responseFailure) {
					responseCallback(responseFailure, "ERROR");
					return false;
				}
		});
	};
	self.CreateNewTag = function(parameters, responseCallback){
		$.cordys.ajax({
			method: "CreateGCTag",
			namespace: "http://schemas/OpenTextBasicComponents/GCTag/operations",
			parameters:parameters,
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
  self.GetContractCustomTabDetails = function (property_name,responseCallback) {
		$.cordys.ajax({
			method: "GetPropertyByName",
			namespace: "http://schemas/OpenTextBasicComponents/GCProperties/operations",
			parameters: {"Name":property_name},
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
})();

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
		checkForUserRole();
		listINTParties();
		listEXTParties();
		
		self.saveAttributes = function () {
			
			if (self.isINTDirty() || self.isEXTDirty()) {
				updateAccounts();
			}
		}
		$("#btn_filterINTPartiesList").click(function (iEventObject) {
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
		});
		$("#btn_filterEXTPartiesList").click(function (iEventObject) {
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
		});
		$(".cc-filter-header").click(function (iEventObject) {
			var l_headerSpan = $(this);
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

var l_contract_details_nav_model = new ContractDetailsNavModel();
var l_contract_details_model = new ContractDetailsInfoModel();
var l_contract_accounts_model = new ContractAccountsInfoModel();
var l_custom_attributes_info_model = new CustomAttributesListModel();
var l_contract_lines_info_model = new ContractLinesInfoModel();
var l_contract_renewal_info_model = new ContractRenewalInfoModel();
var l_contract_termination_info_model = new ContractTerminationInfoModel();
var l_contract_other_details_info_model = new ContractOtherDetailsInfoModel();
var l_contract_tags_info_model = new ContractTagsInfoModel();
var l_contract_custom_tab_info_model = new ContractCCustomTabInfoModel();

const all_page_models = [
	l_contract_details_model,
	l_contract_accounts_model,
	l_custom_attributes_info_model,
	l_contract_lines_info_model,
	l_contract_renewal_info_model,
	l_contract_termination_info_model,
	l_contract_other_details_info_model,
	l_contract_tags_info_model,
  l_contract_custom_tab_info_model
];

$(document).ready(function () {
	contractItemID = getUrlParameterValue("instanceId", null, true);
	contractID = contractItemID.substr(contractItemID.lastIndexOf(".") + 1);
	isExecutedContract = getUrlParameterValue("isExecutedContract", null, true);
	currentState = getUrlParameterValue("contractStatus", null, true);
	ko.applyBindings(l_contract_details_nav_model, document.getElementById("contract_details_nav_container"));
	ko.applyBindings(l_contract_details_model, document.getElementById("contract_details_tab"));
	ko.applyBindings(l_contract_accounts_model, document.getElementById("contract_accounts_tab"));
	ko.applyBindings(l_custom_attributes_info_model, document.getElementById("custom_attributes_tab"));
	ko.applyBindings(l_contract_lines_info_model, document.getElementById("contract_lines_tab"));
	ko.applyBindings(l_contract_renewal_info_model, document.getElementById("contract_renewal_tab"));
	ko.applyBindings(l_contract_termination_info_model, document.getElementById("contract_termination_tab"));
	ko.applyBindings(l_contract_other_details_info_model, document.getElementById("contract_other_details_tab"));

	ko.applyBindings(l_contract_tags_info_model, document.getElementById("contract_tags_tab"));
  	ko.applyBindings(l_contract_custom_tab_info_model, document.getElementById("contract_custom_tab_details"));	
	var i_locale = getlocale();
	var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
	translateLabels("com/opentext/apps/commoncomponents/BasicComponents/BasicComponents", i_locale, true);
	loadRTLIfRequired(i_locale, rtl_css);

	if (window.parent.parent) {
		configuratorIframe = $('[src*="contractdetails.htm"]', window.parent.parent.document);
		if (configuratorIframe) {
			configuratorIframe.css('border', 'none');
		}
	}
	createToastDiv();
	var styleAttr = document.getElementById("successToast").getAttribute("style");
	document.getElementById("successToast").setAttribute("style", styleAttr + ";z-index:5999");

	// Load main page.
	all_page_models[0].loadPageContent();
});

function toggleDropDown(){

	document.getElementById("dropdown-box").classList.toggle("show");
}

function loadSelectedCTRTags(){
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
			if(data && data.GCTag){
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
					else{
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

function loadContractTags(){
	$.cordys.ajax({
		method: "FetchAllTags",
		namespace: "http://schemas/OpenTextBasicComponents/GCTag/operations",
		
		success: function (data) {
			if(data){
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

function addDataToTagsDropDown(iElementList, iModel){
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
		else{
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