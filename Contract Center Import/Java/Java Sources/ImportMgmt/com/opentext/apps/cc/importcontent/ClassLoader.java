package com.opentext.apps.cc.importcontent;

import com.cordys.cpc.bsf.busobject.exception.WSAppServerException;
import com.cordys.cpc.bsf.cache.CachedReflection;
import com.eibus.util.logger.CordysLogger;
import com.opentext.apps.cc.custom.Utilities;

public class ClassLoader 
{
	
	private static final CordysLogger LOGGER = CordysLogger.getCordysLogger(ClassLoader.class);	
	/*
	 * 
	 * @param className : Name of the class to be initiated 
	 * 
	 * returns the instance of the class
	 * 
	 */ 
    public static Object initiateClass(String className)
    {
    	Object object = null;	
    	try
    	{
    		if(!Utilities.isStringEmpty(className))
    		{
    			Class<?> c = CachedReflection.getInstance().getClassObject(className).getKlas();
   	    	 	object =  c.newInstance();
    		}
    		 
    	    return object;
    	}
    
    	catch(WSAppServerException | InstantiationException | IllegalAccessException e)
    	{   	    		
	  			LOGGER.error("Error loading class "+className);
	  			return null;
    	}
    

    }
    
    /*
	 * 
	 * @param className : Name of the class to be initiated 
	 * 
	 * @param loader : 	loader context in which the class needs to be loaded
	 * 
	 * returns the instance of the class
	 * 
	 */
    
    public static Object initiateClassWithLoader(String className, java.lang.ClassLoader loader)
    {	
    	Object classObject = null;
		try 
		{
			classObject = (loader.loadClass(className)).newInstance();
		}
		catch(InstantiationException |  ClassNotFoundException | IllegalAccessException e)
    	{   	    		
  			LOGGER.error("Error loading class "+className);
  			return null;
	    	 
    	}
		return classObject;
    }

}
