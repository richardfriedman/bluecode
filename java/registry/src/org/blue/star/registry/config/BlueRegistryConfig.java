package org.blue.star.registry.config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.blue.star.registry.exceptions.RegistryConfigurationException;

/**
 * 
 * 	@created 31 Jul 2007 - 09:41:30
 *	@filename org.blue.star.registry.config.BlueRegistryConfig.java
 *  	@author Rob.Blake@arjuna.com
 *	@version 0.1
 *
 *	<p>This class is the File Configuration for the Blue Registry. It will return a BufferedReader to
 *	a local file which contains the configuration for the Blue Registry.</p> 
 *
 */
public class BlueRegistryConfig extends BaseRegistryConfig implements RegistryConfig
{
	/** Logging Variables */
    private String cn = "org.blue.star.registry.config.BlueRegistryConfig";
    
    /* getBufferedReader() method */
    public BufferedReader getBufferedReader(String configLocation) throws RegistryConfigurationException
    {
    	try
    	{
    		return new BufferedReader(new FileReader(configLocation));
    	}
    	catch(FileNotFoundException e)
    	{
    		logger.debug(cn + ".getBufferedReader() - FileNotFoundException trying to open FileReader to file '" + configLocation + "'");
    		throw new RegistryConfigurationException(e.getCause().getMessage(),e);
    	}
    }
}