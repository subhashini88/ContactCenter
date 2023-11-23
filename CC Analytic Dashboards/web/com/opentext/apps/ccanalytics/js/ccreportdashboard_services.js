$.cordys.json.defaults.removeNamespacePrefix = true;
const ACTION = "@action";
const TYPE = "@type";
const CRT_TILE = "CRT_TILE";
const UPD_TAB = "UPD_TAB";
const CRT_TAB = "CRT_TAB";
const UPD_TILE = "UPD_TILE";
const TAB_TYPE = "TAB";
const TILE_TYPE = "TILE";

//Save Chart
function saveChartService(data, callback) {

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
                //show success message
                // console.log("create successful");
                callback(SUCCESS);
            },
            error: function (errorResponse) {
                //Show error message in modal
                // console.log("Error");
                callback(FAIL);

            }
        });

}


//Save Chart
function deleteChartService(data, callback) {
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
                //Show error message in modal
                // console.log("Error");
                callback(FAIL);

            }
        });

}

//load user created charts
function rereshTabDashboardService(req, callback) {
    $.cordys.ajax(
        {
            namespace: "http://schemas.opentext.com/apps/ccanalyticdashboards/23.3",
            method: "RefreshDashboardWithTabId",
            parameters: req,
            success: function (data) {
                callback(data, SUCCESS);
            },
            error: function (errorResponse) {
                //Show error message in modal
                callback(data, FAIL)
                // console.log("Error");
                return;
            }
        }
    )
}

//load user created charts
function loadUserChartsService(callback) {
    $.cordys.ajax(
        {
            namespace: "http://schemas.opentext.com/apps/ccanalyticdashboards/23.3",
            method: "GetAllUserSavedCharts",
            success: function (data) {
                // console.log("successful");
                if (data && data.Response && data.Response.FindUserBasedSavedChartsResponse && data.Response.FindUserBasedSavedChartsResponse.CCUserSavedReports) {
                    callback(data.Response.FindUserBasedSavedChartsResponse.CCUserSavedReports, SUCCESS);
                }
            },
            error: function (errorResponse) {
                //Show error message in modal
                // console.log("Error");
                return;
            }
        }
    )
}
function readRelatedReportService(req, callback) {
    $.cordys.ajax(
        {
            namespace: "http://schemas/OpenTextCCAnalyticDashboards/CCUserReportsDashboard.RelatedReportNode/operations",
            method: "ReadRelatedReportNode",
            parameters: req,
            success: function (data) {
                // console.log("successful");
                callback(data, SUCCESS)
            },
            error: function (errorResponse) {
                // console.log("Error");
                return;
            }
        }
    )
}
function loadUserChartsService(callback) {
    $.cordys.ajax(
        {
            namespace: "http://schemas.opentext.com/apps/ccanalyticdashboards/23.3",
            method: "GetAllUserSavedCharts",
            success: function (data) {
                // console.log("successful");
                if (data && data.Response && data.Response.FindUserBasedSavedChartsResponse && data.Response.FindUserBasedSavedChartsResponse.CCUserSavedReports) {
                    callback(data.Response.FindUserBasedSavedChartsResponse.CCUserSavedReports, SUCCESS);
                }
            },
            error: function (errorResponse) {
                //Show error message in modal
                // console.log("Error");
                return;
            }
        }
    )
}

function loadReportTabsService(callback) {
    $.cordys.ajax(
        {
            namespace: "http://schemas.opentext.com/apps/ccanalyticdashboards/23.3",
            method: "GetAllUserSavedReports",
            success: function (data) {
                let arr = [];
                // console.log("successful");
                if (data.Response["wstxns1:FindUserBasedSavedChartsResponse"]) {
                    Object.keys(data.Response["wstxns1:FindUserBasedSavedChartsResponse"]).forEach(ele => {
                        if (ele.includes("CCUserSavedReports")) {
                            arr.push(data.Response["wstxns1:FindUserBasedSavedChartsResponse"][ele])
                        }
                    });
                }
                callback(arr, SUCCESS);
            },
            error: function (errorResponse) {
                //Show error message in modal
                // console.log("Error");
                return;

            }
        }
    )
}

function createOrUpdateReport(reqObj, callbackfunc) {
    $.cordys.ajax(
        {
            namespace: "http://schemas.opentext.com/apps/ccanalyticdashboards/23.3",
            method: "CreateOrUpdateUserDashBoard",
            parameters: reqObj,
            success: function (data) {
                // console.log("successful");
                callbackfunc(data, SUCCESS);
            },
            error: function (errorResponse) {
                //Show error message in modal
                console.log("Error");
                return;
            }
        }
    )
}

function refreshUserReport(reqObj, callbackfunc) {
    $.cordys.ajax(
        {
            namespace: "http://schemas.opentext.com/apps/ccanalytics/23.3",
            method: "ProcessReportInd",
            parameters: reqObj,
            success: function (data) {
                // console.log("successful");
                callbackfunc(data, SUCCESS);
            },
            error: function (errorResponse) {
                //Show error message in modal
                // console.log("Error");
                return;
            }
        }
    )
}

function deleteTabService(data, callback) {
    let req = { AllReportNodes: {} };
    req.AllReportNodes.DeletedReportNodes = [];
    if (data.tiles().length) {
        data.tiles().forEach(tile => {
            if (tile.itemId) {
                req.AllReportNodes.DeletedReportNodes.push({ "ReportNode": { "ItemId1": tile.tileItemId() } })
            }
        });
    }
    req.AllReportNodes.DeletedReportNodes.push({ "ReportNode": { "ItemId1": data.tabItemId1() } });

    req.Tab = {
        Name: data.name(),
        Order: data.order(),
        Id1: data.tabId1(),
        Id: data.dashboardId(),
        ItemId: data.tabItemId(),
    };

    createOrUpdateReport(req, function (resp, status) {
        callback(status);
    })
}
function MoveTabService(data, direction, callback) {
    let req = {};
    req.ReportNodes = [];
    req.ReportNodes.push({ "ReportNode": { "ItemId1": data.tabItemId() } });
    createOrUpdateReport(req, function (resp, status) {
        callback(status);
    })
}

function saveReportService(data, callback) {
    function formTileReqObject(data) {
        let req = {};
        if (data.deletedTiles().length) {
            req.DeletedReportNodes = [];
            data.deletedTiles().forEach(tile => req.DeletedReportNodes.push({ "ReportNode": { "ItemId1": tile.tileItemId() } }))
        }
        if (data.tiles().length) {
            req.ReportNodes = [];
            data.tiles().forEach(tile => {
                let reportNode = { ReportNode: {} };
                if (!tile.tileItemId()) {
                    reportNode.ReportNode[ACTION] = CRT_TILE;
                } else {
                    if (tile.isEdit()) {
                        reportNode.ReportNode[ACTION] = UPD_TILE;
                    }
                }
                // if(reportNode.ReportNode[ACTION]){
                reportNode.ReportNode[TYPE] = TILE_TYPE;
                reportNode.ReportNode["ItemId1"] = tile.tileItemId();
                reportNode.ReportNode["Order"] = tile.order();
                reportNode.ReportNode["Name"] = tile.name();
                reportNode.ReportNode["SourceReportNodeItemId"] = data.tabItemId();
                reportNode.ReportNode["RelatedConfigItemId"] = tile.relatedConfigItemId();
                req.ReportNodes.push(reportNode);
                // }
            });
        }
        return req;
    }

    function formTabReqObject(data) {
        let req = { ReportNodes: {} };

        if (data.tiles().length) {

            req.ReportNodes.ReportNode = {
                Order: data.order(),
                Name: data.name(),
                ItemId1: data.tabItemId()
            }

            req.ReportNodes.ReportNode[ACTION] = data.tabItemId() ? UPD_TAB : CRT_TAB;
            req.ReportNodes.ReportNode[TYPE] = TAB_TYPE;
        }
        return req;
    }


    if (!data.tabItemId()) {
        createOrUpdateReport(formTabReqObject(data), function (resp, status) {
            if (status == SUCCESS) {
                data.tabItemId(resp.Response.RelatedReportNode["RelatedReportNode-id"].ItemId1);
                createOrUpdateReport(formTileReqObject(data), function (resp, status) {
                    callback(status);
                });
            }
        });
    }
    else {
        createOrUpdateReport(formTileReqObject(data), function (resp, status) {
            if (status == SUCCESS) {
                createOrUpdateReport(formTabReqObject(data), function (resp, status) {
                    callback(status);
                });
            }
        });
    }


    createOrUpdateReport(req, function (resp, status) {
        if (status == SUCCESS) {
            // createOrUpdateReport(formTabReqObject(data), function(resp, status){
            //     callback(status);
            // });
        }
    });
}

function loadAllTabsForUserService(callback) {
    $.cordys.ajax(
        {
            namespace: "http://schemas.opentext.com/apps/ccanalyticdashboards/23.3",
            method: "GetAllUserReports",
            success: function (data) {
                // console.log("successful");
                callback(data, SUCCESS);
            },
            error: function (errorResponse) {
                //Show error message in modal
                // console.log("Error");
                return;
            }
        }
    )
}

function loadTilesInTabService(req, callback) {
    $.cordys.ajax(
        {
            namespace: "http://schemas.opentext.com/apps/ccanalyticdashboards/23.3",
            method: "GetAllTilesByTab",
            parameters: req,
            success: function (data) {
                // console.log("successful");
                callback(data.Response.FindZ_INT_TilesResponse, SUCCESS);
            },
            error: function (errorResponse) {
                //Show error message in modal
                // console.log("Error");
                return;
            }
        }
    )
}


function getContractJSONDataTable(inreq, callbackfunc) {
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
        method: "GetContractJsonData",
        parameters: inreq,
        success: function (data) {
            callbackfunc(data, "SUCCESS");
        },
        error: function (responseFailure) {
            callbackfunc(null, "ERROR");
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving the contract properties. Contact your administrator."), 35000);
            return false;
        }
    });
}



function downloadReport(allContractDataJSON) {

    var fileName = "Contracts.xlsx"
    $(".export-button").addClass('disable-click');

    $.cordys.ajax
        ({
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            method: "ExportData",
            parameters: allContractDataJSON,
            success: function (data) {
                if (data && data.DownloadExcelResponse && data.DownloadExcelResponse.tuple && data.DownloadExcelResponse.tuple.old && data.DownloadExcelResponse.tuple.old.downloadExcel &&
                    data.DownloadExcelResponse.tuple.old.downloadExcel.downloadExcel) {
                    downloadReportIntoLocal(self.base64ToArrayBuffer(data.DownloadExcelResponse.tuple.old.downloadExcel.downloadExcel), fileName);
                    $(".export-button").removeClass('disable-click');

                }
            },
            error: function (error) {
                $(".export-button").removeClass('disable-click');
                errorToast(3000, getTranslationMessage('Unable to export contracts. Ensure that the WS-App services are running.'));
            }
        });
}




function exportSelectedContracts() {
    let reqContractProps = Object.assign({}, contractProperties);
    let ctrObj = {};
    ctrObj.contracts = {};
    ctrObj.contracts.contract = []
    ctrObj.contracts.contract.push(JSON.stringify(contractProperties));
    l_contractSearchResults_model.selectedContracts().forEach(function (ctr) {
        Object.entries(contractProperties).forEach(([key, val]) => reqContractProps[key] = ctr.contractData()[key])
        ctrObj.contracts.contract.push(JSON.stringify(reqContractProps));
        reqContractProps = Object.assign({}, contractProperties);
    });
    downloadReport(ctrObj);
}



function downloadReportIntoLocal(data, fileName) {

    var mimeType = "data:application/vnd.ms-excel;base64";
    var fileName = fileName || "Contracts.xlsx";
    var content = data;

    toString = function (a) { return String(a); };
    var blobContent = (window.Blob || window.MozBlob || window.WebKitBlob || toString);

    blobContent = blobContent.call ? blobContent.bind(window) : Blob;

    var blob = content instanceof blobContent ? content : new blobContent([content], { type: mimeType });

    // For IE.
    if (navigator.msSaveBlob) {
        return navigator.msSaveBlob(blob, fileName);
    }
    // Chrome, FF and others.
    if (window.URL) {
        downloader(window.URL.createObjectURL(blob), fileName);
    }
    return true;
}

function base64ToArrayBuffer(base64) {
    var binaryString = window.atob(base64);
    var binaryLen = binaryString.length;
    var bytes = new Uint8Array(binaryLen);
    for (var i = 0; i < binaryLen; i++) {
        var ascii = binaryString.charCodeAt(i);
        bytes[i] = ascii;
    }
    return bytes;
}

function downloader(url, fileName) {
    var link = document.createElement("a");
    if ('download' in link) {
        link.href = url;
        link.setAttribute("download", fileName);
        link.style.display = "none";
        document.body.appendChild(link);
        setTimeout(function () {
            link.click();
            document.body.removeChild(link);
            setTimeout(function () { window.URL.revokeObjectURL(link.href); }, 250);
        }, 66);
        return true;
    }
}


function getCustomAttributesListService(inreq, callbackfunc) {
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


function getDefaultReportsService(inreq, callbackfunc) {
    $.cordys.ajax({
        namespace: "http://schemas/OpenTextCCAnalyticDashboards/CCUserSavedReports/operations",
        method: "getAllDefaultReports",
        success: function (data) {
            callbackfunc(data, "SUCCESS");
        },
        error: function (responseFailure) {
            callbackfunc(data, "ERROR");
            showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while loading the default reports. Contact your administrator."), 10000);
            return false;
        }
    });
}


function saveUserReportsService(inreq, callbackfunc) {
    $.cordys.ajax({
        namespace: "http://schemas/OpenTextCCAnalyticDashboards/CCUserSavedReports/operations",
        method: "CreateCCUserSavedReports",
        parameters: inreq,
        success: function (data) {
            callbackfunc(data, "SUCCESS");
        },
        error: function (responseFailure) {
            callbackfunc(data, "ERROR");
            showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while saving. Contact your administrator."), 10000);
            return false;
        }
    });
}


function updateOrdersOfReportTabsService(inreq, callbackfunc) {
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/ccanalyticdashboards/23.3",
        method: "UpdateOrdersOfReportTabs",
        parameters: inreq,
        success: function (data) {
            callbackfunc(data, "SUCCESS");
        },
        error: function (responseFailure) {
            callbackfunc(data, "ERROR");
            showOrHideErrorInfo("div_listErrorInfoArea", true, getTranslationMessage("An error occurred while updating the order. Contact your administrator."), 10000);
            return false;
        }
    });
}

function getGeneralCTRPreviewDetails(callback) {
    $.cordys.ajax({
        namespace: "http://schemas.opentext.com/apps/cc/basiccomponents/20.2",
        method: "GetGeneralAttrWithFilters",
        async: false,
        parameters: {},
        success: function (data) {
            callback(data.GeneralAttributes.FindZ_INT_GeneralAttrListResponse.RelatedGCProps, "SUCCESS");
        },
        error: function (responseFailure) {
            showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while retrieving the contract properties. Contact your administrator."), 10000);
            return false;
        }
    });
}