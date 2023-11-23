function prepareInputArray(firsttilecount, secondtilecount, thirdtilecount, fourthtilecount, fifthtilecount, sixthtilecount, seventhtilecount, username) {
  const tilesToShow = [
    {
      "name": "DoubleSquareTile",
      "text": [username, "Pending reviews", "Pending assignments"],
      "bgcolor": "#2E3D98",
      "count": [firsttilecount, secondtilecount],
      "icon": "../img/tile_renewal_32.svg"
    },
    {
      "name": "SingleTextTile",
      "text": ["Requests", "Pending requests"],
      "urlToNavigateOnClick": "../../../../../app/start/web/perform/panelLayout/F8B156D5E69DA1E996A3C8583B8264F5/null?force_desktop=true",
      "bgcolor": "#5864AD",
      "count": thirdtilecount,
      "icon": "../img/tile_request_32.svg"
    },
    {
      "name": "SingleTextTile",
      "text": ["My assignments", "Pending assignments"],
      "urlToNavigateOnClick": "../../../../../app/start/web/perform/panelLayout/F8B156D5E69DA1E996A2E0369A98A4F5/null?force_desktop=true",
      "bgcolor": "#067D14",
      "count": fourthtilecount,
      "icon": "../img/tile_assignment_32.svg"
    },
    {
      "name": "SingleTextTile",
      "text": ["Recent contracts", "Recent contracts"],
      "urlToNavigateOnClick": "../../../../../app/start/web/perform/panelLayout/F8B156D5E69DA1E996A2E0369A8CE4F5/null?force_desktop=true",
      "bgcolor": "#0D1442",
      "count": fifthtilecount,
      "icon": "../img/tile_recent_contracts_32.svg"
    },
    {
      "name": "SingleTextTile",
      "text": ["Renewals", "Pending renewals"],
      "urlToNavigateOnClick": "../../../../../app/start/web/perform/panelLayout/F8B156D5E69DA1E996A488A4387BA4F5/null?force_desktop=true",
      "bgcolor": "#780050",
      "count": sixthtilecount,
      "icon": '../img/tile_renewal_32.svg'
    },
    {
      "name": "SingleTextTile",
      "text": ["All contracts", "Contracts"],
      "urlToNavigateOnClick": "../../../../../app/start/web/perform/panelLayout/F8B156D5E69DA1E996A7198A7913E4F5/null?force_desktop=true",
      "bgcolor": "#414979",
      "count": seventhtilecount,
      "icon": '../img/tile_all_contracts_32.svg'
    }
  ]
  createTiles(tilesToShow);
}
$(document).ready(function () {

  var i_locale = getlocale();
  var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
  translateLabels("com/opentext/apps/cc/htm/ContractAuthorDashboard", i_locale);
  loadRTLIfRequired(i_locale, rtl_css);
  if (window.parent.parent) {
      contractAuthorDashboard = $('[src*="ContractAuthorDashboard.htm"]', window.parent.parent.document);
      if (contractAuthorDashboard) {
        contractAuthorDashboard.css('border', 'none');
      }
  }

  //  createTiles(tilesToShow);
  var username = getUrlParameterValue("username", null, true);
  getData('getPendRevForConAuthor', 'http://schemas.opentext.com/apps/contractcenter/16.3').done(function (data) {
    var firsttilecount;
    if (data.ctrCount.text != undefined)
      firsttilecount = data.ctrCount.text;
    else
      firsttilecount = 0;
    getData('getPendingAssignmentsCount', 'http://schemas.opentext.com/apps/contractcenter/16.3').done(function (data2) {
      var secondtilecount, fourthtilecount;
      if (data2.count.text != undefined) {
        secondtilecount = data2.count.text;
        fourthtilecount = data2.count.text;
      }
      else {
        secondtilecount = 0;
        fourthtilecount = 0;
      }
      getData('getApprovedRequestsCount', 'http://schemas.opentext.com/apps/contractinitiation/19.2').done(function (data3) {
        var thirdtilecount;
        if (data3.count.text != undefined)
          thirdtilecount = data3.count.text;
        else
          thirdtilecount = 0;
        getData('getAllRecentContractsCount', 'http://schemas.opentext.com/apps/contractcenter/16.3').done(function (data5) {
          var fifthtilecount;
          if (data5.count.text != undefined)
            fifthtilecount = data5.count.text;
          else
            fifthtilecount = 0;
          getData('getAllContractsCount', 'http://schemas.opentext.com/apps/contractcenter/16.3').done(function (data7) {
            var seventhtilecount;
            if (data7.count.text != undefined)
              seventhtilecount = data7.count.text;
            else
              seventhtilecount = 0;
            getData('getPendingRenewalsCount', 'http://schemas.opentext.com/apps/contractcenter/16.3').done(function (data6) {
              var sixthtilecount;
              if (data6.count.text != undefined)
                sixthtilecount = data6.count.text;
              else
                sixthtilecount = 0;
              prepareInputArray(firsttilecount, secondtilecount, thirdtilecount, fourthtilecount, fifthtilecount, sixthtilecount, seventhtilecount, username);
            })
          })
        })
      })
    })
  });
});

frameElement.ownerDocument.frameReloaded = function (iFrameElement) {
  var lStyleContent = '.layout-zone, .emp-viewport{padding:0px !important}';
  var lDoc = iFrameElement.contentWindow.document;
  var lHead = lDoc.head || lDoc.getElementsByTagName('head')[0];
  var lStyleElem = lDoc.createElement('style');
  lStyleElem.type = 'text/css';
  if (lStyleElem.styleSheet) {
    lStyleElem.styleSheet.cssText = lStyleContent;
  }
  else {
    lStyleElem.appendChild(lDoc.createTextNode(lStyleContent));
  }
  lHead.appendChild(lStyleElem);
}
frameElement.setAttribute("onload", "this.ownerDocument.frameReloaded(this)");



