$.cordys.json.defaults.removeNamespacePrefix = true;
var cInstanceId, defaultContainingSectionId, relatedTemplateId, termRelatedStatus, i_locale, templateType;


const defaultPerPage = 25;
const listPerPageArr = [
    { key: 0, val: "Show all" },
    { key: 100, val: "100 per page" },
    { key: 50, val: "50 per page" },
    { key: 25, val: "25 per page" }];

let l_containerZeroTermReplaceMap = new Map();
var cc_terms_services = (function () {
    var self = {};
    self.getTermsListService = function (params, responseCallback) {
        $.cordys.ajax({
            method: "getContractTerms",
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            parameters: params,
            success: function (responseSuccess) {
                responseCallback(responseSuccess);
            },
            error: function (responseFailure) {
                responseCallback(responseFailure, "ERROR");
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while loading Obligations. Contact the administrator."), 10000);
                return false;
            }
        });
    };
    self.getContractRuleResultMap = function (contractId, callBack) {
        $.cordys.ajax({
            method: "getContractRuleResults",
            namespace: "http://schemas/OpenTextContractCenter/Contract.RuleResult/operations",
            parameters:
            {
                'contractId': contractId,
            }
        }).done(function (data) {
            if (data.RuleResult && data.RuleResult.length > 0) {
                data.RuleResult.forEach(ele => {
                    l_contract_rule_result.set(ele.Rule["Rule-id"].Id, ele.Result);
                });
            } else if (data.RuleResult) {
                l_contract_rule_result.set(data.RuleResult.Rule["Rule-id"].Id, data.RuleResult.Result);
            }
            callBack();
        }).fail(function (error) {

        });
    }
    return self;
})();




function TermsListViewModel() {
    var self = this;
    self.termsList = ko.observableArray();
    self.clauseContainerList = ko.observableArray();

    //Pagination START
    self.totalListsCount = ko.observable(0);
    self.currentPage = ko.observable(1);
    self.hideDecrement = ko.observable(false);
    self.hideIncrement = ko.observable(false);

    self.refreshTable = function () {
        var offset = (self.currentPage() - 1) * defaultPerPage;
        self.fetchTermsList(offset, defaultPerPage);
    };

    self.fetchTermsList = function (offset, limit) {
        var input = { "contractId": cInstanceId.split(".")[1], offset: offset, limit: limit };
        cc_terms_services.getTermsListService(input, function (response_data, status) {
            if (status !== "ERROR") {
                _populateDatatable(response_data);
                self.totalListsCount(response_data.Response.FindZ_INT_TermsListResponse["@total"]);
                self.currentPage(offset / defaultPerPage + 1);
            }
        });
    };

    function _populateDatatable(response_data) {
        if (response_data.Response.FindZ_INT_TermsListResponse && response_data.Response.FindZ_INT_TermsListResponse.TermInstance) {

            self.clauseContainerList.removeAll();
            if (response_data.ClauseContainers.Containers.FindZ_INT_SCMappingListResponse && response_data.ClauseContainers.Containers.FindZ_INT_SCMappingListResponse.ContainingClauses) {
                var clauseContainers = response_data.ClauseContainers.Containers.FindZ_INT_SCMappingListResponse.ContainingClauses;
                clauseContainers.forEach(container => {
                    self.clauseContainerList.push(container);
                });
            }

            var termsData = response_data.Response.FindZ_INT_TermsListResponse.TermInstance;
            self.termsList.removeAll();
            var allTerms = [];
            if (Array.isArray(termsData)) {
                for (var i = 0; i < termsData.length; i++) {
                    // self.termsList.push(ko.mapping.fromJS(_populateData(termsData[i])));
                    allTerms.push(_populateData(termsData[i]));
                }
            } else {
                // self.termsList.push(ko.mapping.fromJS(_populateData(termsData)));
                allTerms.push(_populateData(termsData));
            }
            var conditionInstRes = cc_conditionauth_util.processConditionalCont(_populateConditionContData(allTerms),
                l_contract_rule_result);
            _populateVisibleTerms(conditionInstRes, allTerms);
        } else {
            self.termsList.removeAll();
        }

    };

    function _populateData(response_data) {
        var termsObj = {};
        termsObj.itemId1 = response_data["TermInstance-id"].ItemId1;
        termsObj.termName = _getTextValue(response_data.RelatedTerm.ReferringTo.Name);
        // _getTextValue(response_data.RelatedTerm.Title.Value);
        termsObj.clauseName = _getTextValue(response_data.ReferedSCMapping.LinkedClause.Name);
        termsObj.container = {
            Id: response_data.ReferedSCMapping["ContainingClauses-id"].Id,
            Id1: response_data.ReferedSCMapping["ContainingClauses-id"].Id1,
            Id2: response_data.ReferedSCMapping["ContainingClauses-id"].Id2,
            ItemId2: response_data.ReferedSCMapping["ContainingClauses-id"].ItemId2
        };
        termsObj.parentClause = _getTextValue('');
        termsObj.parentSection = _getTextValue('');
        if (response_data.ReferedSCMapping.ParentContainer) {
            if (response_data.ReferedSCMapping.ParentContainer.LinkedClause) {
                termsObj.parentClause = _getTextValue(response_data.ReferedSCMapping.ParentContainer.LinkedClause.Name);
            }
            if (response_data.ReferedSCMapping.ParentContainer.LinkedSection) {
                termsObj.parentSection = _getTextValue(response_data.ReferedSCMapping.ParentContainer.LinkedSection.Name);
            }
        }
        termsObj.value = _getTextValue(response_data.Value);
        if (response_data.ReferedSCMapping.RelatedCondition) {
            termsObj.RelatedCondition = response_data.ReferedSCMapping.RelatedCondition;
            // termsObj.RuleId = response_data.RelatedCondition['Rule-id'].Id;
        }
        return termsObj;
    };


    self.decrementToLast = function () {
        self.currentPage(1);
        self.hideDecrement(true);
        self.hideIncrement(false);
        self.refreshTable();
    }

    self.decrementOffsetLimit = function () {
        if (self.currentPage() > 1) {
            self.currentPage(self.currentPage() - 1);
        }
        if (self.currentPage() < Math.ceil(self.totalListsCount() / defaultPerPage)) {
            self.hideIncrement(false);
        }
        if (self.currentPage() == 1) {
            self.hideDecrement(true);
        }
        if (self.currentPage() < 1) {
            return;
        }
        self.refreshTable();
    }

    self.incrementOffsetLimit = function () {
        var totalPages = Math.ceil(self.totalListsCount() / defaultPerPage);
        if (self.currentPage() < Math.ceil(self.totalListsCount() / defaultPerPage)) {
            self.currentPage(self.currentPage() + 1);
        }
        if (self.currentPage() == Math.ceil(self.totalListsCount() / defaultPerPage)) {
            self.hideIncrement(true);
        }
        if (self.currentPage() > 1) {
            self.hideDecrement(false);
        }
        self.refreshTable();
    }

    self.incrementToLast = function () {
        self.currentPage(Math.ceil(self.totalListsCount() / defaultPerPage));
        self.hideDecrement(false);
        self.hideIncrement(true);
        self.refreshTable();
    }
    //Pagination END



}

function getTextAfter(iText, imatchString) {
    if (iText && imatchString) {
        return iText.substring(iText.indexOf(imatchString) + 1);
    }
    else {
        return iText;
    }
}

function _getTextValue(obj) {
    if (obj) {
        return obj && obj.text ? obj.text : obj;
    }
    else {
        return '';
    }
}

function toggle(data) {
    data(!data());
}

var l_TermsListViewModel = new TermsListViewModel();
var l_ClauseContainer_List = [];
var l_contract_rule_result = new Map();


$(function () {
    i_locale = getlocale();
    translateLabels("com/opentext/apps/contractcenter/ContractCenter/ContractCenter", i_locale);
    var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
    loadRTLIfRequired(i_locale, rtl_css);
    $('[src*="generateDocument.htm"]', window.parent.parent.document).parent().css('padding-left', '0px');
    cInstanceId = getUrlParameterValue("instanceId", null, true);
    defaultContainingSectionId = getUrlParameterValue("defaultContSectionId", null, true);
    relatedTemplateId = getUrlParameterValue("relatedTemplateId", null, true);
    termRelatedStatus = getUrlParameterValue("Z_INT_CTRInfo", null, true);
    templateType = getUrlParameterValue("templateType", null, true);
    ko.applyBindings(l_TermsListViewModel, document.getElementById("List_container"));
    if (relatedTemplateId != "undefined" && templateType == "External party document") {
        $("#externalPartyDocumentType").show();
        $("#List_container").hide();
    } else if (relatedTemplateId != "undefined" && templateType == "Internal party document") {
        $("#internalPartyDocumentType").show();
        $("#List_container").hide();
    }
    else if (relatedTemplateId == "undefined") {
        $("#executedorExternalContract").show();
        $("#List_container").hide();
    }
    else if (termRelatedStatus && (termRelatedStatus.includes("TERMS:NOTSTARTED")
        || termRelatedStatus.includes("TERMS:FAILED"))) {
        checkForUserRole();
    }
    else {
        if (defaultContainingSectionId != "undefined") {
            loadTermsLayout();
        }
        else {
            var redirection_URL = "../../../../../app/start/web/#/item/" + cInstanceId + "/F8B156BC2CB7A1EB8346A292E085F084?openContentInParentWindow=true";
            if (window.navigator.language !== i_locale) {
                redirection_URL = setUrlParameter(redirection_URL, 'language', i_locale);
            }
            window.location = redirection_URL;
        }
    }
});

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
                triggerCreateTerms(cInstanceId);
            } else {
                $("#terms_not_create_main_div").show();
            }

        }).fail(function (error) {
            loadTermsLayout();
        })
}

function triggerCreateTerms(cInstanceId) {
    $.cordys.ajax(
        {
            method: "ExecuteProcess",
            namespace: "http://schemas.cordys.com/bpm/execution/1.0",
            parameters:
            {
                "type": "definition",
                "receiver": "com/opentext/apps/cc/contract/TermInstanceGeneration",
                "rootEntityInstanceId": cInstanceId

            },
        }).done(function (data) {
            loadTermsLayout();

        }).fail(function (error) {
            $("#terms_create_failed_main_div").show();
            updateTermsCreationError(cInstanceId);
        })
}
function updateTermsCreationError(cInstanceId) {
    $.cordys.ajax(
        {
            method: "ExecuteProcess",
            namespace: "http://schemas.cordys.com/bpm/execution/1.0",
            parameters:
            {
                "type": "definition",
                "receiver": "com/opentext/apps/cc/contract/UpdateTermInstanceGenerationError",
                "rootEntityInstanceId": cInstanceId

            },
        }).done(function (data) {
            //

        }).fail(function (error) {
            //
        })
}

function loadTermsLayout() {
    cc_terms_services.getContractRuleResultMap(cInstanceId.split(".")[1], function () {
        l_TermsListViewModel.refreshTable();
    });
}

function _populateVisibleTerms(conditionInstRes, allTerms) {
    allTerms.forEach(term => {
        if (!conditionInstRes.has(term.itemId1) || conditionInstRes.get(term.itemId1)) {
            l_TermsListViewModel.termsList.push(ko.mapping.fromJS(term));
        }
    })
}

function _populateConditionContData(allTerms) {
    var l_input_conditionInstMap = new Map();
    _populateContainerReplaceTerms(allTerms);
    allTerms.forEach(term => {
        if (term.RelatedCondition) {
            if (getTextValue(term.RelatedCondition.Action) === 'HIDE') {
                l_input_conditionInstMap.set(term.itemId1,
                    { action: 'HIDE', ruleId: term.RelatedCondition.ConditionRule['Rule-id'].Id });
            } else if (getTextValue(term.RelatedCondition.Action) === 'REPLACE') {
                _populateReplaceContainerCond(term, allTerms, l_input_conditionInstMap);
            }
        } else {
            if (l_containerZeroTermReplaceMap.has(term.container.Id2)) {
                l_input_conditionInstMap.set(term.itemId1, { action: 'HIDE', ruleId: l_containerZeroTermReplaceMap.get(term.container.Id2).RuleId });
            }
        }
    });
    return l_input_conditionInstMap;
}

function _populateContainerReplaceTerms(allTerms) {
    var replaceContainers = l_TermsListViewModel.clauseContainerList().filter(ele => ele.RelatedCondition && getTextValue(ele.RelatedCondition.Action) === 'REPLACE');
    replaceContainers.forEach(ele => {
        var sourcecont = l_TermsListViewModel.clauseContainerList().filter(cont => cont["ContainingClauses-id"].Id2 === ele.RelatedCondition.SourceContainer["ContainingClauses-id"].Id2);
        var termsArr = allTerms.filter(term => (term.container.Id2 === ele["ContainingClauses-id"].Id2));
        if (termsArr.length <= 0) {
            l_containerZeroTermReplaceMap.set(sourcecont[0]["ContainingClauses-id"].Id2,
                {
                    SourceContainerId: ele["ContainingClauses-id"].Id2,
                    RuleId: ele.RelatedCondition.ConditionRule["Rule-id"].Id
                });
        }
    });
}

function _populateReplaceContainerCond(replaceTerm, allTerms, l_input_conditionInstMap) {
    var sourceTermArr = allTerms.filter(
        ele =>
            (ele.container.Id2 === replaceTerm.RelatedCondition.SourceContainer["ContainingClauses-id"].Id2));
    if (sourceTermArr.length > 0) {
        sourceTermArr.forEach(sourceTerm => {
            l_input_conditionInstMap.set(sourceTerm.itemId1,
                {
                    action: 'HIDE',
                    ruleId: replaceTerm.RelatedCondition.ConditionRule['Rule-id'].Id
                });
            if (sourceTerm.ParentContainer) {
                replaceTerm.ParentContainer = sourceTerm.ParentContainer;
            }
        }
        );
    }
    l_input_conditionInstMap.set(replaceTerm.itemId1, { action: 'SHOW', ruleId: replaceTerm.RelatedCondition.ConditionRule['Rule-id'].Id });
}
