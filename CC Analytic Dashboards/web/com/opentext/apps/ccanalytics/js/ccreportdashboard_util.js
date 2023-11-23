var configObject = {};
$.cordys.json.defaults.removeNamespacePrefix = true;
var g_RSSEToken = "";
function getiHUbConfiguration(iCallback, isDashboard) {
	if (EXECRPT_USECONFIGJS) {
		configObject.url = EXECRPT_HOSTNAME ? (EXECRPT_PROTOCOL + "://" + EXECRPT_HOSTNAME + ":" + EXECRPT_IPORTALPORT) : "";
		configObject.volume = EXECRPT_VOLUME;
		if (configObject.url) {
			loadJSAPI(iCallback, isDashboard);
		} else {
			showOrHideInfo(true, getTranslationMessage("[Error]: iHUB configuration is missing."), "INFO");
		}
	} else {
		var iHUBConfigModel = $.cordys.ajax({
			namespace: "http://schemas.cordys.com/1.0/xmlstore",
			method: "GetXMLObject",
			parameters: {
				key: "/OpenText/configuration/iHubConnection.xml"
			},
			success: function (data) {
				if (data.tuple && data.tuple.old && data.tuple.old.configuration) {
					var config = data.tuple.old.configuration;
					//From Process Platform 16.4
					if (config.hostname) {
						configObject.url = config.protocol + "://" + config.hostname + ":" + config.iportalport;
						configObject.volume = config.volume;
					}
					//Process Platform 16.3 or earlier
					else if (config.url) {
						configObject.url = config.url + ":8700";
						configObject.volume = config.volume;
					}
				}
				if (configObject.url) {
					loadJSAPI(iCallback, isDashboard);
				} else {
					showOrHideInfo(true, getTranslationMessage("[Error]: iHUB configuration is missing."), "INFO");
				}
			}
		}).fail(function (iCode, iReason) {
			showOrHideInfo(true, getTranslationMessage("[Error]: An error occured while fetching iHUB reports."), "ERROR");
		});
	}

}

function loadJSAPI(iCallback, isDashboard) {
	var l_script = document.createElement('script');
	l_script.setAttribute('type', 'text/javascript');
	l_script.setAttribute('language', 'Javascript');
	l_script.setAttribute('src', configObject.url + "/iportal/jsapi");
	document.getElementById("reports_main_div").appendChild(l_script);

	if (l_script.readyState) { //IE
		l_script.onreadystatechange = function () {
			if (l_script.readyState == "loaded" || l_script.readyState == "complete") {
				l_script.onreadystatechange = null;
				getAnalyticsRSSEToken();
			} else {
				showOrHideInfo(true, getTranslationMessage("[Error]: Fail to load JSAPI."), "ERROR");
			}
		};
	} else { //Others
		l_script.onload = function () {
			getAnalyticsRSSEToken(iCallback, isDashboard);
		};
	}
}

function getAnalyticsRSSEToken(iCallback, isDashboard) {
	if (EXECRPT_USECONFIGJS) {
		initializeAndLoadReport(iCallback, isDashboard);
	} else {
		var RSSETokenModel = $.cordys.ajax({
			namespace: "http://schemas.opentext.com/ihubconnector/1.0",
			method: "GetAuthId",
			parameters: {},
			success: function (data) {
				g_RSSEToken = data.AuthId;
				if (g_RSSEToken) {
					initializeAndLoadReport(iCallback, isDashboard);
				}
			}
		}).fail(function (iCode, iReason) {
			g_RSSEToken = "";
			//showOrHideInfo(true, "[Error]: An error occured while authenticating with iHUB.", "ERROR");
		});
	}
}


function initializeAndLoadReport(iCallback, isDashboard) {
	if (isDashboard) {
		actuate.load("dashboard");
	}
	actuate.load("viewer");

	actuate.load("parameter");
	var reqOps = new actuate.RequestOptions();
	//reqOps.setRepositoryType(actuate.RequestOptions.REPOSITORY_ENCYCLOPEDIA);
	reqOps.setVolume(configObject.volume);
        reqOps.setVolumeProfile("enterprise");

	if (EXECRPT_USECONFIGJS) {
		actuate.initialize(configObject.url + "/iportal", reqOps, EXECRPT_USERID, EXECRPT_PASSWORD, iCallback);
	} else if (g_RSSEToken) {
		reqOps.setRESTAuthToken(g_RSSEToken);
		actuate.initialize(configObject.url + "/iportal", reqOps, "", "", iCallback);
	}
}


function showOrHideInfo(iShow, iMessage, iType, iVisibleTime) {
	var l_infoArea = document.getElementById("InfoArea");

	if (iShow) {
		l_infoArea.style.display = "inline";
		l_infoArea.firstElementChild.innerText = iMessage;
		if (iVisibleTime) {
			setTimeout(showOrHideInfo, iVisibleTime);
		}
	} else {
		l_infoArea.style.display = "none";
		l_infoArea.firstElementChild.innerText = "";
	}
}

var peObject = {};

function openURLinPerformLayout(iURL, iDispalyName) {
	if (!peObject.itemManager) {
		var parent = window;
		while (!parent.ot_App && parent.parent && parent != parent.parent) {
			parent = parent.parent;
		}

		peObject.initialized = true;
		peObject.isPEAvailable = false;
		if (!parent.ot_App) {
			return window.open(iURL);
		}

		if (parent.ot_App.itemManager) {
			peObject.apiWindow = parent;
			peObject.itemManager = parent.ot_App.itemManager;
			peObject.isPEAvailable = true;
		}

		peObject.listId = window.frameElement.getAttribute("listId"); //NOMBV
		peObject.breadcrumbId = window.frameElement.getAttribute("breadcrumbid"); //NOMBV
		if (peObject.breadcrumbId == null && window.parent && window.parent.frameElement) //NOMBV
		{
			peObject.listId = window.parent.frameElement.getAttribute("listId"); //NOMBV
			peObject.breadcrumbId = window.parent.frameElement.getAttribute("breadcrumbId"); //NOMBV
		}
	}
	peObject.itemManager.loadUrl(iURL, iDispalyName, peObject.breadcrumbId);
}

function emptyReportCallback() {
}

function OpenContractsList() {
	window.open("../../../../../app/processExperience/web/perform/pages/005056C00008A1E7A4BFA6A95BA59D65/8771eb39dbba3501a4f28d02db6e607b?authID=non-sso");
}

function OpenContract(ctr_id) {
	window.open("../../../../../app/processExperience/web/perform/items/005056C00008A1E795653A59509D399D." + ctr_id + "/18DBF20F3D64A1E799B6C9E364C29E88/8771eb39dbba3501a4f28d02db6e607b?authID=non-sso");
}

function getUrlParameterValue(iParameter, iURL, bDecode) {
	var l_paramValue = "";
	if (iParameter) {
		var l_curLocation = (iURL && iURL.length) ? iURL : window.location.search;
		if (l_curLocation && l_curLocation.length) {
			var l_parArray = l_curLocation.split("?")[1];
			if (l_parArray && l_parArray.length) {
				var l_parameters = l_parArray.split("&");
				for (var i = 0; i < l_parameters.length; i++) {
					l_parameter = l_parameters[i].split("=");
					if (l_parameter[0] && l_parameter[0].toLowerCase() == iParameter.toLowerCase()) {
						return (bDecode ? decodeURIComponent(l_parameter[1]) : l_parameter[1]);
					}
				}
			}
		}
	}
	return l_paramValue;
}
function getApplicationURL() {
	var l_curLocation = window.location.href;
	if (l_curLocation && l_curLocation.length) {
		var l_parrts = l_curLocation.split("/com/opentext/");
		return l_parrts[0] + '/app/start/web/item/';
	}
}