
function createSquareTiles(textArray, backgroundcolor, count) {
   var tilediv = document.createElement("div");
   var child_div1 = "<span style='font-size :25px;color: white;display: block;position:relative;top:8px;'>" + getTranslationMessage("Welcome") + ', ' + getTranslationMessage(textArray[0]) + " </span><div class='col-md-12' style='margin-top:8px;padding:0px'> <div id='welcome_1' class='col-md-6' style='padding:0px;margin-top:40px;padding-right:1px;'>   <div class='subtile'>  <span style=' color: white;font-size: 54px;'>" + count[0] + "</span><br><span style='color:white;font-size:18px;'>" + getTranslationMessage(textArray[1]) + "</br></span> </div></div> "
   var child_div2 = " <div class='col-md-6' id='welcome_2' style='padding:0px;padding-left:1px;margin-top:40px;'><div  class='substile'><span style='font-size: 54px;color: white;'>" + count[1] + " </span><br><span style='font-size: 18px;color: white;'>" + getTranslationMessage(textArray[2]) + " </span></div></div></div></div>";
   tilediv.innerHTML = child_div1 + child_div2;
   tilediv.className = "tile";
   tilediv.style.backgroundColor = backgroundcolor;
   return tilediv;
}
function createSearchTile(backgroundColor, icon) {
   var tilediv = document.createElement("div");
   var secondtile = document.createElement("div");
   secondtile.className = "author-dashboard-panel-heading panel-heading"
   secondtile.style.textAlign = "left"
   secondtile.style.fontSize = '22px';
   secondtile.style.color = 'white'
   var imgelement = document.createElement('img');
   imgelement.src = icon;
   secondtile.appendChild(imgelement)
   var spanelement = document.createElement('span')
   spanelement.innerHTML = getTranslationMessage("Search documents")
   spanelement.style.marginLeft = '8px'
   spanelement.style.position = 'absolute'
   secondtile.appendChild(spanelement)
   var panelbody = "<div class='panel-body' style='padding-top:40px'>  <div class='form-group has-feedback'><input type='text' class='form-control' id='inputValidation' placeholder='" + getTranslationMessage("Search") + "'/><span class='glyphicon glyphicon-search form-control-feedback'></span></div></div></div>"
   //tilediv.innerHTML=secondtile;
   tilediv.className = "tile panel"
   tilediv.style.borderRadius = '0px'
   tilediv.style.backgroundColor = backgroundColor;
   tilediv.appendChild(secondtile)
   tilediv.innerHTML += panelbody;
   return tilediv;
}
function createSingleTextTiles(textArray, navigationlink, backgroundcolor, count, icon) {
   var tilediv = document.createElement("div");
   var secondtile = document.createElement("div");
   secondtile.className = "author-dashboard-panel-heading panel-heading"
   secondtile.style.textAlign = "left"
   secondtile.style.fontSize = '22px';
   secondtile.style.color = 'white'
   var imgelement = document.createElement('img');
   imgelement.src = icon;
   secondtile.appendChild(imgelement)
   var spanelement = document.createElement('span')
   spanelement.innerHTML = getTranslationMessage(textArray[0])
   spanelement.style.marginLeft = '8px'
   spanelement.style.position = 'absolute'
   secondtile.appendChild(spanelement)
   var panelbody = "<div class='panel-body' style='padding-top:40px'><span style='font-size:62px;color:white;font-weight:600'>" + count + "</span><br><span style='color:white;font-size:18px;'>" + getTranslationMessage(textArray[1]) + "</span> </div></div>"
   //tilediv.innerHTML=secondtile;
   tilediv.className = "tile panel"
   tilediv.style.backgroundColor = backgroundcolor;
   tilediv.style.borderRadius = '0px';
   tilediv.appendChild(secondtile)
   tilediv.innerHTML += panelbody;
   //tilediv.click=navigateToList(navigationlink);
   $(tilediv).click(function () {


      navigatetoURL(navigationlink)
   })
   return tilediv;
}
function createTiles(inputTilesArray) {
   document.body.style.background = 'linear-gradient(42.5deg, #0E122B 13.95%, #0F142F 26.75%, #131939 36.86%, #19224A 46.06%, #222E61 54.65%, #243167 56.44%, #23346A 63.97%, #203E72 69.86%, #1B4F7F 75.2%, #146693 80.2%, #0B84AB 83.33%, #088CB2 86.05%)';
   var containerdivelem = document.createElement('div');
   containerdivelem.className = "container-fluid";
   containerdivelem.style.marginRight = '5%';
   containerdivelem.style.marginLeft = '5%';
   containerdivelem.style.marginTop = '5%';
   var divinnerHTML;
   var resultHTML = '';
   for (i = 0; i < inputTilesArray.length; i++) {
      var tileposdiv = document.createElement('div');
      tileposdiv.className = "author-dashboard-col-md-3 col-md-3";
      tileposdiv.style.padding = '0px';
      tileposdiv.style.minWidth = '260px';
      tileposdiv.style.cursor = 'pointer';
      divinnerHTML = '';
      if (inputTilesArray[i].name == "SingleTextTile")
         divinnerHTML = createSingleTextTiles(inputTilesArray[i].text, inputTilesArray[i].urlToNavigateOnClick, inputTilesArray[i].bgcolor, inputTilesArray[i].count, inputTilesArray[i].icon);
      if (inputTilesArray[i].name == "DoubleSquareTile")
         divinnerHTML = createSquareTiles(inputTilesArray[i].text, inputTilesArray[i].bgcolor, inputTilesArray[i].count, inputTilesArray[i].icon);
      if (inputTilesArray[i].name == "SearchTile")
         divinnerHTML = createSearchTile(inputTilesArray[i].bgcolor, inputTilesArray[i].icon);
      tileposdiv.appendChild(divinnerHTML);
      containerdivelem.appendChild(tileposdiv)
   }
   document.body.appendChild(containerdivelem);
   var i_locale = getlocale();
   var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
   loadRTLIfRequired(i_locale, rtl_css);
}

function getData(methodname, namespace) {
   var fetch = "../../../../../app/entityRestService/Elements(5c58c3f47c463e41958dfd1302689c4e.90B11C8A394711E5F7F056DFE990BFCB)/ResultItems?include=PropDescs,Elements,Rules&language=en-US&finals_ct=7605b9c7afa89dc59f6a6e49ee1e180d90677ac9"
   return $.cordys.ajax(
      {
         method: methodname,
         namespace: namespace
      });
}
function navigatetoURL(navigationlink) {
   window.open(navigationlink, '_self');
}