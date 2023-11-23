
// Models.
var ExternalContactsListModel = function () {
	var self = this;
	self.ExternalContactsList = ko.observableArray([]);
	self.currentPage = ko.observable(1);
	self.externalContactsCount = ko.observable('');
	self.checkedExtContactNames = ko.observable('');
	self.onCheckboxValueChanged = function (iItem, event) {
		var l_checked = event.currentTarget.checked;
		if (l_checked) {
			checkedExternalcontacts[getTextValue(iItem.ContainingPerson.User_ID)] = getTextValue(iItem.ContainingPerson.DisplayName);
			$('ai-dialog-footer .btn-primary:contains("Ok")', window.parent.parent.document).attr("disabled", false);
		}
		else {
			delete checkedExternalcontacts[getTextValue(iItem.ContainingPerson.User_ID)];
			if (Object.keys(checkedExternalcontacts).length == 0) {
				$('ai-dialog-footer .btn-primary:contains("Ok")', window.parent.parent.document).attr("disabled", true);
			}
		}
		if ($('.ExtContact_Checkbox:checked').length == $('.ExtContact_Checkbox').length) {
			$('#selectAll').prop('checked', true);
		}
		var temp_checkedExtContactNames = "";
		Object.keys(checkedExternalcontacts).forEach(function (key) {
			if (checkedExternalcontacts[key] != "") {
				temp_checkedExtContactNames = temp_checkedExtContactNames + checkedExternalcontacts[key] + ", ";
			}
		});
		l_externalContacts_model.checkedExtContactNames(temp_checkedExtContactNames);
	}
	self.onCheckAllValueChanged = function (iItem, event) {
		var l_checked = event.currentTarget.checked;
		if (l_checked) {
			iItem.ExternalContactsList().forEach(function (iToken) {
				if (!sharedExternalcontacts[getTextValue(iToken.ContainingPerson.User_ID)]) {
					checkedExternalcontacts[getTextValue(iToken.ContainingPerson.User_ID)] = getTextValue(iToken.ContainingPerson.DisplayName);
				}
			});
			$('ai-dialog-footer .btn-primary:contains("Ok")', window.parent.parent.document).attr("disabled", false);
		}
		else {
			iItem.ExternalContactsList().forEach(function (iToken) {
				if (!sharedExternalcontacts[getTextValue(iToken.ContainingPerson.User_ID)]) {
					delete checkedExternalcontacts[getTextValue(iToken.ContainingPerson.User_ID)];
				}
			});
			if (Object.keys(checkedExternalcontacts).length == 0) {
				$('ai-dialog-footer .btn-primary:contains("Ok")', window.parent.parent.document).attr("disabled", true);
			}
		}
		var temp_checkedExtContactNames = "";
		Object.keys(checkedExternalcontacts).forEach(function (key) {
			if (checkedExternalcontacts[key] != "") {
				temp_checkedExtContactNames = temp_checkedExtContactNames + checkedExternalcontacts[key] + ", ";
			}
		});
		l_externalContacts_model.checkedExtContactNames(temp_checkedExtContactNames);
	}
}

// Model variables.
var l_externalContacts_model = new ExternalContactsListModel();


// Methods.

// attaching events to the page.
function attachEventstoList() {
	$('#img_SearchContacts').click(function () {
		l_externalContacts_model.currentPage('1');
		extContactsOffsetValue = 0;
		$('#ext_contacts_decrementer').css('display', 'none');
		$('#ext_contacts_incrementer').css('display', 'inline');
		getExternalContactsCount();
	});
	$('#id_searchExtContactsInput').keypress(function (e) {
		var key = e.which;
		if (key == 13)  // the enter key code
		{
			l_externalContacts_model.currentPage('1');
			extContactsOffsetValue = 0;
			$('#ext_contacts_decrementer').css('display', 'none');
			$('#ext_contacts_incrementer').css('display', 'inline');
			getExternalContactsCount();
			return false;
		}
	});
}

// clear external contacts filter search data.
function clearExtContactsData() {
	offsetValue = 0;
	l_externalContacts_model.currentPage(1);
	extContactsOffsetValue = 0;
	$("#id_searchExtContactsInput").val("");
	var tableid = $('#id_externalContactsList').parent("table").prop("id");
	$('#' + tableid + ' input[type=checkbox]:checked').removeAttr('checked');
	$('#selectAll').checked = false;
}

// Select all.
function checkAll(ele) {
	if (ele.checked) {
		$('.ExtContact_Checkbox').prop('checked', true);
	}
	else {
		$('.ExtContact_Checkbox').prop('checked', false);
	}
	$('.ExtContact_Checkbox').change(function () {
		$('#selectAll')[0].checked = false;
	});
}

// Form data in a presentable way from read service.
function addDatatoExtContactsView(iElementList, iModel) {
	if (l_shared_users_model.usersList()) {
		if (l_shared_users_model.usersList().length) {
			l_shared_users_model.usersList().forEach(function (iToken) {
				sharedExternalcontacts[getTextValue(iToken.userid())] = getTextValue(iToken.username());
			});
		}
		else {
			sharedExternalcontacts[getTextValue(l_shared_users_model.usersList().userid())] = getTextValue(l_shared_users_model.usersList().username());
		}
	}
	iModel.ExternalContactsList.removeAll();
	if (iElementList) {
		if (iElementList.length) {
			iElementList.forEach(function (iElement) {
				iElement.isSharedAlready = ko.computed(function () {
					return (sharedExternalcontacts[getTextValue(iElement.ContainingPerson.User_ID)]) ? true : false;
				});
				iElement.isSelected = ko.computed(function () {
					return (sharedExternalcontacts[getTextValue(iElement.ContainingPerson.User_ID)]) ? true : false;
				});
				if (!iElement.isSelected()) {
					iElement.isSelected = ko.computed(function () {
						return (checkedExternalcontacts[getTextValue(iElement.ContainingPerson.User_ID)]) ? true : false;
					});
				}
				iModel.ExternalContactsList.push(iElement);
			});
		}
		else {
			iElementList.isSharedAlready = ko.computed(function () {
				return (sharedExternalcontacts[getTextValue(iElementList.ContainingPerson.User_ID)]) ? true : false;
			});
			iElementList.isSelected = ko.computed(function () {
				return (sharedExternalcontacts[getTextValue(iElementList.ContainingPerson.User_ID)]) ? true : false;
			});
			if (!iElementList.isSelected()) {
				iElementList.isSelected = ko.computed(function () {
					return (checkedExternalcontacts[getTextValue(iElementList.ContainingPerson.User_ID)]) ? true : false;
				});
			}
			iModel.ExternalContactsList.push(iElementList);
		}
	}
	if ($('.ExtContact_Checkbox:checked').length == $('.ExtContact_Checkbox').length) {
		$('#id_externalContacts #selectAll').prop('checked', true);
	}
	else {
		$('#id_externalContacts #selectAll').prop('checked', false);
	}
	$("#id_externalContactsList [isSharedAlready=true]").attr("disabled", true);
	$("#id_externalContactsList [isSharedAlready=true]").css("background-color", "#ccc");
	if ($("#id_externalContactsList input[isSharedAlready=true]").length == $('.ExtContact_Checkbox').length) {
		$('#id_externalContacts #selectAll').attr("disabled", true);
	}
	else {
		$('#id_externalContacts #selectAll').attr("disabled", false);
	}
}

// Next page in a pagination.
function ext_contacts_incrementOffsetLimit() {
	if (l_externalContacts_model.currentPage() < Math.ceil(l_externalContacts_model.externalContactsCount() / 5)) {
		extContactsOffsetValue = extContactsOffsetValue + 5;
		l_externalContacts_model.currentPage(isNaN(parseInt(l_externalContacts_model.currentPage())) ? 0 : parseInt(l_externalContacts_model.currentPage()));
		l_externalContacts_model.currentPage(parseInt(l_externalContacts_model.currentPage()) + 1);
	}
	if (l_externalContacts_model.currentPage() == Math.ceil(l_externalContacts_model.externalContactsCount() / 5)) {
		document.getElementById("ext_contacts_incrementer").style.display = "none";
	}
	if (l_externalContacts_model.currentPage() > 1) {
		document.getElementById("ext_contacts_decrementer").style.removeProperty("display");
	}
	var temp_searchElement = "";
	if ($('#id_searchExtContactsInput').val()) {
		temp_searchElement = $('#id_searchExtContactsInput').val();
	}
	getExternalContactsCount(temp_searchElement);
}

// Previous page in a pagination.
function ext_contacts_decrementOffsetLimit() {
	if (l_externalContacts_model.currentPage() > 1) {
		extContactsOffsetValue = extContactsOffsetValue - 5;
		l_externalContacts_model.currentPage(parseInt(l_externalContacts_model.currentPage()) - 1);
	}
	if (l_externalContacts_model.currentPage() < Math.ceil(l_externalContacts_model.externalContactsCount() / 5)) {
		document.getElementById("ext_contacts_incrementer").style.removeProperty("display");
	}
	if (l_externalContacts_model.currentPage() == 1) {
		document.getElementById("ext_contacts_decrementer").style.display = "none";
	}
	if (l_externalContacts_model.currentPage() < 1)
		return;
	var temp_searchElement = "";
	if ($('#id_searchExtContactsInput').val()) {
		temp_searchElement = $('#id_searchExtContactsInput').val();
	}
	getExternalContactsCount(temp_searchElement);
}

// Last page in a pagination.
function ext_contacts_incrementToLast() {
	ext_contacts_is_last_page_clicked = 1;
	getExternalContactsCount();
}

// First page in a pagination.
function ext_contacts_decrementToLast() {
	ext_contacts_is_first_page_clicked = 1;
	getExternalContactsCount()
}

// Services.

// Get external contacts count.
function getExternalContactsCount() {
	var temp_searchElement = "";
	if ($('#id_searchExtContactsInput').val()) {
		temp_searchElement = $('#id_searchExtContactsInput').val();
	}
	$.cordys.ajax({
		method: "GetAllUserIDContactsforaCTR",
		namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
		parameters: {
			"contractID": contractItemId.substring(contractItemId.indexOf(".") + 1),
			"userIDSearch": temp_searchElement,
			"Offset": extContactsOffsetValue,
			"limit": extContactsLimitValue
		},
	}).done(function (data) {
		if (data.FindZ_INT_ExternalContactsListResponse['@total'] != undefined) {
			l_externalContacts_model.externalContactsCount(data.FindZ_INT_ExternalContactsListResponse['@total']);
			attachEventstoList();
			if (l_externalContacts_model.currentPage() == 1) {
				document.getElementById("ext_contacts_decrementer").style.display = "none";
			}
		}
		$('#selectAll')[0].checked = false;
		if (l_externalContacts_model.currentPage() == 1) {
			document.getElementById("ext_contacts_decrementer").style.display = "none";
			document.getElementById("ext_contacts_incrementer").style.display = "inline";
		}
		if (l_externalContacts_model.externalContactsCount() <= 5) {
			l_externalContacts_model.currentPage('1');
			$('#ext_contacts_decrementer,#ext_contacts_incrementer').css('display', 'none');
		}
		if (ext_contacts_is_first_page_clicked) {
			extContactsOffsetValue = 0;
			l_externalContacts_model.currentPage('1');
			ext_contacts_is_first_page_clicked = 1;
			$('#ext_contacts_incrementer').css('display', 'inline');
			$('#ext_contacts_decrementer').css('display', 'none');
			ext_contacts_is_first_page_clicked = 0;
		}
		if (ext_contacts_is_last_page_clicked) {
			extContactsOffsetValue = (Math.ceil(l_externalContacts_model.externalContactsCount() / 5) - 1) * 5;
			l_externalContacts_model.currentPage(Math.ceil(l_externalContacts_model.externalContactsCount() / 5));
			ext_contacts_is_last_page_clicked = 0;
			$('#ext_contacts_incrementer').css('display', 'none');
			$('#ext_contacts_decrementer').css('display', 'inline');
			ext_contacts_is_last_page_clicked = 0;
		}
		addDatatoExtContactsView(data.FindZ_INT_ExternalContactsListResponse.RelatedContacts, l_externalContacts_model);
		$('#cc-externalContacts-loadmsg').hide();
		$('#id_externalContactsDiv').show();
		//loadExternalPartyContacts(temp_searchElement);
	}).fail(function (error) {
	});
}

// Load external contacts based on search.
function loadExternalPartyContacts(l_searchElement) {
	$('#selectAll')[0].checked = false;
	if (l_externalContacts_model.currentPage() == 1) {
		document.getElementById("ext_contacts_decrementer").style.display = "none";
		document.getElementById("ext_contacts_incrementer").style.display = "inline";
	}
	if (l_externalContacts_model.externalContactsCount() <= 5) {
		l_externalContacts_model.currentPage('1');
		$('#ext_contacts_decrementer,#ext_contacts_incrementer').css('display', 'none');
	}
	if (ext_contacts_is_first_page_clicked) {
		extContactsOffsetValue = 0;
		l_externalContacts_model.currentPage('1');
		ext_contacts_is_first_page_clicked = 1;
		$('#ext_contacts_incrementer').css('display', 'inline');
		$('#ext_contacts_decrementer').css('display', 'none');
		ext_contacts_is_first_page_clicked = 0;
	}
	if (ext_contacts_is_last_page_clicked) {
		extContactsOffsetValue = (Math.ceil(l_externalContacts_model.externalContactsCount() / 5) - 1) * 5;
		l_externalContacts_model.currentPage(Math.ceil(l_externalContacts_model.externalContactsCount() / 5));
		ext_contacts_is_last_page_clicked = 0;
		$('#ext_contacts_incrementer').css('display', 'none');
		$('#ext_contacts_decrementer').css('display', 'inline');
		ext_contacts_is_last_page_clicked = 0;
	}
	ExternalContactsListModel = $.cordys.ajax(
		{
			method: "GetExternalContactsbyPartyItemID",
			namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
			parameters: {
				"PartyItemID": externalPartyItemID.substring(externalPartyItemID.indexOf(".") + 1),
				"Cursor":
				{
					"Offset": extContactsOffsetValue,
					"limit": extContactsLimitValue
				},
				"searchElement": l_searchElement
			},
			success: function (responseSuccess) {
				if (responseSuccess) {
					addDatatoExtContactsView(responseSuccess.ExternalContacts.FindZ_INT_ExternalContactsListResponse.RelatedContacts, l_externalContacts_model);
					$('#cc-externalContacts-loadmsg').hide();
					$('#id_externalContactsDiv').show();
				} else {
					notifyError(getTranslationMessage("An error occured while retriving external contacts"), 10000);
				}
			},
			error: function (responseFailure) {
				notifyError(getTranslationMessage("An error occured while retriving external contacts"), 10000);
				return false;
			}
		});
}
