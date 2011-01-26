package org.blue.star.registry;

import java.io.File;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * This class is used to deal with any shutdowns that are encountered by the
 * Blue Dynamic Registry.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public class BlueDynamicRegistryShutdownHandler extends Thread
{
	/** The Registry that we are to handle shutdowns for */
	private BlueDynamicRegistry r;
	
	/** Logging Variables */
	private static Logger logger = LogManager.getLogger("org.blue.star.registry.BlueDynamicRegistryShutdownHandler");
	private String cn = "org.blue.star.registry.BlueDynamicRegistryShutdownHandler";
	
	/**
	 * Constructor that takes a registry as a parameter.
	 * @param r - The Registry we are to handle shutdowns for.
	 */
	public BlueDynamicRegistryShutdownHandler(BlueDynamicRegistry r)
	{
		this.r = r;
	}
	
	/* run() method */
	public void run()
	{
		logger.info("Blue Dynamic Registry: Shutdown Signal Received.");
		
		File file = new File(this.r.getTempDirectory());
		
		for(String s: file.list())
		{
			try
			{
				if(s.endsWith(".cfg"))
				{
					new File(s).delete();
				}
			}
			catch(SecurityException e)
			{
				logger.debug(cn + ".run() - Cannot delete temporary file '" + s + "'");
			}
		}
	}
}
