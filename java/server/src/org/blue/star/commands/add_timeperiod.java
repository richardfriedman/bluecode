package org.blue.star.commands;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.base.blue;
import org.blue.star.common.objects;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;
import org.blue.star.xdata.xodtemplate;

/*
 * Command for adding timeperiods to the system.
 */

public class add_timeperiod implements ICommand
{
	private static Logger logger = LogManager.getLogger("org.blue.commands.add_timeperiod");
	
	/* Grab command name */
	public String getCommandName() {
		
		return "EXECUTE_JAVA_COMMAND";
	}
	
	/* Grab command String */
	public String getCommandString() {
		return "EXECUTE_JAVA_COMMAND;org.blue.commands.add_timeperiod;%s;%s";		
	}

	/* And process the command */
	public void processCommand(long timestamp, String args)
	{
		if(args == null )
		{
		    logger.info( "No parameters passed to command. Expecting : " + getCommandString());
		    return;
		}

		String[] split = args.split(";");
		
		if(split.length != 2)
		{
		    logger.info( "Incorrect command parameters. Expecting : " + getCommandString() );
		    return;
		}

		String file = split[0].trim();
		String timeperiod = split[1].trim();
		int priorCount = 0;
		
		if(timeperiod.equalsIgnoreCase("all"))
		{
			priorCount = objects.timeperiod_list.size();
		}

		logger.debug( "Processed command " + args );
		
		if(xodtemplate.xodtemplate_read_dynamic_data(blue.config_file,file,common_h.READ_TIMEPERIODS,common_h.TRUE) == common_h.OK)
		{
		    if(!timeperiod.equalsIgnoreCase("all"))
		    {
		    	if(objects.find_timeperiod(timeperiod) != null)
		    	{
		    		logger.info("INFO: Time Period '" +  timeperiod + "' Successfully added!");
		    	}
		    	else
		    	{
		    		logger.info("ERROR: Failed to add Time Period '" + timeperiod + "'");
		    	}
		    }
		    else
		    {
		    	logger.info("INFO: Added '" + (objects.timeperiod_list.size() - priorCount) + "' timeperiods from file '" + file + "'");
		    }
		}
		else
		{
			logger.info("ERROR: Failed To Read Time Period Details from File '" + file + "'");
		}
	}
}