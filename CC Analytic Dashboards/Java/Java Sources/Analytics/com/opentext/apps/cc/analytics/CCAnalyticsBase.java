/*
  This class has been generated by the Code Generator
*/

package com.opentext.apps.cc.analytics;

import com.cordys.cpc.bsf.busobject.BusObjectConfig;
import com.cordys.cpc.bsf.classinfo.ClassInfo;


public abstract class CCAnalyticsBase extends com.cordys.cpc.bsf.busobject.CustomBusObject
{
    // tags used in the XML document
    private static ClassInfo s_classInfo = null;
    public static ClassInfo _getClassInfo()//NOPMD framework ensures this is thread safe
    {
        if ( s_classInfo == null )//NOPMD
        {
            s_classInfo = newClassInfo(CCAnalytics.class);
            s_classInfo.setUIDElements(new String[]{});
        }
        return s_classInfo;
    }

    public CCAnalyticsBase(BusObjectConfig config)
    {
        super(config);
    }






}
