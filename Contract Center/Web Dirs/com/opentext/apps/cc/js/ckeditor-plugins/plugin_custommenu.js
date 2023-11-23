'use strict';
CKEDITOR.plugins.cc_custom_menu = {
    requires: 'menu,menubutton',
    icons: 'dropdown',
    init: initializeList
}

var menuLoadingFirstTime = true;
function initializeList(editor) {
    var configuration = editor.config.ccmenu;
    var menuArray = [];
    if (configuration) {
        for (var menuItem in configuration) {
            if (menuItem && configuration.hasOwnProperty(menuItem)) {
                var ccMenuItem = createCCMenuObj(menuItem, configuration);
                if (ccMenuItem) {
                    menuArray.push(ccMenuItem);
                    editor.addMenuGroup(ccMenuItem.name);
                    editor.addMenuItems(ccMenuItem.options);
                    if (ccMenuItem.label && ccMenuItem.visible) {
                        CKEDITOR.addCss('.cke_button__' + ccMenuItem.name.toLowerCase() +
                            '_label{display: inline !important;overflow:hidden;width:' + ccMenuItem.label + 'px;}');
                    }
                }


            }
        }
    }

    for (var i = 0; i < menuArray.length; i++) {
        if (menuArray[i].options) {
            for (var option in menuArray[i].options) {
                if (menuArray[i].options.hasOwnProperty(option) &&
                    menuArray[i].options[option].labelPromise) {
                    menuArray[i].options[option].labelPromise().then(function (label) {
                        editor.getMenuItem(menuArray[i].options[option].name).label = label;
                    });
                }
            }
        }
        addToCKEdiorMenu(menuArray[i],editor);
    }



}

var CCCustomMenu = function () {
    var self = this;

    self.name = '';
    self.label = '';
    self.icon = 'dropdown';
    self.width = 0;
    self.visible = false;

    self.options = {};

    self.addItem = function (option) {
        option['group'] = self.name;
        option['role'] = 'menuitemcheckbox';
        if (typeof option['label'] !== 'string') {
            option['labelPromise'] = option['label'];
            option['label'] = option['name'];
        }

        self.options[option['name']] = option;
    };
}

function createCCMenuObj(menuItemName, ccMenuConfig) {
    if (ccMenuConfig) {
        var ccCustomMenuItem = new CCCustomMenu();
        ccCustomMenuItem.name = menuItemName;
        if (ccMenuConfig[menuItemName].label) {
            ccCustomMenuItem.label = ccMenuConfig[menuItemName].label.text;
            ccCustomMenuItem.width = ccMenuConfig[menuItemName].label.width;
            ccCustomMenuItem.visible = ccMenuConfig[menuItemName].label.visible;
        }
        ccCustomMenuItem.icon = ccMenuConfig[menuItemName].icon;
        if (ccMenuConfig[menuItemName].items) {
            for (var i = 0; i < ccMenuConfig[menuItemName].items.length; i++) {
                ccCustomMenuItem.addItem(ccMenuConfig[menuItemName].items[i]);
            }
        }
        return ccCustomMenuItem;
    }
}

function addToCKEdiorMenu(menuItem,editor) {
    editor.ui.add(menuItem.name, CKEDITOR.UI_MENUBUTTON, {
        label: menuItem.label,
        icon: menuItem.icon,
        name: menuItem.name,
        onMenu: function () {
            var active = {};
            if (menuItem.options) {
                for (var option in menuItem.options) {
                    active[option] = editor.getCommand(menuItem.options[option].command).state;                                     
                }
            }        
            if (menuLoadingFirstTime) {
                setTimeout(function () {
                    $("iframe[id^='cke_'][id$='_frame']").contents().find("head")
                        .append($('<link rel="stylesheet" type="text/css" href="../css/cke_custom_menu.css">')
                        );
                }, 100);
                menuLoadingFirstTime = false;
            }
            return active;
        }
    });
}

CKEDITOR.plugins.add('custommenu', CKEDITOR.plugins.cc_custom_menu);