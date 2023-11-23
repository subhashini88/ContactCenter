$.cordys.json.defaults.removeNamespacePrefix = true;
var document_urls = [];
var contacts = [];
var cInstanceId;
const BUSINESS_WORKSPACE = "BUSINESS_WORKSPACE";
var EnvelopesListModel = function () {
    this.Listed_Envelopes = ko.observableArray([]);
}
var envelopes_list_model = new EnvelopesListModel();

var DocumentsListModel = function () {
    this.repositoryType = ko.observable("");
    this.Listed_Documents = ko.observableArray([]);
    this.breadCrumb = ko.observableArray([]);
    this.document_urls = ko.observableArray([]);
}
var documents_list_model = new DocumentsListModel();
$(document).ready(function () {
    var i_locale = getlocale();
    translateLabels("com/opentext/apps/contractcenter/DocuSignIntegrator/DocuSignIntegrator", i_locale, true);
    var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
    loadRTLIfRequired(i_locale, rtl_css);
    loadDocuments();
    // getAccessToken();
    getContacts();
    ko.applyBindings(documents_list_model, document.getElementById("defaultContainer_documents"));
    ko.applyBindings(envelopes_list_model, document.getElementById('defaultContainer_envelopes'));
    platformDialogModifications("Send");
})
function loadEnvelopes() {
    var ERROR_VALIDATE_TOKEN = "Unable to validate the configurations. Verify the entered details.";
    $.cordys.ajax({
        method: "getEnvelopesStatus",
        namespace: "http://schemas.opentext.com/apps/docusignintegrator/19.2",
        parameters: {
            instance_id: cInstanceId
        }
    }).done(function (data) {

        if (data.Envelopes) {
            if (data.Envelopes.Envelope)
                addEnvelopesToView(data.Envelopes.Envelope, envelopes_list_model);
            if (data.Envelopes.text == "Empty") {
                notifyError(getTranslationMessage(ERROR_VALIDATE_TOKEN), 3000);
                $('#loader').css({ 'display': 'none' })
            }
        }
        else {
            $('#loader').css({ 'display': 'none' })
        }
    }).fail(function (error) {

    });
}

function loadDocuments() {
    var itemID = getUrlParameterValue(
        "instanceId", null, true);
    cInstanceId = getId(itemID);
    getRepositoryType(function () {
        if (!isXECM()) {
            $.cordys.ajax({
                method: "getContractDocumentsByID",
                namespace: "http://schemas/OpenTextContractCenter/Contract.Contents/operations",
                parameters: {
                    ContractID: cInstanceId
                }
            }).done(function (data) {
                loadEnvelopes()
                addDatatoView(data.Contents, documents_list_model);
            }).fail(function (error) { });
        } else {
            // console.log(data);
            $.cordys.ajax({
                method: "getContractBWSDocs",
                namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
                parameters: {
                    "contractId": cInstanceId
                }
            }).done(function (data) {
                loadEnvelopes();
                documents_list_model.breadCrumb.push({ name: "CTR-" + cInstanceId, Id: cInstanceId });
                addDatatoView(data.documentList.Parent.Nodes.Node, documents_list_model);
            }).fail(function (error) { });
        }
    });
}

function isXECM() {
    return (documents_list_model.repositoryType() && documents_list_model.repositoryType() === BUSINESS_WORKSPACE);
}

function getRepositoryType(callBackFunc) {
    if (!documents_list_model.repositoryType()) {
        $.cordys.ajax({
            method: "GetPropertyByName",
            namespace: "http://schemas/OpenTextBasicComponents/GCProperties/operations",
            parameters: {
                Name: "DOCUMENT_REPOSITORY"
            }
        }).done(function (data) {
            var repoType =  (data.GCProperties && data.GCProperties.value===BUSINESS_WORKSPACE)?  BUSINESS_WORKSPACE: "" ;
            documents_list_model.repositoryType(repoType);
            callBackFunc();
        }).fail(function (error) { });
    } else {
        callBackFunc();
    }
}


function platformDialogModifications(okBtnName) {
    /* Sign documents dialog resizing css */
    $("ai-dialog-body iframe", window.parent.document).css({
        'width': '100%',
        'height': '100%'
    });
    $("ai-dialog-body", window.parent.document).css({
        'overflow': 'inherit',
        'height': 'calc(100% - 115px)',
        'padding-bottom': '0px'
    });
    $('ai-dialog[ref="dialogContent"]', window.parent.document).animate({
        'height': '80vh',
        'width': '80vw',
        'transition': 'all 0.9s ease-in-out',
        'transform': 'scale(0.9)'
    }, 500);
    /*create send button and replace with ok button*/
    if ($('ai-dialog-footer .btn-primary:contains("OK")', window.parent.document)[0].innerText == "OK") {
        $('ai-dialog-footer .btn-primary:contains("OK")', window.parent.document).hide();
        $('ai-dialog-footer .btn-primary:contains("OK")', window.parent.document).parent().append("<button class='btn btn-primary proceed'><span class='ui-button-text'>" + okBtnName + "</span></button>");
        $('.proceed', window.parent.document).click(function () {
            proceedToEsign();
        });
        $('.proceed', window.parent.document).attr('disabled', true);
    }

}

function addEnvelopesToView(iEnvelopesList, iModel) {
    if (iEnvelopesList.length != undefined) {
        for (i = 0; i < iEnvelopesList.length; i++) {
            iModel.Listed_Envelopes.push(getEnvelope(iEnvelopesList[i]))
        }
    }
    else {
        iModel.Listed_Envelopes.push(getEnvelope(iEnvelopesList))
    }
    $('#loader').css({ 'display': 'none' })
}
function getEnvelope(iEnvelope) {
    var envelopes = {}
    envelopes.Status = iEnvelope.EnvelopeStatus
    if (envelopes.Status == "") {
        envelopes.Status = "Unknown";
    }
    if (iEnvelope.Documents.GetContainingDocumentsResponse.ContainingDocuments.length != undefined) {
        var Documents = ''
        for (j = 0; j < iEnvelope.Documents.GetContainingDocumentsResponse.ContainingDocuments.length; j++) {
            if (j == iEnvelope.Documents.GetContainingDocumentsResponse.ContainingDocuments.length - 1) {
                Documents += iEnvelope.Documents.GetContainingDocumentsResponse.ContainingDocuments[j].Documentname;
            }
            else {
                Documents += iEnvelope.Documents.GetContainingDocumentsResponse.ContainingDocuments[j].Documentname + "<br/>";
            }
        }
        envelopes.ID = iEnvelope.Documents.GetContainingDocumentsResponse.ContainingDocuments[0].Owner["EnvolopeStatus-id"].Id
        envelopes.Documents = Documents;
    }
    else {
        envelopes.ID = iEnvelope.Documents.GetContainingDocumentsResponse.ContainingDocuments.Owner["EnvolopeStatus-id"].Id
        envelopes.Documents = iEnvelope.Documents.GetContainingDocumentsResponse.ContainingDocuments.Documentname;
    }
    return envelopes;
    // iModel.Listed_Envelopes.push(envelopes);  
}
function addDatatoView(iDocumentList, iModel) {
    var extension;
    iModel.Listed_Documents.removeAll();
    documents = {};
    var documents_count = 0;
    if (iDocumentList && !isXECM()) {
        if(Array.isArray(iDocumentList)){
            for (i = 0; i < iDocumentList.length; i++) {
                    documents = {}
                if (iDocumentList[i].ContentTemplate.ContentType == "FILE") {
                    populateDocData(documents,iDocumentList[i],iModel); 
                    iModel.Listed_Documents.push(documents);
                    documents_count++;
                }
            }
        }else{
                documents = {}
                if (iDocumentList.ContentTemplate.ContentType == "FILE") {
                    populateDocData(documents,iDocumentList, iModel); 
                    iModel.Listed_Documents.push(documents);
                    documents_count++;
                } 
        }
        
    } else if (iDocumentList && isXECM()) {
       if(Array.isArray(iDocumentList)){
        for (i = 0; i < iDocumentList.length; i++) {
            documents = {}
            populateDocData(documents,iDocumentList[i],iModel);
            iModel.Listed_Documents.push(documents);
            documents_count++;
        }
       }else{
            documents = {}
            populateDocData(documents,iDocumentList,iModel);
            iModel.Listed_Documents.push(documents);
            documents_count++;
       }
    }
    if (documents_count > 0)
        $('#loader').css({ 'display': 'none' });
}


function populateDocData(documents,iDocumentList,iModel) {
    if (!isXECM()) {
            documents.Id = iDocumentList["Contents-id"].Id1;
            documents.name = iDocumentList.File.FileName;
            documents.folder_path = JSON.parse(iDocumentList.File.StorageTicket).documentURL;
            documents.lastModified = getLocalDateandTime(iDocumentList.File.LastModified);
            documents.size = getDocumentSize(Math.round(iDocumentList.File.FileSize / 1024))
            documents.src = getImagePathofDocument(documents.name)
    }else if(isXECM()){
            documents.Id = iDocumentList.Id;
            documents.name = iDocumentList.Name;
            documents.folder_path = iModel.breadCrumb().reduce((accumulator, currentValue, index) => index > 0 ? (accumulator + currentValue.name + "/") : "/", "");
            documents.lastModified = null;
            documents.size = null;
            documents.checked = ko.observable(false);
            documents.type = iDocumentList.Type;
            documents.src = getImagePathofDocument(iDocumentList.Type);
            iModel.document_urls().forEach(ele => {
                if (ele.documentid === documents.Id) {
                    documents.checked(true);
                }
            })
    }
    return documents;
}

function goToSelectedFolder(iData) {
    if (iData.type === "Folder") {
        documents_list_model.breadCrumb.push({ name: iData.name, Id: iData.Id });
        documents_list_model.Listed_Documents.removeAll();
        $.cordys.ajax({
            method: "getContractBWSDocs",
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            parameters: {
                "businessWorkspaceId": iData.Id
            }
        }).done(function (data) {
            addDatatoView(data.documentList.Parent.Nodes.Node, documents_list_model);
        }).fail(function (error) { });
    }
}

function breadCrumbSelect(iData, event) {
    console.log(iData);
    var index = documents_list_model.breadCrumb.indexOf(iData);
    var isRootPath = (index === 0);
    if (isRootPath || iData.type === "Folder") {
        documents_list_model.breadCrumb.splice(index + 1, documents_list_model.breadCrumb().length);
        documents_list_model.Listed_Documents.removeAll();
        $.cordys.ajax({
            method: "getContractBWSDocs",
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            parameters: {
                "businessWorkspaceId": isRootPath ? "" : iData.Id,
                "contractId": !isRootPath ? "" : iData.Id
            }
        }).done(function (data) {
            addDatatoView(data.documentList.Parent.Nodes.Node, documents_list_model);
        }).fail(function (error) { });
    }
}

function getLocalDateandTime(lastmodifieddate) {
    var current_date = new Date(lastmodifieddate)
    var date = current_date.toLocaleDateString();
    var time = current_date.toLocaleTimeString();
    return date + ' ' + time;
}
function getDocumentSize(current_size) {
    var size;
    if (current_size > 1024) {
        size = Math.round(current_size / 1024) + ' ' + 'MB';
    }
    else {
        size = current_size + ' ' + 'KB'
    }
    return size;
}
function getImagePathofDocument(documentname) {
    var extension = getExtensionOfFile(documentname);
    var src;
    if (extension == "docx")
        src = "../img/word.svg"
    else if (extension == "pdf")
        src = "../img/pdf.png"
    else if (extension == "Folder")
        src = "../img/folder.svg"
    else if (extension == "png" || extension == "jpeg" || extension == "svg")
        src = "../img/genericimage.png"
    else
        src = "../img/genericdoc.svg"
    return src;
}
function getExtensionOfFile(filename) {
    return filename.split('.').pop();
}
function getId(inputId) {
    var res = inputId.split(".");
    return res[res.length - 1];
}
function checkboxclicked(index, curr_element_checked) {
    docpath = {};
    docpath.folder_path = documents_list_model.Listed_Documents()[index].folder_path;
    docpath.documentid = documents_list_model.Listed_Documents()[index].Id;
    docpath.name = documents_list_model.Listed_Documents()[index].name;
    if (curr_element_checked == true) {
        document_urls.push(docpath)
        $('.proceed', window.parent.document).attr('disabled', false)
    }
    else {
        document_urls.splice(document_urls.indexOf(docpath), 1)
        $('.selectall').prop('checked', false);
        if ($(".doc_cb:checked").length == 0) {
            $('.proceed', window.parent.document).attr('disabled', true)
        }
    }
    return true;
}
function xECMcheckboxclicked(index, curr_element_checked) {
    docpath = {};
    var selectedDocument = documents_list_model.Listed_Documents()[index];
    docpath.folder_path = selectedDocument.folder_path;
    docpath.documentid = selectedDocument.Id;
    docpath.name = selectedDocument.name;
    if (selectedDocument.type !== "Folder" && curr_element_checked == true) {
        documents_list_model.document_urls.push(docpath)
        $('.proceed', window.parent.document).attr('disabled', false)
    }
    else if (selectedDocument.type !== "Folder") {
        documents_list_model.document_urls.splice(documents_list_model.document_urls.indexOf(docpath), 1);
        $('.selectall').prop('checked', false);
        if (documents_list_model.document_urls().length == 0) {
            $('.proceed', window.parent.document).attr('disabled', true)
        }
    }
    return true;
}

function uncheckDocument(iData, event) {
    var removeData = null;
    documents_list_model.Listed_Documents().forEach(ele => {
        if (ele.Id === iData.documentid) {
            removeData = ele;
        }
    });
    removeData ? removeData.checked(false) : "";
    documents_list_model.document_urls.remove(iData);
    if (documents_list_model.document_urls().length == 0) {
        $('.proceed', window.parent.document).attr('disabled', true)
    }
}

function allDocumentsClicked(checked) {
    document_urls = [];
    if (checked == true) {
        $('.doc_cb').prop('checked', true);
        for (i = 0; i < documents_list_model.Listed_Documents().length; i++) {
            docpath = {};
            docpath.folder_path = documents_list_model.Listed_Documents()[i].folder_path;
            docpath.documentid = documents_list_model.Listed_Documents()[i].Id;
            docpath.name = documents_list_model.Listed_Documents()[i].name;
            document_urls.push(docpath)
        }
        $('.proceed', window.parent.document).attr('disabled', false)
    }
    else {
        document_urls = [];
        $('.doc_cb').prop('checked', false);
        $('.proceed', window.parent.document).attr('disabled', true)
    }

    return true;
}
function allxECMDocumentsClicked(checked) {
    if (checked == true) {
        $('.doc_cb').prop('checked', true);
        for (i = 0; i < documents_list_model.Listed_Documents().length; i++) {
            docpath = {};
            docpath.folder_path = documents_list_model.Listed_Documents()[i].folder_path;
            docpath.documentid = documents_list_model.Listed_Documents()[i].Id;
            docpath.name = documents_list_model.Listed_Documents()[i].name;
            if (documents_list_model.Listed_Documents()[i].type !== "Folder") {
                documents_list_model.document_urls.push(docpath)
                documents_list_model.Listed_Documents()[i].checked(true);
            }
        }
        $('.proceed', window.parent.document).attr('disabled', false)
    }
    else {
        documents_list_model.document_urls.removeAll();
        $('.doc_cb').prop('checked', false);
        $('.proceed', window.parent.document).attr('disabled', true)
    }
    return true;
}

function proceedToEsign() {
    $('.proceed', window.parent.document).attr('disabled', true)
    //webservice to get senderViewUrl
    var Documents = [];
    var selectedDocuments = isXECM() ? documents_list_model.document_urls() : document_urls;

    var ERROR_VALIDATE_TOKEN = "Unable to validate the configurations. Verify the entered details.";
    for (i = 0; i < selectedDocuments.length; i++) {
        var document = {};
        document.id = selectedDocuments[i].documentid
        document.path = selectedDocuments[i].folder_path
        Documents.push(document);
    }
    var recipients = [];
    for (i = 0; i < contacts.length; i++) {
        var signers = {};
        signers = contacts[i];
        recipients.push(signers);
    }
    var return_url = window.location.href;
    var itemID = getUrlParameterValue(
        "instanceId", null, true);
    $.cordys.ajax({
        method: "getDocumentStream",
        namespace: "http://schemas.opentext.com/apps/docusignintegrator/19.2",
        parameters: {
            'documents': { 'document': Documents },
            'recepients': { 'signers': recipients },
            'return_url': return_url,
            'item_id': itemID
        }
    }).done(function (data) {

        if (data.ProceedToEsignResponse != undefined) {

            openDocuSign(data.ProceedToEsignResponse.tuple.old.proceedToEsign.proceedToEsign.docusign.senderViewUrl);
            saveDocumentStatus(selectedDocuments, data.ProceedToEsignResponse.tuple.old.proceedToEsign.proceedToEsign.docusign.envelopeId, cInstanceId, "Created")
        }
        else {
            notifyError(getTranslationMessage(ERROR_VALIDATE_TOKEN), 3000);
        }
    }).fail(function (error) {

    });

}

function openDocuSign(url) {
    $('#loader').css({ 'display': 'block' })
    $('[src*="esign.htm"]', window.parent.document).attr('src', url);
    $('.proceed', window.parent.document).hide();
    $('.btn-cancel', window.parent.document).hide();
}
function getContacts() {
    $.cordys.ajax({
        method: "getContactsfromID",
        namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
        parameters: '<ns0:ID xmlns:ns0="http://schemas.cordys.com/default">' + cInstanceId + '</ns0:ID>'
    }).done(function (data) {
        if (data.ExternalContacts) {
            if (data.ExternalContacts.text != undefined && data.ExternalContacts.text != "empty") {
                contacts.push(data.ExternalContacts.text);
            }
        }
        if (data.InternalContacts) {
            if (data.InternalContacts.text != undefined && data.InternalContacts.text != "empty") {
                contacts.push(data.InternalContacts.text);
            }
        }
    }).fail(function (error) {

    });
}
function saveDocumentStatus(documentss, envelopeid, contractid, envelopestatus) {
    var Documents = [];
    for (i = 0; i < documentss.length; i++) {
        Document = {}
        Document.documentid = documentss[i].documentid
        Document.documentname = documentss[i].name
        Documents.push(Document);
    }
    $.cordys.ajax({
        method: "SaveDocumentStatus",
        namespace: "http://schemas.opentext.com/apps/docusignintegrator/19.2",
        parameters: {
            'Documents': { 'Document': Documents },
            'ContractID': contractid,
            'EnvolopeID': envelopeid,
            'EnvolopeStatus': envelopestatus
        }
    }).done(function (data) {
        //(data)
        // addDatatoView(data.Contents,documents_list_model)
    }).fail(function (error) {

    });
}