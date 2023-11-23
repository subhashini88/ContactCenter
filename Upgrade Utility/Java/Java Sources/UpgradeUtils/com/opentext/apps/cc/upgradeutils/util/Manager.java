package com.opentext.apps.cc.upgradeutils.util;

import java.util.List;
import java.util.Map;

public interface Manager {
	/**
	 * Returns the list of key-value pairs for each row. 
	 * The readConfiguration argument specify the configuration
	 * that will be used for reading the excel.
	 * <p>
	 * This method prepares a key-value pair for each field in a 
	 * row using a map. A list of such map's will be returned  
	 * and each map corresponds to each row in the excel
	 *   
	 * @param readConfiguration  provides configuration details while reading excel 
	 * @return      the list of key-value pairs for each row	 
	 */
	public List<Map<String,String>>  read(ReadConfiguration readConfiguration);
	
	/**
	 * This method writes data to the excel
	 * file by reading configuration from 
	 * argument writeConfiguration.
	 *   
	 * @writeConfiguration  provides configuration details while writing to excel	 *  
	 */
	public void write(WriteConfiguration writeConfiguration );//can we club persist n write

}
