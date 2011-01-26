package org.blue.star.commands;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.base.blue;
import org.blue.star.base.commands;
import org.blue.star.common.objects;
import org.blue.star.include.blue_h;
import org.blue.star.include.objects_h;
import org.blue.star.xdata.xodcache;

/*
 * A Command that removes a Host from the Blue Montoring setup.
 */

public class delete_host implements ICommand
{
	private static Logger logger = LogManager.getLogger("org.blue.commands.delete_host");
	
	public String getCommandName()
	{
		return "EXECUTE_JAVA_COMMAND";
	}

	public String getCommandString()
	{
		return "EXECUTE_JAVA_COMMAND;org.blue.commands.delete_host;%s";
	}

	/* Method to remove a host */
	public void processCommand(long timestamp, String args)
	{
		if(args == null )
		{
		    logger.info( "No parameters passed to command. Expecting : " + getCommandString());
		    return;
		}
		
		/* Firstly verify that the host exists */
		objects_h.host tempHost = objects.find_host(args);
		
		/* If we don't have a host, we're bailing out */
		if(tempHost == null)
		{
			logger.info("Error: Host '" + args + "' does not exist within current Blue Object list");
			return;
		}
		
		/* try and be as clean as we can here, by stopping host and it's associated services checking first */
		commands.disable_host_checks(tempHost);
		commands.disable_host_service_checks(tempHost);
				
		/* Attempt to remove the host from our object store */
		if(objects.remove_host(args))
		{
			xodcache.cache_objects(blue.macro_x[blue_h.MACRO_OBJECTCACHEFILE]);
			/* Really verify that the host has gone */
			if(objects.find_host(args) == null)
			{
				logger.info("Host successfully removed from Blue");
				return;
			}
		}
		
		logger.info("Error: Unable to remove Host from Blue");
		
	}

}
