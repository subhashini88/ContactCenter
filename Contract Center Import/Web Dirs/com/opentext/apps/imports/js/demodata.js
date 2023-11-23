function downloadDemoDataFile(reportType) {
    inputParams = { notificationItem: reportType };
    const fileName = reportType + '.zip';
    $.cordys.ajax
        ({
            namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
            method: "DownloadNotificationImportReport",
            parameters: inputParams,
            error: function (error) {
                errorToast(3000, getTranslationMessage('Unable to download zip file. Ensure that the WS-App services are running.'));
            },
            success: function (data) {
                if (data && data.tuple && data.tuple.old && data.tuple.old.downloadNotificationImportReport &&
                    data.tuple.old.downloadNotificationImportReport.downloadNotificationImportReport) {
                    var sampleArr = base64ToArrayBuffer(data.tuple.old.downloadNotificationImportReport.downloadNotificationImportReport);
                    downloadReportIntoLocal(sampleArr, fileName);
                }
            }
        });
}

function downloadReportIntoLocal(data, fileName) {
    var mimeType = "data:application/vnd.ms-excel;base64";
    var fileName = fileName || "report.xls";
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
};

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
$(function () {
    var i_locale = getlocale();
    var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
    //translateLabels("com/opentext/apps/contractcenter/ContractCenter/ContractCenter", i_locale);
    loadRTLIfRequired(i_locale, rtl_css);
})