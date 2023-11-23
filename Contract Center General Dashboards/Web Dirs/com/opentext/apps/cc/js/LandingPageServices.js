$.cordys.json.defaults.removeNamespacePrefix = true;
const FULFILLED = "fulfilled";
const LIMIT = 25;
const OFFSET = 0;

var lpservices = (function () {
    var self = this;

    self.parseToJSON = function (inputString) {
        try {
            return JSON.parse(inputString);
        } catch (exp) {
            return false;
        }

    }

    self.undefinedReplacer = function (key, val) {
        return typeof val === "undefined" ? null : val;
    }

    self.getSavedSearchResult = function (savedSearchID, limit = LIMIT, offset = OFFSET, callback) {
        $.cordys.ajax({
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            method: "GetContractSearchResults",
            parameters: {
                "id": savedSearchID,
                "limit": limit,
                "offset": offset,
            },
            success: function (data) {
                callback(SUCCESS, data);
            },
            error: function (responseFailure) {
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving the saved search results. Contact your administrator."), 10000);
                return false;
            }
        });
    }

    self.getCustomAttrAndParties = function (iItemID, callback) {
        $.cordys.ajax({
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            method: "GetConPreviewDetails",
            parameters:
            {
                "ContractItemId": iItemID,
            },
            success: function (data) {
                callback(SUCCESS, data.ContractProperties);
            },
            error: function (responseFailure) {
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while loading contract details. Contact your administrator."), 10000);
                return false;
            }
        });
    }

    self.saveConfig = function (reqObj_CRT = {}, reqObj_UPD = {}) {

        if (reqObj_CRT.hasOwnProperty("View") || reqObj_UPD.hasOwnProperty("View")) {
            $.cordys.ajax({
                namespace: "http://schemas.opentext.com/apps/cc/basiccomponents/20.2",
                method: "SavePersonalDashboard",
                parameters: {
                    "AllViews": {
                        "NewViews": reqObj_CRT,
                        "EditViews": reqObj_UPD,
                    }
                },
                success: function (data) {
                    successToast(3000, getTranslationMessage("Configuration saved."));
                    self.loadDashboard(viewsModel);
                },
                error: function (responseFailure) {
                    showOrHideErrorInfo("div_dashboardError", true, getTranslationMessage("Unable to save the changes. Contact your administrator."), 10000);
                    return false;
                }
            });
        }
    }

    self.loadDashboard = function (iModel) {
        $.cordys.ajax({
            namespace: "http://schemas.opentext.com/apps/cc/basiccomponents/20.2",
            method: "GetPersonalDashboard",
            success: function (data) {
                iModel.isDefaultView(data.IsDefaultView.text == 'true');
                if (data && data.Views) {
                    if (iModel.isDefaultView() && data.Views.GCViewRoleMapping) {
                        iModel.populateAllDefaultViews(data.Views.GCViewRoleMapping);

                    } else {
                        if (data.Views.GCView) {
                            iModel.populateAllViews(data.Views.GCView);
                        }
                    }
                }
                iModel.isEditMode(false);
                iModel.isDirty(false);
            },
            error: function (responseFailure) {
                showOrHideErrorInfo("div_dashboardError", true, getTranslationMessage("Unable to retrieve the tiles. Contact your administrator."), 10000);
                return false;
            }
        });
    }

    self.resetDashboard = function (iModel) {
        $.cordys.ajax({
            namespace: "http://schemas.opentext.com/apps/cc/basiccomponents/20.2",
            method: "ResetPersonalDashboard",
            success: function (data) {
                //show success message
                successToast(3000, getTranslationMessage("Your dashboard has been reset."));
                self.loadDashboard(viewsModel);
            },
            error: function (responseFailure) {
                showOrHideErrorInfo("div_dashboardError", true, getTranslationMessage("An error occurred while resetting the dashboard. Contact your administrator."), 10000);
                return false;
            }
        });
    }

    self.getChartData = function (iElement, viewModelObj) {
        //get related chart data
        viewModelObj.loadViewChartData();
    }

    self.getCurrentViewData = function (viewData, viewModelObj) {
        const viewTypeLoadMapping = new Map([[LIST_TYPE, getCurrentListData], [SAVEDSEARCH_TYPE, getCurrentSavedSearchData]]);
        viewTypeLoadMapping.get(viewModelObj.viewType())(viewData, viewModelObj);
    }


    self.getViewData = function (viewModelObj) {
        const viewTypeLoadMapping = new Map([[LIST_TYPE, getListData], [SAVEDSEARCH_TYPE, getSavedSearchData], [CHART_TYPE, getSavedChartData]]);
        viewTypeLoadMapping.get(viewModelObj.viewType())(viewModelObj);
    }

    const getCurrentSavedSearchData = function (viewData, viewModelObj) {
        let iElement = viewModelObj.relatedViewData;
        const savedSearchID = iElement[IdConstMap.get(SAVEDSEARCH_TYPE)].Id;
        self.getSavedSearchResult(savedSearchID, LIMIT, OFFSET, function (status, data) {
            if (status == SUCCESS) {
                if (data && data.tuple.old.getContractSearchResults) {
                    const res = self.parseToJSON(data.tuple.old.getContractSearchResults.getContractSearchResults);
                    viewModelObj.loadSearchResultsData(viewModelObj.relatedViewData, res);
                }
            }
        });
    }

    const getCurrentListData = function (viewData, viewModelObj) {
        self.getListDataAPI(viewData.viewListData().listID, viewData.viewListData().defaultColumns(), function (data, status) {
            if (status == SUCCESS) {
                viewModelObj.loadViewListData(viewData.relatedViewData, data);
            } else {
                viewModelObj.errorMsg(getTranslationMessage("Unable to load related list. Contact the administrator."));
                return false;
            }
        });
    }

    const getSavedSearchData = function (viewModelObj) {
        let iElement = viewModelObj.relatedViewData;
        const savedSearchID = iElement[IdConstMap.get(SAVEDSEARCH_TYPE)].Id;
        self.getSavedSearchResult(savedSearchID, LIMIT, OFFSET, function (status, data) {
            if (status == SUCCESS) {
                if (data && data.tuple.old.getContractSearchResults) {
                    const res = self.parseToJSON(data.tuple.old.getContractSearchResults.getContractSearchResults);
                    viewModelObj.loadSearchResultsData(viewModelObj.relatedViewData, res);
                }
            }
        });
    }

    const getSavedChartData = function (viewModelObj, callBack) {
        viewModelObj.viewDisplayName(viewModelObj.oLabel);
        viewModelObj.label(getTextValue(viewModelObj.oLabel));
        self.readChartService(viewModelObj.relatedViewItemID(), (resp, status) => {
            viewModelObj[RELATEDREPORTVIEWITEMID](resp.GCChart.RelatedConfigId);
            viewModelObj["lastUpdatedDate"](resp.GCChart.Tracking.LastModifiedDate);
            self.readCCUserSavedRepService(resp.GCChart.RelatedConfigId, (respSavedRep, status) => {
                let tile = populateTileNode(viewModelObj, resp.GCChart, respSavedRep, resp.GCChart.ProcessedReportData);
                viewModelObj.tileNode(tile);
                self.renderTile(tile);
                if (callBack) { callBack(resp, status) }
            });
        });
    }

    self.renderTile = (tile) => {
        if (document.getElementById(tile.tileDivId())) {
            tile.height = ($("#" + tile.tileDivId()).parent().parent()[0].clientHeight - 35);
            if (!tile.height) {
                tile.height = 290;
            }
            tile.width = (document.getElementById(tile.tileDivId()).clientWidth);
            tile.loadReportData();
            tile.reportData().renderReport(tile.tileDivId());
        }
    }

    function populateTileNode(viewModelObj, gcChart, respSavedRep, rawReportData) {
        let tileNode = new TileNode();
        tileNode.name(viewModelObj.oLabel);
        tileNode.tileItemId(viewModelObj.relatedViewItemID());
        tileNode.tileDivId("tile_id" + viewModelObj.oViewIndex);;
        tileNode.order(viewModelObj.oOrder);
        tileNode.rawReportData(rawReportData);
        tileNode.reportType(getTextValue(respSavedRep.CCUserSavedReports.ChartType));
        if (respSavedRep.CCUserSavedReports.ConfigType && getTextValue(respSavedRep.CCUserSavedReports.ConfigType) === "DEFAULTSQL") {
            tileNode.name(getTextValue(viewModelObj.oLabel));
            tileNode.reportType(getTextValue(gcChart.DefaultChartType));
            tileNode.defaultChartDuration(gcChart.DefaultChartDuration);
            tileNode.defaultChartType(gcChart.DefaultChartType);
            tileNode.defaultGroupByCol(gcChart.DefaultGroupByCol);
        }
        tileNode.reportConfiguration(respSavedRep.CCUserSavedReports);
        // tileNode.lastupdated(viewModelObj.Tracking.LastModifiedDate ? new Date(viewModelObj.Tracking.LastModifiedDate) : "");
        tileNode.relatedConfigItemId(respSavedRep.CCUserSavedReports["CCUserSavedReports-id"].ItemId);
        // tiles.push(tileNode);
        return tileNode;
    }

    const getListData = function (viewModelObj) {
        let iElement = viewModelObj.relatedViewData;
        let listName = getTextValue(viewModelObj.relatedViewData.Name);
        const contractLists = new Set(["GenericContractsOrgBased", "UpcomingTerminationsWithin3Months", "GenericMyContracts", "Z_INT_CTRCreatedByUser",
            "UpcomingRenewalsWithinAMonth", "UpcomingRenewalsWithinAWeek", "UpcomingRenewalsWithin3Months", "UpcomingTerminationsWithinAMonth", "UpcomingTerminationsWithinAWeek",
            "Z_LND_UpcomingAutoRenewWithinAMonth", "Z_LND_UpcomingAutoRenewWithinAWeek", "Z_LND_UpcomingAutoRenewWithin3Months", "Z_LND_ContractsInNegotiation"
        ]);

        function getListID() {
            try {
                if (iElement.ListMetadata) {
                    let ListMetadataJSON = JSON.parse(getTextValue(iElement.ListMetadata));
                    return `${ListMetadataJSON.versionId}.${ListMetadataJSON.viewId}`;
                }
            } catch (exp) {
                return false;
            }
        }

        function getDefaultColumns() {
            try {
                if (iElement.DefaultColumns) {
                    return JSON.parse(getTextValue(iElement.DefaultColumns));
                }
            } catch (exp) {
                return false;
            }
        }

        function loadListDataFromAPI() {
            self.getListDataAPI(getListID(), getDefaultColumns(), function (data, status) {
                if (status == SUCCESS) {
                    viewModelObj.loadViewListData(iElement, data);
                } else {
                    viewModelObj.errorMsg(getTranslationMessage("Unable to load related list. Contact the administrator."));
                    return false;
                }
            });
        }

        if (contractLists.has(listName)) {
            fetchServiceResponse("../../../app/entityRestService/Elements(" + getListID() + ")?excludeMeta=true&language=en-US", {
                "method": "GET"
            }, function (data, status) {
                if (status == SUCCESS) {
                    loadListDataFromAPI();
                } else {
                    viewModelObj.errorMsg(getTranslationMessage("Unable to load related list. Contact the administrator."));
                }
            });
        } else {
            loadListDataFromAPI();
        }
    }

    self.getRoleBasedViewsByType = function (iModel, viewType, callback) {
        $.cordys.ajax({
            namespace: "http://schemas.opentext.com/apps/cc/basiccomponents/20.2",
            method: "GetViewsByRole",
            parameters: {
                ViewType: viewType,
            },
            success: function (data) {
                if (data.Views && data.Views.GCViewRoleMapping) {
                    if (viewType == LIST_TYPE) {
                        iModel.loadAllListsByRole(data.Views.GCViewRoleMapping);
                    } else if (viewType == CHART_TYPE) {
                        iModel.loadAllChartsByRole(data.Views.GCViewRoleMapping);
                    }
                    callback(SUCCESS);
                }
            },
            error: function (responseFailure) {
                showOrHideErrorInfo("div_dashboardError", true, getTranslationMessage("Unable to load views. Contact your administrator."), 10000);
                return false;
            }
        });
    }

    self.getAllChartsByRole = function (iModel) {
        $.cordys.ajax({
            namespace: "http://schemas.opentext.com/apps/cc/basiccomponents/20.2",
            method: "GetViewsByRole",
            parameters: {
                ViewType: "CHART",
                ChartDisplayName: "",

            },
            success: function (data) {
                iModel.loadAllListsByRole(data);

            },
            error: function (responseFailure) {
                showOrHideErrorInfo("div_dashboardError", true, getTranslationMessage("Unable to load lists. Contact your administrator."), 10000);
                return false;
            }
        });
    }


    self.getListDefinition = function (listId, callback) {
        return fetchServiceData("../../../app/entityRestService/Elements(" + listId + ")?excludeMeta=true&language=en-US", {
            "method": "GET"
        });
    }

    self.getListDefinition1 = async function (listId, callback) {
        return await fetchServiceData("../../../app/entityRestService/Elements(" + listId + ")?excludeMeta=true&language=en-US", {
            "method": "GET"
        });
    }

    self.getListDataAPI = function (listId, defaultColumns, callback) {
        var listRecords = { total: 0, records: [] };

        if (!listId || !defaultColumns) {
            callback(listRecords, FAIL);
        }

        function getPropertyFromPath(prop, path) {
            path.split('.').forEach((e) => prop = prop[e]);
            return prop;
        }

        fetchServiceResponse("../../../app/entityRestService/Elements(" + listId + ")/ResultItems?excludeMeta=true&language=en-US", {
            "method": "POST", "bodyObj": {
                "resultOption": "ItemsAndTotalCount",
                "skip": 0,
                "top": LIMIT
            }
        }, function (listData, status) {
            if (status == SUCCESS) {
                let data = listData.result;

                listRecords.total = data.totalCount ? data.totalCount : 0;

                if (data.items && data.items.length > 0) {
                    data.items.forEach(function (item) {
                        let itemObj = {
                            properties: {
                                columnProps: {},
                                otherProps: {}
                            }
                        };
                        Object.keys(defaultColumns.columnProps).forEach((prop) =>
                            itemObj.properties.columnProps[prop] = getPropertyFromPath(item, defaultColumns.columnProps[prop].path));

                        Object.keys(defaultColumns.otherProps).forEach((prop) =>
                            itemObj.properties.otherProps[prop] = getPropertyFromPath(item, defaultColumns.otherProps[prop].path));

                        listRecords.records.push(itemObj);
                    });
                }
                callback(listRecords, SUCCESS);
            } else {
                callback(listRecords, FAIL);
            }
        });
    }
    /* Saved searches services */

    self.getAllSavedSearches = function (iModel, searchText = '') {
        $.cordys.ajax({
            namespace: "http://schemas.opentext.com/apps/cc/basiccomponents/20.2",
            method: "GetPersonalSavedSearches",
            parameters: {
                'limit': '25',
                'searchName': searchText,
            },
            success: function (data) {
                if (data && data.GetUserRelatedSearchesResponse) {
                    iModel.loadAllSavedSearches(data.GetUserRelatedSearchesResponse.GCSavedSearch);
                }
            },
            error: function (responseFailure) {
                //Change div to lookup model
                showOrHideErrorInfo("div_Tilelookup", true, getTranslationMessage("An error occurred while retrieving saved searches. Contact your administrator."), 10000);
            }
        });
    }

    self.getGeneralCTRPreviewDetails = function (callback) {
        $.cordys.ajax({
            namespace: "http://schemas.opentext.com/apps/cc/basiccomponents/20.2",
            method: "GetGeneralAttrWithFilters",
            async: false,
            success: function (data) {
                //addDataToContractsDetails(data.GeneralAttributes.FindZ_INT_GeneralAttrListResponse.RelatedGCProps);
                callback("SUCCESS", data.GeneralAttributes.FindZ_INT_GeneralAttrListResponse.RelatedGCProps);
            },
            error: function (responseFailure) {
                showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving the contract properties. Contact your administrator."), 10000);
                return false;
            }
        });
    }

    self.getCustomAttributesListService = function (inreq, callbackfunc) {
        $.cordys.ajax({
            namespace: "http://schemas.opentext.com/apps/cc/customattributes/21.4",
            method: "GetCustAttrWithFilters",
            success: function (data) {
                callbackfunc(data, "SUCCESS");
            },
            error: function (responseFailure) {
                callbackfunc(data, "ERROR");
                showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while loading the custom attributes. Contact your administrator."), 10000);
                return false;
            }
        });
    }

    self.loadUserChartsService = function (callback) {
        if(isUserEligible){
        $.cordys.ajax(
            {
                namespace: "http://schemas.opentext.com/apps/ccanalyticdashboards/23.3",
                method: "GetAllUserSavedCharts",
                success: function (data) {
                    if (data && data.Response && data.Response.FindUserBasedSavedChartsResponse && data.Response.FindUserBasedSavedChartsResponse.CCUserSavedReports) {
                        callback(data.Response.FindUserBasedSavedChartsResponse.CCUserSavedReports, SUCCESS);
                    }
                },
                error: function (errorResponse) {
                    return;
                }
            }
        )
    }
    }

    self.loadUserChartsService = function (callback) {
        if(isUserEligible){
        $.cordys.ajax(
            {
                namespace: "http://schemas.opentext.com/apps/ccanalyticdashboards/23.3",
                method: "GetAllUserSavedCharts",
                success: function (data) {
                    if (data && data.Response && data.Response.FindUserBasedSavedChartsResponse && data.Response.FindUserBasedSavedChartsResponse.CCUserSavedReports) {
                        callback(data.Response.FindUserBasedSavedChartsResponse.CCUserSavedReports, SUCCESS);
                    }
                },
                error: function (errorResponse) {
                    return;
                }
            }
        )
    }
}

    self.getDefaultReportsService = function (inreq, callbackfunc) {
        $.cordys.ajax({
            namespace: "http://schemas/OpenTextCCAnalyticDashboards/CCUserSavedReports/operations",
            method: "getAllDefaultReports",
            success: function (data) {
                callbackfunc(data, "SUCCESS");
            },
            error: function (responseFailure) {
                callbackfunc(data, "ERROR");
                showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while loading the custom attributes. Contact your administrator."), 10000);
                return false;
            }
        });
    }

    self.deleteChartService = function (data, callback) {
        $.cordys.ajax(
            {
                namespace: "http://schemas.opentext.com/apps/ccanalyticdashboards/23.3",
                method: "CreateOrUpdateSavedReports",
                parameters:
                {
                    "DeleteSavedReport": { "SavedReportItemId": data.itemId() }
                },
                success: function (resp) {
                    callback(resp, SUCCESS);
                },
                error: function (errorResponse) {
                    callback(FAIL);

                }
            });

    }

    //Save Chart
    self.saveChartService = function (data, callback) {

        function formReqObject(data) {
            let req = {};
            req.SavedReportItemId = data.isNew() ? "" : data.itemId();
            req.Aggregator = data.aggregator();
            req.AggregatorColumn = data.aggregatorColumn();
            req.ChartType = data.reportType();
            req.DataSetGroup = dataSetGroupMap[data.dataSet()];
            req.DataSet = data.dataSet();
            req.YColumn = (data.yColumn() && (data.yColumn() != '--select--') && data.reportType() !== 'PIE' && data.reportType() !== 'BARSINGLE' && data.reportType() !== 'BARSINGLEHORIZONTAL') ?
                data.yColumn() :
                "";
            // { '@xsi:nil': 'true', "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance" }
            req.XColumn = data.xColumn();
            req.Name = data.name();

            return req;
        }

        $.cordys.ajax(
            {
                namespace: "http://schemas.opentext.com/apps/ccanalyticdashboards/23.3",
                method: "CreateOrUpdateSavedReports",
                parameters:
                {
                    "SavedReport": formReqObject(data)
                },
                success: function (resp) {
                    if (data.isNew()) {
                        data.itemId(resp.Response.CreateCCUserSavedReportsResponse.CCUserSavedReports["CCUserSavedReports-id"].ItemId);
                        data.isNew(false);
                    }
                    callback(SUCCESS);
                },
                error: function (errorResponse) {
                    callback(FAIL);

                }
            });

    }


    self.readChartService = function (chartItemId, callback) {
        $.cordys.ajax(
            {
                namespace: "http://schemas/OpenTextBasicComponents/GCChart/operations",
                method: "ReadGCChart",
                parameters:
                {
                    "GCChart-id": { "ItemId": chartItemId }
                },
                success: function (resp) {
                    callback(resp, SUCCESS);
                },
                error: function (errorResponse) {
                    callback(FAIL);
                }
            });

    }
    self.readCCUserSavedRepService = function (chartItemId, callback) {
        $.cordys.ajax(
            {
                namespace: "http://schemas/OpenTextCCAnalyticDashboards/CCUserSavedReports/operations",
                method: "ReadCCUserSavedReports",
                parameters:
                {
                    "CCUserSavedReports-id": { "ItemId": chartItemId }
                },
                success: function (resp) {
                    callback(resp, SUCCESS);
                },
                error: function (errorResponse) {
                    callback(FAIL);

                }
            });

    }
    self.processRepService = function (chartItemId, callback) {
        $.cordys.ajax(
            {
                namespace: "http://schemas.opentext.com/apps/ccanalytics/23.3",
                method: "ProcessMyDashboardReportInd",
                parameters:
                {
                    "chartItemId": chartItemId
                },
                success: function (resp) {
                    callback(resp, SUCCESS);
                },
                error: function (errorResponse) {
                    callback(FAIL);
                }
            });
    }
    return self;
}());