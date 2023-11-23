$.cordys.json.defaults.removeNamespacePrefix = true;
var isDashboardWindow = true;

let viewsModel;
let expandedViewModel;
let chartsModel;
let listModel;
let contractModel;
let username = "";
let isUserEligible;
const LIST_TYPE = "LIST";;
const CHART_TYPE = "CHART";
const SAVEDSEARCH_TYPE = "SAVEDSEARCH";

const ACTION = "@ACTION";
const PARAMS = "PARAMS";
const LABEL = "label";
const VIEWTYPE = "viewType";
const RELATEDVIEW = "relatedViewItemID";
const RELATEDREPORTVIEWITEMID = "relatedReportItemID";
const VIEWITEMID = "viewItemId";
const GCViewRoleMappingId = 'GCViewRoleMapping-id';
const GCViewId = 'GCView-id';
const DefaultChartDuration = 'defaultChartDuration';
const DefaultChartType = 'defaultChartType';
const DefaultGroupByCol = 'defaultGroupByCol';


const CTRListProperties = ['GeneratedContractId', 'ContractName'];
const CTRListHeaders = ['Contract ID', 'Contract name'];

const ORDER = "order";
const RELATED = 'Related';
const DEFAULT = 'Default';

const ExternalParties = "External parties";
const InternalParties = "Internal parties";

const IdConstMap = new Map([[LIST_TYPE, 'GCList-id'], [CHART_TYPE, 'GCChart-id'], [SAVEDSEARCH_TYPE, 'GCSavedSearch-id']]);
const DispNameConstMap = new Map([[LIST_TYPE, 'DisplayName'], [CHART_TYPE, 'DisplayName'], [SAVEDSEARCH_TYPE, 'SearchName']]);
const RelConstMap = new Map([[LIST_TYPE, 'List'], [CHART_TYPE, 'Chart'], [SAVEDSEARCH_TYPE, 'SavedSearch']]);

//const tileShades = ["#A0006B", "#4f3690", "#008383", "#2e3d98", "#5f6e77", "#078db3", "#df3324", "#00639b"];
const tileShades = ["#3b296c", "#008a8b", "#5864ad", "#078db3", "#5f6e77"];
const numericDataTypes = ['DECIMAL', 'FLOAT', 'INTEGER', 'NUMBER'];
const boolDataType = 'BOOLEAN';
const dateDataType = 'DATE';


// const paginationParams = [
//   { name: '1 per page', value: 1 },
//   { name: '10 per page', value: 10 },
//   { name: '25 per page', value: 25 },
// ]

// const contractProperties = {};

function toggleExpandedView() {
  $("#id_expandedView").toggle();
  $("#id_dashboard").toggle();
}

function isLastTile(iItem) {
  iItem.singleTile($(".view-tile:visible").index($(".view-tile:visible").last()) < 1);
}

function formViewDeleteObj(iItem) {
  let Obj = {};
  Obj[ACTION] = "DELETE";
  Obj[VIEWITEMID] = iItem.viewItemId();
  viewsModel.editedViews[iItem.viewItemId()] = Obj;
}

function formViewUpdateObj_old(iItem, ...iKey) {
  let Obj = {};
  Obj[ACTION] = "UPDATE";
  for (let key of iKey) Obj[key] = iItem[key]();
  Obj[VIEWITEMID] = iItem.viewItemId();
  viewsModel.editedViews[iItem.viewItemId()] = Obj;
}

function formViewUpdateObj(iItem, ...iKey) {
  let Obj = {};
  Obj[ACTION] = "UPDATE";
  let keys = [VIEWTYPE, RELATEDVIEW, RELATEDREPORTVIEWITEMID, LABEL];
  let chartKeys = [DefaultChartDuration, DefaultChartType, DefaultGroupByCol];
  for (let key of keys) Obj[key] = iItem[key]();
  for (let key of chartKeys) Obj[key] = iItem.tileNode()[key]();
  Obj[VIEWITEMID] = iItem.viewItemId();
  viewsModel.editedViews[iItem.viewItemId()] = Obj;
}

var ContractModel = function (data) {

  var self = this;
  let allContractData = {};
  let customData = {};
  let ctrFixedData = {};

  self.liClass = ko.observable("nav-item");
  self.tabPaneClass = ko.observable("tab-pane fade");
  self.contractDetails = {};
  self.isChecked = ko.observable(false);


  if (data) {

    const currentLoc = window.location.href;
    const homeInd = currentLoc.lastIndexOf("/home/") + 6;

    allContractData = data.ctrpropjson_cjson;
    customData = data.custpropjson_cjson;
    ctrFixedData = data.fixedpropjson_cjson;
    self.contractData = ko.observable(allContractData);
    self.customData = ko.observable(customData);
    self.fixedData = ko.observable(ctrFixedData);
    self.contractId = ko.observable(allContractData.ID);
    self.contractIdAttr = ko.observable(`#${allContractData.ID}`);
    self.contractName = ko.observable(allContractData.ContractName);
    self.previewDocUrl = ko.observable("/home/" + currentLoc.substring(homeInd, homeInd + currentLoc.substring(homeInd).indexOf("/")) + "/app/start/web/item/"
      + "005056C00008A1E795653A59509D399D." + allContractData.ID + "/005056C00008A1E7AA661CA0C93D1C4A");
    self.genContractId = ko.observable(ctrFixedData.GeneratedContractId);
  }

  self.parseTileContractValues = function (data) {
    let contractData = {
      properties: {
        columnProps: {},
        otherProps: {}
      }
    }
    ctrFixedData = data.fixedpropjson_cjson;
    allContractData = data.ctrpropjson_cjson;

    contractData.properties.columnProps["GeneratedContractId"] = ctrFixedData.GeneratedContractId;
    contractData.properties.columnProps["ContractName"] = allContractData.ContractName;
    contractData.properties.otherProps.ItemId = `${EntityTypes.Contract.Definition_ID}.${allContractData.ID}`;

    return contractData;

  }


  self.parseContractValues = function () {

    let contractData = {
      properties: {
        columnProps: {},
        customProps: {},
        generalProps: {},
        otherProps: {}
      }
    };

    function _parseDuration(name, value) {
      let durationArray = [];
      if (value) {
        if (value.includes('P') && value.includes('M')) {
          let months = parseInt(value.substring(value.indexOf('P') + 1, value.indexOf('M')));
          if (months > 0) {
            durationArray.push(months.toString().concat(months > 1 ? " months" : " month"));
          }
        }
        if (value.includes('M') && value.includes('D')) {
          const days = parseInt(value.substring(value.indexOf('M') + 1, value.indexOf('D')));
          if (days > 0) {
            durationArray.push(days.toString().concat(days > 1 ? " days" : " day"));
          }
        }
        contractData.properties.generalProps[name] = durationArray.length > 0 ? durationArray.join(', ') : "No duration sepcified."
      }
    }

    function _parseDate(name) {
      if (allContractData[name]) {
        contractData.properties.generalProps[name] = formateDatetoLocale(allContractData[name]);
      }
    }

    Object.keys(contractProperties).forEach(prop => {
      contractData.properties.generalProps[prop] = ctrFixedData.hasOwnProperty(prop) ? ctrFixedData[prop] : allContractData[prop]
    });


    contractData.properties.generalProps.AutoRenew = allContractData.AutoRenew === "true" ? "Yes" : "No";
    contractData.properties.generalProps.ClientEarlyTermRight = allContractData.ClientEarlyTermRight === "true" ? "Yes" : "No";
    contractData.properties.generalProps.IsExecuted = ctrFixedData.IsExecuted === "true" ? "Yes" : "No";
    contractData.properties.generalProps.ContractDocumentType = ctrFixedData.ContractDocumentType === "true" ? "Yes" : "No";
    contractData.properties.generalProps.Perpetual = allContractData.Perpetual === "true" ? "Yes" : "No";
    contractData.properties.generalProps.Validated = allContractData.Validated === "true" ? "Yes" : "No";
    contractData.properties.generalProps.PriceProtection = allContractData.PriceProtection === "1" ? "Yes" : "No";

    //Parsing Date values
    _parseDate('MinStartdate');
    _parseDate('CurrentEndDate');
    _parseDate('CurrentStartDate');
    _parseDate('InitialExpiryDate');
    _parseDate('NextExpirationDate');
    _parseDate('StartDate');
    _parseDate('PriceProtectionDate');
    _parseDate('SignatureDate');
    _parseDate('CancellationDate');
    _parseDate('ValidatedOn');

    //Parsing Duration values
    _parseDuration('AutoRenewDuration', contractData.properties.generalProps.AutoRenewDuration);
    _parseDuration('InitialContractTenure', contractData.properties.generalProps.InitialContractTenure);

    CTRListProperties.forEach(prop => contractData.properties.columnProps[prop] = allContractData[prop]);
    contractData.properties.columnProps["GeneratedContractId"] = ctrFixedData.GeneratedContractId;
    contractData.properties.otherProps.ItemId = `${EntityTypes.Contract.Definition_ID}.${self.contractId()}`;

    self.contractDetails = new Object(contractData);
  }
  return self;
}

var ExpandedViewModel = function () {
  let self = this;
  self.title = ko.observable('');
  self.viewType = ko.observable('');
  self.layoutURL = ko.observable('');
  self.relatedViewItemID = ko.observable('');

  self.contractsList = ko.observableArray([]);
  self.contractDetailsList = ko.observableArray([]);
  self.contractFieldsList = ko.observableArray([]);

  self.paginationPerPage = ko.observableArray(paginationParams);
  self.limitValue = ko.observable(paginationParams[2].value);
  self.numOfPages = ko.observable(1);
  self.currentPage = ko.observable(1);
  self.totalContractsCount = ko.observable(-1);

  self.isCollapsed = ko.observable(false);
  self.isCollapsedPreview = ko.observable(false);
  self.selectedContracts = ko.observableArray([]);

  self.isContractPreviewActive = ko.observable(true);
  self.docRepoType = ko.observable('');
  self.isAllChecked = ko.observable(false);
  self.lastUpdatedDate = ko.observable();

  self.loadContractsList = function (resp) {
    let iElementList = resp.data;
    self.totalContractsCount(resp.count);

    self.contractsList.removeAll();
    if (iElementList) {
      if (Array.isArray(iElementList)) {
        iElementList.forEach(function (ctr) {
          let contractmodel = new ContractModel(ctr);
          contractmodel.parseContractValues();
          self.contractsList.push(contractmodel);
        });
      } else {
        let contractmodel = new ContractModel(iElementList);
        contractmodel.parseContractValues();
        self.contractsList.push(contractmodel);
      }
    }
  }

  self.loadListLayout = function (data) {
    const currentLoc = window.location.href;
    const homeInd = currentLoc.lastIndexOf("/home/") + 6;
    self.layoutURL('');
    self.title(data.label());
    self.viewType(data.viewType());
    self.relatedViewItemID(self.relatedViewItemID());
    self.layoutURL(currentLoc.substring(0, homeInd) + currentLoc.substring(homeInd, homeInd + currentLoc.substring(homeInd).indexOf("/")) + "/app/start/web/perform/panelLayout/"
      + data.viewListData().layoutID());
  }

  self.openContract = function (iItem) {
    var url = "../../../../../app/start/web/perform/item/005056C00008A1E795653A59509D399D." + iItem.contractId();
    window.open(url, '_blank');
  }

  self.removeUncheckedContract = function (iItem) {
    self.selectedContracts.remove(function (contract) {
      return contract.contractId == iItem.contractId;
    });
  }

  self.openContractFromActionBar = function () {
    if (self.selectedContracts().length == 1) {
      var url = "../../../../../app/start/web/perform/item/005056C00008A1E795653A59509D399D." + self.selectedContracts()[0].contractId();
      window.open(url, '_blank');
    }
  }

  self.exportSelectedContracts = function () {
    let ctrObj = {};
    ctrObj.contracts = {};
    ctrObj.contracts.contract = []

    ctrObj.contracts.contract.push(JSON.stringify(contractProperties));
    self.selectedContracts().forEach(function (ctr) {
      const ctrDataObj = {};
      Object.keys(contractProperties).forEach(key => {
        ctrDataObj[key] = ctr.contractDetails.properties.generalProps[key];
      });
      ctrObj.contracts.contract.push(JSON.stringify(ctrDataObj, lpservices.undefinedReplacer));
    });
    l_searchService.downloadReport(ctrObj);
  }

  self.exportAllContracts = function () {

    let allContractsList = [];
    const searchItemID = self.relatedViewItemID();
    let ctrObj = {};
    ctrObj.contracts = {};
    ctrObj.contracts.contract = []
    ctrObj.contracts.contract.push(JSON.stringify(contractProperties));

    lpservices.getSavedSearchResult(searchItemID.substring(searchItemID.indexOf('.') + 1), 1000000, 0,
      function (status, data) {
        if (status == SUCCESS) {
          if (data && data.tuple.old.getContractSearchResults) {
            const elements = lpservices.parseToJSON(data.tuple.old.getContractSearchResults.getContractSearchResults);
            const iElementList = elements.data;
            if (iElementList) {
              if (Array.isArray(iElementList)) {
                iElementList.forEach(function (ctr) {
                  let contractmodel = new ContractModel(ctr);
                  contractmodel.parseContractValues();
                  allContractsList.push(contractmodel);
                });
              } else {
                let contractmodel = new ContractModel(ctr);
                contractmodel.parseContractValues();
                allContractsList.push(contractmodel);
              }
            }
            allContractsList.forEach(function (ctr) {
              const ctrDataObj = {};
              Object.keys(contractProperties).forEach(function (key) {
                ctrDataObj[key] = ctr.contractDetails.properties.generalProps[key];
              });
              ctrObj.contracts.contract.push(JSON.stringify(ctrDataObj, lpservices.undefinedReplacer));
            });
            l_searchService.downloadReport(ctrObj);
          }
        }
      });
  }

  self.showPreview = function (data) {
    if (!self.docRepoType()) {
      l_searchService.getDocRepositoryType(expandedViewModel);
    }
    $("#div_ContractDetails").css('visibility', "visible");
    self.contractFieldsList.removeAll();
    self.addToPreviewList();
  }

  self.loadPreview = function (data, callback) {

    const _parseValue = (val, dataType) => {
      let parsedVal;
      if (numericDataTypes.includes(dataType)) {
        parsedVal = formateNumbertoLocale(val);
      } else if (dataType == boolDataType) {
        parsedVal = val == "true" ? "Yes" : "No";
      } else if (dataType == dateDataType) {
        parsedVal = val == formateDatetoLocale(val);
      }
      return parsedVal;
    }

    lpservices.getCustomAttrAndParties("005056C00008A1E795653A59509D399D." + data.contractId(), function (status, iElementList) {
      if (status == SUCCESS) {
        if (iElementList) {
          data.contractDetails.properties.otherProps[InternalParties] = iElementList.CTRAddlProps.AllInternalParties;
          data.contractDetails.properties.otherProps[ExternalParties] = iElementList.CTRAddlProps.AllExternalParties;
          let customAttrList = iElementList.GetMappedCustomAttributesResponse.FindZ_INT_RelatedAttributesListResponse.RelatedAttributes;
          if (customAttrList) {
            if (!Array.isArray(customAttrList)) {
              customAttrList = new Array(customAttrList);
            }
            customAttrList.forEach(function (iElement) {
              data.contractDetails.properties.customProps[iElement.RelatedLabel.Label.text] = _parseValue(iElement.Value, iElement.DataType);
            });
          }
        }
      }
      callback(SUCCESS);
    });
  };

  self.dismissPreview = function () {
    $("#div_ContractDetails").css('visibility', 'hidden');
  }

  self.addToPreviewList = function () {
    var ctrObj;
    var maxLen = self.selectedContracts().length > 3 ? 3 : self.selectedContracts().length;
    for (var ind = 0; ind < maxLen - 1; ind++) {
      ctrObj = self.selectedContracts()[ind];
      ctrObj.liClass("nav-item");
      ctrObj.tabPaneClass("tab-pane fade");
      self.contractFieldsList.push(ctrObj);
    }
    ctrObj = self.selectedContracts()[ind];
    ctrObj.liClass("nav-item active");
    ctrObj.tabPaneClass("tab-pane active");
    self.loadPreview(ctrObj, function (status) {
      if (status == SUCCESS) {
        self.contractFieldsList.push(ctrObj);
      }
    });
  }

  self.removeFromPreviewList = function (data, event) {
    if (!self.contractFieldsList().includes(data)) { return; }
    if (self.contractFieldsList().length > 1) {
      if (document.getElementById(`#${data.contractId()}`).classList.contains('active')) {
        if (self.contractFieldsList().indexOf(data) == 0) {
          self.contractFieldsList()[1].liClass("nav-item active");
          self.contractFieldsList()[1].tabPaneClass("tab-pane active");
        } else {
          self.contractFieldsList()[self.contractFieldsList().indexOf(data) - 1].liClass("nav-item active");
          self.contractFieldsList()[self.contractFieldsList().indexOf(data) - 1].tabPaneClass("tab-pane active");
        }
      }
    }
    self.contractFieldsList.remove(data);
  }


  self.clearExpandedView = function () {
    self.totalContractsCount(-1);
    self.currentPage(1);
    self.numOfPages(1);
    self.limitValue(paginationParams[2].value);
    self.contractsList.removeAll();
  }

  self.onPerPageCountChange = function () {
    if (self.contractsList().length > 0) {
      self.resetPaginationParams();
      self.renderContractsList();
    }
  }

  self.resetPaginationParams = function () {
    self.currentPage(1);
    var totalPages = Math.ceil(self.totalContractsCount() / self.limitValue());
    self.numOfPages(totalPages < 1 ? 1 : totalPages);
  }

  self.renderContractsList = function (data) {
    if (data) {
      self.relatedViewItemID(data.relatedViewItemID());
      self.clearExpandedView();
      self.title(data.label());
      self.viewType(data.viewType());
    }
    const searchItemID = self.relatedViewItemID();
    lpservices.getSavedSearchResult(searchItemID.substring(searchItemID.indexOf('.') + 1), self.limitValue(), self.limitValue() * (self.currentPage() - 1),
      function (status, data) {
        if (status == SUCCESS) {
          if (data && data.tuple.old.getContractSearchResults) {
            const elements = lpservices.parseToJSON(data.tuple.old.getContractSearchResults.getContractSearchResults);
            self.loadContractsList(elements);
          }
        }
      });

  }

  self.refreshUserReport = () => {
    console.log("Refresh report.");
  }

  self.renderChart = function (data) {
    if (data) {
      self.relatedViewItemID(data.relatedViewItemID());
      self.clearExpandedView();
      self.title(data.label());
      self.viewType(data.viewType());
      self.lastUpdatedDate(data.lastUpdatedDate());
      let tile = data.tileNode();
      tile.height = ($("#tile_id_max").parent().parent().parent()[0].clientHeight);
      tile.width = (document.getElementById("tile_id_max").clientWidth);
      tile.loadReportData();
      tile.reportData() ? tile.reportData().renderReport("tile_id_max") : null;
    }
    const searchItemID = self.relatedViewItemID();
    console.log("Chart method called.");
  }

  self.onAllContractsCheckboxValueChanged = function (iItem, event) {
    if (!iItem.isAllChecked()) {
      self.contractsList().forEach(ctr => {
        self.selectedContracts.push(ctr);
        ctr.isChecked(true);
      });
    } else {
      self.selectedContracts().forEach(ctr => {
        ctr.isChecked(false);
      });
      self.selectedContracts.removeAll();
    }
    iItem.isAllChecked(!iItem.isAllChecked());
  }

  self.onContractRowCheckboxValueChanged = function (iItem, event) {
    event.stopPropagation();
    if (iItem.isChecked()) {
      self.selectedContracts.remove(iItem);
    } else {
      self.selectedContracts.push(iItem);
    }
    if (self.selectedContracts().length == self.contractsList().length) {
      self.isAllChecked(true);
    } else {
      self.isAllChecked(false);
    }
    iItem.isChecked(!iItem.isChecked());
  }

  self.goToFirstPage = function () {
    self.currentPage(1);
    self.renderContractsList();
  }
  self.goToPreviousPage = function () {
    self.currentPage(self.currentPage() - 1);
    self.renderContractsList();
  }
  self.goToNextPage = function () {
    self.currentPage(self.currentPage() + 1);
    self.renderContractsList();
  }
  self.goToLastPage = function () {
    self.currentPage(self.numOfPages());
    self.renderContractsList();
  }

  self.selectedContracts.subscribe(function (newVal) {
    $("#btn_openContract").attr("disabled", !(self.selectedContracts().length == 1));
    $("#btn_exportContracts").attr("disabled", !(self.selectedContracts().length > 0));
    if (self.selectedContracts().length == 0 || self.selectedContracts().length > 3) {
      $("#btn_previewContracts").attr("disabled", true);
    } else {
      $("#btn_previewContracts").attr("disabled", false);
    }
  });

}

var TypeLookupModel = function () {

  var self = this;
  self.lists = [];
  self.searchText = ko.observable('');
  self.filteredList = ko.observableArray([]);
  self.savedSearchesList = ko.observableArray([]);
  self.charts = ko.observableArray([]);
  self.selectedViewItemID = ko.observable('');
  self.selectedViewDisplayName = ko.observable('');
  self.activeViewType = ko.observable();
  self.selectReportModal = ko.observable(new SelectReportModal());
  self.hiddenTabs = ko.observableArray([]);

  self.activeViewType.subscribe(function (val) {
    if (val == LIST_TYPE) {
      if (!typeLookupModel.lists.length) {
        lpservices.getRoleBasedViewsByType(typeLookupModel, LIST_TYPE, function () { });
      } else {
        typeLookupModel.filteredList(typeLookupModel.lists);
      }
    } else if (val == SAVEDSEARCH_TYPE) {
      if (!self.savedSearchesList().length) {
        lpservices.getAllSavedSearches(typeLookupModel, typeLookupModel.searchText());
      }
    } else if (val == CHART_TYPE) { }
  });

  self.loadAllListsByRole = function (iElementList) {
    if (iElementList) {
      if (iElementList.length) {
        iElementList.forEach((list) => self.lists.push(list.RelatedDefaultList));
      } else {
        self.lists.push(iElementList.RelatedDefaultList);
      }
      self.filteredList(self.lists);
    }
  }

  self.loadAllChartsByRole = function (iElementList) {
    if (iElementList) {
      if (iElementList.length) {
        iElementList.forEach((list) => self.charts.push(list.RelatedDefaultChart));
      }
    }
  }

  self.loadAllSavedSearches = function (iElementList) {
    self.savedSearchesList.removeAll();
    if (!iElementList) {
      return;
    } else if (!Array.isArray(iElementList)) {
      iElementList = new Array(iElementList);
    }
    iElementList.forEach((search) => self.savedSearchesList.push(search));
  }

  self.selectViewType = function (data, element) {
    const tabType = $(element.target).data('tabtype');

    if (!$(element.target).hasClass('cc-tab-active')) {
      self.activeViewType(tabType);
    }
  }

  self.searchResults = function () {
    if (self.activeViewType() == LIST_TYPE) {
      self.filteredList(self.lists.filter((e) => (e.DisplayName.text.toLowerCase()).includes(self.searchText().toLowerCase())));
    } else if (self.activeViewType() == SAVEDSEARCH_TYPE) {
      lpservices.getAllSavedSearches(typeLookupModel, typeLookupModel.searchText());
    }
  }

  self.selectViewRadioButton = function (iItem, event) {
    $(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
    $(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");

    self.selectedViewItemID(iItem[IdConstMap.get(self.activeViewType())].ItemId);
    self.selectedViewDisplayName(getTextValue(iItem[DispNameConstMap.get(self.activeViewType())]));
  }

  self.resetTypeLookupModel = () => {
    self.searchText('');
    self.selectedViewItemID('');
    self.selectedViewDisplayName('');
    self.selectReportModal(new SelectReportModal());
  }

}

var viewIndexTrack = 0;

var viewModel = function (data) {

  var self = this;

  self.oLabel = '';
  self.oViewType = '';
  self.oViewItemId = '';
  self.oViewIndex = viewIndexTrack++;
  self.oRelatedViewItemID = '';
  self.oOrder;
  self.oViewDisplayName = '';
  self.relatedViewData;

  if (data) {
    self.viewType = ko.observable(data.ViewType);
    self.viewItemId = ko.observable(viewsModel.isDefaultView() ? data[GCViewRoleMappingId].ItemId : data[GCViewId].ItemId1);
    self.order = ko.observable(data.Order);
    self.label = ko.observable(data.Label);

  } else {
    self.viewItemId = ko.observable('');
    self.viewType = ko.observable('');
    self.order = ko.observable();
    self.label = ko.observable('');
  }

  self.totalCount = ko.observable();
  self.listRecordsCount = ko.observable();
  self.viewDisplayName = ko.observable('');//actual display name of list/chart
  self.viewListData = ko.observable();
  self.viewChartData = ko.observable();
  self.relatedViewItemID = ko.observable('');
  self.relatedReportItemID = ko.observable('');
  self.defaultChartDuration = ko.observable();
  self.defaultChartType = ko.observable();
  self.defaultGroupByCol = ko.observable();

  self.isNewTile = ko.observable(false);
  self.isRemovedTile = ko.observable(false);
  self.errorMsg = ko.observable('');
  self.singleTile = ko.observable(false);
  self.isLoaded = ko.observable();
  self.tileNode = ko.observable();
  self.lastUpdatedDate = ko.observable();
  // self.tileNode = ko.observable();
  // self.selectReportModal = ko.observable(new SelectReportModal());

  self.removeView = function (iItem) {
    if (self.singleTile()) {
      return;
    }
    $("#div_ConfirmDelModal").modal({
      backdrop: 'static',
      keyboard: false
    });

    $("#btn_confirmDel").off("click").on("click", function () {
      if (self.isNewTile()) {
        viewsModel.newViews.remove(iItem);
      } else {
        formViewDeleteObj(iItem);
      }
      viewsModel.isDirty(true);
      iItem.isRemovedTile(true);
      viewsModel.updListsCountMap(iItem.relatedViewItemID(), true);
    });
  }

  self.refreshUserReport = function (iItem) {
    console.log(iItem.oRelatedViewItemID);
    lpservices.processRepService(iItem.oRelatedViewItemID, (resp, status) => {
      self.tileNode().rawReportData(resp.tuple.old.processMyDashboardReportInd.processMyDashboardReportInd);
      lpservices.renderTile(self.tileNode());
    })
  }
  self.onLabelChange = function (iItem) {
    viewsModel.isDirty(true);
    if (!self.isNewTile() && iItem.label()) {
      formViewUpdateObj(iItem, LABEL);
    }
  }


  self.openReportSelectModal = () => {
    viewsModel.selectReportModal().refreshModal();
    setTimeout(() => {
      $("#div_editLayoutTileModal").modal({
        backdrop: 'static',
        keyboard: false
      });
      $("#btn_createOrUpdateConfig").off("click").on("click", function () {

        updateView();
        $(".lookup-list tr").find('.cc-select-column').removeClass("cc-radio-on");
      });

    }, 600);

    function updateView() {
      var reportSelected = null;
      if (viewsModel.selectReportModal().activeTab() === myReports) {
        var reportSelected = viewsModel.selectReportModal().myReports().find(report => report.selected());
        self.viewDisplayName(reportSelected.name());
        self.label(reportSelected.name());
      } else if (viewsModel.selectReportModal().activeTab() === sharedReports) {
        var reportSelected = viewsModel.selectReportModal().sharedReports().find(report => report.selected());
        self.viewDisplayName(reportSelected.shrdChartName());
        self.label(reportSelected.shrdChartName());
      }
      if (reportSelected) {
        $('#div_editLayoutTileModal').modal('hide');
        viewsModel.isDirty(true);
        self.viewType("CHART");
        self[RELATEDREPORTVIEWITEMID](reportSelected.itemId());
        if (!self.isNewTile()) {
          if (self.oViewType != self.viewType()) {
            formViewUpdateObj(self, VIEWTYPE, RELATEDVIEW, RELATEDREPORTVIEWITEMID, LABEL);
          } else {
            formViewUpdateObj(self, VIEWTYPE, RELATEDVIEW, RELATEDREPORTVIEWITEMID, LABEL);
          }
        }
      }
    }

  }

  self.openSelectViewModal = function (iItem, event) {
    typeLookupModel.resetTypeLookupModel();

    if (!typeLookupModel.activeViewType()) {
      typeLookupModel.activeViewType(LIST_TYPE);
    }

    $("#div_selectView").modal({
      backdrop: 'static',
      keyboard: false
    });

    $("#btn_selectYes").off("click").on("click", function () {
      if (isDuplicateList()) {
        $("#div_ConfirmAddModal").modal({
          backdrop: 'static',
          keyboard: false
        });

        $("#btn_confirmAdd").off("click").on("click", function () {
          updateGeneralView();
        });
      } else {
        updateGeneralView();
      }
      $(".lookup-list tr").find('.cc-select-column').removeClass("cc-radio-on");
    });


    function updateGeneralView() {
      if (typeLookupModel.activeViewType() !== "CHART") {
        updateView();
      } else {
        updateChartView();
      }
    }
    function updateView() {
      $("#div_selectView").modal('hide');
      viewsModel.isDirty(true);

      if (typeLookupModel.selectedViewItemID() && self.relatedViewItemID() != typeLookupModel.selectedViewItemID()) {
        self.viewDisplayName(typeLookupModel.selectedViewDisplayName());
        self.label(typeLookupModel.selectedViewDisplayName());
        self.viewType(typeLookupModel.activeViewType());
        self.relatedViewItemID(typeLookupModel.selectedViewItemID());

        if (self.oViewType != self.viewType()) {
          formViewUpdateObj(self, VIEWTYPE, RELATEDVIEW, LABEL);
        } else {
          formViewUpdateObj(self, RELATEDVIEW, LABEL);
        }
        viewsModel.updListsCountMap(iItem.relatedViewItemID(), true);
        viewsModel.updListsCountMap(typeLookupModel.selectedViewItemID());
      }
    }

    function updateChartView() {
      var reportSelected = null;
      let selectReportTabType = typeLookupModel.selectReportModal().activeTab();
      if (selectReportTabType === myReports) {
        var reportSelected = typeLookupModel.selectReportModal().myReports().find(report => report.selected());
        self.viewDisplayName(reportSelected.name());
        self.label(reportSelected.name());
      } else if (selectReportTabType === sharedReports) {
        var reportSelected = typeLookupModel.selectReportModal().sharedReports().find(report => report.selected());
        self.viewDisplayName(reportSelected.shrdChartName());
        self.label(reportSelected.shrdChartName());
        self.defaultChartDuration(reportSelected.defaultDuration());
        self.defaultChartType(reportSelected.yColumn() ? "STACKEDBAR" : "BARSINGLE");
        self.defaultGroupByCol(reportSelected.yColumn());
      }
      if (reportSelected) {
        $('#div_selectView').modal('hide');
        viewsModel.isDirty(true);
        self.viewType("CHART");
        self[RELATEDREPORTVIEWITEMID](reportSelected.itemId());
        if (!self.isNewTile()) {
          if (self.oViewType != self.viewType()) {
            formViewUpdateObj(self, VIEWTYPE, RELATEDVIEW, RELATEDREPORTVIEWITEMID, LABEL);
          } else {
            formViewUpdateObj(self, VIEWTYPE, RELATEDVIEW, RELATEDREPORTVIEWITEMID, LABEL);
          }
        }
      }
    }

    function isDuplicateList() {
      const count = viewsModel.listsCountMap.get(typeLookupModel.selectedViewItemID());
      return count && count > 0;
    }

    $("#btn_selectNo, .close-button-lookup").off("click").on("click", function () {
      $(".lookup-list tr").find('.cc-select-column').removeClass("cc-radio-on");
    });
  }




  self.refreshTile = function (data) {
    self.isLoaded(false);
    lpservices.getCurrentViewData(data, self);
  }

  self.expandList = function (data) {
    const methodMapping = new Map([[LIST_TYPE, expandedViewModel.loadListLayout], [SAVEDSEARCH_TYPE, expandedViewModel.renderContractsList], [CHART_TYPE, expandedViewModel.renderChart]]);
    methodMapping.get(data.viewType())(data);
    toggleExpandedView();
  }
  self.expandChart = function (data) {
    toggleExpandedView();
    const methodMapping = new Map([[LIST_TYPE, expandedViewModel.loadListLayout], [SAVEDSEARCH_TYPE, expandedViewModel.renderContractsList], [CHART_TYPE, expandedViewModel.renderChart]]);
    methodMapping.get(data.viewType())(data);
  }

  self.loadViewListData = function (listMetaData, listDataJSON) {

    let listModel = new ListModel();
    let ListMetadataJSON = lpservices.parseToJSON(getTextValue(listMetaData.ListMetadata));
    const listDisplayName = getTextValue(listMetaData.DisplayName);

    listModel.listName(getTextValue(listMetaData.Name));

    if (!self.label()) {
      self.label(getTextValue(listMetaData.DisplayName));
      self.oLabel = getTextValue(listMetaData.DisplayName);
    }

    if (listDataJSON) {
      self.totalCount(parseInt(listDataJSON.total));
      self.listRecordsCount(listDataJSON.records ? listDataJSON.records.length : 0);
      listModel.totalRecords(parseInt(listDataJSON.total));
      listModel.listRecords(listDataJSON.records);
    } else {
      self.errorMsg(`Unable to render "${listDisplayName}" list`);
    }

    if (ListMetadataJSON) {
      listModel.itemLayoutID(ListMetadataJSON.ItemLayoutID);
      listModel.layoutID(ListMetadataJSON.LayoutID);
    }

    self.viewDisplayName(getTextValue(listMetaData.DisplayName));
    self.oViewDisplayName = getTextValue(listMetaData.DisplayName);
    listModel.listType(getTextValue(listMetaData.ListType));
    listModel.defaultColumns(JSON.parse(getTextValue(listMetaData.DefaultColumns)));
    listModel.listID = `${ListMetadataJSON.versionId}.${ListMetadataJSON.viewId}`;
    listModel.listHeaders(Object.values(listModel.defaultColumns().columnProps).map((obj) => obj.displayName));
    listModel.listProperties(Object.keys(listModel.defaultColumns().columnProps));
    self.viewListData(listModel);
    self.isLoaded(true);
  }

  self.loadViewChartData = function (iElementList) {
    if (iElementList && iElementList.length) {
      iElementList.forEach(function (iElement) {
        self.viewChartData.push(new ChartModel(iElement));
      });
    }
  }

  self.loadSearchResultsData = function (savedSearchMetaData, searchResultsData) {

    let searchResultsDataJSON = {};
    searchResultsDataJSON.total = searchResultsData.count ? searchResultsData.count : 0;
    searchResultsDataJSON.records = self.loadContractsListInTile(searchResultsData.data);

    let listModel = new ListModel();
    const searchName = getTextValue(savedSearchMetaData.SearchName);
    self.oViewDisplayName = searchName;
    self.viewDisplayName(searchName);

    if (!self.label()) {
      self.label(getTextValue(searchName));
      self.oLabel = getTextValue(searchName);
    }

    self.totalCount(parseInt(searchResultsDataJSON.total));
    self.listRecordsCount(searchResultsDataJSON.records ? searchResultsDataJSON.records.length : 0);
    listModel.totalRecords(parseInt(searchResultsDataJSON.total));
    listModel.listRecords(searchResultsDataJSON.records);
    listModel.listHeaders(CTRListHeaders);
    listModel.listProperties(CTRListProperties);
    self.viewListData(listModel);
    self.isLoaded(true);
  }

  self.loadContractsListInTile = function (iElementList) {
    let ctrRecords = [];
    if (iElementList) {
      if (Array.isArray(iElementList)) {
        iElementList.forEach(function (ctr) {
          ctrRecords.push(contractModel.parseTileContractValues(ctr));
        });
      } else {
        ctrRecords.push(contractModel.parseTileContractValues(iElementList));
      }
    }
    return ctrRecords;
  }

}

var ViewsModel = function () {

  var self = this;
  self.editedViews = {};
  self.newViews = ko.observableArray([]);
  let date = new Date();
  let hrs = date.getHours();
  self.username = ko.observable(username);
  self.isDefaultView = ko.observable();
  self.todaysDate = date.toLocaleString('default', { day: 'numeric', month: 'long', year: 'numeric' }).replace(',', '');
  self.timeOfDay = hrs < 12 ? "Morning" : hrs < 16 ? "Afternoon" : "Evening";
  self.isEditMode = ko.observable(false);
  self.allViews = ko.observableArray([]);
  self.isDirty = ko.observable(false);
  self.listItems = ko.observableArray([]);
  self.listsCountMap = new Map();
  self.selectReportModal = ko.observable(new SelectReportModal());

  self.updListsCountMap = function (key, dec) {
    const count = self.listsCountMap.get(key);
    if (dec) {
      self.listsCountMap.set(key, count - 1);
    } else {
      self.listsCountMap.set(key, count ? count + 1 : 1);
    }
  }

  self.expandList = function (data) {

    expandedViewModel.title(getTextValue(data.DisplayName));
    let parsedData = lpservices.parseToJSON(getTextValue(data.ListMetadata));
    if (parsedData && parsedData.LayoutID) {
      // navigateToInstance(data.viewListData().layoutID(), "", { "layoutID": "", "clearBreadcrumb": false, "breadcrumbName": '' });
      // window.open("../../../../../app/start/web/perform/panelLayout/" + parsedData.LayoutID + "/null?force_desktop=true", "_self")
      const currentLoc = window.location.href;
      const homeInd = currentLoc.lastIndexOf("/home/") + 6;
      expandedViewModel.layoutURL(currentLoc.substring(0, homeInd) + currentLoc.substring(homeInd, homeInd + currentLoc.substring(homeInd).indexOf("/")) + "/app/start/web/perform/panelLayout/"
        + parsedData.LayoutID);
      $("#id_dashboard").toggle();
      $("#id_expandedView").toggle();
    }
  }

  self.displayMode = ko.computed(function () {
    return self.isEditMode() ? "all-views-template-editable" : "all-views-template-viewonly";
  }, self);

  self.allViewsByOrder = ko.pureComputed(function () {
    return allViews.sorted(function (a, b) {
      return a.order > b.order ? 1 : a.order < b.order ? -1 : 0;
    });
  });

  self.configureViews = function () {
    self.newViews([]);
    self.editedViews = {};
    self.isEditMode(true);
  }

  self.showAllLists = function () {
    if (!self.listItems().length) {
      lpservices.getRoleBasedViewsByType(typeLookupModel, LIST_TYPE, function (status) {
        if (status == SUCCESS) {
          self.listItems(typeLookupModel.lists);
        }
      });
    }
  }

  self.newView = function () {
    typeLookupModel.resetTypeLookupModel();


    if (!typeLookupModel.activeViewType()) {
      typeLookupModel.activeViewType(LIST_TYPE);
    }

    $("#div_selectView").modal({
      backdrop: 'static',
      keyboard: false
    });

    $("#btn_selectYes").off('click').on("click", function () {

      if (isDuplicateList()) {
        $("#div_ConfirmAddModal").modal({
          backdrop: 'static',
          keyboard: false
        });

        $("#btn_confirmAdd").off("click").on("click", function () {
          $("#div_selectView").modal('hide');
          createNewGenericView();
        });
      } else {
        $("#div_selectView").modal('hide');
        createNewGenericView();
      }
      $(".lookup-list tr").find('.cc-select-column').removeClass("cc-radio-on");
    });

    $("#btn_selectNo, .close-button-lookup").off("click").on("click", function () {
      $(".lookup-list tr").find('.cc-select-column').removeClass("cc-radio-on");
    });

    function createNewGenericView() {
      if (typeLookupModel.activeViewType() !== "CHART") {
        createNewView();
      } else {
        createNewChartView();
      }
    }

    function createNewChartView() {

      var reportSelected = null;
      let reportSelectedView = typeLookupModel.selectReportModal().activeTab();
      if (reportSelectedView === myReports) {
        var reportSelected = typeLookupModel.selectReportModal().myReports().find(report => report.selected());
      } else if (reportSelectedView === sharedReports) {
        var reportSelected = typeLookupModel.selectReportModal().sharedReports().find(report => report.selected());
      }

      if (!reportSelected) return;
      $('#div_editLayoutTileModal').modal('hide');
      let viewModelObj = new viewModel();
      viewModelObj.isNewTile(true);
      viewModelObj.viewType("CHART");
      viewModelObj.oViewType = "CHART";
      viewModelObj.viewDisplayName(reportSelected.name());
      viewModelObj.label(reportSelected.name());
      if (reportSelectedView === myReports) {
        viewModelObj.viewDisplayName(reportSelected.name());
        viewModelObj.label(reportSelected.name());
      } else if (reportSelectedView === sharedReports) {
        viewModelObj.viewDisplayName(reportSelected.shrdChartName());
        viewModelObj.label(reportSelected.shrdChartName());
        viewModelObj.defaultChartDuration(reportSelected.defaultDuration());
        viewModelObj.defaultChartType(reportSelected.yColumn() ? "STACKEDBAR" : "BARSINGLE");
        viewModelObj.defaultGroupByCol(reportSelected.yColumn());
      }
      viewModelObj[RELATEDREPORTVIEWITEMID](reportSelected.itemId());
      viewsModel.allViews.push(viewModelObj);
      viewsModel.newViews.push(viewModelObj);
      viewsModel.isDirty(true);

      $(".lookup-list tr").find('.cc-select-column').removeClass("cc-radio-on");
    }




    function createNewView() {
      if (!typeLookupModel.selectedViewItemID()) return;

      let viewModelObj = new viewModel();
      viewModelObj.isNewTile(true);

      viewModelObj.viewType(typeLookupModel.activeViewType());
      viewModelObj.viewDisplayName(typeLookupModel.selectedViewDisplayName());
      viewModelObj.label(typeLookupModel.selectedViewDisplayName());
      viewModelObj.relatedViewItemID(typeLookupModel.selectedViewItemID());
      viewsModel.updListsCountMap(viewModelObj.relatedViewItemID());

      viewsModel.allViews.push(viewModelObj);
      viewsModel.newViews.push(viewModelObj);
      viewsModel.isDirty(true);

      $(".lookup-list tr").find('.cc-select-column').removeClass("cc-radio-on");
    }

    function isDuplicateList() {
      const count = viewsModel.listsCountMap.get(typeLookupModel.selectedViewItemID());
      return count && count > 0;
    }
  }

  self.newReportView = function () {

    let createNewView = () => {

      var reportSelected = null;
      if (viewsModel.selectReportModal().activeTab() === myReports) {
        var reportSelected = viewsModel.selectReportModal().myReports().find(report => report.selected());
      } else if (viewsModel.selectReportModal().activeTab() === sharedReports) {
        var reportSelected = viewsModel.selectReportModal().sharedReports().find(report => report.selected());
      }

      if (!reportSelected) return;
      $('#div_editLayoutTileModal').modal('hide');
      let viewModelObj = new viewModel();
      viewModelObj.isNewTile(true);
      viewModelObj.viewType("CHART");
      viewModelObj.oViewType = "CHART";
      viewModelObj.viewDisplayName(reportSelected.name());
      viewModelObj.label(reportSelected.name());
      if (viewsModel.selectReportModal().activeTab() === myReports) {
        viewModelObj.viewDisplayName(reportSelected.name());
        viewModelObj.label(reportSelected.name());
      } else if (viewsModel.selectReportModal().activeTab() === sharedReports) {
        viewModelObj.viewDisplayName(reportSelected.shrdChartName());
        viewModelObj.label(reportSelected.shrdChartName());
        viewModelObj.defaultChartDuration(reportSelected.defaultDuration());
        viewModelObj.defaultChartType(reportSelected.yColumn() ? "STACKEDBAR" : "BARSINGLE");
        viewModelObj.defaultGroupByCol(reportSelected.yColumn());
      }
      viewModelObj[RELATEDREPORTVIEWITEMID](reportSelected.itemId());
      viewsModel.allViews.push(viewModelObj);
      viewsModel.newViews.push(viewModelObj);
      viewsModel.isDirty(true);

      $(".lookup-list tr").find('.cc-select-column').removeClass("cc-radio-on");
    }


    viewsModel.selectReportModal().refreshModal();
    $("#div_editLayoutTileModal").modal({
      backdrop: 'static',
      keyboard: false
    });

    $("#btn_createOrUpdateConfig").off("click").on("click", function () {
      createNewView();
      $(".lookup-list tr").find('.cc-select-column').removeClass("cc-radio-on");
    });


  }

  function _rerenderAllChartViews() {
    self.allViews().forEach(view => {
      if (view.oViewType === "CHART") {
        lpservices.renderTile(view.tileNode());
      }
    })
  }

  self.cancelConfigChanges = function () {

    if (!self.isDirty()) {
      self.isEditMode(false);
      _rerenderAllChartViews();
      return;
    }
    $("#div_discardModal").modal({
      backdrop: 'static',
      keyboard: false
    });
    $("#btn_cancelChanges").off("click").on("click", function () {
      cancelConfigChanges(self.allViews);
      self.isEditMode(false);
      _rerenderAllChartViews();
    });

    function cancelConfigChanges(iviews) {
      self.listsCountMap.clear();
      let removedViews = [];
      iviews().forEach(function (view) {

        if (view.isRemovedTile()) {
          view.isRemovedTile(false);
        }
        if (view.isNewTile()) {
          removedViews.push(view);
          //iviews.remove(view);
        } else {
          view.label(view.oLabel);
          view.viewType(view.oViewType);
          view.viewItemId(view.oViewItemId);
          view.relatedViewItemID(view.oRelatedViewItemID);
          view.order(view.oOrder);
          view.viewDisplayName(view.oViewDisplayName);
        }
        self.updListsCountMap(view.relatedViewItemID());
      });
      iviews.removeAll(removedViews);
      self.editedViews = {};
      self.newViews.removeAll();
      self.isDirty(false);
    }
  }

  self.saveConfigChanges = function () {

    function formReqForCreate(iviews) {
      let viewsObj = {};
      let views = [];
      iviews.forEach(function (view) {
        if (view.isRemovedTile && !view.isRemovedTile()) {
          views.push({
            "label": view.label(),
            "order": view.order(),
            "viewtype": view.viewType(),
            "relatedViewID": view.relatedViewItemID(),
            "relatedReportItemID": view[RELATEDREPORTVIEWITEMID](),
            "defaultChartDuration": view[DefaultChartDuration](),
            "defaultChartType": view[DefaultChartType](),
            "defaultGroupByCol": view[DefaultGroupByCol]()
          });
        }
      });
      if (views.length) viewsObj["View"] = views;
      return viewsObj;
    }

    function formReqForUpdDel() {
      let viewsObj = {};
      let views = [];
      Object.keys(self.editedViews).forEach(e => views.push(self.editedViews[e]));
      if (views.length) viewsObj["View"] = views;
      return viewsObj;
    }

    if (self.isDirty()) {
      if (self.isDefaultView()) {
        lpservices.saveConfig(formReqForCreate(self.allViews()));
      } else {
        let CRTObj = formReqForCreate(self.newViews());
        let UPDObj = formReqForUpdDel();
        if (Object.keys(UPDObj).length || Object.keys(CRTObj).length) {
          lpservices.saveConfig(CRTObj, UPDObj);
        } else {
          self.isEditMode(false);
        }
      }
    } else {
      self.isEditMode(false);
    }
  }

  self.resetConfigChanges = function () {
    $("#div_ConfirmModal").modal({
      backdrop: 'static',
      keyboard: false
    });
    $("#btn_resetChanges").off("click").on("click", function () {
      lpservices.resetDashboard();
    })
  }

  self.populateAllDefaultViews = function (iElementList) {
    let viewModelObj;
    let relName;
    let viewType;
    self.allViews.removeAll();
    self.listsCountMap.clear();
    if (!Array.isArray(iElementList)) {
      iElementList = new Array(iElementList);
    }



    iElementList.forEach(function (iElement) {
      viewModelObj = new viewModel(iElement);
      viewType = iElement.ViewType;
      relName = RELATED.concat(DEFAULT).concat(RelConstMap.get(viewType));
      viewModelObj.oViewType = viewType;
      viewModelObj.oViewItemId = iElement[GCViewRoleMappingId].ItemId;
      viewModelObj.oOrder = iElement.Order;
      viewModelObj.oRelatedViewItemID = iElement[relName][IdConstMap.get(viewType)].ItemId;
      viewModelObj.relatedViewItemID(viewModelObj.oRelatedViewItemID);
      viewModelObj.relatedViewData = iElement[relName];
      viewsModel.updListsCountMap(viewModelObj.relatedViewItemID());
      lpservices.getViewData(viewModelObj);

      self.allViews.push(viewModelObj);
    });
    // _pushTempReport();
    console.log(self.allViews());
  }


  self.populateAllViews = function (iElementList) {
    let viewModelObj;
    let relName;
    let viewType;
    self.allViews.removeAll();
    self.listsCountMap.clear();

    if (!Array.isArray(iElementList)) {
      iElementList = new Array(iElementList);
    }

    iElementList.forEach(function (iElement) {

      viewModelObj = new viewModel(iElement);
      viewType = iElement.ViewType;
      relName = RELATED.concat(RelConstMap.get(viewType));
      viewModelObj.oViewType = viewType;
      viewModelObj.oViewItemId = iElement[GCViewId].ItemId1;
      viewModelObj.oOrder = iElement.Order;
      viewModelObj.oLabel = iElement.Label;
      if (iElement[relName]) {
        viewModelObj.viewType(viewType);
        viewModelObj.relatedViewItemID(iElement[relName][IdConstMap.get(viewType)].ItemId);
        viewModelObj.oRelatedViewItemID = iElement[relName][IdConstMap.get(viewType)].ItemId;
        viewModelObj.relatedViewData = iElement[relName];
        viewsModel.updListsCountMap(viewModelObj.relatedViewItemID());
        lpservices.getViewData(viewModelObj);
      }
      self.allViews.push(viewModelObj);
    });
  }
}

var ChartModel = function () {
  var self = this;

}

var ListModel = function (data) {

  var self = this;

  self.itemID = ko.observable('');
  self.layoutID = ko.observable('');
  self.itemLayoutID = ko.observable('');
  self.listType = ko.observable('');
  self.listName = ko.observable('');
  self.listRecords = ko.observableArray([]);
  self.listHeaders = ko.observableArray([]);
  self.listProperties = ko.observableArray([]);
  self.defaultColumns = ko.observable();
  self.listID = '';
  self.totalRecords = ko.observable(-1);

  self.openListRecord = function (data) {
    // navigateToInstance(data.properties.otherProps.ItemId, "", { "layoutID": "", "clearBreadcrumb": false, "breadcrumbName": self.displayName() });
    navigateToInstance(data.properties.otherProps.ItemId, "", { "layoutID": "" });
  }
}

function changePreviewType() {

  var targetElement = event.currentTarget.firstElementChild
  var l_currentClassName = targetElement.className;
  if (l_currentClassName == "cc-select-column cc-radio-off") {
    $(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on").addClass("cc-radio-off");
    $(targetElement).removeClass("cc-radio-off");
    $(targetElement).addClass("cc-radio-on");
  }
  if (event.currentTarget.id == "div_ctrPreview") {
    expandedViewModel.isContractPreviewActive(true);
    $("#div_defaultDocPreview").addClass('hidden');
    $("#div_contractDetailsPreview").removeClass('hidden');
  }
  else if (event.currentTarget.id == "div_docpreview") {
    expandedViewModel.isContractPreviewActive(false);

    $("#div_contractDetailsPreview").addClass('hidden');
    $("#div_defaultDocPreview").removeClass('hidden');
  }
}

function truncateString(val) {
  return val.length > 46 ? `${val.substring(0, 46)}...` : val;
}

function isContractViewUser(callback) {
  $.cordys.ajax(
    {
      method: "CheckCurrentUserInRoles",
      namespace: "http://schemas.opentext.com/apps/cc/configworkflow/20.2",
      async: false,
      parameters:
      {
        "Roles": {
          "Role": (ALLCCROLES.CTR_PRI).concat(ALLCCROLES.CTR_SEC).concat(ALLCCROLES.OBL)
        }
      },
    }).done(function (data) {
      const res = getTextValue(data.IsCurrentUserRoles);
      isUserEligible = res.toLowerCase() == "true";
      callback(res && isUserEligible);
    }).fail(function (error) { })
}

$(function () {
  viewIndexTrack = 0;
  if (window.location.href == window.parent.parent.location.href && window != window.parent.parent) {
    window.parent.parent.location.href = window.parent.parent.location.href;
    return;
  }

  var i_locale = getlocale();
  translateLabels("com/opentext/apps/contractcenter/CCGeneralDashboards/CCGeneralDashboards", i_locale);
  var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
  loadRTLIfRequired(i_locale, rtl_css);
  const firstName = getUrlParameterValue("firstname", null, true);
  username = firstName ? firstName : getUrlParameterValue("username", null, true);

  if (window.parent.parent) {
    customAttributesFrame = $('[src*="LandingPage.htm"]', window.parent.parent.document);
    if (customAttributesFrame) {
      customAttributesFrame.parent().parent().parent().css('border', 'none')
      customAttributesFrame.css('border', 'none');
    }
  }
  createToastDiv();

  viewsModel = new ViewsModel();
  typeLookupModel = new TypeLookupModel();
  expandedViewModel = new ExpandedViewModel();
  contractModel = new ContractModel();

  ko.applyBindings(expandedViewModel, $("#id_expandedView")[0]);
  ko.applyBindings(viewsModel, $("#id_allViews")[0]);
  ko.applyBindings(typeLookupModel, $("#div_selectView")[0]);

  lpservices.loadDashboard(viewsModel);

  if (!(typeof isUserEligible == 'boolean')) {
    isContractViewUser(function (eligible) {
      if (!eligible) {
        typeLookupModel.hiddenTabs.push(SAVEDSEARCH_TYPE);
        typeLookupModel.hiddenTabs.push(CHART_TYPE);
      }
    });
  }

  lpservices.getGeneralCTRPreviewDetails(function (status, ctrprops) {
    if (status == "SUCCESS") {
      ctrprops.forEach(prop => contractProperties[prop.Name] = prop.DisplayName);
    }
  });

});