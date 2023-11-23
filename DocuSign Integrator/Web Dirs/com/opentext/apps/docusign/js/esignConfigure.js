$.cordys.json.defaults.removeNamespacePrefix = true;
var l_esignConfigure_model;
var CONFIGURATION_ID = null;
var IS_SAVED = false;

var ERROR_TOKEN_GENERATION = "Unable to generate the access token. Verify the entered details."

var ERROR_LOAD_CONFIG = "Unable to retrieve the configurations. Contact your administrator.";

var ERROR_DELETE_CONFIG = "Unable to clear the configurations. Contact your administrator.";
var SUCCESS_DELETE_CONFIG = "Configurations cleared.";

var ERROR_CREATE_CONFIG = "Unable to save the configurations. Contact your administrator.";
var SUCCESS_CREATE_CONFIG = "Configurations saved.";

var ERROR_UPDATE_CONFIG = "Unable to update the configurations. Contact your administrator.";
var SUCCESS_UPDATE_CONFIG = "Configurations updated.";

var ERROR_VALIDATE_TOKEN = "Unable to validate the configurations. Verify the entered details.";

var ERROR_TEST_CONFIG = "The test connection has failed.";
var SUCCESS_TEST_CONFIG = "The test connection was successful.";

function ESignConfigureModel() {
	var self = this;
	self.coreSignBaseURL = ko.observable('');
	self.siteId = ko.observable('');
	self.clientId = ko.observable('');
	self.clientSecret = ko.observable('');
	self.authEndpointURL = ko.observable('');
	self.tokenEndpointURL = ko.observable('');
	self.providerName = ko.observable('');
	self.providerType = ko.observable('');
	self.disableCoreSignTest = ko.observable(true);
	self.remainingFieldsCheck = ko.observable(false);

	self.testSignatureConn = function () {
		$.cordys.ajax({
			namespace: "http://schemas.opentext.com/bps/entity/buildingblock/file",
			method: "TestSignatureConnection",
			parameters: {
				"SignatureProvider": l_esignConfigure_model.providerName(),
				"BaseURL": l_esignConfigure_model.coreSignBaseURL(),
				"ClientId": l_esignConfigure_model.clientId(),
				"ClientSecret": l_esignConfigure_model.clientSecret(),
				"TokenEndPoint": l_esignConfigure_model.authEndpointURL()
			},
			success: function (data) {
				if (data) {
					console.log(data);
					addToastDiv(getTranslationMessage(SUCCESS_TEST_CONFIG));
					successToast(3000);
					l_esignConfigure_model.clientSecret('');
				}
			}, error: function (error) {
				notifyError(getTranslationMessage(ERROR_TEST_CONFIG), 3000);
				return false;
			}
		});
	};

	self.validateCoreSignFields = function () {
		if (l_esignConfigure_model.coreSignBaseURL() != '' && l_esignConfigure_model.siteId() != '' && l_esignConfigure_model.clientId() != '') {
			if (l_esignConfigure_model.authEndpointURL() != '' && l_esignConfigure_model.providerName() != '') {
				return true;
			}
		}
		return false;
	}

	self.clientSecret.subscribe(function (newVal) {
		if (newVal) {
			l_esignConfigure_model.disableCoreSignTest(!self.remainingFieldsCheck());
		} else {
			l_esignConfigure_model.disableCoreSignTest(true);
		}
	});

};


$(function () {
	var i_locale = getlocale();
	translateLabels("com/opentext/apps/contractcenter/DocuSignIntegrator/DocuSignIntegrator", i_locale);
	var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
	loadRTLIfRequired(i_locale, rtl_css);
	l_esignConfigure_model = new ESignConfigureModel();
	ko.applyBindings(l_esignConfigure_model, document.getElementById("esignProvider-container"));
	fetchESignProvider(function (response) {
		if (response.ESignature_Provider === "CORE_SIGNATURE" && response.Document_Repository === "BUSINESS_WORKSPACE") {
			l_esignConfigure_model.providerType("CORE_NOTSUPPORTED");
		} else if (response.ESignature_Provider === "CORE_SIGNATURE" && response.Document_Repository !== "") {
			l_esignConfigure_model.providerType(getTextValue(response.ESignature_Provider));
			fetchCoreSignDetails();
		} else if (response.ESignature_Provider === "DOCUSIGN") {
			l_esignConfigure_model.providerType(getTextValue(response.ESignature_Provider));
			loadDocuSignProperties();
		} else {
			l_esignConfigure_model.providerType("NOT_CONFIGURED");
		}
	});

	createToastDiv()
	//Adding action methods            
	$('#action-testconnection').click(docusignActionTestConnection);
	$('#action-confirm-delete').click(docusignActionDelete);
	$('#action-confirm-update').click(docusignActionCreateAndUpdate);
	$('#action-create').click(docusignActionCreateAndUpdate);

	$('#username, #password, #iKey').change(validateProps);

});

function fetchESignProvider(callbackfunc) {
	$.cordys.ajax({
		namespace: "http://schemas/OpenTextBasicComponents/GC/operations",
		method: "ReadGC"
		, success: function (data) {
			if (data.GC) {
				callbackfunc(data.GC);
			}
		}, error: function (error) {
			notifyError(getTranslationMessage(ERROR_LOAD_CONFIG), 3000);
			return false;
		}
	});
}



function fetchCoreSignDetails() {
	$.cordys.ajax({
		namespace: "http://schemas.opentext.com/1.0/ocp",
		method: "GetConfiguration",
		//parameters: {"key": "/OpenText/esign/configuration/eSignatureConnection.xml"},
		success: function (data) {
			if (data) {
				if (data.Configuration && data.Configuration.ESignature) {
					populateCoreSignDetails(data.Configuration.ESignature);
				}
				else {

					notifyError(getTranslationMessage(ERROR_LOAD_CONFIG), 3000);
				}
			}
		}, error: function (error) {
			notifyError(getTranslationMessage(ERROR_LOAD_CONFIG), 3000);
			return false;
		}
	});
}


function populateCoreSignDetails(response) {
	l_esignConfigure_model.coreSignBaseURL(getTextValue(response.eSignBaseURL));
	l_esignConfigure_model.siteId(getTextValue(response.SiteID));
	l_esignConfigure_model.clientId(getTextValue(response.ClientID));
	//l_esignConfigure_model.clientSecret(getTextValue(response.ClientSecret));
	l_esignConfigure_model.authEndpointURL(getTextValue(response.AuthEndPoint));
	//l_esignConfigure_model.tokenEndpointURL(getTextValue(response.TokenEndPoint));
	l_esignConfigure_model.providerName(getTextValue(response.eSignProvider));
	l_esignConfigure_model.remainingFieldsCheck(l_esignConfigure_model.validateCoreSignFields());
}

function loadDocuSignProperties() {
	$.cordys.ajax({
		namespace: "http://schemas/OpenTextDocuSignIntegrator/DocuSignConfiguration/operations",
		method: "getDocuSignConfiguration"
		, success: function (data) {
			mapPropertiesToView(data.DocuSignConfiguration);
			data.DocuSignConfiguration !== undefined ? actionItems_Update() : actionItems_Create();
		}, error: function (error) {
			notifyError(getTranslationMessage(ERROR_LOAD_CONFIG), 3000);
			return false;
		}
	});
}

function mapPropertiesToView(configuration) {
	//Mapping username
	$('#username').val(configuration !== undefined ? configuration.Username : "");

	//Mapping pass field.
	//$('#password').val(atob(configuration !== undefined ? configuration.Password : ""));

	//Mapping Integrator key
	$('#iKey').val(configuration !== undefined ? configuration.IntegratorKey : "");

	//Mapping Access token
	$('#token').val(configuration !== undefined ? configuration.AccessToken : "");

	//Mapping Account ID
	$('#accountId').val(configuration !== undefined ? configuration.AccountID : "");

	//Mapping Docusign end point URL.
	$('#docusign_endpoint').val(configuration !== undefined ? configuration.EndPointURL : "");

	CONFIGURATION_ID = configuration !== undefined ? configuration["DocuSignConfiguration-id"].Id : "";
}

function docusignActionTestConnection() {
	IS_SAVED = true;
	if (validateProps()) {
		// generateAccessToken('rohit.setty926@gmail.com', 'RSdec050*', '27ec02c3-7902-4d3b-b97d-32d3ef32d6a3');
		//generateAccessToken($('#username').val(), $('#password').val(), $('#iKey').val(), $('#docusign_endpoint').val(),true);
		testorSaveDocuSignConfig($('#username').val(), $('#password').val(), $('#iKey').val(), $('#docusign_endpoint').val(), true);
	}
}

function testorSaveDocuSignConfig(usr, pwd, key, docuSignURL, isTestConfig) {
	$.cordys.ajax({
		namespace: "http://schemas.opentext.com/apps/docusignintegrator/19.2",
		method: "TestandSaveDocuSignConfig",
		parameters: {
			userName: usr,
			passwordB64: btoa(pwd),
			integratorKey: key,
			docuSignURL: docuSignURL,
			isTestConn: isTestConfig,
			docuSignConfigID: CONFIGURATION_ID
		},
		success: function (data) {
			if (data && data.testConnFlag && data.testConnFlag.text == "SUCCESS") {
				$('#password').val("");
				if (isTestConfig) {
					addToastDiv(getTranslationMessage(SUCCESS_TEST_CONFIG));
					successToast(3000);
				}
				else if (data && data.createUpdateData && data.createUpdateData.UpdateDocuSignConfigurationResponse) {
					mapPropertiesToView(data.createUpdateData.UpdateDocuSignConfigurationResponse.DocuSignConfiguration);
					addToastDiv(getTranslationMessage(SUCCESS_UPDATE_CONFIG));
					successToast(3000);
				}
			} else {
				$("#errorToast").removeClass("hide");
				isTestConfig ? notifyError(getTranslationMessage(ERROR_TEST_CONFIG), 3000) : notifyError(getTranslationMessage(ERROR_TOKEN_GENERATION), 3000);
				return false;
			}
		},
		error: function (error) {
			$("#errorToast").removeClass("hide");
			isTestConfig ? notifyError(getTranslationMessage(ERROR_TEST_CONFIG), 3000) : notifyError(getTranslationMessage(ERROR_TOKEN_GENERATION), 3000);
			return false;
		}
	});
}

function docusignActionDelete() {
	$.cordys.ajax({
		namespace: "http://schemas/OpenTextDocuSignIntegrator/DocuSignConfiguration/operations",
		method: "DeleteDocuSignConfiguration",
		parameters: {
			"DocuSignConfiguration-id": {
				Id: CONFIGURATION_ID
			}
		}, success: function (data) {
			loadDocuSignProperties();
			IS_SAVED = false;
			addToastDiv(getTranslationMessage(SUCCESS_DELETE_CONFIG));
			successToast(3000);
		},
		error: function (error) {
			notifyError(getTranslationMessage(ERROR_DELETE_CONFIG), 3000);
			return false;
		}
	});
}

function docusignActionCreateAndUpdate() {
	IS_SAVED = true;
	if (validateProps()) {
		// generateAccessToken('rohit.setty926@gmail.com', 'RSdec050*', '27ec02c3-7902-4d3b-b97d-32d3ef32d6a3');
		//generateAccessToken($('#username').val(), $('#password').val(), $('#iKey').val(), $('#docusign_endpoint').val());
		testorSaveDocuSignConfig($('#username').val(), $('#password').val(), $('#iKey').val(), $('#docusign_endpoint').val(), false);
	}
}

function actionItems_Create() {
	$('#action-update, #action-delete, #action-testconnection').hide();
	$('#action-create').show();
}

function actionItems_Update() {
	IS_SAVED = true;
	$('#action-update, #action-delete, #action-testconnection').show();
	$('#action-update').attr('disabled', true);
	$('#action-create').hide();
}

function validateProps() {
	if (!IS_SAVED) {
		return true;
	}
	var invalidProps = [];
	if ($('#username').val() == "") {
		$('#username').addClass("apps-error");
		invalidProps.push("User name");
	} else {
		$('#username').removeClass("apps-error");
	}

	if ($('#password').val() == "") {
		$('#password').addClass("apps-error");
		invalidProps.push("Password");
	} else {
		$('#password').removeClass("apps-error");
	}

	if ($('#iKey').val() == "") {
		$('#iKey').addClass("apps-error");
		invalidProps.push("Integrator key");
	} else {
		$('#iKey').removeClass("apps-error");
	}

	//Email validation
	var emailRegex = /^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/;
	if (emailRegex.test($('#username').val()) == false) {
		$('#username').addClass("apps-error");
		invalidProps.push("FORMAT_ERROR#Username#email ID");
	} else {
		$('#username').removeClass("apps-error");
	}


	if (invalidProps.length == 1) {
		updateToastContent(contentText(invalidProps));
	} else if (invalidProps.length > 1) {
		updateToastContent(getTranslationMessage("{0} errors", [invalidProps.length]), contentText(invalidProps));
	} else {
		errorToastToggle("hide");
		$('.docusign-config-actions button').prop('disabled', '');
		return true;
	}

	errorToastToggle("show");
	//$('.docusign-config-actions button').prop('disabled', 'disabled');
	return false;
}

function contentText(inValidList) {

	return inValidList.map(function (e, i) {
		if (e.startsWith("FORMAT_ERROR")) {
			return getTranslationMessage("{0}: Enter a valid {1}.", [e.split("#")[1], e.split("#")[2]]);
		} else {
			return getTranslationMessage("{0} cannot be empty.", [e]);
		}
	}).join("<br>");
}

function generateAccessToken(usr, pwd, key, docuSignURL, isTestConfig) {
	$.cordys.ajax({
		namespace: "http://schemas.opentext.com/apps/docusignintegrator/19.2",
		method: "GenerateDocuSignAccessToken",
		parameters: {
			username: usr,
			password: pwd,
			key: key,
			docuSignURL: docuSignURL
		},
		success: function (data) {
			if (data.tuple.old.GenerateDocuSignAccessToken.GenerateDocuSignAccessToken != "") {
				validateAccessToken(usr, pwd, key, data.tuple.old.GenerateDocuSignAccessToken.GenerateDocuSignAccessToken, docuSignURL, isTestConfig);
			} else {
				$("#errorToast").removeClass("hide");
				isTestConfig ? notifyError(getTranslationMessage(ERROR_TEST_CONFIG), 3000) : notifyError(getTranslationMessage(ERROR_TOKEN_GENERATION), 3000);
				return false;
			}
		},
		error: function (error) {
			$("#errorToast").removeClass("hide");
			isTestConfig ? notifyError(getTranslationMessage(ERROR_TEST_CONFIG), 3000) : notifyError(getTranslationMessage(ERROR_TOKEN_GENERATION), 3000);
			return false;
		}
	});

	//To test with sample value
	// validateAccessToken(usr, pwd, key, "ER+LFCYMKxwXGee0XTvpq2Vhzxw=", isTestConfig);
}

function validateAccessToken(usr, pwd, key, accessToken, docuSignURL, isTestConfig) {
	$.cordys.ajax({
		namespace: "http://schemas.opentext.com/apps/docusignintegrator/19.2",
		method: "ValidateAccessToken",
		parameters: {
			key: accessToken,
			docuSignURL: docuSignURL
		},
		success: function (data) {
			if (data.tuple.old.ValidateAccessToken.ValidateAccessToken) {
				if (isTestConfig) {
					//Compare Account IDs
					if (data.tuple.old.ValidateAccessToken.ValidateAccessToken == $('#accountId').val()) {
						addToastDiv(getTranslationMessage(SUCCESS_TEST_CONFIG));
						successToast(3000);
					} else {
						notifyError(getTranslationMessage(ERROR_TEST_CONFIG), 3000);
						return false;
					}
				} else {
					CreateDocuSignConfiguration(usr, pwd, key, accessToken, data.tuple.old.ValidateAccessToken.ValidateAccessToken, docuSignURL);
				}
			} else {
				$("#errorToast").removeClass("hide");
				isTestConfig ? notifyError(getTranslationMessage(ERROR_TEST_CONFIG), 3000) : notifyError(getTranslationMessage(ERROR_VALIDATE_TOKEN), 3000);
				return false;
			}
		},
		error: function (error) {
			$("#errorToast").removeClass("hide");
			notifyError(getTranslationMessage(ERROR_VALIDATE_TOKEN), 3000);
			return false;
		}
	});
}

function CreateDocuSignConfiguration(usr, pwd, key, accessToken, accountId, docuSignURL) {
	$.cordys.ajax({
		namespace: "http://schemas/OpenTextDocuSignIntegrator/DocuSignConfiguration/operations",
		method: "getDocuSignConfiguration"
		, success: function (data) {
			if (data.DocuSignConfiguration !== undefined) {
				//Update config
				$.cordys.ajax({
					namespace: "http://schemas/OpenTextDocuSignIntegrator/DocuSignConfiguration/operations",
					method: "UpdateDocuSignConfiguration",
					parameters: {
						"DocuSignConfiguration-id": {
							Id: CONFIGURATION_ID
						},
						"DocuSignConfiguration-update": {
							Username: usr,
							Password: btoa(pwd),
							IntegratorKey: key,
							AccessToken: accessToken,
							AccountID: accountId,
							EndPointURL: docuSignURL ? docuSignURL : { '@xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance', '@xsi:nil': 'true' }
						},
						old: {
							DocuSignConfiguration: {
								"DocuSignConfiguration-id": {
									Id: CONFIGURATION_ID
								}
							}
						}
					},
					success: function (data) {
						if (data.DocuSignConfiguration !== undefined) {
							mapPropertiesToView(data.DocuSignConfiguration);
							actionItems_Update();

							addToastDiv(getTranslationMessage(SUCCESS_UPDATE_CONFIG));
							successToast(3000);
						} else {
							notifyError(getTranslationMessage(ERROR_UPDATE_CONFIG), 3000);
							actionItems_Create();
							return false;
						}
					},
					error: function () {
						notifyError(getTranslationMessage(ERROR_UPDATE_CONFIG), 3000);
						return false;
					}
				});
			} else {
				//Create config
				$.cordys.ajax({
					namespace: "http://schemas/OpenTextDocuSignIntegrator/DocuSignConfiguration/operations",
					method: "CreateDocuSignConfiguration",
					parameters: {
						"DocuSignConfiguration-create": {
							Username: usr,
							Password: btoa(pwd),
							IntegratorKey: key,
							AccessToken: accessToken,
							AccountID: accountId,
							EndPointURL: docuSignURL
						}
					},
					success: function (data) {
						if (data.DocuSignConfiguration !== undefined) {
							mapPropertiesToView(data.DocuSignConfiguration);
							actionItems_Update();

							addToastDiv(getTranslationMessage(SUCCESS_CREATE_CONFIG));
							successToast(3000);
						} else {
							notifyError(getTranslationMessage(ERROR_CREATE_CONFIG), 3000);
							actionItems_Create();
							return false;
						}
					},
					error: function () {
						notifyError(getTranslationMessage(ERROR_CREATE_CONFIG), 3000);
						return false;
					}
				});
			}
		}, error: function (error) {
			notifyError(getTranslationMessage(ERROR_LOAD_CONFIG), 3000);
			return false;
		}
	});
}
function enableUpdate() {
	$('#action-update').attr('disabled', false);
}