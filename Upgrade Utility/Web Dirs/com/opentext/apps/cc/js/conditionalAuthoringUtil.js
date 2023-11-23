
var cc_conditionauth_util = (function () {
    var self = {};
    //returns map of (instid,true/false) true is visible, false is hide
    self.processConditionalCont = function (instanceListMap, ruleresultMap) {
        var resultInstanceMap = new Map();
        instanceListMap.forEach((instance, instId) => {
            if (!instance.action) {
                resultInstanceMap.set(instId, true);
            } else if (instance.action === 'SHOW' && !(ruleresultMap.get(instance.ruleId) === 'true')) {
                resultInstanceMap.set(instId, false);
            } else if (instance.action === 'HIDE' && (ruleresultMap.get(instance.ruleId) === 'true')) {
                resultInstanceMap.set(instId, false);
            } else {
                resultInstanceMap.set(instId, true);
            }
        });
        return resultInstanceMap;
    };
    return self;
})();
