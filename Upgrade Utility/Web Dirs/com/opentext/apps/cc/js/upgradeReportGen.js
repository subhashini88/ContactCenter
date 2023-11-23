function importProcess() {
  $.cordys.ajax
    ({
      namespace: "http://schemas.opentext.com/apps/contractcenter/16.3",
      method: "ImportProcess",
      error: function (error) {
        errorToast(3000, getTranslationMessage('Process registration import failed. Import again.'));
      },
      success: function (data) {
        successToast(3000, getTranslationMessage('Process registration imported.'));
      }
    });
}
function downloadUpgradeErrorReport() {
  $.cordys.ajax
    ({
      namespace: "http://schemas.opentext.com/UpgradeUtils/22.3",
      method: "downloadUpgradeErrorReport",
      error: function (error) {
        errorToast(3000, getTranslationMessage('Configurator list import failed. Import again.'));
      },
      success: function (data) {
        if (data && data.tuple && data.tuple.old && data.tuple.old.downloadUpgradeErrorReport &&
          data.tuple.old.downloadUpgradeErrorReport.downloadUpgradeErrorReport && data.tuple.old.downloadUpgradeErrorReport.downloadUpgradeErrorReport === "PROCESS_REGISTRATION_SHOULD_BE_FIRST") {
          errorToast(3000, getTranslationMessage('You must import the process registration first.'));
        } else {
          successToast(3000, getTranslationMessage('Configurator list imported.'));
        }
      }
    });
}

function downloadReport(data) {
  var inputParams = { jobId: data.Id };
  var fileName = "UpgradeErrorReport.xlsx"
  $.cordys.ajax
    ({
      namespace: "http://schemas.opentext.com/UpgradeUtils/22.3",
      method: "DownloadUpgradeErrorReport",
      parameters: inputParams,
      error: function (error) {
        errorToast(3000, getTranslationMessage('Unable to download report. Ensure that the WS-App services are running.'));
      },
      success: function (data) {
        if (data && data.tuple && data.tuple.old && data.tuple.old.downloadUpgradeErrorReport &&
          data.tuple.old.downloadUpgradeErrorReport.downloadUpgradeErrorReport) {
          var output = data.tuple.old.downloadUpgradeErrorReport.downloadUpgradeErrorReport;
          if (output && output === 'NO_REPORT_AVAILABLE') {
            errorToast(3000, getTranslationMessage('You must import before downloading the report.'));
          } else {
            var sampleArr = base64ToArrayBuffer(data.tuple.old.downloadUpgradeErrorReport.downloadUpgradeErrorReport);
            downloadReportIntoLocal(sampleArr, fileName);
          }
        }
      }
    });
}

function downloadReportIntoLocal(data, fileName) {

  var mimeType = "data:application/vnd.ms-excel;base64";
  var fileName = fileName || "report.xlsx";
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